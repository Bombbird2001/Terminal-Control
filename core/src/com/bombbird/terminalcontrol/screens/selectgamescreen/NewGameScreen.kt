package com.bombbird.terminalcontrol.screens.selectgamescreen

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.isTCHXAvailable
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.files.FileLoader

class NewGameScreen(game: TerminalControl, background: Image?) : SelectGameScreen(game, background) {
    init {
        TerminalControl.updateRevision()
    }

    /** Overrides loadLabel method in SelectGameScreen to load appropriate title for label  */
    override fun loadLabel() {
        //Set label params
        super.loadLabel()
        val headerLabel = Label("Choose airport:", labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)
    }

    /** Overrides loadButton method in SelectGameScreen to load an additional tutorial button */
    override fun loadButtons() {
        super.loadButtons()

        val tutorialButton = TextButton("Tutorial", buttonStyle)
        tutorialButton.setSize(350f, MainMenuScreen.BUTTON_HEIGHT_SMALL)
        tutorialButton.setPosition(2880 - 350f, 1620 * 0.6f)
        tutorialButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                val handle = Gdx.files.internal("game/available.arpt")
                val airportArray = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
                var airac = 1
                for (arptData in airportArray) {
                    val arpt = arptData.split(":".toRegex()).toTypedArray()[0]
                    if (arpt == "TCTP") {
                        airac = arptData.split(":".toRegex()).toTypedArray()[1].split(",".toRegex()).toTypedArray()[0].split("-".toRegex()).toTypedArray()[1].toInt()
                        break
                    }
                }

                if (airac == -1) {
                    Gdx.app.log("NewGameScreen", "Tutorial airport TCTP's AIRAC unavailable")
                    return
                }

                val radarScreen = RadarScreen(game, "TCTP", airac, -1, true)
                TerminalControl.radarScreen = radarScreen
                game.screen = radarScreen
            }

        })
        stage.addActor(tutorialButton)
    }

    /** Overrides loadScroll method in SelectGameScreen to load airport info into scrollPane  */
    override fun loadScroll() {
        //Load airports
        val airports = Array<String>()
        airports.add("TCTP\nHaoyuan International Airport", "TCWS\nChangli International Airport")
        if (TerminalControl.full) {
            airports.add("TCTT\nNaheda Airport", "TCHH\nTang Gong International Airport", "TCBB\nSaikan International Airport", "TCBD\nLon Man International Airport")
            airports.add("TCMD\nHadrise Airport", "TCPG\nShartes o' Dickens Airport")
        }
        airports.add(if (isTCHXAvailable) "TCHX\nTai Kek International Airport" else "????")
        for (airport in airports) {
            val airportButton = TextButton(airport, buttonStyle)
            airportButton.name = airport.substring(0, 4)
            airportButton.label.wrap = true
            airportButton.addListener(object : ChangeListener() {
                override fun changed(event: ChangeEvent, actor: Actor) {
                    val name = actor.name
                    if ("????" == name) {
                        CustomDialog("????", "Hmmm... there may or may not\nbe a free airport somewhere?", "", "Oh cool").show(stage)
                        return
                    }
                    val handle = Gdx.files.internal("game/available.arpt")
                    val airportArray = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
                    var found = false
                    var airac = -1
                    for (arptData in airportArray) {
                        val arpt = arptData.split(":".toRegex()).toTypedArray()[0]
                        if (arpt == name) {
                            found = true
                            airac = arptData.split(":".toRegex()).toTypedArray()[1].split(",".toRegex()).toTypedArray()[0].split("-".toRegex()).toTypedArray()[1].toInt()
                            break
                        }
                    }
                    val handle1: FileHandle
                    when (Gdx.app.type) {
                        Application.ApplicationType.Android -> handle1 = Gdx.files.local("saves/saves.saves")
                        Application.ApplicationType.Desktop -> handle1 = Gdx.files.external(FileLoader.mainDir + "/saves/saves.saves")
                        else -> {
                            handle1 = Gdx.files.local("saves/saves.saves")
                            Gdx.app.log("File load error", "Unknown platform " + Gdx.app.type.name + " used!")
                        }
                    }
                    var slot = 0
                    if (handle1.exists()) {
                        val saves = Array(handle1.readString().split(",".toRegex()).toTypedArray())
                        while (saves.contains(slot.toString(), false)) {
                            slot++
                        }
                    }
                    if (found && airac > -1) {
                        val radarScreen = RadarScreen(game, name, airac, slot, false)
                        TerminalControl.radarScreen = radarScreen
                        game.screen = radarScreen
                    } else {
                        if (!found) {
                            Gdx.app.log("Directory not found", "Directory not found for $name")
                        } else {
                            Gdx.app.log("Invalid AIRAC cycle", "Invalid AIRAC cycle $airac")
                        }
                    }
                    event.handle()
                }
            })
            scrollTable.add(airportButton).width(MainMenuScreen.BUTTON_WIDTH * 1.2f).height(MainMenuScreen.BUTTON_HEIGHT)
            scrollTable.row()
        }
        val scrollPane = ScrollPane(scrollTable)
        scrollPane.setupFadeScrollBars(1f, 1.5f)
        scrollPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.6f
        scrollPane.y = 1620 * 0.2f
        scrollPane.width = MainMenuScreen.BUTTON_WIDTH * 1.2f
        scrollPane.height = 1620 * 0.6f
        stage.addActor(scrollPane)
    }
}