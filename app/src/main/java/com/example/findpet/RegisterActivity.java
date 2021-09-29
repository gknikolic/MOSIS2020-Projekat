package com.example.findpet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEditText,
            passwordEditText,
            repeatPassEditText,
            usernameEditText,
            phoneNumEditText;

    private Button regBtn;
    private ProgressBar progressBar;
    private static final String FIREBASE_CHILD = "users";
    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();
        initializeUI();

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });
    }

    private void registerNewUser() {
        progressBar.setVisibility(View.VISIBLE);

        String email, password, repeatPassword, username, phoneNumber;
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        repeatPassword = repeatPassEditText.getText().toString();
        username = usernameEditText.getText().toString();
        phoneNumber = phoneNumEditText.getText().toString();

        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword)) {
            Toast.makeText(getApplicationContext(), "Please enter password and repeat it.", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email.", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(getApplicationContext(), "Please enter Username and Phone Number.", Toast.LENGTH_LONG).show();
            return;
        }
        if(!password.equals(repeatPassword)){
            Toast.makeText(getApplicationContext(), "Password and repeated password don't match. Try Again.", Toast.LENGTH_LONG).show();
            return;
        }
//        TODO: CHECK IF USERNAME ALREADY EXISTS!!!

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);

                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Registration failed! Please try again later", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
        });
    }

    private void initializeUI() {
        emailEditText = findViewById(R.id.email_reg);
        passwordEditText = findViewById(R.id.password_reg);
        repeatPassEditText = findViewById(R.id.repeat_pass_reg);
        usernameEditText = findViewById(R.id.username_reg);
        phoneNumEditText = findViewById(R.id.phone_num_reg);
        regBtn = findViewById(R.id.register_btn);
        progressBar = findViewById(R.id.progressBar);
    }

    private boolean isValidEmail(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
