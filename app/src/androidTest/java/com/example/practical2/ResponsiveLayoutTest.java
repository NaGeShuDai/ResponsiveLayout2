package com.example.practical2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.res.Configuration;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ResponsiveLayoutTest {
    private static final int[] CONTENT_IDS = {
            R.id.lab_title, R.id.layout_title, R.id.word_this, R.id.word_is,
            R.id.word_my, R.id.word_first, R.id.application_title,
            R.id.change_button, R.id.cancel_button
    };

    private void assertLayout(int width, int height, int smallestWidth,
                              String expectedTag, int expectedOrientation) {
        Context app = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Configuration config = new Configuration(app.getResources().getConfiguration());
        config.screenWidthDp = width;
        config.screenHeightDp = height;
        config.smallestScreenWidthDp = smallestWidth;
        config.orientation = width > height
                ? Configuration.ORIENTATION_LANDSCAPE : Configuration.ORIENTATION_PORTRAIT;
        Context context = new ContextThemeWrapper(app.createConfigurationContext(config),
                R.style.Theme_Practical2);
        LinearLayout root = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.activity_main, null, false);
        assertEquals(expectedTag, root.getTag());
        assertEquals(expectedOrientation, root.getOrientation());
        for (int id : CONTENT_IDS) {
            assertNotNull("Missing view: " + id, root.findViewById(id));
        }
    }

    @Test public void phonePortraitUsesDefault() {
        assertLayout(360, 640, 360, "phone_portrait", LinearLayout.VERTICAL);
    }

    @Test public void phoneLandscapeUsesLand() {
        assertLayout(640, 360, 360, "phone_landscape", LinearLayout.HORIZONTAL);
    }

    @Test public void belowTabletThresholdRemainsPhone() {
        assertLayout(599, 960, 599, "phone_portrait", LinearLayout.VERTICAL);
    }

    @Test public void tabletPortraitUsesSmallestWidth() {
        assertLayout(600, 960, 600, "tablet_sw600dp", LinearLayout.HORIZONTAL);
    }

    @Test public void tabletLandscapePrefersSmallestWidthOverLand() {
        assertLayout(960, 600, 600, "tablet_sw600dp", LinearLayout.HORIZONTAL);
    }

    @Test public void runningActivityShowsAllContentWithinWindow() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            androidx.test.espresso.Espresso.onView(
                    androidx.test.espresso.matcher.ViewMatchers.withId(R.id.main)).check(
                    androidx.test.espresso.assertion.ViewAssertions.matches(
                            androidx.test.espresso.matcher.ViewMatchers.isDisplayed()));
            scenario.onActivity(activity -> {
                View root = activity.findViewById(R.id.main);
                Configuration config = activity.getResources().getConfiguration();
                String expectedOrientation = InstrumentationRegistry.getArguments()
                        .getString("expectedOrientation");
                if (expectedOrientation != null) {
                    assertEquals("Device must actually rotate",
                            "landscape".equals(expectedOrientation)
                                    ? Configuration.ORIENTATION_LANDSCAPE
                                    : Configuration.ORIENTATION_PORTRAIT,
                            config.orientation);
                }
                String expectedDevice = InstrumentationRegistry.getArguments()
                        .getString("expectedDevice");
                if (expectedDevice != null) {
                    assertEquals("Device must use the requested screen class",
                            "tablet".equals(expectedDevice), config.smallestScreenWidthDp >= 600);
                }
                String expected = config.smallestScreenWidthDp >= 600 ? "tablet_sw600dp"
                        : config.orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? "phone_landscape" : "phone_portrait";
                assertEquals(expected, root.getTag());
                int[] rootLocation = new int[2];
                root.getLocationOnScreen(rootLocation);
                for (int id : CONTENT_IDS) {
                    View view = activity.findViewById(id);
                    assertTrue("View is visible: " + id, view.isShown());
                    assertTrue("View has area: " + id, view.getWidth() > 0 && view.getHeight() > 0);
                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    assertTrue("View stays inside left edge: " + id, location[0] >= rootLocation[0]);
                    assertTrue("View stays inside top edge: " + id, location[1] >= rootLocation[1]);
                    assertTrue("View stays inside right edge: " + id,
                            location[0] + view.getWidth() <= rootLocation[0] + root.getWidth());
                    assertTrue("View stays inside bottom edge: " + id,
                            location[1] + view.getHeight() <= rootLocation[1] + root.getHeight());
                }
            });
        }
    }
}
