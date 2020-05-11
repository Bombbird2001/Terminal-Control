package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager;
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency;
import com.bombbird.terminalcontrol.screens.BasicScreen;
import com.bombbird.terminalcontrol.screens.gamescreen.GameScreen;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

public class SettingsScreen extends SettingsTemplateScreen {
    public int xOffset;
    public int yOffset;

    public SelectBox.SelectBoxStyle selectBoxStyle;
    public Label.LabelStyle labelStyle;

    public Array<SettingsTab> settingsTabs;
    public int tab;

    public SettingsScreen(final TerminalControl game) {
        super(game, null, null);
    }

    /** Loads selectBox for settings */
    public void loadBoxes() {

    }

    /** Creates a default selectBox configuration with standard styles */
    public SelectBox<String> createStandardSelectBox() {
        SelectBox<String> box = new SelectBox<>(selectBoxStyle);
        box.setSize(1200, 300);
        box.setAlignment(Align.center);
        box.getList().setAlignment(Align.center);

        return box;
    }

    /** Loads buttons */
    public void loadButton() {
        //Adds buttons by default, position, function depends on type of settings screen
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        cancelButton = new TextButton("Cancel", textButtonStyle);
        cancelButton.setSize(1200, 300);
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800);
        stage.addActor(cancelButton);

        confirmButton = new TextButton("Confirm", textButtonStyle);
        confirmButton.setSize(1200, 300);
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800);
        stage.addActor(confirmButton);

        backButton = new TextButton("<", textButtonStyle);
        backButton.setSize(400, 400);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab <= 0) return;
                tab--;
                updateTabs(true);
            }
        });
        stage.addActor(backButton);

        nextButton = new TextButton(">", textButtonStyle);
        nextButton.setSize(400, 400);
        nextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab >= settingsTabs.size - 1) return;
                tab++;
                updateTabs(true);
            }
        });
        stage.addActor(nextButton);
    }

    /** Loads labels */
    public void loadLabel() {
    }

    /** Loads the various actors into respective tabs, overriden in respective classes */
    public void loadTabs() {
        //No default implementation
    }

    /** Sets relevant options into select boxes */
    public void setOptions() {

    }

    /** Confirms and applies the changes set */
    public void sendChanges() {
        GameSaver.saveSettings();
    }

    /** Overrides show method of BasicScreen */
    @Override
    public void show() {
        if (Fonts.defaultFont6 == null) {
            //Regenerate fonts that were disposed
            Fonts.generateAllFonts();
        }
    }

    /** Overrides dispose method of BasicScreen */
    @Override
    public void dispose() {
        super.dispose();
    }
}
