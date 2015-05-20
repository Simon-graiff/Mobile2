package dhbw.mobile2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent on 19.05.2015.
 */
public class ParticipantsListFragment extends Fragment implements View.OnClickListener{
    ListView participantsListView;
    String eventId;
    List<ParseUser> listParticipants;
    Button backToEventDetailButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.participants_list_fragment, container, false);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        eventId = sharedPref.getString("eventId", "LyRCMu490k");

        backToEventDetailButton = (Button) rootView.findViewById(R.id.participantsListBackButton);
        backToEventDetailButton.setOnClickListener(this);

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

        return rootView;
    }


    @Override
    public void onClick(View view){
        if (view == backToEventDetailButton){
            Fragment fragment = null;
            fragment = new EventDetailFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        }
    }


    private void createParticipantsList(){
        participantsListView = (ListView) getView().findViewById(R.id.participatorsListView);
        final ArrayList<String> arrayListParticipants = new ArrayList<String>();
        for (int i = 0; i < listParticipants.size(); i++) {
            try {
                arrayListParticipants.add(listParticipants.get(i).fetchIfNeeded().getUsername());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, arrayListParticipants);
        participantsListView.setAdapter(adapter);
    }
}
