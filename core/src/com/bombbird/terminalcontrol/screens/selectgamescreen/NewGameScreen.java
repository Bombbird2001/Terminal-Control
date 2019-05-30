package com.bombbird.terminalcontrol.screens.selectgamescreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;

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
        String[] airports;
        if ("full".equals(Gdx.files.internal("game/type.type").readString())) {
            airports = new String[] {"Tutorial\n(Progress not saved)", "RCTP\nTaiwan Taoyuan International Airport", "WSSS\nSingapore Changi Airport", "RJTT\nTokyo Haneda Airport", "VHHH\nHong Kong International Airport", "RJBB\nOsaka Kansai International Airport", "VTBD\nBangkok Don Mueang International Airport"};
        } else {
            airports = new String[] {"Tutorial\n(Progress not saved)", "RCTP\nTaiwan Taoyuan International Airport", "WSSS\nSingapore Changi Airport"};
        }
        for (final String airport: airports) {
            TextButton airportButton = new TextButton(airport, getButtonStyle());
            airportButton.setName(airport.substring(0, 4));
            airportButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String name = actor.getName();
                    boolean tutorial = false;
                    if ("Tuto".equals(name)) {
                        name = "RCTP";
                        tutorial = true;
                    }
                    FileHandle handle = Gdx.files.internal("game/available.arpt");
                    String[] airports = handle.readString().split("\\r?\\n");
                    boolean found = false;
                    int airac = -1;
                    for (String arptData: airports) {
                        String arpt = arptData.split(":")[0];
                        if (arpt.equals(name)) {
                            found = true;
                            airac = Integer.parseInt(arptData.split(":")[1].split(",")[0].split("-")[1]);
                            break;
                        }
                    }

                    FileHandle handle1;
                    if (Gdx.app.getType() == Application.ApplicationType.Android) {
                        handle1 = Gdx.files.local("saves/saves.saves");
                    } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                        handle1 = Gdx.files.external(FileLoader.mainDir + "/saves/saves.saves");
                    } else {
                        handle1 = Gdx.files.local("saves/saves.saves");
                        Gdx.app.log("File load error", "Unknown platform " + Gdx.app.getType().name() + " used!");
                    }
                    int slot = 0;
                    if (handle1.exists()) {
                        Array<String> saves = new Array<String>(handle1.readString().split(","));
                        while (saves.contains(Integer.toString(slot), false)) {
                            slot++;
                        }
                    }

                    if (found && airac > -1) {
                        RadarScreen radarScreen = new RadarScreen(game, name, airac, slot, tutorial);
                        TerminalControl.radarScreen = radarScreen;
                        game.setScreen(radarScreen);
                    } else {
                        if (!found) {
                            Gdx.app.log("Directory not found", "Directory not found for " + name);
                        } else {
                            Gdx.app.log("Invalid AIRAC cycle", "Invalid AIRAC cycle " + airac);
                        }
                    }
                    event.handle();
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
