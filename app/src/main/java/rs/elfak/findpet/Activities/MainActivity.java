package rs.elfak.findpet.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

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

    private SharedPreferences sharedPreferences;
    private DrawerLayout drawer;
    private User currentUser;
    private ArrayList<User> users = new ArrayList<>();
    private TextView username;
    private TextView locationServiceStatus;
    private ImageView profileImage;
    private FragmentName startupFragment = FragmentName.Dashboard;
    NavigationView navigationView;
    ProgressDialog progressDialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //TODO Check how this affect location service
        int id= android.os.Process.myPid();
        android.os.Process.killProcess(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UsersData.getInstance().addUpdateListener(this);

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("isLogged", false)){
            Intent i = new Intent(MainActivity.this, GetStartedActivity.class);
            startActivity(i);
            finish();
        }

        InitSideBar(savedInstanceState);
        UsersData.getInstance().setCurrentUserUID(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //show progressbar dialog
        LoadCurrentUser waitToLoadUser = new LoadCurrentUser();
        waitToLoadUser.execute();

        Intent i = getIntent();
        FragmentName possibleStartupFragment = (FragmentName) i.getSerializableExtra(Constants.FRAGMENT_ENUM_KEY);
        if(possibleStartupFragment != null) {
            this.startupFragment = possibleStartupFragment;
        }

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

        if(currentUser != null) {
            Log.i("USERS", "Callback for loading user data in main activity:  "  + currentUser.username);
            //header of sidebar
            this.username.setText(currentUser.username);
            this.locationServiceStatus.setText("Location service status: " + (currentUser.locationEnabled ? "enabled" : "disabled"));
            this.profileImage.setImageBitmap(currentUser.profilePicture);
        }
    }

    @Override
    public void CurrentUserLoaded() {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.nav_dashboard:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                break;
            case R.id.nav_message:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MessagesFragment()).commit();
                break;
            case R.id.nav_map:
                bundle.putSerializable(Constants.USER_KEY, currentUser);
                bundle.putSerializable(Constants.FREINDS_KEY, users);
                MapsFragment mapsFragment = new MapsFragment();
                mapsFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mapsFragment).commit();
                break;
            case R.id.nav_user:
                if(currentUser != null) {
                    bundle.putSerializable(Constants.USER_KEY, currentUser);
                    UserFragment userFragment = new UserFragment();
                    userFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, userFragment).commit();
                    break;
                }else {
                    Toast.makeText(this, "User not fetched yet. Please wait", Toast.LENGTH_LONG).show();
                    break;
                }
            case R.id.nav_log_out:
                LogOut();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    class LoadCurrentUser extends AsyncTask<Void, Integer, Boolean> implements UsersListEventListener{

        protected boolean isLoaded = false;

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            UsersData.getInstance().removeUpdateListener(this);

            //switch to fragment on starting app
            navigationView.getMenu().getItem(startupFragment.getValue()).setChecked(true);
            onNavigationItemSelected(navigationView.getMenu().getItem(startupFragment.getValue()));

            progressDialog.dismiss();
            Log.i("BACKGROUND_TASK", "Finished.");
        }

        @Override
        protected Boolean doInBackground(Void... usersData) {
            //bad solution because of pulling
//            while(UsersData.getInstance().getCurrentLoggedUser() == null)
//            {
//                try {
//                    Thread.currentThread().sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Log.i("BACKGROUND_TASK", "Waiting for current user to be loaded");
//            }

            while (isLoaded == false) {} //keep thread alive until callback for loaded current user is not called
            Log.i("BACKGROUND_TASK", "Current user is loaded.");
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("BACKGROUND_TASK", "Started.");

            UsersData.getInstance().addUpdateListener(this);

            // setup a progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setTitle("please wait..");

            progressDialog.show();

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
    }

}