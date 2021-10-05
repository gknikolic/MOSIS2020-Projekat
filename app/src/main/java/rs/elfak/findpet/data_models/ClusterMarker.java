package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {
    public LatLng position;
    public String title;
    public String snippet;
    public Bitmap iconPicture;
    public User user;

    public ClusterMarker(LatLng position, String title, String snippet, Bitmap iconPicture, User user) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPicture = iconPicture;
        this.user = user;
    }

    public ClusterMarker() {

    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    @Override
    public String toString() {
        return user.username; // What to display in the Spinner list.
    }
}
