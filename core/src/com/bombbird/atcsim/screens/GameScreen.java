package com.bombbird.atcsim.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.RangeCircle;

public class GameScreen implements Screen, GestureDetector.GestureListener, InputProcessor {
    //Init game (set in constructor)
    final AtcSim game;
    Stage stage;

    //Set input processors
    InputMultiplexer inputMultiplexer = new InputMultiplexer();
    InputProcessor inputProcessor1 = this;
    InputProcessor inputProcessor2;
    private int lastX;
    private int lastY;

    //Create new camera
    OrthographicCamera camera;
    Viewport viewport;

    //Zoom, pan constants
    private static float ZOOMCONSTANT = 0.5f;
    private static float SCROLLCONSTANT = 150.0f;

    //Create texture stuff
    Skin skin;
    ShapeRenderer shapeRenderer;

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
        rangeCircles[0] = new RangeCircle(game, 10, -67, shapeRenderer);
        rangeCircles[1] = new RangeCircle(game, 30, -235, shapeRenderer);
        rangeCircles[2] = new RangeCircle(game, 50, -397, shapeRenderer);
        for (RangeCircle circle: rangeCircles) {
            stage.addActor(circle);
        }
    }

    void loadScroll() {
        //Add scroll listener
        stage.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                camera.zoom += amount * ZOOMCONSTANT / 12.0f;
                return false;
            }
        });
    }

    private void handleInput(float dt) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += ZOOMCONSTANT * dt;
            //If the A Key is pressed, add 0.02 to the Camera's Zoom
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= ZOOMCONSTANT * dt;
            //If the Q Key is pressed, subtract 0.02 from the Camera's Zoom
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-SCROLLCONSTANT * dt, 0, 0);
            //If the LEFT Key is pressed, translate the camera -3 units in the X-Axis
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(SCROLLCONSTANT * dt, 0, 0);
            //If the RIGHT Key is pressed, translate the camera 3 units in the X-Axis
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -SCROLLCONSTANT * dt, 0);
            //If the DOWN Key is pressed, translate the camera -3 units in the Y-Axis
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, SCROLLCONSTANT * dt, 0);
            //If the UP Key is pressed, translate the camera 3 units in the Y-Axis
        }
        if (camera.zoom < 0.2f) {
            camera.zoom = 0.2f;
        } else if (camera.zoom > 1.0f) {
            camera.zoom = 1.0f;
        }

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float xDeviation = -(effectiveViewportWidth - camera.viewportWidth) / 2f;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;
        float yDeviation = -(effectiveViewportHeight - camera.viewportHeight) / 2f;
        float leftLimit = effectiveViewportWidth / 2f;
        float rightLimit = leftLimit + 2 * xDeviation;
        float downLimit = effectiveViewportHeight / 2f;
        float upLimit = downLimit + 2 * yDeviation;

        if (camera.position.x < leftLimit) {
            camera.position.x = leftLimit;
        } else if (camera.position.x > rightLimit) {
            camera.position.x = rightLimit;
        }
        if (camera.position.y < downLimit) {
            camera.position.y = downLimit;
        } else if (camera.position.y > upLimit) {
            camera.position.y = upLimit;
        }
    }

    @Override
    public void show() {

    }

    void renderShape() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput(delta);
        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        stage.act(delta);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (RangeCircle rangeCircle: rangeCircles) {
            rangeCircle.renderShape();
        }
        renderShape();
        shapeRenderer.end();
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
        shapeRenderer.dispose();
        stage.clear();
        stage.dispose();
        skin.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        lastX = screenX;
        lastY = screenY;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        int deltaX = (int)((screenX - lastX) * camera.zoom);
        int deltaY = (int)((screenY - lastY) * camera.zoom);
        camera.translate(-deltaX, deltaY);
        lastX = screenX;
        lastY = screenY;
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
