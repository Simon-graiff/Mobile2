package dhbw.mobile2;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

import java.util.List;

public class EventDetailFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {


    //EventObject
    public ParseObject eventObject;

    //ProgressDialog
    private ProgressDialog progressDialog;


    //Relevant Users
    ParseUser currentUser;
    List<ParseUser> listParticipants;

    //Dynamic Information of Event
    private String eventId;
    private boolean statusParticipation = false;

    //Views
    private TextView detailCategoryDynamic;
    private TextView detailDescriptionDynamic;
    private TextView detailLocationNameDynamic;
    private TextView detailCreatorNameDynamic;
    private TextView detailCreationTimeDynamic;
    private Button detailButtonParticipate;
    private Button detailButtonListParticipants;
    private Button detailButtonNavigate;
    private View rootView;
    private LinearLayout creatorView;

    //Map Elements
    private GoogleMap map = null;
    private LocationManager locationManager;
    private MapView  myMapView ;
    public List<EventManagerItem> eventManager = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();

    //HelperClass
    private HelperClass helperObject = new HelperClass();

    //constructor
    public EventDetailFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);
        rootView.setBackgroundColor(Color.rgb(240, 240, 240));

        //Map initialization
        myMapView = (MapView) rootView.findViewById(R.id.mapView);
        myMapView.onCreate(savedInstanceState);

        //fetch from local DB which event to display
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        eventId = sharedPref.getString("eventId", "no_event");

        currentUser = ParseUser.getCurrentUser();


        //declare TextViews
        detailCategoryDynamic = (TextView) (rootView.findViewById(R.id.detail_category_dynamic));
        detailDescriptionDynamic = (TextView) (rootView.findViewById(R.id.detail_description_dynamic));
        detailLocationNameDynamic = (TextView) (rootView.findViewById(R.id.detail_location_name_dynamic));
        detailCreatorNameDynamic = (TextView) (rootView.findViewById(R.id.detail_creator_dynamic));
        detailCreationTimeDynamic = (TextView) (rootView.findViewById(R.id.detail_creation_time_dynamic));
        creatorView = (LinearLayout) (rootView.findViewById(R.id.creator_view));

        //initialize Buttons and set their listeners
        detailButtonParticipate = (Button) rootView.findViewById(R.id.detail_button_participate);
        detailButtonParticipate.setOnClickListener(this);
        detailButtonListParticipants = (Button) rootView.findViewById(R.id.detail_participants_dynamic);
        detailButtonListParticipants.setOnClickListener(this);
        detailButtonNavigate = (Button) rootView.findViewById(R.id.detail_button_navigate);
        detailButtonNavigate.setOnClickListener(this);
        creatorView.setOnClickListener(this);

        return rootView;
    }

    //the following have to be implemented for the map, especially myMapView.onPause()

    @Override
    public void onResume(){
        //Initialize progressDialog
        progressDialog = ProgressDialog.show(getActivity(), "Loading", "Please wait..");

        super.onResume();
        if(myMapView!=null){
            myMapView.onResume();
        }

        //Set the NavDrawer Item "MyEvent" if it isnt activated
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

        //With this every necessary data is fetched for the event details
        retrieveParseData();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (myMapView != null) {
            myMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myMapView != null) {
            myMapView.onDestroy();
        }
        if (eventObject != null){
            eventObject.unpinInBackground();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (myMapView != null) {
            myMapView.onLowMemory();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //returns the data from Parse for the specified event
    public void retrieveParseData() {

        //Query to fetch the object
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.getInBackground(eventId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException queryException) {
                if (queryException == null) {

                    eventObject = object;
                    eventId = object.getObjectId();

                    //for Participantlist to use the event is pinned in local DB
                    eventObject.pinInBackground();


                    listParticipants = eventObject.getList("participants");

                    EventDetailFragment.this.fillDynamicData();
                    EventDetailFragment.this.checkParticipationStatus(object);

                    //asynchronous call to map
                    if (myMapView != null) {
                        myMapView.getMapAsync(EventDetailFragment.this);
                    }
                    progressDialog.dismiss();
                } else {
                    System.out.print("Object could not be received");
                }

            }
        });
    }



    private void fillDynamicData(){

        //simple Types
        fillSimpleType("category", detailCategoryDynamic);
        fillSimpleType("description", detailDescriptionDynamic);
        fillSimpleType("locationName", detailLocationNameDynamic);
        fillSimpleType("creator", detailCreatorNameDynamic);

        //add eventTitle to ActionBar
        String title = eventObject.getString("title");
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if ((title != null) && (actionBar != null)){
            actionBar.setTitle(title);
        }
        //fill more complicated types

        //fill creator Name
        fillCreatorName();

        //fill time scope
        detailCreationTimeDynamic.setText(helperObject.getTimeScopeString(eventObject));

        //fill Participants String
        detailButtonListParticipants.setText(
                helperObject.getParticipantsString(eventObject) + " participants");

        //fill categoryPicture
        String category = eventObject.getString("category");
        ImageView categoryImage = (ImageView) rootView.findViewById(R.id.category_picture);
        helperObject.setCategoryImage(categoryImage, category);

        //load ProfilePicture
        loadProfilePicture();
    }

    /*  Task of fillSimpleType: Fill a TextView with the respective attribute of the eventObject
        It needs the name of the attribute in the ParseObject to fetch the necessary data and the
        TextView in which it should fill the data.
        Then the data is taken out of the event object into the TextView.
     */
    public void fillSimpleType (String dynamicField, TextView textViewToFill){
        String stringFromObject = eventObject.getString(dynamicField);
        textViewToFill.setText(stringFromObject);
    }

    private void fillCreatorName(){
               try {
                   //set Name of the creator of the event
                   String creatorName =
                           eventObject.getParseUser("creator").fetchIfNeeded().getUsername();
                   detailCreatorNameDynamic.setText(creatorName);

               } catch (Exception e) {
                   e.printStackTrace();
               }
            }


    /* Every ParseUser contains a field "eventId". In this field is either the Object-ID of the
    event the user participates in or, if the user does not participate in any event, the field is
    null or filled with "no_event". To check whether the user participates in any event,
    it has therefore to be checked whether his eventId-field is null or "no_event".
     */
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

    @Override
    public void onClick(View view) {
        if (view == detailButtonParticipate){
            activateParticipation();
        } else if (view == detailButtonListParticipants) {
            helperObject.switchToFragment(getFragmentManager(), new ParticipantsListFragment());
        } else if (view == detailButtonNavigate){
            navigateToEvent();
            //creatorView: TextField with creator name or picture of creator
        } else if (view == creatorView){
              try {
                helperObject.switchToProfileFragment(getFragmentManager(),
                        eventObject.getParseUser("creator").fetchIfNeeded().getObjectId());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadProfilePicture(){

        ParseFile profilepicture = null;
        try {
            profilepicture = eventObject.getParseUser("creator").fetchIfNeeded().getParseFile("profilepicture");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //get profile picture asynchronously
        if (profilepicture != null) {
            profilepicture.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    int heightPixels = EventDetailFragment.this.getActivity().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
                    //Set the ProfilePicuture to the ImageView and scale it to the screen size
                    bitmap = helperObject.getCroppedCircleBitmap(bitmap);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                            ((int) (heightPixels * 0.1)),
                            ((int) (heightPixels * 0.1)),
                            false));

                }
            });
        }
    }

    public void activateParticipation(){
        //add CurrentUser to ParseObject

        // if user does not participate in an event already, he can participate in this one
        if (!statusParticipation) {
            //user can only participate if maximum amount of participants has not yet been reached
            if (listParticipants.size() <= eventObject.getInt("maxMembers")){
                //add User to Participators List of the event
                listParticipants.add(currentUser);
                eventObject.put("participants", listParticipants);
                eventObject.saveInBackground();

                //fill eventId of user with this event
                ParseUser.getCurrentUser().put("eventId", eventId);
                ParseUser.getCurrentUser().saveInBackground();

                changeParticipationToTrue();
                //fill Participants String
                detailButtonListParticipants.setText(helperObject.getParticipantsString(eventObject) + " participants");
            } else {
                //show Toast that user cannot participate

                CharSequence text = "Sorry, but you cannot participate in this event. " +
                        "The maximum amount of participants has already been reached.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getActivity(), text, duration);
                toast.show();
            }
        } else {
            
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
            builder.setMessage(
                    "You already participate in an event at the moment. " +
                            "Do you want to cancel your other event to participate in this one?")
                    .setPositiveButton("Yes", userParticipatesDialogClickListener)
                    .setNegativeButton("No", userParticipatesDialogClickListener)
                    .show();
        }

    }

    //if user already participates in an event ask him whether he wants to cancel the other one
    DialogInterface.OnClickListener userParticipatesDialogClickListener = new DialogInterface.OnClickListener() {
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

    private void deactivateParticipation(){
        if (statusParticipation) {
            removeUserFromList(eventObject);
            changeParticipationToFalse();

            ParseUser.getCurrentUser().put("eventId", R.string.detail_no_event);
            ParseUser.getCurrentUser().saveInBackground();

            helperObject.switchToFragment(getFragmentManager(), new MapFragment());
        }
    }

    //Switch all things so user cannot participate in this event
    private void changeParticipationToTrue(){
        statusParticipation = true;
        detailButtonParticipate.setText("Don\'t participate");
        detailButtonParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventDetailFragment.this.deactivateParticipation();
            }
        });

    }

    //Switch all things so user can participate in this event
    private void changeParticipationToFalse() {
        statusParticipation = false;
        detailButtonParticipate.setText("Participate");
        detailButtonParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventDetailFragment.this.activateParticipation();
            }
        });
    }

    //remove the current user from the participant list
    private void removeUserFromList(ParseObject object){
        List<ParseUser> listParticipants = object.getList("participants");
        try {
            listParticipants.remove(ParseUser.getCurrentUser().fetchIfNeeded());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        object.put("participants", listParticipants);
        object.saveInBackground();
    }


    private void setUpMap(){

        if(map != null){

            map.setOnMarkerClickListener(this);

            Location userPosition = getUserPosition();

            if(userPosition != null){

                drawEvent();

                //fit the event and the user both on the screen and move the camera to this setting
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }
                LatLng coordinates = new LatLng(userPosition.getLatitude(),
                        userPosition.getLongitude());
                builder.include(coordinates);
                LatLngBounds bounds = builder.build();
                int padding = 50; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                map.animateCamera(cu);


            }else{
                Log.d("Main", "No last position found");
            }
        }
    }//End of setUpMap

    private Location getUserPosition(){
        return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private void drawMarker(LatLng position, String title, String color, String eventID){

        if(color.equals("red")) {
            Marker m = map.addMarker(new MarkerOptions()
                            .title(title)
                            .position(position)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );
            markers.add(m);
            Log.d("Main", "Creates red marker");

            eventManager.add(new EventManagerItem(m.getId(), eventID, position));

        }
    }

    //same as in EventMap
    public void drawEvent(){
        //Creating ParseGeoPoint with user's current location
        Location userLocation = getUserPosition();
        ParseGeoPoint point = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
        ParseObject user = new ParseObject("User");
        user.put("location", point);

        ParseGeoPoint tmpPoint = eventObject.getParseGeoPoint("geoPoint");
        double tmpLat = tmpPoint.getLatitude();
        double tmpLng = tmpPoint.getLongitude();
        LatLng tmpLatLng = new LatLng(tmpLat, tmpLng);
        String tmpTitle = eventObject.getString("title");
        String color = "red";
        String eventID = eventObject.getObjectId();
        drawMarker(tmpLatLng, tmpTitle, color, eventID);

        fillDistance();
    }

    private void fillDistance(){
        //fill distance
        ParseGeoPoint currentLocation =
                new ParseGeoPoint (
                        getUserPosition().getLatitude(),
                        getUserPosition().getLongitude());
        TextView distanceView = (TextView) rootView.findViewById(R.id.detail_distance_dynamic);
        distanceView.setText(helperObject.calculateDistance(
                currentLocation,
                eventObject.getParseGeoPoint("geoPoint")) + " km");
    }

    public void navigateToEvent(){
        String[] mode = {"driving", "walking", "bicycling"};

        //Start a DialogBox asking for the mode of getting to the event
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("How do you get there?").
              //  setMessage("Message Body").
                setItems(mode, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                      double latitude = markers.get(0).getPosition().latitude;
                      double longitude = markers.get(0).getPosition().longitude;
                      String mode1 = "d";
                      switch (which) {
                          case 0:
                              mode1 = "d";
                              break;
                          case 1:
                              mode1 = "w";
                              break;
                          case 2:
                              mode1 = "b";
                              break;
                      }
                      Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + ", " + longitude + "&mode=" + mode1);
                      Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                      mapIntent.setPackage("com.google.android.apps.maps");
                      EventDetailFragment.this.startActivity(mapIntent);
                  }
              })
                .show();



    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("Main", "Map is now ready");
        map = googleMap;
        if (map != null) {
            map.getUiSettings().setMyLocationButtonEnabled(false);

            map.setMyLocationEnabled(true);
            map.setOnMarkerClickListener(this);

        }

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        setUpMap();



    }
}


