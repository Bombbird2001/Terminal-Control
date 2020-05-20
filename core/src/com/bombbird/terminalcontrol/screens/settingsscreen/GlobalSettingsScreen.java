package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

public class GlobalSettingsScreen extends SettingsTemplateScreen {
    private boolean sendCrash;
    private CheckBox sendCrashBox;
    private Label sendLabel;

    private SelectBox<String> zoom;
    private Label zoomLabel;
    private boolean increaseZoom;

    private SelectBox<String> autosave;
    private Label autosaveLabel;
    private int saveInterval;

    private SelectBox<String> defaultTab;
    private Label defaultTabLabel;
    private int defaultTabNo;

    public GlobalSettingsScreen(TerminalControl game, Image background) {
        super(game, null, background);

        infoString = "Set the global game settings below.";
        loadUI(-1200, -200);

        setOptions();
    }

    @Override
    public void setButtonListeners() {
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                background.scaleBy(-specialScale);
                game.setScreen(new MenuSettingsScreen(game, background));
            }
        });

        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendChanges();
                background.scaleBy(-specialScale);
                game.setScreen(new MenuSettingsScreen(game, background));
            }
        });
    }

    /** Loads selectBox for display settings */
    @Override
    public void loadBoxes() {
        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOn = TerminalControl.skin.getDrawable("Checked");
        checkBoxStyle.checkboxOff = TerminalControl.skin.getDrawable("Unchecked");
        checkBoxStyle.font = Fonts.defaultFont20;
        checkBoxStyle.fontColor = Color.WHITE;

        sendCrashBox = new CheckBox(" Send anonymous crash reports", checkBoxStyle);
        sendCrashBox.setPosition(5760 / 2f + 400, 3240 * 0.7f - 200);
        sendCrashBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendCrash = sendCrashBox.isChecked();
            }
        });
        stage.addActor(sendCrashBox);

        zoom = createStandardSelectBox();
        zoom.setItems("Off", "On");
        zoom.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                increaseZoom = "On".equals(zoom.getSelected());
            }
        });

        autosave = createStandardSelectBox();
        autosave.setItems("Never", "30 sec", "1 min", "2 mins", "5 mins");
        autosave.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = autosave.getSelected();
                if ("Never".equals(selected)) {
                    saveInterval = -1;
                } else if (selected.contains("sec")) {
                    saveInterval = Integer.parseInt(selected.split(" ")[0]);
                } else if (selected.contains("min")) {
                    saveInterval = Integer.parseInt(selected.split(" ")[0]) * 60;
                } else {
                    Gdx.app.log("Default settings", "Invalid autosave setting: " + selected);
                }
            }
        });

        defaultTab = createStandardSelectBox();
        defaultTab.setItems("Lateral", "Altitude", "Speed");
        defaultTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = defaultTab.getSelected();
                if ("Lateral".equals(selected)) {
                    defaultTabNo = 0;
                } else if ("Altitude".equals(selected)) {
                    defaultTabNo = 1;
                } else if ("Speed".equals(selected)) {
                    defaultTabNo = 2;
                } else {
                    Gdx.app.log(getClass().getName(), "Unknown default tab " + selected + " selected");
                }
            }
        });
    }

    /** Loads labels for display settings */
    @Override
    public void loadLabel() {
        super.loadLabel();

        sendLabel = new Label("Sending anonymous crash reports will allow\nus to improve your game experience.\nNo personal or device information will be\nsent.", labelStyle);
        sendLabel.setPosition(sendCrashBox.getX(), sendCrashBox.getY() - 475);
        stage.addActor(sendLabel);

        zoomLabel = new Label("Increased radar zoom: ", labelStyle);

        autosaveLabel = new Label("Autosave interval: ", labelStyle);

        defaultTabLabel = new Label("Default UI tab: ", labelStyle);
    }

    /** Loads actors for display settings into tabs */
    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this, 1);
        tab1.addActors(zoom, zoomLabel);
        tab1.addActors(autosave, autosaveLabel);
        tab1.addActors(defaultTab, defaultTabLabel);

        settingsTabs.add(tab1);
    }

    /** Sets relevant options into select boxes */
    @Override
    public void setOptions() {
        sendCrash = TerminalControl.sendAnonCrash;
        increaseZoom = TerminalControl.increaseZoom;
        saveInterval = TerminalControl.saveInterval;
        defaultTabNo = TerminalControl.defaultTabNo;

        sendCrashBox.setChecked(sendCrash);
        zoom.setSelected(increaseZoom ? "On" : "Off");
        if (saveInterval == -1) {
            autosave.setSelected("Never");
        } else if (saveInterval < 60) {
            autosave.setSelected(saveInterval + " sec");
        } else {
            int min = saveInterval / 60;
            autosave.setSelected(min + " min" + (min > 1 ? "s" : ""));
        }
        defaultTab.setSelectedIndex(defaultTabNo);
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        TerminalControl.sendAnonCrash = sendCrash;
        TerminalControl.increaseZoom = increaseZoom;
        TerminalControl.saveInterval = saveInterval;
        TerminalControl.defaultTabNo = defaultTabNo;

        GameSaver.saveSettings();
    }

    @Override
    public void updateTabs(boolean screenActive) {
        super.updateTabs(screenActive);
        sendCrashBox.setVisible(tab == 0);
        sendLabel.setVisible(tab == 0);
    }
}
