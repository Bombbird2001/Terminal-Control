package com.bombbird.terminalcontrol.ui.tabs

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.entities.procedures.holding.HoldingPoints
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.ui.Ui
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.math.MathTools.modulateHeading
import java.util.*
import kotlin.math.roundToInt

class LatTab(ui: Ui) : Tab(ui) {
    private lateinit var hdgBox: Label
    private lateinit var hdgBoxClick: Button
    private lateinit var hdg90add: TextButton
    private lateinit var hdg90minus: TextButton
    private lateinit var hdg10add: TextButton
    private lateinit var hdg10minus: TextButton
    private lateinit var hdg5add: TextButton
    private lateinit var hdg5minus: TextButton
    private lateinit var ilsBox: SelectBox<String>
    private lateinit var leftButton: TextButton
    private lateinit var rightButton: TextButton
    private val waypoints: Array<String> = Array()
    private val ils: Array<String> = Array()

    var isLatModeChanged = false
        private set
    var isWptChanged = false
    var isHdgChanged = false
    var isDirectionChanged = false
        private set
    var isAfterWptChanged = false
        private set
    var isAfterWptHdgChanged = false
    var isHoldWptChanged = false
        private set
    var isIlsChanged = false
    var isStarChanged = false

    init {
        loadHdgElements()
        loadILSBox()
    }

    fun loadModes() {
        modeButtons.addButton(NavState.SID_STAR, "SID/STAR")
        modeButtons.addButton(NavState.AFTER_WPT_HDG, "After wpt, hdg")
        modeButtons.addButton(NavState.VECTORS, "Vectors")
        modeButtons.addButton(NavState.HOLD_AT, "Hold")
        modeButtons.addButton(NavState.CHANGE_STAR, "Change STAR")
    }

    fun updateModeButtons() {
        selectedAircraft?.let {
            modeButtons.changeButtonText(NavState.SID_STAR, it.sidStar.name + if (it is Arrival) " STAR" else " SID")
        }
        modeButtons.setButtonColour(false)
    }

    private fun loadILSBox() {
        //Selectbox for selecting ILS
        val boxStyle = SelectBox.SelectBoxStyle()
        boxStyle.font = Fonts.defaultFont20
        boxStyle.fontColor = Color.WHITE
        boxStyle.listStyle = listStyle
        boxStyle.scrollStyle = paneStyle
        boxStyle.background = Ui.lightBoxBackground
        ilsBox = SelectBox(boxStyle)
        ilsBox.setAlignment(Align.center)
        ilsBox.list.setAlignment(Align.center)
        ilsBox.items = ils
        //And set ILS box visible
        ilsBox.isVisible = false
        ilsBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                choiceMade()
                event.handle()
            }
        })
        addActor(ilsBox, 0.65f, 0.25f, 3240 - 1325f, 300f)
    }

    private fun loadHdgElements() {
        //Label for heading
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont30
        labelStyle.fontColor = Color.WHITE
        labelStyle.background = Ui.hdgBoxBackgroundDrawable
        hdgBox = Label("360", labelStyle)
        hdgBox.setAlignment(Align.center)
        addActor(hdgBox, 1.1f / 3, 0.8f / 3, 3240 - 2600f, 900f)

        //Button for click spot below heading label, fixes annoying "click-through" bug
        val buttonStyle = Button.ButtonStyle()
        buttonStyle.up = Ui.transBackgroundDrawable
        buttonStyle.down = Ui.transBackgroundDrawable
        hdgBoxClick = Button(buttonStyle)
        hdgBoxClick.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                event.handle()
            }
        })
        addActor(hdgBoxClick, 1.1f / 3, 0.8f / 3, 3240 - 2600f, 900f)
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.down = Ui.lightBoxBackground
        textButtonStyle.up = Ui.lightBoxBackground
        textButtonStyle.font = Fonts.defaultFont20
        val textButtonStyle1 = TextButton.TextButtonStyle()
        textButtonStyle1.fontColor = Color.WHITE
        textButtonStyle1.down = Ui.lightestBoxBackground
        textButtonStyle1.up = Ui.lightestBoxBackground
        textButtonStyle1.font = Fonts.defaultFont20

        //+90 button
        hdg90add = newHdgButton(90, textButtonStyle)

        //-90 button
        hdg90minus = newHdgButton(-90, textButtonStyle)

        //+10 button
        hdg10add = newHdgButton(10, textButtonStyle1)

        //-10 button
        hdg10minus = newHdgButton(-10, textButtonStyle1)

        //+5 button
        hdg5add = newHdgButton(5, textButtonStyle)

        //-5 button
        hdg5minus = newHdgButton(-5, textButtonStyle)
        val textButtonStyle2 = TextButton.TextButtonStyle()
        textButtonStyle2.fontColor = Color.WHITE
        textButtonStyle2.down = TerminalControl.skin.getDrawable("Button_down_sharp")
        textButtonStyle2.up = Ui.lightestBoxBackground
        textButtonStyle2.checked = TerminalControl.skin.getDrawable("Button_down_sharp")
        textButtonStyle2.font = Fonts.defaultFont20

        //Left button
        leftButton = TextButton("Left", textButtonStyle2)
        leftButton.setProgrammaticChangeEvents(false)
        leftButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (leftButton.isChecked) {
                    rightButton.isChecked = false
                }
                choiceMade()
                event.handle()
            }
        })
        addActor(leftButton, 0.1f, 0.4f, 3240 - 1700f, 300f)

        //Right button
        rightButton = TextButton("Right", TextButton.TextButtonStyle(textButtonStyle2))
        rightButton.setProgrammaticChangeEvents(false)
        rightButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (rightButton.isChecked) {
                    leftButton.isChecked = false
                }
                choiceMade()
                event.handle()
            }
        })
        addActor(rightButton, 0.5f, 0.4f, 3240 - 1700f, 300f)
    }

    private fun newHdgButton(value: Int, buttonStyle: TextButton.TextButtonStyle): TextButton {
        val button = TextButton((if (value > 0) "+" else "") + value, buttonStyle)
        button.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                updateClearedHdg(value)
                event.handle()
            }
        })
        var offset = 1700
        if (value == -90 || value == 90) {
            offset = 2000
        } else if (value == -10 || value == 10) {
            offset = 2300
        } else if (value == -5 || value == 5) {
            offset = 2600
        }
        addActor(button, (if (value > 0) 1.9f else 0.3f) / 3, 0.8f / 3, 3240 - offset.toFloat(), 300f)
        radarScreen.uiStage.addActor(button)
        return button
    }

    private fun updateClearedHdg(deltaHdg: Int) {
        var hdgChange = deltaHdg
        if (latMode == NavState.AFTER_WPT_HDG) {
            val remainder = afterWptHdg % 5
            if (remainder != 0) {
                if (hdgChange < 0) {
                    hdgChange += 5 - remainder
                } else {
                    hdgChange -= remainder
                }
            }
            afterWptHdg += hdgChange
            afterWptHdg = modulateHeading(afterWptHdg)
        } else {
            val remainder = clearedHdg % 5
            if (remainder != 0) {
                if (hdgChange < 0) {
                    hdgChange += 5 - remainder
                } else {
                    hdgChange -= remainder
                }
            }
            clearedHdg += hdgChange
            clearedHdg = modulateHeading(clearedHdg)
        }
        updateElements()
        compareWithAC()
        updateElementColours()
    }

    override fun updateElements() {
        selectedAircraft?.let {
            notListening = true
            modeButtons.updateButtonActivity(it.navState.latModes)
            if (it.sidStarIndex >= it.route.size) {
                if (it.navState.dispLatMode.last() == NavState.HOLD_AT) {
                    it.navState.updateLatModes(NavState.REMOVE_SIDSTAR_AFTERHDG, false) //Don't remove hold at if aircraft is gonna hold
                } else {
                    it.navState.updateLatModes(NavState.REMOVE_ALL_SIDSTAR, false)
                }
            }
            ils.clear()
            ils.add(Ui.NOT_CLEARED_APCH)
            for (approach in it.airport.approaches.values) {
                val rwy = approach.name.substring(3)
                if (it.airport.landingRunways.containsKey(rwy) && it.airport.runways[rwy]?.isEmergencyClosed == false) {
                    ils.add(approach.name)
                }
            }
            if (clearedILS == null) clearedILS = Ui.NOT_CLEARED_APCH
            if (!ils.contains(clearedILS, false)) {
                ils.clear()
                ils.add(Ui.NOT_CLEARED_APCH)
                if (clearedILS?.let { it2 -> it.airport.runways.containsKey(it2.substring(3)) } == true) {
                    ils.add(clearedILS)
                } else {
                    clearedILS = Ui.NOT_CLEARED_APCH
                }
            }
            ilsBox.isVisible = ui.tab == 0 && it is Arrival && (!it.emergency.isActive || !it.emergency.isEmergency || it.emergency.isReadyForApproach)
            ilsBox.items = ils
            ilsBox.selected = clearedILS
            if (latMode == NavState.AFTER_WPT_HDG || latMode == NavState.SID_STAR) {
                //Make waypoint box visible
                if (visible) {
                    valueBox.isVisible = true
                }
                waypoints.clear()
                var startIndex: Int = it.sidStarIndex
                val latestDirect = it.navState.clearedDirect.last()
                if (latestDirect != null && it.route.waypoints.contains(latestDirect, false)) startIndex = it.route.findWptIndex(latestDirect.name)
                for (waypoint in it.route.getRemainingWaypoints(startIndex, it.route.size - 1)) {
                    waypoints.add(waypoint.name)
                }
                valueBox.items = waypoints
                if (latMode == NavState.AFTER_WPT_HDG) {
                    valueBox.setSelected(afterWpt)
                } else {
                    if (!waypoints.contains(clearedWpt, false)) {
                        clearedWpt = waypoints.first()
                    }
                    valueBox.setSelected(clearedWpt)
                }
            } else if (latMode == NavState.HOLD_AT && it is Arrival) {
                if (visible) {
                    valueBox.isVisible = true
                }
                waypoints.clear()
                if (it.isHolding) {
                    it.holdWpt?.let { it2 -> waypoints.add(it2.name) }
                } else {
                    val waypoints1: Array<Waypoint> = it.route.waypoints
                    for (i in 0 until waypoints1.size) {
                        if (it.route.holdProcedure.holdingPoints.containsKey(waypoints1[i].name) && it.route.findWptIndex(waypoints1[i].name) >= it.route.findWptIndex(it.navState.clearedDirect.last()?.name)) {
                            //Check if holding point is after current aircraft direct
                            waypoints.add(waypoints1[i].name)
                        }
                    }
                }
                if (!waypoints.contains(holdWpt, false)) {
                    holdWpt = if (waypoints.isEmpty) {
                        it.navState.updateLatModes(NavState.REMOVE_HOLD_ONLY, true)
                        null
                    } else {
                        waypoints.first()
                    }
                }
                valueBox.items = waypoints
                valueBox.setSelected(holdWpt)
            } else if (latMode == NavState.CHANGE_STAR && it is Arrival) {
                if (visible) {
                    valueBox.isVisible = true
                }
                if (newStar == null) {
                    valueBox.setSelectedIndex(0)
                } else {
                    valueBox.setSelected(newStar)
                }
            } else {
                //Otherwise hide it
                valueBox.setVisible(false)
            }

            //Show heading box if heading mode, otherwise hide it
            showHdgBoxes((latMode == NavState.AFTER_WPT_HDG || latMode == NavState.VECTORS) && visible && (!it.isLocCap || clearedILS == null || Ui.NOT_CLEARED_APCH == clearedILS || clearedILS?.let { it2 -> it.airport.approaches[it2.substring(3)] } != it.navState.clearedApch.last()) && !(it.isLocCap && Ui.NOT_CLEARED_APCH == clearedILS))
            showDirBoxes(latMode == NavState.VECTORS && visible && (!it.isLocCap || clearedILS == null || Ui.NOT_CLEARED_APCH == clearedILS || clearedILS?.let { it2 -> it.airport.approaches[it2.substring(3)] } != it.navState.clearedApch.last()) && !(it.isLocCap && Ui.NOT_CLEARED_APCH == clearedILS))
            if (latMode == NavState.AFTER_WPT_HDG) {
                hdgBox.setText(afterWptHdg.toString())
            } else if (latMode == NavState.VECTORS) {
                hdgBox.setText(clearedHdg.toString())
            }
            notListening = false
        }
    }

    override fun compareWithAC() {
        isLatModeChanged = false
        isHdgChanged = false
        isWptChanged = false
        isAfterWptChanged = false
        isAfterWptHdgChanged = false
        isHoldWptChanged = false
        isStarChanged = false
        isIlsChanged = false
        isDirectionChanged = false
        tabChanged = false

        selectedAircraft?.let {
            isLatModeChanged = latMode != it.navState.dispLatMode.last() && latMode != NavState.CHANGE_STAR
            isHdgChanged = clearedHdg != it.navState.clearedHdg.last()
            val lastDirect = it.navState.clearedDirect.last()
            if (clearedWpt != null && lastDirect != null) {
                isWptChanged = clearedWpt != lastDirect.name
            }
            val lastAfterWpt = it.navState.clearedAftWpt.last()
            if (afterWpt != null && lastAfterWpt != null) {
                isAfterWptChanged = afterWpt != lastAfterWpt.name
            }
            isAfterWptHdgChanged = afterWptHdg != it.navState.clearedAftWptHdg.last()
            val lastHoldWpt = it.navState.clearedHold.last()
            if (holdWpt != null && lastHoldWpt != null) {
                isHoldWptChanged = holdWpt != lastHoldWpt.name
            }
            val lastNewStar = it.navState.clearedNewStar.last()
            isStarChanged = (lastNewStar == null && newStar != null || lastNewStar != null && lastNewStar != newStar) && newStar != it.sidStar.name + " arrival"
            isIlsChanged = false
            if (it is Arrival) {
                if (clearedILS == null) {
                    clearedILS = Ui.NOT_CLEARED_APCH
                }
                val lastILS = it.navState.clearedApch.last()
                isIlsChanged = if (lastILS == null) {
                    //Not cleared approach yet
                    Ui.NOT_CLEARED_APCH != clearedILS
                } else {
                    clearedILS != lastILS.name
                }
            }
            isDirectionChanged = false
            isDirectionChanged = if (it.isVectored) {
                //If previous mode is vectors mode
                it.navState.clearedTurnDir.last() != turnDir
            } else {
                //If previous mode is not vectors mode
                turnDir == NavState.TURN_LEFT || turnDir == NavState.TURN_RIGHT
            }
            tabChanged = if (isLatModeChanged || isIlsChanged) {
                true
            } else {
                when (latMode) {
                    NavState.AFTER_WPT_HDG -> isAfterWptChanged || isAfterWptHdgChanged
                    NavState.SID_STAR -> isWptChanged
                    NavState.VECTORS -> isHdgChanged || isDirectionChanged
                    NavState.HOLD_AT -> isHoldWptChanged
                    NavState.CHANGE_STAR -> isStarChanged
                    else -> tabChanged
                }
            }
        }
    }

    override fun updateElementColours() {
        notListening = true
        //Lat mode selectbox colour
        modeButtons.setButtonColour(isLatModeChanged)

        //Lat mode waypoint box colour
        when (latMode) {
            NavState.AFTER_WPT_HDG -> valueBox.style.fontColor = if (isAfterWptChanged) Color.YELLOW else Color.WHITE
            NavState.SID_STAR -> valueBox.style.fontColor = if (isWptChanged) Color.YELLOW else Color.WHITE
            NavState.HOLD_AT -> valueBox.style.fontColor = if (isHoldWptChanged) Color.YELLOW else Color.WHITE
            NavState.CHANGE_STAR -> valueBox.style.fontColor = if (isStarChanged) Color.YELLOW else Color.WHITE
        }

        //Lat mode ILS box colour
        ilsBox.style.fontColor = if (isIlsChanged) Color.YELLOW else Color.WHITE

        //Set the box colour for left/right buttons regardless of latmode
        leftButton.isChecked = turnDir == NavState.TURN_LEFT
        rightButton.isChecked = turnDir == NavState.TURN_RIGHT

        //Lat mode hdg box colour
        if (latMode == NavState.AFTER_WPT_HDG) {
            hdgBox.style.fontColor = if (isAfterWptHdgChanged) Color.YELLOW else Color.WHITE
        } else if (latMode == NavState.VECTORS) {
            hdgBox.style.fontColor = if (isHdgChanged) Color.YELLOW else Color.WHITE
            leftButton.style.fontColor = Color.WHITE
            rightButton.style.fontColor = Color.WHITE
            if (isDirectionChanged) {
                val prevDir: Int = selectedAircraft?.navState?.clearedTurnDir?.last() ?: NavState.NO_DIRECTION
                if (turnDir == NavState.NO_DIRECTION) {
                    if (prevDir == NavState.TURN_LEFT) {
                        leftButton.style.fontColor = Color.YELLOW
                    } else if (prevDir == NavState.TURN_RIGHT) {
                        rightButton.style.fontColor = Color.YELLOW
                    }
                } else if (turnDir == NavState.TURN_LEFT) {
                    leftButton.style.fontColor = Color.YELLOW
                } else if (turnDir == NavState.TURN_RIGHT) {
                    rightButton.style.fontColor = Color.YELLOW
                }
            }
        }
        super.updateElementColours()
        notListening = false
    }

    override fun updateMode() {
        selectedAircraft?.let {
            //Check if changing the waypoint leads to change in max speed restrictions
            if (spdMode == NavState.SID_STAR_RESTR && isWptChanged) {
                val maxSpd: Int = it.route.getWptMaxSpd(clearedWpt)
                if (maxSpd > -1 && clearedSpd > maxSpd) {
                    clearedSpd = maxSpd
                    ui.spdTab.updateElements()
                }
            }
            it.navState.sendLat(latMode, clearedWpt, afterWpt, holdWpt, afterWptHdg, clearedHdg, clearedILS, newStar, turnDir)
        }
    }

    override fun resetTab() {
        super.resetTab()
        updateSidStarOptions()
    }

    /** Gets the current lateral nav state of aircraft of the latest transmission, sets variable to them  */
    override val acState: Unit
        get() {
            isLatModeChanged = false
            isHdgChanged = false
            isDirectionChanged = false
            isWptChanged = false
            isAfterWptChanged = false
            isAfterWptHdgChanged = false
            isHoldWptChanged = false
            isIlsChanged = false
            isStarChanged = false
            selectedAircraft?.let {
                latMode = it.navState.dispLatMode.last()
                modeButtons.mode = latMode
                clearedHdg = it.navState.clearedHdg.last()
                turnDir = it.navState.clearedTurnDir.last()
                clearedWpt = it.navState.clearedDirect.last()?.name
                afterWpt = it.navState.clearedAftWpt.last()?.name
                afterWptHdg = it.navState.clearedAftWptHdg.last()
                holdWpt = it.navState.clearedHold.last()?.name
                clearedILS = it.navState.clearedApch.last()?.name ?: Ui.NOT_CLEARED_APCH
                if (it is Arrival) {
                    newStar = it.navState.clearedNewStar.last()
                }
            }
        }

    //If previous mode is not a heading mode, set clearedHdg to current aircraft heading
    override val choices: Unit
        get() {
            notListening = true
            val prevMode = latMode
            latMode = modeButtons.mode
            if (latMode == NavState.VECTORS) {
                turnDir = when {
                    leftButton.isChecked -> NavState.TURN_LEFT
                    rightButton.isChecked -> NavState.TURN_RIGHT
                    else -> NavState.NO_DIRECTION
                }
            }
            if (latMode == NavState.AFTER_WPT_HDG) {
                valueBox.items = waypoints
                afterWpt = valueBox.selected
            } else if (latMode == NavState.SID_STAR) {
                valueBox.items = waypoints
                clearedWpt = valueBox.selected
            } else if (latMode == NavState.HOLD_AT) {
                valueBox.items = waypoints
                holdWpt = valueBox.selected
            } else if (latMode == NavState.VECTORS) {
                selectedAircraft?.let {
                    if (prevMode != NavState.VECTORS) {
                        //If previous mode is not a heading mode, set clearedHdg to current aircraft heading
                        clearedHdg = it.heading.roundToInt()
                        clearedHdg = modulateHeading(clearedHdg)
                    }
                }
            } else if (latMode == NavState.CHANGE_STAR && selectedAircraft is Arrival) {
                selectedAircraft?.let {
                    val starsAvailable = RandomSTAR.getAllPossibleSTARnames(it.airport)
                    starsAvailable.removeValue(it.sidStar.name + " arrival", false)
                    starsAvailable.insert(0, it.sidStar.name + " arrival")
                    valueBox.items = starsAvailable
                    newStar = valueBox.selected
                }
            }
            ilsBox.items = ils
            clearedILS = ilsBox.selected
            updateSidStarOptions()
            notListening = false
        }

    private fun showHdgBoxes(show: Boolean) {
        hdgBox.isVisible = show
        hdgBoxClick.isVisible = show
        hdg90add.isVisible = show
        hdg90minus.isVisible = show
        hdg10add.isVisible = show
        hdg10minus.isVisible = show
        hdg5add.isVisible = show
        hdg5minus.isVisible = show
    }

    private fun showDirBoxes(show: Boolean) {
        leftButton.isVisible = show
        rightButton.isVisible = show
    }

    private fun updateSidStarOptions() {
        selectedAircraft?.let {
            notListening = true
            if (it is Arrival && latMode == NavState.SID_STAR && !it.navState.altModes.contains("Descend via STAR", false)) {
                it.navState.updateAltModes(NavState.ADD_SIDSTAR_RESTR_UNRESTR, false)
            } else if (it is Departure && latMode == NavState.SID_STAR && !it.navState.altModes.contains("Climb via SID", false)) {
                it.navState.updateAltModes(NavState.ADD_SIDSTAR_RESTR_UNRESTR, false)
            } else if (latMode != NavState.HOLD_AT && latMode != NavState.SID_STAR && latMode != NavState.AFTER_WPT_HDG && (it.navState.altModes.removeValue("Descend via STAR", false) || it.navState.altModes.removeValue("Climb via SID", false))) {
                ui.altTab.modeButtons.mode = NavState.NO_RESTR
                altMode = NavState.NO_RESTR
            }
            if (it is Arrival && latMode == NavState.SID_STAR && !it.navState.spdModes.contains("STAR speed restrictions", false)) {
                it.navState.updateSpdModes(NavState.ADD_SIDSTAR_RESTR_UNRESTR, false)
            } else if (it is Departure && latMode == NavState.SID_STAR && !it.navState.spdModes.contains("SID speed restrictions", false)) {
                it.navState.updateSpdModes(NavState.ADD_SIDSTAR_RESTR_UNRESTR, false)
            } else if (latMode != NavState.HOLD_AT && latMode != NavState.SID_STAR && latMode != NavState.AFTER_WPT_HDG && (it.navState.spdModes.removeValue("STAR speed restrictions", false) || it.navState.spdModes.removeValue("SID speed restrictions", false))) {
                ui.spdTab.modeButtons.mode = NavState.NO_RESTR
                spdMode = NavState.NO_RESTR
            }
            if (it is Arrival) {
                if (latMode == NavState.SID_STAR && clearedILS != Ui.NOT_CLEARED_APCH) {
                    altMode = NavState.SID_STAR_RESTR
                    it.navState.updateAltModes(NavState.REMOVE_UNRESTR, false)
                    var lowestAlt = if (it.route.apchTrans == "")
                        it.airport.approaches[clearedILS?.substring(3)]?.getNextPossibleTransition(radarScreen.waypoints[clearedWpt], it.route)?.first?.restrictions?.let { it2 -> if (it2.size > 0) it2[it2.size - 1][0] else null }
                    else it.route.getWptMinAlt(it.route.size - 1)
                    if (lowestAlt == -1) lowestAlt = radarScreen.minAlt
                    if (lowestAlt != null) clearedAlt = lowestAlt
                } else if (clearedILS == Ui.NOT_CLEARED_APCH) {
                    if (it.navState.containsCode(latMode, NavState.SID_STAR, NavState.AFTER_WPT_HDG, NavState.HOLD_AT)) {
                        it.navState.updateAltModes(NavState.ADD_SIDSTAR_RESTR_UNRESTR, false)
                    } else {
                        it.navState.updateAltModes(NavState.ADD_UNRESTR_ONLY, false)
                    }
                }
            }
            if (it is Arrival && it.navState.dispLatMode.last() != NavState.HOLD_AT && it.navState.clearedDirect.last() != null) {
                var found = false
                val waypoints: HashMap<String, HoldingPoints> = it.route.holdProcedure.holdingPoints
                for (holdingPoint in waypoints.keys) {
                    if (it.route.findWptIndex(holdingPoint) >= it.route.findWptIndex(it.navState.clearedDirect.last()?.name)) {
                        //Check if holding point is after current aircraft direct
                        found = true
                        break
                    }
                }
                if (!found) {
                    it.navState.updateLatModes(NavState.REMOVE_HOLD_ONLY, false)
                }
            }
            if (it is Arrival) {
                var clearedApp = false
                for (apch in it.navState.clearedApch) {
                    if (apch != null) {
                        clearedApp = true
                        break
                    }
                }
                if (!it.navState.latModes.contains(Ui.CHANGE_STAR, false) && clearedILS == Ui.NOT_CLEARED_APCH && !clearedApp)
                    it.navState.updateLatModes(NavState.ADD_CHANGE_STAR, false)
                else if (it.navState.latModes.contains(Ui.CHANGE_STAR, false) && (clearedILS != Ui.NOT_CLEARED_APCH || clearedApp))
                    it.navState.updateLatModes(NavState.REMOVE_CHANGE_STAR, false)
            }
            ui.updateElements()
            ui.compareWithAC()
            ui.updateElementColours()
            notListening = false
        }
    }
}