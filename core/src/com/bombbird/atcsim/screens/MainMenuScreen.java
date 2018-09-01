package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.atcsim.AtcSim;

public class MainMenuScreen implements Screen {
    //Init game (set in constructor)
    private final AtcSim game;
    private Stage stage;

    //Create new camera
    private OrthographicCamera camera;
    private Viewport viewport;

    //Create texture stuff
    private TextureAtlas buttonAtlas;
    private Skin skin;

    public MainMenuScreen(final AtcSim game) {
        this.game = game;

        //Set camera params
        camera = new OrthographicCamera();
        viewport = new FillViewport(1440, 810, camera);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);

        //Set stage params
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        loadUI(1440, 810);
    }

    private void loadUI(int width, int height) {
        int buttonWidth = width / 4;
        int buttonHeight = height / 10;

        //Reset stage
        stage.clear();

        //Set label params
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.fonts.defaultFont40;
        labelStyle.fontColor = Color.WHITE;
        Label headerLabel = new Label("ATC Sim", labelStyle);
        headerLabel.setWidth(buttonWidth);
        headerLabel.setHeight(buttonHeight);
        headerLabel.setPosition(width / 2.0f - buttonWidth / 2.0f, height * 0.8f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);

        //Set button textures
        buttonAtlas = new TextureAtlas(Gdx.files.internal("main_menu/mainmenubuttons.atlas"));
        skin = new Skin();
        skin.addRegions(buttonAtlas);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.fonts.defaultFont40;
        buttonStyle.up = skin.getDrawable("Button_up");
        buttonStyle.down = skin.getDrawable("Button_down");

        //Set new game button params
        TextButton newGameButton = new TextButton("New Game", buttonStyle);
        newGameButton.setPosition(width / 2.0f - buttonWidth / 2.0f, height * 0.65f);
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
        loadGameButton.setPosition(width / 2.0f - buttonWidth / 2.0f, height * 0.5f);
        loadGameButton.setWidth(buttonWidth);
        loadGameButton.setHeight(buttonHeight);
        loadGameButton.getLabel().setAlignment(Align.center);
        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Load game -> Saved games screen
            }
        });
        stage.addActor(loadGameButton);

        //Set settings button params
        TextButton settingsButton = new TextButton("Settings", buttonStyle);
        settingsButton.setPosition(width / 2.0f - buttonWidth / 2.0f, height * 0.35f);
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
        quitButton.setPosition(width / 2.0f - buttonWidth / 2.0f, height * 0.2f);
        quitButton.setWidth(buttonWidth);
        quitButton.setHeight(buttonHeight);
        quitButton.getLabel().setAlignment(Align.center);
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Quit game
                dispose();
                game.dispose();
                Gdx.app.exit();
                System.exit(0);
            }
        });
        stage.addActor(quitButton);
    }

    @Override
    public void show() {

    }

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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        stage.getViewport().update(width, height, true);
        //Gdx.app.log("Resize", width + " " + height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        buttonAtlas.dispose();
    }
}
