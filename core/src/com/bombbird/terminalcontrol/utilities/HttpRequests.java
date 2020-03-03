package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Gdx;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.weather.Metar;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class HttpRequests {
    private static final MediaType json = MediaType.parse("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    public static void sendError(final String error, final int count) {
        if (!TerminalControl.sendAnonCrash) return;
        JSONObject jo = new JSONObject();
        jo.put("password", Values.SEND_ERROR_PASSWORD);
        jo.put("error", error);
        RequestBody body = RequestBody.create(jo.toString(), json);
        final Request request = new Request.Builder()
                .url(Values.SEND_ERROR_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //If requests fails due to timeout
                e.printStackTrace();
                if (count <= 2) sendError(error, count + 1);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Gdx.app.log("sendError", response.toString());
                    response.close();
                } else {
                    if (response.body() != null) System.out.println(response.body().string());
                }
                response.close();
            }
        });
    }

    public static void getMetar(final Metar metar, final boolean retry) {
        JSONObject jo = new JSONObject();
        jo.put("password", Values.GET_METAR_PASSWORD);
        JSONArray apts = new JSONArray();
        for (String newIcao: TerminalControl.radarScreen.airports.keySet()) {
            apts.put(RenameManager.reverseNameAirportICAO(newIcao));
        }
        jo.put("airports", apts);
        RequestBody body = RequestBody.create(jo.toString(), json);
        Request request = new Request.Builder()
                .url(Values.GET_METAR_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                metar.randomWeather();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (response.code() == 503 && retry) {
                        System.out.println("503 received: trying again");
                        response.close();
                        TerminalControl.radarScreen.loadingPercent = "10%";
                        getMetar(metar, false);
                    } else {
                        //Generate offline weather
                        response.close();
                        metar.randomWeather();
                    }
                } else {
                    String responseText = "";
                    if (response.body() != null) responseText = response.body().string();
                    if ("Update".equals(responseText)) {
                        //Server requested for METAR update
                        System.out.println("Update requested");
                        TerminalControl.radarScreen.loadingPercent = "20%";
                        getApiKey(metar, 0);
                    } else {
                        //METAR JSON text has been received
                        metar.setMetarObject(new JSONObject(responseText));
                        metar.updateRadarScreenState();
                    }
                    response.close();
                }
            }
        });
    }

    private static void getApiKey(final Metar metar, final int count) {
        RequestBody body = RequestBody.create("{\"password\":\"" + Values.API_PASSWORD + "\"}", json);
        Request request = new Request.Builder()
                .url(Values.API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //If requests fails due to timeout
                e.printStackTrace();
                if (count <= 2) {
                    getApiKey(metar, count + 1);
                    return;
                }
                metar.randomWeather();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Gdx.app.log("getApiKey", response.toString());
                    response.close();
                    if (count <= 2) {
                        getApiKey(metar, count + 1);
                        return;
                    }
                    metar.randomWeather();
                } else {
                    String apiKey = null;
                    if (response.body() != null) apiKey = response.body().string();
                    if (apiKey == null) {
                        response.close();
                        if (count <= 2) {
                            getApiKey(metar, count + 1);
                        } else {
                            metar.randomWeather();
                        }
                        return;
                    }
                    receiveMetar(metar, apiKey, true);
                    TerminalControl.radarScreen.loadingPercent = "40%";
                }
                response.close();
            }
        });
    }

    private static void receiveMetar(final Metar metar, final String apiKey, final boolean retry) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String newIcao: TerminalControl.radarScreen.airports.keySet()) {
            if (stringBuilder.length() > 0) stringBuilder.append(",");
            String arpt = RenameManager.reverseNameAirportICAO(newIcao);
            if ("VHHX".equals(arpt)) arpt = "VHHH";
            stringBuilder.append(arpt);
        }
        final Request request = new Request.Builder()
                .addHeader("X-API-KEY", apiKey)
                .url("https://api.checkwx.com/metar/" + stringBuilder.toString() + "/decoded")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //If requests fails due to timeout
                e.printStackTrace();
                Gdx.app.log("API metar error", "CheckWX API may not be working!");

                //If retrying
                if (retry) {
                    Gdx.app.log("receiveMetar", "Retrying getting weather from API");
                    receiveMetar(metar, apiKey, false);
                    return;
                }
                metar.randomWeather();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Gdx.app.log("receiveMetar", response.toString());
                    response.close();
                    if (retry) {
                        Gdx.app.log("receiveMetar", "Retrying getting weather from API");
                        receiveMetar(metar, apiKey, false);
                        return;
                    }
                    metar.randomWeather();
                } else {
                    String responseText = "";
                    if (response.body() != null) responseText = response.body().string();
                    JSONObject jo = new JSONObject(responseText);
                    sendMetar(metar, jo);
                    TerminalControl.radarScreen.loadingPercent = "60%";
                }
                response.close();
            }
        });
    }

    private static void sendMetar(final Metar metar, final JSONObject jo) {
        jo.put("password", Values.SEND_METAR_PASSWORD);
        RequestBody body = RequestBody.create(jo.toString(), json);
        Request request = new Request.Builder()
                .url(Values.SEND_METAR_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //If requests fails due to timeout
                e.printStackTrace();
                sendMetar(metar, jo);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Gdx.app.log("sendMetar", response.toString());
                    metar.randomWeather();
                    response.close();
                    return;
                } else {
                    if (response.body() != null) System.out.println(response.body().string());
                }
                response.close();
                getMetar(metar, true);
                TerminalControl.radarScreen.loadingPercent = "80%";
            }
        });
    }
}
