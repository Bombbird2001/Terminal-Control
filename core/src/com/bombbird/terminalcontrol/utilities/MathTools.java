package com.bombbird.terminalcontrol.utilities;

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
}
