package com.example.surprise;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText userNameEditText, contactEditText;
    private TextView errorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userNameEditText = findViewById(R.id.updateProfileUserName);
        contactEditText = findViewById(R.id.updateProfileContact);
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

                            userNameEditText.setText(userName);
                            contactEditText.setText(contact);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle possible errors.
                    });
        }

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = userNameEditText.getText().toString();
                String contact = contactEditText.getText().toString();

                if (contact.isEmpty() || userName.isEmpty()) {
                    errorMessageTextView.setVisibility(View.VISIBLE);
                } else {
                    if (user != null) {
                        String userId = user.getUid();
                        db.collection("users").document(userId)
                                .update("name", userName, "contact", contact)
                                .addOnSuccessListener(aVoid -> finish())
                                .addOnFailureListener(e -> {
                                    errorMessageTextView.setText("Error: " + e.getMessage());
                                    errorMessageTextView.setVisibility(View.VISIBLE);
                                });
                    }
                }
            }
        });
    }
}