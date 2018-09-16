package com.bombbird.atcsim.entities;

import com.bombbird.atcsim.screens.RadarScreen;
import okhttp3.*;
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
                    throw new IOException("Unexpected code " + response);
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
                    throw new IOException("Unexpected code " + response);
                } else {
                    String responseText = response.body().string();
                    //System.out.println("Receive METAR decoded string: " + responseText);
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
                    throw new IOException("Unexpected code " + response);
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
        jo.put("airports", RadarScreen.airports.keySet());
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
                    if (response.code() == 503) {
                        System.out.println("503 received: trying again");
                        getMetar(mediaType, client);
                    }
                    throw new IOException("Unexpected code " + response);
                } else {
                    String responseText = response.body().string();
                    if (responseText.equals("Update")) {
                        System.out.println("Update requested");
                        radarScreen.loadingPercent = "20%";
                        getApiKey(mediaType, client);
                    } else {
                        metarObject = new JSONObject(responseText);
                        updateAirports();
                        radarScreen.loadingPercent = "100%";
                        radarScreen.loading = false;
                    }
                }
            }
        });
    }

    private void updateAirports() {
        for (Airport airport: RadarScreen.airports.values()) {
            airport.setMetar(metarObject);
        }
    }
}
