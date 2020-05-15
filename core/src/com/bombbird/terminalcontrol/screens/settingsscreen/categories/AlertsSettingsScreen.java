package com.bombbird.terminalcontrol.screens.settingsscreen.categories;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

public class AlertsSettingsScreen extends SettingsTemplateScreen {
    public SelectBox<String> advTraj;
    public SelectBox<String> area;
    public SelectBox<String> collision;

    public Label advTrajLabel;
    public int advTrajTime;

    public Label areaLabel;
    public int areaWarning;

    public Label collisionLabel;
    public int collisionWarning;

    public AlertsSettingsScreen(TerminalControl game, RadarScreen radarScreen, Image background) {
        super(game, radarScreen, background);

        loadUI(-1200, 0);

        setOptions();
    }

    /** Loads selectBox for display settings */
    @Override
    public void loadBoxes() {
        advTraj = createStandardSelectBox();
        advTraj.setItems(UnlockManager.getTrajAvailable());
        advTraj.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Off".equals(advTraj.getSelected())) {
                    advTrajTime = -1;
                } else {
                    advTrajTime = Integer.parseInt(advTraj.getSelected().split(" ")[0]);
                }
            }
        });

        area = createStandardSelectBox();
        area.setItems(UnlockManager.getAreaAvailable());
        area.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Off".equals(area.getSelected())) {
                    areaWarning = -1;
                } else {
                    areaWarning = Integer.parseInt(area.getSelected().split(" ")[0]);
                }
            }
        });

        collision = createStandardSelectBox();
        collision.setItems(UnlockManager.getCollisionAvailable());
        collision.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Off".equals(collision.getSelected())) {
                    collisionWarning = -1;
                } else {
                    collisionWarning = Integer.parseInt(collision.getSelected().split(" ")[0]);
                }
            }
        });
    }

    /** Loads labels for display settings */
    @Override
    public void loadLabel() {
        super.loadLabel();
        advTrajLabel = new Label("Advanced\ntrajectory: ", labelStyle);
        areaLabel = new Label("Area\npenetration\nalert: ", labelStyle);
        collisionLabel = new Label("Collision alert: ", labelStyle);
    }

    /** Loads actors for display settings into tabs */
    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this, 2);
        tab1.addActors(advTraj, advTrajLabel);
        tab1.addActors(area, areaLabel);
        tab1.addActors(collision, collisionLabel);

        settingsTabs.add(tab1);
    }

    /** Sets relevant options into select boxes */
    @Override
    public void setOptions() {
        if (radarScreen == null) {
            advTrajTime = TerminalControl.advTraj;
            areaWarning = TerminalControl.areaWarning;
            collisionWarning = TerminalControl.collisionWarning;
        } else {
            advTrajTime = radarScreen.advTraj;
            areaWarning = radarScreen.areaWarning;
            collisionWarning = radarScreen.collisionWarning;
        }

        advTraj.setSelected(advTrajTime == -1 ? "Off" : advTrajTime + " sec");
        area.setSelected(areaWarning == -1 ? "Off" : areaWarning + " sec");
        collision.setSelected(collisionWarning == -1 ? "Off": collisionWarning + " sec");
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        if (radarScreen != null) {
            radarScreen.advTraj = advTrajTime;
            radarScreen.areaWarning = areaWarning;
            radarScreen.collisionWarning = collisionWarning;
        } else {
            TerminalControl.advTraj = advTrajTime;
            TerminalControl.areaWarning = areaWarning;
            TerminalControl.collisionWarning = collisionWarning;

            GameSaver.saveSettings();
        }
    }
}
