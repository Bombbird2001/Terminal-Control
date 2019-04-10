package com.bombbird.terminalcontrol.entities.airports;

import java.util.HashMap;

public abstract class AirportName {
    public static final HashMap<String, String> airportNames = new HashMap<String, String>();

    public static String getAirportName(String icao) {
        return airportNames.get(icao);
    }
}
