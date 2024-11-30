package com.adsolutions.englishapp;

import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AppStorageHelpers {

    public static String convertToListAsStringOnSave(List<Integer> listInt) {
        if(listInt == null || listInt.isEmpty()) {
            return null;
        }
        return listInt.stream().map(String::valueOf).collect(Collectors.joining(";"));
    }

    public static List<Integer> convertToListFromStringOnRead(String intListAsString) {
        if(intListAsString == null || intListAsString.isEmpty()) {
            return null;
        }
        return Arrays.stream(intListAsString.split(";")).map(Integer::parseInt).collect(Collectors.toList());
    }

    public static void storeKeyAsString(SharedPreferences sharedPreferences, String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}