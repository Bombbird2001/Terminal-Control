package com.bombbird.terminalcontrol.entities.weather;

import com.badlogic.gdx.Gdx;
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

    public void updateMetar(final boolean tutorial) {
        if (radarScreen.liveWeather == RadarScreen.Weather.LIVE) {
            HttpRequests.getMetar(this, true);
        } else {
            Runnable threadRunner = () -> {
                if (tutorial) {
                    updateTutorialMetar();
                } else if (radarScreen.liveWeather == RadarScreen.Weather.RANDOM || metarObject == null) {
                    randomWeather();
                }
            };
            new Thread(threadRunner).start();
        }
    }

    private void updateTutorialMetar() {
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
        for (Airport airport: radarScreen.airports.values()) {
            airport.setMetar(metarObject);
        }
    }

    /** Create new weather based on current (i.e. small changes only) */
    public JSONObject randomBasedOnCurrent() {
        JSONObject airports = new JSONObject();
        for (String airport: radarScreen.airports.keySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("visibility", VisibilityChance.getRandomVis());

            int windDir = metarObject.getJSONObject(airport).getInt("windDirection") + MathUtils.random(-2, 2) * 10;
            if (windDir > 360) {
                windDir -= 360;
            } else if (windDir <= 0) {
                windDir += 360;
            }
            jsonObject.put("windDirection", windDir);

            int currentSpd = metarObject.getJSONObject(airport).getInt("windSpeed");
            int windSpd = (2 * currentSpd + WindspeedChance.getRandomWindspeed(airport, windDir)) / 3;
            jsonObject.put("windSpeed", windSpd);

            String ws = WindshearChance.getRandomWsForAllRwy(radarScreen.airports.get(airport), windSpd);
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
            visibility = VisibilityChance.getRandomVis();
            windDir = WindDirChance.getRandomWindDir(airport);
            windSpd = WindspeedChance.getRandomWindspeed(airport, windDir);

            ws = WindshearChance.getRandomWsForAllRwy(radarScreen.airports.get(airport), windSpd);

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

    public void randomWeather() {
        metarObject = metarObject == null ? generateRandomWeather() : randomBasedOnCurrent();
        updateRadarScreenState();
    }

    public void updateRadarScreenState() {
        if (quit) return;
        if (prevMetar == null || !metarObject.toString().equals(prevMetar.toString())) {
            Gdx.app.postRunnable(() -> radarScreen.updateInformation());
        }
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
        prevMetar = metarObject;
        updateAirports();
        Gdx.app.postRunnable(() -> radarScreen.ui.updateMetar());
        radarScreen.loadingPercent = "100%";
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
