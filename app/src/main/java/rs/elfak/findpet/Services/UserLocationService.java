package rs.elfak.findpet.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.User;

public class UserLocationService extends Service {

    private static final long MSECONDS = 10000;
    private static final float DISTANCE = 100;
    private static final float OBJECT_NEAR_DISTANCE = 200;
    private static final String TAG = "UserLocationService";
    private LocationListener locationListener;
    private LocationManager gpsLocationManager;
    private LocationManager networkLocationManager;
    private User currentUser;

    public UserLocationService() {
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        currentUser = UsersData.getInstance().getCurrentLogedUser();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                UsersData.getInstance().updateLocation(location);
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
        gpsLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        networkLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }

        gpsLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MSECONDS,
                DISTANCE,
                locationListener
        );
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStart service");
        boolean useGps = intent.getBooleanExtra("useGps", true);
        if (!useGps){
            networkLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MSECONDS,
                    DISTANCE,
                    locationListener
            );
        }
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    public void checkForNearUser(Location location){
        for(User user: UsersData.getInstance().getUsers()){
            if(!user.key.equals(currentUser.key)){
                float[] distance = new float[1];
                Location.distanceBetween(
                        location.getLatitude(),
                        location.getLongitude(),
                        Double.parseDouble(user.latitude),
                        Double.parseDouble(user.longitude),
                        distance
                );
                if(distance[0] < OBJECT_NEAR_DISTANCE){
                    //make a notification
                }
            }
        }
    }

    public void checkForNearPets(Location location){
        //todo: make this method
        //same as checkForNearUser just for pets
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(gpsLocationManager != null){
            gpsLocationManager.removeUpdates(locationListener);
        }
        if(networkLocationManager != null){
            networkLocationManager.removeUpdates(locationListener);
        }
    }
}