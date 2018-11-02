package com.bombbird.terminalcontrol.entities.aircrafts.approaches;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class ILS extends Actor {
    private String name;
    private float x;
    private float y;
    private int heading;
    private float gsOffset;
    private int minima;
    private int gsAlt;
    //TODO Set go around altitude
    private Runway rwy;

    private Array<Vector2> gsRings = new Array<Vector2>();

    private static final float distance1 = MathTools.nmToPixel(17);
    private static final int angle1 = 35;

    private static final float distance2 = MathTools.nmToPixel(25);
    private static final int angle2 = 10;

    public ILS(String name, float x, float y, int heading, float gsOffset, int minima, int gsAlt, Runway rwy) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.heading = heading;
        this.gsOffset = gsOffset;
        this.minima = minima;
        this.gsAlt = gsAlt;
        this.rwy = rwy;

        calculateGsRings();
    }

    public void calculateGsRings() {
        for (int i = 2; i <= gsAlt / 1000; i++) {
            gsRings.add(new Vector2(x + MathTools.nmToPixel(getDistAtGsAlt(i * 1000)) * MathUtils.cosDeg(270 - heading + RadarScreen.magHdgDev), y + MathTools.nmToPixel(getDistAtGsAlt(i * 1000)) * MathUtils.sinDeg(270 - heading + RadarScreen.magHdgDev)));
        }
    }

    /** Draws ILS line using shapeRenderer */
    public void renderShape() {
        if (rwy.isLanding()) {
            GameScreen.shapeRenderer.setColor(Color.CYAN);
            GameScreen.shapeRenderer.line(x, y, x + distance2 * MathUtils.cosDeg(270 - heading + RadarScreen.magHdgDev), y + distance2 * MathUtils.sinDeg(270 - heading + RadarScreen.magHdgDev));
            drawGsCircles();
        }
    }

    public void drawGsCircles() {
        for (Vector2 vector2: gsRings) {
            GameScreen.shapeRenderer.circle(vector2.x, vector2.y, 8);
        }
    }

    /** Tests if coordinates input is inside either of the 2 ILS arcs */
    public boolean isInsideILS(float planeX, float planeY) {
        return isInsideArc(planeX, planeY, distance1, angle1) || isInsideArc(planeX, planeY, distance2, angle2);
    }

    /** Tests if coordinates input is inside the arc of the ILS given the arc angle and distance */
    private boolean isInsideArc(float planeX, float planeY, float distance, int angle) {
        float deltaX = planeX - x;
        float deltaY = planeY - y;
        double planeHdg = 0;
        if (deltaX == 0) {
            if (deltaY > 0) {
                planeHdg = 180;
            } else if (deltaY < 0) {
                planeHdg = 360;
            }
        } else {
            double principleAngle = Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees;
            if (deltaX > 0) {
                //Quadrant 1/4
                planeHdg = 270 - principleAngle;
            } else {
                //Quadrant 2/3
                planeHdg = 90 - principleAngle;
            }
        }

        planeHdg += RadarScreen.magHdgDev;

        if (planeHdg <= 0) {
            planeHdg += 360;
        } else if (planeHdg > 360) {
            planeHdg -= 360;
        }

        float smallRange = heading - angle / 2f;
        float bigRange = smallRange + angle;

        boolean inAngle = false;

        if (smallRange <= 0) {
            if (planeHdg >= smallRange + 360 && planeHdg <= 360) {
                inAngle = true;
            } else if (planeHdg > 0 && planeHdg <= bigRange) {
                inAngle = true;
            }
        } else if (bigRange > 360) {
            if (planeHdg <= bigRange - 360 && planeHdg > 0) {
                inAngle = true;
            } else if (planeHdg <= 360 && planeHdg >= smallRange) {
                inAngle = true;
            }
        } else if (planeHdg <= bigRange && planeHdg >= smallRange) {
            inAngle = true;
        }

        boolean inDist;

        float dist = MathTools.distanceBetween(x, y, planeX, planeY);
        inDist = dist <= distance;

        return inAngle && inDist;
    }

    /** Gets the coordinates of the point on the localiser 1nm ahead of aircraft */
    public Vector2 getPointAhead(Aircraft aircraft) {
        return getPointAtDist(getDistFrom(aircraft.getX(), aircraft.getY()) - 0.75f);
    }

    /** Gets the coordinates of the point on the localiser at a distance away from ILS origin */
    private Vector2 getPointAtDist(float dist) {
        return new Vector2(x + MathTools.nmToPixel(dist) * MathUtils.cosDeg(270 - heading + RadarScreen.magHdgDev), y + MathTools.nmToPixel(dist) * MathUtils.sinDeg(270 - heading + RadarScreen.magHdgDev));
    }

    /** Gets the glide slope altitude (in feet) at distance away from ILS origin */
    private float getGSAltAtDist(float dist) {
        return MathTools.nmToFeet(dist + gsOffset) * (float) Math.tan(Math.toRadians(3)) + rwy.getElevation();
    }

    /** Gets the glide slope altitude (in feet) of aircraft */
    public float getGSAlt(Aircraft aircraft) {
        return getGSAltAtDist(getDistFrom(aircraft.getX(), aircraft.getY()));
    }

    /** Gets the distance (in nautical miles) from GS origin for a specified altitude */
    public float getDistAtGsAlt(float altitude) {
        return MathTools.feetToNm((altitude - rwy.getElevation()) / (float) Math.tan(Math.toRadians(3))) - gsOffset;
    }

    /** Gets distance (in nautical miles) from ILS origin, of the input coordinates */
    private float getDistFrom(float planeX, float planeY) {
        return MathTools.pixelToNm(MathTools.distanceBetween(x, y, planeX, planeY));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Runway getRwy() {
        return rwy;
    }

    public void setRwy(Runway rwy) {
        this.rwy = rwy;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public int getHeading() {
        return heading;
    }

    public float getGsOffset() {
        return gsOffset;
    }

    public int getMinima() {
        return minima;
    }

    public int getGsAlt() {
        return gsAlt;
    }
}
