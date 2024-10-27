package com.adsolutions.englishapp;

import static com.adsolutions.englishapp.NotificationScheduleManager.getScheduleMap;
import static com.adsolutions.englishapp.QuestionManager.getDictionaryMapEn;
import static com.adsolutions.englishapp.QuestionManager.getDictionaryMapPl;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ExternalDataManager {

    private static String spreadsheetId = "1_5IbT2gS7193n-ZjDJYpHA9Fd75Pc1m2HTSy5UbeY1Q";
    private static String apiKey = "AIzaSyB5Gjx7RiLUQWdx63tLyU0Snq8lLr7GrXw";

    public static void getDataFromAPI(Context context, Supplier<Void> callbackSetQuestionText) {
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(fetchAndFillInDictionary("EN", callbackSetQuestionText));
        queue.add(fetchAndFillInDictionary("PL", callbackSetQuestionText));
        queue.add(fetchAndFillSchedule());
    }

    private static String getSpreadsheetUrl(String tabName) {
        return "https://sheets.googleapis.com/v4/spreadsheets/" + spreadsheetId + "/values/"
                + tabName + "?alt=json&key=" + apiKey;
    }

    private static JsonObjectRequest fetchAndFillInDictionary(String language, Supplier<Void> callbackSetQuestionText) {
        String url = Objects.equals(language, "EN") ?
                getSpreadsheetUrl("EN-PL") : getSpreadsheetUrl("PL-EN");
        return new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                JSONArray rows = response.getJSONArray("values");
                for (int i = 0; i < rows.length(); i++) {
                    JSONArray row = rows.getJSONArray(i);
                    String keyWord = row.getString(0);
                    List<String> listAnswers = new ArrayList<>();
                    for (int j = 1; j < row.length(); j++) {
                        listAnswers.add(row.getString(j));
                    }

                    if(Objects.equals(language, "EN")) {
                        getDictionaryMapEn().put(keyWord, listAnswers);
                    } else {
                        getDictionaryMapPl().put(keyWord, listAnswers);
                    }
                }
                callbackSetQuestionText.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, error -> { Log.e("Error", "Error response: " + error); });
    }

    private static JsonObjectRequest fetchAndFillSchedule() {
        String url = getSpreadsheetUrl("schedule");
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
                e.printStackTrace();
            }
        }, error -> { Log.e("Error", "Error response: " + error); });
    }
}
