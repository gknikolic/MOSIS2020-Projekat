package rs.elfak.findpet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import rs.elfak.findpet.Repositories.UsersData;
import rs.elfak.findpet.data_models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
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
    public static boolean isTaken = false;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference();
        initializeUI();
        sharedPreferences = getSharedPreferences("login", MODE_PRIVATE);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
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

    private void registerNewUser() {
        String email, password, username, phoneNumber;
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        username = usernameEditText.getText().toString();
        phoneNumber = phoneNumEditText.getText().toString();
        progressBar.setVisibility(View.VISIBLE);
        if(checkFieldsValues()){
            checkIfUsernameIsTaken(username);
            if (RegisterActivity.isTaken) {
                Toast.makeText(getApplicationContext(), "Registration failed! Username is already taken", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                                User newUser = new User(username, email, phoneNumber);
                                newUser.key  = mAuth.getCurrentUser().getUid();
                                UsersData.getInstance().addNewUser(newUser);
                                Log.i(TAG, mAuth.getCurrentUser().getUid());
                                sharedPreferences.edit().putBoolean("isLogged", true).apply();
                                Intent intent = new Intent(rs.elfak.findpet.RegisterActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthWeakPasswordException e) {
                                    Toast.makeText(getApplicationContext(), "Registration failed! Weak Password.", Toast.LENGTH_LONG).show();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    Toast.makeText(getApplicationContext(), "Registration failed! Email is not valid.", Toast.LENGTH_LONG).show();
                                } catch(FirebaseAuthUserCollisionException e) {
                                    Toast.makeText(getApplicationContext(), "Registration failed! Email is already taken.", Toast.LENGTH_LONG).show();
                                } catch(Exception e) {
                                    Log.e(TAG, e.getMessage());
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        }
            });
        }
    }

    private void checkIfUsernameIsTaken(String username) {
        RegisterActivity.isTaken = false;
        Query query = dbReference.child("users")
                .orderByChild("username")
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds: snapshot.getChildren()){
                    if(username.equals(ds.getValue(User.class).username)) {
                        RegisterActivity.isTaken = true;
                        Log.i(TAG, "taken true");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.i(TAG, Boolean.toString(RegisterActivity.isTaken));
    }

    private boolean checkFieldsValues() {
        String email, password, repeatPassword, username, phoneNumber;
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        repeatPassword = repeatPassEditText.getText().toString();
        username = usernameEditText.getText().toString();
        phoneNumber = phoneNumEditText.getText().toString();
        if (TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword)) {
            Toast.makeText(getApplicationContext(), "Please enter password and repeat it.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(getApplicationContext(), "Please enter Username.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }
        if(TextUtils.isEmpty(phoneNumber)){
            Toast.makeText(getApplicationContext(), "Please enter Phone Number.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }
        if(!password.equals(repeatPassword)){
            Toast.makeText(getApplicationContext(), "Password and repeated password don't match. Try Again.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }
        return true;
    }

}
