package com.bombbird.terminalcontrol.sounds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import com.bombbird.terminalcontrol.TerminalControl;

public class SoundManager {
    private final Sound conflict;
    private final Sound runwayChange;
    private final Sound initialContact;
    private final Sound alert;

    private boolean conflictPlaying;
    private boolean runwayChangePlaying;
    private boolean initialContactPlaying;
    private boolean alertPlaying;

    private final Timer timer;

    public SoundManager() {
        conflict = Gdx.audio.newSound(Gdx.files.internal("game/audio/conflict.wav"));
        runwayChange = Gdx.audio.newSound(Gdx.files.internal("game/audio/rwy_change.wav"));
        initialContact = Gdx.audio.newSound(Gdx.files.internal("game/audio/initial_contact.wav"));
        alert = Gdx.audio.newSound(Gdx.files.internal("game/audio/alert.wav"));
        timer = new Timer();
    }

    /** Loops the conflict audio effect */
    public void playConflict() {
        if (!conflictPlaying && TerminalControl.radarScreen.getSoundSel() >= 1) {
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
        if (!runwayChangePlaying && TerminalControl.radarScreen.getSoundSel() >= 1) {
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

    /** Plays initial contact sound effect if not playing */
    public void playInitialContact() {
        //Play only if not playing AND sound selected is sound effects only
        if (!initialContactPlaying && TerminalControl.radarScreen.getSoundSel() == 1) {
            if (alertPlaying) {
                //If alert playing, wait till alert has finished then play this again
                timer.scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        playInitialContact();
                    }
                }, 0.45f);
            } else {
                initialContact.play(0.8f);
                initialContactPlaying = true;
                timer.scheduleTask(new Timer.Task() {
                    @Override
                    public void run() {
                        initialContactPlaying = false;
                    }
                }, 0.23f);
            }
        }
    }

    /** Plays alert sound effect if not playing */
    public void playAlert() {
        //Play only if not playing AND sound selected is sound effects only
        if (!alertPlaying && TerminalControl.radarScreen.getSoundSel() == 1) {
            alert.play(0.8f);
            alertPlaying = true;
            timer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    alertPlaying = false;
                }
            }, 0.45f);
        }
    }

    /** Pauses playing all sounds */
    public void pause() {
        conflict.pause();
        runwayChange.pause();
        initialContact.pause();
        alert.pause();
        timer.stop();
    }

    /** Resumes playing all sounds */
    public void resume() {
        conflict.resume();
        runwayChange.resume();
        initialContact.resume();
        alert.resume();
        timer.start();
    }

    /** Stops, disposes all sounds */
    public void dispose() {
        conflict.stop();
        conflict.dispose();
        runwayChange.stop();
        runwayChange.dispose();
        initialContact.stop();
        initialContact.dispose();
        alert.stop();
        alert.dispose();
    }
}
