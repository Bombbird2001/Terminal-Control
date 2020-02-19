package com.bombbird.terminalcontrol.entities.separation;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.separation.trajectory.PositionPoint;
import com.bombbird.terminalcontrol.entities.separation.trajectory.Trajectory;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class CollisionChecker {
    private RadarScreen radarScreen;
    private Array<Aircraft[]> aircraftStorage;
    private Array<PositionPoint[]> pointStorage;

    public CollisionChecker() {
        radarScreen = TerminalControl.radarScreen;
        aircraftStorage = new Array<>();
        pointStorage = new Array<>();
    }

    /** Checks separation between trajectory points of same timing */
    public void checkSeparation() {
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            aircraft.setTrajectoryConflict(false);
        }
        aircraftStorage.clear();
        pointStorage.clear();
        for (int a = Trajectory.INTERVAL; a <= radarScreen.collisionWarning; a += Trajectory.INTERVAL) {
            Array<Array<PositionPoint>> altitudePositionMatrix = radarScreen.trajectoryStorage.getPoints().get(a / 5 - 1);
            //Check for each separate timing
            for (int i = 0; i < altitudePositionMatrix.size; i++) {
                //Get all possible points to check
                Array<PositionPoint> checkPoints = new Array<>();
                if (i - 1 >= 0) {
                    checkPoints.addAll(altitudePositionMatrix.get(i - 1));
                }
                if (i + 1 < altitudePositionMatrix.size - 1) {
                    checkPoints.addAll(altitudePositionMatrix.get(i + 1));
                }
                checkPoints.addAll(altitudePositionMatrix.get(i));

                for (int j = 0; j < checkPoints.size; j++) {
                    PositionPoint point1 = checkPoints.get(j);
                    Aircraft aircraft1 = point1.getAircraft();
                    if (aircraft1 == null) continue;
                    for (int k = j + 1; k < checkPoints.size; k++) {
                        PositionPoint point2 = checkPoints.get(k);
                        Aircraft aircraft2 = point2.getAircraft();
                        if (aircraft2 == null) continue;

                        //Exception cases:
                        if (aircraft1.isConflict() || aircraft1.isTerrainConflict() || aircraft2.isConflict() || aircraft2.isTerrainConflict()) {
                            //If one of the aircraft is in conflict, inhibit warning for now
                            continue;
                        }

                        if (aircraft1.isTrajectoryConflict() && aircraft2.isTrajectoryConflict()) {
                            //If both aircraft have been identified as potential conflicts already
                            continue;
                        }

                        if (aircraft1.getEmergency().isActive() || aircraft2.getEmergency().isActive()) {
                            //If either plane is emergency
                            continue;
                        }

                        if (aircraft1.isOnGround() || aircraft2.isOnGround()) {
                            //If either aircraft is on the ground
                            continue;
                        }

                        if (point1.altitude < aircraft1.getAirport().getElevation() + 1400 || point2.altitude < aircraft2.getAirport().getElevation() + 1400 || (aircraft1.getAltitude() > radarScreen.maxAlt && aircraft2.getAltitude() > radarScreen.maxAlt)) {
                            //If either point is below 1400 feet or both above max alt
                            continue;
                        }

                        if (aircraft1.getIls() != null && aircraft2.getIls() != null && aircraft1.getIls().isInsideILS(aircraft1.getX(), aircraft1.getY()) && aircraft2.getIls().isInsideILS(aircraft2.getX(), aircraft2.getY())) {
                            //If both planes have captured ILS and both have captured LOC and are within at least 1 of the 2 arcs
                            continue;
                        }

                        //NOZ/NTZ/go around will not be considered for collision warning

                        float dist = MathTools.pixelToNm(MathTools.distanceBetween(point1.x, point1.y, point2.x, point2.y));

                        if (Math.abs(point1.altitude - point2.altitude) < 990 && dist < radarScreen.separationMinima + 0.2f) {
                            //Possible conflict, add to save arrays
                            aircraftStorage.add(new Aircraft[] {aircraft1, aircraft2});
                            pointStorage.add(new PositionPoint[] {point1, point2});
                            aircraft1.setTrajectoryConflict(true);
                            aircraft2.setTrajectoryConflict(true);
                            aircraft1.getDataTag().startFlash();
                            aircraft2.getDataTag().startFlash();
                        }
                    }
                }
            }
        }
    }

    /** Renders STCAS alerts */
    public void renderShape() {
        radarScreen.shapeRenderer.setColor(Color.MAGENTA);
        for (int i = 0; i < aircraftStorage.size; i++) {
            Aircraft aircraft1 = aircraftStorage.get(i)[0];
            Aircraft aircraft2 = aircraftStorage.get(i)[1];
            PositionPoint point1 = pointStorage.get(i)[0];
            PositionPoint point2 = pointStorage.get(i)[1];
            radarScreen.shapeRenderer.line(aircraft1.getRadarX(), aircraft1.getRadarY(), point1.x, point1.y);
            radarScreen.shapeRenderer.line(aircraft2.getRadarX(), aircraft2.getRadarY(), point2.x, point2.y);

            float midX = (point1.x + point2.x) / 2;
            float midY = (point1.y + point2.y) / 2;
            float halfLength = MathTools.nmToPixel((radarScreen.separationMinima + 0.2f) / 2);
            radarScreen.shapeRenderer.rect(midX - halfLength, midY - halfLength, 2 * halfLength, 2 * halfLength);
        }
    }
}
