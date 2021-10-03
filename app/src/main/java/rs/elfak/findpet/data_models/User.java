package rs.elfak.findpet.data_models;

import android.graphics.drawable.Drawable;

import com.google.firebase.database.Exclude;

public class User {
    public String username;
    public String phoneNumber;
    public String email;
    public Location location;
    public boolean locationEnabled;
    public int totalPoints;
    @Exclude
    public String key;
    @Exclude
    public Drawable profilePicture;

    public User(){}

    public User(String username, String email, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.location = new Location();
        this.totalPoints = 0;
        this.locationEnabled = true; //todo change to false!!!!
    }

}
