package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen;
import com.bombbird.terminalcontrol.screens.selectgamescreen.NewGameScreen;
import com.bombbird.terminalcontrol.screens.ui.Ui;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class MainMenuScreen implements Screen {
    //Init game (set in constructor)
    private final TerminalControl game;
    private Stage stage;

    //Create new camera
    private OrthographicCamera camera;
    private Viewport viewport;

    public MainMenuScreen(final TerminalControl game) {
        this.game = game;

        //Set camera params
        camera = new OrthographicCamera();
        camera.setToOrtho(false,2880, 1620);
        viewport = new FitViewport(TerminalControl.WIDTH, TerminalControl.HEIGHT, camera);
        viewport.apply();

        //Set stage params
        stage = new Stage(new FitViewport(2880, 1620));
        stage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true);
        Gdx.input.setInputProcessor(stage);
    }

    /** Loads the UI elements to be rendered on screen */
    private void loadUI() {
        int buttonWidth = 1000;
        int buttonHeight = 200;

        //Reset stage
        stage.clear();

        //Set title icon
        Image image = new Image(new Texture(Gdx.files.internal("game/ui/MainMenuIcon.png")));
        image.scaleBy(0.5f);
        image.setPosition(2880 / 2.0f - 1.5f * image.getWidth() / 2.0f, 1620 * 0.825f);
        stage.addActor(image);

        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont20;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Set new game button params
        TextButton newGameButton = new TextButton("New Game", buttonStyle);
        newGameButton.setPosition(2880 / 2.0f - buttonWidth / 2.0f, 1620 * 0.65f);
        newGameButton.setWidth(buttonWidth);
        newGameButton.setHeight(buttonHeight);
        newGameButton.getLabel().setAlignment(Align.center);
        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Start new game -> Choose airport screen
                game.setScreen(new NewGameScreen(game));
                dispose();
            }
        });
        stage.addActor(newGameButton);

        //Set load game button params
        TextButton loadGameButton = new TextButton("Load Game", buttonStyle);
        loadGameButton.setPosition(2880 / 2.0f - buttonWidth / 2.0f, 1620 * 0.5f);
        loadGameButton.setWidth(buttonWidth);
        loadGameButton.setHeight(buttonHeight);
        loadGameButton.getLabel().setAlignment(Align.center);
        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Load game -> Saved games screen
                game.setScreen(new LoadGameScreen(game));
                dispose();
            }
        });
        stage.addActor(loadGameButton);

        //Set settings button params
        TextButton settingsButton = new TextButton("Settings", buttonStyle);
        settingsButton.setPosition(2880 / 2.0f - buttonWidth / 2.0f, 1620 * 0.35f);
        settingsButton.setWidth(buttonWidth);
        settingsButton.setHeight(buttonHeight);
        settingsButton.getLabel().setAlignment(Align.center);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Change settings -> Settings screen
            }
        });
        stage.addActor(settingsButton);

        //Set quit button params
        TextButton quitButton = new TextButton("Quit", buttonStyle);
        quitButton.setPosition(2880 / 2.0f - buttonWidth / 2.0f, 1620 * 0.2f);
        quitButton.setWidth(buttonWidth);
        quitButton.setHeight(buttonHeight);
        quitButton.getLabel().setAlignment(Align.center);
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Quit game
                RadarScreen.disposeStatic();
                Ui.disposeStatic();
                dispose();
                Gdx.app.exit();
            }
        });
        stage.addActor(quitButton);
    }

    /** Implements show method of screen */
    @Override
    public void show() {
        if (Fonts.defaultFont6 == null) {
            //Regenerate fonts that were disposed
            Fonts.generateAllFonts();
        }
        loadUI();
    }

    /** Main rendering method for rendering to spriteBatch */
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.3f, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        stage.act(delta);
        game.batch.begin();
        stage.draw();
        game.batch.end();
    }

    /** Implements resize method of screen, adjusts camera & viewport properties after resize for better UI */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
    }

    /** Implements pause method of screen */
    @Override
    public void pause() {
        //No default implementation
    }

    /** Implements resume method of screen */
    @Override
    public void resume() {
        //No default implementation
    }

    /** Implements hide method of screen */
    @Override
    public void hide() {
        //No default implementation
    }

    /** Implements dispose method of screen, disposes resources after they're no longer needed */
    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
    }
}
