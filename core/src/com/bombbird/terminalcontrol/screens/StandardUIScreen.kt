package com.bombbird.terminalcontrol.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts

open class StandardUIScreen(game: TerminalControl, val background: Image?) : BasicScreen(game, 2880, 1620) {
    lateinit var backButton: TextButton

    /** Loads screen UI, to be overridden in each screen  */
    open fun loadUI() {
        //Reset stage and add background
        stage.clear()
        if (background != null) stage.addActor(background)
    }

    /** Loads heading label  */
    fun loadLabel(header: String?) {
        //Set label params
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE
        val headerLabel = Label(header, labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)
    }

    /** Loads back button  */
    open fun loadButtons() {
        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        //Set back button params
        backButton = TextButton("<= Back", buttonStyle)
        backButton.width = MainMenuScreen.BUTTON_WIDTH
        backButton.height = MainMenuScreen.BUTTON_HEIGHT
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f)
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go back to main menu
                game.screen = MainMenuScreen(game, background)
            }
        })
        stage.addActor(backButton)
    }

    /** Removes the default change listener added for going back to main menu screen */
    fun removeDefaultBackButtonChangeListener() {
        for (listener in backButton.listeners) {
            if (listener is ChangeListener) backButton.removeListener(listener)
        }
    }

    /** Overrides render method to include detection of back button on android  */
    override fun render(delta: Float) {
        super.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            //On android, emulate backButton is pressed
            backButton.toggle()
        }
    }

    /** Overrides show method of basic screen  */
    override fun show() {
        loadUI()
    }
}