package dhbw.mobile2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

import static android.widget.Toast.LENGTH_SHORT;
import static dhbw.mobile2.R.*;

public class ProfileFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private String username;
    private String gender;
    private String aboutMe;
    private ParseFile profilepictureFile;
    private Bitmap profilepictureFileBitmap;
    private View rootView = null;
    private ProgressDialog progressDialog;
    private String userID;

    public static ProfileFragment newInstance(String userID) {
        ProfileFragment f = new ProfileFragment();
        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("UserID", userID);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        progressDialog = ProgressDialog.show(getActivity(), "Loading", "Please wait.."); //Show loading dialog until data has been pulled from parse
        rootView = inflater.inflate(layout.fragment_profile, container, false);
        userID= getArguments().getString("UserID");

        //Add OnClickListener to the Profile data Change Entry
        rootView.findViewById(id.layout_change).setOnClickListener(this);
        rootView.findViewById(id.layout_reload).setOnClickListener(this);

        //Add OnClickListener to the Profile data Change Entry
        rootView.findViewById(id.layout_profile_fragment).setOnTouchListener(this);
        rootView.findViewById(id.editText_username).setOnTouchListener(this);


         //Check if the current's user profile is called or another users profile
        if(userID.equals(ParseUser.getCurrentUser().getObjectId())){
            username = ParseUser.getCurrentUser().getUsername();
            gender= ParseUser.getCurrentUser().getString("gender");
            aboutMe =ParseUser.getCurrentUser().getString("aboutMe");
            profilepictureFile = ParseUser.getCurrentUser().getParseFile("profilepicture");
            profilepictureFileBitmap = getRoundedCornerBitmap();
            updateLayout();
        }else{
            pullUserDataFromParse();
        }
       return rootView;
    }

    //Hiding the Keyboard if not needed
    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private void updateLayout() {
        //Update Profile Picutre
        ImageView imageView = (ImageView) rootView.findViewById(id.imageView_ProfilePicuture);
        // Get the Pixesl of the Screen to Scale the ProfilePicture according to the Screen Size
        int heightPixels = getActivity().getApplicationContext().getResources().getDisplayMetrics().heightPixels;
        //Set the ProfilePicuture to the ImageView and scale it to the screen size
        imageView.setImageBitmap(Bitmap.createScaledBitmap(profilepictureFileBitmap, ((int) (heightPixels * 0.2)), ((int) (heightPixels * 0.2)), false));


        //Set user information
        getActivity().setTitle(username); //Change ActionBar title to the username's title
        TextView usernameView = (TextView) rootView.findViewById(id.editText_username);
        usernameView.setText(username);

        TextView aboutMeView = (TextView) rootView.findViewById(id.editText_about);
        aboutMeView.setText(aboutMe);

        //Only show about me if any text is entered
            if (!aboutMe.equals("")){
                rootView.findViewById(id.editText_about).setVisibility(View.VISIBLE);
            }

        if(gender.equalsIgnoreCase("male")){
            //Disable female Button
            ImageButton femaleButton =(ImageButton) rootView.findViewById(id.imageButton_female);
            femaleButton.setImageResource(drawable.ic_female_disabled);
        } else if (gender.equalsIgnoreCase("female")){
            //Dissable male BUtton
            ImageButton maleButton =(ImageButton) rootView.findViewById(id.imageButton_male);
            maleButton.setImageResource(drawable.ic_male_disabled);
        }

        //Adjust ProfileLayout if called user profile is the own profle
        if(userID.equals(ParseUser.getCurrentUser().getObjectId())){
            //Show the Change Profile data Fields
            rootView.findViewById(id.layout_change).setVisibility(View.VISIBLE);
        }
        //Update Done! Close Loading Dialog
        progressDialog.dismiss();
    }

    //Fetching all information of an other user from parse
    private void pullUserDataFromParse(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", userID);
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    username = parseUser.getUsername();
                    gender = parseUser.getString("gender");
                    aboutMe =parseUser.getString("aboutMe");
                    profilepictureFile = parseUser.getParseFile("profilepicture");
                    profilepictureFileBitmap = getRoundedCornerBitmap(); //Transfer ParseFile to Bitmap
                    updateLayout();
                } else {
                    Log.e("ParseUser", "Cannot retrive User with UserID= " + userID);
                    e.printStackTrace();
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

    @Override
    public void onClick(View v) {

        if(v.getId()!= id.layout_reload) {
            //Hide Change Buttons
            rootView.findViewById(id.layout_change).setVisibility(View.GONE);
            rootView.findViewById(id.editText_about).setVisibility(View.VISIBLE);
            rootView.findViewById(id.editText_about).setFocusableInTouchMode(true);
            rootView.findViewById(id.editText_about).setFocusable(true);
            rootView.findViewById(id.layout_reload).setVisibility(View.VISIBLE);

        }
        else{
            //Loading Screen while reloading changes in background
            progressDialog= ProgressDialog.show(getActivity(), "Loading Data", "Please wait..");
            //Save changes
            reloadFacebookInformation();
            //Save aboutMe before reloading Layout
            EditText aboutMeTextField = (EditText) rootView.findViewById(id.editText_about);
            String aboutMe = aboutMeTextField.getText().toString();
            if(aboutMe!=null){
                ParseUser.getCurrentUser().put("aboutMe", aboutMe);
                ParseUser.getCurrentUser().saveInBackground();
            }
            updateFragmentScreen();
              }
    }

    private void updateFragmentScreen(){
        //fetch new Data of Database
        ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    //Reload Screen
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.frame_container, newInstance(userID)).commit();
                    progressDialog.dismiss();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    public void onResume(){
        super.onResume();
        ListView mDrawerList;
        mDrawerList = (ListView) getActivity().findViewById(R.id.list_slidermenu);
        mDrawerList.setItemChecked(0, true);
        mDrawerList.setSelection(0);
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(username);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
           hideSoftKeyboard();

        //Check if any changes were made
        EditText aboutMeTextField = (EditText) rootView.findViewById(id.editText_about);
        String newAboutMe = aboutMeTextField.getText().toString();
        if (!aboutMe.equals(newAboutMe)) {
            //Save changes
            aboutMe = newAboutMe;
            ParseUser.getCurrentUser().put("aboutMe", newAboutMe);
            ParseUser.getCurrentUser().saveInBackground();
            //Inform user that the changes are saved
            Toast.makeText(getActivity().getWindow().getContext(), "Saved changes!", Toast.LENGTH_LONG).show();

            FragmentManager fragmentManager = getFragmentManager();
            Fragment fragment = ProfileFragment.newInstance(ParseUser.getCurrentUser().getObjectId());
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        }

        return false;
    }
    
    //This function calls a cloud code function which updates the parse user with the facebook user
    private void reloadFacebookInformation(){
        Map<String, Object> param = new HashMap<>();
        ParseCloud.callFunctionInBackground("reloadFacebookInformation", param, new FunctionCallback<Object>() {
            @Override
            public void done(Object stringObjectMap, ParseException e) {
                if (e == null) {
                    Toast.makeText(getActivity(), "Your profile was updated", Toast.LENGTH_LONG).show();
                    Log.d("reloadFacebookInformation", "successful");
                } else {
                    Toast.makeText(getActivity(), "Error relloading Data", LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

        });
    }
}
