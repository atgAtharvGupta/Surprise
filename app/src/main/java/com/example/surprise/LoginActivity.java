package com.example.surprise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        EditText emailEditText = findViewById(R.id.loginActivityEmail);
        EditText passwordEditText = findViewById(R.id.loginActivityPassword);
        Button loginButton = findViewById(R.id.loginActivityButton);
        TextView forgotPassword = findViewById(R.id.loginActivityForgotPassword);
        TextView newUser = findViewById(R.id.loginActivityNewUser);
        TextView errorMessage = findViewById(R.id.loginActivityErrorMessage);

        // Check for the message from SignupActivity
        Intent intent = getIntent();
        if (intent.hasExtra("message")) {
            String message = intent.getStringExtra("message");
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage.setText("Email and Password cannot be empty");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    Intent dashboard = new Intent(LoginActivity.this, Dashboard.class);
                                    startActivity(dashboard);
                                    finish();
                                } else {
                                    errorMessage.setText("Please verify your email address.");
                                }
                            } else {
                                errorMessage.setText("Authentication failed: " + task.getException().getMessage());
                            }
                        });
            }
        });
    }
}