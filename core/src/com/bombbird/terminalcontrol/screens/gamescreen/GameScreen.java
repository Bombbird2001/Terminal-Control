package com.bombbird.terminalcontrol.screens.gamescreen;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.RangeCircle;
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.PauseScreen;
import com.bombbird.terminalcontrol.ui.DataTag;
import com.bombbird.terminalcontrol.ui.RandomTip;
import com.bombbird.terminalcontrol.ui.RequestFlasher;
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
    public boolean uiLoaded = false;
    public boolean loading;
    public float loadingTime = 0;
    public String loadingPercent;
    private float loadedTime = 0;
    private Label loadingLabel;
    private Label tipLabel;

    //Flag whether to quit the tutorial on next loop
    private boolean tutorialQuit;

    //Play time timer
    private float playTime;

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

    //Flashes rectangles to alert of user of aircraft with request if aircraft not within view
    public RequestFlasher requestFlasher;

    //Sounds
    public SoundManager soundManager = new SoundManager();

    //Create range circles
    private final Array<RangeCircle> rangeCircles;
    public int rangeCircleDist = 0;

    //Create obstacle resources
    public Array<Obstacle> obsArray;

    //Create airports
    public final HashMap<String, Airport> airports = new HashMap<>();

    //HashMap of planes
    public final HashMap<String, Aircraft> aircrafts = new HashMap<>();

    //Create waypoints
    public HashMap<String, Waypoint> waypoints;

    public boolean running;

    public int speed = 1;

    public GameScreen(final TerminalControl game) {
        this.game = game;

        //Initiate range circles
        rangeCircles = new Array<>();

        loading = false;
        loadingPercent = "0%";

        loadLabels();

        running = false;

        playTime = 0;
    }

    /** Load loading, tips labels */
    private void loadLabels() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;
        loadingLabel = new Label("", labelStyle);

        Label.LabelStyle labelStyle1 = new Label.LabelStyle();
        labelStyle1.font = Fonts.defaultFont16;
        labelStyle1.fontColor = Color.WHITE;
        tipLabel = new Label("", labelStyle1);
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
        float MIN_ZOOM = TerminalControl.increaseZoom ? 0.2f : 0.3f;
        float MAX_ZOOM_ANDROID = 0.6f;
        float MAX_ZOOM_DESKTOP = 1.0f;
        if (camera.zoom < MIN_ZOOM) {
            camera.zoom = MIN_ZOOM;
            zooming = false;
            zoomedIn = true;
        } else if (Gdx.app.getType() == Application.ApplicationType.Android && camera.zoom > MAX_ZOOM_ANDROID && !loading) {
            camera.zoom = MAX_ZOOM_ANDROID;
            zooming = false;
            zoomedIn = false;
        } else if (camera.zoom > MAX_ZOOM_DESKTOP) {
            camera.zoom = MAX_ZOOM_DESKTOP;
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

    /** Renders shapes for aircraft trajectory line, obstacle and restricted areas, APW, STCAS and others; overridden in radarScreen class */
    public void renderShape() {
        //No default implementation
    }

    /** Load radar screen range circles */
    public void loadRange() {
        for (RangeCircle circle: rangeCircles) {
            //Clear any existing circles
            circle.remove();
        }
        rangeCircles.clear();
        if (rangeCircleDist == 0) return;
        for (int i = rangeCircleDist; i < 50; i += rangeCircleDist) {
            RangeCircle circle = new RangeCircle(i);
            rangeCircles.add(circle);
            stage.addActor(circle);
        }
    }

    /** Renders the range circles */
    public void renderRangeCircles() {
        for (RangeCircle rangeCircle: rangeCircles) {
            rangeCircle.renderShape();
        }
    }

    /** Updates timer specifically for tutorial */
    public void updateTutorial() {
        //No default implementation
    }

    /** Main update method, overridden in radarScreen */
    public void update() {
        //No default implementation
    }

    /** Main rendering method for rendering to spriteBatch */
    @Override
    public void render(float delta) {
        try {
            //Clear screen
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));

            if (running) {
                //Test for input, update camera
                if (!loading) {
                    handleInput(delta);
                }
                camera.update();

                //Set rendering for stage camera
                game.batch.setProjectionMatrix(camera.combined);
                shapeRenderer.setProjectionMatrix(camera.combined);

                //Update stage
                for (int i = 0; i < speed; i++) {
                    if (checkTutorialPaused()) break;
                    stage.act(delta);
                    labelStage.act(delta);
                }

                //Render each of the range circles, obstacles using shaperenderer, update loop
                if (!loading) {
                    stage.getViewport().apply();
                    for (int i = 0; i < speed; i++) {
                        updateTutorial(); //Tutorial timer is updated even if tutorial is paused
                        if (!checkTutorialPaused()) update();
                    }
                    //Update playtime counter
                    playTime += delta;
                    //Render shapes only if METAR has finished loading
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                    renderShape();
                    shapeRenderer.end();
                }

                //Draw to the spritebatch
                game.batch.setProjectionMatrix(labelStage.getCamera().combined);
                boolean liveWeather = ((RadarScreen) this).getWeatherSel() == RadarScreen.Weather.LIVE && !((RadarScreen) this).getTutorial();
                String loadingText = liveWeather ? "Loading live weather.   " : "Loading.   ";
                if (loading) {
                    //Write loading text if loading
                    loadingTime += delta;
                    loadedTime += delta;
                    if (loadedTime > 1.5) {
                        loadedTime = 0;
                        loadingText = liveWeather ? "Loading live weather.   " : "Loading.   ";
                    } else if (loadedTime > 1) {
                        loadingText = liveWeather ? "Loading live weather... " : "Loading... ";
                    } else if (loadedTime > 0.5) {
                        loadingText = liveWeather ? "Loading live weather..  " : "Loading..  ";
                    }
                    loadingText += loadingPercent;
                    loadingLabel.setText(loadingText);
                    loadingLabel.setPosition(1920 - loadingLabel.getPrefWidth() / 2, 1550);

                    game.batch.begin();
                    loadingLabel.draw(game.batch, 1);
                    if (!RandomTip.tipsLoaded()) RandomTip.loadTips();
                    if ("".equals(tipLabel.getText().toString())) tipLabel.setText(RandomTip.randomTip());
                    tipLabel.setPosition(1920 - tipLabel.getPrefWidth() / 2, 960);
                    tipLabel.draw(game.batch, 1);
                    game.batch.end();
                } else if (checkAircraftLoaded()) {
                    stage.draw();
                    labelStage.getViewport().apply();
                    labelStage.draw();

                    //Special shape rendering here so it won't be blocked by labels
                    requestFlasher.update();
                }

                //Draw the UI overlay
                uiCam.update();
                if (!loading) {
                    game.batch.setProjectionMatrix(uiCam.combined);
                    uiStage.act();
                    uiStage.getViewport().apply();
                    uiStage.draw();
                }
            }

            if (tutorialQuit) {
                TerminalControl.radarScreen = null;
                game.setScreen(new MainMenuScreen(game, null));
            }
        } catch (Exception e) {
            ErrorHandler.sendGenericError(e, true);
        }
    }

    /** Try to fix crash on some devices where navstate is null after loading */
    private boolean checkAircraftLoaded() {
        for (Aircraft aircraft: aircrafts.values()) {
            if (aircraft.getNavState() == null) return false;
        }

        return true;
    }

    /** Check if tutorial is in paused state */
    private boolean checkTutorialPaused() {
        return (((RadarScreen)this).getTutorialManager() != null && ((RadarScreen)this).getTutorialManager().isPausedForReading());
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
        setGameRunning(false);
    }

    /** Implements resume method of screen */
    @Override
    public void resume() {
        setGameRunning(true);
    }

    /** Implements hide method of screen */
    @Override
    public void hide() {
        //Not disposing unless game is quit specifically
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
            //Gdx.app.postRunnable(() -> obsArray = FileLoader.loadObstacles()); Reload obstacles - Debug use only
            zooming = true;
            return true;
        }
        //Shows approximate position of mouse pointer click in game world - Debug use only
        //Vector3 vector3 = new Vector3(x, y, 0);
        //Vector3 vector3_new = stage.getCamera().unproject(vector3);
        //Gdx.app.log("Coordinates", vector3_new.toString());
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
        float screenRatio = Math.max(screenHeightRatio, screenWidthRatio);
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

    /** Sets whether game is running */
    public void setGameRunning(boolean running) {
        this.running = running;

        if (this.running) {
            float prevCamX = camera.position.x;
            float prevCamY = camera.position.y;
            if (game.getScreen() != this) game.setScreen(this);
            Gdx.input.setInputProcessor(inputMultiplexer);
            camera.position.x = prevCamX;
            camera.position.y = prevCamY;
            soundManager.resume();
            DataTag.startTimers();
            DataTag.setBorderBackground();
        } else {
            soundManager.pause();
            DataTag.pauseTimers();
            game.setScreen(new PauseScreen(game, (RadarScreen) this));
        }
    }

    public void setTutorialQuit(boolean tutorialQuit) {
        this.tutorialQuit = tutorialQuit;
    }

    public float getPlayTime() {
        return playTime;
    }

    public void setPlayTime(float playTime) {
        this.playTime = playTime;
    }
}
