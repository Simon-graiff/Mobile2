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
public class ParticipantsListAdapter extends ArrayAdapter {
    private final Activity context;
    private final ArrayList<String> itemname;
    private final ArrayList<Bitmap> img;

    public ParticipantsListAdapter(Activity context, ArrayList<String> itemname, ArrayList<Bitmap> img) {
        super(context, R.layout.list_participants, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.img=img;
    }

    public View getView(int position,View view,ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_participants, null,true);

        TextView userName = (TextView) rowView.findViewById(R.id.user_name);
        ImageView userPicture = (ImageView) rowView.findViewById(R.id.user_picture);

        userName.setText(itemname.get(position));
        userPicture.setImageBitmap(img.get(position));
        return rowView;

    };
}
