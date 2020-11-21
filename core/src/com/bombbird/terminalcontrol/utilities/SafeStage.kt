package com.bombbird.terminalcontrol.utilities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.Viewport

class SafeStage(viewport: Viewport, batch: SpriteBatch): Stage(viewport, batch) {

    /** Overrides addActor method to ensure thread safety when adding actors in other threads */
    override fun addActor(actor: Actor?) {
        if (Thread.currentThread().name == "main") {
            super.addActor(actor)
        } else Gdx.app.postRunnable { super.addActor(actor) }
    }
}