package com.bombbird.terminalcontrol.entities.weather

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.bombbird.terminalcontrol.TerminalControl

class StormIntensity(var intensity: Int, val x: Int, val y: Int, private val cell: ThunderCell) {
    companion object {
        var LOADED_ICONS = false
        lateinit var STORM_BLUE: NinePatch
        lateinit var STORM_LIME: NinePatch
        lateinit var STORM_YELLOW: NinePatch
        lateinit var STORM_ORANGE: NinePatch
        lateinit var STORM_RED: NinePatch

        /** Loads label, icon resources  */
        private fun loadResources() {
            if (!LOADED_ICONS) {
                STORM_BLUE = NinePatch(TerminalControl.skin.getPatch("StormBlue"))
                STORM_LIME = NinePatch(TerminalControl.skin.getPatch("StormLime"))
                STORM_YELLOW = NinePatch(TerminalControl.skin.getPatch("StormYellow"))
                STORM_ORANGE = NinePatch(TerminalControl.skin.getPatch("StormOrange"))
                STORM_RED = NinePatch(TerminalControl.skin.getPatch("StormRed"))
                LOADED_ICONS = true
            }
        }
    }

    private var intensityPatch: Image? = null

    init {
        loadResources()
        updateIntensity(intensity)
    }

    fun updateIntensity(newIntensity: Int) {
        if (intensityPatch == null || (intensity + 1) / 2 != (newIntensity + 1) / 2) {
            //Image needs updating
            intensityPatch = when (newIntensity) {
                0 -> null
                1, 2 -> Image(STORM_BLUE)
                3, 4 -> Image(STORM_LIME)
                5, 6 -> Image(STORM_YELLOW)
                7, 8 -> Image(STORM_ORANGE)
                9, 10 -> Image(STORM_RED)
                else -> {
                    Gdx.app.log("Storm Intensity", "Invalid storm intensity $newIntensity")
                    null
                }
            }
            intensityPatch?.setSize(10f, 10f)
        }
        intensity = newIntensity
    }

    fun draw() {
        val radarScreen = TerminalControl.radarScreen ?: return
        intensityPatch?.setPosition(cell.centreX + x * 10, cell.centreY + y * 10)
        intensityPatch?.draw(radarScreen.game.batch, 1f)
    }
}