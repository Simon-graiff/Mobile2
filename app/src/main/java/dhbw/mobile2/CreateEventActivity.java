package dhbw.mobile2;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.ParseObject;


public class CreateEventActivity extends ActionBarActivity {

    private EditText mEditText_title;
    private EditText mEditText_duration;
    private EditText mEditText_location;
    private EditText mEditText_maxMembers;
    private EditText mEditText_description;
    private Spinner mSpinner_category;
    private LocationManager lm;
    private Location lastLocation = null;
    static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;
    private ParseObject event = null;


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        mEditText_title = (EditText) findViewById(R.id.editText_title);
        mEditText_duration = (EditText) findViewById(R.id.editText_duration);
        mEditText_location = (EditText) findViewById(R.id.editText_location);
        mEditText_maxMembers = (EditText) findViewById(R.id.editText_maxMembers);
        mEditText_description = (EditText) findViewById(R.id.editText_FeedbackBody);
        mSpinner_category = (Spinner) findViewById(R.id.SpinnerFeedbackType);
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_event, menu);
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
        //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);

        event = new ParseObject("Event");
        event.put("title", mEditText_title.getText().toString());
        event.put("description", mEditText_description.getText().toString());
        event.put("category", mSpinner_category.getSelectedItem().toString());
        event.put("locationName", mEditText_location.getText().toString());
        event.put("duration", mEditText_duration.getText().toString());
        event.put("maxMembers", mEditText_maxMembers.getText().toString());
        event.saveInBackground();
    }

    private void setLocationDataInEventObject(Location oldLocation, Location location) {
        Toast.makeText(getApplicationContext(), location.toString(), Toast.LENGTH_LONG).show();
        if(isBetterLocation(oldLocation, location)) {
            Toast.makeText(getApplicationContext(), "Better location found", Toast.LENGTH_LONG).show();
            lastLocation = location;
            event.put("longitude", location.getLongitude());
            event.put("latitude", location.getLatitude());
        }else {
            lm.removeUpdates(locationListener);
            lastLocation = null;
            event.saveInBackground();
            Toast.makeText(getApplicationContext(), "Event saved: " + event.toString(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
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
        Toast.makeText(getApplicationContext(), newLocation.getTime() + ";" + oldLocation.getTime() + ";"+ newLocation.getAccuracy() + ";"+oldLocation.getAccuracy(), Toast.LENGTH_LONG).show();
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
}
