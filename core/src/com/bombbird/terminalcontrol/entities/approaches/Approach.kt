package com.bombbird.terminalcontrol.entities.approaches

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.sidstar.Route
import com.bombbird.terminalcontrol.entities.sidstar.RouteData
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.ui.tabs.Tab
import com.bombbird.terminalcontrol.utilities.math.MathTools
import org.json.JSONObject
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

open class Approach(val airport: Airport, name: String, jsonObject: JSONObject) : Actor() {
    companion object {
        const val angle1 = 6
        const val angle2 = 3
        const val dashDistNm = 1f
    }

    var distance1 = MathTools.nmToPixel(10f)
    var ilsDistNm = 25f
    var distance2 = MathTools.nmToPixel(ilsDistNm)

    var gsRings = Array<Vector2>()

    val routeDataMap = HashMap<String, RouteData>()

    private var name: String
    private var x = 0f
    private var y = 0f
    var heading = 0
        private set
    var gsOffsetNm = 0f
    var minima = 0
        private set
    var gsAlt = 0
        private set
    lateinit var towerFreq: kotlin.Array<String>
        private set
    lateinit var rwy: Runway
        private set
    val missedApchProc: MissedApproach
    var isNpa = false
    var nonPrecAlts: Queue<FloatArray>? = null
    var minAlt = 0
    val radarScreen: RadarScreen = TerminalControl.radarScreen!!

    init {
        this.name = name
        loadInfo(jsonObject)

        var missed = name
        if ("IMG" == name.substring(0, 3)) {
            missed = "LDA" + rwy.name
            if (!airport.missedApproaches.containsKey(missed)) missed = "CIR" + rwy.name
        }
        if ("TCHX" == airport.icao && "IMG13" == name) missed = "IGS13"
        missedApchProc = airport.missedApproaches[missed]!!
    }

    private fun loadInfo(jo: JSONObject) {
        rwy = airport.runways[jo.getString("runway")]!!
        heading = jo.getInt("heading")
        x = jo.getDouble("x").toFloat()
        y = jo.getDouble("y").toFloat()
        gsOffsetNm = jo.getDouble("gsOffset").toFloat()
        minima = jo.getInt("minima")
        gsAlt = jo.getInt("maxGS")
        towerFreq = jo.getString("tower").split(">".toRegex()).toTypedArray()

        val alts = jo.optJSONArray("npa")
        if (alts != null && alts.length() > 0) {
            isNpa = true
            nonPrecAlts = Queue()
            for (i in 0 until alts.length()) {
                val altDist = FloatArray(2)
                val data = alts.getString(i).split(">")
                altDist[0] = data[0].toFloat()
                altDist[1] = data[1].toFloat()
                nonPrecAlts?.addLast(altDist)
            }
        }

        val joWpts = jo.getJSONObject("wpts")
        for (key in joWpts.keySet()) {
            val rData = RouteData()
            val transWpt = joWpts.getJSONArray(key)
            for (i in 0 until transWpt.length()) {
                val data = transWpt.getString(i).split(" ".toRegex()).toTypedArray()
                val wptName = data[0]
                rData.add(
                    radarScreen.waypoints[wptName]!!,
                    intArrayOf(data[1].toInt(), data[2].toInt(), data[3].toInt()),
                    data.size > 4 && data[4] == "FO"
                )
            }
            routeDataMap[key] = rData
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
        planeHdg = MathTools.modulateHeading(planeHdg)
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
        val dist = MathTools.distanceBetween(x, y, planeX, planeY)
        inDist = dist <= distance
        return inAngle && inDist
    }

    /** Gets the coordinates of the point on the localiser a distance ahead (behind if -ve) of aircraft  */
    fun getPointAhead(aircraft: Aircraft, distAhead: Float): Vector2 {
        return getPointAtDist(getDistFrom(aircraft.x, aircraft.y) - distAhead)
    }

    /** Gets the coordinates of the point on the localiser at a distance away from ILS origin  */
    fun getPointAtDist(dist: Float): Vector2 {
        return Vector2(x + MathTools.nmToPixel(dist) * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat(), y + MathTools.nmToPixel(dist) * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat())
    }

    /** Gets the glide slope altitude (in feet) at distance away from ILS origin  */
    fun getGSAltAtDist(dist: Float): Float {
        return MathTools.nmToFeet(dist + gsOffsetNm) * tan(Math.toRadians(3.0)).toFloat() + rwy.elevation
    }

    /** Gets the glide slope altitude (in feet) of aircraft  */
    fun getGSAlt(aircraft: Aircraft): Float {
        return getGSAltAtDist(getDistFrom(aircraft.x, aircraft.y))
    }

    /** Gets the distance (in nautical miles) from GS origin for a specified altitude  */
    fun getDistAtGsAlt(altitude: Float): Float {
        return MathTools.feetToNm((altitude - rwy.elevation) / tan(Math.toRadians(3.0)).toFloat()) - gsOffsetNm
    }

    /** Gets distance (in nautical miles) from ILS origin, of the input coordinates  */
    fun getDistFrom(planeX: Float, planeY: Float): Float {
        return MathTools.pixelToNm(MathTools.distanceBetween(x, y, planeX, planeY))
    }

    /** Returns the routeData for the next possible transition, as well as any additional waypoints to be removed after it; if not available, empty routeData are returned */
    fun getNextPossibleTransition(direct: Waypoint?, route: Route): Triple<RouteData, RouteData, Waypoint?> {
        if (direct == null) return Triple(routeDataMap["vector"] ?: RouteData(), RouteData(), route.waypoints[route.size - 1]) //If not flying to a direct currently, return the default transition
        val index = route.findWptIndex(direct.name)
        for (i in index until route.size) {
            routeDataMap[route.getWaypoint(i)?.name]?.let {
                return Triple(it, route.routeData.getRange(i + 1, route.size - 1), route.getWaypoint(i)) //If a transition found, return it (heh)
            }
        }
        return Triple(RouteData(), RouteData(), null) //If no transitions found at all, return the default transition
    }

    /** Draws ILS line using shapeRenderer  */
    fun renderShape() {
        val landing = rwy.isLanding
        val aircraft = radarScreen.selectedAircraft
        val selectedIls = aircraft is Arrival && aircraft.airport == airport && (aircraft.controlState == Aircraft.ControlState.ARRIVAL && Tab.clearedILS == name || aircraft.controlState == Aircraft.ControlState.UNCONTROLLED && this == aircraft.apch)
        val rwyChange = radarScreen.runwayChanger.containsLandingRunway(airport.icao, rwy.name)
        if ((landing || selectedIls || rwyChange) && !rwy.isEmergencyClosed) {
            radarScreen.shapeRenderer.color = radarScreen.iLSColour
            if (selectedIls || rwyChange) radarScreen.shapeRenderer.color = Color.YELLOW
            if (radarScreen.showIlsDash) {
                val gsOffsetPx = -MathTools.nmToPixel(gsOffsetNm)
                var trackerX = x + gsOffsetPx * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()
                var trackerY = y + gsOffsetPx * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()
                val deltaX = MathTools.nmToPixel(dashDistNm) * cos(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()
                val deltaY = MathTools.nmToPixel(dashDistNm) * sin(Math.toRadians(270 - heading + radarScreen.magHdgDev.toDouble())).toFloat()
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
}