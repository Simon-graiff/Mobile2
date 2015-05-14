package dhbw.mobile2;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dhbw.mobile2.R;

public class MyEventFragment extends Fragment {

    public MyEventFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_my_event, container, false);

        return rootView;
    }

}

