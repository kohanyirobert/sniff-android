package com.github.kohanyirobert.sniff.fragment;

import android.os.Bundle;
import android.view.Menu;

import com.github.kohanyirobert.sniff.R;

import androidx.preference.PreferenceFragmentCompat;

public final class SettingsFragment extends PreferenceFragmentCompat {

    public static final String API_URL = "api_url";
    public static final String API_KEY = "api_key";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.fragment_preferences);
    }
}
