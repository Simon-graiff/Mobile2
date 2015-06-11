package dhbw.mobile2;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mats on 13.05.15.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private final String TAG = "HALLO";

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
        Log.d(TAG, "CREATED");
    }

    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "CALLED");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Location location = geofencingEvent.getTriggeringLocation();
        if (geofencingEvent.hasError()) {
            String errorMessage = ""+geofencingEvent.getErrorCode();
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Map<String, Object> param = new HashMap<>();
            param.put("longitude", location.getLongitude());
            param.put("latitude", location.getLatitude());
            param.put("userId", ParseUser.getCurrentUser().getObjectId());

            //Location ermitteln und Server-Request abschicken
            Log.i(TAG, "Geofence gefunden");
            ParseCloud.callFunctionInBackground("getNearEvents", param, new FunctionCallback<Map<String, Object>>() {
                @Override
                public void done(Map<String, Object> stringObjectMap, ParseException e) {
                    if (e == null) {
                        Log.d(TAG, "Geofence Backgorund-Call successfull");
                        Log.d(TAG, stringObjectMap.get("events").toString());
                    } else {
                        Log.d(TAG,"Error im Cloud Code");
                    }
                }
            });
        } else {
            Log.e(TAG, "Error");
        }
    }
}
