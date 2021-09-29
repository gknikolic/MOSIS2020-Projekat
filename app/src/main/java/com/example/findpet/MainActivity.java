package com.example.findpet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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