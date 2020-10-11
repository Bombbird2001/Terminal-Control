package com.bombbird.terminalcontrol.ui.utilitybox

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts
import org.json.JSONArray
import kotlin.collections.HashMap

class UtilityBox() {
    private val header: Label

    private val labelStyleStorage: HashMap<String, Label.LabelStyle> = HashMap()

    val commsManager: CommsManager
    val commsTable: Table
    val commsPane: ScrollPane

    init {
        header = Label("Communications", getLabelStyle(Color.WHITE))
        header.setPosition(0.5f * TerminalControl.radarScreen.ui.paneWidth - header.width / 2, 3240 * 0.42f)
        TerminalControl.radarScreen.ui.addActor(header, 0.3f, 0.3f, 3240 * 0.42f, 50f)

        commsManager = CommsManager(this)
        commsTable = Table()
        commsPane = ScrollPane(commsTable)
        commsPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        var inputListener: InputListener? = null
        for (eventListener in commsPane.listeners) {
            if (eventListener is InputListener) {
                inputListener = eventListener
            }
        }
        if (inputListener != null) commsPane.removeListener(inputListener)
        TerminalControl.radarScreen.ui.addActor(commsPane, 0.1f, 0.8f, 3240 * 0.05f, 3240 * 0.35f)
    }

    constructor(save: JSONArray) : this() {
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

    fun updateHeaderWidth(paneWidth: Float) {
        header.x = 0.5f * paneWidth - header.width / 2
    }

    fun setVisible(show: Boolean) {
        commsPane.isVisible = show
        header.isVisible = show
    }

    fun remove() {
        commsPane.remove()
        header.remove()
    }
}