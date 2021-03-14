package com.bombbird.terminalcontrol.entities.waypoints

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.waypoints.WaypointShifter.loadData
import com.bombbird.terminalcontrol.utilities.Fonts
import kotlin.math.round

class Waypoint(private var name: String, val posX: Int, val posY: Int) : Actor() {
    private var restrVisible: Boolean
    private val restrLabel: Label
    private val nameLabel: Label
    var distToGo = -1f
        set(value) {
            if (round(field * 10) / 10f != round(value * 10) / 10f) {
                //Update label if distToGo has changed by 0.1 or more
                distToGoLabel.setText(if (value >= 0) (round(value * 10) / 10f).toString() else "")
                distToGoLabel.x = posX - distToGoLabel.width / 2
            }
            field = value
        }
    var distToGoVisible = false
    private var distToGoLabel: Label
    var isSelected = false
    private var flyOver = false
    private val radarScreen = TerminalControl.radarScreen!!
    private val shapeRenderer = radarScreen.shapeRenderer

    init {
        //Set the label
        val labelStyle = LabelStyle()
        labelStyle.font = Fonts.defaultFont6
        labelStyle.fontColor = Color.GRAY
        nameLabel = Label(name, labelStyle)
        nameLabel.setPosition(posX - nameLabel.width / 2, posY + 16f)
        nameLabel.setAlignment(Align.bottom)

        //Set restriction label
        restrLabel = Label("This should not be visible", labelStyle)
        restrLabel.setPosition(posX - restrLabel.width / 2, posY + 48f)
        restrLabel.setAlignment(Align.bottom)
        restrVisible = false

        //Set dist to go label
        distToGoLabel = Label("", labelStyle)
        distToGoLabel.setPosition(posX.toFloat(), posY - 44f)
        distToGoLabel.setAlignment(Align.bottom)

        adjustPositions()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (isSelected && isInsideRadar) {
            nameLabel.draw(batch, 1f)
            if (restrVisible) {
                restrLabel.draw(batch, 1f)
            }
            if (distToGoVisible && radarScreen.selectedAircraft?.eligibleDisplayDistToGo() == true) {
                distToGoLabel.draw(batch, 1f)
            }
        }
    }

    /** Moves the restriction information labels for certain waypoints to reduce clutter  */
    private fun adjustPositions() {
        loadData()
        if ("ITRF14.1" == name) {
            nameLabel.moveBy(-80f, -16f)
            restrLabel.moveBy(-80f, -16f)
            distToGoLabel.moveBy(-80f, -16f)
            return
        }
        val icao = radarScreen.mainName
        if (WaypointShifter.movementData.containsKey(icao) && WaypointShifter.movementData[icao]?.containsKey(name) == true) {
            val shiftData = WaypointShifter.movementData[icao]?.get(name) ?: return
            restrLabel.moveBy(shiftData[0].toFloat(), shiftData[1].toFloat())
        }
    }

    fun renderShape() {
        if (isSelected && isInsideRadar) {
            shapeRenderer.color = Color.GRAY
            shapeRenderer.circle(posX.toFloat(), posY.toFloat(), 12f, 10)
        }
    }

    /** Called to update whether this waypoint is considered as a fly-over waypoint for any aircraft (fully filled circle)  */
    fun updateFlyOverStatus() {
        //Flyover is true if at least one aircraft has it as a direct flyover
        flyOver = false
        for (aircraft in radarScreen.aircrafts.values) {
            val direct = aircraft.navState.clearedDirect.first()
            if (direct != null && direct.getName() == name && aircraft.route.getWptFlyOver(name)) {
                flyOver = true
                break
            }
        }
    }

    /** Used to set flyOver to true when creating new departures since the Departure would not have been constructed and added to the HashMap of aircrafts  */
    fun setDepFlyOver() {
        flyOver = true
    }

    /** Checks whether the waypoint is marked as a flyover waypoint for correct rendering  */
    fun isFlyOver(): Boolean {
        val selectedAircraft = radarScreen.selectedAircraft
        return if (selectedAircraft != null && selectedAircraft.remainingWaypoints.contains(this, true)) {
            //If there is aircraft selected, and remaining waypoints contains this waypoint, return whether this waypoint is flyover
            selectedAircraft.route.getWptFlyOver(name)
        } else {
            //Otherwise, just use the flyOver waypoint which should be updated earlier
            flyOver
        }
    }

    /** Displays the input speed/altitude restrictions above waypoint name  */
    fun setRestrDisplay(maxSpeed: Int, minAlt: Int, maxAlt: Int) {
        if (maxSpeed == -1 && minAlt == -1 && maxAlt == -1) {
            restrVisible = false
            return
        }
        var restrStr = ""
        if (minAlt == maxAlt && minAlt > -1) {
            restrStr = (minAlt / 100).toString()
        } else {
            if (minAlt > -1) {
                restrStr = "A" + minAlt / 100
            }
            if (maxAlt > -1) {
                if (restrStr.isNotEmpty()) restrStr += " "
                restrStr += "B" + maxAlt / 100
            }
        }
        if (maxSpeed > -1) {
            restrStr = if (restrStr.isNotEmpty()) {
                           """
                ${maxSpeed}kts
                $restrStr
                """.trimIndent()
            } else {
                maxSpeed.toString() + "kts"
            }
        }
        restrLabel.setText(restrStr)
        restrVisible = true
    }

    /** Returns whether the waypoint is inside the radar screen coordinates  */
    val isInsideRadar: Boolean
        get() = posX in 1260..4500 && posY in 0..3240

    override fun getName(): String {
        return name
    }

    override fun setName(name: String) {
        this.name = name
    }
}