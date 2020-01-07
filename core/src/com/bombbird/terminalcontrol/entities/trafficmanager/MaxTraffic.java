package com.bombbird.terminalcontrol.entities.trafficmanager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class MaxTraffic {
    private static final HashMap<String, Float> maxTraffic = new HashMap<>();
    private static final HashMap<String, int[][]> nightTime = new HashMap<>();
    private static final HashMap<String, Float> nightMaxTraffic = new HashMap<>();

    public static void loadHashmaps() {
        maxTraffic.put("TCTP", 20f);
        maxTraffic.put("TCWS", 16f);
        maxTraffic.put("TCTT", 26f);
        maxTraffic.put("TCBB", 18f);
        maxTraffic.put("TCHH", 20f);
        maxTraffic.put("TCBD", 20f);
        maxTraffic.put("TCMD", 22f);
        maxTraffic.put("TCPG", 24f);

        nightTime.put("TCTT", new int[][] {
            {2300, 2400},
            {0, 600}
        });
        nightTime.put("TCBB", new int[][] {
                {2230, 2400},
                {0, 615}
        });
        nightTime.put("TCHH", new int[][] {
                {2300, 2400},
                {0, 700}
        });
        nightTime.put("TCMD", new int[][] {
                {2300, 2400},
                {0, 700}
        });

        nightMaxTraffic.put("TCTT", 6f);
        nightMaxTraffic.put("TCBB", 4f);
        nightMaxTraffic.put("TCHH", 14f);
        nightMaxTraffic.put("TCMD", 14f);
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
