package com.bombbird.terminalcontrol.screens.settingsscreen.customsetting

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.DataTagSettingsScreen
import com.bombbird.terminalcontrol.ui.datatag.DataTag
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
    private lateinit var availableFieldsFull: HashSet<Pair<String, Int>>
    private lateinit var miniTagBoxes1: Array<Array<SelectBox<String>>>
    private lateinit var availableFieldsMini1: HashSet<Pair<String, Int>>
    private lateinit var miniTagBoxes2: Array<Array<SelectBox<String>>>
    private lateinit var availableFieldsMini2: HashSet<Pair<String, Int>>

    private lateinit var backButton: TextButton
    private lateinit var nextButton: TextButton

    private lateinit var fieldOrder: HashMap<String, Int>

    private lateinit var previewTag: Label
    private lateinit var previewLabelFields: HashMap<String, String>
    private lateinit var previewFiller: Image

    private lateinit var showWhenChangedBox: CheckBox
    private lateinit var currentSelectField: String

    private var boxListening = true

    /** Load the UI */
    fun loadUI() {
        stage.clear()

        if (background != null) {
            stage.addActor(background)
        }

        fieldOrder = HashMap()
        fieldOrder[DataTag.CALLSIGN] = 0
        fieldOrder[DataTag.CALLSIGN_RECAT] = 1
        fieldOrder[DataTag.ICAO_TYPE] = 2
        fieldOrder[DataTag.ICAO_TYPE_WAKE] = 3
        fieldOrder[DataTag.ALTITUDE_FULL] = 4
        fieldOrder[DataTag.ALTITUDE] = 5
        fieldOrder[DataTag.CLEARED_ALT] = 6
        fieldOrder[DataTag.HEADING] = 7
        fieldOrder[DataTag.LAT_CLEARED] = 8
        fieldOrder[DataTag.SIDSTAR_CLEARED] = 9
        fieldOrder[DataTag.GROUND_SPEED] = 10
        fieldOrder[DataTag.CLEARED_IAS] = 11
        fieldOrder[DataTag.AIRPORT] = 12

        availableFieldsFull = HashSet()
        availableFieldsMini1 = HashSet()
        availableFieldsMini2 = HashSet()

        loadButton()
        loadBoxes()
        loadLabel()
        loadPreview()
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
        val checkBoxStyle = CheckBox.CheckBoxStyle()
        checkBoxStyle.checkboxOn = TerminalControl.skin.getDrawable("Checked")
        checkBoxStyle.checkboxOff = TerminalControl.skin.getDrawable("Unchecked")
        checkBoxStyle.font = Fonts.defaultFont24
        checkBoxStyle.fontColor = Color.WHITE
        showWhenChangedBox = CheckBox(" Show only when changed", checkBoxStyle)
        showWhenChangedBox.setPosition(3700f, 3240 * 0.6f)
        showWhenChangedBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (showWhenChangedBox.isChecked) currLayout.onlyShowWhenChanged.add(currentSelectField)
                else currLayout.onlyShowWhenChanged.remove(currentSelectField)
                updatePreview()
            }
        })
        showWhenChangedBox.isVisible = false
        stage.addActor(showWhenChangedBox)

        currLayoutBox = createStandardBox()
        currLayoutBox.setPosition(800f, 3240 * 0.77f)
        currLayoutBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (!boxListening) return
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

        resetAvailableFieldList(availableFieldsFull)
        resetAvailableFieldList(availableFieldsMini1)
        resetAvailableFieldList(availableFieldsMini2)
        fullTagBoxes = Array()
        for (i in 0 until 4) {
            val lineArray = Array<SelectBox<String>>()
            for (j in 0 until 4) {
                val theBox = createStandardBox()
                theBox.setPosition(200 + j * (theBox.width + 50), 3240 * 0.6f - i * (theBox.height + 25))
                theBox.items = Array(availableFieldsFull.map { it.first }.toTypedArray())
                theBox.addListener(object: ChangeListener() {
                    override fun changed(event: ChangeEvent?, actor: Actor?) {
                        if (!boxListening) return
                        if (theBox.selected != "(None)") availableFieldsFull.remove(Pair(theBox.selected, fieldOrder[theBox.selected]!!))
                        theBox.name?.let { if (it != "(None)") availableFieldsFull.add(Pair(it, fieldOrder[it]!!)) }
                        theBox.name = theBox.selected
                        updateConfig(0, true)
                        updateCheckbox(theBox.selected)
                    }
                })
                theBox.addListener(object: ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        super.clicked(event, x, y)
                        updateCheckbox(theBox.selected)
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
                theBox.items = Array(availableFieldsMini1.map { it.first }.toTypedArray())
                theBox.addListener(object: ChangeListener() {
                    override fun changed(event: ChangeEvent?, actor: Actor?) {
                        if (!boxListening) return
                        if (theBox.selected != "(None)") availableFieldsMini1.remove(Pair(theBox.selected, fieldOrder[theBox.selected]!!))
                        theBox.name?.let { if (it != "(None)") availableFieldsMini1.add(Pair(it, fieldOrder[it]!!)) }
                        theBox.name = theBox.selected
                        updateConfig(1, true)
                        updateCheckbox(theBox.selected)
                    }
                })
                theBox.addListener(object: ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        super.clicked(event, x, y)
                        updateCheckbox(theBox.selected)
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
                theBox.items = Array(availableFieldsMini2.map { it.first }.toTypedArray())
                theBox.addListener(object: ChangeListener() {
                    override fun changed(event: ChangeEvent?, actor: Actor?) {
                        if (!boxListening) return
                        if (theBox.selected != "(None)") availableFieldsMini2.remove(Pair(theBox.selected, fieldOrder[theBox.selected]!!))
                        theBox.name?.let { if (it != "(None)") availableFieldsMini2.add(Pair(it, fieldOrder[it]!!)) }
                        theBox.name = theBox.selected
                        updateConfig(2, true)
                        updateCheckbox(theBox.selected)
                    }
                })
                theBox.addListener(object: ClickListener() {
                    override fun clicked(event: InputEvent?, x: Float, y: Float) {
                        super.clicked(event, x, y)
                        updateCheckbox(theBox.selected)
                    }
                })
                lineArray.add(theBox)
                stage.addActor(theBox)
            }
            miniTagBoxes2.add(lineArray)
        }
    }

    /** Resets, adds all available fields into list */
    private fun resetAvailableFieldList(list: HashSet<Pair<String, Int>>) {
        list.clear()
        list.add(Pair("(None)", -1))
        list.add(Pair(DataTag.CALLSIGN, fieldOrder[DataTag.CALLSIGN]!!))
        list.add(Pair(DataTag.CALLSIGN_RECAT, fieldOrder[DataTag.CALLSIGN_RECAT]!!))
        list.add(Pair(DataTag.ICAO_TYPE, fieldOrder[DataTag.ICAO_TYPE]!!))
        list.add(Pair(DataTag.ICAO_TYPE_WAKE, fieldOrder[DataTag.ICAO_TYPE_WAKE]!!))
        list.add(Pair(DataTag.ALTITUDE_FULL, fieldOrder[DataTag.ALTITUDE_FULL]!!))
        list.add(Pair(DataTag.ALTITUDE, fieldOrder[DataTag.ALTITUDE]!!))
        list.add(Pair(DataTag.CLEARED_ALT, fieldOrder[DataTag.CLEARED_ALT]!!))
        list.add(Pair(DataTag.HEADING, fieldOrder[DataTag.HEADING]!!))
        list.add(Pair(DataTag.LAT_CLEARED, fieldOrder[DataTag.LAT_CLEARED]!!))
        list.add(Pair(DataTag.SIDSTAR_CLEARED, fieldOrder[DataTag.SIDSTAR_CLEARED]!!))
        list.add(Pair(DataTag.GROUND_SPEED, fieldOrder[DataTag.GROUND_SPEED]!!))
        list.add(Pair(DataTag.CLEARED_IAS, fieldOrder[DataTag.CLEARED_IAS]!!))
        list.add(Pair(DataTag.AIRPORT, fieldOrder[DataTag.AIRPORT]!!))
    }

    /** Sets all select boxes to match config layout */
    private fun matchBoxesWithConfig() {
        boxListening = false
        fullTagBoxes.forEach { it.forEach { box -> box.selected = "(None)" } }
        miniTagBoxes1.forEach { it.forEach { box -> box.selected = "(None)" } }
        miniTagBoxes2.forEach { it.forEach { box -> box.selected = "(None)" } }

        resetAvailableFieldList(availableFieldsFull)
        resetAvailableFieldList(availableFieldsMini1)
        resetAvailableFieldList(availableFieldsMini2)

        for ((row, line) in currLayout.arrangement.withIndex()) {
            for ((column, value) in line.withIndex()) {
                fullTagBoxes[row][column].selected = value
                fullTagBoxes[row][column].name = value
                availableFieldsFull.remove(Pair(value, fieldOrder[value]!!))
            }
        }
        updateConfig(0, false)

        for ((row, line) in currLayout.miniArrangement.first.withIndex()) {
            for ((column, value) in line.withIndex()) {
                miniTagBoxes1[row][column].selected = value
                miniTagBoxes1[row][column].name = value
                availableFieldsMini1.remove(Pair(value, fieldOrder[value]!!))
            }
        }
        updateConfig(1, false)

        for ((row, line) in currLayout.miniArrangement.second.withIndex()) {
            for ((column, value) in line.withIndex()) {
                miniTagBoxes2[row][column].selected = value
                miniTagBoxes2[row][column].name = value
                availableFieldsMini2.remove(Pair(value, fieldOrder[value]!!))
            }
        }
        updateConfig(2, true)
        boxListening = true
    }

    /** Sets the config to the layout in the boxes */
    private fun updateConfig(boxIndex: Int, updatePreview: Boolean) {
        when (boxIndex) {
            0 -> {
                //Main box
                currLayout.arrangement.forEach { it.clear() }
                for ((row, line) in fullTagBoxes.withIndex()) {
                    line.forEach {
                        val copy = Array(availableFieldsFull.toTypedArray())
                        it.selected?.let { selected ->
                            if (selected != "(None)") {
                                currLayout.arrangement[row].add(selected)
                                copy.add(Pair(selected, fieldOrder[selected]!!))
                            }
                        }
                        copy.sort { o1, o2 -> o1.second - o2.second }
                        it.items = Array(copy.map { field -> field.first }.toTypedArray())
                    }
                }
            }
            1 -> {
                currLayout.miniArrangement.first.forEach { it.clear() }
                for ((row, line) in miniTagBoxes1.withIndex()) {
                    line.forEach {
                        val copy = Array(availableFieldsMini1.toTypedArray())
                        it.selected?.let { selected ->
                            if (selected != "(None)") {
                                currLayout.miniArrangement.first[row].add(selected)
                                copy.add(Pair(selected, fieldOrder[selected]!!))
                            }
                        }
                        copy.sort { o1, o2 -> o1.second - o2.second }
                        it.items = Array(copy.map { field -> field.first }.toTypedArray())
                    }
                }
                currLayout.updateEmptyFields()
            }
            2 -> {
                currLayout.miniArrangement.second.forEach { it.clear() }
                for ((row, line) in miniTagBoxes2.withIndex()) {
                    line.forEach {
                        val copy = Array(availableFieldsMini2.toTypedArray())
                        it.selected?.let { selected ->
                            if (selected != "(None)") {
                                currLayout.miniArrangement.second[row].add(selected)
                                copy.add(Pair(selected, fieldOrder[selected]!!))
                            }
                        }
                        copy.sort { o1, o2 -> o1.second - o2.second }
                        it.items = Array(copy.map { field -> field.first }.toTypedArray())
                    }
                }
                currLayout.updateEmptyFields()
            } else -> Gdx.app.log("Datatag config", "Unknown box code $boxIndex")
        }

        if (updatePreview) updatePreview()
    }

    private fun updateCheckbox(field: String) {
        currentSelectField = field
        showWhenChangedBox.isVisible = DataTagConfig.CAN_BE_HIDDEN.contains(field)
        if (DataTagConfig.CAN_BE_HIDDEN.contains(field)) showWhenChangedBox.isChecked = currLayout.showOnlyWhenChanged(field)
    }

    /** Loads the spawn box options */
    private fun loadOptions() {
        boxListening = false
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
            deleteButton.isVisible = false
            previewTag.isVisible = false
            previewFiller.isVisible = false
            updatePage1Visibility(false)
            updatePage2Visibility(false)
        }
        boxListening = true
    }

    /** Resets the page elements when a new layout is selected */
    private fun updateNewLayoutPage() {
        matchBoxesWithConfig()
        layoutNameField.text = currLayoutBox.selected
        layoutNameField.isVisible = true
        layoutNameLabel.isVisible = true
        deleteButton.isVisible = true
        previewTag.isVisible = true
        previewFiller.isVisible = true
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
                                deleteButton.isVisible = false
                                previewTag.isVisible = false
                                previewFiller.isVisible = false
                                showWhenChangedBox.isVisible = false
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
                    CustomDialog("Invalid name", "Layout name cannot be empty", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                    return
                }
                if (layoutNameField.text == "+ Add new layout" || layoutNameField.text == "(None)") {
                    CustomDialog("Invalid name", "Layout name is invalid", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                    return
                }
                if (layoutNameField.text == "New layout") {
                    CustomDialog("Set name", "Please set a new name for the layout", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                    return
                }
                if (currLayout.name != layoutNameField.text) {
                    if (TerminalControl.datatagConfigs.contains(layoutNameField.text)) {
                        CustomDialog("Invalid name", "Name already used for another layout", "", "Ok", height = 1000, width = 2400, fontScale = 2f).show(stage)
                        return
                    }
                    boxListening = false
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
                updatePreview()
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
                updatePreview()
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

    private fun loadPreview() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont24
        labelStyle.fontColor = Color.WHITE
        previewTag = Label("", labelStyle)

        previewFiller = Image(TerminalControl.skin.getDrawable("FillerImage"))

        stage.addActor(previewFiller)
        stage.addActor(previewTag)

        previewLabelFields = HashMap()
        previewLabelFields[DataTag.CALLSIGN] = "ABC123"
        previewLabelFields[DataTag.CALLSIGN_RECAT] = "ABC123/B"
        previewLabelFields[DataTag.ICAO_TYPE] = "B77W"
        previewLabelFields[DataTag.ICAO_TYPE_WAKE] = "B77W/H/B"
        previewLabelFields[DataTag.AIRPORT] = "TCTP"
    }

    private fun updatePreview() {
        val vertSpd = " ^ "
        val clearedVert = "90"
        val exped = " => "
        val clearedAltFull = "110"
        val clearedAlt = if (currLayout.showOnlyWhenChanged(DataTag.CLEARED_ALT)) "" else "110"

        previewLabelFields[DataTag.ALTITUDE] = "64"
        previewLabelFields[DataTag.CLEARED_ALT] = clearedAlt
        previewLabelFields[DataTag.ALTITUDE_FULL] = "${previewLabelFields[DataTag.ALTITUDE]}$vertSpd$clearedVert$exped$clearedAltFull"

        val heading = 121
        previewLabelFields[DataTag.HEADING] = heading.toString()
        previewLabelFields[DataTag.GROUND_SPEED] = "270"

        val clearedSpd = if (currLayout.showOnlyWhenChanged(DataTag.CLEARED_IAS)) ""
        else "250"
        previewLabelFields[DataTag.CLEARED_IAS] = clearedSpd

        val latCleared = if (currLayout.showOnlyWhenChanged(DataTag.LAT_CLEARED)) "" else "HICAL"
        previewLabelFields[DataTag.LAT_CLEARED] = latCleared

        val sidStarCleared = if (currLayout.showOnlyWhenChanged(DataTag.SIDSTAR_CLEARED)) ""
        else "HICAL1C"
        previewLabelFields[DataTag.SIDSTAR_CLEARED] = sidStarCleared

        var updatedText = currLayout.generateTagText(previewLabelFields, backButton.isVisible)
        if (updatedText.isBlank()) updatedText = "Set fields and see\nthe preview here"
        previewTag.setText(updatedText)
        previewTag.pack()
        previewTag.setPosition(4000f, 3240 * 0.45f - previewTag.height / 2)

        previewFiller.setSize(previewTag.width + 60, previewTag.height + 30)
        previewFiller.setPosition(previewTag.x - 30, previewTag.y - 15)
    }

    /** Overrides show method of basic screen */
    override fun show() {
        loadUI()
    }

    /** Overrides render method to include updating of minimized preview tag */
    override fun render(delta: Float) {
        super.render(delta)
        if (backButton.isVisible && !currLayout.firstEmpty && !currLayout.secondEmpty) updatePreview()
    }

    /** Return to datatag settings screen */
    private fun returnToDataTagScreen() {
        background?.scaleBy(-SettingsTemplateScreen.specialScale)
        game.screen = DataTagSettingsScreen(game, TerminalControl.radarScreen, background)
    }
}