package com.adsolutions.englishapp.enums;

import java.util.Arrays;

public enum SettingsTab {
    TOPIC(0),
    DATA_SOURCE(1),
    STORAGE(2),
    MONITORING(3);

    public final int value;

    private SettingsTab(int _value) {
        value = _value;
    }

    public static SettingsTab from(int value) {
        return Arrays.stream(SettingsTab.values())
                .filter(t -> t.value == value).findFirst().orElseThrow();
    }
}