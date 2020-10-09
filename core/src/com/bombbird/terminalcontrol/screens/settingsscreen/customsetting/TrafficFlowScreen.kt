package com.bombbird.terminalcontrol.screens.settingsscreen.customsetting

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.TrafficSettingsScreen
import com.bombbird.terminalcontrol.utilities.Fonts

/** Screen for managing traffic flow into airports */
class TrafficFlowScreen(val game: TerminalControl): BasicScreen(game, 5760, 3240) {
    var mode: Int = 0
    val airportFlow: HashMap<String, SelectBox<Int>> = HashMap()
    val airportClosed: HashMap<String, SelectBox<String>> = HashMap()

    lateinit var modeBox: SelectBox<String>
    lateinit var maxPlanesBox: SelectBox<String>

    /** Load the UI */
    fun loadUI() {
        stage.clear()
        loadButton()
        loadLabel()
        loadOptions()
    }

    /** Loads the traffic flow options for each airport */
    private fun loadOptions() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont30
        labelStyle.fontColor = Color.WHITE

        val modeLabel = Label("Mode:", labelStyle)
        modeLabel.setAlignment(Align.right)

        val maxPlaneLabel = Label("Max aircraft:", labelStyle)
        maxPlaneLabel.setAlignment(Align.right)

        modeBox = SelectBox(SettingsTemplateScreen.selectBoxStyle)
        modeBox.setItems("Normal", "Max planes", "Flow rate")

        maxPlanesBox = SelectBox(SettingsTemplateScreen.selectBoxStyle)
        maxPlanesBox.setItems() //TODO

        val table = Table()
        table.add(modeLabel).width(500f).height(300f)
        table.add(modeBox).width(1000f).height(300f).padRight(160f)
    }

    /** Loads heading label  */
    private fun loadLabel() {
        //Set label params
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont30
        labelStyle.fontColor = Color.WHITE

        val headerLabel = Label("Custom Weather", labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH.toFloat()
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT.toFloat()
        headerLabel.setPosition(5760 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 3240 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)

        val labelStyle1 = Label.LabelStyle()
        labelStyle1.font = Fonts.defaultFont20
        labelStyle1.fontColor = Color.WHITE
        val noteLabel = Label("Note: In flow mode, set flow to 0 to close an airport", labelStyle1)
        noteLabel.setPosition(5760 / 2.0f - noteLabel.width / 2.0f, 3240 * 0.8f)
        noteLabel.setAlignment(Align.center)
        stage.addActor(noteLabel)
    }

    /** Loads buttons  */
    private fun loadButton() {
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont30
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        val cancelButton = TextButton("Cancel", textButtonStyle)
        cancelButton.setSize(1200f, 300f)
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800.toFloat())
        cancelButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                returnToTrafficScreen()
            }
        })
        stage.addActor(cancelButton)

        val confirmButton = TextButton("Confirm", textButtonStyle)
        confirmButton.setSize(1200f, 300f)
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800.toFloat())
        confirmButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //TODO Update traffic flow
                returnToTrafficScreen()
            }
        })
        stage.addActor(confirmButton)
    }

    /** Overrides show method of basic screen */
    override fun show() {
        loadUI()
    }

    /** Return to traffic settings screen */
    private fun returnToTrafficScreen() {
        game.screen = TrafficSettingsScreen(game, TerminalControl.radarScreen, null)
    }
}