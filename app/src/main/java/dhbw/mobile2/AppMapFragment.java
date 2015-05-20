package dhbw.mobile2;

import android.app.Fragment;
//import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AppMapFragment extends Fragment
        implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    public LocationManager locationManager;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map = null;
    MapView myMapView = null;
    public List<EventManagerItem> eventManager = new ArrayList<EventManagerItem>();

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
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 500, 5, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 0, locationListener);

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
            map.setOnMapLongClickListener(this);
            map.setOnMarkerClickListener(this);

            setUpMap();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(map!=null){
            myMapView.onResume();
        }

    }

    @Override
    public void onPause() {
        if (map != null)
            myMapView.onPause();
        super.onPause();
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

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(coordinates)
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

    private Location getUserPosition(){

        Location userPosition = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(userPosition!=null) {
            LatLng pos = new LatLng(userPosition.getLatitude(), userPosition.getLongitude());
            String title = "Your position";
            String color = "green";
            String id = "Your position";
            drawMarker(pos, title, color, id);
        }

        return userPosition;
    }

    private GoogleMap.CancelableCallback cancelableCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
            scroll();
        }

        @Override
        public void onCancel() {

        }

        //Reducing scroll speed to reduce amount of necessary data
        public void scroll(){
            map.animateCamera(CameraUpdateFactory.scrollBy(10, -10));
        }
    };

    //Click listener for long taps on map. Has to be public, since overriding a foreign method.
    @Override
    public void onMapLongClick(LatLng point){
        Log.d("Main", "Map was tapped on:"+point);
        String s = "For test purpose only";
        String c = "red";
        String i = "Testmarker";
        drawMarker(point, s, c, i);
    }

    private void drawMarker(LatLng position, String title, String color, String eventID){

        if(color.equals("red")) {
            Marker m = map.addMarker(new MarkerOptions()
                            .title(title)
                            .position(position)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );

            eventManager.add(new EventManagerItem(m.getId(), eventID));

        }else if(color.equals("green")){
            Marker m = map.addMarker(new MarkerOptions()
                            .title(title)
                            .position(position)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );

            eventManager.add(new EventManagerItem(m.getId(), eventID));
        }
    }

    private void drawEvents(){

        //Creating ParseGeoPoint with user's current location
        Location userLocation = getUserPosition();
        ParseGeoPoint point = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
        ParseObject user = new ParseObject("User");
        user.put("location", point);

        //Preparing query
        ParseGeoPoint queryParameter = (ParseGeoPoint) user.get("location");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereWithinKilometers("geoPoint", queryParameter, 5);

        Log.d("Main", "Waiting for Callback...");

        //Executing query
        List<ParseObject> list = null;
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> eventList, ParseException e) {
                if (e == null) {

                    Log.d("Main", "Retrieved " + eventList.size() + " events");

                    if (!eventList.isEmpty()) {
                        for (int i = 0; i < eventList.size(); i++) {
                            //list.add(eventList.get(i));
                            Log.d("Main", "Added an object");

                            ParseGeoPoint tmpPoint = (ParseGeoPoint) eventList.get(i).get("geoPoint");
                            double tmpLat = tmpPoint.getLatitude();
                            double tmpLng = tmpPoint.getLongitude();
                            LatLng tmpLatLng = new LatLng(tmpLat, tmpLng);

                            String tmpTitle = eventList.get(i).getString("title");
                            String color = "red";
                            String eventID = eventList.get(i).getObjectId();
                            Log.d("Main", "eventID = "+ eventID);
                            drawMarker(tmpLatLng, tmpTitle, color, eventID);
                        }
                    }

                } else {
                    Log.d("Main", "Error: " + e.getMessage());
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

        //Save eventID in SharedPreferences
        if(!eventID.equals("Not found")){
            if(!eventID.equals("Your position")) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("eventId", eventID);
                editor.commit();

                Fragment eventDetailFragment = new EventDetailFragment();
                //ChildFragmentManager
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.frame_container, eventDetailFragment).commit();
            }
        }

        return true;
    }
}