package com.bombbird.terminalcontrol.screens.selectgamescreen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;

public class LoadGameScreen extends SelectGameScreen {
    public LoadGameScreen(final TerminalControl game) {
        super(game);
    }

    /** Overrides loadLabel method in SelectGameScreen to load appropriate title for label */
    @Override
    public void loadLabel() {
        //Set label params
        super.loadLabel();
        Label headerLabel = new Label("Choose save to load:", getLabelStyle());
        headerLabel.setWidth(BUTTON_WIDTH);
        headerLabel.setHeight(BUTTON_HEIGHT);
        headerLabel.setPosition(2880 / 2.0f - BUTTON_WIDTH / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        getStage().addActor(headerLabel);
    }

    /** Overrides loadScroll method in SelectGameScreen to load save info into scrollPane */
    @Override
    public void loadScroll() {
        //TODO Load saves


        ScrollPane scrollPane = new ScrollPane(getScrollTable());
        scrollPane.setupFadeScrollBars(1, 1.5f);
        scrollPane.setX(2880 / 2f - BUTTON_WIDTH * 0.6f);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(BUTTON_WIDTH * 1.2f);
        scrollPane.setHeight(1620 * 0.6f);

        getStage().addActor(scrollPane);
    }
}
