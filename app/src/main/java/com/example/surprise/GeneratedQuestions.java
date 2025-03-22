package com.example.surprise;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GeneratedQuestions extends AppCompatActivity {

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
        RecyclerView recyclerView = findViewById(R.id.generatedQuestionsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        Uri pdf = getIntent().getParcelableExtra("pdf");
        //TODO
        //use this pdf to generate the questions from server
        ArrayList<Question> questions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            questions.add(new Question(
                    "Does true equality exist?",
                    "no", "yes", "yesn't", "arguable", "A"
            ));
        }
        RecyclerViewGeneratedQuestionsAdapter adapter = new RecyclerViewGeneratedQuestionsAdapter(questions,this);
        recyclerView.setAdapter(adapter);

        Button startTestButton = findViewById(R.id.generatedQuestionsStartTest);
        startTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                String testID = "1234";
                String password = "1234";
                TextView id = findViewById(R.id.testGeneratedNoticeID);
                id.setText(testID);
                TextView passwordTextView = findViewById(R.id.testGeneratedNoticePassword);
                passwordTextView.setText(password);
                findViewById(R.id.testGeneratedNotice).setVisibility(View.VISIBLE);
                //genrate the test if a test not active
            }
        });
        findViewById(R.id.testGeneratedNoticebtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = ((TextView)findViewById(R.id.testGeneratedNoticeID)).getText().toString();
                String passwrod = ((TextView)findViewById(R.id.testGeneratedNoticePassword)).getText().toString();
                copyToClipboard(GeneratedQuestions.this,"id: " + id + "\n" + "password: " + passwrod);
                finish();
            }
        });
    }
    public void copyToClipboard(Context context, String textToCopy) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("copied_text", textToCopy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}