package com.bombbird.terminalcontrol.screens.gamescreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.*;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager;
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.airports.AirportName;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle;
import com.bombbird.terminalcontrol.entities.runways.Runway;
import com.bombbird.terminalcontrol.entities.separation.AreaPenetrationChecker;
import com.bombbird.terminalcontrol.entities.separation.CollisionChecker;
import com.bombbird.terminalcontrol.entities.separation.SeparationChecker;
import com.bombbird.terminalcontrol.entities.separation.trajectory.TrajectoryStorage;
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR;
import com.bombbird.terminalcontrol.entities.trafficmanager.MaxTraffic;
import com.bombbird.terminalcontrol.entities.waketurbulence.WakeManager;
import com.bombbird.terminalcontrol.entities.waypoints.BackupWaypoints;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.waypoints.WaypointManager;
import com.bombbird.terminalcontrol.entities.weather.Metar;
import com.bombbird.terminalcontrol.entities.weather.WindDirChance;
import com.bombbird.terminalcontrol.entities.weather.WindshearChance;
import com.bombbird.terminalcontrol.entities.weather.WindspeedChance;
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.TrafficFlowScreen;
import com.bombbird.terminalcontrol.ui.*;
import com.bombbird.terminalcontrol.ui.tabs.Tab;
import com.bombbird.terminalcontrol.sounds.Pronunciation;
import com.bombbird.terminalcontrol.utilities.RenameManager;
import com.bombbird.terminalcontrol.utilities.Revision;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import com.bombbird.terminalcontrol.utilities.math.RandomGenerator;
import com.bombbird.terminalcontrol.ui.tutorial.TutorialManager;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

import java.util.*;

public class RadarScreen extends GameScreen {
    public float getPreviousOffset() {
        return previousOffset;
    }

    public enum Weather {
        LIVE,
        RANDOM,
        STATIC
    }

    public enum TfcMode {
        NORMAL,
        ARRIVALS_ONLY
    }

    public int saveId;
    public String mainName;
    public float magHdgDev;
    public int maxAlt;
    public int minAlt;
    public int transLvl;
    public int separationMinima;
    public int divertHdg;
    public int airac;
    public String callsign;
    public String deptCallsign;
    public int trajectoryLine;
    public int pastTrajTime;
    public Weather weatherSel;
    public int soundSel;
    public Emergency.Chance emerChance;
    public TfcMode tfcMode;
    public boolean allowNight;
    public int nightStart;
    public int nightEnd;
    public float radarSweepDelay;
    public int advTraj;
    public int areaWarning;
    public int collisionWarning;
    public boolean showMva;
    public boolean showIlsDash;
    public boolean compactData;
    public boolean showUncontrolled;
    public boolean alwaysShowBordersBackground;
    public int lineSpacingValue;
    public int colourStyle;
    public boolean realisticMetar;

    //Advanced traffic settings
    public int trafficMode;
    public int maxPlanes;
    public int flowRate;

    //Whether the game is a tutorial
    public boolean tutorial = false;

    //Score of current game
    private float planesToControl; //To keep track of how well the user is coping; number of arrivals to control is approximately this value
    private int score; //Score of the player; equal to the number of planes landed without a separation incident (with other traffic or terrain)
    private int highScore; //High score of player

    //Fun stats
    private int arrivals;
    private int separationIncidents;
    private float wakeInfringeTime;
    private int emergenciesLanded;

    private float spawnTimer;
    private float previousOffset;

    private char information;

    //Timer for getting METAR every quarter of hour
    private Timer timer;
    private Metar metar;

    //Timer for updating aircraft radar returns, trails, save and discord RPC every given amount of time
    private float radarTime;
    private float trailTime;
    private float saveTime;
    private float rpcTime;

    //Stores callsigns of all aircraft generated and aircraft waiting to be generated (for take offs)
    private HashSet<String> allAircraft;

    //Waypoint manager for managing waypoint selected status
    public WaypointManager waypointManager;

    //Separation checker for checking separation between aircraft & terrain
    public SeparationChecker separationChecker;

    //Trajectory storage, APW, STCAS
    public TrajectoryStorage trajectoryStorage;
    public AreaPenetrationChecker areaPenetrationChecker;
    public CollisionChecker collisionChecker;

    //Wake turbulence checker
    public WakeManager wakeManager;

    //Tutorial manager
    public TutorialManager tutorialManager = null;

    //Communication box to keep track of aircraft transmissions
    private CommBox commBox;

    //Runway change box for changing runway configuration
    private RunwayChanger runwayChanger;

    //The selected aircraft
    private Aircraft selectedAircraft;

    //Simultaneous landing achievement storage
    private final LinkedHashMap<String, Float> simultaneousLanding;

    //Easter egg thing yey
    private com.badlogic.gdx.utils.Queue<Character> lastTapped;

    private final JSONObject save;
    private final int revision; //Revision for indicating if save parser needs to do anything special

    public RadarScreen(final TerminalControl game, String name, int airac, int saveID, boolean tutorial) {
        //Creates new game
        super(game);
        save = null;
        mainName = name;
        this.airac = airac;
        revision = Revision.CURRENT_REVISION;
        saveId = saveID;
        this.tutorial = tutorial;

        planesToControl = 6f;
        score = 0;
        highScore = 0;
        arrivals = 0;
        separationIncidents = 0;
        wakeInfringeTime = 0;
        emergenciesLanded = 0;
        spawnTimer = 0;
        previousOffset = 0;
        information = (char) MathUtils.random(65, 90);

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = radarSweepDelay;
        trailTime = 10f;
        saveTime = TerminalControl.saveInterval;
        rpcTime = 60f;

        simultaneousLanding = new LinkedHashMap<>();

        if (tutorial) {
            trajectoryLine = 90;
            pastTrajTime = -1;
            radarSweepDelay = 2;
            advTraj = -1;
            areaWarning = -1;
            collisionWarning = -1;
            showMva = true;
            showIlsDash = false;
            compactData = false;
            showUncontrolled = false;
            alwaysShowBordersBackground = true;
            rangeCircleDist = 0;
            lineSpacingValue = 1;
            colourStyle = 0;
            realisticMetar = false;
            emerChance = Emergency.Chance.OFF;
            soundSel = TerminalControl.getDefaultSoundSetting();
            weatherSel = Weather.STATIC;
        } else {
            trajectoryLine = TerminalControl.trajectorySel;
            pastTrajTime = TerminalControl.pastTrajTime;
            radarSweepDelay = TerminalControl.radarSweep;
            advTraj = TerminalControl.advTraj;
            areaWarning = TerminalControl.areaWarning;
            collisionWarning = TerminalControl.collisionWarning;
            showMva = TerminalControl.showMva;
            showIlsDash = TerminalControl.showIlsDash;
            compactData = TerminalControl.compactData;
            showUncontrolled = TerminalControl.showUncontrolled;
            alwaysShowBordersBackground = TerminalControl.alwaysShowBordersBackground;
            rangeCircleDist = TerminalControl.rangeCircleDist;
            lineSpacingValue = TerminalControl.lineSpacingValue;
            colourStyle = TerminalControl.colourStyle;
            realisticMetar = TerminalControl.realisticMetar;
            weatherSel = TerminalControl.weatherSel;
            soundSel = TerminalControl.soundSel;
            emerChance = TerminalControl.emerChance;
        }
        tfcMode = TfcMode.NORMAL;
        allowNight = true;
        nightStart = 2200;
        nightEnd = 600;

        trafficMode = 0;
        maxPlanes = -1;
        flowRate = -1;

        wakeManager = new WakeManager();

        loadEasterEggQueue();

        if (tutorial) {
            tutorialManager = new TutorialManager(this);
        }
    }

    public RadarScreen(final TerminalControl game, JSONObject save) {
        //Loads the game from save
        super(game);
        this.save = save;
        saveId = save.getInt("saveId");
        mainName = RenameManager.renameAirportICAO(save.getString("MAIN_NAME"));
        airac = save.getInt("AIRAC");
        revision = save.optInt("revision", 0);

        planesToControl = (float) save.getDouble("planesToControl");
        score = save.getInt("score");
        highScore = save.getInt("highScore");
        arrivals = save.getInt("arrivals");
        separationIncidents = save.optInt("separationIncidents", 0);
        wakeInfringeTime = (float) save.optDouble("wakeInfringeTime", 0);
        emergenciesLanded = save.optInt("emergenciesLanded", 0);
        spawnTimer = (float) save.optDouble("spawnTimer", 60);
        previousOffset = (float) save.optDouble("previousOffset", 0);
        information = save.isNull("information") ? (char) MathUtils.random(65, 90) : (char) save.getInt("information");

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = (float) save.optDouble("radarTime", TerminalControl.radarSweep);
        trailTime = (float) save.optDouble("trailTime", 10);
        saveTime = TerminalControl.saveInterval;
        rpcTime = 60f;

        trajectoryLine = save.optInt("trajectoryLine", 90);
        pastTrajTime = save.optInt("pastTrajTime", -1);
        radarSweepDelay = (float) save.optDouble("radarSweep", 2);
        advTraj = save.optInt("advTraj", -1);
        areaWarning = save.optInt("areaWarning", -1);
        collisionWarning = save.optInt("collisionWarning", -1);
        showMva = save.optBoolean("showMva", true);
        showIlsDash = save.optBoolean("showIlsDash", false);
        compactData = save.optBoolean("compactData", false);
        showUncontrolled = save.optBoolean("showUncontrolled", false);
        alwaysShowBordersBackground = save.optBoolean("alwaysShowBordersBackground", true);
        rangeCircleDist = save.optInt("rangeCircleDist", 0);
        lineSpacingValue = save.optInt("lineSpacingValue", 1);
        colourStyle = save.optInt("colourStyle", 0);
        realisticMetar = save.optBoolean("realisticMetar", false);
        String weather = save.optString("liveWeather");
        if ("true".equals(weather)) {
            weatherSel = Weather.LIVE;
        } else if ("false".equals(weather)) {
            weatherSel = Weather.RANDOM;
        } else {
            weatherSel = RadarScreen.Weather.valueOf(save.getString("liveWeather"));
        }
        soundSel = save.isNull("sounds") ? TerminalControl.getDefaultSoundSetting() : save.getInt("sounds");
        if (save.isNull("emerChance")) {
            emerChance = Emergency.Chance.MEDIUM;
        } else {
            emerChance = Emergency.Chance.valueOf(save.getString("emerChance"));
        }
        if (save.isNull("tfcMode")) {
            tfcMode = TfcMode.NORMAL;
        } else {
            tfcMode = TfcMode.valueOf(save.getString("tfcMode"));
        }
        allowNight = save.optBoolean("allowNight", true);
        nightStart = save.optInt("nightStart", 2200);
        nightEnd = save.optInt("nightEnd", 600);

        trafficMode = save.optInt("trafficMode", 0);
        maxPlanes = save.optInt("maxPlanes", -1);
        flowRate = save.optInt("flowRate", -1);

        wakeManager = save.isNull("wakeManager") ? new WakeManager() : new WakeManager(save.getJSONObject("wakeManager"));

        loadEasterEggQueue();

        simultaneousLanding = new LinkedHashMap<>();
    }

    private void loadEasterEggQueue() {
        lastTapped = new com.badlogic.gdx.utils.Queue<>();
        lastTapped.addLast(' ');
        lastTapped.addLast(' ');
    }

    private void loadBackupWaypoints() {
        if (save == null) return;
        if (save.isNull("backupWpts")) {
            //No backup waypoints saved, add possible backup ones in here
            HashMap<String, int[]> backup = BackupWaypoints.loadBackupWpts(mainName);
            for (Map.Entry<String, int[]> entry: backup.entrySet()) {
                if (waypoints.containsKey(entry.getKey())) continue;
                Waypoint newWpt = new Waypoint(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
                waypoints.put(entry.getKey(), newWpt);
                stage.addActor(newWpt);
            }
        } else {
            //Waypoints saved, load them directly instead
            JSONObject wpts = save.getJSONObject("backupWpts");
            for (String wptName: wpts.keySet()) {
                JSONObject wpt = wpts.getJSONObject(wptName);
                //Add the waypoint only if it doesn't exist
                if (!waypoints.containsKey(wptName)) {
                    Waypoint newWpt = new Waypoint(wptName, wpt.getInt("x"), wpt.getInt("y"));
                    waypoints.put(wptName, newWpt);
                    stage.addActor(newWpt);
                }
            }
        }
    }

    private void loadStageCamTimer() {
        //Set stage params
        stage = new Stage(new ScalingViewport(Scaling.fillY, 5760, 3240), game.batch);
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

        labelStage = new Stage(new ScalingViewport(Scaling.fillY, 5760, 3240), game.batch);
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
        uiStage = new Stage(new ExtendViewport(1920, 3240), game.batch);
        uiStage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);
        ui = new Ui();
        ui.loadTabs();

        uiCam = (OrthographicCamera) uiStage.getViewport().getCamera();
        uiCam.setToOrtho(false, 1920, 3240);
        uiViewport = new ExtendViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, uiCam);
        uiViewport.apply();
        uiCam.position.set(2880, 1620, 0);
    }

    private void loadAirports() {
        //Load airport information form file, add to hashmap
        FileHandle handle = Gdx.files.internal("game/" + mainName +"/" + airac + "/airport.arpt");
        JSONObject jo = new JSONObject(handle.readString());
        minAlt = jo.getInt("minAlt");
        maxAlt = jo.getInt("maxAlt");
        transLvl = jo.getInt("transLvl");
        separationMinima = jo.getInt("minSep");
        magHdgDev = (float) jo.getDouble("magHdgDev");
        callsign = jo.getString("apchCallsign");
        deptCallsign = jo.getString("depCallsign");

        JSONObject airports1 = jo.getJSONObject("airports");
        for (String icao: airports1.keySet()) {
            JSONObject airport1 = airports1.getJSONObject(icao);
            Airport airport = new Airport(icao, airport1.getInt("elevation"), airport1.getInt("ratio"));
            airport.loadOthers();
            airports.put(icao, airport);
            AirportName.airportNames.put(icao, airport1.getString("name"));
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

        metar = save == null ? new Metar(this) : new Metar(this, save.getJSONObject("metar"));

        //Update the METAR every quarter of the hour
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Gdx.app.postRunnable(() -> {
                    if (!runwayChanger.isVisible()) return;
                    runwayChanger.hideAll();
                    commBox.setVisible(true);
                });
                metar.updateMetar(tutorial);
            }
        }, calendar.getTime(), 900000);

        if (save == null) {
            metar.updateMetar(tutorial); //Update the current airport METAR if not from save (airports not loaded in save at this stage)
        }
    }

    /** Creates a new departure at the given airport */
    public void newDeparture(String callsign, String icaoType, Airport airport, com.bombbird.terminalcontrol.entities.runways.Runway runway) {
        aircrafts.put(callsign, new Departure(callsign, icaoType, airport, runway));
    }

    /** Creates a new arrival for random airport */
    private void newArrival() {
        if (trafficMode == TrafficFlowScreen.FLOW_RATE) {
            if (flowRate == 0) return;
            setPlanesToControl(arrivals);
            spawnTimer = -previousOffset; //Subtract the additional (or less) time before spawning previous aircraft
            float defaultRate = 3600f / flowRate;
            spawnTimer += defaultRate; //Add the constant rate timing
            previousOffset = defaultRate * MathUtils.random(-0.1f, 0.1f);
            spawnTimer += previousOffset;
        } else {
            //Min 50sec for >=4 planes diff, max 80sec for <=1 plane diff
            spawnTimer = 90f - 10 * (planesToControl - arrivals);
            spawnTimer = MathUtils.clamp(spawnTimer, 50, 80);
        }

        Airport airport = RandomGenerator.randomAirport();
        if (airport == null) {
            //If airports not available, set planes to control equal to current arrival number
            //so there won't be a sudden wave of new arrivals once airport is available again
            setPlanesToControl(arrivals);
            return;
        }
        if (!RandomSTAR.starAvailable(airport)) {
            spawnTimer = 10f; //Wait for another 10 seconds if no spawn points available
            return;
        }
        String[] aircraftInfo = RandomGenerator.randomPlane(airport);
        Arrival arrival = new Arrival(aircraftInfo[0], aircraftInfo[1], airport);
        aircrafts.put(aircraftInfo[0], arrival);
        arrivals++;
    }

    /** Loads the full UI for RadarScreen */
    private void loadUI() {
        //Reset stage
        stage.clear();

        //Show loading screen
        loading = true;
        loadingTime = 0;

        //Load range circles
        loadRange();

        //Load shoreline data
        Shoreline.loadShoreline();

        //Load waypoints
        waypoints = FileLoader.loadWaypoints();
        loadBackupWaypoints();

        //Load specific waypoint pronunciation
        Pronunciation.loadPronunciation();

        //Load maximum traffic limits for different airports
        MaxTraffic.loadHashmaps();

        //Load airports
        loadAirports();

        //Load separation checker
        separationChecker = new SeparationChecker();
        stage.addActor(separationChecker);

        //Load trajectory storage, APW, STCAS
        trajectoryStorage = new TrajectoryStorage();
        areaPenetrationChecker = new AreaPenetrationChecker();
        collisionChecker = new CollisionChecker();

        //Load aircraft callsign hashMap
        allAircraft = new HashSet<>();

        //Load waypoint manager
        waypointManager = new WaypointManager();

        //Load obstacles
        obsArray = FileLoader.loadObstacles();

        //Load panels
        loadPanel();
        ui.setNormalPane(true);
        ui.setSelectedPane(null);

        //Load communication box
        commBox = new CommBox();

        //Load runway change box
        runwayChanger = new RunwayChanger();

        //Load request flasher
        requestFlasher = new RequestFlasher(this);

        //Initialise tutorial manager if is tutorial
        if (tutorial) tutorialManager.init();

        //Load weather chances
        WindshearChance.loadWsChance();
        WindspeedChance.loadWindSpdChance();
        WindDirChance.loadWindDirChance();

        //Load METARs
        loadMetar();

        loadInputProcessors();
    }

    /** Updates the display state (flyover or not) for all waypoints */
    private void updateWaypointDisplay() {
        for (Waypoint waypoint: waypoints.values()) {
            waypoint.updateFlyOverStatus();
        }
    }

    /** Updates the time values for each timer & runs tasks when time is reached */
    private void updateTimers() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        radarTime -= deltaTime;
        if (radarTime <= 0) {
            updateRadarInfo();
            if (radarSweepDelay / speed < 0.25f) {
                radarTime += 0.25f * speed;
            } else {
                radarTime += radarSweepDelay;
            }
        }

        trailTime -= deltaTime;
        if (trailTime <= 0) {
            addTrailDot();
            trailTime += 10f;
        }

        rpcTime -= deltaTime;
        if (rpcTime <= 0) {
            TerminalControl.discordManager.updateRPC();
            rpcTime += 60f;
        }

        if (!tutorial) {
            if (TerminalControl.saveInterval > 0) {
                //If autosave enabled
                saveTime -= deltaTime;
                if (saveTime <= 0) {
                    GameSaver.saveGame();
                    saveTime += TerminalControl.saveInterval;
                }
            }

            arrivals = 0;
            for (Aircraft aircraft: aircrafts.values()) {
                if (aircraft instanceof Arrival && aircraft.getControlState() == Aircraft.ControlState.ARRIVAL) arrivals++;
            }

            spawnTimer -= deltaTime;
            if (spawnTimer <= 0 && arrivals < planesToControl) {
                //Minimum 50 sec interval between each new plane
                newArrival();
            }
        }

        if (!UnlockManager.unlocks.contains("parallelLanding")) {
            //Update simultaneous landing linkedhashmap
            Iterator<Map.Entry<String, Float>> iterator = simultaneousLanding.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Float> entry = iterator.next();
                entry.setValue(entry.getValue() + deltaTime);
                if (entry.getValue() > 5) iterator.remove();
            }
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
    public void updateTutorial() {
        if (tutorialManager != null) tutorialManager.update();
    }

    @Override
    public void update() {
        //Update timers
        updateTimers();

        //Updates waypoints status
        waypointManager.update();

        //Updates STAR timers
        RandomSTAR.update();

        //Update airport stuff
        for (Airport airport: airports.values()) {
            airport.update();
        }

        //Update runway changer timer
        runwayChanger.update();

        //Update tutorial stuff if is tutorial
        if (tutorialManager != null) {
            tutorialManager.update();
        }
    }

    @Override
    public void renderShape() {
        //Updates aircraft separation status
        separationChecker.update();

        //Update trajectory stuff (full version only)
        if (TerminalControl.full) {
            trajectoryStorage.update();
            areaPenetrationChecker.renderShape();
            collisionChecker.renderShape();
        }

        //Draw shoreline
        Shoreline.renderShape();

        //Draw obstacles
        Array<Obstacle> saveForLast = new Array<>();
        for (Obstacle obstacle : obsArray) {
            if (obstacle.isEnforced() || obstacle.isConflict() || obstacle.getLabel().getText().toString().charAt(0) == '#') {
                saveForLast.add(obstacle);
            } else {
                obstacle.renderShape();
            }
        }
        for (Obstacle obstacle : saveForLast) {
            obstacle.renderShape();
        }

        //Draw the range circles if present
        renderRangeCircles();

        //Additional adjustments for certain airports
        shapeRenderer.setColor(Color.BLACK);
        if ("TCTP".equals(mainName)) {
            shapeRenderer.line(4500, 2416, 4500, 2124);
            shapeRenderer.line(1256, 2050, 1256, 1180);
        }

        //Draw runway(s) for each airport
        for (Airport airport: airports.values()) {
            airport.renderRunways();
        }

        //Draw approach zone(s) for each airport
        for (Airport airport: airports.values()) {
            airport.renderZones();
        }

        //Draw waypoints, reset restriction display
        Array<Waypoint> flyOver = new Array<>();
        for (Waypoint waypoint: waypoints.values()) {
            waypoint.setRestrDisplay(-1, -1, -1); //Reset all restrictions before drawing in renderShape
            if (waypoint.isFlyOver()) {
                //Save flyovers for later
                flyOver.add(waypoint);
            } else {
                waypoint.renderShape();
            }
        }

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        //Draw filled flyover waypoints
        for (Waypoint waypoint: flyOver) {
            waypoint.renderShape();
        }

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        //Draw aircraft
        for (Aircraft aircraft: aircrafts.values()) {
            aircraft.renderShape();
        }

        //Draw ILS arcs
        for (Airport airport: airports.values()) {
            for (ILS ils: airport.getApproaches().values()) {
                ils.renderShape();
            }
        }

        separationChecker.renderShape();

        if (selectedAircraft != null) {
            wakeManager.renderWake(selectedAircraft);
        }
        wakeManager.renderIlsWake();

        shapeRenderer.end();
    }

    public void addToEasterEggQueue(Aircraft aircraft) {
        lastTapped.removeFirst();
        lastTapped.addLast(aircraft.getCallsign().charAt(0));
        checkEasterEgg();
    }

    private void checkEasterEgg() {
        if (lastTapped.size < 2) return;
        if (!UnlockManager.isTCHXAvailable() && lastTapped.first() == 'H' && lastTapped.last() == 'X') {
            //Easter egg unlocked
            UnlockManager.unlockEgg("HX");
            commBox.alertMsg("Congratulations, you have found the easter egg! A new airport is waiting for you!");
        }
    }

    public boolean addAndCheckSimultLanding(Arrival arrival) {
        for (String callsign: simultaneousLanding.keySet()) {
            Aircraft aircraft = aircrafts.get(callsign);
            if (!arrival.getAirport().equals(aircraft.getAirport())) continue;
            String airportIcao = arrival.getAirport().getIcao();
            if (arrival.getIls() == null) return false;
            String rwy1 = arrival.getIls().getRwy().getName();
            if (aircraft.getIls() == null) continue;
            String rwy2 = aircraft.getIls().getRwy().getName();
            String[] rwys = {rwy1, rwy2};
            if ("TCWS".equals(airportIcao)) {
                if (ArrayUtils.contains(rwys, "02L") && ArrayUtils.contains(rwys, "02C") || ArrayUtils.contains(rwys, "20R") && ArrayUtils.contains(rwys, "20C")) return true;
            } else if ("TCTT".equals(airportIcao) || "TCAA".equals(airportIcao)) {
                if (ArrayUtils.contains(rwys, "34L") && ArrayUtils.contains(rwys, "34R") || ArrayUtils.contains(rwys, "16R") && ArrayUtils.contains(rwys, "16L")) return true;
            } else if ("TCPG".equals(airportIcao)) {
                if (ArrayUtils.contains(rwys, "26L") && ArrayUtils.contains(rwys, "27R") || ArrayUtils.contains(rwys, "08R") && ArrayUtils.contains(rwys, "09L")) return true;
            }
        }
        simultaneousLanding.put(arrival.getCallsign(), 0f);
        return false;
    }

    /** Estimates the duration played (if save has no play time data) */
    private float estimatePlayTime() {
        int landed = 0;
        for (Airport airport: airports.values()) {
            landed += airport.getLandings();
        }

        //Assume 90 seconds between landings lol
        return landed * 90;
    }

    /** Updates the colour scheme of the radar screen */
    public void updateColourStyle() {
        //Runway label colour
        for (Airport airport: airports.values()) {
            for (Runway runway: airport.getRunways().values()) {
                runway.setLabelColor(getDefaultColour());
            }
        }
    }

    /** Gets the route/runway drawing colour depending on colour scheme */
    public Color getDefaultColour() {
        switch (colourStyle) {
            case 0:
                return Color.WHITE;
            case 1:
                return Color.GRAY;
            default:
                Gdx.app.log("RadarScreen", "Unknown colour style " + colourStyle);
                return Color.WHITE;
        }
    }

    /** Gets the ILS drawing colour depending on colour scheme */
    public Color getILSColour() {
        switch (colourStyle) {
            case 0:
                return Color.CYAN;
            case 1:
                return Color.GRAY;
            default:
                Gdx.app.log("RadarScreen", "Unknown colour style " + colourStyle);
                return Color.CYAN;
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) && !tutorial && !loading) {
            //On android, change to pause screen if not paused, un-pause if paused
            setGameRunning(!running);
        }
        super.render(delta);
    }

    @Override
    public void show() {
        //Implements show method of screen, loads UI & save (if available) after show is called if it hasn't been done
        if (!uiLoaded) {
            Ui.generatePaneTextures();
            DataTag.setLoadedIcons(false);
            Tab.setLoadedStyles(false);
            loadUI();
            GameLoader.loadSaveData(save);
            uiLoaded = true;
            setPlayTime(save == null ? 0 : (float) save.optDouble("playTime", estimatePlayTime()));
        }
        TerminalControl.discordManager.updateRPC();
        updateWaypointDisplay();
        setGameRunning(true);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        //Updates position of tutorial box if tutorial
        if (tutorial) {
            tutorialManager.getScrollPane().setX((float) TerminalControl.WIDTH / TerminalControl.HEIGHT * 3240 - 1750);
        }
    }

    @Override
    public void dispose() {
        //Implements dispose method of screen, disposes resources after they're no longer needed
        super.dispose();

        timer.cancel();
        TerminalControl.tts.cancel();
    }

    /** Disposes of static final variables after user quits app */
    public static void disposeStatic() {
        if (DataTag.SKIN != null) DataTag.SKIN.dispose();
        if (DataTag.ICON_ATLAS != null) DataTag.ICON_ATLAS.dispose();
    }

    public void setSelectedAircraft(Aircraft aircraft) {
        runwayChanger.hideAll();
        if (selectedAircraft != null) {
            selectedAircraft.setSelected(false);
            selectedAircraft.getDataTag().updateBorderBackgroundVisibility(false);
        }
        if (aircraft != null) {
            aircraft.setSelected(true);
            aircraft.getDataTag().updateBorderBackgroundVisibility(true);
        }

        if (aircraft != null && aircraft.isArrivalDeparture()) {
            ui.setSelectedPane(aircraft);
            ui.setNormalPane(false);
        } else {
            ui.setNormalPane(true);
            ui.setSelectedPane(null);
            commBox.setVisible(true);
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
        planesToControl = trafficMode == TrafficFlowScreen.PLANES_IN_CONTROL ? maxPlanes : MathUtils.clamp(planesToControl, 4f, MaxTraffic.getMaxTraffic(mainName));
        this.planesToControl = planesToControl;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        if (score < 0) score = 0;
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

    public int getDepartures() {
        int count = 0;
        for (Aircraft aircraft: aircrafts.values()) {
            if (aircraft instanceof Departure && aircraft.isArrivalDeparture()) {
                //If aircraft is a departure, and is in your control
                count++;
            }
        }
        return count;
    }

    public float getRadarTime() {
        return radarTime;
    }

    public float getTrailTime() {
        return trailTime;
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

    public HashSet<String> getAllAircraft() {
        return allAircraft;
    }

    public void setAllAircraft(HashSet<String> allAircraft) {
        this.allAircraft = allAircraft;
    }

    public RunwayChanger getRunwayChanger() {
        return runwayChanger;
    }

    public char getInformation() {
        return information;
    }

    public void updateInformation() {
        information++;
        if (information > 90) information -= 26;
        if (commBox != null) commBox.normalMsg("Information " + information + " is now current");
    }

    public void setRadarTime(float radarTime) {
        this.radarTime = radarTime;
    }

    public int getPlanesInControl() {
        int count = 0;
        for (Aircraft aircraft: aircrafts.values()) {
            if (aircraft.isArrivalDeparture()) count++;
        }

        return count;
    }

    public int getRevision() {
        return revision;
    }

    public void setArrivals(int arrivals) {
        this.arrivals = arrivals;
    }

    public int getSeparationIncidents() {
        return separationIncidents;
    }

    public void setSeparationIncidents(int separationIncidents) {
        this.separationIncidents = separationIncidents;
    }

    public float getWakeInfringeTime() {
        return wakeInfringeTime;
    }

    public void setWakeInfringeTime(float wakeInfringeTime) {
        this.wakeInfringeTime = wakeInfringeTime;
    }

    public int getEmergenciesLanded() {
        return emergenciesLanded;
    }

    public void setEmergenciesLanded(int emergenciesLanded) {
        this.emergenciesLanded = emergenciesLanded;
    }
}
