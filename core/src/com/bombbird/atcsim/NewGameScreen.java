package com.bombbird.atcsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class NewGameScreen implements Screen {
    //Init game (set in constructor)
    private final AtcSim game;
    private Stage stage;
    private Table table;
    private Table scrollTable;

    //Create new camera
    private OrthographicCamera camera;
    private Viewport viewport;

    //Create texture stuff
    private TextureAtlas airportAtlas;
    private Skin skin;

    NewGameScreen(final AtcSim game) {
        this.game = game;

        //Set camera params
        camera = new OrthographicCamera();
        viewport = new FillViewport(1440, 810, camera);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);

        //Set stage params
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        //Set table params (for scrollpane)
        table = new Table();
        scrollTable = new Table();

        loadUI(1440, 810);
    }

    private void loadUI(int width, int height) {
        int buttonWidth = width / 4;
        int buttonHeight = height / 5;

        //Reset stage
        stage.clear();

        //Set label params
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.defaultFont40;
        labelStyle.fontColor = Color.WHITE;
        Label headerLabel = new Label("Choose airport:", labelStyle);
        headerLabel.setWidth(buttonWidth);
        headerLabel.setHeight(height / 10.0f);
        headerLabel.setPosition(width / 2.0f - buttonWidth / 2.0f, height * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);

        //Set button textures
        //Using main menu textures for now, will change later
        airportAtlas = new TextureAtlas(Gdx.files.internal("buttons/main_menu/mainmenubuttons.atlas"));
        skin = new Skin();
        skin.addRegions(airportAtlas);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.defaultFont40;
        buttonStyle.up = skin.getDrawable("Button_up");
        buttonStyle.down = skin.getDrawable("Button_down");

        //Load airports
        String[] airports = {"RCTP", "WSSS"};//, "VHHH", "RJAA", "WMKK", "WIII", "ZSPD", "VTBS", "VVTS"};
        for (String airport: airports) {
            TextButton airportButton = new TextButton(airport, buttonStyle);
            airportButton.setWidth(buttonWidth);
            airportButton.setHeight(buttonHeight);
            scrollTable.add(airportButton);
            scrollTable.row();
        }
        ScrollPane scrollPane = new ScrollPane(scrollTable);
        table.setBounds(width / 2.0f - buttonWidth * 0.75f, height * 0.2f, buttonWidth * 1.5f, height * 0.6f);
        table.add(scrollPane).fill().expand();

        stage.addActor(table);

        //Set back button params
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
        airportAtlas.dispose();
    }
}
