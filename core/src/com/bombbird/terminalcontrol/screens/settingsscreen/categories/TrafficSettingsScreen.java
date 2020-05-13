package com.bombbird.terminalcontrol.screens.settingsscreen.categories;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

import java.util.Locale;

public class TrafficSettingsScreen extends SettingsTemplateScreen {
    public SelectBox<String> emer;

    public Label emerChanceLabel;
    public Emergency.Chance emerChance;

    //In game only
    private SelectBox<String> tfcMode;
    private SelectBox<String> night;
    private SelectBox<String> nightStartHour;
    private SelectBox<String> nightStartMin;
    private SelectBox<String> nightEndHour;
    private SelectBox<String> nightEndMin;

    private Label tfcLabel;
    private RadarScreen.TfcMode tfcSel;

    private Label nightLabel;
    private Label timeLabel;
    private Label timeLabel2;

    private boolean allowNight;
    private int nightStart;
    private int nightEnd;

    public TrafficSettingsScreen(TerminalControl game, RadarScreen radarScreen, Image background) {
        super(game, radarScreen, background);

        loadUI(-1200, 0);

        setOptions();
    }

    /** Loads selectBox for display settings */
    @Override
    public void loadBoxes() {
        emer = createStandardSelectBox();
        emer.setItems("Off", "Low", "Medium", "High");
        emer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                emerChance = Emergency.Chance.valueOf(emer.getSelected().toUpperCase(Locale.US));
            }
        });

        if (radarScreen != null) {
            tfcMode = createStandardSelectBox();
            tfcMode.setItems("Normal", "Arrivals only");
            tfcMode.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    tfcSel = RadarScreen.TfcMode.valueOf(tfcMode.getSelected().toUpperCase(Locale.US).replaceAll(" ", "_"));
                }
            });

            night = createStandardSelectBox();
            night.setItems("On", "Off");
            night.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    allowNight = "On".equals(night.getSelected());
                    updateTabs(true);
                }
            });
            night.setName("night2");

            nightStartHour = new SelectBox<>(selectBoxStyle);
            Array<String> options7 = new Array<>(24);
            for (int i = 0; i < 24; i++) {
                String hour = Integer.toString(i);
                if (hour.length() == 1) hour = "0" + hour;
                options7.add(hour);
            }
            nightStartHour.setItems(options7);
            nightStartHour.setSize(300, 300);
            nightStartHour.setAlignment(Align.center);
            nightStartHour.getList().setAlignment(Align.center);
            nightStartHour.setName("night");

            nightStartMin = new SelectBox<>(selectBoxStyle);
            nightStartMin.setItems("00", "15", "30", "45");
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
            nightStartMin.setPosition(400, 0);
            nightStartMin.setAlignment(Align.center);
            nightStartMin.getList().setAlignment(Align.center);
            nightStartMin.setName("night");

            nightEndHour = new SelectBox<>(selectBoxStyle);
            nightEndHour.setItems(options7);
            nightEndHour.setSize(300, 300);
            nightEndHour.setPosition(950, 0);
            nightEndHour.setAlignment(Align.center);
            nightEndHour.getList().setAlignment(Align.center);
            nightEndHour.setName("night");

            nightEndMin = new SelectBox<>(selectBoxStyle);
            nightEndMin.setItems("00", "15", "30", "45");
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
            nightEndMin.setPosition(1350, 0);
            nightEndMin.setAlignment(Align.center);
            nightEndMin.getList().setAlignment(Align.center);
            nightEndMin.setName("night");
        }
    }

    /** Loads labels for display settings */
    @Override
    public void loadLabel() {
        super.loadLabel();
        emerChanceLabel = new Label("Emergencies: ", labelStyle);

        if (radarScreen != null) {
            tfcLabel = new Label("Traffic: ", labelStyle);

            nightLabel = new Label("Night mode: ", labelStyle);
            nightLabel.setName("night2");

            timeLabel = new Label("Active from:", labelStyle);
            timeLabel.setName("night");

            timeLabel2 = new Label("to", labelStyle);
            timeLabel2.setPosition(nightStartHour.getX() - nightLabel.getWidth() + 1300, nightStartHour.getY() + nightStartHour.getHeight() / 2 - timeLabel2.getHeight() / 2);
            timeLabel2.setName("night");
        }
    }

    /** Loads actors for display settings into tabs */
    @Override
    public void loadTabs() {
        SettingsTab tab1 = new SettingsTab(this, 2);
        tab1.addActors(emer, emerChanceLabel);

        if (radarScreen != null) {
            tab1.addActors(tfcMode, tfcLabel);
            tab1.addActors(night, nightLabel);
            tab1.addActors(nightStartHour, timeLabel, nightStartMin, nightEndHour, nightEndMin, timeLabel2);
        }

        settingsTabs.add(tab1);
    }

    /** Sets relevant options into select boxes */
    @Override
    public void setOptions() {
        if (radarScreen != null) {
            emerChance = radarScreen.emerChance;
        } else {
            emerChance = TerminalControl.emerChance;
        }
        String tmp = emerChance.toString().toLowerCase(Locale.US);
        emer.setSelected(tmp.substring(0, 1).toUpperCase() + tmp.substring(1));

        if (radarScreen != null) {
            tfcSel = radarScreen.tfcMode;
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
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        if (radarScreen == null) {
            TerminalControl.emerChance = emerChance;

            GameSaver.saveSettings();
        } else {
            radarScreen.emerChance = emerChance;
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
    }

    public boolean isAllowNight() {
        return allowNight && radarScreen != null;
    }
}
