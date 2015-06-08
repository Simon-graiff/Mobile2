package dhbw.mobile2;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class EventlistAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> itemname;
    private final ArrayList<String> category;
    private ArrayList<Double> distance;
    private ArrayList<String> time;
    private ArrayList<String> participantCount;

    private HelperClass helperObject = new HelperClass();

    public EventlistAdapter(Activity context,
                            ArrayList<String> itemname,
                            ArrayList<String> category,
                            ArrayList<Double> distance,
                            ArrayList<String> time,
                            ArrayList<String> participantCount) {
        super(context, R.layout.list_item_event, itemname);

        this.distance=distance;
        this.context=context;
        this.itemname=itemname;
        this.category=category;
        this.time = time;
        this.participantCount = participantCount;
    }

    public View getView(int position,View view,ViewGroup parent) {
        View rowView = context.getLayoutInflater().inflate(R.layout.list_item_event, null, true);

        TextView eventName      = (TextView)    rowView.findViewById(R.id.event_name);
        ImageView categoryImage = (ImageView)   rowView.findViewById(R.id.category_picture);
        TextView distanceView   = (TextView)    rowView.findViewById(R.id.distance);
        TextView timeView       = (TextView)    rowView.findViewById(R.id.time);
        TextView participantView= (TextView)    rowView.findViewById(R.id.participantCount);

        //set all fields with their respective content
        distanceView.setText(distance.get(position) + " km");
        eventName.setText(itemname.get(position));
        timeView.setText(time.get(position));
        participantView.setText(participantCount.get(position));
        helperObject.setCategoryImage(categoryImage, category.get(position));

        return rowView;

    }
}
