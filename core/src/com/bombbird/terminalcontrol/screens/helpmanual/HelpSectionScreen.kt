package com.bombbird.terminalcontrol.screens.helpmanual

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.StandardUIScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.AirportHelpScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.HelpScreen

class HelpSectionScreen(game: TerminalControl, background: Image?, private val page: String) : StandardUIScreen(game, background) {
    private val scrollTable: Table = Table()

    override fun loadUI() {
        super.loadUI()
        loadLabel(page)
        loadScroll()
        loadContent()
        loadButtons()
    }

    private fun loadScroll() {
        val scrollPane = ScrollPane(scrollTable)
        scrollPane.x = 2880 / 2f - MainMenuScreen.BUTTON_WIDTH
        scrollPane.y = 1620 * 0.2f
        scrollPane.width = MainMenuScreen.BUTTON_WIDTH * 2f
        scrollPane.height = 1620 * 0.6f
        scrollPane.style.background = TerminalControl.skin.getDrawable("ListBackground")
        stage.addActor(scrollPane)
    }

    private fun loadContent() {
        HelpManager.loadContent(scrollTable, page)
    }

    /** Loads the default button styles and back button  */
    override fun loadButtons() {
        super.loadButtons()
        removeDefaultBackButtonChangeListener()
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go back to main menu
                if (page.length == 4) {
                    game.screen = AirportHelpScreen(game, background)
                } else {
                    game.screen = HelpScreen(game, background)
                }
            }
        })
    }
}