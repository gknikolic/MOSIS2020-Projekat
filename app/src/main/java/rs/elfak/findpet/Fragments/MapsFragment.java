package rs.elfak.findpet.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import rs.elfak.findpet.Activities.MainActivity;
import rs.elfak.findpet.Adapters.ClusterSpinnerAdapter;
import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.R;
import rs.elfak.findpet.data_models.ClusterMarker;
import rs.elfak.findpet.data_models.User;
import rs.elfak.findpet.Utilities.MyClusterManagerRenderer;

public class MapsFragment extends Fragment {

    private GoogleMap map;
    private Spinner caseTypeSpinner;
    private Spinner markersSpinner;
    private ClusterSpinnerAdapter clusterSpinnerAdapter;
    private User currentUser;
    private ArrayList<User> users;
    private Button btnMyMarker;
    private ProgressDialog progressDialog;

    private LatLngBounds mMapBoundary;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private List<ClusterMarker> mClusterMarkers = new ArrayList<>(); //markers on map


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //from main activity
        currentUser = (User) getArguments().getSerializable(Constants.USER_KEY);
        users = (ArrayList<User>) getArguments().getSerializable(Constants.FREINDS_KEY);
        final View rootView = inflater.inflate(R.layout.fragment_maps, container, false);
        return  rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            // setup a progress dialog
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("please wait..");

            progressDialog.show();
            mapFragment.getMapAsync(callback);
        }

        //TODO Investigate why onClick and onSelectItem listeners don't work in Google map activities

        caseTypeSpinner = (Spinner) getView().findViewById(R.id.mapsFragment_caseTypeSpinner);
        caseTypeSpinner.setAdapter(new ArrayAdapter<CaseType>(getContext(), R.layout.spinner_item, CaseType.values()));
        caseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(), "clicked: " + CaseType.values()[i].toString(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        markersSpinner = (Spinner) getView().findViewById(R.id.mapsFragment_markersSpinner); //adapter for this spinner is set after markers added in onMapReady function

        btnMyMarker = view.findViewById(R.id.mapsFragment_btnZoomMyMarker);
        btnMyMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("MAPS", "btn clicked");
                Toast.makeText(getContext(), "Clicked", Toast.LENGTH_LONG).show();
                cameraZoomToLocation(currentUser.location.getLocation());
            }
        });


    }

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

//            LatLng myLocation = currentUser.location.getLocation();
//            map.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
//            map.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
//            cameraZoomToLocation(myLocation);

            //TODO create marker with picture, only for friends
            addMapMarkers();

            initMarkersSpinner();
            progressDialog.dismiss();

            Log.i("MAPS", "On map create method");
        }
    };

    private void cameraZoomToLocation(LatLng location)
    {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    private void addMapMarkers(){

        if(map != null){

            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), map);
            }
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getActivity(),
                        map,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            //TODO Add filter for friends only
            for(User friend: users){

//                Log.d("CLUSTER_MARKER", "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                try{
                    String snippet = "";
                    if(friend.getUser_id().equals(currentUser.getUser_id())) {
                        snippet = "This is you";
                    }
                    else{
                        snippet = "Determine route to " + friend.username + "?";
                    }

                    Bitmap avatar = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.avatar);; // set the default avatar
                    try{
                        if(friend.profilePicture != null) {
                            avatar = friend.profilePicture;
                        }
                    }catch (NumberFormatException e){
                        Log.d("MAPS", "addMapMarkers: no avatar for " + friend.username + ", setting default.");
                    }
                    if(friend.location != null) {
                        ClusterMarker newClusterMarker = new ClusterMarker(
                                friend.location.getLocation(),
                                friend.username,
                                snippet,
                                avatar,
                                friend
                        );
                        mClusterManager.addItem(newClusterMarker);
                        mClusterMarkers.add(newClusterMarker);
                    }

                }catch (NullPointerException e){
                    Log.e("MAPS", "addMapMarkers: NullPointerException: " + e.getMessage() );
                }

            }
            mClusterManager.cluster();

            setCameraView();

//            cameraZoomToLocation(currentUser.location.getLocation());
        }

    }

    /**
     * Determines the view boundary then sets the camera
     * Sets the view
     */
    private void setCameraView() {

        // Set a boundary to start
        double bottomBoundary = currentUser.location.latitude - .1;
        double leftBoundary = currentUser.location.longitude - .1;
        double topBoundary = currentUser.location.latitude + .1;
        double rightBoundary = currentUser.location.longitude + .1;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
    }

    private void initMarkersSpinner() {
        clusterSpinnerAdapter = new ClusterSpinnerAdapter(getContext(),
                0,
                mClusterMarkers);
        markersSpinner.setAdapter(clusterSpinnerAdapter); // Set the custom adapter to the spinner

        // You can create an anonymous listener to handle the event when is selected an spinner item
//        markersSpinner.setAdapter(new ArrayAdapter<ClusterMarker>(getContext(), R.layout.spinner_item, mClusterMarkers));
        markersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ClusterMarker marker = clusterSpinnerAdapter.getItem(i);
                cameraZoomToLocation(marker.position);
                Log.i("MOVE MARKER", "selected user: " + marker.user);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}