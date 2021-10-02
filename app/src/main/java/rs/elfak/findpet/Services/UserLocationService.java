package rs.elfak.findpet.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

import rs.elfak.findpet.App;
import rs.elfak.findpet.MainActivity;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.User;
import rs.elfak.findpet.RepositoryEventListeners.UsersListEventListener;

public class UserLocationService extends Service implements UsersListEventListener, LocationListener{

    private static final long MSECONDS = 10000;
    private static final float DISTANCE = 100;
    private static final float OBJECT_NEAR_DISTANCE = 200;
    private static final String TAG = "UserLocationService";
    private LocationListener locationListener;
    private LocationManager locationManager;
    Location location;
    private NotificationManagerCompat notificationManager;
    private User currentUser;
    private UsersData usersDataReference;

    public UserLocationService() {
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        usersDataReference = UsersData.getInstance();
        currentUser = usersDataReference.getCurrentLogedUser();
        notificationManager = (NotificationManagerCompat) NotificationManagerCompat.from(this);



        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                usersDataReference.updateLocation(location);
                checkForNearUser(location);
                checkForNearPets(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MSECONDS,
                DISTANCE,
                this
        );
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean useGps = true;
        if(intent != null)
             useGps = intent.getBooleanExtra("useGps", true);
        if (useGps){
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MSECONDS,
                    DISTANCE,
                    this
            );
        }
        else{
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MSECONDS,
                    DISTANCE,
                    this
            );
        }

        String locationProvider = (useGps)?"GPS":"Network based";
        showForegroundNotification(locationProvider);

        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void showForegroundNotification(String locationProvider){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                0
        );
        Notification notification =
                new NotificationCompat.Builder(this, App.LOCATION_SERVICE_FOREGROUND_CHANNEL)
                        .setContentTitle("Foreground Location Service")
                        .setContentText(locationProvider + " location service is running in foreground")
                        .setSmallIcon(R.drawable.ic_location_on_24)
                        .setContentIntent(pendingIntent)
                        .build();

        startForeground(1, notification);
    }

    public void checkForNearUser(Location location){
//        Log.i(TAG, "check for near user users_list_len: " + usersDataReference.getUsers().size());
        for(User user: usersDataReference.getUsers()){
            if(!user.key.equals(currentUser.key)){
                float[] distance = new float[1];
                Location.distanceBetween(
                        location.getLatitude(),
                        location.getLongitude(),
                        Double.parseDouble(user.latitude),
                        Double.parseDouble(user.longitude),
                        distance
                );
                Log.i(TAG, "User: " + user.username);
                Log.i(TAG, "Distance: " + Float.toString(distance[0]));
                if(distance[0] < OBJECT_NEAR_DISTANCE && currentUser.locationEnabled){
                    sendUserNearNotification(user.username);
                }
            }
        }
    }

    private void sendUserNearNotification(String username){
        Intent notificationIntent = new Intent(this, MainActivity.class); //ovde treba mapa da se otvori
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                0
        );
        Log.i(TAG, "Usao u notification!!!");
        Notification notification =
                new NotificationCompat.Builder(this, App.USER_NEAR_CHANNEL_ID)
                        .setContentTitle("User Near!")
                        .setContentText("User is near you")
                        .setSmallIcon(R.drawable.ic_person_pin_24)
                        .setContentIntent(pendingIntent)
                        .build();

        notificationManager.notify(2, notification);
    }

    public void checkForNearPets(Location location){
        //todo: make this method
        //same as checkForNearUser just for pets
    }

    private void sendPetNearNotification(){
        Intent notificationIntent = new Intent(this, MainActivity.class); //ovde treba mapa da se otvori
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                0
        );
        Notification notification =
                new NotificationCompat.Builder(this, App.PET_NEAR_CHANNEL_ID)
                        .setContentTitle("User Near!")
                        .setContentText("User is near you")
                        .setSmallIcon(R.drawable.ic_pets_24)
                        .setContentIntent(pendingIntent)
                        .build();

        notificationManager.notify(3, notification);
    }


    @Override
    public void OnUsersListUpdated() {
        for (User user : UsersData.getInstance().getUsers())
            if (!user.key.equals(currentUser.key) && location != null) {
                float[] distance = new float[2];
                Location.distanceBetween(Double.parseDouble(user.latitude), Double.parseDouble(user.longitude), location.getLatitude(),
                        location.getLongitude(), distance);
                if (distance[0] < 100 && user.locationEnabled)
                    sendUserNearNotification(user.username);
            }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.location = location;
        usersDataReference.updateLocation(location);
        checkForNearUser(location);
        checkForNearPets(location);
    }


    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {

    }

    @Override
    public void onFlushComplete(int requestCode) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
