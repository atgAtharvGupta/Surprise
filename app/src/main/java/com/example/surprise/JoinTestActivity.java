package com.example.surprise;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.surprise.network.ApiService;
import com.example.surprise.network.McqModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

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
                if(TextUtils.isEmpty(id) || TextUtils.isEmpty(password)){
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                fetchMcqs(id,password);
                Intent test = new Intent(getApplicationContext(),Test.class);
                startActivity(test);
                finish();
            }
        });
    }
    private void fetchMcqs(String userId, String password) {
        ApiService.getInstance().getMcqs(userId, password, new ApiService.McqCallback() {
            @Override
            public void onSuccess(final JSONArray mcqs) {
                // Since OkHttp callbacks run on a background thread,
                // we need to switch to the main thread to update UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<McqModel> mcqList = McqModel.fromJsonArray(mcqs);
                            // Now use the mcqList to update your UI
                            Log.d(TAG, "Received " + mcqList.size() + " MCQs");

                            for(int i=0; i<mcqList.size(); i++){
                                Question question = new Question(
                                        mcqList.get(i).getQuestion(),
                                        mcqList.get(i).getOptions().get("A"),
                                        mcqList.get(i).getOptions().get("B"),
                                        mcqList.get(i).getOptions().get("C"),
                                        mcqList.get(i).getOptions().get("D"),
                                        mcqList.get(i).getCorrectAnswer()
                                );
                                Test.questionArrayList.add(question);
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing MCQs", e);
                            Toast.makeText(JoinTestActivity.this,
                                    "Error processing MCQ data",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onError(final String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "API Error: " + errorMsg);
                        Toast.makeText(JoinTestActivity.this,
                                "Error: " + errorMsg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}