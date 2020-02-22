package com.bombbird.terminalcontrol.entities.separation.trajectory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class TrajectoryStorage {
    private Array<Array<Array<PositionPoint>>> points;
    private RadarScreen radarScreen;
    private float timer;

    public TrajectoryStorage() {
        radarScreen = TerminalControl.radarScreen;

        int requiredSize = Math.max(TerminalControl.radarScreen.areaWarning, TerminalControl.radarScreen.collisionWarning) / Trajectory.INTERVAL;
        points = new Array<>(true, requiredSize);
        resetStorage();

        timer = 2.5f;
    }

    /** Clears the storage before updating with new points */
    private void resetStorage() {
        points.clear();
        for (int j = 0; j < Math.max(TerminalControl.radarScreen.areaWarning, TerminalControl.radarScreen.collisionWarning) / Trajectory.INTERVAL; j++) {
            Array<Array<PositionPoint>> altitudePositionMatrix = new Array<>();
            for (int i = 0; i < radarScreen.maxAlt / 1000; i++) {
                altitudePositionMatrix.add(new Array<>());
            }
            points.add(altitudePositionMatrix);
        }
    }

    /** Main update function, updates every 5 seconds */
    public void update() {
        timer -= Gdx.graphics.getDeltaTime();
        if (timer > 0) return;
        timer += Trajectory.INTERVAL / 2.0f;
        updateTrajPoints();
        radarScreen.areaPenetrationChecker.checkSeparation();
        radarScreen.collisionChecker.checkSeparation();
    }

    /** Calculates trajectory for all aircrafts, updates points array with new trajectory points */
    private void updateTrajPoints() {
        resetStorage();
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            aircraft.getTrajectory().calculateTrajectory();
            int timeIndex = 0;
            for (PositionPoint positionPoint: aircraft.getTrajectory().getPositionPoints()) {
                if (positionPoint.altitude / 1000 < radarScreen.maxAlt / 1000) {
                    points.get(timeIndex).get(positionPoint.altitude / 1000).add(positionPoint);
                }
                timeIndex++;
            }
        }
    }

    public Array<Array<Array<PositionPoint>>> getPoints() {
        return points;
    }
}
