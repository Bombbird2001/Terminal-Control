package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;

public class DefaultSettingsScreen extends SettingsScreen {
    private Image background;
    private static float specialScale = 7.8f;

    private boolean sendCrash;
    private CheckBox sendCrashBox;

    public DefaultSettingsScreen(final TerminalControl game, Image background) {
        super(game);

        this.background = background;
        this.background.scaleBy(specialScale);

        trajectorySel = TerminalControl.trajectorySel;
        weatherSel = TerminalControl.weatherSel;
        soundSel = TerminalControl.soundSel;
        sendCrash = TerminalControl.sendAnonCrash;
        emerChance = TerminalControl.emerChance;

        loadUI(-600, -200);

        setOptions();
    }

    @Override
    public void loadUI(int xOffset, int yOffset) {
        stage.addActor(background);
        super.loadUI(xOffset, yOffset);
    }

    @Override
    public void loadButton() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        cancelButton = new TextButton("Cancel", textButtonStyle);
        cancelButton.setSize(1200, 300);
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Back to main menu
                background.scaleBy(-specialScale);
                game.setScreen(new MainMenuScreen(game, background));
                dispose();
            }
        });
        stage.addActor(cancelButton);

        confirmButton = new TextButton("Confirm", textButtonStyle);
        confirmButton.setSize(1200, 300);
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendChanges();
                background.scaleBy(-specialScale);
                game.setScreen(new MainMenuScreen(game, background));
                dispose();
            }
        });
        stage.addActor(confirmButton);
    }

    @Override
    public void loadBoxes(int xOffset, int yOffset) {
        super.loadBoxes(xOffset, yOffset);
        CheckBox.CheckBoxStyle checkBoxStyle = new CheckBox.CheckBoxStyle();
        checkBoxStyle.checkboxOn = TerminalControl.skin.getDrawable("Checked");
        checkBoxStyle.checkboxOff = TerminalControl.skin.getDrawable("Unchecked");
        checkBoxStyle.font = Fonts.defaultFont20;
        checkBoxStyle.fontColor = Color.WHITE;

        sendCrashBox = new CheckBox(" Send anonymous crash reports", checkBoxStyle);
        sendCrashBox.setPosition(5760 / 2f + 1600 + xOffset, 3240 * 0.7f + yOffset);
        sendCrashBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sendCrash = sendCrashBox.isChecked();
            }
        });
        stage.addActor(sendCrashBox);
    }

    @Override
    public void loadLabel() {
        super.loadLabel();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        Label infoLabel = new Label("Set the default game settings below. You can still change these settings for individual games.", labelStyle);
        infoLabel.setPosition(5760 / 2f - infoLabel.getWidth() / 2f, 3240 - 300);
        stage.addActor(infoLabel);

        Label sendLabel = new Label("Sending anonymous crash reports will allow\nus to improve your game experience.\nNo personal or device information will be\nsent.", labelStyle);
        sendLabel.setPosition(sendCrashBox.getX(), sendCrashBox.getY() - 475);
        stage.addActor(sendLabel);
    }

    @Override
    public void setOptions() {
        super.setOptions();
        sendCrashBox.setChecked(TerminalControl.sendAnonCrash);
    }

    @Override
    public void sendChanges() {
        TerminalControl.trajectorySel = trajectorySel;
        TerminalControl.weatherSel = weatherSel;
        TerminalControl.soundSel = soundSel;
        TerminalControl.sendAnonCrash = sendCrash;
        TerminalControl.emerChance = emerChance;

        GameSaver.saveSettings(trajectorySel, weatherSel, soundSel, sendCrash, emerChance);
    }
}
