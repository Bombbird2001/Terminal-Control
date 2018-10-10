package com.bombbird.atcsim.screens.Ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.entities.aircrafts.Arrival;
import com.bombbird.atcsim.entities.aircrafts.Departure;

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
        int highestSpd;
        if (spdMode.contains("SID") || spdMode.contains("STAR")) {
            //Set spd restrictions in box
            if (latMode.equals("After waypoint, fly heading")) {
                highestSpd = selectedAircraft.getMaxWptSpd(afterWpt);
            } else {
                highestSpd = selectedAircraft.getMaxWptSpd(clearedWpt);
            }
            if (highestSpd == -1) {
                highestSpd = 250;
            }
        } else {
            highestSpd = selectedAircraft.getClimbSpd();
        }
        if (selectedAircraft instanceof Departure) {
            if (selectedAircraft.getClearedIas() <= selectedAircraft.getV2()) {
                lowestSpd = selectedAircraft.getV2();
            } else {
                lowestSpd = 200;
            }
        } else if (selectedAircraft instanceof Arrival) {
            //TODO Set minimum apch spd for ILS
            //lowestSpd = selectedAircraft.getApchSpd();
            lowestSpd = 160;
        } else {
            lowestSpd = 0;
            Gdx.app.log("Invalid aircraft type", "Aircraft not instance of departure or arrival");
        }
        if (lowestSpd % 10 != 0) {
            spds.add(Integer.toString(lowestSpd));
            int spdTracker = lowestSpd + (10 - lowestSpd % 10);
            while (spdTracker <= 250) {
                spds.add(Integer.toString(spdTracker));
                spdTracker += 10;
            }
        } else {
            while (lowestSpd <= 250) {
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
        spdModeChanged = !spdMode.equals(selectedAircraft.getNavState().getSpdMode());
        spdChanged = !(clearedSpd == selectedAircraft.getClearedIas());
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
        selectedAircraft.getNavState().setSpdMode(spdMode);
        selectedAircraft.setClearedIas(clearedSpd);
        selectedAircraft.setTargetIas(clearedSpd);
    }

    @Override
    public void updatePaneWidth(float paneWidth) {
        notListening = true;
        super.updatePaneWidth(paneWidth);
        notListening = false;
    }

    @Override
    public void getACState() {
        spdMode = selectedAircraft.getNavState().getSpdMode();
        spdModeChanged = false;
        clearedSpd = selectedAircraft.getClearedIas();
        spdChanged = false;
    }

    @Override
    public void getChoices() {
        spdMode = settingsBox.getSelected();
        clearedSpd = Integer.parseInt(valueBox.getSelected());
    }
}
