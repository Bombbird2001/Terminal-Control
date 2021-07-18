package com.bombbird.terminalcontrol.ui.datatag

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.Timer
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.entities.approaches.Circling
import com.bombbird.terminalcontrol.entities.approaches.RNP
import com.bombbird.terminalcontrol.ui.tabs.Tab
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.math.MathTools.getRequiredTrack
import com.bombbird.terminalcontrol.utilities.math.MathTools.pointsAtBorder
import com.bombbird.terminalcontrol.utilities.math.MathTools.withinRange

class DataTag(aircraft: Aircraft) {
    companion object {
        //Rendering parameters
        private val BUTTON_STYLE_CTRL = ImageButton.ImageButtonStyle()
        private val BUTTON_STYLE_DEPT = ImageButton.ImageButtonStyle()
        private val BUTTON_STYLE_UNCTRL = ImageButton.ImageButtonStyle()
        private val BUTTON_STYLE_ENROUTE = ImageButton.ImageButtonStyle()
        lateinit var DRAWABLE_GREEN: NinePatchDrawable
        lateinit var DRAWABLE_BLUE: NinePatchDrawable
        lateinit var DRAWABLE_ORANGE: NinePatchDrawable
        lateinit var DRAWABLE_RED: NinePatchDrawable
        lateinit var DRAWABLE_MAGENTA: NinePatchDrawable
        var LOADED_ICONS = false
        private val tapTimer = Timer()
        private val flashTimer = Timer()

        //Datatag value keys
        const val CALLSIGN = "Callsign"
        const val CALLSIGN_RECAT = "Callsign + Wake"
        const val ICAO_TYPE = "Aircraft type"
        const val ICAO_TYPE_WAKE = "Aircraft type + Wake"
        const val ALTITUDE_FULL = "Full altitude info"
        const val ALTITUDE = "Current altitude"
        const val CLEARED_ALT = "Cleared altitude"
        const val HEADING = "Heading"
        const val LAT_CLEARED = "Cleared waypoint/heading"
        const val SIDSTAR_CLEARED = "Cleared SID/STAR/Approach"
        const val GROUND_SPEED = "Ground speed"
        const val CLEARED_IAS = "Cleared speed"
        const val AIRPORT = "Airport"

        /** Pauses all timers  */
        fun pauseTimers() {
            tapTimer.stop()
            flashTimer.stop()
        }

        /** Resumes all timers  */
        fun startTimers() {
            tapTimer.start()
            flashTimer.start()
        }

        /** Resets the border, background for all data tags  */
        fun setBorderBackground() {
            val radarScreen = TerminalControl.radarScreen ?: return
            for (aircraft in radarScreen.aircrafts.values) {
                aircraft.dataTag.updateBorderBackgroundVisibility(false)
            }
        }
    }

    private val radarScreen = TerminalControl.radarScreen!!
    private val aircraft: Aircraft
    private val label: Label
    private val labelFields: HashMap<String, String>
    private val icon: ImageButton
    private val labelButton: Button
    private val clickSpot: Button
    private var dragging: Boolean
    private var flashing: Boolean
    private var tapCount = 0
    var isMinimized = false
    val trailDots: Queue<Image>

    init {
        loadResources()
        this.aircraft = aircraft
        trailDots = Queue()
        dragging = false
        flashing = false
        icon = ImageButton(BUTTON_STYLE_UNCTRL)
        icon.setSize(20f, 20f)
        icon.imageCell.size(20f, 20f)
        radarScreen.stage.addActor(icon)
        labelFields = HashMap()
        labelFields[CALLSIGN] = aircraft.callsign
        labelFields[CALLSIGN_RECAT] = "${aircraft.callsign}/${aircraft.recat}"
        labelFields[ICAO_TYPE] = aircraft.icaoType
        labelFields[ICAO_TYPE_WAKE] = "${aircraft.icaoType}/${aircraft.wakeCat}/${aircraft.recat}"
        labelFields[AIRPORT] = aircraft.airport.icao
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont6
        if (radarScreen.lineSpacingValue == 0) {
            labelStyle.font = Fonts.compressedFont6
        } else if (radarScreen.lineSpacingValue == 2) {
            labelStyle.font = Fonts.expandedFont6
        }
        labelStyle.fontColor = Color.WHITE
        label = Label("Loading...", labelStyle)
        label.setPosition(aircraft.x - label.width / 2, aircraft.y + 25)
        labelButton = Button(TerminalControl.skin.getDrawable("FillerImage"), TerminalControl.skin.getDrawable("FillerImage"))
        labelButton.setSize(label.width + 10, label.height)
        val clickSpotStyle = Button.ButtonStyle(null, null, null)
        clickSpot = Button(clickSpotStyle)
        clickSpot.setSize(labelButton.width, labelButton.height)
        clickSpot.name = aircraft.callsign
        clickSpot.addListener(object : DragListener() {
            override fun drag(event: InputEvent, x: Float, y: Float, pointer: Int) {
                label.moveBy(x - labelButton.width / 2, y - labelButton.height / 2)
                dragging = true
                event.handle()
            }
        })
        clickSpot.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (!dragging) {
                    if (radarScreen.tutorialManager == null || radarScreen.tutorialManager?.isPausedForReading == false) {
                        radarScreen.setSelectedAircraft(radarScreen.aircrafts[actor.name])
                        radarScreen.addToEasterEggQueue(aircraft)
                        aircraft.isSilenced = true
                    }
                    tapCount++
                    if (tapCount >= 2) {
                        if (aircraft.isArrivalDeparture) isMinimized = !isMinimized
                        tapCount = 0
                        tapTimer.clear()
                    }
                    tapTimer.scheduleTask(object : Timer.Task() {
                        override fun run() {
                            tapCount = 0
                        }
                    }, 0.2f)
                } else {
                    dragging = false
                }
            }
        })
        updateBorderBackgroundVisibility(false)
        val labelStage = radarScreen.labelStage
        labelStage.addActor(labelButton)
        labelStage.addActor(label)
        labelStage.addActor(clickSpot)
    }

    /** Loads label, icon resources  */
    private fun loadResources() {
        if (!LOADED_ICONS) {
            BUTTON_STYLE_CTRL.imageUp = TerminalControl.skin.getDrawable("aircraftControlled")
            BUTTON_STYLE_CTRL.imageDown = TerminalControl.skin.getDrawable("aircraftControlled")
            BUTTON_STYLE_DEPT.imageUp = TerminalControl.skin.getDrawable("aircraftDeparture")
            BUTTON_STYLE_DEPT.imageDown = TerminalControl.skin.getDrawable("aircraftDeparture")
            BUTTON_STYLE_UNCTRL.imageUp = TerminalControl.skin.getDrawable("aircraftNotControlled")
            BUTTON_STYLE_UNCTRL.imageDown = TerminalControl.skin.getDrawable("aircraftNotControlled")
            BUTTON_STYLE_ENROUTE.imageUp = TerminalControl.skin.getDrawable("aircraftEnroute")
            BUTTON_STYLE_ENROUTE.imageDown = TerminalControl.skin.getDrawable("aircraftEnroute")
            val labelPatchGreen = NinePatch(TerminalControl.skin.getPatch("labelBorderGreen"))
            val labelPatchBlue = NinePatch(TerminalControl.skin.getPatch("labelBorderBlue"))
            val labelPatchOrange = NinePatch(TerminalControl.skin.getPatch("labelBorderOrange"))
            val labelPatchRed = NinePatch(TerminalControl.skin.getPatch("labelBorderRed"))
            val labelPatchMagenta = NinePatch(TerminalControl.skin.getPatch("labelBorderMagenta"))
            DRAWABLE_GREEN = NinePatchDrawable(labelPatchGreen)
            DRAWABLE_BLUE = NinePatchDrawable(labelPatchBlue)
            DRAWABLE_ORANGE = NinePatchDrawable(labelPatchOrange)
            DRAWABLE_RED = NinePatchDrawable(labelPatchRed)
            DRAWABLE_MAGENTA = NinePatchDrawable(labelPatchMagenta)
            LOADED_ICONS = true
        }
    }

    /** Renders the line joining label and aircraft icon  */
    fun renderShape() {
        //Don't need to draw if aircraft blip is inside box
        val offset = 5
        if (withinRange(aircraft.radarX, label.x - offset, label.x + label.width + offset) && withinRange(aircraft.radarY, label.y, label.y + label.height)) return
        var startX = label.x + label.width / 2
        var startY = label.y + label.height / 2
        if (!labelButton.isVisible) {
            val degree = getRequiredTrack(startX, startY, aircraft.radarX, aircraft.radarY)
            val results = pointsAtBorder(floatArrayOf(label.x - offset, label.x + label.width + offset), floatArrayOf(label.y, label.y + label.height), startX, startY, degree)
            startX = results[0]
            startY = results[1]
        }
        radarScreen.shapeRenderer.line(startX, startY, aircraft.radarX, aircraft.radarY)
    }

    /** Updates the position, draws of the icon  */
    fun updateIcon(batch: Batch?) {
        icon.setPosition(aircraft.radarX - 10, aircraft.radarY - 10)
        icon.color = Color.BLACK //Icon doesn't draw without this for some reason
        icon.draw(batch, 1f)
    }

    /** Updates the icon colour depending on aircraft control state  */
    fun updateIconColors(controlState: Aircraft.ControlState) {
        when (controlState) {
            Aircraft.ControlState.ENROUTE -> icon.style = BUTTON_STYLE_ENROUTE
            Aircraft.ControlState.UNCONTROLLED -> icon.style = BUTTON_STYLE_UNCTRL
            Aircraft.ControlState.ARRIVAL -> icon.style = BUTTON_STYLE_CTRL
            Aircraft.ControlState.DEPARTURE -> icon.style = BUTTON_STYLE_DEPT
        }
    }

    /** Sets the ninePatchDrawable into all aspects of the clickSpot style  */
    private fun setAllNinepatch(ninePatchDrawable: NinePatchDrawable?) {
        clickSpot.style.up = ninePatchDrawable
        clickSpot.style.down = ninePatchDrawable
        clickSpot.style.checked = ninePatchDrawable
    }

    /** Sets this data tag as the last to draw - will always be shown above other data tags  */
    private fun setAsDrawLast() {
        Gdx.app.postRunnable {
            labelButton.remove()
            label.remove()
            clickSpot.remove()
            radarScreen.labelStage.addActor(labelButton)
            radarScreen.labelStage.addActor(label)
            radarScreen.labelStage.addActor(clickSpot)
        }
    }

    /** Updates whether the default green/blue border should be visible  */
    fun updateBorderBackgroundVisibility(visible: Boolean) {
        if (visible) {
            setAsDrawLast()
        }
        if (aircraft.hasEmergency()) {
            labelButton.isVisible = visible || radarScreen.alwaysShowBordersBackground
            setEmergency()
            return
        }
        //If always show background
        if (radarScreen.alwaysShowBordersBackground) {
            labelButton.isVisible = true
            if (!flashing) {
                var ninePatchDrawable: NinePatchDrawable? = null
                if (aircraft is Departure) {
                    ninePatchDrawable = DRAWABLE_GREEN
                } else if (aircraft is Arrival) {
                    ninePatchDrawable = DRAWABLE_BLUE
                }
                setAllNinepatch(ninePatchDrawable)
            }
            return
        }
        //Show background only when selected
        if (visible) {
            if (clickSpot.style.up == null) {
                var ninePatchDrawable: NinePatchDrawable? = null
                if (aircraft is Departure) {
                    ninePatchDrawable = DRAWABLE_GREEN
                } else if (aircraft is Arrival) {
                    ninePatchDrawable = DRAWABLE_BLUE
                }
                setAllNinepatch(ninePatchDrawable)
            }
        } else {
            if (clickSpot.style.up === DRAWABLE_BLUE || clickSpot.style.up === DRAWABLE_GREEN) {
                setAllNinepatch(null)
            }
        }
        labelButton.isVisible = visible
    }

    /** Called to start flashing an aircraft's label borders during initial contact or when a conflict is predicted  */
    fun startFlash() {
        if (flashing) return
        if (aircraft.hasEmergency()) return
        if (aircraft.isTrajectoryConflict || aircraft.isTrajectoryTerrainConflict || aircraft.isActionRequired) {
            flashing = true
            continuousFlashing()
        }
    }

    /** Called by start flashing method and itself to keep flashing the label  */
    private fun continuousFlashing() {
        if (aircraft.isTrajectoryConflict || aircraft.isTrajectoryTerrainConflict) {
            setAllNinepatch(DRAWABLE_MAGENTA)
            flashTimer.scheduleTask(object : Timer.Task() {
                override fun run() {
                    resetFlash()
                }
            }, 1f)
            flashTimer.scheduleTask(object : Timer.Task() {
                override fun run() {
                    continuousFlashing()
                }
            }, 2f)
        } else if (aircraft.isActionRequired) {
            setAllNinepatch(DRAWABLE_ORANGE)
            flashTimer.scheduleTask(object : Timer.Task() {
                override fun run() {
                    resetFlash()
                }
            }, 1f)
            flashTimer.scheduleTask(object : Timer.Task() {
                override fun run() {
                    continuousFlashing()
                }
            }, 2f)
        } else {
            flashing = false
        }
    }

    /** Resets the border to blue/green depending on aircraft type  */
    private fun resetFlash() {
        if (aircraft.hasEmergency()) {
            setEmergency()
            return
        }
        var ninePatchDrawable: NinePatchDrawable? = null
        if (radarScreen.alwaysShowBordersBackground || aircraft.isSelected) {
            if (aircraft is Departure) {
                ninePatchDrawable = DRAWABLE_GREEN
            } else if (aircraft is Arrival) {
                ninePatchDrawable = DRAWABLE_BLUE
            }
        }
        setAllNinepatch(ninePatchDrawable)
    }

    /** Called to change aircraft label to red for emergencies  */
    fun setEmergency() {
        setAllNinepatch(DRAWABLE_RED)
    }

    /** Draws the trail dots for aircraft  */
    fun drawTrailDots(batch: Batch?, parentAlpha: Float) {
        if (radarScreen.pastTrajTime == 0) return
        val size = trailDots.size
        val selectedRequired = radarScreen.pastTrajTime / 10
        for ((index, trail) in trailDots.withIndex()) {
            if (aircraft.isSelected && (radarScreen.pastTrajTime == -1 || size - index <= selectedRequired) || size - index <= 6 && (aircraft.isArrivalDeparture || radarScreen.showUncontrolled)) {
                trail.draw(batch, parentAlpha)
            }
        }
    }

    /** Appends a new image to end of queue for drawing trail dots given a set of coordinates  */
    fun addTrailDot(x: Float, y: Float) {
        val dot: Image
        when (aircraft) {
            is Arrival -> dot = Image(TerminalControl.skin.getDrawable("DotsArrival"))
            is Departure -> dot = Image(TerminalControl.skin.getDrawable("DotsDeparture"))
            else -> {
                dot = Image()
                Gdx.app.log("Trail dot error", "Aircraft not instance of arrival or departure, trail image not found!")
            }
        }
        dot.setPosition(x - dot.width / 2, y - dot.height / 2)
        trailDots.addLast(dot)
    }

    /** Updates the position of the label to prevent it from going out of bounds  */
    fun moderateLabel() {
        if (label.x < 936) {
            label.x = 936f
        } else if (label.x + label.width > 4824) {
            label.x = 4824 - label.width
        }
        if (label.y < 0) {
            label.y = 0f
        } else if (label.y + label.height > 3240) {
            label.y = 3240 - label.height
        }
    }

    /** Updates the font type (compressed/default/expanded)  */
    fun updateLabelStyle(labelStyle: Label.LabelStyle?) {
        label.style = labelStyle
    }

    /** Updates the label on the radar given aircraft's radar data and other data  */
    fun updateLabel() {
        val config = radarScreen.datatagConfig

        val latTab = aircraft.ui.latTab
        val altTab = aircraft.ui.altTab
        val spdTab = aircraft.ui.spdTab

        val vertSpd: String = when {
            aircraft.radarVs < -150 -> " v "
            aircraft.radarVs > 150 -> " ^ "
            else -> " = "
        }

        val clearedVert = if (aircraft.apch is Circling && aircraft is Arrival && aircraft.phase > 0) "VIS" else if (aircraft.isGsCap) {
            if (aircraft.apch?.name?.contains("IMG") == true || (aircraft.apch is Circling && aircraft is Arrival && aircraft.phase > 0) || aircraft.apch is RNP) "VIS"
            else "GS"
        } else if (aircraft.isLocCap && aircraft.apch?.isNpa == true) "NPA" else (aircraft.targetAltitude / 100).toString()

        var exped = if (aircraft.navState.clearedExpedite.last()) " =>> " else " => "
        if (aircraft.isSelected && aircraft.isArrivalDeparture) {
            exped = if (Tab.clearedExpedite) " =>> " else " => "
            if (altTab.isExpediteChanged) exped = "[YELLOW]$exped[WHITE]"
        }

        var clearedAltFull = (aircraft.navState.clearedAlt.last() / 100).toString()
        if (aircraft.isSelected && aircraft.isArrivalDeparture) {
            clearedAltFull = (Tab.clearedAlt / 100).toString()
            if (altTab.isAltChanged) clearedAltFull = "[YELLOW]$clearedAltFull[WHITE]"
        }

        val clearedAlt = if (aircraft.isSelected && aircraft.isArrivalDeparture && altTab.isAltChanged) "[YELLOW]${(Tab.clearedAlt / 100)}[WHITE]"
        else if (config.showOnlyWhenChanged(CLEARED_ALT)) ""
        else (aircraft.navState.clearedAlt.last() / 100).toString()

        labelFields[ALTITUDE] = MathUtils.round(aircraft.radarAlt / 100).toString()
        labelFields[CLEARED_ALT] = clearedAlt
        labelFields[ALTITUDE_FULL] = "${labelFields[ALTITUDE]}$vertSpd$clearedVert$exped$clearedAltFull"

        var heading = MathUtils.round(aircraft.radarHdg.toFloat())
        if (heading == 0) heading = 360
        labelFields[HEADING] = heading.toString()
        labelFields[GROUND_SPEED] = (aircraft.radarGs.toInt()).toString()

        val clearedSpd = if (aircraft.isSelected && aircraft.isArrivalDeparture && spdTab.isSpdChanged) "[YELLOW]" + Tab.clearedSpd + "[WHITE]"
        else if (config.showOnlyWhenChanged(CLEARED_IAS)) ""
        else aircraft.navState.clearedSpd.last().toString()
        labelFields[CLEARED_IAS] = clearedSpd

        var latChanged = false

        var latCleared = if (aircraft.isSelected && aircraft.isArrivalDeparture) {
            if (Tab.latMode == NavState.SID_STAR && (aircraft.isLocCap || Tab.clearedWpt != aircraft.navState.clearedDirect.last()?.name || aircraft.navState.containsCode(aircraft.navState.dispLatMode.last(), NavState.VECTORS, NavState.AFTER_WPT_HDG, NavState.HOLD_AT))) {
                latChanged = latTab.isLatModeChanged || latTab.isWptChanged
                if (aircraft.isLocCap && !latChanged) (if (aircraft.apch is RNP) "RNP" else "LOC") else Tab.clearedWpt ?: ""
            } else if (Tab.latMode == NavState.HOLD_AT) {
                latChanged = latTab.isLatModeChanged || latTab.isHoldWptChanged
                Tab.holdWpt ?: ""
            } else if (Tab.latMode == NavState.AFTER_WPT_HDG) {
                if (aircraft.navState.clearedDirect.last() == aircraft.navState.clearedAftWpt.last() || latTab.isLatModeChanged || latTab.isAfterWptChanged || latTab.isAfterWptHdgChanged) {
                    latChanged = latTab.isLatModeChanged || latTab.isAfterWptChanged || latTab.isAfterWptHdgChanged
                    Tab.afterWpt + "=>" + Tab.afterWptHdg
                } else ""
            } else if (Tab.latMode == NavState.VECTORS) {
                if (aircraft.isLocCap) {
                    if (aircraft.apch is RNP) "RNP" else "LOC"
                } else {
                    latChanged = latTab.isLatModeChanged || latTab.isHdgChanged || latTab.isDirectionChanged
                    Tab.clearedHdg.toString() + when(Tab.turnDir) {
                        NavState.TURN_LEFT -> "L"
                        NavState.TURN_RIGHT -> "R"
                        else -> ""
                    }
                }
            } else ""
        } else {
            if (aircraft.isVectored) {
                if (aircraft.isLocCap) {
                    if (aircraft.apch is RNP) "RNP" else "LOC"
                } else {
                    var hdg = aircraft.navState.clearedHdg.last().toString()
                    val turnDir: Int = aircraft.navState.clearedTurnDir.last()
                    if (turnDir == NavState.TURN_LEFT) {
                        hdg += "L"
                    } else if (turnDir == NavState.TURN_RIGHT) {
                        hdg += "R"
                    }
                    hdg
                }
            } else if (aircraft.navState.dispLatMode.last() == NavState.HOLD_AT) {
                aircraft.holdWpt?.name ?: ""
            } else if (aircraft.navState.clearedDirect.last() != null && aircraft.navState.clearedDirect.last() == aircraft.navState.clearedAftWpt.last() && aircraft.navState.dispLatMode.last() == NavState.AFTER_WPT_HDG) {
                (aircraft.navState.clearedDirect.last()?.name ?: "") + "=>" + aircraft.navState.clearedAftWptHdg.last()
            } else if (aircraft.isLocCap) {
                if (aircraft.apch is RNP) "RNP" else "LOC"
            } else {
                ""
            }
        }
        if (latChanged) latCleared = "[YELLOW]$latCleared[WHITE]"
        else if (config.showOnlyWhenChanged(LAT_CLEARED) && !latChanged && latCleared != "RNP" && latCleared != "LOC") latCleared = ""
        labelFields[LAT_CLEARED] = latCleared

        val sidStarCleared = if (aircraft.isSelected && aircraft.isArrivalDeparture && latTab.isStarChanged) "[YELLOW]${Tab.newStar?.split(" ".toRegex())?.toTypedArray()?.get(0) ?: ""}[WHITE]"
        else if (aircraft.isSelected && aircraft.isArrivalDeparture && latTab.isIlsChanged) "[YELLOW]${Tab.clearedILS}[WHITE]"
        else if (config.showOnlyWhenChanged(SIDSTAR_CLEARED)) ""
        else aircraft.navState.clearedApch.last()?.name ?: aircraft.sidStar.name
        labelFields[SIDSTAR_CLEARED] = sidStarCleared

        var updatedText = config.generateTagText(labelFields, isMinimized || !aircraft.isArrivalDeparture)
        if (aircraft.emergency.isActive) {
            if (aircraft.emergency.isReadyForApproach && aircraft.emergency.isStayOnRwy) {
                updatedText = "$updatedText\nStay on rwy"
            } else if (!aircraft.emergency.isReadyForApproach && aircraft.emergency.isChecklistsSaid && aircraft.emergency.isFuelDumpRequired) {
                updatedText = if (aircraft.emergency.isDumpingFuel) {
                    "$updatedText\nDumping fuel"
                } else {
                    "$updatedText\nFuel dump req"
                }
            }
        } else if (aircraft.isWakeInfringe) {
            updatedText = "$updatedText\nWake alert"
        }
        label.setText(updatedText)
        label.pack()
        labelButton.setSize(label.width + 10, label.height)
        labelButton.setPosition(label.x - 5, label.y)
        clickSpot.setSize(labelButton.width, labelButton.height)
        clickSpot.setPosition(labelButton.x, labelButton.y)
    }

    /** Moves label as aircraft moves  */
    fun moveLabel(deltaX: Float, deltaY: Float) {
        label.moveBy(deltaX, deltaY)
    }

    /** Removes all components of label from stage  */
    fun removeLabel() {
        label.remove()
        icon.remove()
        labelButton.remove()
        clickSpot.remove()
    }

    /** Sets the position of the label  */
    fun setLabelPosition(x: Float, y: Float) {
        label.setPosition(x, y)
    }

    val labelPosition: FloatArray
        get() = floatArrayOf(label.x, label.y)
}