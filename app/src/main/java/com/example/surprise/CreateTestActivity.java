package com.example.surprise;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CreateTestActivity extends AppCompatActivity {

    private static final String TAG = "CreateTestActivity";
    private static final int MAX_QUESTIONS = 60;
    private static final String API_URL = "https://question-gen-production.up.railway.app/generate-mcq";

    private TextView duration;
    private TextView testTitle;
    private Button generateQuestions;
    private TextView numberOfQuestions;
    private Button uploadPDF;
    private TextView fileName;
    private ProgressBar progressBar;

    private Uri selectedPdfUri;
    private final OkHttpClient client = new OkHttpClient();

    private final ActivityResultLauncher<Intent> pdfPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedPdfUri = uri;
                        updateFileNameText(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_test);

        // Set up edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        duration = findViewById(R.id.createTestDuration);
        testTitle = findViewById(R.id.createTestTitle);
        generateQuestions = findViewById(R.id.createTestGenerateTest);
        numberOfQuestions = findViewById(R.id.createTestNumberOfQuestions);
        uploadPDF = findViewById(R.id.createTestUploadButton);
        fileName = findViewById(R.id.createTestPdfFileName);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar == null) {
            // Add a progress bar in your layout if not already there
            Log.w(TAG, "Progress bar not found in layout. Consider adding one.");
        }

        // Set up initial state
        fileName.setText("No file selected");
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Set up click listeners
        uploadPDF.setOnClickListener(view -> openPdfPicker());

        generateQuestions.setOnClickListener(view -> {
            if (selectedPdfUri == null) {
                Toast.makeText(this, "Please upload a PDF file first", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse duration input
            String durationText = duration.getText().toString();
            if (durationText.isEmpty()) {
                Toast.makeText(this, "Please enter a test duration", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse number of questions
            String questionsText = numberOfQuestions.getText().toString();
            if (questionsText.isEmpty()) {
                Toast.makeText(this, "Please enter the number of questions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get test title
            String title = testTitle.getText().toString();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a test title", Toast.LENGTH_SHORT).show();
                return;
            }

            int questionCount = Integer.parseInt(questionsText);
            if (questionCount <= 0 || questionCount > MAX_QUESTIONS) {
                Toast.makeText(this, "Number of questions must be between 1 and " + MAX_QUESTIONS, Toast.LENGTH_SHORT).show();
                return;
            }

            // Start generating the test
            generateTest(title, durationText, questionCount);
        });
    }

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPickerLauncher.launch(intent);
    }

    private void updateFileNameText(Uri uri) {
        String name = getFileNameFromUri(uri);
        fileName.setText(name);
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }

        return result;
    }

    private void generateTest(String title, String testDuration, int numQuestions) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        generateQuestions.setEnabled(false);

        try {
            // Create temp file from uri
            File pdfFile = getPdfFileFromUri(selectedPdfUri);
            if (pdfFile == null) {
                Toast.makeText(this, "Failed to process PDF file", Toast.LENGTH_SHORT).show();
                resetUI();
                return;
            }

            // Build request body
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", pdfFile.getName(),
                            RequestBody.create(MediaType.parse("application/pdf"), pdfFile))
                    .addFormDataPart("numq", String.valueOf(numQuestions))
                    .addFormDataPart("expiry", testDuration)
                    .build();

            // Create request
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(requestBody)
                    .build();

            // Execute request
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(CreateTestActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        resetUI();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            Toast.makeText(CreateTestActivity.this, "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                            resetUI();
                        });
                        response.close();
                        return;
                    }

                    String responseData;
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            runOnUiThread(() -> {
                                Toast.makeText(CreateTestActivity.this, "Empty response from server", Toast.LENGTH_LONG).show();
                                resetUI();
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

                        String testId = json.optString("Id", "0");
                        String testPwd = json.optString("Pwd", "0000");

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

                        ArrayList<Question> questions = new ArrayList<>();

                        for (int i = 0; i < mcqsArray.length(); i++) {
                            JSONObject mcq = mcqsArray.getJSONObject(i);
                            String questionText = mcq.optString("question", "Question " + (i + 1));

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

                            if (mcq.has("correctAnswer")) {
                                correctAnswer = mcq.optString("correctAnswer", "A");
                            }

                            questions.add(new Question(
                                    questionText,
                                    opt1, opt2, opt3, opt4, correctAnswer
                            ));
                        }

                        runOnUiThread(() -> {
                            // Launch the GeneratedQuestions activity
                            Intent intent = new Intent(CreateTestActivity.this, GeneratedQuestions.class);
                            intent.putExtra("questions", questions);
                            intent.putExtra("testId", testId);
                            intent.putExtra("testPwd", testPwd);
                            intent.putExtra("testTitle", title);
                            startActivity(intent);
                            resetUI();
                        });

                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage() + ", Response: " + responseData);
                        runOnUiThread(() -> {
                            Toast.makeText(CreateTestActivity.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            resetUI();
                        });
                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            resetUI();
        }
    }

    private File getPdfFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            File tempFile = File.createTempFile("upload", ".pdf", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            Log.e(TAG, "Error creating file from URI", e);
            return null;
        }
    }

    private void resetUI() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        generateQuestions.setEnabled(true);
    }
}