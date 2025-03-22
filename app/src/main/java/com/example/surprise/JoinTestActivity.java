package com.example.surprise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class JoinTestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_join_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView textViewID = findViewById(R.id.joinTestID);
        Button btn = findViewById(R.id.joinTestButton);
        TextView textViewPassword = findViewById(R.id.joinTestPassword);
        TextView error = findViewById(R.id.joinTestError);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = textViewID.getText().toString();
                String password = textViewPassword.getText().toString();
                //CHECK for id and password on the server
                //TODO
                if(id.equals("1234") && password.equals("1234") ){
                    Intent test = new Intent(getApplicationContext(),Test.class);
                    test.putExtra("id",id);
                    test.putExtra("password",password);
                    startActivity(test);
                    finish();
                }else{
                    error.setVisibility(View.VISIBLE);
                }
                //TODO
                //Also CHECK if test was already submitted or not 🙂🙂 by the user
            }
        });






    }
}