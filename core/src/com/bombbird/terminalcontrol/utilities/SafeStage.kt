package com.bombbird.terminalcontrol.utilities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport

class SafeStage(viewport: Viewport, batch: SpriteBatch): Stage(viewport, batch) {

    /** Overrides addActor method to ensure thread safety when adding actors in other threads */
    override fun addActor(actor: Actor?) {
        if (isOnMainThread()) {
            super.addActor(actor)
        } else Gdx.app.postRunnable { super.addActor(actor) }
    }

    /** Overrides addActor method to ensure thread safety when drawing */
    override fun draw() {
        if (isOnMainThread()) {
            super.draw()
        } else Gdx.app.postRunnable { super.draw() }
    }

    /** Checks whether code is running on main thread */
    private fun isOnMainThread(): Boolean {
        return Thread.currentThread().name == "main" || Thread.currentThread().name.contains("GLThread")
    }
}