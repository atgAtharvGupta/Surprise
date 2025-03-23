package com.example.surprise;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dashboard extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView dashboardUserName;
    private TextView enrollmentNumberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dashboardUserName = findViewById(R.id.dashboardUserName);
        enrollmentNumberTextView = findViewById(R.id.dashboardUserEnrollmentNumber);

        Button createTest = findViewById(R.id.dashboardCreateTest);
        createTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createTestActivity = new Intent(getApplicationContext(), CreateTestActivity.class);
                startActivity(createTestActivity);
            }
        });

        findViewById(R.id.dashboardTestActiveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.dashboardTestActivePOPUP).setVisibility(View.GONE);
            }
        });

        Button joinTest = findViewById(R.id.dashboardJoinTest);
        joinTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent joinTestActivity = new Intent(getApplicationContext(), JoinTestActivity.class);
                startActivity(joinTestActivity);
            }
        });

        FrameLayout editProfile = findViewById(R.id.dashboardEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileActivity = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                startActivity(editProfileActivity);
            }
        });

        Button historyButton = findViewById(R.id.dashboardHistoryButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent historyActivity = new Intent(getApplicationContext(), TestHistoryActivity.class);
                startActivity(historyActivity);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data every time the dashboard becomes visible
        fetchUserData();
    }

    private void fetchUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Show loading state
            dashboardUserName.setText("Loading...");

            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null && !name.isEmpty()) {
                                dashboardUserName.setText(name);
                            } else {
                                dashboardUserName.setText("User");
                            }

                            String enrollment = documentSnapshot.getString("enrollment");
                            if (enrollment != null && !enrollment.isEmpty()) {
                                enrollmentNumberTextView.setText(enrollment);
                            } else {
                                enrollmentNumberTextView.setText("");
                            }

                            Log.d("Dashboard", "User data refreshed: " + name);
                        } else {
                            dashboardUserName.setText("User");
                            enrollmentNumberTextView.setText("");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Dashboard", "Error getting data", e);
                        dashboardUserName.setText("Error");
                    });
        }
    }
}