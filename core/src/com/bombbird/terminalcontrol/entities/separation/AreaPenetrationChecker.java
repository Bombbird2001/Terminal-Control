package com.bombbird.terminalcontrol.entities.separation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.NavState;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle;
import com.bombbird.terminalcontrol.entities.separation.trajectory.PositionPoint;
import com.bombbird.terminalcontrol.entities.separation.trajectory.Trajectory;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class AreaPenetrationChecker {
    private final RadarScreen radarScreen;
    private final Array<Aircraft> aircraftStorage;
    private final Array<PositionPoint> pointStorage;

    public AreaPenetrationChecker() {
        radarScreen = TerminalControl.radarScreen;
        aircraftStorage = new Array<>();
        pointStorage = new Array<>();
    }

    /** Checks separation between points and terrain */
    public void checkSeparation() {
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            aircraft.setTrajectoryTerrainConflict(false);
        }
        aircraftStorage.clear();
        pointStorage.clear();
        for (int i = Trajectory.INTERVAL; i <= radarScreen.areaWarning; i += Trajectory.INTERVAL) {
            //For each trajectory timing
            for (Obstacle obstacle: radarScreen.obsArray) {
                //Check only the array levels below obstacle height
                int requiredArrays = Math.min(obstacle.getMinAlt() / 1000, radarScreen.maxAlt / 1000 - 1);
                for (int j = 0; j < requiredArrays; j++) {
                    for (PositionPoint positionPoint: radarScreen.trajectoryStorage.getPoints().get(i / 5 - 1).get(j)) {
                        Aircraft aircraft = positionPoint.getAircraft();

                        //Exception cases
                        if (aircraft.isConflict() || aircraft.isTerrainConflict()) {
                            //If aircraft is already in conflict, inhibit warning for now
                            continue;
                        }

                        if (aircraft.isTrajectoryTerrainConflict()) {
                            //If aircraft is already predicted to conflict with terrain
                            continue;
                        }

                        if ((aircraft.getNavState().getDispLatMode().first() == NavState.SID_STAR || (aircraft.getIls() != null && aircraft.getIls().isInsideILS(positionPoint.x, positionPoint.y))) && !obstacle.isEnforced() && aircraft.getRoute().inSidStarZone(positionPoint.x, positionPoint.y, aircraft.getAltitude())) {
                            //If latMode is STAR/SID or point is within localizer range, is within sidStarZone and obstacle is not a restricted area, ignore
                            continue;
                        }

                        if (aircraft.getNavState().getClearedIls().first() != null) {
                            //If aircraft is cleared for ILS approach, inhibit to prevent nuisance warnings
                            continue;
                        }

                        if (aircraft.isOnGround() || aircraft.isGsCap() || (aircraft instanceof Arrival && aircraft.getIls() instanceof LDA && aircraft.isLocCap()) || (aircraft instanceof Arrival && aircraft.getIls() != null && aircraft.getIls().getName().contains("IMG")) ||
                                aircraft.isGoAroundWindow()) {
                            //Suppress terrain warnings if aircraft is already on the ILS's GS or is on the NPA, or is on the ground, or is on the imaginary ILS for LDA (if has not captured its GS yet), or just did a go around
                            continue;
                        }

                        if (positionPoint.altitude < obstacle.getMinAlt() - 50 && obstacle.isIn(positionPoint.x, positionPoint.y)) {
                            //Possible conflict, add to save arrays
                            aircraftStorage.add(aircraft);
                            pointStorage.add(positionPoint);
                            aircraft.setTrajectoryTerrainConflict(true);
                            aircraft.getDataTag().startFlash();
                        }
                    }
                }
            }
        }
    }

    /** Renders APW alerts */
    public void renderShape() {
        radarScreen.shapeRenderer.setColor(Color.RED);
        for (int i = 0; i < aircraftStorage.size; i++) {
            Aircraft aircraft = aircraftStorage.get(i);
            float x = pointStorage.get(i).x;
            float y = pointStorage.get(i).y;
            radarScreen.shapeRenderer.line(aircraft.getRadarX(), aircraft.getRadarY(), x, y);
            float halfLength = MathTools.nmToPixel(1.5f);
            radarScreen.shapeRenderer.rect(x - halfLength, y - halfLength, halfLength * 2, halfLength * 2);
        }
    }
}
