package com.bombbird.terminalcontrol.entities.waypoints;

import java.util.HashMap;

public class WaypointShifter {
    public static final HashMap<String, HashMap<String, int[]>> movementData = new HashMap<>();

    public static void loadData() {
        if (movementData.size() > 0) return;
        //TCTP
        putData("TCTP", -64, -8, "AMPOR");
        //TCWS - No adjustments needed
        //TCTT
        putData("TCTT", 32, 0, "LOBBI", "ABSON");
        putData("TCTT", 20, 0, "SCODI");
        putData("TCTT", 0, -96, "BOMDA", "BALAD", "ADMON");
        putData("TCTT", 16, 0, "COCOA", "MELAC", "COALL");
        putData("TCTT", -64, -64, "GHOUL", "RALON");
        putData("TCTT", 48, 0, "CLOWN", "SMITE", "EMMMA");
        putData("TCTT", -88, -64, "RAZOR");
        putData("TCTT", -80, -80, "VALON", "LALLI");
        //TCHH
        putData("TCHH", 20, 0, "MUMMY", "CLIPP");
        //TCBB
        putData("TCBB", -20, 0, "BANDE");
        putData("TCBB", 20, 0, "CLIPS");
        putData("TCBB", 96, -24, "PONCH");
        putData("TCBB", 64, -112, "ERIGE", "GROND");
        //TCBD - No adjustments needed
        //TCMD
        putData("TCMD", 32, -128, "DPT");
        putData("TCMD", -20, 0, "TENSI");
        putData("TCMD", 88, -64, "ABSIT", "GLADI");
        putData("TCMD", 20, 0, "VERDE");
        putData("TCMD", -88, -64, "EMDOT", "PEGAS");
        //TCPG - No adjustments needed
        //TCHX
        putData("TCHX", -8, 0, "CS");
        putData("TCHX", 8, 0, "WR");
    }

    private static void putData(String icao, int xShift, int yShift, String... wpts) {
        if (!movementData.containsKey(icao)) movementData.put(icao, new HashMap<>());
        for (String wpt: wpts) {
            movementData.get(icao).put(wpt, new int[] {xShift, yShift});
        }
    }
}
