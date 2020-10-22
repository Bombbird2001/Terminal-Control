package com.bombbird.terminalcontrol.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.math.MathTools.withinRange
import com.bombbird.terminalcontrol.utilities.saving.FileLoader

object Shoreline {
    private val radarScreen = TerminalControl.radarScreen!!
    private var landmasses = Array<Array<Int>>()

    fun loadShoreline() {
        landmasses = FileLoader.loadShoreline()
    }

    fun renderShape() {
        for (i in 0 until landmasses.size) {
            var j = 2
            while (j < landmasses[i].size) {
                val prevX = landmasses[i][j - 2]
                val prevY = landmasses[i][j - 1]
                val thisX = landmasses[i][j]
                val thisY = landmasses[i][j + 1]
                if ((!withinRange(prevX, 1260, 4500) || !withinRange(prevY, 0, 3240)) && (!withinRange(thisX, 1260, 4500) || !withinRange(thisY, 0, 3240))) {
                    //Both points not inside range, don't draw line
                    j += 2
                    continue
                }
                //Draw lines to connect points
                radarScreen.shapeRenderer.color = Color.BROWN
                radarScreen.shapeRenderer.line(prevX.toFloat(), prevY.toFloat(), thisX.toFloat(), thisY.toFloat())
                j += 2
            }
        }
    }
}