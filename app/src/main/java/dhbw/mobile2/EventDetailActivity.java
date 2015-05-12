package dhbw.mobile2;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class EventDetailActivity extends ActionBarActivity {

    Intent intent;


    //EventObject
    ParseObject eventObject;

    //Relevant Users
    ParseUser currentUser = ParseUser.getCurrentUser();
    List<ParseUser> listParticipants;

    //Dynamic Information of Event
    private String category;
    private String description;
    private String duration;
    private int latitude;
    private String locationName;
    private int longitude;
    private int maxMembers;
    private String title;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        intent = getIntent();
        eventId = intent.getStringExtra("eventId");
        //createExampleEventData();
        retrieveParseData();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume(){
        super.onResume();

    }


        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_detail, menu);
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




                } });
    }


                private void fillDynamicData(ParseObject object){
        declarateViews();

                    fillCategory(object);
                    fillDescription(object);
        fillTitle(object);
                    fillLocationName(object);
                    fillCreatorName(object);
                    fillCreationTime(object);
        fillParticipants(object);
        longitude = object.getInt("longitude");
                    latitude = object.getInt("latitude");
                    duration = object.getString("duration");
    }



    private void declarateViews(){
        detailCategoryDynamic = (TextView) (findViewById(R.id.detail_category_dynamic));
        detailDescriptionDynamic = (TextView) (findViewById(R.id.detail_description_dynamic));
        detailTitleDynamic = (TextView) (findViewById(R.id.detail_title_dynamic));
        detailLocationNameDynamic = (TextView) (findViewById(R.id.detail_location_name_dynamic));
        detailCreatorNameDynamic = (TextView) (findViewById(R.id.detail_creator_dynamic));
        detailCreationTimeDynamic = (TextView) (findViewById(R.id.detail_creation_time_dynamic));
        detailParticipantsDynamic = (TextView) (findViewById(R.id.detail_participants_dynamic));
        detailButtonParticipate = (Button) (findViewById(R.id.detail_button_participate));
    }

    private void fillCategory (ParseObject object){
        category = object.getString("category");
        detailCategoryDynamic.setText(category);
    }

    private void fillDescription(ParseObject object){
        description = object.getString("description");
        detailDescriptionDynamic.setText(description);
    }

    private void fillTitle(ParseObject object){
        title = object.getString("title");
        detailTitleDynamic.setText(title);
    }

    private void fillLocationName(ParseObject object){
        locationName = object.getString("locationName");
        detailLocationNameDynamic.setText(locationName);
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
        maxMembers = object.getInt("maxMembers");
        try {
            String textParticipants = listParticipants.size() + "/" + maxMembers + " participants";

            detailParticipantsDynamic.setText(textParticipants);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkParticipationStatus(ParseObject object){
        List<ParseUser> listParticipants = object.getList("participants");
        for (int i=0; i < listParticipants.size(); i++){
            try {
                if (listParticipants.get(i).fetchIfNeeded().getObjectId().equals(currentUser.getObjectId())){
                    changeParticipationToTrue();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void activateParticipation(View view){
        //add CurrentUser to ParseObject


        if (!statusParticipation) {
            if (listParticipants.size() <= maxMembers) {
                listParticipants.add(currentUser);
                eventObject.put("participants", listParticipants);
                eventObject.saveInBackground();

                changeParticipationToTrue();
            } else {
                Context context = getApplicationContext();
                CharSequence text = "Sorry, but you cannot participate in this event. The maximum amount of participants has already been reached.";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        }


    }

    private void changeParticipationToTrue(){
        statusParticipation = true;
        fillParticipants(eventObject);
        detailButtonParticipate.setText("I don\'t participate");
        detailButtonParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deactivateParticipation(detailButtonParticipate);
            }
        });
    }

    private void changeParticipationToFalse(){
        statusParticipation = false;
        fillParticipants(eventObject);
        detailButtonParticipate.setText("I do participate");
        detailButtonParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateParticipation(detailButtonParticipate);
            }
        });
    }

    private void deactivateParticipation(View view){
        if (statusParticipation == true) {
            listParticipants.remove(currentUser);
            eventObject.put("participants", listParticipants);
            eventObject.saveInBackground();

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

    public void linkParticipantsActivity(View view){
        Intent intent = new Intent(this, ParticipantsListActivity.class);
        intent.putExtra("id", eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

    }
}
