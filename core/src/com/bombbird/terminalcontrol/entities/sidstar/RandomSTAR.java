package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class RandomSTAR {
    private static final HashMap<String, HashMap<String, int[][]>> noise = new HashMap<String, HashMap<String, int[][]>>();
    private static HashMap<String, HashMap<String, Float>> time = new HashMap<String, HashMap<String, Float>>();

    /** Loads STAR noise info for the airport */
    public static void loadStarNoise(String icao) {
        noise.put(icao, FileLoader.loadNoise(icao, false));
    }

    /** Loads arrival entry timings */
    public static void loadEntryTiming(Airport airport) {
        HashMap<String, Float> stars = new HashMap<String, Float>();
        for (Star star: airport.getStars().values()) {
            stars.put(star.getName(), 0f);
        }
        time.put(airport.getIcao(), stars);
    }

    /** Loads arrival entry timings from save */
    public static void loadEntryTiming(Airport airport, JSONObject jsonObject) {
        for (String star: jsonObject.keySet()) {
            if (time.get(airport.getIcao()).containsKey(star)) {
                time.get(airport.getIcao()).put(star, (float) jsonObject.getDouble(star));
            }
        }
    }

    /** Updates timings */
    public static void update() {
        float dt = Gdx.graphics.getDeltaTime();
        for (String icao: time.keySet()) {
            for (String star: time.get(icao).keySet()) {
                time.get(icao).put(star, time.get(icao).get(star) - dt);
            }
        }
    }

    /** Gets a random STAR for the airport and runway */
    public static Star randomSTAR(Airport airport, HashMap<String, Runway> rwys) {
        Array<Star> possibleStars = new Array<Star>();
        for (Star star: airport.getStars().values()) {
            boolean found = false;
            for (int i = 0; i < star.getRunways().size; i++) {
                if (rwys.containsKey(star.getRunways().get(i))) {
                    found = true;
                    break;
                }
            }
            if (found && checkNoise(airport, star.getName()) && time.get(airport.getIcao()).get(star.getName()) < 0) possibleStars.add(star);
        }

        if (possibleStars.size == 0) {
            Array<String> runways = new Array<String>();
            for (String rwy: rwys.keySet()) {
                runways.add(rwy);
            }
            Gdx.app.log("Random STAR", "No STARs found to match criteria for " + airport.getIcao() + " " + runways.toString());
            throw new IllegalArgumentException("No STARs found to match criteria for " + airport.getIcao() + " " + runways.toString());
        } else {
            return possibleStars.get(MathUtils.random(0, possibleStars.size - 1));
        }
    }

    /** Check whether a STAR is allowed to be used for the airport at the current time */
    private static boolean checkNoise(Airport airport, String star) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int additional = calendar.get(Calendar.AM_PM) == Calendar.PM ? 12 : 0;
        int time = (calendar.get(Calendar.HOUR) + additional) * 100 + calendar.get(Calendar.MINUTE);
        if (!noise.containsKey(airport.getIcao()) || !noise.get(airport.getIcao()).containsKey(star)) return true;
        for (int[] timeSlot: noise.get(airport.getIcao()).get(star)) {
            if (time >= timeSlot[0] && time < timeSlot[1]) return true;
        }
        return false;
    }

    public static boolean starAvailable(String icao) {
        for (float time: time.get(icao).values()) {
            if (time < 0) return true;
        }
        return false;
    }

    public static void starUsed(String icao, String star) {
        time.get(icao).put(star, 60f);
    }

    public static HashMap<String, HashMap<String, Float>> getTime() {
        return time;
    }
}
