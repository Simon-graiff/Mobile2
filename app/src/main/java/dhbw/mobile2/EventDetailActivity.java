package dhbw.mobile2;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class EventDetailActivity extends ActionBarActivity {
    ListView detailListView;
    ArrayAdapter<String> detailAdapter;
    String[] eventDetails =
            {"Uhrzeit",
                    "Entfernung",
                    "Ort",
                    "Beschreibung",
                    "Teilnehmer",
                    "Teilnehmen/Abmelden",
                    "Navigation starten"};
    String category;
    String description;
    String duration;
    int latitude;
    String locationName;
    int longitude;
    int maxMembers;
    String title;
    ParseUser creator;
    Date creationTime;
    String eventId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Intent intent = getIntent();
      // createExampleEventData();
        retrieveParseData();
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
        query.getInBackground("OeJFi71v7F", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException queryException) {
                if (queryException == null) {

                    eventId = object.getObjectId();
                    object.pinInBackground();

                    category = object.getString("category");
                    TextView detailCategoryDynamic = (TextView) (findViewById(R.id.detail_category_dynamic));
                    detailCategoryDynamic.setText(category);

                    description = object.getString("description");
                    TextView detailDescripitonDynamic = (TextView) (findViewById(R.id.detail_description_dynamic));
                    detailDescripitonDynamic.setText(description);

                    title = object.getString("title");
                    TextView detailTitleDynamic = (TextView) (findViewById(R.id.detail_title_dynamic));
                    detailTitleDynamic.setText(title);

                    locationName = object.getString("locationName");
                    TextView detailLocationNameDynamic = (TextView) (findViewById(R.id.detail_location_name_dynamic));
                    detailLocationNameDynamic.setText(locationName);


                    try {
                        creator = object.getParseUser("creator").fetchIfNeeded();
                        String creatorName = creator.getUsername();
                        TextView detailCreatorNameDynamic = (TextView) (findViewById(R.id.detail_creator_dynamic));
                        detailCreatorNameDynamic.setText("Erstellt durch " + creatorName);

                    } catch (Exception e) {

                        e.printStackTrace();
                    }

                    creationTime = object.getCreatedAt();
                    Calendar calendar = GregorianCalendar.getInstance();
                    calendar.setTime(creationTime);
                    String creationTimeString = calendar.get(Calendar.HOUR_OF_DAY)+ ":";
                    if (calendar.get(Calendar.MINUTE)< 10){
                        creationTimeString += "0";}

                    creationTimeString +=  calendar.get(Calendar.MINUTE);

                    TextView detailCreationTimeDynamic = (TextView) (findViewById(R.id.detail_creation_time_dynamic));
                    detailCreationTimeDynamic.setText(creationTimeString);


                    List<ParseUser> listParticipators = object.getList("participators");
                    TextView detailParticipatorsDynamic = (TextView) (findViewById(R.id.detail_participators_dynamic));
                    try {
                        detailParticipatorsDynamic.setText(listParticipators.size() + " Teilnehmer");
                    } catch (Exception e) {
                        detailTitleDynamic.setText("did not work");
                        e.printStackTrace();
                    }


                    longitude = object.getInt("longitude");
                    latitude = object.getInt("latitude");
                    duration = object.getString("duration");
                    maxMembers = object.getInt("maxMembers");
                } else {
                    System.out.print("Object could not be received");
                }
            }
        });
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
                List<ParseUser> ListParticipators = list;
                event.put("participators", list);
                event.saveInBackground();
            }
        });
    }

    public void linkParticipatorList(View view){
        Intent intent = new Intent(this, ParticipatorsListActivity.class);
        intent.putExtra("id", eventId);
        startActivity(intent);

    }
}
