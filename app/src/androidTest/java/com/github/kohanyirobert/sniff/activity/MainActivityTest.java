package com.github.kohanyirobert.sniff.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.Surface;
import android.view.View;

import com.github.kohanyirobert.sniff.R;
import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import androidx.test.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_KEY;
import static com.github.kohanyirobert.sniff.fragment.SettingsFragment.API_URL;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MainActivityTest {

    private static final String ARTIST = "test artist";
    private static final String TITLE = "test title";

    private static final String VIDEO_TITLE_WITH_DASH = format("%s - %s", ARTIST, TITLE);
    private static final String VIDEO_TITLE_WITHOUT_DASH = format("%s %s", ARTIST, TITLE);

    private static final String TEXT = "https://youtu.be/ZeJr9a21asI";

    private static final String SUBJECT_FORMAT = "Watch \"%s\" on YouTube";
    private static final String SUBJECT_WITH_DASH = format(SUBJECT_FORMAT, VIDEO_TITLE_WITH_DASH);
    private static final String SUBJECT_WITHOUT_DASH = format(SUBJECT_FORMAT, VIDEO_TITLE_WITHOUT_DASH);

    private static final String BAD_SUBJECT_FORMAT = "Watch %s on YouTube";
    private static final String BAD_SUBJECT_WITH_DASH = format(BAD_SUBJECT_FORMAT, VIDEO_TITLE_WITH_DASH);

    private static final String VALID_API_URL = "https://4pk2ep3glo.execute-api.us-east-1.amazonaws.com/test";
    private static final String VALID_API_KEY = "VzXyJ3VjMazRYhuCZGFTFhmxhEqNWQAxr9babARi";

    private static final String INVALID_API_URL = "https://1234.execute-api.us-dog-2.amazonaws.biz/test";
    private static final String INVALID_API_KEY = "Cr9sxU1";

    public static Matcher<View> hasTextInputLayoutErrorText(final int resourceId) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }

                CharSequence error = ((TextInputLayout) view).getError();
                if (error == null) {
                    return false;
                }
                return InstrumentationRegistry.getTargetContext().getResources().getString(resourceId).equals(error.toString());
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    public static Intent newIntent(String text, String subject) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        assertNull(intent.getCategories());
        intent.setType("plain/text");
        intent.putExtra(MainActivity.EXTRA_MODE, MainActivityMode.TEST.name());
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        return intent;
    }

    public static void clearPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        assertTrue(editor.commit());
    }

    public static void setPreferences(String apiUrl, String apiKey) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(InstrumentationRegistry.getTargetContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString(API_URL, apiUrl);
        editor.putString(API_KEY, apiKey);
        assertTrue(editor.commit());
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class, true, false);

    private UiDevice mDevice;

    @Before
    public void before() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void testRotation_whenRotated_shouldRotate() throws Exception {
        try {
            mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

            setOrientationNaturalAndWait();
            assertEquals(Surface.ROTATION_0, mDevice.getDisplayRotation());

            setOrientationLeftAndWait();
            assertEquals(Surface.ROTATION_90, mDevice.getDisplayRotation());
        } finally {
            mDevice.setOrientationNatural();
            mDevice.unfreezeRotation();
        }
    }

    private void setOrientationNaturalAndWait() throws RemoteException {
        mDevice.setOrientationNatural();
        while (Surface.ROTATION_0 != mDevice.getDisplayRotation()) {
            try {
                TimeUnit.MILLISECONDS.sleep(10L);
            } catch (InterruptedException e) {
            }
        }
    }

    @Test
    public void testVideoTitle_whenSubjectInvalid_shouldDisplaySubjectUntouched() {
        mActivityRule.launchActivity(newIntent(TEXT, BAD_SUBJECT_WITH_DASH));

        onView(withId(R.id.text_view_video_title))
                .check(matches(withText(BAD_SUBJECT_WITH_DASH)));
    }

    @Test
    public void testVideoTitle_whenSubjectValid_shouldDisplayVideoTitle() {
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.text_view_video_title))
                .check(matches(withText(VIDEO_TITLE_WITH_DASH)));
    }

    @Test
    public void testArtist_whenVideoTitleWithoutDash_shouldDisplayNoArtistOrTitle() {
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITHOUT_DASH));

        onView(withId(R.id.edit_text_artist))
                .check(matches(withText("")));

        onView(withId(R.id.edit_text_title))
                .check(matches(withText("")));

        onView(withId(R.id.input_layout_artist))
                .check(matches(hasTextInputLayoutErrorText(R.string.required_artist)));

        onView(withId(R.id.input_layout_title))
                .check(matches(hasTextInputLayoutErrorText(R.string.required_title)));
    }

    @Test
    public void testArtist_whenVideoTitleWithDash_shouldDisplayArtistOrTitle() {
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.edit_text_artist))
                .check(matches(withText(ARTIST)));

        onView(withId(R.id.edit_text_title))
                .check(matches(withText(TITLE)));
    }

    @Test
    public void testArtistAndTitle_whenRotated_shouldRetainEditedArtistAndTitle() throws RemoteException {
        try {
            String newArtist = "my artist";
            String newTitle = "my title";

            mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

            setOrientationNaturalAndWait();

            onView(withId(R.id.edit_text_artist))
                    .perform(clearText(), typeText(newArtist), closeSoftKeyboard());

            onView(withId(R.id.edit_text_title))
                    .perform(clearText(), typeText(newTitle), closeSoftKeyboard());

            setOrientationLeftAndWait();

            onView(withId(R.id.edit_text_artist))
                    .check(matches(withText(newArtist)));

            onView(withId(R.id.edit_text_title))
                    .check(matches(withText(newTitle)));
        } finally {
            mDevice.setOrientationNatural();
            mDevice.unfreezeRotation();
        }
    }

    private void setOrientationLeftAndWait() throws RemoteException {
        mDevice.setOrientationLeft();
        while (Surface.ROTATION_90 != mDevice.getDisplayRotation()) {
            try {
                TimeUnit.MILLISECONDS.sleep(10L);
            } catch (InterruptedException e) {
            }
        }
    }

    @Test
    public void testSend_whenApiUrlAndKeyMissing_shouldNotifyUser() throws Exception {
        setPreferences(null, null);
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.floating_button_send))
                .perform(click());

        onView(allOf(withId(R.id.snackbar_text)))
                .check(matches(withText(R.string.missing_api_url_and_api_key)));
    }

    @Test
    public void testSend_whenApiUrlMissing_shouldNotifyUser() throws Exception {
        setPreferences(null, VALID_API_KEY);
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.floating_button_send))
                .perform(click());

        onView(allOf(withId(R.id.snackbar_text), withText(R.string.missing_api_url)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSend_whenApiKeyMissing_shouldNotifyUser() throws Exception {
        setPreferences(VALID_API_URL, null);
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.floating_button_send))
                .perform(click());

        onView(allOf(withId(R.id.snackbar_text), withText(R.string.missing_api_key)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSend_whenApiUrlAndKeyInvalid_shouldFail() throws Exception {
        setPreferences(INVALID_API_URL, INVALID_API_KEY);
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.floating_button_send))
                .perform(click());

        onView(allOf(withId(R.id.snackbar_text), withText(R.string.invalid_api_url_and_api_key)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSend_whenApiUrlInvalid_shouldFail() throws Exception {
        setPreferences(INVALID_API_URL, VALID_API_KEY);
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.floating_button_send))
                .perform(click());

        onView(allOf(withId(R.id.snackbar_text), withText(R.string.invalid_api_url)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSend_whenApiKeyInvalid_shouldFail() throws Exception {
        setPreferences(VALID_API_URL, INVALID_API_KEY);
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.floating_button_send))
                .perform(click());

        onView(allOf(withId(R.id.snackbar_text), withText(R.string.invalid_api_key)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSend_whenParamsValid_shouldWork() throws Exception {
        setPreferences(VALID_API_URL, VALID_API_KEY);
        mActivityRule.launchActivity(newIntent(TEXT, SUBJECT_WITH_DASH));

        onView(withId(R.id.text_view_video_title))
                .check(matches(withText(VIDEO_TITLE_WITH_DASH)));

        onView(withId(R.id.edit_text_artist))
                .check(matches(withText(ARTIST)));

        onView(withId(R.id.edit_text_title))
                .check(matches(withText(TITLE)));

        onView(withId(R.id.floating_button_send))
                .perform(click());

        onView(allOf(withId(R.id.snackbar_text), withText(R.string.send_request_success)))
                .check(matches(isDisplayed()));
    }
}
