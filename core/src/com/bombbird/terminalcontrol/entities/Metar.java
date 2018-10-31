package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.screens.RadarScreen;
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

    public void updateMetar() {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        getMetar(JSON, client);
    }

    private void sendMetar(final MediaType mediaType, final OkHttpClient client, JSONObject jo, Calendar calendar) {
        jo.put("password", ""); //TODO: Remove before committing
        jo.put("year", calendar.get(Calendar.YEAR));
        jo.put("month", calendar.get(Calendar.MONTH) + 1);
        jo.put("day", calendar.get(Calendar.DAY_OF_MONTH));
        jo.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
        jo.put("minute", calendar.get(Calendar.MINUTE));
        RequestBody body = RequestBody.create(mediaType, jo.toString());
        Request request = new Request.Builder()
                .url("") //TODO: Remove before committing
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
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
        String str = RadarScreen.airports.keySet().toString();
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException(response.toString());
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
        RequestBody body = RequestBody.create(mediaType, "{\"password\":\"\"}"); //TODO: Remove before committing
        Request request = new Request.Builder()
                .url("") //TODO: Remove before committing
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
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
        jo.put("password", ""); //TODO: Remove before committing
        JSONArray apts = new JSONArray(RadarScreen.airports.keySet());
        jo.put("airports", apts);
        RequestBody body = RequestBody.create(mediaType, jo.toString());
        Request request = new Request.Builder()
                .url("") //TODO: Remove before committing
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                metarObject = generateRandomWeather();
                updateAirports();
                RadarScreen.ui.updateMetar();
                radarScreen.loadingPercent = "100%";
                radarScreen.loading = false;
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
                        metarObject = generateRandomWeather();
                        updateAirports();
                        RadarScreen.ui.updateMetar();
                        radarScreen.loadingPercent = "100%";
                        radarScreen.loading = false;
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
                        updateAirports();
                        RadarScreen.ui.updateMetar();
                        radarScreen.loadingPercent = "100%";
                        radarScreen.loading = false;
                    }
                    response.close();
                }
            }
        });
    }

    private void updateAirports() {
        for (Airport airport: RadarScreen.airports.values()) {
            airport.setMetar(metarObject);
        }
    }

    private JSONObject generateRandomWeather() {
        JSONObject jsonObject = new JSONObject();
        for (String airport: RadarScreen.airports.keySet()) {
            //For each airport, create random weather and parse to JSON object
            int rain = -1;
            int visibility;
            int windDir;
            int windSpd;
            int gust = -1;
            String ws;
            if (MathUtils.random(9) >= 8) {
                //20% chance of rain
                rain = MathUtils.random(9);
            }
            visibility = (MathUtils.random(9) + 1) * 1000;
            windDir = (MathUtils.random(35)) * 10 + 10;
            windSpd = MathUtils.random(29) + 1;

            ws = randomWS(windSpd, airport);

            if (windSpd >= 15 && MathUtils.random(2) == 2) {
                //Gusts
                gust = MathUtils.random(windSpd, 40);
            }

            JSONObject object = new JSONObject();
            if (rain > -1) {
                object.put("rain", rain);
            } else {
                object.put("rain", JSONObject.NULL);
            }

            object.put("visibility", visibility);
            object.put("windDirection", windDir);
            object.put("windSpeed", windSpd);

            if (gust > -1) {
                object.put("windGust", gust);
            } else {
                object.put("windGust", JSONObject.NULL);
            }

            if (ws != null && !ws.equals("")) {
                object.put("windshear", ws);
            } else {
                object.put("windshear", JSONObject.NULL);
            }

            jsonObject.put(airport, object);
        }
        return jsonObject;
    }

    private String randomWS(int windSpd, String airport) {
        String ws = null;
        if (windSpd >= 15) {
            if (MathUtils.random(2) == 2) {
                //Runway windshear
                StringBuilder stringBuilder = new StringBuilder();
                for (String runway : RadarScreen.airports.get(airport).getRunways().keySet()) {
                    //Random boolean for each runway
                    if (RadarScreen.airports.get(airport).getRunways().get(runway).isActive() && MathUtils.random(1) == 1) {
                        stringBuilder.append("R");
                        stringBuilder.append(runway);
                        stringBuilder.append(" ");
                    }
                }
                if (stringBuilder.length() > 3 && stringBuilder.length() == RadarScreen.airports.get(airport).getRunways().size() * 3) {
                    ws = "ALL RWY";
                } else {
                    ws = stringBuilder.toString();
                }
            }
        }
        return ws;
    }
}
