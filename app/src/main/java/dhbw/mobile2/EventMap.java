package dhbw.mobile2;

import android.app.Fragment;
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
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class EventMap extends Fragment implements GoogleMap.OnMarkerClickListener {

    private GoogleMap map = null;
    MapView eventMapView = null;
    public List<EventManagerItem> eventManager = new ArrayList<>();
    public List<ParseUser> participantList = new ArrayList<>();
    private ParseObject filter = ParseObject.create("User_Settings");
    private HelperClass helperObject = new HelperClass();


    public EventMap(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_app_map, container, false);
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

        if(getView()!=null) {
            eventMapView = (MapView) getView().findViewById(R.id.EventMapView);
            eventMapView.onCreate(savedInstanceState);

            map = eventMapView.getMap();
        }

        if(map!=null) {
            map.getUiSettings().setMyLocationButtonEnabled(false);

            map.setMyLocationEnabled(true);
            map.setOnMarkerClickListener(this);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(eventMapView !=null){
            eventMapView.onResume();
        }

        //Fetching user filter, to prepare event draw according to settings
        ParseQuery<ParseObject> query = ParseQuery.getQuery("User_Settings");
        query.include("user");
        if (ParseUser.getCurrentUser() != null) {
            try {
                query.whereEqualTo("user", ParseUser.getCurrentUser().fetchIfNeeded());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> retrievedList, ParseException e) {
                    if (e == null) {
                        try {
                            filter = retrievedList.get(0).fetchIfNeeded();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }

                        drawEvents();
                    } else {
                        Log.d("Main", e.getMessage());
                    }
                }
            });
        }

        ListView mDrawerList;
        mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
        mDrawerList.setItemChecked(1, true);
        mDrawerList.setSelection(1);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

        if(actionBar!=null) {
            actionBar.setTitle("Map");
        }
    }

    @Override
    public void onPause() {
        if (map != null)
            eventMapView.onPause();
        super.onPause();

        ((MainActivity) getActivity()).setMapShown(false);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDestroy(){
        if(map!=null) {
            eventMapView.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        if(map!=null) {
            eventMapView.onLowMemory();
        }
    }

    private void drawMarker(LatLng position, String title, String eventID, String category){
        if (getActivity() != null) {
            Marker m = helperObject.drawMarker(position, title, eventID, category, getActivity(), map);
            eventManager.add(new EventManagerItem(m.getId(), eventID, position));
        }
    }

    private void drawEvents(){

        //Initialize filters
        final boolean sport = filter.getBoolean("sport");
        final boolean music = filter.getBoolean("music");
        final boolean chilling = filter.getBoolean("chilling");
        final boolean dancing = filter.getBoolean("dancing");
        final boolean videoGames = filter.getBoolean("videogames");
        final boolean food = filter.getBoolean("food");
        final boolean mixedGenders = filter.getBoolean("mixedgenders");

        //Create ParseGeoPoint with user's current location
        //locationManager.requestSingleUpdate(locationManager.GPS_PROVIDER, locationListener, null);
        //Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("LocationObject");
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objectList, ParseException queryException) {
                if (queryException == null) {
                    ParseObject object = objectList.get(0);
                    double mLong = (double) object.get("mLong");
                    double mLat = (double) object.get("mLat");
                    Location userLocation = new Location("");
                    userLocation.setLatitude(mLat);
                    userLocation.setLongitude(mLong);

                    Log.d("Main", userLocation.toString());
                    LatLng position = new LatLng(mLat, mLong);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(position)
                            .zoom(16)
                            .tilt(40)
                            .build();

                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    ParseGeoPoint point = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
                    ParseObject user = new ParseObject("User");
                    user.put("location", point);

                    //Prepare query
                    ParseGeoPoint queryParameter = (ParseGeoPoint) user.get("location");
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
                    query.whereWithinKilometers("geoPoint", queryParameter, 2);

                    Log.d("Main", "Waiting for Callback...");

                    //Executing query
                    query.findInBackground(new FindCallback<ParseObject>() {
                        public void done(List<ParseObject> eventList, ParseException e) {
                            if (e == null) {

                                //Query: Get all events in the reach of five kilometers
                                ParseQuery<ParseObject> query = ParseQuery.getQuery("FilteredEvents");
                                query.fromLocalDatastore();

                                Log.d("Main", "Retrieved " + eventList.size() + " events");
                                //Executing query
                                query.findInBackground(new FindCallback<ParseObject>() {
                                    public void done(List<ParseObject> eventList, ParseException e) {
                                        Log.d("Main", "Received " + eventList.size() + " events");
                                        if (e == null) {
                                            for (int i = 0; i < eventList.size(); i++) {
                                                eventList.get(i).unpinInBackground();
                                            }

                                        } else {
                                            Log.d("Main", "No events existed in background");
                                        }
                                    }
                                });

                                if (!eventList.isEmpty()) {


                                    Log.d("Main", "Waiting for Callback...");
                                    ArrayList<ParseObject> eventArray = new ArrayList<>();


                                    for (int i = 0; i < eventList.size(); i++) {
                                        //Extraction of latitude and longitude from received GeoPoints
                                        ParseGeoPoint tmpPoint = (ParseGeoPoint) eventList.get(i).get("geoPoint");
                                        double tmpLat = tmpPoint.getLatitude();
                                        double tmpLng = tmpPoint.getLongitude();
                                        LatLng tmpLatLng = new LatLng(tmpLat, tmpLng);

                                        participantList = eventList.get(i).getList("participants");
                                        boolean tmpMale = false;
                                        boolean tmpFemale = false;
                                        for (int j = 0; j < participantList.size(); j++) {
                                            try {
                                                if (participantList.get(j).fetchIfNeeded().getString("gender").equals("male")) {
                                                    tmpMale = true;
                                                } else {
                                                    tmpFemale = true;
                                                }
                                            } catch (ParseException exception) {
                                                exception.printStackTrace();
                                            }
                                        }

                                        //If tmpMale & tmpFemale are both true then the participants of the
                                        //event are from both genders. In case the user wants to avoid that
                                        //a preference check is needed
                                        if (tmpMale && tmpFemale && !mixedGenders) {
                                            Log.d("Main", "There is a mixed gender group");
                                        } else {
                                            Log.d("Main", "In der if");

                                            //Prepare necessary information and draw markers to map
                                            String tmpTitle = eventList.get(i).getString("title");
                                            String category = eventList.get(i).getString("category");
                                            String eventID = eventList.get(i).getObjectId();
                                            Log.d("Main", "eventID =" + eventID);

                                            switch (category) {
                                                case "Sport":
                                                    if (sport) {
                                                        try {
                                                            eventArray.add(eventList.get(i).fetchIfNeeded());
                                                            Log.d("Main", "geoPoint: " + eventArray.get(eventArray.size() - 1).getParseGeoPoint("geoPoint"));
                                                        } catch (ParseException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                        Log.d("Main", "Drew marker: " + tmpTitle);
                                                        drawMarker(tmpLatLng, tmpTitle, eventID, category);
                                                    }
                                                    break;

                                                case "Music":
                                                    if (music) {
                                                        try {
                                                            eventArray.add(eventList.get(i).fetchIfNeeded());
                                                            Log.d("Main", "geoPoint: " + eventArray.get(eventArray.size() - 1).getParseGeoPoint("geoPoint"));
                                                        } catch (ParseException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                        Log.d("Main", "Drew marker: " + tmpTitle);
                                                        drawMarker(tmpLatLng, tmpTitle, eventID, category);
                                                    }
                                                    break;

                                                case "Chilling":
                                                    if (chilling) {
                                                        try {
                                                            eventArray.add(eventList.get(i).fetchIfNeeded());
                                                            Log.d("Main", "geoPoint: " + eventArray.get(eventArray.size() - 1).getParseGeoPoint("geoPoint"));
                                                        } catch (ParseException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                        Log.d("Main", "Drew marker: " + tmpTitle);
                                                        drawMarker(tmpLatLng, tmpTitle, eventID, category);
                                                    }
                                                    break;

                                                case "Dancing":
                                                    if (dancing) {
                                                        try {
                                                            eventArray.add(eventList.get(i).fetchIfNeeded());
                                                            Log.d("Main", "geoPoint: " + eventArray.get(eventArray.size() - 1).getParseGeoPoint("geoPoint"));
                                                        } catch (ParseException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                        Log.d("Main", "Drew marker: " + tmpTitle);
                                                        drawMarker(tmpLatLng, tmpTitle, eventID, category);
                                                    }
                                                    break;

                                                case "Food":
                                                    if (food) {
                                                        try {
                                                            eventArray.add(eventList.get(i).fetchIfNeeded());
                                                            Log.d("Main", "geoPoint: " + eventArray.get(eventArray.size() - 1).getParseGeoPoint("geoPoint"));
                                                        } catch (ParseException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                        Log.d("Main", "Drew marker: " + tmpTitle);
                                                        drawMarker(tmpLatLng, tmpTitle, eventID, category);
                                                    }
                                                    break;

                                                case "Video Games":
                                                    if (videoGames) {
                                                        try {
                                                            eventArray.add(eventList.get(i).fetchIfNeeded());
                                                            Log.d("Main", "geoPoint: " + eventArray.get(eventArray.size() - 1).getParseGeoPoint("geoPoint"));
                                                        } catch (ParseException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                        Log.d("Main", "Drew marker: " + tmpTitle);
                                                        drawMarker(tmpLatLng, tmpTitle, eventID, category);
                                                    }
                                                    break;
                                            }

                                        }
                                    }//End for-loop for eventList
                                    ParseObject listOfFilteredEvents = new ParseObject("FilteredEvents");
                                    listOfFilteredEvents.put("list", eventArray);
                                    listOfFilteredEvents.pinInBackground();

                                    while (true) {
                                        if ((MainActivity) getActivity() == null) {
                                            try {
                                                Thread.sleep(500, 0);
                                            } catch (InterruptedException e1) {
                                                e1.printStackTrace();
                                            }
                                        } else {
                                            ((MainActivity) getActivity()).setMapShown(true);
                                            getActivity().invalidateOptionsMenu();
                                            break;
                                        }
                                    }

                                }


                            } else {
                                Log.d("Main", "Error on callback: " + e.getMessage());
                            }
                            ((MainActivity) getActivity()).setMapShown(true);
                            getActivity().invalidateOptionsMenu();
                        }
                    });
                }
            }
        });
                }

                @Override
                public boolean onMarkerClick ( final Marker m){

                    String markerID = m.getId();
                    String eventID = "Not found";

                    //Retrieve eventID from eventManager with markerID
                    for (int i = 0; i < eventManager.size(); i++) {
                        if (eventManager.get(i).getMarkerID().equals(markerID)) {
                            eventID = eventManager.get(i).getEventID();
                        }
                    }

                    if (!eventID.equals("Not found")) {
                        //Save eventID in SharedPreferences
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("eventId", eventID);
                        editor.apply();

                        //Direction to EventDetailFragment
                        Fragment eventDetailFragment = new EventDetailFragment();
                        helperObject.switchToFragment(getFragmentManager(), eventDetailFragment);
                    }

                    return true;
                }

            public void showListAsActionItem() {
                MenuInflater actionBar = getActivity().getMenuInflater();
            }
        }