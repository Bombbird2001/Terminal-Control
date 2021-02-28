package com.bombbird.terminalcontrol.ui

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.ui.tabs.AltTab
import com.bombbird.terminalcontrol.ui.tabs.LatTab
import com.bombbird.terminalcontrol.ui.tabs.SpdTab
import com.bombbird.terminalcontrol.utilities.Fonts
import org.apache.commons.lang3.StringUtils

class Ui {
    companion object {
        //UI string constants
        //Lateral
        const val AFTER_WPT_FLY_HDG = "After waypoint, fly heading"
        const val FLY_HEADING = "Fly heading"
        const val LEFT_HEADING = "Turn left heading"
        const val RIGHT_HEADING = "Turn right heading"
        const val HOLD_AT = "Hold at"
        const val CHANGE_STAR = "Change STAR"
        const val NOT_CLEARED_APCH = "Approach"

        //Altitude
        const val CLIMB_VIA_SID = "Climb via SID"
        const val DESCEND_VIA_STAR = "Descend via STAR"
        const val CLIMB_DESCEND_TO = "Climb/descend to"
        const val EXPEDITE_TO = "Expedite climb/descent to"

        //Speed
        const val SID_SPD_RESTRICTIONS = "SID speed restrictions"
        const val STAR_SPD_RESTRICTIONS = "STAR speed restrictions"
        const val NO_SPD_RESTRICTIONS = "No speed restrictions"
        lateinit var paneDrawable: NinePatchDrawable
        lateinit var hdgBoxBackgroundDrawable: NinePatchDrawable
        lateinit var transBackgroundDrawable: NinePatchDrawable
        lateinit var lightBoxBackground: NinePatchDrawable
        lateinit var lightestBoxBackground: NinePatchDrawable

        /** Loads the textures for the UI pane  */
        fun generatePaneTextures() {
            paneDrawable = NinePatchDrawable(TerminalControl.skin.getPatch("UiPane"))
            hdgBoxBackgroundDrawable = NinePatchDrawable(TerminalControl.skin.getPatch("BoxBackground"))
            transBackgroundDrawable = NinePatchDrawable(TerminalControl.skin.getPatch("TransBackground"))
            lightBoxBackground = NinePatchDrawable(TerminalControl.skin.getPatch("LightBoxBackground"))
            lightestBoxBackground = NinePatchDrawable(TerminalControl.skin.getPatch("LightestBoxBackground"))
        }
    }

    private lateinit var paneImage: Button
    val actorArray = com.badlogic.gdx.utils.Array<Actor>()
    val actorXArray = com.badlogic.gdx.utils.Array<Float>()
    val actorWidthArray = com.badlogic.gdx.utils.Array<Float>()
    lateinit var latTab: LatTab
    lateinit var altTab: AltTab
    lateinit var spdTab: SpdTab
    private lateinit var latButton: TextButton
    private lateinit var altButton: TextButton
    private lateinit var spdButton: TextButton
    private lateinit var cfmChange: TextButton
    private lateinit var handoverAck: TextButton
    private lateinit var resetAll: TextButton
    private lateinit var labelButton: TextButton
    private lateinit var moreInfoButton: TextButton

    //Label that displays score & high score
    private lateinit var scoreLabel: Label

    //Label that displays simulation speed
    private lateinit var infoLabel: Label

    //TextButton that pauses the game
    private lateinit var pauseButton: TextButton

    //Changes between zoom and distance measuring mode (for Android only)
    private lateinit var zoomDistButton: TextButton

    //Array for METAR info on default pane
    private lateinit var metarPane: ScrollPane
    private lateinit var metarInfos: com.badlogic.gdx.utils.Array<Label>

    //Instructions panel info
    private var selectedAircraft: Aircraft? = null
    var tab: Int
    private val radarScreen = TerminalControl.radarScreen!!

    init {
        tab = 0
        loadNormalPane()
        loadAircraftLabel()
        loadButtons()
    }

    /** Loads the 3 tabs  */
    fun loadTabs() {
        latTab = LatTab(this)
        latTab.loadModes()
        altTab = AltTab(this)
        altTab.loadModes()
        spdTab = SpdTab(this)
        spdTab.loadModes()
        setSelectedPane(null)
    }

    /** Updates the UI label after weather is updated  */
    fun updateMetar() {
        for (i in 0 until metarInfos.size) {
            val label = metarInfos[i]
            //Get airport: ICAO code is 1st 4 letters on label's text
            val airport = radarScreen.airports[label.text.toString().substring(0, 4)] ?: continue
            var metarText: Array<String>
            if (radarScreen.realisticMetar) {
                //Realistic metar format
                metarText = arrayOf("", "", "")
                metarText[0] = airport.icao + " - " + radarScreen.information
                val dep = StringBuilder()
                for (runway in airport.takeoffRunways.keys) {
                    if (dep.isNotEmpty()) dep.append(", ")
                    dep.append(runway)
                }
                val arr = StringBuilder()
                for (runway in airport.landingRunways.keys) {
                    if (arr.isNotEmpty()) arr.append(", ")
                    arr.append(runway)
                }
                metarText[1] = "DEP - $dep     ARR - $arr"
                metarText[2] = airport.metar.optString("metar", "")
            } else {
                //Simple metar format
                metarText = arrayOf("", "", "", "", "")
                metarText[0] = airport.icao
                //Wind: Speed + direction
                if (airport.winds[1] == 0) {
                    metarText[1] = "Winds: Calm"
                } else {
                    if (airport.winds[0] != 0) {
                        metarText[1] = "Winds: " + airport.winds[0] + "@" + airport.winds[1] + "kts"
                    } else {
                        metarText[1] = "Winds: VRB@" + airport.winds[1] + "kts"
                    }
                }
                //Gusts
                if (airport.gusts != -1) {
                    metarText[2] = "Gusting to: " + airport.gusts + "kts"
                } else {
                    metarText[2] = "Gusting to: None"
                }
                //Visbility
                metarText[3] = "Visibility: " + airport.visibility + " metres"
                //Windshear
                metarText[4] = "Windshear: " + airport.windshear
            }
            var success = false
            while (!success) {
                try {
                    label.setText(StringUtils.join(metarText, "\n"))
                    label.wrap = true
                    metarPane.layout()
                    metarPane.layout()
                    success = true
                } catch (e: ArrayIndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadAircraftLabel() {
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont30
        buttonStyle.fontColor = Color.WHITE
        labelButton = TextButton("No aircraft selected", buttonStyle)
        labelButton.align(Align.center)
        labelButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Prevents click from being sent to other elements
                event.handle()
            }
        })
        addActor(labelButton, 0.1f, 0.6f, 3240 - 695f, 270f)
        val buttonStyle1 = TextButton.TextButtonStyle()
        buttonStyle1.font = Fonts.defaultFont24
        buttonStyle1.fontColor = Color.BLACK
        buttonStyle1.up = lightestBoxBackground
        buttonStyle1.down = lightestBoxBackground
        moreInfoButton = TextButton("i", buttonStyle1)
        moreInfoButton.align(Align.center)
        moreInfoButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                val selected = "i".contentEquals(moreInfoButton.text)
                toggleAircraftLabel(selected)
                moreInfoButton.setText(if (selected) "<=" else "i")
                event.handle()
            }
        })
        addActor(moreInfoButton, 0.7f, 0.2f, 3240 - 645f, 170f)
    }

    private fun loadNormalPane() {
        //Background click "catcher"
        val buttonStyle = Button.ButtonStyle()
        buttonStyle.up = paneDrawable
        buttonStyle.down = paneDrawable
        paneImage = Button(buttonStyle)
        paneImage.setPosition(0f, 0f)
        paneImage.setSize(1080 * TerminalControl.WIDTH.toFloat() / TerminalControl.HEIGHT, 3240f)
        paneImage.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Prevents click from being sent to other elements
                event.handle()
            }
        })
        radarScreen.uiStage.addActor(paneImage)

        //Score display
        val labelStyle2 = Label.LabelStyle()
        labelStyle2.font = Fonts.defaultFont30
        labelStyle2.fontColor = Color.WHITE
        scoreLabel = Label("""
    Score: ${radarScreen.score}
    High score: ${radarScreen.highScore}
    """.trimIndent(), labelStyle2)
        addActor(scoreLabel, 1 / 19.2f, -1f, 2875f, scoreLabel.height)

        //Info label
        val labelStyle3 = Label.LabelStyle()
        labelStyle3.font = Fonts.defaultFont20
        labelStyle3.fontColor = Color.WHITE
        infoLabel = Label("", labelStyle3)
        infoLabel.setAlignment(Align.topRight)
        addActor(infoLabel, 0.6f, 0.35f, 2650f, infoLabel.height)
        updateInfoLabel()

        //Pause button
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.fontColor = Color.BLACK
        textButtonStyle.font = Fonts.defaultFont30
        textButtonStyle.up = lightestBoxBackground
        textButtonStyle.down = lightestBoxBackground
        pauseButton = TextButton("||", textButtonStyle)
        pauseButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Set game state to paused
                if (radarScreen.runwayChanger.isVisible) {
                    radarScreen.runwayChanger.hideAll()
                    radarScreen.utilityBox.setVisible(true)
                }
                radarScreen.setGameRunning(false)
                event.handle()
            }
        })
        addActor(pauseButton, 0.75f, 0.2f, 2800f, 300f)
        //radarScreen.uiStage.setDebugAll(true);

        //Metar display labels
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE
        val metarTable = Table()
        metarTable.align(Align.left)
        metarInfos = com.badlogic.gdx.utils.Array()
        for (airport in radarScreen.airports.values) {
            val metarInfo = Label(airport.icao, labelStyle)
            metarInfo.width = paneImage.width * 0.6f
            metarInfos.add(metarInfo)
            metarInfo.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent, x: Float, y: Float) {
                    super.clicked(event, x, y)
                    event.handle()
                    radarScreen.runwayChanger.setAirport(airport.icao)
                    radarScreen.runwayChanger.setMainVisible(true)
                    radarScreen.utilityBox.setVisible(false)
                }
            })
            metarTable.add(metarInfo).width(paneImage.width * 0.6f).padBottom(70f)
            metarTable.row()
        }
        metarPane = ScrollPane(metarTable)
        metarPane.setupFadeScrollBars(1f, 1.5f)
        var inputListener: InputListener? = null
        for (eventListener in metarPane.listeners) {
            if (eventListener is InputListener) {
                inputListener = eventListener
            }
        }
        if (inputListener != null) metarPane.removeListener(inputListener)
        addActor(metarPane, 1 / 19.2f, 0.6f, 1550f, 1200f)
    }

    fun setNormalPane(show: Boolean) {
        //Sets visibility of elements
        for (label in metarInfos) {
            label.isVisible = show
        }
        scoreLabel.isVisible = show
        infoLabel.isVisible = show && infoLabel.text.isNotEmpty()
        pauseButton.isVisible = show
        zoomDistButton.isVisible = show
        if (radarScreen.isUtilityBoxInitialized()) radarScreen.utilityBox.setVisible(show)
    }

    fun setSelectedPane(aircraft: Aircraft?) {
        //Sets visibility of UI pane
        if (aircraft != null) {
            if (aircraft != selectedAircraft) {
                tab = 0
                selectedAircraft = aircraft
                latTab.selectedAircraft = aircraft
                altTab.selectedAircraft = aircraft
                spdTab.selectedAircraft = aircraft
            }
            updateTabVisibility(true)
            updateTabButtons()
            //Make tab buttons visible, then update them to show correct colour
            showTabBoxes(true)
            showChangesButtons(true)
            resetAll()
            updateElementColours()
            updateAckHandButton(aircraft)
            latTab.updateModeButtons()
            altTab.updateModeButtons()
            spdTab.updateModeButtons()
            if (tab != TerminalControl.defaultTabNo && TerminalControl.defaultTabNo >= 0 && TerminalControl.defaultTabNo <= 2) {
                //Change default tab to user selected tab; for some reason changing tab = 1 or 2 above causes crash
                tab = TerminalControl.defaultTabNo
                updateTabButtons()
                updateTabVisibility(true)
                updateElements()
                updateElementColours()
            }
        } else {
            //Aircraft unselected
            selectedAircraft = null
            latTab.selectedAircraft = null
            altTab.selectedAircraft = null
            spdTab.selectedAircraft = null
            updateTabVisibility(false)
            showTabBoxes(false)
            latTab.setVisibility(false)
            altTab.setVisibility(false)
            spdTab.setVisibility(false)
            showChangesButtons(false)
        }
        moreInfoButton.setText("i")
    }

    private fun showTabBoxes(show: Boolean) {
        //Show/hide tab selection buttons
        latButton.isVisible = show
        altButton.isVisible = show
        spdButton.isVisible = show
    }

    private fun showChangesButtons(show: Boolean) {
        //Show/hide elements for changes
        cfmChange.isVisible = show
        handoverAck.isVisible = show
        resetAll.isVisible = show
    }

    private fun loadChangeButtons() {
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont20
        textButtonStyle.fontColor = Color.BLACK
        textButtonStyle.up = lightBoxBackground
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down_sharp")

        //Transmit button
        cfmChange = TextButton("Transmit", textButtonStyle)
        cfmChange.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (cfmChange.style.fontColor === Color.YELLOW && "Acknow-\nledge" == handoverAck.text.toString()) {
                    handoverAck.isVisible = false
                }
                updateMode()
                event.handle()
            }
        })
        addActor(cfmChange, 0.1f, 0.25f, 3240 - 3070f, 370f)

        //Handover/acknowledge button
        val ackStyle = TextButton.TextButtonStyle(textButtonStyle)
        ackStyle.fontColor = Color.YELLOW
        handoverAck = TextButton("Acknow-\nledge", ackStyle)
        handoverAck.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                val txt = handoverAck.text.toString()
                selectedAircraft?.let {
                    when (txt) {
                        "Acknow-\nledge" -> Gdx.app.postRunnable {
                            it.isActionRequired = false
                            handoverAck.isVisible = false
                        }
                        "Handover" -> Gdx.app.postRunnable {
                            it.isActionRequired = false
                            handoverAck.isVisible = false
                            it.contactOther()
                        }
                        else -> Gdx.app.log("Ui error", "Unknown button text $txt")
                    }
                }
                event.handle()
            }
        })
        addActor(handoverAck, 0.375f, 0.25f, 3240 - 3070f, 370f)

        //Undo all changes button
        resetAll = TextButton("Undo all\nchanges", textButtonStyle)
        resetAll.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                resetAll()
                event.handle()
            }
        })
        addActor(resetAll, 0.65f, 0.25f, 3240 - 3070f, 370f)
    }

    private fun loadTabButtons() {
        //Lat mode
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont20
        textButtonStyle.fontColor = Color.BLACK
        textButtonStyle.up = lightBoxBackground
        textButtonStyle.down = lightBoxBackground
        latButton = TextButton("Lateral", textButtonStyle)
        latButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (tab != 0) {
                    //Run only if tab is not lat
                    tab = 0
                    updateTabButtons()
                    updateTabVisibility(true)
                    updateElements()
                    updateElementColours()
                }
                event.handle()
            }
        })
        setTabColours(latButton, true)
        addActor(latButton, 0.1f, 0.25f, 3240 - 400f, 300f)

        //Alt mode
        val textButtonStyle2 = TextButton.TextButtonStyle()
        textButtonStyle2.font = Fonts.defaultFont20
        textButtonStyle2.fontColor = Color.BLACK
        textButtonStyle2.up = lightBoxBackground
        textButtonStyle2.down = lightBoxBackground
        altButton = TextButton("Altitude", textButtonStyle2)
        altButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (tab != 1) {
                    //Run only if tab is not alt
                    tab = 1
                    updateTabButtons()
                    updateTabVisibility(true)
                    updateElements()
                    updateElementColours()
                }
                event.handle()
            }
        })
        addActor(altButton, 0.375f, 0.25f, 3240 - 400f, 300f)

        //Spd mode
        val textButtonStyle3 = TextButton.TextButtonStyle()
        textButtonStyle3.font = Fonts.defaultFont20
        textButtonStyle3.fontColor = Color.BLACK
        textButtonStyle3.up = lightBoxBackground
        textButtonStyle3.down = lightBoxBackground
        spdButton = TextButton("Speed", textButtonStyle3)
        spdButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (tab != 2) {
                    //Run only if tab is not spd
                    tab = 2
                    updateTabButtons()
                    updateTabVisibility(true)
                    updateElements()
                    updateElementColours()
                }
                event.handle()
            }
        })
        addActor(spdButton, 0.65f, 0.25f, 3240 - 400f, 300f)
    }

    private fun updateTabButtons() {
        when (tab) {
            0 -> {
                setTabColours(latButton, true)
                setTabColours(altButton, false)
                setTabColours(spdButton, false)
            }
            1 -> {
                setTabColours(latButton, false)
                setTabColours(altButton, true)
                setTabColours(spdButton, false)
            }
            2 -> {
                setTabColours(latButton, false)
                setTabColours(altButton, false)
                setTabColours(spdButton, true)
            }
            else -> Gdx.app.log("Invalid tab", "Unknown tab number $tab set!")
        }
    }

    private fun setTabColours(textButton: TextButton, selected: Boolean) {
        if (selected) {
            textButton.style.down = TerminalControl.skin.getDrawable("Button_down_sharp")
            textButton.style.up = TerminalControl.skin.getDrawable("Button_down_sharp")
            textButton.style.fontColor = Color.WHITE
        } else {
            textButton.style.down = lightBoxBackground
            textButton.style.up = lightBoxBackground
            textButton.style.fontColor = Color.BLACK
        }
    }

    private fun loadButtons() {
        loadChangeButtons()
        loadTabButtons()

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont20
        buttonStyle.checkedFontColor = Color.WHITE
        buttonStyle.fontColor = Color.BLACK
        buttonStyle.checked = TerminalControl.skin.getDrawable("Button_down_sharp")
        buttonStyle.up = TerminalControl.skin.getDrawable("ListBackground")

        zoomDistButton = TextButton("Zoom\nmode", buttonStyle)
        zoomDistButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                zoomDistButton.setText(if (zoomDistButton.isChecked) "Dist\nmode" else "Zoom\nmode")
                radarScreen.distMode = zoomDistButton.isChecked
            }
        })

        if (Gdx.app.type != Application.ApplicationType.Android) return
        addActor(zoomDistButton, 0.8f, 0.15f, 0.52f * 3240, 300f)
    }

    fun updateState() {
        //Called when aircraft navstate changes not due to player, updates choices so that they are appropriate
        choices
        updateElements()
        compareWithAC()
        updateElementColours()
    }

    private fun updateMode() {
        if (latTab.tabChanged || altTab.tabChanged || spdTab.tabChanged) {
            //Lat mode
            latTab.updateMode()

            //Alt mode
            altTab.updateMode()

            //Spd mode
            spdTab.updateMode()
            selectedAircraft?.navState?.updateState()
            resetAll()
            selectedAircraft?.isActionRequired = false
        }
    }

    private fun resetTab(tab: Int) {
        //Reset current tab to original aircraft state
        when (tab) {
            0 -> latTab.resetTab()
            1 -> altTab.resetTab()
            2 -> spdTab.resetTab()
            else -> Gdx.app.log("Invalid tab", "Unknown tab number $tab specified!")
        }
    }

    private fun resetAll() {
        //Reset all tabs to original aircraft state
        resetTab(0)
        resetTab(1)
        resetTab(2)
    }

    //Gets the choices from the boxes, sets variables to them; called after selections in selectbox changes/heading value changes
    //Lat mode tab
    //Alt mode tab
    //Spd mode tab
    private val choices: Unit
        get() {
            //Gets the choices from the boxes, sets variables to them; called after selections in selectbox changes/heading value changes
            //Lat mode tab
            latTab.choices
            //Alt mode tab
            altTab.choices
            //Spd mode tab
            spdTab.choices
        }

    fun updateElements() {
        when (tab) {
            0 -> latTab.updateElements()
            1 -> altTab.updateElements()
            2 -> spdTab.updateElements()
            else -> Gdx.app.log("Invalid tab", "Unknown tab number $tab specified!")
        }
    }

    fun compareWithAC() {
        latTab.compareWithAC()
        altTab.compareWithAC()
        spdTab.compareWithAC()
    }

    fun updateElementColours() {
        when (tab) {
            0 -> latTab.updateElementColours()
            1 -> altTab.updateElementColours()
            2 -> spdTab.updateElementColours()
            else -> Gdx.app.log("Invalid tab", "Unknown tab number $tab specified!")
        }
    }

    fun updateResetColours() {
        if (latTab.tabChanged || altTab.tabChanged || spdTab.tabChanged) {
            resetAll.style.fontColor = Color.YELLOW
        } else {
            resetAll.style.fontColor = Color.WHITE
        }
    }

    fun addActor(actor: Actor, xRatio: Float, widthRatio: Float, y: Float, height: Float) {
        actor.setPosition(xRatio * paneWidth, y)
        actor.setSize(widthRatio * paneWidth, height)
        radarScreen.uiStage.addActor(actor)
        actorArray.add(actor)
        actorXArray.add(xRatio)
        actorWidthArray.add(widthRatio)
    }

    fun updatePaneWidth() {
        paneImage.setSize(1080 * TerminalControl.WIDTH.toFloat() / TerminalControl.HEIGHT, 3240f)
        for (i in 0 until actorArray.size) {
            actorArray[i].x = actorXArray[i] * paneImage.width
            if (actorWidthArray[i] > 0) actorArray[i].width = actorWidthArray[i] * paneImage.width
        }
        radarScreen.utilityBox.updateHeaderPosition()
    }

    private fun updateTabVisibility(show: Boolean) {
        if (show) {
            when (tab) {
                0 -> {
                    latTab.setVisibility(true)
                    altTab.setVisibility(false)
                    spdTab.setVisibility(false)
                }
                1 -> {
                    latTab.setVisibility(false)
                    altTab.setVisibility(true)
                    spdTab.setVisibility(false)
                }
                2 -> {
                    latTab.setVisibility(false)
                    altTab.setVisibility(false)
                    spdTab.setVisibility(true)
                }
                else -> Gdx.app.log("Invalid tab", "Unknown tab number set")
            }
        } else {
            latTab.setVisibility(false)
            altTab.setVisibility(false)
            spdTab.setVisibility(false)
        }
        toggleAircraftLabel(false)
        labelButton.isVisible = show
        moreInfoButton.isVisible = show
    }

    fun toggleAircraftLabel(moreInfo: Boolean) {
        labelButton.setText(selectedAircraft?.let {
            if (moreInfo) {
                if (it is Arrival) "App spd " + it.apchSpd + "kts" else "Crz alt FL" + (it as Departure).cruiseAlt / 100
            } else it.callsign + "    " + it.icaoType
        } ?: "")
    }

    fun updateScoreLabels() {
        scoreLabel.setText("""
        Score: ${radarScreen.score}
        High score: ${radarScreen.highScore}
        """.trimIndent())
    }

    fun updateInfoLabel() {
        var text = ""
        if (radarScreen.speed > 1) text = radarScreen.speed.toString() + "x speed"
        infoLabel.setText(text)
        infoLabel.isVisible = text.isNotEmpty() && selectedAircraft == null
    }

    fun updateAckHandButton(aircraft: Aircraft) {
        if (aircraft == selectedAircraft) {
            handoverAck.isVisible = true
            when {
                aircraft.canHandover() -> handoverAck.setText("Handover")
                aircraft.isActionRequired -> handoverAck.setText("Acknow-\nledge")
                else -> handoverAck.isVisible = false
            }
        }
    }

    val paneWidth: Float
        get() = paneImage.width
}