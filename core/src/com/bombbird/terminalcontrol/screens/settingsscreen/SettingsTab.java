package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager;

public class SettingsTab {
    private SettingsScreen settingsScreen;
    private Array<Actor> actors;

    public SettingsTab(SettingsScreen settingsScreen) {
        this.settingsScreen = settingsScreen;
        actors = new Array<>();
    }

    public void addActors(Actor... actorList) {
        for (Actor actor: actorList) {
            actors.add(actor);
            settingsScreen.stage.addActor(actor);
        }
    }

    public void updateVisibility(boolean visible) {
        for (Actor actor: actors) {
            if ("night2".equals(actor.getName()) && settingsScreen instanceof GameSettingsScreen) {
                actor.setVisible(visible && DayNightManager.isNightAvailable());
            } else if ("night".equals(actor.getName()) && settingsScreen instanceof GameSettingsScreen) {
                actor.setVisible(visible && DayNightManager.isNightAvailable() && ((GameSettingsScreen) settingsScreen).isAllowNight());
            } else {
                actor.setVisible(visible);
            }
        }
    }
}
