package rs.elfak.findpet.data_models;

import com.google.android.gms.maps.model.LatLng;

public class Location {
    public String latitude;
    public String longitude;

    public Location() {
        this.latitude="";
        this.longitude="";
    }

    public Location(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLng getLocation() {
        return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    public Double getLatitude() {
        return Double.parseDouble(this.latitude);
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return Double.parseDouble(this.longitude);
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
