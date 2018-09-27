package com.bombbird.atcsim.entities.aircrafts;

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
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Runway;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;
import com.bombbird.atcsim.utilities.MathTools;

import static com.bombbird.atcsim.screens.GameScreen.*;

public class Aircraft extends Actor {
    //Rendering parameters
    private Label label;
    private Label.LabelStyle labelStyle;
    public String[] labelText;
    private boolean selected;
    private static boolean loadedIcons = false;
    public static TextureAtlas iconAtlas = new TextureAtlas(Gdx.files.internal("game/aircrafts/aircraftIcons.atlas"));
    public static Skin skin = new Skin();
    private ImageButton icon;
    private ImageButton clickSpot;
    private ImageButton background;
    private ImageButton background2;
    public static ImageButton.ImageButtonStyle buttonStyleCtrl;
    private static ImageButton.ImageButtonStyle buttonStyleDept;
    private static ImageButton.ImageButtonStyle buttonStyleUnctrl;
    private static ImageButton.ImageButtonStyle buttonStyleEnroute;
    private static ImageButton.ImageButtonStyle buttonStyleBackground;
    private static ImageButton.ImageButtonStyle buttonStyleBackgroundSmall;
    private boolean dragging;

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
    private int maxClimb;
    private int typDes;
    private int maxDes;
    private int apchSpd;
    private int controlState;

    //Aircraft position
    private float x;
    private float y;
    private String latMode;
    private double heading;
    private double targetHeading;
    private int clearedHeading;
    private double angularVelocity;
    private double track;
    private Waypoint direct;

    //Altitude
    private float altitude;
    private int clearedAltitude;
    private int targetAltitude;
    private float verticalSpeed;
    private boolean expedite;
    private String altMode;

    //Speed
    private float ias;
    private float tas;
    private float gs;
    private Vector2 deltaPosition;
    private int clearedIas;
    private int targetIas;
    private float deltaIas;
    private String spdMode;

    Aircraft(String callsign, String icaoType, Airport airport) {
        if (!isLoadedIcons()) {
            skin.addRegions(iconAtlas);
            buttonStyleCtrl = new ImageButton.ImageButtonStyle();
            buttonStyleCtrl.imageUp = skin.getDrawable("aircraftControlled");
            buttonStyleCtrl.imageDown = skin.getDrawable("aircraftControlled");
            buttonStyleDept = new ImageButton.ImageButtonStyle();
            buttonStyleDept.imageUp = skin.getDrawable("aircraftDeparture");
            buttonStyleDept.imageDown = skin.getDrawable("aircraftDeparture");
            buttonStyleUnctrl = new ImageButton.ImageButtonStyle();
            buttonStyleUnctrl.imageUp = skin.getDrawable("aircraftNotControlled");
            buttonStyleUnctrl.imageDown = skin.getDrawable("aircraftNotControlled");
            buttonStyleEnroute = new ImageButton.ImageButtonStyle();
            buttonStyleEnroute.imageUp = skin.getDrawable("aircraftEnroute");
            buttonStyleEnroute.imageDown = skin.getDrawable("aircraftEnroute");
            buttonStyleBackground = new ImageButton.ImageButtonStyle();
            buttonStyleBackground.imageUp = skin.getDrawable("labelBackground");
            buttonStyleBackground.imageDown = skin.getDrawable("labelBackground");
            buttonStyleBackgroundSmall = new ImageButton.ImageButtonStyle();
            buttonStyleBackgroundSmall.imageUp = skin.getDrawable("labelBackgroundSmall");
            buttonStyleBackgroundSmall.imageDown = skin.getDrawable("labelBackgroundSmall");
            loadedIcons = true;
        }
        this.callsign = callsign;
        stage.addActor(this);
        this.icaoType = icaoType;
        int[] perfData = AircraftType.getAircraftInfo(icaoType);
        if (perfData == null) {
            //If aircraft type not found in file
            Gdx.app.log("Aircraft not found", icaoType + " not found in game/aircrafts/aircrafts.air");
        }
        if (perfData[0] == 0) {
            wakeCat = 'M';
        } else if (perfData[0] == 1) {
            wakeCat = 'H';
        } else if (perfData[0] == 2) {
            wakeCat = 'J';
        } else {
            Gdx.app.log("Invalid wake category", "Invalid wake turbulence category set for " + callsign + ", " + icaoType + "!");
        }
        float loadFactor = MathUtils.random(-1 , 1) / 20f;
        v2 = (int)(perfData[1] * (1 + loadFactor));
        maxClimb = (int)(perfData[2] * (1 - loadFactor));
        typDes = (int)(perfData[3] * (1 - loadFactor));
        maxDes = (int)(perfData[4] * (1 - loadFactor));
        apchSpd = (int)(perfData[5] * (1 + loadFactor));
        this.airport = airport;
        latMode = "vector";
        heading = 0;
        targetHeading = 0;
        clearedHeading = (int)(heading);
        track = 0;
        altitude = 10000;
        clearedAltitude = 10000;
        targetAltitude = 10000;
        verticalSpeed = 0;
        expedite = false;
        altMode = "open";
        ias = 250;
        tas = MathTools.iasToTas(ias, altitude);
        gs = tas;
        deltaPosition = new Vector2();
        clearedIas = 250;
        targetIas = 250;
        deltaIas = 0;
        spdMode = "open";
        tkofLdg = false;

        selected = false;
        dragging = false;
    }

    public static boolean isLoadedIcons() {
        return loadedIcons;
    }

    public static void setLoadedIcons(boolean loadedIcons) {
        Aircraft.loadedIcons = loadedIcons;
    }

    public void loadLabel() {
        icon = new ImageButton(buttonStyleUnctrl);
        icon.setSize(20, 20);
        icon.getImageCell().size(20, 20);
        stage.addActor(icon);

        background = new ImageButton(buttonStyleBackground);
        background.setPosition(x - 100, y + 25);
        background.setSize(300, 120);
        GameScreen.stage.addActor(background);

        background2 = new ImageButton(buttonStyleBackgroundSmall);
        background2.setPosition(x - 50, y + 25);
        background2.setSize(130, 95);
        GameScreen.stage.addActor(background2);

        labelText = new String[10];
        labelText[9] = airport.getIcao();
        labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont6;
        labelStyle.fontColor = Color.WHITE;
        label = new Label("Loading...", labelStyle);
        label.setPosition(x - 100, y + 25);
        label.setSize(300, 120);
        GameScreen.stage.addActor(label);

        clickSpot = new ImageButton(buttonStyleUnctrl);
        clickSpot.setColor(0, 0, 0, 0);
        clickSpot.setName(callsign);
        clickSpot.setPosition(x - 100, y + 25);
        clickSpot.setSize(300, 120);
        clickSpot.addListener(new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (controlState == 1 || controlState == 2) {
                    label.moveBy(x - clickSpot.getWidth() / 2, y - clickSpot.getHeight() / 2);
                } else {
                    label.moveBy(x - 65, y - 47.5f);
                }
                dragging = true;
                event.handle();
            }
        });
        clickSpot.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!dragging) {
                    RadarScreen.setSelectedAircraft(RadarScreen.aircrafts.get(actor.getName()));
                } else {
                    dragging = false;
                }
            }
        });
        GameScreen.stage.addActor(clickSpot);
    }

    public void renderShape() {
        if (direct != null && selected) {
            drawSidStar();
        }
        moderateLabel();
        shapeRenderer.setColor(Color.WHITE);
        if (controlState == 1 || controlState == 2) {
            GameScreen.shapeRenderer.line(background.getX() + background.getWidth() / 2, background.getY() + background.getHeight() / 2, x, y);
        } else {
            GameScreen.shapeRenderer.line(background2.getX() + background2.getWidth() / 2, background2.getY() + background2.getHeight() / 2, x, y);
        }
        if (controlState == 1 || controlState == 2) {
            shapeRenderer.setColor(Color.GREEN);
            GameScreen.shapeRenderer.line(x, y, x + gs * MathUtils.cosDeg((float)(90 - track)), y + gs * MathUtils.sinDeg((float)(90 - track)));
        }
    }

    double update() {
        tas = MathTools.iasToTas(ias, altitude);
        if (direct != null) {
            direct.setSelected(true);
        }
        updateIas();
        if (tkofLdg) {
            updateTkofLdg();
        }
        if (!onGround) {
            double[] info = updateTargetHeading();
            targetHeading = info[0];
            updateHeading(targetHeading, 0);
            updatePosition(info[1]);
            updateAltitude();
            return targetHeading;
        } else {
            tas = MathTools.iasToTas(ias, altitude);
            gs = tas - airport.getWinds()[1] * MathUtils.cosDeg(airport.getWinds()[0] - runway.getHeading());
            updatePosition(0);
            return 0;
        }
    }

    void updateTkofLdg() {

    }

    private void updateIas() {
        float targetdeltaIas = (getTargetIas() - getIas()) / 5;
        if (targetdeltaIas > deltaIas + 0.05) {
            deltaIas += 0.2f * Gdx.graphics.getDeltaTime();
        } else if (targetdeltaIas < deltaIas - 0.05) {
            deltaIas -= 0.2f * Gdx.graphics.getDeltaTime();
        } else {
            deltaIas = targetdeltaIas;
        }
        float max = 1.5f;
        float min = -2.25f;
        if (isTkofLdg()) {
            max = 3;
            min = -4.5f;
        }
        if (deltaIas > max) {
            deltaIas = max;
        } else if (deltaIas< min) {
            deltaIas = min;
        }
        ias = ias + deltaIas * Gdx.graphics.getDeltaTime();
        if (Math.abs(targetIas - ias) < 1) {
            ias = targetIas;
        }
    }

    private void updateAltitude() {
        float targetVertSpd = (targetAltitude - altitude) / 0.1f;
        if (targetVertSpd > verticalSpeed + 100) {
            verticalSpeed = verticalSpeed + 500 * Gdx.graphics.getDeltaTime();
        } else if (targetVertSpd < verticalSpeed - 100) {
            verticalSpeed = verticalSpeed - 500 * Gdx.graphics.getDeltaTime();
        }
        if (verticalSpeed > maxClimb) {
            verticalSpeed = maxClimb;
        } else if (!expedite && verticalSpeed < -typDes) {
            verticalSpeed = -typDes;
        } else if (expedite && verticalSpeed < -maxDes) {
            verticalSpeed = -maxDes;
        }
        altitude = altitude + verticalSpeed / 60 * Gdx.graphics.getDeltaTime();
        if (Math.abs(targetAltitude - altitude) < 50) {
            altitude = targetAltitude;
            verticalSpeed = 0;
        }
    }

    private double[] updateTargetHeading() {
        getDeltaPosition().setZero();
        double targetHeading;
        double angleDiff;

        //Get wind data
        int[] winds;
        if (altitude - airport.getElevation() <= 4000) {
            winds = airport.getWinds();
        } else {
            winds = RadarScreen.airports.get(RadarScreen.mainName).getWinds();
        }
        float windHdg = winds[0] + 180;
        int windSpd = winds[1];

        if (latMode.equals("vector")) {
            targetHeading = clearedHeading;
            double angle = 180 - windHdg + heading;
            gs = (float) Math.sqrt(Math.pow(tas, 2) + Math.pow(windSpd, 2) - 2 * tas * windSpd * MathUtils.cosDeg((float)angle));
            angleDiff = Math.asin(windSpd * MathUtils.sinDeg((float)angle) / gs) * MathUtils.radiansToDegrees;
        } else {
            //Calculates distance between waypoint and plane
            float deltaX = direct.getPosX() - x;
            float deltaY = direct.getPosY() - y;

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
            targetHeading += RadarScreen.magHdgDev;

            //If within __px of waypoint, target next waypoint
            //Distance determined by angle that needs to be turned
            double distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
            double requiredDistance = Math.abs(findDeltaHeading(findNextTargetHdg(), 0)) / 1.5 + 15;
            if (distance <= requiredDistance) {
                updateDirect();
            }
        }

        if (targetHeading > 360) {
            targetHeading -= 360;
        } else if (targetHeading <= 0) {
            targetHeading += 360;
        }

        return new double[] {targetHeading, angleDiff};
    }

    double findNextTargetHdg() {
        return 0;
    }

    private void updatePosition(double angleDiff) {
        //Angle diff is angle correction due to winds
        track = heading - RadarScreen.magHdgDev + angleDiff;
        deltaPosition.x = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * MathUtils.cosDeg((float)(90 - track));
        deltaPosition.y = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * MathUtils.sinDeg((float)(90 - track));
        x += deltaPosition.x;
        y += deltaPosition.y;
        label.moveBy(deltaPosition.x, deltaPosition.y);
    }

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

    private void updateHeading(double targetHeading, int forceDirection) {
        double deltaHeading = findDeltaHeading(targetHeading, forceDirection);
        //Note: angular velocities unit is change in heading per second
        double targetAngularVelocity = 0;
        if (deltaHeading > 0) {
            //Aircraft needs to turn right
            targetAngularVelocity = 2.5;
        } else if (deltaHeading < 0) {
            //Aircraft needs to turn left
            targetAngularVelocity = -2.5;
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
        icon.setPosition(x - 10, y - 10);
        icon.setColor(Color.BLACK); //Icon doesn't draw without this for some reason
        icon.draw(batch, 1);
    }

    void drawSidStar() {

    }

    void updateDirect() {

    }

    void setControlState(int controlState) {
        this.controlState = controlState;
        if (controlState == -1) { //En route aircraft - gray
            icon.setStyle(buttonStyleEnroute);
        } else if (controlState == 0) { //Uncontrolled aircraft - yellow
            icon.setStyle(buttonStyleUnctrl);
        } else if (controlState == 1) { //Controlled arrival - blue
            icon.setStyle(buttonStyleCtrl);
        } else if (controlState == 2) { //Controlled departure - light green?
            icon.setStyle(buttonStyleDept);
        } else {
            Gdx.app.log("Aircraft control state error", "Invalid control state " + controlState + " set!");
        }
        if (selected) {
            if (controlState == -1 || controlState == 0) {
                RadarScreen.ui.setNormalPane(true);
                RadarScreen.ui.setSelectedPane(false);
            } else {
                RadarScreen.ui.setNormalPane(false);
                RadarScreen.ui.setSelectedPane(true);
            }
        }
    }

    private void moderateLabel() {
        if (label.getX() < 0) {
            label.setX(0);
        } else if (label.getX() + label.getWidth() > 5760) {
            label.setX(5760 - label.getWidth());
        }
        if (label.getY() < 0) {
            label.setY(0);
        } else if (label.getY() + label.getHeight() > 3240) {
            label.setY(3240 - label.getHeight());
        }
    }

    public void updateLabel() {
        String vertSpd;
        if (getVerticalSpeed() < -100) {
            vertSpd = " DOWN ";
        } else if (getVerticalSpeed() > 100) {
            vertSpd = " UP ";
        } else {
            vertSpd = " = ";
        }
        labelText[0] = callsign;
        labelText[1] = icaoType + "/" + wakeCat;
        labelText[2] = Integer.toString((int)(altitude / 100));
        labelText[3] = Integer.toString(clearedAltitude / 100);
        if ((int) heading == 0) {
            heading += 360;
        }
        labelText[4] = Integer.toString((int) heading);
        if (latMode.equals("vector")) {
            labelText[5] = Integer.toString(clearedHeading);
        } else {
            labelText[5] = direct.getName();
        }
        labelText[6] = Integer.toString((int) gs);
        labelText[7] = Integer.toString(getClearedIas());
        String updatedText;
        if (getControlState() == 1 || getControlState() == 2) {
            updatedText = labelText[0] + " " + labelText[1] + "\n" + labelText[2] + vertSpd + labelText[3] + "\n" + labelText[4] + " " + labelText[5] + " " + labelText[8] + "\n" + labelText[6] + " " + labelText[7] + " " + labelText[9];
            label.setSize(300, 120);
            background.setVisible(true);
            background2.setVisible(false);
        } else {
            updatedText = labelText[0] + "\n" + labelText[2] + " " + labelText[4] + "\n" + labelText[6];
            label.setSize(130, 95);
            background.setVisible(false);
            background2.setVisible(true);
        }
        label.setText(updatedText);
        background.setPosition(label.getX() - 5, label.getY());
        background2.setPosition(label.getX() - 5, label.getY());
        clickSpot.setPosition(label.getX() - 5, label.getY());
    }

    public void removeSelectedWaypoints() {

    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setTargetIas(int ias) {
        targetIas = ias;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Label.LabelStyle getLabelStyle() {
        return labelStyle;
    }

    public void setLabelStyle(Label.LabelStyle labelStyle) {
        this.labelStyle = labelStyle;
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

    public ImageButton getBackground() {
        return background;
    }

    public void setBackground(ImageButton background) {
        this.background = background;
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

    public int getMaxClimb() {
        return maxClimb;
    }

    public void setMaxClimb(int maxClimb) {
        this.maxClimb = maxClimb;
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

    public String getLatMode() {
        return latMode;
    }

    public void setLatMode(String latMode) {
        this.latMode = latMode;
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
    }

    public int getTargetAltitude() {
        return targetAltitude;
    }

    public void setTargetAltitude(int targetAltitude) {
        this.targetAltitude = targetAltitude;
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

    public String getAltMode() {
        return altMode;
    }

    public void setAltMode(String altMode) {
        this.altMode = altMode;
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

    public int getTargetIas() {
        return targetIas;
    }

    public float getDeltaIas() {
        return deltaIas;
    }

    public void setDeltaIas(float deltaIas) {
        this.deltaIas = deltaIas;
    }

    public String getSpdMode() {
        return spdMode;
    }

    public void setSpdMode(String spdMode) {
        this.spdMode = spdMode;
    }
}