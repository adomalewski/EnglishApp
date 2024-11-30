package com.adsolutions.englishapp;

import static com.adsolutions.englishapp.NotificationScheduleManager.getScheduleMap;
import static com.adsolutions.englishapp.QuestionManager.getDictionaryMapEn;

import android.content.Context;
import android.util.Log;

import com.adsolutions.englishapp.enums.Topic;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExternalDataManager {

    private static String spreadsheetId;
    private static String apiKey;

    private static final String appSchedulerTab = "AppScheduler";

    public static void getDataFromAPI(Context context, Supplier<Void> callbackSetQuestionText) {
        RequestQueue queue = Volley.newRequestQueue(context);
        QuestionManager.getQuestionTopicsList().forEach(topic -> {
            if(topic.equals(QuestionManager.getQuestionTopic())) {
                queue.add(fetchAndFillInDictionary(topic, callbackSetQuestionText));
            } else {
                queue.add(fetchAndFillInDictionary(topic, () -> null));
            }
        });
        queue.add(fetchAndFillSchedule());
    }

    private static String getSpreadsheetUrl(String tabName) {
        return "https://sheets.googleapis.com/v4/spreadsheets/" + spreadsheetId + "/values/"
                + tabName + "?alt=json&key=" + apiKey;
    }

    private static JsonObjectRequest fetchAndFillInDictionary(Topic topic, Supplier<Void> callbackSetQuestionText) {
            JSONObject jsonObject = null;
            String url = getSpreadsheetUrl(topic.name());
            return new JsonObjectRequest(Request.Method.GET, url, jsonObject, response -> {
                try {
                    JSONArray rows = response.getJSONArray("values");
                    for (int i = 0; i < rows.length(); i++) {
                        JSONArray row = rows.getJSONArray(i);
                        String keyWord = row.getString(0);
                        List<String> listAnswers = new ArrayList<>();
                        for (int j = 1; j < row.length(); j++) {
                            listAnswers.add(row.getString(j));
                        }

                        getDictionaryMapEn().put(keyWord, listAnswers);
                    }
                    callbackSetQuestionText.get();
                } catch (Exception e) {
                    Log.e("Error", "Error when reading dictionary response: " + e);
                }
            }, error -> Log.e("Error", "Dictionary response: " + error));
    }

    private static JsonObjectRequest fetchAndFillSchedule() {
        String url = getSpreadsheetUrl(appSchedulerTab);
        return new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                JSONArray rows = response.getJSONArray("values");
                for (int i = 0; i < rows.length(); i++) {
                    JSONArray row = rows.getJSONArray(i);
                    String weekday = row.getString(0).toLowerCase();
                    List<String> spansList = new ArrayList<>();
                    for (int j = 1; j < row.length(); j++) {
                        spansList.add(row.getString(j));
                    }
                    getScheduleMap().put(weekday, spansList);
                }
            } catch (Exception e) {
                Log.e("Error", "Error when reading schedule response: " + e.getMessage());
            }
        }, error -> Log.e("Error", "Schedule response: " + error));
    }

    public static void setSpreadsheetId(String _spreadsheetId) {
        spreadsheetId = _spreadsheetId;
    }

    public static String getSpreadsheetId() {
        return spreadsheetId;
    }

    public static void setAPIkey(String _apiKey) {
        apiKey = _apiKey;
    }

    public static String getAPIkey() {
        return apiKey;
    }

    public static void storeAllData() {
        AppStorageManager.getInstance().storeSpreadsheetId(spreadsheetId);
        AppStorageManager.getInstance().storeAPIkey(apiKey);
    }

    public static void readAllDataFromStorage() {
        spreadsheetId = AppStorageManager.getInstance().getSpreadsheetId();
        apiKey = AppStorageManager.getInstance().getAPIkey();
    }
}