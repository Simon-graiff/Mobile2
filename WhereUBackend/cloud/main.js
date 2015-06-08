Parse.Cloud.define("getNearEvents", function (request, response) {

    var query = new Parse.Query("Event")

    console.log(request)
    var userLong = request.params.longitude
    var userLat = request.params.latitude
    var userId = request.params.userId

    query.find({
        success: function (results) {
            var events = []
            for (var i = 0; i < results.length; i++) {
                var event = results[i]
                var geoPoint = event.get("geoPoint")
                var geoPointUser = new Parse.GeoPoint(userLong, userLat)
                var distance = geoPoint.kilometersTo(geoPointUser)

                if (distance < 0.250) {
                    events.push(results[i])
                }
            }

            var result = {
                events: events
            }


            var query = new Parse.Query("User")
            query.equalTo('ObjectId', userId)

            Parse.Push.send({
                where: query,
                data: {
                    alert: "There is an awesome event in this area!"
                }
            });

            response.success(result)

        },
        error: function () {
            response.error("Error fetching event-data")
        }
    })
})

Parse.Cloud.define("checkIfInGeoFence", function (request, response) {
    var userId = request.params.userId
    var userLat = request.params.latitude
    var userLong = request.params.longitude
    var userGeoPoint = new Parse.GeoPoint(userLong, userLat)

    var User = Parse.Object.extend("User");
    var user = new User()
    user.id = userId

    var query = new Parse.Query("GeoFence")
        //query.matchesKeyInQuery(userId, "objectId", userQuery)
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