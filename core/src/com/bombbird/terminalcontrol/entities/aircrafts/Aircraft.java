package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Array;
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
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.MathTools;

import static com.bombbird.terminalcontrol.screens.GameScreen.*;

public class Aircraft extends Actor {
    //Rendering parameters
    public static TextureAtlas ICON_ATLAS = new TextureAtlas(Gdx.files.internal("game/aircrafts/aircraftIcons.atlas"));
    public static Skin SKIN = new Skin(ICON_ATLAS);
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_CTRL = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_DEPT = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_UNCTRL = new ImageButton.ImageButtonStyle();
    private static final ImageButton.ImageButtonStyle BUTTON_STYLE_ENROUTE = new ImageButton.ImageButtonStyle();
    private static boolean LOADED_ICONS = false;

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
    private boolean tkofLdg;

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
    private boolean conflict;

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
        if (!LOADED_ICONS) {
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
        this.callsign = callsign;
        STAGE.addActor(this);
        this.icaoType = icaoType;
        wakeCat = AircraftType.getWakeCat(icaoType);
        float loadFactor = MathUtils.random(-1 , 1) / 20f;
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
        tkofLdg = false;
        gsCap = false;
        locCap = false;
        climbSpd = MathUtils.random(270, 290);
        goAround = false;
        conflict = false;

        selected = false;
        dragging = false;
    }

    public void initRadarPos() {
        radarX = x;
        radarY = y;
        radarHdg = heading;
        radarTrack = track;
        radarGs = gs;
        radarAlt = altitude;
        radarVs = verticalSpeed;
    }

    public void loadLabel() {
        icon = new ImageButton(BUTTON_STYLE_UNCTRL);
        icon.setSize(20, 20);
        icon.getImageCell().size(20, 20);
        STAGE.addActor(icon);

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
                    RadarScreen.setSelectedAircraft(RadarScreen.AIRCRAFTS.get(actor.getName()));
                } else {
                    dragging = false;
                }
            }
        });
        clickSpot.setDebug(true);

        STAGE.addActor(labelButton);
        STAGE.addActor(label);
        STAGE.addActor(clickSpot);
    }

    /** Renders shapes using shapeRenderer; all rendering should be called here */
    public void renderShape() {
        drawLatLines();
        moderateLabel();
        SHAPE_RENDERER.setColor(Color.WHITE);
        SHAPE_RENDERER.line(label.getX() + label.getWidth() / 2, label.getY() + label.getHeight() / 2, radarX, radarY);
        if (controlState == 1 || controlState == 2) {
            SHAPE_RENDERER.setColor(color);
            SHAPE_RENDERER.line(radarX, radarY, radarX + radarGs * MathUtils.cosDeg((float)(90 - radarTrack)), radarY + radarGs * MathUtils.sinDeg((float)(90 - radarTrack)));
        }
        clickSpot.drawDebug(SHAPE_RENDERER);
    }

    /** Draws the lines displaying the lateral status of aircraft */
    private void drawLatLines() {
        if (selected) {
            //Draws cleared status
            if (navState.getDispLatMode().last().contains("arrival") || navState.getDispLatMode().first().contains("departure") && direct != null) {
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
                if ((LatTab.latMode.contains("arrival") || LatTab.latMode.contains("departure")) && (UI.latTab.isWptChanged() || UI.latTab.isLatModeChanged())) {
                    uiDrawSidStar();
                } else if (LatTab.latMode.equals("After waypoint, fly heading") && (UI.latTab.isAfterWptChanged() || UI.latTab.isAfterWptHdgChanged() || UI.latTab.isLatModeChanged())) {
                    uiDrawAftWpt();
                } else if (LatTab.latMode.contains("heading") && (LatTab.clearedILS.equals("Not cleared approach") || !locCap) && (UI.latTab.isHdgChanged() || UI.latTab.isLatModeChanged())) {
                    uiDrawHdgLine();
                } else if (LatTab.latMode.equals("Hold at") && (UI.latTab.isLatModeChanged() || UI.latTab.isHoldWptChanged())) {
                    uiDrawHoldPattern();
                }
            }
        }
    }

    public void drawSidStar() {
        SHAPE_RENDERER.setColor(Color.WHITE);
        SHAPE_RENDERER.line(radarX, radarY, navState.getClearedDirect().last().getPosX(), navState.getClearedDirect().last().getPosY());
    }

    public void uiDrawSidStar() {
        SHAPE_RENDERER.setColor(Color.YELLOW);
        SHAPE_RENDERER.line(radarX, radarY, RadarScreen.WAYPOINTS.get(LatTab.clearedWpt).getPosX(), RadarScreen.WAYPOINTS.get(LatTab.clearedWpt).getPosY());
    }

    public void drawAftWpt() {
        SHAPE_RENDERER.setColor(Color.WHITE);
        SHAPE_RENDERER.line(radarX, radarY, navState.getClearedDirect().last().getPosX(), navState.getClearedDirect().last().getPosY());
    }

    public void uiDrawAftWpt() {
        SHAPE_RENDERER.setColor(Color.YELLOW);
        SHAPE_RENDERER.line(radarX, radarY, RadarScreen.WAYPOINTS.get(LatTab.clearedWpt).getPosX(), RadarScreen.WAYPOINTS.get(LatTab.clearedWpt).getPosY());
    }

    private void drawHdgLine() {
        SHAPE_RENDERER.setColor(Color.WHITE);
        SHAPE_RENDERER.line(radarX, radarY, radarX + 6610 * MathUtils.cosDeg(90 - (navState.getClearedHdg().last() - RadarScreen.MAG_HDG_DEV)), radarY + 6610 * MathUtils.sinDeg(90 - (navState.getClearedHdg().last() - RadarScreen.MAG_HDG_DEV)));
    }

    private void uiDrawHdgLine() {
        SHAPE_RENDERER.setColor(Color.YELLOW);
        SHAPE_RENDERER.line(radarX, radarY, radarX + 6610 * MathUtils.cosDeg(90 - (LatTab.clearedHdg - RadarScreen.MAG_HDG_DEV)), radarY + 6610 * MathUtils.sinDeg(90 - (LatTab.clearedHdg - RadarScreen.MAG_HDG_DEV)));
    }

    public void drawHoldPattern() {
        SHAPE_RENDERER.setColor(Color.WHITE);
        if (navState.getClearedDirect().size > 0 && navState.getClearedDirect().last() != null) {
            SHAPE_RENDERER.line(radarX, radarY, navState.getClearedDirect().last().getPosX(), navState.getClearedDirect().last().getPosY());
        }
        ((Star) getSidStar()).getHoldProcedure().renderShape(navState.getClearedHold().last());
    }

    public void uiDrawHoldPattern() {
        SHAPE_RENDERER.setColor(Color.YELLOW);
        SHAPE_RENDERER.line(radarX, radarY, RadarScreen.WAYPOINTS.get(LatTab.clearedWpt).getPosX(), RadarScreen.WAYPOINTS.get(LatTab.clearedWpt).getPosY());
        ((Star) getSidStar()).getHoldProcedure().renderShape(RadarScreen.WAYPOINTS.get(LatTab.holdWpt));
    }

    /** The main update function, called during aircraft draw */
    public double update() {
        tas = MathTools.iasToTas(ias, altitude);
        updateIas();
        if (tkofLdg) {
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
                updateGoAround(); //TODO Implement standard MAP in future versions
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
        if (selected && (controlState == 1 || controlState == 2)) {
            updateUISelections();
            UI.updateState();
        }
        navState.getClearedSpd().removeFirst();
        navState.getClearedSpd().addFirst(clearedIas);
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
        if (tkofLdg) {
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
        if (!expedite && verticalSpeed > typClimb) {
            verticalSpeed = typClimb;
        } else if (!expedite && verticalSpeed < -typDes) {
            verticalSpeed = -typDes;
        } else if (expedite && verticalSpeed > maxClimb) {
            verticalSpeed = maxClimb;
        } else if (expedite && verticalSpeed < -maxDes) {
            verticalSpeed = -maxDes;
        }
        altitude += verticalSpeed / 60 * Gdx.graphics.getDeltaTime();
        if (Math.abs(targetAltitude - altitude) < 50) {
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
            RadarScreen.SEPARATION_CHECKER.updateAircraftPositions();
        }
        prevAlt = altitude;
    }

    /** Called to update the heading aircraft should turn towards to follow instructed lateral mode, returns the heading it should fly as well as the resulting difference in angle of the track due to winds */
    public double[] updateTargetHeading() {
        deltaPosition.setZero();
        double info[];
        double targetHeading;
        double angleDiff;

        //Get wind data
        int[] winds;
        if (altitude - airport.getElevation() <= 4000) {
            winds = airport.getWinds();
        } else {
            winds = RadarScreen.AIRPORTS.get(RadarScreen.MAIN_NAME).getWinds();
        }
        int windHdg = winds[0] + 180;
        int windSpd = winds[1];

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
            info = calculateVectorHdg(clearedHeading, windHdg, windSpd);
        } else if (sidstar && !holding) {
            info = calculatePointTargetHdg(new float[] {direct.getPosX(), direct.getPosY()}, windHdg, windSpd);

            //If within __px of waypoint, target next waypoint
            //Distance determined by angle that needs to be turned
            double distance = MathTools.distanceBetween(x, y, direct.getPosX(), direct.getPosY());
            double requiredDistance = Math.abs(findDeltaHeading(findNextTargetHdg())) / 1.75f + 10;
            if (distance <= requiredDistance) {
                updateDirect();
            }
        } else if (locCap) {
            clearedHeading = ils.getHeading();
            if (!getIls().getRwy().equals(runway)) {
                runway = getIls().getRwy();
            }
            if (getIls() instanceof LDA && MathTools.pixelToNm(MathTools.distanceBetween(x, y, runway.getX(), runway.getY())) <= ((LDA) getIls()).getLineUpDist()) {
                float deltaX = 100 * MathUtils.cosDeg(90 - runway.getTrueHdg());
                float deltaY = 100 * MathUtils.sinDeg(90 - runway.getTrueHdg());
                info = calculatePointTargetHdg(deltaX, deltaY, windHdg, windSpd);
            } else {
                //Calculates x, y of point 0.75nm ahead of plane
                Vector2 position = this.getIls().getPointAhead(this);
                info = calculatePointTargetHdg(new float[] {position.x, position.y}, windHdg, windSpd);
            }
        } else if (holding) {
            if (!navState.getDispLatMode().first().equals("Hold at")) {
                holding = false;
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
                    info = calculateVectorHdg(star.getHoldProcedure().getInboundHdgAtWpt(holdWpt) + 180, windHdg, windSpd);
                    if (MathTools.pixelToNm(MathTools.distanceBetween(x, y, holdWpt.getPosX(), holdWpt.getPosY())) >= star.getHoldProcedure().getLegDistAtWpt(holdWpt) || type1leg) {
                        //Once it has flown leg dist, turn back towards entry fix
                        type1leg = true;
                        info = calculatePointTargetHdg(new float[] {holdWpt.getPosX(), holdWpt.getPosY()}, windHdg, windSpd);
                        //Once it reaches entry fix, init has ended
                        if (MathTools.distanceBetween(x, y, holdWpt.getPosX(), holdWpt.getPosY()) <= 10) {
                            init = true;
                            holdTargetPtSelected[1] = true;
                        }
                    }
                } else {
                    //Apparently no difference for types 2 and 3 in this case - fly straight towards opp waypoint with direction same as hold direction
                    info = calculatePointTargetHdg(holdTargetPt[1], windHdg, windSpd);
                    holdTargetPtSelected[1] = true;
                    double deltaHdg = findDeltaHeading(star.getHoldProcedure().getInboundHdgAtWpt(holdWpt) + 180);
                    boolean left = star.getHoldProcedure().isLeftAtWpt(holdWpt);
                    if (left && deltaHdg > -150 || !left && deltaHdg < 150) {
                        //Set init to true once aircraft has turned to a heading of not more than 150 deg offset from target, in the turn direction
                        init = true;
                    }
                }
            } else {
                float track = star.getHoldProcedure().getInboundHdgAtWpt(holdWpt) - RadarScreen.MAG_HDG_DEV;
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
                info = calculatePointTargetHdg(new float[] {target[0] + distance * MathUtils.cosDeg(270 - track), target[1] + distance * MathUtils.sinDeg(270 - track)}, windHdg, windSpd);
            }
        } else {
            info = new double[] {0, 0};
            Gdx.app.log("Update target heading", "Oops, something went wrong");
        }

        targetHeading = info[0];
        angleDiff = info[1];

        if (targetHeading > 360) {
            targetHeading -= 360;
        } else if (targetHeading <= 0) {
            targetHeading += 360;
        }

        return new double[] {targetHeading, angleDiff};
    }

    private double[] calculateVectorHdg(int heading, int windHdg, int windSpd) {
        double angleDiff;

        targetHeading = heading;
        double angle = 180 - windHdg + heading;
        gs = (float) Math.sqrt(Math.pow(tas, 2) + Math.pow(windSpd, 2) - 2 * tas * windSpd * MathUtils.cosDeg((float)angle));
        angleDiff = Math.asin(windSpd * MathUtils.sinDeg((float)angle) / gs) * MathUtils.radiansToDegrees;

        return new double[] {targetHeading, angleDiff};
    }

    private double[] calculatePointTargetHdg(float[] position, int windHdg, int windSpd) {
        return calculatePointTargetHdg(position[0] - x, position[1] - y, windHdg, windSpd);
    }

    private double[] calculatePointTargetHdg(float deltaX, float deltaY, int windHdg, int windSpd) {
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
        targetHeading -= angleDiff;

        //Aaaand now the cosine rule to determine ground speed
        gs = (float) Math.sqrt(Math.pow(tas, 2) + Math.pow(windSpd, 2) - 2 * tas * windSpd * MathUtils.cosDeg((float)(180 - angle - angleDiff)));

        //Add magnetic deviation to give magnetic heading
        targetHeading += RadarScreen.MAG_HDG_DEV;

        return new double[] {targetHeading, angleDiff};
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
        //Angle diff is angle correction due to winds
        track = heading - RadarScreen.MAG_HDG_DEV + angleDiff;
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
            if (selected) {
                UI.updateState();
            }
        }
        if (x < 1260 || x > 4500 || y < 0 || y > 3240) {
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
        return findDeltaHeading(targetHeading, forceDirection);
    }

    /** Finds the deltaHeading given a forced direction */
    private double findDeltaHeading(double targetHeading, int forceDirection) {
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
            UI.updateState();
        }
    }

    /*
    /** Overriden method that sets next aircraft action after reaching direct
    public void updateGoAroundDirect() {
        //No default implementation
    }
    */

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
            UI.updateState();
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
                RadarScreen.UI.setNormalPane(true);
                RadarScreen.UI.setSelectedPane(null);
            } else {
                RadarScreen.UI.setNormalPane(false);
                RadarScreen.UI.setSelectedPane(this);
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
        if (radarVs < -100) {
            vertSpd = " DOWN ";
        } else if (radarVs > 100) {
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
        } else if (navState.getClearedHold().last() != null && navState.getClearedHold().last().equals(holdWpt)) {
            labelText[5] = holdWpt.getName();
        } else if (navState.getDispLatMode().last().contains(getSidStar().getName()) || navState.getDispLatMode().last().equals("After waypoint, fly heading")) {
            if (navState.getClearedDirect().last().equals(navState.getClearedAftWpt().last()) && navState.getDispLatMode().last().equals("After waypoint, fly heading")) {
                labelText[5] = navState.getClearedDirect().last().getName() + Integer.toString(navState.getClearedAftWptHdg().last());
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
        UI.latTab.getSettingsBox().setSelected(navState.getDispLatMode().last());
        LatTab.clearedHdg = clearedHeading;
        if (direct != null) {
            UI.latTab.getValueBox().setSelected(direct.getName());
        }

        if (this instanceof Departure && Integer.parseInt(UI.altTab.getValueBox().getSelected()) < lowestAlt) {
            UI.altTab.getValueBox().setSelected(Integer.toString(lowestAlt));
        }

        UI.spdTab.getValueBox().setSelected(Integer.toString(clearedIas));
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
            return getSidStar().getRemainingWaypoints(sidStarIndex, getSidStar().getWaypoints().size - 1);
        } else if ("After waypoint, fly heading".equals(navState.getDispLatMode().last())) {
            return getSidStar().getRemainingWaypoints(sidStarIndex, getSidStar().findWptIndex(navState.getClearedAftWpt().last().getName()));
        } else if ("Hold at".equals(navState.getDispLatMode().last())) {
            return getSidStar().getRemainingWaypoints(sidStarIndex, getSidStar().findWptIndex(navState.getClearedHold().last().getName()));
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

    public boolean isTkofLdg() {
        return tkofLdg;
    }

    public void setTkofLdg(boolean tkofLdg) {
        this.tkofLdg = tkofLdg;
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
        if (direct != null) {
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
        }
    }

    /** Removes the aircraft completely from game, including its labels, other elements */
    public void removeAircraft() {
        label.remove();
        icon.remove();
        labelButton.remove();
        clickSpot.remove();
        remove();
        RadarScreen.AIRCRAFTS.remove(callsign);
    }

    /** Overriden method that sets the altitude restrictions of the aircraft */
    public void updateAltRestrictions() {
        //No default implementation
    }

    /** Overriden method that resets the booleans in arrival checking whether the appropriate speeds during approach have been set */
    public void resetApchSpdSet() {
        //No default implementation
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

    public void setDeltaPosition(Vector2 deltaPosition) {
        this.deltaPosition = deltaPosition;
    }

    public int getClearedIas() {
        return clearedIas;
    }

    public void setClearedIas(int clearedIas) {
        this.clearedIas = clearedIas;
    }

    public float getDeltaIas() {
        return deltaIas;
    }

    public void setDeltaIas(float deltaIas) {
        this.deltaIas = deltaIas;
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

    public void setMaxClimb(int maxClimb) {
        this.maxClimb = maxClimb;
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

    public void setClimbSpd(int climbSpd) {
        this.climbSpd = climbSpd;
    }

    public int getMaxWptSpd(String wpt) {
        return getSidStar().getWptMaxSpd(wpt);
    }

    public ILS getIls() {
        return ils;
    }

    public void setIls(ILS ils) {
        if (ils == null && locCap) {
            getIls().getRwy().removeFromArray(this);
        }
        if (this.ils != ils) {
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
}