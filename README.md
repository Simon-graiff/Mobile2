WhereU
=====
Link for the presentation held in DHBW Mannheim: http://htmlpreview.github.io/?https://raw.githubusercontent.com/chrizzTs/Who-U/master/Presentation/slides-whou.html#/

WhereU is a social event platform that gives strangers the opportunity to get notified and join any kinds of events that are around them.

#Getting started
This app is an android app which uses the Parse backend as a service ([more information about Parse] (https://www.parse.com/))
Just clone this projekt and build it with e.g. AndroidStudio or right away with Gradle.
That's it. You are ready to go!

#App Structure
WhereU consists of two activities. The LoginActivity and the MainScreen. The LoginActivity is responsible for providing a login, which leads to user identification by the app. The only way to start the MainScreen-activity is a successful login. The MainScrren manages the actual content delivery and is dependent from a successful login. Both activities and their features are documented below.

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
                    Intent intent = new Intent(LogInActivity.this, MainScreen.class);
                    startActivity(intent);
                } else {
                    Log.d("MyApp", "User logged in with Facebook!");
                    Intent intent = new Intent(LogInActivity.this, MainScreen.class);
                    startActivity(intent);
                }
            }
        });
````
The user can now sign up with Facebook or if he already has signed up with Facebook log in with his connected Facebook account.
If the user has already signed up and just logs in he will be linked back to the MainScreen to use the App.

If the user needs to sign up with his Facebook account his Facebook profile data are retrived by calling the function `retriveFacebookData()` and after that the user is linked to the MainScreen as well.

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
                        redirectToMainScreen();

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

#Main Screen
The MainScreen is the actual activity behind WhereU. It extends an ActionBarActivity and displays only an ActionBar. The more it is responsible for handling clicks in the side menu or sliding gestures, which open the SideBar. Most of the screen belongs to the contentView, in which fragments are placed in by FragmentTransactions. The FragmentTransactions are mostly activated by the user, by pressing an element from the sidebar, or the Android back button. So every screen in the app is a fragment and not an activity. This has several advantages. One is a performance optimization because there is no need for stating intends. This can especially be seen in an emulator environment, but has also a huge impact on real devices. Another advantage is the differentiation between a controller - which handles the navigation through the app - and the UI, which is represented by the fragments (except the ActionBar). Another benefit of this architecture is the use and easy implementation of a back navigation. This is possible by the use of the Android BackStack.



The app launches this activity by default. To ensure that the user is logged in the following check is performed:
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
if(position==0){
    imgIcon.setImageBitmap(Bitmap.createScaledBitmap(bitmap, heightPixels, heightPixels, false));
    txtTitle.setText(ParseUser.getCurrentUser().getUsername());
}else{
    imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
    txtTitle.setText(navDrawerItems.get(position).getTitle());
}
````







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

After this more complicated types are filled.

To fill the time, the attributes "createdAt" and "duration" are needed. These are transformed into String with the format "beginTime - endTime". For this, it converts the Date format to String format with the method of convertDateToString(Date date) the HelperClass (for more information to this look at the documentation of the HelperClass). The method then just inserts the two converted string with a minus in between in a new string.

Afterwards the participants description is filled. 

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