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
import android.view.Menu;
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

    ArrayList<String> titleArray;
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


        getEventData();

       // mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
            //    android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
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
        ((MainScreen) getActivity()).setMapShown(false);
        ((MainScreen) getActivity()).setListShown(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainScreen) getActivity()).setListShown(false);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            mListener.onFragmentInteraction(titleArray.get(position));

            goToEventDetails(idArray.get(position));
        }
    }

    public void goToEventDetails(String objectId){

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("eventId", objectId);
        editor.apply();

        Fragment fragment = new EventDetailFragment();


        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();


    }
    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }

    public void getEventData(){
        titleArray = new ArrayList<>();
        final ArrayList<String> categoryArray = new ArrayList<>();
        idArray = new ArrayList<>();
        final ArrayList<Double> distanceArray = new ArrayList<>();
        final ArrayList<String> timeArray = new ArrayList<>();
        final ArrayList<String> participantCountArray = new ArrayList<>();

        //Creating ParseGeoPoint with user's current location
        LocationManager locationManager =  (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        final Location userLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        final ParseGeoPoint point = new ParseGeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
        ParseObject user = new ParseObject("User");
        user.put("location", point);

        //Preparing query
        ParseGeoPoint queryParameter = (ParseGeoPoint) user.get("location");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.whereWithinKilometers("geoPoint", queryParameter, 1000000);

        Log.d("Main", "Waiting for Callback...");

        //Executing query
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> eventList, ParseException e) {
                Log.d("Main", "Received "+eventList.size()+" events");
                if (e == null) {
                    for (int i=0; i < eventList.size(); i++){
                        ParseObject event = eventList.get(i);
                        titleArray.add(event.getString("title"));
                        idArray.add(event.getObjectId());
                        categoryArray.add(event.getString("category"));
                        ParseGeoPoint eventPoint = event.getParseGeoPoint("geoPoint");
                        double distance = eventPoint.distanceInKilometersTo(point);
                        distance = distance * 10;
                        int distanceInt = (int) distance;
                        distance = distanceInt/10.0;
                        distanceArray.add(distance);
                        Log.d("Main", "title: " + event.getString("title"));

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

                        List<ParseUser> listParticipants = event.getList("participants");
                        int maxMembers = event.getInt("maxMembers");
                        try {
                            String textParticipants = listParticipants.size() + "/" + maxMembers;

                            participantCountArray.add(textParticipants);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
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
/*
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_list_events).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
    */
}
