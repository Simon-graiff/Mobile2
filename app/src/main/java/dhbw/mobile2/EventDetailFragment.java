package dhbw.mobile2;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class EventDetailFragment extends Fragment implements View.OnClickListener {

    Intent intent;

    //EventObject
    public ParseObject eventObject;

    //Relevant Users
    ParseUser currentUser;
    List<ParseUser> listParticipants;

    //Dynamic Information of Event
    private int latitude;
    private int longitude;
    private int maxMembers;
    private ParseUser creator;
    private Date creationTime;
    private String eventId;
    private boolean statusParticipation = false;
    //Views
    private TextView detailCategoryDynamic;
    private TextView detailDescriptionDynamic;
    private TextView detailTitleDynamic;
    private TextView detailLocationNameDynamic;
    private TextView detailCreatorNameDynamic;
    private TextView detailCreationTimeDynamic;
    private TextView detailParticipantsDynamic;
    private Button detailButtonParticipate;
    private Button detailButtonListParticipants;
    View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);

        //Theoretisch kannst Du hier Deine Variablen initialisieren, es kann aber bei unterschiedlichen
        // APIs zu Schwierigkeiten kommen;
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        eventId = sharedPref.getString("eventId", "LyRCMu490k");
        //createExampleEventData();
        retrieveParseData();

        currentUser = ParseUser.getCurrentUser();

        detailButtonParticipate = (Button) rootView.findViewById(R.id.detail_button_participate);
        detailButtonParticipate.setOnClickListener(this);
        detailButtonListParticipants = (Button) rootView.findViewById(R.id.detail_participants_dynamic);
        detailButtonListParticipants.setOnClickListener(this);

        return rootView;
    }


    //Standardkonstruktor
    public EventDetailFragment(){}


    public void retrieveParseData() {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");


        query.getInBackground(eventId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException queryException) {
                if (queryException == null) {

                    eventObject = object;
                    eventId = object.getObjectId();
                    object.pinInBackground();
                    listParticipants = eventObject.getList("participants");

                    fillDynamicData(object);
                    checkParticipationStatus(object);
                } else {
                    System.out.print("Object could not be received");
                }

            }
        });
    }


    private void fillDynamicData(ParseObject object){
        declareViews();

        //simple Types
        fillSimpleType("category", detailCategoryDynamic);
        fillSimpleType("description", detailDescriptionDynamic);
        fillSimpleType("locationName", detailLocationNameDynamic);
        fillSimpleType("title", detailTitleDynamic);


        fillCreatorName(object);
        fillCreationTime(object);
        fillParticipants(object);

        loadProfilePicture();
    }


    public void linkParticipantsActivity(View view){

        Fragment fragment = new ParticipantsListFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
    }

    public void declareViews(){
        detailCategoryDynamic = (TextView) (rootView.findViewById(R.id.detail_category_dynamic));
        detailDescriptionDynamic = (TextView) (rootView.findViewById(R.id.detail_description_dynamic));
        detailTitleDynamic = (TextView) (rootView.findViewById(R.id.detail_title_dynamic));
        detailLocationNameDynamic = (TextView) (rootView.findViewById(R.id.detail_location_name_dynamic));
        detailCreatorNameDynamic = (TextView) (rootView.findViewById(R.id.detail_creator_dynamic));
        detailCreationTimeDynamic = (TextView) (rootView.findViewById(R.id.detail_creation_time_dynamic));
        detailParticipantsDynamic = (TextView) (rootView.findViewById(R.id.detail_participants_dynamic));
    }

    public void fillSimpleType (String dynamicField, TextView textViewToFill){
        getActivity().runOnUiThread(new UIRunnable(dynamicField, textViewToFill, eventObject)
        );
    }


    private void fillCreatorName(ParseObject object){
        try {
            creator = object.getParseUser("creator").fetchIfNeeded();
            String creatorName = creator.getUsername();
            detailCreatorNameDynamic.setText("Created by " + creatorName);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void fillCreationTime(ParseObject object){
        creationTime = object.getCreatedAt();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(creationTime);
        String creationTimeString = calendar.get(Calendar.HOUR_OF_DAY)+ ":";
        if (calendar.get(Calendar.MINUTE)< 10){
            creationTimeString += "0";}

        creationTimeString +=  calendar.get(Calendar.MINUTE);

        detailCreationTimeDynamic.setText(creationTimeString);
    }

    private void fillParticipants(ParseObject object){
        listParticipants = eventObject.getList("participants");
        maxMembers = object.getInt("maxMembers");
        try {
            String textParticipants = listParticipants.size() + "/" + maxMembers + " participants";

            detailParticipantsDynamic.setText(textParticipants);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkParticipationStatus(ParseObject object){
        String eventIdOfUser = currentUser.getString("eventId");
        if (eventIdOfUser != null){
           Log.d("Main", "eventId is not null");
           if (!eventIdOfUser.equals("no_event")) {
               if (eventIdOfUser.equals(eventObject.getObjectId())) {
                   changeParticipationToTrue();
                   Log.d("Main", "eventId does not equal no_event");
               } else {
                   statusParticipation = true;
               }
           } else {

                   changeParticipationToFalse();

           }

       } else {

                changeParticipationToFalse();

           Log.d("Main", "eventId is null");
           changeParticipationToFalse();
        }
    }

    @Override
    public void onClick(View view) {
        if (view == detailButtonParticipate){
            activateParticipation(view);
        } else if (view == detailButtonListParticipants){
            linkParticipantsActivity(view);
        };
    }

    public void loadProfilePicture(){
        ImageView imageView=(ImageView) rootView.findViewById(R.id.imageView);

        ParseFile profilepicture = creator.getParseFile("profilepicture");
        byte [] data = new byte[0];
        try {
            data = profilepicture.getData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
        int heightPixels = getActivity().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        //Set the ProfilePicuture to the ImageView and scale it to the screen size
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, ((int)(heightPixels*0.1)), ((int)(heightPixels*0.1)), false));

    }

    public void activateParticipation(View view){
        //add CurrentUser to ParseObject

        String eventIdOfUser = currentUser.getString("eventId");

        if (!statusParticipation) {
            if (listParticipants.size() <= maxMembers) {
                listParticipants.add(currentUser);
                eventObject.put("participants", listParticipants);
                eventObject.saveInBackground();



                changeParticipationToTrue();
            } else {
                Context context = getActivity();
                CharSequence text = "Sorry, but you cannot participate in this event. The maximum amount of participants has already been reached.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        } else {
            Log.d("Main", "Dialog is shown");
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");


                            query.getInBackground(currentUser.getString("eventId"), new GetCallback<ParseObject>() {
                                public void done(ParseObject object, ParseException queryException) {
                                    if (queryException == null) {

                                        removeUserFromList(object);
                                        statusParticipation = false;
                                        activateParticipation(getView());
                                    } else {
                                        System.out.print("Object could not be received");
                                    }

                                }
                            });

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(
                    "You already participate in an event at the moment. " +
                            "Do you want to cancel your other event to participate in this one?").setPositiveButton(
                    "Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }


    }

    private void changeParticipationToTrue(){
        statusParticipation = true;
        fillParticipants(eventObject);
        detailButtonParticipate.setText("Don\'t participate");
        detailButtonParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivateParticipation(detailButtonParticipate);
            }
        });

        ParseUser.getCurrentUser().put("eventId", eventId);
        ParseUser.getCurrentUser().saveInBackground();
    }

    private void changeParticipationToFalse(){
        statusParticipation = false;
        fillParticipants(eventObject);
        detailButtonParticipate.setText("Participate");
        detailButtonParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateParticipation(detailButtonParticipate);
            }
        });

        ParseUser.getCurrentUser().put("eventId", R.string.detail_no_event);
        ParseUser.getCurrentUser().saveInBackground();
    }

    private void removeUserFromList(ParseObject eventObject){
        List<ParseUser> listParticipants = eventObject.getList("participants");
        listParticipants.remove(currentUser);
        eventObject.put("participants", listParticipants);
        eventObject.saveInBackground();
    }

    private void deactivateParticipation(View view){
        if (statusParticipation) {
            removeUserFromList(eventObject);
            changeParticipationToFalse();
        }
    }

    public void createExampleEventData() {

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("username", "Vi");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                ParseObject event = new ParseObject("Event");
                event.put("title", "Runde Flunkiball auf dem Campus");
                event.put("description", "Wir wollten eine Runde Flunkiball auf dem Campus zocken. Dafuer brauchen wir mindestens 10 Leute.");
                event.put("category", "sport");
                event.put("locationName", "Uni Mannheim");
                event.put("longitude", 8.46181);
                event.put("latitude", 49.483);
                event.put("duration", "3 hours");
                event.put("maxMembers", 30);
                event.put("creator", list.get(0));
                List<ParseUser> ListParticipants = list;
                event.put("participants", list);
                event.saveInBackground();
            }
        });
    }


    public void setEventObject(ParseObject eventObject) {
        this.eventObject = eventObject;
    }
}


