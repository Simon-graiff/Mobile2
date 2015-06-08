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

The geofence-table is a representation of all geofences, that were created by the users. It contains the following attributes:

- center as the location of the user where he created the geofence
- userId a link to the user himself
- and a requestId containing center and userId as a string seperated by ";". This might seem a little redundand in the first place but it makes life and code a lot easier. The client uses this ID as the ID of the android-geofence which this dataset belongs to.

We need this table to determine whether a user is currently in a geofence or not. If that is the case we offer him to delete this geofence, so that he won't be notified again by any events taken place in this geofence. If that is not the case he can create a geofence to enable the notification-service. Since the geofence API doesn't offer anything like getting a list of all geofences created by a particular user or determining whether a user is currently in one - besides the enter and dwell notificition, which is not quite what we needed - we were to build this workaround. The Code, which is necessary to complete the workaround is described in the following section.

#Cloud-Code
Cloud-Code is a possibility offered by parse to write some functions in a javascript-file and submit them to the app's parse-cloud to add functionality, which isn't provided by Parse yet. You can find the javascript file "main.js" in this repo in the cloud folder or in the Cloud-Code section on the parse-website. In total we created three cloud functions, which are now going to be described in proper detail:

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

In case an error is thrown this error is passed to the client to be handled.

**removeGeofence**

If the previous method returned true, the user has the ability to disbale the notifications in this area. Therefore he deletes the geofence (more detail in the client's documentation) and along with this we delete the geofence object from our database using this method. The method takes the user's and the geofence's Id as parameters to create a query, that finds the geofence, which the user is currently in.

``````
var query = new Parse.Query("GeoFence")
query.equalTo("user", user)
query.equalTo("requestId", geoFenceId)
```````

The geofence, that was found is destroyed. Otherwise, in case destroying isn't possible, no geofence was found or an error occured fetching the data from the database appropriate information is passed to the client.

**GetNearEvents**

We wanted our users to be notified in their specified locations (their geofences) about recent events as soon as they walk in. Therefore this method is called by the client when the "ENTER" Event of a geofence is triggered. The user's ID and his location is passed as parameter. With these information a query is built to determine whether some events are up in user's area or not. Since we also want to know if the user is actually interested in those type of events we need to build two queries using promises. The first query fetches all events which are in the user's area.

`````
 var query = new Parse.Query("Event")
  query.withinKilometers("geoPoint", userGeoPoint, 1)

  query.find().then(function (events) {...}
`````

If no events are found the result is returned, no push is sent and the method is done. Otherwise the settings representing the user's interests are fetched from the User_Settings table. He can define his interests in the settings screen (more details in client documentation). The category of each event, that is closed to him (result fo first query) is checked. In case the user likes the category the event is added to the result-array. After every event has been checked a push is sent assuming there was one event of interesed close to him and the result array is passed to the client to be processed there.

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
