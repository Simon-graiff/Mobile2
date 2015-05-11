package dhbw.mobile2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;


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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        Intent intent = getIntent();

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

    public void retrieveParseData(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.getInBackground("FDm6rc2uAI", new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    category = object.getString("category");
                    TextView detailCategoryDynamic = (TextView) (findViewById(R.id.detail_category_dynamic));
                    detailCategoryDynamic.setText(category);

                    description = object.getString("description");

                    title = object.getString("title");
                    TextView detailTitleDynamic = (TextView) (findViewById(R.id.detail_title_dynamic));
                    detailTitleDynamic.setText(title);

                    locationName = object.getString("locationName");
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

    public void createExampleEventData(){

        ParseObject event = new ParseObject("Event");
        event.put("title", "Runde Flunkiball auf dem Campus");
        event.put("description", "Wir wollten eine Runde Flunkiball auf dem Campus zocken. Dafür brauchen wir mindestens 10 Leute.");
        event.put("category", "sport");
        event.put("locationName", "Uni Mannheim");
        event.put("longitude", 8.46181);
        event.put("latitude", 49.483);
        event.put("duration", "3 hours");
        event.put("maxMembers", 30);
        event.saveInBackground();
    }
}
