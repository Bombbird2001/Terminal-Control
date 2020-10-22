package com.bombbird.terminalcontrol.sounds

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Timer
import com.bombbird.terminalcontrol.TerminalControl

class SoundManager {
    private val radarScreen = TerminalControl.radarScreen!!
    private val conflict = Gdx.audio.newSound(Gdx.files.internal("game/audio/conflict.wav"))
    private val runwayChange = Gdx.audio.newSound(Gdx.files.internal("game/audio/rwy_change.wav"))
    private val initialContact = Gdx.audio.newSound(Gdx.files.internal("game/audio/initial_contact.wav"))
    private val alert = Gdx.audio.newSound(Gdx.files.internal("game/audio/alert.wav"))
    private var conflictPlaying = false
    private var runwayChangePlaying = false
    private var initialContactPlaying = false
    private var alertPlaying = false
    private val timer = Timer()

    /** Loops the conflict audio effect  */
    fun playConflict() {
        if (!conflictPlaying && radarScreen.soundSel >= 1) {
            conflict.play(0.8f)
            conflictPlaying = true
            timer.scheduleTask(object : Timer.Task() {
                override fun run() {
                    conflictPlaying = false
                }
            }, 2.07f)
        }
    }

    /** Plays a runway change sound effect if not playing  */
    fun playRunwayChange() {
        if (!runwayChangePlaying && radarScreen.soundSel >= 1) {
            runwayChange.play(0.8f)
            runwayChangePlaying = true
            timer.scheduleTask(object : Timer.Task() {
                override fun run() {
                    runwayChangePlaying = false
                }
            }, 2.0f)
        }
    }

    /** Plays initial contact sound effect if not playing  */
    fun playInitialContact() {
        //Play only if not playing AND sound selected is sound effects only
        if (!initialContactPlaying && radarScreen.soundSel == 1) {
            if (alertPlaying) {
                //If alert playing, wait till alert has finished then play this again
                timer.scheduleTask(object : Timer.Task() {
                    override fun run() {
                        playInitialContact()
                    }
                }, 0.45f)
            } else {
                initialContact.play(0.8f)
                initialContactPlaying = true
                timer.scheduleTask(object : Timer.Task() {
                    override fun run() {
                        initialContactPlaying = false
                    }
                }, 0.23f)
            }
        }
    }

    /** Plays alert sound effect if not playing  */
    fun playAlert() {
        //Play only if not playing AND sound selected is sound effects only
        if (!alertPlaying && radarScreen.soundSel == 1) {
            alert.play(0.8f)
            alertPlaying = true
            timer.scheduleTask(object : Timer.Task() {
                override fun run() {
                    alertPlaying = false
                }
            }, 0.45f)
        }
    }

    /** Pauses playing all sounds  */
    fun pause() {
        conflict.pause()
        runwayChange.pause()
        initialContact.pause()
        alert.pause()
        timer.stop()
    }

    /** Resumes playing all sounds  */
    fun resume() {
        conflict.resume()
        runwayChange.resume()
        initialContact.resume()
        alert.resume()
        timer.start()
    }

    /** Stops, disposes all sounds  */
    fun dispose() {
        conflict.stop()
        conflict.dispose()
        runwayChange.stop()
        runwayChange.dispose()
        initialContact.stop()
        initialContact.dispose()
        alert.stop()
        alert.dispose()
    }
}