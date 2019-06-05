package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.ui.Ui;

public class SpdTab extends Tab {

    private boolean spdModeChanged;
    private boolean spdChanged;
    private Array<String> spds;

    public SpdTab(Ui ui) {
        super(ui);
        spds = new Array<String>();
    }

    @Override
    public void updateElements() {
        notListening = true;
        settingsBox.setItems(selectedAircraft.getNavState().getSpdModes());
        settingsBox.setSelected(spdMode);
        if (visible) {
            valueBox.setVisible(true);
        }
        spds.clear();
        int lowestSpd;
        int highestSpd = -1;
        if (spdMode.contains("SID") || spdMode.contains("STAR")) {
            //Set spd restrictions in box
            if ("Hold at".equals(latMode) && selectedAircraft.isHolding()) {
                highestSpd = selectedAircraft.getRoute().getHoldProcedure().getMaxSpdAtWpt(selectedAircraft.getHoldWpt());
            } else if (clearedWpt != null) {
                highestSpd = selectedAircraft.getMaxWptSpd(clearedWpt);
            }
        }
        if (highestSpd == -1) {
            highestSpd = selectedAircraft.getAltitude() >= 10000 ? selectedAircraft.getClimbSpd() : 250;
        }
        if (selectedAircraft instanceof Departure) {
            lowestSpd = 200;
        } else if (selectedAircraft instanceof Arrival) {
            lowestSpd = 160;
            if (selectedAircraft.getIls() != null && selectedAircraft.isLocCap()) {
                lowestSpd = selectedAircraft.getApchSpd();
            } else if (selectedAircraft.getApchSpd() > lowestSpd) {
                while (selectedAircraft.getApchSpd() > lowestSpd) {
                    lowestSpd += 10;
                }
            }
        } else {
            lowestSpd = 0;
            Gdx.app.log("Invalid aircraft type", "Aircraft not instance of departure or arrival");
        }
        clearedSpd = MathUtils.clamp(clearedSpd, lowestSpd, highestSpd);
        if (lowestSpd % 10 != 0) {
            spds.add(Integer.toString(lowestSpd));
            int spdTracker = lowestSpd + (10 - lowestSpd % 10);
            while (spdTracker <= (highestSpd > 250 ? 250 : highestSpd)) {
                spds.add(Integer.toString(spdTracker));
                spdTracker += 10;
            }
        } else {
            while (lowestSpd <= (highestSpd > 250 ? 250 : highestSpd)) {
                spds.add(Integer.toString(lowestSpd));
                lowestSpd += 10;
            }
        }
        if (highestSpd > 250) {
            spds.add(Integer.toString(highestSpd));
        }
        valueBox.setItems(spds);
        valueBox.setSelected(Integer.toString(clearedSpd));
        notListening = false;
    }

    @Override
    public void compareWithAC() {
        spdModeChanged = !spdMode.equals(selectedAircraft.getNavState().getDispSpdMode().last());
        spdChanged = clearedSpd != selectedAircraft.getNavState().getClearedSpd().last();
    }

    @Override
    public void updateElementColours() {
        notListening = true;
        //Spd mode selectbox colour
        if (spdModeChanged) {
            settingsBox.getStyle().fontColor = Color.YELLOW;
        } else {
            settingsBox.getStyle().fontColor = Color.WHITE;
        }

        //Spd box colour
        if (spdChanged) {
            valueBox.getStyle().fontColor = Color.YELLOW;
        } else {
            valueBox.getStyle().fontColor = Color.WHITE;
        }

        tabChanged = spdModeChanged || spdChanged;
        super.updateElementColours();
        notListening = false;
    }

    @Override
    public void updateMode() {
        selectedAircraft.getNavState().sendSpd(spdMode, clearedSpd);
    }

    @Override
    public void updatePaneWidth(float paneWidth) {
        notListening = true;
        super.updatePaneWidth(paneWidth);
        notListening = false;
    }

    @Override
    public void getACState() {
        spdMode = selectedAircraft.getNavState().getDispSpdMode().last();
        spdModeChanged = false;
        clearedSpd = selectedAircraft.getNavState().getClearedSpd().last();
        spdChanged = false;
    }

    @Override
    public void getChoices() {
        spdMode = settingsBox.getSelected();
        clearedSpd = Integer.parseInt(valueBox.getSelected());
    }
}
