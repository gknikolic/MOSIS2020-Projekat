package rs.elfak.findpet.data_models;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Location  implements Serializable {
    public double latitude;
    public double longitude;

    public Location() {
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
