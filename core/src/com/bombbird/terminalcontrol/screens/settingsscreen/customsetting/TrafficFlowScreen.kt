package com.bombbird.terminalcontrol.screens.settingsscreen.customsetting

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.TrafficSettingsScreen
import com.bombbird.terminalcontrol.utilities.Fonts

/** Screen for managing traffic flow into airports */
class TrafficFlowScreen(val game: TerminalControl): BasicScreen(game, 5760, 3240) {
    companion object {
        const val NORMAL = 0
        const val PLANES_IN_CONTROL = 1
        const val FLOW_RATE = 2
    }

    var mode: Int = 0
    private val airportClosed: HashMap<String, SelectBox<String>> = HashMap()

    private lateinit var valueLabel: Label
    lateinit var modeBox: SelectBox<String>
    lateinit var valueBox: SelectBox<String>

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
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE

        val modeLabel = Label("Mode:", labelStyle)
        modeLabel.setAlignment(Align.right)

        valueLabel = Label("Planes:", labelStyle)
        valueLabel.setAlignment(Align.right)

        modeBox = createStandardBox()
        modeBox.setItems("Normal", "Planes in control", "Flow rate")
        modeBox.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                updateValueBoxChoices()
            }
        })

        valueBox = createStandardBox()

        val table = Table()
        table.setPosition(2880f, 3240 * 0.75f)
        table.align(Align.top)
        table.add(modeLabel).width(500f).height(300f).padRight(100f).padBottom(70f)
        table.add(modeBox).width(1000f).height(300f).padRight(380f).padBottom(70f)
        table.add(valueLabel).width(500f).height(300f).padRight(100f).padBottom(70f)
        table.add(valueBox).width(1000f).height(300f).padRight(380f).padBottom(70f).row()

        var index = 0
        for (airport: Airport in TerminalControl.radarScreen.airports.values) {
            val airportLabel = Label(airport.icao + ":", labelStyle)
            airportLabel.setAlignment(Align.right)

            val closedBox = createStandardBox()
            closedBox.setItems("Open", "Closed")
            airportClosed[airport.icao] = closedBox
            closedBox.selectedIndex = if (airport.isClosed) 1 else 0

            table.add(airportLabel).width(500f).height(300f).padRight(100f).padBottom(70f)
            table.add(closedBox).width(1000f).height(300f).padRight(380f).padBottom(70f)

            if (index >= 1) {
                table.row()
                index = 0
            } else index++
        }

        stage.addActor(table)

        modeBox.selectedIndex = TerminalControl.radarScreen.trafficMode
        updateValueBoxChoices()
    }

    /** Updates the visibility, values in the valueBox depending on the traffic mode selected */
    private fun updateValueBoxChoices() {
        valueBox.isVisible = modeBox.selectedIndex != NORMAL
        valueLabel.isVisible = modeBox.selectedIndex != NORMAL
        val array = Array<String>()
        when (modeBox.selectedIndex) {
            PLANES_IN_CONTROL -> {
                for (x in 0..60 step 5) array.add(x.toString())
                valueBox.items = array
                valueBox.selected = TerminalControl.radarScreen.maxPlanes.toString()
                valueLabel.setText("Planes:")
            }
            FLOW_RATE -> {
                for (x in 0..120 step 10) array.add(x.toString())
                valueBox.items = array
                valueBox.selected = TerminalControl.radarScreen.flowRate.toString()
                valueLabel.setText("Flow rate:\n(Arrivals/hour)")
            }
        }

    }

    /** Makes the standard selectBox for the UI */
    private fun createStandardBox(): SelectBox<String> {
        val selectBox = SelectBox<String>(SettingsTemplateScreen.selectBoxStyle)
        selectBox.setAlignment(Align.center)
        selectBox.list.setAlignment(Align.center)

        return selectBox
    }

    /** Loads heading label  */
    private fun loadLabel() {
        //Set label params
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont30
        labelStyle.fontColor = Color.WHITE

        val headerLabel = Label("Arrival Traffic", labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH.toFloat()
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT.toFloat()
        headerLabel.setPosition(5760 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 3240 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)

        val labelStyle1 = Label.LabelStyle()
        labelStyle1.font = Fonts.defaultFont20
        labelStyle1.fontColor = Color.WHITE
        val noteLabel = Label("Note: These settings affect only arrivals", labelStyle1)
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
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800f)
        cancelButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                returnToTrafficScreen()
            }
        })
        stage.addActor(cancelButton)

        val confirmButton = TextButton("Confirm", textButtonStyle)
        confirmButton.setSize(1200f, 300f)
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800f)
        confirmButton.addListener(object: ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                TerminalControl.radarScreen.trafficMode = modeBox.selectedIndex
                TerminalControl.radarScreen.flowRate = if (modeBox.selectedIndex == 2) (valueBox.selected?.toInt() ?: -1) else -1
                TerminalControl.radarScreen.maxPlanes = if (modeBox.selectedIndex == 1) (valueBox.selected?.toInt() ?: -1) else -1
                if (modeBox.selectedIndex == PLANES_IN_CONTROL) TerminalControl.radarScreen.planesToControl = valueBox.selected?.toFloat() ?: -1f
                for (airport: Map.Entry<String, SelectBox<String>> in airportClosed) {
                    TerminalControl.radarScreen.airports[airport.key]?.isClosed = airport.value.selectedIndex == 1
                }
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