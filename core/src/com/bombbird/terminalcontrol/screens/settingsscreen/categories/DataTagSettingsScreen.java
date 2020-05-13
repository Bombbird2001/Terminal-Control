package com.bombbird.terminalcontrol.screens.settingsscreen.categories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

public class DataTagSettingsScreen extends SettingsTemplateScreen {
    public SelectBox<String> dataTag;

    public Label dataTagLabel;
    public boolean compactData;

    public DataTagSettingsScreen(TerminalControl game, RadarScreen radarScreen, Image background) {
        super(game, radarScreen, background);

        loadUI(-1200, 0);

        setOptions();
    }

    /** Loads selectBox for display settings */
    @Override
    public void loadBoxes() {
        dataTag = createStandardSelectBox();
        dataTag.setItems("Default", "Compact");
        dataTag.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Default".equals(dataTag.getSelected())) {
                    compactData = false;
                } else if ("Compact".equals(dataTag.getSelected())) {
                    compactData = true;
                } else {
                    Gdx.app.log("SettingsScreen", "Unknown data tag setting " + dataTag.getSelected());
                }
            }
        });
    }

    /** Loads labels for display settings */
    @Override
    public void loadLabel() {
        super.loadLabel();
        dataTagLabel = new Label("Data tag style: ", labelStyle);
    }

    /** Loads actors for display settings into tabs */
    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this, 2);
        tab1.addActors(dataTag, dataTagLabel);

        settingsTabs.add(tab1);
    }

    /** Sets relevant options into select boxes */
    @Override
    public void setOptions() {
        if (radarScreen == null) {
            compactData = TerminalControl.compactData;
        } else {
            compactData = radarScreen.compactData;
        }

        dataTag.setSelected(compactData ? "Compact" : "Default");
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        if (radarScreen != null) {
            radarScreen.compactData = compactData;
        } else {
            TerminalControl.compactData = compactData;

            GameSaver.saveSettings();
        }
    }
}
