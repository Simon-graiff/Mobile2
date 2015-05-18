package dhbw.mobile2;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EventDetailFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_detail, container, false);

        //Theoretisch kannst Du hier Deine Variablen initialisieren, es kann aber bei unterschiedlichen
        // APIs zu Schwierigkeiten kommen;

        return rootView;
    }

    //Konstruktor
    public EventDetailFragment(String eventID){

    }

    //Standardkonstruktor
    public EventDetailFragment(){}

}
