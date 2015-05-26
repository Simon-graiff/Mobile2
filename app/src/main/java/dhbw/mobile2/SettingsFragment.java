package dhbw.mobile2;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import dhbw.mobile2.R;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private CheckBox sports_checkbox;
    private CheckBox music_checkbox;
    private CheckBox chilling_checkbox;
    private CheckBox drinking_checkbox;

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

        sports_checkbox.setOnClickListener(this);
        music_checkbox.setOnClickListener(this);
        chilling_checkbox.setOnClickListener(this);
        drinking_checkbox.setOnClickListener(this);

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();


        final String sport = sharedPref.getString("Sport", null);
        final String music = sharedPref.getString("Music", null);
        final String chilling = sharedPref.getString("Chilling", null);
        final String drinking = sharedPref.getString("Drinking", null);

        Log.d("Main", "+++++++++++++++");
        Log.d("Main", "Sport: "+sport);
        Log.d("Main", "Music: "+music);
        Log.d("Main", "Chilling: "+chilling);
        Log.d("Main", "Drinking: "+drinking);
        Log.d("Main", "+++++++++++++++");

        if(sport!=null){
            sports_checkbox.setChecked(false);
        }else if(music!=null){
            music_checkbox.setChecked(false);
        }else if(chilling!=null){
            chilling_checkbox.setChecked(false);
        }else if(drinking!=null){
            drinking_checkbox.setChecked(false);
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
        }
    }

    private void removeQueryParameter(String category){
        Log.d("Main", "removeParameter: "+category);

        editor.remove(category);
        editor.commit();
    }

    //Sets SharedPreference for filtering the received event list in AppMapFragment
    private void setQueryParameter(String category){

        Log.d("Main", "setParameter: "+category);

        editor.putString(category, category);
        editor.commit();
    }
}
