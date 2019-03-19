package com.bombbird.terminalcontrol.sounds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    private Sound conflict;
    private Sound runwayChange;

    private boolean conflictPlaying;

    public SoundManager() {
        conflict = Gdx.audio.newSound(Gdx.files.internal("game/audio/conflict.wav"));
    }

    /** Loops the conflict audio effect */
    public void playConflict() {
        if (!conflictPlaying) {
            long conflictId = conflict.play(1.0f);
            conflict.setLooping(conflictId, true);
            conflictPlaying = true;
        }
    }

    /** Stops playing conflict audio effect */
    public void stopConflict() {
        conflict.stop();
        conflictPlaying = false;
    }

    /** Pauses playing all sounds */
    public void pause() {
        conflict.pause();
    }

    /** Resumes playing all sounds */
    public void resume() {
        conflict.resume();
    }

    /** Stops, disposes all sounds */
    public void dispose() {
        conflict.stop();
        conflict.dispose();
    }
}
