package dhbw.mobile2;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
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
import java.util.Calendar;
import java.util.Date;
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
    private Button mButton_createGeoFence;
    private Button mButton_test;
    private Button mButton_removeGeoFence;
    private ParseObject event = null;
    private ArrayList<Geofence> mGeoFenceList = new ArrayList<>();
    private PendingIntent mGeoFencePendingIntent;
    private View rootView;
    private GoogleApiClient mGoogleApiClient;
    private ParseUser mUser;
    private Location mLocation;
    private String mPendingRequestId;
    private String mCurrentGeoFenceId;
    private Boolean mCreatingGeoFence = false;
    private Boolean mDeletingGeoFence = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_create_event, container, false);

        mEditText_title = (EditText) rootView.findViewById(R.id.editText_title);
        mEditText_duration = (EditText) rootView.findViewById(R.id.editText_duration);
        mEditText_duration.setOnClickListener(this);
        mEditText_location = (EditText) rootView.findViewById(R.id.editText_location);
        mEditText_maxMembers = (EditText) rootView.findViewById(R.id.editText_maxMembers);
        mEditText_description = (EditText) rootView.findViewById(R.id.editText_FeedbackBody);
        mButton_createEvent = (Button) rootView.findViewById(R.id.button_CreateEvent);
        mButton_createEvent.setOnClickListener(this);
        mButton_createGeoFence = (Button) rootView.findViewById(R.id.button_createGeofence);
        mButton_createGeoFence.setOnClickListener(this);
        mButton_test = (Button) rootView.findViewById(R.id.button_test);
        mButton_test.setOnClickListener(this);
        mButton_removeGeoFence = (Button) rootView.findViewById(R.id.button_removeGeofence);
        mButton_removeGeoFence.setOnClickListener(this);

        mUser = ParseUser.getCurrentUser();

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
        ParseObject geoFence = new ParseObject("GeoFence");

        ParseGeoPoint geoPoint = new ParseGeoPoint();
        geoPoint.setLatitude(mLocation.getLatitude());
        geoPoint.setLongitude(mLocation.getLongitude());
        geoFence.put("center", geoPoint);
        geoFence.put("user", ParseUser.getCurrentUser());
        geoFence.put("requestId", mPendingRequestId);
        mCurrentGeoFenceId = mPendingRequestId;
        mPendingRequestId = null;
        geoFence.saveInBackground();
        mCreatingGeoFence = false;
        changeButtons();
    }

    public void createEvent() {
        //start updating the location with locationListener-Object
        int maxMembers;
        try {
            maxMembers = Integer.parseInt(mEditText_maxMembers.getText().toString());

            if(!mEditText_duration.getText().toString().contains("Until:")){
                Toast.makeText(getActivity().getBaseContext(), "Please set a duration!", Toast.LENGTH_LONG).show();
                return;
            }

        } catch (NumberFormatException nfe) {
            Toast.makeText(getActivity().getBaseContext(), "MaxMembers is not a number!", Toast.LENGTH_LONG).show();
            return;
        }

        ParseGeoPoint geoPoint = new ParseGeoPoint();
        geoPoint.setLatitude(mLocation.getLatitude());
        geoPoint.setLongitude(mLocation.getLongitude());

        ArrayList<ParseUser> participants = new ArrayList<>();

        String endTime = mEditText_duration.getText().toString().substring(7);
        int hourOfEnd = Integer.parseInt(endTime.substring(0, 2));
        int minuteOfEnd = Integer.parseInt(endTime.substring(3));
        Boolean endsNextDay = hourOfEnd < Calendar.getInstance().HOUR_OF_DAY;
        Date endDate = new Date();
        endDate.setHours(hourOfEnd);
        endDate.setMinutes(minuteOfEnd);
        if(endsNextDay)
            endDate.setDate(Calendar.getInstance().DAY_OF_MONTH+1);

        event = new ParseObject("Event");
        event.put("title", mEditText_title.getText().toString());
        event.put("description", mEditText_description.getText().toString());
        event.put("category", mSpinner_category.getSelectedItem().toString());
        event.put("locationName", mEditText_location.getText().toString());
        event.put("duration", endDate);
        event.put("maxMembers", maxMembers);
        event.put("participants", participants);
        event.put("creator", mUser);
        event.put("geoPoint", geoPoint);
        event.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseUser.getCurrentUser().put("eventId", event.getObjectId());
                ParseUser.getCurrentUser().saveInBackground();
            }
        });

        Toast.makeText(getActivity().getBaseContext(), "Have fun!", Toast.LENGTH_LONG).show();

        Fragment fragment = new MapFragment();
        getFragmentManager().beginTransaction().replace(R.id.frame_container, fragment).commit();
    }


    public void createGeofence() {
        mCreatingGeoFence = true;
        String requestId = mLocation.getLongitude() + ";" + mLocation.getLatitude() + ";" + mUser.getObjectId();
        mGeoFenceList.add(new Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(10, 10, 2000000) //long,lat,radius
                .setExpirationDuration(Geofence.NEVER_EXPIRE)//millis
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
        mPendingRequestId = requestId;

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingRequest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private void sendEventRequest() {
        Map<String, Object> param = new HashMap<>();
        param.put("longitude", mLocation.getLongitude());
        param.put("latitude", mLocation.getLatitude());
        param.put("userId", mUser.getObjectId());


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

    private void checkIfInGeoFence() {
        Map<String, Object> param = new HashMap<>();
        param.put("longitude", mLocation.getLongitude());
        param.put("latitude", mLocation.getLatitude());
        param.put("userId", mUser.getObjectId());

        ParseCloud.callFunctionInBackground("checkIfInGeoFence", param, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> stringObjectMap, ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity().getBaseContext(), stringObjectMap.get("inGeoFence").toString(), Toast.LENGTH_LONG).show();
                    Boolean isInGeoFence = (Boolean) stringObjectMap.get("inGeoFence");

                    if (isInGeoFence) {
                        mButton_createGeoFence.setEnabled(false);
                        mButton_removeGeoFence.setEnabled(true);
                        Toast.makeText(getActivity().getBaseContext(), stringObjectMap.get("data").toString(), Toast.LENGTH_LONG).show();
                        mCurrentGeoFenceId = stringObjectMap.get("data").toString();
                    } else {
                        mButton_createGeoFence.setEnabled(true);
                        mButton_removeGeoFence.setEnabled(false);
                    }
                } else {
                    Toast.makeText(getActivity().getBaseContext(), "error im CloudCode", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    private void removeGeoFence() {
        mDeletingGeoFence = true;
        ArrayList<String> geoFencesToRemove = new ArrayList<>();
        geoFencesToRemove.add(mCurrentGeoFenceId);
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geoFencesToRemove).setResultCallback(this);
    }

    private void removeGeoFenceFromDatabase() {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", mUser.getObjectId());
        param.put("geoFenceId", mCurrentGeoFenceId);

        ParseCloud.callFunctionInBackground("removeGeoFence", param, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> stringObjectMap, ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity().getBaseContext(), "GeoFenceDeleted", Toast.LENGTH_LONG).show();
                    changeButtons();
                    mDeletingGeoFence = false;
                } else {
                    Toast.makeText(getActivity().getBaseContext(), "error im CloudCode", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    public void showTimePickerDialog() {
        DialogFragment timePickFragment = new TimePickerFragment();
        Bundle args = new Bundle();
        timePickFragment.show(getActivity().getFragmentManager(),"TEST");
    }

    @Override
    public void onClick(View view) {
        if (view == mButton_createEvent) {
            createEvent();
        } else if (view == mButton_createGeoFence) {
            createGeofence();
        } else if (view == mButton_test) {
            sendEventRequest();
        } else if (view == mButton_removeGeoFence) {
            removeGeoFence();
        } else if (view == mEditText_duration) {
            showTimePickerDialog();
        }
    }

    @Override
    public void onResult(Status status) {
        Toast.makeText(getActivity().getBaseContext(), "Result: " + status.toString(), Toast.LENGTH_LONG).show();
        if (mCreatingGeoFence) {
            submitGeofenceToDatabase();
        } else if (mDeletingGeoFence) {
            removeGeoFenceFromDatabase();
        } else {
            Toast.makeText(getActivity().getBaseContext(), "Error Error Error", Toast.LENGTH_LONG).show();
        }
        mDeletingGeoFence = false;
        mCreatingGeoFence = false;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("API", "Connected");
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        checkIfInGeoFence();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("API", "ConnectedSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity().getBaseContext(), "ERROR CONNECTING GOOGLE API CLIENT", Toast.LENGTH_LONG).show();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeoFencePendingIntent != null) {
            return mGeoFencePendingIntent;
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

    private void changeButtons() {
        mButton_removeGeoFence.setEnabled(!mButton_removeGeoFence.isEnabled());
        mButton_createGeoFence.setEnabled(!mButton_createGeoFence.isEnabled());
    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private EditText toEdit;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time plus three hours as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY) + 3;
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,true);
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            EditText toEdit = (EditText) getActivity().findViewById(R.id.editText_duration);
            String hourFiller = "";
            if(hourOfDay < 10)
                hourFiller = "0";

            String minuteFiller = "";
            if(minute < 10)
                minuteFiller = "0";

            toEdit.setText("Until: " + hourFiller + hourOfDay + ":" + minuteFiller + minute);
        }
    }

}