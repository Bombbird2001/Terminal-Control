package com.bombbird.terminalcontrol.screens.selectgamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.FileLoader;
import org.json.JSONArray;
import org.json.JSONObject;

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
        JSONArray saves = FileLoader.loadSaves();
        for (int i = 0; i < saves.length(); i++) {
            final JSONObject jsonObject = saves.getJSONObject(i);
            String toDisplay = jsonObject.getString("MAIN_NAME") + " (AIRAC " + jsonObject.getInt("AIRAC") +  ")\nPlanes landed: " + jsonObject.getInt("landings") + "    Planes taken off: " + jsonObject.getInt("airborne");

            final TextButton saveButton = new TextButton(toDisplay, getButtonStyle());
            saveButton.setName(toDisplay.substring(0, 4));
            saveButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    FileHandle handle = Gdx.files.internal("game/available.arpt");

                    int newestAirac;
                    int minAirac = -1;

                    String[] airports = handle.readString().split("\\r?\\n");
                    newestAirac = Integer.parseInt(airports[0]);
                    for (String icao: airports) {
                        if (icao.split(":")[0].equals(saveButton.getName())) {
                            minAirac = Integer.parseInt(icao.split(":")[1]);
                            break;
                        }
                    }

                    if (minAirac < jsonObject.getInt("AIRAC")) {
                        jsonObject.put("AIRAC", newestAirac);
                    } else {
                        //TODO Alert user that game AIRAC is no longer compatible with newest version, can play with older version but may be removed in the future
                    }

                    RadarScreen radarScreen = new RadarScreen(game, jsonObject);
                    TerminalControl.radarScreen = radarScreen;
                    game.setScreen(radarScreen);

                    event.handle();
                }
            });
            getScrollTable().add(saveButton).width(BUTTON_WIDTH * 1.2f).height(BUTTON_HEIGHT);
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
