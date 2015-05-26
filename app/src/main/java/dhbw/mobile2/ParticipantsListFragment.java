package dhbw.mobile2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent on 19.05.2015.
 */
public class ParticipantsListFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    private OnParticipantInteractionListener mListener;

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

        participantsListView = (ListView) rootView.findViewById(R.id.participatorsListView);
        participantsListView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnParticipantInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnParticipantInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onParticipantInteraction(listParticipants.get(position).getObjectId());

            Log.d("Main", "got into Listener");
            Fragment fragment = ProfileFragment.newInstance(listParticipants.get(position).getObjectId());
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        }
    }




    private void createParticipantsList(){
        ArrayList<String> arrayListParticipantNames = new ArrayList<String>();
        ArrayList<Bitmap> arrayListParticipantPictures = new ArrayList<Bitmap>();
        for (int i = 0; i < listParticipants.size(); i++) {
            try {
                ParseUser user = listParticipants.get(i).fetchIfNeeded();
                arrayListParticipantNames.add(user.getUsername());
                Bitmap userPicture = loadProfilePicture(user);
                arrayListParticipantPictures.add(userPicture);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        ParticipantsListAdapter adapter = new ParticipantsListAdapter(getActivity(),
                arrayListParticipantNames, arrayListParticipantPictures);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
          //      R.layout.list_participants, R.id.Itemname, arrayListParticipantNames);
        participantsListView.setAdapter(adapter);
    }

    public Bitmap loadProfilePicture(ParseUser user){


        ParseFile profilepicture = user.getParseFile("profilepicture");
        byte [] data = new byte[0];
        try {
            data = profilepicture.getData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);
        int heightPixels = getActivity().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        //Set the ProfilePicuture to the ImageView and scale it to the screen size
        bitmap = Bitmap.createScaledBitmap(bitmap, ((int) (heightPixels * 0.1)), ((int) (heightPixels * 0.1)), false);
        return bitmap;
    }

    public interface OnParticipantInteractionListener {
        public void onParticipantInteraction(String id);
    }
}
