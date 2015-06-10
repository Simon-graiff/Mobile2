package dhbw.mobile2;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class EventFilterFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    private ParseObject filter = ParseObject.create("User_Settings");
    private ProgressDialog progressDialog;

    private Switch sport_switch;
    private Switch music_switch;
    private Switch chilling_switch;
    private Switch dancing_switch;
    private Switch videoGames_switch;
    private Switch food_switch;
    private Switch mixedGenderSwitch;

    public EventFilterFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_event_filter, container, false);
        rootView.setBackgroundColor(Color.rgb(240, 240, 240));

        sport_switch = (Switch) rootView.findViewById(R.id.sport_switch);
        music_switch = (Switch) rootView.findViewById(R.id.music_switch);
        chilling_switch = (Switch) rootView.findViewById(R.id.chilling_switch);
        dancing_switch = (Switch) rootView.findViewById(R.id.dancing_switch);
        videoGames_switch = (Switch) rootView.findViewById(R.id.videoGames_switch);
        food_switch = (Switch) rootView.findViewById(R.id.food_switch);
        mixedGenderSwitch = (Switch) rootView.findViewById(R.id.mixedGender_switch);

        sport_switch.setOnCheckedChangeListener(this);
        music_switch.setOnCheckedChangeListener(this);
        chilling_switch.setOnCheckedChangeListener(this);
        dancing_switch.setOnCheckedChangeListener(this);
        videoGames_switch.setOnCheckedChangeListener(this);
        food_switch.setOnCheckedChangeListener(this);
        mixedGenderSwitch.setOnCheckedChangeListener(this);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        return rootView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

        int id = buttonView.getId();

        if(id == R.id.sport_switch){
            if(isChecked){
                filter.put("sport", true);
            }else{
                filter.put("sport", false);
            }
        }else if(id == R.id.music_switch){
            if(isChecked){
                filter.put("music", true);
            }else{
                filter.put("music", false);
            }
        }else if(id == R.id.chilling_switch){
            if(isChecked){
                filter.put("chilling", true);
            }else{
                filter.put("chilling", false);
            }
        }else if(id == R.id.dancing_switch){
            if(isChecked){
                filter.put("dancing", true);
            }else{
                filter.put("dancing", false);
            }
        }else if(id == R.id.videoGames_switch) {
            if (isChecked) {
                filter.put("videogames", true);
            } else {
                filter.put("videogames", false);
            }
        }else if(id == R.id.food_switch){
            if(isChecked){
                filter.put("food", true);
            }else{
                filter.put("food", false);
            }
        }else if(id == R.id.mixedGender_switch){
            if(isChecked){
                filter.put("mixedgenders", true);
            }else{
                filter.put("mixedgenders", false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        filter.saveInBackground();

    }

    @Override
    public void onResume(){
        super.onResume();

        ListView mDrawerList;
        mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
        mDrawerList.setItemChecked(3, true);
        mDrawerList.setSelection(3);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Filter events");

        progressDialog = ProgressDialog.show(getActivity(), "Loading Settings", "Please wait..");

        ParseQuery<ParseObject> query = ParseQuery.getQuery("User_Settings");
        query.include("user");
        query.whereEqualTo("user", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> retrievedList, ParseException e) {
                if (e == null) {
                    filter = retrievedList.get(0);
                    initializeSwitches();
                    progressDialog.dismiss();
                } else {
                    Log.d("Main", e.getMessage());
                }
            }
        });
    }

    public void initializeSwitches(){
        Log.d("Main", "Inizialisation");
        final boolean sport = filter.getBoolean("sport");
        final boolean music = filter.getBoolean("music");
        final boolean chilling = filter.getBoolean("chilling");
        final boolean dancing = filter.getBoolean("dancing");
        final boolean videoGames = filter.getBoolean("videogames");
        final boolean food = filter.getBoolean("food");
        final boolean mixedGenders = filter.getBoolean("mixedgenders");

        if(!sport){
            sport_switch.setChecked(false);
        }

        if(!music){
            music_switch.setChecked(false);
        }

        if(!chilling){
            chilling_switch.setChecked(false);
        }

        if(!dancing){
            dancing_switch.setChecked(false);
        }

        if(!videoGames){
            videoGames_switch.setChecked(false);
        }

        if(!food){
            food_switch.setChecked(false);
        }

        if(!mixedGenders){
            mixedGenderSwitch.setChecked(false);
        }
    }
}
