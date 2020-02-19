package com.bombbird.terminalcontrol.screens.selectgamescreen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.helpmanual.HelpSectionScreen;

public class HelpScreen extends SelectGameScreen {
    public HelpScreen(TerminalControl game, Image background) {
        super(game, background);
    }

    /** Overrides loadLabel method in SelectGameScreen to load appropriate title for label */
    @Override
    public void loadLabel() {
        //Set label params
        super.loadLabel();
        Label headerLabel = new Label("Help Manual", getLabelStyle());
        headerLabel.setWidth(MainMenuScreen.BUTTON_WIDTH);
        headerLabel.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        headerLabel.setPosition(2880 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        getStage().addActor(headerLabel);
    }

    /** Overrides loadScroll method in SelectGameScreen to load airport info into scrollPane */
    @Override
    public void loadScroll() {
        //Load help sections
        String[] sections = {"Airports", "Aircraft instructions", "ILS, LDA", "Separation", "MVAs, restricted areas", "NTZ", "Wake turbulence", "Conflict prediction alerts"};
        for (String section: sections) {
            TextButton button = new TextButton(section, getButtonStyle());
            button.setName(section);
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if ("Airports".equals(button.getName())) {
                        game.setScreen(new AirportHelpScreen(game, background));
                    } else {
                        game.setScreen(new HelpSectionScreen(game, background, button.getName()));
                    }
                }
            });
            getScrollTable().add(button).width(MainMenuScreen.BUTTON_WIDTH * 1.2f).height(MainMenuScreen.BUTTON_HEIGHT);
            getScrollTable().row();
        }
        ScrollPane scrollPane = new ScrollPane(getScrollTable());
        scrollPane.setupFadeScrollBars(1, 1.5f);
        scrollPane.setX(2880 / 2f - MainMenuScreen.BUTTON_WIDTH * 0.6f);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(MainMenuScreen.BUTTON_WIDTH * 1.2f);
        scrollPane.setHeight(1620 * 0.6f);

        getStage().addActor(scrollPane);
    }
}
