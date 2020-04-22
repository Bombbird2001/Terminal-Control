package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;

import java.util.Calendar;
import java.util.TimeZone;

public class DayNightManager {
    public static final Array<String> NIGHT_AVAILABLE = new Array<>();

    private static void loadArray() {
        NIGHT_AVAILABLE.add("TCTT", "TCHH", "TCBB", "TCMD");
    }

    /** Checks whether airport is utilising night operations */
    public static boolean isNight() {
        if (!isNightAvailable() || !TerminalControl.radarScreen.allowNight) return false;
        RadarScreen radarScreen = TerminalControl.radarScreen;
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int additional = calendar.get(Calendar.AM_PM) == Calendar.PM ? 12 : 0;
        int time = (calendar.get(Calendar.HOUR) + additional) * 100 + calendar.get(Calendar.MINUTE);
        if (radarScreen.nightEnd <= radarScreen.nightStart) {
            //Cross midnight
            return time >= radarScreen.nightStart || time < radarScreen.nightEnd;
        } else {
            return time >= radarScreen.nightStart && time < radarScreen.nightEnd;
        }
    }

    /** Checks if a SID/STAR is allowed depending on whether night mode is active */
    public static boolean checkNoiseAllowed(boolean night) {
        if (isNight()) {
            return night;
        } else {
            return !night;
        }
    }

    /** Check whether this airport has night operations available */
    public static boolean isNightAvailable() {
        if (NIGHT_AVAILABLE.size == 0) loadArray();
        if (TerminalControl.radarScreen == null) return false;
        return NIGHT_AVAILABLE.contains(TerminalControl.radarScreen.mainName, false);
    }
}
