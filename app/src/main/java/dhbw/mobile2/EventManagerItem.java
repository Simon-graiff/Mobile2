package dhbw.mobile2;

import com.google.android.gms.maps.model.LatLng;

public class EventManagerItem {

    String markerID;
    String eventID;
    LatLng location;

    public EventManagerItem(String markerID, String eventID, LatLng location){
        this.markerID = markerID;
        this.eventID = eventID;
        this.location = location;
    }

    public String getMarkerID(){
        return markerID;
    }

    public  String getEventID(){
        return eventID;
    }

    public LatLng getLocation(){
        return location;
    }
}
