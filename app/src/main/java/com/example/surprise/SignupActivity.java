package com.example.surprise;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailEditText, nameEditText, contactEditText, passwordEditText, confirmPasswordEditText;
    private TextView errorMessageTextView;
    private static final String TAG = "SignupActivity";

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
        Button backToLoginButton = findViewById(R.id.backToLoginButton);

        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

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
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                Log.d(TAG, "Email verification sent");
                                                saveUserToFirestore(user.getUid(), name, contact, email);
                                            } else {
                                                Log.e(TAG, "Failed to send verification email", emailTask.getException());
                                                showError("Failed to send verification email: " +
                                                        (emailTask.getException() != null ? emailTask.getException().getMessage() : "Unknown error"));
                                            }
                                        });
                            }
                        } else {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            showError("Signup failed: " +
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    });
        });
    }

    private void saveUserToFirestore(String userId, String name, String contact, String email) {
        // Use HashMap instead of User class
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("contact", contact);
        user.put("email", email);
        user.put("enrollment", ""); // Empty string instead of null
        user.put("department", ""); // Empty string instead of null

        // First ensure the user is created successfully before attempting to write
        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved to Firestore successfully for user: " + userId);
                    Toast.makeText(SignupActivity.this, "Signup successful. Verification email sent.", Toast.LENGTH_LONG).show();

                    Intent loginIntent = new Intent(SignupActivity.this, LoginActivity.class);
                    loginIntent.putExtra("message", "Please verify your email before logging in.");
                    startActivity(loginIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data to Firestore", e);
                    showError("Failed to save user data: " + e.getMessage());
                });
    }

    private void showError(String message) {
        errorMessageTextView.setText(message);
        errorMessageTextView.setVisibility(View.VISIBLE);
    }

    // If you need User class for other operations
    public static class User {
        private String name;
        private String contact;
        private String email;
        private String enrollment;
        private String department;

        // Required empty constructor for Firestore
        public User() {
        }

        public User(String name, String contact, String email) {
            this.name = name;
            this.contact = contact;
            this.email = email;
            this.enrollment = "";
            this.department = "";
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

        public String getEnrollment() {
            return enrollment;
        }

        public void setEnrollment(String enrollment) {
            this.enrollment = enrollment;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }
    }
}