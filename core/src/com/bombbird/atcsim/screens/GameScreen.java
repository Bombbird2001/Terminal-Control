package com.bombbird.atcsim.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.RangeCircle;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.aircrafts.Aircraft;
import com.bombbird.atcsim.entities.restrictions.Obstacle;
import com.bombbird.atcsim.entities.restrictions.RestrictedArea;

import java.util.HashMap;

public class GameScreen implements Screen, GestureDetector.GestureListener, InputProcessor {
    //Init game (set in constructor)
    private final AtcSim game;
    public static Stage stage;
    private boolean aircraftLoaded;
    public boolean loading;
    public String loadingPercent;
    private float loadedTime = 0;

    //Set input processors
    InputMultiplexer inputMultiplexer = new InputMultiplexer();
    InputProcessor inputProcessor1 = this;
    InputProcessor inputProcessor2;
    InputProcessor inputProcessor3;
    GestureDetector gd = new GestureDetector(40, 0.2f,1.1f, 0.15f, this);

    //Pinch zoom constants
    private float lastInitialDist = 0;
    private float lastScale = 1;

    //Double tap animation variables
    private boolean zooming = false;
    private boolean zoomedIn = false;

    //Create new camera
    OrthographicCamera camera;
    Viewport viewport;
    private float lastZoom = 1;

    //Create 2nd camera for UI
    public static Ui ui;
    public static Stage uiStage;
    OrthographicCamera uiCam;
    Viewport uiViewport;

    //Create texture stuff
    Skin skin;
    public static ShapeRenderer shapeRenderer;

    //Create range circles
    private RangeCircle[] rangeCircles;

    //Create obstacle resources
    Array<Obstacle> obsArray;
    Array<RestrictedArea> restArray;

    //Create airports + wind data
    public static HashMap<String, Airport> airports;

    //Array of planes
    public static HashMap<String, Aircraft> aircrafts;

    //Create waypoints
    public static HashMap<String, Waypoint> waypoints;

    GameScreen(final AtcSim game) {
        this.game = game;
        shapeRenderer = new ShapeRenderer();

        //Initiate range circles
        rangeCircles = new RangeCircle[3];

        //Initiate airports
        airports = new HashMap<String, Airport>();

        loading = false;
        aircraftLoaded = false;
        loadingPercent = "0%";
    }

    void loadRange() {
        //Load radar screen range circles
        rangeCircles[0] = new RangeCircle(10, -255);
        rangeCircles[1] = new RangeCircle(30, -900);
        rangeCircles[2] = new RangeCircle(50, -1548);
        for (RangeCircle circle: rangeCircles) {
            stage.addActor(circle);
        }
    }

    private void handleInput(float dt) {
        float ZOOMCONSTANT = 0.6f;
        float SCROLLCONSTANT = 150;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            //Zoom in
            camera.zoom += ZOOMCONSTANT * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            //Zoom out
            camera.zoom -= ZOOMCONSTANT * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            //Move left
            camera.translate(-SCROLLCONSTANT / camera.zoom * dt, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            //Move right
            camera.translate(SCROLLCONSTANT / camera.zoom * dt, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            //Move down
            camera.translate(0, -SCROLLCONSTANT / camera.zoom * dt, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //Move up
            camera.translate(0, SCROLLCONSTANT / camera.zoom * dt, 0);
        }

        if (zooming) {
            if (zoomedIn) {
                camera.zoom += 0.05f;
            } else {
                camera.zoom -= 0.05f;
            }
        }

        //Make sure user doesn't zoom in too much or zoom out of bounds
        if (camera.zoom < 0.3f) {
            camera.zoom = 0.3f;
            zooming = false;
            zoomedIn = true;
        } else if (Gdx.app.getType() == Application.ApplicationType.Android && camera.zoom > 0.6f && !loading) {
            camera.zoom = 0.6f;
            zooming = false;
            zoomedIn = false;
        } else if (camera.zoom > 1.0f) {
            camera.zoom = 1.0f;
            zooming = false;
            zoomedIn = false;
        }
        camera.translate(-990 * (camera.zoom - lastZoom), 0); //Ensure camera zooms into the current center
        lastZoom = camera.zoom;

        //Setting new boundaries for camera position after zooming
        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float xDeviation = -(effectiveViewportWidth - camera.viewportWidth) / 2f;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;
        float yDeviation = -(effectiveViewportHeight - camera.viewportHeight) / 2f;
        float xOffset = camera.zoom * 990; //Since I shifted camera to the left by 990 px
        float leftLimit = effectiveViewportWidth / 2f - xOffset;
        float rightLimit = leftLimit + 2 * xDeviation;
        float downLimit = effectiveViewportHeight / 2f;
        float upLimit = downLimit + 2 * yDeviation;

        //Prevent camera from going out of boundary
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

    public void newAircraft() {

    }

    @Override
    public void show() {

    }

    void renderShape() {

    }

    @Override
    public void render(float delta) {
        if (!loading && !aircraftLoaded) {
            //Load the initial aircrafts if METAR has finished loading but aircrafts not loaded yet
            newAircraft();
            aircraftLoaded = true;
        }

        //Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Test for input, update camera
        if (!loading) {
            handleInput(delta);
        }
        camera.update();

        //Set rendering for stage camera
        game.batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        //Update stage
        stage.act(delta);

        //Render each of the range circles, obstacles using shaperenderer
        if (!loading) {
            //Render shapes only if METAR has finished loading
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            for (RangeCircle rangeCircle : rangeCircles) {
                rangeCircle.renderShape();
            }
            renderShape();
            shapeRenderer.end();
        }

        //Draw to the spritebatch
        game.batch.begin();
        String loadingText = "Loading.   ";
        if (loading) {
            //Write loading text if loading
            loadedTime += Gdx.graphics.getDeltaTime();
            if (loadedTime > 1.5) {
                loadedTime = 0;
                loadingText = "Loading.   ";
            } else if (loadedTime > 1) {
                loadingText = "Loading... ";
            } else if (loadedTime > 0.5) {
                loadingText = "Loading..  ";
            }
            AtcSim.fonts.defaultFont20.draw(game.batch, loadingText + loadingPercent, 1560, 1550);
        } else {
            stage.draw();
        }
        game.batch.end();
        if (!loading) {
            ui.update();
            game.batch.setProjectionMatrix(uiCam.combined);
            uiStage.act();
            game.batch.begin();
            uiStage.draw();
            game.batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        stage.getViewport().update(width, height, false);
        float xOffset = camera.zoom * 990;
        camera.position.set(camera.viewportWidth / 2 - xOffset, camera.viewportHeight / 2, 0);

        uiViewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        uiCam.position.set(uiCam.viewportWidth / 2, uiCam.viewportHeight / 2, 0);
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
        RadarScreen.setSelectedAircraft(null);
        if (count == 2 && !loading) {
            zooming = true;
            return true;
        }
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
        if (loading) {
            return false;
        }
        float screenHeightRatio = 3240f / Gdx.graphics.getHeight();
        float screenWidthRatio = 5760f / Gdx.graphics.getWidth();
        float screenRatio = (screenHeightRatio > screenWidthRatio) ? screenHeightRatio : screenWidthRatio;
        camera.translate(-deltaX * camera.zoom * screenRatio, deltaY * camera.zoom * screenRatio);
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (loading) {
            return true;
        }
        if (initialDistance != lastInitialDist) {
            //New zoom
            lastInitialDist = initialDistance;
            lastScale = camera.zoom;
        }
        float ratio = lastInitialDist / distance;
        camera.zoom = lastScale * ratio;
        camera.update();
        return true;
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (!loading) {
            camera.zoom += amount * 0.042f;
        }
        return true;
    }
}
