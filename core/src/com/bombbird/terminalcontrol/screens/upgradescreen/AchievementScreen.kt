package com.bombbird.terminalcontrol.screens.upgradescreen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.utilities.Fonts

class AchievementScreen(game: TerminalControl, background: Image?) : UpgradeScreen(game, background) {
    override fun loadLabel() {
        //Set title label style
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE

        //Set title label
        val headerLabel = Label("Achievements", labelStyle)
        headerLabel.width = MainMenuScreen.BUTTON_WIDTH.toFloat()
        headerLabel.height = MainMenuScreen.BUTTON_HEIGHT.toFloat()
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f)
        headerLabel.setAlignment(Align.center)
        stage.addActor(headerLabel)
    }

    /** Loads the scrollpane used to contain unlocks, milestones  */
    override fun loadScroll() {
        scrollTable = Table()
        val scrollPane = ScrollPane(scrollTable)
        scrollPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.8f
        scrollPane.y = 1620 * 0.25f
        scrollPane.width = MainMenuScreen.BUTTON_WIDTH * 1.6f
        scrollPane.height = 1620 * 0.55f
        scrollPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        stage.addActor(scrollPane)
    }

    /** Loads the achievements into scroll pane  */
    override fun loadUnlocks() {
        //Set scroll pane label textures
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.WHITE
        labelStyle.background = TerminalControl.skin.getDrawable("Button_up")
        for ((_, value1) in UnlockManager.achievementList) {
            val label = Label("""
[ ${value1.title} ]
${value1.description}
""", labelStyle)
            label.setWrap(true)
            label.setAlignment(Align.center)
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 1.1f)
            //Layout twice to set correct width & height
            scrollTable.layout()
            scrollTable.layout()
            val value: Int = value1.currentValue
            val needed: Int = value1.valueNeeded
            val label1 = Label(if (value >= 0) value.coerceAtMost(needed).toString() + "/" + needed else "- / -", labelStyle)
            label1.setAlignment(Align.center)
            scrollTable.add(label1).width(MainMenuScreen.BUTTON_WIDTH * 0.3f).height(label.height)
            val image = Image(TerminalControl.skin.getDrawable(if (value1.isUnlocked) "Checked" else "Unchecked"))
            val ratio = MainMenuScreen.BUTTON_WIDTH * 0.15f / image.width
            scrollTable.add(image).width(MainMenuScreen.BUTTON_WIDTH * 0.15f).height(ratio * image.height).padLeft(MainMenuScreen.BUTTON_WIDTH * 0.025f).padRight(MainMenuScreen.BUTTON_WIDTH * 0.025f)
            scrollTable.row()
        }
        for ((key, value) in UnlockManager.easterEggList) {
            val unlocked = UnlockManager.unlocks.contains(key)
            val label = Label("""
[ $key ]
${if (unlocked) value else "?????"}
""", labelStyle)
            label.setWrap(true)
            label.setAlignment(Align.center)
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 1.1f)
            //Layout twice to set correct width & height
            scrollTable.layout()
            scrollTable.layout()
            val label1 = Label("- / -", labelStyle)
            label1.setAlignment(Align.center)
            scrollTable.add(label1).width(MainMenuScreen.BUTTON_WIDTH * 0.3f).height(label.height)
            val image = Image(TerminalControl.skin.getDrawable(if (unlocked) "Checked" else "Unchecked"))
            val ratio = MainMenuScreen.BUTTON_WIDTH * 0.15f / image.width
            scrollTable.add(image).width(MainMenuScreen.BUTTON_WIDTH * 0.15f).height(ratio * image.height).padLeft(MainMenuScreen.BUTTON_WIDTH * 0.025f).padRight(MainMenuScreen.BUTTON_WIDTH * 0.025f)
            scrollTable.row()
        }
    }
}