package dhbw.mobile2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ParticipatorsListActivity extends ActionBarActivity {
    ListView participatorsListView;
    String eventId;
    List<ParseUser> listParticipators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participators_list);

        Intent intent = getIntent();
        eventId = intent.getStringExtra("id");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
        query.fromLocalDatastore();
        query.getInBackground(eventId, new GetCallback<ParseObject>() {
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    listParticipators = object.getList("participators");
                    createParticipatorsList();
                } else {
                    // something went wrong
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_participators_list, menu);
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

    private void createParticipatorsList(){
        participatorsListView = (ListView) findViewById(R.id.participatorsListView);
        final ArrayList<String> arrayListParticipators = new ArrayList<String>();
        for (int i = 0; i < listParticipators.size(); i++) {
            try {
                arrayListParticipators.add(listParticipators.get(i).fetchIfNeeded().getUsername());
            } catch (Exception e){
                e.printStackTrace();
            }
            }
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, arrayListParticipators);
        participatorsListView.setAdapter(adapter);
    }
}
