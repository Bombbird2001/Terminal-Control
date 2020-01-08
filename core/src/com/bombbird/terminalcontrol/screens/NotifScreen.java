package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class NotifScreen implements Screen {
    public final TerminalControl game;
    private Stage stage;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Image background;

    private static final Array<String> notifArray = new Array<>();

    public NotifScreen(final TerminalControl game, Image background) {
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

        loadNotifs();
    }

    /** Loads all the notifications needed */
    private void loadNotifs() {
        notifArray.add("Hello players, here's an important notice to take note of. In order to reduce resemblance to real life airports, " +
                "airport names, airport ICAO codes, SID/STAR names, as well as waypoint names have been changed. However, the procedures " +
                "themselves remain the same and is still based on that of the real airport. Saves will remain compatible - existing planes " +
                "will continue using old SIDs and STARs, while newly generated ones will use the new SIDs and STARs. " +
                "Old saves that have not been loaded for 6 months or more may not be compatible. We apologise for the inconvenience, " +
                "and we thank you for your understanding and continued support of Terminal Control.");
    }

    /** Loads the full UI of this screen */
    private void loadUI() {
        //Reset stage
        stage.clear();

        loadLabel();
        loadButtons();
    }

    /** Loads labels for credits, disclaimers, etc */
    public void loadLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.WHITE;

        Label notif = new Label(notifArray.get(TerminalControl.revision - 1), labelStyle);
        notif.setWrap(true);
        notif.setWidth(2000);
        notif.setPosition(1465 - notif.getWidth() / 2f, 800);
        stage.addActor(notif);
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
