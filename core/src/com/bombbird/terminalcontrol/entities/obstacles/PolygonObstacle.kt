package com.bombbird.terminalcontrol.entities.obstacles

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts
import java.util.*

class PolygonObstacle(toParse: String) : Obstacle() {
    private lateinit var polygon: Polygon

    init {
        parseInfo(toParse)
    }

    /** Parses the input string into relevant data  */
    private fun parseInfo(toParse: String) {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.GRAY
        val obsInfo = toParse.split(", ".toRegex()).toTypedArray()
        val vertices = ArrayList<Float>()
        for ((index, s1) in obsInfo.withIndex()) {
            when (index) {
                0 -> minAlt = s1.toInt()
                1 -> if (s1.isNotEmpty() && s1[s1.length - 1] == 'C') {
                    isEnforced = true
                    label = Label(s1.substring(0, s1.length - 1), labelStyle)
                } else {
                    label = Label(s1, labelStyle)
                }
                2 -> label.x = s1.toInt().toFloat()
                3 -> label.y = s1.toInt().toFloat()
                else -> vertices.add(s1.toFloat())
            }
        }
        var i = 0
        val verts = FloatArray(vertices.size)
        for (f in vertices) {
            verts[i++] = f
        }
        polygon = Polygon(verts)
    }

    /** Renders the polygon of obstacle to screen  */
    override fun renderShape() {
        if (isConflict || label.text.toString()[0] == '#') {
            shapeRenderer.color = Color.RED
        } else if (!isEnforced) {
            shapeRenderer.color = Color.GRAY
        } else {
            shapeRenderer.color = Color.ORANGE
        }
        shapeRenderer.polygon(polygon.vertices)
    }

    /** Checks whether the input aircraft is inside the polygon area  */
    override fun isIn(x: Float, y: Float): Boolean {
        return polygon.contains(x, y)
    }
}