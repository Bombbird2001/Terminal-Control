package com.bombbird.terminalcontrol.entities.weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.utilities.HttpRequests;
import com.bombbird.terminalcontrol.utilities.RenameManager;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Metar {
    private JSONObject prevMetar;
    private JSONObject metarObject;
    private final RadarScreen radarScreen;
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

    /** Initialise the getting of live weather/changing of random weather */
    public void updateMetar(final boolean tutorial) {
        if (radarScreen.weatherSel == RadarScreen.Weather.LIVE && !tutorial) {
            HttpRequests.getMetar(this, true);
        } else {
            Runnable threadRunner = () -> {
                if (tutorial) {
                    updateTutorialMetar();
                } else {
                    randomWeather();
                }
            };
            new Thread(threadRunner).start();
        }
    }

    /** Sets the weather specific to tutorial */
    private void updateTutorialMetar() {
        metarObject = new JSONObject();

        JSONObject rctpMetar = new JSONObject();
        rctpMetar.put("rain", JSONObject.NULL);
        rctpMetar.put("visibility", 9000);
        rctpMetar.put("windSpeed", 14);
        rctpMetar.put("windDirection", 60);
        rctpMetar.put("windGust", JSONObject.NULL);
        rctpMetar.put("windshear", JSONObject.NULL);
        rctpMetar.put("metar", generateRawMetar(rctpMetar));
        metarObject.put("RCTP", rctpMetar);

        JSONObject rcssMetar = new JSONObject();
        rcssMetar.put("rain", JSONObject.NULL);
        rcssMetar.put("visibility", 10000);
        rcssMetar.put("windSpeed", 8);
        rcssMetar.put("windDirection", 100);
        rcssMetar.put("windGust", JSONObject.NULL);
        rcssMetar.put("windshear", JSONObject.NULL);
        rcssMetar.put("metar", generateRawMetar(rcssMetar));
        metarObject.put("RCSS", rcssMetar);

        updateRadarScreenState();
    }

    /** Updates all in game airports with new weather data */
    private void updateAirports() {
        for (Airport airport: radarScreen.airports.values()) {
            if (prevMetar == null) {
                airport.setRwyChangeTimer(-1);
                airport.setPendingRwyChange(true);
            }
            airport.setMetar(metarObject);
            if (prevMetar == null) {
                airport.setRwyChangeTimer(301);
                airport.setPendingRwyChange(false);
            }
        }
    }

    /** Create new weather based on current (i.e. no big changes) */
    public JSONObject randomBasedOnCurrent() {
        JSONObject airports = new JSONObject();
        for (String airport: radarScreen.airports.keySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("visibility", VisibilityChance.getRandomVis());

            int windDir = metarObject.getJSONObject(RenameManager.reverseNameAirportICAO(airport)).getInt("windDirection") + MathUtils.random(-2, 2) * 10;
            windDir = MathTools.modulateHeading(windDir);
            jsonObject.put("windDirection", windDir);

            int currentSpd = metarObject.getJSONObject(RenameManager.reverseNameAirportICAO(airport)).getInt("windSpeed");
            int windSpd = (2 * currentSpd + WindspeedChance.getRandomWindspeed(airport, windDir)) / 3;
            jsonObject.put("windSpeed", windSpd);

            String ws = WindshearChance.getRandomWsForAllRwy(airport, windSpd);
            jsonObject.put("windshear", "".equals(ws) ? JSONObject.NULL : ws);

            int gust = -1;
            if (windSpd >= 15 && MathUtils.random(2) == 2) {
                //Gusts
                gust = MathUtils.random(windSpd + 3, 40);
            }
            jsonObject.put("windGust", gust > -1 ? gust : JSONObject.NULL);

            jsonObject.put("rain", JSONObject.NULL);

            jsonObject.put("metar", generateRawMetar(jsonObject));

            airports.put(RenameManager.reverseNameAirportICAO(airport), jsonObject);
        }

        return airports;
    }

    /** Generates random weather without any previous weather to "rely" upon */
    private JSONObject generateRandomWeather() {
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

            ws = WindshearChance.getRandomWsForAllRwy(airport, windSpd);

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

            object.put("metar", generateRawMetar(object));

            jsonObject.put(RenameManager.reverseNameAirportICAO(airport), object);
        }
        return jsonObject;
    }

    /** Generates and applies a randomised weather */
    public void randomWeather() {
        if (metarObject == null) {
            metarObject = generateRandomWeather();
        } else if (radarScreen.weatherSel == RadarScreen.Weather.RANDOM) {
            metarObject = randomBasedOnCurrent();
        }
        updateRadarScreenState();
    }

    /** Generates a basic raw metar for randomised weather */
    private String generateRawMetar(JSONObject object) {
        StringBuilder sb = new StringBuilder();
        int windDir = object.optInt("windDirection", 0);
        String windDirStr;
        if (windDir == 0) {
            windDirStr = "VRB";
        } else if (windDir < 100) {
            windDirStr = "0" + windDir;
        } else {
            windDirStr = String.valueOf(windDir);
        }
        sb.append(windDirStr);
        int windSpd = object.optInt("windSpeed", 1);
        sb.append(windSpd < 10 ? "0" + windSpd : windSpd).append("KT ");
        int visibility = object.optInt("visibility", 10000);
        if (visibility >= 10000) visibility = 9999;
        sb.append(visibility).append(" ");
        int temp = MathUtils.random(20, 35);
        sb.append(temp).append("/").append(temp - MathUtils.random(2, 8)).append(" ");
        sb.append("Q").append(MathUtils.random(1005, 1018)).append(" ");
        sb.append(object.isNull("windshear") ? "NOSIG" : "WS " + object.getString("windshear"));

        return sb.toString();
    }

    /** Called after changing the metarObject, to update the in game weather and UI */
    public void updateRadarScreenState() {
        if (quit) return;
        if (prevMetar == null || !metarObject.toString().equals(prevMetar.toString())) {
            Gdx.app.postRunnable(radarScreen::updateInformation);
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
        updateAirports();
        prevMetar = metarObject;
        Gdx.app.postRunnable(() -> radarScreen.ui.updateMetar());
        radarScreen.loadingPercent = "100%";
        radarScreen.loading = false;
    }

    /** Updates the METAR object and in game weather given custom weather data for airports */
    public void updateCustomWeather(HashMap<String, int[]> arptData) {
        for (Map.Entry<String, int[]> entry: arptData.entrySet()) {
            String realIcao = RenameManager.reverseNameAirportICAO(entry.getKey());
            JSONObject object = metarObject.getJSONObject(realIcao);
            object.put("windDirection", entry.getValue()[0]);
            object.put("windSpeed", entry.getValue()[1]);
            String randomWs = WindshearChance.getRandomWsForAllRwy(entry.getKey(), entry.getValue()[1]);
            object.put("windshear", "".equals(randomWs) ? JSONObject.NULL : randomWs);
            object.put("visibility", VisibilityChance.getRandomVis());
            object.put("metar", generateRawMetar(object));
        }
        updateRadarScreenState();
    }

    public JSONObject getMetarObject() {
        return metarObject;
    }

    public boolean isQuit() {
        return quit;
    }

    public void setQuit(boolean quit) {
        this.quit = quit;
    }

    public void setMetarObject(JSONObject metarObject) {
        this.metarObject = metarObject;
    }
}
