package dhbw.mobile2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Vincent on 08.06.2015.
 */
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

        String timeScopeString = this.convertDateToString(creationTime)
                + " - " +
                this.convertDateToString(finishTime);
        return timeScopeString;

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

    public String getParticipantsString(ParseObject object){
        List<ParseUser> listParticipants = object.getList("participants");
        int maxMembers = object.getInt("maxMembers");
        String textParticipants = listParticipants.size() + "/" + maxMembers;
        return textParticipants;

    }

    public Double calculateDistance(ParseGeoPoint userLocation, ParseGeoPoint eventPoint){
        double distance = userLocation.distanceInKilometersTo(eventPoint);
        //get just the kilometer amount with one digit behind the comma
        distance = ((int) distance * 10) / 10.0;
        return distance;
    }

    // add an image depending on the category of the event,
    // return sport of category cannot be detected
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

}
