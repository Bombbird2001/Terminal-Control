package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.aircrafts.NavState;
import com.bombbird.terminalcontrol.ui.Ui;

public class SpdTab extends Tab {

    private boolean spdModeChanged;
    private boolean spdChanged;
    private final Array<String> spds;

    public SpdTab(Ui ui) {
        super(ui);
        spds = new Array<>();
    }

    public void loadModes() {
        modeButtons.addButton(NavState.SID_STAR_RESTR, "SID/STAR restrictions");
        modeButtons.addButton(NavState.NO_RESTR, "Unrestricted");
    }

    public void updateModeButtons() {
        modeButtons.changeButtonText(NavState.SID_STAR_RESTR, selectedAircraft instanceof Arrival ? "STAR restrictions" : "SID restrictions");
        modeButtons.setButtonColour(false);
    }

    @Override
    public void updateElements() {
        if (selectedAircraft == null) return;
        notListening = true;
        modeButtons.updateButtonActivity(selectedAircraft.getNavState().getSpdModes());
        if (visible) {
            valueBox.setVisible(true);
        }
        spds.clear();
        int lowestSpd;
        int highestSpd = -1;
        if (spdMode == NavState.SID_STAR_RESTR) {
            //Set spd restrictions in box
            if (latMode == NavState.HOLD_AT && selectedAircraft.isHolding()) {
                highestSpd = selectedAircraft.getRoute().getHoldProcedure().getMaxSpdAtWpt(selectedAircraft.getHoldWpt());
                if (highestSpd == -1) highestSpd = 250;
            } else if (clearedWpt != null) {
                highestSpd = selectedAircraft.getMaxWptSpd(clearedWpt);
            }
        }
        if (highestSpd == -1) {
            if (selectedAircraft.getAltitude() >= 9900) {
                highestSpd = selectedAircraft.getClimbSpd();
            } else if (spdMode == NavState.NO_RESTR && selectedAircraft.getRequest() == Departure.HIGH_SPEED_REQUEST && selectedAircraft.isRequested()) {
                highestSpd = selectedAircraft.getClimbSpd();
            } else {
                highestSpd = 250;
            }
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
            while (spdTracker <= (Math.min(highestSpd, 250))) {
                spds.add(Integer.toString(spdTracker));
                spdTracker += 10;
            }
        } else {
            while (lowestSpd <= (Math.min(highestSpd, 250))) {
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
        spdModeChanged = spdMode != selectedAircraft.getNavState().getDispSpdMode().last();
        spdChanged = clearedSpd != selectedAircraft.getNavState().getClearedSpd().last();

        tabChanged = spdModeChanged || spdChanged;
    }

    @Override
    public void updateElementColours() {
        notListening = true;
        modeButtons.setButtonColour(spdModeChanged);

        //Spd box colour
        if (spdChanged) {
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
        selectedAircraft.getNavState().sendSpd(spdMode, clearedSpd);
    }

    @Override
    public void getACState() {
        spdMode = selectedAircraft.getNavState().getDispSpdMode().last();
        modeButtons.setMode(spdMode);
        spdModeChanged = false;
        clearedSpd = selectedAircraft.getNavState().getClearedSpd().last();
        spdChanged = false;
    }

    @Override
    public void getChoices() {
        spdMode = modeButtons.getMode();
        clearedSpd = Integer.parseInt(valueBox.getSelected());
    }

    public boolean isSpdChanged() {
        return spdChanged;
    }
}
