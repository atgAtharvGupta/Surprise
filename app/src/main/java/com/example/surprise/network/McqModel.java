package com.example.surprise.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class McqModel {
    private String question;
    private Map<String, String> options; // Changed from List to Map
    private String correctAnswer; // Changed from int to String

    public McqModel(String question, Map<String, String> options, String correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Getters
    public String getQuestion() {
        return question;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    // Parse JSONArray to List of McqModels
    public static List<McqModel> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<McqModel> mcqList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject mcqJson = jsonArray.getJSONObject(i);

            // Get question
            String question = mcqJson.getString("question");

            // Parse options as a JSONObject instead of JSONArray
            JSONObject optionsJson = mcqJson.getJSONObject("options");
            Map<String, String> options = new LinkedHashMap<>(); // LinkedHashMap maintains insertion order

            // Iterate through all keys in the options object
            Iterator<String> keys = optionsJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = optionsJson.getString(key);
                options.put(key, value);
            }

            // Get correct answer
            String correctAnswer = mcqJson.getString("correctAnswer");

            mcqList.add(new McqModel(question, options, correctAnswer));
        }

        return mcqList;
    }
}