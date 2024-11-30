package com.adsolutions.englishapp;

import static com.adsolutions.englishapp.QuestionManager.getCurrentRoundQuestionListIndex;
import static com.adsolutions.englishapp.QuestionManager.getCurrentRoundQuestionsList;
import static com.adsolutions.englishapp.QuestionManager.getPreviousRoundQuestionsList;
import static com.adsolutions.englishapp.QuestionManager.getQuestionText;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class QuestionMonitoring {

    private static final int WORDS_HISTORY_LIMIT = 3000;

    private static List<String> allPreviousQuestionsList = new LinkedList<>();

    public static LinkedHashMap<Integer, Map<String, Integer>> getCurrentMonitoringTableData() {
        LinkedHashMap<Integer, Map<String, Integer>> monitoringTable = new LinkedHashMap<>();

        if (getCurrentRoundQuestionsList() == null || getCurrentRoundQuestionsList().isEmpty()) {
            return monitoringTable;
        }

        getCurrentRoundQuestionsList().forEach(questionIndex -> {
            Map<String, Integer> questionDetails = new LinkedHashMap<>();
            String questionText = getQuestionText(questionIndex);

            int otherWordsOccurToLastSeen = 0;
            int currListPos = getCurrentRoundQuestionsList().indexOf(questionIndex);

            if (currListPos > getCurrentRoundQuestionListIndex()) {
                if (getPreviousRoundQuestionsList() != null && !getPreviousRoundQuestionsList().isEmpty()) {
                    int prevListPos = getPreviousRoundQuestionsList().indexOf(questionIndex);
                    if (prevListPos != -1) {
                        otherWordsOccurToLastSeen += getPreviousRoundQuestionsList().size() - prevListPos - 1;
                    }
                }
                otherWordsOccurToLastSeen += getCurrentRoundQuestionListIndex() + 1;
            } else if (currListPos < getCurrentRoundQuestionListIndex()) {
                otherWordsOccurToLastSeen = getCurrentRoundQuestionListIndex() - currListPos;
            }

            questionDetails.put(questionText, otherWordsOccurToLastSeen);
            monitoringTable.put(questionIndex, questionDetails);
        });

        return sortTableData(monitoringTable);
    }

    private static LinkedHashMap<Integer, Map<String, Integer>> sortTableData(Map<Integer, Map<String, Integer>> table) {
        Comparator<Integer> compareByNumber = (Integer a, Integer b) -> {
            Function<Integer, Integer> getValueForRow = (Integer row) ->
                    new ArrayList<>(Objects.requireNonNull(table.get(row)).values()).get(0);

            if (getValueForRow.apply(a) >= getValueForRow.apply(b)) {
                return -1;
            } else if (Objects.equals(getValueForRow.apply(a), getValueForRow.apply(b))) {
                return 0;
            } else {
                return 1;
            }
        };

        return table.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(compareByNumber))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static void addCurrentTextToAllPreviousQuestionsList() {
        allPreviousQuestionsList.add(getQuestionText());
    }

    private static void shortenAllPreviousQuestionsList() {
        if (allPreviousQuestionsList == null || allPreviousQuestionsList.size() <= WORDS_HISTORY_LIMIT) {
            return;
        }

        allPreviousQuestionsList.subList(WORDS_HISTORY_LIMIT, allPreviousQuestionsList.size()).clear();
    }

    public static void clearAllPreviousQuestionsList() {
        if (allPreviousQuestionsList != null) {
            allPreviousQuestionsList.clear();
        }
    }

    public static List<String> getDecoratedAllPreviousQuestionsList() {
        shortenAllPreviousQuestionsList();

        List<String> output = new LinkedList<>();
        for (int i = 0; i < allPreviousQuestionsList.size(); i++) {
            output.add(format("%s. %s", i, allPreviousQuestionsList.get(i)));
        }

        return output;
    }

    public static String getDecoratedCurrentRoundQuestionList() {
        if (getCurrentRoundQuestionsList() == null || getCurrentRoundQuestionsList().isEmpty()) {
            return "";
        }

        return getCurrentRoundQuestionsList().stream()
                .map(String::valueOf).collect(Collectors.joining(", "));
    }

    public static String getDecoratedPreviousRoundQuestionList() {
        if (getPreviousRoundQuestionsList() == null || getPreviousRoundQuestionsList().isEmpty()) {
            return "";
        }

        return getPreviousRoundQuestionsList().stream()
                .map(String::valueOf).collect(Collectors.joining(", "));
    }

    public static String getDecoratedCurrentRoundQuestionListIndex() {
        return String.valueOf(getCurrentRoundQuestionListIndex());
    }
}