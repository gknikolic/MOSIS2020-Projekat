package rs.elfak.findpet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

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

        Button logOutButton = (Button)findViewById(R.id.logout_button);
        this.setLogoutButtonClickListener(logOutButton);

        this.registerLogOutBroadcastReceiver();
//        HashMap<String, String> user = new HashMap<>();
//        user.put("ime", "Mika");
//        user.put("prezime", "Peric");
//        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users");
//        dbRef.push().setValue(user);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.setValue("Hello, World!");
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

    private void setLogoutButtonClickListener(Button logOutBtn){
        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction("ACTION_LOGOUT");
                sendBroadcast(broadcastIntent);
                sharedPreferences.edit().putBoolean("isLogged", false).apply();
                Intent i = new Intent(MainActivity.this, GetStartedActivity.class);
                startActivity(i);
            }
        });
    }
}