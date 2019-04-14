package com.bombbird.terminalcontrol.entities.procedures;

import java.util.HashMap;

public class FlyOverPts {
    //Fly over waypoints
    public static final HashMap<String, String[]> FLY_OVER_PTS = new HashMap<String, String[]>();

    //Loads the fly over points
    public static void loadPoints() {
        FLY_OVER_PTS.put("RCTP", new String[] {"TP050", "TP060", "TP064", "TP230", "TP240", "OCTAN", "NEPAS"});
        FLY_OVER_PTS.put("RJTT", new String[] {"TT501"});
        FLY_OVER_PTS.put("RJAA", new String[] {"ASPEN", "BEAMS", "ARIES", "BOXER", "ASTRA"});
        FLY_OVER_PTS.put("RJBB", new String[] {"B6R10", "B6R13", "B6R14", "B4L10", "B4R20", "B6L20"});
        FLY_OVER_PTS.put("VHHH", new String[] {"07LDER", "25RDER", "PORPA", "ROVER", "PRAWN"});
        FLY_OVER_PTS.put("VMMC", new String[] {"MC501"});
        FLY_OVER_PTS.put("VTBD", new String[] {"RIVER"});
    }
}
