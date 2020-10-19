package com.bombbird.terminalcontrol.screens.gamescreen

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.input.GestureDetector.GestureListener
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.viewport.Viewport
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.RangeCircle
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.screens.MainMenuScreen
import com.bombbird.terminalcontrol.screens.PauseScreen
import com.bombbird.terminalcontrol.sounds.SoundManager
import com.bombbird.terminalcontrol.ui.DataTag
import com.bombbird.terminalcontrol.ui.RandomTip.loadTips
import com.bombbird.terminalcontrol.ui.RandomTip.randomTip
import com.bombbird.terminalcontrol.ui.RandomTip.tipsLoaded
import com.bombbird.terminalcontrol.ui.RequestFlasher
import com.bombbird.terminalcontrol.ui.Ui
import com.bombbird.terminalcontrol.utilities.ErrorHandler
import com.bombbird.terminalcontrol.utilities.Fonts
import kotlin.collections.HashMap

open class GameScreen(val game: TerminalControl) : Screen, GestureListener, InputProcessor {
    lateinit var stage: Stage
    lateinit var labelStage: Stage
    var uiLoaded = false
    var loading = false
    var loadingTime = 0f
    var loadingPercent = "0%"
    private var loadedTime = 0f
    private lateinit var loadingLabel: Label
    private lateinit var tipLabel: Label

    //Flag whether to quit the tutorial on next loop
    private var tutorialQuit = false

    //Play time timer
    var playTime: Float

    //Set input processors
    var inputMultiplexer = InputMultiplexer()
    lateinit var gd: GestureDetector

    //Pinch zoom constants
    private var lastInitialDist = 0f
    private var lastScale = 1f

    //Double tap animation variables
    private var zooming = false
    private var zoomedIn = false

    //Create new camera
    lateinit var camera: OrthographicCamera
    lateinit var viewport: Viewport
    private var lastZoom = 1f

    //Create 2nd camera for UI
    lateinit var ui: Ui
    lateinit var uiStage: Stage
    lateinit var uiCam: OrthographicCamera
    lateinit var uiViewport: Viewport

    //Create texture stuff
    val shapeRenderer = ShapeRenderer()

    //Flashes rectangles to alert of user of aircraft with request if aircraft not within view
    lateinit var requestFlasher: RequestFlasher

    //Sounds
    var soundManager = SoundManager()

    //Create range circles
    private val rangeCircles: Array<RangeCircle> = Array()
    var rangeCircleDist = 0

    //Create obstacle resources
    var obsArray = Array<Obstacle>()

    //Create airports
    val airports = HashMap<String, Airport>()

    //HashMap of planes
    val aircrafts = HashMap<String, Aircraft>()

    //Create waypoints
    var waypoints: HashMap<String, Waypoint> = HashMap()
    var running = false
    var speed = 1

    init {
        //Initiate range circles
        loadLabels()
        playTime = 0f
    }

    /** Load loading, tips labels  */
    private fun loadLabels() {
        val labelStyle = LabelStyle()
        labelStyle.font = Fonts.defaultFont20
        labelStyle.fontColor = Color.WHITE
        loadingLabel = Label("", labelStyle)
        val labelStyle1 = LabelStyle()
        labelStyle1.font = Fonts.defaultFont16
        labelStyle1.fontColor = Color.WHITE
        tipLabel = Label("", labelStyle1)
    }

    /** Handles input from keyboard, mouse, moderates them  */
    private fun handleInput(dt: Float) {
        val zoomConstant = 0.6f
        val scrollConstant = 150f
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            //Zoom in
            camera.zoom += zoomConstant * dt
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            //Zoom out
            camera.zoom -= zoomConstant * dt
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            //Move left
            camera.translate(-scrollConstant / camera.zoom * dt, 0f, 0f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            //Move right
            camera.translate(scrollConstant / camera.zoom * dt, 0f, 0f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            //Move down
            camera.translate(0f, -scrollConstant / camera.zoom * dt, 0f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //Move up
            camera.translate(0f, scrollConstant / camera.zoom * dt, 0f)
        }
        if (zooming) {
            if (zoomedIn) {
                camera.zoom += 0.05f
            } else {
                camera.zoom -= 0.05f
            }
        }
        moderateZoom()
        moderateCamPos()
    }

    /** Prevents user from over or under zooming  */
    private fun moderateZoom() {
        val minZoom = if (TerminalControl.increaseZoom) 0.2f else 0.3f
        val maxZoomAndroid = 0.6f
        val maxZoomDesktop = 1.0f
        if (camera.zoom < minZoom) {
            camera.zoom = minZoom
            zooming = false
            zoomedIn = true
        } else if (Gdx.app.type == Application.ApplicationType.Android && camera.zoom > maxZoomAndroid && !loading) {
            camera.zoom = maxZoomAndroid
            zooming = false
            zoomedIn = false
        } else if (camera.zoom > maxZoomDesktop) {
            camera.zoom = maxZoomDesktop
            zooming = false
            zoomedIn = false
        }
        camera.translate(-990 * (camera.zoom - lastZoom), 0f) //Ensure camera zooms into the current center
        lastZoom = camera.zoom
    }

    /** Moderates the position of the camera to prevent it from going out of boundary  */
    private fun moderateCamPos() {
        //Setting new boundaries for camera position after zooming
        val effectiveViewportWidth = (camera.viewportWidth - ui.paneWidth) * camera.zoom //Take width of pane into account
        val xDeviation = -(effectiveViewportWidth - camera.viewportWidth) / 2f
        val effectiveViewportHeight = camera.viewportHeight * camera.zoom
        val yDeviation = -(effectiveViewportHeight - camera.viewportHeight) / 2f
        val xOffset = camera.zoom * 990 //Since I shifted camera to the left by 990 px
        val leftLimit = effectiveViewportWidth / 2f - xOffset
        val rightLimit = leftLimit + 2 * xDeviation
        val downLimit = effectiveViewportHeight / 2f
        val upLimit = downLimit + 2 * yDeviation

        //Prevent camera from going out of boundary
        if (camera.position.x < leftLimit) {
            camera.position.x = leftLimit
        } else if (camera.position.x > rightLimit) {
            camera.position.x = rightLimit
        }
        if (camera.position.y < downLimit) {
            camera.position.y = downLimit
        } else if (camera.position.y > upLimit) {
            camera.position.y = upLimit
        }

        //System.out.println(camera.zoom);
        //System.out.println(camera.position.x);
        //System.out.println(camera.position.y);
    }

    /** Implements show method of screen; overridden in radarScreen class  */
    override fun show() {
        //No default implementation
    }

    /** Renders shapes for aircraft trajectory line, obstacle and restricted areas, APW, STCAS and others; overridden in radarScreen class  */
    open fun renderShape() {
        //No default implementation
    }

    /** Load radar screen range circles  */
    fun loadRange() {
        for (circle in rangeCircles) {
            //Clear any existing circles
            circle.remove()
        }
        rangeCircles.clear()
        if (rangeCircleDist == 0) return
        var i = rangeCircleDist
        while (i < 50) {
            val circle = RangeCircle(i)
            rangeCircles.add(circle)
            stage.addActor(circle)
            i += rangeCircleDist
        }
    }

    /** Renders the range circles  */
    fun renderRangeCircles() {
        for (rangeCircle in rangeCircles) {
            rangeCircle.renderShape()
        }
    }

    /** Updates timer specifically for tutorial  */
    open fun updateTutorial() {
        //No default implementation
    }

    /** Main update method, overridden in radarScreen  */
    open fun update() {
        //No default implementation
    }

    /** Main rendering method for rendering to spriteBatch  */
    override fun render(delta: Float) {
        try {
            //Clear screen
            Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT or if (Gdx.graphics.bufferFormat.coverageSampling) GL20.GL_COVERAGE_BUFFER_BIT_NV else 0)
            if (running) {
                //Test for input, update camera
                if (!loading) {
                    handleInput(delta)
                }
                camera.update()

                //Set rendering for stage camera
                game.batch.projectionMatrix = camera.combined
                shapeRenderer.projectionMatrix = camera.combined

                //Update stage
                for (i in 0 until speed) {
                    if (checkTutorialPaused()) break
                    stage.act(delta)
                    labelStage.act(delta)
                }

                //Render each of the range circles, obstacles using shaperenderer, update loop
                if (!loading) {
                    stage.viewport.apply()
                    for (i in 0 until speed) {
                        updateTutorial() //Tutorial timer is updated even if tutorial is paused
                        if (!checkTutorialPaused()) update()
                    }
                    //Update playtime counter
                    playTime += delta
                    //Render shapes only if METAR has finished loading
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
                    renderShape()
                    shapeRenderer.end()
                }

                //Draw to the spritebatch
                game.batch.projectionMatrix = labelStage.camera.combined
                val liveWeather = (this as RadarScreen).weatherSel == RadarScreen.Weather.LIVE && !this.tutorial
                if (loading) {
                    //Write loading text if loading
                    loadingTime += delta
                    loadedTime += delta
                    var loadingText = when {
                        loadedTime > 1.5 -> {
                            loadedTime = 0f
                            if (liveWeather) "Loading live weather.   " else "Loading.   "
                        }
                        loadedTime > 1 -> if (liveWeather) "Loading live weather... " else "Loading... "
                        loadedTime > 0.5 -> if (liveWeather) "Loading live weather..  " else "Loading..  "
                        else -> if (liveWeather) "Loading live weather.   " else "Loading.   "
                    }
                    loadingText += loadingPercent
                    loadingLabel.setText(loadingText)
                    loadingLabel.setPosition(1920 - loadingLabel.prefWidth / 2, 1550f)
                    game.batch.begin()
                    loadingLabel.draw(game.batch, 1f)
                    if (!tipsLoaded()) loadTips()
                    if ("" == tipLabel.text.toString()) tipLabel.setText(randomTip())
                    tipLabel.setPosition(1920 - tipLabel.prefWidth / 2, 960f)
                    tipLabel.draw(game.batch, 1f)
                    game.batch.end()
                } else {
                    stage.draw()
                    labelStage.viewport.apply()
                    labelStage.draw()

                    //Special shape rendering here so it won't be blocked by labels
                    requestFlasher.update()
                }

                //Draw the UI overlay
                uiCam.update()
                if (!loading) {
                    game.batch.projectionMatrix = uiCam.combined
                    uiStage.act()
                    uiStage.viewport.apply()
                    uiStage.draw()
                }
            }
            if (tutorialQuit) {
                TerminalControl.radarScreen = null
                game.screen = MainMenuScreen(game, null)
            }
        } catch (e: Exception) {
            ErrorHandler.sendGenericError(e, true)
        }
    }

    /** Check if tutorial is in paused state  */
    private fun checkTutorialPaused(): Boolean {
        return (this as RadarScreen).tutorialManager?.isPausedForReading ?: false
    }

    /** Implements resize method of screen, adjusts camera & viewport properties after resize for better UI  */
    override fun resize(width: Int, height: Int) {
        TerminalControl.WIDTH = width
        TerminalControl.HEIGHT = height
        ui.updatePaneWidth()
        viewport.update(width, height, false)
        stage.viewport.update(width, height, false)
        labelStage.viewport.update(width, height, false)
        val xOffset = camera.zoom * 990
        camera.position[camera.viewportWidth / 2 - xOffset, camera.viewportHeight / 2] = 0f
        uiViewport.update(width, height, true)
        uiStage.viewport.update(width, height, true)
        uiCam.position[uiCam.viewportWidth / 2f, uiCam.viewportHeight / 2f] = 0f
        if (Gdx.app.type == Application.ApplicationType.Desktop) {
            var resizeAgain = false
            var newWidth = width
            var newHeight = height
            if (newWidth < 960) {
                newWidth = 960
                resizeAgain = true
            }
            if (newHeight < 540) {
                newHeight = 540
                resizeAgain = true
            }
            if (resizeAgain) {
                resize(newWidth, newHeight)
            }
        }
    }

    /** Implements pause method of screen  */
    override fun pause() {
        setGameRunning(false)
    }

    /** Implements resume method of screen  */
    override fun resume() {
        setGameRunning(true)
    }

    /** Implements hide method of screen  */
    override fun hide() {
        //Not disposing unless game is quit specifically
    }

    /** Implements dispose method of screen, disposes resources after they're no longer needed  */
    override fun dispose() {
        stage.clear()
        stage.dispose()
        labelStage.clear()
        labelStage.dispose()
        uiStage.clear()
        uiStage.dispose()
        shapeRenderer.dispose()
        aircrafts.clear()
        airports.clear()
        waypoints.clear()
        soundManager.dispose()
    }

    /** Implements touchdown method of gestureListener  */
    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    /** Implements tap method of gestureListener, tests for tap and double tap  */
    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        TerminalControl.radarScreen?.setSelectedAircraft(null)
        if (count == 2 && !loading) {
            //Gdx.app.postRunnable(() -> obsArray = FileLoader.loadObstacles()); Reload obstacles - Debug use only
            zooming = true
            return true
        }
        //Shows approximate position of mouse pointer click in game world - Debug use only
        //Vector3 vector3 = new Vector3(x, y, 0);
        //Vector3 vector3_new = stage.getCamera().unproject(vector3);
        //Gdx.app.log("Coordinates", vector3_new.toString());
        return false
    }

    /** Implements longPress method of gestureListener  */
    override fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    /** Implements fling method of gestureListener  */
    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        return false
    }

    /** Implements pan method of gestureListener, tests for panning to shift radar screen around  */
    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        if (loading) {
            return false
        }
        val screenHeightRatio = 3240f / Gdx.graphics.height
        val screenWidthRatio = 5760f / Gdx.graphics.width
        val screenRatio = screenHeightRatio.coerceAtLeast(screenWidthRatio)
        camera.translate(-deltaX * camera.zoom * screenRatio, deltaY * camera.zoom * screenRatio)
        return false
    }

    /** Implements panStop method of gestureListener  */
    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    /** Implements zoom method of gestureListener, tests for pinch zooming to adjust radar screen zoom level  */
    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        if (loading) {
            return true
        }
        if (initialDistance != lastInitialDist) {
            //New zoom
            lastInitialDist = initialDistance
            lastScale = camera.zoom
        }
        val ratio = lastInitialDist / distance
        camera.zoom = lastScale * ratio
        camera.update()
        return true
    }

    /** Implements pinch method of gestureListener  */
    override fun pinch(initialPointer1: Vector2, initialPointer2: Vector2, pointer1: Vector2, pointer2: Vector2): Boolean {
        return false
    }

    /** Implements pinchStop method of gestureListener  */
    override fun pinchStop() {
        //No default implementation
    }

    /** Implements keyDown method of inputListener  */
    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    /** Implements keyUp method of inputListener  */
    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    /** Implements keyTyped method of inputListener  */
    override fun keyTyped(character: Char): Boolean {
        return false
    }

    /** Implements touchdown method of inputListener  */
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    /** Implements touchUp method of inputListener  */
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    /** Implements touchDragged method of inputListener  */
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    /** Implements mouseMoved method of inputListener  */
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    /** Implements scrolled method of inputListener, tests for scrolling to adjust radar screen zoom levels  */
    override fun scrolled(amount: Int): Boolean {
        if (!loading) {
            camera.zoom += amount * 0.042f
        }
        return true
    }

    /** Sets whether game is running  */
    fun setGameRunning(running: Boolean) {
        this.running = running
        if (this.running) {
            val prevCamX = camera.position.x
            val prevCamY = camera.position.y
            if (game.screen !== this) game.screen = this
            Gdx.input.inputProcessor = inputMultiplexer
            camera.position.x = prevCamX
            camera.position.y = prevCamY
            soundManager.resume()
            DataTag.startTimers()
            DataTag.setBorderBackground()
        } else {
            soundManager.pause()
            DataTag.pauseTimers()
            game.screen = PauseScreen(game, this as RadarScreen)
        }
    }

    fun setTutorialQuit(tutorialQuit: Boolean) {
        this.tutorialQuit = tutorialQuit
    }
}