package com.example.surprise;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GeneratedQuestions extends AppCompatActivity implements QuestionRegenerateListener {

    private static final String TAG = "GeneratedQuestions";
    private static final String REPLACE_API_URL = "https://question-gen-production.up.railway.app/replace-q";

    private ArrayList<Question> questions;
    private String testId;
    private String testPwd;
    private String testTitle;
    private RecyclerViewGeneratedQuestionsAdapter adapter;
    private ProgressBar progressBar;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_generated_questions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Add a progress bar in your layout
        progressBar = findViewById(R.id.progressBar);
        if (progressBar == null) {
            Log.w(TAG, "Progress bar not found in layout. Consider adding one.");
        } else {
            progressBar.setVisibility(View.GONE);
        }

        RecyclerView recyclerView = findViewById(R.id.generatedQuestionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get the generated questions from the intent
        questions = (ArrayList<Question>) getIntent().getSerializableExtra("questions");
        testId = getIntent().getStringExtra("testId");
        testPwd = getIntent().getStringExtra("testPwd");
        testTitle = getIntent().getStringExtra("testTitle");

        if (questions == null) {
            questions = new ArrayList<>();
            // Fallback sample questions if needed
            for (int i = 0; i < 10; i++) {
                questions.add(new Question(
                        "Sample Question " + (i + 1),
                        "Option A", "Option B", "Option C", "Option D", "A"
                ));
            }
        }

        adapter = new RecyclerViewGeneratedQuestionsAdapter(questions, this, this);
        recyclerView.setAdapter(adapter);

        Button startTestButton = findViewById(R.id.generatedQuestionsStartTest);
        startTestButton.setOnClickListener(v -> {
            TextView id = findViewById(R.id.testGeneratedNoticeID);
            id.setText("Test ID: " + testId);

            TextView passwordTextView = findViewById(R.id.testGeneratedNoticePassword);
            passwordTextView.setText("Password: " + testPwd);

            findViewById(R.id.testGeneratedNotice).setVisibility(View.VISIBLE);
        });

        findViewById(R.id.testGeneratedNoticebtn).setOnClickListener(v -> {
            String id = ((TextView) findViewById(R.id.testGeneratedNoticeID)).getText().toString();
            String password = ((TextView) findViewById(R.id.testGeneratedNoticePassword)).getText().toString();
            copyToClipboard(GeneratedQuestions.this, id + "\n" + password);
            finish();
        });
    }

    @Override
    public void onRegenerateQuestion(int position) {
        if (testId == null || testPwd == null) {
            Toast.makeText(this, "Cannot regenerate: Test credentials missing", Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        try {
            JSONObject requestJson = new JSONObject();
            requestJson.put("Id", testId);
            requestJson.put("Pwd", testPwd);
            requestJson.put("index", position);

            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    requestJson.toString());

            Request request = new Request.Builder()
                    .url(REPLACE_API_URL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(GeneratedQuestions.this, "Network error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(GeneratedQuestions.this, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                        });
                        response.close();
                        return;
                    }

                    String responseData;
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            runOnUiThread(() -> {
                                Toast.makeText(GeneratedQuestions.this, "Empty response from server", Toast.LENGTH_SHORT).show();
                                if (progressBar != null) progressBar.setVisibility(View.GONE);
                            });
                            return;
                        }
                        responseData = responseBody.string();
                    }

                    Log.d(TAG, "Response: " + responseData);

                    try {
                        // First, try to parse as JSONObject
                        JSONObject json;
                        try {
                            json = new JSONObject(responseData);
                        } catch (JSONException e) {
                            // If that fails, try to parse as JSONArray
                            JSONArray jsonArray = new JSONArray(responseData);
                            json = jsonArray.getJSONObject(0);
                        }

                        // Check if "mcqs" is directly in the object or if there's a "questions" field
                        JSONArray mcqsArray;
                        if (json.has("mcqs")) {
                            mcqsArray = json.getJSONArray("mcqs");
                        } else if (json.has("questions")) {
                            mcqsArray = json.getJSONArray("questions");
                        } else {
                            // If neither exists, the response might be the array itself
                            mcqsArray = new JSONArray(responseData);
                        }

                        // Update the questions list
                        ArrayList<Question> updatedQuestions = new ArrayList<>();

                        for (int i = 0; i < mcqsArray.length(); i++) {
                            JSONObject mcq = mcqsArray.getJSONObject(i);
                            String questionText = mcq.optString("question", "Question " + (i+1));

                            // Handle options which might be in different formats
                            String opt1 = "Option A";
                            String opt2 = "Option B";
                            String opt3 = "Option C";
                            String opt4 = "Option D";
                            String correctAnswer = "A";

                            // Try to get options from different possible formats
                            if (mcq.has("options")) {
                                Object optionsObj = mcq.get("options");
                                if (optionsObj instanceof JSONArray) {
                                    JSONArray options = (JSONArray) optionsObj;
                                    if (options.length() > 0) opt1 = options.optString(0, opt1);
                                    if (options.length() > 1) opt2 = options.optString(1, opt2);
                                    if (options.length() > 2) opt3 = options.optString(2, opt3);
                                    if (options.length() > 3) opt4 = options.optString(3, opt4);
                                } else if (optionsObj instanceof JSONObject) {
                                    JSONObject options = (JSONObject) optionsObj;
                                    opt1 = options.optString("A", opt1);
                                    opt2 = options.optString("B", opt2);
                                    opt3 = options.optString("C", opt3);
                                    opt4 = options.optString("D", opt4);
                                }
                            }

                            if (mcq.has("answer")) {
                                correctAnswer = mcq.optString("answer", "A");
                            } else if (mcq.has("correctAnswer")) {
                                correctAnswer = mcq.optString("correctAnswer", "A");
                            }

                            updatedQuestions.add(new Question(
                                    questionText,
                                    opt1, opt2, opt3, opt4, correctAnswer
                            ));
                        }

                        runOnUiThread(() -> {
                            questions.clear();
                            questions.addAll(updatedQuestions);
                            adapter.notifyDataSetChanged();
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                            Toast.makeText(GeneratedQuestions.this, "Question regenerated successfully", Toast.LENGTH_SHORT).show();
                        });

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage() + ", Response: " + responseData);
                        runOnUiThread(() -> {
                            Toast.makeText(GeneratedQuestions.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (progressBar != null) progressBar.setVisibility(View.GONE);
                        });
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (progressBar != null) progressBar.setVisibility(View.GONE);
        }
    }

    public void copyToClipboard(Context context, String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copied_text", textToCopy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}