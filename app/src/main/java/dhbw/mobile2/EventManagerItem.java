package dhbw.mobile2;

public class EventManagerItem {

    String markerID;
    String eventID;

    public EventManagerItem(String markerID, String eventID){
        this.markerID = markerID;
        this.eventID = eventID;
    }

    public String getMarkerID(){
        return markerID;
    }

    public  String getEventID(){
        return eventID;
    }
}
