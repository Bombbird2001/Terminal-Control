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
import com.bombbird.terminalcontrol.entities.*;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.trafficmanager.ArrivalManager;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import com.bombbird.terminalcontrol.entities.waypoints.WaypointManager;
import com.bombbird.terminalcontrol.screens.ui.Ui;
import com.bombbird.terminalcontrol.utilities.FileLoader;
import com.bombbird.terminalcontrol.utilities.GameLoader;
import com.bombbird.terminalcontrol.utilities.GameSaver;
import org.json.JSONObject;

import java.util.*;

public class RadarScreen extends GameScreen {
    public int saveId;
    public String mainName;
    public float magHdgDev;
    public int maxAlt;
    public int minAlt;
    public int transLvl;
    public int separationMinima;
    public int airac;
    public static float RADAR_SWEEP_DELAY = 2f; //TODO Change radar sweep delay in settings

    //Score of current game
    private float planesToControl; //To keep track of how well the user is coping; number of arrivals to control is approximately this number
    private int score; //Score of the player; equal to the number of planes landed without a separation incident (with other traffic or terrain)
    private int highScore; //High score of player

    private int arrivals;

    //Timer for getting METAR every quarter of hour
    private Timer timer;
    private Metar metar;

    //Timer for updating aircraft radar returns, trails and save every given amount of time
    private float radarTime;
    private float trailTime;
    private float saveTime;

    //Waypoint manager for managing waypoint selected status
    private WaypointManager waypointManager;

    //Separation checker for checking separation between aircrafts & terrain
    public SeparationChecker separationChecker;

    //Manages arrival traffic to prevent conflict prior to handover
    private ArrivalManager arrivalManager;

    private Aircraft selectedAircraft;

    private JSONObject save;

    public RadarScreen(final TerminalControl game, String name, int airac, int saveID) {
        //Creates new game
        super(game);
        save = null;
        mainName = name;
        this.airac = airac;
        saveId = saveID;

        planesToControl = 2f;
        score = 0;
        highScore = 0;
        arrivals = 0;

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = RADAR_SWEEP_DELAY;
        trailTime = 10f;
        saveTime = 60f;
    }

    public RadarScreen(final TerminalControl game, JSONObject save) {
        //Loads the game from save
        super(game);
        this.save = save;
        saveId = save.getInt("saveId");
        mainName = save.getString("MAIN_NAME");
        airac = save.getInt("AIRAC");

        planesToControl = (float) save.getDouble("planesToControl");
        score = save.getInt("score");
        highScore = save.getInt("highScore");
        arrivals = save.getInt("arrivals");

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = (float) save.getDouble("radarTime");
        trailTime = (float) save.getDouble("trailTime");
        saveTime = 60f;
    }

    private void loadStageCamTimer() {
        //Set stage params
        stage = new Stage(new ScalingViewport(Scaling.fillY, 5760, 3240));
        stage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);

        //Set camera params
        camera = (OrthographicCamera) stage.getViewport().getCamera();
        camera.setToOrtho(false,5760, 3240);
        viewport = new ScalingViewport(Scaling.fillY, TerminalControl.WIDTH, TerminalControl.HEIGHT, camera);
        viewport.apply();
        camera.position.set(1890, 1620, 0);
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            camera.position.set(2286, 1620, 0);
        }

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
        uiStage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);
        ui = new Ui();

        uiCam = (OrthographicCamera) uiStage.getViewport().getCamera();
        uiCam.setToOrtho(false, 1920, 3240);
        uiViewport = new ExtendViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, uiCam);
        uiViewport.apply();
        uiCam.position.set(2880, 1620, 0);
    }

    private void loadAirports() {
        //Load airport information form file, add to hashmap
        FileHandle handle = Gdx.files.internal("game/" + mainName +"/" + airac + "/airport.arpt");
        int index = 0;
        for (String s: handle.readString().split("\\r?\\n")) {
            switch (index) {
                case 0: minAlt = Integer.parseInt(s); break;
                case 1: maxAlt = Integer.parseInt(s); break;
                case 2: transLvl = Integer.parseInt(s); break;
                case 3: separationMinima = Integer.parseInt(s); break;
                case 4: magHdgDev = Float.parseFloat(s); break;
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
                    Airport airport = new Airport(icao, elevation);
                    airport.loadOthers();
                    airports.put(icao, airport);
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

    /** Generates initial aircrafts (to prevent user getting overwhelmed at the start) */
    public void newAircraft() {
        aircrafts.put("EVA226", new Arrival("EVA226", "B77W", airports.get("RCTP")));
        arrivals++;

        //Spawn another 2 aircrafts after 1.5 minute intervals
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                planesToControl += 1;
            }
        }, 90000);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                planesToControl += 1;
            }
        }, 180000);
    }

    /** Creates a new departure at the given airport */
    public void newDeparture(String callsign, String icaoType, Airport airport, Runway runway) {
        aircrafts.put(callsign, new Departure(callsign, icaoType, airport, runway));
    }

    /** Creates a new arrival for random airport */
    private void newArrival() {
        if (arrivals < planesToControl * 2 / 3) {
            String[] aircraftInfo = RandomGenerator.randomPlane();
            while (aircrafts.get(aircraftInfo[0]) != null) {
                //Ensures there are no duplicates
                aircraftInfo = RandomGenerator.randomPlane();
            }
            Arrival arrival = new Arrival(aircraftInfo[0], aircraftInfo[1], RandomGenerator.randomAirport());
            arrivalManager.checkArrival(arrival);
            aircrafts.put(aircraftInfo[0], arrival);
            arrivals++;
        }
    }

    /** Loads the full UI for RadarScreen */
    private void loadUI() {
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

        //Load separation checker
        separationChecker = new SeparationChecker();
        stage.addActor(separationChecker);

        //Load waypoint manager
        waypointManager = new WaypointManager();

        //Load arrival manager
        arrivalManager = new ArrivalManager();

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

    /** Updates the time values for each timer & runs tasks when time is reached */
    private void updateTimers() {
        radarTime -= Gdx.graphics.getDeltaTime();
        if (radarTime <= 0) {
            updateRadarInfo();
            radarTime += RADAR_SWEEP_DELAY;
        }

        trailTime -= Gdx.graphics.getDeltaTime();
        if (trailTime <= 0) {
            addTrailDot();
            trailTime += 10f;
        }

        saveTime -= Gdx.graphics.getDeltaTime();
        if (saveTime <= 0) {
            GameSaver.saveGame();
            saveTime += 60f;
        }
    }

    /** Sets the radar return of aircraft to current aircraft information */
    private void updateRadarInfo() {
        for (Aircraft aircraft: aircrafts.values()) {
            aircraft.updateRadarInfo();
        }
    }

    /** Adds a new trail dot value to the aircraft's trail queue */
    private void addTrailDot() {
        for (Aircraft aircraft: aircrafts.values()) {
            aircraft.addTrailDot();
        }
    }

    @Override
    public void renderShape() {
        //Update timers
        updateTimers();

        //Check whether new aircrafts needs to be generated
        newArrival();

        //Updates waypoints status
        waypointManager.update();

        //Updates aircraft separation status
        separationChecker.update();

        //Draw obstacles
        for (Obstacle obstacle: obsArray) {
            obstacle.renderShape();
        }

        //Draw restricted areas
        for (RestrictedArea restrictedArea: restArray) {
            restrictedArea.renderShape();
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
        //Implements show method of screen, loads UI & save (if available) after show is called
        loadUI();
        GameLoader.loadSaveData(save);
    }

    @Override
    public void dispose() {
        //Implements dispose method of screen, disposes resources after they're no longer needed
        super.dispose();

        timer.cancel();
    }

    /** Disposes of static final variables after user quits app */
    public static void disposeStatic() {
        Aircraft.SKIN.dispose();
        Aircraft.ICON_ATLAS.dispose();
    }

    public void setSelectedAircraft(Aircraft aircraft) {
        if (selectedAircraft != null) {
            selectedAircraft.setSelected(false);
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

    public Aircraft getSelectedAircraft() {
        return selectedAircraft;
    }

    public float getPlanesToControl() {
        return planesToControl;
    }

    public void setPlanesToControl(float planesToControl) {
        this.planesToControl = planesToControl;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        if (score > highScore) {
            highScore = score;
        }
        ui.updateScoreLabels();
    }

    public int getHighScore() {
        return highScore;
    }

    public int getArrivals() {
        return arrivals;
    }

    public void setArrivals(int arrivals) {
        this.arrivals = arrivals;
    }

    public ArrivalManager getArrivalManager() {
        return arrivalManager;
    }

    public float getRadarTime() {
        return radarTime;
    }

    public float getTrailTime() {
        return trailTime;
    }
}
