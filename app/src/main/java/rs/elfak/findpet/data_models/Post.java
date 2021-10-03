package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;


import java.util.Date;

import rs.elfak.findpet.Enums.CaseType;

public class Post {
    public Date timestamp;
    public String text;
    public Bitmap image;
    public boolean IsFinished;
    public Location location;
    public User user;
    public Pet pet;
    public CaseType caseType;

}
