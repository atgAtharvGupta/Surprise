package com.example.surprise;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CreateTestActivity extends AppCompatActivity {

    private static final int MAX_QUESTIONS = 50;

    private TextView duration;
    private Button generateQuestions;
    private TextView numberOfQuestions;
    private Button uploadPDF;
    private TextView fileName;

    private Uri selectedPdfUri;

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
        generateQuestions = findViewById(R.id.createTestGenerateTest);
        numberOfQuestions = findViewById(R.id.createTestNumberOfQuestions);
        uploadPDF = findViewById(R.id.createTestUploadButton);
        fileName = findViewById(R.id.createTestPdfFileName);

        // Set up initial state
        fileName.setText("No file selected");

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

            int questionCount = Integer.parseInt(questionsText);
            if (questionCount <= 0 || questionCount > MAX_QUESTIONS) {
                Toast.makeText(this, "Number of questions must be between 1 and " + MAX_QUESTIONS, Toast.LENGTH_SHORT).show();
                return;
            }

            // Start generating the test
            generateTest(durationText, questionCount);
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

    private void generateTest(String testDuration, int numQuestions) {
        Intent generatedQuestions = new Intent(CreateTestActivity.this,GeneratedQuestions.class);
        generatedQuestions.putExtra("pdf",this.selectedPdfUri);
        startActivity(generatedQuestions);
        finish();
    }
}