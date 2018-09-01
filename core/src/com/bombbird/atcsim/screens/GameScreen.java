package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.RangeCircle;

public class GameScreen implements Screen {
    //Init game (set in constructor)
    final AtcSim game;
    Stage stage;

    //Create new camera
    OrthographicCamera camera;
    Viewport viewport;

    //Create texture stuff
    Skin skin;
    private ShapeRenderer shapeRenderer;

    //Create range circles
    private RangeCircle[] rangeCircles;

    GameScreen(final AtcSim game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();

        //Initiate range circles
        rangeCircles = new RangeCircle[3];
    }

    void loadRange() {
        //Load radar screen
        rangeCircles[0] = new RangeCircle(game, 10, 60);
        rangeCircles[1] = new RangeCircle(game, 30, 228);
        rangeCircles[2] = new RangeCircle(game, 50, 390);
        for (RangeCircle circle: rangeCircles) {
            stage.addActor(circle);
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        stage.act(delta);
        stage.draw();
        game.batch.begin();
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
        dispose();
    }

    @Override
    public void dispose() {
        stage.clear();
        for (RangeCircle circle: rangeCircles) {
            circle.dispose();
        }
        stage.dispose();
        skin.dispose();
        shapeRenderer.dispose();
    }
}
