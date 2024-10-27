package com.adsolutions.englishapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class AppStorageManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private String preferencesFilename = "app.settings";

    private static AppStorageManager appStorageManager;

    public static AppStorageManager getInstance() {
        if (appStorageManager == null) {
            appStorageManager = new AppStorageManager();
        }

        return appStorageManager;
    }

    private AppStorageManager() {
    }

    public void initializeStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(preferencesFilename, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public void storeQuestionLanguage(String questionLanguage) {
        editor = sharedPreferences.edit();
        editor.putString("questionLanguage", questionLanguage);
        editor.commit();
        editor.apply();
    }

    public String getQuestionLanguage() {
        return sharedPreferences.getString("questionLanguage", null);
    }

    public void storeQuestionText(String questionText) {
        editor = sharedPreferences.edit();
        editor.putString("questionText", questionText);
        editor.commit();
        editor.apply();
    }

    public String getQuestionText() {
        return sharedPreferences.getString("questionText", null);
    }
}