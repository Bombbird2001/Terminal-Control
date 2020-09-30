package com.bombbird.terminalcontrol.screens.helpmanual;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.StandardUIScreen;
import com.bombbird.terminalcontrol.screens.selectgamescreen.AirportHelpScreen;
import com.bombbird.terminalcontrol.screens.selectgamescreen.HelpScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class HelpSectionScreen extends StandardUIScreen {
    private final Table scrollTable;
    private final String page;

    public HelpSectionScreen(TerminalControl game, Image background, String page) {
        super(game, background);

        this.page = page;
        scrollTable = new Table();
    }

    public void loadUI() {
        super.loadUI();

        loadLabel(page);
        loadScroll();
        loadContent();
        loadButtons();
    }

    private void loadScroll() {
        ScrollPane scrollPane = new ScrollPane(scrollTable);

        scrollPane.setX(2880 / 2f - MainMenuScreen.BUTTON_WIDTH);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(MainMenuScreen.BUTTON_WIDTH * 2);
        scrollPane.setHeight(1620 * 0.6f);
        scrollPane.getStyle().background = TerminalControl.skin.getDrawable("ListBackground");

        stage.addActor(scrollPane);
    }

    private void loadContent() {
        HelpManager.loadContent(scrollTable, page);
    }

    /** Loads the default button styles and back button */
    public void loadButtons() {
        //Set button textures
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont12;
        buttonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        buttonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Set back button params
        backButton = new TextButton("<= Back", buttonStyle);
        backButton.setWidth(MainMenuScreen.BUTTON_WIDTH);
        backButton.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        backButton.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.05f);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Go back to main menu
                if (page.length() == 4) {
                    game.setScreen(new AirportHelpScreen(game, background));
                } else {
                    game.setScreen(new HelpScreen(game, background));
                }
            }
        });

        stage.addActor(backButton);
    }
}
