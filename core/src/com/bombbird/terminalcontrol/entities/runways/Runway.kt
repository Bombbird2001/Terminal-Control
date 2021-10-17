package com.bombbird.terminalcontrol.entities.runways

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.approaches.Approach
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.feetToPixel

class Runway(toParse: String) {
    companion object {
        //Set dimensions
        private const val halfWidth = 2f
    }

    //Name of runway
    lateinit var name: String

    //The opposite runway
    lateinit var oppRwy: Runway

    //The previous go around
    var goAround: Aircraft? = null

    //Set landing/takeoff status
    var isLanding = false
        private set
    var isTakeoff = false
        private set

    //Position of bottom center of runway
    var x = 0f
    var y = 0f
    var elevation = 0

    //Initial climb altitude for aircraft
    var initClimb = 0
        private set
    var feetLength = 0
        private set
    private var pxLength = 0f

    //Set heading of runway
    var heading = 0
    var trueHdg = 0f
        private set

    //Label of runway
    lateinit var label: Label
        private set

    //Position offsets not dependent on zoom
    private val xOffsetL: Float
    private val yOffsetL: Float

    //Set polygon to render later
    private val polygon: Polygon

    //Set the ILS
    lateinit var apch: Approach

    //Whether storm is in departure path
    var isStormInPath = false

    //Array of aircraft on approach
    var aircraftOnApp: Array<Aircraft>

    //Whether emergency aircraft is staying on it
    var isEmergencyClosed: Boolean
    private val radarScreen: RadarScreen = TerminalControl.radarScreen!!
    private val shapeRenderer: ShapeRenderer = radarScreen.shapeRenderer

    init {
        parseInfo(toParse)
        aircraftOnApp = Array()
        isEmergencyClosed = false

        //Calculate the position offsets that are not dependent on zoom
        xOffsetL = pxLength * MathUtils.cosDeg(90 - trueHdg)
        yOffsetL = pxLength * MathUtils.sinDeg(90 - trueHdg)

        //Create polygon
        polygon = Polygon()
    }

    /** Parses the input string into relevant data for the runway  */
    private fun parseInfo(toParse: String) {
        val labelStyle = LabelStyle()
        labelStyle.font = Fonts.defaultFont8
        labelStyle.fontColor = radarScreen.defaultColour
        val rwyInfo = toParse.split(",".toRegex()).toTypedArray()
        var labelX = 0f
        var labelY = 0f
        for ((index, s1) in rwyInfo.withIndex()) {
            when (index) {
                0 -> {
                    name = s1
                    label = Label(name, labelStyle)
                }
                1 -> x = s1.toFloat()
                2 -> y = s1.toFloat()
                3 -> {
                    feetLength = s1.toInt()
                    pxLength = feetToPixel(feetLength.toFloat())
                }
                4 -> {
                    heading = s1.toInt()
                    trueHdg = heading - radarScreen.magHdgDev
                }
                5 -> labelX = s1.toFloat()
                6 -> labelY = s1.toFloat()
                7 -> elevation = s1.toInt()
                8 -> initClimb = s1.toInt()
                else -> Gdx.app.log("Load error", "Unexpected additional parameter in data for runway $name")
            }
        }
        Gdx.app.postRunnable {
            label = Label(name, labelStyle)
            label.setPosition(labelX, labelY)
            radarScreen.stage.addActor(label)
            label.isVisible = false
        }
    }

    /** Sets runway status for landing, takeoffs  */
    fun setActive(landing: Boolean, takeoff: Boolean) {
        isLanding = landing
        isTakeoff = takeoff
        Gdx.app.postRunnable { label.isVisible = landing || takeoff }
    }

    /** Renders the runway rectangle  */
    fun renderShape() {
        //Calculate the position offsets depending on zoom
        val zoom = radarScreen.camera.zoom
        val xOffsetW = halfWidth * zoom * MathUtils.sinDeg(90 - trueHdg)
        val yOffsetW = -halfWidth * zoom * MathUtils.cosDeg(90 - trueHdg)
        polygon.vertices = floatArrayOf(x - xOffsetW, y - yOffsetW, x - xOffsetW + xOffsetL, y - yOffsetW + yOffsetL, x + xOffsetL + xOffsetW, y + yOffsetL + yOffsetW, x + xOffsetW, y + yOffsetW)
        if (isLanding || isTakeoff) {
            shapeRenderer.color = radarScreen.defaultColour
            shapeRenderer.polygon(polygon.vertices)
        } else if (!oppRwy.isTakeoff && !oppRwy.isLanding) {
            shapeRenderer.color = Color.DARK_GRAY
            shapeRenderer.polygon(polygon.vertices)
        }
    }

    /** Called to remove aircraft from the array of aircraft on approach, should be called during go arounds/cancelling approaches  */
    fun removeFromArray(aircraft: Aircraft) {
        aircraftOnApp.removeValue(aircraft, false)
    }

    /** Called to add the aircraft to the array, automatically determines the position of the aircraft in the array, should be called during initial aircraft LOC capture  */
    fun addToArray(aircraft: Aircraft) {
        aircraftOnApp.add(aircraft)
        if (aircraftOnApp.size > 1) {
            var thisIndex = aircraftOnApp.size - 1
            while (thisIndex > 0 && distanceBetween(aircraft.x, aircraft.y, x, y) < distanceBetween(aircraftOnApp[thisIndex - 1].x, aircraftOnApp[thisIndex - 1].y, x, y) && !aircraftOnApp[thisIndex - 1].isOnGround) {
                aircraftOnApp.swap(thisIndex - 1, thisIndex)
                thisIndex -= 1
            }
        }
    }

    /** Called during an (unlikely) event that for some reason the current aircraft overtakes the aircraft in front of it  */
    fun swapAircraft(aircraft: Aircraft) {
        val thisIndex = aircraftOnApp.indexOf(aircraft, false)
        aircraftOnApp.swap(thisIndex, thisIndex - 1)
    }

    /** Changes the color of the runway label  */
    fun setLabelColor(color: Color?) {
        label.style.fontColor = color
    }

    /** Returns whether opposite runway has been set */
    fun isOppRwySet(): Boolean {
        return this::oppRwy.isInitialized
    }

    val position: FloatArray
        get() = floatArrayOf(x, y)
}