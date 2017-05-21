package com.github.kohanyirobert.sniff.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_KEY;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_URL;

public final class MainActivityTestRule extends ActivityTestRule<MainActivity> {

    protected String mExtraText;
    protected String mExtraSubject;
    protected String mSettingApiUrl;
    protected String mSettingApiKey;

    public MainActivityTestRule(String extraText, String extraSubject) {
        this(extraText, extraSubject, null, null);
    }

    public MainActivityTestRule(String extraText, String extraSubject, String settingApiUrl, String settingApiKey) {
        super(MainActivity.class);
        this.mExtraText = extraText;
        this.mExtraSubject = extraSubject;
        this.mSettingApiUrl = settingApiUrl;
        this.mSettingApiKey = settingApiKey;
    }

    @Override
    protected Intent getActivityIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if (intent.getCategories() == null
                || intent.getCategories().isEmpty()
                || intent.getCategories().contains(Intent.CATEGORY_DEFAULT)) {
            intent.addCategory(Intent.CATEGORY_DEFAULT);
        }
        intent.putExtra(Intent.EXTRA_TEXT, mExtraText);
        intent.putExtra(Intent.EXTRA_SUBJECT, mExtraSubject);
        return intent;
    }

    @Override
    protected void beforeActivityLaunched() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        if (mSettingApiUrl != null) {
            editor.putString(API_URL, mSettingApiUrl);
        }
        if (mSettingApiKey != null) {
            editor.putString(API_KEY, mSettingApiKey);
        }
        editor.apply();
    }
}
