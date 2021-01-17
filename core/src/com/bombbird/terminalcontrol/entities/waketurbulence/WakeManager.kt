package com.bombbird.terminalcontrol.entities.waketurbulence

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.approaches.Circling
import com.bombbird.terminalcontrol.entities.separation.trajectory.PositionPoint
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import com.bombbird.terminalcontrol.utilities.math.MathTools.withinRange
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

class WakeManager() {
    private val aircraftWakes: HashMap<String, Array<PositionPoint>> = HashMap()
    private val radarScreen: RadarScreen
        get() = TerminalControl.radarScreen!!

    constructor(save: JSONObject) : this() {
        for (i in 0 until save.length()) {
            val callsign = save.names().getString(i)
            val array = save.getJSONArray(callsign)
            val wakePoints = Array<PositionPoint>()
            for (j in 0 until array.length()) {
                wakePoints.add(PositionPoint(array.getJSONObject(j)))
            }
            aircraftWakes[callsign] = wakePoints
        }
    }

    /** Initialises aircraft array for new aircraft  */
    fun addAircraft(callsign: String) {
        aircraftWakes[callsign] = Array()
    }

    /** Removes array for aircraft  */
    fun removeAircraft(callsign: String) {
        aircraftWakes.remove(callsign)
    }

    /** Called after 0.5nm travelled, adds a new point from aircraft, updates subsequent points to decrement distance, total maximum 16 points for 8nm  */
    fun addPoint(aircraft: Aircraft) {
        if (!aircraftWakes.containsKey(aircraft.callsign)) aircraftWakes[aircraft.callsign] = Array()
        aircraftWakes[aircraft.callsign]?.add(PositionPoint(aircraft, aircraft.x, aircraft.y, aircraft.altitude.toInt()))
        val extra = (aircraftWakes[aircraft.callsign]?.size ?: 0) - 16
        if (extra > 0) aircraftWakes[aircraft.callsign]?.removeRange(0, extra - 1)
    }

    /** Checks for aircraft separation from wake turbulence of other aircraft, returns -1 if safe separation, else a positive float which is the difference between required, actual separation  */
    fun checkAircraftWake(aircraft: Aircraft): Float {
        if (aircraft.isOnGround) {
            //Remove wake array, ignore if aircraft has landed
            removeAircraft(aircraft.callsign)
            return (-1).toFloat()
        }
        if (aircraft is Departure && aircraft.altitude <= aircraft.airport.elevation + 3000) return (-1).toFloat() //Temporarily ignore if departure is still below 3000 feet AGL
        if (aircraft.emergency.isActive) return (-1).toFloat() //Ignore if aircraft is an active emergency
        for (callsign in aircraftWakes.keys) {
            if (callsign == aircraft.callsign) continue  //Skip if is itself
            val aircraft2 = radarScreen.aircrafts[callsign] ?: continue //Front plane
            if (distanceBetween(aircraft.x, aircraft.y, aircraft2.x, aircraft2.y) > 8 * 32.4) continue  //Skip if aircraft is more than 8nm away
            val reqDist = SeparationMatrix.getWakeSepDist(aircraft2.recat, aircraft.recat)
            if (reqDist < 3) continue  //Skip if required separation is less than 3
            var dist = 0f
            val wakePoints = aircraftWakes[callsign] ?: continue
            for (i in wakePoints.size - 1 downTo 0) {
                dist += if (i == wakePoints.size - 1) {
                    pixelToNm(distanceBetween(aircraft2.x, aircraft2.y, wakePoints[i].x, wakePoints[i].y))
                } else {
                    0.5f
                }
                if (!withinRange(aircraft.altitude - wakePoints[i].altitude, -950f, 50f)) continue  //If altitude difference between wake point and aircraft does not fulfill 0 to less than 1000feet, no conflict, continue
                val distBetPoint = pixelToNm(distanceBetween(aircraft.x, aircraft.y, wakePoints[i].x, wakePoints[i].y))
                if (distBetPoint > 0.4f) continue  //If distance between the current point and aircraft is more than 0.4nm, no conflict, continue
                if (dist + distBetPoint < reqDist - 0.2) return reqDist - dist - distBetPoint //If cumulative distance + point distance is less than required distance, conflict (0.2nm leeway)
            }
        }
        return (-1).toFloat()
    }

    /** Renders the wake lines when aircraft is selected, call in shape renderer render method only  */
    fun renderWake(aircraft: Aircraft) {
        if (!aircraftWakes.containsKey(aircraft.callsign)) return
        var prevX: Float = aircraft.radarX
        var prevY: Float = aircraft.radarY
        radarScreen.shapeRenderer.color = Color.ORANGE
        val points = aircraftWakes[aircraft.callsign] ?: return
        for (i in points.size - 2 downTo 0) {
            radarScreen.shapeRenderer.line(prevX, prevY, points[i].x, points[i].y)
            prevX = points[i].x
            prevY = points[i].y
            if ((points.size - 1 - i) % 2 == 0) radarScreen.shapeRenderer.circle(prevX, prevY, 8f) //Draw only for 1nm intervals
        }
    }

    /** Draws letter representing separation required for each recat category, call in draw method only  */
    fun drawSepRequired(batch: Batch?, aircraft: Aircraft) {
        if (!aircraftWakes.containsKey(aircraft.callsign)) return
        val points = aircraftWakes[aircraft.callsign] ?: return
        var prevDist = 0
        for (i in 0..5) {
            val reqDist = SeparationMatrix.getWakeSepDist(aircraft.recat, (i + 'A'.toInt()).toChar())
            if (reqDist == 0) continue
            if (reqDist == prevDist) continue
            val index = points.size - 1 - reqDist * 2
            if (index < 0) break
            var thing = (i + 'A'.toInt()).toChar().toString()
            if (i < 5 && SeparationMatrix.getWakeSepDist(aircraft.recat, (i + 1 + 'A'.toInt()).toChar()) == reqDist) thing += "/" + (i + 1 + 'A'.toInt()).toChar()
            val offset = if (thing.length > 1) 20 else 6
            Fonts.defaultFont6?.draw(batch, thing, points[index].x - offset, points[index].y)
            prevDist = reqDist
        }
    }

    /** Renders the wake lines/arc when aircraft is on the ILS with aircraft behind it, call in shape renderer render method only  */
    fun renderIlsWake() {
        for (aircraft in radarScreen.aircrafts.values) {
            if (aircraft is Departure) continue
            if (!aircraft.isLocCap) continue
            if (aircraft.isOnGround) continue
            val ils = aircraft.ils ?: continue
            if (ils is Circling && aircraft is Arrival && aircraft.phase > 0) continue //Don't draw if aircraft is already on visual segment of circling approach
            val rwy = ils.rwy ?: continue
            var aircraft1: Aircraft? = null
            var index: Int = rwy.aircraftOnApp.size - 1
            while (true) {
                if (index < 0) break
                val nextAircraft: Aircraft = rwy.aircraftOnApp.get(index)
                if (nextAircraft.callsign == aircraft.callsign) break
                aircraft1 = rwy.aircraftOnApp.get(index)
                index--
            }
            if (aircraft1 == null) continue
            val reqDist = SeparationMatrix.getWakeSepDist(aircraft.recat, aircraft1.recat)
            if (reqDist < 3) continue
            val centre: Vector2 = ils.getPointAtDist(ils.getDistFrom(aircraft.radarX, aircraft.radarY) + reqDist)
            val halfWidth = if (aircraft.isSelected) 50 else 30
            val trackRad = Math.toRadians(ils.heading - radarScreen.magHdgDev.toDouble())
            val xOffset = halfWidth * cos(trackRad).toFloat()
            val yOffset = halfWidth * sin(trackRad).toFloat()
            radarScreen.shapeRenderer.color = if (aircraft.isSelected) Color.YELLOW else Color.ORANGE
            radarScreen.shapeRenderer.line(centre.x - xOffset, centre.y + yOffset, centre.x + xOffset, centre.y - yOffset)
        }
    }

    /** Returns a jsonobject used to save data for this wake manager  */
    val save: JSONObject
        get() {
            val save = JSONObject()
            for (callsign in aircraftWakes.keys) {
                val array = JSONArray()
                val points = aircraftWakes[callsign] ?: continue
                for (i in 0 until points.size) {
                    val point = JSONObject()
                    val pt = points[i]
                    point.put("x", pt.x.toDouble())
                    point.put("y", pt.y.toDouble())
                    point.put("altitude", pt.altitude)
                    array.put(point)
                }
                save.put(callsign, array)
            }
            return save
        }
}