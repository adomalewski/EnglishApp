package com.adsolutions.englishapp;

import static android.content.Context.MODE_PRIVATE;

import static com.adsolutions.englishapp.AppStorageHelpers.convertToListAsStringOnSave;
import static com.adsolutions.englishapp.AppStorageHelpers.convertToListFromStringOnRead;
import static com.adsolutions.englishapp.AppStorageHelpers.storeKeyAsString;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class AppStorageManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Questions data
    private static final String QUESTION_TEXT = "questionText";
    private static final String QUESTION_LANGUAGE = "questionLanguage";
    private static final String QUESTION_TOPIC = "questionTopic";
    private static final String CURRENT_ROUND_QUESTION_LIST_INDEX = "currentRoundQuestionListIndex";
    private static final String CURRENT_ROUND_QUESTION_LIST = "currentRoundQuestionList";
    private static final String PREVIOUS_ROUND_QUESTION_LIST = "previousRoundQuestionList";

    // Data source
    private static final String API_KEY = "apiKey";
    private static final String SPREADSHEET_ID = "spreadsheetId";

    public static final String preferencesFilename = "app.settings";

    private static AppStorageManager appStorageManager;

    public static AppStorageManager getInstance() {
        if (appStorageManager == null) {
            appStorageManager = new AppStorageManager();
        }

        return appStorageManager;
    }

    public static boolean deleteSharedPreferences(Context context, String name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.deleteSharedPreferences(name);
        } else {
            context.getSharedPreferences(name, MODE_PRIVATE).edit().clear().apply();
            File dir = new File(context.getApplicationInfo().dataDir, "shared_prefs");
            return new File(dir, name + ".xml").delete();
        }
    }

    private AppStorageManager() {
    }

    public void initializeStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(preferencesFilename, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();
    }

    public void storeQuestionLanguage(String questionLanguage) {
        storeKeyAsString(sharedPreferences, QUESTION_LANGUAGE, questionLanguage);
    }

    public String getQuestionLanguage() {
        return sharedPreferences.getString(QUESTION_LANGUAGE, null);
    }

    public void storeQuestionTopic(String questionTopic) {
        storeKeyAsString(sharedPreferences, QUESTION_TOPIC, questionTopic);
    }

    public String getQuestionTopic() {
        return sharedPreferences.getString(QUESTION_TOPIC, null);
    }

    public void storeQuestionText(String questionText) {
        storeKeyAsString(sharedPreferences, QUESTION_TEXT, questionText);
    }

    public String getQuestionText() {
        return sharedPreferences.getString(QUESTION_TEXT, null);
    }

    public void storeCurrentRoundQuestionListIndex(int currentRoundQuestionListIndex) {
        saveCurrentRoundQuestionListIndex(String.valueOf(currentRoundQuestionListIndex));
    }

    public void resetCurrentRoundQuestionListIndex() {
        saveCurrentRoundQuestionListIndex(null);
    }

    private void saveCurrentRoundQuestionListIndex(String currentRoundQuestionListIndex) {
        editor = sharedPreferences.edit();

        if (currentRoundQuestionListIndex == null || currentRoundQuestionListIndex.isBlank()) {
            editor.remove(CURRENT_ROUND_QUESTION_LIST_INDEX);
        } else {
            editor.putString(CURRENT_ROUND_QUESTION_LIST_INDEX, currentRoundQuestionListIndex);
        }

        editor.commit();
        editor.apply();
    }

    public int getCurrentRoundQuestionListIndex() {
        return Integer.parseInt(sharedPreferences.getString(CURRENT_ROUND_QUESTION_LIST_INDEX, "0"));
    }

    public void storeCurrentRoundQuestionsList(List<Integer> currentRoundQuestionsList) {
        storeKeyAsString(sharedPreferences, CURRENT_ROUND_QUESTION_LIST,
                convertToListAsStringOnSave(currentRoundQuestionsList));
    }

    public List<Integer> getCurrentRoundQuestionsList() {
        return convertToListFromStringOnRead(
                sharedPreferences.getString(CURRENT_ROUND_QUESTION_LIST, null));
    }

    public void resetCurrentRoundQuestionsList() {
        storeCurrentRoundQuestionsList(Collections.emptyList());
    }

    public void storePreviousRoundQuestionsList(List<Integer> previousRoundQuestionsList) {
        storeKeyAsString(sharedPreferences, PREVIOUS_ROUND_QUESTION_LIST,
                convertToListAsStringOnSave(previousRoundQuestionsList));
    }

    public List<Integer> getPreviousRoundQuestionsList() {
        return convertToListFromStringOnRead(
                sharedPreferences.getString(PREVIOUS_ROUND_QUESTION_LIST, null));
    }

    public void resetPreviousRoundQuestionsList() {
        storePreviousRoundQuestionsList(Collections.emptyList());
    }

    public String getAPIkey() {
        return sharedPreferences.getString(API_KEY, null);
    }

    public void storeAPIkey(String apiKey) {
        storeKeyAsString(sharedPreferences, API_KEY, apiKey);
    }

    public String getSpreadsheetId() {
        return sharedPreferences.getString(SPREADSHEET_ID, null);
    }

    public void storeSpreadsheetId(String spreadsheetId) {
        storeKeyAsString(sharedPreferences, SPREADSHEET_ID, spreadsheetId);
    }
}