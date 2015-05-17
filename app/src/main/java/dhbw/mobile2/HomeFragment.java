package dhbw.mobile2;

import android.app.Fragment;
//import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import dhbw.mobile2.R;

public class HomeFragment extends Fragment implements GoogleMap.OnMapLongClickListener {

    public LocationManager locationManager;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;

    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {

            Log.d("Main", "Location changed");

            double lat = location.getLatitude();
            double lon = location.getLongitude();
            LatLng position = new LatLng(lat, lon);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(position)
                    .zoom(1)
                    .bearing(13)
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

    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setUpMapIfNeeded();
        map.setOnMapLongClickListener(this);

        return rootView;
    }

    private void setUpMapIfNeeded(){

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.location_map);
        map = mapFragment.getMap();

        map.setMyLocationEnabled(true);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);

        if(map == null){

            Log.d("Main", "Map was not instantiated!");

            //Trying to fetch map, again
            map = supportMapFragment.getMap();

        }else {

            Log.d("Main", "Map is instantiated");

            LocationManager locationManager = (LocationManager) getActivity()
                    .getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();

            Location userPosition = locationManager.getLastKnownLocation(
                    locationManager.getBestProvider(criteria, false)
            );

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

            }else{
                Log.d("Main", "No last position found");
            }
        }
    }//End of setUpMapIfNeeded

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
        //setMarker(point);
        setGeoFence(point);
    }

    private void setMarker(LatLng position){
        map.addMarker(new MarkerOptions()
                        .title("Testmarker")
                        .position(position)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        );
    }

    private void setGeoFence(LatLng position){

    }

}