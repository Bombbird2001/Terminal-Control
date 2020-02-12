package com.bombbird.terminalcontrol.screens;

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
import com.bombbird.terminalcontrol.entities.*;
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.airports.AirportName;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle;
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR;
import com.bombbird.terminalcontrol.entities.trafficmanager.MaxTraffic;
import com.bombbird.terminalcontrol.entities.waketurbulence.WakeManager;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.waypoints.WaypointManager;
import com.bombbird.terminalcontrol.entities.weather.Metar;
import com.bombbird.terminalcontrol.entities.weather.WindDirChance;
import com.bombbird.terminalcontrol.entities.weather.WindshearChance;
import com.bombbird.terminalcontrol.entities.weather.WindspeedChance;
import com.bombbird.terminalcontrol.ui.*;
import com.bombbird.terminalcontrol.ui.tabs.Tab;
import com.bombbird.terminalcontrol.sounds.Pronunciation;
import com.bombbird.terminalcontrol.utilities.RenameManager;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import com.bombbird.terminalcontrol.utilities.math.RandomGenerator;
import com.bombbird.terminalcontrol.utilities.TutorialManager;
import org.json.JSONObject;

import java.util.*;

public class RadarScreen extends GameScreen {
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
    public Weather liveWeather;
    public int soundSel;
    public Emergency.Chance emerChance;
    public TfcMode tfcMode;
    public boolean allowNight;
    public int nightStart;
    public int nightEnd;
    public float radarSweepDelay = 2f; //TODO Change radar sweep delay in settings for unlocks

    //Whether the game is a tutorial
    public boolean tutorial = false;

    //Score of current game
    private float planesToControl; //To keep track of how well the user is coping; number of arrivals to control is approximately this value
    private int score; //Score of the player; equal to the number of planes landed without a separation incident (with other traffic or terrain)
    private int highScore; //High score of player

    private int arrivals;
    private float spawnTimer;

    private char information;

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

    private JSONObject save;
    private int revision; //Revision for indicating if save parser needs to do anything special
    public static final int CURRENT_REVISION = 1;

    public RadarScreen(final TerminalControl game, String name, int airac, int saveID, boolean tutorial) {
        //Creates new game
        super(game);
        save = null;
        mainName = name;
        this.airac = airac;
        saveId = saveID;
        this.tutorial = tutorial;

        planesToControl = 6f;
        score = 0;
        highScore = 0;
        arrivals = 0;
        spawnTimer = 0;
        information = (char) MathUtils.random(65, 90);

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = radarSweepDelay;
        trailTime = 10f;
        saveTime = 60f;

        trajectoryLine = TerminalControl.trajectorySel;
        liveWeather = TerminalControl.weatherSel;
        soundSel = TerminalControl.soundSel;
        emerChance = TerminalControl.emerChance;
        tfcMode = TfcMode.NORMAL;
        allowNight = true;
        nightStart = 2200;
        nightEnd = 600;

        wakeManager = new WakeManager();

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
        spawnTimer = (float) save.getDouble("spawnTimer");
        information = save.isNull("information") ? (char) MathUtils.random(65, 90) : (char) save.getInt("information");

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = (float) save.getDouble("radarTime");
        trailTime = (float) save.getDouble("trailTime");
        saveTime = 60f;

        trajectoryLine = save.getInt("trajectoryLine");
        String weather = save.optString("liveWeather");
        if ("true".equals(weather)) {
            liveWeather = Weather.LIVE;
        } else if ("false".equals(weather)) {
            liveWeather = Weather.RANDOM;
        } else {
            liveWeather = RadarScreen.Weather.valueOf(save.getString("liveWeather"));
        }
        soundSel = save.isNull("sounds") ? 2 : save.getInt("sounds");
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

        wakeManager = save.isNull("wakeManager") ? new WakeManager() : new WakeManager(save.getJSONObject("wakeManager"));
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
                metar.updateMetar(tutorial);
            }
        }, calendar.getTime(), 900000);

        if (save == null) {
            metar.updateMetar(tutorial); //Update the current airport METAR if not from save (airports not loaded in save at this stage)
        }
    }

    /** Creates a new departure at the given airport */
    public void newDeparture(String callsign, String icaoType, Airport airport, Runway runway) {
        if (tfcMode == TfcMode.ARRIVALS_ONLY) return;
        aircrafts.put(callsign, new Departure(callsign, icaoType, airport, runway));
    }

    /** Creates a new arrival for random airport */
    private void newArrival() {
        Airport airport = RandomGenerator.randomAirport();
        if (!RandomSTAR.starAvailable(airport.getIcao())) {
            spawnTimer = 10f; //Wait for another 10 seconds if no spawn points available
            return;
        }
        String[] aircraftInfo = RandomGenerator.randomPlane(airport);
        Arrival arrival = new Arrival(aircraftInfo[0], aircraftInfo[1], airport);
        aircrafts.put(aircraftInfo[0], arrival);
        arrivals++;

        //Min 50sec for >=4 planes diff, max 80sec for <=1 plane diff
        spawnTimer = 90f - 10 * (planesToControl - arrivals);
        spawnTimer = MathUtils.clamp(spawnTimer, 50, 80);
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

        //Load specific waypoint pronunciation
        Pronunciation.loadPronunciation();

        //Load maximum traffic limits for different airports
        MaxTraffic.loadHashmaps();

        //Load airports
        loadAirports();

        //Load separation checker
        separationChecker = new SeparationChecker();
        stage.addActor(separationChecker);

        //Load aircraft callsign hashMap
        allAircraft = new HashMap<>();

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

    /** Updates the time values for each timer & runs tasks when time is reached */
    private void updateTimers() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        radarTime -= deltaTime;
        if (radarTime <= 0) {
            updateRadarInfo();
            radarTime += radarSweepDelay;
        }

        trailTime -= deltaTime;
        if (trailTime <= 0) {
            addTrailDot();
            trailTime += 10f;
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

        runwayChanger.update();
    }

    @Override
    public void renderShape() {
        //Updates aircraft separation status
        separationChecker.update();

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

        super.renderShape();

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

        //Draw waypoints
        Array<Waypoint> flyOver = new Array<>();
        for (Waypoint waypoint: waypoints.values()) {
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

        separationChecker.renderShape();

        if (selectedAircraft != null) {
            wakeManager.renderWake(selectedAircraft);
        }
        wakeManager.renderIlsWake();

        if (tutorialManager != null) {
            tutorialManager.update();
        }

        shapeRenderer.end();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) && !tutorial && !loading) {
            //On android, change to pause screen if not paused, un-pause if paused
            setGameState(GameScreen.state == State.PAUSE ? State.RUN : State.PAUSE);
        }
        super.render(delta);
    }

    @Override
    public void show() {
        //Regenerates textures if disposed
        Ui.generatePaneTextures();
        DataTag.setLoadedIcons(false);
        Tab.setLoadedStyles(false);

        //Implements show method of screen, loads UI & save (if available) after show is called
        loadUI();
        GameLoader.loadSaveData(save);
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
        }
        if (aircraft != null) {
            aircraft.setSelected(true);
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
        planesToControl = MathUtils.clamp(planesToControl, 4f, MaxTraffic.getMaxTraffic(mainName));
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

    public HashMap<String, Boolean> getAllAircraft() {
        return allAircraft;
    }

    public void setAllAircraft(HashMap<String, Boolean> allAircraft) {
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
}
