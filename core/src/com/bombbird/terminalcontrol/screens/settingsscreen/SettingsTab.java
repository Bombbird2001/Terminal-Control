package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager;

public class SettingsTab {
    private static final int MAX_LENGTH = 4;
    private final int maxWidth;

    private final SettingsScreen settingsScreen;
    private final Array<Actor> actors;
    private int count;

    public SettingsTab(SettingsScreen settingsScreen, int maxWidth) {
        this.settingsScreen = settingsScreen;
        actors = new Array<>();
        this.maxWidth = maxWidth;
        count = 0;
    }

    public void addActors(SelectBox<String> box, Label label, Actor... actorList) {
        if (count >= MAX_LENGTH * maxWidth) throw new IllegalStateException("Tab has too many items: " + count + " of " + MAX_LENGTH * maxWidth);

        //Add box
        box.setX(5760 / 2f - 400 + settingsScreen.xOffset + (2400 * (float)(count / MAX_LENGTH)));
        box.setY(3240 * (0.8f - (count % MAX_LENGTH) * 0.15f) + settingsScreen.yOffset);
        actors.add(box);

        //Add label
        label.setX(box.getX() - 100 - label.getWidth());
        label.setY(box.getY() + box.getHeight() / 2 - label.getHeight() / 2);
        actors.add(label);

        //Add any other required actors with x, y position based on box position
        for (Actor actor: actorList) {
            actor.setX(actor.getX() + box.getX());
            actor.setY(actor.getY() + box.getY());
            actors.add(actor);
        }

        //Add all actors to stage
        for (Actor actor: actors) {
            settingsScreen.stage.addActor(actor);
        }
        count++;
    }

    public void updateVisibility(boolean visible) {
        //if (!DayNightManager.isNightAvailable() && settingsScreen instanceof GameSettingsScreen && !TerminalControl.full) settingsScreen.nextButton.setVisible(false); Temporary workaround, remove once more settings added
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
