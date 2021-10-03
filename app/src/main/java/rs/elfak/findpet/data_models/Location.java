package rs.elfak.findpet.data_models;

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
}
