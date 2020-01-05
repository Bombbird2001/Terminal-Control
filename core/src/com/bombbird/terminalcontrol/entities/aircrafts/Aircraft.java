package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.sidstar.Route;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.ui.DataTag;
import com.bombbird.terminalcontrol.ui.tabs.LatTab;
import com.bombbird.terminalcontrol.ui.tabs.Tab;
import com.bombbird.terminalcontrol.ui.Ui;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONArray;
import org.json.JSONObject;

public class Aircraft extends Actor {
    public enum ControlState {
        UNCONTROLLED,
        ARRIVAL,
        DEPARTURE,
        ENROUTE
    }

    //Android text-to-speech
    private String voice;
    private static final String[] VOICES = {"en-gb-x-gba-local", "en-gb-x-fis#female_1-local", "en-us-x-sfg#male_1-local", "en-au-x-aud-local",
            "en-us-x-sfg#female_1-local", "en-gb-x-rjs-local", "en-gb-x-rjs#female_3-local", "en-gb-x-gbd-local",
            "en-gb-x-fis#male_2-local", "en-us-x-sfg#female_2-local", "en-gb-x-rjs#female_1-local",
            "en-gb-x-fis#male_3-local", "en-gb-x-fis-local", "en-gb-x-gbb-local", "en-gb-x-fis#female_2-local",
            "en-us-x-sfg#male_2-local", "en-gb-x-rjs#female_2-local", "en-gb-x-fis#male_1-local", "en-us-x-sfg#female_3-local",
            "en-gb-x-fis#female_3-local"
    };

    public RadarScreen radarScreen;
    private Stage stage;
    public ShapeRenderer shapeRenderer;
    public Ui ui;

    private DataTag dataTag;
    private Color color;

    private boolean selected;
    private boolean actionRequired;
    private boolean fuelEmergency;

    //Aircraft information
    private Airport airport;
    private Runway runway;
    private boolean onGround;
    private boolean tkOfLdg;

    //Aircraft characteristics
    private String callsign;
    private String icaoType;
    private char wakeCat;
    private char recat;
    private boolean wakeInfringe;
    private float wakeTolerance;
    private int v2;
    private int typClimb;
    private int maxClimb;
    private int typDes;
    private int maxDes;
    private int apchSpd;
    private ControlState controlState;
    private NavState navState;
    private boolean goAround;
    private boolean goAroundWindow;
    private float goAroundTime;
    private boolean conflict;
    private boolean warning;
    private boolean terrainConflict;
    private Emergency emergency;

    //Aircraft position
    private float x;
    private float y;
    private double heading;
    private double targetHeading;
    private int clearedHeading;
    private double angularVelocity;
    private double track;
    private Route route;
    private int sidStarIndex;
    private Waypoint direct;
    private Waypoint afterWaypoint;
    private int afterWptHdg;
    private ILS ils;
    private boolean locCap;
    private Waypoint holdWpt;
    private boolean holding;
    private int holdingType;
    private boolean init;
    private boolean type1leg;
    private float[][] holdTargetPt;
    private boolean[] holdTargetPtSelected;
    private float prevDistTravelled;

    //Altitude
    private float prevAlt;
    private float altitude;
    private int clearedAltitude;
    private int targetAltitude;
    private float verticalSpeed;
    private boolean expedite;
    private float expediteTime;
    private int lowestAlt;
    private int highestAlt;
    private boolean gsCap;

    //Speed
    private float ias;
    private float tas;
    private float gs;
    private Vector2 deltaPosition;
    private int clearedIas;
    private float deltaIas;
    private int climbSpd;

    //Radar returns (for sweep delay)
    private float radarX;
    private float radarY;
    private double radarHdg;
    private double radarTrack;
    private float radarGs;
    private float radarAlt;
    private float radarVs;

    public Aircraft(String callsign, String icaoType, Airport airport) {
        loadResources();

        this.callsign = callsign;
        stage.addActor(this);
        this.icaoType = icaoType;
        wakeCat = AircraftType.getWakeCat(icaoType);
        recat = AircraftType.getRecat(icaoType);
        wakeInfringe = false;
        wakeTolerance = 0;
        float loadFactor = MathUtils.random(-1 , 1) / 40f;
        v2 = (int)(AircraftType.getV2(icaoType) * (1 + loadFactor));
        typClimb = (int)(AircraftType.getTypClimb(icaoType) * (1 - loadFactor));
        maxClimb = typClimb + 1000;
        typDes = (int)(AircraftType.getTypDes(icaoType) * (1 - loadFactor));
        maxDes = typDes + 1000;
        apchSpd = (int)(AircraftType.getApchSpd(icaoType) * (1 + loadFactor));
        if (airport.getLandingRunways().size() == 0) {
            //No landing runways available at departure airport, land at main airport instead
            this.airport = radarScreen.airports.get(radarScreen.mainName);
        } else {
            this.airport = airport;
        }
        heading = 0;
        targetHeading = 0;
        clearedHeading = (int)(heading);
        track = 0;
        sidStarIndex = 0;
        afterWptHdg = 360;
        altitude = 10000;
        clearedAltitude = 10000;
        targetAltitude = 10000;
        verticalSpeed = 0;
        expedite = false;
        expediteTime = 0;
        ias = 250;
        tas = MathTools.iasToTas(ias, altitude);
        gs = tas;
        deltaPosition = new Vector2();
        prevDistTravelled = 0;
        clearedIas = 250;
        deltaIas = 0;
        tkOfLdg = false;
        gsCap = false;
        locCap = false;
        holdingType = 0;
        climbSpd = MathUtils.random(270, 280);
        goAround = false;
        goAroundWindow = false;
        goAroundTime = 0;
        conflict = false;
        warning = false;
        terrainConflict = false;
        emergency = new Emergency(this, radarScreen.emerChance);

        radarScreen.wakeManager.addAircraft(callsign);

        voice = VOICES[MathUtils.random(0, VOICES.length - 1)];
    }

    /** Constructs aircraft from another aircraft */
    public Aircraft(Aircraft aircraft) {
        loadResources();

        callsign = aircraft.callsign;
        stage.addActor(this);
        this.icaoType = aircraft.icaoType;
        wakeCat = aircraft.wakeCat;
        recat = aircraft.recat;
        wakeInfringe = aircraft.wakeInfringe;
        wakeTolerance = aircraft.wakeTolerance;
        v2 = aircraft.v2;
        typClimb = aircraft.typClimb;
        maxClimb = aircraft.maxClimb;
        typDes = aircraft.typDes;
        maxDes = aircraft.maxDes;
        apchSpd = aircraft.apchSpd;
        airport = aircraft.airport;
        heading = aircraft.heading;
        targetHeading = aircraft.targetHeading;
        clearedHeading = aircraft.clearedHeading;
        track = aircraft.track;
        sidStarIndex = aircraft.sidStarIndex;
        afterWptHdg = aircraft.afterWptHdg;
        altitude = aircraft.altitude;
        clearedAltitude = aircraft.clearedAltitude;
        targetAltitude = aircraft.targetAltitude;
        verticalSpeed = aircraft.verticalSpeed;
        expedite = aircraft.expedite;
        expediteTime = aircraft.expediteTime;
        ias = aircraft.ias;
        tas = aircraft.tas;
        gs = aircraft.gs;
        deltaPosition = aircraft.deltaPosition;
        prevDistTravelled = aircraft.prevDistTravelled;
        clearedIas = aircraft.clearedIas;
        deltaIas = aircraft.deltaIas;
        tkOfLdg = aircraft.tkOfLdg;
        gsCap = aircraft.gsCap;
        locCap = aircraft.locCap;
        holdingType = aircraft.holdingType;
        climbSpd = aircraft.climbSpd;
        goAround = aircraft.goAround;
        goAroundWindow = aircraft.goAroundWindow;
        goAroundTime = aircraft.goAroundTime;
        conflict = aircraft.conflict;
        warning = aircraft.warning;
        terrainConflict = aircraft.terrainConflict;
        emergency = aircraft.emergency;
        radarAlt = aircraft.radarAlt;
        radarGs = aircraft.radarGs;
        radarHdg = aircraft.radarHdg;
        radarTrack = aircraft.radarTrack;
        radarVs = aircraft.radarVs;
        radarX = aircraft.radarX;
        radarY = aircraft.radarY;
        actionRequired = aircraft.actionRequired;
        route = aircraft.route;
        x = aircraft.x;
        y = aircraft.y;
        angularVelocity = aircraft.angularVelocity;
        onGround = aircraft.onGround;
        navState = aircraft.navState;
        navState.setAircraft(this);

        voice = aircraft.voice;
    }

    public Aircraft(JSONObject save) {
        loadResources();

        airport = radarScreen.airports.get(save.getString("airport"));
        runway = save.isNull("runway") ? null : airport.getRunways().get(save.getString("runway"));
        onGround = save.getBoolean("onGround");
        tkOfLdg = save.getBoolean("tkOfLdg");

        callsign = save.getString("callsign");
        stage.addActor(this);
        icaoType = save.getString("icaoType");
        wakeCat = save.getString("wakeCat").charAt(0);
        recat = save.isNull("recat") ? AircraftType.getRecat(icaoType) : (char) save.getInt("recat");
        wakeInfringe = save.optBoolean("wakeInfringe");
        wakeTolerance = (float) save.optDouble("wakeTolerance", 0);
        v2 = save.getInt("v2");
        typClimb = save.getInt("typClimb");
        maxClimb = save.getInt("maxClimb");
        typDes = save.getInt("typDes");
        maxDes = save.getInt("maxDes");
        apchSpd = save.getInt("apchSpd");
        navState = new NavState(this, save.getJSONObject("navState"));
        goAround = save.getBoolean("goAround");
        goAroundWindow = save.getBoolean("goAroundWindow");
        goAroundTime = (float) save.getDouble("goAroundTime");
        conflict = save.getBoolean("conflict");
        warning = save.getBoolean("warning");
        if (save.isNull("terrainConflict")) {
            terrainConflict = false;
        } else {
            terrainConflict = save.getBoolean("terrainConflict");
        }
        emergency = save.optJSONObject("emergency") == null ? new Emergency(this, false) : new Emergency(this, save.getJSONObject("emergency"));

        x = (float) save.getDouble("x");
        y = (float) save.getDouble("y");
        prevDistTravelled = (float) save.optDouble("prevDistTravelled", 0);
        heading = save.getDouble("heading");
        targetHeading = save.getDouble("targetHeading");
        clearedHeading = save.getInt("clearedHeading");
        angularVelocity = save.getDouble("angularVelocity");
        track = save.getDouble("track");
        sidStarIndex = save.getInt("sidStarIndex");
        direct = save.isNull("direct") ? null : radarScreen.waypoints.get(save.getString("direct"));
        afterWaypoint = save.isNull("afterWaypoint") ? null : radarScreen.waypoints.get(save.getString("afterWaypoint"));
        afterWptHdg = save.getInt("afterWptHdg");
        ils = save.isNull("ils") ? null : airport.getApproaches().get(save.getString("ils").substring(3));
        locCap = save.getBoolean("locCap");
        holdWpt = save.isNull("holdWpt") ? null : radarScreen.waypoints.get(save.getString("holdWpt"));
        holding = save.getBoolean("holding");
        holdingType = save.isNull("holdingType") ? 0 : save.getInt("holdingType");
        init = save.getBoolean("init");
        type1leg = save.getBoolean("type1leg");

        if (save.isNull("holdTargetPt")) {
            //Null holding arrays
            holdTargetPt = null;
            holdTargetPtSelected = null;
        } else {
            //Not null
            JSONArray the2points = save.getJSONArray("holdTargetPt");
            holdTargetPt = new float[2][2];
            for (int i = 0; i < the2points.length(); i++) {
                JSONArray coordinates = the2points.getJSONArray(i);
                holdTargetPt[i][0] = (float) coordinates.getDouble(0);
                holdTargetPt[i][1] = (float) coordinates.getDouble(1);
            }
            JSONArray the2bools = save.getJSONArray("holdTargetPtSelected");
            holdTargetPtSelected = new boolean[2];
            holdTargetPtSelected[0] = the2bools.getBoolean(0);
            holdTargetPtSelected[1] = the2bools.getBoolean(1);
        }

        prevAlt = (float) save.getDouble("prevAlt");
        altitude = (float) save.getDouble("altitude");
        clearedAltitude = save.getInt("clearedAltitude");
        targetAltitude = save.getInt("targetAltitude");
        verticalSpeed = (float) save.getDouble("verticalSpeed");
        expedite = save.getBoolean("expedite");
        if (save.isNull("expediteTime")) {
            expediteTime = 0;
        } else {
            expediteTime = (float) save.getDouble("expediteTime");
        }
        lowestAlt = save.getInt("lowestAlt");
        highestAlt = save.getInt("highestAlt");
        gsCap = save.getBoolean("gsCap");

        ias = (float) save.getDouble("ias");
        tas = (float) save.getDouble("tas");
        gs = (float) save.getDouble("gs");

        JSONArray delta = save.getJSONArray("deltaPosition");
        deltaPosition = new Vector2();
        deltaPosition.x = (float) delta.getDouble(0);
        deltaPosition.y = (float) delta.getDouble(1);

        clearedIas = save.getInt("clearedIas");
        deltaIas = (float) save.getDouble("deltaIas");
        climbSpd = save.getInt("climbSpd");

        radarX = (float) save.getDouble("radarX");
        radarY = (float) save.getDouble("radarY");
        radarHdg = save.getDouble("radarHdg");
        radarTrack = save.getDouble("radarTrack");
        radarGs = (float) save.getDouble("radarGs");
        radarAlt = (float) save.getDouble("radarAlt");
        radarVs = (float) save.getDouble("radarVs");

        voice = save.isNull("voice") ? VOICES[MathUtils.random(0, VOICES.length - 1)] : save.getString("voice");
    }

    /** Loads & sets aircraft resources */
    private void loadResources() {
        radarScreen = TerminalControl.radarScreen;
        stage = radarScreen.stage;
        shapeRenderer = radarScreen.shapeRenderer;
        ui = radarScreen.ui;
    }

    /** Sets the initial radar position for aircraft */
    public void initRadarPos() {
        radarX = x;
        radarY = y;
        radarHdg = heading;
        radarTrack = track;
        radarGs = gs;
        radarAlt = altitude;
        radarVs = verticalSpeed;
    }

    /** Loads the aircraft data labels */
    public void loadLabel() {
        dataTag = new DataTag(this);
    }

    /** Loads other aircraft data label info */
    public void loadOtherLabelInfo(JSONObject save) {
        JSONArray trails = save.getJSONArray("trailDots");
        for (int i = 0; i < trails.length(); i++) {
            getDataTag().addTrailDot((float) trails.getJSONArray(i).getDouble(0), (float) trails.getJSONArray(i).getDouble(1));
        }

        if (!save.isNull("labelPos")) {
            JSONArray labelPos = save.getJSONArray("labelPos");
            dataTag.setLabelPosition((float) labelPos.getDouble(0), (float) labelPos.getDouble(1));
        }

        dataTag.setMinimized(!save.isNull("dataTagMin") && save.getBoolean("dataTagMin"));

        if (save.isNull("fuelEmergency")) {
            if (save.isNull("emergency")) {
                //Save from before fuel emergency update
                fuelEmergency = false;
            } else {
                //Change key from emergency to fuel emergency
                fuelEmergency = save.getBoolean("emergency");
            }
        }
        if (fuelEmergency || emergency.isActive()) dataTag.setEmergency();

        actionRequired = !save.isNull("actionRequired") && save.getBoolean("actionRequired");
        if (actionRequired) dataTag.flashIcon();
    }

    /** Renders shapes using shapeRenderer; all rendering should be called here */
    public void renderShape() {
        drawLatLines();
        dataTag.moderateLabel();
        shapeRenderer.setColor(Color.WHITE);
        dataTag.renderShape();
        if (isArrivalDeparture()) {
            shapeRenderer.setColor(color);
            shapeRenderer.line(radarX, radarY, radarX + radarScreen.trajectoryLine / 3600f * MathTools.nmToPixel(radarGs) * (float) Math.cos(Math.toRadians(90 - radarTrack)), radarY + radarScreen.trajectoryLine / 3600f * MathTools.nmToPixel(radarGs) * (float) Math.sin(Math.toRadians(90 - radarTrack)));
        }
    }

    /** Draws the lines displaying the lateral status of aircraft */
    private void drawLatLines() {
        if (selected) {
            //Draws cleared status
            if (navState.getDispLatMode().last().contains("arrival") || navState.getDispLatMode().last().contains("departure") && direct != null) {
                drawSidStar();
            } else if (navState.getDispLatMode().last().equals("After waypoint, fly heading") && direct != null) {
                drawAftWpt();
            } else if (navState.getDispLatMode().last().contains("heading") && (!locCap || navState.getClearedIls().last() == null)) {
                drawHdgLine();
            } else if (navState.getDispLatMode().last().equals("Hold at")) {
                drawHoldPattern();
            }

            //Draws selected status (from UI)
            if (isArrivalDeparture()) {
                if ((LatTab.latMode.contains("arrival") || LatTab.latMode.contains("departure")) && (ui.latTab.isWptChanged() || ui.latTab.isLatModeChanged())) {
                    uiDrawSidStar();
                } else if ("After waypoint, fly heading".equals(LatTab.latMode) && (ui.latTab.isAfterWptChanged() || ui.latTab.isAfterWptHdgChanged() || ui.latTab.isLatModeChanged())) {
                    uiDrawAftWpt();
                } else if (LatTab.latMode.contains("heading") && (this instanceof Departure || "Not cleared approach".equals(LatTab.clearedILS) || !locCap) && (ui.latTab.isHdgChanged() || ui.latTab.isLatModeChanged())) {
                    uiDrawHdgLine();
                } else if ("Hold at".equals(LatTab.latMode) && (ui.latTab.isLatModeChanged() || ui.latTab.isHoldWptChanged())) {
                    uiDrawHoldPattern();
                }
            }
        }
    }

    /** Draws the cleared sidStar when selected */
    public void drawSidStar() {
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(radarX, radarY, navState.getClearedDirect().last().getPosX(), navState.getClearedDirect().last().getPosY());
    }

    /** Draws the sidStar for the UI */
    public void uiDrawSidStar() {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.line(radarX, radarY, radarScreen.waypoints.get(LatTab.clearedWpt).getPosX(), radarScreen.waypoints.get(LatTab.clearedWpt).getPosY());
    }

    /** Draws the cleared after waypoint + cleared outbound heading when selected */
    public void drawAftWpt() {
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(radarX, radarY, navState.getClearedDirect().last().getPosX(), navState.getClearedDirect().last().getPosY());
    }

    /** Draws the after waypoint + outbound heading for UI */
    public void uiDrawAftWpt() {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.line(radarX, radarY, radarScreen.waypoints.get(LatTab.clearedWpt).getPosX(), radarScreen.waypoints.get(LatTab.clearedWpt).getPosY());
    }

    /** Draws the cleared heading when selected */
    private void drawHdgLine() {
        shapeRenderer.setColor(Color.WHITE);
        float[] point = MathTools.pointsAtBorder(new float[] {1260, 4500}, new float[] {0, 3240}, radarX, radarY, navState.getClearedHdg().last() - radarScreen.magHdgDev);
        shapeRenderer.line(radarX, radarY, point[0], point[1]);
    }

    /** Draws the heading for the UI */
    private void uiDrawHdgLine() {
        shapeRenderer.setColor(Color.YELLOW);
        float[] point = MathTools.pointsAtBorder(new float[] {1260, 4500}, new float[] {0, 3240}, radarX, radarY, LatTab.clearedHdg - radarScreen.magHdgDev);
        shapeRenderer.line(radarX, radarY, point[0], point[1]);
    }

    /** Draws the cleared holding pattern when selected */
    public void drawHoldPattern() {
        shapeRenderer.setColor(Color.WHITE);
        if (!holding) shapeRenderer.line(radarX, radarY, direct.getPosX(), direct.getPosY());
        route.getHoldProcedure().renderShape(navState.getClearedHold().last());
    }

    /** Draws the holding pattern for the UI */
    public void uiDrawHoldPattern() {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.line(radarX, radarY, radarScreen.waypoints.get(LatTab.clearedWpt).getPosX(), radarScreen.waypoints.get(LatTab.clearedWpt).getPosY());
        route.getHoldProcedure().renderShape(radarScreen.waypoints.get(LatTab.holdWpt));
    }

    /** The main update function */
    public double update() {
        if (navState!= null) navState.updateTime();
        tas = MathTools.iasToTas(ias, altitude);
        updateIas();
        if (tkOfLdg) {
            updateTkofLdg();
        }
        if (!onGround) {
            double[] info = updateTargetHeading();
            targetHeading = info[0];
            updateHeading(targetHeading);
            updatePosition(info[1]);
            updateAltitude(false, false);
            updateSpd();
            if (goAround) {
                updateGoAround();
            }
            if (goAroundWindow) {
                goAroundTime -= Gdx.graphics.getDeltaTime();
                if (goAroundTime < 0) {
                    goAroundWindow = false;
                    goAroundTime = 0;
                }
            }
            emergency.update();
            return targetHeading;
        } else {
            gs = tas - airport.getWinds()[1] * (float) Math.cos(Math.toRadians(airport.getWinds()[0] - runway.getHeading()));
            if (gs < 0) gs = 0;
            updatePosition(0);
            emergency.update();
            return 0;
        }
    }

    /** Overriden method that updates the target speed of the aircraft depending on situation */
    public void updateSpd() {
        navState.getClearedSpd().removeFirst();
        navState.getClearedSpd().addFirst(clearedIas);
        if (selected && isArrivalDeparture()) {
            updateUISelections();
            ui.updateState();
        }
    }

    /** Overriden method for arrival/departure */
    public void updateTkofLdg() {
        //No default implementation
    }

    /** Overriden method for arrivals during go around */
    public void updateGoAround() {
        //No default implementation
    }

    /** Updates the aircraft speed */
    private void updateIas() {
        float targetdeltaIas = (clearedIas - ias) / 5;
        if (targetdeltaIas > deltaIas + 0.05) {
            deltaIas += 0.2f * Gdx.graphics.getDeltaTime();
        } else if (targetdeltaIas < deltaIas - 0.05) {
            deltaIas -= 0.2f * Gdx.graphics.getDeltaTime();
        } else {
            deltaIas = targetdeltaIas;
        }
        float max;
        float min;
        if (tkOfLdg) {
            max = 3;
            if (gs >= 60) {
                min = -4.5f;
            } else {
                min = -1.5f;
            }
        } else {
            max = 1.25f * (1 - verticalSpeed / maxClimb);
            min = -2 * (1 + verticalSpeed / maxDes);
        }
        if (deltaIas > max) {
            deltaIas = max;
        } else if (deltaIas < min) {
            deltaIas = min;
        }
        ias = ias + deltaIas * Gdx.graphics.getDeltaTime();
        if (Math.abs(clearedIas - ias) < 1) {
            ias = clearedIas;
        }
    }

    public void updateAltitude(boolean holdAlt, boolean fixedVs) {
        float targetVertSpd = 0;
        if (!holdAlt) targetVertSpd = (targetAltitude - altitude) / 0.1f;
        if (fixedVs) targetVertSpd = verticalSpeed;
        if (targetVertSpd > verticalSpeed + 100) {
            verticalSpeed = verticalSpeed + 500 * Gdx.graphics.getDeltaTime();
        } else if (targetVertSpd < verticalSpeed - 100) {
            verticalSpeed = verticalSpeed - 500 * Gdx.graphics.getDeltaTime();
        }
        float multiplier = altitude > 20000 ? 0.8f : 1;
        if (!expedite && verticalSpeed > typClimb * multiplier) {
            verticalSpeed = typClimb * multiplier;
        } else if (!expedite && verticalSpeed < -typDes * multiplier) {
            verticalSpeed = -typDes * multiplier;
        } else if (expedite && verticalSpeed > maxClimb * multiplier) {
            verticalSpeed = maxClimb * multiplier;
        } else if (expedite && verticalSpeed < -maxDes * multiplier) {
            verticalSpeed = -maxDes * multiplier;
        }
        expediteTime += expedite ? Gdx.graphics.getDeltaTime() : 0;
        altitude += verticalSpeed / 60 * Gdx.graphics.getDeltaTime();

        if (Math.abs(targetAltitude - altitude) < 50 && Math.abs(verticalSpeed) < 200) {
            altitude = targetAltitude;
            verticalSpeed = 0;
            if ("Expedite climb/descent to".equals(navState.getDispAltMode().first())) {
                navState.getDispAltMode().removeFirst();
                navState.getDispAltMode().addFirst("Climb/descend to");
                navState.getClearedExpedite().removeFirst();
                navState.getClearedExpedite().addFirst(false);
                expedite = false;
            }
        }
        if (prevAlt < altitude && (int)(prevAlt / 1000) <= (int)(altitude / 1000)) {
            updateAltRestrictions();
        }
        if ((int)(prevAlt / 1000) != (int)(altitude / 1000)) {
            radarScreen.separationChecker.updateAircraftPositions();
        }
        prevAlt = altitude;
    }

    /** Gets aircraft to contact other frequencies, overriden in Arrival, Departure */
    public void contactOther() {
        //No default implementation
    }

    private double findRequiredDistance(double deltaHeading) {
        float turnRate = ias > 250 ? 1.5f : 3f;
        double radius = gs / 3600 / (MathUtils.degreesToRadians * turnRate);
        double halfTheta = (180 - deltaHeading) / 2f;
        return 32.4 * radius / Math.tan(Math.toRadians(halfTheta)) + 5;
    }

    /** Called to update the heading aircraft should turn towards to follow instructed lateral mode, returns the heading it should fly as well as the resulting difference in angle of the track due to winds */
    public double[] updateTargetHeading() {
        deltaPosition.setZero();
        double targetHeading;

        //Get wind data
        int[] winds;
        if (altitude - airport.getElevation() <= 4000) {
            winds = airport.getWinds();
        } else {
            winds = radarScreen.airports.get(radarScreen.mainName).getWinds();
        }
        int windHdg = winds[0] + 180;
        int windSpd = winds[1];
        if (winds[0] == 0) {
            windSpd = 0;
        }

        boolean sidstar = navState != null && (navState.getDispLatMode().first().contains(getSidStar().getName()) || navState.getDispLatMode().first().equals("After waypoint, fly heading") || navState.getDispLatMode().first().equals("Hold at"));
        boolean vector = navState != null && !sidstar && navState.getDispLatMode().first().contains("heading");

        if (this instanceof Departure) {
            //Check if aircraft has climbed past initial climb
            sidstar = sidstar && ((Departure) this).isSidSet();
            if (!sidstar) {
                //Otherwise continue climbing on current heading
                vector = true;
            }
        }

        if (vector && !locCap) {
            targetHeading = clearedHeading;
        } else if (sidstar && !holding) {
            if (ils != null) {
                setIls(null);
                navState.voidAllIls();
            }
            targetHeading = calculatePointTargetHdg(new float[] {direct.getPosX(), direct.getPosY()}, windHdg, windSpd);

            //If within __px of waypoint, target next waypoint
            //Distance determined by angle that needs to be turned
            double distance = MathTools.distanceBetween(x, y, direct.getPosX(), direct.getPosY());
            double requiredDistance;
            if (route.getWptFlyOver(direct.getName())) {
                requiredDistance = 4;
            } else {
                requiredDistance = findRequiredDistance(Math.abs(findSidStarDeltaHeading(findNextTargetHdg(), targetHeading)));
            }
            if (distance <= requiredDistance) {
                updateDirect();
            }
        } else if (locCap) {
            clearedHeading = ils.getHeading();
            if (!ils.getRwy().equals(runway)) {
                runway = ils.getRwy();
            }
            if (ils instanceof LDA && MathTools.pixelToNm(MathTools.distanceBetween(x, y, runway.getX(), runway.getY()) - 15) <= ((LDA) ils).getLineUpDist()) {
                ils = ((LDA) ils).getImaginaryIls();
                gsCap = false;
                return updateTargetHeading();
            } else {
                //Calculates x, y of point 0.75nm ahead of plane
                Vector2 position = ils.getPointAhead(this, 0.75f);
                targetHeading = calculatePointTargetHdg(new float[] {position.x, position.y}, windHdg, windSpd);
            }
        } else if (holding) {
            if (navState != null && !navState.getDispLatMode().first().equals("Hold at")) {
                holding = false;
                return updateTargetHeading();
            }
            if (holdTargetPt == null) {
                float[] point = route.getHoldProcedure().getOppPtAtWpt(holdWpt);
                holdTargetPt = new float[][] {{holdWpt.getPosX(), holdWpt.getPosY()}, point};
                holdTargetPtSelected = new boolean[] {false, false};
                navState.initHold();
            }
            if (!init) {
                if (holdingType == 0) holdingType = route.getHoldProcedure().getEntryProcAtWpt(holdWpt, heading);
                //Aircraft has just entered holding pattern, follow procedures relevant to each type of holding pattern entry
                if (holdingType == 1) {
                    //After reaching waypoint, fly opposite inbound track, then after flying for leg dist, turn back to entry fix in direction opposite of holding direction
                    targetHeading = route.getHoldProcedure().getInboundHdgAtWpt(holdWpt) + 180;
                    if (MathTools.pixelToNm(MathTools.distanceBetween(x, y, holdWpt.getPosX(), holdWpt.getPosY())) >= route.getHoldProcedure().getLegDistAtWpt(holdWpt) || type1leg) {
                        //Once it has flown leg dist, turn back towards entry fix
                        type1leg = true;
                        targetHeading = calculatePointTargetHdg(new float[] {holdWpt.getPosX(), holdWpt.getPosY()}, windHdg, windSpd);
                        //Once it reaches entry fix, init has ended
                        if (MathTools.distanceBetween(x, y, holdWpt.getPosX(), holdWpt.getPosY()) <= 10) {
                            init = true;
                            holdTargetPtSelected[1] = true;
                        }
                    }
                } else {
                    //Apparently no difference for types 2 and 3 in this case - fly straight towards opp waypoint with direction same as hold direction
                    targetHeading = calculatePointTargetHdg(holdTargetPt[1], windHdg, windSpd);
                    holdTargetPtSelected[1] = true;
                    double deltaHdg = findDeltaHeading(route.getHoldProcedure().getInboundHdgAtWpt(holdWpt) + 180);
                    boolean left = route.getHoldProcedure().isLeftAtWpt(holdWpt);
                    if (left && deltaHdg > -150 || !left && deltaHdg < 150) {
                        //Set init to true once aircraft has turned to a heading of not more than 150 deg offset from target, in the turn direction
                        init = true;
                    }
                }
            } else {
                float track = route.getHoldProcedure().getInboundHdgAtWpt(holdWpt) - radarScreen.magHdgDev;
                if (holdTargetPtSelected[1]) {
                    track += 180;
                }
                float[] target = holdTargetPtSelected[0] ? holdTargetPt[0] : holdTargetPt[1];
                //Just keep turning and turning and turning
                float distance = MathTools.distanceBetween(x, y, target[0], target[1]);
                if (distance <= 10) {
                    //If reached target point
                    holdTargetPtSelected[0] = !holdTargetPtSelected[0];
                    holdTargetPtSelected[1] = !holdTargetPtSelected[1];
                }
                distance -= MathTools.nmToPixel(0.5f);
                targetHeading = calculatePointTargetHdg(new float[] {target[0] + distance * (float) Math.cos(Math.toRadians(270 - track)), target[1] + distance * (float) Math.sin(Math.toRadians(270 - track))}, windHdg, windSpd);
            }
        } else {
            targetHeading = 0;
            Gdx.app.log("Update target heading", "Oops, something went wrong");
        }

        if (targetHeading > 360) {
            targetHeading -= 360;
        } else if (targetHeading <= 0) {
            targetHeading += 360;
        }

        return new double[] {targetHeading, calculateAngleDiff(heading, windHdg, windSpd)};
    }

    private double calculateAngleDiff(double heading, int windHdg, int windSpd) {
        double angle = 180 - windHdg + heading;
        gs = (float) Math.sqrt(Math.pow(tas, 2) + Math.pow(windSpd, 2) - 2 * tas * windSpd * Math.cos(Math.toRadians(angle)));
        return Math.asin(windSpd * Math.sin(Math.toRadians(angle)) / gs) * MathUtils.radiansToDegrees;
    }

    private double calculatePointTargetHdg(float[] position, int windHdg, int windSpd) {
        return calculatePointTargetHdg(position[0] - x, position[1] - y, windHdg, windSpd);
    }

    private double calculatePointTargetHdg(float deltaX, float deltaY, int windHdg, int windSpd) {
        double angleDiff;

        //Find target track angle
        double principleAngle = Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees;
        if (deltaX >= 0) {
            targetHeading = 90 - principleAngle;
        } else {
            targetHeading = 270 - principleAngle;
        }

        //Calculate required aircraft heading to account for winds
        //Using sine rule to determine angle between aircraft velocity and actual velocity
        double angle = windHdg - targetHeading;
        angleDiff = Math.asin(windSpd * Math.sin(Math.toRadians(angle)) / tas) * MathUtils.radiansToDegrees;
        targetHeading -= angleDiff;  //Heading = track - anglediff

        //Add magnetic deviation to give magnetic heading
        targetHeading += radarScreen.magHdgDev;

        return targetHeading;
    }

    public double findNextTargetHdg() {
        if ((navState.getDispLatMode().first().equals("After waypoint, fly heading") && direct.equals(afterWaypoint)) || navState.getDispLatMode().first().equals("Hold at") && direct.equals(holdWpt)) {
            return targetHeading;
        }
        Waypoint nextWpt = route.getWaypoint(getSidStarIndex() + 1);
        if (nextWpt == null) {
            if (this instanceof Departure) return ((Departure) this).getOutboundHdg();
            return targetHeading;
        } else {
            float deltaX = nextWpt.getPosX() - getDirect().getPosX();
            float deltaY = nextWpt.getPosY() - getDirect().getPosY();
            double nextTarget;
            double principleAngle = Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees;
            if (deltaX >= 0) {
                nextTarget = 90 - principleAngle;
            } else {
                nextTarget = 270 - principleAngle;
            }
            return nextTarget;
        }
    }

    /** Updates the lateral position of the aircraft and its label, removes aircraft if it goes out of radar range */
    private void updatePosition(double angleDiff) {
        //Angle diff is angle correction due to winds = track - heading
        track = heading - radarScreen.magHdgDev + angleDiff;
        deltaPosition.x = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * (float) Math.cos(Math.toRadians(90 - track));
        deltaPosition.y = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * (float) Math.sin(Math.toRadians((90 - track)));
        x += deltaPosition.x;
        y += deltaPosition.y;

        float dist = MathTools.pixelToNm(MathTools.distanceBetween(0, 0, deltaPosition.x, deltaPosition.y));
        if (!onGround) prevDistTravelled += dist;
        if (prevDistTravelled > 0.5) {
            prevDistTravelled -= 0.5;
            radarScreen.wakeManager.addPoint(this);
        }
        float diffDist = radarScreen.wakeManager.checkAircraftWake(this);
        if (diffDist < 0) {
            //Safe separation
            wakeInfringe = false;
            wakeTolerance -= Gdx.graphics.getDeltaTime() * 2;
        } else {
            wakeInfringe = true;
            wakeTolerance += Gdx.graphics.getDeltaTime() * diffDist;
        }
        if (wakeTolerance < 0) wakeTolerance = 0;

        if (!locCap && ils != null && ils.isInsideILS(x, y)) {
            locCap = true;
            navState.replaceAllHdgModes();
            navState.getLatModes().removeValue(getSidStar().getName() + " arrival", false);
            navState.getLatModes().removeValue("After waypoint, fly heading", false);
            navState.getLatModes().removeValue("Hold at", false);
            if (selected) {
                ui.updateState();
            }
        }
        if (x < 1260 || x > 4500 || y < 0 || y > 3240) {
            if (this instanceof Arrival) {
                radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.95f));
                radarScreen.getCommBox().warningMsg(callsign + " has left the airspace!");
            } else if (this instanceof Departure && navState != null && navState.getDispLatMode().last().contains("departure") && navState.getClearedAlt().last() == radarScreen.maxAlt) {
                //Contact centre if departure is on SID, is not high enough but is cleared to highest altitude
                contactOther();
            }
            removeAircraft();
        }
    }

    /** Finds the deltaHeading with the appropriate force direction under different circumstances */
    private double findDeltaHeading(double targetHeading) {
        int forceDirection = 0;
        if (navState.getDispLatMode().first().equals("Turn left heading")) {
            forceDirection = 1;
        } else if (navState.getDispLatMode().first().equals("Turn right heading")) {
            forceDirection = 2;
        } else if (navState.getDispLatMode().first().equals("Hold at") && holding && !init) {
            if (holdingType == 1 && MathTools.pixelToNm(MathTools.distanceBetween(x, y, holdWpt.getPosX(), holdWpt.getPosY())) >= route.getHoldProcedure().getLegDistAtWpt(holdWpt)) {
                forceDirection = route.getHoldProcedure().isLeftAtWpt(holdWpt) ? 2 : 1;
            } else if (holdingType == 2 || holdingType == 3) {
                forceDirection = route.getHoldProcedure().isLeftAtWpt(holdWpt) ? 1 : 2;
            }
        } else if (this instanceof Departure && navState.getDispLatMode().first().contains("departure")) {
            //Force directions for certain departure procedures
            if (getSidStar().getName().contains("NANKO1") && runway.getName().contains("06") && direct != null && "NANKO".equals(direct.getName()) && heading > 90 && heading < 320) {
                //RJBB NANKO1(6L) and NANKO1(6R) departures
                forceDirection = 1;
            } else if (getSidStar().getName().contains("KANSAI1") && runway.getName().contains("24") && direct != null && "KNE".equals(direct.getName()) && heading > 180 && heading <= 360) {
                //RJBB KANSAI1(24L) and KANSAI1(24R) departures
                forceDirection = 2;
            } else if ("ASUKA4".equals(getSidStar().getName()) && direct != null && "ASUKA".equals(direct.getName()) && heading > 180 && heading <= 360) {
                //RJOO ASUKA4 departure
                forceDirection = 1;
            } else if ("OTSU5".equals(getSidStar().getName()) && direct != null && "D101P".equals(direct.getName()) && heading > 180 && heading <= 360) {
                //RJOO OTSU5 departure
                forceDirection = 1;
            } else if ("MINAC3".equals(getSidStar().getName()) && direct != null && "ITE16".equals(direct.getName()) && heading > 180 && heading <= 360) {
                //RJOO MINAC3 departure
                forceDirection = 1;
            } else if ((getSidStar().getName().contains("NLG2D") || "SHL2D".equals(getSidStar().getName()) || "MIPAG2D".equals(getSidStar().getName())) && direct != null && "MCU".equals(direct.getName()) && heading > 90 && heading <= 360) {
                //VMMC NLG2D, SHL2D and MIPAG2D departures
                forceDirection = 2;
            } else if ("OPPAR3".equals(getSidStar().getName()) && runway.getName().contains("16") && direct != null && "OPPAR".equals(direct.getName()) && heading < 165) {
                //RJTT OPPAR3 departure
                forceDirection = 1;
            }
        }
        return findDeltaHeading(targetHeading, forceDirection, heading);
    }

    /** Finds the deltaHeading for the leg after current leg */
    private double findSidStarDeltaHeading(double targetHeading, double prevHeading) {
        if (targetHeading == -1) return 0; //Return 0 if last waypoint is outside radar, no early turn required
        return findDeltaHeading(targetHeading, 0, prevHeading);
    }

    /** Finds the deltaHeading given a forced direction */
    private double findDeltaHeading(double targetHeading, int forceDirection, double heading) {
        double deltaHeading = targetHeading - heading;
        while (deltaHeading > 360) deltaHeading -= 360;
        while (deltaHeading < -360) deltaHeading += 360;
        switch (forceDirection) {
            case 0: //Not specified: pick quickest direction
                if (deltaHeading > 180) {
                    deltaHeading -= 360; //Turn left: deltaHeading is -ve
                } else if (deltaHeading <= -180) {
                    deltaHeading += 360; //Turn right: deltaHeading is +ve
                }
                break;
            case 1: //Must turn left
                if (deltaHeading > 0) {
                    deltaHeading -= 360;
                }
                break;
            case 2: //Must turn right
                if (deltaHeading < 0) {
                    deltaHeading += 360;
                }
                break;
            default:
                Gdx.app.log("Direction error", "Invalid turn direction specified!");
        }
        return deltaHeading;
    }

    /** Updates the aircraft heading given an input targetHeading */
    private void updateHeading(double targetHeading) {
        double deltaHeading = findDeltaHeading(targetHeading);
        //Note: angular velocities unit is change in heading per second
        double targetAngularVelocity = 0;
        if (deltaHeading > 0) {
            //Aircraft needs to turn right
            targetAngularVelocity = ias > 250 ? 1.5 : 3;
        } else if (deltaHeading < 0) {
            //Aircraft needs to turn left
            targetAngularVelocity = ias > 250 ? -1.5 : -3;
        }
        if (Math.abs(deltaHeading) <= 10) {
            targetAngularVelocity = deltaHeading / 3;
            if (navState.getDispLatMode().first().contains("Turn")) {
                navState.replaceAllHdgModes();
                if (selected && isArrivalDeparture()) {
                    updateUISelections();
                    ui.updateState();
                }
            }
        }
        //Update angular velocity towards target angular velocity
        if (targetAngularVelocity > angularVelocity + 0.1f) {
            //If need to turn right, start turning right
            angularVelocity += 0.5f * Gdx.graphics.getDeltaTime();
        } else if (targetAngularVelocity < angularVelocity - 0.1f) {
            //If need to turn left, start turning left
            angularVelocity -= 0.5f * Gdx.graphics.getDeltaTime();
        } else {
            //If within +-0.1 of target, set equal to target
            angularVelocity = targetAngularVelocity;
        }

        //Add angular velocity to heading
        heading = heading + angularVelocity * Gdx.graphics.getDeltaTime();
        if (heading > 360) {
            heading -= 360;
        } else if (heading <= 0) {
            heading += 360;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        for (int i = 0; i < radarScreen.speed; i++) {
            if (radarScreen.tutorialManager != null && radarScreen.tutorialManager.isPauseForReading()) break;
            update();
        }
        dataTag.updateLabel();
        dataTag.updateIcon(batch);

        dataTag.drawTrailDots(batch, parentAlpha);

        if (selected) radarScreen.wakeManager.drawSepRequired(batch, this);
    }

    /** Updates direct waypoint of aircraft to next waypoint in SID/STAR, or switches to vector mode if after waypoint, fly heading option selected */
    private void updateDirect() {
        sidStarIndex++;
        if (direct.equals(afterWaypoint) && navState.getDispLatMode().first().equals("After waypoint, fly heading")) {
            clearedHeading = afterWptHdg;
            navState.getLatModes().removeValue("After waypoint, fly heading", false);
            navState.getLatModes().removeValue("Hold at", false);
            if (navState.getDispAltMode().first().contains("STAR")) {
                navState.getDispAltMode().removeFirst();
                navState.getDispAltMode().addFirst("Climb/descend to");
            }
            if (navState.getDispSpdMode().first().contains("STAR")) {
                navState.getDispSpdMode().removeFirst();
                navState.getDispSpdMode().addFirst("No speed restrictions");
            }
            navState.getClearedHdg().removeFirst();
            navState.getClearedHdg().addFirst(afterWptHdg);
            updateVectorMode();
            direct = null;
        } else if (direct.equals(holdWpt) && navState.getDispLatMode().first().equals("Hold at")) {
            holding = true;
            int spdRestr = route.getHoldProcedure().getMaxSpdAtWpt(holdWpt);
            if (spdRestr > -1 && clearedIas > spdRestr) {
                clearedIas = spdRestr;
            }
            direct = route.getWaypoint(sidStarIndex);
            if (direct == null) {
                navState.getLatModes().removeValue(getSidStar().getName() + " arrival", false);
            }
            radarScreen.getCommBox().holdEstablishMsg(this, holdWpt.getName());
        } else {
            direct = route.getWaypoint(sidStarIndex);
            if (direct == null) {
                navState.getDispLatMode().removeFirst();
                navState.getDispLatMode().addFirst("Fly heading");
                navState.replaceAllClearedAltMode();
                setAfterLastWpt();
            }
        }
        navState.getClearedDirect().removeFirst();
        navState.getClearedDirect().addFirst(direct);
        updateAltRestrictions();
        updateTargetAltitude();
        updateClearedSpd();
        if (selected && isArrivalDeparture()) {
            updateUISelections();
            ui.updateState();
        }
    }

    /** Overriden method that sets aircraft heading after the last waypoint is reached */
    public void setAfterLastWpt() {
        //No default implementation
    }

    /** Switches aircraft latMode to vector, sets active nav state latMode to vector */
    public void updateVectorMode() {
        //Switch aircraft latmode to vector mode
        navState.getDispLatMode().removeFirst();
        navState.getDispLatMode().addFirst("Fly heading");
    }

    /** Removes the SID/STAR options from aircraft UI after there are no waypoints left*/
    public void removeSidStarMode() {
        if (!navState.getLatModes().removeValue(getSidStar().getName() + " arrival", false)) {
            navState.getLatModes().removeValue(getSidStar().getName() + " departure", false);
        } else {
            navState.getLatModes().removeValue("After waypoint, fly heading", false);
            if (!"Hold at".equals(getNavState().getDispLatMode().last())) navState.getLatModes().removeValue("Hold at", false);
        }
        if (selected && isArrivalDeparture()) {
            ui.updateState();
        }
    }

    /** Updates the control state of the aircraft, and updates the UI pane visibility if aircraft is selected */
    public void setControlState(ControlState controlState) {
        this.controlState = controlState;
        dataTag.updateIconColors(controlState);
        actionRequired = actionRequired && isArrivalDeparture();
        if (selected) {
            if (controlState == ControlState.UNCONTROLLED || controlState == ControlState.ENROUTE) {
                ui.setNormalPane(true);
                ui.setSelectedPane(null);
            } else {
                ui.setNormalPane(false);
                ui.setSelectedPane(this);
            }
        }
    }

    /** Returns whether control state of aircraft is arrival or departure */
    public boolean isArrivalDeparture() {
        return controlState == ControlState.ARRIVAL || controlState == ControlState.DEPARTURE;
    }

    /** Updates the selections in the UI when it is active and aircraft state changes that requires selections to change in order to be valid */
    public void updateUISelections() {
        ui.latTab.getSettingsBox().setSelected(navState.getDispLatMode().last());
        LatTab.clearedHdg = clearedHeading;
        if (direct != null && ui.latTab.getSettingsBox().getSelected().contains(getSidStar().getName())) {
            ui.latTab.getValueBox().setSelected(direct.getName());
        }

        if (this instanceof Departure && !ui.altTab.valueBox.getSelected().contains("FL") && Integer.parseInt(ui.altTab.valueBox.getSelected()) < lowestAlt) {
            ui.altTab.getValueBox().setSelected(Integer.toString(lowestAlt));
        }

        ui.spdTab.getValueBox().setSelected(Integer.toString(clearedIas));
    }

    /** Gets the current aircraft data and sets the radar data to it, called after every radar sweep */
    public void updateRadarInfo() {
        dataTag.moveLabel(x - radarX, y - radarY);
        radarX = x;
        radarY = y;
        radarHdg = heading;
        radarTrack = track;
        radarAlt = altitude;
        radarGs = gs;
        radarVs = verticalSpeed;
    }

    public Array<Waypoint> getRemainingWaypoints() {
        if (navState.getDispLatMode().last().contains(getSidStar().getName())) {
            return route.getRemainingWaypoints(route.findWptIndex(navState.getClearedDirect().last().getName()), route.getWaypoints().size - 1);
        } else if ("After waypoint, fly heading".equals(navState.getDispLatMode().last())) {
            return route.getRemainingWaypoints(route.findWptIndex(navState.getClearedDirect().last().getName()), route.findWptIndex(navState.getClearedAftWpt().last().getName()));
        } else if ("Hold at".equals(navState.getDispLatMode().last())) {
            return route.getRemainingWaypoints(route.findWptIndex(navState.getClearedDirect().last().getName()), route.findWptIndex(navState.getClearedHold().last().getName()));
        }
        return null;
    }

    public Array<Waypoint> getUiRemainingWaypoints() {
        if (selected) {
            if (Tab.latMode.contains(getSidStar().getName())) {
                return route.getRemainingWaypoints(route.findWptIndex(Tab.clearedWpt), route.getWaypoints().size - 1);
            } else if ("After waypoint, fly heading".equals(Tab.latMode)) {
                return route.getRemainingWaypoints(sidStarIndex, route.findWptIndex(Tab.afterWpt));
            } else if ("Hold at".equals(Tab.latMode)) {
                return route.getRemainingWaypoints(sidStarIndex, route.findWptIndex(Tab.holdWpt));
            }
        }
        return null;
    }

    /** Checks if aircraft is being manually vectored */
    public boolean isVectored() {
        return navState.getDispLatMode().last().contains("Turn") || navState.getDispLatMode().last().equals("Fly heading");
    }

    public SidStar getSidStar() {
        return null;
    }

    public int getSidStarIndex() {
        return sidStarIndex;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Airport getAirport() {
        return airport;
    }

    public void setAirport(Airport airport) {
        this.airport = airport;
    }

    public Runway getRunway() {
        return runway;
    }

    public void setRunway(Runway runway) {
        this.runway = runway;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isTkOfLdg() {
        return tkOfLdg;
    }

    public void setTkOfLdg(boolean tkOfLdg) {
        this.tkOfLdg = tkOfLdg;
    }

    public String getCallsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }

    public String getIcaoType() {
        return icaoType;
    }

    public void setIcaoType(String icaoType) {
        this.icaoType = icaoType;
    }

    public char getWakeCat() {
        return wakeCat;
    }

    public void setWakeCat(char wakeCat) {
        this.wakeCat = wakeCat;
    }

    public int getV2() {
        return v2;
    }

    public void setV2(int v2) {
        this.v2 = v2;
    }

    public int getTypClimb() {
        return typClimb;
    }

    public void setTypClimb(int typClimb) {
        this.typClimb = typClimb;
    }

    public int getTypDes() {
        return typDes;
    }

    public void setTypDes(int typDes) {
        this.typDes = typDes;
    }

    public int getMaxDes() {
        return maxDes;
    }

    public void setMaxDes(int maxDes) {
        this.maxDes = maxDes;
    }

    public int getApchSpd() {
        return apchSpd;
    }

    public void setApchSpd(int apchSpd) {
        this.apchSpd = apchSpd;
    }

    public ControlState getControlState() {
        return controlState;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getTargetHeading() {
        return targetHeading;
    }

    public void setTargetHeading(double targetHeading) {
        this.targetHeading = targetHeading;
    }

    public int getClearedHeading() {
        return clearedHeading;
    }

    public void setClearedHeading(int clearedHeading) {
        this.clearedHeading = clearedHeading;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public double getTrack() {
        return track;
    }

    public void setTrack(double track) {
        this.track = track;
    }

    public Waypoint getDirect() {
        return direct;
    }

    public void setDirect(Waypoint direct) {
        this.direct = direct;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public int getClearedAltitude() {
        return clearedAltitude;
    }

    public void setClearedAltitude(int clearedAltitude) {
        this.clearedAltitude = clearedAltitude;
        updateAltRestrictions();
        updateTargetAltitude();
    }

    public int getTargetAltitude() {
        return targetAltitude;
    }

    public void setTargetAltitude(int targetAltitude) {
        this.targetAltitude = targetAltitude;
    }

    /** Gets current cleared altitude, compares it to highest and lowest possible altitudes, sets the target altitude and possibly the cleared altitude itself */
    public void updateTargetAltitude() {
        //When called, gets current cleared altitude, alt nav mode and updates the target altitude of aircraft
        if (navState.getDispAltMode().first().contains("/")) {
            //No alt restrictions
            targetAltitude = clearedAltitude;
        } else {
            //Restrictions
            if (clearedAltitude > highestAlt) {
                if (this instanceof Arrival) {
                    clearedAltitude = highestAlt;
                    navState.replaceAllClearedAlt();
                }
                targetAltitude = highestAlt;
            } else if (clearedAltitude < lowestAlt) {
                if (this instanceof Departure) {
                    clearedAltitude = lowestAlt;
                    navState.replaceAllClearedAlt();
                }
                targetAltitude = lowestAlt;
            } else {
                targetAltitude = clearedAltitude;
            }
        }
    }

    /** Updates the cleared IAS under certain circumstances */
    private void updateClearedSpd() {
        int highestSpd = -1;
        if (!"No speed restrictions".equals(navState.getDispSpdMode().last()) && direct != null) {
            highestSpd = route.getWptMaxSpd(direct.getName());
        }
        if (highestSpd == -1) {
            if (altitude > 10000) {
                highestSpd = climbSpd;
            } else {
                highestSpd = 250;
            }
        }
        if (clearedIas > highestSpd) {
            clearedIas = highestSpd;
            navState.replaceAllClearedSpdToLower();
            if (selected && isArrivalDeparture()) updateUISelections();
        }
    }

    /** Removes the aircraft completely from game, including its labels, other elements */
    public void removeAircraft() {
        dataTag.removeLabel();
        remove();
        radarScreen.getAllAircraft().remove(callsign);
        radarScreen.aircrafts.remove(callsign);
        radarScreen.separationChecker.updateAircraftPositions();
        radarScreen.wakeManager.removeAircraft(callsign);
    }

    /** Overriden method that sets the altitude restrictions of the aircraft */
    public void updateAltRestrictions() {
        //No default implementation
    }

    /** Overriden method that resets the booleans in arrival checking whether the appropriate speeds during approach have been set */
    public void resetApchSpdSet() {
        //No default implementation
    }

    /** Appends a new image to end of queue for aircraft's own position */
    public void addTrailDot() {
        if (gs <= 80) return; //Don't add dots if below 80 knots ground speed
        dataTag.addTrailDot(x, y);
    }

    /** Returns heavy/super if wake category is heavy or super */
    public String getWakeString() {
        if (wakeCat == 'H') return " heavy";
        if (wakeCat == 'J') return " super";
        return "";
    }

    public float getVerticalSpeed() {
        return verticalSpeed;
    }

    public void setVerticalSpeed(float verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    public boolean isExpedite() {
        return expedite;
    }

    public void setExpedite(boolean expedite) {
        this.expedite = expedite;
    }

    public float getIas() {
        return ias;
    }

    public void setIas(float ias) {
        this.ias = ias;
    }

    public float getTas() {
        return tas;
    }

    public void setTas(float tas) {
        this.tas = tas;
    }

    public float getGs() {
        return gs;
    }

    public void setGs(float gs) {
        this.gs = gs;
    }

    public Vector2 getDeltaPosition() {
        return deltaPosition;
    }

    public int getClearedIas() {
        return clearedIas;
    }

    public void setClearedIas(int clearedIas) {
        this.clearedIas = clearedIas;
        updateClearedSpd();
    }

    public float getDeltaIas() {
        return deltaIas;
    }

    public NavState getNavState() {
        return navState;
    }

    public void setNavState(NavState navState) {
        this.navState = navState;
    }

    public int getMaxClimb() {
        return maxClimb;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    public void setSidStarIndex(int sidStarIndex) {
        this.sidStarIndex = sidStarIndex;
    }

    public Waypoint getAfterWaypoint() {
        return afterWaypoint;
    }

    public void setAfterWaypoint(Waypoint afterWaypoint) {
        this.afterWaypoint = afterWaypoint;
    }

    public int getAfterWptHdg() {
        return afterWptHdg;
    }

    public void setAfterWptHdg(int afterWptHdg) {
        this.afterWptHdg = afterWptHdg;
    }

    public int getLowestAlt() {
        return lowestAlt;
    }

    public void setLowestAlt(int lowestAlt) {
        this.lowestAlt = lowestAlt;
    }

    public int getHighestAlt() {
        return highestAlt;
    }

    public void setHighestAlt(int highestAlt) {
        this.highestAlt = highestAlt;
    }

    public int getClimbSpd() {
        return climbSpd;
    }

    public int getMaxWptSpd(String wpt) {
        return route.getWptMaxSpd(wpt);
    }

    public ILS getIls() {
        return ils;
    }

    public void setIls(ILS ils) {
        if (this.ils != ils) {
            if (this instanceof Arrival) ((Arrival) this).setNonPrecAlts(null);
            if (locCap) {
                this.ils.getRwy().removeFromArray(this);
                if (selected && isArrivalDeparture()) ui.updateState();
            }
            gsCap = false;
            locCap = false;
            resetApchSpdSet();
            if (clearedIas < 160) {
                clearedIas = 160;
                navState.replaceAllClearedSpdToHigher();
            }
        }
        this.ils = ils;
    }

    public boolean isGsCap() {
        return gsCap;
    }

    public void setGsCap(boolean gsCap) {
        this.gsCap = gsCap;
    }

    public boolean isLocCap() {
        return locCap;
    }

    public Waypoint getHoldWpt() {
        if (holdWpt == null && "Hold at".equals(navState.getDispLatMode().last())) holdWpt = radarScreen.waypoints.get(navState.getClearedHold().last().getName());
        return holdWpt;
    }

    public void setHoldWpt(Waypoint holdWpt) {
        this.holdWpt = holdWpt;
    }

    public boolean isHolding() {
        return holding;
    }

    public boolean isGoAround() {
        return goAround;
    }

    public void setGoAround(boolean goAround) {
        this.goAround = goAround;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public float getRadarX() {
        return radarX;
    }

    public float getRadarY() {
        return radarY;
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    public boolean isGoAroundWindow() {
        return goAroundWindow;
    }

    public void setGoAroundWindow(boolean goAroundWindow) {
        this.goAroundWindow = goAroundWindow;
        if (goAroundWindow) {
            goAroundTime = 120;
        }
    }

    public float getGoAroundTime() {
        return goAroundTime;
    }

    public boolean isInit() {
        return init;
    }

    public boolean isType1leg() {
        return type1leg;
    }

    public float[][] getHoldTargetPt() {
        return holdTargetPt;
    }

    public boolean[] getHoldTargetPtSelected() {
        return holdTargetPtSelected;
    }

    public float getPrevAlt() {
        return prevAlt;
    }

    public double getRadarHdg() {
        return radarHdg;
    }

    public double getRadarTrack() {
        return radarTrack;
    }

    public float getRadarGs() {
        return radarGs;
    }

    public float getRadarAlt() {
        return radarAlt;
    }

    public float getRadarVs() {
        return radarVs;
    }

    public boolean isTerrainConflict() {
        return terrainConflict;
    }

    public void setTerrainConflict(boolean terrainConflict) {
        this.terrainConflict = terrainConflict;
    }

    public float getExpediteTime() {
        return expediteTime;
    }

    public void setExpediteTime(float expediteTime) {
        this.expediteTime = expediteTime;
    }

    public String getVoice() {
        return voice;
    }

    public void setRadarHdg(double radarHdg) {
        this.radarHdg = radarHdg;
    }

    public DataTag getDataTag() {
        return dataTag;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(boolean actionRequired) {
        this.actionRequired = actionRequired;
    }

    public boolean isFuelEmergency() {
        return fuelEmergency;
    }

    public void setFuelEmergency(boolean fuelEmergency) {
        this.fuelEmergency = fuelEmergency;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public int getHoldingType() {
        return holdingType;
    }

    public void setHoldingType(int holdingType) {
        this.holdingType = holdingType;
    }

    public Emergency getEmergency() {
        return emergency;
    }

    public void setMaxClimb(int maxClimb) {
        this.maxClimb = maxClimb;
    }

    public float getPrevDistTravelled() {
        return prevDistTravelled;
    }

    public void setPrevDistTravelled(float prevDistTravelled) {
        this.prevDistTravelled = prevDistTravelled;
    }

    public char getRecat() {
        return recat;
    }

    public void setRecat(char recat) {
        this.recat = recat;
    }

    public boolean isWakeInfringe() {
        return wakeInfringe;
    }

    public void setWakeInfringe(boolean wakeInfringe) {
        this.wakeInfringe = wakeInfringe;
    }

    public float getWakeTolerance() {
        return wakeTolerance;
    }

    public void setWakeTolerance(float wakeTolerance) {
        this.wakeTolerance = wakeTolerance;
    }
}