package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.utilities.FileLoader;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class RandomSTAR {
    private static final HashMap<String, HashMap<String, int[][]>> noise = new HashMap<String, HashMap<String, int[][]>>();

    /** Loads STAR noise info for the airport */
    public static void loadStarNoise(String icao) {
        noise.put(icao, FileLoader.loadSidNoise(icao, false));
    }

    /** Gets a random STAR for the airport and runway */
    public static Star randomSTAR(Airport airport, String rwy) {
        Array<Star> possibleStars = new Array<Star>();
        for (Star star: airport.getStars().values()) {
            if (star.getRunways().contains(rwy, false) && checkNoise(airport, star.getName())) possibleStars.add(star);
        }

        if (possibleStars.size == 0) {
            Gdx.app.log("Random STAR", "No STARs found to match criteria for " + airport.getIcao() + " " + rwy);
            throw new IllegalArgumentException("No STARs found to match criteria for " + airport.getIcao() + " " + rwy);
        } else {
            return possibleStars.get(MathUtils.random(0, possibleStars.size - 1));
        }
    }

    /** Check whether a STAR is allowed to be used for the airport at the current time */
    private static boolean checkNoise(Airport airport, String star) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int time = calendar.get(Calendar.HOUR) * 100 + calendar.get(Calendar.MINUTE);
        if (!noise.containsKey(airport.getIcao()) || !noise.get(airport.getIcao()).containsKey(star)) return true;
        for (int[] timeSlot: noise.get(airport.getIcao()).get(star)) {
            if (time >= timeSlot[0] && time < timeSlot[1]) return true;
        }
        return false;
    }
}
