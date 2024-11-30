package com.adsolutions.englishapp;

public class AppStateManager {

    private static boolean preOpenAnotherActivity = false;

    private static boolean afterResetStorage = false;
    private static boolean afterChangedDataSourceIds = false;

    public static boolean isNotPreOpenAnotherActivity() {
        return !preOpenAnotherActivity;
    }

    public static boolean unsetPreOpenAnotherActivity() {
        return preOpenAnotherActivity = false;
    }

    public static boolean setPreOpenAnotherActivity() {
        return preOpenAnotherActivity = true;
    }

    public static boolean isAfterResetStorage() {
        return afterResetStorage;
    }

    public static boolean unsetAfterResetStorage() {
        return afterResetStorage = false;
    }

    public static boolean setAfterResetStorage() {
        return afterResetStorage = true;
    }

    public static boolean isAfterChangedDataSourceIds() {
        return afterChangedDataSourceIds;
    }

    public static boolean unsetAfterChangedDataSourceIds() {
        return afterChangedDataSourceIds = false;
    }

    public static boolean setAfterChangedDataSourceIds() {
        return afterChangedDataSourceIds = true;
    }
}