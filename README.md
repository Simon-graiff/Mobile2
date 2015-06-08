WhereU
=====
Link for the presentation held in DHBW Mannheim: http://htmlpreview.github.io/?https://raw.githubusercontent.com/chrizzTs/Who-U/master/Presentation/slides-whou.html#/

WhereU is a social event platform that gives strangers the opportunity to get notified and join any kinds of events that are around them.

#Getting started
This app is an android app which uses the Parse backend as a service ([more information about Parse] (https://www.parse.com/))
Just clone this projekt and build it with e.g. AndroidStudio or right away with Gradle.
That's it. You are ready to go!

#App Structure
WhereU consists of two activities: The LoginActivity and the MainActivity. The LoginActivity is responsible for providing a login, which leads to user identification by the app. The only way to start the MainActivity is a successful login. The MainScrren manages the actual content delivery and is dependent from a successful login. It is the actual activity behind WhereU. It extends an ActionBarActivity and displays only an ActionBar. The more it is responsible for handling clicks in the side menu or sliding gestures, which open the SideBar. Most of the screen belongs to the contentView, in which fragments are placed in by FragmentTransactions. The FragmentTransactions are mostly activated by the user, by pressing an element from the sidebar, or the Android back button. So every screen in the app is a fragment and not an activity. This has several advantages:
<ul>
    <li>One is a performance optimization because there is no need for stating intends. This can especially be seen in an emulator environment, but has also a huge impact on real devices.</li>
    <li>The more this concept provides a differentiation between a controller - which handles the navigation through the app - and the UI, which is represented by the fragments (except the ActionBar).</li>
    <li>Another benefit of this architecture is the use and easy implementation of a back navigation. This is possible by the use of the Android BackStack.</li>
  </ul>

#LoginActivity
The app uses the ParseFacebookUtils library to call the Facebook SDK ([more information see Parse Doku] (https://www.parse.com/docs/android/guide#users-facebook-users))
````
        ParseFacebookUtils.logInWithReadPermissionsInBackground(LogInActivity.this, permissions, new LogInCallback() {
            @Override
            public void done(final ParseUser user, ParseException err) {
                if (user == null) {
                    Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                    Toast.makeText(getApplicationContext(), "Please Log-in with Facebook!", Toast.LENGTH_LONG).show();
                    ParseUser.logOut();
                } else if (user.isNew()) {
                    Log.d("MyApp", "User signed up and logged in through Facebook!");
                    retriveFacebookData();
                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Log.d("MyApp", "User logged in with Facebook!");
                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        });
````
The user can now sign up with Facebook or if he already has signed up with Facebook log in with his connected Facebook account.
If the user has already signed up and just logs in he will be linked back to the MainActivity to use the App.

If the user needs to sign up with his Facebook account his Facebook profile data are retrived by calling the function `retriveFacebookData()` and after that the user is linked to the MainActivity as well.

**retriveFacebookData()**

This function uses the Facebook GraphAPI and sends a Me-Request to retrive the needed Facebook data. ([For more information see Facebook Graph API] (https://developers.facebook.com/docs/graph-api))
The Facebook GrahphAPI returns the username and the gender of the the user which then is saved to Parse by calling:
````
           @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                if (user != null) {
                    try {
                        ParseUser.getCurrentUser().put("username", user.getString("name"));
                        ParseUser.getCurrentUser().put("gender", user.getString("gender"));
                        ParseUser.getCurrentUser().put("aboutMe", "");
                        ParseUser.getCurrentUser().saveInBackground();
                        redirectToMainActivity();

                    } catch (Exception e) {
                        Log.e("Error from FB Data", e.getMessage());
                    }
                }
            }
````

To retrive the Facebook profile picture a similar call to the Facebook GraphAPI can be sent which returns the URL of the Facebook picture.
This picture is downloaded by the function 
**DownloadPictureTask()**
This method uses the BufferedInputStream which can then by saved as a byte array.

This byte array can be saved as a ParseFile to the Parse database
`````
byte[] data = IOUtils.toByteArray(in);
//Saves the ByteArray to Parse as a File
ParseFile file = new ParseFile("profilepicture.jpg", data);
ParseUser.getCurrentUser().put("profilepicture", file);
ParseUser.getCurrentUser().saveInBackground();
`````

#MainActivity 
**onCreate**
The app launches this activity by default, which executes the code, which is stated in the onCreate-Method. To ensure that the user is logged in (which is critical) the following check is performed:
````
//Check if User is logged in
if(ParseUser.getCurrentUser() == null){
    //If the user is not logged in call the loginActiviy
    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
    startActivity(intent);
} else {
    currentUser = ParseUser.getCurrentUser();
}
````
If the user is not logged in yet, the user is redirected to the LoginActivity to handel the login.

After this check, the SideBar is built up. One SideBar element consists of an icon and a text. Both are stored in arrays and imported during onCreate. After the import, the icons are added to an ArrayList, called "navDrawerItems", which consists special objects, the NavDrawerItems:
````
navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
...
navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
````

NavDrawerItem is a special class which makes it possible to display an icon and a text in the SideBar. This class consists just of a constructor and getters/setters. After that a second class is used, the NavDrawerListAdapter. This class extends BaseAdapter and is returning Views - the SideBar elements. It also converts the icons (which are to this point drawables) to ImageViews, which can be seen below:
````
if(position==0) {
    imgIcon.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 75, heightPixels, false));
    txtTitle.setText(ParseUser.getCurrentUser().getUsername());
}else if(position==2){
    if(!statusParticipation){
        //display createEvent
        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        txtTitle.setText(navDrawerItems.get(position).getTitle());
    }else{
        //display myEvent
        imgIcon.setImageResource(R.drawable.ic_my_event);
        txtTitle.setText("My Event");
    }
}else{
    imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
    txtTitle.setText(navDrawerItems.get(position).getTitle());
}
````
The snippet above also shows the manipulation of the menu entries. The adapter returns every NavDrawerItem in the Sidebar individually. Position is the index of the SideBar position (0 is the first entry). This provides the opportunity for customizing them. Since the icons and the text for the SideBar items are stored in an array, they are all determined before the app is even started. To improve the look and feel of the WhereU the entry, which provides the link to the user profile, is customized with the Facebook profile picture of the user as icon and his name as text. It is obvious that these information have to be gathered from parse objects because there is no other connection to Facebook. The if-statement for ````position==2```` shows another example for the mentioned customization. The executed code changes the menu entry dependent from the user's participation in an event.

After that a drawer listener has to be assigned to the SideBar with: ````mDrawerLayout.setDrawerListener(mDrawerToggle);````. mDrawerToggle is a customized ActionBarDrawerToggle. The only reasons for customization is the implementation of code, which hides the Android soft keyboard, in case it is open and setting the WhereU-Slogan to the ActionBar. The listener opens the SideBar and is also responsible for a close.

To be able to handle the clicks on the SideBar entries, another ClickListener has to be assigned: ````mDrawerList.setOnItemClickListener(new SlideMenuClickListener());```` This listener returns the position of the clicked element in the SideBar.


**Routing forward**
Routing forward through the app implicates the handling of user clicks. Since the just mentioned ClickListener returns the position, a simple if-statement provides the ability to perform custom code on a click. This custom code is mostly the creation of a certain fragment, e.g. a ProfileFragment, in case the user clicks on the profile entry in the SideBar. No matter which entry is clicked, the fragment is called same. This supports the execution of a universal FragmentTrasaction:
````
FragmentManager fragmentManager = getFragmentManager();
FragmentTransaction transaction = fragmentManager.beginTransaction();
transaction.replace(R.id.frame_container, fragment);
transaction.addToBackStack(null);
transaction.commit();
````
Goal of the FragmentTransaction is replacing the shown fragment (from user's view the current screen) with another, without changing the activity. The FragmentManager handles the actual transaction. The replace method needs two important parameters: the "container" which shall show the replacing fragment, and the content (the fragment) which will be displayed. It is important to mention that the transaction will <i>replace</i> a fragment. This means that the first fragment is forced to enter the "onPause" state. It is not longer active. The opposite would be to use the <i>add</i> method. After declaring the target container and fragment, the paused fragment has to be thrown on top of the BackStack. This stack is a collection which stores after every FragmentTransaction the replaced fragment. This enables a proper back navigation.


**Routing backward**
The back navigation is an essential part of every Android app. This can be shown by the fact that Android phones have a back button, which can be used at any time, in any app. According to the Android design guidelines the implementation of an own back button is not recommended. This is why WhereU takes advantage of the ````onBackPressed()```` method. Every time the back button is pressed, this method is executed. The executed code is pretty simple:
````
FragmentManager fragmentManager = getFragmentManager();
if(fragmentManager.getBackStackEntryCount()!=0){
    fragmentManager.popBackStack();
}else{
    super.onBackPressed();
}
````
Since all former shown fragments are stored on a stack, the last fragment is the first on the stack. In case the stack is not empty, the currently shown fragment is replaced with the first fragment on the stack. In case there is no fragment left, the app will exit.


**Geofencing**

Now we’re going to take a deeper look into the functions handling the geofences. First there is „checkIfInGeofence“. This method is called by the GoogleApiCallbackHandler which is created similar as in the createEventFragment. To not overfill this section the description of the googleApiClient-usage has been put there. The parameters that have to be passed are the longitude, the latitude and the user’s Id. These parameters are put into a background call of the „checkIfInGeofence“ function in the cloud-code (more details in backend-documentation) so that this method can be seen as the implementation of a remote procedure call. Additionally the background-method takes a callback which will be executed after the cloud code. It assigns the result, a boolean value into the class-variable "mInGeofence", which is used to determine whether the „createGeofence“ oder the „deleteGeofence“ button has to be shown in the actionbar. Other than that the „mCurrentGeofenceId“-field is set to the id of the geofence which is returned from the backend as well.

``````
param.put("longitude", longitude);
param.put("latitude", latitude);
param.put("userId", user);

ParseCloud.callFunctionInBackground("checkIfInGeoFence", param, new FunctionCallback<Map<String, Object>>() {
        @Override
        public void done(Map<String, Object> stringObjectMap, ParseException e) {
                [...]//Callback
        }
});
`````

The functionality of the mentioned buttons will be the content of the following lines. To be able to follow a geofence’s lifecycle let’s start with the „createGeofence“-function. At the beginning it is figured out whether the „createGeofence“ button is called for the first time using the SharedPreferences in the „checkIfFirstGeofence“-function.

`````
SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
if(!prefs.getBoolean("firstTime", false)) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstTime", true);
        editor.commit();
        return true;
}
return false;
```

If the button is clicked for the first time an AlertDialog is shown to explain the user what he just did and how he can revert his actions. Therefore the „showFirstTimeNotification“-function is called. A little special is that the OnClickListener of the button stays empty. This is because we don't want anything to happen here. It's just a little information for our users to make the use of this app more convenient.

`````
new AlertDialog.Builder(this)
        .setTitle("Creating Geofence")
                .setMessage("From now on you'll be notified if any events are up in this area. To stop notifications just push the button again.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
```

As soon as the user has confirmed the notification or the button isn’t pressed for the first time, the requestId is built which uniquely represents the geofence to be created. We configure the geofence to be at the user’s location with a radius of 250 metres. Besides that, it shall never expire, it can only be deleted by the user himself. The last line sets the „mCreatingGeofence“-flag true, so that the callback can decide whether a new geofence is created or an existing one is deleted.

`````
mGeoFenceList.add(new Geofence.Builder()
        .setRequestId(requestId)
        .setCircularRegion(mLocation.getLongitude(), mLocation.getLatitude(), 500) //long,lat,radius
        .setExpirationDuration(Geofence.NEVER_EXPIRE)//millis
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build());
```

After the geofence has been created internally the „onResult“-callback is called. Assuming the geofence-creation didn’t throw any error, it calls the „submitGeofenceToDatabase“. Those lines of code create a new database object in the Parse backend and finalizes the creation of the geofence. In this finalizing process
the „mInGeofence“-flag is set true
the „mCreatingGeofence“-flag is set false and
the „mCurrentGeofenceId“-field is set to the id of the recently created geofence
Since all these statements are simple assignments of variables no code has to be shown here.

Now the button in the upper right changed to „deleteGeofence“ which calls the „removeGeofence“-function. The mCurrentGeofenceId is put into an ArrayList (requirement of android API) and the LocationServices API is called to remove the geofence internally. Furthermore the class-variables are changed accordingly.
As soon as the internal removal of the geofence could be executed successfully, the „onResult“-callback is called again, which in turn calls the „removeGeofenceFromDatabase“. This method takes the ID of the current user as well as the assigned „mCurrentGeofenceId“ to call the „removeGeofence“-function of the cloud code. The callback changes the app’s variables again so that the user can create a new geofence in this area if he wants to.

In both cases the Parse-backend is changed only if the client and the internal android system could handle the geofence successfully so that we keep both in sync all the time.




#ProfileScreen
The ProfileScreen is used for the own user to show his information and to see other users information.
The user itself can edit some of his data like the about me textfield.
To call the ProfileScreen Fragment it is necessary to provide the UserId of the user whose profile you want to checkout.

The ProfileFragment.call checks if the provided UserId is equal to the currentUserID to know if the change data button needs to be enabled or not.

If the current user sees his own profile he can edit the about me textfield which will be saved automatically when leaving the fragment. 
`````   
@Override
    public void onPause() {
        EditText aboutMeTextField = (EditText) rootView.findViewById(id.editText_about);
        String newAboutMe = aboutMeTextField.getText().toString();

        if(!aboutMe.equals(newAboutMe)){
            ParseUser.getCurrentUser().put("aboutMe", newAboutMe);
            ParseUser.getCurrentUser().saveInBackground();
            Toast.makeText(getActivity().getWindow().getContext(), "Your changes are saved!", Toast.LENGTH_LONG).show();
        }
`````

#EventDetail(Fragment)

**Structure**

On the EventDetail Screen the user can see all data of the relevant event on one glance. The creator of the event is shown, a description, further information like the category of the event and the location, but also dynamic data like the number of participants for the event, the distance and a map showing the user himself and the event. The user has the ability to participate in the event and be navigated to it via two buttons. The number of participants is also clickable and links to the ParticipantList(Fragment) where the list of participants is shown. Dependent on the category of the event, a different picture is shown in the upper left corner. In the upper right corner a picture of the user is shown who created the event. If the picture or his name is clicked, his profile page is shown.

EventDetail is composed of the files:

`java/dhbw.mobile2/EventDetailFragment.java`

`res/layout/fragment_event_detail.xml`

The data for the event is retrieved from Parse. Any Screen which calls EventDetail saves the ID of the event locally so the fragment just has to fetch it. After this, the data is taken from the retrieved ParseObject and filled into the relevant fields.

`````   
    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    eventId = sharedPref.getString("eventId", "no_event");
`````   

**Participation Status**

Users can participate only once at a time in an event and also can only participate in only one event at all. The method checkParticipationStatus(ParseObject object) checks wether the user already participates in this event or if he participates in another. The Participate Button is dependently labeled and the logic changes to ensure the explained business rule.

Every ParseUser contains a field "eventId". In this field is either the Object-ID of the event the user participates in or, if the user does not participate in any event, the field is null or filled with "no_event". To check whether the user participates in any event, it has therefore to be checked whether his eventId-field is null or "no_event".

````
private void checkParticipationStatus(ParseObject object){
        String eventIdOfUser;
        try {
            eventIdOfUser = ParseUser.getCurrentUser().fetch().getString("eventId");
            // user is not already in an event if his attribute eventId is null or if it equals no_event
            if (eventIdOfUser != null){
                if (!eventIdOfUser.equals("no_event")) {
                    if (eventIdOfUser.equals(object.getObjectId())) {
                        changeParticipationToTrue();
                    } else {
                        statusParticipation = true;
                    }
                } else {
                    changeParticipationToFalse();
                }
            } else {
                changeParticipationToFalse();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
````

The method changeParticipationToTrue() is called when the user already participates in the an event. It changes all fields to represent this state and ensures that the user cannot participate twice in events. Most important is that the OnClickListener of the ParticipateButton calls DeactivateParticipation() instead of ActivateParticipation(). Also the caption is changed and the variable statusParticipation is switched. The method changeParticipationToFalse is the pendent to changeParticipationToTrue and changes all relevant data, so the user can participate in the event again.

````
    private void changeParticipationToTrue(){
        statusParticipation = true;
        fillParticipants(eventObject);
        detailButtonParticipate.setText("Don\'t participate");
        detailButtonParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventDetailFragment.this.deactivateParticipation();
            }
        });
    }

    //Switch all things so user can participate in this event
    private void changeParticipationToFalse(){
        statusParticipation = false;
        fillParticipants(eventObject);
        detailButtonParticipate.setText("Participate");
        detailButtonParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventDetailFragment.this.activateParticipation();
            }
        });
    }
````

**Activate and Deactivate Participation**

If a user does not participate in an event already and the maximum amount of participants in the event has not been reached, if he/she clicks on the Button with the caption "Participate" will participate in the shown event. The correspondent method is activateParticipation(). First the user is added to the list of participants of the event and the Id of the event is saved for the user. Then the method changeParticipationToTrue() is called as described in the previous section.

````
if (listParticipants.size() <= maxMembers) {
                //add User to Participators List of the event
                listParticipants.add(currentUser);
                eventObject.put("participants", listParticipants);
                eventObject.saveInBackground();

                //fill eventId of user with this event
                ParseUser.getCurrentUser().put("eventId", eventId);
                ParseUser.getCurrentUser().saveInBackground();

                changeParticipationToTrue();
            } 
````

If the user already participates in an event, he can decide whether he wants to deactivate his participation in the other event and participate in the shown one or if he wants to stay with the other one. For this a Dialog pops up, implemented with the Dialog Interface. If the user wants to change events he will be removed from the participators list of the other event, his participation status is set to false and the method activateParticipation() is called again (this time activating the participation because the participation status is false).

             
````
DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        //if he chose Yes, remove user from other event, set ParticipationStatus to false
                        //and try again
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked

                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
                            try {
                                String previousEventId = ParseUser.getCurrentUser().fetchIfNeeded().getString("eventId");
                            query.getInBackground(previousEventId, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException queryException) {
                                    if (queryException == null) {

                                        removeUserFromList(object);
                                        statusParticipation = false;
                                        activateParticipation();
                                    }

                                }});
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            break;

                        //if user chooses No, leave everything as it is
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(
                    "You already participate in an event at the moment. " +
                            "Do you want to cancel your other event to participate in this one?").setPositiveButton(
                    "Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
                 
````

With the method deactivateParticipation() the user is removed from the participators list of the current event and his eventId is set to "no_event" so he can participate in events again. Afterwards the screen switches to the Map Screen because the selected NavDrawer-Item may not be be correct anymore, the CreateEvent function is still greyed out and the user can look for new events.. 

**onResume behavior**

When the fragment is put back to the screen (onResume() is called), it is first checked wether the NavDrawer-Item MyEvent was called, because either this can be the case or that the fragment is called on the map or the list of events. If the NavDrawer-Item was called, the MainScreen Activity will pass a SharedPreferences Item that is fetched in the onResume() method. Depending on its state the NavDrawer-Item 1 (Events) is checked or NavDrawer-Item 2 (My Event) is checked.

````
SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean myEventActivated = sharedPref.getBoolean("myEventActivated", false);
        ListView mDrawerList;
        mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
        if (!myEventActivated) {
            mDrawerList.setItemChecked(1, true);
            mDrawerList.setSelection(1);
        }else{
            mDrawerList.setItemChecked(2, true);
            mDrawerList.setSelection(2);
        }
````

After this, all relevant data is fetched with retrieveParseData(). All dynamic data fields are filled with the method fillDynamicData(ParseObject object) and the Participation Status is checked.

**Filling of Dynamic Data Fields**

The method fillDynamicData() fills the whole EventDetail Page with data. In this section the needed functions are explained.

The dynamic data of every event is stored in a ParseObject. This type of ParseObject contains the (here relevant) columns:
objectId    :   String
category    :   String 
description :   String
duration    :   Date
locationName:   String
maxMembers  :   Number
title       :   String
createdAt   :   Date
creator     :   Pointer<_User>
geoPoint    :   GeoPoint
paricipants :   Array (of Pointer_<User>

Some of these fields are very easy to insert, because they are in a String format and just need to be allocated to the respective TextFields with the name. This simple type is handled by the method fillSimpleType(String dynamicField, TextView textViewToFill). It needs the name of the attribute in the ParseObject to fetch the necessary data and the TextView in which it should fill the data. Then the data is taken out of the event object into the TextView. Data fields which use this method are: category, description, locationName, creator.

````
public void fillSimpleType (String dynamicField, TextView textViewToFill){
        String stringFromObject = eventObject.getString(dynamicField);
        textViewToFill.setText(stringFromObject);
    }
````

After this more complicated types are filled. For these methods from the HelperClass are used (for more information to this look at the documentation of the HelperClass). First the title of the event is set as the ActionBar title, then the creator name is set with fillCreatorName(). It takes the name from the eventObject attribute creator which returns the ParseUser with the name. Subsequently the time scope text is set with the HelperClass method getTimeScopeString(). Afterwards the participants description is filled with the method getParticipantsString(ParseObject object) from the HelperClass. Lastly the Category Picture is drawn with the method from the HelperClass "setCategoryImage()".

**Navigation To The Event**

If the Button with the caption "Navigate Me" is called, a navigation to the event location is started. The user is asked how he/she will get to the event. Options are "driving", "walking" and "bycicling", stored in the array mode. The question is asked with an AlertDialog. To start the navigation an intent has to be sent to the Google Maps Application (it has to be installed on the device). 

The intent is created with a class named Uri which parses the specifications given to the navigation. After the String "google.navigation:q=" for the navigation the latitude and the longitude have to be given in the String. In this case these are the latitude and longitude of the event location. Lastly after the String "&mode=" the mode of follows. In this mode d is passed if the user chose driving, w is passed if the user chose walking and b is passed if the user bhose bicycling. After this an intent is created out of the parsed String, the Application is selected with mapIntent.setpackage() and the navigation activity is started.

````
Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + ", " + longitude + "&mode=" + mode1);
                      Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                      mapIntent.setPackage("com.google.android.apps.maps");
                      EventDetailFragment.this.startActivity(mapIntent);
````

#HelperClass

**convertDateToString and getTimeScopeString**

The method convertDateToString(Date date) does what its name already tells: It converts a variable in a Date Format into a variable in a String format. This is done with the GregorianCalendar Class. Then a zero is added before the minutes if the minutes are below 10 to have always the same format.

The method getTimeScopeString(ParseObject object) takes an EventObject and builds its creationTime and finishTime into a String to have the time scope for the whole event. It takes the Dates out of the EventObject and puts a minus sign in between them.

**switchToFragment and switchToProfileFragment**

The method switchToFragment switches to the specified fragment. It uses the strategy explained in the capter "MainScreen". switchToProfileFragement switches to the Profile Fragement which is slightly another strategy than the other fragments as explained in the chapter "Profile Page". It creates the ProfileFragment and then calls switchToFragment().

#ParticipantsList(Fragment)

On the ParticipantsList Screen every user who takes part in an event is shown with his picture and his name. The screen works with a ListView, in which each List Item represents one user with his name and his profile picture. Each List Item is clickable and redirects to the Profile Page of the user. The screen is composed out of the files:


`java/dhbw.mobile2/ParticipantsListFragment.java`:

Fills the List View with the name and picture of the participants (saved in the event object from Parse).
In onItemClick() the user is redirected to the respective Profile Page.

`java/dhbw.mobile2/ParticipantsListAdapter.java`:

Contains the data of the ListView

`res/layout/list_item_participants.xml`:

Represents the structure of one participant list item

`res/layout/fragment_participants_list.xml`:

Contains just the ListView

#ListEvents(Fragment)
The ListEvents-Page is an alternative to the Map-View of Events. All events in the area are shown in a ListView with their most relevant facts.                                                                                                               

From the left to the right there is shown a little icon representing the category of the event, the event title, the distance to the event, the number of participants in the events together with the maximum amount of people able to participate and time with the time the event started and the time the event will probably end.

If an event item is clicked, the EventDetail-Page of the respective event is shown.

The ListEvents Page is composed of the files:

`java/dhbw.mobile2/EventListAdapter.java`:

Contains the data of the ListView of all events

`java/dhbw.mobile2/ListEventsFragment.java`:

Contains the logic of the Page. This page has two main goals:

1. Get the data of the relevant events

2. Pass this data to the EventListAdapter

To 1.:

The data for the relevant events is taken from the AppMapFragment. The EventList Page is opened through an ActionBar Item that is shown only on the MapView. When the MapView fetches events and filters them, they are saved in a localDataStore ParseObject. This object is fetched in getEventData() and the the relevant data for the ListView is then extracted in the callback to pass it to the EventListAdapter.

To 2.:

The EventListAdapter takes the data of the events with ArrayLists containing the single data items. Apart from the category, only strings are passed to the Adapter, which are just shown to the user. The Adapter can easily iterate through these arrays. For the category the EventListFragement sends the String name of the category to the Adapter and the Adapter looks for the matching icon. Apart from the String objects, the ID of the Events objects has to be send as well, so the user can be redirected to the EventDetail Page of this ID when it is called. 

`res/layout/fragment_events_list.xml`:

Contains just the ListView with the events.

`res/layout/list_item_events.xml`:

Contains the structure of one single Event Item in the List. 

#CreateEvent(Fragment)
This fragment is designed as a formular for the user to insert all data, that are necessary to create a new event. For the different attributes that are inserted and how they are stored please check the database-documentation in the whereU-Backend-folder.

The class implements three interfaces in total. First of all the onClickListener is necessary to get notified when a button is clicked and to call the proper action. Besides that the ConnectionCallbacks and the OnConnectionFailedListener are implemented to be able to use the googleApiClient. This client, besides other class-variables like references to the ui-elements, is set up during the creating process of the screen and this object in the onCreateView method. Since this code is trivial we think no further explanations are necessary for this method. Solely the instantiation of the api client contains something mentionable. Only the Location Api is set up. Nothing else is needed in this fragment.

``````
mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getBaseContext())
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
``````

The next method should be explained in a little more detail because it contains the main functionality of this fragment. CreateEvent (ca. line 95-170) is called after clicking on the "Start the awesome" button on the screen by the OnClickListener ( ca. line 199-210). First thing it does is checking whether the content of the maxMembers-field is convertable to an integer and if the end time is set.

`````
        try {
            maxMembers = Integer.parseInt(mEditText_maxMembers.getText().toString());

            if(!mEditText_duration.getText().toString().contains("Until:")){
                Toast.makeText(getActivity().getBaseContext(), "Please set a duration!", Toast.LENGTH_LONG).show();
                return;
            }

        } catch (NumberFormatException nfe) {
            Toast.makeText(getActivity().getBaseContext(), "MaxMembers is not a number!", Toast.LENGTH_LONG).show();
            return;
        }
`````

If the members field isn't convertable a NumberFormatException is thrown and caught showing a toast which informs the user about the problem. In case this Exception is thrown the method returns. Additionally it is checked whether the duration editText-field contains "Until:". The reason for that is that if someone clicks the field a dateTimePicker is shown to select the date, which is afterwards converted into a string starting with "Until: ", that can be analyzed in later steps easily. With that workaround we avoid error concerning the formatting

Assuming those two tests passed the geoPoint is created. Therefore another instance variable mLocation is used. This is set by the callbackHandler of the googleApiClient as soon as it is connected in ca. line 215. At this point we can suppose that getLastLocation() provides a Location because the same method is used in the MainScreen earlier. There it is ensured that this method doesn't return null.

`````
ParseGeoPoint geoPoint = new ParseGeoPoint();
        geoPoint.setLatitude(mLocation.getLatitude());
        geoPoint.setLongitude(mLocation.getLongitude());
````

Subsequently an ArrayList is created which will contain all users, that participate in this event. At creation time the creator is obviously the only participant.

`````
ArrayList<ParseUser> participants = new ArrayList<>();
        try {
            participants.add(ParseUser.getCurrentUser().fetchIfNeeded());
        } catch (ParseException e) {
            e.printStackTrace();
        }
````

This step is followed by reading the end time of the event from the mEditText_duration field. Here the simple string handling of the time comes into play. The präfix "Until: " is cut via a substring call. With the same method the hour and the minute are separated and casted into int values. If the given hour is earlier than the hour now we expect the event to last until the next day which might be the case for a party-event. Events that are longer are not representable by this application.

``````
String endTime = mEditText_duration.getText().toString().substring(7);
int hourOfEnd = Integer.parseInt(endTime.substring(0, 2));
int minuteOfEnd = Integer.parseInt(endTime.substring(3));
Boolean endsNextDay = hourOfEnd < Calendar.getInstance().HOUR_OF_DAY;
Date endDate = new Date();
endDate.setHours(hourOfEnd);
endDate.setMinutes(minuteOfEnd);
if(endsNextDay)
        endDate.setDate(Calendar.getInstance().DAY_OF_MONTH+1);
`````

The string we are talking about is created in ca. lines 259-270 the onTimeSet method of the TimePickerFragment which is shown as soon as the user wants to insert the time. The minutes und hours smaller than ten are filled by a leading 0 and the präfix "Until: " is set.

``````
String hourFiller = "";
if(hourOfDay < 10)
        hourFiller = "0";

[...]

toEdit.setText("Until: " + hourFiller + hourOfDay + ":" + minuteFiller + minute);
``````

The next few lines collect all these data together into a single event-object, which is then saved via the "saveInBackground" method provided by Parse. Afterwards the user is headed to the eventDetail-Screen in which he can see his recently created event.
