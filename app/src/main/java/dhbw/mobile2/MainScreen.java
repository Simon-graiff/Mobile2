package dhbw.mobile2;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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


public class MainScreen extends ActionBarActivity implements ListEventsFragment.OnFragmentInteractionListener, ParticipantsListFragment.OnParticipantInteractionListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // nav drawer title
    private CharSequence mDrawerTitle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    private boolean statusParticipation = false;
    private boolean cancelOtherEvent = false;

    private boolean mapShown = true;
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



    public static FragmentManager fragmentManager;

    //currentUser
    ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //Check if User is logged in
        if(ParseUser.getCurrentUser() == null){
            //If the user is not logged in call the loginActiviy
            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
            startActivity(intent);
        } else {
            currentUser = ParseUser.getCurrentUser();
        }


        mTitle = mDrawerTitle = getTitle();

        //Load slide menu items & icons
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        //Adding items to array, counter is deactivated:
        //0 = Profile
        //1 = Map
        //2 = Create new Event
        //3 = My event
        //4 = Settings
        //5 = Logout

        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));

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
                R.drawable.ic_drawer, //nav menu toggle icon  war mal ic_drawer
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);

                //Is called on onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);

                //Calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            //Setting MapFragment as default fragment
            Fragment fragment = new AppMapFragment();
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
            fragment = new AppMapFragment();
        }else if(position==2){
            checkParticipation();
            if (statusParticipation){
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                ParseUser.getCurrentUser().put("eventId", "no_event");
                                ParseUser.getCurrentUser().saveInBackground();
                                createEventFragment();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }


                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(
                        "You already participate in an event. " +
                                "Cancel other event to create new one?").setPositiveButton(
                        "Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            } else {
                fragment = new CreateEventFragment();
            }

        }else if(position==3){
            mDrawerList.setItemChecked(3, true);
            mDrawerList.setSelection(3);
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


        }else if(position==4){
            fragment = new SettingsFragment();
        }else if(position==5){
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

            FragmentManager fragmentManager = getFragmentManager();
            //fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.frame_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        } else {
            //Error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    private void createEventFragment(){
        Fragment fragment = new CreateEventFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

            // update selected item and title, then close the drawer
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
        getMenuInflater().inflate(R.menu.menu_create_event, menu);
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
                openList();
                return true;
            case R.id.action_map:
                openMap();
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

    private void openList(){
        Fragment fragment;
                FragmentManager fragmentManager = getFragmentManager();
                fragment = new ListEventsFragment();

                 FragmentTransaction transaction = fragmentManager.beginTransaction();
                 transaction.replace(R.id.frame_container, fragment);
                 transaction.addToBackStack(null);
                 transaction.commit();
            }

    private void openMap(){
              Fragment fragment = new AppMapFragment();
                FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
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
        menu.findItem(R.id.action_notify_activate).setVisible(mapShown && !mInGeofence && !mCreatingGeoFence && !mDeletingGeoFence);
        menu.findItem(R.id.action_notify_deactivate).setVisible(mapShown && mInGeofence && !mDeletingGeoFence && !mCreatingGeoFence);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

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
        Log.d("Main", "onBackPressed");

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
        mCreatingGeoFence = true;
        invalidateOptionsMenu();
        if(mLocation != null) {
            String requestId = mLocation.getLongitude() + ";" + mLocation.getLatitude() + ";" + currentUser.getObjectId();
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
        }else{
            Toast.makeText(getApplicationContext(), "Please wait until GPS works...", Toast.LENGTH_LONG).show();
        }
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