package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.CategorySelectScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.OtherSettingsScreen;
import com.bombbird.terminalcontrol.screens.upgradescreen.AchievementScreen;
import com.bombbird.terminalcontrol.screens.upgradescreen.UpgradeScreen;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class PauseScreen extends BasicScreen {
    private final RadarScreen radarScreen;

    public PauseScreen(TerminalControl game, RadarScreen radarScreen) {
        super(game, 5760, 3240);
        this.radarScreen = radarScreen;

        camera.position.set(2880, 1620, 0);

        loadButtons();
        loadLabels();
    }

    /** Loads the buttons for screen */
    private void loadButtons() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        TextButton resumeButton = new TextButton("Resume", textButtonStyle);
        resumeButton.setSize(1200, 300);
        resumeButton.setPosition((5760 - 1200) / 2f, 3240 - 1200);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Un-pause the game
                radarScreen.setGameRunning(true);
                event.handle();
            }
        });
        stage.addActor(resumeButton);

        TextButton settingsButton = new TextButton("Settings", textButtonStyle);
        settingsButton.setSize(1200, 300);
        settingsButton.setPosition((5760 - 1200) / 2f, 3240 - 1600);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Change to settings state
                if (radarScreen.tutorial) {
                    game.setScreen(new OtherSettingsScreen(game, radarScreen, null));
                } else {
                    game.setScreen(new CategorySelectScreen(game, null, radarScreen));
                }
            }
        });
        stage.addActor(settingsButton);

        TextButton quitButton = new TextButton("Quit", textButtonStyle);
        quitButton.setSize(1200, 300);
        quitButton.setPosition((5760 - 1200) / 2f, 3240 - 2000);
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu screen
                radarScreen.getMetar().setQuit(true);
                if (!radarScreen.tutorial) GameSaver.saveGame(); //Save the game first
                TerminalControl.radarScreen = null;
                radarScreen.game.setScreen(new MainMenuScreen(radarScreen.game, null));
                radarScreen.dispose();
            }
        });
        stage.addActor(quitButton);

        int BUTTON_WIDTH_SMALL = MainMenuScreen.BUTTON_WIDTH_SMALL * 2;
        int BUTTON_HEIGHT_SMALL = MainMenuScreen.BUTTON_HEIGHT_SMALL * 2;

        //Set upgrade button (for full version only)
        if (TerminalControl.full) {
            ImageButton.ImageButtonStyle imageButtonStyle3 = new ImageButton.ImageButtonStyle();
            imageButtonStyle3.imageUp = TerminalControl.skin.getDrawable("Upgrade_up");
            imageButtonStyle3.imageUp.setMinWidth(BUTTON_WIDTH_SMALL);
            imageButtonStyle3.imageUp.setMinHeight(BUTTON_HEIGHT_SMALL);
            imageButtonStyle3.imageDown = TerminalControl.skin.getDrawable("Upgrade_down");
            imageButtonStyle3.imageDown.setMinWidth(BUTTON_WIDTH_SMALL);
            imageButtonStyle3.imageDown.setMinHeight(BUTTON_HEIGHT_SMALL);
            ImageButton upgradeButton = new ImageButton(imageButtonStyle3);
            upgradeButton.setPosition(5760 - BUTTON_WIDTH_SMALL, 1790 + 50);
            upgradeButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL);
            upgradeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    //Go to upgrade screen
                    game.setScreen(new UpgradeScreen(game, null));
                }
            });
            stage.addActor(upgradeButton);
        }

        //Set achievement button
        ImageButton.ImageButtonStyle imageButtonStyle4 = new ImageButton.ImageButtonStyle();
        imageButtonStyle4.imageUp = TerminalControl.skin.getDrawable("Medal_up");
        imageButtonStyle4.imageUp.setMinWidth(BUTTON_WIDTH_SMALL);
        imageButtonStyle4.imageUp.setMinHeight(BUTTON_HEIGHT_SMALL);
        imageButtonStyle4.imageDown = TerminalControl.skin.getDrawable("Medal_down");
        imageButtonStyle4.imageDown.setMinWidth(BUTTON_WIDTH_SMALL);
        imageButtonStyle4.imageDown.setMinHeight(BUTTON_HEIGHT_SMALL);
        ImageButton achievementButton = new ImageButton(imageButtonStyle4);
        achievementButton.setPosition(5760 - BUTTON_WIDTH_SMALL, 1790 - 50 - BUTTON_HEIGHT_SMALL);
        achievementButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL);
        achievementButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go to upgrade screen
                game.setScreen(new AchievementScreen(game, null));
            }
        });
        stage.addActor(achievementButton);
    }

    /** Loads the stats label on left side of screen */
    private void loadLabels() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont24;
        labelStyle.fontColor = Color.WHITE;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(radarScreen.getArrivals()).append(" arrivals in control\n");
        stringBuilder.append(radarScreen.getDepartures()).append(" departures in control\n");
        stringBuilder.append(radarScreen.getSeparationIncidents()).append(" total separation incidents\n");
        stringBuilder.append((int) radarScreen.getWakeInfringeTime()).append("s of wake separation infringement\n");
        int totalSecs = (int) radarScreen.getPlayTime();
        int hrs = totalSecs / 3600;
        int mins = (totalSecs % 3600) / 60;
        String minStr = mins < 10 ? "0" + mins : Integer.toString(mins);
        int sec = totalSecs % 60;
        String secStr = sec < 10 ? "0" + sec : Integer.toString(sec);
        stringBuilder.append(hrs).append(":").append(minStr).append(":").append(secStr).append(" of total play time");
        Label statsLabel = new Label(stringBuilder.toString(), labelStyle);
        statsLabel.setPosition(100, 1790 - statsLabel.getHeight() / 2);
        stage.addActor(statsLabel);
    }
}
