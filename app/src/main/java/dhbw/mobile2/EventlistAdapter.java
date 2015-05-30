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

    public EventlistAdapter(Activity context, ArrayList<String> itemname, ArrayList<String> category, ArrayList<Double> distance) {
        super(context, R.layout.listitem_event, itemname);

        this.distance=distance;
        this.context=context;
        this.itemname=itemname;
        this.category=category;
    }

    public View getView(int position,View view,ViewGroup parent) {
        View rowView = context.getLayoutInflater().inflate(R.layout.listitem_event, null, true);

        TextView eventName = (TextView) rowView.findViewById(R.id.event_name);
        ImageView categoryImage = (ImageView) rowView.findViewById(R.id.category_picture);
        TextView distanceView = (TextView) rowView.findViewById(R.id.distance);

        long distanceLong = distance.get(position).longValue();
        distanceView.setText(distanceLong+ " km");
        eventName.setText(itemname.get(position));

        categoryImage.setImageResource(R.drawable.ic_sport);
        switch (category.get(position)){
            case "Sport":
                categoryImage.setImageResource(R.drawable.ic_sport_blue);
                break;
            case "Chilling":
                categoryImage.setImageResource(R.drawable.ic_chilling_blue);
                break;
            case "Dancing":
                categoryImage.setImageResource(R.drawable.ic_dance_blue);
                break;
            case "Food":
                categoryImage.setImageResource(R.drawable.ic_food_blue);
                break;
            case "Music":
                categoryImage.setImageResource(R.drawable.ic_music_blue);
                break;
            case "Videogames":
                categoryImage.setImageResource(R.drawable.ic_videogames_blue);
                break;
        }

        return rowView;

    }
}
