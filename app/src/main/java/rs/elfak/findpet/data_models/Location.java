package rs.elfak.findpet.data_models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

public class Location {
    public Double latitude;
    public Double longitude;

    public Location() {
        this.latitude=null;
        this.longitude=null;
    }

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Exclude
    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

}
