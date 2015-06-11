#Backend Documentation

As BaaS-Provider we decided to work with Parse (https://www.parse.com/ Application ID: XtayL1TkW4GF7OyZPikWIRvdWlr4LrnOs1OjsFCO). It offers user-administration, object-oriented database services and the possibility to submit own cloud-code written in javascript. The following lines will describe how and for which services we used Parse.

#Database

This is a simple description of all the tables with its attributes we developed for our app. How they're used and a description of the code creating, reading, updating or deleting these tables can be found in the REAMDME-file of the app's front-end.
Some of the tables (Installation, Role and Session) were a given and we had no influence on the structure. The names are self-explaining. The User-table is also administrated by Parse, while we added all the different attributes, that have to be saved for a user. All this information is retrieved via the Facebook-API, which is described in more detail in the "Login"-Section. Attributes that are saved for later use in the app are: username, password, gender and profilpicure.

The event-table stores all the events, that were created by our users. Therefore we need the attributes: 
- participants, which is an array linking to all the users participating on this particular event, 
- geopoint, a struct containing two double values for the longitude and the latitude of the event, 
- duration as a date,
- name and description as a string,
- category as one of multiple strings which are defined in the strings.xml of the android project,
- creator as a seperate link to the user who created this event,
- locationName which can be chosen by the creator without any restrictions,
- maxMembers the maximum amount of participators,
- and some more Parse-internal attributes, which are not relevant.

For the settings we created a separate table so that the User-table doesn't get too messy. We called it User_Settings and it contains
- the userId as a link to the users table
- and a boolean value for each possible category like sports, dancing and so on...

We use this table to check if a push has to be sent after a user entered one of his defined geofences. Because we don't want to bother him with pushes of events he or she is not interested in, we check the event's category and send a push only if it fits.

The geofence-table is a representation of all geofences, that were created by the users. It contains the following attributes:

- center, as the location of the user where he created the geofence
- userId, which is a link to the user himself
- and a requestId, containing center and userId as a string seperated by ";". This might seem a little redundand in the first place but it makes life and code a lot easier. The client uses this ID as the ID of the android-geofence which this dataset belongs to.

We need this table to determine whether a user is currently in a geofence or not. If that is the case we offer him to delete this geofence, so that he won't be notified again by any events taken place in this geofence. If that is not the case he can create a geofence to enable the notification-service. Since the geofence API doesn't offer anything like getting a list of all geofences created by a particular user, or determining whether a user is currently in a geofence - besides the enter and dwell notificition, which is not quite what we needed - we were to build this workaround. The Code, which is necessary to complete the workaround is described in the section below.

#Cloud-Code
Cloud-Code is a possibility, offered by parse, for writing some functions in a javascript-file and submit them to the app's parse-cloud to add functionality, which isn't provided by Parse yet. You can find the javascript file "main.js" in this repo in the cloud folder or in the Cloud-Code section on the parse-website. In total we created three cloud functions, which will now be described in proper detail:

**CheckIfInGeoFence**

This method is called when the user gets to the MainScreen. It takes the user's ID and his long/lat as parameters. Then it creates a query searching for all geofences which belong to this user and are not further away than one kilometer.

`````
var query = new Parse.Query("GeoFence")
query.withinKilometers("center", userGeoPoint, 1)
query.equalTo("user", user)
`````
If the query can be executed successfully and a geofence is found the response's "inGeoFence"-Flag is set true and addtitionally the ID of the geofence is added to the response as "data". Otherwise, when a query is executed successfully but there no geofences which fit the criteria, the flag is set false and no data is returned. 
`````
if (results.length > 0) {
  response.success({
    inGeoFence: true,
    data: results[0].get("requestId")
  })
} else {
  response.success({
    inGeoFence: false
  })
}
`````

In case an error is thrown it is passed to the client to be handled.

**removeGeofence**

If the previous method returned true, the user has the ability to disbale the notifications in this area. Therefore he deletes the geofence (more detail in the client's documentation) and along with this we delete the geofence object from our database using this method. The method takes the user's and the geofence's Id as parameters to create a query, that finds the geofence, which the user is currently in.

``````
var query = new Parse.Query("GeoFence")
query.equalTo("user", user)
query.equalTo("requestId", geoFenceId)
```````

The geofence, that was found, is destroyed. Otherwise, in case destroying isn't possible, no geofence was found or an error occured fetching the data from the database appropriate information is passed to the client.

**GetNearEvents**

We wanted our users to be notified in their specified locations (their geofences) about recent events as soon as they walk into the . Therefore this method is called by the client when the "ENTER" Event of a geofence is triggered. The user's ID and his location is passed as parameter. With these information a query is built to determine whether some events are up in user's area or not. Since we also want to know if the user is actually interested in those type of events we need to build two queries using promises. The first query fetches all events which are in the user's area.

`````
 var query = new Parse.Query("Event")
  query.withinKilometers("geoPoint", userGeoPoint, 1)

  query.find().then(function (events) {...}
`````

If no events are found the result is returned, no push is sent and the method is done. Otherwise the settings representing the user's interests are fetched from the User_Settings table. He can define his interests in the settings screen (more details in client documentation). The category of each event, that is closed to him (result fo first query) is checked. In case the user likes the category, the event is added to the result-array. After every event has been checked a push is sent assuming there was one event of interesed close to him and the result array is passed to the client to be processed there.

``````
var settingsQuery = new Parse.Query("User_Settings")
settingsQuery.equalTo("user", user)
settingsQuery.find().then(function (settings) {
  var result = []
  for (var i = 0; i < events.length; i++) {
    var category = events[i].get("category").toLowerCase().replace(' ', '').toString()
    if (settings[0].get(category)) {
      result.push(events[i])
    }
  }

  if (result.length > 0) {
    Parse.Push.send({
     //where: query,
      data: {
        alert: "There is an awesome event in this area!"
      }
    })
  }
  
  response.success({
    events: result
  })
})
````

**initializeNewUser**

When a user first signs up there are several settings that needs to be initialized, set to a default value and all the facebook information needs to be retrieved. This function ensures that all the requiered steps to initialize a new user are fullfilled successfully.
This function is called by the LoginActivity.
`````
Parse.Cloud.define("initializeNewUser", function(request, response) { 
    var retrieveFacebookPicture = false;
    var initializeSettings=false;
    var loadFacebookInformation = false;

    //this helper function ensures that all the promises are returned successfully before responding to the client
    function allConditionsTrue(){
    if(loadFacebookInformation && initializeSettings && retrieveFacebookPicture){
        return true;
    }
    }

      Parse.Cloud.run("initializeSettings").then(
        function(result) {
            initializeSettings=true;
            if(allConditionsTrue()){
               response.success(result);
            }
        },
        function(result, error) {
          response.error(error);
        })


      Parse.Cloud.run("retrieveFacebookPicture").then(
        function(result) {
          retrieveFacebookPicture = true;
          if(allConditionsTrue()){
             response.success(result);
          }
        },
        function(result, error) {
          response.error(error);
        })

      Parse.Cloud.run("loadFacebookInformation").then(
        function(result) {
          loadFacebookInformation = true;
          //Initialize about me as empty String to avoid errors
          request.user.set("aboutMe", "");
          request.user.save();
          if(allConditionsTrue()){
             response.success(result);
          }
        },
        function(result, error) {
          response.error(error);
        })
 })
`````


**initializeSettings**

This function creates a user_Settings object and sets all event filters to true. This ensures that the user will retrive all kind of events near by as default.

`````
Parse.Cloud.define("initializeSettings", function(request, response) { 
        var UserSettings = Parse.Object.extend("User_Settings")
        var userSettings = new UserSettings()
        userSettings.save({
            chilling: true,
            dancing: true,
            food: true,
            music: true,
            sport: true,
            videogames:true,
            mixedgenders: true,
            user: request.user
        }, {
        success: function(result) {
            // The object was saved successfully.
            response.success(result)
        },
        error: function(result, error) {
            // The save failed.
            response.error(error)
        }
        })
    })
`````

**retrieveFacebookPicture**

This function retrieves the facebook profile picture and saves it to the PraseUserObject.
This function calls the facebook graph api to get the url of the picture.

`````
Parse.Cloud.define("retrieveFacebookPicture", function (request, response) {
    //Get the facebook id of the current user
    var fId = Parse.User.current().get('authData')['facebook'].id;
    //Create the url to request the picture of the facebook user
    var url = "https://graph.facebook.com/" + fId + "/picture"
    Parse.Cloud.httpRequest({
        url: url,
        followRedirects: true,
        params: {
            type : 'large'
        },
        success: function(httpImgFile)
        {
            //This part uses a input buffer to buffer the bytes of a picture and transfer it to a byteArray to save it as ParseFile
            var myFile = new Parse.File("profilepicture.jpg", {base64: httpImgFile.buffer.toString('base64', 0, httpImgFile.buffer.length)});
            myFile.save().then(function() {
                //When the picture was saved to parse as parseFile a reference to the picture is saved in the ParseUser
                var currentUser = request.user;
                currentUser.set("profilepicture", myFile);
                currentUser.save();
                // The file has been saved to the user.
                response.success("successfull saved fb profile picture")
                },
                 function(error) {
                    response.error(error)
                  }
            );
        },
        error: function(error){
            console.log("unsuccessful http request");
            response.error(error);
        }
    });

})
````

**loadFacebookInformation**

This function calls the facebook graph api by handing the user's access_token to the api.
It can then retrieve information about the user connected to the access_token which are then saved to parse.
`````
Parse.Cloud.define("loadFacebookInformation", function(request, response) { 
    //Get the facebook facebook acccess_token of the current user
    var access_token = Parse.User.current().get('authData')['facebook'].access_token;
    var currentUser = request.user;
    //Create the url to request the picture of the facebook user
    var url = "https://graph.facebook.com/me"
    Parse.Cloud.httpRequest({
        url: url,
        params: {
            access_token : access_token,
            //Define the information that are requested from facebook
            fields: "name,gender"
        },
        success: function(result)
        {
            //saving the retrieved facebook information to the parse user
            var data = result.data;
            currentUser.set("username", data.name);
            currentUser.set("gender", data.gender);
            currentUser.save();
            response.success(result)
        },
        error: function(error){
            response.error(error);
        }
    });
 })

`````

**reloadFacebookInformation**

This function is called by the ProfileFragment in case a user wants to update his profile. This function then reloads the data from facebbok (name, gender and picture) and saves it to parse

````
Parse.Cloud.define("reloadFacebookInformation", function(request, response) { 
    var retrieveFacebookPicture = false;
    var loadFacebookInformation = false;

    //this helper function ensures that all the promises are returned successfully before responding to the client
    function allConditionsTrue(){
    if(loadFacebookInformation && retrieveFacebookPicture){
        return true;
    }
    }

      Parse.Cloud.run("retrieveFacebookPicture").then(
        function(result) {
          retrieveFacebookPicture = true;
          if(allConditionsTrue()){
             response.success(result);
          }
        },
        function(result, error) {
          response.error(error);
        })

      Parse.Cloud.run("loadFacebookInformation").then(
        function(result) {
          loadFacebookInformation = true;
          if(allConditionsTrue()){
             response.success(result);
          }
        },
        function(result, error) {
          response.error(error);
        })
 })

````




