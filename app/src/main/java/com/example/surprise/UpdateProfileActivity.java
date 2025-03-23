package com.example.surprise;

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

public class UpdateProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText userNameEditText, contactEditText, enrollmentEditText, departmentEditText;
    private TextView errorMessageTextView;
    private static final String TAG = "UpdateProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userNameEditText = findViewById(R.id.updateProfileUserName);
        contactEditText = findViewById(R.id.updateProfileContact);
        enrollmentEditText = findViewById(R.id.updateProfileEnrollmentNumber);
        departmentEditText = findViewById(R.id.updateProfileDepartment);
        errorMessageTextView = findViewById(R.id.updateProfileError);
        Button updateButton = findViewById(R.id.updateProfileButton);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            String contact = documentSnapshot.getString("contact");
                            String enrollment = documentSnapshot.getString("enrollment");
                            String department = documentSnapshot.getString("department");

                            userNameEditText.setText(userName);
                            contactEditText.setText(contact);
                            enrollmentEditText.setText(enrollment);
                            departmentEditText.setText(department);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error fetching user data", e);
                        errorMessageTextView.setText("Error loading data: " + e.getMessage());
                        errorMessageTextView.setVisibility(View.VISIBLE);
                    });
        }

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameEditText.getText().toString().trim();
                String contact = contactEditText.getText().toString().trim();
                String enrollment = enrollmentEditText.getText().toString().trim();
                String department = departmentEditText.getText().toString().trim();

                if (userName.isEmpty()) {
                    errorMessageTextView.setText("Name cannot be empty");
                    errorMessageTextView.setVisibility(View.VISIBLE);
                    return;
                }

                if (user != null) {
                    String userId = user.getUid();

                    // Create a map with all fields to update
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", userName);
                    updates.put("contact", contact);
                    updates.put("enrollment", enrollment);
                    updates.put("department", department);

                    db.collection("users").document(userId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Profile updated successfully");
                                Toast.makeText(UpdateProfileActivity.this,
                                        "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error updating profile", e);
                                errorMessageTextView.setText("Error: " + e.getMessage());
                                errorMessageTextView.setVisibility(View.VISIBLE);
                            });
                }
            }
        });
    }
}