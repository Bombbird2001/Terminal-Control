package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.Locale;

public class GameSettingsScreen extends SettingsScreen {
    private RadarScreen radarScreen;

    private SelectBox<String> speed;
    private SelectBox<String> tfcMode;
    private SelectBox<String> night;
    private SelectBox<String> nightStartHour;
    private SelectBox<String> nightStartMin;
    private SelectBox<String> nightEndHour;
    private SelectBox<String> nightEndMin;

    private Label speedLabel;
    private int speedSel;

    private Label tfcLabel;
    private RadarScreen.TfcMode tfcSel;

    private Label nightLabel;
    private Label timeLabel;
    private Label timeLabel2;
    private boolean allowNight;
    private int nightStart;
    private int nightEnd;

    public GameSettingsScreen(final TerminalControl game, final RadarScreen radarScreen) {
        super(game);

        this.radarScreen = radarScreen;

        loadUI(-1200, 0);
    }

    @Override
    public void loadButton() {
        super.loadButton();
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                radarScreen.setGameState(GameScreen.State.PAUSE);
            }
        });

        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendChanges();
                radarScreen.setGameState(GameScreen.State.PAUSE);
            }
        });
    }

    @Override
    public void loadBoxes(int xOffset, int yOffset) {
        super.loadBoxes(xOffset, yOffset);

        int additionaOffsetX = 2000;

        speed = new SelectBox<>(selectBoxStyle);
        Array<String> options4 = new Array<>(3);
        options4.add("1x", "2x", "4x");
        speed.setItems(options4);
        speed.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                speedSel = speed.getSelected().charAt(0) - 48;
            }
        });
        speed.setSize(1200, 300);
        speed.setPosition(5760 / 2f - 400 + xOffset + additionaOffsetX, 3240 * 0.8f + yOffset);
        speed.setAlignment(Align.center);
        speed.getList().setAlignment(Align.center);

        tfcMode = new SelectBox<>(selectBoxStyle);
        Array<String> options5 = new Array<>(3);
        options5.add("Normal", "Arrivals only");
        tfcMode.setItems(options5);
        tfcMode.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                tfcSel = RadarScreen.TfcMode.valueOf(tfcMode.getSelected().toUpperCase(Locale.US).replaceAll(" ", "_"));
            }
        });
        tfcMode.setSize(1200, 300);
        tfcMode.setPosition(5760 / 2f - 400 + xOffset + additionaOffsetX, 3240 * 0.65f + yOffset);
        tfcMode.setAlignment(Align.center);
        tfcMode.getList().setAlignment(Align.center);

        night = new SelectBox<>(selectBoxStyle);
        Array<String> options6 = new Array<>(2);
        options6.add("On", "Off");
        night.setItems(options6);
        night.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                allowNight = "On".equals(night.getSelected());
                setVisible(true);
            }
        });
        night.setSize(1200, 300);
        night.setPosition(5760 / 2f - 400 + xOffset + additionaOffsetX, 3240 * 0.5f + yOffset);
        night.setAlignment(Align.center);
        night.getList().setAlignment(Align.center);

        nightStartHour = new SelectBox<>(selectBoxStyle);
        Array<String> options7 = new Array<>(24);
        for (int i = 0; i < 24; i++) {
            String hour = Integer.toString(i);
            if (hour.length() == 1) hour = "0" + hour;
            options7.add(hour);
        }
        nightStartHour.setItems(options7);
        nightStartHour.setSize(300, 300);
        nightStartHour.setPosition(night.getX(), 3240 * 0.35f + yOffset);
        nightStartHour.setAlignment(Align.center);
        nightStartHour.getList().setAlignment(Align.center);
        nightStartHour.setName("night");

        nightStartMin = new SelectBox<>(selectBoxStyle);
        Array<String> options8 = new Array<>(4);
        options8.add("00", "15", "30", "45");
        nightStartMin.setItems(options8);
        nightStartHour.addListener(new ChangeListener() {
            //Put here to prevent any potential NPE
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                nightStart = Integer.parseInt(nightStartHour.getSelected()) * 100 + Integer.parseInt(nightStartMin.getSelected());
            }
        });
        nightStartMin.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                nightStart = Integer.parseInt(nightStartHour.getSelected()) * 100 + Integer.parseInt(nightStartMin.getSelected());
            }
        });
        nightStartMin.setSize(300, 300);
        nightStartMin.setPosition(nightStartHour.getX() + 400, nightStartHour.getY());
        nightStartMin.setAlignment(Align.center);
        nightStartMin.getList().setAlignment(Align.center);
        nightStartMin.setName("night");

        nightEndHour = new SelectBox<>(selectBoxStyle);
        nightEndHour.setItems(options7);
        nightEndHour.setSize(300, 300);
        nightEndHour.setPosition(nightStartMin.getX() + 550, nightStartHour.getY());
        nightEndHour.setAlignment(Align.center);
        nightEndHour.getList().setAlignment(Align.center);
        nightEndHour.setName("night");

        nightEndMin = new SelectBox<>(selectBoxStyle);
        nightEndMin.setItems(options8);
        nightEndHour.addListener(new ChangeListener() {
            //Put here to prevent any potential NPE
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                nightEnd = Integer.parseInt(nightEndHour.getSelected()) * 100 + Integer.parseInt(nightEndMin.getSelected());
            }
        });
        nightEndMin.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                nightEnd = Integer.parseInt(nightEndHour.getSelected()) * 100 + Integer.parseInt(nightEndMin.getSelected());
            }
        });
        nightEndMin.setSize(300, 300);
        nightEndMin.setPosition(nightEndHour.getX() + 400, nightStartHour.getY());
        nightEndMin.setAlignment(Align.center);
        nightEndMin.getList().setAlignment(Align.center);
        nightEndMin.setName("night");
    }

    @Override
    public void loadLabel() {
        super.loadLabel();

        speedLabel = new Label("Speed: ", labelStyle);
        speedLabel.setPosition(speed.getX() - 100 - speedLabel.getWidth(), speed.getY() + speed.getHeight() / 2 - speedLabel.getHeight() / 2);

        tfcLabel = new Label("Traffic: ", labelStyle);
        tfcLabel.setPosition(tfcMode.getX() - 100 - tfcLabel.getWidth(), tfcMode.getY() + tfcMode.getHeight() / 2 - tfcLabel.getHeight() / 2);

        nightLabel = new Label("Night mode: ", labelStyle);
        nightLabel.setPosition(night.getX() - 100 - nightLabel.getWidth(), night.getY() + night.getHeight() / 2 - nightLabel.getHeight() / 2);

        timeLabel = new Label("Active from:", labelStyle);
        timeLabel.setPosition(nightStartHour.getX() - 100 - nightLabel.getWidth(), nightStartHour.getY() + nightStartHour.getHeight() / 2 - timeLabel.getHeight() / 2);
        timeLabel.setName("time");

        timeLabel2 = new Label("to", labelStyle);
        timeLabel2.setPosition(timeLabel.getX() + 1400, timeLabel.getY());
        timeLabel2.setName("time");
    }

    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this);
        tab1.addActors(trajectoryLine, trajectoryLabel);
        tab1.addActors(weather, weatherLabel);
        tab1.addActors(sound, soundLabel);
        tab1.addActors(emer, emerChanceLabel);
        tab1.addActors(speed, speedLabel);
        tab1.addActors(tfcMode, tfcLabel);
        tab1.addActors(night, nightLabel);
        tab1.addActors(nightStartHour, nightStartMin, nightEndHour, nightEndMin, timeLabel, timeLabel2);
        settingsTabs.add(tab1);
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        radarScreen.trajectoryLine = trajectorySel;
        radarScreen.liveWeather = weatherSel;
        radarScreen.soundSel = soundSel;
        radarScreen.emerChance = emerChance;
        radarScreen.speed = speedSel;
        radarScreen.tfcMode = tfcSel;
        boolean changed;
        changed = radarScreen.allowNight != allowNight || radarScreen.nightStart != nightStart || radarScreen.nightEnd != nightEnd;
        radarScreen.allowNight = allowNight;
        radarScreen.nightStart = nightStart;
        radarScreen.nightEnd = nightEnd;
        if (changed) {
            for (Airport airport: radarScreen.airports.values()) {
                airport.updateRunwayUsage(); //Possible change in runway usage
            }
        }
        radarScreen.ui.updateInfoLabel();
    }

    /** Gets radarscreen settings before setting options */
    @Override
    public void setOptions() {
        trajectorySel = radarScreen.trajectoryLine;
        weatherSel = radarScreen.liveWeather;
        soundSel = radarScreen.soundSel;
        emerChance = radarScreen.emerChance;
        speedSel = radarScreen.speed;
        tfcSel = radarScreen.tfcMode;

        super.setOptions();

        speed.setSelected(speedSel + "x");
        String tmp2 = tfcSel.toString().toLowerCase(Locale.US);
        tfcMode.setSelected((tmp2.substring(0, 1).toUpperCase() + tmp2.substring(1)).replaceAll("_", " "));

        allowNight = radarScreen.allowNight;
        night.setSelected(allowNight ? "On" : "Off");

        nightStart = radarScreen.nightStart;
        String hr = Integer.toString(nightStart / 100);
        if (hr.length() == 1) hr = "0" + hr;
        nightStartHour.setSelected(hr);
        nightStartMin.setSelected(Integer.toString(nightStart % 100));

        nightEnd = radarScreen.nightEnd;
        String hr2 = Integer.toString(nightEnd / 100);
        if (hr2.length() == 1) hr2 = "0" + hr2;
        nightEndHour.setSelected(hr2);
        nightEndMin.setSelected(Integer.toString(nightEnd % 100));
    }

    /** Sets visibility of elements */
    public void setVisible(boolean show) {
        updateTabs(show);

        confirmButton.setVisible(show);
        cancelButton.setVisible(show);
    }

    public Stage getStage() {
        return stage;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public boolean isAllowNight() {
        return allowNight;
    }
}
