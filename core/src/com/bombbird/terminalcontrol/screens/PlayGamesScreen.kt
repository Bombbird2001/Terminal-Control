package com.bombbird.terminalcontrol.screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.Fonts

class PlayGamesScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    private lateinit var signedIn: Label
    private lateinit var signInOutButton: TextButton
    private lateinit var openAchievementButton: TextButton
    private lateinit var syncGameButton: TextButton

    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        loadLabel()
        loadButtons()
    }

    /** Loads labels for credits, disclaimers, etc  */
    fun loadLabel() {
        super.loadLabel("Google Play Games")
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.WHITE
        signedIn = Label((if (TerminalControl.playGamesInterface.isSignedIn()) "Signed in" else "Not signed in") + " to Google Play Games", labelStyle)
        signedIn.setPosition(1440 - signedIn.width / 2f, 1300f)
        stage.addActor(signedIn)
    }

    /** Overridden to go back to info screen instead of main menu screen */
    override fun loadButtons() {
        super.loadButtons()

        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        val x = 1440f - MainMenuScreen.BUTTON_WIDTH / 2f

        signInOutButton = TextButton(if (TerminalControl.playGamesInterface.isSignedIn()) "Sign Out" else "Sign In", buttonStyle)
        signInOutButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT)
        signInOutButton.setPosition(x, 1000f)
        signInOutButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (TerminalControl.playGamesInterface.isSignedIn()) {
                    object : CustomDialog("Sign Out", "Sign out of Google Play Games?", "Stay signed in", "Sign out") {
                        override fun result(resObj: Any?) {
                            super.result(resObj)
                            if (resObj == DIALOG_POSITIVE) {
                                //Open Google Play Games
                                TerminalControl.playGamesInterface.gameSignOut()
                                updateSignInStatus()
                            }
                        }
                    }.show(stage)
                } else {
                    TerminalControl.playGamesInterface.gameSignIn()
                }
            }
        })
        stage.addActor(signInOutButton)

        openAchievementButton = TextButton("Achievements", buttonStyle)
        openAchievementButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT)
        openAchievementButton.setPosition(x, 750f)
        openAchievementButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (TerminalControl.playGamesInterface.isSignedIn()) TerminalControl.playGamesInterface.showAchievements()
            }
        })
        openAchievementButton.isVisible = TerminalControl.playGamesInterface.isSignedIn()
        stage.addActor(openAchievementButton)

        syncGameButton = TextButton("Sync game saves", buttonStyle)
        syncGameButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT)
        syncGameButton.setPosition(x, 500f)
        syncGameButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (TerminalControl.playGamesInterface.isSignedIn()) TerminalControl.playGamesInterface.startDriveSignIn()
            }
        })
        syncGameButton.isVisible = TerminalControl.playGamesInterface.isSignedIn()
        stage.addActor(syncGameButton)
    }

    fun updateSignInStatus() {
        if (TerminalControl.playGamesInterface.isSignedIn()) {
            signedIn.setText("Signed in to Google Play Games")
            signInOutButton.setText("Sign Out")
            openAchievementButton.isVisible = true
            syncGameButton.isVisible = true
        } else {
            signInOutButton.setText("Sign In")
            signedIn.setText("Not signed in to Google Play Games")
            openAchievementButton.isVisible = false
            syncGameButton.isVisible = false
        }
    }
}