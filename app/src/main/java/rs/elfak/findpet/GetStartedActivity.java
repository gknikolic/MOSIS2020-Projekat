package rs.elfak.findpet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GetStartedActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);


        Button loginButton = (Button)findViewById(R.id.loginActivityBtn);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(rs.elfak.findpet.GetStartedActivity.this, LoginActivity.class);
                startActivity(i);
//                finish();
            }
        });

        Button registerButton = (Button)findViewById(R.id.registerActivityBtn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(rs.elfak.findpet.GetStartedActivity.this, RegisterActivity.class);
                startActivity(i);
//                finish();
            }
        });
    }
}
