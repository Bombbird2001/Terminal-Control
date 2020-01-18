package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.bombbird.terminalcontrol.screens.RadarScreen;

import java.util.Calendar;
import java.util.TimeZone;

public class DayNightManager {
    private RadarScreen radarScreen;

    public DayNightManager(RadarScreen radarScreen) {
        this.radarScreen = radarScreen;
    }

    public boolean checkNoiseAllowed(boolean night) {
        if (!radarScreen.allowNight && night) return false;
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
}
