package dhbw.mobile2;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

    //Map Elements
    private GoogleMap map = null;
    private LocationManager locationManager;
    private MapView  myMapView ;
    public List<EventManagerItem> eventManager = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();

    //Konstruktur
    public EventDetailFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);

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

        //initialize Buttons and set their listeners
        detailButtonParticipate = (Button) rootView.findViewById(R.id.detail_button_participate);
        detailButtonParticipate.setOnClickListener(this);
        detailButtonListParticipants = (Button) rootView.findViewById(R.id.detail_participants_dynamic);
        detailButtonListParticipants.setOnClickListener(this);
        detailButtonNavigate = (Button) rootView.findViewById(R.id.detail_button_navigate);
        detailButtonNavigate.setOnClickListener(this);

        return rootView;
    }

    //the following have to be implemented for the map, especially myMapView.onPause()

    @Override
    public void onResume(){
        super.onResume();
        if(map!=null){
            myMapView.onResume();
        }

        ListView mDrawerList;
        mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
        mDrawerList.setItemChecked(1, true);
        mDrawerList.setSelection(1);
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
            public void done(ParseObject object, ParseException queryException) {
                if (queryException == null) {

                    eventObject = object;
                    eventId = object.getObjectId();

                    //for Participantlist to use the event is pinned in local DB
                    object.pinInBackground();


                    listParticipants = eventObject.getList("participants");

                    fillDynamicData(object);
                    checkParticipationStatus(object);

                    //asynchronous call to map
                    if (myMapView != null) {
                        myMapView.getMapAsync(EventDetailFragment.this);
                    }
                } else {
                    System.out.print("Object could not be received");
                }

            }
        });
    }

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
                categoryImage.setImageResource(R.drawable.ic_sport);
                break;
            case "Chilling":
                categoryImage.setImageResource(R.drawable.ic_chilling);
                break;
            case "Food":
                categoryImage.setImageResource(R.drawable.ic_food);
                break;
            case "Music":
                categoryImage.setImageResource(R.drawable.ic_music);
                break;
            case "Videogames":
                categoryImage.setImageResource(R.drawable.ic_videogames);
                break;
        }

        //fill more complex types
        fillCreationTime(object);
        fillParticipants(object);

        //load ProfilePicture
        loadProfilePicture();
    }


    public void linkParticipantsActivity(){

        Fragment fragment = new ParticipantsListFragment();
        /*FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();*/
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void fillSimpleType (String dynamicField, TextView textViewToFill){
        getActivity().runOnUiThread(new UIRunnable(dynamicField, textViewToFill, eventObject)
        );
    }


    private void fillCreationTime(ParseObject object){
        Date creationTime;
        creationTime = object.getCreatedAt();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(creationTime);
        String creationTimeString = calendar.get(Calendar.HOUR_OF_DAY)+ ":";
        if (calendar.get(Calendar.MINUTE)< 10){
            creationTimeString += "0";}

        creationTimeString += calendar.get(Calendar.MINUTE);

        detailCreationTimeDynamic.setText(creationTimeString);
    }

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
        String eventIdOfUser = currentUser.getString("eventId");
        if (eventIdOfUser != null){
            Log.d("Main", "eventId is not null");
           if (!eventIdOfUser.equals("no_event")) {
               if (eventIdOfUser.equals(object.getObjectId())) {
                   changeParticipationToTrue();
                   Log.d("Main", "eventId does not equal no_event");
               } else {
                   statusParticipation = true;
               }
           } else {
                   changeParticipationToFalse();
           }
       } else {
           changeParticipationToFalse();
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
        }
    }

    private void loadProfilePicture(){
        ImageView imageView=(ImageView) rootView.findViewById(R.id.imageView);

        ParseUser creator = eventObject.getParseUser("creator");
        ParseFile profilepicture = creator.getParseFile("profilepicture");
        byte [] data = new byte[0];
        try {
            data = profilepicture.getData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
        int heightPixels = getActivity().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        //Set the ProfilePicuture to the ImageView and scale it to the screen size
        bitmap = getCroppedCircleBitmap(bitmap);
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, ((int)(heightPixels*0.1)), ((int)(heightPixels*0.1)), false));

    }

    public void activateParticipation(){
        //add CurrentUser to ParseObject

        if (!statusParticipation) {
            if (listParticipants.size() <= maxMembers) {
                listParticipants.add(currentUser);
                eventObject.put("participants", listParticipants);
                eventObject.saveInBackground();



                changeParticipationToTrue();
            } else {
                Context context = getActivity();
                CharSequence text = "Sorry, but you cannot participate in this event. The maximum amount of participants has already been reached.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        } else {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");


                            query.getInBackground(currentUser.getString("eventId"), new GetCallback<ParseObject>() {
                                public void done(ParseObject object, ParseException queryException) {
                                    if (queryException == null) {

                                        removeUserFromList(object);
                                        statusParticipation = false;
                                        activateParticipation();
                                    } else {
                                        System.out.print("Object could not be received");
                                    }

                                }
                            });

                            break;

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
                deactivateParticipation();
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
                activateParticipation();
            }
        });

        ParseUser.getCurrentUser().put("eventId", R.string.detail_no_event);
        ParseUser.getCurrentUser().saveInBackground();
    }

    private void removeUserFromList(ParseObject eventObject){
        List<ParseUser> listParticipants = eventObject.getList("participants");
        listParticipants.remove(currentUser);
        eventObject.put("participants", listParticipants);
        eventObject.saveInBackground();
    }

    private void deactivateParticipation(){
        if (statusParticipation) {
            removeUserFromList(eventObject);
            changeParticipationToFalse();
        }
    }




    private void setUpMap(){

        if(map == null){

            Log.d("Main", "Map was not instantiated!");

            //Trying to fetch map, again
            //map = supportMapFragment.getMap();

        }else {
            map.setOnMarkerClickListener(this);

            Log.d("Main", "Map is instantiated");

            Location userPosition = getUserPosition();

            if(userPosition != null){

                Log.d("Main", "Last position was found");
                drawEvent();

                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                Log.d("Main", "Count of markers: " + markers.size());
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
        double latitude = markers.get(0).getPosition().latitude;
        double longitude = markers.get(0).getPosition().longitude;
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + ", " + longitude + "&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
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


