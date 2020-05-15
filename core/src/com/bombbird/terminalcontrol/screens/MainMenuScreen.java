package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.selectgamescreen.HelpScreen;
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen;
import com.bombbird.terminalcontrol.screens.selectgamescreen.NewGameScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.MenuSettingsScreen;
import com.bombbird.terminalcontrol.screens.upgradescreen.AchievementScreen;
import com.bombbird.terminalcontrol.screens.upgradescreen.UpgradeScreen;
import com.bombbird.terminalcontrol.ui.Ui;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;

public class MainMenuScreen extends BasicScreen {
    //Background image
    private Image background;

    //Button constants
    public static final int BUTTON_WIDTH = 1000;
    public static final int BUTTON_HEIGHT = 200;
    public static final int BUTTON_WIDTH_SMALL = 200;
    public static final int BUTTON_HEIGHT_SMALL = 200;

    public MainMenuScreen(final TerminalControl game, Image background) {
        super(game, 2880, 1620);

        this.background = background;

        TerminalControl.loadVersionInfo();
        TerminalControl.loadSettings();
        UnlockManager.loadStats();

        if (!TerminalControl.loadedDiscord) TerminalControl.discordManager.initializeDiscord();
        TerminalControl.discordManager.updateRPC();
    }

    /** Loads the UI elements to be rendered on screen */
    private void loadUI() {
        //Reset stage
        stage.clear();

        //Background image
        if (background == null) {
            int max = TerminalControl.full ? 8 : 2;
            FileHandle fileHandle = Gdx.files.internal("game/ui/mainMenuImages/" + MathUtils.random(1, max) + ".png");
            background = new Image(new Texture(fileHandle));
            background.scaleBy(6.8f);
        }
        stage.addActor(background);

        //Set title icon
        Image image = new Image(new Texture(Gdx.files.internal("game/ui/MainMenuIcon.png")));
        image.scaleBy(0.5f);
        image.setPosition(2880 / 2.0f - 1.5f * image.getWidth() / 2.0f, 1620 * 0.775f);
        stage.addActor(image);

        //Additional lite image if version is lite
        if (!TerminalControl.full) {
            Image image1 = new Image(new Texture(Gdx.files.internal("game/ui/Lite.png")));
            image1.scaleBy(0.25f);
            image1.setPosition(2880 / 2.0f + 2.5f * image1.getWidth(), 1620 * 0.83f);
            stage.addActor(image1);
        }

        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont20;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Set new game button params
        TextButton newGameButton = new TextButton("New Game", buttonStyle);
        float yPos = Gdx.app.getType() == Application.ApplicationType.Android ? 0.45f : 0.55f;
        newGameButton.setPosition(2880 / 2.0f - BUTTON_WIDTH / 2.0f, 1620 * yPos);
        newGameButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        newGameButton.getLabel().setAlignment(Align.center);
        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Start new game -> Choose airport screen
                game.setScreen((TerminalControl.revisionNeedsUpdate() && FileLoader.checkIfSaveExists()) ? new NotifScreen(game, background) : new NewGameScreen(game, background));
            }
        });
        stage.addActor(newGameButton);

        //Set load game button params
        TextButton loadGameButton = new TextButton("Load Game", buttonStyle);
        float yPos1 = Gdx.app.getType() == Application.ApplicationType.Android ? 0.3f : 0.4f;
        loadGameButton.setPosition(2880 / 2.0f - BUTTON_WIDTH / 2.0f, 1620 * yPos1);
        loadGameButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        loadGameButton.getLabel().setAlignment(Align.center);
        loadGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Load game -> Saved games screen
                game.setScreen((TerminalControl.revisionNeedsUpdate() && FileLoader.checkIfSaveExists()) ? new NotifScreen(game, background) : new LoadGameScreen(game, background));
            }
        });
        stage.addActor(loadGameButton);

        //Set settings button
        ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
        imageButtonStyle.imageUp = TerminalControl.skin.getDrawable("Settings_up");
        imageButtonStyle.imageDown = TerminalControl.skin.getDrawable("Settings_down");
        ImageButton settingsButton = new ImageButton(imageButtonStyle);
        settingsButton.setPosition(1440 - 1200 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL);
        settingsButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL);
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go to settings screen
                game.setScreen(new MenuSettingsScreen(game, background));
            }
        });
        stage.addActor(settingsButton);

        //Set info button
        ImageButton.ImageButtonStyle imageButtonStyle1 = new ImageButton.ImageButtonStyle();
        imageButtonStyle1.imageUp = TerminalControl.skin.getDrawable("Information_up");
        imageButtonStyle1.imageDown = TerminalControl.skin.getDrawable("Information_down");
        ImageButton infoButton = new ImageButton(imageButtonStyle1);
        infoButton.setPosition(1440 - 950 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL);
        infoButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL);
        infoButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go to info screen
                game.setScreen(new InfoScreen(game, background));
            }
        });
        stage.addActor(infoButton);

        //Set help button
        ImageButton.ImageButtonStyle imageButtonStyle2 = new ImageButton.ImageButtonStyle();
        imageButtonStyle2.imageUp = TerminalControl.skin.getDrawable("Help_up");
        imageButtonStyle2.imageDown = TerminalControl.skin.getDrawable("Help_down");
        ImageButton helpButton = new ImageButton(imageButtonStyle2);
        helpButton.setPosition(1440 - 700 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL);
        helpButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL);
        helpButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go to help screen
                game.setScreen(new HelpScreen(game, background));
            }
        });
        stage.addActor(helpButton);

        //Set upgrade button (for full version only)
        if (TerminalControl.full) {
            ImageButton.ImageButtonStyle imageButtonStyle3 = new ImageButton.ImageButtonStyle();
            imageButtonStyle3.imageUp = TerminalControl.skin.getDrawable("Upgrade_up");
            imageButtonStyle3.imageDown = TerminalControl.skin.getDrawable("Upgrade_down");
            ImageButton upgradeButton = new ImageButton(imageButtonStyle3);
            upgradeButton.setPosition(1440 + 700 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL);
            upgradeButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL);
            upgradeButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    //Go to upgrade screen
                    game.setScreen(new UpgradeScreen(game, background));
                }
            });
            stage.addActor(upgradeButton);
        }

        //Set achievement button
        ImageButton.ImageButtonStyle imageButtonStyle4 = new ImageButton.ImageButtonStyle();
        imageButtonStyle4.imageUp = TerminalControl.skin.getDrawable("Medal_up");
        imageButtonStyle4.imageDown = TerminalControl.skin.getDrawable("Medal_down");
        ImageButton achievementButton = new ImageButton(imageButtonStyle4);
        achievementButton.setPosition(1440 + 950 - BUTTON_WIDTH_SMALL / 2.0f, 1620 - BUTTON_HEIGHT_SMALL);
        achievementButton.setSize(BUTTON_WIDTH_SMALL, BUTTON_HEIGHT_SMALL);
        achievementButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go to upgrade screen
                game.setScreen(new AchievementScreen(game, background));
            }
        });
        stage.addActor(achievementButton);

        //Changelog button
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont12;
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");
        TextButton changelogButton = new TextButton("Changelog", textButtonStyle);
        changelogButton.setSize(350, BUTTON_HEIGHT_SMALL);
        changelogButton.setPosition(2880 - changelogButton.getWidth(), 1620 * 0.6f);
        changelogButton.getLabel().setAlignment(Align.center);
        changelogButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new ChangelogScreen(game, background));
            }
        });
        stage.addActor(changelogButton);

        //Set quit button params if desktop
        if (Gdx.app.getType() == Application.ApplicationType.Android) return;
        TextButton quitButton = new TextButton("Quit", buttonStyle);
        quitButton.setPosition(2880 / 2.0f - BUTTON_WIDTH / 2.0f, 1620 * 0.1f);
        quitButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        quitButton.getLabel().setAlignment(Align.center);
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Quit game
                RadarScreen.disposeStatic();
                Ui.disposeStatic();
                dispose();
                Gdx.app.exit();
            }
        });
        stage.addActor(quitButton);
    }

    /** Overrides show method of BasicScreen */
    @Override
    public void show() {
        if (Fonts.defaultFont6 == null) {
            //Regenerate fonts that were disposed
            Fonts.generateAllFonts();
        }
        loadUI();
    }
}
