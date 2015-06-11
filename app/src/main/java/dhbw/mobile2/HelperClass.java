package dhbw.mobile2;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.widget.ImageView;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class HelperClass {

    /*
    The method convertDateToString(Date date) does what its name already tells:
    It converts a variable in a Date Format into a variable in a String format.
     */
    public String convertDateToString(Date date){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        String dateString = calendar.get(Calendar.HOUR_OF_DAY)+ ":";

        //if the minutes are below 10 add a zero to make every number have the same length
        if (calendar.get(Calendar.MINUTE)< 10){
            dateString += "0";}
            dateString += calendar.get(Calendar.MINUTE);
        return dateString;
    }

    /*
    The method getTimeScopeString(ParseObject object) takes an EventObject and
    builds its creationTime and finishTime into a String to have the time scope for the whole event.
     */
    public String getTimeScopeString(ParseObject object){
        Date creationTime;
        Date finishTime;
        creationTime = object.getCreatedAt();
        finishTime = object.getDate("duration");

        return this.convertDateToString(creationTime)
                + " - " +
                this.convertDateToString(finishTime);

    }

    //switches to the specified fragment
    public void switchToFragment(FragmentManager fragmentManager, Fragment fragment){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //the ProfileFragment differs slightly so it needs a special method
    public void switchToProfileFragment(FragmentManager fragmentManager, String userId){
        switchToFragment(fragmentManager, ProfileFragment.newInstance(userId));
    }

    //Get the number of participants of the event in the format:
    //currentParticipants/maximumParticipants
    public String getParticipantsString(ParseObject object){
        List<ParseUser> listParticipants = object.getList("participants");
        int maxMembers = object.getInt("maxMembers");
        return listParticipants.size() + "/" + maxMembers;

    }

    /*
    CalculateDistance calculates distance between two ParseGeoPoints.
    Therefore the ParseGeoPoint method distanceInKilometersTo is used,
    which returns the distance as a double. In the application a distance with
    one digit behind the comma is wanted, so distance is
    multiplied by 10 and then divided by an integer 10.
     */
    public Double calculateDistance(ParseGeoPoint userLocation, ParseGeoPoint eventPoint){
        double distance = userLocation.distanceInKilometersTo(eventPoint);
        //get just the kilometer amount with one digit behind the comma
        distance = ((int) distance * 10) / 10.0;
        return distance;
    }

    // add an image depending on the category of the event,
    // return sport if category cannot be detected
    public void setCategoryImage(ImageView categoryImage, String category){
        switch (category){
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
            case "Video Games":
                categoryImage.setImageResource(R.drawable.ic_videogames_blue);
                break;
            default:
                categoryImage.setImageResource(R.drawable.ic_sport_blue);
                break;
        }
    }

    /*
    The EventDetailFragment needs the objectId of the event it is supposed to display as described
    in the chapter of EventDetail. The method putEventIdToSharedPref saves the given Event-ID as a
    SharedPrefences in the activity. It need a SharedPreferences object and a SharedPreferences
     Editor object of the SharedPreferences objekt. It then saved the Event-ID as the variable
     "eventId" as SharedPreferences and uses "apply" to save it asynchronously.
     */
    public void putEventIdToSharedPref(Activity activity, String eventId){
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("eventId", eventId);
        editor.apply();
    }

    //get a cropped circle out of the image
    public Bitmap getCroppedCircleBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

}
