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
import java.util.*

class Waypoint(private var name: String, val posX: Int, val posY: Int) : Actor() {
    private var restrVisible: Boolean
    private val restrLabel: Label
    val label: Label
    var isSelected = false
    private var flyOver = false
    private val shapeRenderer = TerminalControl.radarScreen!!.shapeRenderer

    init {
        //Set the label
        val labelStyle = LabelStyle()
        labelStyle.font = Fonts.defaultFont6
        labelStyle.fontColor = Color.GRAY
        label = Label(name, labelStyle)
        label.setPosition(posX - label.width / 2, posY + 16.toFloat())
        label.setAlignment(Align.bottom)

        //Set restriction label
        val labelStyle1 = LabelStyle()
        labelStyle1.font = Fonts.defaultFont6
        labelStyle1.fontColor = Color.GRAY
        restrLabel = Label("This should not be visible", labelStyle1)
        restrLabel.setPosition(posX - restrLabel.width / 2, posY + 48.toFloat())
        restrLabel.setAlignment(Align.bottom)
        restrVisible = false
        adjustPositions()
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        if (isSelected && isInsideRadar) {
            label.draw(batch, 1f)
            if (restrVisible) restrLabel.draw(batch, 1f)
        }
    }

    /** Moves the restriction information labels for certain waypoints to reduce clutter  */
    private fun adjustPositions() {
        loadData()
        if ("ITRF14.1" == name) {
            label.moveBy(-80f, -16f)
            restrLabel.moveBy(-80f, -16f)
            return
        }
        val icao = TerminalControl.radarScreen!!.mainName
        if (WaypointShifter.movementData.containsKey(icao) && WaypointShifter.movementData[icao]!!.containsKey(name)) {
            val shiftData = WaypointShifter.movementData[icao]!![name]
            restrLabel.moveBy(shiftData!![0].toFloat(), shiftData[1].toFloat())
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
        for (aircraft in TerminalControl.radarScreen!!.aircrafts.values) {
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
        val selectedAircraft = TerminalControl.radarScreen!!.getSelectedAircraft()
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

    companion object {
        @JvmField
        val flyOverPts = HashMap<String, Boolean>()
    }
}