package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.restrictions.Obstacle;
import com.bombbird.atcsim.entities.restrictions.RestrictedArea;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class RadarScreen extends GameScreen {
    public static String mainName;

    RadarScreen(final AtcSim game, String name) {
        super(game);

        mainName = name;

        //Set stage params
        stage = new Stage(new FitViewport(1440, 810));
        stage.getViewport().update(AtcSim.WIDTH, AtcSim.HEIGHT, true);
        inputProcessor2 = stage;
        inputMultiplexer.addProcessor(inputProcessor2);
        inputMultiplexer.addProcessor(inputProcessor1);
        Gdx.input.setInputProcessor(inputMultiplexer);

        //Set camera params
        camera = (OrthographicCamera) stage.getViewport().getCamera();
        camera.setToOrtho(false,1440, 810);
        viewport = new FitViewport(AtcSim.WIDTH, AtcSim.HEIGHT, camera);
        viewport.apply();
    }

    private void loadAirports() {
        //TODO: Set file containing airport information
        Airport rctp = new Airport(mainName);
        airports.add(rctp);
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

        //Load scroll listener
        loadScroll();

        //Load obstacles
        loadObstacles();

        //Load altitude restrictions
        loadRestricted();
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
        waypoints = new Hashtable<String, Waypoint>(indivWpt.length + 1, 0.999f);
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
        for (Obstacle obstacle: obsArray) {
            obstacle.renderShape();
        }
        //Additional adjustments for certain airports
        shapeRenderer.setColor(Color.BLACK);
        if (mainName.equals("RCTP")) {
            shapeRenderer.line(1125, 604, 1125, 531);
            shapeRenderer.line(314, 512.5f, 314, 295);
        }
        for (Airport airport: airports) {
            airport.renderRunways();
        }
        Enumeration<String> enumKeys = waypoints.keys();
        while (enumKeys.hasMoreElements()) {
            String key = enumKeys.nextElement();
            waypoints.get(key).renderShape();
        }
        for (RestrictedArea restrictedArea: restArray) {
            restrictedArea.renderShape();
        }
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
    }
}
