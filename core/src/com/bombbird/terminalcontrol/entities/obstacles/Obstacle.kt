package com.bombbird.terminalcontrol.entities.obstacles

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft

open class Obstacle : Actor() {
    var minAlt = 0
    lateinit var label: Label
    var isConflict = false
    var isEnforced = false
    val radarScreen = TerminalControl.radarScreen!!
    val shapeRenderer = radarScreen.shapeRenderer

    /** Draws the label to screen  */
    override fun draw(batch: Batch, parentAlpha: Float) {
        if (!radarScreen.showMva) return
        if (isConflict || label.text.toString()[0] == '#') {
            label.style.fontColor = Color.RED
        } else if (!isEnforced) {
            label.style.fontColor = Color.GRAY
        } else {
            label.style.fontColor = Color.ORANGE
        }
        label.draw(batch, 1f)
    }

    /** Renders the obstacle with shape renderer, overridden  */
    open fun renderShape() {
        //No default implementation
    }

    /** Checks if input aircraft is in the obstacle  */
    fun isIn(aircraft: Aircraft): Boolean {
        return isIn(aircraft.x, aircraft.y)
    }

    /** Checks if input coordinates is in the obstacle  */
    open fun isIn(x: Float, y: Float): Boolean {
        //No default implementation
        return false
    }
}