package com.github.kohanyirobert.sniff;

import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_KEY;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_URL;

public class SniffInstrumentation extends Instrumentation {

    private String mApiUrl;
    private String mApiKey;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mApiUrl = arguments.getString(API_URL, null);
        mApiKey = arguments.getString(API_KEY, null);
        start();
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getTargetContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(API_URL, mApiUrl);
        editor.putString(API_KEY, mApiKey);
        editor.apply();
    }
}
