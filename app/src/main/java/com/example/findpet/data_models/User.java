package com.example.findpet.data_models;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.google.firebase.database.Exclude;

public class User {
    public String username;
    public String phoneNumber;
    public String email;
    public String latitude;
    public String longitude;
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
        this.latitude = "";
        this.longitude = "";
        this.totalPoints = 0;
    }

}
