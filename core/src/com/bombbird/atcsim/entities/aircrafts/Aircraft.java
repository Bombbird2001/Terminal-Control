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
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;
import com.bombbird.atcsim.utilities.MathTools;

import static com.bombbird.atcsim.screens.GameScreen.*;

public class Aircraft extends Actor {
    //Rendering parameters
    Label label;
    private Label.LabelStyle labelStyle;
    String[] labelText;
    private boolean selected;
    private static boolean loadedIcons = false;
    public static TextureAtlas iconAtlas = new TextureAtlas(Gdx.files.internal("game/aircrafts/aircraftIcons.atlas"));
    public static Skin skin = new Skin();
    private ImageButton icon;
    private static ImageButton.ImageButtonStyle buttonStyleCtrl;
    private static ImageButton.ImageButtonStyle buttonStyleUnctrl;
    private static ImageButton.ImageButtonStyle buttonStyleEnroute;
    private Image background;

    //Aircraft information
    Airport airport;
    //Aircraft characteristics
    String callsign;
    String icaoType;
    char wakeCat;
    int[] maxVertSpd;
    int minSpeed;
    private int controlState;

    //Aircraft position
    float x;
    float y;
    String latMode;
    double heading;
    int clearedHeading;
    private double angularVelocity;
    double track;
    Waypoint direct;

    //Altitude
    float altitude;
    public int clearedAltitude;
    private int targetAltitude;
    private float verticalSpeed;
    public String altMode;

    //Speed
    public float ias;
    private float tas;
    private float gs;
    private Vector2 deltaPosition;
    public int clearedIas;
    private int targetIas;
    private float deltaIas;
    public String spdMode;

    //Winds
    int[] winds;

    Aircraft(String callsign, String icaoType, char wakeCat, int[] maxVertSpd, int minSpeed) {
        if (!loadedIcons) {
            skin.addRegions(iconAtlas);
            buttonStyleCtrl = new ImageButton.ImageButtonStyle();
            buttonStyleCtrl.imageUp = skin.getDrawable("aircraftControlled");
            buttonStyleCtrl.imageDown = skin.getDrawable("aircraftControlled");
            buttonStyleUnctrl = new ImageButton.ImageButtonStyle();
            buttonStyleUnctrl.imageUp = skin.getDrawable("aircraftNotControlled");
            buttonStyleUnctrl.imageDown = skin.getDrawable("aircraftNotControlled");
            buttonStyleEnroute = new ImageButton.ImageButtonStyle();
            buttonStyleEnroute.imageUp = skin.getDrawable("aircraftEnroute");
            buttonStyleEnroute.imageDown = skin.getDrawable("aircraftEnroute");
            loadedIcons = true;
        }

        this.callsign = callsign;
        stage.addActor(this);
        this.icaoType = icaoType;
        this.wakeCat = wakeCat;
        this.maxVertSpd = maxVertSpd;
        this.minSpeed = minSpeed;
        latMode = "star";
        heading = 0;
        clearedHeading = (int) heading;
        track = 0;
        altitude = 10000;
        clearedAltitude = 10000;
        targetAltitude = 10000;
        verticalSpeed = 0;
        altMode = "open";
        ias = 250;
        tas = ias * (1 + altitude / 1000 * 0.02f);
        gs = tas;
        deltaPosition = new Vector2();
        clearedIas = 250;
        targetIas = 250;
        deltaIas = 0;
        spdMode = "open";

        selected = false;
        loadLabel();
    }

    private void loadLabel() {
        icon = new ImageButton(buttonStyleUnctrl);
        icon.setSize(40, 40);
        icon.getImageCell().size(40, 40);
        icon.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                aircrafts.get(actor.getName()).setSelected(true);
                event.handle();
                System.out.println("Aircraft clicked/tapped");
            }
        });
        stage.addActor(icon);

        background = new Image();
        background.setDrawable(skin.getDrawable("labelBackground"));
        background.setPosition(x - 100, y + 25);
        background.setSize(290, 120);
        GameScreen.stage.addActor(background);

        icon.setName(callsign);
        labelText = new String[10];
        labelText[9] = "";
        labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont6;
        labelStyle.fontColor = Color.WHITE;
        label = new Label("Loading...", labelStyle);
        label.setPosition(x - 100, y + 25);
        label.setSize(290, 120);
        label.addListener(new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                label.moveBy(x - label.getWidth() / 2, y - label.getHeight() / 2);
                event.handle();
            }
        });
        GameScreen.stage.addActor(label);
    }

    public void renderShape() {
        if (selected && direct != null) {
            drawStar();
        }
        moderateLabel();
        shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(label.getX() + label.getWidth() / 2, label.getY() + label.getHeight() / 2, x, y);
        if (controlState == 1) {
            shapeRenderer.setColor(Color.GREEN);
            GameScreen.shapeRenderer.line(x, y, x + gs * MathUtils.cosDeg((float)(90 - track)), y + gs * MathUtils.sinDeg((float)(90 - track)));
            shapeRenderer.setColor(Color.BROWN);
            GameScreen.shapeRenderer.line(x, y, x + tas * MathUtils.cosDeg((float)(90 - heading + RadarScreen.magHdgDev)), y + tas * MathUtils.sinDeg((float)(90 - heading + RadarScreen.magHdgDev)));
        }
    }

    double update() {
        if (direct != null) {
            direct.setSelected(true);
        }
        double[] info = updateTargetHeading();
        updateHeading(info[0], 0);
        updatePosition(info[1]);
        return info[0];
    }

    private double[] updateTargetHeading() {
        deltaPosition.setZero();
        double targetHeading;
        double angleDiff;

        //Get wind data
        if (altitude - airport.elevation <= 4000) {
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
            float deltaX = direct.x - x;
            float deltaY = direct.y - y;
            float distance = (float)Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
            //If within 25px of waypoint, target next waypoint
            if (distance <= 25) {
                updateDirect();
            }

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
        }

        if (targetHeading > 360) {
            targetHeading -= 360;
        } else if (targetHeading <= 0) {
            targetHeading += 360;
        }
        //System.out.println(callsign + " target heading: " + targetHeading);
        return new double[] {targetHeading, angleDiff};
    }

    private void updatePosition(double angleDiff) {
        track = heading - RadarScreen.magHdgDev + angleDiff;
        deltaPosition.x = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * MathUtils.cosDeg((float)(90 - track));
        deltaPosition.y = Gdx.graphics.getDeltaTime() * MathTools.nmToPixel(gs) / 3600 * MathUtils.sinDeg((float)(90 - track));
        x += deltaPosition.x;
        y += deltaPosition.y;
        label.moveBy(deltaPosition.x, deltaPosition.y);
    }

    private void updateHeading(double targetHeading, int forceDirection) {
        double deltaHeading = targetHeading - heading;
        //System.out.println(callsign + " deltaheading: " + deltaHeading);
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
        //System.out.println(callsign + " new deltaheading: " + deltaHeading);
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
        //System.out.println(callsign + " angular velocity: " + angularVelocity);
        //System.out.println(callsign + " target angular velocity: " + targetAngularVelocity);
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
        heading += angularVelocity * Gdx.graphics.getDeltaTime();
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
        icon.setPosition(x - 20, y - 20);
        icon.setColor(Color.BLACK); //Icon doesn't draw without this for some reason
        icon.draw(batch, 1);
    }

    void drawStar() {

    }

    void updateDirect() {

    }

    public void setControlState(int controlState) {
        this.controlState = controlState;
        if (controlState == -1) {
            icon.setStyle(buttonStyleEnroute);
        } else if (controlState == 0) {
            icon.setStyle(buttonStyleUnctrl);
        } else if (controlState == 1) {
            icon.setStyle(buttonStyleCtrl);
        } else {
            Gdx.app.log("Aircraft control state error", "Invalid control state " + controlState + " set!");
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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
        if (verticalSpeed < -100) {
            vertSpd = " DOWN ";
        } else if (verticalSpeed > 100) {
            vertSpd = " UP ";
        } else {
            vertSpd = " = ";
        }
        labelText[0] = callsign;
        labelText[1] = icaoType + "/" + wakeCat;
        labelText[2] = Integer.toString((int)(altitude / 100));
        labelText[3] = Integer.toString(clearedAltitude / 100);
        labelText[4] = Integer.toString((int)heading);
        if (latMode.equals("vector")) {
            labelText[5] = Integer.toString(clearedHeading);
        } else {
            labelText[5] = direct.name;
        }
        labelText[6] = Integer.toString((int)gs);
        labelText[7] = Integer.toString(clearedIas);
        String updatedText;
        if (controlState == 1) {
            updatedText = labelText[0] + " " + labelText[1] + "\n" + labelText[2] + vertSpd + labelText[3] + "\n" + labelText[4] + " " + labelText[5] + " " + labelText[8] + "\n" + labelText[6] + " " + labelText[7] + " " + labelText[9];
            label.setSize(290, 120);
            background.setSize(290, 120);
        } else {
            updatedText = labelText[0] + "\n" + labelText[2] + " " + labelText[4] + "\n" + labelText[6];
            label.setSize(120, 95);
            background.setSize(120, 95);
        }
        label.setText(updatedText);
        background.setPosition(label.getX(), label.getY());
    }
}