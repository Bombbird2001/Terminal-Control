package com.bombbird.terminalcontrol.utilities.math;

import com.badlogic.gdx.math.MathUtils;

public class MathTools {
    //Set some constant conversion/formula methods
    /** Converts nautical mile to pixel */
    public static float nmToPixel(float nm) {
        return nm * 32.4f;
    }

    /** Converts pixel to nautical mile */
    public static float pixelToNm(float pixel) {
        return pixel / 32.4f;
    }

    /** Converts nautical mile to feet */
    public static float nmToFeet(float nm) {
        return nm * 6076.12f;
    }

    /** Converts feet to nautical mile */
    public static float feetToNm(float feet) {
        return feet / 6076.12f;
    }

    /** Converts feet to pixel */
    public static float feetToPixel(float feet) {
        return nmToPixel(feetToNm(feet));
    }

    /** Returns the approximate true airspeed at a given indicated airspeed and altitude */
    public static float iasToTas(float ias, float altitude) {
        return ias * (1 + altitude / 1000 * 0.02f);
    }

    /** Returns the distance between 2 points in pixels */
    public static float distanceBetween(float x, float y, float x2, float y2) {
        return (float)Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
    }

    /** Converts feet to metres */
    public static float feetToMetre(float feet) {
        return feet / 3.28084f;
    }

    /** Calculates the shortest distance required to reach the border supplied with a given track */
    public static float distanceFromBorder(float[] xBorder, float[] yBorder, float x, float y, float track) {
        float xDistRight = (xBorder[1] - x) / MathUtils.cosDeg(90 - track);
        float xDistLeft = (xBorder[0] - x) / MathUtils.cosDeg(90 - track);
        float yDistUp = (yBorder[1] - y) / MathUtils.sinDeg(90 - track);
        float yDistDown = (yBorder[0] - y) / MathUtils.sinDeg(90 - track);
        float xDist = xDistRight > 0 ? xDistRight : xDistLeft;
        float yDist = yDistUp > 0 ? yDistUp : yDistDown;
        return xDist > yDist ? yDist : xDist;
    }

    /** Calculates the point where the line from a point at a specified track meets the radar screen's border */
    public static float[] pointsAtBorder(float[] xBorder, float[] yBorder, float x, float y, float track) {
        float dist = distanceFromBorder(xBorder, yBorder, x, y, track);
        return new float[] {x + dist * MathUtils.cosDeg(90 - track), y + dist * MathUtils.sinDeg(90 - track)};
    }
}
