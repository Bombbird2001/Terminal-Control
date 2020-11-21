package com.bombbird.terminalcontrol.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.SafeStage

open class BasicScreen(val game: TerminalControl, width: Int, height: Int) : Screen {
    val stage: SafeStage
    val camera: OrthographicCamera = OrthographicCamera()
    private val viewport: Viewport

    init {
        //Set camera params
        camera.setToOrtho(false, width.toFloat(), height.toFloat())
        viewport = FitViewport(TerminalControl.WIDTH.toFloat(), TerminalControl.HEIGHT.toFloat(), camera)
        viewport.apply()

        //Set stage params
        stage = SafeStage(FitViewport(width.toFloat(), height.toFloat()), game.batch)
        stage.viewport.update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true)
        Gdx.input.inputProcessor = stage
    }

    /** Implements show method of screen, should be overridden by individual classes  */
    override fun show() {
        //No default implementation
    }

    /** Main rendering method for rendering to spriteBatch  */
    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT or if (Gdx.graphics.bufferFormat.coverageSampling) GL20.GL_COVERAGE_BUFFER_BIT_NV else 0)
        camera.update()
        game.batch.projectionMatrix = camera.combined
        stage.act(delta)
        stage.draw()
    }

    /** Implements resize method of screen, adjusts camera & viewport properties after resize for better UI  */
    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        stage.viewport.update(width, height, true)
        camera.position[camera.viewportWidth / 2, camera.viewportHeight / 2] = 0f
    }

    /** Implements pause method of screen  */
    override fun pause() {
        //No default implementation
    }

    /** Implements resume method of screen  */
    override fun resume() {
        //No default implementation
    }

    /** Implements hide method of screen  */
    override fun hide() {
        dispose()
    }

    /** Implements dispose method of screen  */
    override fun dispose() {
        stage.clear()
        stage.dispose()
    }
}