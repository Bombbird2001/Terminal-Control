package com.bombbird.terminalcontrol.screens.selectgamescreen;

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
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class SelectGameScreen implements Screen {
    //Init game (set in constructor)
    public final TerminalControl game;
    private Stage stage;
    private Table scrollTable;

    //Create new camera
    private OrthographicCamera camera;
    private Viewport viewport;

    //Styles
    private Label.LabelStyle labelStyle;
    private TextButton.TextButtonStyle buttonStyle;

    //Background image (from MainMenuScreen)
    private Image background;

    public SelectGameScreen(final TerminalControl game, Image background) {
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

        //Set table params (for scrollpane)
        scrollTable = new Table();

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
        loadScroll();
    }

    /** Loads the appropriate labelStyle, and is overridden to load a label with the appropriate text */
    public void loadLabel() {
        //Set label style
        labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;
    }

    /** Loads the default button styles and back button */
    private void loadButtons() {
        //Set button textures
        buttonStyle = new TextButton.TextButtonStyle();
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
            }
        });

        stage.addActor(backButton);
    }

    /** Loads the contents of the scrollPane */
    public void loadScroll() {
        //No default implementation
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
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));

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
                Gdx.app.log("SelectGameScreen", "stage.draw() render error");
                game.batch.end();
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
        dispose();
    }

    /** Implements dispose method of screen, called to dispose assets once unneeded */
    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
    }

    public Label.LabelStyle getLabelStyle() {
        return labelStyle;
    }

    public TextButton.TextButtonStyle getButtonStyle() {
        return buttonStyle;
    }

    public Stage getStage() {
        return stage;
    }

    public Table getScrollTable() {
        return scrollTable;
    }
}
