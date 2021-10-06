package rs.elfak.findpet.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.content.AsyncTaskLoader;

import android.Manifest;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import rs.elfak.findpet.Enums.FragmentName;
import rs.elfak.findpet.Fragments.DashboardFragment;
import rs.elfak.findpet.Fragments.MapsFragment;
import rs.elfak.findpet.Fragments.MessagesFragment;
import rs.elfak.findpet.Fragments.UserFragment;
import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.R;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.RepositoryEventListeners.UsersListEventListener;
import rs.elfak.findpet.Services.UserLocationService;
import rs.elfak.findpet.data_models.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, UsersListEventListener {

    private static final int PERMISSION_ACCESS_LOCATION = 100;
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawer;
    private User currentUser;
    private ArrayList<User> users = new ArrayList<>();
    private TextView username;
    private TextView locationServiceStatus;
    private ImageView profileImage;
    private ProgressDialog progressDialog;
    NavigationView navigationView;
    FragmentName startupFragment = FragmentName.Dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("isLogged", false)){
            Intent i = new Intent(MainActivity.this, GetStartedActivity.class);
            startActivity(i);
            finish();
        }

        Intent i = getIntent();
        FragmentName posibleStartupFragment = (FragmentName) i.getSerializableExtra(Constants.FRAGMENT_ENUM_KEY);
        if(posibleStartupFragment != null) {
            this.startupFragment = posibleStartupFragment;
        }

        InitSideBar(savedInstanceState);
        UsersData.getInstance().setCurrentUserUID(FirebaseAuth.getInstance().getCurrentUser().getUid());
        UsersData.getInstance().setUpdateListener(this);
        //block UI
        LoadCurrentUser waitForCurrentUser = new LoadCurrentUser();
        waitForCurrentUser.execute();

        //select item in menu
        MenuItem selectedMenuItem = navigationView.getMenu().getItem(startupFragment.getValue()).setChecked(true);
        onNavigationItemSelected(selectedMenuItem);

        this.startLocationService();
        this.registerLogOutBroadcastReceiver();
    }

    public void InitSideBar(Bundle savedInstanceState) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View header = navigationView.getHeaderView(0); //inflate header view
        this.username = header.findViewById(R.id.nav_header_username);
        this.locationServiceStatus = header.findViewById(R.id.nav_header_location_service_status);
        this.profileImage = header.findViewById(R.id.nav_header_user_image);

        if(savedInstanceState == null) { //this will prevent reloading fragment when rotating device
            //first fragment when we open app
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    @Override
    public void OnUsersListUpdated() {
        this.currentUser = UsersData.getInstance().getCurrentLoggedUser();
        this.users = UsersData.getInstance().getUsers();

        while(currentUser == null) {

        }

        if(currentUser != null) {
            Log.i("USERS", "Callback for loading user data in main activity:  "  + currentUser.username);
            //header of sidebar
            this.username.setText(currentUser.username);
            this.locationServiceStatus.setText("Location service status: " + (currentUser.locationEnabled ? "enabled" : "disabled"));
            this.profileImage.setImageBitmap(currentUser.profilePicture);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_dashboard:
                openDashboardFragment();
                break;
            case R.id.nav_message:
                openMessagesFragment();
                break;
            case R.id.nav_map:
                openMapFragment();
                break;
            case R.id.nav_user:
                if(currentUser != null) {
                    openUserFragment();
                }else {
                    Toast.makeText(this, "User not fetched yet. Please wait", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.nav_log_out:
                LogOut();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void openDashboardFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
    }

    void openMessagesFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MessagesFragment()).commit();
    }

    void openMapFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.USER_KEY, currentUser);
        bundle.putSerializable(Constants.FREINDS_KEY, users);
        MapsFragment mapsFragment = new MapsFragment();
        mapsFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapsFragment).commit();
    }

    void openUserFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.USER_KEY, currentUser);
        UserFragment userFragment = new UserFragment();
        userFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, userFragment).commit();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    void startLocationService() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_LOCATION
            );
        } else {
            startService();
        }
    }

    public void startService(){
        Intent service = new Intent(getApplicationContext(), UserLocationService.class);
        service.putExtra("useGps", true);
        ContextCompat.startForegroundService(this, service);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_ACCESS_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                startLocationService();
            }
            else{
                Toast.makeText(
                        this,
                        "To use location you must grant location permissions",
                        Toast.LENGTH_LONG
                );
            }
        }
    }

    private void registerLogOutBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ACTION_LOGOUT");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("MainActvity", "LogOut called");
                finish();
            }
        }, intentFilter);
    }

    private void LogOut() {
        FirebaseAuth.getInstance().signOut();
        currentUser = null;
        Intent service = new Intent(getApplicationContext(), UserLocationService.class);
        stopService(service);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ACTION_LOGOUT");
        sendBroadcast(broadcastIntent);
        sharedPreferences.edit().putBoolean("isLogged", false).apply();
        Intent i = new Intent(MainActivity.this, GetStartedActivity.class);
        startActivity(i);
    }

    //AsyncTask<params, progress, result>
    class LoadCurrentUser extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //users are populated through UserListEventListener

            progressDialog.dismiss();
        }

        @Override
        protected Boolean doInBackground(Void... usersData) {
            while(currentUser == null) //current user will be set through UserListEvenListener implementation
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i("MY TASK", "Waiting to load Current user");
            }
            Log.i("MY TASK DONE", "Current user loaded");

            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // setup a progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//            progressDialog.setProgress(values[0]);
        }
    }
}
