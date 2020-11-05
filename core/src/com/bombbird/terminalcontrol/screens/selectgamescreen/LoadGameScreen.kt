package com.bombbird.terminalcontrol.screens.selectgamescreen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Timer
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.ui.dialogs.DeleteDialog
import com.bombbird.terminalcontrol.utilities.RenameManager.renameAirportICAO
import com.bombbird.terminalcontrol.utilities.saving.FileLoader
import org.json.JSONArray

class LoadGameScreen(game: TerminalControl, background: Image?) : SelectGameScreen(game, background) {
    private val timer: Timer = Timer()
    private lateinit var loadingLabel: Label

    val deleteDialog = DeleteDialog()

    internal inner class MultiThreadLoad : Runnable {
        override fun run() {
            val saves = FileLoader.loadSaves()
            Gdx.app.postRunnable {
                loadSavedGamesUI(saves)
                stopAnimatingLabel()
            }
        }
    }

    init {
        TerminalControl.updateRevision()
    }

    /** Overrides loadLabel method in SelectGameScreen to load appropriate title for label  */
    override fun loadLabel() {
        //Set label params
        super.loadLabel()
        val headerLabel = Label("Choose save to load:", labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)
    }

    /** Overrides loadScroll method in SelectGameScreen to load save info into scrollPane  */
    override fun loadScroll() {
        loadingLabel = Label("", labelStyle)
        loadingLabel.setPosition(2880 / 2.0f - loadingLabel.width / 2.0f, 1620 * 0.5f)
        loadingLabel.isVisible = true
        stage.addActor(loadingLabel)
        animateLoadingLabel()
        Thread(MultiThreadLoad()).start() //Load the saves from another thread
    }

    /** Called after file I/O is complete to display the loaded saves  */
    private fun loadSavedGamesUI(saves: JSONArray) {
        val label = Label("No saves found!", labelStyle)
        label.setPosition(2880 / 2.0f - label.width / 2.0f, 1620 * 0.5f)
        label.isVisible = false
        stage.addActor(label)
        if (saves.length() == 0) {
            label.isVisible = true
        }
        for (i in 0 until saves.length()) {
            var multiplier = 1f
            val jsonObject = saves.getJSONObject(i)
            val toDisplay = """${renameAirportICAO(jsonObject.getString("MAIN_NAME"))} (Score: ${jsonObject.getInt("score")}    High score: ${jsonObject.getInt("highScore")})
Planes landed: ${jsonObject.getInt("landings")}    Planes departed: ${jsonObject.getInt("airborne")}"""
            val saveButton = TextButton(toDisplay, buttonStyle)
            saveButton.name = toDisplay.substring(0, 4)
            val handle = Gdx.files.internal("game/available.arpt")
            val airacs = Array<IntArray>()
            val airports = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
            for (icao in airports) {
                if (icao.split(":".toRegex()).toTypedArray()[0] == saveButton.name) {
                    val airacRanges = icao.split(":".toRegex()).toTypedArray()[1]
                    for (range in airacRanges.split(",".toRegex()).toTypedArray()) {
                        airacs.add(intArrayOf(range.split("-".toRegex()).toTypedArray()[0].toInt(), range.split("-".toRegex()).toTypedArray()[1].toInt()))
                    }
                    break
                }
            }
            val newestAirac = airacs[0][1] //Newest airac is always the largest number at the 1st position in array
            var airac = jsonObject.getInt("AIRAC")
            for (range in airacs) {
                if (airac >= range[0] && airac <= range[1]) {
                    airac = range[1]
                }
            }
            jsonObject.put("AIRAC", airac)
            if (airac < newestAirac) {
                saveButton.setText("$toDisplay\nNote: New game AIRAC $newestAirac has changed\nsignificantly from older AIRAC $airac")
                multiplier = 1.75f
            }
            saveButton.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    val radarScreen = RadarScreen(game, jsonObject)
                    TerminalControl.radarScreen = radarScreen
                    game.screen = radarScreen
                    event.handle()
                }
            })
            scrollTable.add(saveButton).width(MainMenuScreen.BUTTON_WIDTH * 1.2f).height(MainMenuScreen.BUTTON_HEIGHT * multiplier)
            val deleteButton = TextButton("Delete", buttonStyle)
            deleteButton.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    deleteDialog.show(stage, toDisplay, jsonObject.getInt("saveId"), scrollTable, deleteButton, saveButton, label)
                }
            })
            scrollTable.add(deleteButton).width(MainMenuScreen.BUTTON_WIDTH * 0.4f).height(MainMenuScreen.BUTTON_HEIGHT * multiplier)
            scrollTable.row()
        }
        val scrollPane = ScrollPane(scrollTable)
        scrollPane.fadeScrollBars = true
        scrollPane.setupFadeScrollBars(1f, 1.5f)
        scrollPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.8f
        scrollPane.y = 1620 * 0.2f
        scrollPane.width = MainMenuScreen.BUTTON_WIDTH * 1.6f
        scrollPane.height = 1620 * 0.6f
        stage.addActor(scrollPane)
    }

    /** Starts animating the loading label  */
    private fun animateLoadingLabel() {
        timer.scheduleTask(object : Timer.Task() {
            override fun run() {
                Gdx.app.postRunnable {
                    loadingLabel.setText("Loading.")
                    loadingLabel.setPosition(2880 / 2.0f - loadingLabel.prefWidth / 2.0f, 1620 * 0.5f)
                }
            }
        }, 0.25f)
        timer.scheduleTask(object : Timer.Task() {
            override fun run() {
                Gdx.app.postRunnable {
                    loadingLabel.setText("Loading..")
                    loadingLabel.setPosition(2880 / 2.0f - loadingLabel.prefWidth / 2.0f, 1620 * 0.5f)
                }
            }
        }, 0.5f)
        timer.scheduleTask(object : Timer.Task() {
            override fun run() {
                Gdx.app.postRunnable {
                    loadingLabel.setText("Loading...")
                    loadingLabel.setPosition(2880 / 2.0f - loadingLabel.prefWidth / 2.0f, 1620 * 0.5f)
                }
                animateLoadingLabel()
            }
        }, 0.75f)
    }

    /** Stops animating the loading label  */
    private fun stopAnimatingLabel() {
        Gdx.app.postRunnable {
            loadingLabel.isVisible = false
            timer.clear()
        }
    }
}