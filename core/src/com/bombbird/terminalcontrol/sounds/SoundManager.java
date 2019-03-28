package com.bombbird.terminalcontrol.sounds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.bombbird.terminalcontrol.TerminalControl;

public class SoundManager {
    private Sound conflict;
    private Sound runwayChange;

    private boolean conflictPlaying;
    private boolean runwayChangePlaying;

    private Timer timer;

    public SoundManager() {
        conflict = Gdx.audio.newSound(Gdx.files.internal("game/audio/conflict.wav"));
        runwayChange = Gdx.audio.newSound(Gdx.files.internal("game/audio/rwy_change.wav"));
        timer = new Timer();
    }

    /** Loops the conflict audio effect */
    public void playConflict() {
        if (!conflictPlaying && TerminalControl.radarScreen.soundSel >= 1) {
            conflict.play(0.8f);
            conflictPlaying = true;
            timer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    conflictPlaying = false;
                }
            }, 2.07f);
        }
    }

    /** Plays a runway change sound effect if not playing */
    public void playRunwayChange() {
        if (!runwayChangePlaying && TerminalControl.radarScreen.soundSel >= 1) {
            runwayChange.play(0.8f);
            runwayChangePlaying = true;
            timer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    runwayChangePlaying = false;
                }
            }, 2.0f);
        }
    }

    /** Pauses playing all sounds */
    public void pause() {
        conflict.pause();
        runwayChange.pause();
        timer.stop();
    }

    /** Resumes playing all sounds */
    public void resume() {
        conflict.resume();
        runwayChange.resume();
        timer.start();
    }

    /** Stops, disposes all sounds */
    public void dispose() {
        conflict.stop();
        conflict.dispose();
        runwayChange.stop();
        runwayChange.dispose();
    }
}
