package com.bombbird.terminalcontrol.ui.dialogs

import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts

open class CustomDialog(title: String, val text: String, private var negative: String, var positive: String, val height: Int = 500, val width: Int = 1200, private val fontScale: Float = 1f): Dialog(title, TerminalControl.skin.get("defaultDialog", WindowStyle::class.java)) {
    companion object {
        //Dialog constants
        const val DIALOG_NEGATIVE = 0
        const val DIALOG_POSITIVE = 1
    }

    init {
        titleLabel.setAlignment(Align.top)
        titleLabel.setFontScale(fontScale)
        titleLabel.setScale(fontScale)
        buttonTable.defaults().width(5f / 12 * width).height(160f * fontScale).padLeft(0.025f * width).padRight(0.025f * width)
        isMovable = false
        initialize()
    }

    private fun initialize() {
        padTop(0.28f * height)
        padBottom(0.04f * height)

        updateText(text)
        generateButtons()

        isModal = true
    }

    fun updateText(newText: String) {
        contentTable.clearChildren()
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        val label = Label(newText, labelStyle)
        label.setScale(fontScale)
        label.setFontScale(fontScale)
        label.setAlignment(Align.center)
        text(label)
    }

    fun updateButtons(newNegative: String, newPositive: String) {
        negative = newNegative
        positive = newPositive
        generateButtons()
    }

    private fun generateButtons() {
        buttonTable.clearChildren()

        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont16
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        if (negative.isNotEmpty()) {
            val negativeButton = TextButton(negative, buttonStyle)
            negativeButton.label.setScale(fontScale)
            negativeButton.label.setFontScale(fontScale)
            button(negativeButton, DIALOG_NEGATIVE)
        }
        if (positive.isNotEmpty()) {
            val positiveButton = TextButton(positive, buttonStyle)
            positiveButton.label.setScale(fontScale)
            positiveButton.label.setFontScale(fontScale)
            button(positiveButton, DIALOG_POSITIVE)
        }
    }

    override fun getPrefHeight(): Float {
        return height.toFloat()
    }

    override fun getPrefWidth(): Float {
        return width.toFloat()
    }
}