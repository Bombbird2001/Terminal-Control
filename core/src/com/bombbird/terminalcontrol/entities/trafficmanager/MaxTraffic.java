package com.bombbird.terminalcontrol.entities.trafficmanager;

import java.util.HashMap;

public class MaxTraffic {
    private static final HashMap<String, Float> maxTraffic = new HashMap<>();
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

        nightMaxTraffic.put("TCTT", 6f);
        nightMaxTraffic.put("TCBB", 4f);
        nightMaxTraffic.put("TCHH", 14f);
        nightMaxTraffic.put("TCMD", 14f);
    }

    public static float getMaxTraffic(String icao) {
        return DayNightManager.isNight() && nightMaxTraffic.containsKey(icao) ? nightMaxTraffic.get(icao) : maxTraffic.get(icao);
    }
}
