package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.screens.gamescreen.GameScreen;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;

import java.util.Locale;

public class GameSettingsScreen extends SettingsScreen {
    private final RadarScreen radarScreen;

    public GameSettingsScreen(final TerminalControl game, final RadarScreen radarScreen) {
        super(game);

        this.radarScreen = radarScreen;

        loadUI(-1200, 0);
    }

    @Override
    public void loadButton() {
        super.loadButton();
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                radarScreen.setGameState(GameScreen.State.PAUSE);
            }
        });

        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendChanges();
                radarScreen.setGameState(GameScreen.State.PAUSE);
            }
        });

        backButton.setPosition(5760 / 2f - 2400, 3240 - 2800);
        nextButton.setPosition(5760 / 2f + 1900, 3240 - 2800);
    }

    @Override
    public void loadBoxes() {
        super.loadBoxes();
    }

    @Override
    public void loadLabel() {
        super.loadLabel();
    }

    @Override
    public void loadTabs() {

    }

    /** Confirms and applies the changes set */
    @Override
    public void sendChanges() {

    }

    /** Gets radarScreen settings before setting options */
    @Override
    public void setOptions() {

        if (radarScreen.tutorial) {

            return;
        }

        super.setOptions();
    }

    /** Sets visibility of elements */
    public void setVisible(boolean show) {
        updateTabs(show);

        confirmButton.setVisible(show);
        cancelButton.setVisible(show);
    }
}
