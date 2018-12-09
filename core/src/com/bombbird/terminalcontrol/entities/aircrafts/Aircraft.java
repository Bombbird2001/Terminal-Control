package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.screens.ui.LatTab;
import com.bombbird.terminalcontrol.screens.ui.Tab;
import com.bombbird.terminalcontrol.screens.ui.Ui;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.MathTools;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Aircraft extends Actor {
    //Rendering parameters
    public static TextureAtlas ICON_ATLAS;
    public static Skin SKIN;
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_CTRL = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_DEPT = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_UNCTRL = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_ENROUTE = new ImageButton.ImageButtonStyle();
    private static boolean LOADED_ICONS = false;

    public RadarScreen radarScreen;
    private Stage stage;
    public ShapeRenderer shapeRenderer;
    public Ui ui;

    private Label label;
    private String[] labelText;
    private boolean selected;
    private ImageButton icon;
    private Button labelButton;
    private Button clickSpot;
    private boolean dragging;
    private Color color;

    //Aircraft information
    private Airport airport;
    private Runway runway;
    private boolean onGround;
    private boolean tkOfLdg;

    //Aircraft characteristics
    private String callsign;
    private String icaoType;
    private char wakeCat;
    private int v2;
    private int typClimb;
    private int maxClimb;
    private int typDes;
    private int maxDes;
    private int apchSpd;
    private int controlState;
    private NavState navState;
    private boolean goAround;
    private boolean goAroundWindow;
    private float goAroundTime;
    private boolean conflict;
    private boolean warning;

    //Aircraft position
    private float x;
    private float y;
    private double heading;
    private double targetHeading;
    private int clearedHeading;
    private double angularVelocity;
    private double track;
    private int sidStarIndex;
    private Waypoint direct;
    private Waypoint afterWaypoint;
    private int afterWptHdg;
    private ILS ils;
    private boolean locCap;
    private Waypoint holdWpt;
    private boolean holding;
    private boolean init;
    private boolean type1leg;
    private float[][] holdTargetPt;
    private boolean[] holdTargetPtSelected;
    private Queue<Image> trailDots;

    //Altitude
    private float prevAlt;
    private float altitude;
    private int clearedAltitude;
    private int targetAltitude;
    private float verticalSpeed;
    private boolean expedite;
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
        float loadFactor = MathUtils.random(-1 , 1) / 40f;
        v2 = (int)(AircraftType.getV2(icaoType) * (1 + loadFactor));
        typClimb = (int)(AircraftType.getTypClimb(icaoType) * (1 - loadFactor));
        maxClimb = typClimb + 1000;
        typDes = (int)(AircraftType.getTypDes(icaoType) * (1 - loadFactor));
        maxDes = typDes + 1000;
        apchSpd = (int)(AircraftType.getApchSpd(icaoType) * (1 + loadFactor));
        this.airport = airport;
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
        ias = 250;
        tas = MathTools.iasToTas(ias, altitude);
        gs = tas;
        deltaPosition = new Vector2();
        clearedIas = 250;
        deltaIas = 0;
        tkOfLdg = false;
        gsCap = false;
        locCap = false;
        climbSpd = MathUtils.random(270, 285);
        goAround = false;
        goAroundWindow = false;
        goAroundTime = 0;
        conflict = false;
        trailDots = new Queue<Image>();

        selected = false;
        dragging = false;
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

        x = (float) save.getDouble("x");
        y = (float) save.getDouble("y");
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

        trailDots = new Queue<Image>();
        JSONArray trails = save.getJSONArray("trailDots");
        for (int i = 0; i < trails.length(); i++) {
            addTrailDot((float) trails.getJSONArray(i).getDouble(0), (float) trails.getJSONArray(i).getDouble(1));
        }

        prevAlt = (float) save.getDouble("prevAlt");
        altitude = (float) save.getDouble("altitude");
        clearedAltitude = save.getInt("clearedAltitude");
        targetAltitude = save.getInt("targetAltitude");
        verticalSpeed = (float) save.getDouble("verticalSpeed");
        expedite = save.getBoolean("expedite");
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
    }

    /** Loads & sets aircraft resources */
    private void loadResources() {
        radarScreen = TerminalControl.radarScreen;
        stage = radarScreen.stage;
        shapeRenderer = radarScreen.shapeRenderer;
        ui = radarScreen.ui;

        if (!LOADED_ICONS) {
            ICON_ATLAS = new TextureAtlas(Gdx.files.internal("game/aircrafts/aircraftIcons.atlas"));
            SKIN = new Skin(ICON_ATLAS);

            BUTTON_STYLE_CTRL.imageUp = SKIN.getDrawable("aircraftControlled");
            BUTTON_STYLE_CTRL.imageDown = SKIN.getDrawable("aircraftControlled");
            BUTTON_STYLE_DEPT.imageUp = SKIN.getDrawable("aircraftDeparture");
            BUTTON_STYLE_DEPT.imageDown = SKIN.getDrawable("aircraftDeparture");
            BUTTON_STYLE_UNCTRL.imageUp = SKIN.getDrawable("aircraftNotControlled");
            BUTTON_STYLE_UNCTRL.imageDown = SKIN.getDrawable("aircraftNotControlled");
            BUTTON_STYLE_ENROUTE.imageUp = SKIN.getDrawable("aircraftEnroute");
            BUTTON_STYLE_ENROUTE.imageDown = SKIN.getDrawable("aircraftEnroute");
            LOADED_ICONS = true;
        }
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
        icon = new ImageButton(BUTTON_STYLE_UNCTRL);
        icon.setSize(20, 20);
        icon.getImageCell().size(20, 20);
        stage.addActor(icon);

        labelText = new String[11];
        labelText[9] = airport.getIcao();
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont6;
        labelStyle.fontColor = Color.WHITE;
        label = new Label("Loading...", labelStyle);
        label.setPosition(x - label.getWidth() / 2, y + 25);

        labelButton = new Button(SKIN.getDrawable("labelBackgroundSmall"), SKIN.getDrawable("labelBackgroundSmall"));
        labelButton.setSize(label.getWidth() + 10, label.getHeight());

        clickSpot = new Button(new Button.ButtonStyle());
        clickSpot.setSize(labelButton.getWidth(), labelButton.getHeight());
        clickSpot.setName(callsign);
        clickSpot.addListener(new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                label.moveBy(x - labelButton.getWidth() / 2, y - labelButton.getHeight() / 2);
                dragging = true;
                event.handle();
            }
        });
        clickSpot.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!dragging) {
                    radarScreen.setSelectedAircraft(radarScreen.aircrafts.get(actor.getName()));
                } else {
                    dragging = false;
                }
            }
        });
        clickSpot.setDebug(true);

        Stage labelStage = TerminalControl.radarScreen.labelStage;
        labelStage.addActor(labelButton);
        labelStage.addActor(label);
        labelStage.addActor(clickSpot);
    }

    /** Renders shapes using shapeRenderer; all rendering should be called here */
    public void renderShape() {
        drawLatLines();
        moderateLabel();
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(label.getX() + label.getWidth() / 2, label.getY() + label.getHeight() / 2, radarX, radarY);
        if (controlState == 1 || controlState == 2) {
            shapeRenderer.setColor(color);
            shapeRenderer.line(radarX, radarY, radarX + radarScreen.trajectoryLine / 3600f * MathTools.nmToPixel(radarGs) * MathUtils.cosDeg((float)(90 - radarTrack)), radarY + radarScreen.trajectoryLine / 3600f * MathTools.nmToPixel(radarGs) * MathUtils.sinDeg((float)(90 - radarTrack)));
            //shapeRenderer.line(x, y, x + gs * MathUtils.cosDeg((float)(90 - track)), y + gs * MathUtils.sinDeg((float)(90 - track)));
        }
        clickSpot.drawDebug(shapeRenderer);
    }

    /** Draws the lines displaying the lateral status of aircraft */
    private void drawLatLines() {
        if (selected) {
            //Draws cleared status
            if (navState.getDispLatMode().last().contains("arrival") || navState.getDispLatMode().last().contains("departure") && direct != null) {
                drawSidStar();
            } else if (navState.getDispLatMode().last().equals("After waypoint, fly heading") && direct != null) {
                drawAftWpt();
            } else if (navState.getDispLatMode().last().contains("heading") && !locCap) {
                drawHdgLine();
            } else if (navState.getDispLatMode().last().equals("Hold at")) {
                drawHoldPattern();
            }

            //Draws selected status (from UI)
            if (controlState == 1 || controlState == 2) {
                //System.out.println("arrival/departure: " + (LatTab.latMode.contains("arrival") || LatTab.latMode.contains("departure")) + " hdgChanged: " + ui.latTab.isHdgChanged() + " latModeChanged: " + ui.latTab.isLatModeChanged() + " wptChanged: " + ui.latTab.isWptChanged());
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
        ((Star) getSidStar()).getHoldProcedure().renderShape(navState.getClearedHold().last());
    }

    /** Draws the holding pattern for the UI */
    public void uiDrawHoldPattern() {
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.line(radarX, radarY, radarScreen.waypoints.get(LatTab.clearedWpt).getPosX(), radarScreen.waypoints.get(LatTab.clearedWpt).getPosY());
        ((Star) getSidStar()).getHoldProcedure().renderShape(radarScreen.waypoints.get(LatTab.holdWpt));
    }

    /** The main update function, called during aircraft draw */
    public double update() {
        navState.updateTime();
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
            updateAltitude();
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
            return targetHeading;
        } else {
            gs = tas - airport.getWinds()[1] * MathUtils.cosDeg(airport.getWinds()[0] - runway.getHeading());
            updatePosition(0);
            return 0;
        }
    }

    /** Overriden method that updates the target speed of the aircraft depending on situation */
    public void updateSpd() {
        navState.getClearedSpd().removeFirst();
        navState.getClearedSpd().addFirst(clearedIas);
        if (selected && (controlState == 1 || controlState == 2)) {
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
        float max = 1.5f;
        float min = -2.25f;
        if (tkOfLdg) {
            max = 3;
            if (gs >= 60) {
                min = -4.5f;
            } else {
                min = -1.5f;
            }
        }
        if (deltaIas > max) {
            deltaIas = max;
        } else if (deltaIas< min) {
            deltaIas = min;
        }
        ias = ias + deltaIas * Gdx.graphics.getDeltaTime();
        if (Math.abs(clearedIas - ias) < 1) {
            ias = clearedIas;
        }
    }

    public void updateAltitude() {
        float targetVertSpd = (targetAltitude - altitude) / 0.1f;
        if (targetVertSpd > verticalSpeed + 100) {
            verticalSpeed = verticalSpeed + 500 * Gdx.graphics.getDeltaTime();
        } else if (targetVertSpd < verticalSpeed - 100) {
            verticalSpeed = verticalSpeed - 500 * Gdx.graphics.getDeltaTime();
        }
        float multiplier = altitude > 20000 ? 0.75f : 1;
        if (!expedite && verticalSpeed > typClimb * multiplier) {
            verticalSpeed = typClimb * multiplier;
        } else if (!expedite && verticalSpeed < -typDes * multiplier) {
            verticalSpeed = -typDes * multiplier;
        } else if (expedite && verticalSpeed > maxClimb * multiplier) {
            verticalSpeed = maxClimb * multiplier;
        } else if (expedite && verticalSpeed < -maxDes * multiplier) {
            verticalSpeed = -maxDes * multiplier;
        }
        altitude += verticalSpeed / 60 * Gdx.graphics.getDeltaTime();

        if (Math.abs(targetAltitude - altitude) < 50 && Math.abs(verticalSpeed) < 200) {
            altitude = targetAltitude;
            verticalSpeed = 0;
            if ("Expedite climb/descent to".equals(navState.getDispAltMode().first())) {
                navState.getDispAltMode().removeFirst();
                navState.getDispAltMode().addFirst("Climb/descend to");
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

        boolean sidstar = navState.getDispLatMode().first().contains(getSidStar().getName()) || navState.getDispLatMode().first().equals("After waypoint, fly heading") || navState.getDispLatMode().first().equals("Hold at");
        boolean vector = !sidstar && navState.getDispLatMode().first().contains("heading");

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
            targetHeading = calculatePointTargetHdg(new float[] {direct.getPosX(), direct.getPosY()}, windHdg, windSpd);

            //If within __px of waypoint, target next waypoint
            //Distance determined by angle that needs to be turned
            double distance = MathTools.distanceBetween(x, y, direct.getPosX(), direct.getPosY());
            double requiredDistance;
            String[] flyOverWpts = {"TP050", "TP060", "TP064", "TP230", "TP240", "TT501"};
            if (ArrayUtils.contains(flyOverWpts, direct.getName())) {
                requiredDistance = 2;
            } else {
                requiredDistance = findRequiredDistance(Math.abs(findSidStarDeltaHeading(findNextTargetHdg(), targetHeading)));
            }
            if (distance <= requiredDistance) {
                updateDirect();
            }
        } else if (locCap) {
            clearedHeading = ils.getHeading();
            if (!getIls().getRwy().equals(runway)) {
                runway = getIls().getRwy();
                navState.getLatModes().removeValue(getSidStar() + " arrival", false);
            }
            if (getIls() instanceof LDA && MathTools.pixelToNm(MathTools.distanceBetween(x, y, runway.getX(), runway.getY())) <= ((LDA) getIls()).getLineUpDist()) {
                float deltaX = 100 * MathUtils.cosDeg(90 - runway.getTrueHdg());
                float deltaY = 100 * MathUtils.sinDeg(90 - runway.getTrueHdg());
                targetHeading = calculatePointTargetHdg(deltaX, deltaY, windHdg, windSpd);
            } else {
                //Calculates x, y of point 0.75nm ahead of plane
                Vector2 position = this.getIls().getPointAhead(this);
                targetHeading = calculatePointTargetHdg(new float[] {position.x, position.y}, windHdg, windSpd);
            }
        } else if (holding) {
            if (!navState.getDispLatMode().first().equals("Hold at")) {
                holding = false;
                return updateTargetHeading();
            }
            Star star = (Star) getSidStar(); //For convenience
            if (holdTargetPt == null) {
                float[] point = star.getHoldProcedure().getOppPtAtWpt(holdWpt);
                holdTargetPt = new float[][] {{holdWpt.getPosX(), holdWpt.getPosY()}, point};
                holdTargetPtSelected = new boolean[] {false, false};
            }
            if (!init) {
                //Aircraft has just entered holding pattern, follow procedures relevant to each type of holding pattern entry
                int type = star.getHoldProcedure().getEntryProcAtWpt(holdWpt);
                if (type == 1) {
                    //After reaching waypoint, fly opposite inbound track, then after flying for leg dist, turn back to entry fix in direction opposite of holding direction
                    targetHeading = star.getHoldProcedure().getInboundHdgAtWpt(holdWpt) + 180;
                    if (MathTools.pixelToNm(MathTools.distanceBetween(x, y, holdWpt.getPosX(), holdWpt.getPosY())) >= star.getHoldProcedure().getLegDistAtWpt(holdWpt) || type1leg) {
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
                    double deltaHdg = findDeltaHeading(star.getHoldProcedure().getInboundHdgAtWpt(holdWpt) + 180);
                    boolean left = star.getHoldProcedure().isLeftAtWpt(holdWpt);
                    if (left && deltaHdg > -150 || !left && deltaHdg < 150) {
                        //Set init to true once aircraft has turned to a heading of not more than 150 deg offset from target, in the turn direction
                        init = true;
                    }
                }
            } else {
                float track = star.getHoldProcedure().getInboundHdgAtWpt(holdWpt) - radarScreen.magHdgDev;
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
                targetHeading = calculatePointTargetHdg(new float[] {target[0] + distance * MathUtils.cosDeg(270 - track), target[1] + distance * MathUtils.sinDeg(270 - track)}, windHdg, windSpd);
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
        gs = (float) Math.sqrt(Math.pow(tas, 2) + Math.pow(windSpd, 2) - 2 * tas * windSpd * MathUtils.cosDeg((float) angle));
        return Math.asin(windSpd * MathUtils.sinDeg((float)angle) / gs) * MathUtils.radiansToDegrees;
    }

    private double calculatePointTargetHdg(float[] position, int windHdg, int windSpd) {
        return calculatePointTargetHdg(position[0] - x, position[1] - y, windHdg, windSpd);
    }

    private double calculatePointTargetHdg(float deltaX, float deltaY, int windHdg, int windSpd) {
        double angleDiff;

        //Find target track angle
        if (deltaX >= 0) {
            targetHeading = 90 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
        } else {
            targetHeading = 270 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
        }

        //Calculate required aircraft heading to account for winds
        //Using sine rule to determine angle between aircraft velocity and actual velocity
        double angle = windHdg - targetHeading;
        angleDiff = Math.asin(windSpd * MathUtils.sinDeg((float)angle) / tas) * MathUtils.radiansToDegrees;
        targetHeading -= angleDiff;  //Heading = track - anglediff

        //Add magnetic deviation to give magnetic heading
        targetHeading += radarScreen.magHdgDev;

        return targetHeading;
    }

    public double findNextTargetHdg() {
        if ((navState.getDispLatMode().first().equals("After waypoint, fly heading") && direct.equals(afterWaypoint)) || navState.getDispLatMode().first().equals("Hold at") && direct.equals(holdWpt)) {
            return clearedHeading;
        }
        Waypoint nextWpt = getSidStar().getWaypoint(getSidStarIndex() + 1);
        if (nextWpt == null) {
            return -1;
        } else {
            float deltaX = nextWpt.getPosX() - getDirect().getPosX();
            float deltaY = nextWpt.getPosY() - getDirect().getPosY();
            double nextTarget;
            if (deltaX >= 0) {
                nextTarget = 90 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            } else {
                nextTarget = 270 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            }
            return nextTarget;
        }
    }

    /** Updates the lateral position of the aircraft and its label, removes aircraft if it goes out of radar range */
    private void updatePosition(double angleDiff) {
        //Angle diff is angle correction due to winds = track - heading
        track = heading - radarScreen.magHdgDev + angleDiff;
        if (!onGround && getIls() != null && getIls() instanceof LDA && MathTools.pixelToNm(MathTools.distanceBetween(x, y, getIls().getRwy().getX(), getIls().getRwy().getY())) <= ((LDA) getIls()).getLineUpDist()) {
            //Set track && heading to runway
            track = getIls().getRwy().getTrueHdg();
            heading = track + angleDiff;
        }
        deltaPosition.x = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * MathUtils.cosDeg((float)(90 - track));
        deltaPosition.y = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * MathUtils.sinDeg((float)(90 - track));
        x += deltaPosition.x;
        y += deltaPosition.y;
        if (!locCap && getIls() != null && getIls().isInsideILS(x, y)) {
            locCap = true;
            navState.replaceAllHdgModes();
            navState.getLatModes().removeValue(getSidStar().getName() + " arrival", false);
            if (selected) {
                ui.updateState();
            }
        }
        if (x < 1260 || x > 4500 || y < 0 || y > 3240) {
            if (this instanceof Arrival) radarScreen.setArrivals(radarScreen.getArrivals() - 1);
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
            Star star = (Star) getSidStar();
            int type = star.getHoldProcedure().getEntryProcAtWpt(holdWpt);
            if (type == 1 && MathTools.pixelToNm(MathTools.distanceBetween(x, y, holdWpt.getPosX(), holdWpt.getPosY())) >= star.getHoldProcedure().getLegDistAtWpt(holdWpt)) {
                forceDirection = star.getHoldProcedure().isLeftAtWpt(holdWpt) ? 2 : 1;
            } else if (type == 2 || type == 3) {
                forceDirection = star.getHoldProcedure().isLeftAtWpt(holdWpt) ? 1 : 2;
            }
        }
        return findDeltaHeading(targetHeading, forceDirection, heading);
    }

    /** Finds the deltaHeading for the leg after current leg */
    private double findSidStarDeltaHeading(double targetHeading, double prevHeading) {
        return findDeltaHeading(targetHeading, 0, prevHeading);
    }

    /** Finds the deltaHeading given a forced direction */
    private double findDeltaHeading(double targetHeading, int forceDirection, double heading) {
        double deltaHeading = targetHeading - heading;
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
                if (selected && (controlState == 1 || controlState == 2)) {
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
        update();
        updateLabel();
        icon.setPosition(radarX - 10, radarY - 10);
        icon.setColor(Color.BLACK); //Icon doesn't draw without this for some reason
        icon.draw(batch, 1);

        int index = 0;
        int size = trailDots.size;
        for (Image trail: trailDots) {
            if (selected || (size - index <= 5 && (controlState == 1 || controlState == 2))) {
                trail.draw(batch, parentAlpha);
            }
            index++;
        }
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
            int spdRestr = ((Star) getSidStar()).getHoldProcedure().getMaxSpdAtWpt(holdWpt);
            if (clearedIas > spdRestr) {
                clearedIas = spdRestr;
            }
            direct = getSidStar().getWaypoint(sidStarIndex);
            if (direct == null) {
                navState.getLatModes().removeValue(getSidStar().getName() + " arrival", false);
            }
        } else {
            direct = getSidStar().getWaypoint(sidStarIndex);
            if (direct == null) {
                navState.getDispLatMode().removeFirst();
                navState.getDispLatMode().addFirst("Fly heading");
                setAfterLastWpt();
            }
        }
        navState.getClearedDirect().removeFirst();
        navState.getClearedDirect().addFirst(direct);
        updateAltRestrictions();
        updateTargetAltitude();
        updateClearedSpd();
        if (selected && (controlState == 1 || controlState == 2)) {
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
            navState.getLatModes().removeValue("Hold at", false);
        }
        if (selected && (controlState == 1 || controlState == 2)) {
            ui.updateState();
        }
    }

    /** Updates the control state of the aircraft, and updates the UI pane visibility if aircraft is selected */
    public void setControlState(int controlState) {
        this.controlState = controlState;
        if (controlState == -1) { //En route aircraft - gray
            icon.setStyle(BUTTON_STYLE_ENROUTE);
        } else if (controlState == 0) { //Uncontrolled aircraft - yellow
            icon.setStyle(BUTTON_STYLE_UNCTRL);
        } else if (controlState == 1) { //Controlled arrival - blue
            icon.setStyle(BUTTON_STYLE_CTRL);
        } else if (controlState == 2) { //Controlled departure - green
            icon.setStyle(BUTTON_STYLE_DEPT);
        } else {
            Gdx.app.log("Aircraft control state error", "Invalid control state " + controlState + " set!");
        }
        if (selected) {
            if (controlState == -1 || controlState == 0) {
                ui.setNormalPane(true);
                ui.setSelectedPane(null);
            } else {
                ui.setNormalPane(false);
                ui.setSelectedPane(this);
            }
        }
    }

    /** Updates the position of the label to prevent it from going out of bounds */
    private void moderateLabel() {
        if (label.getX() < 936) {
            label.setX(936);
        } else if (label.getX() + label.getWidth() > 4824) {
            label.setX(4824 - label.getWidth());
        }
        if (label.getY() < 0) {
            label.setY(0);
        } else if (label.getY() + label.getHeight() > 3240) {
            label.setY(3240 - label.getHeight());
        }
    }

    /** Updates the label on the radar given aircraft's radar data and other data */
    public void updateLabel() {
        String vertSpd;
        if (radarVs < -150) {
            vertSpd = " DOWN ";
        } else if (radarVs > 150) {
            vertSpd = " UP ";
        } else {
            vertSpd = " = ";
        }
        labelText[0] = callsign;
        labelText[1] = icaoType + "/" + wakeCat;
        labelText[2] = Integer.toString(MathUtils.round(radarAlt / 100));
        labelText[3] = gsCap ? "GS" : Integer.toString(targetAltitude / 100);
        labelText[10] = Integer.toString(navState.getClearedAlt().last() / 100);
        if ((MathUtils.round((float) radarHdg) == 0)) {
            radarHdg += 360;
        }
        labelText[4] = Integer.toString(MathUtils.round((float) radarHdg));
        if (navState.getDispLatMode().first().contains("heading") && !navState.getDispLatMode().first().equals("After waypoint, fly heading")) {
            if (locCap) {
                labelText[5] = "LOC";
            } else {
                labelText[5] = Integer.toString(navState.getClearedHdg().last());
            }
        } else if ("Hold at".equals(navState.getDispLatMode().last())) {
            if (holding || (direct != null && direct.equals(holdWpt))) {
                labelText[5] = holdWpt.getName();
            } else if (direct != null) {
                labelText[5] = direct.getName();
            }
        } else if (navState.getDispLatMode().last().contains(getSidStar().getName()) || navState.getDispLatMode().last().equals("After waypoint, fly heading")) {
            if (navState.getClearedDirect().last().equals(navState.getClearedAftWpt().last()) && navState.getDispLatMode().last().equals("After waypoint, fly heading")) {
                labelText[5] = navState.getClearedDirect().last().getName() + navState.getClearedAftWptHdg().last();
            } else {
                labelText[5] = navState.getClearedDirect().last().getName();
            }
        }
        labelText[6] = Integer.toString((int) radarGs);
        labelText[7] = Integer.toString(navState.getClearedSpd().last());
        if (navState.getClearedIls().last() != null) {
            labelText[8] = navState.getClearedIls().last().getName();
        } else {
            labelText[8] = getSidStar().getName();
        }
        String exped = navState.getClearedExpedite().last() ? " =>> " : " => ";
        String updatedText;
        if (getControlState() == 1 || getControlState() == 2) {
            updatedText = labelText[0] + " " + labelText[1] + "\n" + labelText[2] + vertSpd + labelText[3] + exped + labelText[10] + "\n" + labelText[4] + " " + labelText[5] + " " + labelText[8] + "\n" + labelText[6] + " " + labelText[7] + " " + labelText[9];
        } else {
            updatedText = labelText[0] + "\n" + labelText[2] + " " + labelText[4] + "\n" + labelText[6];
        }
        label.setText(updatedText);
        label.pack();
        labelButton.setSize(label.getWidth() + 10, label.getHeight());
        labelButton.setPosition(label.getX() - 5, label.getY());
        clickSpot.setSize(labelButton.getWidth(), labelButton.getHeight());
        clickSpot.setPosition(labelButton.getX(), labelButton.getY());
    }

    /** Updates the selections in the UI when it is active and aircraft state changes that requires selections to change in order to be valid */
    private void updateUISelections() {
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
        label.moveBy(x - radarX, y - radarY);
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
            return getSidStar().getRemainingWaypoints(getSidStar().findWptIndex(navState.getClearedDirect().last().getName()), getSidStar().getWaypoints().size - 1);
        } else if ("After waypoint, fly heading".equals(navState.getDispLatMode().last())) {
            return getSidStar().getRemainingWaypoints(getSidStar().findWptIndex(navState.getClearedDirect().last().getName()), getSidStar().findWptIndex(navState.getClearedAftWpt().last().getName()));
        } else if ("Hold at".equals(navState.getDispLatMode().last())) {
            return getSidStar().getRemainingWaypoints(getSidStar().findWptIndex(navState.getClearedDirect().last().getName()), getSidStar().findWptIndex(navState.getClearedHold().last().getName()));
        }
        return null;
    }

    public Array<Waypoint> getUiRemainingWaypoints() {
        if (selected) {
            if (Tab.latMode.contains(getSidStar().getName())) {
                return getSidStar().getRemainingWaypoints(getSidStar().findWptIndex(Tab.clearedWpt), getSidStar().getWaypoints().size - 1);
            } else if ("After waypoint, fly heading".equals(Tab.latMode)) {
                return getSidStar().getRemainingWaypoints(sidStarIndex, getSidStar().findWptIndex(Tab.afterWpt));
            } else if ("Hold at".equals(Tab.latMode)) {
                return getSidStar().getRemainingWaypoints(sidStarIndex, getSidStar().findWptIndex(Tab.holdWpt));
            }
        }
        return null;
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

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public String[] getLabelText() {
        return labelText;
    }

    public void setLabelText(String[] labelText) {
        this.labelText = labelText;
    }

    public boolean isSelected() {
        return selected;
    }

    public ImageButton getIcon() {
        return icon;
    }

    public void setIcon(ImageButton icon) {
        this.icon = icon;
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

    public int getControlState() {
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
                targetAltitude = highestAlt;
            } else if (clearedAltitude < lowestAlt) {
                if (this instanceof Departure) {
                    clearedAltitude = lowestAlt;
                    navState.getClearedAlt().removeFirst();
                    navState.getClearedAlt().addFirst(clearedAltitude);
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
            highestSpd = getSidStar().getWptMaxSpd(direct.getName());
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
            if (selected && (controlState == 1 || controlState == 2)) updateUISelections();
        }
    }

    /** Removes the aircraft completely from game, including its labels, other elements */
    public void removeAircraft() {
        label.remove();
        icon.remove();
        labelButton.remove();
        clickSpot.remove();
        remove();
        radarScreen.getAllAircraft().remove(callsign);
        radarScreen.aircrafts.remove(callsign);
    }

    /** Overriden method that sets the altitude restrictions of the aircraft */
    public void updateAltRestrictions() {
        //No default implementation
    }

    /** Overriden method that resets the booleans in arrival checking whether the appropriate speeds during approach have been set */
    public void resetApchSpdSet() {
        //No default implementation
    }

    /** Appends a new image to end of queue for drawing trail dots given a set of coordinates */
    private void addTrailDot(float x, float y) {
        Image image;
        if (this instanceof Arrival) {
            image = new Image(SKIN.getDrawable("DotsArrival"));
        } else if (this instanceof Departure) {
            image = new Image(SKIN.getDrawable("DotsDeparture"));
        } else {
            image = new Image();
            Gdx.app.log("Trail dot error", "Aircraft not instance of arrival or departure, trail image not found!");
        }
        image.setPosition(x - image.getWidth() / 2, y - image.getHeight() / 2);
        trailDots.addLast(image);
    }

    /** Appends a new image to end of queue for aircraft's own position */
    public void addTrailDot() {
        if (gs <= 80) return; //Don't add dots if below 80 knots ground speed
        addTrailDot(x, y);
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
        return getSidStar().getWptMaxSpd(wpt);
    }

    public ILS getIls() {
        return ils;
    }

    public void setIls(ILS ils) {
        if (this.ils != ils) {
            if (locCap) {
                this.ils.getRwy().removeFromArray(this);
            }
            gsCap = false;
            locCap = false;
            resetApchSpdSet();
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

    public Queue<Image> getTrailDots() {
        return trailDots;
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

    public static void setLoadedIcons(boolean loadedIcons) {
        LOADED_ICONS = loadedIcons;
    }
}