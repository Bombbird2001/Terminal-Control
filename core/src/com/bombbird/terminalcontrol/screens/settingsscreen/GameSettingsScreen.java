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
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.Locale;

public class GameSettingsScreen extends SettingsScreen {
    private RadarScreen radarScreen;

    public GameSettingsScreen(final TerminalControl game, final RadarScreen radarScreen) {
        super(game);

        this.radarScreen = radarScreen;

        loadUI(-1000, 0);
    }

    @Override
    public void loadButton() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        cancelButton = new TextButton("Cancel", textButtonStyle);
        cancelButton.setSize(1200, 300);
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                radarScreen.setGameState(GameScreen.State.PAUSE);
            }
        });
        stage.addActor(cancelButton);

        confirmButton = new TextButton("Confirm", textButtonStyle);
        confirmButton.setSize(1200, 300);
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendChanges();
                radarScreen.setGameState(GameScreen.State.PAUSE);
            }
        });
        stage.addActor(confirmButton);
    }

    @Override
    public void loadBoxes(int xOffset, int yOffset) {
        super.loadBoxes(xOffset, yOffset);

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
        speed.setPosition(5760 / 2f - 400 + xOffset + 1800, 3240 * 0.8f + yOffset);
        speed.setAlignment(Align.center);
        speed.getList().setAlignment(Align.center);
        stage.addActor(speed);

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
        tfcMode.setPosition(5760 / 2f - 400 + xOffset + 1800, 3240 * 0.65f + yOffset);
        tfcMode.setAlignment(Align.center);
        tfcMode.getList().setAlignment(Align.center);
        stage.addActor(tfcMode);
    }

    @Override
    public void loadLabel() {
        super.loadLabel();

        speedLabel = new Label("Speed: ", labelStyle);
        speedLabel.setPosition(speed.getX() - 100 - speedLabel.getWidth(), speed.getY() + speed.getHeight() / 2 - speedLabel.getHeight() / 2);
        stage.addActor(speedLabel);

        tfcLabel = new Label("Traffic: ", labelStyle);
        tfcLabel.setPosition(tfcMode.getX() - 100 - tfcLabel.getWidth(), tfcMode.getY() + tfcMode.getHeight() / 2 - tfcLabel.getHeight() / 2);
        stage.addActor(tfcLabel);
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
        radarScreen.ui.updateSpeedLabel();
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
    }

    /** Sets visibility of elements */
    public void setVisible(boolean show) {
        trajectoryLine.setVisible(show);
        trajectoryLabel.setVisible(show);
        weather.setVisible(show);
        weatherLabel.setVisible(show);
        sound.setVisible(show);
        soundLabel.setVisible(show);
        emer.setVisible(show);
        emerChanceLabel.setVisible(show);
        speed.setVisible(show);
        speedLabel.setVisible(show);
        tfcMode.setVisible(show);
        tfcLabel.setVisible(show);
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
}
