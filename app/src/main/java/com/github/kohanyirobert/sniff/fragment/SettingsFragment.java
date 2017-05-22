package com.github.kohanyirobert.sniff.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.github.kohanyirobert.sniff.R;

public final class SettingsFragment extends PreferenceFragmentCompat {

    public static final String API_URL = "api-url";
    public static final String API_KEY = "api-key";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.fragment_preferences);
    }
}
