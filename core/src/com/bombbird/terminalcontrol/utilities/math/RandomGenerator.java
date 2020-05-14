package com.bombbird.terminalcontrol.utilities.math;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;

public class RandomGenerator {
    private static final String[] excluded = Gdx.files.internal("game/aircrafts/exclude.air").readString().split("\\r?\\n");

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
        } while (ArrayUtils.contains(excluded, airline + number) || TerminalControl.radarScreen.getAllAircraft().get(airline + number) != null);

        TerminalControl.radarScreen.getAllAircraft().put(airline + number, true);

        return new String[] {airline + number, aircraft};
    }

    /** Generates a random airport given the RadarScreen mainName variable */
    public static Airport randomAirport() {
        int total = 0;
        HashMap<Airport, int[]> airportRange = new HashMap<>();
        for (Airport airport: TerminalControl.radarScreen.airports.values()) {
            String[] mainArpts = new String[] {"TCTP", "TCWS", "TCTT", "TCBB", "TCHH", "TCBS", "TCMD", "TCPG", "TCHX"};
            if (airport.isCongested() && !ArrayUtils.contains(mainArpts, airport.getIcao())) continue; //Don't spawn arrivals into a congested secondary airport
            total += airport.getAircraftRatio();
            airportRange.put(airport, new int[] {total - airport.getAircraftRatio(), total});
        }

        if (total < 1) return TerminalControl.radarScreen.airports.get(TerminalControl.radarScreen.mainName); //Return main airport if sth fks up
        Airport airport = null;
        do {
            int index = MathUtils.random(1, total);
            for (Airport airport1 : TerminalControl.radarScreen.airports.values()) {
                if (airportRange.containsKey(airport1) && index > airportRange.get(airport1)[0] && index <= airportRange.get(airport1)[1]) {
                    airport = airport1;
                }
            }
        } while (airport == null || airport.getLandingRunways().size() == 0);
        return airport;
    }
}
