package com.bombbird.terminalcontrol.ui;

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class RequestFlasher {
    private RadarScreen radarScreen;

    public RequestFlasher(RadarScreen radarScreen) {
        this.radarScreen = radarScreen;
    }

    public void update() {
        //float cameraX = radarScreen.camera.position.x;
        //float cameraY = radarScreen.camera.position.y;
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (aircraft.isActionRequired()) {

            }
        }
    }
}
