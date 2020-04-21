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
    private final Image background;
    private static final float specialScale = 7.8f;

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
        advTrajTime = TerminalControl.advTraj;
        areaWarning = TerminalControl.areaWarning;
        collisionWarning = TerminalControl.collisionWarning;
        defaultTabNo = TerminalControl.defaultTabNo;

        loadUI(-1200, -200);

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

        backButton.setPosition(5760 / 2f - 2500, 3240 - 2800);
        nextButton.setPosition(5760 / 2f + 2000, 3240 - 2800);
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
        sendCrashBox.setPosition(5760 / 2f + 400, 3240 * 0.7f - 200);
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

        defaultTab = new SelectBox<>(selectBoxStyle);
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
                    Gdx.app.log("DefaultSettings", "Unknown default tab " + selected + " selected");
                }
            }
        });
        defaultTab.setSize(1200, 300);
        defaultTab.setAlignment(Align.center);
        defaultTab.getList().setAlignment(Align.center);
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

        sendLabel = new Label("Sending anonymous crash reports will allow\nus to improve your game experience.\nNo personal or device information will be\nsent.", labelStyle2);
        sendLabel.setPosition(sendCrashBox.getX(), sendCrashBox.getY() - 475);
        stage.addActor(sendLabel);

        zoomLabel = new Label("Increased radar zoom: ", labelStyle);

        autosaveLabel = new Label("Autosave interval: ", labelStyle);

        defaultTabLabel = new Label("Default UI tab: ", labelStyle);
    }

    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this, 1);
        tab1.addActors(trajectoryLine, trajectoryLabel);
        tab1.addActors(weather, weatherLabel);
        tab1.addActors(sound, soundLabel);
        tab1.addActors(emer, emerChanceLabel);
        settingsTabs.add(tab1);

        SettingsTab tab2 = new SettingsTab(this, 2);
        if (TerminalControl.full) {
            tab2.addActors(sweep, sweepLabel);
            tab2.addActors(advTraj, advTrajLabel);
            tab2.addActors(area, areaLabel);
            tab2.addActors(collision, collisionLabel);
        }
        tab2.addActors(zoom, zoomLabel);
        tab2.addActors(autosave, autosaveLabel);
        tab2.addActors(defaultTab, defaultTabLabel);
        settingsTabs.add(tab2);
    }

    @Override
    public void setOptions() {
        super.setOptions();
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
        TerminalControl.advTraj = advTrajTime;
        TerminalControl.areaWarning = areaWarning;
        TerminalControl.collisionWarning = collisionWarning;
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
