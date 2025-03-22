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
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText, nameEditText, contactEditText, passwordEditText, confirmPasswordEditText;
    private TextView errorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.signupActivityEmail);
        nameEditText = findViewById(R.id.signupActivityName);
        contactEditText = findViewById(R.id.signupActivityContact);
        passwordEditText = findViewById(R.id.signupActivityPassword);
        confirmPasswordEditText = findViewById(R.id.signupActivityconfirmPassword);
        errorMessageTextView = findViewById(R.id.signupActivityErrorMessage);
        Button submitButton = findViewById(R.id.submitActivityButton);

        submitButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String name = nameEditText.getText().toString().trim();
            String contact = contactEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            // Hide error message when retrying signup
            errorMessageTextView.setVisibility(View.GONE);

            if (email.isEmpty() || name.isEmpty() || contact.isEmpty() || password.isEmpty()) {
                showError("All fields are required.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match.");
                return;
            }

            if (password.length() < 6) {
                showError("Password must be at least 6 characters long.");
                return;
            }

            // Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                saveUserToFirestore(user.getUid(), name, contact, email);
                                            } else {
                                                showError("Failed to send verification email.");
                                            }
                                        });
                            }
                        } else {
                            showError("Signup failed: " + task.getException().getMessage());
                        }
                    });
        });
    }

    private void saveUserToFirestore(String userId, String name, String contact, String email) {
        User newUser = new User(name, contact, email);
        db.collection("users").document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, "Signup successful. Verify your email.", Toast.LENGTH_LONG).show();
                    Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
                    loginIntent.putExtra("message", "Please verify your email before logging in.");
                    startActivity(loginIntent);
                    finish();
                })
                .addOnFailureListener(e -> showError("Failed to save user: " + e.getMessage()));
    }

    private void showError(String message) {
        errorMessageTextView.setText(message);
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    public class User {
        private String name;
        private String contact;
        private String email;

        public User(String name, String contact, String email) {
            this.name = name;
            this.contact = contact;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContact() {
            return contact;
        }

        public void setContact(String contact) {
            this.contact = contact;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}