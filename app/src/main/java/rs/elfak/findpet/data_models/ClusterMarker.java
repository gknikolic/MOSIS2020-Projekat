package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

public abstract class ClusterMarker implements ClusterItem, Serializable {
    public LatLng position;
    public String title;
    public String snippet;
    public Bitmap iconPicture;


    public ClusterMarker(LatLng position, String title, String snippet, Bitmap iconPicture) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPicture = iconPicture;
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

}
