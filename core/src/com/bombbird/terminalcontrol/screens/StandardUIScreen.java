package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class StandardUIScreen extends BasicScreen {
    public final Image background;

    public StandardUIScreen(final TerminalControl game, Image background) {
        super(game, 2880, 1620);

        //Set background image to that shown on main menu screen
        this.background = background;
    }

    /** Loads screen UI, to be overridden in each screen */
    public void loadUI() {
        //Reset stage and add background
        stage.clear();
        if (background != null) stage.addActor(background);
    }

    /** Loads heading label */
    public void loadLabel(String header) {
        //Set label params
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;
        Label headerLabel = new Label(header, labelStyle);
        headerLabel.setWidth(MainMenuScreen.BUTTON_WIDTH);
        headerLabel.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);
    }

    /** Loads back button */
    public void loadButtons() {
        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont12;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Set back button params
        TextButton backButton = new TextButton("<= Back", buttonStyle);
        backButton.setWidth(MainMenuScreen.BUTTON_WIDTH);
        backButton.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu
                game.setScreen(new MainMenuScreen(game, background));
            }
        });

        stage.addActor(backButton);
    }

    /** Overrides show method of basic screen */
    @Override
    public void show() {
        loadUI();
    }
}
