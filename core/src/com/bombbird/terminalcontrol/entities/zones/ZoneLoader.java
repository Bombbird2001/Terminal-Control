package com.bombbird.terminalcontrol.entities.zones;

import com.badlogic.gdx.utils.Array;

public class ZoneLoader {
    public static Array<ApproachZone> loadApchZones(String icao) {
        Array<ApproachZone> approachZones = new Array<>();
        if ("TCTT".equals(icao)) {
            approachZones = loadApchTCTT();
        } else if ("TCWS".equals(icao)) {
            approachZones = loadApchTCWS();
        } else if ("TCAA".equals(icao)) {
            approachZones = loadApchTCAA();
        }

        return approachZones;
    }

    private static Array<ApproachZone> loadApchTCTT() {
        Array<ApproachZone> approachZones = new Array<>();
        approachZones.add(new ApproachZone("34L", "34R", 2895.4f, 1602.7f, 337, 0.75f, 26, 0.329f));
        approachZones.add(new ApproachZone("22", "23", 2881.8f, 1684.4f, 277, 1.25f, 26, 0.329f));

        return approachZones;
    }

    private static Array<ApproachZone> loadApchTCWS() {
        Array<ApproachZone> approachZones = new Array<>();
        approachZones.add(new ApproachZone("02L", "02C", 2870.0f, 1594.5f, 23, 0.73f, 25, 0.329f));
        approachZones.add(new ApproachZone("20C", "20R", 2886.2f, 1630.7f, 203, 0.73f, 26, 0.329f));

        return approachZones;
    }

    private static Array<ApproachZone> loadApchTCAA() {
        Array<ApproachZone> approachZones = new Array<>();
        approachZones.add(new ApproachZone("34L", "34R", 3863.9f, 2000.4f, 337, 1.18f, 25, 0.329f));
        approachZones.add(new ApproachZone("16L", "16R", 3805.9f, 2098.3f, 157, 1.18f, 25, 0.329f));

        return approachZones;
    }

    public static Array<DepartureZone> loadDepZones(String icao) {
        Array<DepartureZone> departureZones = new Array<>();
        if ("TCTT".equals(icao)) {
            departureZones = loadDepTCTT();
        } else if ("TCAA".equals(icao)) {
            departureZones = loadDepTCAA();
        } else if ("TCMD".equals(icao)) {
            departureZones = loadDepTCMD();
        } else if ("TCPG".equals(icao)) {
            departureZones = loadDepTCPG();
        }

        return departureZones;
    }

    private static Array<DepartureZone> loadDepTCTT() {
        Array<DepartureZone> departureZones = new Array<>();
        departureZones.add(new DepartureZone("16L", "16R", 2875.6f, 1636.8f, 157, 3.1f, 10, 0.329f));

        return departureZones;
    }

    private static Array<DepartureZone> loadDepTCAA() {
        Array<DepartureZone> departureZones = new Array<>();
        departureZones.add(new DepartureZone("16L", "16R", 3805.9f, 2098.3f, 157, 3.1f, 19, 0.329f));
        departureZones.add(new DepartureZone("34L", "34R", 3863.9f, 2000.4f, 337, 3.1f, 16, 0.329f));

        return departureZones;
    }

    private static Array<DepartureZone> loadDepTCMD() {
        Array<DepartureZone> departureZones = new Array<>();
        departureZones.add(new DepartureZone("14L", "14R", 2869.7f, 1653.5f, 143, 3.1f, 16, 0.329f));
        departureZones.add(new DepartureZone("36L", "36R", 2871.3f, 1676.2f, 1, 3.1f, 5, 0.329f));

        return departureZones;
    }

    private static Array<DepartureZone> loadDepTCPG() {
        Array<DepartureZone> departureZones = new Array<>();
        departureZones.add(new DepartureZone("08L", "09R", 2837.8f, 1614.6f, 85, 3.1f, 16, 0.329f));
        departureZones.add(new DepartureZone("26R", "27L", 2956.2f, 1624.9f, 265, 3.1f, 10, 0.329f));

        return departureZones;
    }

    public static Array<AltitudeExclusionZone> loadAltExclZones(String icao) {
        Array<AltitudeExclusionZone> zones = new Array<>();
        if ("TCAA".equals(icao)) {
            zones = loadExclTCAA();
        } else if ("TCBD".equals(icao)) {
            zones = loadExclTCBD();
        } else if ("TCMD".equals(icao)) {
            zones = loadExclTCMD();
        }

        return zones;
    }

    private static Array<AltitudeExclusionZone> loadExclTCAA() {
        Array<AltitudeExclusionZone> zones = new Array<>();
        zones.add(new AltitudeExclusionZone(new String[] {"16L", "16R"}, 3828.3f, 2060.9f, 157, 14, 5));

        return zones;
    }

    private static Array<AltitudeExclusionZone> loadExclTCBD() {
        Array<AltitudeExclusionZone> zones = new Array<>();
        zones.add(new AltitudeExclusionZone(new String[] {"03L"}, 2952.6f, 1595.8f, 30, 12, 7));

        return zones;
    }

    private static Array<AltitudeExclusionZone> loadExclTCMD() {
        Array<AltitudeExclusionZone> zones = new Array<>();
        zones.add(new AltitudeExclusionZone(new String[] {"18L", "18R"}, 2870.9f, 1737.3f, 181, 20, 5));

        return zones;
    }
}
