package com.github.kohanyirobert.sniff.activity;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.github.kohanyirobert.sniff.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static java.lang.String.format;

@RunWith(AndroidJUnit4.class)
@RequiresDevice
public class MainFragmentTest {

    private static final String TEST_ARTIST = "test artist";
    private static final String TEST_TITLE = "test title";
    private static final String TEST_VIDEO_TITLE = format("%s - %s", TEST_ARTIST, TEST_TITLE);

    private static final String EXTRA_TEXT = "https://youtu.be/test";
    private static final String EXTRA_SUBJECT = format("Watch \"%s\" on YouTube", TEST_VIDEO_TITLE);

    private static final String SETTING_API_URL = "https://test.api/url";
    private static final String SETTING_API_KEY = "test-api-key";

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new MainActivityTestRule(
            EXTRA_TEXT,
            EXTRA_SUBJECT,
            SETTING_API_URL,
            SETTING_API_KEY);

    @Test
    public void test_whenCorrectlyInvoked_shouldInitTextFields() throws Exception {
        onView(ViewMatchers.withId(R.id.text_view_video_title))
                .check(matches(withText(TEST_VIDEO_TITLE)));

        onView(withId(R.id.edit_text_artist))
                .check(matches(withText(TEST_ARTIST)));

        onView(withId(R.id.edit_text_title))
                .check(matches(withText(TEST_TITLE)));
    }
}
