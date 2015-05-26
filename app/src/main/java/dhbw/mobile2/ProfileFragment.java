package dhbw.mobile2;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class ProfileFragment extends Fragment {

    private String userID;
    private String username;
    private String gender;
    private ParseFile profilepictureFile;
    private Bitmap profilepictureFileBitmap;
    private View rootView = null;
    private ProgressDialog progressDialog;


    public static ProfileFragment newInstance(String UserID) {
        ProfileFragment f = new ProfileFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("UserID", UserID);
        f.setArguments(args);
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        progressDialog = ProgressDialog.show(getActivity(), "Loading", "Please wait.."); //Show loading dialog until data has been pulled from parse
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        userID= getArguments().getString("UserID");


        pullUserDataFromParse();
        return rootView;
    }

    private void updateLayout(){

        //Update Profile Picutre
        ImageView imageView=(ImageView) rootView.findViewById(R.id.imageView_ProfilePicuture);
        // Get the Pixesl of the Screen to Scale the ProfilePicture according to the Screen Size
        int heightPixels = getActivity().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        //Set the ProfilePicuture to the ImageView and scale it to the screen size
        imageView.setImageBitmap(Bitmap.createScaledBitmap(profilepictureFileBitmap, ((int) (heightPixels * 0.2)), ((int) (heightPixels * 0.2)), false));


        //Set user information
        getActivity().setTitle(username); //Change ActionBar title to the username's title
        TextView usernameView =(TextView) rootView.findViewById(R.id.textView_Username);
        usernameView.setText("Username: " + username);
        TextView genderView =(TextView) rootView.findViewById(R.id.textView_Gender);
        genderView.setText("Gender: " + gender);

        //Update Done! Close Loading Dialog
        progressDialog.dismiss();
    }

    private void pullUserDataFromParse(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userID);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {

                if (e == null) {
                    username = parseUser.getUsername();
                    gender = parseUser.getString("gender");
                    profilepictureFile = parseUser.getParseFile("profilepicture");
                    profilepictureFileBitmap = getRoundedCornerBitmap(); //Transfer ParseFile to Bitmap
                    updateLayout();
                } else {
                    Log.e("ParseUser", "Cannot retrive User with UserID= " + userID + " from Parse");
                }

            }
        });

    }




    /**
     * This Function returns a Bitmap that thats cut to a circle
     * @return
     */

    private Bitmap getRoundedCornerBitmap() {
        byte [] data = new byte[0];
        try {
            data = profilepictureFile.getData();
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
