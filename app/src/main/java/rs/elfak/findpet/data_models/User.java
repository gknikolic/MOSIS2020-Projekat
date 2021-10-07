package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class User implements Serializable {
    public String username;
    public String phoneNumber;
    public String email;
    public String fullName;
    public Location location;
    public boolean locationEnabled;
    public boolean profilePictureUploaded;
    public int totalPoints;
    @Exclude
    public String key;
    @Exclude
    public transient Bitmap profilePicture;

    public User(){}

    public User(String username, String email, String phoneNumber, String fullName) {
        this.username = username;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.location = new Location();
        this.totalPoints = 0;
        this.locationEnabled = true; //todo change to false!!!!
    }

    @Exclude
    public String getUser_id() {
        return this.key;
    }

    @Override
    public String toString() {
        return username;
    }
}
