package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency;
import com.bombbird.terminalcontrol.entities.aircrafts.NavState;
import com.bombbird.terminalcontrol.ui.Ui;

public class AltTab extends Tab {

    private boolean altModeChanged;
    private boolean altChanged;
    private final Array<String> alts;

    public AltTab(Ui ui) {
        super(ui);
        alts = new Array<>();
    }

    @Override
    public void updateElements() {
        if (selectedAircraft == null) return;
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
            if (Ui.CLIMB_VIA_SID.equals(altMode) && selectedAircraft.getRoute().getWptMinAlt(LatTab.clearedWpt) > highestAlt) {
                highestAlt = selectedAircraft.getRoute().getWptMinAlt(LatTab.clearedWpt);
            }
        } else if (selectedAircraft instanceof Arrival) {
            lowestAlt = TerminalControl.radarScreen.minAlt;
            if (Ui.HOLD_AT.equals(latMode)) {
                int[] restr = selectedAircraft.getRoute().getHoldProcedure().getAltRestAtWpt(TerminalControl.radarScreen.waypoints.get(LatTab.holdWpt));
                lowestAlt = restr[0];
                highestAlt = restr[1];
            } else if (Ui.DESCEND_VIA_STAR.equals(altMode) && selectedAircraft.getAltitude() < TerminalControl.radarScreen.maxAlt) {
                //Set alt restrictions in box
                highestAlt = (int) selectedAircraft.getAltitude();
                highestAlt -= highestAlt % 1000;
                int starHighestAlt = selectedAircraft.getDirect() == null ? TerminalControl.radarScreen.maxAlt : selectedAircraft.getRoute().getWptMaxAlt(selectedAircraft.getDirect().getName());
                if (starHighestAlt > -1) highestAlt = starHighestAlt;
            }
            if (highestAlt == -1) {
                highestAlt = TerminalControl.radarScreen.maxAlt;
            }
            if (highestAlt < (int) selectedAircraft.getAltitude() && (int) selectedAircraft.getAltitude() <= TerminalControl.radarScreen.maxAlt) {
                highestAlt = ((int) selectedAircraft.getAltitude()) / 1000 * 1000;
            }
            String icao = selectedAircraft.getAirport().getIcao();
            if ("TCOO".equals(icao) && selectedAircraft.getAltitude() >= 3499 && highestAlt == 3000) {
                highestAlt = 3500;
            } else if ((("TCHH".equals(icao) && selectedAircraft.getSidStar().getRunways().contains("25R", false)) || "TCHX".equals(icao)) && selectedAircraft.getAltitude() >= 4499 && highestAlt == 4000) {
                highestAlt = 4500;
            }
            if (selectedAircraft.getEmergency().isActive() && selectedAircraft.getEmergency().getType() == Emergency.Type.PRESSURE_LOSS) {
                highestAlt = 10000; //Cannot climb above 10000 feet due to pressure loss
            }
            if (highestAlt < lowestAlt) highestAlt = lowestAlt;
            if (selectedAircraft.isGsCap() || (selectedAircraft.getIls() != null && selectedAircraft.getIls().isNpa() && selectedAircraft.isLocCap())) {
                highestAlt = lowestAlt = selectedAircraft.getIls().getMissedApchProc().getClimbAlt();
            }
        } else {
            lowestAlt = 0;
            highestAlt = 10000;
            Gdx.app.log("Invalid aircraft type", "Aircraft not instance of departure or arrival");
        }
        clearedAlt = MathUtils.clamp(clearedAlt, lowestAlt, highestAlt);
        //Adds the possible altitudes between range to array
        String icao = selectedAircraft.getAirport().getIcao();
        if (lowestAlt % 1000 != 0) {
            alts.add(Integer.toString(lowestAlt));
            int altTracker = lowestAlt + (1000 - lowestAlt % 1000);
            while (altTracker <= highestAlt) {
                String toAdd = altTracker / 100 >= TerminalControl.radarScreen.transLvl ? "FL" + altTracker / 100 : Integer.toString(altTracker);
                alts.add(toAdd);
                if ("TCOO".equals(icao) && altTracker == 3000) alts.add("3500");
                if ((("TCHH".equals(icao) && selectedAircraft.getSidStar().getRunways().contains("25R", false)) || "TCHX".equals(icao)) && altTracker == 4000) alts.add("4500");
                altTracker += 1000;
            }
        } else {
            while (lowestAlt <= highestAlt) {
                String toAdd = lowestAlt / 100 >= TerminalControl.radarScreen.transLvl ? "FL" + lowestAlt / 100 : Integer.toString(lowestAlt);
                alts.add(toAdd);
                if ("TCOO".equals(icao) && lowestAlt == 3000) alts.add("3500");
                if ((("TCHH".equals(icao) && selectedAircraft.getSidStar().getRunways().contains("25R", false)) || "TCHX".equals(icao)) && lowestAlt == 4000) alts.add("4500");
                lowestAlt += 1000;
            }
        }
        valueBox.setItems(alts);
        valueBox.setSelected(clearedAlt / 100 >= TerminalControl.radarScreen.transLvl ? "FL" + clearedAlt / 100 : Integer.toString(clearedAlt));
        notListening = false;
    }

    @Override
    public void compareWithAC() {
        altModeChanged = !altMode.equals(selectedAircraft.getNavState().getLastDispModeString(NavState.ALTITUDE));
        altChanged = clearedAlt != selectedAircraft.getNavState().getClearedAlt().last();

        tabChanged = altModeChanged || altChanged;
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

        super.updateElementColours();
        notListening = false;
    }

    @Override
    public void updateMode() {
        if (selectedAircraft == null) return;
        if (selectedAircraft.getNavState() == null) return;
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
        altMode = selectedAircraft.getNavState().getLastDispModeString(NavState.ALTITUDE);
        altModeChanged = false;
        clearedAlt = selectedAircraft.getNavState().getClearedAlt().last();
        altChanged = false;
    }

    @Override
    public void getChoices() {
        altMode = settingsBox.getSelected();
        clearedAlt = valueBox.getSelected().contains("FL") ? Integer.parseInt(valueBox.getSelected().substring(2)) * 100 : Integer.parseInt(valueBox.getSelected());
    }

    public boolean isAltChanged() {
        return altChanged;
    }
}
