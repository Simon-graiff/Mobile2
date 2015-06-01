package dhbw.mobile2;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;


import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;



/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ListEventsFragment extends Fragment implements AdapterView.OnItemClickListener {



    private OnFragmentInteractionListener mListener;

    ArrayList<String> idArray;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;
    View screenView;
    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListEventsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get the data to fill the event lines
        getEventData();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        screenView = inflater.inflate(R.layout.fragment_events_list, container, false);

        // Set the adapter
        mListView = (AbsListView) screenView.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return screenView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //activate the ActionBar Button for the Map; reload ActionBar
        ((MainScreen) getActivity()).setListShown(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        //deactivate the ActionBar Button for the Map; reload ActionBar
        ((MainScreen) getActivity()).setListShown(false);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //activate the Listener for the ListItems
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //deactivate the Listener for the ListItems
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {

            mListener.onFragmentInteraction(idArray.get(position));

            //switch to screen "EventDetail"
            goToEventDetails(idArray.get(position));
        }
    }

    public void goToEventDetails(String objectId){

        //provide the eventId of the event object for the EventDetail-Screen
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("eventId", objectId);
        editor.apply();

        //create a new EventDetail-Fragment for the relevant event
        Fragment fragment = new EventDetailFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
    }

    //Listener to fetch events on event items in the list
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }

    public void getEventData(){
        //necessary content for eah list item
        final ArrayList<String> titleArray = new ArrayList<>();
        final ArrayList<String> categoryArray = new ArrayList<>();
        final ArrayList<Double> distanceArray = new ArrayList<>();
        final ArrayList<String> timeArray = new ArrayList<>();
        final ArrayList<String> participantCountArray = new ArrayList<>();

        //Creating ParseGeoPoint with user's current location for further processing in query
        LocationManager locationManager =  (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        final Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final ParseGeoPoint point = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());


        //Query: Get all events in the reach of five kilometers
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereWithinKilometers("geoPoint", point, 5);

        Log.d("Main", "Waiting for Callback...");

        //Executing query
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> eventList, ParseException e) {
                Log.d("Main", "Received "+eventList.size()+" events");
                if (e == null) {
                    for (int i=0; i < eventList.size(); i++){
                        //get single event object from query
                        ParseObject event = eventList.get(i);
                        //add title to Event List object
                        titleArray.add(event.getString("title"));
                        //add id to Event List object
                        idArray.add(event.getObjectId());
                        //add category to Event List object
                        categoryArray.add(event.getString("category"));

                        //calculate distance and add it to Event List object
                        ParseGeoPoint eventPoint = event.getParseGeoPoint("geoPoint");
                        double distance = eventPoint.distanceInKilometersTo(point);
                        //get just the kilometer amount with one digit behind the comma
                        distance = ((int) distance * 10)/10.0;
                        distanceArray.add(distance);

                        //start: get the time of the event, create a string out of it and add it to Event List object
                        Date creationTime;
                        Date finishTime;
                        creationTime = event.getCreatedAt();
                        finishTime = event.getDate("duration");
                        Calendar calendar = GregorianCalendar.getInstance();

                        calendar.setTime(creationTime);
                        String creationTimeString = calendar.get(Calendar.HOUR_OF_DAY)+ ":";
                        if (calendar.get(Calendar.MINUTE)< 10){
                            creationTimeString += "0";}
                        creationTimeString += calendar.get(Calendar.MINUTE);

                        calendar.setTime(finishTime);
                        String finishTimeString = calendar.get(Calendar.HOUR_OF_DAY)+ ":";
                        if (calendar.get(Calendar.MINUTE)< 10){
                            finishTimeString += "0";}
                        finishTimeString += calendar.get(Calendar.MINUTE);
                        String time = creationTimeString + " - " + finishTimeString;
                        timeArray.add(time);
                        //end: get the time of the event, create a string out of it and add it to Event List object

                        //get a string from the current participants and maximum amount of them, add
                        //it to Event object
                        List<ParseUser> listParticipants = event.getList("participants");
                        int maxMembers = event.getInt("maxMembers");
                        try {
                            String textParticipants = listParticipants.size() + "/" + maxMembers;

                            participantCountArray.add(textParticipants);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    //add all event object content to the Event List
                    mAdapter = new EventlistAdapter(getActivity(),
                            titleArray,
                            categoryArray,
                            distanceArray,
                            timeArray,
                            participantCountArray);
                    mListView.setAdapter(mAdapter);
                }else {
                    Log.d("Main", "Exception: "+e);
                }
            }
        });
    }
}
