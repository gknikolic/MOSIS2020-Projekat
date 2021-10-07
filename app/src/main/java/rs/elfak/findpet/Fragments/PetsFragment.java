package rs.elfak.findpet.Fragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

import rs.elfak.findpet.Adapters.SpinnerWithPlaceholderAdapter;
import rs.elfak.findpet.Adapters.ClusterSpinnerAdapter;
import rs.elfak.findpet.Enums.CaseType;
import rs.elfak.findpet.Enums.PetType;
import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.RepositoryEventListeners.UsersListEventListener;
import rs.elfak.findpet.Utilities.MyClusterManagerRenderer;
import rs.elfak.findpet.data_models.ClusterMarker;
import rs.elfak.findpet.data_models.PetClusterMarker;
import rs.elfak.findpet.data_models.PetFilterModel;
import rs.elfak.findpet.data_models.Post;
import rs.elfak.findpet.data_models.User;

public class PetsFragment extends Fragment {

    //params from main activity
    private User currentUser;
    private ArrayList<User> users;
    private ArrayList<Post> posts;
    private PetFilterModel filterModel;

    //for maps
    private LatLngBounds mMapBoundary;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ClusterSpinnerAdapter clusterSpinnerAdapter;
    private List<ClusterMarker> mClusterMarkers = new ArrayList<>(); //markers on map
    private boolean isMapReady = false; //use for background post

    //widgets
    private ProgressDialog progressDialogForMaps;
    private ProgressDialog progressDialogForFiltering;
    private GoogleMap map;
    private Spinner caseTypeSpinner;
    private Spinner petTypeSpinner;
    private EditText tbxName;
    private Button btnApplyFilters;
    private Button btnClearFilters;

    public PetsFragment(PetFilterModel filterModel) {
        this.filterModel = filterModel;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //from main activity
        currentUser = (User) getArguments().getSerializable(Constants.USER_KEY);
        users = (ArrayList<User>) getArguments().getSerializable(Constants.FREINDS_KEY);
        final View rootView = inflater.inflate(R.layout.fragment_pets, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.friendsFragment_map);
        if (mapFragment != null) {
            // setup a progress dialog
            progressDialogForMaps = new ProgressDialog(getContext());
            progressDialogForMaps.setCancelable(false);
            progressDialogForMaps.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialogForMaps.setTitle(R.string.progressDialogTitle);

            progressDialogForMaps.show();
            mapFragment.getMapAsync(callback);
        }

        tbxName = (EditText) getView().findViewById((R.id.petsFragment_name));

        //TODO Consider to enable first option
        caseTypeSpinner = getView().findViewById(R.id.petsFragment_caseTypeSpinner);
        String[] caseTypesWithPlaceHolder = new String[CaseType.values().length + 1];
        caseTypesWithPlaceHolder[0] = getView().getResources().getString(R.string.spinnerPlaceholder);
        for (int i = 1; i < caseTypesWithPlaceHolder.length; i++) {
            caseTypesWithPlaceHolder[i] = CaseType.values()[i - 1].toString();
        }
        caseTypeSpinner.setAdapter(new SpinnerWithPlaceholderAdapter(getContext(), R.layout.spinner_item, caseTypesWithPlaceHolder));
        caseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = parent.getItemAtPosition(position).toString();
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
                    Toast.makeText
                            (getContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                    filterModel.caseType = CaseType.valueOf(selectedItemText);
                } else {
                    filterModel.caseType = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //TODO Consider to enable first option
        petTypeSpinner = (Spinner) getView().findViewById(R.id.petsFragment_petTypeSpinner);
        String[] petTypesWithPlaceHolder = new String[PetType.values().length + 1];
        petTypesWithPlaceHolder[0] = getView().getResources().getString(R.string.spinnerPlaceholder);
        for (int i = 1; i < petTypesWithPlaceHolder.length; i++) {
            petTypesWithPlaceHolder[i] = PetType.values()[i - 1].toString();
        }
        petTypeSpinner.setAdapter(new SpinnerWithPlaceholderAdapter(getContext(), R.layout.spinner_item, petTypesWithPlaceHolder));
        petTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = parent.getItemAtPosition(position).toString();
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
                    Toast.makeText
                            (getContext(), "Selected : " + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                    filterModel.petType = PetType.valueOf(selectedItemText);
                } else {
                    filterModel.petType = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnApplyFilters = (Button) getView().findViewById(R.id.petsFragment_btnApplyFilters);
        btnApplyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        btnClearFilters = (Button) getView().findViewById(R.id.petsFragment_btnClearFilters);
        btnClearFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                caseTypeSpinner.setSelection(0);
                caseTypeSpinner.setSelection(0);
                tbxName.setText("");
                Toast.makeText(getContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
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

            //adding basic marker
//            LatLng myLocation = currentUser.location.getLocation();
//            map.addMarker(new MarkerOptions().position(myLocation).title("My Location"));
//            map.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
//            cameraZoomToLocation(myLocation);

            //TODO create marker with picture for pets or use basic markers?
//            addMapMarkers();
//            initMarkersSpinner();
            progressDialogForMaps.dismiss();

            Log.i("MAPS", "On map create method");
        }
    };

    private void cameraZoomToLocation(LatLng location) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    private void addMapMarkers() {

        if (map != null) {

            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), map);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getActivity(),
                        map,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            //TODO Add filter for friends only
            for (Post post : posts) {

//                Log.d("CLUSTER_MARKER", "addMapMarkers: location: " + userLocation.getGeo_point().toString());
                try {
                    String snippet = "Owner: " + UsersData.getInstance().getUser(post.userKey).username;

                    Bitmap avatar = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.avatar);
                    ; // set the default avatar
                    try {
                        if (post.image != null) {
                            avatar = post.image;
                        }
                    } catch (NumberFormatException e) {
                        Log.d("MAPS", "addMapMarkers: no avatar for postKey:" + post.key + " for pet " + post.pet.name + ", setting default.");
                    }
                    if (post.location != null) {
                        ClusterMarker newClusterMarker = new PetClusterMarker(
                                post.location.getLocation(),
                                post.toString(),
                                snippet,
                                avatar,
                                post
                        );
                        mClusterManager.addItem(newClusterMarker);
                        mClusterMarkers.add(newClusterMarker);
                    }

                } catch (NullPointerException e) {
                    Log.e("MAPS", "addMapMarkers: NullPointerException: " + e.getMessage());
                }

            }
            mClusterManager.cluster();

            setCameraView();

//            cameraZoomToLocation(currentUser.location.getLocation());
        }

    }

    private void clearAllMarkersFromMap() {
        //TODO Implement
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

    //AsyncTask<params, progress, result>
    class ApplyFilter extends AsyncTask<Void, Integer, Boolean> implements UsersListEventListener {

        PetFilterModel myFilterModel;

        protected boolean isLoaded = false;

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progressDialogForFiltering.dismiss();
            Log.i("BACKGROUND_TASK", "Finished.");
        }

        @Override
        protected Boolean doInBackground(Void... usersData) {
            while (isMapReady == false) {} //keep thread alive until callback for getting map ready finished
            Log.i("BACKGROUND_TASK", "Callback for getting map ready finished");


            Log.i("BACKGROUND_TASK", "Filters are applied");
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("BACKGROUND_TASK", "Started.");

            // setup a progress dialog
            progressDialogForFiltering = new ProgressDialog(getContext());
            progressDialogForFiltering.setCancelable(false);
            progressDialogForFiltering.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialogForFiltering.setTitle(R.string.filtering);

            progressDialogForFiltering.show();

            myFilterModel = filterModel;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//            progressDialog.setProgress(values[0]);
        }

        @Override
        public void OnUsersListUpdated() {
//            Log.i("BACKGROUND_TASK", "Callback");
        }

        @Override
        public void CurrentUserLoaded() {
            isLoaded = true;
        }

        @Override
        public void OnUserLocationChanged(String userKey) {

        }
    }

}