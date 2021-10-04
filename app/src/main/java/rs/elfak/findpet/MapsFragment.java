package rs.elfak.findpet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.Helpers.Helpers;
import rs.elfak.findpet.data_models.Pet;
import rs.elfak.findpet.data_models.User;

public class MapsFragment extends Fragment {

    GoogleMap map;
    Spinner caseTypeSpinner;
    User currentUser;
    ArrayList<User> friends;
    Pet pet;


    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

            LatLng myLocation = currentUser.location.getLocation();
            map.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
            map.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            moveToLocation(myLocation);

            //create marker with picture, only for friends
//            createMarkerWithBitmap(currentUser.location.getLocation(), Helpers.bitmapFromUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSPjkW6L6Fi2RYRQtGGPZeDA_Qt0qADmENA6A&usqp=CAU"), currentUser.username);
//            moveToLocation(myLocation);

            Log.i("MAPS", "On map create method");
        }
    };

    private void createMarkerWithBitmap(LatLng location, Bitmap picture, String title) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(80, 80, conf);
        Canvas canvas1 = new Canvas(bmp);

        // paint defines the text color, stroke width and size
        Paint color = new Paint();
        color.setTextSize(35);
        color.setColor(Color.BLACK);

        // modify canvas
        canvas1.drawBitmap(picture, 0,0, color);
        canvas1.drawText(title, 30, 40, color);

        // add marker to Map
        map.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                // Specifies the anchor to be at a particular point in the marker image.
                .anchor(0.5f, 1));
    }

    private void moveToLocation(LatLng location)
    {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //from main activity
        currentUser = (User) getArguments().getSerializable(Constants.USER_KEY);
        friends = (ArrayList<User>) getArguments().getSerializable(Constants.FREINDS_KEY);
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        caseTypeSpinner = (Spinner) view.findViewById(R.id.mapsFragment_caseTypeSpinner);
        caseTypeSpinner.setAdapter(new ArrayAdapter<CaseType>(getContext(), android.R.layout.simple_spinner_item, CaseType.values()));
    }
}