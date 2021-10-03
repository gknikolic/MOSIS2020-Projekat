package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;
import android.location.Location;

import java.util.Date;

public class Post {
    public String userName;
    public Date timestamp;
    public Bitmap userImage;
    public String text;
    public Bitmap postImage;
    public String phoneNumber;
    public Location location;

    public Post() {

    }

    public Post(Date timestamp, Bitmap userImage, String text, Bitmap postImage, String phoneNumber, Location location) {
        this.timestamp = timestamp;
        this.userImage = userImage;
        this.text = text;
        this.postImage = postImage;
        this.phoneNumber = phoneNumber;
        this.location = location;
    }
}
