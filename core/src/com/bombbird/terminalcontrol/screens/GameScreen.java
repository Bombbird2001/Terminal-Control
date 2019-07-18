package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.RangeCircle;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.restrictions.PolygonObstacle;
import com.bombbird.terminalcontrol.entities.restrictions.CircleObstacle;
import com.bombbird.terminalcontrol.ui.DataTag;
import com.bombbird.terminalcontrol.ui.Ui;
import com.bombbird.terminalcontrol.sounds.SoundManager;
import com.bombbird.terminalcontrol.utilities.ErrorHandler;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.HashMap;

public class GameScreen implements Screen, GestureDetector.GestureListener, InputProcessor {
    public Stage stage;
    public Stage labelStage;

    //Init game (set in constructor)
    public final TerminalControl game;
    public boolean loading;
    public String loadingPercent;
    private float loadedTime = 0;

    //Set input processors
    public InputMultiplexer inputMultiplexer = new InputMultiplexer();
    public GestureDetector gd = new GestureDetector(40, 0.2f,1.1f, 0.15f, this);

    //Pinch zoom constants
    private float lastInitialDist = 0;
    private float lastScale = 1;

    //Double tap animation variables
    private boolean zooming = false;
    private boolean zoomedIn = false;

    //Create new camera
    public OrthographicCamera camera;
    public Viewport viewport;
    private float lastZoom = 1;

    //Create 2nd camera for UI
    public Ui ui;
    public Stage uiStage;
    public OrthographicCamera uiCam;
    public Viewport uiViewport;

    //Create texture stuff
    public final ShapeRenderer shapeRenderer = new ShapeRenderer();

    //Sounds
    public SoundManager soundManager = new SoundManager();

    //Create range circles
    private RangeCircle[] rangeCircles;

    //Create obstacle resources
    public Array<PolygonObstacle> obsArray;
    public Array<CircleObstacle> restArray;

    //Create airports
    public final HashMap<String, Airport> airports = new HashMap<String, Airport>();

    //HashMap of planes
    public final HashMap<String, Aircraft> aircrafts = new HashMap<String, Aircraft>();

    //Create waypoints
    public HashMap<String, Waypoint> waypoints;

    //Overlay screen when paused
    private PauseScreen pauseScreen;

    //Overlay screen for game settings
    private SettingsScreen settingsScreen;

    //Game state
    public enum State {
        PAUSE,
        RUN,
        SETTINGS
    }
    public static State state = State.RUN;

    public GameScreen(final TerminalControl game) {
        this.game = game;

        //Initiate range circles
        rangeCircles = new RangeCircle[2];

        loading = false;
        loadingPercent = "0%";

        pauseScreen = new PauseScreen(this);
        settingsScreen = new SettingsScreen((RadarScreen) this);

        setGameState(State.RUN);
    }

    /** Load radar screen range circles */
    public void loadRange() {
        rangeCircles[0] = new RangeCircle(10, -255);
        rangeCircles[1] = new RangeCircle(35, -1062);
        for (RangeCircle circle: rangeCircles) {
            stage.addActor(circle);
        }
    }

    /** Handles input from keyboard, mouse, moderates them */
    private void handleInput(float dt) {
        float ZOOM_CONSTANT = 0.6f;
        float SCROLL_CONSTANT = 150;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            //Zoom in
            camera.zoom += ZOOM_CONSTANT * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            //Zoom out
            camera.zoom -= ZOOM_CONSTANT * dt;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            //Move left
            camera.translate(-SCROLL_CONSTANT / camera.zoom * dt, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            //Move right
            camera.translate(SCROLL_CONSTANT / camera.zoom * dt, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            //Move down
            camera.translate(0, -SCROLL_CONSTANT / camera.zoom * dt, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //Move up
            camera.translate(0, SCROLL_CONSTANT / camera.zoom * dt, 0);
        }

        if (zooming) {
            if (zoomedIn) {
                camera.zoom += 0.05f;
            } else {
                camera.zoom -= 0.05f;
            }
        }

        moderateZoom();

        moderateCamPos();
    }

    /** Prevents user from over or under zooming */
    private void moderateZoom() {
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
    }

    /** Moderates the position of the camera to prevent it from going out of boundary */
    private void moderateCamPos() {
        //Setting new boundaries for camera position after zooming
        float effectiveViewportWidth = (camera.viewportWidth - ui.getPaneWidth()) * camera.zoom; //Take width of pane into account
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

        //System.out.println(camera.zoom);
        //System.out.println(camera.position.x);
        //System.out.println(camera.position.y);
    }

    /** Implements show method of screen; overridden in radarScreen class */
    @Override
    public void show() {
        //No default implementation
    }

    /** Renders shapes for aircraft trajectory line, obstacle and restricted areas; overridden in radarScreen class */
    public void renderShape() {
        for (RangeCircle rangeCircle : rangeCircles) {
            rangeCircle.renderShape();
        }
    }

    /** Main rendering method for rendering to spriteBatch */
    @Override
    public void render(float delta) {
        try {
            //Clear screen
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            switch (state) {
                case RUN:
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
                    labelStage.act(delta);

                    //Render each of the range circles, obstacles using shaperenderer
                    if (!loading) {
                        stage.getViewport().apply();
                        //Render shapes only if METAR has finished loading
                        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
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
                        Fonts.defaultFont20.draw(game.batch, loadingText + loadingPercent, 1560, 1550);
                    } else {
                        stage.draw();
                        game.batch.end();
                        game.batch.setProjectionMatrix(labelStage.getCamera().combined);
                        game.batch.begin();
                        labelStage.getViewport().apply();
                        labelStage.draw();
                    }
                    game.batch.end();

                    //Draw the UI overlay
                    uiCam.update();
                    if (!loading) {
                        game.batch.setProjectionMatrix(uiCam.combined);
                        uiStage.act();
                        game.batch.begin();
                        uiStage.getViewport().apply();
                        boolean success = false;
                        while (!success) {
                            try {
                                uiStage.draw();
                                game.batch.end();
                                success = true;
                            } catch (IllegalArgumentException e) {
                                Gdx.app.log("GameScreen", "uiStage.draw() render error");
                                uiStage.getBatch().end();
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case PAUSE:
                    game.batch.setProjectionMatrix(pauseScreen.getCamera().combined);
                    pauseScreen.getStage().act();
                    game.batch.begin();
                    pauseScreen.getStage().getViewport().apply();
                    pauseScreen.getStage().draw();
                    game.batch.end();
                    break;
                case SETTINGS:
                    game.batch.setProjectionMatrix(settingsScreen.getCamera().combined);
                    settingsScreen.getStage().act();
                    game.batch.begin();
                    settingsScreen.getStage().getViewport().apply();
                    settingsScreen.getStage().draw();
                    game.batch.end();
                    break;
                default:
                    Gdx.app.log("Game state error", "Invalid game state " + state + " set!");
            }
        } catch (Exception e) {
            ErrorHandler.sendGenericError(e);
        }
    }

    /** Implements resize method of screen, adjusts camera & viewport properties after resize for better UI */
    @Override
    public void resize(int width, int height) {
        TerminalControl.WIDTH = width;
        TerminalControl.HEIGHT = height;

        ui.updatePaneWidth();

        viewport.update(width, height, false);
        stage.getViewport().update(width, height, false);
        labelStage.getViewport().update(width, height, false);
        float xOffset = camera.zoom * 990;
        camera.position.set(camera.viewportWidth / 2 - xOffset, camera.viewportHeight / 2, 0);

        uiViewport.update(width, height, true);
        uiStage.getViewport().update(width, height, true);
        uiCam.position.set(uiCam.viewportWidth / 2f, uiCam.viewportHeight / 2f, 0);

        pauseScreen.getViewport().update(width, height, true);
        pauseScreen.getStage().getViewport().update(width, height, true);
        pauseScreen.getCamera().position.set(pauseScreen.getCamera().viewportWidth / 2f, pauseScreen.getCamera().viewportHeight / 2f, 0);

        settingsScreen.getViewport().update(width, height, true);
        settingsScreen.getStage().getViewport().update(width, height, true);
        settingsScreen.getCamera().position.set(settingsScreen.getCamera().viewportWidth / 2f, settingsScreen.getCamera().viewportHeight / 2f, 0);

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            boolean resizeAgain = false;
            int newWidth = width;
            int newHeight = height;
            if (newWidth < 960) {
                newWidth = 960;
                resizeAgain = true;
            }
            if (newHeight < 540) {
                newHeight = 540;
                resizeAgain = true;
            }
            if (resizeAgain) {
                resize(newWidth, newHeight);
            }
        }
    }

    /** Implements pause method of screen */
    @Override
    public void pause() {
        setGameState(State.PAUSE);
        soundManager.pause();
    }

    /** Implements resume method of screen */
    @Override
    public void resume() {
        setGameState(State.RUN);
        soundManager.resume();
    }

    /** Implements hide method of screen */
    @Override
    public void hide() {
        dispose();
    }

    /** Implements dispose method of screen, disposes resources after they're no longer needed */
    @Override
    public void dispose() {
        stage.clear();
        stage.dispose();

        labelStage.clear();
        labelStage.dispose();

        uiStage.clear();
        uiStage.dispose();

        shapeRenderer.dispose();

        aircrafts.clear();
        airports.clear();
        waypoints.clear();

        soundManager.dispose();
    }

    /** Implements touchdown method of gestureListener */
    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    /** Implements tap method of gestureListener, tests for tap and double tap */
    @Override
    public boolean tap(float x, float y, int count, int button) {
        TerminalControl.radarScreen.setSelectedAircraft(null);
        if (count == 2 && !loading) {
            zooming = true;
            return true;
        }
        return false;
    }

    /** Implements longPress method of gestureListener */
    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    /** Implements fling method of gestureListener */
    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    /** Implements pan method of gestureListener, tests for panning to shift radar screen around */
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

    /** Implements panStop method of gestureListener */
    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    /** Implements zoom method of gestureListener, tests for pinch zooming to adjust radar screen zoom level */
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

    /** Implements pinch method of gestureListener */
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    /** Implements pinchStop method of gestureListener */
    @Override
    public void pinchStop() {
        //No default implementation
    }

    /** Implements keyDown method of inputListener */
    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    /** Implements keyUp method of inputListener */
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    /** Implements keyTyped method of inputListener */
    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    /** Implements touchdown method of inputListener */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /** Implements touchUp method of inputListener */
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    /** Implements touchDragged method of inputListener */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    /** Implements mouseMoved method of inputListener */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    /** Implements scrolled method of inputListener, tests for scrolling to adjust radar screen zoom levels */
    @Override
    public boolean scrolled(int amount) {
        if (!loading) {
            camera.zoom += amount * 0.042f;
        }
        return true;
    }

    /** Sets the current game state to the input state */
    public void setGameState(State s) {
        GameScreen.state = s;
        pauseScreen.setVisible(s == State.PAUSE);
        settingsScreen.setVisible(s == State.SETTINGS);
        if (s == State.RUN) {
            Gdx.input.setInputProcessor(inputMultiplexer);
            soundManager.resume();
            DataTag.startTimers();
        } else if (s == State.PAUSE) {
            Gdx.input.setInputProcessor(pauseScreen.getStage());
            soundManager.pause();
            DataTag.pauseTimers();
        } else if (s == State.SETTINGS) {
            Gdx.input.setInputProcessor(settingsScreen.getStage());
            soundManager.pause();
            DataTag.pauseTimers();
        }
    }

    public SettingsScreen getSettingsScreen() {
        return settingsScreen;
    }
}
