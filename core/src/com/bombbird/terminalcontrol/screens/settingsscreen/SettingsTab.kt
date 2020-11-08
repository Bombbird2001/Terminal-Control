package com.bombbird.terminalcontrol.screens.settingsscreen

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager.isNightAvailable
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.TrafficSettingsScreen

class SettingsTab(private val settingsTemplateScreen: SettingsTemplateScreen, private val maxWidth: Int) {
    companion object {
        private const val MAX_LENGTH = 4
    }

    private val actors: Array<Actor> = Array()
    private var count: Int = 0

    fun addActors(box: SelectBox<String>, label: Label, vararg actorList: Actor) {
        check(count < MAX_LENGTH * maxWidth) { "Tab has too many items: " + count + " of " + MAX_LENGTH * maxWidth }

        //Add box
        box.x = 5760 / 2f - 400 + settingsTemplateScreen.xOffset + 2400 * (count / MAX_LENGTH).toFloat()
        box.y = 3240 * (0.8f - count % MAX_LENGTH * 0.15f) + settingsTemplateScreen.yOffset
        actors.add(box)
        settingsTemplateScreen.stage.addActor(box)

        //Add label
        label.x = box.x - 100 - label.width
        label.y = box.y + box.height / 2 - label.height / 2
        actors.add(label)
        settingsTemplateScreen.stage.addActor(label)

        //Add any other required actors with x, y position based on box position
        for (actor in actorList) {
            actor.x = actor.x + box.x
            actor.y = actor.y + box.y
            actors.add(actor)
            settingsTemplateScreen.stage.addActor(actor)
        }
        count++
    }

    fun addButton(textButton: TextButton) {
        check(count < MAX_LENGTH * maxWidth) { "Tab has too many items: " + count + " of " + MAX_LENGTH * maxWidth }

        //Add box
        textButton.x = 5760 / 2f - 400 + settingsTemplateScreen.xOffset + 2400 * (count / MAX_LENGTH).toFloat()
        textButton.y = 3240 * (0.8f - count % MAX_LENGTH * 0.15f) + settingsTemplateScreen.yOffset
        actors.add(textButton)
        settingsTemplateScreen.stage.addActor(textButton)
        count++
    }

    fun updateVisibility(visible: Boolean) {
        //if (!DayNightManager.isNightAvailable() && settingsScreen is GameSettingsScreen && !TerminalControl.full) settingsScreen.nextButton.setVisible(false); Temporary workaround, remove once more settings added
        for (actor in actors) {
            if ("night2" == actor.name && settingsTemplateScreen is TrafficSettingsScreen) {
                actor.isVisible = visible && isNightAvailable
            } else if ("night" == actor.name && settingsTemplateScreen is TrafficSettingsScreen) {
                actor.isVisible = visible && isNightAvailable && settingsTemplateScreen.isAllowNight()
            } else {
                actor.isVisible = visible
            }
        }
    }
}