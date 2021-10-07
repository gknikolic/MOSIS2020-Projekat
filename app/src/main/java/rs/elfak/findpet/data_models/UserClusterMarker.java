package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

public class UserClusterMarker extends ClusterMarker {
    public User user;

    public UserClusterMarker(LatLng position, String title, String snippet, Bitmap iconPicture, User user) {
        super(position, title, snippet, iconPicture);
        this.user = user;
    }

    @Override
    public String toString() {
        return user.username; // What to display in the Spinner list.
    }
}
