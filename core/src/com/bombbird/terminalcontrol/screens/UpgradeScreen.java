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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.UnlockManager;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.Map;

public class UpgradeScreen implements Screen {
    //Init game (set in constructor)
    private final TerminalControl game;
    private Stage stage;

    //Create new camera
    private OrthographicCamera camera;
    private Viewport viewport;

    //Scroll table
    private Table scrollTable;

    //Background image
    private Image background;

    public UpgradeScreen(final TerminalControl game, Image background) {
        this.game = game;
        this.background = background;

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

    @Override
    public void show() {
        if (Fonts.defaultFont6 == null) {
            //Regenerate fonts that were disposed
            Fonts.generateAllFonts();
        }
        loadUI();
    }

    /** Loads the UI elements to be rendered on screen */
    private void loadUI() {
        //Reset stage
        stage.clear();

        stage.addActor(background);

        loadButtons();
        loadLabel();
        loadScroll();
        loadUnlocks();
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

    /** Loads the appropriate title for label */
    private void loadLabel() {
        //Set title label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        //Set title label
        Label headerLabel = new Label("Milestones & Unlocks", labelStyle);
        headerLabel.setWidth(MainMenuScreen.BUTTON_WIDTH);
        headerLabel.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);

        //Set additional description label style
        Label.LabelStyle labelStyle1 = new Label.LabelStyle();
        labelStyle1.font = Fonts.defaultFont12;
        labelStyle1.fontColor = Color.WHITE;

        //Set description label
        Label label = new Label("Once an option is unlocked, you can visit the settings page to change to the desired option.\nTotal planes landed: " + UnlockManager.getPlanesLanded(), labelStyle1);
        label.setPosition((2880 - label.getWidth()) / 2, 1620 * 0.75f);
        label.setAlignment(Align.center);
        stage.addActor(label);
    }

    /** Loads the scrollpane used to contain unlocks, milestones */
    private void loadScroll() {
        scrollTable = new Table();
        ScrollPane scrollPane = new ScrollPane(scrollTable);

        scrollPane.setX(2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.8f);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(MainMenuScreen.BUTTON_WIDTH * 1.6f);
        scrollPane.setHeight(1620 * 0.5f);
        scrollPane.getStyle().background = TerminalControl.skin.getDrawable("ListBackground");

        stage.addActor(scrollPane);
    }

    /** Loads the unlocks into scroll pane */
    private void loadUnlocks() {
        //Set scroll pane label textures
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.WHITE;
        labelStyle.background = TerminalControl.skin.getDrawable("Button_up");

        for (Map.Entry<String, Integer> entry: UnlockManager.unlockList.entrySet()) {
            Label label = new Label("\n" + UnlockManager.unlockDescription.get(entry.getKey()) + "\n", labelStyle);
            label.setWrap(true);
            label.setAlignment(Align.center);
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 1.1f);
            //Layout twice to set correct width & height
            scrollTable.layout();
            scrollTable.layout();
            int required = entry.getValue();
            Label label1 = new Label(Math.min(UnlockManager.getPlanesLanded(), required) + "/" + required, labelStyle);
            label1.setAlignment(Align.center);
            scrollTable.add(label1).width(MainMenuScreen.BUTTON_WIDTH * 0.3f).height(label.getHeight());
            Image image = new Image(TerminalControl.skin.getDrawable(UnlockManager.getPlanesLanded() >= required ? "Checked" : "Unchecked"));
            float ratio = MainMenuScreen.BUTTON_WIDTH * 0.15f / image.getWidth();
            scrollTable.add(image).width(MainMenuScreen.BUTTON_WIDTH * 0.15f).height(ratio * image.getHeight()).padLeft(MainMenuScreen.BUTTON_WIDTH * 0.025f).padRight(MainMenuScreen.BUTTON_WIDTH * 0.025f);
            scrollTable.row();
        }

        for (Map.Entry<String, String> entry: UnlockManager.easterEggList.entrySet()) {
            boolean unlocked = UnlockManager.unlocks.contains(entry.getKey());
            Label label = new Label("\n" + (unlocked ? entry.getValue() : "?????")+ "\n", labelStyle);
            label.setWrap(true);
            label.setAlignment(Align.center);
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 1.1f);
            //Layout twice to set correct width & height
            scrollTable.layout();
            scrollTable.layout();
            Label label1 = new Label("- / -", labelStyle);
            label1.setAlignment(Align.center);
            scrollTable.add(label1).width(MainMenuScreen.BUTTON_WIDTH * 0.3f).height(label.getHeight());
            Image image = new Image(TerminalControl.skin.getDrawable(unlocked ? "Checked" : "Unchecked"));
            float ratio = MainMenuScreen.BUTTON_WIDTH * 0.15f / image.getWidth();
            scrollTable.add(image).width(MainMenuScreen.BUTTON_WIDTH * 0.15f).height(ratio * image.getHeight()).padLeft(MainMenuScreen.BUTTON_WIDTH * 0.025f).padRight(MainMenuScreen.BUTTON_WIDTH * 0.025f);
            scrollTable.row();
        }
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
