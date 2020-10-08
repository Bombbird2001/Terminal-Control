package com.bombbird.terminalcontrol.screens.settingsscreen.customsetting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.screens.BasicScreen;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.SettingsTemplateScreen;
import com.bombbird.terminalcontrol.screens.settingsscreen.categories.OtherSettingsScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.HashMap;
import java.util.Map;

public class WeatherScreen extends BasicScreen {
    private HashMap<String, Array<SelectBox<Integer>>> boxMap;

    public WeatherScreen(TerminalControl game) {
        super(game, 5760, 3240);
    }

    public void loadUI() {
        stage.clear();
        loadButton();
        loadLabel();
        loadOptions();
    }

    /** Loads the wind options for each airport */
    private void loadOptions() {
        boxMap = new HashMap<>();

        RadarScreen radarScreen = TerminalControl.radarScreen;
        int currentY = (int)(3240 * 0.65f);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont30;
        labelStyle.fontColor = Color.WHITE;

        for (Airport airport: radarScreen.airports.values()) {
            Array<SelectBox<Integer>> boxes = new Array<>();

            Label airportLabel = new Label(airport.getIcao() + ": ", labelStyle);
            airportLabel.setPosition(5760 * 0.2f, currentY);
            airportLabel.setHeight(300);
            airportLabel.setAlignment(Align.left);
            stage.addActor(airportLabel);

            Label atLabel = new Label("@", labelStyle);
            atLabel.setPosition(5760 * 0.5f, currentY);
            atLabel.setHeight(300);
            atLabel.setAlignment(Align.left);
            stage.addActor(atLabel);

            Label knotsLabel = new Label("kts", labelStyle);
            knotsLabel.setPosition(5760 * 0.675f, currentY);
            knotsLabel.setHeight(300);
            knotsLabel.setAlignment(Align.left);
            stage.addActor(knotsLabel);

            //Hundreds place for heading
            SelectBox<Integer> hdg1 = generateSmallBoxes();
            hdg1.setPosition(5760 * 0.3f, currentY);
            hdg1.setItems(0, 1, 2, 3);
            stage.addActor(hdg1);
            boxes.add(hdg1);

            //Tens place for heading
            SelectBox<Integer> hdg2 = generateSmallBoxes();
            hdg2.setPosition(hdg1.getX() + 325, currentY);
            hdg2.setItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            stage.addActor(hdg2);
            boxes.add(hdg2);

            //Ones place for heading
            SelectBox<Integer> hdg3 = generateSmallBoxes();
            hdg3.setPosition(hdg2.getX() + 325, currentY);
            hdg3.setItems(0, 5);
            stage.addActor(hdg3);
            boxes.add(hdg3);

            //Tens place for speed
            SelectBox<Integer> spd1 = generateSmallBoxes();
            spd1.setPosition(5760 * 0.55f, currentY);
            spd1.setItems(0, 1, 2, 3);
            stage.addActor(spd1);
            boxes.add(spd1);

            //Ones place for speed
            SelectBox<Integer> spd2 = generateSmallBoxes();
            spd2.setPosition(spd1.getX() + 325, currentY);
            spd2.setItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            stage.addActor(spd2);
            boxes.add(spd2);

            boxMap.put(airport.getIcao(), boxes);

            //Set the current weather for the airport into the boxes
            int windDir = airport.getWinds()[0];
            int windSpd = airport.getWinds()[1];

            hdg1.setSelected(windDir / 100);
            hdg2.setSelected((windDir / 10) % 10);
            hdg3.setSelected(windDir % 10);

            spd1.setSelected(windSpd / 10);
            spd2.setSelected(windSpd % 10);

            modulateHdg(hdg1, hdg2, hdg3);

            //Set listeners after initializing all boxes
            hdg1.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    modulateHdg(hdg1, hdg2, hdg3);
                }
            });

            hdg2.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    modulateHdg(hdg1, hdg2, hdg3);
                }
            });

            currentY -= 486;
        }
    }

    /** Ensures heading is valid, does not exceed 360 */
    private void modulateHdg(SelectBox<Integer> hdg1, SelectBox<Integer> hdg2, SelectBox<Integer> hdg3) {
        if (hdg1.getSelected() == 3) {
            hdg2.setItems(0, 1, 2, 3, 4, 5, 6);
        } else {
            hdg2.setItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
            hdg3.setItems(0, 5);
        }
        if (hdg1.getSelected() == 3 && hdg2.getSelected() == 6) {
            hdg3.setItems(0);
        } else {
            hdg3.setItems(0, 5);
        }
    }

    /** Generates a standard small selectBox */
    private SelectBox<Integer> generateSmallBoxes() {
        SelectBox<Integer> box = new SelectBox<>(SettingsTemplateScreen.selectBoxStyle);
        box.setSize(300, 300);
        box.setAlignment(Align.center);
        box.getList().setAlignment(Align.center);

        return box;
    }

    /** Loads heading label */
    private void loadLabel() {
        //Set label params
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont30;
        labelStyle.fontColor = Color.WHITE;
        Label headerLabel = new Label("Custom Weather", labelStyle);
        headerLabel.setWidth(MainMenuScreen.BUTTON_WIDTH);
        headerLabel.setHeight(MainMenuScreen.BUTTON_HEIGHT);
        headerLabel.setPosition(5760 / 2.0f - MainMenuScreen.BUTTON_WIDTH / 2.0f, 3240 * 0.85f);
        headerLabel.setAlignment(Align.center);
        stage.addActor(headerLabel);

        Label.LabelStyle labelStyle1 = new Label.LabelStyle();
        labelStyle1.font = Fonts.defaultFont20;
        labelStyle1.fontColor = Color.WHITE;
        Label noteLabel = new Label("Note: Use HDG 000 for variable (VRB) wind direction", labelStyle1);
        noteLabel.setPosition(5760 / 2.0f - noteLabel.getWidth() / 2.0f, 3240 * 0.8f);
        noteLabel.setAlignment(Align.center);
        stage.addActor(noteLabel);
    }

    /** Loads buttons */
    private void loadButton() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = TerminalControl.skin.getDrawable("Button_up");
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        TextButton cancelButton = new TextButton("Cancel", textButtonStyle);
        cancelButton.setSize(1200, 300);
        cancelButton.setPosition(5760 / 2f - 1600, 3240 - 2800);
        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new OtherSettingsScreen(game, TerminalControl.radarScreen, null));
            }
        });
        stage.addActor(cancelButton);

        TextButton confirmButton = new TextButton("Confirm", textButtonStyle);
        confirmButton.setSize(1200, 300);
        confirmButton.setPosition(5760 / 2f + 400, 3240 - 2800);
        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                HashMap<String, int[]> newData = new HashMap<>();
                for (Map.Entry<String, Array<SelectBox<Integer>>> entry: boxMap.entrySet()) {
                    int windHdg = entry.getValue().get(0).getSelected() * 100 + entry.getValue().get(1).getSelected() * 10 + entry.getValue().get(2).getSelected();
                    int windSpd = entry.getValue().get(3).getSelected() * 10 + entry.getValue().get(4).getSelected();
                    newData.put(entry.getKey(), new int[] {windHdg, windSpd});
                }

                TerminalControl.radarScreen.weatherSel = RadarScreen.Weather.STATIC;
                TerminalControl.radarScreen.getMetar().updateCustomWeather(newData);
                game.setScreen(new OtherSettingsScreen(game, TerminalControl.radarScreen, null));
            }
        });
        stage.addActor(confirmButton);
    }

    /** Overrides show method of basic screen */
    @Override
    public void show() {
        loadUI();
    }
}
