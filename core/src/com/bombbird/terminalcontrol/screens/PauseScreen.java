package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class PauseScreen {
    private GameScreen gameScreen;

    private TextButton resumeButton;
    private TextButton settingsButton;
    private TextButton quitButton;

    private Stage stage;
    private OrthographicCamera camera;
    private Viewport viewport;

    public PauseScreen(GameScreen gameScreen) {
        this.gameScreen = gameScreen;

        stage = new Stage(new FitViewport(5760, 3240));
        stage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);

        camera = (OrthographicCamera) stage.getViewport().getCamera();
        camera.setToOrtho(false, 5760, 3240);
        viewport = new FitViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, camera);
        viewport.apply();
        camera.position.set(2880, 1620, 0);

        loadButtons();

        System.out.println("Local: " + Gdx.files.getLocalStoragePath());
        System.out.println("External: " + Gdx.files.getExternalStoragePath());
    }

    /** Loads the buttons for screen */
    private void loadButtons() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        resumeButton = new TextButton("Resume", textButtonStyle);
        resumeButton.setSize(1200, 300);
        resumeButton.setPosition((5760 - 1200) / 2f, 3240 - 1200);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Un-pause the game
                gameScreen.setGameState(GameScreen.State.RUN);
                event.handle();
            }
        });
        stage.addActor(resumeButton);

        settingsButton = new TextButton("Settings", textButtonStyle);
        settingsButton.setSize(1200, 300);
        settingsButton.setPosition((5760 - 1200) / 2f, 3240 - 1600);
        stage.addActor(settingsButton);

        quitButton = new TextButton("Quit", textButtonStyle);
        quitButton.setSize(1200, 300);
        quitButton.setPosition((5760 - 1200) / 2f, 3240 - 2000);
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu screen
                for (Aircraft aircraft: RadarScreen.AIRCRAFTS.values()) {
                    //Clears timer for all aircrafts' navState
                    aircraft.getNavState().clearAll();
                }
                dispose();
                gameScreen.game.setScreen(new MainMenuScreen(gameScreen.game));
            }
        });
        stage.addActor(quitButton);
    }

    /** Draws each element of the pauseScreen */
    public void draw() {
        stage.draw();
    }

    /** Sets whether each element is visible or not */
    public void setVisible(boolean visible) {
        resumeButton.setVisible(visible);
        settingsButton.setVisible(visible);
        quitButton.setVisible(visible);
    }

    /** Disposes of unneeded resources */
    private void dispose() {
        stage.clear();
        stage.dispose();

        stage = null;
    }

    public Stage getStage() {
        return stage;
    }

    public Camera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }
}
