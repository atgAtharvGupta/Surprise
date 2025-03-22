package com.example.surprise;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class RecycleViewQuestionsAdapter extends RecyclerView.Adapter<RecycleViewQuestionsAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Question> questionArrayList;

    // Stores the selected option for each question (position -> optionNumber)
    private HashMap<Integer, Integer> answers = new HashMap<>();

    public RecycleViewQuestionsAdapter(Context context, ArrayList<Question> questionArrayList) {
        this.context = context;
        this.questionArrayList = questionArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.question, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Question question = questionArrayList.get(position);

        holder.question.setText(question.question);
        holder.option1.setText(question.option1);
        holder.option2.setText(question.option2);
        holder.option3.setText(question.option3);
        holder.option4.setText(question.option4);

        holder.option1.setOnCheckedChangeListener(null);
        holder.option2.setOnCheckedChangeListener(null);
        holder.option3.setOnCheckedChangeListener(null);
        holder.option4.setOnCheckedChangeListener(null);

        if (answers.containsKey(position)) {
            int selectedOption = answers.get(position);
            holder.option1.setChecked(selectedOption == 1);
            holder.option2.setChecked(selectedOption == 2);
            holder.option3.setChecked(selectedOption == 3);
            holder.option4.setChecked(selectedOption == 4);
        } else {
            holder.option1.setChecked(false);
            holder.option2.setChecked(false);
            holder.option3.setChecked(false);
            holder.option4.setChecked(false);
        }

        //settin up listenrers
        setupOptionListeners(holder, position);
    }

    @Override
    public int getItemCount() {
        return questionArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView question;
        RadioButton option1, option2, option3, option4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.questionTest);
            option1 = itemView.findViewById(R.id.option1);
            option2 = itemView.findViewById(R.id.option2);
            option3 = itemView.findViewById(R.id.option3);
            option4 = itemView.findViewById(R.id.option4);
        }
    }
    private void setupOptionListeners(ViewHolder holder, int position) {
        RadioButton[] options = {holder.option1, holder.option2, holder.option3, holder.option4};

        for (int x = 0; x < options.length; x++) {
            final int i = x;
            final int optionNumber = i + 1;  // Option 1, 2, 3, 4
            options[i].setOnClickListener(v -> {
                if (answers.containsKey(position) && answers.get(position) == optionNumber) {
                    answers.remove(position);
                    options[i].setChecked(false);
                } else {
                    answers.put(position, optionNumber);
                    for (int j = 0; j < options.length; j++) {
                        options[j].setChecked(j == i);
                    }
                }
            });
        }
    }
    public void submit(String id, String pass) {
        //TODO
        //WRITE CODE TO SUBMIT THE ANSWERS
        System.out.println("Submitting test for ID: " + id + " with Password: " + pass);
        for (int i = 0; i < questionArrayList.size(); i++) {
            String questionText = questionArrayList.get(i).question;
            int selectedOption = answers.getOrDefault(i, -1); // -1 = Not Attempted
            String result = "Q" + (i + 1) + ": " + questionText +
                    " -> Selected Option: " + (selectedOption == -1 ? "Not Attempted" : selectedOption);
            System.out.println(result);  // Log all answers (you can process these as needed)
        }

    }
}
