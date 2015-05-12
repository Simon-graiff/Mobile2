package dhbw.mobile2;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ParticipantsListActivity extends ActionBarActivity {
    ListView participantsListView;
    String eventId;
    List<ParseUser> listParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants_list);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("id");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.fromLocalDatastore();
        query.getInBackground(eventId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    listParticipants = object.getList("participants");
                    createParticipantsList();
                } else {
                    // something went wrong
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_participants_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }





    private void createParticipantsList(){
        participantsListView = (ListView) findViewById(R.id.participatorsListView);
        final ArrayList<String> arrayListParticipants = new ArrayList<String>();
        for (int i = 0; i < listParticipants.size(); i++) {
            try {
                arrayListParticipants.add(listParticipants.get(i).fetchIfNeeded().getUsername());
            } catch (Exception e){
                e.printStackTrace();
            }
            }
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, arrayListParticipants);
        participantsListView.setAdapter(adapter);
    }
}
