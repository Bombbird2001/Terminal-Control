package com.bombbird.terminalcontrol.ui;

import com.badlogic.gdx.utils.Array;

public class RandomTip {
    private static final Array<String> tips = new Array<String>();

    /** Loads a list of tips to be shown during loading screen */
    public static void loadTips() {
        tips.add("Double tap the label of a controlled aircraft to minimise/maximise it");
        tips.add("Separation is reduced to 2.5nm for aircrafts on the same ILS both within 10nm from touchdown");
        tips.add("Separation is not required for aircrafts on approach to parallel runways");
        tips.add("Put aircrafts in holding patterns if the airspace is becoming congested");
        tips.add("Be careful of the MVAs (gray) when assigning headings manually");
        tips.add("MVAs (gray) do not apply to aircrafts following a SID/STAR");
        tips.add("Restricted areas (orange) must be avoided by all aircrafts");
        tips.add("Windshear can cause aircrafts to go around");
        tips.add("Circles on the LDA indicate the step-down points for a non-precision approach");
        tips.add("Some waypoints are fly-over points; aircrafts will fly directly over it before turning");
        tips.add("Some aircraft descend/climb slower than others");
        tips.add("An aircraft will go around if it is too high or too fast");
        tips.add("An aircraft will go around if there is another aircraft on the runway");
        tips.add("A minimum separation of 3nm or 1000feet must be kept between all aircraft (except in some cases)");
        tips.add("An aircraft will divert if it is low on fuel");
    }

    /** Checks if the tips array has been loaded */
    public static boolean tipsLoaded() {
        return tips.size > 0;
    }

    /** Returns a random tip to be used */
    public static String randomTip() {
        return tips.random();
    }
}
