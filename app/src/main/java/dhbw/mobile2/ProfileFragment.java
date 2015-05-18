package dhbw.mobile2;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

public class ProfileFragment extends Fragment {

    public ProfileFragment (){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);


        Bitmap bitmap = getRoundedCornerBitmap();
        ImageView imageView=(ImageView) rootView.findViewById(R.id.imageView_ProfilePicuture);
        // Get the Pixesl of the Screen to Scale the ProfilePicture according to the Screen Size
        int heightPixels = getActivity().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        //Set the ProfilePicuture to the ImageView and scale it to the screen size
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, ((int)(heightPixels*0.2)), ((int)(heightPixels*0.2)), false));

        //Set Username and Gender
        TextView username =(TextView) rootView.findViewById(R.id.textView_Username);
        username.setText("Username: " + ParseUser.getCurrentUser().getUsername());
        TextView gender =(TextView) rootView.findViewById(R.id.textView_Gender);
        gender.setText("Gender: "+ ParseUser.getCurrentUser().get("gender"));



        return rootView;


    }


    /**
     * This Function returns a Bitmap that thats cut to a circle
     * @return
     */

    public static Bitmap getRoundedCornerBitmap() {
        ParseFile profilepicture = ParseUser.getCurrentUser().getParseFile("profilepicture");
        byte [] data = new byte[0];
        try {
            data = profilepicture.getData();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length);


        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 100;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;

    }
}
