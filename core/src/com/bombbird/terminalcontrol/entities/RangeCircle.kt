package com.bombbird.terminalcontrol.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel

class RangeCircle(private val range: Int) : Actor() {
    companion object {
        val DARK_GREEN = Color(0x005720ff)
    }

    private val labelUp: Label
    private val labelDown: Label
    private val shapeRenderer = TerminalControl.radarScreen!!.shapeRenderer

    init {
        val yOffset = nmToPixel(range.toFloat()).toInt()
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont6
        labelStyle.fontColor = DARK_GREEN
        labelUp = Label(range.toString() + "nm", labelStyle)
        val xOffset = (labelUp.width / 2).toInt()
        labelUp.setPosition(5760 / 2f - xOffset, 3240 / 2f + yOffset)
        labelDown = Label(range.toString() + "nm", labelStyle)
        labelDown.setPosition(5760 / 2f - xOffset, 3240 / 2f - yOffset - 30)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        labelUp.draw(batch, 1f)
        labelDown.draw(batch, 1f)
    }

    fun renderShape() {
        shapeRenderer.color = DARK_GREEN
        shapeRenderer.circle(2880f, 1620f, range / 10f * 324, 60)
    }
}