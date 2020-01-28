package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.Locale;

public class SettingsScreen implements Screen {
    public final TerminalControl game;

    public Stage stage;
    public OrthographicCamera camera;
    public Viewport viewport;

    public SelectBox<String> trajectoryLine;
    public SelectBox<String> weather;
    public SelectBox<String> sound;
    public SelectBox<String> emer;

    public TextButton confirmButton;
    public TextButton cancelButton;
    public TextButton backButton;
    public TextButton nextButton;

    public Label trajectoryLabel;
    public int trajectorySel;

    public Label weatherLabel;
    public RadarScreen.Weather weatherSel;

    public Label soundLabel;
    public int soundSel;

    public Label emerChanceLabel;
    public Emergency.Chance emerChance;

    public SelectBox.SelectBoxStyle selectBoxStyle;
    public Label.LabelStyle labelStyle;

    public Array<SettingsTab> settingsTabs;
    public int tab;

    public SettingsScreen(final TerminalControl game) {
        this.game = game;

        //Set camera params
        camera = new OrthographicCamera();
        camera.setToOrtho(false,5760, 3240);
        viewport = new FitViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, camera);
        viewport.apply();

        //Set stage params
        stage = new Stage(new FitViewport(5760, 3240));
        stage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);
        Gdx.input.setInputProcessor(stage);

        settingsTabs = new Array<>();
        tab = 0;
    }

    public void loadUI(int xOffset, int yOffset) {
        loadStyles();

        loadBoxes(xOffset, yOffset);

        loadButton();

        loadLabel();

        loadTabs();

        updateTabs(true);
    }

    /** Loads the styles for the selectBox */
    public void loadStyles() {
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
    public void loadBoxes(int xOffset, int yOffset) {
        trajectoryLine = new SelectBox<>(selectBoxStyle);
        Array<String> options = new Array<>(3);
        options.add("60 sec", "90 sec", "120 sec", "150 sec");
        trajectoryLine.setItems(options);
        trajectoryLine.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                trajectorySel = Integer.parseInt(trajectoryLine.getSelected().split(" ")[0]);
            }
        });
        trajectoryLine.setSize(1200, 300);
        trajectoryLine.setPosition(5760 / 2f - 400 + xOffset, 3240 * 0.8f + yOffset);
        trajectoryLine.setAlignment(Align.center);
        trajectoryLine.getList().setAlignment(Align.center);

        weather = new SelectBox<>(selectBoxStyle);
        Array<String> options1 = new Array<>(3);
        options1.add("Live weather", "Random weather", "Static weather"); //TODO Add custom weather in future
        weather.setItems(options1);
        weather.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                weatherSel = RadarScreen.Weather.valueOf(weather.getSelected().split(" ")[0].toUpperCase());
            }
        });
        weather.setSize(1200, 300);
        weather.setPosition(5760 / 2f - 400 + xOffset, 3240 * 0.65f + yOffset);
        weather.setAlignment(Align.center);
        weather.getList().setAlignment(Align.center);

        sound = new SelectBox<>(selectBoxStyle);
        Array<String> options2 = new Array<>(2);
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            options2.add("Pilot voices + sound effects", "Sound effects only", "Off");
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            options2.add("Sound effects", "Off");
        }
        sound.setItems(options2);
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
        sound.setSize(1200, 300);
        sound.setPosition(5760 / 2f - 400 + xOffset, 3240 * 0.5f + yOffset);
        sound.setAlignment(Align.center);
        sound.getList().setAlignment(Align.center);

        emer = new SelectBox<>(selectBoxStyle);
        Array<String> options3 = new Array<>(4);
        options3.add("Off", "Low", "Medium", "High");
        emer.setItems(options3);
        emer.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                emerChance = Emergency.Chance.valueOf(emer.getSelected().toUpperCase(Locale.US));
            }
        });
        emer.setSize(1200, 300);
        emer.setPosition(5760 / 2f - 400 + xOffset, 3240 * 0.35f + yOffset);
        emer.setAlignment(Align.center);
        emer.getList().setAlignment(Align.center);
    }

    /** Loads buttons */
    public void loadButton() {
        //Adds buttons by default, position, function depends on type of settings screen
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        cancelButton = new TextButton("Cancel", textButtonStyle);
        cancelButton.setSize(1200, 300);
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800);
        stage.addActor(cancelButton);

        confirmButton = new TextButton("Confirm", textButtonStyle);
        confirmButton.setSize(1200, 300);
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800);
        stage.addActor(confirmButton);

        backButton = new TextButton("<", textButtonStyle);
        backButton.setSize(400, 400);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab <= 0) return;
                tab--;
                updateTabs(true);
            }
        });
        stage.addActor(backButton);

        nextButton = new TextButton(">", textButtonStyle);
        nextButton.setSize(400, 400);
        nextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab >= settingsTabs.size - 1) return;
                tab++;
                updateTabs(true);
            }
        });
        stage.addActor(nextButton);
    }

    /** Loads labels */
    public void loadLabel() {
        labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        trajectoryLabel = new Label("Trajectory line timing: ", labelStyle);
        trajectoryLabel.setPosition(trajectoryLine.getX() - 100 - trajectoryLabel.getWidth(), trajectoryLine.getY() + trajectoryLine.getHeight() / 2 - trajectoryLabel.getHeight() / 2);

        weatherLabel = new Label("Weather: ", labelStyle);
        weatherLabel.setPosition(weather.getX() - 100 - weatherLabel.getWidth(), weather.getY() + weather.getHeight() / 2 - weatherLabel.getHeight() / 2);

        soundLabel = new Label("Sounds: ", labelStyle);
        soundLabel.setPosition(sound.getX() - 100 - soundLabel.getWidth(), sound.getY() + sound.getHeight() / 2 - soundLabel.getHeight() / 2);

        emerChanceLabel = new Label("Emergencies: ", labelStyle);
        emerChanceLabel.setPosition(emer.getX() - 100 - emerChanceLabel.getWidth(), emer.getY() + emer.getHeight() / 2 - emerChanceLabel.getHeight() / 2);
    }

    /** Loads the various actors into respective tabs, overriden in respective classes */
    public void loadTabs() {
        //No default implementation
    }

    /** Changes the tab displayed */
    public void updateTabs(boolean screenActive) {
        backButton.setVisible(tab > 0);
        nextButton.setVisible(tab < settingsTabs.size - 1);
        if (tab > settingsTabs.size - 1 || tab < 0) {
            Gdx.app.log("SettingsScreen", "Invalid tab set: " + tab + ", size is " + settingsTabs.size);
            return;
        }
        for (int i = 0; i < settingsTabs.size; i++) {
            if (i == tab) {
                settingsTabs.get(i).updateVisibility(screenActive);
            } else {
                settingsTabs.get(i).updateVisibility(false);
            }
        }
    }

    /** Sets relevant options into select boxes */
    public void setOptions() {
        trajectoryLine.setSelected(trajectorySel + " sec");
        weather.setSelected(weatherSel.toString().charAt(0) + weatherSel.toString().substring(1).toLowerCase() + " weather");
        int soundIndex = (Gdx.app.getType() == Application.ApplicationType.Android ? 2 : 1) - soundSel;
        if (soundIndex < 0) soundIndex = 0;
        sound.setSelectedIndex(soundIndex);
        String tmp = emerChance.toString().toLowerCase(Locale.US);
        emer.setSelected(tmp.substring(0, 1).toUpperCase() + tmp.substring(1));
    }

    /** Confirms and applies the changes set */
    public void sendChanges() {
        //No default implementation
    }

    @Override
    public void show() {
        if (Fonts.defaultFont6 == null) {
            //Regenerate fonts that were disposed
            Fonts.generateAllFonts();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        stage.act(delta);
        game.batch.begin();
        boolean success = false;
        while (!success) {
            try {
                stage.draw();
                game.batch.end();
                success = true;
            } catch (IndexOutOfBoundsException e) {
                Gdx.app.log("SettingsScreen", "stage.draw() render error");
                stage.getBatch().end();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
    }

    @Override
    public void pause() {
        //No default implementation
    }

    @Override
    public void resume() {
        //No default implementation
    }

    @Override
    public void hide() {
        //No default implementation
    }

    @Override
    public void dispose() {

    }
}
