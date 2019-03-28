package com.github.kohanyirobert.sniff.fragment;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import android.view.Menu;

import com.github.kohanyirobert.sniff.R;

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
