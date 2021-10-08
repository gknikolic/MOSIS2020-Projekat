package rs.elfak.findpet.Fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.TaskInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import rs.elfak.findpet.Adapters.ClusterSpinnerAdapter;
import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.Helpers.GetArrayListFromStream;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.RepositoryEventListeners.UsersListEventListener;
import rs.elfak.findpet.Utilities.MyClusterManagerRendererWithPicture;
import rs.elfak.findpet.Utilities.MyClusterManagerRendererWithoutImage;
import rs.elfak.findpet.data_models.ClusterMarker;
import rs.elfak.findpet.data_models.User;
import rs.elfak.findpet.Utilities.MyClusterManagerRenderer;
import rs.elfak.findpet.data_models.UserClusterMarker;

public class FriendsFragment extends Fragment {

    //params from main activity
    private User currentUser;
    private ArrayList<User> users;
    private boolean showOtherUsers = false;

    //for maps
    private LatLngBounds mMapBoundary;
    private ClusterManager<ClusterMarker> mClusterManagerWithPicture;
    private ClusterManager<ClusterMarker> mClusterManagerWithoutPicture;
    private MyClusterManagerRendererWithoutImage myClusterManagerRendererWithoutPicture;
    private MyClusterManagerRendererWithPicture myClusterManagerRendererWithPicture;
    private ClusterSpinnerAdapter clusterSpinnerAdapter;
    private List<ClusterMarker> mClusterMarkers = new ArrayList<>(); //markers on map

    //widgets
    private Button btnMyMarker;
    private ProgressDialog progressDialog;
    private GoogleMap map;
    private Spinner caseTypeSpinner;
    private Spinner markersSpinner;
    private SwitchCompat showOtherUsersSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //from main activity
        currentUser = (User) getArguments().getSerializable(Constants.USER_KEY);
        users = (ArrayList<User>) getArguments().getSerializable(Constants.FREINDS_KEY);
        final View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        return  rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.friendsFragment_map);
        if (mapFragment != null) {
            // setup a progress dialog
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle(R.string.progressDialogTitle);

            progressDialog.show();
            mapFragment.getMapAsync(callback);
        }

        markersSpinner = (Spinner) getView().findViewById(R.id.friendsFragment_markersSpinner); //adapter for this spinner is set after markers added in onMapReady function
        showOtherUsersSwitch = (SwitchCompat) getView().findViewById(R.id.friendsFragment_showOtherUsers);
        showOtherUsersSwitch.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if(showOtherUsersSwitch.isChecked()) {
                    UsersData.getInstance().addUpdateListener(userCallback);
                    addMapMarkersWithPicture(null);
                }
                else {
                    UsersData.getInstance().removeUpdateListener(userCallback);
                    removeMarkersWithPicture(null); //remove all except current user
                }
            }
        });


        btnMyMarker = view.findViewById(R.id.friendsFragment_btnZoomMyMarker);
        btnMyMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("MAPS", "btn clicked");
                Toast.makeText(getContext(), "Clicked", Toast.LENGTH_LONG).show();
                cameraZoomToLocation(currentUser.location.getLocation());
            }
        });


    }

    protected UsersListEventListener userCallback = new UsersListEventListener() {
        @Override
        public void OnUsersListUpdated() {

        }

        @Override
        public void CurrentUserLoaded() {

        }

        @Override
        public void OnUserLocationChanged(String userKey) {
            changeUserLocationOnMap(userKey);
        }
    };

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;

//            LatLng myLocation = currentUser.location.getLocation();
//            map.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
//            map.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
//            cameraZoomToLocation(myLocation);

            initMarkersSpinner();

            //on app start add only current user
            addMapMarkersWithPicture(currentUser.key);

            clusterSpinnerAdapter.notifyDataSetChanged();

            map.setMyLocationEnabled(true);

            setCameraView();

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

    private void addMapMarkersWithPicture(@Nullable String userKey) {

        if(map != null){

            if(mClusterManagerWithPicture == null){
                mClusterManagerWithPicture = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), map);
            }
            if(mClusterManagerWithoutPicture == null){
                mClusterManagerWithoutPicture = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), map);
            }
            if(myClusterManagerRendererWithPicture == null){
                myClusterManagerRendererWithPicture = new MyClusterManagerRendererWithPicture(
                        getActivity(),
                        map,
                        mClusterManagerWithPicture
                );
                mClusterManagerWithPicture.setRenderer(myClusterManagerRendererWithPicture);
            }
            if(myClusterManagerRendererWithoutPicture == null){
                myClusterManagerRendererWithoutPicture = new MyClusterManagerRendererWithoutImage(
                        getActivity(),
                        map,
                        mClusterManagerWithoutPicture
                );
                mClusterManagerWithoutPicture.setRenderer(myClusterManagerRendererWithoutPicture);
            }

            boolean isMarkerForCurrentUser = false;

            ArrayList<User> users = new ArrayList<>();

            if(userKey != null) {
                //this if is for adding cluster for current user or when changing location for other users
                users = new ArrayList<>();
                users.add(UsersData.getInstance().getUser(userKey));

                if(userKey.equals(currentUser.key)){
                    isMarkerForCurrentUser = true;
                }
            }
            else {
                //add all users without current user
                users = UsersData.getInstance().getUsers();
                users.remove(currentUser);
                Log.i("MAPS", "Fetched " + users.size() + " for adding cluster");
            }

            for(User user: users){

//                Log.d("CLUSTER_MARKER", "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                try{
                    String snippet = "";
                    if(user.getUser_id().equals(currentUser.getUser_id())) {
                        snippet = "This is you";
                    }
                    else{
                        snippet = "Determine route to " + user.username + "?";
                    }

                    Bitmap avatar = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.avatar);; // set the default avatar
                    try{
                        if(user.profilePicture != null) {
                            avatar = user.profilePicture;
                        }
                    }catch (NumberFormatException e){
                        Log.d("MAPS", "addMapMarkers: no avatar for " + user.username + ", setting default.");
                    }
                    if(user.location != null) {
                        UserClusterMarker newClusterMarker = new UserClusterMarker(
                                user.location.getLocation(),
                                user.username,
                                snippet,
                                avatar,
                                user
                        );

                        if(isMarkerForCurrentUser || currentUser.friends != null && currentUser.friends.containsKey(user.key)) {
                            //it's a friend or current user
                            mClusterManagerWithPicture.addItem(newClusterMarker);
                        }
                        else {
                            mClusterManagerWithoutPicture.addItem(newClusterMarker);
                        }


                        mClusterMarkers.add(newClusterMarker);
                    }

                }catch (NullPointerException e){
                    Log.e("MAPS", "addMapMarkers: NullPointerException: " + e.getMessage() );
                }

            }

            //update view
            mClusterManagerWithPicture.cluster();
            mClusterManagerWithoutPicture.cluster();

            //update list
            clusterSpinnerAdapter.notifyDataSetChanged();

            //setCameraView();

//            cameraZoomToLocation(currentUser.location.getLocation());
        }
    }

    private void removeMarkersWithPicture(@Nullable String userKey) {
        if(map != null) {

            ArrayList<User> users = new ArrayList<>();
            if(userKey != null && userKey.equals(currentUser.key)) {
                //prevent to delete current user marker by entering here and leaving users list empty
            }
            else if(userKey != null) {
                //delete only one marker (used for location update)
                users.add(UsersData.getInstance().getUser(userKey));
            }
            else {
                users = UsersData.getInstance().getUsers();
                users.remove(currentUser);
            }


            Log.i("MAPS", "Delete " + users.size() + " markers.");

            for (User user: users) {
                ClusterMarker cluster = getClusterMarker(user.key);
                if(cluster != null) {
                    if(currentUser.friends != null && currentUser.friends.containsKey(user.key)) {
                        //it's a friend or current user
                        mClusterManagerWithPicture.removeItem(cluster);
                    }
                    else {
                        mClusterManagerWithoutPicture.removeItem(cluster);
                    }

                    mClusterMarkers.remove(cluster);
                }
            }

            //update view
            mClusterManagerWithPicture.cluster();
            mClusterManagerWithoutPicture.cluster();

            clusterSpinnerAdapter.notifyDataSetChanged();

            //setCameraView();
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
                Log.i("MOVE MARKER", "selected user: " + marker.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void changeUserLocationOnMap(String userKey) {
        ClusterMarker cluster = getClusterMarker(userKey);
        if(cluster != null) {
            Log.i("MAPS", "Friend " + UsersData.getInstance().getUser(userKey) + " deleting location.");
            removeMarkersWithPicture(userKey);
            addMapMarkersWithPicture(userKey);
        }

    }

    private ClusterMarker getClusterMarker(String userKey) {
        ClusterMarker cluster = null;
        for (ClusterMarker marker: mClusterMarkers) {
            if(((UserClusterMarker) marker).user.key.equals(userKey)){
                cluster = marker;
                break;
            }
        }
        return cluster;
    }
}