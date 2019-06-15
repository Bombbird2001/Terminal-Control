package com.bombbird.terminalcontrol.entities.zones;

import com.badlogic.gdx.utils.Array;

public class ZoneLoader {
    public static Array<ApproachZone> loadApchZones(String icao) {
        Array<ApproachZone> approachZones = new Array<ApproachZone>();
        if ("RJTT".equals(icao)) {
            approachZones = loadApchRJTT();
        } else if ("WSSS".equals(icao)) {
            approachZones = loadApchWSSS();
        }

        return approachZones;
    }

    private static Array<ApproachZone> loadApchRJTT() {
        Array<ApproachZone> approachZones = new Array<ApproachZone>();
        approachZones.add(new ApproachZone("34L", "34R", 2895.4f, 1602.7f, 337, 3.1f, 26, 0.329f));
        approachZones.add(new ApproachZone("22", "23", 2881.8f, 1684.4f, 277, 3.1f, 26, 0.329f));

        return approachZones;
    }

    private static Array<ApproachZone> loadApchWSSS() {
        Array<ApproachZone> approachZones = new Array<ApproachZone>();
        approachZones.add(new ApproachZone("02L", "02C", 2870.0f, 1594.5f, 23, 3.1f, 25, 0.329f));
        approachZones.add(new ApproachZone("20C", "20R", 2885.2f, 1630.7f, 203, 3.1f, 26, 0.329f));

        return approachZones;
    }

    public static Array<DepartureZone> loadDepZones(String icao) {
        Array<DepartureZone> departureZones = new Array<DepartureZone>();
        if ("RJTT".equals(icao)) {
            departureZones = loadDepRJTT();
        } else if ("RJAA".equals(icao)) {
            departureZones = loadDepRJAA();
        }

        return departureZones;
    }

    private static Array<DepartureZone> loadDepRJTT() {
        Array<DepartureZone> departureZones = new Array<DepartureZone>();
        departureZones.add(new DepartureZone("16L", "16R", 2895.4f, 1602.7f, 157, 3.1f, 10, 0.329f));

        return departureZones;
    }

    private static Array<DepartureZone> loadDepRJAA() {
        Array<DepartureZone> departureZones = new Array<DepartureZone>();
        departureZones.add(new DepartureZone("16L", "16R", 3828.3f, 2060.9f, 157, 3.1f, 19, 0.329f));
        departureZones.add(new DepartureZone("34L", "34R", 3828.3f, 2060.9f, 337, 3.1f, 16, 0.329f));

        return departureZones;
    }

    public static Array<AltitudeExclusionZone> loadAltExclZones(String icao) {
        Array<AltitudeExclusionZone> zones = new Array<AltitudeExclusionZone>();
        if ("RJAA".equals(icao)) {
            zones = loadExclRJAA();
        }

        return zones;
    }

    private static Array<AltitudeExclusionZone> loadExclRJAA() {
        Array<AltitudeExclusionZone> zones = new Array<AltitudeExclusionZone>();
        zones.add(new AltitudeExclusionZone(new String[] {"16L", "16R"}, 3828.3f, 2060.9f, 157, 14, 5));

        return zones;
    }
}
