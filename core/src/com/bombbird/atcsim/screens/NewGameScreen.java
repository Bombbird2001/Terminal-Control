package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.atcsim.AtcSim;

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
        camera.setToOrtho(false,1440, 810);
        viewport = new FitViewport(AtcSim.WIDTH, AtcSim.HEIGHT, camera);
        viewport.apply();

        //Set stage params
        stage = new Stage(new FitViewport(1440, 810));
        stage.getViewport().update(AtcSim.WIDTH, AtcSim.HEIGHT, true);
        Gdx.input.setInputProcessor(stage);

        //Set table params (for scrollpane)
        table = new Table();
        scrollTable = new Table();
    }

    private void loadUI() {
        int buttonWidth = 500;
        int buttonHeight = 100;

        //Reset stage
        stage.clear();

        //Set label params
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.fonts.defaultFont40;
        labelStyle.fontColor = Color.WHITE;
        Label headerLabel = new Label("Choose airport:", labelStyle);
        headerLabel.setWidth(buttonWidth);
        headerLabel.setHeight(buttonHeight);
        headerLabel.setPosition(1440 / 2.0f - buttonWidth / 2.0f, 810 * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);

        //Set button textures
        //Using main menu textures for now, will change later
        airportAtlas = new TextureAtlas(Gdx.files.internal("new_game/backbuttons.atlas"));
        skin = new Skin();
        skin.addRegions(airportAtlas);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = game.fonts.defaultFont20;
        buttonStyle.up = skin.getDrawable("Button_up");
        buttonStyle.down = skin.getDrawable("Button_down");

        //Load airports
        String[] airports = {"RCTP\nTaiwan Taoyuan International Airport", "WSSS\nSingapore Changi Airport", "VHHH\nHong Kong International Airport", "RJAA\nNarita International Airport", "WMKK\nKuala Lumpur International Airport", "WIII\nSoekarno-Hatta International Airport", "ZSPD\nShanghai Pudong International Airport", "VTBS\nBangkok Suvarnabhumi Airport", "VVTS\nTan Son Nhat International Airport"};
        for (String airport: airports) {
            final TextButton airportButton = new TextButton(airport, buttonStyle);
            airportButton.setName(airport);
            airportButton.setWidth(buttonWidth);
            airportButton.setHeight(buttonHeight);
            airportButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String name = actor.getName();
                    if (name.equals("RCTP\nTaiwan Taoyuan International Airport")) {
                        game.setScreen(new RctpScreen(game));
                    } else if (name.equals("WSSS\nSingapore Changi Airport")) {
                    } else if (name.equals("VHHH\nHong Kong International Airport")) {
                    } else if (name.equals("RJAA\nNarita International Airport")) {
                    } else if (name.equals("WMKK\nKuala Lumpur International Airport")) {
                    } else if (name.equals("WIII\nSoekarno-Hatta International Airport")) {
                    } else if (name.equals("ZSPD\nShanghai Pudong International Airport")) {
                    } else if (name.equals("VTBS\nBangkok Suvarnabhumi Airport")) {
                    } else if (name.equals("VVTS\nTan Son Nhat International Airport")) {
                    } else {
                        System.out.println("Airport not found");
                    }
                }
            });
            scrollTable.add(airportButton);
            scrollTable.row();
        }
        ScrollPane scrollPane = new ScrollPane(scrollTable);
        table.setBounds(1440 / 2.0f - 500 * 0.75f, 810 * 0.2f, buttonWidth * 1.5f, 810 * 0.6f);
        table.add(scrollPane).fill().expand();

        stage.addActor(table);

        //Set back button params
        TextButton backButton = new TextButton("<- Back", buttonStyle);
        backButton.setWidth(buttonWidth);
        backButton.setHeight(buttonHeight);
        backButton.setPosition(1440 / 2.0f - buttonWidth / 2.0f, 810 * 0.05f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu
                game.setScreen(new MainMenuScreen(game));
            }
        });

        stage.addActor(backButton);
    }

    @Override
    public void show() {
        loadUI();
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
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
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
        dispose();
    }

    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();
        skin.dispose();
        airportAtlas.dispose();
    }
}
