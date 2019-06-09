package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.utils.Array;

public class ZoneLoader {
    public static Array<ApproachZone> loadZones(String icao) {
        Array<ApproachZone> approachZones = new Array<ApproachZone>();
        if ("RJTT".equals(icao)) {
            approachZones = loadRJTT();
        } else if ("WSSS".equals(icao)) {
            approachZones = loadWSSS();
        }

        return approachZones;
    }

    private static Array<ApproachZone> loadRJTT() {
        Array<ApproachZone> approachZones = new Array<ApproachZone>();
        approachZones.add(new ApproachZone("34L", "34R", 2895.4f, 1602.7f, 337, 3.1f, 26, 0.329f));
        approachZones.add(new ApproachZone("22", "23", 2881.8f, 1684.4f, 277, 3.1f, 26, 0.329f));

        return approachZones;
    }

    private static Array<ApproachZone> loadWSSS() {
        Array<ApproachZone> approachZones = new Array<ApproachZone>();
        approachZones.add(new ApproachZone("02L", "02C", 2870.0f, 1594.5f, 23, 3.1f, 25, 0.329f));
        approachZones.add(new ApproachZone("20C", "20R", 2885.2f, 1630.7f, 203, 3.1f, 26, 0.329f));

        return approachZones;
    }
}
