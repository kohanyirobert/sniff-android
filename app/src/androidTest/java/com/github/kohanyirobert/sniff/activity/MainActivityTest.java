package com.github.kohanyirobert.sniff.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import com.github.kohanyirobert.sniff.R;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasFocus;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_KEY;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_URL;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.allOf;

@RunWith(AndroidJUnit4.class)
@RequiresDevice
public class MainActivityTest {

    private static final String TEST_ARTIST = "test artist";
    private static final String TEST_TITLE = "test title";
    private static final String TEST_VIDEO_TITLE = format("%s - %s", TEST_ARTIST, TEST_TITLE);

    private static final String EXTRA_TEXT = "https://youtu.be/test";
    private static final String EXTRA_SUBJECT = format("Watch \"%s\" on YouTube", TEST_VIDEO_TITLE);

    private static final String SETTING_API_URL = "https://test.api/url";
    private static final String SETTING_API_KEY = "test-api-key";

    public static Intent newIntent(String text, String subject) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        Assert.assertNull(intent.getCategories());
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        return intent;
    }

    public static void clearPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        Assert.assertTrue(editor.commit());
    }

    public static void setPreferences(String apiUrl, String apiKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString(API_URL, apiUrl);
        editor.putString(API_KEY, apiKey);
        Assert.assertTrue(editor.commit());
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Test
    public void test_apiUrlOrKeyNotSet_displaySettingsFragment() throws Exception {
        clearPreferences();
        mActivityRule.launchActivity(newIntent(EXTRA_TEXT, EXTRA_SUBJECT));

        onView(withText(R.string.api_settings))
                .check((matches(isDisplayed())));

        onView(withText(R.string.api_url))
                .check((matches(isDisplayed())))
                .perform(click());

        onView(allOf(hasFocus(), isAssignableFrom(EditText.class)))
                .check(matches(withText("")));

        onView(withText(R.string.api_url))
                .perform(pressBack())
                .perform(pressBack());

        onView(withText(R.string.api_key))
                .check((matches(isDisplayed())))
                .perform(click());

        onView(allOf(hasFocus(), isAssignableFrom(EditText.class)))
                .check(matches(withText("")));

        onView(withText(R.string.api_key))
                .perform(pressBack())
                .perform(pressBack());
    }

    @Test
    public void test_apiUrlAndKeySet_displayMainFragment() throws Exception {
        setPreferences(SETTING_API_URL, SETTING_API_KEY);
        mActivityRule.launchActivity(newIntent(EXTRA_TEXT, EXTRA_SUBJECT));

        onView(withId(R.id.coordinator_layout_main))
                .check(matches(isDisplayed()));
    }

    @Test
    public void test_textAndSubjectCorrect() throws Exception {
        setPreferences(SETTING_API_URL, SETTING_API_KEY);
        mActivityRule.launchActivity(newIntent(EXTRA_TEXT, EXTRA_SUBJECT));

        onView(withId(R.id.text_view_video_title))
                .check(matches(withText(TEST_VIDEO_TITLE)));

        onView(withId(R.id.edit_text_artist))
                .check(matches(withText(TEST_ARTIST)));

        onView(withId(R.id.edit_text_title))
                .check(matches(withText(TEST_TITLE)));
    }
}
