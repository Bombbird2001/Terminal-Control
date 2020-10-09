package com.bombbird.terminalcontrol.utilities.math

import kotlin.math.*

/** Constant conversions, formula methods */
object MathTools {
    /** Converts nautical mile to pixel  */
    @kotlin.jvm.JvmStatic
    fun nmToPixel(nm: Float): Float {
        return nm * 32.4f
    }

    /** Converts pixel to nautical mile  */
    @kotlin.jvm.JvmStatic
    fun pixelToNm(pixel: Float): Float {
        return pixel / 32.4f
    }

    /** Converts nautical mile to feet  */
    @kotlin.jvm.JvmStatic
    fun nmToFeet(nm: Float): Float {
        return nm * 6076.12f
    }

    /** Converts feet to nautical mile  */
    @kotlin.jvm.JvmStatic
    fun feetToNm(feet: Float): Float {
        return feet / 6076.12f
    }

    /** Converts feet to pixel  */
    @kotlin.jvm.JvmStatic
    fun feetToPixel(feet: Float): Float {
        return nmToPixel(feetToNm(feet))
    }

    /** Returns the approximate true airspeed at a given indicated airspeed and altitude  */
    @kotlin.jvm.JvmStatic
    fun iasToTas(ias: Float, altitude: Float): Float {
        return ias * (1 + altitude / 1000 * 0.02f)
    }

    /** Returns the distance between 2 points in pixels  */
    @kotlin.jvm.JvmStatic
    fun distanceBetween(x: Float, y: Float, x2: Float, y2: Float): Float {
        return sqrt((x2 - x.toDouble()).pow(2.0) + (y2 - y.toDouble()).pow(2.0)).toFloat()
    }

    /** Converts feet to metres  */
    @kotlin.jvm.JvmStatic
    fun feetToMetre(feet: Float): Float {
        return feet / 3.28084f
    }

    /** Calculates the shortest distance required to reach the border supplied with a given track  */
    @kotlin.jvm.JvmStatic
    fun distanceFromBorder(xBorder: FloatArray, yBorder: FloatArray, x: Float, y: Float, direction: Float): Float {
        val cos = cos(Math.toRadians(90 - direction.toDouble())).toFloat()
        val xDistRight = (xBorder[1] - x) / cos
        val xDistLeft = (xBorder[0] - x) / cos
        val sin = sin(Math.toRadians(90 - direction.toDouble())).toFloat()
        val yDistUp = (yBorder[1] - y) / sin
        val yDistDown = (yBorder[0] - y) / sin
        val xDist = if (xDistRight > 0) xDistRight else xDistLeft
        val yDist = if (yDistUp > 0) yDistUp else yDistDown
        return xDist.coerceAtMost(yDist)
    }

    /** Calculates the point where the line from a point at a specified track meets a rectangle's border  */
    @kotlin.jvm.JvmStatic
    fun pointsAtBorder(xBorder: FloatArray?, yBorder: FloatArray?, x: Float, y: Float, direction: Float): FloatArray {
        val dist = distanceFromBorder(xBorder!!, yBorder!!, x, y, direction)
        return floatArrayOf(x + dist * cos(Math.toRadians(90 - direction.toDouble())).toFloat(), y + dist * sin(Math.toRadians(90 - direction.toDouble())).toFloat())
    }

    /** Checks whether integer is within range of 2 integers  */
    @kotlin.jvm.JvmStatic
    fun withinRange(no: Int, min: Int, max: Int): Boolean {
        return no in min..max
    }

    /** Checks whether float is within range of 2 floats  */
    @kotlin.jvm.JvmStatic
    fun withinRange(no: Float, min: Float, max: Float): Boolean {
        return no > min && no < max
    }

    /** Calculates the required track to achieve a displacement of deltaX, deltaY  */
    @kotlin.jvm.JvmStatic
    fun getRequiredTrack(deltaX: Float, deltaY: Float): Float {
        return 90 - Math.toDegrees(atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()
    }

    /** Calculates the required track to go from initial point with x, y to destination point with destX, destY  */
    @kotlin.jvm.JvmStatic
    fun getRequiredTrack(x: Float, y: Float, destX: Float, destY: Float): Float {
        return getRequiredTrack(destX - x, destY - y)
    }

    /** Ensures the heading/track supplied is > 0 and <= 360  */
    @kotlin.jvm.JvmStatic
    fun modulateHeading(heading: Double): Double {
        var newHeading = heading
        while (newHeading > 360) newHeading -= 360.0
        while (newHeading <= 0) newHeading += 360.0
        return newHeading
    }

    /** Ensures the heading/track supplied is > 0 and <= 360  */
    @kotlin.jvm.JvmStatic
    fun modulateHeading(heading: Int): Int {
        var newHeading = heading
        while (newHeading > 360) newHeading -= 360
        while (newHeading <= 0) newHeading += 360
        return newHeading
    }

    /** Calculates the component of vector with a magnitude towards one direction in another direction */
    @kotlin.jvm.JvmStatic
    fun componentInDirection(magnitude: Float, dir1: Float, dir2: Float): Float {
        return magnitude * cos(Math.toRadians((dir1 - dir2).toDouble())).toFloat()
    }

    /** Calculates the component of a vector with direction and magnitude, in another direction */
    @kotlin.jvm.JvmStatic
    fun componentInDirection(magnitude: Int, dir1: Int, dir2: Int): Float {
        return componentInDirection(magnitude.toFloat(), dir1.toFloat(), dir2.toFloat())
    }
}