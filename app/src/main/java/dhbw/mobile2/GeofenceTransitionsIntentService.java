package dhbw.mobile2;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by Mats on 13.05.15.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private final String TAG = "GTIS";

    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = ""+geofencingEvent.getErrorCode();
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            //Location ermitteln und Server-Request abschicken
            Log.i(TAG, "Geofence gefunden");
        } else {
            Log.e(TAG, "Error");
        }
    }
}
