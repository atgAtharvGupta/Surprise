package com.example.surprise;

import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
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

public class Test extends AppCompatActivity implements  Submit.OnSubmitCalled {
    static ArrayList<Question> questionArrayList = new ArrayList<>();
    private int pauseCount,resumeCount;
    public void onSubmitCalled(){
        finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        pauseCount++;
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCount++;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        questionArrayList.clear();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //prevent screenshots
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_test);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        Button testAcknowledge = findViewById(R.id.testAcknowledge);
        testAcknowledge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.testDisclaimer).setVisibility(View.INVISIBLE);
                findViewById(R.id.testQuestionsRecyclerView).setVisibility(View.VISIBLE);
            }
        });

        String id = getIntent().getStringExtra("id");
        String password = getIntent().getStringExtra("password");

        // Sample Questions - Replace with actual server data
        //TODO
        String testEndTime = "18:26"; // Get this from server (24-hour format)

        //TODO
        System.out.println("DEBUG");
        System.out.println(questionArrayList.size());
        for (int i = 0; i < questionArrayList.size(); i++) {
            System.out.println(questionArrayList.get(i).question);
        }

        RecyclerView recyclerViewQuestions = findViewById(R.id.testQuestionsRecyclerView);
        recyclerViewQuestions.setLayoutManager(new LinearLayoutManager(this));
        RecycleViewQuestionsAdapter recycleViewQuestionsAdapter = new RecycleViewQuestionsAdapter(this, questionArrayList);
        recyclerViewQuestions.setAdapter(recycleViewQuestionsAdapter);

        int endHour = Integer.parseInt(testEndTime.substring(0, 2));
        int endMinute = Integer.parseInt(testEndTime.substring(3, 5));

        // Start timer thread
        Submit submitThread = new Submit(endHour, endMinute, id, password, recycleViewQuestionsAdapter,this,this,pauseCount,resumeCount);
        submitThread.start();
    }
}

// Timer Thread Class
class Submit extends Thread {
    public interface OnSubmitCalled{
        void onSubmitCalled();
    }
    private final int targetHour;
    private final int targetMinute;
    private final String id;
    private final String password;
    private int pauseCount,resumeCount;
    private final RecycleViewQuestionsAdapter adapter;
    private final Context context;
    private final OnSubmitCalled onSubmitCalled;

    Submit(int hour, int minute, String id, String password, RecycleViewQuestionsAdapter adapter,OnSubmitCalled onSubmitCalled,Context context,int pauseCount,int resumeCount) {
        this.onSubmitCalled = onSubmitCalled;
        this.targetHour = hour;
        this.targetMinute = minute;
        this.id = id;
        this.password = password;
        this.adapter = adapter;
        this.context = context;
    }

    @Override
    public void run() {
        while (true) {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            //Submit answers OR response every one second
            adapter.submit(id, password,pauseCount,resumeCount);


            if (   (currentHour == targetHour && currentMinute == targetMinute)    ) {
                onSubmitCalled.onSubmitCalled();
                break;
            }

            // checks every one second
            try {
                Thread.sleep(1000);  // Sleep for 1 second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(context, "Submitted!", Toast.LENGTH_SHORT).show();
    }
}