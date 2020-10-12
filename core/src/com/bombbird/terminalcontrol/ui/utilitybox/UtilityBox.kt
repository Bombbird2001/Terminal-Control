package com.bombbird.terminalcontrol.ui.utilitybox

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts
import org.json.JSONArray
import kotlin.collections.HashMap

class UtilityBox {
    private val header: Label

    private val labelStyleStorage: HashMap<String, Label.LabelStyle> = HashMap()

    private val toggleButton: TextButton

    val commsManager: CommsManager
    val commsTable: Table
    val commsPane: ScrollPane

    val statusManager: StatusManager
    val statusTable: Table
    val statusPane: ScrollPane
    val statusLabel: Label

    init {
        header = Label("Communications", getLabelStyle(Color.WHITE))
        header.setAlignment(Align.center)
        header.setPosition(TerminalControl.radarScreen.ui.paneWidth / 2f - header.width / 2f, 3240 * 0.42f)
        TerminalControl.radarScreen.uiStage.addActor(header)

        commsManager = CommsManager(this)
        commsTable = Table()
        commsPane = ScrollPane(commsTable)
        commsPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        var inputListener: InputListener? = null
        for (eventListener in commsPane.listeners) {
            if (eventListener is InputListener) {
                inputListener = eventListener
                break
            }
        }
        if (inputListener != null) commsPane.removeListener(inputListener)
        TerminalControl.radarScreen.ui.addActor(commsPane, 0.1f, 0.8f, 3240 * 0.05f, 3240 * 0.35f)

        statusManager = StatusManager(this)
        statusTable = Table()
        statusPane = ScrollPane(statusTable)
        statusPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        for (eventListener in statusPane.listeners) {
            if (eventListener is InputListener) {
                inputListener = eventListener
                break
            }
        }
        if (inputListener != null) statusPane.removeListener(inputListener)
        statusPane.isVisible = false
        TerminalControl.radarScreen.ui.addActor(statusPane, 0.1f, 0.8f, 3240 * 0.05f, 3240 * 0.35f)

        statusLabel = Label("Loading statuses...", getLabelStyle(Color.WHITE))
        statusLabel.setWrap(true)
        statusTable.add(statusLabel).width(commsPane.width - 20).pad(15f, 10f, 15f, 10f).actor.invalidate()

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.up = TerminalControl.skin.getDrawable("ListBackground")
        textButtonStyle.down = TerminalControl.skin.getDrawable("ListBackground")
        textButtonStyle.font = Fonts.defaultFont20
        textButtonStyle.fontColor = Color.BLACK
        toggleButton = TextButton("< >", textButtonStyle)
        toggleButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                commsPane.isVisible = !commsPane.isVisible
                statusPane.isVisible = !statusPane.isVisible
                statusManager.active = statusPane.isVisible
                header.setText(if (statusManager.active) "Status" else "Communications")
                updateHeaderPosition()
                event.handle()
            }
        })
        TerminalControl.radarScreen.ui.addActor(toggleButton, 0.8f, 0.15f, 3240 * 0.42f - 25, 300f)
    }

    fun updateHeaderPosition() {
        header.setPosition(TerminalControl.radarScreen.ui.paneWidth / 2f - header.width / 2f, 3240 * 0.42f)
    }

    fun loadSave(save: JSONArray) {
        commsManager.loadLabels(save)
    }

    fun getLabelStyle(color: Color): Label.LabelStyle {
        if (labelStyleStorage.containsKey(color.toString())) {
            labelStyleStorage[color.toString()]?.let {
                return it
            }
        }
        val labelStyle = Label.LabelStyle()
        labelStyle.fontColor = color
        labelStyle.font = Fonts.defaultFont20
        labelStyleStorage[color.toString()] = labelStyle
        return labelStyle
    }

    fun setVisible(show: Boolean) {
        commsPane.isVisible = show && !statusManager.active
        val statusWasVisible = statusPane.isVisible
        statusPane.isVisible = show && statusManager.active
        if (!statusWasVisible && statusPane.isVisible) statusManager.timer = -1f //Reset timer if status pane just became visible
        header.isVisible = show
        toggleButton.isVisible = show
    }
}