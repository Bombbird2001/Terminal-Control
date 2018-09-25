package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.screens.RadarScreen;
import com.bombbird.atcsim.utilities.MathTools;

import static com.bombbird.atcsim.screens.GameScreen.shapeRenderer;

public class Runway extends Actor {
    //Name of runway
    private String name;

    //Set landing/takeoff status
    private boolean active;
    private boolean landing;
    private boolean takeoff;

    //Position of bottom center of runway
    private float x;
    private float y;
    private int elevation;

    //Set constant width
    private static float halfWidth = 2f;

    //Set heading of runway
    private int heading;
    private float trueHdg;

    //Label of runway
    private Label label;

    //Set polygon to render later
    private Polygon polygon;

    //Set the ILS
    //TODO: Implement ILS

    //Set windshear properties
    private boolean windshear;

    public Runway(String name, float x, float y, float length, int heading, float textX, float textY, int elevation) {
        //Set the parameters
        this.setName(name);
        this.setX(x);
        this.setY(y);
        this.setHeading(heading);
        this.setElevation(elevation);
        setTrueHdg(heading - RadarScreen.magHdgDev);

        //Convert length in feet to pixels
        float pxLength = MathTools.feetToPixel(length);

        //Calculate the position offsets
        float xOffsetW = getHalfWidth() * MathUtils.sinDeg(90 - getTrueHdg());
        float yOffsetW = -getHalfWidth() * MathUtils.cosDeg(90 - getTrueHdg());
        float xOffsetL = pxLength * MathUtils.cosDeg(90 - getTrueHdg());
        float yOffsetL = pxLength * MathUtils.sinDeg(90 - getTrueHdg());

        //Set the label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont8;
        labelStyle.fontColor = Color.WHITE;
        setLabel(new Label(name, labelStyle));
        getLabel().setPosition(textX, textY);
        setPolygon(new Polygon(new float[] {x - xOffsetW, y - yOffsetW, x - xOffsetW + xOffsetL, y - yOffsetW + yOffsetL, x + xOffsetL + xOffsetW, y + yOffsetL + yOffsetW, x + xOffsetW, y + yOffsetW}));
    }

    public static float getHalfWidth() {
        return halfWidth;
    }

    public static void setHalfWidth(float halfWidth) {
        Runway.halfWidth = halfWidth;
    }

    void setActive(boolean landing, boolean takeoff) {
        this.setLanding(landing);
        this.setTakeoff(takeoff);
        setActive(landing || takeoff);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        getLabel().draw(batch, 1);
    }

    void renderShape() {
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.polygon(getPolygon().getVertices());
    }

    boolean isActive() {
        return active;
    }

    public boolean isLanding() {
        return landing;
    }

    public boolean isTakeoff() {
        return takeoff;
    }

    public int getElevation() {
        return elevation;
    }

    public int getHeading() {
        return heading;
    }

    public float[] getPosition() {
        return new float[] {getX(), getY()};
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setLanding(boolean landing) {
        this.landing = landing;
    }

    public void setTakeoff(boolean takeoff) {
        this.takeoff = takeoff;
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

    public void setElevation(int elevation) {
        this.elevation = elevation;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public float getTrueHdg() {
        return trueHdg;
    }

    public void setTrueHdg(float trueHdg) {
        this.trueHdg = trueHdg;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public boolean isWindshear() {
        return windshear;
    }

    public void setWindshear(boolean windshear) {
        this.windshear = windshear;
    }
}
