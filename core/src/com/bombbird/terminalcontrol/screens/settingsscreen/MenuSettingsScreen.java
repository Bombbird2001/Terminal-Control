package com.bombbird.terminalcontrol.screens.settingsscreen;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.StandardUIScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class MenuSettingsScreen extends StandardUIScreen {
    public MenuSettingsScreen(TerminalControl game, Image background) {
        super(game, background);
    }

    /** Loads the different buttons to go to different settings screens */
    public void loadButtons() {
        super.loadButtons();

        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont16;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        TextButton gameSettingsButton = new TextButton("Default game settings", buttonStyle);
        gameSettingsButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT);
        gameSettingsButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.65f);
        stage.addActor(gameSettingsButton);

        TextButton globalSettingsButton = new TextButton("Global settings", buttonStyle);
        globalSettingsButton.setSize(MainMenuScreen.BUTTON_WIDTH, MainMenuScreen.BUTTON_HEIGHT);
        globalSettingsButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.5f);
        stage.addActor(globalSettingsButton);
    }

    @Override
    public void loadUI() {
        super.loadUI();
        loadLabel("Settings");
        loadButtons();
    }
}
