package com.bombbird.terminalcontrol.screens.settingsscreen.customsetting

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.DataTagSettingsScreen
import com.bombbird.terminalcontrol.ui.datatag.DataTagConfig
import com.bombbird.terminalcontrol.utilities.Fonts

class DataTagLayoutScreen(game: TerminalControl, val background: Image?): BasicScreen(game, 5760, 3240) {
    private lateinit var currLayoutBox: SelectBox<String>
    private lateinit var currLayout: DataTagConfig
    private lateinit var fullTagBoxes: Array<Array<SelectBox<String>>>
    private lateinit var availableFieldsFull: HashSet<String>
    private lateinit var miniTagBoxes1: Array<Array<SelectBox<String>>>
    private lateinit var miniTagBoxes2: Array<Array<SelectBox<String>>>

    private lateinit var backButton: TextButton
    private lateinit var nextButton: TextButton

    /** Load the UI */
    fun loadUI() {
        stage.clear()

        if (background != null) {
            stage.addActor(background)
        }

        loadButton()
        loadBoxes()
        loadLabel()
        loadOptions()
    }

    /** Load the label describing each field */
    private fun loadLabel() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE

        val headerLabel = Label("Manage datatag layouts", labelStyle)
        headerLabel.setPosition(5760 / 2.0f - headerLabel.width / 2.0f, 3240 - 300f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)

        val airportLabel = Label("Layout: ", labelStyle)
        airportLabel.setPosition(1340f - airportLabel.width, 3240 * 0.8f + 150 - airportLabel.height / 2)
        stage.addActor(airportLabel)
    }

    /** Loads the select boxes for datatag layout arrangement */
    private fun loadBoxes() {
        currLayoutBox = createStandardBox()
        currLayoutBox.setPosition(1440f, 3240 * 0.8f)
        currLayoutBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (currLayoutBox.selected == "+ Add layout") {
                    currLayoutBox.selected = "New layout"
                    currLayout = DataTagConfig()
                    matchBoxesWithConfig()
                } else if (currLayoutBox.selected != "(None)") {
                    currLayout = DataTagConfig(currLayoutBox.selected)
                    matchBoxesWithConfig()
                }
                event.handle()
            }
        })
        stage.addActor(currLayoutBox)

        fullTagBoxes = Array()
        for (i in 0 until 4) {
            val lineArray = Array<SelectBox<String>>()
            for (j in 0 until 4) {
                val theBox = createStandardBox()
                theBox.setPosition(200 + j * (theBox.width + 50), 3240 * 0.65f - i * (theBox.height + 25))
                theBox.addListener(object: ChangeListener() {
                    override fun changed(event: ChangeEvent?, actor: Actor?) {
                        updateConfig(0)
                    }
                })
                lineArray.add(theBox)
                stage.addActor(theBox)
            }
            fullTagBoxes.add(lineArray)
        }

        miniTagBoxes1 = Array()
        for (i in 0 until 3) {
            val lineArray = Array<SelectBox<String>>()
            for (j in 0 until 2) {
                val theBox = createStandardBox()
                theBox.setPosition(200 + j * (theBox.width + 50), 3240 * 0.65f - i * (theBox.height + 25))
                theBox.addListener(object: ChangeListener() {
                    override fun changed(event: ChangeEvent?, actor: Actor?) {
                        updateConfig(1)
                    }
                })
                lineArray.add(theBox)
                stage.addActor(theBox)
            }
            miniTagBoxes1.add(lineArray)
        }

        miniTagBoxes2 = Array()
        for (i in 0 until 3) {
            val lineArray = Array<SelectBox<String>>()
            for (j in 0 until 2) {
                val theBox = createStandardBox()
                theBox.setPosition(200 + j * (theBox.width + 50), 3240 * 0.65f - i * (theBox.height + 25))
                theBox.addListener(object: ChangeListener() {
                    override fun changed(event: ChangeEvent?, actor: Actor?) {
                        updateConfig(2)
                    }
                })
                lineArray.add(theBox)
                stage.addActor(theBox)
            }
            miniTagBoxes2.add(lineArray)
        }
    }

    /** Sets all select boxes to match config layout */
    private fun matchBoxesWithConfig() {
        fullTagBoxes.forEach { it.forEach { box -> box.selected = "(None)" } }
        miniTagBoxes1.forEach { it.forEach { box -> box.selected = "(None)" } }
        miniTagBoxes2.forEach { it.forEach { box -> box.selected = "(None)" } }

        for ((row, line) in currLayout.arrangement.withIndex()) {
            for ((column, value) in line.withIndex()) {
                fullTagBoxes[row][column].selected = value
            }
        }

        for ((row, line) in currLayout.miniArrangement.first.withIndex()) {
            for ((column, value) in line.withIndex()) {
                miniTagBoxes1[row][column].selected = value
            }
        }

        for ((row, line) in currLayout.miniArrangement.second.withIndex()) {
            for ((column, value) in line.withIndex()) {
                miniTagBoxes2[row][column].selected = value
            }
        }
    }

    /** Sets the config to the layout in the boxes */
    private fun updateConfig(boxIndex: Int) {
        when (boxIndex) {
            0 -> {
                //Main box
                currLayout.arrangement.forEach { it.clear() }
                for ((row, line) in fullTagBoxes.withIndex()) {
                    line.forEach { if (it.selected != "(None") currLayout.arrangement[row].add(it.selected) }
                }
            }
            1 -> {
                currLayout.miniArrangement.first.forEach { it.clear() }
                for ((row, line) in miniTagBoxes1.withIndex()) {
                    line.forEach { if (it.selected != "(None") currLayout.miniArrangement.first[row].add(it.selected) }
                }
            }
            2 -> {
                currLayout.miniArrangement.second.forEach { it.clear() }
                for ((row, line) in miniTagBoxes2.withIndex()) {
                    line.forEach { if (it.selected != "(None") currLayout.miniArrangement.second[row].add(it.selected) }
                }
            } else -> Gdx.app.log("Datatag config", "Unknown box code $boxIndex")
        }
    }

    /** Loads the spawn box options */
    private fun loadOptions() {
        val customLayouts = Array(TerminalControl.datatagConfigs.filter { it != "Default" && it != "Compact" }.toTypedArray())
        if (customLayouts.isEmpty) customLayouts.add("(None)")
        customLayouts.add("+ Add layout")
        currLayoutBox.items = customLayouts
        currLayoutBox.selectedIndex = 0

        if (currLayoutBox.selected != "(None)") {
            currLayout = DataTagConfig(currLayoutBox.selected)
            matchBoxesWithConfig()
        }
        updatePage1Visibility(currLayoutBox.selected != "(None)")
        updatePage2Visibility(false)
    }

    /** Makes the standard selectBox for the UI */
    private fun createStandardBox(): SelectBox<String> {
        val selectBox = SelectBox<String>(SettingsTemplateScreen.selectBoxStyle)
        selectBox.setSize(700f, 300f)
        selectBox.setAlignment(Align.center)
        selectBox.list.setAlignment(Align.center)

        return selectBox
    }

    /** Loads buttons  */
    private fun loadButton() {
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont30
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        val cancelButton = TextButton("Cancel", textButtonStyle)
        cancelButton.setSize(1200f, 300f)
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800f)
        cancelButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                returnToDataTagScreen()
            }
        })
        stage.addActor(cancelButton)

        val confirmButton = TextButton("Save", textButtonStyle)
        confirmButton.setSize(1200f, 300f)
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800f)
        confirmButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //TODO save new config file
                returnToDataTagScreen()
            }
        })
        stage.addActor(confirmButton)

        backButton = TextButton("<", textButtonStyle)
        backButton.setSize(400f, 400f)
        backButton.setPosition(5760 / 2f - 2500, 3240 - 2800f)
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                updatePage1Visibility(true)
                updatePage2Visibility(false)
            }
        })
        backButton.isVisible = false
        stage.addActor(backButton)
        nextButton = TextButton(">", textButtonStyle)
        nextButton.setSize(400f, 400f)
        nextButton.setPosition(5760 / 2f + 2000, 3240 - 2800f)
        nextButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                updatePage1Visibility(false)
                updatePage2Visibility(true)
            }
        })
        stage.addActor(nextButton)
    }

    private fun updatePage1Visibility(visible: Boolean) {
        fullTagBoxes.forEach { it.forEach { box -> box.isVisible = visible } }
        nextButton.isVisible = visible
    }

    private fun updatePage2Visibility(visible: Boolean) {
        miniTagBoxes1.forEach { it.forEach { box -> box.isVisible = visible } }
        miniTagBoxes2.forEach { it.forEach { box -> box.isVisible = visible } }
        backButton.isVisible = visible
    }

    /** Overrides show method of basic screen */
    override fun show() {
        loadUI()
    }

    /** Return to datatag settings screen */
    private fun returnToDataTagScreen() {
        background?.scaleBy(-SettingsTemplateScreen.specialScale)
        game.screen = DataTagSettingsScreen(game, TerminalControl.radarScreen, background)
    }
}