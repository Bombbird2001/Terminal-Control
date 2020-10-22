package com.bombbird.terminalcontrol.screens.upgradescreen

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Align
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.planesLanded
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.PauseScreen
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.utilities.Fonts

open class UpgradeScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    //Scroll table
    lateinit var scrollTable: Table

    /** Loads the UI elements to be rendered on screen  */
    override fun loadUI() {
        super.loadUI()
        loadButtons()
        loadLabel()
        loadScroll()
        loadUnlocks()
    }

    /** Loads back button  */
    override fun loadButtons() {
        //Set button textures
        val buttonStyle = TextButton.TextButtonStyle()
        buttonStyle.font = Fonts.defaultFont12
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up")
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down")

        //Set back button params
        backButton = TextButton("<= Back", buttonStyle)
        backButton.width = MainMenuScreen.BUTTON_WIDTH.toFloat()
        backButton.height = MainMenuScreen.BUTTON_HEIGHT.toFloat()
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f)
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go back to main menu
                val radarScreen = TerminalControl.radarScreen
                if (radarScreen == null) {
                    game.screen = MainMenuScreen(game, background)
                } else {
                    game.screen = PauseScreen(game, radarScreen)
                }
            }
        })
        stage.addActor(backButton)
    }

    /** Loads the appropriate title for label  */
    open fun loadLabel() {
        super.loadLabel("Milestones & Unlocks")

        //Set additional description label style
        val labelStyle1 = Label.LabelStyle()
        labelStyle1.font = Fonts.defaultFont12
        labelStyle1.fontColor = Color.WHITE

        //Set description label
        val label = Label("""
    Once an option is unlocked, you can visit the settings page to change to the desired option.
    Total planes landed: $planesLanded
    """.trimIndent(), labelStyle1)
        label.setPosition((2880 - label.width) / 2, 1620 * 0.75f)
        label.setAlignment(Align.center)
        stage.addActor(label)
    }

    /** Loads the scrollpane used to contain unlocks, milestones  */
    open fun loadScroll() {
        scrollTable = Table()
        val scrollPane = ScrollPane(scrollTable)
        scrollPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.8f
        scrollPane.y = 1620 * 0.2f
        scrollPane.width = MainMenuScreen.BUTTON_WIDTH * 1.6f
        scrollPane.height = 1620 * 0.5f
        scrollPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        stage.addActor(scrollPane)
    }

    /** Loads the unlocks into scroll pane  */
    open fun loadUnlocks() {
        //Set scroll pane label textures
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.WHITE
        labelStyle.background = TerminalControl.skin.getDrawable("Button_up")
        for ((key, required) in UnlockManager.unlockList) {
            val label = Label("""
                
                ${UnlockManager.unlockDescription[key]}
                
                """.trimIndent(), labelStyle)
            label.setWrap(true)
            label.setAlignment(Align.center)
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 1.1f)
            //Layout twice to set correct width & height
            scrollTable.layout()
            scrollTable.layout()
            val label1 = Label(planesLanded.coerceAtMost(required).toString() + "/" + required, labelStyle)
            label1.setAlignment(Align.center)
            scrollTable.add(label1).width(MainMenuScreen.BUTTON_WIDTH * 0.3f).height(label.height)
            val image = Image(TerminalControl.skin.getDrawable(if (planesLanded >= required) "Checked" else "Unchecked"))
            val ratio = MainMenuScreen.BUTTON_WIDTH * 0.15f / image.width
            scrollTable.add(image).width(MainMenuScreen.BUTTON_WIDTH * 0.15f).height(ratio * image.height).padLeft(MainMenuScreen.BUTTON_WIDTH * 0.025f).padRight(MainMenuScreen.BUTTON_WIDTH * 0.025f)
            scrollTable.row()
        }
    }
}