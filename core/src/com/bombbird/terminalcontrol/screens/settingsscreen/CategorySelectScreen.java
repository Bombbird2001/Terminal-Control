package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.StandardUIScreen;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.*;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class CategorySelectScreen extends StandardUIScreen {
    public RadarScreen radarScreen;

    public CategorySelectScreen(TerminalControl game, Image background, RadarScreen radarScreen) {
        super(game, background);

        this.radarScreen = radarScreen;
    }

    /** Loads the different buttons to go to different settings screens */
    public void loadButtons() {
        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont16;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        TextButton displayButton = new TextButton("Display", buttonStyle);
        displayButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT);
        displayButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH - 100, 1620 * 0.65f);
        displayButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new DisplaySettingsScreen(game, null, background));
            }
        });
        stage.addActor(displayButton);

        TextButton dataTagButton = new TextButton("Data tag", buttonStyle);
        dataTagButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT);
        dataTagButton.setPosition(2880 / 2.0f + 100, 1620 * 0.65f);
        dataTagButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new DataTagSettingsScreen(game, null, background));
            }
        });
        stage.addActor(dataTagButton);

        TextButton trafficButton = new TextButton("Traffic", buttonStyle);
        trafficButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT);
        trafficButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH - 100, 1620 * 0.5f);
        trafficButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new TrafficSettingsScreen(game, null, background));
            }
        });
        stage.addActor(trafficButton);

        TextButton othersButton = new TextButton("Others", buttonStyle);
        othersButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT);
        othersButton.setPosition(2880 / 2.0f + 100, 1620 * 0.5f);
        othersButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new OtherSettingsScreen(game, null, background));
            }
        });
        stage.addActor(othersButton);

        TextButton alertsButton = new TextButton("Alerts", buttonStyle);
        alertsButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT);
        alertsButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH - 100, 1620 * 0.35f);
        alertsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new AlertsSettingsScreen(game, null, background));
            }
        });
        if (TerminalControl.full) stage.addActor(alertsButton);

        //Set button textures
        TextButton.TextButtonStyle buttonStyle1 = new TextButton.TextButtonStyle();
        buttonStyle1.font = Fonts.defaultFont12;
        buttonStyle1.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle1.down = TerminalControl.skin.getDrawable("Button_down");

        //Set back button params
        TextButton backButton = new TextButton("<= Back", buttonStyle1);
        backButton.setWidth(MainMenuScreen.BUTTON_WIDTH);
        backButton.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu
                if (radarScreen != null) {
                    //TODO Set the correct state
                } else {
                    game.setScreen(new MenuSettingsScreen(game, background));
                }
            }
        });

        stage.addActor(backButton);
    }

    @Override
    public void loadUI() {
        if (background != null) stage.addActor(background);
        super.loadUI();
        loadLabel(radarScreen != null ? "Settings" : "Default game settings");
        loadButtons();
    }
}
