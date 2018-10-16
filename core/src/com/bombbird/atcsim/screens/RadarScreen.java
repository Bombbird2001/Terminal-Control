package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.*;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.ILS;
import com.bombbird.atcsim.entities.Metar;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.aircrafts.Aircraft;
import com.bombbird.atcsim.entities.aircrafts.Arrival;
import com.bombbird.atcsim.entities.aircrafts.Departure;
import com.bombbird.atcsim.entities.restrictions.Obstacle;
import com.bombbird.atcsim.entities.restrictions.RestrictedArea;
import com.bombbird.atcsim.screens.Ui.Ui;
import com.bombbird.atcsim.utilities.FileLoader;

import java.util.*;

public class RadarScreen extends GameScreen {
    public static String mainName;
    public static float magHdgDev;
    public static int maxDeptAlt;
    public static int maxArrAlt;
    public static int minArrAlt;
    private Timer timer;
    private static Metar metar;
    private static Aircraft selectedAircraft;

    public RadarScreen(final AtcSim game, String name) {
        super(game);
        mainName = name;

        //Set stage params
        stage = new Stage(new ScalingViewport(Scaling.fillY, 5760, 3240));
        stage.getViewport().update(AtcSim.WIDTH, AtcSim.HEIGHT, true);

        //Set camera params
        camera = (OrthographicCamera) stage.getViewport().getCamera();
        camera.setToOrtho(false,5760, 3240);
        viewport = new ScalingViewport(Scaling.fillY, AtcSim.WIDTH, AtcSim.HEIGHT, camera);
        viewport.apply();
        camera.position.set(1890, 1620, 0);
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            camera.position.set(2286, 1620, 0);
        }

        //Set aircraft array
        aircrafts = new HashMap<String, Aircraft>();

        //Set timer for METAR
        timer = new Timer(true);
    }

    private void loadInputProcessors() {
        //Set input processors
        inputProcessor2 = stage;
        inputProcessor3 = uiStage;
        inputMultiplexer.addProcessor(inputProcessor3);
        inputMultiplexer.addProcessor(inputProcessor2);
        inputMultiplexer.addProcessor(gd);
        inputMultiplexer.addProcessor(inputProcessor1);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void loadPanel() {
        //Set 2nd stage, camera for UI
        uiStage = new Stage(new ExtendViewport(1920, 3240));
        uiStage.getViewport().update(AtcSim.WIDTH, AtcSim.HEIGHT, true);
        ui = new Ui();

        uiCam = (OrthographicCamera) uiStage.getViewport().getCamera();
        uiCam.setToOrtho(false, 1920, 3240);
        uiViewport = new ExtendViewport(AtcSim.WIDTH, AtcSim.HEIGHT, uiCam);
        uiViewport.apply();
        uiCam.position.set(2880, 1620, 0);
    }

    private void loadAirports() {
        //Load airport information form file, add to hashmap
        FileHandle handle = Gdx.files.internal("game/" + mainName +"/airport.arpt");
        int index = 0;
        for (String s: handle.readString().split("\\r?\\n")) {
            switch (index) {
                case 0: minArrAlt = Integer.parseInt(s); break;
                case 1: maxArrAlt = Integer.parseInt(s); break;
                case 2: maxDeptAlt = Integer.parseInt(s); break;
                case 3: magHdgDev = Float.parseFloat(s); break;
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

    private void loadMetar() {
        //Load METAR info for airports, sets it for each airport
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.add(Calendar.MINUTE, 15);
        int minute = calendar.get(Calendar.MINUTE);
        if (minute >= 45) {
            minute = 45;
        } else if (minute >= 30) {
            minute = 30;
        } else if (minute >= 15) {
            minute = 15;
        } else {
            minute = 0;
        }
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        System.out.println(calendar.getTime().toString());

        metar = new Metar(this);

        //Update the METAR every quarter of the hour
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                metar.updateMetar();
            }
        }, calendar.getTime(), 900000);

        metar.updateMetar();
    }

    public void newAircraft() {
        //Creates new aircrafts TODO: Auto-generate aircraft
        aircrafts.put("EVA226", new Arrival("EVA226", "B77W", airports.get("RCTP")));
        aircrafts.put("UIA231", new Arrival("UIA231", "A321", airports.get("RCSS")));
        aircrafts.put("CAL753", new Arrival("CAL753", "A333", airports.get("RCTP")));
        aircrafts.put("EVA851", new Arrival("EVA851", "A321", airports.get("RCTP")));
        aircrafts.put("ANA788", new Departure("ANA788", "B789", airports.get("RCTP")));
        aircrafts.put("UIA232", new Departure("UIA232", "A321", airports.get("RCSS")));
    }

    private void loadUI() {
        //Loads the full UI of radarscreen

        //Reset stage
        stage.clear();

        //Show loading screen
        loading = true;

        //Load range circles
        loadRange();

        //Load waypoints
        waypoints = FileLoader.loadWaypoints();

        //Load airports
        loadAirports();

        //Load obstacles
        obsArray = FileLoader.loadObstacles();

        //Load altitude restrictions
        restArray = FileLoader.loadRestricted();

        //Load panels
        loadPanel();
        ui.setNormalPane(true);
        ui.setSelectedPane(null);

        //Load METARs
        loadMetar();

        loadInputProcessors();
    }

    @Override
    public void renderShape() {
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

        //Draw ILS arcs
        for (Airport airport: airports.values()) {
            for (ILS ils: airport.getApproaches().values()) {
                ils.renderShape();
            }
        }

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void show() {
        //Implements show method of screen, loads UI after show is called
        loadUI();
    }

    @Override
    public void dispose() {
        //Implements dispose method of screen, disposes resources after they're no longer needed
        uiStage.clear();
        uiStage.dispose();
        stage.clear();
        stage.dispose();
        ui.dispose();
        skin.dispose();
        Aircraft.skin.dispose();
        Aircraft.iconAtlas.dispose();
    }

    public static Aircraft getSelectedAircraft() {
        return selectedAircraft;
    }

    public static void setSelectedAircraft(Aircraft aircraft) {
        if (selectedAircraft != null) {
            selectedAircraft.setSelected(false);
            if (selectedAircraft != aircraft) {
                selectedAircraft.updateSelectedWaypoints(aircraft);
            }
        }
        if (aircraft != null) {
            aircraft.setSelected(true);
        }

        if (aircraft != null && (aircraft.getControlState() == 1 || aircraft.getControlState() == 2)) {
            ui.setSelectedPane(aircraft);
            ui.setNormalPane(false);
        } else {
            ui.setNormalPane(true);
            ui.setSelectedPane(null);
        }
        selectedAircraft = aircraft;
    }
}
