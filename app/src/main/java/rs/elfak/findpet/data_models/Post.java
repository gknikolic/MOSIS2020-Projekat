package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;


import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Date;

import rs.elfak.findpet.Enums.CaseType;

public class Post implements Serializable {
    public Date timestamp;
    public String text;
    public boolean IsFinished;
    public Location location;
    public String userKey;
    public Pet pet;
    public CaseType caseType;

    @Exclude
    public String key;

    @Exclude
    public transient Bitmap image;
}
