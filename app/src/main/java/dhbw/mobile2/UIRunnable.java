package dhbw.mobile2;

import android.widget.TextView;

import com.parse.ParseObject;

/**
 * Created by Vincent on 13.05.2015.
 */
public class UIRunnable implements Runnable{
    String dynamicField;
    TextView textViewToFill;
    ParseObject eventObject;


    public UIRunnable(String dynamicField, TextView textViewToFill, ParseObject eventObject){
        this.dynamicField = dynamicField;
        this.textViewToFill = textViewToFill;
        this.eventObject = eventObject;

    }

    public void run() {
        String stringFromObject = eventObject.getString(dynamicField);
        textViewToFill.setText(stringFromObject);
    }
}
