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

On the EventDetail Screen the user can see all data of the relevant event on one glance. The creator of the event is shown, a description, further information like the category of the event and the location, but also dynamic data like the number of participants for the event, the distance and a map showing the user himself and the event. The user has the ability to participate in the event and be navigated to it via two buttons. The number of participants is also clickable and links to the ParticipantList(Fragment) where the list of participants is shown.

EventDetail is composed of the files:

java/dhbw.mobile2/EventDetailFragment.java
res/layout/fragment_event_detail.xml

The data for the event is retrieved from Parse. Any Screen which call EventDetail saves the ID of the event locally so the fragment just has to fetch it. After this, the data is taken from the retrieved ParseObject and filled into the relevant fields.

`````   
    SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
    eventId = sharedPref.getString("eventId", "no_event");
`````   

Users can participate only once at a time in an event and also can only participate in only one event at all. The method checkParticipationStatus(ParseObject object) checks wether the user already participates in this event or if he participates in another. The Participate Button is dependantly labeled and the logic changes to ensure the explained business rule.

Dependant on the category of the event, a different picture is shown in the upper left corner. In the upper right corner a picture of the user is shown who created the event. If the picture or his name is clicked, his profile page is shown.

#ParticipantsList(Fragment)

On the ParticipantsList Screen every user who takes part in an event is shown with his picture and his name. The screen is composed out of the files:

java/dhbw.mobile2/ParticipantsListFragment.java
java/dhbw.mobile2/ParticipantsListAdapter.java
res/layout/list_participants.xml
participants_list_fragment.xml

