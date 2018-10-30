package com.bombbird.terminalcontrol.utilities;

public class MathTools {
    //Set some constant conversion/formula methods
    public static float nmToPixel(float nm) {
        return nm * 32.4f;
    }

    public static float pixelToNm(float pixel) {
        return pixel / 32.4f;
    }

    public static float nmToFeet(float nm) {
        return nm * 6076.12f;
    }

    public static float feetToNm(float feet) {
        return feet / 6076.12f;
    }

    public static float feetToPixel(float feet) {
        return nmToPixel(feetToNm(feet));
    }

    public static float iasToTas(float ias, float altitude) {
        return ias * (1 + altitude / 1000 * 0.02f);
    }

    public static float distanceBetween(float x, float y, float x2, float y2) {
        return (float)Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
    }

    public static double sinDeg(double angle) {
        return Math.toDegrees(Math.sin(angle));
    }

    public static double cosDeg(double angle) {
        return Math.toDegrees(Math.cos(angle));
    }
}
