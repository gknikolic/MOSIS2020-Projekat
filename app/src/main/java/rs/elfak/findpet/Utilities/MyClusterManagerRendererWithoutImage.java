package rs.elfak.findpet.Utilities;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import rs.elfak.findpet.data_models.ClusterMarker;

public class MyClusterManagerRendererWithoutImage extends MyClusterManagerRenderer {
    public MyClusterManagerRendererWithoutImage(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
        imageView.setImageBitmap(item.iconPicture);
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.title(item.title);
    }
}
