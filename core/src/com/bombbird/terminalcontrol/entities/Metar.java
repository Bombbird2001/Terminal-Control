package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.HttpRequests;
import org.json.JSONObject;

public class Metar {
    private JSONObject prevMetar;
    private JSONObject metarObject;
    private RadarScreen radarScreen;
    private boolean quit;

    //Thread lock
    private final Object lock = new Object();

    public Metar(RadarScreen radarScreen) {
        this.radarScreen = radarScreen;
    }

    public Metar(RadarScreen radarScreen, JSONObject save) {
        this(radarScreen);
        prevMetar = save;
        metarObject = save;
    }

    public void updateMetar() {
        if (radarScreen.liveWeather) {
            HttpRequests.getMetar(this, true);
        } else {
            randomWeather();
        }
    }

    public void updateTutorialMetar() {
        metarObject = new JSONObject();

        JSONObject rctpMetar = new JSONObject();
        rctpMetar.put("rain", JSONObject.NULL);
        rctpMetar.put("visibility", 9000);
        rctpMetar.put("windSpeed", 14);
        rctpMetar.put("windDirection", 60);
        rctpMetar.put("windGust", JSONObject.NULL);
        rctpMetar.put("windshear", JSONObject.NULL);
        metarObject.put("RCTP", rctpMetar);

        JSONObject rcssMetar = new JSONObject();
        rcssMetar.put("rain", JSONObject.NULL);
        rcssMetar.put("visibility", 10000);
        rcssMetar.put("windSpeed", 8);
        rcssMetar.put("windDirection", 100);
        rcssMetar.put("windGust", JSONObject.NULL);
        rcssMetar.put("windshear", JSONObject.NULL);
        metarObject.put("RCSS", rcssMetar);

        updateRadarScreenState();
    }

    private void updateAirports() {
        if (prevMetar == null || !metarObject.toString().equals(prevMetar.toString())) radarScreen.updateInformation();
        for (Airport airport: radarScreen.airports.values()) {
            airport.setMetar(metarObject);
        }
    }

    /** Create new weather based on current (i.e. small changes only) */
    public JSONObject randomBasedOnCurrent() {
        JSONObject airports = new JSONObject();
        for (String airport: radarScreen.airports.keySet()) {
            JSONObject jsonObject = new JSONObject();
            int visibility = metarObject.getJSONObject(airport).getInt("visibility") + MathUtils.randomSign() * 1000;
            visibility = MathUtils.clamp(visibility, 1000, 10000);
            jsonObject.put("visibility", visibility);

            int windDir = metarObject.getJSONObject(airport).getInt("windDirection") + MathUtils.random(-20, 20);
            if (windDir > 360) {
                windDir -= 360;
            } else if (windDir <= 0) {
                windDir += 360;
            }
            jsonObject.put("windDirection", windDir);

            int windSpd = metarObject.getJSONObject(airport).getInt("windSpeed") + MathUtils.random(-15, 15);
            windSpd = MathUtils.clamp(windSpd, 5, 30);
            jsonObject.put("windSpeed", windSpd);

            String ws = randomWS(windSpd, airport);
            jsonObject.put("windshear", "".equals(ws) ? JSONObject.NULL : ws);

            int gust = -1;
            if (windSpd >= 15 && MathUtils.random(2) == 2) {
                //Gusts
                gust = MathUtils.random(windSpd + 3, 40);
            }
            jsonObject.put("windGust", gust > -1 ? gust : JSONObject.NULL);

            jsonObject.put("rain", JSONObject.NULL);

            airports.put(airport, jsonObject);
        }

        return airports;
    }

    public JSONObject generateRandomWeather() {
        JSONObject jsonObject = new JSONObject();
        for (String airport: radarScreen.airports.keySet()) {
            //For each airport, create random weather and parse to JSON object
            int visibility;
            int windDir;
            int windSpd;
            int gust = -1;
            String ws;
            visibility = (MathUtils.random(9) + 1) * 1000;
            windDir = (MathUtils.random(35)) * 10 + 10;
            windSpd = MathUtils.random(25) + 5;

            ws = randomWS(windSpd, airport);

            if (windSpd >= 15 && MathUtils.random(2) == 2) {
                //Gusts
                gust = MathUtils.random(windSpd + 3, 40);
            }

            JSONObject object = new JSONObject();
            object.put("rain", JSONObject.NULL);

            object.put("visibility", visibility);
            object.put("windDirection", windDir);
            object.put("windSpeed", windSpd);

            object.put("windGust", gust > -1 ? gust : JSONObject.NULL);

            object.put("windshear", "".equals(ws) ? JSONObject.NULL : ws);

            jsonObject.put(airport, object);
        }
        return jsonObject;
    }

    private String randomWS(int windSpd, String airport) {
        String ws = "";
        if (windSpd >= 15 && MathUtils.random(2) == 2) {
            //Runway windshear
            StringBuilder stringBuilder = new StringBuilder();
            for (Runway runway : radarScreen.airports.get(airport).getRunways().values()) {
                //Random boolean for each runway
                if (MathUtils.random(2) == 2) {
                    stringBuilder.append("R");
                    stringBuilder.append(runway.getName());
                    stringBuilder.append(" ");
                }
            }
            if (stringBuilder.length() > 3 && stringBuilder.length() == radarScreen.airports.get(airport).getLandingRunways().size() * 3) {
                ws = "ALL RWY";
            } else {
                ws = stringBuilder.toString();
            }
        }
        return ws;
    }

    public void randomWeather() {
        metarObject = metarObject == null ? generateRandomWeather() : randomBasedOnCurrent();
        updateRadarScreenState();
    }

    public void updateRadarScreenState() {
        if (quit) return;
        updateAirports();
        radarScreen.ui.updateMetar();
        radarScreen.loadingPercent = "100%";
        if (radarScreen.loadingTime < 4.5) {
            long deltaTime = (long)((4.5 - radarScreen.loadingTime) * 1000);
            try {
                synchronized (lock) {
                    lock.wait(deltaTime);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        radarScreen.loading = false;
    }

    public JSONObject getMetarObject() {
        return metarObject;
    }

    public void setQuit(boolean quit) {
        this.quit = quit;
    }

    public void setMetarObject(JSONObject metarObject) {
        this.metarObject = metarObject;
    }
}
