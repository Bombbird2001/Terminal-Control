package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Values;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class Metar {
    private JSONObject metarObject;
    private String apiKey;
    private RadarScreen radarScreen;

    public Metar(RadarScreen radarScreen) {
        apiKey = "";
        this.radarScreen = radarScreen;
    }

    public Metar(RadarScreen radarScreen, JSONObject save) {
        this(radarScreen);
        metarObject = save;
    }

    public void updateMetar() {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        if (radarScreen.liveWeather) {
            getMetar(JSON, client);
        } else {
            metarObject = metarObject == null ? generateRandomWeather() : randomBasedOnCurrent();
            updateRadarScreenState();
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

    private void sendMetar(final MediaType mediaType, final OkHttpClient client, JSONObject jo, Calendar calendar) {
        jo.put("password", Values.SEND_METAR_PASSWORD);
        jo.put("year", calendar.get(Calendar.YEAR));
        jo.put("month", calendar.get(Calendar.MONTH) + 1);
        jo.put("day", calendar.get(Calendar.DAY_OF_MONTH));
        jo.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
        jo.put("minute", calendar.get(Calendar.MINUTE));
        RequestBody body = RequestBody.create(mediaType, jo.toString());
        Request request = new Request.Builder()
                .url(Values.SEND_METAR_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //If requests fails due to timeout
                e.printStackTrace();
                receiveMetar(mediaType, client);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException(response.toString());
                } else {
                    System.out.println(response.body().string());
                    getMetar(mediaType, client);
                    radarScreen.loadingPercent = "80%";
                }
            }
        });
    }

    private void receiveMetar(final MediaType mediaType, final OkHttpClient client) {
        String str = radarScreen.airports.keySet().toString();
        Request request = new Request.Builder()
                .addHeader("X-API-KEY", apiKey)
                .url("https://api.checkwx.com/metar/" + str.substring(1, str.length() - 1).replaceAll("\\s","") + "/decoded")
                .build();
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int minute = calendar.get(Calendar.MINUTE);
        if (minute >= 45) {
            minute = 30;
        } else if (minute >= 30) {
            minute = 15;
        } else if (minute >= 15) {
            minute = 0;
        } else {
            minute = 45;
            calendar.add(Calendar.HOUR_OF_DAY, -1);
        }
        calendar.set(Calendar.MINUTE, minute);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //If requests fails due to timeout
                e.printStackTrace();
                receiveMetar(mediaType, client);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println(response.body().string());
                } else {
                    String responseText = response.body().string();
                    JSONObject jo = new JSONObject(responseText);
                    sendMetar(mediaType, client, jo, calendar);
                    radarScreen.loadingPercent = "60%";
                }
            }
        });
    }

    private void getApiKey(final MediaType mediaType, final OkHttpClient client) {
        RequestBody body = RequestBody.create(mediaType, "{\"password\":\"" + Values.API_PASSWORD + "\"}");
        Request request = new Request.Builder()
                .url(Values.API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //If requests fails due to timeout
                e.printStackTrace();
                receiveMetar(mediaType, client);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException(response.toString());
                } else {
                    apiKey = response.body().string();
                    receiveMetar(mediaType, client);
                    radarScreen.loadingPercent = "40%";
                }
            }
        });
    }

    private void getMetar(final MediaType mediaType, final OkHttpClient client) {
        JSONObject jo = new JSONObject();
        jo.put("password", Values.GET_METAR_PASSWORD);
        JSONArray apts = new JSONArray(radarScreen.airports.keySet());
        jo.put("airports", apts);
        RequestBody body = RequestBody.create(mediaType, jo.toString());
        Request request = new Request.Builder()
                .url(Values.GET_METAR_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                metarObject = metarObject == null ? generateRandomWeather() : randomBasedOnCurrent();
                updateRadarScreenState();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (response.code() == 503) {
                        System.out.println("503 received: trying again");
                        radarScreen.loadingPercent = "10%";
                        getMetar(mediaType, client);
                        response.close();
                    } else {
                        //Generate offline weather
                        response.close();
                        metarObject = metarObject == null ? generateRandomWeather() : randomBasedOnCurrent();
                        updateRadarScreenState();
                        System.out.println(response.body().string());
                    }
                } else {
                    String responseText = response.body().string();
                    System.out.println(responseText);
                    if (responseText.equals("Update")) {
                        //Server requested for METAR update
                        System.out.println("Update requested");
                        radarScreen.loadingPercent = "20%";
                        getApiKey(mediaType, client);
                    } else {
                        //METAR JSON text has been received
                        metarObject = new JSONObject(responseText);
                        updateRadarScreenState();
                    }
                    response.close();
                }
            }
        });
    }

    private void updateAirports() {
        for (Airport airport: radarScreen.airports.values()) {
            airport.setMetar(metarObject);
        }
    }

    /** Create new weather based on current (i.e. small changes only) */
    private JSONObject randomBasedOnCurrent() {
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

            String ws = randomWS(windDir, windSpd, airport);
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

    private JSONObject generateRandomWeather() {
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

            ws = randomWS(windDir, windSpd, airport);

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

    private String randomWS(int windHdg, int windSpd, String airport) {
        String ws = "";
        if (windSpd >= 15 && MathUtils.random(2) == 2) {
            //Runway windshear
            StringBuilder stringBuilder = new StringBuilder();
            for (Runway runway : radarScreen.airports.get(airport).getRunways().values()) {
                //Random boolean for each runway
                if (radarScreen.airports.get(airport).runwayActiveForWind(windHdg, runway) && MathUtils.random(1) == 1) {
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

    private void updateRadarScreenState() {
        updateAirports();
        radarScreen.ui.updateMetar();
        radarScreen.loadingPercent = "100%";
        radarScreen.loading = false;
    }

    public JSONObject getMetarObject() {
        return metarObject;
    }
}
