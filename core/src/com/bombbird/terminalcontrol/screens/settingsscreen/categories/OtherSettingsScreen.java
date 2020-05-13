package com.bombbird.terminalcontrol.screens.settingsscreen.categories;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager;
import com.bombbird.terminalcontrol.screens.WeatherScreen;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTab;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class OtherSettingsScreen extends SettingsTemplateScreen {
    public SelectBox<String> weather;
    public SelectBox<String> sound;
    public SelectBox<String> sweep;

    public Label weatherLabel;
    public RadarScreen.Weather weatherSel;

    public Label soundLabel;
    public int soundSel;

    public Label sweepLabel;
    public float radarSweep;

    //In game only
    private SelectBox<String> speed;

    private Label speedLabel;
    private int speedSel;

    public OtherSettingsScreen(TerminalControl game, RadarScreen radarScreen, Image background) {
        super(game, radarScreen, background);

        loadUI(-1200, 0);

        setOptions();
    }

    /** Loads selectBox for display settings */
    @Override
    public void loadBoxes() {
        weather = createStandardSelectBox();
        Array<String> weatherModes = new Array<>();
        weatherModes.add("Live weather", "Random weather", "Static weather");
        if (radarScreen != null) weatherModes.add("Set custom weather...");
        weather.setItems(weatherModes);
        weather.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Set custom weather...".equals(weather.getSelected())) {
                    //Go to weather change screen
                    game.setScreen(new WeatherScreen(game));
                } else {
                    weatherSel = RadarScreen.Weather.valueOf(weather.getSelected().split(" ")[0].toUpperCase());
                }
            }
        });

        sound = createStandardSelectBox();
        Array<String> options = new Array<>(2);
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            options.add("Pilot voices + sound effects", "Sound effects only", "Off");
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            options.add("Sound effects", "Off");
        }
        sound.setItems(options);
        sound.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Pilot voices + sound effects".equals(sound.getSelected())) {
                    soundSel = 2;
                } else if ("Sound effects".equals(sound.getSelected()) || "Sound effects only".equals(sound.getSelected())) {
                    soundSel = 1;
                } else if ("Off".equals(sound.getSelected())) {
                    soundSel = 0;
                }
            }
        });

        sweep = createStandardSelectBox();
        sweep.setItems(UnlockManager.getSweepAvailable());
        sweep.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                radarSweep = Float.parseFloat(sweep.getSelected().substring(0, sweep.getSelected().length() - 1));
            }
        });

        if (radarScreen != null) {
            speed = createStandardSelectBox();
            speed.setItems("1x", "2x", "4x");
            speed.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    speedSel = speed.getSelected().charAt(0) - 48;
                }
            });
        }
    }

    /** Loads labels for display settings */
    @Override
    public void loadLabel() {
        super.loadLabel();
        weatherLabel = new Label("Weather: ", labelStyle);
        soundLabel = new Label("Sounds: ", labelStyle);
        sweepLabel = new Label("Radar sweep: ", labelStyle);

        if (radarScreen != null) {
            speedLabel = new Label("Speed: ", labelStyle);
        }
    }

    /** Loads actors for display settings into tabs */
    @Override
    public void loadTabs() {
        if (radarScreen != null && radarScreen.tutorial) {
            setOptions();
            //Only show speed tab in tutorial settings
            SettingsTab tab1 = new SettingsTab(this, 2);
            tab1.addActors(speed, speedLabel);
            settingsTabs.add(tab1);
            return;
        }

        SettingsTab tab1 = new SettingsTab(this, 2);
        tab1.addActors(weather, weatherLabel);
        tab1.addActors(sound, soundLabel);
        tab1.addActors(sweep, sweepLabel);

        if (radarScreen != null) {
            tab1.addActors(speed, speedLabel);
        }

        settingsTabs.add(tab1);
    }

    /** Sets relevant options into select boxes */
    @Override
    public void setOptions() {
        if (radarScreen != null) {
            speedSel = radarScreen.speed;
            speed.setSelected(speedSel + "x");
            weatherSel = radarScreen.weatherSel;
            soundSel = radarScreen.soundSel;
            speedSel = radarScreen.speed;
            radarSweep = radarScreen.radarSweepDelay;
            if (radarScreen.tutorial) return;
        } else {
            radarSweep = TerminalControl.radarSweep;
            weatherSel = TerminalControl.weatherSel;
            soundSel = TerminalControl.soundSel;
        }

        weather.setSelected(weatherSel.toString().charAt(0) + weatherSel.toString().substring(1).toLowerCase() + " weather");
        int soundIndex = (Gdx.app.getType() == Application.ApplicationType.Android ? 2 : 1) - soundSel;
        if (soundIndex < 0) soundIndex = 0;
        sound.setSelectedIndex(soundIndex);
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        sweep.setSelected(df.format(radarSweep) + "s");
    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {
        if (radarScreen != null) {
            radarScreen.weatherSel = weatherSel;
            radarScreen.soundSel = soundSel;
            radarScreen.speed = speedSel;
            radarScreen.radarSweepDelay = radarSweep;
            if (radarSweep < radarScreen.getRadarTime()) radarScreen.setRadarTime(radarSweep);
            radarScreen.ui.updateInfoLabel();
        } else {
            TerminalControl.weatherSel = weatherSel;
            TerminalControl.soundSel = soundSel;
            TerminalControl.radarSweep = radarSweep;

            GameSaver.saveSettings();
        }
    }
}
