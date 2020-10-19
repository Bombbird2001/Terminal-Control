package com.bombbird.terminalcontrol.entities.approaches

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.ui.tabs.LatTab
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.feetToNm
import com.bombbird.terminalcontrol.utilities.math.MathTools.modulateHeading
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToFeet
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

open class ILS(val airport: Airport, toParse: String) : Actor() {
    companion object {
        private val distance1 = nmToPixel(10f)
        private const val angle1 = 6
        private const val ilsDistNm = 25f
        private val distance2 = nmToPixel(ilsDistNm)
        private const val angle2 = 3
        private const val dashDistNm = 1f
    }

    private lateinit var name: String
    private var x = 0f
    private var y = 0f
    var heading = 0
        private set
    private var gsOffsetNm = 0f
    var minima = 0
        private set
    var gsAlt = 0
        private set
    lateinit var towerFreq: Array<String>
        private set
    var rwy: Runway? = null
        private set
    val missedApchProc: MissedApproach?
    var isNpa = false
    private var gsRings = com.badlogic.gdx.utils.Array<Vector2>()
    var minAlt = 0
    private val radarScreen: RadarScreen = TerminalControl.radarScreen!!

    init {
        parseInfo(toParse)
        var missed = name
        if ("IMG" == name.substring(0, 3)) missed = "LDA" + name.substring(3)
        if ("TCHX" == airport.icao && "IMG13" == name) missed = "IGS13"
        missedApchProc = airport.missedApproaches[missed]
        calculateGsRings()
    }

    /** Parses the input string into info for the ILS  */
    fun parseInfo(toParse: String) {
        isNpa = false
        for ((index, s1) in toParse.split(",".toRegex()).toTypedArray().withIndex()) {
            when (index) {
                0 -> name = s1
                1 -> rwy = airport.runways[s1]
                2 -> heading = s1.toInt()
                3 -> x = s1.toFloat()
                4 -> y = s1.toFloat()
                5 -> gsOffsetNm = s1.toFloat()
                6 -> minima = s1.toInt()
                7 -> gsAlt = s1.toInt()
                8 -> towerFreq = s1.split(">".toRegex()).toTypedArray()
                else -> if (this !is LDA) {
                    Gdx.app.log("Load error", "Unexpected additional parameter in game/" + radarScreen.mainName + "/ils" + airport.icao + ".ils")
                }
            }
        }
    }

    /** Calculates positions of the GS rings; overridden for LDAs  */
    fun calculateGsRings() {
        minAlt = -1
        for (i in 2..gsAlt / 1000) {
            if (i * 1000 > airport.elevation + 1000) {
                gsRings.add(Vector2(x + nmToPixel(getDistAtGsAlt(i * 1000.toFloat())) * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat(), y + nmToPixel(getDistAtGsAlt(i * 1000.toFloat())) * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()))
                if (minAlt == -1) minAlt = i
            }
        }
    }

    /** Draws ILS line using shapeRenderer  */
    fun renderShape() {
        val landing = rwy?.isLanding == true
        val aircraft = radarScreen.getSelectedAircraft()
        val selectedIls = aircraft is Arrival && aircraft.airport == airport && (aircraft.controlState == Aircraft.ControlState.ARRIVAL && LatTab.clearedILS == name || aircraft.controlState == Aircraft.ControlState.UNCONTROLLED && this == aircraft.ils)
        val rwyChange = radarScreen.runwayChanger.containsLandingRunway(airport.icao, rwy?.name ?: "")
        if ((landing || selectedIls || rwyChange) && rwy?.isEmergencyClosed == false) {
            radarScreen.shapeRenderer.color = radarScreen.iLSColour
            if (selectedIls || rwyChange) radarScreen.shapeRenderer.color = Color.YELLOW
            if (radarScreen.showIlsDash) {
                val gsOffsetPx = -nmToPixel(gsOffsetNm)
                var trackerX = x + gsOffsetPx * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()
                var trackerY = y + gsOffsetPx * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()
                val deltaX = nmToPixel(dashDistNm) * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()
                val deltaY = nmToPixel(dashDistNm) * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()
                var drawing = true
                radarScreen.shapeRenderer.line(x, y, trackerX, trackerY)
                run {
                    var i = 0
                    while (i < ilsDistNm + gsOffsetNm) {
                        drawing = !drawing
                        if (drawing) {
                            radarScreen.shapeRenderer.line(trackerX, trackerY, trackerX + deltaX, trackerY + deltaY)
                        }
                        trackerX += deltaX
                        trackerY += deltaY
                        i += dashDistNm.toInt()
                    }
                }

                //Separation marks at 5, 10, 15, 20 miles (except some airports)
                val halfWidth = 30
                val trackRad = Math.toRadians(heading - radarScreen.magHdgDev.toDouble())
                val xOffset = halfWidth * cos(trackRad).toFloat()
                val yOffset = halfWidth * sin(trackRad).toFloat()
                var marksDist = ilsDistNm.toInt()
                if ("TCSS" == airport.icao && "ILS10" == name) {
                    marksDist = 11
                } else if ("TCBD" == airport.icao && "ILS03L" == name) {
                    marksDist = 6
                } else if ("TCBB" == airport.icao && name.contains("ILS24")) {
                    marksDist = 11
                }
                var i = 5
                while (i < marksDist) {
                    val centre = getPointAtDist(i - gsOffsetNm)
                    radarScreen.shapeRenderer.line(centre.x - xOffset, centre.y + yOffset, centre.x + xOffset, centre.y - yOffset)
                    i += 5
                }
            } else {
                radarScreen.shapeRenderer.line(x, y, x + distance2 * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat(), y + distance2 * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat())
            }
            drawGsCircles()
        }
    }

    private fun drawGsCircles() {
        for ((i, vector2) in gsRings.withIndex()) {
            radarScreen.shapeRenderer.color = if (i + minAlt > 3 && !isNpa) Color.GREEN else radarScreen.iLSColour
            radarScreen.shapeRenderer.circle(vector2.x, vector2.y, 8f)
        }
    }

    /** Tests if coordinates input is inside either of the 2 ILS arcs  */
    fun isInsideILS(planeX: Float, planeY: Float): Boolean {
        return isInsideArc(planeX, planeY, distance1, angle1) || isInsideArc(planeX, planeY, distance2, angle2)
    }

    /** Tests if coordinates input is inside the arc of the ILS given the arc angle and distance  */
    private fun isInsideArc(planeX: Float, planeY: Float, distance: Float, angle: Int): Boolean {
        val deltaX = planeX - x
        val deltaY = planeY - y
        var planeHdg = 0.0
        if (deltaX == 0f) {
            if (deltaY > 0) {
                planeHdg = 180.0
            } else if (deltaY < 0) {
                planeHdg = 360.0
            }
        } else {
            val principleAngle = Math.toDegrees(atan(deltaY / deltaX.toDouble()))
            planeHdg = if (deltaX > 0) {
                //Quadrant 1/4
                270 - principleAngle
            } else {
                //Quadrant 2/3
                90 - principleAngle
            }
        }
        planeHdg += radarScreen.magHdgDev
        planeHdg = modulateHeading(planeHdg)
        val smallRange = heading - angle / 2f
        val bigRange = smallRange + angle
        var inAngle = false
        if (smallRange <= 0) {
            if (planeHdg >= smallRange + 360 && planeHdg <= 360) {
                inAngle = true
            } else if (planeHdg > 0 && planeHdg <= bigRange) {
                inAngle = true
            }
        } else if (bigRange > 360) {
            if (planeHdg <= bigRange - 360 && planeHdg > 0) {
                inAngle = true
            } else if (planeHdg in smallRange..360f) {
                inAngle = true
            }
        } else if (planeHdg in smallRange..bigRange) {
            inAngle = true
        }
        val inDist: Boolean
        val dist = distanceBetween(x, y, planeX, planeY)
        inDist = dist <= distance
        return inAngle && inDist
    }

    /** Gets the coordinates of the point on the localiser a distance ahead (behind if -ve) of aircraft  */
    fun getPointAhead(aircraft: Aircraft, distAhead: Float): Vector2 {
        return getPointAtDist(getDistFrom(aircraft.x, aircraft.y) - distAhead)
    }

    /** Gets the coordinates of the point on the localiser at a distance away from ILS origin  */
    fun getPointAtDist(dist: Float): Vector2 {
        return Vector2(x + nmToPixel(dist) * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat(), y + nmToPixel(dist) * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat())
    }

    /** Gets the glide slope altitude (in feet) at distance away from ILS origin  */
    fun getGSAltAtDist(dist: Float): Float {
        return nmToFeet(dist + gsOffsetNm) * tan(Math.toRadians(3.0)).toFloat() + (rwy?.elevation ?: 0)
    }

    /** Gets the glide slope altitude (in feet) of aircraft  */
    fun getGSAlt(aircraft: Aircraft): Float {
        return getGSAltAtDist(getDistFrom(aircraft.x, aircraft.y))
    }

    /** Gets the distance (in nautical miles) from GS origin for a specified altitude  */
    fun getDistAtGsAlt(altitude: Float): Float {
        return feetToNm((altitude - (rwy?.elevation ?: 0)) / tan(Math.toRadians(3.0)).toFloat()) - gsOffsetNm
    }

    /** Gets distance (in nautical miles) from ILS origin, of the input coordinates  */
    fun getDistFrom(planeX: Float, planeY: Float): Float {
        return pixelToNm(distanceBetween(x, y, planeX, planeY))
    }

    override fun getName(): String {
        return name
    }

    override fun setName(name: String) {
        this.name = name
    }

    override fun getX(): Float {
        return x
    }

    override fun setX(x: Float) {
        this.x = x
    }

    override fun getY(): Float {
        return y
    }

    override fun setY(y: Float) {
        this.y = y
    }

    fun setGsRings(gsRings: com.badlogic.gdx.utils.Array<Vector2>) {
        this.gsRings = gsRings
    }
}