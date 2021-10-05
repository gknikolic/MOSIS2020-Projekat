package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class User implements Serializable, Parcelable {
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

    protected User(Parcel in) {
        key = in.readString();
        username = in.readString();
        phoneNumber = in.readString();
        email = in.readString();
        fullName = in.readString();
        locationEnabled = in.readByte() != 0;
        profilePictureUploaded = in.readByte() != 0;
        totalPoints = in.readInt();
        profilePicture = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(key);
        dest.writeString(username);
        dest.writeString(String.valueOf(profilePicture));
        dest.writeString(phoneNumber);
        dest.writeString(fullName);
        dest.writeString(String.valueOf(totalPoints));
        dest.writeString(String.valueOf(profilePictureUploaded));
    }

    @Exclude
    public String getUser_id() {
        return this.key;
    }

}
