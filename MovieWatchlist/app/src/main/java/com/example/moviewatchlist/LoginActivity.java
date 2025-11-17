package com.example.moviewatchlist;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;

/*
 LoginActivity handles user authentication.
 It allows:
 - Logging in with an existing email/password
 - Creating a new account
 - Redirecting an already authenticated user directly to MainActivity
*/
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        /*
         If a user is already logged in, skip login screen
         and go directly to the app's home page (MainActivity).
         This prevents making the user log in again every time.
        */
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // Get references to input fields and buttons
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Button triggers for login and registration
        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());
    }

    /*
     Attempts to authenticate the user using Firebase Authentication.
     Validates input before trying to log in.
    */
    private void loginUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         Firebase Authentication method for login.
         If successful -> redirect to MainActivity.
         If failed -> display the error message.
        */
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /*
     Creates a new user account using Firebase Authentication.
     Also validates input.
    */
    private void registerUser() {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        // Input validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
         Firebase method for registering a new account.
         If successful -> automatically logs in and opens MainActivity.
         If failed -> show the error returned by Firebase.
        */
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this,
                                "Error: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
