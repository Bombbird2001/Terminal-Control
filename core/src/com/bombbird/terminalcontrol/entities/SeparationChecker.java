package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class SeparationChecker {
    private Array<Array<Aircraft>> flightLevels;

    public SeparationChecker() {
        flightLevels = new Array<Array<Aircraft>>(true, RadarScreen.maxArrAlt / 1000);
        for (int i = 0; i < RadarScreen.maxArrAlt / 1000; i++) {
            flightLevels.add(new Array<Aircraft>());
        }
    }

    /** Updates the state of aircraft separation */
    public void update() {
        for (Aircraft aircraft: RadarScreen.aircrafts.values()) {
            aircraft.setConflict(false);
        }
        checkAircraftSep();
        checkRestrSep();
    }

    /** Updates the levels each aircraft belongs to */
    public void updateAircraftPositions() {
        for (Array<Aircraft> array: flightLevels) {
            array.clear();
        }
        for (Aircraft aircraft: RadarScreen.aircrafts.values()) {
            if (((int)(aircraft.getAltitude() / 1000)) < RadarScreen.maxArrAlt / 1000) {
                flightLevels.get((int)(aircraft.getAltitude() / 1000)).add(aircraft);
            }
        }
    }

    /** Checks that each aircraft is separated from one another */
    private void checkAircraftSep() {
        for (int i = 0; i < flightLevels.size; i++) {
            //Get all the possible planes to check
            Array<Aircraft> planesToCheck = new Array<Aircraft>();
            if (i - 1 >= 0) {
                planesToCheck.addAll(flightLevels.get(i - 1));
            }
            if (i + 1 < flightLevels.size - 1) {
                planesToCheck.addAll(flightLevels.get(i + 1));
            }
            planesToCheck.addAll(flightLevels.get(i));

            for (int j = 0; j < planesToCheck.size; j++) {
                for (int k = j + 1; k < planesToCheck.size; k++) {
                    Aircraft plane1 = planesToCheck.get(j);
                    Aircraft plane2 = planesToCheck.get(k);
                    if (Math.abs(plane1.getAltitude() - plane2.getAltitude()) < 950 && MathTools.pixelToNm(MathTools.distanceBetween(plane1.getX(), plane1.getY(), plane2.getX(), plane2.getY())) < 3) {
                        //TODO Change separation minima depending on airport & visibility, and in future reduced separation for emergencies
                        //Aircrafts have infringed minima of 1000 feet and 3nm apart
                        plane1.setConflict(true);
                        plane2.setConflict(true);
                    }
                }
            }
        }
    }

    /** Checks that each aircraft is separated from each obstacles/restricted area */
    private void checkRestrSep() {
        for (Aircraft aircraft: RadarScreen.aircrafts.values()) {
            for (Obstacle obstacle: RadarScreen.obsArray) {
                if (aircraft.getAltitude() < obstacle.getMinAlt() && obstacle.isIn(aircraft)) {
                    aircraft.setConflict(true);
                }
            }
            for (RestrictedArea restrictedArea: RadarScreen.restArray) {
                if (aircraft.getAltitude() < restrictedArea.getMinAlt() && restrictedArea.isIn(aircraft)) {
                    aircraft.setConflict(true);
                }
            }
        }
    }
}
