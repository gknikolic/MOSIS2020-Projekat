package rs.elfak.findpet.Services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import java.util.List;

import rs.elfak.findpet.App;
import rs.elfak.findpet.Activities.MainActivity;
import rs.elfak.findpet.Enums.FragmentName;
import rs.elfak.findpet.Helpers.Constants;
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
        currentUser = usersDataReference.getCurrentLoggedUser();
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
        Log.i("OnStart", "Service started");
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
        Log.i(TAG, "LOCATION CHANGED ############################");
        for(User user: usersDataReference.getUsers()) {
            Log.i(TAG, "User: " + user.username);
        }
        currentUser = usersDataReference.getCurrentLoggedUser();
        if(currentUser != null){
            Log.i(TAG, "Current user not null");
            for(User user: usersDataReference.getUsers()){
                if(!user.key.equals(currentUser.key)){
                    if(user.locationEnabled){
                        float[] distance = new float[1];
                        Location.distanceBetween(
                                location.getLatitude(),
                                location.getLongitude(),
                                user.location.latitude,
                                user.location.longitude,
                                distance
                        );
                        Log.v(TAG, "User: " + user.username);
                        Log.v(TAG, "Distance: " + Float.toString(distance[0]));
                        if(distance[0] < OBJECT_NEAR_DISTANCE && currentUser.locationEnabled){
                            sendUserNearNotification(user.username);
                        }
                    }
                }
            }
        }
    }

    private void sendUserNearNotification(String username){
        Intent notificationIntent = new Intent(this, MainActivity.class); //ovde treba mapa da se otvori
        notificationIntent.putExtra(Constants.FRAGMENT_ENUM_KEY, FragmentName.Friends);
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
                        .setContentText("User " + username + " is near you")
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
        for(User user: UsersData.getInstance().getUsers()){
            Log.i(TAG, "#########################" + user.username + "#########################");
        }
        if(currentUser != null){
            Log.i("UsersDataCallback", "Callback entered");
            for (User user : UsersData.getInstance().getUsers()) {
                if (!user.key.equals(currentUser.key) && location != null) {
                    float[] distance = new float[2];
                    Location.distanceBetween(user.location.latitude, user.location.longitude, location.getLatitude(),
                            location.getLongitude(), distance);
                    if (distance[0] < 100 && user.locationEnabled)
                        sendUserNearNotification(user.username);
                }
            }
        }
    }

    @Override
    public void CurrentUserLoaded() {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.i("locationChanged", "Callback entered " + location.getLatitude() + " " + location.getLongitude());
        this.location = location;
        UsersData.getInstance().updateLocation(location);
        //TODO Uncomment this
        checkForNearUser(location);
//        checkForNearPets(location);
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

    @SuppressLint("MissingPermission")
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
