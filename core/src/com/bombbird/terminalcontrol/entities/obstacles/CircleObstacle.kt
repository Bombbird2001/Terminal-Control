package com.bombbird.terminalcontrol.entities.obstacles

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.bombbird.terminalcontrol.utilities.Fonts

class CircleObstacle(toParse: String) : Obstacle() {
    private lateinit var circle: Circle

    init {
        parseData(toParse)
    }

    /** Parses input string into relevant information  */
    private fun parseData(toParse: String) {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.GRAY
        val restInfo = toParse.split(", ".toRegex()).toTypedArray()
        var centreX = 0f
        var centreY = 0f
        var radius = 0f
        for ((index, s1) in restInfo.withIndex()) {
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
                4 -> centreX = s1.toFloat()
                5 -> centreY = s1.toFloat()
                6 -> radius = s1.toFloat()
                else -> Gdx.app.log("Load error", "Unexpected additional parameter in game/" + radarScreen.mainName + "/restricted.rest")
            }
        }
        setPosition(centreX - radius, centreY - radius)
        setSize(radius * 2, radius * 2)
        circle = Circle(centreX, centreY, radius)
    }

    /** Renders the circle for restricted areas on screen  */
    override fun renderShape() {
        shapeRenderer.end()
        shapeRenderer.begin(if ("TCWS" == radarScreen.mainName && minAlt == 30000) ShapeRenderer.ShapeType.Filled else ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color.BLACK
        shapeRenderer.circle(circle.x, circle.y, circle.radius)
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        if (isConflict) {
            shapeRenderer.color = Color.RED
        } else if (!isEnforced) {
            shapeRenderer.color = Color.GRAY
        } else {
            shapeRenderer.color = Color.ORANGE
        }
        shapeRenderer.circle(circle.x, circle.y, circle.radius)
    }

    /** Checks if input aircraft is in the circle  */
    override fun isIn(x: Float, y: Float): Boolean {
        return circle.contains(x, y)
    }
}