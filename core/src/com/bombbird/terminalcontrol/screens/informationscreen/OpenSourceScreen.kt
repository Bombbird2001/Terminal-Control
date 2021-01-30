package com.bombbird.terminalcontrol.screens.informationscreen

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.Fonts

class OpenSourceScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        loadLabel()
        loadButtons()
    }

    /** Loads labels for credits, disclaimers, etc  */
    fun loadLabel() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.WHITE
        val licenses = Label("Software and libraries used:", labelStyle)
        licenses.setPosition(1440 - licenses.width / 2f, 1500f)
        stage.addActor(licenses)
    }

    /** Overridden to go back to info screen instead of main menu screen */
    override fun loadButtons() {
        super.loadButtons()
        removeDefaultBackButtonChangeListener()
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor?) {
                //Back to info screen
                game.screen = InfoScreen(game, background)
            }
        })

        val licenseLinks = Array<kotlin.Array<String>>()
        licenseLinks.add(arrayOf("libGDX", "https://github.com/libgdx/libgdx/blob/master/LICENSE"))
        licenseLinks.add(arrayOf("OkHttp", "https://github.com/square/okhttp/blob/master/LICENSE.txt"))
        licenseLinks.add(arrayOf("Apache Commons Lang", "https://github.com/apache/commons-lang/blob/master/LICENSE.txt"))
        licenseLinks.add(arrayOf("Open Sans Font", "https://fonts.google.com/specimen/Open+Sans#license"))
        licenseLinks.add(arrayOf("JSON in Java", "https://www.json.org/license.html"))

        if (Gdx.app.type == Application.ApplicationType.Desktop) {
            licenseLinks.add(arrayOf("Balbolka", "http://www.cross-plus-a.com/bconsole.htm"))
            licenseLinks.add(arrayOf("MaryTTS", "https://github.com/marytts/marytts/blob/master/LICENSE.md"))
            licenseLinks.add(arrayOf("CMU voices for MaryTTS", "https://github.com/marytts/voice-cmu-slt-hsmm/blob/master/LICENSE.txt"))
            licenseLinks.add(arrayOf("DFKI voices for MaryTTS", "https://github.com/marytts/voice-dfki-prudence-hsmm/blob/master/LICENSE.md"))
        }

        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        var x = 1440f - MainMenuScreen.BUTTON_WIDTH / 1.5f - 50
        var y = 1275f
        for (link in licenseLinks) {
            val button = TextButton(link[0], buttonStyle)
            button.setSize(MainMenuScreen.BUTTON_WIDTH / 1.5f, MainMenuScreen.BUTTON_HEIGHT / 1.5f)
            button.setPosition(x, y)
            button.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    //Display open browser dialog
                    object : CustomDialog("Open Link?", "Open link to the license in browser?", "Cancel", "Open") {
                        override fun result(resObj: Any?) {
                            super.result(resObj)
                            if (resObj == DIALOG_POSITIVE) {
                                //Open browser
                                TerminalControl.browserInterface.openBrowser(link[1])
                            }
                        }
                    }.show(stage)
                }
            })
            stage.addActor(button)
            if (x < 1400) x = 1490f else {
                x = 1440f - MainMenuScreen.BUTTON_WIDTH / 1.5f - 50
                y -= 180
            }
        }
    }
}