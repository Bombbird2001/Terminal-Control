package com.bombbird.terminalcontrol.entities.procedures.holding;

import java.util.HashMap;

public class BackupHoldingPoints {
    public static HashMap<String, HoldingPoints> loadBackupPoints(String icao, HashMap<String, HoldingPoints> points) {
        HashMap<String, HoldingPoints> newMap = getHashMap(icao);
        newMap.putAll(points);
        return newMap;
    }

    private static HashMap<String, HoldingPoints> getHashMap(String icao) {
        //Load old holding waypoints before waypoint overhaul
        switch (icao) {
            case "TCTP":
                //TCTP
                HashMap<String, HoldingPoints> tctp = new HashMap<>();
                tctp.put("BRAVO", new HoldingPoints("BRAVO", new int[] {5000, -1}, 230, true, 46, 5));
                tctp.put("JAMMY", new HoldingPoints("JAMMY", new int[] {4000, -1}, 230, true, 68, 5));
                tctp.put("JUNTA", new HoldingPoints("JUNTA", new int[] {4000, -1}, 230, true, 54, 5));
                tctp.put("AUGUR", new HoldingPoints("AUGUR", new int[] {5000, -1}, 230, false, 218, 5));
                tctp.put("SEPIA", new HoldingPoints("SEPIA", new int[] {5000, -1}, 230, false, 234, 5));
                tctp.put("MARCH", new HoldingPoints("MARCH", new int[] {4000, -1}, 230, false, 234, 5));
                return tctp;
            case "TCSS":
                //TCSS
                HashMap<String, HoldingPoints> tcss = new HashMap<>();
                tcss.put("DUPAR", new HoldingPoints("DUPAR", new int[] {5000, -1}, 230, true, 85, 5));
                tcss.put("BESOM", new HoldingPoints("BESOM", new int[] {4500, -1}, 230, false, 289, 5));
                tcss.put("SASHA", new HoldingPoints("SASHA", new int[] {6000, -1}, 230, false, 228, 5));
                tcss.put("ZONLI", new HoldingPoints("ZONLI", new int[] {5000, -1}, 230, false, 51, 5));
                tcss.put("YILAN", new HoldingPoints("YILAN", new int[] {6000, -1}, 230, false, 335, 5));
                tcss.put("PINSI", new HoldingPoints("PINSI", new int[] {5000, -1}, 230, true, 299, 5));
                tcss.put("KUDOS", new HoldingPoints("KUDOS", new int[] {6000, -1}, 230, false, 280, 5));
                tcss.put("FUSIN", new HoldingPoints("FUSIN", new int[] {9000, -1}, 230, false, 70, 5));
                return tcss;
            case "TCWS":
                //TCWS
                HashMap<String, HoldingPoints> tcws = new HashMap<>();
                tcws.put("BOBAG", new HoldingPoints("BOBAG", new int[] {6000, 18000}, 220, false, 82, 5));
                tcws.put("SAMKO", new HoldingPoints("SAMKO", new int[] {4000, 14000}, 220, true, 348, 5));
                tcws.put("NYLON", new HoldingPoints("NYLON", new int[] {3000, 14000}, 220, true, 203, 5));
                tcws.put("LAVAX", new HoldingPoints("LAVAX", new int[] {7000, 14000}, 220, true, 269, 5));
                tcws.put("REMES", new HoldingPoints("REMES", new int[] {6000, 14000}, 220, false, 348, 5));
                return tcws;
            default:
                return new HashMap<>();
        }
    }
}
