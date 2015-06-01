package dhbw.mobile2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import dhbw.mobile2.R;

public class SettingsFragment extends Fragment
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private CheckBox sports_checkbox;
    private CheckBox music_checkbox;
    private CheckBox chilling_checkbox;
    private CheckBox drinking_checkbox;
    private CheckBox disco_checkbox;
    private CheckBox videoGames_checkbox;
    private CheckBox food_checkbox;
    private Switch mixedGenderSwitch;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public SettingsFragment (){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        sports_checkbox = (CheckBox) rootView.findViewById(R.id.sports_checkbox);
        music_checkbox = (CheckBox) rootView.findViewById(R.id.music_checkbox);
        chilling_checkbox = (CheckBox) rootView.findViewById(R.id.chilling_checkbox);
        drinking_checkbox = (CheckBox) rootView.findViewById(R.id.drinking_checkbox);
        disco_checkbox = (CheckBox) rootView.findViewById(R.id.disco_checkbox);
        videoGames_checkbox = (CheckBox) rootView.findViewById(R.id.videoGames_checkbox);
        food_checkbox = (CheckBox) rootView.findViewById(R.id.food_checkbox);
        mixedGenderSwitch = (Switch) rootView.findViewById(R.id.mixedGender_switch);

        sports_checkbox.setOnClickListener(this);
        music_checkbox.setOnClickListener(this);
        chilling_checkbox.setOnClickListener(this);
        drinking_checkbox.setOnClickListener(this);
        disco_checkbox.setOnClickListener(this);
        videoGames_checkbox.setOnClickListener(this);
        food_checkbox.setOnClickListener(this);
        mixedGenderSwitch.setOnCheckedChangeListener(this);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        //Using String instead of boolean offers the advantage of the String.equals(...) method.
        //This simplifies the check in AppMapFragment.
        final String sport = sharedPref.getString("Sport", null);
        final String music = sharedPref.getString("Music", null);
        final String chilling = sharedPref.getString("Chilling", null);
        final String drinking = sharedPref.getString("Drinking", null);
        final String disco = sharedPref.getString("Disco", null);
        final String videoGames = sharedPref.getString("VideoGames", null);
        final String food = sharedPref.getString("Food", null);
        final boolean mixedGenders = sharedPref.getBoolean("MixedGenders", true);

        Log.d("Main", "+++++++++++++++");
        Log.d("Main", "Sport: "+sport);
        Log.d("Main", "Music: "+music);
        Log.d("Main", "Chilling: "+chilling);
        Log.d("Main", "Drinking: "+drinking);
        Log.d("Main", "Disco: "+disco);
        Log.d("Main", "Video Games: "+videoGames);
        Log.d("Main", "Food: "+food);
        Log.d("Main", "Mixed Genders "+mixedGenders);
        Log.d("Main", "+++++++++++++++");

        if(sport!=null){
            sports_checkbox.setChecked(false);
        }

        if(music!=null){
            music_checkbox.setChecked(false);
        }

        if(chilling!=null){
            chilling_checkbox.setChecked(false);
        }

        if(drinking!=null){
            drinking_checkbox.setChecked(false);
        }

        if(disco!=null){
            disco_checkbox.setChecked(false);
        }

        if(videoGames!=null){
            disco_checkbox.setChecked(false);
        }

        if(food!=null){
            food_checkbox.setChecked(false);
        }

        if(mixedGenders==false){
            mixedGenderSwitch.setChecked(false);
        }

        return rootView;
    }

    @Override
    public void onClick(View v){

        boolean checked = ((CheckBox) v).isChecked();
        int id = v.getId();

        if(id == R.id.sports_checkbox){
            if(checked){
                removeQueryParameter("Sport");
            }else{
                setQueryParameter("Sport");
            }
        }else if(id == R.id.music_checkbox){
            if(checked){
                removeQueryParameter("Music");
            }else{
                setQueryParameter("Music ");
            }
        }else if(id == R.id.chilling_checkbox){
            if(checked){
                removeQueryParameter("Chilling");
            }else{
                setQueryParameter("Chilling");
            }
        }else if(id == R.id.drinking_checkbox){
            if(checked){
                removeQueryParameter("Drinking");
            }else{
                setQueryParameter("Drinking");
            }
        }else if(id == R.id.disco_checkbox){
            if(checked){
                removeQueryParameter("Disco");
            }else{
                setQueryParameter("Disco");
            }
        }else if(id == R.id.videoGames_checkbox){
            if(checked){
                removeQueryParameter("VideoGames");
            }else{
                setQueryParameter("VideoGames");
            }
        }else if(id == R.id.food_checkbox){
            if(checked){
                removeQueryParameter("Food");
            }else{
                setQueryParameter("Food");
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
        if(isChecked){
            Log.d("Main", "Checked");
            editor.remove("MixedGenders");
            editor.commit();
        }else{
            Log.d("Main", "Not Checked");
            editor.putBoolean("MixedGenders", false);
            editor.commit();
        }
    }

    private void removeQueryParameter(String category){
        Log.d("Main", "removeParameter: "+category);

        editor.remove(category);
        editor.commit();
    }

    //Sets SharedPreference for filtering the received event list in AppMapFragment
    private void setQueryParameter(String category){

        Log.d("Main", "setParameter: " + category);

        editor.putString(category, category);
        editor.commit();
    }

    @Override
    public void onResume(){
        super.onResume();

        ListView mDrawerList;
        mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
        mDrawerList.setItemChecked(4, true);
        mDrawerList.setSelection(4);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Settings");

    }
}
