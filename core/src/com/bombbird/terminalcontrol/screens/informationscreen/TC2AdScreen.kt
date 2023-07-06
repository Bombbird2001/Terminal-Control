package com.bombbird.terminalcontrol.screens.informationscreen

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class TC2AdScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        loadLabel()
        loadButtons()
    }

    /** Loads features labels  */
    fun loadLabel() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE
        val title = Label("Get Terminal Control 2!", labelStyle)
        title.setPosition(1440f - title.width / 2f, 1400f)
        stage.addActor(title)

        val labelStyle2 = Label.LabelStyle()
        labelStyle2.font = Fonts.defaultFont12
        labelStyle2.fontColor = Color.WHITE
        val features = Label("""
            - Multiplayer with up to 4 players depending on airport
            - More customization of aircraft route and clearances
            
            Please note that as the game is in beta, there will be some bugs.
            Additional airports, features will be added as development continues.
        """.trimIndent(), labelStyle2)
        features.wrap = true
        features.setPosition(1440f - features.width / 2f, 400f)
        stage.addActor(features)
    }

    /** Also loads buttons to store listings */
    override fun loadButtons() {
        super.loadButtons()

        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont16
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        val openStore = TextButton("                        Get it here!   ", buttonStyle)
        openStore.height = 400f
        openStore.setPosition(1440f - openStore.width / 2, 1050f - openStore.height / 2)
        openStore.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor?) {
                // Open link to play store/itch.io listing
                TerminalControl.browserInterface.openBrowser(
                    if (Gdx.app.type == Application.ApplicationType.Android) "https://play.google.com/store/apps/details?id=com.bombbird.terminalcontrol2"
                    else "https://bombbird2001.itch.io/terminal-control-2")
            }
        })
        stage.addActor(openStore)

        val image = Image(Texture(Gdx.files.internal("game/ui/mainMenuImages/TC2.png")))
        image.setSize(280f, 280f)
        image.setPosition(1440f - 100 - image.width, 1050f - image.height / 2)
        image.touchable = Touchable.disabled
        stage.addActor(image)
    }
}