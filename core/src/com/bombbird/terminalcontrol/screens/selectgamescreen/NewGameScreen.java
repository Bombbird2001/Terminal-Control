package com.bombbird.terminalcontrol.screens.selectgamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class NewGameScreen extends SelectGameScreen {
    public NewGameScreen(final TerminalControl game) {
        super(game);
    }

    /** Overrides loadLabel method in SelectGameScreen to load appropriate title for label */
    @Override
    public void loadLabel() {
        //Set label params
        super.loadLabel();
        Label headerLabel = new Label("Choose airport:", getLabelStyle());
        headerLabel.setWidth(BUTTON_WIDTH);
        headerLabel.setHeight(BUTTON_HEIGHT);
        headerLabel.setPosition(2880 / 2.0f - BUTTON_WIDTH / 2.0f, 1620 * 0.85f);
        headerLabel.setAlignment(Align.center);
        getStage().addActor(headerLabel);
    }

    /** Overrides loadScroll method in SelectGameScreen to load airport info into scrollPane */
    @Override
    public void loadScroll() {
        //Load airports
        String[] airports = {"RCTP\nTaiwan Taoyuan International Airport", "WSSS\nSingapore Changi Airport", "VHHH\nHong Kong International Airport", "RJAA\nNarita International Airport", "WMKK\nKuala Lumpur International Airport", "WIII\nSoekarno-Hatta International Airport", "ZSPD\nShanghai Pudong International Airport", "VTBS\nBangkok Suvarnabhumi Airport", "VVTS\nTan Son Nhat International Airport"};
        for (final String airport: airports) {
            final TextButton airportButton = new TextButton(airport, getButtonStyle());
            airportButton.setName(airport.substring(0, 4));
            airportButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String name = actor.getName();
                    FileHandle handle = Gdx.files.internal("game/available.arpt");
                    String[] airports = handle.readString().split("\\r?\\n");
                    boolean found = false;
                    int airac = -1;
                    for (String arptData: airports) {
                        String arpt = arptData.split(":")[0];
                        airac = Integer.parseInt(arptData.split(":")[1]);
                        if (arpt.equals(name)) {
                            found = true;
                            break;
                        }
                    }
                    if (found && airac > -1) {
                        game.setScreen(new RadarScreen(game, name, airac));
                    } else {
                        if (!found) {
                            Gdx.app.log("Directory not found", "Directory not found for " + name);
                            //TODO Set popup to ask user to get full version
                        } else {
                            Gdx.app.log("Invalid AIRAC cycle", "Invalid AIRAC cycle " + airac);
                        }
                    }
                }
            });
            getScrollTable().add(airportButton).width(BUTTON_WIDTH * 1.2f).height(BUTTON_HEIGHT);
            getScrollTable().row();
        }
        ScrollPane scrollPane = new ScrollPane(getScrollTable());
        scrollPane.setupFadeScrollBars(1, 1.5f);
        scrollPane.setX(2880 / 2f - BUTTON_WIDTH * 0.6f);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(BUTTON_WIDTH * 1.2f);
        scrollPane.setHeight(1620 * 0.6f);

        getStage().addActor(scrollPane);
    }
}
