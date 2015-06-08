#Backend Documentation

As BaaS-Provider we decided to work with Parse (https://www.parse.com/ Application ID: XtayL1TkW4GF7OyZPikWIRvdWlr4LrnOs1OjsFCO). It offers user-administration, object-oriented database services and the possibility to submit own cloud-code written in javascript. The following lines will describe how and for which services we used Parse.

#Database

Some of the tables (Installation, Role and Session) were a given and we had no influence on the structure. The names are self-explaining. The User-table is also administrated by Parse, while we added all the different attributes, that have to be saved for a user. All this information is retrieved via the Facebook-API, which is described in more detail in the "Login"-Section. Attributes that are saved for later use in the app are: username, password, gender and profilpicure.

The event-table stores all the events, that were created by our users. Therefore we need the attributes: 
- participants, which is an array linking to all the users participating on this particular event, 
- geopoint, a struct containing two double values for the longitude and the latitude of the event, 
- duration as a date,
- name and description as a string,
- category as one of multiple strings which are defined in the strings.xml of the android project,
- creator as a seperate link to the user who created this event,
- locationName which can be choosen by the creator without any restrictions,
- maxMembers the maximum amount of participators,
- and some more Parse-internal attributes, which are not relevant.

The geofence-table is a representation of all geofences, that were created by the users. We need this table to determine whether a user is currently in a geofence or not. If that is the case we offer him to delete this geofence, so that he won't be notified again by any events taken place in this geofence. If that is not the case he can create a geofence to enable the notification-service. Since the geofence API doesn't offer anything like getting a list of all geofences created by a particular user or determining whether a user is currently in one - besides the enter and dwell notificition, which is not quite what we needed - we were to build this workaround. The Code, which is necessary to complete the workaround is described in the following section.

#Cloud-Code
Cloud-Code is a possibility offered by parse to write some functions in a javascript-file and submit them to the app's parse-cloud to add functionality, which isn't provided by Parse yet. You can find the javascript file "main.js" in this repo in the cloud folder or in the Cloud-Code section on the parse-website.
