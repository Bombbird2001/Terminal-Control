package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.screens.RadarScreen;

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

    Runway(String name, float x, float y, float length, int heading, float textX, float textY, int elevation) {
        //Set the parameters
        this.name = name;
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.elevation = elevation;
        trueHdg = heading - RadarScreen.magHdgDev;

        //Convert length in feet to pixels
        length = AtcSim.feetToPixel(length);

        //Calculate the position offsets
        float xOffsetW = halfWidth * MathUtils.sinDeg(90 - trueHdg);
        float yOffsetW = -halfWidth * MathUtils.cosDeg(90 - trueHdg);
        float xOffsetL = length * MathUtils.cosDeg(90 - trueHdg);
        float yOffsetL = length * MathUtils.sinDeg(90 - trueHdg);

        //Set the label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont8;
        labelStyle.fontColor = Color.WHITE;
        label = new Label(name, labelStyle);
        label.setPosition(textX, textY);
        polygon = new Polygon(new float[] {x - xOffsetW, y - yOffsetW, x - xOffsetW + xOffsetL, y - yOffsetW + yOffsetL, x + xOffsetL + xOffsetW, y + yOffsetL + yOffsetW, x + xOffsetW, y + yOffsetW});
    }

    void setActive(boolean landing, boolean takeoff) {
        this.landing = landing;
        this.takeoff = takeoff;
        active = landing || takeoff;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        label.draw(batch, 1);
    }

    void renderShape() {
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.polygon(polygon.getVertices());
    }

    boolean isActive() {
        return active;
    }
}
