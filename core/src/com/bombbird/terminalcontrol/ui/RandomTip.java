package com.bombbird.terminalcontrol.ui;

import com.badlogic.gdx.utils.Array;

public class RandomTip {
    private static final Array<String> tips = new Array<String>();

    /** Loads a list of tips to be shown during loading screen */
    public static void loadTips() {
        tips.add("Double tap the label of a controlled aircraft to minimise/maximise it");
        tips.add("Separation is reduced to 2.5nm for aircrafts on the same ILS both within 10nm from touchdown");
        tips.add("In this game, separation is not required for aircrafts on approach to parallel runways (varies in real life)");
        tips.add("Put aircrafts in holding patterns if the airspace is becoming congested");
        tips.add("Be careful of the MVAs (gray) when assigning headings manually");
        tips.add("MVAs (gray) do not apply to aircrafts following a SID/STAR");
        tips.add("Restricted areas (orange) must be avoided by all aircrafts");
        tips.add("Windshear can cause an aircraft to go around");
        tips.add("Circles on the LDA indicate the step-down points for a non-precision approach");
        tips.add("Some waypoints are fly-over points; aircrafts will fly directly over it before turning");
        tips.add("Some aircraft climb/descend slower than others");
        tips.add("An aircraft will go around if it is too high or too fast");
        tips.add("An aircraft will go around if there is another aircraft on the runway");
        tips.add("A minimum separation of 3nm or 1000 feet must be kept between all aircraft (except in some cases)");
        tips.add("An aircraft will divert if it is low on fuel");
        tips.add("Refer to the AIP of the relevant aviation authorities to learn more about an airport's SID/STARs");
        tips.add("Fly-over waypoints are indicated by a filled circle");
        tips.add("Tap on the METAR label of an airport (top left of screen) to change its runway configuration");
        tips.add("Wake turbulence can cause an aircraft to go around");
        tips.add("Check out the help manual for more detailed descriptions of airports, game mechanics");
        tips.add("Like this game? Please rate it on the Google Play Store!");
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
