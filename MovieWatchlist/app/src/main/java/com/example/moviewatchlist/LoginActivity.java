package com.example.moviewatchlist;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import com.google.firebase.auth.FirebaseAuth;

/*
 LoginActivity is used 4 auth.
 It allows:
 - Logging in with an existing account
 - Creating a new account
 - if logged in go to mainactivity
*/
public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase auth
        mAuth = FirebaseAuth.getInstance();

        /*
         If logged in go to main page
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
     tries to authenticate.
     validates input.
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
         Firebase auth method 4 login. (if suceful go to main page)

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
     Creates new user and validate input
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
         creating account if suceful go 2 main page
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
