package com.adsolutions.englishapp;

import static com.adsolutions.englishapp.DrawQuestionHelper.checkIfOneOfLastElementsOfListContainsIntValue;
import static com.adsolutions.englishapp.DrawQuestionHelper.populateAllIndices;
import static com.adsolutions.englishapp.QuestionMonitoring.clearAllPreviousQuestionsList;

import com.adsolutions.englishapp.enums.Language;
import com.adsolutions.englishapp.enums.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class QuestionManager {

    private static final Map<String, List<String>> dictionaryMapEn = new LinkedHashMap<>();
    private static final Map<String, List<String>> dictionaryMapPl = new LinkedHashMap<>();

    private static final Language initialQuestionLanguage = Language.EN;
    private static final Topic initialQuestionTopic = Topic.GENERAL;
    private static int currentRoundQuestionListIndex = 0;
    private static List<Integer> previousRoundQuestionsList;
    private static List<Integer> currentRoundQuestionsList;
    private static Language questionLanguage = initialQuestionLanguage;
    private static Topic questionTopic = initialQuestionTopic;
    private static List<Topic> questionTopicsList =
            new ArrayList<>(Collections.singletonList(Topic.GENERAL));

    private static String questionText;

    private static final int distanceToHandleRepeatedWordsNearBoundary = 5;

    public static Language getQuestionLanguage() {
        return questionLanguage;
    }

    public static void setQuestionLanguage(Language _questionLanguage) {
        questionLanguage = _questionLanguage;
    }

    public static Topic getQuestionTopic() {
        return questionTopic;
    }

    public static void setQuestionTopic(Topic _questionTopic) {
        questionTopic = _questionTopic;
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

    public static List<Topic> getQuestionTopicsList() {
        return questionTopicsList;
    }

    public static void setQuestionTopicsList(List<Topic> _questionTopicsList) {
        questionTopicsList = _questionTopicsList;
    }

    private static void drawQuestion() {
        Random rand = new Random();
        int randomIndexInDynamicList;
        int newDictionaryLength = getNewDictionary().size();

        if (newDictionaryLength == 0) {
            return;
        }

        List<Integer> dynamicListWithInitiallyAllIndices = populateAllIndices(newDictionaryLength);
        List<Integer> allOrderedNewIndices = new ArrayList<>();

        for (int i = 0; i < newDictionaryLength; i++) {
            randomIndexInDynamicList = rand.nextInt(dynamicListWithInitiallyAllIndices.size());
            allOrderedNewIndices.add(dynamicListWithInitiallyAllIndices.get(randomIndexInDynamicList));
            dynamicListWithInitiallyAllIndices.remove(randomIndexInDynamicList);
        }

        if (isCurrentRoundQuestionsListWithElements()) {
            int currentRoundQuestionsListSize = currentRoundQuestionsList.size();
            if (newDictionaryLength >= distanceToHandleRepeatedWordsNearBoundary * 2) {
                int maxDistanceInCurrRoundQuestionList = Math.min(distanceToHandleRepeatedWordsNearBoundary, currentRoundQuestionsListSize);
                List<Integer> dynamicListWithIndicesToCheckWithinDistanceInNewList = populateAllIndices(distanceToHandleRepeatedWordsNearBoundary);
                int transmissionCurrentIndex = newDictionaryLength / 2;
                for (int i = 1; i <= maxDistanceInCurrRoundQuestionList && isTransmissionStillPossible(transmissionCurrentIndex, newDictionaryLength); i++) {
                    int questionIndexInCurrRoundQuestionList = currentRoundQuestionsList.get(currentRoundQuestionsListSize - i);
                    for (int j = 0; j < dynamicListWithIndicesToCheckWithinDistanceInNewList.size() && isTransmissionStillPossible(transmissionCurrentIndex, newDictionaryLength); j++) {
                        int dynamicListWithIndicesToCheckWithinDistanceInNewListNormalizedIndex = dynamicListWithIndicesToCheckWithinDistanceInNewList.get(j);
                        if (allOrderedNewIndices.get(dynamicListWithIndicesToCheckWithinDistanceInNewListNormalizedIndex).equals(questionIndexInCurrRoundQuestionList)) {
                            while (isTransmissionStillPossible(transmissionCurrentIndex, newDictionaryLength)) {
                                int questionIndexToCheck = allOrderedNewIndices.get(transmissionCurrentIndex);
                                if (checkIfOneOfLastElementsOfListContainsIntValue(currentRoundQuestionsList, maxDistanceInCurrRoundQuestionList, questionIndexToCheck)) {
                                    transmissionCurrentIndex++;
                                } else {
                                    Collections.swap(allOrderedNewIndices, dynamicListWithIndicesToCheckWithinDistanceInNewListNormalizedIndex, transmissionCurrentIndex);
                                    transmissionCurrentIndex++;
                                    j = removeIndexInListAndReturnIndexToEndLoop(dynamicListWithIndicesToCheckWithinDistanceInNewList, j);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        previousRoundQuestionsList = currentRoundQuestionsList;
        currentRoundQuestionsList = allOrderedNewIndices;
    }

    private static boolean isCurrentRoundQuestionsListWithElements() {
        return currentRoundQuestionsList != null && !currentRoundQuestionsList.isEmpty();
    }

    private static int removeIndexInListAndReturnIndexToEndLoop(List<Integer> list, int index) {
        list.remove(index);
        return list.size();
    }

    private static boolean isTransmissionStillPossible(int transmissionCurrentIndex, int newDictionaryLength) {
        return transmissionCurrentIndex < newDictionaryLength;
    }

    private static List<String> getNewDictionary() {
        if (Objects.equals(questionLanguage, Language.EN)) {
            return new ArrayList<>(dictionaryMapEn.keySet());
        } else {
            return new ArrayList<>(dictionaryMapPl.keySet());
        }
    }

    private static void setQuestion(int index) {
        questionText = getQuestionText(index);
    }

    public static String getQuestionText(int index) {
        List<String> dictionary = getNewDictionary();
        if (dictionary.isEmpty()) {
            return null;
        }
        return dictionary.get(index);
    }

    public static String getFormattedAnswer() {
        StringBuilder textBuilder = new StringBuilder();
        Map<String, List<String>> dictionary = Objects.equals(questionLanguage, Language.EN) ?
                dictionaryMapEn : dictionaryMapPl;

        if (questionText == null || questionText.isEmpty() || dictionary.get(questionText) == null) {
            return "";
        }

        Objects.requireNonNull(dictionary.get(questionText)).forEach(text -> {
            if (textBuilder.length() > 0) {
                textBuilder.append("\n");
            }
            textBuilder.append("- ").append(text);
        });

        return textBuilder.toString();
    }

    public static void retrieveNextQuestion() {
        if (isCurrentRoundQuestionsListWithElements() && currentRoundQuestionsList.size() - 1 > currentRoundQuestionListIndex) {
            currentRoundQuestionListIndex++;
            setQuestionUsingQuestionsList();
        } else {
            resetCurrentRoundQuestionListIndex();
            drawQuestion();
            if (isCurrentRoundQuestionsListWithElements()) {
                AppStorageManager.getInstance().storeCurrentRoundQuestionsList(currentRoundQuestionsList);
                AppStorageManager.getInstance().storePreviousRoundQuestionsList(previousRoundQuestionsList);
                setQuestionUsingQuestionsList();
            } else {
                questionText = "";
            }
        }

        AppStorageManager.getInstance().storeQuestionText(questionText);
        AppStorageManager.getInstance().storeCurrentRoundQuestionListIndex(currentRoundQuestionListIndex);
    }

    private static void setQuestionUsingQuestionsList() {
        int questionIndex = currentRoundQuestionsList.get(currentRoundQuestionListIndex);
        setQuestion(questionIndex);
    }

    public static void readQuestionsDataFromStorage() {
        questionLanguage = Language.valueOf(AppStorageManager.getInstance().getQuestionLanguage());
        questionTopic = Topic.valueOf(AppStorageManager.getInstance().getQuestionTopic());
        questionText = AppStorageManager.getInstance().getQuestionText();
        currentRoundQuestionsList = AppStorageManager.getInstance().getCurrentRoundQuestionsList();
        previousRoundQuestionsList = AppStorageManager.getInstance().getPreviousRoundQuestionsList();
        currentRoundQuestionListIndex = AppStorageManager.getInstance().getCurrentRoundQuestionListIndex();
    }

    public static void storeQuestionsData() {
        AppStorageManager.getInstance().storeQuestionLanguage(questionLanguage.name());
        AppStorageManager.getInstance().storeQuestionTopic(questionTopic.name());
        AppStorageManager.getInstance().storeQuestionText(questionText);
        AppStorageManager.getInstance().storeCurrentRoundQuestionsList(currentRoundQuestionsList);
        AppStorageManager.getInstance().storePreviousRoundQuestionsList(previousRoundQuestionsList);
        AppStorageManager.getInstance().storeCurrentRoundQuestionListIndex(currentRoundQuestionListIndex);
    }

    public static void clearHistory() {
        clearPreviousRoundQuestionsList();
        clearCurrentRoundQuestionsList();
        clearAllPreviousQuestionsList();
    }

    public static List<Integer> getCurrentRoundQuestionsList() {
        return currentRoundQuestionsList;
    }

    public static void clearCurrentRoundQuestionsList() {
        if (currentRoundQuestionsList != null) {
            currentRoundQuestionsList.clear();
        }
    }

    public static void clearPreviousRoundQuestionsList() {
        if (previousRoundQuestionsList != null) {
            previousRoundQuestionsList.clear();
        }
    }

    public static List<Integer> getPreviousRoundQuestionsList() {
        return previousRoundQuestionsList;
    }

    public static Integer getCurrentRoundQuestionListIndex() {
        return currentRoundQuestionListIndex;
    }

    public static void resetCurrentRoundQuestionListIndex() {
        currentRoundQuestionListIndex = 0;
    }

    public static void resetQuestionLanguage() {
        questionLanguage = initialQuestionLanguage;
    }

    public static void resetQuestionTopic() {
        questionTopic = initialQuestionTopic;
    }

    public static void resetDictionaryMaps() {
        dictionaryMapEn.clear();
        dictionaryMapPl.clear();
    }

    public static void storeSettingsIfMissing() {
        if (AppStorageManager.getInstance().getQuestionLanguage() == null) {
            AppStorageManager.getInstance().storeQuestionLanguage(initialQuestionLanguage.name());
        }
        if (AppStorageManager.getInstance().getQuestionTopic() == null) {
            AppStorageManager.getInstance().storeQuestionTopic(initialQuestionTopic.name());
        }
    }

    public static void setInitialStatesAfterReset() {
        resetQuestionLanguage();
        resetQuestionTopic();
        resetCurrentRoundQuestionListIndex();
    }

    public static void storeSettingsAfterReset() {
        AppStorageManager.getInstance().storeQuestionLanguage(initialQuestionLanguage.name());
        AppStorageManager.getInstance().storeQuestionTopic(initialQuestionTopic.name());
        AppStorageManager.getInstance().storeQuestionText(null);
        AppStorageManager.getInstance().resetCurrentRoundQuestionListIndex();
        AppStorageManager.getInstance().resetCurrentRoundQuestionsList();
        AppStorageManager.getInstance().resetPreviousRoundQuestionsList();
        AppStorageManager.getInstance().storeAPIkey(ExternalDataManager.getAPIkey());
        AppStorageManager.getInstance().storeSpreadsheetId(ExternalDataManager.getSpreadsheetId());
    }

    public static void storeSettingsAfterChangedDataSourceIds() {
        AppStorageManager.getInstance().storeQuestionText(null);
        AppStorageManager.getInstance().resetCurrentRoundQuestionListIndex();
        AppStorageManager.getInstance().resetCurrentRoundQuestionsList();
        AppStorageManager.getInstance().resetPreviousRoundQuestionsList();
    }

    public static boolean areDictionariesFilled() {
        return !dictionaryMapEn.isEmpty() && !dictionaryMapPl.isEmpty();
    }

    public static boolean isWordInStorage() {
        readQuestionsDataFromStorage();
        return questionText != null && !questionText.isEmpty();
    }
}