package com.example.surprise.network;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {
    private static final String TAG = "ApiService";
    private static final String BASE_URL = "https://question-gen-production.up.railway.app"; // Replace with actual URL
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static ApiService instance;
    private final OkHttpClient client;

    private ApiService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public interface McqCallback {
        void onSuccess(JSONArray mcqs);
        void onError(String errorMsg);
    }

    public void getMcqs(String id, String pwd, final McqCallback callback) {
        try {
            // Create JSON request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("Id", id);
            jsonBody.put("Pwd", pwd);
            String jsonString = jsonBody.toString();
            RequestBody body = RequestBody.create(JSON, jsonString);

            // Build the request
            Request request = new Request.Builder()
                    .url(BASE_URL + "/get-mcq")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")  // Explicitly request JSON response
                    .build();

            // Execute the request asynchronously
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    callback.onError("Network request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() == null) {
                        callback.onError("Empty response from server");
                        return;
                    }

                    String responseData = response.body().string();
                    response.close(); // Close the response to prevent resource leaks

                    // Log the raw response for debugging
                    Log.d(TAG, "Raw response: " + responseData.substring(0, Math.min(500, responseData.length())));

                    // Check if the response is HTML instead of JSON
                    if (responseData.trim().startsWith("<!DOCTYPE") || responseData.trim().startsWith("<html")) {
                        callback.onError("Server returned HTML instead of JSON. Please check the server URL and endpoint.");
                        return;
                    }

                    try {
                        if (!response.isSuccessful()) {
                            // Try to parse as JSON, but handle the case where it might not be JSON
                            try {
                                JSONObject errorJson = new JSONObject(responseData);
                                String errorMessage = errorJson.optString("error", "Error code: " + response.code());
                                callback.onError(errorMessage);
                            } catch (JSONException e) {
                                // If parsing fails, return the response code with raw data preview
                                callback.onError("Error code: " + response.code() +
                                        " - Response is not valid JSON. First 100 chars: " +
                                        responseData.substring(0, Math.min(100, responseData.length())));
                            }
                            return;
                        }

                        // Try to parse the response as JSON
                        JSONObject jsonResponse = new JSONObject(responseData);
                        if (jsonResponse.has("mcqs")) {
                            JSONArray mcqs = jsonResponse.getJSONArray("mcqs");
                            callback.onSuccess(mcqs);
                        } else {
                            callback.onError("Response doesn't contain MCQs data. Available keys: " +
                                    jsonResponse.keys().toString());
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error", e);
                        callback.onError("Failed to parse response: " + e.getMessage() +
                                "\nFirst 100 chars of response: " +
                                responseData.substring(0, Math.min(100, responseData.length())));
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error", e);
            callback.onError("Failed to create request: " + e.getMessage());
        }
    }
}