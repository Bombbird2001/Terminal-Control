package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.BasicScreen;
import com.bombbird.terminalcontrol.screens.PauseScreen;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class SettingsTemplateScreen extends BasicScreen {
    //Background image
    public Image background;
    public static final float specialScale = 7.8f;

    public int xOffset;
    public int yOffset;

    //Default buttons
    public TextButton confirmButton;
    public TextButton cancelButton;
    public TextButton backButton;
    public TextButton nextButton;

    //Tabs
    public Array<SettingsTab> settingsTabs;
    public int tab;

    //Styles
    public static SelectBox.SelectBoxStyle selectBoxStyle;
    public Label.LabelStyle labelStyle;

    //Default game settings or in game settings
    public RadarScreen radarScreen;

    //Info string label
    public String infoString;

    //Class name of screen
    public String className;

    public SettingsTemplateScreen(TerminalControl game, RadarScreen radarScreen, Image background) {
        super(game, 5760, 3240);

        this.radarScreen = radarScreen;

        this.background = background;
        if (this.background != null) {
            this.background.scaleBy(specialScale);
            stage.addActor(this.background);
        }

        settingsTabs = new Array<>();
        tab = 0;

        className = getClass().getSimpleName();
    }

    public void loadUI(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;

        loadStyles();

        loadBoxes();

        loadButton();

        loadLabel();

        loadTabs();

        updateTabs(true);
    }

    /** Loads the styles for the selectBox */
    public void loadStyles() {
        ScrollPane.ScrollPaneStyle paneStyle = new ScrollPane.ScrollPaneStyle();
        paneStyle.background = TerminalControl.skin.getDrawable("ListBackground");

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = Fonts.defaultFont20;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.BLACK;
        Drawable button_down = TerminalControl.skin.getDrawable("Button_down");
        button_down.setTopHeight(75);
        button_down.setBottomHeight(75);
        listStyle.selection = button_down;

        selectBoxStyle = new SelectBox.SelectBoxStyle();
        selectBoxStyle.font = Fonts.defaultFont20;
        selectBoxStyle.fontColor = Color.WHITE;
        selectBoxStyle.listStyle = listStyle;
        selectBoxStyle.scrollStyle = paneStyle;
        selectBoxStyle.background = TerminalControl.skin.getDrawable("Button_up");
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
        backButton.setPosition(5760 / 2f - 2500, 3240 - 2800);
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
        nextButton.setPosition(5760 / 2f + 2000, 3240 - 2800);
        nextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab >= settingsTabs.size - 1) return;
                tab++;
                updateTabs(true);
            }
        });
        stage.addActor(nextButton);

        setButtonListeners();
    }

    /** Sets the default listeners for the cancel, confirm buttons, overridden in global settings to go to different screen */
    public void setButtonListeners() {
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (radarScreen != null) {
                    if (radarScreen.tutorial) {
                        game.setScreen(new PauseScreen(game, radarScreen));
                    } else {
                        game.setScreen(new CategorySelectScreen(game, null, radarScreen));
                    }
                } else {
                    background.scaleBy(-specialScale);
                    game.setScreen(new CategorySelectScreen(game, background, null));
                }
            }
        });

        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendChanges();
                if (radarScreen != null) {
                    if (radarScreen.tutorial) {
                        game.setScreen(new PauseScreen(game, radarScreen));
                    } else {
                        game.setScreen(new CategorySelectScreen(game, null, radarScreen));
                    }
                } else {
                    background.scaleBy(-specialScale);
                    game.setScreen(new CategorySelectScreen(game, background, null));
                }
            }
        });


    }

    /** Changes the tab displayed */
    public void updateTabs(boolean screenActive) {
        backButton.setVisible(tab > 0);
        nextButton.setVisible(tab < settingsTabs.size - 1);
        if (tab > settingsTabs.size - 1 || tab < 0) {
            Gdx.app.log("SettingsTemplateScreen", "Invalid tab set: " + tab + ", size is " + settingsTabs.size);
            return;
        }
        for (int i = 0; i < settingsTabs.size; i++) {
            if (i == tab) {
                settingsTabs.get(i).updateVisibility(screenActive);
            } else {
                settingsTabs.get(i).updateVisibility(false);
            }
        }
    }

    /** Creates a default selectBox configuration with standard styles */
    public SelectBox<String> createStandardSelectBox() {
        SelectBox<String> box = new SelectBox<>(selectBoxStyle);
        box.setSize(1200, 300);
        box.setAlignment(Align.center);
        box.getList().setAlignment(Align.center);

        return box;
    }

    /** Updates the relevant settings */
    public void sendChanges() {
        //No default implementation
    }

    /** Loads selectBox for settings, overridden in respective classes */
    public void loadBoxes() {
        //No default implementation
    }

    /** Loads the selectBox labels, overridden in respective classes */
    public void loadLabel() {
        labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        if (radarScreen == null && !(this instanceof GlobalSettingsScreen)) infoString += " These are default settings, you can still change them for individual games.";
        Label infoLabel = new Label(infoString, labelStyle);
        infoLabel.setPosition(5760 / 2f - infoLabel.getWidth() / 2f, 3240 - 300);
        stage.addActor(infoLabel);
    }

    /** Loads the various actors into respective tabs, overridden in respective classes */
    public void loadTabs() {
        //No default implementation
    }

    /** Sets the current options into selectBoxes */
    public void setOptions() {
        //No default implementation
    }

    /** Overrides render method to include detection of back button on android */
    @Override
    public void render(float delta) {
        super.render(delta);
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            //On android, emulate cancelButton is pressed
            cancelButton.toggle();
        }
    }
}
