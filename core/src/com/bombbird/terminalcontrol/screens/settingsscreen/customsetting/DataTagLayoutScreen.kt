package com.bombbird.terminalcontrol.screens.settingsscreen.customsetting

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.DataTagSettingsScreen
import com.bombbird.terminalcontrol.ui.datatag.DataTagConfig
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.files.GameSaver

class DataTagLayoutScreen(game: TerminalControl, val background: Image?): BasicScreen(game, 5760, 3240) {
    private lateinit var currLayoutBox: SelectBox<String>
    private lateinit var currLayout: DataTagConfig
    private lateinit var layoutNameLabel: Label
    private lateinit var layoutNameField: TextField
    private lateinit var deleteButton: TextButton
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

        val layoutLabel = Label("Layout: ", labelStyle)
        layoutLabel.setPosition(700f - layoutLabel.width, 3240 * 0.77f + 150 - layoutLabel.height / 2)
        stage.addActor(layoutLabel)

        layoutNameLabel = Label("Layout name: ", labelStyle)
        layoutNameLabel.setPosition(2400f - layoutNameLabel.width, 3240 * 0.77f + 150 - layoutNameLabel.height / 2)
        stage.addActor(layoutNameLabel)
    }

    /** Loads the select boxes for datatag layout arrangement */
    private fun loadBoxes() {
        currLayoutBox = createStandardBox()
        currLayoutBox.setPosition(800f, 3240 * 0.77f)
        currLayoutBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (currLayoutBox.selected == "+ Add layout") {
                    val newItems = Array(currLayoutBox.items)
                    newItems.removeValue("(None)", false)
                    newItems.add("New layout")
                    currLayoutBox.items = newItems
                    currLayoutBox.selected = "New layout"
                    currLayout = DataTagConfig("New layout")
                    GameSaver.saveDatatagLayout(currLayout)
                    updateNewLayoutPage()
                } else if (currLayoutBox.selected != "(None)") {
                    currLayout = DataTagConfig(currLayoutBox.selected)
                    updateNewLayoutPage()
                }
                event.handle()
            }
        })
        stage.addActor(currLayoutBox)

        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE

        val textFieldStyle = TextField.TextFieldStyle()
        textFieldStyle.background = TerminalControl.skin.getDrawable("Button_up")
        textFieldStyle.font = Fonts.defaultFont24
        textFieldStyle.fontColor = Color.WHITE
        val oneCharSizeCalibrationThrowAway = Label("|", labelStyle)
        val cursorColor = Pixmap(
            oneCharSizeCalibrationThrowAway.width.toInt(),
            oneCharSizeCalibrationThrowAway.height.toInt(),
            Pixmap.Format.RGB888
        )
        cursorColor.setColor(Color.WHITE)
        cursorColor.fill()
        textFieldStyle.cursor = Image(Texture(cursorColor)).drawable

        layoutNameField = TextField("", textFieldStyle)
        layoutNameField.setSize(1000f, 300f)
        layoutNameField.setPosition(2500f, 3240 * 0.77f)
        layoutNameField.maxLength = 32
        layoutNameField.alignment = Align.center
        stage.addActor(layoutNameField)
        stage.keyboardFocus = layoutNameField

        fullTagBoxes = Array()
        for (i in 0 until 4) {
            val lineArray = Array<SelectBox<String>>()
            for (j in 0 until 4) {
                val theBox = createStandardBox()
                theBox.setPosition(200 + j * (theBox.width + 50), 3240 * 0.6f - i * (theBox.height + 25))
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
                theBox.setPosition(200 + j * (theBox.width + 50), 3240 * 0.6f - i * (theBox.height + 25) - 162)
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
                theBox.setPosition(2000 + j * (theBox.width + 50), 3240 * 0.6f - i * (theBox.height + 25) - 162)
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
            updateNewLayoutPage()
        } else {
            layoutNameField.isVisible = false
            layoutNameLabel.isVisible = false
            updatePage1Visibility(false)
            updatePage2Visibility(false)
        }
    }

    /** Resets the page elements when a new layout is selected */
    private fun updateNewLayoutPage() {
        matchBoxesWithConfig()
        layoutNameField.text = currLayoutBox.selected
        layoutNameField.isVisible = true
        layoutNameLabel.isVisible = true
        deleteButton.isVisible = true
        updatePage1Visibility(true)
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

        deleteButton = TextButton("Delete layout", textButtonStyle)
        deleteButton.setSize(900f, 300f)
        deleteButton.setPosition(3900f, 3240 * 0.77f)
        deleteButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                object: CustomDialog("Delete layout", "Delete ${currLayout.name}?", "Keep", "Delete", height = 1000, width = 2400, fontScale = 2f) {
                    override fun result(resObj: Any?) {
                        if (resObj == DIALOG_POSITIVE) {
                            GameSaver.deleteDatatagConfig(currLayout.name)
                            val newItems = Array(currLayoutBox.items)
                            newItems.removeValue(currLayout.name, false)
                            if (newItems.size <= 1) newItems.insert(0, "(None)")
                            currLayoutBox.items = newItems
                            currLayoutBox.selectedIndex = 0
                            if (currLayoutBox.selected != "(None)") {
                                currLayout = DataTagConfig(currLayoutBox.selected)
                                updateNewLayoutPage()
                            } else {
                                layoutNameField.isVisible = false
                                layoutNameLabel.isVisible = false
                                updatePage1Visibility(false)
                                updatePage2Visibility(false)
                            }
                        }
                    }
                }.show(stage)
            }
        })
        stage.addActor(deleteButton)

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
                if (layoutNameField.text.isBlank()) {
                    CustomDialog("Invalid name", "Name cannot be empty", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                    return
                }
                if (layoutNameField.text == "+ Add new layout" || layoutNameField.text == "(None)") {
                    CustomDialog("Invalid name", "Name is invalid", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                    return
                }
                if (currLayout.name != layoutNameField.text) {
                    if (TerminalControl.datatagConfigs.contains(layoutNameField.text)) {
                        CustomDialog("Invalid name", "Name already used for another layout", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                        return
                    }
                    GameSaver.deleteDatatagConfig(currLayout.name)
                    val newItems = Array(currLayoutBox.items)
                    newItems[newItems.indexOf(currLayout.name, false)] = layoutNameField.text
                    currLayoutBox.items = newItems
                    currLayout.name = layoutNameField.text
                    currLayoutBox.selected = currLayout.name
                }
                GameSaver.saveDatatagLayout(currLayout)
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