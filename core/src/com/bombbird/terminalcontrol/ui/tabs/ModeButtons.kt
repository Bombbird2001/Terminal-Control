package com.bombbird.terminalcontrol.ui.tabs

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.ui.Ui
import com.bombbird.terminalcontrol.utilities.Fonts
import java.util.*

class ModeButtons(private val tab: Tab) {
    var mode = 0
    private val buttons: HashMap<Int, TextButton> = HashMap()
    private val inactiveButtons: HashSet<Int> = HashSet()

    /** Initializes the button with its mode code and display text  */
    fun addButton(code: Int, text: String?) {
        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.font = Fonts.defaultFont20
        textButtonStyle.fontColor = Color.BLACK
        textButtonStyle.up = Ui.lightBoxBackground
        textButtonStyle.down = Ui.lightBoxBackground
        val button = TextButton(text, textButtonStyle)
        button.name = code.toString()
        button.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                if (!isInactive(actor.name.toInt())) {
                    mode = actor.name.toInt()
                    tab.choiceMade()
                }
                event.handle()
            }
        })
        val column = buttons.size % 3
        val row = buttons.size / 3
        tab.addActor(button, 0.1f + column * 0.275f, 0.25f, 2240 - 325 * row.toFloat(), 300f)
        button.label.wrap = true
        buttons[code] = button
    }

    /** Changes the text on the button  */
    fun changeButtonText(code: Int, text: String?) {
        if (!buttons.containsKey(code)) return
        buttons[code]?.setText(text)
    }

    /** Sets the currently selected button font as yellow when mode has changed  */
    fun setButtonColour(modeChanged: Boolean) {
        for (textButton in buttons.values) {
            if (textButton.name == mode.toString()) {
                textButton.style.down = TerminalControl.skin.getDrawable("Button_down_sharp")
                textButton.style.up = TerminalControl.skin.getDrawable("Button_down_sharp")
                textButton.style.fontColor = if (modeChanged) Color.YELLOW else Color.WHITE
            } else {
                textButton.style.fontColor = Color.BLACK
            }
        }
    }

    /** Checks whether the mode for each button exists in the aircraft's allowed modes, sets activity  */
    fun updateButtonActivity(modes: Array<String>) {
        val allowedModes = HashSet<Int>()
        for (mode in modes) {
            allowedModes.add(NavState.getCodeFromString(mode))
        }
        for (textButton in buttons.values) {
            val code = textButton.name.toInt()
            if (allowedModes.contains(code)) {
                setActive(code)
            } else {
                setInactive(code)
            }
        }
    }

    /** Sets the button with the code to be inactive (greyed out)  */
    private fun setInactive(button: Int) {
        inactiveButtons.add(button)
        buttons[button]?.style?.down = TerminalControl.skin.getDrawable("ListBackground")
        buttons[button]?.style?.up = TerminalControl.skin.getDrawable("ListBackground")
    }

    /** Sets the button with the code to be active  */
    private fun setActive(button: Int) {
        inactiveButtons.remove(button)
        buttons[button]?.style?.down = Ui.lightBoxBackground
        buttons[button]?.style?.up = Ui.lightBoxBackground
    }

    /** Checks whether the button with the code is inactive  */
    private fun isInactive(button: Int): Boolean {
        return inactiveButtons.contains(button)
    }
}