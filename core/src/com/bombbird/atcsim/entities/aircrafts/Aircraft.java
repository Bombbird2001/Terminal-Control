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
    private static ImageButton.ImageButtonStyle buttonStyleCtrl;
    private static ImageButton.ImageButtonStyle buttonStyleDept;
    private static ImageButton.ImageButtonStyle buttonStyleUnctrl;
    private static ImageButton.ImageButtonStyle buttonStyleEnroute;
    private Image background;

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
            setButtonStyleCtrl(new ImageButton.ImageButtonStyle());
            getButtonStyleCtrl().imageUp = skin.getDrawable("aircraftControlled");
            getButtonStyleCtrl().imageDown = skin.getDrawable("aircraftControlled");
            setButtonStyleDept(new ImageButton.ImageButtonStyle());
            getButtonStyleDept().imageUp = skin.getDrawable("aircraftDeparture");
            getButtonStyleDept().imageDown = skin.getDrawable("aircraftDeparture");
            setButtonStyleUnctrl(new ImageButton.ImageButtonStyle());
            getButtonStyleUnctrl().imageUp = skin.getDrawable("aircraftNotControlled");
            getButtonStyleUnctrl().imageDown = skin.getDrawable("aircraftNotControlled");
            setButtonStyleEnroute(new ImageButton.ImageButtonStyle());
            getButtonStyleEnroute().imageUp = skin.getDrawable("aircraftEnroute");
            getButtonStyleEnroute().imageDown = skin.getDrawable("aircraftEnroute");
            setLoadedIcons(true);
        }
        this.setCallsign(callsign);
        stage.addActor(this);
        this.setIcaoType(icaoType);
        int[] perfData = AircraftType.getAircraftInfo(icaoType);
        if (perfData == null) {
            //If aircraft type not found in file
            Gdx.app.log("Aircraft not found", icaoType + " not found in game/aircrafts/aircrafts.air");
        }
        if (perfData[0] == 0) {
            setWakeCat('M');
        } else if (perfData[0] == 1) {
            setWakeCat('H');
        } else if (perfData[0] == 2) {
            setWakeCat('J');
        } else {
            Gdx.app.log("Invalid wake category", "Invalid wake turbulence category set for " + callsign + ", " + icaoType + "!");
        }
        float loadFactor = MathUtils.random(-1 , 1) / 20f;
        setV2((int)(perfData[1] * (1 + loadFactor)));
        setMaxClimb((int)(perfData[2] * (1 - loadFactor)));
        setTypDes((int)(perfData[3] * (1 - loadFactor)));
        setMaxDes((int)(perfData[4] * (1 - loadFactor)));
        setApchSpd((int)(perfData[5] * (1 + loadFactor)));
        this.setAirport(airport);
        setLatMode("vector");
        setHeading(0);
        setTargetHeading(0);
        setClearedHeading((int) getHeading());
        setTrack(0);
        setAltitude(10000);
        setClearedAltitude(10000);
        setTargetAltitude(10000);
        setVerticalSpeed(0);
        setExpedite(false);
        setAltMode("open");
        setIas(250);
        setTas(MathTools.iasToTas(getIas(), getAltitude()));
        setGs(getTas());
        setDeltaPosition(new Vector2());
        setClearedIas(250);
        setTargetIas(250);
        setDeltaIas(0);
        setSpdMode("open");
        setTkofLdg(false);

        setSelected(false);
        loadLabel();
    }

    public static boolean isLoadedIcons() {
        return loadedIcons;
    }

    public static void setLoadedIcons(boolean loadedIcons) {
        Aircraft.loadedIcons = loadedIcons;
    }

    public static ImageButton.ImageButtonStyle getButtonStyleCtrl() {
        return buttonStyleCtrl;
    }

    public static void setButtonStyleCtrl(ImageButton.ImageButtonStyle buttonStyleCtrl) {
        Aircraft.buttonStyleCtrl = buttonStyleCtrl;
    }

    public static ImageButton.ImageButtonStyle getButtonStyleDept() {
        return buttonStyleDept;
    }

    public static void setButtonStyleDept(ImageButton.ImageButtonStyle buttonStyleDept) {
        Aircraft.buttonStyleDept = buttonStyleDept;
    }

    public static ImageButton.ImageButtonStyle getButtonStyleUnctrl() {
        return buttonStyleUnctrl;
    }

    public static void setButtonStyleUnctrl(ImageButton.ImageButtonStyle buttonStyleUnctrl) {
        Aircraft.buttonStyleUnctrl = buttonStyleUnctrl;
    }

    public static ImageButton.ImageButtonStyle getButtonStyleEnroute() {
        return buttonStyleEnroute;
    }

    public static void setButtonStyleEnroute(ImageButton.ImageButtonStyle buttonStyleEnroute) {
        Aircraft.buttonStyleEnroute = buttonStyleEnroute;
    }

    private void loadLabel() {
        setIcon(new ImageButton(getButtonStyleUnctrl()));
        getIcon().setSize(40, 40);
        getIcon().getImageCell().size(40, 40);
        getIcon().addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                aircrafts.get(actor.getName()).setSelected(true);
                event.handle();
                System.out.println("Aircraft clicked/tapped");
            }
        });
        stage.addActor(getIcon());

        setBackground(new Image());
        getBackground().setDrawable(skin.getDrawable("labelBackground"));
        getBackground().setPosition(getX() - 100, getY() + 25);
        getBackground().setSize(290, 120);
        GameScreen.stage.addActor(getBackground());

        getIcon().setName(getCallsign());
        setLabelText(new String[10]);
        getLabelText()[9] = "";
        setLabelStyle(new Label.LabelStyle());
        getLabelStyle().font = AtcSim.fonts.defaultFont6;
        getLabelStyle().fontColor = Color.WHITE;
        setLabel(new Label("Loading...", getLabelStyle()));
        getLabel().setPosition(getX() - 100, getY() + 25);
        getLabel().setSize(290, 120);
        getLabel().addListener(new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                getLabel().moveBy(x - getLabel().getWidth() / 2, y - getLabel().getHeight() / 2);
                event.handle();
            }
        });
        GameScreen.stage.addActor(getLabel());
    }

    public void renderShape() {
        if (isSelected() && getDirect() != null) {
            drawSidStar();
        }
        moderateLabel();
        shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(getLabel().getX() + getLabel().getWidth() / 2, getLabel().getY() + getLabel().getHeight() / 2, getX(), getY());
        if (getControlState() == 1 || getControlState() == 2) {
            shapeRenderer.setColor(Color.GREEN);
            GameScreen.shapeRenderer.line(getX(), getY(), getX() + getGs() * MathUtils.cosDeg((float)(90 - getTrack())), getY() + getGs() * MathUtils.sinDeg((float)(90 - getTrack())));
        }
    }

    double update() {
        setTas(MathTools.iasToTas(getIas(), getAltitude()));
        if (getDirect() != null) {
            getDirect().setSelected(true);
        }
        updateIas();
        if (isTkofLdg()) {
            updateTkofLdg();
        }
        if (!isOnGround()) {
            double[] info = updateTargetHeading();
            setTargetHeading(info[0]);
            updateHeading(getTargetHeading(), 0);
            updatePosition(info[1]);
            updateAltitude();
            return getTargetHeading();
        } else {
            setTas(MathTools.iasToTas(getIas(), getAltitude()));
            setGs(getTas() - getAirport().getWinds()[1] * MathUtils.cosDeg(getAirport().getWinds()[0] - getRunway().getHeading()));
            updatePosition(0);
            return 0;
        }
    }

    void updateTkofLdg() {

    }

    private void updateIas() {
        float targetdeltaIas = (getTargetIas() - getIas()) / 5;
        if (targetdeltaIas > getDeltaIas() + 0.05) {
            setDeltaIas(getDeltaIas() + 0.2f * Gdx.graphics.getDeltaTime());
        } else if (targetdeltaIas < getDeltaIas() - 0.05) {
            setDeltaIas(getDeltaIas() - 0.2f * Gdx.graphics.getDeltaTime());
        } else {
            setDeltaIas(targetdeltaIas);
        }
        float max = 1.5f;
        float min = -2.25f;
        if (isTkofLdg()) {
            max = 3;
            min = -4.5f;
        }
        if (getDeltaIas() > max) {
            setDeltaIas(max);
        } else if (getDeltaIas() < min) {
            setDeltaIas(min);
        }
        setIas(getIas() + getDeltaIas() * Gdx.graphics.getDeltaTime());
        if (Math.abs(getTargetIas() - getIas()) < 1) {
            setIas(getTargetIas());
        }
    }

    private void updateAltitude() {
        float targetVertSpd = (getTargetAltitude() - getAltitude()) / 0.1f;
        if (targetVertSpd > getVerticalSpeed() + 100) {
            setVerticalSpeed(getVerticalSpeed() + 500 * Gdx.graphics.getDeltaTime());
        } else if (targetVertSpd < getVerticalSpeed() - 100) {
            setVerticalSpeed(getVerticalSpeed() - 500 * Gdx.graphics.getDeltaTime());
        }
        if (getVerticalSpeed() > getMaxClimb()) {
            setVerticalSpeed(getMaxClimb());
        } else if (!isExpedite() && getVerticalSpeed() < -getTypDes()) {
            setVerticalSpeed(-getTypDes());
        } else if (isExpedite() && getVerticalSpeed() < -getMaxDes()) {
            setVerticalSpeed(-getMaxDes());
        }
        setAltitude(getAltitude() + getVerticalSpeed() / 60 * Gdx.graphics.getDeltaTime());
        if (Math.abs(getTargetAltitude() - getAltitude()) < 50) {
            setAltitude(getTargetAltitude());
            setVerticalSpeed(0);
        }
    }

    private double[] updateTargetHeading() {
        getDeltaPosition().setZero();
        double targetHeading;
        double angleDiff;

        //Get wind data
        int[] winds;
        if (getAltitude() - getAirport().getElevation() <= 4000) {
            winds = getAirport().getWinds();
        } else {
            winds = RadarScreen.airports.get(RadarScreen.mainName).getWinds();
        }
        float windHdg = winds[0] + 180;
        int windSpd = winds[1];

        if (getLatMode().equals("vector")) {
            targetHeading = getClearedHeading();
            double angle = 180 - windHdg + getHeading();
            setGs((float) Math.sqrt(Math.pow(getTas(), 2) + Math.pow(windSpd, 2) - 2 * getTas() * windSpd * MathUtils.cosDeg((float)angle)));
            angleDiff = Math.asin(windSpd * MathUtils.sinDeg((float)angle) / getGs()) * MathUtils.radiansToDegrees;
        } else {
            //Calculates distance between waypoint and plane
            float deltaX = getDirect().getPosX() - getX();
            float deltaY = getDirect().getPosY() - getY();

            //Find target track angle
            if (deltaX >= 0) {
                targetHeading = 90 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            } else {
                targetHeading = 270 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            }

            //Calculate required aircraft heading to account for winds
            //Using sine rule to determine angle between aircraft velocity and actual velocity
            double angle = windHdg - targetHeading;
            angleDiff = Math.asin(windSpd * MathUtils.sinDeg((float)angle) / getTas()) * MathUtils.radiansToDegrees;
            targetHeading -= angleDiff;

            //Aaaand now the cosine rule to determine ground speed
            setGs((float) Math.sqrt(Math.pow(getTas(), 2) + Math.pow(windSpd, 2) - 2 * getTas() * windSpd * MathUtils.cosDeg((float)(180 - angle - angleDiff))));

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
        setTrack(getHeading() - RadarScreen.magHdgDev + angleDiff);
        getDeltaPosition().x = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(getGs()) / 3600 * MathUtils.cosDeg((float)(90 - getTrack()));
        getDeltaPosition().y = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(getGs()) / 3600 * MathUtils.sinDeg((float)(90 - getTrack()));
        setX(getX() + getDeltaPosition().x);
        setY(getY() + getDeltaPosition().y);
        getLabel().moveBy(getDeltaPosition().x, getDeltaPosition().y);
    }

    private double findDeltaHeading(double targetHeading, int forceDirection) {
        double deltaHeading = targetHeading - getHeading();
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
        if (targetAngularVelocity > getAngularVelocity() + 0.1f) {
            //If need to turn right, start turning right
            setAngularVelocity(getAngularVelocity() + 0.5f * Gdx.graphics.getDeltaTime());
        } else if (targetAngularVelocity < getAngularVelocity() - 0.1f) {
            //If need to turn left, start turning left
            setAngularVelocity(getAngularVelocity() - 0.5f * Gdx.graphics.getDeltaTime());
        } else {
            //If within +-0.1 of target, set equal to target
            setAngularVelocity(targetAngularVelocity);
        }

        //Add angular velocity to heading
        setHeading(getHeading() + getAngularVelocity() * Gdx.graphics.getDeltaTime());
        if (getHeading() > 360) {
            setHeading(getHeading() - 360);
        } else if (getHeading() <= 0) {
            setHeading(getHeading() + 360);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        update();
        updateLabel();
        getIcon().setPosition(getX() - 20, getY() - 20);
        getIcon().setColor(Color.BLACK); //Icon doesn't draw without this for some reason
        getIcon().draw(batch, 1);
    }

    void drawSidStar() {

    }

    void updateDirect() {

    }

    void setControlState(int controlState) {
        this.controlState = controlState;
        if (controlState == -1) { //En route aircraft - gray
            getIcon().setStyle(getButtonStyleEnroute());
        } else if (controlState == 0) { //Uncontrolled aircraft - yellow
            getIcon().setStyle(getButtonStyleUnctrl());
        } else if (controlState == 1) { //Controlled arrival - blue
            getIcon().setStyle(getButtonStyleCtrl());
        } else if (controlState == 2) { //Controlled departure - light green?
            getIcon().setStyle(getButtonStyleDept());
        } else {
            Gdx.app.log("Aircraft control state error", "Invalid control state " + controlState + " set!");
        }
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    private void moderateLabel() {
        if (getLabel().getX() < 0) {
            getLabel().setX(0);
        } else if (getLabel().getX() + getLabel().getWidth() > 5760) {
            getLabel().setX(5760 - getLabel().getWidth());
        }
        if (getLabel().getY() < 0) {
            getLabel().setY(0);
        } else if (getLabel().getY() + getLabel().getHeight() > 3240) {
            getLabel().setY(3240 - getLabel().getHeight());
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
        getLabelText()[0] = getCallsign();
        getLabelText()[1] = getIcaoType() + "/" + getWakeCat();
        getLabelText()[2] = Integer.toString((int)(getAltitude() / 100));
        getLabelText()[3] = Integer.toString(getClearedAltitude() / 100);
        if ((int) getHeading() == 0) {
            setHeading(getHeading() + 360);
        }
        getLabelText()[4] = Integer.toString((int) getHeading());
        if (getLatMode().equals("vector")) {
            getLabelText()[5] = Integer.toString(getClearedHeading());
        } else {
            getLabelText()[5] = getDirect().getName();
        }
        getLabelText()[6] = Integer.toString((int) getGs());
        getLabelText()[7] = Integer.toString(getClearedIas());
        String updatedText;
        if (getControlState() == 1 || getControlState() == 2) {
            updatedText = getLabelText()[0] + " " + getLabelText()[1] + "\n" + getLabelText()[2] + vertSpd + getLabelText()[3] + "\n" + getLabelText()[4] + " " + getLabelText()[5] + " " + getLabelText()[8] + "\n" + getLabelText()[6] + " " + getLabelText()[7] + " " + getLabelText()[9];
            getLabel().setSize(290, 120);
            getBackground().setSize(290, 120);
        } else {
            updatedText = getLabelText()[0] + "\n" + getLabelText()[2] + " " + getLabelText()[4] + "\n" + getLabelText()[6];
            getLabel().setSize(120, 95);
            getBackground().setSize(120, 95);
        }
        getLabel().setText(updatedText);
        getBackground().setPosition(getLabel().getX(), getLabel().getY());
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

    public Image getBackground() {
        return background;
    }

    public void setBackground(Image background) {
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