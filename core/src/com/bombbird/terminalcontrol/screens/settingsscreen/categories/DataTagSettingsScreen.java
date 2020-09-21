package com.bombbird.terminalcontrol.screens.settingsscreen.categories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

public class DataTagSettingsScreen extends SettingsTemplateScreen {
    public SelectBox<String> dataTag;
    public SelectBox<String> bordersBackground;
    public SelectBox<String> lineSpacing;

    public Label dataTagLabel;
    public boolean compactData;

    public Label bordersBackgroundLabel;
    public boolean alwaysShowBordersBackground;

    public Label lineSpacingLabel;
    public int lineSpacingValue;

    public DataTagSettingsScreen(TerminalControl game, RadarScreen radarScreen, Image background) {
        super(game, radarScreen, background);

        infoString = "Set the data tag display options below.";
        loadUI(-1200, -200);

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
                    Gdx.app.log(className, "Unknown data tag setting " + dataTag.getSelected());
                }
            }
        });

        bordersBackground = createStandardSelectBox();
        bordersBackground.setItems("Always", "When selected");
        bordersBackground.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Always".equals(bordersBackground.getSelected())) {
                    alwaysShowBordersBackground = true;
                } else if ("When selected".equals(bordersBackground.getSelected())) {
                    alwaysShowBordersBackground = false;
                } else {
                    Gdx.app.log(className, "Unknown show borders setting " + dataTag.getSelected());
                }
            }
        });

        lineSpacing = createStandardSelectBox();
        lineSpacing.setItems("Compact", "Default", "Large");
        lineSpacing.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lineSpacingValue = lineSpacing.getSelectedIndex();
            }
        });
    }

    /** Loads labels for display settings */
    @Override
    public void loadLabel() {
        super.loadLabel();
        dataTagLabel = new Label("Data tag style: ", labelStyle);
        bordersBackgroundLabel = new Label("Show data tag border\nand background: ", labelStyle);
        lineSpacingLabel = new Label("Row spacing", labelStyle);
    }

    /** Loads actors for display settings into tabs */
    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this, 2);
        tab1.addActors(dataTag, dataTagLabel);
        tab1.addActors(bordersBackground, bordersBackgroundLabel);
        tab1.addActors(lineSpacing, lineSpacingLabel);

        settingsTabs.add(tab1);
    }

    /** Sets relevant options into select boxes */
    @Override
    public void setOptions() {
        if (radarScreen == null) {
            compactData = TerminalControl.compactData;
            alwaysShowBordersBackground = TerminalControl.alwaysShowBordersBackground;
            lineSpacingValue = TerminalControl.lineSpacingValue;
        } else {
            compactData = radarScreen.compactData;
            alwaysShowBordersBackground = radarScreen.alwaysShowBordersBackground;
            lineSpacingValue = radarScreen.lineSpacingValue;
        }

        dataTag.setSelected(compactData ? "Compact" : "Default");
        bordersBackground.setSelected(alwaysShowBordersBackground ? "Always" : "When selected");
        lineSpacing.setSelectedIndex(lineSpacingValue);
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        if (radarScreen != null) {
            radarScreen.compactData = compactData;
            radarScreen.alwaysShowBordersBackground = alwaysShowBordersBackground;
            boolean lineSpacingChanged = radarScreen.lineSpacingValue != lineSpacingValue;
            radarScreen.lineSpacingValue = lineSpacingValue;

            //Apply the new line spacing to label
            if (lineSpacingChanged) {
                Label.LabelStyle newLabelStyle = new Label.LabelStyle();
                newLabelStyle.fontColor = Color.WHITE;
                switch (radarScreen.lineSpacingValue) {
                    case 0:
                        newLabelStyle.font = Fonts.compressedFont6;
                        break;
                    case 1:
                        newLabelStyle.font = Fonts.defaultFont6;
                        break;
                    case 2:
                        newLabelStyle.font = Fonts.expandedFont6;
                        break;
                    default:
                        newLabelStyle.font = Fonts.defaultFont6;
                        Gdx.app.log(getClass().getName(), "Unknown radarScreen line spacing value " + radarScreen.lineSpacingValue);
                }
                for (Aircraft aircraft: radarScreen.aircrafts.values()) {
                    //Apply the changes to all
                    aircraft.getDataTag().updateLabelStyle(newLabelStyle);
                }
            }
        } else {
            TerminalControl.compactData = compactData;
            TerminalControl.alwaysShowBordersBackground = alwaysShowBordersBackground;
            TerminalControl.lineSpacingValue = lineSpacingValue;

            GameSaver.saveSettings();
        }
    }
}
