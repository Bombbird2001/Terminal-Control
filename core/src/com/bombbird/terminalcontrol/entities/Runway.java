package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.MathTools;

import static com.bombbird.terminalcontrol.screens.GameScreen.shapeRenderer;

public class Runway extends Actor {
    //Name of runway
    private String name;

    //The opposite runway
    private Runway oppRwy;

    //Set landing/takeoff status
    private boolean active;
    private boolean landing;
    private boolean takeoff;

    //Position of bottom center of runway
    private float x;
    private float y;
    private int elevation;

    //Set dimensions
    private static float halfWidth = 2f;
    private float pxLength;

    //Set heading of runway
    private int heading;
    private float trueHdg;

    //Label of runway
    private Label label;

    //Set polygon to render later
    private Polygon polygon;

    //Set the ILS
    private ILS ils;

    //Set windshear properties
    private boolean windshear;

    public Runway(String toParse) {
        parseInfo(toParse);

        //Calculate the position offsets
        float xOffsetW = halfWidth * MathUtils.sinDeg(90 - getTrueHdg());
        float yOffsetW = -halfWidth * MathUtils.cosDeg(90 - getTrueHdg());
        float xOffsetL = pxLength * MathUtils.cosDeg(90 - getTrueHdg());
        float yOffsetL = pxLength * MathUtils.sinDeg(90 - getTrueHdg());

        //Create polygon
        setPolygon(new Polygon(new float[] {x - xOffsetW, y - yOffsetW, x - xOffsetW + xOffsetL, y - yOffsetW + yOffsetL, x + xOffsetL + xOffsetW, y + yOffsetL + yOffsetW, x + xOffsetW, y + yOffsetW}));

        GameScreen.stage.addActor(this);
    }

    private void parseInfo(String toParse) {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont8;
        labelStyle.fontColor = Color.WHITE;

        String rwyInfo[] = toParse.split(",");
        int index = 0;
        for (String s1: rwyInfo) {
            switch (index) {
                case 0: name = s1;
                        label = new Label(name, labelStyle);
                        break;
                case 1: x = Float.parseFloat(s1); break;
                case 2: y = Float.parseFloat(s1); break;
                case 3: pxLength = MathTools.feetToPixel(Integer.parseInt(s1)); break;
                case 4: heading = Integer.parseInt(s1);
                        trueHdg = heading - RadarScreen.magHdgDev;
                        break;
                case 5: label.setX(Float.parseFloat(s1)); break;
                case 6: label.setY(Float.parseFloat(s1)); break;
                case 7: elevation = Integer.parseInt(s1); break;
                default: Gdx.app.log("Load error", "Unexpected additional parameter in data for runway " + name);
            }
            index++;
        }
    }

    public void setActive(boolean landing, boolean takeoff) {
        this.setLanding(landing);
        this.setTakeoff(takeoff);
        setActive(landing || takeoff);
        setVisible(landing || takeoff);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        label.draw(batch, 1);
    }

    public void renderShape() {
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.polygon(getPolygon().getVertices());
    }

    public boolean isActive() {
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

    public float getPxLength() {
        return pxLength;
    }

    public void setPxLength(float pxLength) {
        this.pxLength = pxLength;
    }

    public ILS getIls() {
        return ils;
    }

    public void setIls(ILS ils) {
        this.ils = ils;
    }

    public Runway getOppRwy() {
        return oppRwy;
    }

    public void setOppRwy(Runway oppRwy) {
        this.oppRwy = oppRwy;
    }
}
