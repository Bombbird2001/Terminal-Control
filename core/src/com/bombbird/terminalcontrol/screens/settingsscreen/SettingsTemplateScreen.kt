package com.bombbird.terminalcontrol.screens.settingsscreen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.ui.List
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.PauseScreen
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.Fonts

open class SettingsTemplateScreen(game: TerminalControl, val radarScreen: RadarScreen?, val background: Image?) : BasicScreen(game, 5760, 3240) {
    companion object {
        const val specialScale = 7.8f

        //Styles
        lateinit var selectBoxStyle: SelectBox.SelectBoxStyle
    }

    var xOffset = 0
    var yOffset = 0

    //Default buttons
    lateinit var confirmButton: TextButton
    lateinit var cancelButton: TextButton
    private lateinit var backButton: TextButton
    private lateinit var nextButton: TextButton

    //Tabs
    var settingsTabs: Array<SettingsTab>
    var tab: Int
    lateinit var labelStyle: Label.LabelStyle

    //Info string label
    lateinit var infoString: String

    //Class name of screen
    var className: String

    init {
        if (background != null) {
            background.scaleBy(specialScale)
            stage.addActor(background)
        }
        settingsTabs = Array()
        tab = 0
        className = javaClass.simpleName
    }
    
    fun loadUI(xOffset: Int, yOffset: Int) {
        this.xOffset = xOffset
        this.yOffset = yOffset
        loadStyles()
        loadBoxes()
        loadButton()
        loadLabel()
        loadTabs()
        updateTabs(true)
    }

    /** Loads the styles for the selectBox  */
    private fun loadStyles() {
        val paneStyle = ScrollPane.ScrollPaneStyle()
        paneStyle.background = TerminalControl.skin.getDrawable("ListBackground")
        val listStyle = List.ListStyle()
        listStyle.font = Fonts.defaultFont20
        listStyle.fontColorSelected = Color.WHITE
        listStyle.fontColorUnselected = Color.BLACK
        val buttonDown = TerminalControl.skin.getDrawable("Button_down")
        buttonDown.topHeight = 75f
        buttonDown.bottomHeight = 75f
        listStyle.selection = buttonDown
        selectBoxStyle = SelectBox.SelectBoxStyle()
        selectBoxStyle.font = Fonts.defaultFont20
        selectBoxStyle.fontColor = Color.WHITE
        selectBoxStyle.listStyle = listStyle
        selectBoxStyle.scrollStyle = paneStyle
        selectBoxStyle.background = TerminalControl.skin.getDrawable("Button_up")
    }

    /** Loads buttons  */
    open fun loadButton() {
        //Adds buttons by default, position, function depends on type of settings screen
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont30
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        cancelButton = TextButton("Cancel", textButtonStyle)
        cancelButton.setSize(1200f, 300f)
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800f)
        stage.addActor(cancelButton)
        confirmButton = TextButton("Confirm", textButtonStyle)
        confirmButton.setSize(1200f, 300f)
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800f)
        stage.addActor(confirmButton)
        backButton = TextButton("<", textButtonStyle)
        backButton.setSize(400f, 400f)
        backButton.setPosition(5760 / 2f - 2500, 3240 - 2800f)
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (tab <= 0) return
                tab--
                updateTabs(true)
            }
        })
        stage.addActor(backButton)
        nextButton = TextButton(">", textButtonStyle)
        nextButton.setSize(400f, 400f)
        nextButton.setPosition(5760 / 2f + 2000, 3240 - 2800f)
        nextButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (tab >= settingsTabs.size - 1) return
                tab++
                updateTabs(true)
            }
        })
        stage.addActor(nextButton)
        setButtonListeners()
    }

    /** Sets the default listeners for the cancel, confirm buttons, overridden in global settings to go to different screen  */
    open fun setButtonListeners() {
        cancelButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (radarScreen != null) {
                    if (radarScreen.tutorial) {
                        game.screen = PauseScreen(game, radarScreen)
                    } else {
                        game.screen = CategorySelectScreen(game, null, radarScreen)
                    }
                } else {
                    background?.scaleBy(-specialScale)
                    game.screen = CategorySelectScreen(game, background, null)
                }
            }
        })
        confirmButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                sendChanges()
                if (radarScreen != null) {
                    if (radarScreen.tutorial) {
                        game.screen = PauseScreen(game, radarScreen)
                    } else {
                        game.screen = CategorySelectScreen(game, null, radarScreen)
                    }
                } else {
                    background?.scaleBy(-specialScale)
                    game.screen = CategorySelectScreen(game, background, null)
                }
            }
        })
    }

    /** Changes the tab displayed  */
    open fun updateTabs(screenActive: Boolean) {
        backButton.isVisible = tab > 0
        nextButton.isVisible = tab < settingsTabs.size - 1
        if (tab > settingsTabs.size - 1 || tab < 0) {
            Gdx.app.log("SettingsTemplateScreen", "Invalid tab set: " + tab + ", size is " + settingsTabs.size)
            return
        }
        for (i in 0 until settingsTabs.size) {
            if (i == tab) {
                settingsTabs[i].updateVisibility(screenActive)
            } else {
                settingsTabs[i].updateVisibility(false)
            }
        }
    }

    /** Creates a default selectBox configuration with standard styles  */
    fun createStandardSelectBox(): SelectBox<String> {
        val box = SelectBox<String>(selectBoxStyle)
        box.setSize(1200f, 300f)
        box.setAlignment(Align.center)
        box.list.setAlignment(Align.center)
        return box
    }

    /** Updates the relevant settings  */
    open fun sendChanges() {
        //No default implementation
    }

    /** Loads selectBox for settings, overridden in respective classes  */
    open fun loadBoxes() {
        //No default implementation
    }

    /** Loads the selectBox labels, overridden in respective classes  */
    open fun loadLabel() {
        labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE
        if (radarScreen == null && this !is GlobalSettingsScreen) infoString += " These are default settings, you can still change them for individual games."
        val infoLabel = Label(infoString, labelStyle)
        infoLabel.setPosition(5760 / 2f - infoLabel.width / 2f, 3240 - 300f)
        stage.addActor(infoLabel)
    }

    /** Loads the various actors into respective tabs, overridden in respective classes  */
    open fun loadTabs() {
        //No default implementation
    }

    /** Sets the current options into selectBoxes  */
    open fun setOptions() {
        //No default implementation
    }

    /** Overrides render method to include detection of back button on android  */
    override fun render(delta: Float) {
        super.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            //On android, emulate cancelButton is pressed
            cancelButton.toggle()
        }
    }
}