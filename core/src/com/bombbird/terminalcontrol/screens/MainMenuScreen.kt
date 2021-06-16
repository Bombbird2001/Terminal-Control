package com.bombbird.terminalcontrol.screens

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager
import com.bombbird.terminalcontrol.screens.informationscreen.InfoScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.HelpScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.NewGameScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.MenuSettingsScreen
import com.bombbird.terminalcontrol.screens.upgradescreen.AchievementScreen
import com.bombbird.terminalcontrol.screens.upgradescreen.UpgradeScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.Fonts.defaultFont20
import com.bombbird.terminalcontrol.utilities.Fonts.generateAllFonts
import com.bombbird.terminalcontrol.utilities.SurveyAdsManager
import com.bombbird.terminalcontrol.utilities.files.FileLoader

class MainMenuScreen(game: TerminalControl, private var background: Image?) : BasicScreen(game, 2880, 1620) {
    companion object {
        //Button constants
        const val BUTTON_WIDTH = 1000f
        const val BUTTON_HEIGHT = 200f
        const val BUTTON_WIDTH_SMALL = 200f
        const val BUTTON_HEIGHT_SMALL = 200f
    }

    private var endDialog: Dialog? = null
    var dialogVisible = false

    init {
        TerminalControl.loadVersionInfo()
        TerminalControl.loadSettings()
        TerminalControl.loadDatatagConfigs()
        UnlockManager.loadStats()
        if (!TerminalControl.loadedDiscord) TerminalControl.discordManager.initializeDiscord()
        TerminalControl.discordManager.updateRPC()
        if (Gdx.app.type == Application.ApplicationType.Desktop) TerminalControl.tts.loadVoices()
        SurveyAdsManager.loadData()
    }

    /** Loads the UI elements to be rendered on screen  */
    private fun loadUI() {
        //Reset stage
        stage.clear()

        //Background image
        if (background == null) {
            val max = if (TerminalControl.full) 8 else 2
            val fileHandle = Gdx.files.internal("game/ui/mainMenuImages/" + MathUtils.random(1, max) + ".png")
            background = Image(Texture(fileHandle))
            background?.scaleBy(6.8f)
        }
        stage.addActor(background)

        //Set title icon
        val image = Image(Texture(Gdx.files.internal("game/ui/mainMenuImages/MainMenuIcon.png")))
        image.scaleBy(0.5f)
        image.setPosition(2880 / 2.0f - 1.5f * image.width / 2.0f, 1620 * 0.775f)
        stage.addActor(image)

        //Additional lite image if version is lite
        if (!TerminalControl.full) {
            val image1 = Image(Texture(Gdx.files.internal("game/ui/mainMenuImages/Lite.png")))
            image1.scaleBy(0.25f)
            image1.setPosition(2880 / 2.0f + 2.5f * image1.width, 1620 * 0.83f)
            stage.addActor(image1)
        }

        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = defaultFont20
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        //Set new game button params
        val newGameButton = TextButton("New Game", buttonStyle)
        val yPos = if (Gdx.app.type == Application.ApplicationType.Android) 0.45f else 0.55f
        newGameButton.setPosition(2880 / 2.0f - BUTTON_WIDTH / 2.0f, 1620 * yPos)
        newGameButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
        newGameButton.label.setAlignment(Align.center)
        newGameButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Start new game -> Choose airport screen
                game.screen = if (TerminalControl.revisionNeedsUpdate() && FileLoader.checkIfSaveExists()) NotifScreen(game, background) else NewGameScreen(game, background)
            }
        })
        stage.addActor(newGameButton)

        //Set load game button params
        val loadGameButton = TextButton("Load Game", buttonStyle)
        val yPos1 = if (Gdx.app.type == Application.ApplicationType.Android) 0.3f else 0.4f
        loadGameButton.setPosition(2880 / 2.0f - BUTTON_WIDTH / 2.0f, 1620 * yPos1)
        loadGameButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
        loadGameButton.label.setAlignment(Align.center)
        loadGameButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Load game -> Saved games screen
                game.screen = if (TerminalControl.revisionNeedsUpdate() && FileLoader.checkIfSaveExists()) NotifScreen(game, background) else LoadGameScreen(game, background)
            }
        })
        stage.addActor(loadGameButton)

        //Set settings button
        val imageButtonStyle = ImageButton.ImageButtonStyle()
        imageButtonStyle.imageUp = TerminalControl.skin.getDrawable("Settings_up")
        imageButtonStyle.imageDown = TerminalControl.skin.getDrawable("Settings_down")
        val settingsButton = ImageButton(imageButtonStyle)
        settingsButton.setPosition(1440 - 1200 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL)
        settingsButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL)
        settingsButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go to settings screen
                game.screen = MenuSettingsScreen(game, background)
            }
        })
        stage.addActor(settingsButton)

        //Set info button
        val imageButtonStyle1 = ImageButton.ImageButtonStyle()
        imageButtonStyle1.imageUp = TerminalControl.skin.getDrawable("Information_up")
        imageButtonStyle1.imageDown = TerminalControl.skin.getDrawable("Information_down")
        val infoButton = ImageButton(imageButtonStyle1)
        infoButton.setPosition(1440 - 950 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL)
        infoButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL)
        infoButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go to info screen
                game.screen = InfoScreen(game, background)
            }
        })
        stage.addActor(infoButton)

        //Set help button
        val imageButtonStyle2 = ImageButton.ImageButtonStyle()
        imageButtonStyle2.imageUp = TerminalControl.skin.getDrawable("Help_up")
        imageButtonStyle2.imageDown = TerminalControl.skin.getDrawable("Help_down")
        val helpButton = ImageButton(imageButtonStyle2)
        helpButton.setPosition(1440 - 700 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL)
        helpButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL)
        helpButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go to help screen
                game.screen = HelpScreen(game, background)
            }
        })
        stage.addActor(helpButton)

        //Set upgrade button (for full version only)
        if (TerminalControl.full) {
            val imageButtonStyle3 = ImageButton.ImageButtonStyle()
            imageButtonStyle3.imageUp = TerminalControl.skin.getDrawable("Upgrade_up")
            imageButtonStyle3.imageDown = TerminalControl.skin.getDrawable("Upgrade_down")
            val upgradeButton = ImageButton(imageButtonStyle3)
            upgradeButton.setPosition(1440 + 700 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL)
            upgradeButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL)
            upgradeButton.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    //Go to upgrade screen
                    game.screen = UpgradeScreen(game, background)
                }
            })
            stage.addActor(upgradeButton)
        }

        //Set achievement button
        val imageButtonStyle4 = ImageButton.ImageButtonStyle()
        imageButtonStyle4.imageUp = TerminalControl.skin.getDrawable("Medal_up")
        imageButtonStyle4.imageDown = TerminalControl.skin.getDrawable("Medal_down")

        val achievementButton = ImageButton(imageButtonStyle4)
        achievementButton.setPosition(1440 + 950 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL)
        achievementButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL)
        achievementButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go to upgrade screen
                game.screen = AchievementScreen(game, background)
            }
        })
        stage.addActor(achievementButton)

        //Set Google Play Games button (if Android)
        if (Gdx.app.type == Application.ApplicationType.Android) {
            val imageButtonStyle5 = ImageButton.ImageButtonStyle()
            imageButtonStyle5.imageUp = TerminalControl.skin.getDrawable("Play_game_up")
            imageButtonStyle5.imageDown = TerminalControl.skin.getDrawable("Play_game_down")

            val playGameButton = ImageButton(imageButtonStyle5)
            playGameButton.setPosition(1440 + 1200 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL)
            playGameButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL)
            playGameButton.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent?, actor: Actor?) {
                    //Go to Play Games screen
                    game.screen = PlayGamesScreen(game, background)
                }
            })
            stage.addActor(playGameButton)
        }

        //Changelog button
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont12
        textButtonStyle.fontColor = Color.WHITE
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")
        val changelogButton = TextButton("Changelog", textButtonStyle)
        changelogButton.setSize(350f, BUTTON_HEIGHT_SMALL)
        changelogButton.setPosition(2880 - changelogButton.width, 1620 * 0.6f)
        changelogButton.label.setAlignment(Align.center)
        changelogButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                game.screen = ChangelogScreen(game, background)
            }
        })
        stage.addActor(changelogButton)

        //Loads the quit game dialog
        loadDialog()

        //Set quit button params if desktop
        if (Gdx.app.type == Application.ApplicationType.Android) {
            return
        }
        val quitButton = TextButton("Quit", buttonStyle)
        quitButton.setPosition(2880 / 2.0f - BUTTON_WIDTH / 2.0f, 1620 * 0.1f)
        quitButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT)
        quitButton.label.setAlignment(Align.center)
        quitButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Quit game
                TerminalControl.tts.quit()
                dispose()
                Gdx.app.exit()
            }
        })
        stage.addActor(quitButton)
    }

    /** Loads the exit dialog */
    private fun loadDialog() {
        endDialog = object : CustomDialog("Quit Game?", "", "Cancel", "Quit", height = 400) {
            override fun result(resObj: Any?) {
                if (resObj == DIALOG_POSITIVE) {
                    //Quit game
                    dispose()
                    Gdx.app.exit()
                }
                dialogVisible = false
            }
        }
    }

    /** Overrides render method to include detection of back button on android  */
    override fun render(delta: Float) {
        super.render(delta)
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            //On android, emulate backButton is pressed - show exit dialog
            if (dialogVisible) endDialog?.hide() else endDialog?.show(stage)
            dialogVisible = !dialogVisible
        }
    }

    /** Overrides show method of BasicScreen  */
    override fun show() {
        if (Fonts.defaultFont6 == null) {
            //Regenerate fonts that were disposed
            generateAllFonts()
            TerminalControl.skin.get("defaultDialog", Window.WindowStyle::class.java).titleFont = defaultFont20
        }
        loadUI()
    }
}