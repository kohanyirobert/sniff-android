package com.github.kohanyirobert.sniff.activity;

import android.content.Intent;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.github.kohanyirobert.sniff.R;
import com.github.kohanyirobert.sniff.activity.MainActivity;
import com.github.kohanyirobert.sniff.activity.MainActivityParameters;

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
public class MainActivityTest {

    private static final String TEST_ID = "YLfkgo-3_sk";

    private static final String TEST_NORMALIZED_URL = format(MainActivityParameters.YOUTUBE_URL_FORMAT, TEST_ID);

    private static final String TEST_ARTIST = "椎名林檎";
    private static final String TEST_TITLE = "長く短い祭 from百鬼夜行";
    private static final String TEST_VIDEO_TITLE = format("%s - %s", TEST_ARTIST, TEST_TITLE);

    private static final String TEST_INTENT_TEXT = format("https://youtu.be/%s", TEST_ID);
    private static final String TEST_INTENT_SUBJECT = format("Watch \"%s\" on YouTube", TEST_VIDEO_TITLE);

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class) {

        @Override
        protected Intent getActivityIntent() {
            Intent intent = super.getActivityIntent();
            intent.setAction(Intent.ACTION_SEND);
            if (intent.getCategories() == null
                    || intent.getCategories().isEmpty()
                    || intent.getCategories().contains(Intent.CATEGORY_DEFAULT)) {
                intent.addCategory(Intent.CATEGORY_DEFAULT);
            }
            intent.putExtra(Intent.EXTRA_TEXT, TEST_INTENT_TEXT);
            intent.putExtra(Intent.EXTRA_SUBJECT, TEST_INTENT_SUBJECT);
            return intent;
        }
    };

    @Test
    public void test1() throws Exception {
        onView(ViewMatchers.withId(R.id.text_view_video_title))
                .check(matches(withText(TEST_VIDEO_TITLE)));

        onView(withId(R.id.edit_text_artist))
                .check(matches(withText(TEST_ARTIST)));

        onView(withId(R.id.edit_text_title))
                .check(matches(withText(TEST_TITLE)));
    }
}
