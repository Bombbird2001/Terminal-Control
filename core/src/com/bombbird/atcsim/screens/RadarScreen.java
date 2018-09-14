package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.aircrafts.Aircraft;
import com.bombbird.atcsim.entities.aircrafts.Arrival;
import com.bombbird.atcsim.entities.restrictions.Obstacle;
import com.bombbird.atcsim.entities.restrictions.RestrictedArea;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RadarScreen extends GameScreen {
    public static String mainName;
    public static float magHdgDev;
    private Timer timer;
    private String apiKey;
    private JSONObject metarObject;

    RadarScreen(final AtcSim game, String name) {
        super(game);
        mainName = name;

        //Set stage params
        stage = new Stage(new FitViewport(5760, 3240));
        stage.getViewport().update(AtcSim.WIDTH, AtcSim.HEIGHT, true);
        inputProcessor2 = stage;
        inputMultiplexer.addProcessor(inputProcessor2);
        inputMultiplexer.addProcessor(inputProcessor1);
        Gdx.input.setInputProcessor(inputMultiplexer);

        //Set camera params
        camera = (OrthographicCamera) stage.getViewport().getCamera();
        camera.setToOrtho(false,5760, 3240);
        viewport = new FitViewport(AtcSim.WIDTH, AtcSim.HEIGHT, camera);
        viewport.apply();

        //Set aircraft array
        aircrafts = new HashMap<String, Aircraft>();

        //Set timer for METAR
        timer = new Timer(true);

        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }

    private void loadAirports() {
        FileHandle handle = Gdx.files.internal("game/" + mainName +"/airport.arpt");
        int index = 0;
        for (String s: handle.readString().split("\\r?\\n")) {
            switch (index) {
                case 0: magHdgDev = Float.parseFloat(s); break;
                default:
                    int index1 = 0;
                    String icao = "";
                    int elevation = 0;
                    for (String s1: s.split(" ")) {
                        switch (index1) {
                            case 0: icao = s1; break;
                            case 1: elevation = Integer.parseInt(s1); break;
                            default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + mainName + "/airport.arpt");
                        }
                        index1++;
                    }
                    airports.put(icao, new Airport(icao, elevation));
            }
            index++;
        }
    }

    private void updateMetar() {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        getMetar(JSON, client);
    }

    private void loadMetar() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.MINUTE, 15);
        int minute = calendar.get(Calendar.MINUTE);
        if (minute >= 55) {
            minute = 55;
        } else if (minute >= 40) {
            minute = 40;
        } else if (minute >= 25) {
            minute = 25;
        } else if (minute >= 10) {
            minute = 10;
        } else {
            minute = 55;
            calendar.add(Calendar.HOUR_OF_DAY, -1);
        }
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        System.out.println(calendar.getTime().toString());

        //Update the METAR every 15 minutes starting from 10 minutes after each quarter of the hour
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateMetar();
            }
        }, calendar.getTime(), 900000);

        updateMetar();
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
                }
            }
        });
    }

    private void receiveMetar(final MediaType mediaType, final OkHttpClient client) {
        String str = airports.keySet().toString();
        Request request = new Request.Builder()
                .addHeader("X-API-KEY", apiKey)
                .url("https://api.checkwx.com/metar/" + str.substring(1, str.length() - 1).replaceAll("\\s","") + "/decoded")
                .build();
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int minute = calendar.get(Calendar.MINUTE);
        if (minute >= 55) {
            minute = 45;
        } else if (minute >= 40) {
            minute = 30;
        } else if (minute >= 25) {
            minute = 15;
        } else if (minute >= 10) {
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
                }
            }
        });
    }

    private void getMetar(final MediaType mediaType, final OkHttpClient client) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        JSONObject jo = new JSONObject();
        jo.put("password", ""); //TODO: Remove before committing
        jo.put("year", calendar.get(Calendar.YEAR));
        jo.put("month", calendar.get(Calendar.MONTH) + 1);
        jo.put("day", calendar.get(Calendar.DAY_OF_MONTH));
        jo.put("hour", calendar.get(Calendar.HOUR_OF_DAY));
        jo.put("minute", calendar.get(Calendar.MINUTE));
        jo.put("airports", airports.keySet());
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
                    String responseText = response.body().string();
                    if (responseText.equals("Update")) {
                        getApiKey(mediaType, client);
                        System.out.println("Update requested");
                    } else {
                        //TODO: Decode json into metar data
                        metarObject = new JSONObject(responseText);
                        System.out.println(metarObject.toString());
                    }
                }
            }
        });
    }

    private void newAircraft() {
        aircrafts.put("EVA226", new Arrival("EVA226", "B77W", 'H', new int[]{4000, -3000}, 147, airports.get("RCTP")));
        aircrafts.put("UIA231", new Arrival("UIA231", "A321", 'M', new int[]{3000, -2500}, 124, airports.get("RCSS")));
    }

    private void loadUI() {
        //Reset stage
        stage.clear();

        //Load range circles
        loadRange();

        //Load waypoints
        loadWaypoints();

        //Load airports
        loadAirports();

        //Load METARs
        loadMetar();

        //Load scroll listener
        loadScroll();

        //Load obstacles
        loadObstacles();

        //Load altitude restrictions
        loadRestricted();

        //Load planes
        newAircraft();
    }

    private void loadObstacles() {
        obstacles = Gdx.files.internal("game/" + mainName + "/obstacle.obs");
        obsArray = new Array<Obstacle>();
        String[] indivObs = obstacles.readString().split("\\r?\\n");
        for (String s: indivObs) {
            //For each individual obstacle:
            String[] obsInfo = s.split(", ");
            int index = 0;
            int minAlt = 0;
            String text = "";
            int textX = 0;
            int textY = 0;
            ArrayList<Float> vertices = new ArrayList<Float>();
            for (String s1: obsInfo) {
                switch (index) {
                    case 0: minAlt = Integer.parseInt(s1); break;
                    case 1: text = s1; break;
                    case 2: textX = Integer.parseInt(s1); break;
                    case 3: textY = Integer.parseInt(s1); break;
                    default: vertices.add(Float.parseFloat(s1));
                }
                index++;
            }
            int i = 0;
            float[] verts = new float[vertices.size()];
            for (float f: vertices) {
                verts[i++] = f;
            }
            Obstacle obs = new Obstacle(verts, minAlt, text, textX, textY);
            obsArray.add(obs);
            stage.addActor(obs);
        }
    }

    private void loadRestricted() {
        restrictions = Gdx.files.internal("game/" + mainName + "/restricted.rest");
        restArray = new Array<RestrictedArea>();
        String[] indivRests = restrictions.readString().split("\\r?\\n");
        for (String s: indivRests) {
            //For each individual restricted area
            String[] restInfo = s.split(", ");
            int index = 0;
            int minAlt = 0;
            String text = "";
            int textX = 0;
            int textY = 0;
            float centreX = 0;
            float centreY = 0;
            float radius = 0;
            for (String s1: restInfo) {
                switch (index) {
                    case 0: minAlt = Integer.parseInt(s1); break;
                    case 1: text = s1; break;
                    case 2: textX = Integer.parseInt(s1); break;
                    case 3: textY = Integer.parseInt(s1); break;
                    case 4: centreX = Float.parseFloat(s1); break;
                    case 5: centreY = Float.parseFloat(s1); break;
                    case 6: radius = Float.parseFloat(s1); break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + mainName + "/restricted.rest");
                }
                index++;
            }
            RestrictedArea area = new RestrictedArea(centreX, centreY, radius, minAlt, text, textX, textY);
            restArray.add(area);
            stage.addActor(area);
        }
    }

    private void loadWaypoints() {
        FileHandle handle = Gdx.files.internal("game/" + mainName + "/waypoint.way");
        String wayptStr = handle.readString();
        String[] indivWpt = wayptStr.split("\\r?\\n");
        waypoints = new HashMap<String, Waypoint>(indivWpt.length + 1, 0.999f);
        for (String s: indivWpt) {
            //For each waypoint
            int index = 0;
            String name = "";
            int x = 0;
            int y = 0;
            for (String s1: s.split(" ")) {
                switch (index) {
                    case 0: name = s1; break;
                    case 1: x = Integer.parseInt(s1); break;
                    case 2: y = Integer.parseInt(s1); break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + name + "/restricted.rest");
                }
                index++;
            }
            Waypoint waypoint = new Waypoint(name, x, y);
            waypoints.put(name, waypoint);
            stage.addActor(waypoint);
        }
    }

    @Override
    void renderShape() {
        //Draw obstacles
        for (Obstacle obstacle: obsArray) {
            obstacle.renderShape();
        }

        //Additional adjustments for certain airports
        shapeRenderer.setColor(Color.BLACK);
        if (mainName.equals("RCTP")) {
            shapeRenderer.line(4500, 2416, 4500, 2124);
            shapeRenderer.line(1256, 2050, 1256, 1180);
        }

        //Draw runway(s) for each airport
        for (Airport airport: airports.values()) {
            airport.renderRunways();
        }

        //Draw waypoints
        for (Waypoint waypoint: waypoints.values()) {
            waypoint.renderShape();
        }

        //Draw aircrafts
        for (Aircraft aircraft: aircrafts.values()) {
            aircraft.renderShape();
        }

        //Draw restricted areas
        for (RestrictedArea restrictedArea: restArray) {
            restrictedArea.renderShape();
        }

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void show() {
        loadUI();
    }

    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
        skin.dispose();
        Aircraft.skin.dispose();
        Aircraft.iconAtlas.dispose();
    }
}
