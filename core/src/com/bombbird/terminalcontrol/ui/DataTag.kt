package com.bombbird.terminalcontrol.ui

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
    private val labelText: Array<String>
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
        labelText = arrayOf("", "", "", "", "", "", "", "", "", "", "")
        labelText[9] = aircraft.airport.icao
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
        val latTab = aircraft.ui.latTab
        val altTab = aircraft.ui.altTab
        val spdTab = aircraft.ui.spdTab
        val vertSpd: String = when {
            aircraft.radarVs < -150 -> " v "
            aircraft.radarVs > 150 -> " ^ "
            else -> " = "
        }
        labelText[0] = aircraft.callsign
        labelText[1] = aircraft.icaoType + "/" + aircraft.wakeCat + "/" + aircraft.recat
        labelText[2] = MathUtils.round(aircraft.radarAlt / 100).toString()
        labelText[3] = if (aircraft.apch is Circling && aircraft is Arrival && aircraft.phase > 0) "VIS" else if (aircraft.isGsCap) {
            if (aircraft.apch?.name?.contains("IMG") == true || (aircraft.apch is Circling && aircraft is Arrival && aircraft.phase > 0)) "VIS" else "GS"
        } else if (aircraft.isLocCap && aircraft.apch?.isNpa == true) "NPA" else (aircraft.targetAltitude / 100).toString()
        labelText[10] = (aircraft.navState.clearedAlt.last() / 100).toString()
        if (aircraft.isSelected && aircraft.isArrivalDeparture) {
            labelText[10] = (Tab.clearedAlt / 100).toString()
            if (altTab.isAltChanged) labelText[10] = "[YELLOW]" + labelText[10] + "[WHITE]"
        }
        if (MathUtils.round(aircraft.radarHdg.toFloat()) == 0) {
            aircraft.radarHdg = aircraft.radarHdg + 360
        }
        labelText[4] = MathUtils.round(aircraft.radarHdg.toFloat()).toString()
        labelText[6] = (aircraft.radarGs.toInt()).toString()
        labelText[7] = if (aircraft.isSelected && aircraft.isArrivalDeparture && spdTab.isSpdChanged) "[YELLOW]" + Tab.clearedSpd + "[WHITE]" else aircraft.navState.clearedSpd.last().toString()
        var exped = if (aircraft.navState.clearedExpedite.last()) " =>> " else " => "
        if (aircraft.isSelected && aircraft.isArrivalDeparture) {
            exped = if (Tab.clearedExpedite) " =>> " else " => "
            if (altTab.isExpediteChanged) exped = "[YELLOW]$exped[WHITE]"
        }
        var updatedText: String
        if (radarScreen.compactData) {
            if (aircraft.isSelected && aircraft.isArrivalDeparture) {
                var changed = false
                if (Tab.latMode == NavState.SID_STAR && (aircraft.isLocCap || Tab.clearedWpt != aircraft.navState.clearedDirect.last()?.name || aircraft.navState.containsCode(aircraft.navState.dispLatMode.last(), NavState.VECTORS, NavState.AFTER_WPT_HDG, NavState.HOLD_AT))) {
                    labelText[5] = Tab.clearedWpt ?: ""
                    changed = latTab.isLatModeChanged || latTab.isWptChanged
                    if (aircraft.isLocCap && !changed) labelText[5] = "LOC"
                } else if (Tab.latMode == NavState.HOLD_AT) {
                    labelText[5] = Tab.holdWpt ?: ""
                    changed = latTab.isLatModeChanged || latTab.isHoldWptChanged
                } else if (Tab.latMode == NavState.AFTER_WPT_HDG) {
                    if (aircraft.navState.clearedDirect.last() == aircraft.navState.clearedAftWpt.last() || latTab.isLatModeChanged || latTab.isAfterWptChanged || latTab.isAfterWptHdgChanged) {
                        labelText[5] = Tab.afterWpt + "=>" + Tab.afterWptHdg
                        changed = latTab.isLatModeChanged || latTab.isAfterWptChanged || latTab.isAfterWptHdgChanged
                    } else {
                        labelText[5] = ""
                    }
                } else if (Tab.latMode == NavState.VECTORS) {
                    if (aircraft.isLocCap) {
                        labelText[5] = "LOC"
                    } else {
                        labelText[5] = Tab.clearedHdg.toString()
                        if (Tab.turnDir == NavState.TURN_LEFT) {
                            labelText[5] += "L"
                        } else if (Tab.turnDir == NavState.TURN_RIGHT) {
                            labelText[5] += "R"
                        }
                        changed = latTab.isLatModeChanged || latTab.isHdgChanged || latTab.isDirectionChanged
                    }
                } else {
                    labelText[5] = ""
                }
                if (changed) labelText[5] = "[YELLOW]" + labelText[5] + "[WHITE]"
            } else {
                if (aircraft.isVectored) {
                    if (aircraft.isLocCap) {
                        labelText[5] = "LOC"
                    } else {
                        labelText[5] = aircraft.navState.clearedHdg.last().toString()
                        val turnDir: Int = aircraft.navState.clearedTurnDir.last()
                        if (turnDir == NavState.TURN_LEFT) {
                            labelText[5] += "L"
                        } else if (turnDir == NavState.TURN_RIGHT) {
                            labelText[5] += "R"
                        }
                    }
                } else if (aircraft.navState.dispLatMode.last() == NavState.HOLD_AT) {
                    labelText[5] = aircraft.holdWpt?.name ?: ""
                } else if (aircraft.navState.clearedDirect.last() != null && aircraft.navState.clearedDirect.last() == aircraft.navState.clearedAftWpt.last() && aircraft.navState.dispLatMode.last() == NavState.AFTER_WPT_HDG) {
                    labelText[5] = (aircraft.navState.clearedDirect.last()?.name ?: "") + "=>" + aircraft.navState.clearedAftWptHdg.last()
                } else if (aircraft.isLocCap) {
                    labelText[5] = "LOC"
                } else {
                    labelText[5] = ""
                }
            }
            if (aircraft.isSelected && aircraft.isArrivalDeparture) {
                var changed = false
                if (Tab.latMode == NavState.SID_STAR) {
                    labelText[8] = aircraft.sidStar.name
                    changed = latTab.isLatModeChanged && aircraft.navState.dispLatMode.last() != NavState.AFTER_WPT_HDG && aircraft.navState.dispLatMode.last() != NavState.HOLD_AT || latTab.isIlsChanged
                    if (Ui.NOT_CLEARED_APCH != Tab.clearedILS) {
                        if (aircraft.isLocCap) {
                            labelText[8] = Tab.clearedILS ?: ""
                        } else {
                            labelText[8] += " " + Tab.clearedILS
                        }
                    }
                } else if (Tab.latMode == NavState.AFTER_WPT_HDG || Tab.latMode == NavState.HOLD_AT) {
                    labelText[8] = aircraft.sidStar.name
                    changed = latTab.isIlsChanged
                    if (Ui.NOT_CLEARED_APCH != Tab.clearedILS) labelText[8] += " " + (Tab.clearedILS ?: "")
                } else if (Tab.latMode == NavState.VECTORS) {
                    if (Ui.NOT_CLEARED_APCH == Tab.clearedILS) {
                        if (aircraft.emergency.isEmergency && aircraft.emergency.isActive && !aircraft.navState.latModes.get(0).contains("arrival")) {
                            labelText[8] = ""
                        } else {
                            labelText[8] = aircraft.sidStar.name
                        }
                    } else {
                        labelText[8] = Tab.clearedILS ?: ""
                        changed = latTab.isLatModeChanged || latTab.isIlsChanged
                    }
                } else if (Tab.latMode == NavState.CHANGE_STAR) {
                    labelText[8] = Tab.newStar?.split(" ".toRegex())?.toTypedArray()?.get(0) ?: ""
                    changed = latTab.isStarChanged
                } else {
                    labelText[8] = ""
                }
                if (changed) labelText[8] = "[YELLOW]" + labelText[8] + "[WHITE]"
            } else {
                if (aircraft.isVectored && aircraft.navState.clearedApch.last() != null) {
                    labelText[8] = aircraft.navState.clearedApch.last()?.name ?: ""
                } else {
                    if (aircraft.emergency.isEmergency && aircraft.emergency.isActive && !aircraft.navState.latModes.get(0).contains("arrival")) {
                        labelText[8] = ""
                    } else {
                        labelText[8] = aircraft.sidStar.name
                        if (aircraft.navState.clearedApch.last() != null) {
                            if (aircraft.isLocCap) {
                                labelText[8] = aircraft.navState.clearedApch.last()?.name ?: ""
                            } else {
                                labelText[8] += " " + (aircraft.navState.clearedApch.last()?.name ?: "")
                            }
                        }
                    }
                }
            }
            if (!isMinimized && aircraft.isArrivalDeparture) {
                updatedText = """${labelText[0]} ${labelText[1]}
${labelText[2]}$vertSpd${labelText[3]}$exped${labelText[10]}
${labelText[5]}${if (labelText[5].isEmpty()) "" else " "}${labelText[8]}
${labelText[6]} ${labelText[7]} ${labelText[9]}"""
            } else {
                if (System.currentTimeMillis() % 4000 >= 2000) {
                    updatedText = """${labelText[0]}/${aircraft.recat}
${labelText[2]}  ${labelText[6]}"""
                } else {
                    var clearedAltStr = labelText[10]
                    if (aircraft.apch is Circling && aircraft is Arrival && aircraft.phase > 0) {
                        clearedAltStr = "VIS"
                    } else if (aircraft.isGsCap) {
                        clearedAltStr = if (aircraft.apch?.name?.contains("IMG") == true) "VIS" else "GS"
                    } else if (aircraft.isLocCap && aircraft.apch?.isNpa == true) {
                        clearedAltStr = "NPA"
                    }
                    updatedText = """${labelText[0]}/${aircraft.recat}
$clearedAltStr  ${aircraft.icaoType}"""
                }
            }
        } else {
            if (aircraft.isSelected && aircraft.isArrivalDeparture) {
                var changed = false
                if (Tab.latMode == NavState.VECTORS) {
                    if (aircraft.isLocCap) {
                        labelText[5] = "LOC"
                    } else {
                        labelText[5] = Tab.clearedHdg.toString()
                        if (Tab.turnDir == NavState.TURN_LEFT) {
                            labelText[5] += "L"
                        } else if (Tab.turnDir == NavState.TURN_RIGHT) {
                            labelText[5] += "R"
                        }
                        changed = latTab.isLatModeChanged || latTab.isHdgChanged || latTab.isDirectionChanged
                    }
                } else if (Tab.latMode == NavState.HOLD_AT) {
                    labelText[5] = Tab.holdWpt ?: ""
                    changed = latTab.isLatModeChanged || latTab.isHoldWptChanged
                } else if (Tab.latMode == NavState.SID_STAR || Tab.latMode == NavState.AFTER_WPT_HDG) {
                    if (Tab.latMode == NavState.AFTER_WPT_HDG && (aircraft.direct != null && Tab.afterWpt == aircraft.direct?.name || latTab.isAfterWptChanged || latTab.isAfterWptHdgChanged)) {
                        labelText[5] = Tab.afterWpt + "=>" + Tab.afterWptHdg
                        changed = latTab.isLatModeChanged || latTab.isAfterWptHdgChanged || latTab.isAfterWptChanged
                    } else {
                        labelText[5] = Tab.clearedWpt ?: ""
                        changed = latTab.isWptChanged
                        if (aircraft.isLocCap && !changed) {
                            labelText[5] = "LOC"
                        }
                    }
                }
                if (changed) labelText[5] = "[YELLOW]" + labelText[5] + "[WHITE]"
                changed = false
                if (Tab.latMode == NavState.CHANGE_STAR) {
                    labelText[8] = Tab.newStar?.split(" ".toRegex())?.toTypedArray()?.get(0) ?: ""
                    changed = latTab.isStarChanged
                } else {
                    if (aircraft.emergency.isEmergency && aircraft.emergency.isActive && !aircraft.navState.latModes.get(0).contains("arrival")) {
                        labelText[8] = ""
                    } else {
                        labelText[8] = aircraft.sidStar.name
                        changed = latTab.isLatModeChanged && Tab.latMode == NavState.SID_STAR && aircraft.navState.dispLatMode.last() != NavState.AFTER_WPT_HDG && aircraft.navState.dispLatMode.last() != NavState.HOLD_AT || latTab.isIlsChanged
                        if (aircraft.isLocCap) {
                            if (Tab.clearedILS != Ui.NOT_CLEARED_APCH) {
                                labelText[8] = Tab.clearedILS ?: ""
                            }
                        } else {
                            if (Tab.clearedILS != Ui.NOT_CLEARED_APCH) {
                                labelText[8] += " " + (Tab.clearedILS ?: "")
                            }
                        }
                    }
                }
                if (changed) labelText[8] = "[YELLOW]" + labelText[8] + "[WHITE]"
            } else {
                if (aircraft.isVectored) {
                    if (aircraft.isLocCap) {
                        labelText[5] = "LOC"
                    } else {
                        labelText[5] = aircraft.navState.clearedHdg.last().toString()
                        val turnDir: Int = aircraft.navState.clearedTurnDir.last()
                        if (turnDir == NavState.TURN_LEFT) {
                            labelText[5] += "L"
                        } else if (turnDir == NavState.TURN_RIGHT) {
                            labelText[5] += "R"
                        }
                    }
                } else if (aircraft.navState.dispLatMode.last() == NavState.HOLD_AT) {
                    if (aircraft.isHolding || aircraft.direct != null && aircraft.holdWpt != null && aircraft.direct == aircraft.holdWpt) {
                        labelText[5] = aircraft.holdWpt?.name ?: ""
                    } else if (aircraft.direct != null) {
                        labelText[5] = aircraft.direct?.name ?: ""
                    }
                } else if (aircraft.navState.containsCode(aircraft.navState.dispLatMode.last(), NavState.SID_STAR, NavState.AFTER_WPT_HDG)) {
                    if (aircraft.navState.clearedDirect.last() != null && aircraft.navState.clearedDirect.last() == aircraft.navState.clearedAftWpt.last() && aircraft.navState.dispLatMode.last() == NavState.AFTER_WPT_HDG) {
                        labelText[5] = (aircraft.navState.clearedDirect.last()?.name ?: "") + "=>" + aircraft.navState.clearedAftWptHdg.last()
                    } else if (aircraft.navState.clearedDirect.last() != null) {
                        labelText[5] = aircraft.navState.clearedDirect.last()?.name ?: ""
                        if (aircraft.isLocCap) labelText[5] = "LOC"
                    } else {
                        labelText[5] = ""
                    }
                }
                if (aircraft.emergency.isEmergency && aircraft.emergency.isActive && !aircraft.navState.latModes.get(0).contains("arrival")) {
                    labelText[8] = ""
                } else {
                    labelText[8] = aircraft.sidStar.name
                    if (aircraft.isLocCap) {
                        if (aircraft.navState.clearedApch.last() != null) {
                            labelText[8] = aircraft.navState.clearedApch.last()?.name ?: ""
                        }
                    } else {
                        if (aircraft.navState.clearedApch.last() != null) {
                            labelText[8] += " " + (aircraft.navState.clearedApch.last()?.name ?: "")
                        }
                    }
                }
            }
            updatedText = if (!isMinimized && aircraft.isArrivalDeparture) {
                """${labelText[0]} ${labelText[1]}
${labelText[2]}$vertSpd${labelText[3]}$exped${labelText[10]}
${labelText[4]} ${labelText[5]}${if (labelText[5].isEmpty()) "" else " "}${labelText[8]}
${labelText[6]} ${labelText[7]} ${labelText[9]}"""
            } else {
                """${labelText[0]}/${aircraft.recat}
${labelText[2]} ${labelText[4]}
${labelText[6]}"""
            }
        }
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