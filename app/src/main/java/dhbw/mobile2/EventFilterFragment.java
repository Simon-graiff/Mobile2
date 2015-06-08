package dhbw.mobile2;

import android.app.Fragment;
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

public class EventFilterFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public EventFilterFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_event_filter, container, false);
        rootView.setBackgroundColor(Color.rgb(240, 240, 240));

        Switch sport_switch = (Switch) rootView.findViewById(R.id.sport_switch);
        Switch music_switch = (Switch) rootView.findViewById(R.id.music_switch);
        Switch chilling_switch = (Switch) rootView.findViewById(R.id.chilling_switch);
        Switch dancing_switch = (Switch) rootView.findViewById(R.id.dancing_switch);
        Switch videoGames_switch = (Switch) rootView.findViewById(R.id.videoGames_switch);
        Switch food_switch = (Switch) rootView.findViewById(R.id.food_switch);
        Switch mixedGenderSwitch = (Switch) rootView.findViewById(R.id.mixedGender_switch);

        sport_switch.setOnCheckedChangeListener(this);
        music_switch.setOnCheckedChangeListener(this);
        chilling_switch.setOnCheckedChangeListener(this);
        dancing_switch.setOnCheckedChangeListener(this);
        videoGames_switch.setOnCheckedChangeListener(this);
        food_switch.setOnCheckedChangeListener(this);
        mixedGenderSwitch.setOnCheckedChangeListener(this);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Using String instead of boolean offers the advantage of the String.equals(...) method.
        //This simplifies the check in EventMap.
        final String sport = sharedPref.getString("Sport", null);
        final String music = sharedPref.getString("Music", null);
        final String chilling = sharedPref.getString("Chilling", null);
        final String dancing = sharedPref.getString("Dancing", null);
        final String videoGames = sharedPref.getString("Video Games", null);
        final String food = sharedPref.getString("Food", null);
        final boolean mixedGenders = sharedPref.getBoolean("MixedGenders", true);

        Log.d("Main", "+++++++++++++++");
        Log.d("Main", "Sport: "+sport);
        Log.d("Main", "Music: "+music);
        Log.d("Main", "Chilling: "+chilling);
        Log.d("Main", "Dancing: "+dancing);
        Log.d("Main", "Video Games: "+videoGames);
        Log.d("Main", "Food: "+food);
        Log.d("Main", "Mixed Genders "+mixedGenders);
        Log.d("Main", "+++++++++++++++");

        if(sport!=null){
            sport_switch.setChecked(false);
        }

        if(music!=null){
            music_switch.setChecked(false);
        }

        if(chilling!=null){
            chilling_switch.setChecked(false);
        }

        if(dancing!=null){
            dancing_switch.setChecked(false);
        }

        if(videoGames!=null){
            videoGames_switch.setChecked(false);
        }

        if(food!=null){
            food_switch.setChecked(false);
        }

        if(mixedGenders==false){
            mixedGenderSwitch.setChecked(false);
        }

        return rootView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

        int id = buttonView.getId();

        if(id == R.id.sport_switch){
            if(isChecked){
                editor.remove("Sport");
                editor.commit();
            }else{
                editor.putString("Sport", "Sport");
                editor.commit();
            }
        }else if(id == R.id.music_switch){
            if(isChecked){
                editor.remove("Music");
                editor.commit();
            }else{
                editor.putString("Music", "Music");
                editor.commit();
            }
        }else if(id == R.id.chilling_switch){
            if(isChecked){
                editor.remove("Chilling");
                editor.commit();
            }else{
                editor.putString("Chilling", "Chilling");
                editor.commit();
            }
        }else if(id == R.id.dancing_switch){
            if(isChecked){
                editor.remove("Dancing");
                editor.commit();
            }else{
                editor.putString("Dancing", "Dancing");
                editor.commit();
            }
        }else if(id == R.id.videoGames_switch) {
            if (isChecked) {
                editor.remove("Video Games");
                editor.commit();
            } else {
                editor.putString("Video Games", "Video Games");
                editor.commit();
            }
        }else if(id == R.id.food_switch){
            if(isChecked){
                editor.remove("Food");
                editor.commit();
            }else{
                editor.putString("Food", "Food");
                editor.commit();
            }
        }else if(id == R.id.mixedGender_switch){
            if(isChecked){
                editor.remove("MixedGenders");
                editor.commit();
            }else{
                editor.putBoolean("MixedGenders", false);
                editor.commit();
            }
        }
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
    }
}
