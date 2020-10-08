package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager;
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.TrafficSettingsScreen;

public class SettingsTab {
    private static final int MAX_LENGTH = 4;
    private final int maxWidth;

    private final SettingsTemplateScreen settingsTemplateScreen;
    private final Array<Actor> actors;
    private int count;

    public SettingsTab(SettingsTemplateScreen settingsTemplateScreen, int maxWidth) {
        this.settingsTemplateScreen = settingsTemplateScreen;
        actors = new Array<>();
        this.maxWidth = maxWidth;
        count = 0;
    }

    public void addActors(SelectBox<String> box, Label label, Actor... actorList) {
        if (count >= MAX_LENGTH * maxWidth) throw new IllegalStateException("Tab has too many items: " + count + " of " + MAX_LENGTH * maxWidth);

        //Add box
        box.setX(5760 / 2f - 400 + settingsTemplateScreen.xOffset + (2400 * (float)(count / MAX_LENGTH)));
        box.setY(3240 * (0.8f - (count % MAX_LENGTH) * 0.15f) + settingsTemplateScreen.yOffset);
        actors.add(box);
        settingsTemplateScreen.stage.addActor(box);

        //Add label
        label.setX(box.getX() - 100 - label.getWidth());
        label.setY(box.getY() + box.getHeight() / 2 - label.getHeight() / 2);
        actors.add(label);
        settingsTemplateScreen.stage.addActor(label);

        //Add any other required actors with x, y position based on box position
        for (Actor actor: actorList) {
            actor.setX(actor.getX() + box.getX());
            actor.setY(actor.getY() + box.getY());
            actors.add(actor);
            settingsTemplateScreen.stage.addActor(actor);
        }

        count++;
    }

    public void addButton(TextButton textButton) {
        if (count >= MAX_LENGTH * maxWidth) throw new IllegalStateException("Tab has too many items: " + count + " of " + MAX_LENGTH * maxWidth);

        //Add box
        textButton.setX(5760 / 2f - 400 + settingsTemplateScreen.xOffset + (2400 * (float)(count / MAX_LENGTH)));
        textButton.setY(3240 * (0.8f - (count % MAX_LENGTH) * 0.15f) + settingsTemplateScreen.yOffset);
        actors.add(textButton);
        settingsTemplateScreen.stage.addActor(textButton);
        
        count++;
    }

    public void updateVisibility(boolean visible) {
        //if (!DayNightManager.isNightAvailable() && settingsScreen instanceof GameSettingsScreen && !TerminalControl.full) settingsScreen.nextButton.setVisible(false); Temporary workaround, remove once more settings added
        for (Actor actor: actors) {
            if ("night2".equals(actor.getName()) && settingsTemplateScreen instanceof TrafficSettingsScreen) {
                actor.setVisible(visible && DayNightManager.isNightAvailable());
            } else if ("night".equals(actor.getName()) && settingsTemplateScreen instanceof TrafficSettingsScreen) {
                actor.setVisible(visible && DayNightManager.isNightAvailable() && ((TrafficSettingsScreen) settingsTemplateScreen).isAllowNight());
            } else {
                actor.setVisible(visible);
            }
        }
    }
}
