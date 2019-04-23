package com.bombbird.terminalcontrol.entities.procedures;

import com.bombbird.terminalcontrol.TerminalControl;

import java.util.HashMap;

public class FlyOverPts {
    /** Loads the fly over points */
    public static void loadPoints() {
        HashMap<String, String[]> flyOverWpts = new HashMap<String, String[]>();

        flyOverWpts.put("RCTP", new String[] {"TP050", "TP060", "TP064", "TP230", "TP240", "OCTAN", "NEPAS", "MUKKA", "SITZE", "BITAN", "SULIN"});
        flyOverWpts.put("RJTT", new String[] {"TT501", "ASPEN", "BEAMS", "ARIES", "BOXER", "ASTRA"});
        flyOverWpts.put("RJBB", new String[] {"B6R10", "B6R13", "B6R14", "B4L10", "B4R20", "B6L20"});
        flyOverWpts.put("VHHH", new String[] {"07LDER", "25RDER", "PORPA", "ROVER", "PRAWN", "MC501"});
        flyOverWpts.put("VTBD", new String[] {"RIVER", "LOUIS", "ROVEN", "VARIS", "EXSON"});

        if (!flyOverWpts.containsKey(TerminalControl.radarScreen.mainName)) return;
        for (String s: flyOverWpts.get(TerminalControl.radarScreen.mainName)) {
            TerminalControl.radarScreen.waypoints.get(s).setFlyOver(true);
        }
    }
}
