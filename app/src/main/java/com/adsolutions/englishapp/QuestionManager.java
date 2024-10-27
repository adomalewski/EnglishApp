package com.adsolutions.englishapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class QuestionManager {

    private static final Map<String, List<String>> dictionaryMapEn = new HashMap<>();
    private static final Map<String, List<String>> dictionaryMapPl = new HashMap<>();

    private static final String initialQuestionLanguage = "EN";
    private static String questionLanguage = initialQuestionLanguage;

    private static String questionText;

    public static String getQuestionLanguage() {
        return questionLanguage;
    }

    public static void setQuestionLanguage(String _questionLanguage) {
        questionLanguage = _questionLanguage;
    }

    public static String getQuestionText() {
        return questionText;
    }

    public static Map<String, List<String>> getDictionaryMapEn() {
        return dictionaryMapEn;
    }

    public static Map<String, List<String>> getDictionaryMapPl() {
        return dictionaryMapPl;
    }

    public static boolean checkResult(String userAnswer) {
        List<String> goodAnswers;
        if (Objects.equals(questionLanguage, "EN")) {
            goodAnswers = dictionaryMapEn.get(questionText);
        } else {
            goodAnswers = dictionaryMapPl.get(questionText);
        }
        String userAnswerLowerCase = userAnswer.toLowerCase();
        return Objects.requireNonNull(goodAnswers).stream().anyMatch(s -> {
            s = s.toLowerCase();
            if (s.equals(userAnswerLowerCase)) {
                return true;
            }
            if (s.endsWith("?")) {
                s = s.substring(0, s.length() - 1);
                return s.equals(userAnswerLowerCase);
            }
            return false;
        });
    }

    public static void drawQuestion() {
        Random rand = new Random();
        int randIndex;
        List<String> keysAsArray;
        if (Objects.equals(questionLanguage, "EN")) {
            randIndex = rand.nextInt(dictionaryMapEn.size());
            keysAsArray = new ArrayList<>(dictionaryMapEn.keySet());
        } else {
            randIndex = rand.nextInt(dictionaryMapPl.size());
            keysAsArray = new ArrayList<>(dictionaryMapPl.keySet());
        }
        questionText = keysAsArray.get(randIndex);
    }

    public static String getFormattedAnswer() {
        StringBuilder textBuilder = new StringBuilder();
        Map<String, List<String>> dictionary = Objects.equals(questionLanguage, "EN") ?
                dictionaryMapEn : dictionaryMapPl;
        Objects.requireNonNull(dictionary.get(questionText)).forEach(text -> {
            if (textBuilder.length() > 0) {
                textBuilder.append("\n");
            }
            textBuilder.append(text);
        });

        return textBuilder.toString();
    }

    public static void retrieveNextQuestion() {
        drawQuestion();
        AppStorageManager.getInstance().storeQuestionText(questionText);
    }

    public static void readQuestionsDataFromStorage() {
        questionLanguage = AppStorageManager.getInstance().getQuestionLanguage();
        questionText = AppStorageManager.getInstance().getQuestionText();
    }

    public static void storeQuestionsData() {
        AppStorageManager.getInstance().storeQuestionLanguage(questionLanguage);
        AppStorageManager.getInstance().storeQuestionText(questionText);
    }

    public static void storeSettingsIfUnexist() {
        if (AppStorageManager.getInstance().getQuestionLanguage() == null) {
            AppStorageManager.getInstance().storeQuestionLanguage(initialQuestionLanguage);
        }
    }

    public static boolean isDictionaryFilled() {
        return !dictionaryMapEn.isEmpty() && !dictionaryMapPl.isEmpty();
    }

    public static boolean isWordInStorage() {
        readQuestionsDataFromStorage();
        return questionText != null;
    }
}