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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.*;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.*;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.airports.AirportName;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.procedures.RandomSTAR;
import com.bombbird.terminalcontrol.entities.trafficmanager.ArrivalManager;
import com.bombbird.terminalcontrol.entities.trafficmanager.MaxTraffic;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.restrictions.PolygonObstacle;
import com.bombbird.terminalcontrol.entities.restrictions.CircleObstacle;
import com.bombbird.terminalcontrol.entities.waypoints.WaypointManager;
import com.bombbird.terminalcontrol.ui.*;
import com.bombbird.terminalcontrol.ui.tabs.Tab;
import com.bombbird.terminalcontrol.sounds.Pronunciation;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import com.bombbird.terminalcontrol.utilities.math.RandomGenerator;
import com.bombbird.terminalcontrol.utilities.TutorialManager;
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
    public int divertHdg;
    public int airac;
    public String callsign;
    public String deptCallsign;
    public int trajectoryLine = 90;
    public boolean liveWeather = true;
    public int soundSel;
    public float radarSweepDelay = 2f; //TODO Change radar sweep delay in settings for unlocks

    //Whether the game is a tutorial
    public boolean tutorial = false;

    //Score of current game
    private float planesToControl; //To keep track of how well the user is coping; number of arrivals to control is approximately this value
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

    //Tutorial manager
    private TutorialManager tutorialManager = null;

    //Communication box to keep track of aircraft transmissions
    private CommBox commBox;

    //Runway change box for changing runway configuration
    private RunwayChanger runwayChanger;

    private Aircraft selectedAircraft;

    private JSONObject save;

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

        loadStageCamTimer();

        //Set timer for radar delay, trails and autosave
        radarTime = radarSweepDelay;
        trailTime = 10f;
        saveTime = 60f;

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            soundSel = 1;
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            soundSel = 2;
        }

        if (tutorial) {
            tutorialManager = new TutorialManager(this);
        }
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
        soundSel = save.isNull("sounds") ? 2 : save.getInt("sounds");
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
                if (tutorial) {
                    metar.updateTutorialMetar();
                } else {
                    metar.updateMetar();
                }
            }
        }, calendar.getTime(), 900000);

        if (save == null) {
            if (tutorial) {
                metar.updateTutorialMetar();
            } else {
                metar.updateMetar(); //Update the current airport METAR if not from save (airports not loaded in save at this stage)
            }
        }
    }

    /** Creates a new departure at the given airport */
    public void newDeparture(String callsign, String icaoType, Airport airport, Runway runway) {
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
        ArrivalManager.checkArrival(arrival);
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

        //Load shoreline image file
        Image image = FileLoader.loadShoreline();
        image.setScale(3240 / image.getWidth());
        image.setX(1260);
        stage.addActor(image);

        //Load range circles
        loadRange();

        //Load waypoints
        waypoints = FileLoader.loadWaypoints();

        //Load specific waypoint pronunciation
        Pronunciation.loadPronunciation();

        //Load maximum traffic limits for different airports
        MaxTraffic.loadMaxTraffic();

        //Load airports
        loadAirports();

        //Load separation checker
        separationChecker = new SeparationChecker();
        stage.addActor(separationChecker);

        //Load aircraft callsign hashMap
        allAircraft = new HashMap<String, Boolean>();

        //Load waypoint manager
        waypointManager = new WaypointManager();

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

        //Load runway change box
        runwayChanger = new RunwayChanger();

        //Initialise tutorial manager if is tutorial
        if (tutorial) tutorialManager.init();

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
            saveTime -= deltaTime;
            if (saveTime <= 0) {
                GameSaver.saveGame();
                saveTime += 60f;
            }

            arrivals = 0;
            for (Aircraft aircraft: aircrafts.values()) {
                if (aircraft instanceof Arrival && aircraft.getControlState() == 1) arrivals++;
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
    public void renderShape() {
        //Update timers
        updateTimers();

        //Updates waypoints status
        waypointManager.update();

        //Updates aircraft separation status
        separationChecker.update();

        //Updates STAR timers
        RandomSTAR.update();

        //Draw obstacles
        Array<PolygonObstacle> saveForLast = new Array<PolygonObstacle>();
        for (PolygonObstacle polygonObstacle : obsArray) {
            if (polygonObstacle.isEnforced() || polygonObstacle.isConflict() || polygonObstacle.getLabel().getText().toString().charAt(0) == '#') {
                saveForLast.add(polygonObstacle);
            } else {
                polygonObstacle.renderShape();
            }
        }
        for (PolygonObstacle polygonObstacle : saveForLast) {
            polygonObstacle.renderShape();
        }

        //Draw restricted areas
        Array<CircleObstacle> saveForLast1 = new Array<CircleObstacle>();
        for (CircleObstacle circleObstacle : restArray) {
            if (circleObstacle.isConflict()) {
                saveForLast1.add(circleObstacle);
            } else {
                circleObstacle.renderShape();
            }
        }
        for (CircleObstacle circleObstacle : saveForLast1) {
            circleObstacle.renderShape();
        }

        super.renderShape();

        //Additional adjustments for certain airports
        shapeRenderer.setColor(Color.BLACK);
        if ("RCTP".equals(mainName)) {
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

        separationChecker.renderShape();

        if (tutorialManager != null) {
            tutorialManager.update();
        }

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
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

        if (aircraft != null && (aircraft.getControlState() == 1 || aircraft.getControlState() == 2)) {
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
        planesToControl = MathUtils.clamp(planesToControl, 4f, MaxTraffic.maxTraffic.get(mainName));
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

    public Timer getTimer() {
        return timer;
    }

    public RunwayChanger getRunwayChanger() {
        return runwayChanger;
    }
}
