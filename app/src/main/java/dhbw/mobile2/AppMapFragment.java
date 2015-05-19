package dhbw.mobile2;

import android.app.Fragment;
//import android.support.v4.app.Fragment;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
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

import java.util.List;

public class AppMapFragment extends Fragment
        implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    public LocationManager locationManager;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    MapView myMapView;

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
        myMapView = (MapView) rootView.findViewById(R.id.myMapView);
        myMapView.onCreate(savedInstanceState);

        map = myMapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);

        map.setMyLocationEnabled(true);
        map.setOnMapLongClickListener(this);
        map.setOnMarkerClickListener(this);

        MapsInitializer.initialize(this.getActivity());

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 500, 5, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500,0,locationListener);

        setUpMap();


        /*try{
            MapsInitializer.initialize(this.getActivity());
        }catch (GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        }*/

        return rootView;
    }

    @Override
    public void onResume(){
        myMapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        myMapView.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        myMapView.onLowMemory();
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

        Log.d("Main", "Getting user's position");

        Location userPosition = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(userPosition!=null) {
            LatLng pos = new LatLng(userPosition.getLatitude(), userPosition.getLongitude());
            String title = "Your position";
            String color = "green";
            drawMarker(pos, title, color);
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
        drawMarker(point, s, c);
    }

    private void drawMarker(LatLng position, String title, String color){

        if(color.equals("red")) {
            map.addMarker(new MarkerOptions()
                            .title(title)
                            .position(position)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            );
        }else if(color.equals("green")){
            map.addMarker(new MarkerOptions()
                            .title(title)
                            .position(position)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            );
        }
    }

    private void drawEvents(){

        Log.d("Main", "Entered drawEvents");

        //Creating ParseGeoPoint with user's current location
        Location userLocation = getUserPosition();
        ParseGeoPoint point = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
        ParseObject user = new ParseObject("User");
        user.put("location", point);

        Log.d("Main", "Created ParseObject");

        //Preparing query
        ParseGeoPoint queryParameter = (ParseGeoPoint) user.get("location");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereNear("location", queryParameter);
        query.whereWithinKilometers("location", queryParameter, 5.0);

        Log.d("Main", "Prepared query and starting execution");

        //Executing query
        final List<ParseObject> list = null;
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> eventList, ParseException e) {
                if (e == null) {

                    Log.d("Main", "Retrieved " + eventList.size() + " events");

                    if (!eventList.isEmpty()) {
                        for (int i = 1; i < eventList.size(); i++) {
                            //list.add(eventList.get(i));
                            Log.d("Main", "Added an object");

                            double tmpLat = list.get(i).getDouble("latitude");
                            double tmpLng = list.get(i).getDouble("longitude");
                            LatLng tmpLatLng = new LatLng(tmpLat, tmpLng);

                            String tmpTitle = list.get(i).getString("title");
                            String color = "red";
                            //String eventID = list.get(i).getString("id");
                            drawMarker(tmpLatLng, tmpTitle, color);
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

        Log.d("Main", "You've tapped a marker at: "+m.getPosition());
        Log.d("Main", "ID is: " + m.getId());

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("eventId", "l9lvvbNByv");
        editor.commit();

        Log.d("Main", "Executed commit");

        return true;
    }
}