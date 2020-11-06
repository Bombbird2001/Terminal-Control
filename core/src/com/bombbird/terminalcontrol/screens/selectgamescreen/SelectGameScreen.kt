package com.bombbird.terminalcontrol.screens.selectgamescreen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.utilities.Fonts

open class SelectGameScreen(game: TerminalControl, val background: Image?) : BasicScreen(game, 2880, 1620) {
    val scrollTable: Table = Table()
    lateinit var backButton: TextButton

    //Styles
    lateinit var labelStyle: Label.LabelStyle
        private set
    lateinit var buttonStyle: TextButton.TextButtonStyle

    /** Loads the full UI of this screen  */
    open fun loadUI() {
        //Reset stage
        stage.clear()
        stage.addActor(background)
        loadLabel()
        loadButtons()
        loadScroll()
    }

    /** Loads the appropriate labelStyle, and is overridden to load a label with the appropriate text  */
    open fun loadLabel() {
        //Set label style
        labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE
    }

    /** Loads the default button styles and back button  */
    open fun loadButtons() {
        //Set button textures
        buttonStyle = TextButton.TextButtonStyle()
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

    /** Loads the contents of the scrollPane  */
    open fun loadScroll() {
        //No default implementation
    }

    /** Implements show method of screen  */
    override fun show() {
        loadUI()
    }

    /** Overrides render method to include detection of back button on android  */
    override fun render(delta: Float) {
        super.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            //On android, emulate backButton is pressed
            backButton.toggle()
        }
    }
}