package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.ui.Ui;

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
        int highestAlt = -1;
        if (selectedAircraft instanceof Departure) {
            lowestAlt = selectedAircraft.getLowestAlt();
            highestAlt = TerminalControl.radarScreen.maxAlt;
        } else if (selectedAircraft instanceof Arrival) {
            lowestAlt = TerminalControl.radarScreen.minAlt;
            if (latMode.equals("Hold at")) {
                int[] restr = selectedAircraft.getRoute().getHoldProcedure().getAltRestAtWpt(TerminalControl.radarScreen.waypoints.get(LatTab.holdWpt));
                lowestAlt = restr[0];
                highestAlt = restr[1];
            } else if (altMode.contains("STAR") && selectedAircraft.getAltitude() < TerminalControl.radarScreen.maxAlt) {
                //Set alt restrictions in box
                highestAlt = (int)selectedAircraft.getAltitude();
                highestAlt -= highestAlt % 1000;
                int starHighestAlt = selectedAircraft.getRoute().getWptMaxAlt(selectedAircraft.getDirect().getName());
                if (starHighestAlt > -1) highestAlt = starHighestAlt;
            }
            if (highestAlt == -1) {
                highestAlt = TerminalControl.radarScreen.maxAlt;
            }
            if (selectedAircraft.isGsCap() || (selectedAircraft.getIls() instanceof LDA && selectedAircraft.isLocCap())) {
                highestAlt = lowestAlt = selectedAircraft.getIls().getMissedApchProc().getClimbAlt();
            }
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
                String toAdd = altTracker / 100 >= TerminalControl.radarScreen.transLvl ? "FL" + altTracker / 100 : Integer.toString(altTracker);
                alts.add(toAdd);
                if ("RJOO".equals(selectedAircraft.getAirport().getIcao()) && altTracker == 3000) alts.add("3500");
                altTracker += 1000;
            }
        } else {
            while (lowestAlt <= highestAlt) {
                String toAdd = lowestAlt / 100 >= TerminalControl.radarScreen.transLvl ? "FL" + lowestAlt / 100 : Integer.toString(lowestAlt);
                alts.add(toAdd);
                if ("RJOO".equals(selectedAircraft.getAirport().getIcao()) && lowestAlt == 3000) alts.add("3500");
                lowestAlt += 1000;
            }
        }
        valueBox.setItems(alts);
        valueBox.setSelected(clearedAlt / 100 >= TerminalControl.radarScreen.transLvl ? "FL" + clearedAlt / 100 : Integer.toString(clearedAlt));
        notListening = false;
    }

    @Override
    public void compareWithAC() {
        altModeChanged = !altMode.equals(selectedAircraft.getNavState().getDispAltMode().last());
        altChanged = clearedAlt != selectedAircraft.getNavState().getClearedAlt().last();
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
        selectedAircraft.getNavState().sendAlt(altMode, clearedAlt);
    }

    @Override
    public void updatePaneWidth(float paneWidth) {
        notListening = true;
        super.updatePaneWidth(paneWidth);
        notListening = false;
    }

    @Override
    public void getACState() {
        altMode = selectedAircraft.getNavState().getDispAltMode().last();
        altModeChanged = false;
        clearedAlt = selectedAircraft.getNavState().getClearedAlt().last();
        altChanged = false;
    }

    @Override
    public void getChoices() {
        altMode = settingsBox.getSelected();
        clearedAlt = valueBox.getSelected().contains("FL") ? Integer.parseInt(valueBox.getSelected().substring(2)) * 100 : Integer.parseInt(valueBox.getSelected());
    }
}
