package com.example.surprise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dashboard extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView dashboardUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dashboardUserName = findViewById(R.id.dashboardUserName);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            dashboardUserName.setText(name);
                        } else {
                            dashboardUserName.setText("User");
                        }
                    })
                    .addOnFailureListener(e -> {
                        dashboardUserName.setText("Error");
                    });
        }

        Button createTest = findViewById(R.id.dashboardCreateTest);
        createTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                //CHECK if this user has already created a test or not
//                if(testAlreadyCreated){
//                    CardView testAlreadyActivePOPUP = findViewById(R.id.dashboardTestActivePOPUP);
//                    testAlreadyActivePOPUP.setVisibility(View.VISIBLE);
//                    //TODO
//                    //get id and password of the active test session from the server
//                    TextView id = findViewById(R.id.dashboardTestActiveID);
//                    id.setText("1234");
//                    TextView password = findViewById(R.id.dashboardTestActivePassword);
//                    password.setText("1234");
//                    return;
//                }
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

        //get enrollment number from server
        //TODO
        String enrollmentNumber = "XXXXXXXXXX".toUpperCase();
        TextView enrollmentNumberTextView = findViewById(R.id.dashboardUserEnrollmentNumber);
        enrollmentNumberTextView.setText(enrollmentNumber);

        FrameLayout editProfile = findViewById(R.id.dashboardEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editProfileActivity = new Intent(getApplicationContext(),UpdateProfileActivity.class);
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
}