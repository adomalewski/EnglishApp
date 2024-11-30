package com.adsolutions.englishapp;

import java.util.ArrayList;
import java.util.List;

public class DrawQuestionHelper {

    public static boolean checkIfOneOfLastElementsOfListContainsIntValue(List<Integer> list, int distance, int value) {
        int maxDistance = Math.min(list.size(), distance);
        for(int i = 1; i <= maxDistance; i++) {
            if(list.get(list.size() - i).equals(value))
                return true;
        }
        return false;
    }

    public static List<Integer> populateAllIndices(int size) {
        List<Integer> indices = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            indices.add(i);
        }
        return indices;
    }
}