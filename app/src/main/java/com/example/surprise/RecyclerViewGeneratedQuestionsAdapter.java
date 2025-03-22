package com.example.surprise;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewGeneratedQuestionsAdapter extends RecyclerView.Adapter<RecyclerViewGeneratedQuestionsAdapter.ViewHolder> {
    ArrayList<Question> questions;
    Context context;
    RecyclerViewGeneratedQuestionsAdapter(ArrayList<Question> questions,Context context){
        this.questions = questions;
        this.context = context;

    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.generated_question,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.regenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                //Send  a request to the server to regenerate this question and update the questions array
            }
        });
        holder.question.setText(questions.get(position).question);
        holder.option1.setText("A: " + questions.get(position).option1);
        holder.option2.setText("B: " + questions.get(position).option2);
        holder.option3.setText("C: " + questions.get(position).option3);
        holder.option4.setText("D: " + questions.get(position).option4);
        holder.correctOption.setText(questions.get(position).correctOption);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView question;
        TextView option1,option2,option3,option4,correctOption;
        ImageButton regenerate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.generatedQuestionDescription);
            option1 = itemView.findViewById(R.id.generatedQuestionOption1);
            option2 = itemView.findViewById(R.id.generatedQuestionOption2);
            option3 = itemView.findViewById(R.id.generatedQuestionOption3);
            option4 = itemView.findViewById(R.id.generatedQuestionOption4);
            correctOption = itemView.findViewById(R.id.generatedQuestionCorrectOption);
            regenerate = itemView.findViewById(R.id.generatedQuestionRegenerate);
        }
    }
}
