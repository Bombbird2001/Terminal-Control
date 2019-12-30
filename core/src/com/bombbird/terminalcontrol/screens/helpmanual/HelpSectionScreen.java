package com.bombbird.terminalcontrol.screens.helpmanual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.selectgamescreen.AirportHelpScreen;
import com.bombbird.terminalcontrol.screens.selectgamescreen.HelpScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class HelpSectionScreen implements Screen {
    public final TerminalControl game;
    private Stage stage;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Image background;
    private Table scrollTable;
    private String page;

    public HelpSectionScreen(TerminalControl game, Image background, String page) {
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

        this.page = page;
    }

    public void loadUI() {
        //Reset stage
        stage.clear();

        stage.addActor(background);

        loadLabel();
        loadScroll();
        loadContent();
        loadButtons();
    }

    public void loadLabel() {
        //Set label params
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;
        Label headerLabel = new Label(page, labelStyle);
        headerLabel.setWidth(MainMenuScreen.BUTTON_WIDTH);
        headerLabel.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);
    }

    private void loadScroll() {
        scrollTable = new Table();
        ScrollPane scrollPane = new ScrollPane(scrollTable);

        scrollPane.setX(2880 / 2f - MainMenuScreen.BUTTON_WIDTH);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(MainMenuScreen.BUTTON_WIDTH * 2);
        scrollPane.setHeight(1620 * 0.6f);
        scrollPane.getStyle().background = TerminalControl.skin.getDrawable("ListBackground");

        stage.addActor(scrollPane);
    }

    private void loadContent() {
        HelpManager.loadContent(scrollTable, page);
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
                if (page.length() == 4) {
                    game.setScreen(new AirportHelpScreen(game, background));
                } else {
                    game.setScreen(new HelpScreen(game, background));
                }
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
                Gdx.app.log("HelpSectionScreen", "stage.draw() render error");
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
