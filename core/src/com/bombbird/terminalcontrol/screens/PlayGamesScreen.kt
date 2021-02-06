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
    private lateinit var uploadGameButton: TextButton
    private lateinit var downloadGameButton: TextButton

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

        uploadGameButton = TextButton("Save progress\nto cloud", buttonStyle)
        uploadGameButton.setSize(MainMenuScreen.BUTTON_WIDTH / 2f - 25, MainMenuScreen.BUTTON_HEIGHT * 1.25f)
        uploadGameButton.setPosition(x, 450f)
        uploadGameButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (TerminalControl.playGamesInterface.isSignedIn()) {
                    object : CustomDialog("Save to cloud", "Save game progress to cloud?\nCaution: All current game data saved in the\ncloud will be overwritten!", "Don't save", "Save") {
                        override fun result(resObj: Any?) {
                            if (resObj == DIALOG_POSITIVE) {
                                //Save progress
                                TerminalControl.playGamesInterface.driveSaveGame()
                            }
                        }
                    }.show(stage)
                }
            }
        })
        uploadGameButton.isVisible = TerminalControl.playGamesInterface.isSignedIn()
        stage.addActor(uploadGameButton)

        downloadGameButton = TextButton("Load progress\nfrom cloud", buttonStyle)
        downloadGameButton.setSize(MainMenuScreen.BUTTON_WIDTH / 2f - 25, MainMenuScreen.BUTTON_HEIGHT * 1.25f)
        downloadGameButton.setPosition(x + MainMenuScreen.BUTTON_WIDTH / 2f + 25, 450f)
        downloadGameButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                if (TerminalControl.playGamesInterface.isSignedIn()) {
                    object : CustomDialog("Load from cloud", "Load game progress from cloud?\nCaution: All current game data saved on\nthis device will be overwritten!", "Don't load", "Load") {
                        override fun result(resObj: Any?) {
                            if (resObj == DIALOG_POSITIVE) {
                                //Load progress
                                TerminalControl.playGamesInterface.driveLoadGame()
                            }
                        }
                    }.show(stage)
                }
            }
        })
        downloadGameButton.isVisible = TerminalControl.playGamesInterface.isSignedIn()
        stage.addActor(downloadGameButton)
    }

    fun updateSignInStatus() {
        if (TerminalControl.playGamesInterface.isSignedIn()) {
            signedIn.setText("Signed in to Google Play Games")
            signInOutButton.setText("Sign Out")
            openAchievementButton.isVisible = true
            uploadGameButton.isVisible = true
            downloadGameButton.isVisible = true
        } else {
            signInOutButton.setText("Sign In")
            signedIn.setText("Not signed in to Google Play Games")
            openAchievementButton.isVisible = false
            uploadGameButton.isVisible = false
            downloadGameButton.isVisible = false
        }
    }
}