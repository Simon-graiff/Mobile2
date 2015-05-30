package dhbw.mobile2;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CreateEventFragment extends Fragment
        implements ResultCallback<Status>, OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText mEditText_title;
    private EditText mEditText_duration;
    private EditText mEditText_location;
    private EditText mEditText_maxMembers;
    private EditText mEditText_description;
    private Spinner mSpinner_category;
    private Button mButton_createEvent;
    private Button mButton_creteGeoFence;
    private Button mButton_test;
    private LocationManager lm;
    private Location lastLocation = null;
    static final int TIME_DIFFERENCE_THRESHOLD = 60 * 1000;
    private ParseObject event = null;
    private ArrayList<Geofence> mGeoFenceList = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;
    private View rootView;
    private GoogleApiClient mGoogleApiClient;

    private Boolean mCreatingEventObject = false;
    private Boolean mSearchingEvents = false;
    private Boolean mCreatingGeofence = false;

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
        mButton_creteGeoFence = (Button) rootView.findViewById(R.id.button_createGeofence);
        mButton_creteGeoFence.setOnClickListener(this);
        mButton_test = (Button) rootView.findViewById(R.id.button_test);
        mButton_test.setOnClickListener(this);

        mSpinner_category = (Spinner) rootView.findViewById(R.id.SpinnerFeedbackType);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        return rootView;
    }

    private void submitGeofenceToDatabase() {
        ParseObject geofence = new ParseObject("GeoFence");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        ParseGeoPoint geoPoint = new ParseGeoPoint();
        geoPoint.setLatitude(location.getLatitude());
        geoPoint.setLongitude(location.getLongitude());
        geofence.put("geoPoint", geoPoint);
        geofence.put("user", ParseUser.getCurrentUser());
        geofence.saveInBackground();
    }

    public void createEvent(){
        //start updating the location with locationListener-Object
        int maxMembers;
        try{
            maxMembers = Integer.parseInt(mEditText_maxMembers.getText().toString());
        }catch(NumberFormatException nfe){
            Toast.makeText(getActivity().getBaseContext(), "MaxMembers is not a number!", Toast.LENGTH_LONG).show();
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        ParseGeoPoint geoPoint = new ParseGeoPoint();
        geoPoint.setLatitude(location.getLatitude());
        geoPoint.setLongitude(location.getLongitude());

        ArrayList<ParseUser> participants = new ArrayList<>();

        event = new ParseObject("Event");
        event.put("title", mEditText_title.getText().toString());
        event.put("description", mEditText_description.getText().toString());
        event.put("category", mSpinner_category.getSelectedItem().toString());
        event.put("locationName", mEditText_location.getText().toString());
        event.put("duration", mEditText_duration.getText().toString());
        event.put("maxMembers", maxMembers);
        participants.add(ParseUser.getCurrentUser());
        event.put("participants", participants);
        event.put("creator", ParseUser.getCurrentUser());
        event.put("geoPoint", geoPoint);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseUser.getCurrentUser().put("eventId", event.getObjectId());
                ParseUser.getCurrentUser().saveInBackground();
            }
        });
        mCreatingEventObject = false;


        Toast.makeText(getActivity().getBaseContext(), "Have fun!", Toast.LENGTH_LONG).show();

        Fragment fragment = new MapFragment();
        getFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).commit();
    }


    public void createGeofence (){
        mGeoFenceList.add(new Geofence.Builder()
                .setRequestId("Test")
                .setCircularRegion(10, 10, 2000000) //long,lat,radius
                .setExpirationDuration(Geofence.NEVER_EXPIRE)//millis
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private void sendEventRequest(){
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Map<String,Object> param = new HashMap<>();
        param.put("longitude",location.getLongitude());
        param.put("latitude",location.getLatitude());
        param.put("userId", ParseUser.getCurrentUser().getObjectId());


        ParseCloud.callFunctionInBackground("getNearEvents", param, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> stringObjectMap, ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity().getBaseContext(), stringObjectMap.get("events").toString(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity().getBaseContext(), "error im CloudCode", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == mButton_createEvent){
            createEvent();
        }else if(view == mButton_creteGeoFence){
            createGeofence();
        }else if(view==mButton_test){
            sendEventRequest();
        }
    }

    @Override
    public void onResult(Status status) {
        Toast.makeText(getActivity().getBaseContext(), "Result: "+status.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("API", "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("API", "ConnectedSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity().getBaseContext(), "ERROR CONNECTING GOOGLE API CLIENT", Toast.LENGTH_LONG).show();
    }

    private PendingIntent getGeofencePendingIntent(){
        if( mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity().getBaseContext(), GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(getActivity().getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeoFenceList);
        return builder.build();
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

    @Override
    public void onResume(){
        super.onResume();

        ListView mDrawerList;
        mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
        mDrawerList.setItemChecked(2, true);
        mDrawerList.setSelection(2);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Create new event");
    }
}
