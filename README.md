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
The MainScreen is the actual activity behind WhereU. It extends an ActionBarActivity and displays only an ActionBar. The more it is responsible for handling clicks in the side menu or sliding gestures, which open the SideBar. Most of the screen belongs to the contentView, in which fragments are placed in by FragmentTransactions. The FragmentTransactions are mostly activated by the user, by pressing an element from the sidebar, or the Android back button. So every screen in the app is a fragment and not an activity. This has several advantages:
<ul>
    <li>One is a performance optimization because there is no need for stating intends. This can especially be seen in an emulator environment, but has also a huge impact on real devices.</li>
    <li>Another advantage is the differentiation between a controller - which handles the navigation through the app - and the UI, which is represented by the fragments (except the ActionBar).</li>
    <li>Another benefit of this architecture is the use and easy implementation of a back navigation. This is possible by the use of the Android BackStack.</li>
  </ul>

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

On the EventDetail Screen the user can see all data of the relevant event on one glance. The creator of the event is shown, a description, further information like the category of the event and the location, but also dynamic data like the number of participants for the event, the distance and a map showing the user himself and the event. The user has the ability to participate in the event and be navigated to it via two buttons. The number of participants is also clickable and links to the ParticipantList(Fragment) where the list of participants is shown.

EventDetail is composed of the files:

`java/dhbw.mobile2/EventDetailFragment.java`

`res/layout/fragment_event_detail.xml`

The data for the event is retrieved from Parse. Any Screen which call EventDetail saves the ID of the event locally so the fragment just has to fetch it. After this, the data is taken from the retrieved ParseObject and filled into the relevant fields.

`````   
    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    eventId = sharedPref.getString("eventId", "no_event");
`````   

Users can participate only once at a time in an event and also can only participate in only one event at all. The method checkParticipationStatus(ParseObject object) checks wether the user already participates in this event or if he participates in another. The Participate Button is dependantly labeled and the logic changes to ensure the explained business rule.

Dependant on the category of the event, a different picture is shown in the upper left corner. In the upper right corner a picture of the user is shown who created the event. If the picture or his name is clicked, his profile page is shown.

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
