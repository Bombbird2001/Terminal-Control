package com.bombbird.terminalcontrol.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Timer
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.Fonts

class NotifScreen(game: TerminalControl, background: Image?) : StandardUIScreen(game, background) {
    companion object {
        private val notifArray = Array<String>()
    }

    private val timer: Timer = Timer()

    init {
        loadNotifs()
    }

    /** Loads all the notifications needed  */
    private fun loadNotifs() {
        notifArray.add("As part of a recommendation by an aviation authority, in order to reduce resemblance to real life airports, " +
                "airport names, airport ICAO codes, SID/STAR names, as well as waypoint names have been changed. However, the procedures " +
                "themselves remain the same and is still based on that of the real airport. Saves will remain compatible - existing planes " +
                "will continue using old SIDs and STARs, while newly generated ones will use the new SIDs and STARs. " +
                "Old saves that have not been loaded for 6 months or more may not be compatible. We apologise for the inconvenience, " +
                "and we thank you for your understanding and continued support of Terminal Control.")
    }

    /** Loads the full UI of this screen  */
    override fun loadUI() {
        super.loadUI()
        background?.isVisible = false
        loadLabel()
        loadButtons()
    }

    /** Loads labels for credits, disclaimers, etc  */
    fun loadLabel() {
        val labelStyle = Label.LabelStyle()
        labelStyle.font = Fonts.defaultFont12
        labelStyle.fontColor = Color.WHITE
        val notif = Label("""
            Hello players, here's a message from the developer:
            
            ${notifArray[TerminalControl.LATEST_REVISION - 1]}
            """.trimIndent(), labelStyle)
        notif.wrap = true
        notif.width = 2000f
        notif.setPosition(1465 - notif.width / 2f, 800f)
        stage.addActor(notif)
    }

    /** Loads the default button styles and back button  */
    override fun loadButtons() {
        super.loadButtons()
        removeDefaultBackButtonChangeListener()
        backButton.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent, actor: Actor) {
                //Go back to main menu
                TerminalControl.updateRevision()
                game.screen = MainMenuScreen(game, background)
            }
        })
        backButton.isVisible = false
        timer.scheduleTask(object : Timer.Task() {
            override fun run() {
                Gdx.app.postRunnable { backButton.isVisible = true }
            }
        }, 5f)
    }
}