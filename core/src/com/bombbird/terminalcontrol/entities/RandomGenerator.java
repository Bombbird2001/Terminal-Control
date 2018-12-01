package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;

import java.util.HashMap;

public class RandomGenerator {


    /** Generates a random plane (with callsign, aircraft type) */
    public static String[] randomPlane(Airport airport) {
        int size = airport.getAirlines().size();

        String airline;
        int number;
        String aircraft;

        do {
            airline = airport.getAirlines().get(MathUtils.random(size - 1));
            number = MathUtils.random(1, 999);
            String[] aircrafts = airport.getAircrafts().get(airline).split(">");
            aircraft = aircrafts[MathUtils.random(aircrafts.length - 1)];
        } while (TerminalControl.radarScreen.getAllAircraft().get(airline + number) != null);

        TerminalControl.radarScreen.getAllAircraft().put(airline + number, true);

        return new String[] {airline + number, aircraft};
    }

    /** Generates a random airport given the RadarScreen mainName variable */
    public static Airport randomAirport() {
        int total = 0;
        HashMap<Airport, int[]> airportRange = new HashMap<Airport, int[]>();
        for (Airport airport: TerminalControl.radarScreen.airports.values()) {
            total += airport.getAircraftRatio();
            airportRange.put(airport, new int[] {total - airport.getAircraftRatio(), total});
        }
        int index = MathUtils.random(1, total);
        for (Airport airport: TerminalControl.radarScreen.airports.values()) {
            if (index > airportRange.get(airport)[0] && index <= airportRange.get(airport)[1]) {
                return airport;
            }
        }
        Gdx.app.log("Random airport error", "Something went wrong with generating the random airport, returning main airport");
        return TerminalControl.radarScreen.airports.get(TerminalControl.radarScreen.mainName);
    }
}
