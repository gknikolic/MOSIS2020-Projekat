package rs.elfak.findpet.data_models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;

public class PetClusterMarker extends ClusterMarker {
    public Post post;

    public PetClusterMarker(LatLng position, String title, String snippet, Bitmap iconPicture, Post post) {
        super(position, title, snippet, iconPicture);
        this.post = post;
    }

    @Override
    public String toString() {
        return post.caseType.toString() + " " + post.pet.type.toString() + ": " +  post.pet.name; // What to display in the Spinner list. and cluster marker title
    }
}
