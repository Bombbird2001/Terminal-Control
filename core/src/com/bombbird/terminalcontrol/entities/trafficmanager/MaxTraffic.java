package com.bombbird.terminalcontrol.entities.trafficmanager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class MaxTraffic {
    private static final HashMap<String, Float> maxTraffic = new HashMap<>();
    private static final HashMap<String, int[][]> nightTime = new HashMap<>();
    private static final HashMap<String, Float> nightMaxTraffic = new HashMap<>();

    public static void loadHashmaps() {
        maxTraffic.put("RCTP", 20f);
        maxTraffic.put("WSSS", 16f);
        maxTraffic.put("RJTT", 26f);
        maxTraffic.put("RJBB", 18f);
        maxTraffic.put("VHHH", 20f);
        maxTraffic.put("VTBD", 20f);
        maxTraffic.put("LEMD", 22f);

        nightTime.put("RJTT", new int[][] {
            {2300, 2400},
            {0, 600}
        });
        nightTime.put("RJBB", new int[][] {
                {2230, 2400},
                {0, 615}
        });
        nightTime.put("VHHH", new int[][] {
                {2300, 2400},
                {0, 700}
        });
        nightTime.put("LEMD", new int[][] {
                {2300, 2400},
                {0, 700}
        });

        nightMaxTraffic.put("RJTT", 6f);
        nightMaxTraffic.put("RJBB", 4f);
        nightMaxTraffic.put("VHHH", 14f);
        nightMaxTraffic.put("LEMD", 14f);
    }

    /** Checks if night time operations is active for airport */
    public static boolean isNight(String icao) {
        if (!nightTime.containsKey(icao)) return false;

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int additional = calendar.get(Calendar.AM_PM) == Calendar.PM ? 12 : 0;
        int time = (calendar.get(Calendar.HOUR) + additional) * 100 + calendar.get(Calendar.MINUTE);

        for (int[] timeSlot: nightTime.get(icao)) {
            if (time >= timeSlot[0] && time < timeSlot[1]) return true;
        }

        return false;
    }

    public static float getMaxTraffic(String icao) {
        return isNight(icao) ? nightMaxTraffic.get(icao) : maxTraffic.get(icao);
    }
}
