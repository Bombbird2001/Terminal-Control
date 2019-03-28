package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class SettingsScreen {
    private RadarScreen radarScreen;

    private Stage stage;
    private OrthographicCamera camera;
    private Viewport viewport;

    private SelectBox.SelectBoxStyle selectBoxStyle;
    private SelectBox<String> trajectoryLine;
    private SelectBox<String> weather;
    private SelectBox<String> sound;

    private TextButton confirmButton;
    private TextButton cancelButton;

    private Label trajectoryLabel;
    private int trajectorySel;

    private Label weatherLabel;
    private boolean weatherSel;

    private Label soundLabel;
    private int soundSel;

    public SettingsScreen(final RadarScreen radarScreen) {
        this.radarScreen = radarScreen;
        stage = new Stage(new FitViewport(5760, 3240));
        stage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);

        camera = (OrthographicCamera) stage.getViewport().getCamera();
        camera.setToOrtho(false, 5760, 3240);
        viewport = new FitViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, camera);
        viewport.apply();
        camera.position.set(2880, 1620, 0);

        trajectorySel = radarScreen.trajectoryLine;
        weatherSel = radarScreen.liveWeather;

        loadStyles();

        loadSelectBox();

        loadButton();

        loadLabel();
    }

    /** Loads the styles for the selectBox */
    private void loadStyles() {
        ScrollPane.ScrollPaneStyle paneStyle = new ScrollPane.ScrollPaneStyle();
        paneStyle.background = TerminalControl.skin.getDrawable("ListBackground");

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = Fonts.defaultFont20;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.BLACK;
        Drawable button_down = TerminalControl.skin.getDrawable("Button_down");
        button_down.setTopHeight(75);
        button_down.setBottomHeight(75);
        listStyle.selection = button_down;

        selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = Fonts.defaultFont20;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.listStyle = listStyle;
        selectBoxStyle.scrollStyle = paneStyle;
        selectBoxStyle.background = TerminalControl.skin.getDrawable("Button_up");
    }

    /** Loads selectBox for settings */
    private void loadSelectBox() {
        trajectoryLine = new SelectBox<String>(selectBoxStyle);
        Array<String> options = new Array<String>(3);
        options.add("60 sec", "90 sec", "120 sec", "150 sec");
        trajectoryLine.setItems(options);
        trajectoryLine.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                trajectorySel = Integer.parseInt(trajectoryLine.getSelected().split(" ")[0]);
            }
        });
        trajectoryLine.setSize(1200, 300);
        trajectoryLine.setPosition(5760 / 2f - 400, 3240 * 0.8f);
        trajectoryLine.setAlignment(Align.center);
        trajectoryLine.getList().setAlignment(Align.center);
        stage.addActor(trajectoryLine);

        weather = new SelectBox<String>(selectBoxStyle);
        Array<String> options1 = new Array<String>(2);
        options1.add("Live weather", "Random weather"); //TODO Add custom weather in future
        weather.setItems(options1);
        weather.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                weatherSel = "Live weather".equals(weather.getSelected());
            }
        });
        weather.setSize(1200, 300);
        weather.setPosition(5760 / 2f - 400, 3240 * 0.6f);
        weather.setAlignment(Align.center);
        weather.getList().setAlignment(Align.center);
        stage.addActor(weather);

        sound = new SelectBox<String>(selectBoxStyle);
        Array<String> options2 = new Array<String>(2);
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            options2.add("Text-to-speech + sound effects", "Sound effects only", "Off");
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            options2.add("Sound effects", "Off");
        }
        sound.setItems(options2);
        sound.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if ("Text-to-speech + sound effects".equals(sound.getSelected())) {
                    soundSel = 2;
                } else if ("Sound effects".equals(sound.getSelected()) || "Sound effects only".equals(sound.getSelected())) {
                    soundSel = 1;
                } else if ("Off".equals(sound.getSelected())) {
                    soundSel = 0;
                }
            }
        });
        sound.setSize(1200, 300);
        sound.setPosition(5760 / 2f - 400, 3240 * 0.4f);
        sound.setAlignment(Align.center);
        sound.getList().setAlignment(Align.center);
        stage.addActor(sound);
    }

    /** Loads buttons */
    private void loadButton() {
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

    /** Loads labels */
    private void loadLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        trajectoryLabel = new Label("Trajectory line timing: ", labelStyle);
        trajectoryLabel.setPosition(trajectoryLine.getX() - 100 - trajectoryLabel.getWidth(), trajectoryLine.getY() + trajectoryLine.getHeight() / 2 - trajectoryLabel.getHeight() / 2);
        stage.addActor(trajectoryLabel);

        weatherLabel = new Label("Weather: ", labelStyle);
        weatherLabel.setPosition(weather.getX() - 100 - weatherLabel.getWidth(), weather.getY() + weather.getHeight() / 2 - weatherLabel.getHeight() / 2);
        stage.addActor(weatherLabel);

        soundLabel = new Label("Sounds: ", labelStyle);
        soundLabel.setPosition(sound.getX() - 100 - soundLabel.getWidth(), sound.getY() + sound.getHeight() / 2 - soundLabel.getHeight() / 2);
        stage.addActor(soundLabel);
    }

    /** Confirms and applies the changes set */
    private void sendChanges() {
        radarScreen.trajectoryLine = trajectorySel;
        radarScreen.liveWeather = weatherSel;
        radarScreen.soundSel = soundSel;
    }

    /** Sets visibility of elements */
    public void setVisible(boolean show) {
        trajectoryLine.setVisible(show);
        trajectoryLabel.setVisible(show);
        weather.setVisible(show);
        weatherLabel.setVisible(show);
        sound.setVisible(show);
        soundLabel.setVisible(show);
        confirmButton.setVisible(show);
        cancelButton.setVisible(show);
    }

    public void setOptions() {
        trajectorySel = radarScreen.trajectoryLine;
        trajectoryLine.setSelected(radarScreen.trajectoryLine + " sec");
        weatherSel = radarScreen.liveWeather;
        weather.setSelectedIndex(weatherSel ? 0 : 1);
        soundSel = radarScreen.soundSel;
        int soundIndex = (Gdx.app.getType() == Application.ApplicationType.Android ? 2 : 1) - soundSel;
        if (soundIndex < 0) soundIndex = 0;
        sound.setSelectedIndex(soundIndex);
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
