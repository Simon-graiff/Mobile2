package dhbw.mobile2;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Vincent on 24.05.2015.
 */
public class EventlistAdapter extends ArrayAdapter {
    private final Activity context;
    private final ArrayList<String> itemname;
    private final ArrayList<String> category;
    private ArrayList<Double> distance;

    public EventlistAdapter(Activity context, ArrayList<String> itemname, ArrayList<String> category, ArrayList<Double> distance) {
        super(context, R.layout.listitem_event, itemname);
        // TODO Auto-generated constructor stub

        this.distance=distance;
        this.context=context;
        this.itemname=itemname;
        this.category=category;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listitem_event, null, true);

        TextView eventName = (TextView) rowView.findViewById(R.id.event_name);
        ImageView categoryImage = (ImageView) rowView.findViewById(R.id.category_picture);
        TextView distanceView = (TextView) rowView.findViewById(R.id.distance);

        long distanceLong = distance.get(position).longValue();
        distanceView.setText(distanceLong+ "km");
        eventName.setText(itemname.get(position));

        categoryImage.setImageResource(R.drawable.ic_sport);
        switch (category.get(position)){
            case "sport":
                categoryImage.setImageResource(R.drawable.ic_sport);
                break;
            case "chilling":
                categoryImage.setImageResource(R.drawable.ic_chilling);
            case "food":
                categoryImage.setImageResource(R.drawable.ic_food);
        }

        return rowView;

    };
}
