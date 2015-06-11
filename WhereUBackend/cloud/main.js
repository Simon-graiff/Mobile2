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

Parse.Cloud.define("initializeNewUser", function (request, response) { 
    var UserSettings = Parse.Object.extend("User_Settings")
    var userSettings = new UserSettings()
    userSettings.save({
        chilling: true,
        dancing: true,
        food: true,
        music: true,
        sport: true,
        videogames: true,
        mixedgenders: true,
        user: request.user
    },   {
        success: function (userSettings) {
            // The object was saved successfully.
            response.success({
                result: "created settings"
            })
        },
        error: function (userSettings, error) {
            // The save failed.
            console.log(error)
            response.error({
                result: "error"
            })
        }
    }) 
})



Parse.Cloud.define("retriveFacebookPicture", function (request, response) {
    Parse.Cloud.httpRequest({
        url: request.params.url,
        followRedirects: true,
        params: {
            type: 'large'
        },
        success: function (httpImgFile) {
            var myFile = new Parse.File("profilepicture.jpg", {
                base64: httpImgFile.buffer.toString('base64', 0, httpImgFile.buffer.length)
            });
            myFile.save().then(function () {
                var currentUser = request.user;
                currentUser.set("profilepicture", myFile);
                currentUser.save();
                console.log("saved picture as file")
                // The file has been saved to Parse.
                response.success("successfull saved fb profile picture")
            }, function (error) {
                response.error(error)
            });



        },
        error: function (httpResponse) {
            console.log("unsuccessful http request");
            response.error(httpResponse);
        }

    });

})