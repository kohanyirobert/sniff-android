package com.github.kohanyirobert.sniff.activity;

public enum MainActivityMode {

    PRODUCTION,
    DEVELOPMENT,
    TEST;

    public static final MainActivityMode valueOf(String name, MainActivityMode defaultMode) {
        if (name == null) {
            return defaultMode;
        }
        return MainActivityMode.valueOf(name);
    }
}
