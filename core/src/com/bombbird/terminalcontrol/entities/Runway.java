package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class Runway {
    //Name of runway
    private String name;

    //The opposite runway
    private Runway oppRwy;

    //Set landing/takeoff status
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

    //Array of aircraft on approach
    private Array<Aircraft> aircraftsOnAppr;

    private RadarScreen radarScreen;
    private ShapeRenderer shapeRenderer;

    public Runway(String toParse) {
        radarScreen = TerminalControl.radarScreen;
        shapeRenderer = radarScreen.shapeRenderer;

        parseInfo(toParse);

        aircraftsOnAppr = new Array<Aircraft>();

        //Calculate the position offsets
        float xOffsetW = halfWidth * MathUtils.sinDeg(90 - getTrueHdg());
        float yOffsetW = -halfWidth * MathUtils.cosDeg(90 - getTrueHdg());
        float xOffsetL = pxLength * MathUtils.cosDeg(90 - getTrueHdg());
        float yOffsetL = pxLength * MathUtils.sinDeg(90 - getTrueHdg());

        //Create polygon
        polygon = new Polygon(new float[] {x - xOffsetW, y - yOffsetW, x - xOffsetW + xOffsetL, y - yOffsetW + yOffsetL, x + xOffsetL + xOffsetW, y + yOffsetL + yOffsetW, x + xOffsetW, y + yOffsetW});
    }

    /** Parses the input string into relevant data for the runway */
    private void parseInfo(String toParse) {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont8;
        labelStyle.fontColor = Color.WHITE;

        String[] rwyInfo = toParse.split(",");
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
                        trueHdg = heading - radarScreen.magHdgDev;
                        break;
                case 5: label.setX(Float.parseFloat(s1)); break;
                case 6: label.setY(Float.parseFloat(s1)); break;
                case 7: elevation = Integer.parseInt(s1); break;
                default: Gdx.app.log("Load error", "Unexpected additional parameter in data for runway " + name);
            }
            index++;
        }
        radarScreen.stage.addActor(label);
    }

    /** Sets runway status for landing, takeoffs */
    public void setActive(boolean landing, boolean takeoff) {
        this.landing = landing;
        this.takeoff = takeoff;
        label.setVisible(landing || takeoff);
    }

    /** Renders the runway rectangle */
    public void renderShape() {
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.polygon(polygon.getVertices());
    }

    /** Called to remove aircraft from the array of aircrafts on approach, should be called during go arounds/cancelling approaches */
    public void removeFromArray(Aircraft aircraft) {
        aircraftsOnAppr.removeValue(aircraft, false);
    }

    /** Called to add the aircraft to the array, automatically determines the position of the aircraft in the array, should be called during initial aircraft LOC capture, returns aircraft index in array */
    public void addToArray(Aircraft aircraft) {
        aircraftsOnAppr.add(aircraft);
        if (aircraftsOnAppr.size > 1) {
            int thisIndex = aircraftsOnAppr.size - 1;
            while (MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), x, y) < MathTools.distanceBetween(aircraftsOnAppr.get(thisIndex - 1).getX(), aircraftsOnAppr.get(thisIndex  - 1).getY(), x, y) && !aircraftsOnAppr.get(thisIndex - 1).isOnGround()) {
                aircraftsOnAppr.swap(thisIndex - 1, thisIndex);
                thisIndex -= 1;
            }
        }
    }

    /** Called during an (unlikely) event that for some reason the current aircraft overtakes the aircraft in front of it */
    public void swapAircrafts(Aircraft aircraft) {
        int thisIndex = aircraftsOnAppr.indexOf(aircraft, false);
        aircraftsOnAppr.swap(thisIndex, thisIndex - 1);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

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

    public boolean isWindshear() {
        return windshear;
    }

    public void setWindshear(boolean windshear) {
        this.windshear = windshear;
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

    public Array<Aircraft> getAircraftsOnAppr() {
        return aircraftsOnAppr;
    }

    public void setAircraftsOnAppr(Array<Aircraft> aircraftsOnAppr) {
        this.aircraftsOnAppr = aircraftsOnAppr;
    }

    public Label getLabel() {
        return label;
    }
}