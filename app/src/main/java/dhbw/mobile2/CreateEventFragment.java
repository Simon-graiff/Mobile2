package dhbw.mobile2;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;


public class CreateEventFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, OnClickListener {

    private EditText mEditText_title;
    private EditText mEditText_duration;
    private EditText mEditText_location;
    private EditText mEditText_maxMembers;
    private EditText mEditText_description;
    private Spinner mSpinner_category;
    private Button mButton_createEvent;
    private Button mButton_creteGeofence;
    private LocationManager lm;
    private Location lastLocation = null;
    static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;
    private ParseObject event = null;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Geofence> mGeofenceList = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;
    private View rootView;


    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setLocationDataInEventObject(lastLocation, location);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_create_event, container, false);

        mEditText_title = (EditText) rootView.findViewById(R.id.editText_title);
        mEditText_duration = (EditText) rootView.findViewById(R.id.editText_duration);
        mEditText_location = (EditText) rootView.findViewById(R.id.editText_location);
        mEditText_maxMembers = (EditText) rootView.findViewById(R.id.editText_maxMembers);
        mEditText_description = (EditText) rootView.findViewById(R.id.editText_FeedbackBody);
        mButton_createEvent = (Button) rootView.findViewById(R.id.button_CreateEvent);
        mButton_createEvent.setOnClickListener(this);
        mButton_creteGeofence = (Button) rootView.findViewById(R.id.button_createGeofence);
        mButton_creteGeofence.setOnClickListener(this);

        mSpinner_category = (Spinner) rootView.findViewById(R.id.SpinnerFeedbackType);
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        return rootView;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_create_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createEvent(View w){
        //start updating the location with locationListener-Object
        int maxMembers = 0;
        try{
            maxMembers = Integer.parseInt(mEditText_maxMembers.getText().toString());
        }catch(NumberFormatException nfe){
            Toast.makeText(getActivity().getBaseContext(), "MaxMembers is not a number!", Toast.LENGTH_LONG).show();
            return;
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        ParseUser user = ParseUser.getCurrentUser();
        ArrayList<ParseUser> participants = new ArrayList<ParseUser>();

        event = new ParseObject("Event");
        event.put("title", mEditText_title.getText().toString());
        event.put("description", mEditText_description.getText().toString());
        event.put("category", mSpinner_category.getSelectedItem().toString());
        event.put("locationName", mEditText_location.getText().toString());
        event.put("duration", mEditText_duration.getText().toString());
        event.put("maxMembers",maxMembers);
        event.put("participants", participants);
        event.put("creator", user);
    }

    public void createGeofence(View w){
        mGoogleApiClient.connect();
    }

    private PendingIntent getGeofencePendingIntent(){
        if( mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity().getBaseContext(), GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(getActivity().getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setLocationDataInEventObject(Location oldLocation, Location location) {
        Toast.makeText(getActivity().getBaseContext(), location.toString(), Toast.LENGTH_LONG).show();
        if(isBetterLocation(oldLocation, location)) {
            Toast.makeText(getActivity().getBaseContext(), "Better location found", Toast.LENGTH_LONG).show();
            lastLocation = location;
            event.put("longitude", location.getLongitude());
            event.put("latitude", location.getLatitude());
        }else {
            lm.removeUpdates(locationListener);
            lastLocation = null;
            event.saveInBackground();
            Toast.makeText(getActivity().getBaseContext(), "Event saved: " + event.toString(), Toast.LENGTH_LONG).show();

            Fragment fragment = new HomeFragment();
            getFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).commit();
        }
    }

    private boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if(oldLocation == null) {
            return true;
        }

        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();
        Toast.makeText(getActivity().getBaseContext(), newLocation.getTime() + ";" + oldLocation.getTime() + ";"+ newLocation.getAccuracy() + ";"+oldLocation.getAccuracy(), Toast.LENGTH_LONG).show();
        if(isMoreAccurate && isNewer) {
            // More accurate and newer is always better.
            return true;
        } else if(isMoreAccurate && !isNewer) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // If time difference is not greater then allowed threshold we accept it.
            if(timeDifference > - TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mGeofenceList.add(new Geofence.Builder()
                .setRequestId("Test")
                .setCircularRegion(10, 10, 250) //long,lat,radius
                .setExpirationDuration(10000000)//millis
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                mGeofenceList,
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getActivity().getBaseContext(),"Connection Suspended", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity().getBaseContext(), "Connection Failed: "+connectionResult, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResult(Status status) {
        Toast.makeText(getActivity().getBaseContext(), "Result: "+status.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if (view == mButton_createEvent){
            createEvent(view);
        }else if(view == mButton_creteGeofence){
            createGeofence(view);
        }
    }
}
