package rs.elfak.findpet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import rs.elfak.findpet.Helpers.Constants;
import rs.elfak.findpet.Helpers.Helpers;
import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.RepositoryEventListeners.UsersListEventListener;
import rs.elfak.findpet.Services.UserLocationService;
import rs.elfak.findpet.data_models.Post;
import rs.elfak.findpet.data_models.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, UsersListEventListener {

    private static final int PERMISSION_ACCESS_LOCATION = 100;
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawer;
    private User currentUser;
    private TextView username;
    private TextView locationServiceStatus;
    private ImageView profileImage;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitSideBar(savedInstanceState);
        UsersData.getInstance().setUpdateListener(this);

        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("isLogged", false)){
            Intent i = new Intent(MainActivity.this, GetStartedActivity.class);
            startActivity(i);
            finish();
        }

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
        this.currentUser = UsersData.getInstance().getCurrentLogedUser();
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
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DashboardFragment()).commit();
                break;
            case R.id.nav_message:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MessagesFragment()).commit();
                break;
            case R.id.nav_map:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapFragment()).commit();
                break;
            case R.id.nav_user:
                Bundle bundle = new Bundle();
                bundle.putSerializable(Constants.USER_KEY, currentUser);
                UserFragment fragment = new UserFragment();
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                break;
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
        service.putExtra("useGps", false);
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
                finish();
            }
        }, intentFilter);
    }


    private void LogOut() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ACTION_LOGOUT");
        sendBroadcast(broadcastIntent);
        sharedPreferences.edit().putBoolean("isLogged", false).apply();
        Intent i = new Intent(MainActivity.this, GetStartedActivity.class);
        startActivity(i);
    }

}