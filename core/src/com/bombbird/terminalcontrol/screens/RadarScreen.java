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
import com.bombbird.terminalcontrol.screens.ui.CommBox;
import com.bombbird.terminalcontrol.screens.ui.Tab;
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
    public String[] centreFreq;
    public int trajectoryLine = 90;
    public boolean liveWeather = true;
    public float radarSweepDelay = 2f; //TODO Change radar sweep delay in settings for unlocks

    //Score of current game
    private float planesToControl; //To keep track of how well the user is coping; number of arrivals to control is approximately 2/3 this value
    private int score; //Score of the player; equal to the number of planes landed without a separation incident (with other traffic or terrain)
    private int highScore; //High score of player

    private int arrivals;
    private float spawnTimer;

    //Timer for getting METAR every quarter of hour
    private Timer timer;
    private Metar metar;

    //Timer for updating aircraft radar returns, trails and save every given amount of time
    private float radarTime;
    private float trailTime;
    private float saveTime;

    //Stores callsigns of all aircrafts generated and aircrafts waiting to be generated (for take offs)
    private HashMap<String, Boolean> allAircraft;

    //Waypoint manager for managing waypoint selected status
    private WaypointManager waypointManager;

    //Separation checker for checking separation between aircrafts & terrain
    public SeparationChecker separationChecker;

    //Manages arrival traffic to prevent conflict prior to handover
    private ArrivalManager arrivalManager;

    //Communication box to keep track of aircraft transmissions
    private CommBox commBox;

    private Aircraft selectedAircraft;

    private JSONObject save;

    public RadarScreen(final TerminalControl game, String name, int airac, int saveID) {
        //Creates new game
        super(game);
        save = null;
        mainName = name;
        this.airac = airac;
        saveId = saveID;

        planesToControl = 1f;
        score = 0;
        highScore = 0;
        arrivals = 0;
        spawnTimer = 0;

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = radarSweepDelay;
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
        spawnTimer = (float) save.getDouble("spawnTimer");

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = (float) save.getDouble("radarTime");
        trailTime = (float) save.getDouble("trailTime");
        saveTime = 60f;

        trajectoryLine = save.getInt("trajectoryLine");
        liveWeather = save.getBoolean("liveWeather");
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

        labelStage = new Stage(new ScalingViewport(Scaling.fillY, 5760, 3240));
        labelStage.getViewport().setCamera(camera);
        labelStage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);

        //Set timer for METAR
        timer = new Timer(true);
    }

    private void loadInputProcessors() {
        //Set input processors
        inputMultiplexer.addProcessor(uiStage);
        inputMultiplexer.addProcessor(labelStage);
        inputMultiplexer.addProcessor(gd);
        inputMultiplexer.addProcessor(this);
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
                case 5: centreFreq = s.split(">"); break;
                default:
                    int index1 = 0;
                    String icao = "";
                    int elevation = 0;
                    int aircraftRatio = 0;
                    for (String s1: s.split(" ")) {
                        switch (index1) {
                            case 0: icao = s1; break;
                            case 1: elevation = Integer.parseInt(s1); break;
                            case 2: aircraftRatio = Integer.parseInt(s1); break;
                            default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + mainName + "/airport.arpt");
                        }
                        index1++;
                    }
                    Airport airport = new Airport(icao, elevation, aircraftRatio);
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

        metar = save == null ? new Metar(this) : new Metar(this, save.getJSONObject("metar"));

        //Update the METAR every quarter of the hour
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                metar.updateMetar();
            }
        }, calendar.getTime(), 900000);

        if (save == null) metar.updateMetar(); //Update the current airport METAR if not from save (airports not loaded in save at this stage)
    }

    /** Generates initial aircrafts (to prevent user getting overwhelmed at the start) */
    public void newAircraft() {
        if (save != null) return;

        //Spawn another 4 aircrafts after 2 minute intervals
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                planesToControl += 1.5;
            }
        }, 120000);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                planesToControl += 1.5;
            }
        }, 240000);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                planesToControl += 1.5;
            }
        }, 360000);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                planesToControl += 1.5;
            }
        }, 480000);
    }

    /** Creates a new departure at the given airport */
    public void newDeparture(String callsign, String icaoType, Airport airport, Runway runway) {
        aircrafts.put(callsign, new Departure(callsign, icaoType, airport, runway));
    }

    /** Creates a new arrival for random airport */
    private void newArrival() {
        Airport airport = RandomGenerator.randomAirport();

        String[] aircraftInfo = RandomGenerator.randomPlane(airport);
        Arrival arrival = new Arrival(aircraftInfo[0], aircraftInfo[1], airport);
        arrivalManager.checkArrival(arrival);
        aircrafts.put(aircraftInfo[0], arrival);
        arrivals++;
        spawnTimer = 45f;
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

        //Load aircraft callsign hashMap
        allAircraft = new HashMap<String, Boolean>();

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

        //Load communication box
        commBox = new CommBox();

        //Load METARs
        loadMetar();

        loadInputProcessors();
    }

    /** Updates the time values for each timer & runs tasks when time is reached */
    private void updateTimers() {
        radarTime -= Gdx.graphics.getDeltaTime();
        if (radarTime <= 0) {
            updateRadarInfo();
            radarTime += radarSweepDelay;
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

        spawnTimer -= Gdx.graphics.getDeltaTime();
        if (spawnTimer <= 0 && arrivals < planesToControl * 2 / 3) {
            //Ensure at least 45 sec interval between each new plane
            newArrival();
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
        //Regenerates textures if disposed
        Ui.generatePaneTextures();
        Aircraft.setLoadedIcons(false);
        Tab.setLoadedStyles(false);

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
        if (Aircraft.SKIN != null) Aircraft.SKIN.dispose();
        if (Aircraft.ICON_ATLAS != null) Aircraft.ICON_ATLAS.dispose();
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

    public void setArrivalManager(ArrivalManager arrivalManager) {
        this.arrivalManager = arrivalManager;
    }

    public Metar getMetar() {
        return metar;
    }

    public float getSpawnTimer() {
        return spawnTimer;
    }

    public CommBox getCommBox() {
        return commBox;
    }

    public void setCommBox(CommBox commBox) {
        this.commBox = commBox;
    }

    public HashMap<String, Boolean> getAllAircraft() {
        return allAircraft;
    }

    public void setAllAircraft(HashMap<String, Boolean> allAircraft) {
        this.allAircraft = allAircraft;
    }
}
