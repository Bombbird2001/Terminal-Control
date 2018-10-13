package com.bombbird.atcsim.screens.Ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.entities.aircrafts.Arrival;
import com.bombbird.atcsim.entities.aircrafts.Departure;
import com.bombbird.atcsim.screens.RadarScreen;

public class AltTab extends Tab {

    private boolean altModeChanged;
    private boolean altChanged;
    private Array<String> alts;

    public AltTab(Ui ui) {
        super(ui);
        alts = new Array<String>();
    }

    @Override
    public void updateElements() {
        notListening = true;
        settingsBox.setItems(selectedAircraft.getNavState().getAltModes());
        settingsBox.setSelected(altMode);
        if (visible) {
            valueBox.setVisible(true);
        }
        alts.clear();
        int lowestAlt;
        int highestAlt;
        if (selectedAircraft instanceof Departure) {
            lowestAlt = selectedAircraft.getLowestAlt();
            highestAlt = RadarScreen.maxDeptAlt;
        } else if (selectedAircraft instanceof Arrival) {
            lowestAlt = RadarScreen.minArrAlt;
            if (altMode.contains("STAR")) {
                //Set alt restrictions in box
                highestAlt = (int)selectedAircraft.getAltitude();
                highestAlt -= highestAlt % 1000;
            } else {
                highestAlt = RadarScreen.maxArrAlt;
            }
            //TODO Set minimum alt for each ILS if cleared app
        } else {
            lowestAlt = 0;
            highestAlt = 10000;
            Gdx.app.log("Invalid aircraft type", "Aircraft not instance of departure or arrival");
        }

        //Adds the possible altitudes between range to array
        if (lowestAlt % 1000 != 0) {
            alts.add(Integer.toString(lowestAlt));
            int altTracker = lowestAlt + (1000 - lowestAlt % 1000);
            while (altTracker <= highestAlt) {
                alts.add(Integer.toString(altTracker));
                altTracker += 1000;
            }
        } else {
            while (lowestAlt <= highestAlt) {
                alts.add(Integer.toString(lowestAlt));
                lowestAlt += 1000;
            }
        }
        valueBox.setItems(alts);
        valueBox.setSelected(Integer.toString(clearedAlt));
        notListening = false;
    }

    @Override
    public void compareWithAC() {
        altModeChanged = !altMode.equals(selectedAircraft.getNavState().getAltMode());
        altChanged = !(clearedAlt == selectedAircraft.getClearedAltitude());
    }

    @Override
    public void updateElementColours() {
        notListening = true;
        //Alt mode selectbox colour
        if (altModeChanged) {
            settingsBox.getStyle().fontColor = Color.YELLOW;
        } else {
            settingsBox.getStyle().fontColor = Color.WHITE;
        }

        //Alt box colour
        if (altChanged) {
            valueBox.getStyle().fontColor = Color.YELLOW;
        } else {
            valueBox.getStyle().fontColor = Color.WHITE;
        }

        tabChanged = altModeChanged || altChanged;
        super.updateElementColours();
        notListening = false;
    }

    @Override
    public void updateMode() {
        if (altMode.equals("Climb via SID") || altMode.equals("Descend via STAR")) {
            selectedAircraft.setAltMode("sidstar");
        } else {
            selectedAircraft.setAltMode("open");
            selectedAircraft.setExpedite(altMode.contains("Expedite"));
        }
        selectedAircraft.getNavState().setAltMode(altMode);

        selectedAircraft.setClearedAltitude(clearedAlt);
    }

    @Override
    public void updatePaneWidth(float paneWidth) {
        notListening = true;
        super.updatePaneWidth(paneWidth);
        notListening = false;
    }

    @Override
    public void getACState() {
        altMode = selectedAircraft.getNavState().getAltMode();
        altModeChanged = false;
        clearedAlt = selectedAircraft.getClearedAltitude();
        altChanged = false;
    }

    @Override
    public void getChoices() {
        altMode = settingsBox.getSelected();
        clearedAlt = Integer.parseInt(valueBox.getSelected());
    }
}
