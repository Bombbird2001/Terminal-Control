package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.bombbird.terminalcontrol.utilities.FileLoader;

import java.util.HashMap;

public class AircraftType {
    public static final HashMap<String, int[]> aircraftTypes = FileLoader.loadAircraftData();

    public static char getWakeCat(String type) {
        int code = aircraftTypes.get(type)[0];
        if (code == 0) {
            return 'M';
        } else if (code == 1) {
            return 'H';
        } else if (code == 2) {
            return 'J';
        } else {
            Gdx.app.log("Invalid wake category", "Invalid wake turbulence category set for " + type + "!");
            return 'M';
        }
    }

    public static int getV2(String type) {
        return aircraftTypes.get(type)[1];
    }

    public static int getTypClimb(String type) {
        return aircraftTypes.get(type)[2];
    }

    public static int getTypDes(String type) {
        return aircraftTypes.get(type)[3];
    }

    public static int getApchSpd(String type) {
        return aircraftTypes.get(type)[4];
    }
}
