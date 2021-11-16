package com.bombbird.terminalcontrol.screens.informationscreen

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

class FullVersionAdScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        loadLabel()
        loadButtons()
    }

    /** Loads labels for credits, disclaimers, etc  */
    fun loadLabel() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE
        val title = Label("Get the full version of Terminal Control!", labelStyle)
        title.setPosition(1440f - title.width / 2f, 1300f)
        stage.addActor(title)

        val labelStyle2 = Label.LabelStyle()
        labelStyle2.font = Fonts.defaultFont12
        labelStyle2.fontColor = Color.WHITE
        val features = Label("""
            - Access full version airports with no ads
            - Adjustable radar sweep timings
            - Unlock-able traffic, terrain collision warning systems
            - Customisable data tags
        """.trimIndent(), labelStyle2)
        features.wrap = true
        features.setPosition(1440f - features.width / 2f, 400f)
        stage.addActor(features)
    }

    /** Also loads buttons to privacy policy, open source screen */
    override fun loadButtons() {
        super.loadButtons()

        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont16
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        val openStore = TextButton("                        Get it here!   ", buttonStyle)
        openStore.height = 400f
        openStore.setPosition(1440f - openStore.width / 2, 950f - openStore.height / 2)
        openStore.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor?) {
                //Open link to play store listing
                TerminalControl.browserInterface.openBrowser("https://play.google.com/store/apps/details?id=com.bombbird.terminalcontrol")
            }
        })
        stage.addActor(openStore)

        val image = Image(Texture(Gdx.files.internal("game/ui/mainMenuImages/IconFull.png")))
        image.setSize(280f, 280f)
        image.setPosition(1440f - 100 - image.width, 950f - image.height / 2)
        image.touchable = Touchable.disabled
        stage.addActor(image)
    }
}