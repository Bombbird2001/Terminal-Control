package com.bombbird.atcsim.utilities;

public class MathTools {
    //Set some constant conversion methods
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
}
