package com.bombbird.terminalcontrol.utilities.math;

public class MathToolsJava {
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
        return (float) Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
    }

    /** Converts feet to metres */
    public static float feetToMetre(float feet) {
        return feet / 3.28084f;
    }

    /** Calculates the shortest distance required to reach the border supplied with a given track */
    public static float distanceFromBorder(float[] xBorder, float[] yBorder, float x, float y, float direction) {
        float cos = (float) Math.cos(Math.toRadians(90 - direction));
        float xDistRight = (xBorder[1] - x) / cos;
        float xDistLeft = (xBorder[0] - x) / cos;
        float sin = (float) Math.sin(Math.toRadians(90 - direction));
        float yDistUp = (yBorder[1] - y) / sin;
        float yDistDown = (yBorder[0] - y) / sin;
        float xDist = xDistRight > 0 ? xDistRight : xDistLeft;
        float yDist = yDistUp > 0 ? yDistUp : yDistDown;
        return Math.min(xDist, yDist);
    }

    /** Calculates the point where the line from a point at a specified track meets a rectangle's border */
    public static float[] pointsAtBorder(float[] xBorder, float[] yBorder, float x, float y, float direction) {
        float dist = distanceFromBorder(xBorder, yBorder, x, y, direction);
        return new float[] {x + dist * (float) Math.cos(Math.toRadians(90 - direction)), y + dist * (float) Math.sin(Math.toRadians(90 - direction))};
    }

    /** Checks whether integer is within range of 2 integers */
    public static boolean withinRange(int no, int min, int max) {
        return no >= min && no <= max;
    }

    /** Checks whether float is within range of 2 floats */
    public static boolean withinRange(float no, float min, float max) {
        return no > min && no < max;
    }

    /** Calculates the required track to achieve a displacement of deltaX, deltaY */
    public static float getRequiredTrack(float deltaX, float deltaY) {
        return 90 - (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
    }

    /** Calculates the required track to go from initial point with x, y to destination point with destX, destY */
    public static float getRequiredTrack(float x, float y, float destX, float destY) {
        return getRequiredTrack(destX - x, destY - y);
    }

    /** Ensures the heading/track supplied is > 0 and <= 360 */
    public static double modulateHeading(double heading) {
        double newHeading = heading;
        while (newHeading > 360) newHeading -= 360;
        while (newHeading <= 0) newHeading += 360;
        return newHeading;
    }

    /** Ensures the heading/track supplied is > 0 and <= 360 */
    public static int modulateHeading(int heading) {
        int newHeading = heading;
        while (newHeading > 360) newHeading -= 360;
        while (newHeading <= 0) newHeading += 360;
        return newHeading;
    }
}
