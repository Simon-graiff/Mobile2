Parse.Cloud.job("deleteExpiredEvents", function (request, response) {
    console.log("Called at: ")
    var now = new Date()
    console.log(now)

    var query = new Parse.Query("Event")
    query.lessThan("duration", now)
    query.find().then(function (events) {
        console.log(events)

        events.forEach(function (event) {
            var userQuery = new Parse.Query(Parse.User)
            userQuery.equalTo("eventId", event.get("objectId"))
            userQuery.find().then(function (users) {
                users.forEach(function (user) {
                    user.eventId = null
                    user.saveInBackground()
                })
            })

            event.destroy({
                success: function () {
                    console.log("deleted:")
                    console.log(event)
                },
                error: function (error) {
                    console.log(error)
                }
            })
        })
        response.success("successfully done")
    }, function (error) {
        console.log(error)
        response.error(error)
    })
})

Parse.Cloud.define("getNearEvents", function (request, response) {

    console.log(request)
    var userLong = request.params.longitude
    var userLat = request.params.latitude
    var userId = request.params.userId
    var userGeoPoint = new Parse.GeoPoint(userLat, userLong)

    var User = Parse.Object.extend("User");
    var user = new User()
    user.id = userId

    var query = new Parse.Query("Event")
    query.withinKilometers("geoPoint", userGeoPoint, 1)

    query.find().then(function (events) {
        if (!events.length > 0) {
            response.success({
                events: "No Events"
            })
            return
        }

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

            console.log(result)
            console.log(result.length)

            if (result.length > 0) {
                console.log("Push called")
                var pushQuery = new Parse.Query(Parse.User);
                query.equalTo('objectId', userId);

                Parse.Push.send({
                    where: pushQuery,
                    data: {
                        alert: "There is an awesome event in this area!"
                    }
                })
            }

            response.success({
                events: result
            })
        })
    })
})

Parse.Cloud.define("checkIfInGeoFence", function (request, response) {
    var userId = request.params.userId
    var userLat = request.params.latitude
    var userLong = request.params.longitude
    var userGeoPoint = new Parse.GeoPoint(userLat, userLong)

    var User = Parse.Object.extend("User");
    var user = new User()
    user.id = userId

    var query = new Parse.Query("GeoFence")
    query.withinKilometers("center", userGeoPoint, 1)
    query.equalTo("user", user)

    query.find({
        success: function (results) {
            console.log(results)
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
        },
        error: function (error) {
            console.log(error)
            response.error("Error fetching event-data")
        }
    })

})

Parse.Cloud.define("removeGeoFence", function (request, response) {
    var userId = request.params.userId
    var geoFenceId = request.params.geoFenceId

    var User = Parse.Object.extend("User")
    var user = new User()
    user.id = userId

    var query = new Parse.Query("GeoFence")
    query.equalTo("user", user)
    query.equalTo("requestId", geoFenceId)

    query.find({
        success: function (results) {
            results[0].destroy({
                success: function (myObject) {
                    response.success({
                        result: "deleted"
                    })
                },
                error: function (myObject, error) {
                    console.log(error)
                    response.error({
                        result: "error"
                    })
                }
            })
        },
        error: function (error) {
            console.log(error)
            response.error({
                result: "error"
            })
        }
    })

})


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