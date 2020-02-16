package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

public class DefaultSettingsScreen extends SettingsScreen {
    private Image background;
    private static float specialScale = 7.8f;

    private boolean sendCrash;
    private CheckBox sendCrashBox;

    private SelectBox<String> zoom;
    private Label zoomLabel;
    private boolean increaseZoom;

    private SelectBox<String> autosave;
    private Label autosaveLabel;
    private int saveInterval;

    public DefaultSettingsScreen(final TerminalControl game, Image background) {
        super(game);

        this.background = background;
        this.background.scaleBy(specialScale);

        trajectorySel = TerminalControl.trajectorySel;
        radarSweep = TerminalControl.radarSweep;
        weatherSel = TerminalControl.weatherSel;
        soundSel = TerminalControl.soundSel;
        sendCrash = TerminalControl.sendAnonCrash;
        emerChance = TerminalControl.emerChance;
        increaseZoom = TerminalControl.increaseZoom;
        saveInterval = TerminalControl.saveInterval;
        areaWarning = TerminalControl.areaWarning;
        collisionWarning = TerminalControl.collisionWarning;

        loadUI(-600, -200);

        setOptions();
    }

    @Override
    public void loadUI(int xOffset, int yOffset) {
        stage.addActor(background);
        super.loadUI(xOffset, yOffset);
    }

    @Override
    public void loadButton() {
        super.loadButton();
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Back to main menu
                background.scaleBy(-specialScale);
                game.setScreen(new MainMenuScreen(game, background));
                dispose();
            }
        });

        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendChanges();
                background.scaleBy(-specialScale);
                game.setScreen(new MainMenuScreen(game, background));
                dispose();
            }
        });

        backButton.setPosition(5760 / 2f - 2500, 3240 - 2300);
        nextButton.setPosition(5760 / 2f + 500, 3240 - 2300);
    }

    @Override
    public void loadBoxes() {
        super.loadBoxes();

        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOn = TerminalControl.skin.getDrawable("Checked");
        checkBoxStyle.checkboxOff = TerminalControl.skin.getDrawable("Unchecked");
        checkBoxStyle.font = Fonts.defaultFont20;
        checkBoxStyle.fontColor = Color.WHITE;

        sendCrashBox = new CheckBox(" Send anonymous crash reports", checkBoxStyle);
        sendCrashBox.setPosition(5760 / 2f + 1000, 3240 * 0.7f - 200);
        sendCrashBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendCrash = sendCrashBox.isChecked();
            }
        });
        stage.addActor(sendCrashBox);

        zoom = new SelectBox<>(selectBoxStyle);
        zoom.setItems("Off", "On");
        zoom.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                increaseZoom = "On".equals(zoom.getSelected());
            }
        });
        zoom.setSize(1200, 300);
        zoom.setAlignment(Align.center);
        zoom.getList().setAlignment(Align.center);

        autosave = new SelectBox<>(selectBoxStyle);
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
        autosave.setSize(1200, 300);
        autosave.setAlignment(Align.center);
        autosave.getList().setAlignment(Align.center);
    }

    @Override
    public void loadLabel() {
        super.loadLabel();

        Label.LabelStyle labelStyle2 = new Label.LabelStyle();
        labelStyle2.font = Fonts.defaultFont20;
        labelStyle2.fontColor = Color.WHITE;

        Label infoLabel = new Label("Set the default game settings below. You can still change some of these settings for individual games.", labelStyle2);
        infoLabel.setPosition(5760 / 2f - infoLabel.getWidth() / 2f, 3240 - 300);
        stage.addActor(infoLabel);

        Label sendLabel = new Label("Sending anonymous crash reports will allow\nus to improve your game experience.\nNo personal or device information will be\nsent.", labelStyle2);
        sendLabel.setPosition(sendCrashBox.getX(), sendCrashBox.getY() - 475);
        stage.addActor(sendLabel);

        zoomLabel = new Label("Increased radar zoom: ", labelStyle);

        autosaveLabel = new Label("Autosave interval:", labelStyle);
    }

    @Override
    public void loadTabs(int xOffset, int yOffset) {
        SettingsTab tab1 = new SettingsTab(this, 1);
        tab1.addActors(trajectoryLine, trajectoryLabel, xOffset, yOffset);
        tab1.addActors(weather, weatherLabel, xOffset, yOffset);
        tab1.addActors(sound, soundLabel, xOffset, yOffset);
        tab1.addActors(emer, emerChanceLabel, xOffset, yOffset);
        settingsTabs.add(tab1);

        SettingsTab tab2 = new SettingsTab(this, 1);
        if (TerminalControl.full) {
            tab2.addActors(sweep, sweepLabel, xOffset, yOffset);
            tab2.addActors(area, areaLabel, xOffset, yOffset);
            tab2.addActors(collision, collisionLabel, xOffset, yOffset);
            tab2.addActors(zoom, zoomLabel, xOffset, yOffset);
            settingsTabs.add(tab2);
            SettingsTab tab3 = new SettingsTab(this, 1);
            tab3.addActors(autosave, autosaveLabel, xOffset, yOffset);
            settingsTabs.add(tab3);
        } else {
            tab2.addActors(zoom, zoomLabel, xOffset, yOffset);
            tab2.addActors(autosave, autosaveLabel, xOffset, yOffset);
            settingsTabs.add(tab2);
        }
    }

    @Override
    public void setOptions() {
        super.setOptions();
        sendCrashBox.setChecked(TerminalControl.sendAnonCrash);
        zoom.setSelected(TerminalControl.increaseZoom ? "On" : "Off");
        if (TerminalControl.saveInterval == -1) {
            autosave.setSelected("Never");
        } else if (TerminalControl.saveInterval < 60) {
            autosave.setSelected(TerminalControl.saveInterval + " sec");
        } else {
            int min = TerminalControl.saveInterval / 60;
            autosave.setSelected(min + " min" + (min > 1 ? "s" : ""));
        }
    }

    @Override
    public void sendChanges() {
        TerminalControl.trajectorySel = trajectorySel;
        TerminalControl.weatherSel = weatherSel;
        TerminalControl.soundSel = soundSel;
        TerminalControl.sendAnonCrash = sendCrash;
        TerminalControl.increaseZoom = increaseZoom;
        TerminalControl.emerChance = emerChance;
        TerminalControl.saveInterval = saveInterval;
        TerminalControl.radarSweep = radarSweep;
        TerminalControl.areaWarning = areaWarning;
        TerminalControl.collisionWarning = collisionWarning;

        GameSaver.saveSettings();
    }
}
