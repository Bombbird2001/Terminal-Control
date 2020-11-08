package com.bombbird.terminalcontrol.entities.separation.trajectory

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.utilities.math.MathTools.getRequiredTrack
import com.bombbird.terminalcontrol.utilities.math.MathTools.modulateHeading
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import kotlin.math.*

class Trajectory(private val aircraft: Aircraft) {
    companion object {
        const val INTERVAL = 5
    }

    private var deltaHeading = 0f
    val positionPoints: Array<PositionPoint> = Array()
    val radarScreen = TerminalControl.radarScreen!!

    /** Calculates trajectory of aircraft, adds points to array  */
    fun calculateTrajectory() {
        //Calculate simple linear trajectory, plus arc if aircraft is turning > 5 degrees
        var requiredTime = radarScreen.areaWarning.coerceAtLeast(radarScreen.collisionWarning)
        requiredTime = requiredTime.coerceAtLeast(radarScreen.advTraj)
        positionPoints.clear()
        val targetHeading = aircraft.heading.toFloat() + deltaHeading
        var windSpd: Int = aircraft.winds[1]
        val windHdg: Int = aircraft.winds[0]
        if (windHdg == 0) windSpd = 0
        var targetTrack = targetHeading + aircraft.calculateAngleDiff(targetHeading.toDouble(), windHdg + 180, windSpd).toFloat() - radarScreen.magHdgDev
        targetTrack = modulateHeading(targetTrack.toDouble()).toFloat()
        if (abs(deltaHeading) > 5) {
            //Calculate arc if aircraft is turning > 5 degrees
            var turnRate = if (aircraft.ias > 250) 1.5f else 3f //In degrees/second
            val turnRadius: Float = aircraft.gs / 3600 / Math.toRadians(turnRate.toDouble()).toFloat() //In nautical miles - r = v/w - turnRate must be converted to radians/second - GS must be coverted to nm/second
            val centerOffsetAngle = 360 - aircraft.track.toFloat() //In degrees
            val deltaX = nmToPixel(turnRadius) * cos(Math.toRadians(centerOffsetAngle.toDouble())).toFloat() //In px
            val deltaY = nmToPixel(turnRadius) * sin(Math.toRadians(centerOffsetAngle.toDouble())).toFloat() //In px
            val turnCenter = Vector2()
            val centerToCircum = Vector2()
            if (deltaHeading > 0) {
                //Turning right
                turnCenter.x = aircraft.x + deltaX
                turnCenter.y = aircraft.y + deltaY
                centerToCircum.x = -deltaX
                centerToCircum.y = -deltaY
            } else {
                //Turning left
                turnCenter.x = aircraft.x - deltaX
                turnCenter.y = aircraft.y - deltaY
                centerToCircum.x = deltaX
                centerToCircum.y = deltaY
                turnRate = -turnRate
            }
            var remainingAngle = deltaHeading
            var prevPos = Vector2()
            val sidStarMode = (aircraft.navState.containsCode(aircraft.navState.dispLatMode.first(), NavState.SID_STAR, NavState.AFTER_WPT_HDG) || aircraft.navState.dispLatMode.first() == NavState.HOLD_AT && !aircraft.isHolding)
            var prevTargetTrack = targetTrack
            var i = INTERVAL
            while (i <= requiredTime) {
                if (remainingAngle / turnRate > INTERVAL) {
                    remainingAngle -= turnRate * INTERVAL
                    centerToCircum.rotateDeg(-turnRate * INTERVAL)
                    val newVector = Vector2(turnCenter)
                    prevPos = newVector.add(centerToCircum)
                    if (sidStarMode && aircraft.direct != null) {
                        //Do additional turn checking
                        aircraft.direct?.let {
                            val newTrack = modulateHeading(getRequiredTrack(prevPos.x, prevPos.y, it.posX.toFloat(), it.posY.toFloat()).toDouble()).toFloat()
                            remainingAngle += newTrack - prevTargetTrack //Add the difference in target track to remaining angle
                            if (newTrack < 16 && newTrack > 0 && prevTargetTrack <= 360 && prevTargetTrack > 344) remainingAngle += 360f //In case new track rotates right past 360 hdg
                            if (newTrack <= 360 && newTrack > 344 && prevTargetTrack < 16 && newTrack > 0) remainingAngle -= 360f //In case new track rotates left past 360 hdg
                            prevTargetTrack = newTrack
                        }
                    }
                } else {
                    val remainingTime = INTERVAL - remainingAngle / turnRate
                    centerToCircum.rotateDeg(-remainingAngle)
                    val newVector = Vector2(turnCenter)
                    if (abs(remainingAngle) > 0.1) prevPos = newVector.add(centerToCircum)
                    remainingAngle = 0f
                    val straightVector = Vector2(0f, nmToPixel(remainingTime * aircraft.gs / 3600))
                    straightVector.rotateDeg(-prevTargetTrack)
                    prevPos.add(straightVector)
                }
                positionPoints.add(PositionPoint(aircraft, prevPos.x, prevPos.y, 0))
                i += INTERVAL
            }
        } else {
            var i = INTERVAL
            while (i <= requiredTime) {
                val trackVector = Vector2(0f, nmToPixel(i * aircraft.gs / 3600))
                trackVector.rotateDeg(if (aircraft.isOnGround) -(aircraft.runway?.heading ?: 0) + radarScreen.magHdgDev else -targetTrack)
                positionPoints.add(PositionPoint(aircraft, aircraft.x + trackVector.x, aircraft.y + trackVector.y, 0))
                i += INTERVAL
            }
        }
        var index = 1
        for (positionPoint in positionPoints) {
            val time = index * INTERVAL //Time from now in seconds
            var targetAlt: Float = aircraft.targetAltitude.toFloat()
            if (aircraft.isGsCap) targetAlt = -100f
            if (aircraft.altitude > targetAlt) {
                //Descending
                positionPoint.altitude = max(aircraft.altitude + aircraft.effectiveVertSpd[0] * time / 60, targetAlt).toInt()
            } else {
                //Climbing
                positionPoint.altitude = min(aircraft.altitude + aircraft.effectiveVertSpd[1] * time / 60, targetAlt).toInt()
            }
            index++
        }
    }

    fun renderPoints() {
        if (!aircraft.isSelected) return
        radarScreen.shapeRenderer.color = Color.ORANGE
        for (i in 0 until radarScreen.advTraj / 5) {
            if (i >= positionPoints.size) break
            val positionPoint = positionPoints[i]
            radarScreen.shapeRenderer.circle(positionPoint.x, positionPoint.y, 5f)
        }
    }

    fun setDeltaHeading(deltaHeading: Float) {
        this.deltaHeading = deltaHeading
    }
}