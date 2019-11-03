package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class InfoScreen implements Screen {
    public final TerminalControl game;
    private Stage stage;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Image background;

    public InfoScreen(final TerminalControl game, Image background) {
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

        //Set background image to that shown on main menu screen
        this.background = background;
    }

    /** Loads the full UI of this screen */
    private void loadUI() {
        //Reset stage
        stage.clear();

        stage.addActor(background);

        loadLabel();
        loadButtons();
    }

    /** Loads labels for credits, disclaimers, etc */
    public void loadLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.WHITE;

        Label copyright = new Label("Terminal Control" + (TerminalControl.full ? "" : ": Lite") + "\nCopyright \u00a9 2018-2019, Bombbird\nVersion " + TerminalControl.versionName + ", build " + TerminalControl.versionCode, labelStyle);
        copyright.setPosition(918, 1375);
        stage.addActor(copyright);

        Label licenses = new Label("Open source software/libraries used:\n\n" +
                "libGDX - Apache License 2.0\n" +
                "JSON In Java - JSON License\n" +
                "OkHttp3 - Apache License 2.0\n" +
                "Apache Commons Lang - Apache License 2.0\n" +
                "Open Sans font - Apache License 2.0", labelStyle);
        licenses.setPosition(1435 - licenses.getWidth() / 2f, 825);
        stage.addActor(licenses);

        Label disclaimer = new Label("While we make effort to ensure that this game is as realistic as possible, " +
                "please note that this game is not a completely accurate representation of real life air traffic control " +
                "and should not be used for purposes such as real life training.", labelStyle);
        disclaimer.setWrap(true);
        disclaimer.setWidth(1600);
        disclaimer.setPosition(1465 - disclaimer.getWidth() / 2f, 500);
        stage.addActor(disclaimer);
    }

    /** Loads the default button styles and back button */
    private void loadButtons() {
        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont12;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Set back button params
        TextButton backButton = new TextButton("<= Back", buttonStyle);
        backButton.setWidth(MainMenuScreen.BUTTON_WIDTH);
        backButton.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu
                game.setScreen(new MainMenuScreen(game, background));
                dispose();
            }
        });

        stage.addActor(backButton);
    }

    /** Implements show method of screen */
    @Override
    public void show() {
        loadUI();
    }

    /** Main rendering method for rendering to spriteBatch */
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
                Gdx.app.log("InfoScreen", "stage.draw() render error");
                stage.getBatch().end();
                e.printStackTrace();
            }
        }
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

    /** Implements dispose method of screen */
    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
    }
}
