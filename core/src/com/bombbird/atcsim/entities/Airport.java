package com.bombbird.atcsim.entities;

import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.screens.GameScreen;

public class Airport {
    private Array<Runway> activeRunways;
    private String icao;
    private String metar;

    public Airport(String icao) {
        this.icao = icao;
        activeRunways = new Array<Runway>();
        activeRunways.add(new Runway("05L", 713, 402, 12008, 54, 693, 402, true, true));
        GameScreen.stage.addActor(activeRunways.first());
        activeRunways.add(new Runway("05R", 717, 398, 12467, 54, 720, 387, true, true));
        GameScreen.stage.addActor(activeRunways.get(1));
    }

    public void renderRunways() {
        for (Runway runway: activeRunways) {
            runway.renderShape();
        }
    }
}
