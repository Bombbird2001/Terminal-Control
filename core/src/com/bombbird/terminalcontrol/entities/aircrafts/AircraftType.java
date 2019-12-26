package com.bombbird.terminalcontrol.entities.aircrafts;

import com.bombbird.terminalcontrol.utilities.saving.FileLoader;

import java.util.HashMap;

public class AircraftType {
    private static final HashMap<String, int[]> aircraftTypes = FileLoader.loadAircraftData();

    public static char getWakeCat(String type) {
        if (!aircraftTypes.containsKey(type)) return 'M';
        return (char) aircraftTypes.get(type)[0];
    }

    public static char getRecat(String type) {
        if (!aircraftTypes.containsKey(type)) return 'D';
        return (char) aircraftTypes.get(type)[5];
    }

    public static int getV2(String type) {
        if (!aircraftTypes.containsKey(type)) return 150;
        return aircraftTypes.get(type)[1];
    }

    public static int getTypClimb(String type) {
        if (!aircraftTypes.containsKey(type)) return 2000;
        return aircraftTypes.get(type)[2];
    }

    public static int getTypDes(String type) {
        if (!aircraftTypes.containsKey(type)) return 2000;
        return aircraftTypes.get(type)[3];
    }

    public static int getApchSpd(String type) {
        if (!aircraftTypes.containsKey(type)) return 150;
        return aircraftTypes.get(type)[4];
    }
}
