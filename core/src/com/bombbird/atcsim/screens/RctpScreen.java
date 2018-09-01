package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.bombbird.atcsim.AtcSim;

class RctpScreen extends GameScreen {
    RctpScreen(final AtcSim game) {
        super(game);

        //Set camera params
        camera = new OrthographicCamera();
        viewport = new FillViewport(1440, 810, camera);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);

        //Set stage params
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        loadUI(1440, 810);
    }

    private void loadUI(int width, int height) {
        //Reset stage
        stage.clear();

        //Set test label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.fonts.defaultFont40;
        labelStyle.fontColor = Color.WHITE;
        Label headerLabel = new Label("Success!!!!!", labelStyle);
        headerLabel.setWidth(500);
        headerLabel.setHeight(100);
        headerLabel.setPosition(width / 2.0f - 500 / 2.0f, height * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);

        //Load textures
        airportAtlas = new TextureAtlas();
        airportAtlas.dispose();

        //Load radar screen
        radarScreenTexture = new Texture(Gdx.files.internal("game/default_screen.png"));
        radarScreenImage = new Image(radarScreenTexture);
        stage.addActor(radarScreenImage);
    }
}
