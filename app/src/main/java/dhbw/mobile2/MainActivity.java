package dhbw.mobile2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements ListEventsFragment.OnFragmentInteractionListener, ParticipantsListFragment.OnParticipantInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    //NavDrawer title, stored app title, menu items for slide menu
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] navMenuTitles;

    private boolean statusParticipation = false;
    private boolean cancelOtherEvent = false;

    private boolean mapShown = false;
    private boolean listShown = false;

    private Boolean mCreatingGeoFence = false;
    private Boolean mDeletingGeoFence = false;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private ArrayList<Geofence> mGeoFenceList = new ArrayList<>();
    private PendingIntent mGeoFencePendingIntent;
    private String mPendingRequestId;
    private String mCurrentGeoFenceId;
    private Boolean mInGeofence = false;

    //currentUser
    ParseUser currentUser;

    //HelperObject
    HelperClass helperObject = new HelperClass();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        TypedArray navMenuIcons;
        ArrayList<NavDrawerItem> navDrawerItems;
        NavDrawerListAdapter adapter;

        //Check if User is logged in
        if(ParseUser.getCurrentUser() == null){
            //If the user is not logged in call the loginActiviy
            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
            startActivity(intent);
        } else {
            try {
                currentUser = ParseUser.getCurrentUser().fetchIfNeeded();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        mTitle = mDrawerTitle = getTitle();

        //Load slide menu items & icons
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
        navDrawerItems = new ArrayList<>();

        //Adding items to array, counter is deactivated:
        //0 = Profile
        //1 = Map
        //2 = Create new Event or MyEvent
        //3 = Settings
        //4 = Logout

        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));

        //Recycle the typed array
        navMenuIcons.recycle();

        //Setting the nav drawer list adapter
        if(ParseUser.getCurrentUser()!=null){
            adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
            mDrawerList.setAdapter(adapter);
        }


        //Enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer, //Menu toggle icon
                R.string.app_name, //Description if drawer opens
                R.string.app_name //Description if drawer closes
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);

                //Is called on onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);

                mDrawerList.invalidateViews();

                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                //Calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            //Setting MapFragment as default fragment
            Fragment fragment = new EventMap();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        }

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onParticipantInteraction(String id) {

    }

    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            displayView(position);
        }
    }

    private void displayView(int position) {

        Fragment fragment = null;

        if(position==0) {
            fragment = ProfileFragment.newInstance(ParseUser.getCurrentUser().getObjectId());
        }else if(position==1){
            fragment = new EventMap();
        }else if(position==2){
            checkParticipation();
            if (statusParticipation){

                mDrawerList.setItemChecked(2, true);
                mDrawerList.setSelection(2);
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("myEventActivated", true);
                String eventId = "";
                try {
                    eventId = ParseUser.getCurrentUser().fetch().getString("eventId");
                    editor.putString("eventId", eventId);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                editor.apply();
                Log.d("Main", "eventId: " + eventId);

                fragment = new EventDetailFragment();
            } else {
                fragment = new CreateEventFragment();
            }


        }else if(position==3){
            fragment = new EventFilterFragment();
        }else if(position==4){
            fragment = new LogoutFragment();
        }

        // If called Fragment is the logout fragment just add the fragment to the other instead of replacing it
        // because only the showDialog needs to be displayed and it shall be overlapping the existing fragment
        if (fragment != null && fragment.getClass() == LogoutFragment.class) {
            Fragment.instantiate(getApplicationContext(), LogoutFragment.class.getName(), new Bundle());
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().add(R.id.frame_container, fragment).commit();
        }

        if (fragment != null && fragment.getClass() != LogoutFragment.class) {

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            setTitle(navMenuTitles[position]);
            mDrawerLayout.closeDrawer(mDrawerList);

            helperObject.switchToFragment(getFragmentManager(), fragment);

        } else {
            //Error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    private void createEventFragment(){
        Fragment fragment = new CreateEventFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

            //Update selected item and title, then close the drawer
            mDrawerList.setItemChecked(2, true);
            mDrawerList.setSelection(2);
            setTitle(navMenuTitles[2]);
            mDrawerLayout.closeDrawer(mDrawerList);

        }

    private void checkParticipation(){
        String eventIdOfUser = ParseUser.getCurrentUser().getString("eventId");
        if (eventIdOfUser != null){
            statusParticipation = !eventIdOfUser.equals("no_event");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //getMenuInflater().inflate(R.menu.main, menu);
        getMenuInflater().inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_list_events:
                //switch to List of Events
                helperObject.switchToFragment(getFragmentManager(), new ListEventsFragment());
                return true;
            case R.id.action_map:
                //switch to Map
                helperObject.switchToFragment(getFragmentManager(), new EventMap());
            case R.id.action_settings:
                return true;
            case R.id.action_notify_activate:
                createGeofence();
                return true;
            case R.id.action_notify_deactivate:
                removeGeoFence();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //Called when invalidateOptionsMenu() is triggered
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*// if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);*/
        menu.findItem(R.id.action_list_events).setVisible(mapShown);
        menu.findItem(R.id.action_map).setVisible(listShown);
        menu.findItem(R.id.action_notify_activate).setVisible(mapShown && !mInGeofence
                 && !mCreatingGeoFence);
        menu.findItem(R.id.action_notify_deactivate).setVisible(mapShown && mInGeofence
                 && !mDeletingGeoFence);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(mTitle);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    public void setMapShown(boolean mapShown) {
        this.mapShown = mapShown;
    }

    public void setListShown(boolean listShown) {
        this.listShown = listShown;
    }

    @Override
    public void onBackPressed(){
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.getBackStackEntryCount()!=0){
            fragmentManager.popBackStack();
        }else{
            super.onBackPressed();
        }
    }

    private void removeGeoFence() {
        mDeletingGeoFence = true;
        invalidateOptionsMenu();
        ArrayList<String> geoFencesToRemove = new ArrayList<>();
        geoFencesToRemove.add(mCurrentGeoFenceId);
        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geoFencesToRemove).setResultCallback(this);
    }

    private void removeGeoFenceFromDatabase() {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", currentUser.getObjectId());
        param.put("geoFenceId", mCurrentGeoFenceId);

        ParseCloud.callFunctionInBackground("removeGeoFence", param, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> stringObjectMap, ParseException e) {
                if (e == null) {
                    Toast.makeText(getApplicationContext(), "GeoFenceDeleted", Toast.LENGTH_LONG).show();
                    mDeletingGeoFence = false;
                    mInGeofence = false;
                    invalidateOptionsMenu();
                } else {
                    Toast.makeText(getApplicationContext(), "error im CloudCode", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

    public void createGeofence() {
        if(checkIfFirstGeoFence()){
            showFirstTimeNotification();
        }
        invalidateOptionsMenu();
        if(mLocation != null) {
            String requestId = mLocation.getLongitude() + ";" + mLocation.getLatitude() + ";" + currentUser.getObjectId();
            mGeoFenceList.add(new Geofence.Builder()
                    .setRequestId(requestId)
                    .setCircularRegion(mLocation.getLongitude(), mLocation.getLatitude(), 500) //long,lat,radius
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)//millis
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
            mPendingRequestId = requestId;

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
            mCreatingGeoFence = true;
        }else{
            Toast.makeText(getApplicationContext(), "Please wait until GPS works...", Toast.LENGTH_LONG).show();
        }
    }

    private Boolean checkIfFirstGeoFence(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getBoolean("firstTime", false)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
            return true;
        }
        return false;
    }

    private void showFirstTimeNotification(){
        new AlertDialog.Builder(this)
                .setTitle("Creating Geofence")
                .setMessage("From now on you'll be notified if any events are up in this area. To stop notifications just push the button again.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
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
        mCreatingGeoFence = false;
        mInGeofence = true;
        invalidateOptionsMenu();
        geoFence.saveInBackground();
    }

    private void checkIfInGeoFence(double longitude, double latitude, String user) {
        Map<String, Object> param = new HashMap<>();

        param.put("longitude", longitude);
        param.put("latitude", latitude);
        param.put("userId", user);

        ParseCloud.callFunctionInBackground("checkIfInGeoFence", param, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> stringObjectMap, ParseException e) {
                if (e == null) {
                    if ((Boolean) stringObjectMap.get("inGeoFence")) {
                        mCurrentGeoFenceId = stringObjectMap.get("data").toString();
                        mInGeofence = true;
                        invalidateOptionsMenu();
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onResult(Status status) {
        Toast.makeText(getApplicationContext(), "Result: " + status.toString(), Toast.LENGTH_LONG).show();
        if (mCreatingGeoFence) {
            submitGeofenceToDatabase();
        } else if (mDeletingGeoFence) {
            removeGeoFenceFromDatabase();
        } else {
            Toast.makeText(getApplicationContext(), "Error Error Error", Toast.LENGTH_LONG).show();
        }
        mDeletingGeoFence = false;
        mCreatingGeoFence = false;
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeoFencePendingIntent != null) {
            return mGeoFencePendingIntent;
        }
        Intent intent = new Intent(getApplicationContext(), GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeoFenceList);
        return builder.build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("API", "Connected");
        mLocation = getLocation(LocationServices.FusedLocationApi);
        if(currentUser != null && mLocation != null)
            checkIfInGeoFence(mLocation.getLongitude(),mLocation.getLatitude(),currentUser.getObjectId());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("API", "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "ERROR CONNECTING GOOGLE API CLIENT", Toast.LENGTH_LONG).show();
    }

    public Location getLocation(FusedLocationProviderApi locationClient){
        Log.d("ABC","Called");
        if(locationClient.getLastLocation(mGoogleApiClient) == null){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getLocation(locationClient);
        }else{
            return locationClient.getLastLocation(mGoogleApiClient);
        }
    }
}