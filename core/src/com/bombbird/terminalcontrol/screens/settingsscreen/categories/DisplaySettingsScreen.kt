package com.bombbird.terminalcontrol.screens.settingsscreen.categories

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.utilities.saving.GameSaver

class DisplaySettingsScreen(game: TerminalControl, radarScreen: RadarScreen?, background: Image?) : SettingsTemplateScreen(game, radarScreen, background) {
    lateinit var trajectoryLine: SelectBox<String>
    lateinit var pastTrajectory: SelectBox<String>
    lateinit var mva: SelectBox<String>
    lateinit var ilsDash: SelectBox<String>
    lateinit var showUncontrolledTrail: SelectBox<String>
    lateinit var rangeCircle: SelectBox<String>
    lateinit var colour: SelectBox<String>
    lateinit var metar: SelectBox<String>
    lateinit var trajectoryLabel: Label
    var trajectorySel = 0
    private lateinit var pastTrajLabel: Label
    var pastTrajTime = 0
    private lateinit var mvaLabel: Label
    var showMva = false
    private lateinit var ilsDashLabel: Label
    var showIlsDash = false
    private lateinit var showUncontrolledLabel: Label
    var showUncontrolled = false
    private lateinit var rangeCircleLabel: Label
    var rangeCircleDist = 0
    private lateinit var colourLabel: Label
    var colourStyle = 0
    private lateinit var metarLabel: Label
    var realisticMetar = false

    init {
        infoString = "Set the game display settings below."
        loadUI(-1200, -200)
        setOptions()
    }

    /** Loads selectBox for display settings  */
    override fun loadBoxes() {
        trajectoryLine = createStandardSelectBox()
        trajectoryLine.setItems("Off", "60 sec", "90 sec", "120 sec", "150 sec")
        trajectoryLine.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                trajectorySel = if ("Off" == trajectoryLine.selected) {
                    0
                } else {
                    trajectoryLine.selected.split(" ".toRegex()).toTypedArray()[0].toInt()
                }
            }
        })

        pastTrajectory = createStandardSelectBox()
        pastTrajectory.setItems("Off", "60 sec", "120 sec", "All")
        pastTrajectory.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                pastTrajTime = when (pastTrajectory.selected) {
                    "Off" -> 0
                    "All" -> -1
                    else -> pastTrajectory.selected.split(" ".toRegex()).toTypedArray()[0].toInt()
                }
            }
        })

        mva = createStandardSelectBox()
        mva.setItems("Show", "Hide")
        mva.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                when (mva.selected) {
                    "Show" -> showMva = true
                    "Hide" -> showMva = false
                    else -> Gdx.app.log(className, "Unknown MVA setting " + mva.selected)
                }
            }
        })

        ilsDash = createStandardSelectBox()
        ilsDash.setItems("Simple", "Realistic")
        ilsDash.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                when (ilsDash.selected) {
                    "Simple" -> showIlsDash = false
                    "Realistic" -> showIlsDash = true
                    else -> Gdx.app.log(className, "Unknown ILS dash setting " + ilsDash.selected)
                }
            }
        })

        showUncontrolledTrail = createStandardSelectBox()
        showUncontrolledTrail.setItems("When selected", "Always")
        showUncontrolledTrail.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                when (showUncontrolledTrail.selected) {
                    "Always" -> showUncontrolled = true
                    "When selected" -> showUncontrolled = false
                    else -> Gdx.app.log(className, "Unknown uncontrolled trail setting " + ilsDash.selected)
                }
            }
        })

        rangeCircle = createStandardSelectBox()
        rangeCircle.setItems("Off", "5nm", "10nm", "15nm", "20nm")
        rangeCircle.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                rangeCircleDist = if ("Off" == rangeCircle.selected) {
                    0
                } else {
                    rangeCircle.selected.replace("nm", "").toInt()
                }
            }
        })

        colour = createStandardSelectBox()
        colour.setItems("More colourful", "More standardised")
        colour.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                when (colour.selected) {
                    "More colourful" -> colourStyle = 0
                    "More standardised" -> colourStyle = 1
                    else -> Gdx.app.log(className, "Unknown colour style setting " + colour.selected)
                }
            }
        })

        metar = createStandardSelectBox()
        metar.setItems("Simple", "Realistic")
        metar.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                realisticMetar = "Realistic" == metar.selected
            }
        })
    }

    /** Loads labels for display settings  */
    override fun loadLabel() {
        super.loadLabel()
        trajectoryLabel = Label("Trajectory line: ", labelStyle)
        pastTrajLabel = Label("Aircraft trail: ", labelStyle)
        mvaLabel = Label("MVA altitude: ", labelStyle)
        ilsDashLabel = Label("ILS display: ", labelStyle)
        showUncontrolledLabel = Label("Show uncontrolled:\naircraft trail", labelStyle)
        rangeCircleLabel = Label("Range rings:", labelStyle)
        colourLabel = Label("Colour style:", labelStyle)
        metarLabel = Label("Metar display:", labelStyle)
    }

    /** Loads actors for display settings into tabs  */
    override fun loadTabs() {
        val tab1 = SettingsTab(this, 2)
        tab1.addActors(trajectoryLine, trajectoryLabel)
        tab1.addActors(pastTrajectory, pastTrajLabel)
        tab1.addActors(mva, mvaLabel)
        tab1.addActors(ilsDash, ilsDashLabel)
        tab1.addActors(showUncontrolledTrail, showUncontrolledLabel)
        tab1.addActors(rangeCircle, rangeCircleLabel)
        tab1.addActors(colour, colourLabel)
        tab1.addActors(metar, metarLabel)
        settingsTabs.add(tab1)
    }

    /** Sets relevant options into select boxes  */
    override fun setOptions() {
        if (radarScreen == null) {
            //Use default settings
            trajectorySel = TerminalControl.trajectorySel
            pastTrajTime = TerminalControl.pastTrajTime
            showMva = TerminalControl.showMva
            showIlsDash = TerminalControl.showIlsDash
            showUncontrolled = TerminalControl.showUncontrolled
            rangeCircleDist = TerminalControl.rangeCircleDist
            colourStyle = TerminalControl.colourStyle
            realisticMetar = TerminalControl.realisticMetar
        } else {
            //Use game settings
            trajectorySel = radarScreen.trajectoryLine
            pastTrajTime = radarScreen.pastTrajTime
            showMva = radarScreen.showMva
            showIlsDash = radarScreen.showIlsDash
            showUncontrolled = radarScreen.showUncontrolled
            rangeCircleDist = radarScreen.rangeCircleDist
            colourStyle = radarScreen.colourStyle
            realisticMetar = radarScreen.realisticMetar
        }
        trajectoryLine.selected = if (trajectorySel == 0) "Off" else "$trajectorySel sec"
        when (pastTrajTime) {
            -1 -> pastTrajectory.setSelected("All")
            0 -> pastTrajectory.setSelected("Off")
            else -> pastTrajectory.setSelected("$pastTrajTime sec")
        }
        mva.selected = if (showMva) "Show" else "Hide"
        ilsDash.selected = if (showIlsDash) "Realistic" else "Simple"
        showUncontrolledTrail.selected = if (showUncontrolled) "Always" else "When selected"
        rangeCircle.selected = if (rangeCircleDist == 0) "Off" else rangeCircleDist.toString() + "nm"
        colour.selectedIndex = colourStyle
        metar.selected = if (realisticMetar) "Realistic" else "Simple"
    }

    /** Confirms and applies the changes set  */
    override fun sendChanges() {
        if (radarScreen != null) {
            radarScreen.trajectoryLine = trajectorySel
            radarScreen.pastTrajTime = pastTrajTime
            radarScreen.showMva = showMva
            radarScreen.showIlsDash = showIlsDash
            radarScreen.showUncontrolled = showUncontrolled
            val rangeDistChanged = radarScreen.rangeCircleDist != rangeCircleDist
            radarScreen.rangeCircleDist = rangeCircleDist
            val colourChanged = radarScreen.colourStyle != colourStyle
            radarScreen.colourStyle = colourStyle
            val metarChanged = radarScreen.realisticMetar != realisticMetar
            radarScreen.realisticMetar = realisticMetar
            Gdx.app.postRunnable {
                if (rangeDistChanged) radarScreen.loadRange() //Reload the range circles in case of any changes
                if (colourChanged) radarScreen.updateColourStyle() //Update the label colours
                if (metarChanged) radarScreen.ui.updateMetar() //Update metar display
            }
        } else {
            TerminalControl.trajectorySel = trajectorySel
            TerminalControl.pastTrajTime = pastTrajTime
            TerminalControl.showMva = showMva
            TerminalControl.showIlsDash = showIlsDash
            TerminalControl.showUncontrolled = showUncontrolled
            TerminalControl.rangeCircleDist = rangeCircleDist
            TerminalControl.colourStyle = colourStyle
            TerminalControl.realisticMetar = realisticMetar
            GameSaver.saveSettings()
        }
    }
}