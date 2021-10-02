package rs.elfak.findpet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends android.app.Application{
    public static final String LOCATION_SERVICE_FOREGROUND_CHANNEL = "findpet_foreground";
    public static final String USER_NEAR_CHANNEL_ID =  "findpet_user_near";
    public static final String PET_NEAR_CHANNEL_ID =  "findpet_pet_near";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel userNearChannel = new NotificationChannel(
                    USER_NEAR_CHANNEL_ID,
                    "UserNear",
                    NotificationManager.IMPORTANCE_HIGH
            );
            userNearChannel.setDescription("You have a user near your location");

            NotificationChannel petNearChannel = new NotificationChannel(
                    PET_NEAR_CHANNEL_ID,
                    "PetNear",
                    NotificationManager.IMPORTANCE_HIGH
            );
            petNearChannel.setDescription("You have a pet near your location");

            NotificationChannel locationServiceForegroundChannel = new NotificationChannel(
                    LOCATION_SERVICE_FOREGROUND_CHANNEL,
                    "FindPetForeground",
                    NotificationManager.IMPORTANCE_LOW
            );
            locationServiceForegroundChannel.setDescription("FindPet location service is running in foreground");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(userNearChannel);
            notificationManager.createNotificationChannel(petNearChannel);
            notificationManager.createNotificationChannel(locationServiceForegroundChannel);
        }
    }


}
