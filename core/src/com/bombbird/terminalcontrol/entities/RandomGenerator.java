package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.aircrafts.AircraftType;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class RandomGenerator {
    /** Generates a random plane (with callsign, aircraft type) */
    public static String[] randomPlane() {
        //TODO Load airline callsigns from file
        String[] callsigns = new String[] {"EVA", "CAL", "UIA", "MDA"};
        String[] aircrafts = AircraftType.aircraftTypes.keySet().toArray(new String[0]);
        return new String[] {callsigns[MathUtils.random(callsigns.length - 1)] + Integer.toString(MathUtils.random(1, 999)), aircrafts[MathUtils.random(aircrafts.length - 1)]};
    }

    /** Generates a random airport given the RadarScreen mainName variable */
    public static Airport randomAirport() {
        if ("RCTP".equals(RadarScreen.MAIN_NAME)) {
            if (MathUtils.random(9) < 9) {
                return RadarScreen.AIRPORTS.get("RCTP");
            }
            return RadarScreen.AIRPORTS.get("RCSS");
        }
        Gdx.app.log("Unknown mainName", "Unknowm mainName " + RadarScreen.MAIN_NAME + " set!");
        return RadarScreen.AIRPORTS.get("RCTP");
    }
}
