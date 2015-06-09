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