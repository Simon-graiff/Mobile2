package dhbw.mobile2;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    private int maxMembers;
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

    //constructur
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
        eventId = sharedPref.getString("eventId", "LyRCMu490k");

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

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean myEventActivated = sharedPref.getBoolean("myEventActivated", false);
        if (!myEventActivated) {
            ListView mDrawerList;
            mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);

            mDrawerList.setItemChecked(1, true);
            mDrawerList.setSelection(1);
        }
        retrieveParseData();
    }

    @Override
    public void onPause(){
        super.onPause();
        myMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        myMapView.onLowMemory();
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
                    object.pinInBackground();


                    listParticipants = eventObject.getList("participants");

                    EventDetailFragment.this.fillDynamicData(object);
                    EventDetailFragment.this.checkParticipationStatus(object);

                    //asynchronous call to map
                    if (myMapView != null) {
                        myMapView.getMapAsync(EventDetailFragment.this);
                    } progressDialog.dismiss();
                }
                else {
                    System.out.print("Object could not be received");
                }

            }
        });
    }

    //get a cropped circle out of the image, copied from the internet
    public Bitmap getCroppedCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    private void fillDynamicData(ParseObject object){

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

        //fill categoryPicture
        String category = eventObject.getString("category");
        ImageView categoryImage = (ImageView) rootView.findViewById(R.id.category_picture);
        switch (category){
            case "Sport":
                categoryImage.setImageResource(R.drawable.ic_sport_blue);
                break;
            case "Chilling":
                categoryImage.setImageResource(R.drawable.ic_chilling_blue);
                break;
            case "Dancing":
                categoryImage.setImageResource(R.drawable.ic_dance_blue);
                break;
            case "Food":
                categoryImage.setImageResource(R.drawable.ic_food_blue);
                break;
            case "Music":
                categoryImage.setImageResource(R.drawable.ic_music_blue);
                break;
            case "Video Games":
                categoryImage.setImageResource(R.drawable.ic_videogames_blue);
                break;
            default:
                categoryImage.setImageResource(R.drawable.ic_sport_blue);
                break;
        }

        //fill more complex types
        fillTime(object);
        fillParticipants(object);
        fillCreatorName(object);

        //load ProfilePicture
        loadProfilePicture();
    }

    private void fillCreatorName(ParseObject object){
               try {
                   //set Name of the creator of the event
                   String creatorName = object.getParseUser("creator").fetchIfNeeded().getUsername();
                   detailCreatorNameDynamic.setText(creatorName);

               } catch (Exception e) {
                   e.printStackTrace();
               }
            }


    public void linkParticipantsActivity(){
        // switch to ParticipantList
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, new ParticipantsListFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //fill a TextView with the respective attribute of the eventObject
    public void fillSimpleType (String dynamicField, TextView textViewToFill){
        String stringFromObject = eventObject.getString(dynamicField);
        textViewToFill.setText(stringFromObject);
    }

    //set the time of the Event in a String
    private void fillTime(ParseObject object){
        Date creationTime;
        Date finishTime;
        creationTime = object.getCreatedAt();
        finishTime = object.getDate("duration");
        Calendar calendar = GregorianCalendar.getInstance();

        calendar.setTime(creationTime);
        String creationTimeString = calendar.get(Calendar.HOUR_OF_DAY)+ ":";
        if (calendar.get(Calendar.MINUTE)< 10){
            creationTimeString += "0";}
        creationTimeString += calendar.get(Calendar.MINUTE);

        calendar.setTime(finishTime);
        String finishTimeString = calendar.get(Calendar.HOUR_OF_DAY)+ ":";
        if (calendar.get(Calendar.MINUTE)< 10){
            finishTimeString += "0";}
        finishTimeString += calendar.get(Calendar.MINUTE);


        detailCreationTimeDynamic.setText(creationTimeString + " - " + finishTimeString);
    }

    // fill a string with the size of the participants
    private void fillParticipants(ParseObject object){
        listParticipants = eventObject.getList("participants");
        maxMembers = object.getInt("maxMembers");
        try {
            String textParticipants = listParticipants.size() + "/" + maxMembers + " participants";
            detailButtonListParticipants.setText(textParticipants);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkParticipationStatus(ParseObject object){
        String eventIdOfUser = "";
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
        } else if (view == detailButtonListParticipants){
            linkParticipantsActivity();
        } else if (view == detailButtonNavigate){
            navigateToEvent();
        } else if (view == creatorView){
            Fragment fragment;
            try {
                fragment = ProfileFragment.newInstance(eventObject.getParseUser("creator").fetchIfNeeded().getObjectId());
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
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
                    bitmap = EventDetailFragment.this.getCroppedCircleBitmap(bitmap);
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
            if (listParticipants.size() <= maxMembers) {
                listParticipants.add(currentUser);
                eventObject.put("participants", listParticipants);
                eventObject.saveInBackground();

                changeParticipationToTrue();
            } else {
                //show Toast that user cannot participate

                CharSequence text = "Sorry, but you cannot participate in this event. " +
                        "The maximum amount of participants has already been reached.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(getActivity(), text, duration);
                toast.show();
            }
        } else {
            //if user already participates in an event ask him wether he wants to cancel the other one
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
                                    } else {
                                        System.out.print("Object could not be received");
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
        }


    }

    //Switch all things so user cannot participate in this event
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

        ParseUser.getCurrentUser().put("eventId", eventId);
        ParseUser.getCurrentUser().saveInBackground();
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

        ParseUser.getCurrentUser().put("eventId", R.string.detail_no_event);
        ParseUser.getCurrentUser().saveInBackground();
    }

    //remove the current user from the participant list
    private void removeUserFromList(ParseObject object){
        List<ParseUser> listParticipants = object.getList("participants");
        try {
            listParticipants.remove(ParseUser.getCurrentUser().fetchIfNeeded());
            Log.d("Main", "User has been removed from " + eventObject.getString("title"));
        } catch (ParseException e) {
           Log.d("Main", "User could not been removed from " + eventObject.getString("title"));
        }
        object.put("participants", listParticipants);
        object.saveInBackground();
    }

    private void deactivateParticipation(){
        if (statusParticipation) {
            removeUserFromList(eventObject);
            changeParticipationToFalse();
        }
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

    //same as in AppMapFragment
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

        //fill distance
        ParseGeoPoint currentLocation = new ParseGeoPoint (userLocation.getLatitude(), userLocation.getLongitude());
        double distance = currentLocation.distanceInKilometersTo(eventObject.getParseGeoPoint("geoPoint"));
        distance = distance * 10;
        int distanceInt = (int) distance;
        distance = distanceInt/10.0;
        TextView distanceView = (TextView) rootView.findViewById(R.id.detail_distance_dynamic);
        if ((distance % 10) == 0){
            int intDistance = (int) distance;
            distanceView.setText(  intDistance + " km");
        } else {
            distanceView.setText(distance + " km");
        }
    }


    public void navigateToEvent(){
        String[] mode = {"driving", "walking", "bicycling"};
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


