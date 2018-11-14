package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.*;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.SeparationChecker;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.Metar;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import com.bombbird.terminalcontrol.entities.waypoints.WaypointManager;
import com.bombbird.terminalcontrol.screens.ui.Ui;
import com.bombbird.terminalcontrol.utilities.FileLoader;

import java.util.*;

public class RadarScreen extends GameScreen {
    public static String MAIN_NAME;
    public static float MAG_HDG_DEV;
    public static int MAX_ALT;
    public static int MIN_ALT;
    public static int SEPARATION_MINIMA;
    public static int AIRAC;
    public static float RADAR_SWEEP_DELAY = 2f; //TODO Change radar sweep delay in UI

    //Score of current game
    public float planesToControl = 4; //To keep track of how well the user is coping; number of planes to control is approximately this number
    private static int score = 0; //Score of the player; equal to the number of planes landed without a separation incident (with other traffic or terrain)
    private static int highScore = 0; //High score of player

    //Timer for getting METAR every quarter of hour
    private Timer timer;
    private static Metar METAR;

    //Timer for updating aircraft radar returns every given amount of time
    private com.badlogic.gdx.utils.Timer radarTimer;

    //Waypoint manager for managing waypoint selected status
    private WaypointManager waypointManager;

    //Separation checker for checking separation between aircrafts & terrain
    public static SeparationChecker SEPARATION_CHECKER;

    private static Aircraft SELECTED_AIRCRAFT;

    public RadarScreen(final TerminalControl game, String name, int airac) {
        super(game);
        MAIN_NAME = name;
        AIRAC = airac;

        //Set stage params
        STAGE = new Stage(new ScalingViewport(Scaling.fillY, 5760, 3240));
        STAGE.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);

        //Set camera params
        camera = (OrthographicCamera) STAGE.getViewport().getCamera();
        camera.setToOrtho(false,5760, 3240);
        viewport = new ScalingViewport(Scaling.fillY, TerminalControl.WIDTH, TerminalControl.HEIGHT, camera);
        viewport.apply();
        camera.position.set(1890, 1620, 0);
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            camera.position.set(2286, 1620, 0);
        }

        //Set timer for METAR
        timer = new Timer(true);

        //Set timer for radar delay
        radarTimer = new com.badlogic.gdx.utils.Timer();

        waypointManager = new WaypointManager(); }

    private void loadInputProcessors() {
        //Set input processors
        inputProcessor2 = STAGE;
        inputProcessor3 = UI_STAGE;
        inputMultiplexer.addProcessor(inputProcessor3);
        inputMultiplexer.addProcessor(inputProcessor2);
        inputMultiplexer.addProcessor(gd);
        inputMultiplexer.addProcessor(inputProcessor1);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void loadPanel() {
        //Set 2nd stage, camera for UI
        UI_STAGE = new Stage(new ExtendViewport(1920, 3240));
        UI_STAGE.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);
        UI = new Ui();

        uiCam = (OrthographicCamera) UI_STAGE.getViewport().getCamera();
        uiCam.setToOrtho(false, 1920, 3240);
        uiViewport = new ExtendViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, uiCam);
        uiViewport.apply();
        uiCam.position.set(2880, 1620, 0);
    }

    private void loadAirports() {
        //Load airport information form file, add to hashmap
        FileHandle handle = Gdx.files.internal("game/" + MAIN_NAME +"/" + Integer.toString(AIRAC) + "/airport.arpt");
        int index = 0;
        for (String s: handle.readString().split("\\r?\\n")) {
            switch (index) {
                case 0: MIN_ALT = Integer.parseInt(s); break;
                case 1: MAX_ALT = Integer.parseInt(s); break;
                case 2: SEPARATION_MINIMA = Integer.parseInt(s); break;
                case 3: MAG_HDG_DEV = Float.parseFloat(s); break;
                default:
                    int index1 = 0;
                    String icao = "";
                    int elevation = 0;
                    for (String s1: s.split(" ")) {
                        switch (index1) {
                            case 0: icao = s1; break;
                            case 1: elevation = Integer.parseInt(s1); break;
                            default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + MAIN_NAME + "/airport.arpt");
                        }
                        index1++;
                    }
                    Airport airport = new Airport(icao, elevation);
                    airport.loadOthers();
                    AIRPORTS.put(icao, airport);
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

        METAR = new Metar(this);

        //Update the METAR every quarter of the hour
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                METAR.updateMetar();
            }
        }, calendar.getTime(), 900000);

        METAR.updateMetar();
    }

    public void newAircraft() {
        //Creates new aircrafts TODO: Auto-generate aircraft; remove this function after it is done
        AIRCRAFTS.put("EVA226", new Arrival("EVA226", "B77W", AIRPORTS.get("RCTP")));
        AIRCRAFTS.put("UIA231", new Arrival("UIA231", "A321", AIRPORTS.get("RCSS")));
        AIRCRAFTS.put("CAL753", new Arrival("CAL753", "A333", AIRPORTS.get("RCTP")));
        AIRCRAFTS.put("EVA851", new Arrival("EVA851", "A321", AIRPORTS.get("RCTP")));
        //AIRCRAFTS.put("ANA788", new Departure("ANA788", "B789", AIRPORTS.get("RCTP"), AIRPORTS.get("RCTP").getRunways().get("05L")));
        //AIRCRAFTS.put("UIA232", new Departure("UIA232", "A321", AIRPORTS.get("RCSS"), AIRPORTS.get("RCSS").getRunways().get("10")));
    }

    /** Creates a new departure at the given airport */
    public static void newDeparture(String callsign, String icaoType, Airport airport, Runway runway) {
        AIRCRAFTS.put(callsign, new Departure(callsign, icaoType, airport, runway));
    }

    /** Creates a new arrival for the given airport */
    public static void newArrival() {
        //TODO Auto-generate arrivals
    }


    /** Loads the full UI for RadarScreen */
    private void loadUI() {
        //Reset stage
        STAGE.clear();

        //Show loading screen
        loading = true;

        //Load range circles
        loadRange();

        //Load waypoints
        WAYPOINTS = FileLoader.loadWaypoints();

        //Load airports
        loadAirports();

        //Load separation checker
        SEPARATION_CHECKER = new SeparationChecker();
        STAGE.addActor(SEPARATION_CHECKER);

        //Load obstacles
        OBS_ARRAY = FileLoader.loadObstacles();

        //Load altitude restrictions
        REST_ARRAY = FileLoader.loadRestricted();

        //Load panels
        loadPanel();
        UI.setNormalPane(true);
        UI.setSelectedPane(null);

        //Load METARs
        loadMetar();

        loadInputProcessors();

        loadUpdateTimer();
    }

    private void loadUpdateTimer() {
        radarTimer.scheduleTask(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                updateRadarInfo();
            }
        }, RADAR_SWEEP_DELAY, RADAR_SWEEP_DELAY);
    }

    private void updateRadarInfo() {
        for (Aircraft aircraft: AIRCRAFTS.values()) {
            aircraft.updateRadarInfo();
        }
    }

    @Override
    public void renderShape() {
        //Updates waypoints status
        waypointManager.update();

        //Updates aircraft separation status
        SEPARATION_CHECKER.update();

        //Draw obstacles
        for (Obstacle obstacle: OBS_ARRAY) {
            obstacle.renderShape();
        }

        //Draw restricted areas
        for (RestrictedArea restrictedArea: REST_ARRAY) {
            restrictedArea.renderShape();
        }

        //Additional adjustments for certain airports
        SHAPE_RENDERER.setColor(Color.BLACK);
        if (MAIN_NAME.equals("RCTP")) {
            SHAPE_RENDERER.line(4500, 2416, 4500, 2124);
            SHAPE_RENDERER.line(1256, 2050, 1256, 1180);
        }

        //Draw runway(s) for each airport
        for (Airport airport: AIRPORTS.values()) {
            airport.renderRunways();
        }

        //Draw waypoints
        for (Waypoint waypoint: WAYPOINTS.values()) {
            waypoint.renderShape();
        }

        //Draw aircrafts
        for (Aircraft aircraft: AIRCRAFTS.values()) {
            aircraft.renderShape();
        }

        //Draw ILS arcs
        for (Airport airport: AIRPORTS.values()) {
            for (ILS ils: airport.getApproaches().values()) {
                ils.renderShape();
            }
        }

        SHAPE_RENDERER.end();
        SHAPE_RENDERER.begin(ShapeRenderer.ShapeType.Filled);
    }

    @Override
    public void show() {
        //Implements show method of screen, loads UI after show is called
        loadUI();
    }

    @Override
    public void dispose() {
        //Implements dispose method of screen, disposes resources after they're no longer needed
        UI_STAGE.clear();
        UI_STAGE.dispose();
        STAGE.clear();
        STAGE.dispose();
        UI.dispose();
        skin.dispose();
        Aircraft.SKIN.dispose();
        Aircraft.ICON_ATLAS.dispose();
    }

    public static void setSelectedAircraft(Aircraft aircraft) {
        if (SELECTED_AIRCRAFT != null) {
            SELECTED_AIRCRAFT.setSelected(false);
        }
        if (aircraft != null) {
            aircraft.setSelected(true);
        }

        if (aircraft != null && (aircraft.getControlState() == 1 || aircraft.getControlState() == 2)) {
            UI.setSelectedPane(aircraft);
            UI.setNormalPane(false);
        } else {
            UI.setNormalPane(true);
            UI.setSelectedPane(null);
        }
        SELECTED_AIRCRAFT = aircraft;
    }

    public static Aircraft getSelectedAircraft() {
        return SELECTED_AIRCRAFT;
    }

    public float getPlanesToControl() {
        return planesToControl;
    }

    public void setPlanesToControl(float planesToControl) {
        this.planesToControl = planesToControl;
    }

    public static int getScore() {
        return score;
    }

    public static void setScore(int score) {
        RadarScreen.score = score;
        if (score > highScore) {
            highScore = score;
        }
        UI.updateScoreLabels();
    }

    public static int getHighScore() {
        return highScore;
    }
}
