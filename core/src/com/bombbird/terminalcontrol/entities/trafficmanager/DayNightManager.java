package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.RadarScreen;

import java.util.Calendar;
import java.util.TimeZone;

public class DayNightManager {
    public static final Array<String> NIGHT_AVAILABLE = new Array<>();

    private static void loadArray() {
        NIGHT_AVAILABLE.add("TCTT", "TCHH", "TCBB", "TCMD");
    }

    public static boolean checkNoiseAllowed(boolean night) {
        RadarScreen radarScreen = TerminalControl.radarScreen;
        if (!radarScreen.allowNight) return !night;
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int additional = calendar.get(Calendar.AM_PM) == Calendar.PM ? 12 : 0;
        int time = (calendar.get(Calendar.HOUR) + additional) * 100 + calendar.get(Calendar.MINUTE);
        if (radarScreen.nightEnd < radarScreen.nightStart) {
            //Cross midnight, additional step needed
            if (time >= radarScreen.nightStart || time < radarScreen.nightEnd) {
                return night; //True if is night and star is night
            } else {
                return !night; //True if is day and star is day
            }
        } else {
            if (time >= radarScreen.nightStart && time < radarScreen.nightEnd) {
                return night; //True if is night and star is night
            } else {
                return !night; //True if is day and star is day
            }
        }
    }

    public static boolean isNightAvailable() {
        if (NIGHT_AVAILABLE.size == 0) loadArray();
        if (TerminalControl.radarScreen == null) return false;
        return NIGHT_AVAILABLE.contains(TerminalControl.radarScreen.mainName, false);
    }
}
