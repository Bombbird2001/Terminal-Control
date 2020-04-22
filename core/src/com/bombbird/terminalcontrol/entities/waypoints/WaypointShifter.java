package com.bombbird.terminalcontrol.entities.waypoints;

import java.util.HashMap;

public class WaypointShifter {
    public static final HashMap<String, HashMap<String, int[]>> movementData = new HashMap<>();

    public static void loadData() {
        if (movementData.size() > 0) return;
        //TCTP - No adjustments needed
        //TCWS - No adjustments needed
        //TCTT
        putData("TCTT", 32, 0, "LOBBI", "ABSON");
        putData("TCTT", 20, 0, "SCODI");
        putData("TCTT", 0, -128, "BOMDA", "BALAD", "ADMON");
        putData("TCTT", 16, 0, "COCOA", "MELAC", "COALL");
        putData("TCTT", -64, -112, "GHOUL", "RALON");
        putData("TCTT", 48, 0, "CLOWN", "SMITE", "EMMMA");
        //TCHH
        putData("TCHH", 20, 0, "MUMMY", "CLIPP");
        //TCBB
        //TCBD
        //TCMD
        //TCPG
        //TCHX
    }

    private static void putData(String icao, int xShift, int yShift, String... wpts) {
        if (!movementData.containsKey(icao)) movementData.put(icao, new HashMap<>());
        for (String wpt: wpts) {
            movementData.get(icao).put(wpt, new int[] {xShift, yShift});
        }
    }
}
