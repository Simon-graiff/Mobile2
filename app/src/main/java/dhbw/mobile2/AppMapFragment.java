package dhbw.mobile2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AppMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    public LocationManager locationManager;
    private GoogleMap map = null;
    MapView myMapView = null;
    public List<EventManagerItem> eventManager = new ArrayList<>();
    public List<ParseUser> participantList = new ArrayList<>();

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            Log.d("Main", "Location changed");

            double lat = location.getLatitude();
            double lon = location.getLongitude();
            LatLng position = new LatLng(lat, lon);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)
                    .zoom(16)
                    .tilt(40)
                    .build();

            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            drawEvents();
        }



        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    public AppMapFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_app_map, container, false);

        //MapsInitializer.initialize(this.getActivity());

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
       // locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 500, 5, locationListener);
       // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);
        locationManager.requestSingleUpdate(locationManager.GPS_PROVIDER, locationListener, null);

        /*try{
            MapsInitializer.initialize(this.getActivity());
        }catch (GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        }*/

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        try {
            MapsInitializer.initialize(this.getActivity());
        }catch (Exception e){
            e.printStackTrace();
        }

        //myMapView = (MapView) rootView.findViewById(R.id.myMapView);
        myMapView = (MapView) getView().findViewById(R.id.myMapView);
        myMapView.onCreate(savedInstanceState);

        map = myMapView.getMap();

        if(map!=null) {
            map.getUiSettings().setMyLocationButtonEnabled(false);

            map.setMyLocationEnabled(true);
            map.setOnMarkerClickListener(this);

            setUpMap();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(myMapView!=null){
            myMapView.onResume();
        }
        locationManager.requestSingleUpdate(locationManager.GPS_PROVIDER, locationListener, null);
      //  locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 500, 5, locationListener);
       // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);


        ListView mDrawerList;
        mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
        mDrawerList.setItemChecked(1, true);
        mDrawerList.setSelection(1);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Map");
    }

    @Override
    public void onPause() {
        if (map != null)
            myMapView.onPause();
        super.onPause();
        locationManager.removeUpdates(locationListener);

        ((MainScreen) getActivity()).setMapShown(false);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDestroy(){
        if(map!=null) {
            myMapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        if(map!=null) {
            myMapView.onLowMemory();
        }
    }

    private void setUpMap(){

        if(map == null){

            Log.d("Main", "Map was not instantiated!");

            //Trying to fetch map, again
            //map = supportMapFragment.getMap();

        }else {

            Log.d("Main", "Map is instantiated");

            Location userPosition = getUserPosition();

            if(userPosition != null){

                Log.d("Main", "Last position was found");

                LatLng coordinates = new LatLng(userPosition.getLatitude(),
                        userPosition.getLongitude());

                //map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 13));

                double lat = userPosition.getLatitude();
                double lon = userPosition.getLongitude();
                LatLng position = new LatLng(lat, lon);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(position)
                        .zoom(16)
                        .tilt(40)
                        .build();

                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                drawEvents();

            }else{
                Log.d("Main", "No last position found");
            }
        }
    }//End of setUpMap

    private Location getUserPosition() {
        locationManager.requestSingleUpdate(locationManager.GPS_PROVIDER, locationListener, null);
        Location userPosition = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return userPosition;
    }

    private void drawMarker(LatLng position, String title, String eventID){

        Marker m = map.addMarker(new MarkerOptions()
                        .title(title)
                        .position(position)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        );

        eventManager.add(new EventManagerItem(m.getId(), eventID, position));

    }

    private void drawEvents(){

        //Create ParseGeoPoint with user's current location
        Location userLocation = getUserPosition();
        ParseGeoPoint point = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
        ParseObject user = new ParseObject("User");
        user.put("location", point);

        //Prepare filter options
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        final String sport = sharedPref.getString("Sport", null);
        final String music = sharedPref.getString("Music", null);
        final String chilling = sharedPref.getString("Chilling", null);
        final String dancing = sharedPref.getString("Dancing", null);
        final String videoGames = sharedPref.getString("Video Games", null);
        final String food = sharedPref.getString("Food", null);
        final boolean mixedGenders = sharedPref.getBoolean("MixedGenders", true);

        //Prepare query
        ParseGeoPoint queryParameter = (ParseGeoPoint) user.get("location");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereWithinKilometers("geoPoint", queryParameter, 5);

        Log.d("Main", "Waiting for Callback...");

        //Executing query
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> eventList, ParseException e) {
                if (e == null) {

                    Log.d("Main", "Retrieved " + eventList.size() + " events");

                    if (!eventList.isEmpty()) {

                        //Query: Get all events in the reach of five kilometers
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("FilteredEvents");
                        query.fromLocalDatastore();

                        Log.d("Main", "Waiting for Callback...");
                        ArrayList<ParseObject> eventArray = new ArrayList<>();

                        //Executing query
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> eventList, ParseException e) {
                                Log.d("Main", "Received " + eventList.size() + " events");
                                if (e == null) {
                                    for (int i=0; i < eventList.size(); i++){
                                        eventList.get(i).unpinInBackground();
                                    }

                                } else {
                                    Log.d("Main", "No events existed in background");
                                }
                            }
                        });
                        for (int i = 0; i < eventList.size(); i++) {
                            //Extraction of latitude and longitude from recieved GeoPoints
                            ParseGeoPoint tmpPoint = (ParseGeoPoint) eventList.get(i).get("geoPoint");
                            double tmpLat = tmpPoint.getLatitude();
                            double tmpLng = tmpPoint.getLongitude();
                            LatLng tmpLatLng = new LatLng(tmpLat, tmpLng);

                            participantList = eventList.get(i).getList("participants");
                            boolean tmpMale=false;
                            boolean tmpFemale=false;
                            for(int j=0; j<participantList.size();j++){
                                try {
                                    if (participantList.get(j).fetchIfNeeded().getString("gender").equals("male")) {
                                        tmpMale = true;
                                    } else {
                                        tmpFemale = true;
                                    }
                                }catch (ParseException exception){
                                    exception.printStackTrace();
                                }
                            }

                            //If tmpMale & tmpFemale are both true then the participants of the
                            //event are from both genders. In case the user wants to avoid that
                            //a preference check is needed
                            if(tmpMale==true && tmpFemale==true && mixedGenders==false){
                                Log.d("Main", "There is a mixed gender group");
                            }else{
                                Log.d("Main", "In der if");

                                //Prepare necessary information and draw markers to map
                                String tmpTitle = eventList.get(i).getString("title");
                                String category = eventList.get(i).getString("category");
                                String eventID = eventList.get(i).getObjectId();
                                Log.d("Main", "eventID =" + eventID);

                                //An event can only belong to one category. If any of the following
                                //conditions fails, there is no need for checking the others.


                                if(!category.equals(sport)){
                                    if(!category.equals(music)){
                                        if(!category.equals(chilling)){
                                            if(!category.equals(dancing)){
                                                if(!category.equals(videoGames)){
                                                    if(!category.equals(food)){
                                                        try {
                                                            eventArray.add(eventList.get(i).fetchIfNeeded());
                                                            Log.d("Main", "geoPoint: " + eventArray.get(eventArray.size() - 1).getParseGeoPoint("geoPoint"));
                                                        } catch (ParseException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                        Log.d("Main", "Drew marker: "+tmpTitle);
                                                        drawMarker(tmpLatLng, tmpTitle, eventID);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }//End for-loop for eventList
                        ParseObject listOfFilteredEvents = new ParseObject("FilteredEvents");
                        listOfFilteredEvents.put("list", eventArray);
                        listOfFilteredEvents.pinInBackground();
                        ((MainScreen) getActivity()).setMapShown(true);
                        getActivity().invalidateOptionsMenu();
                    }

                } else {
                    Log.d("Main", "Error on callback: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(final Marker m){

        String markerID = m.getId();
        String eventID = "Not found";

        //Retrieve eventID from eventManager with markerID
        for(int i=0; i<eventManager.size(); i++){
            if(eventManager.get(i).getMarkerID().equals(markerID)){
                eventID = eventManager.get(i).getEventID();
            }
        }

        if(!eventID.equals("Not found")){
            //Save eventID in SharedPreferences
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("eventId", eventID);
            editor.apply();

            //Direction to EventDetailFragment
            Fragment eventDetailFragment = new EventDetailFragment();
            /*FragmentManager fragmentManager = getActivity().getFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .add(R.id.frame_container, eventDetailFragment, "eventDetailFragment")
                    .addToBackStack(null)
                    //.replace(R.id.frame_container, eventDetailFragment)
                    .commit();*/
            FragmentTransaction transaction = getActivity().getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, eventDetailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        return true;
    }

    public void showListAsActionItem(){
        MenuInflater actionBar = getActivity().getMenuInflater();
    }
}