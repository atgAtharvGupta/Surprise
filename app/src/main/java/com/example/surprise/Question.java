package com.example.surprise;

import java.io.Serializable;

public class Question implements Serializable {
    String question;
    String option1, option2, option3, option4, correctOption;

    public Question(String question, String option1, String option2, String option3, String option4, String correctOption) {
        this.question = question;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctOption = correctOption;
    }
}