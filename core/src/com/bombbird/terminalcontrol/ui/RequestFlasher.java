package com.bombbird.terminalcontrol.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class RequestFlasher {
    private RadarScreen radarScreen;
    private OrthographicCamera camera;

    public RequestFlasher(RadarScreen radarScreen) {
        this.radarScreen = radarScreen;
        camera = radarScreen.camera;
    }

    public void update() {
        float ratio = (float) TerminalControl.WIDTH / TerminalControl.HEIGHT;
        float defaultRatio = 16 / 9f;
        float totalX = ratio * 3240;
        float xOffset = totalX - 5760;
        float minX = 0;
        float minY = 0;
        float maxX = 0;
        float maxY = 0;
        Vector3[] points = camera.frustum.planePoints;
        for (int i = 0; i < 4; i++) {
            if (i == 0) {
                minX = points[i].x + radarScreen.ui.getPaneWidth() * camera.zoom - xOffset / 2 * camera.zoom;
                minY = points[i].y;
            } else if (i == 2) {
                maxX = points[i].x;
                maxY = points[i].y;
                if (ratio < defaultRatio) maxX += xOffset / 2 * camera.zoom;
            }
        }
        float margin = camera.zoom * 80;
        minX += margin;
        minY += margin;
        maxX -= margin;
        maxY -= margin;

        float ctrX = (minX + maxX) / 2;
        float ctrY = (minY + maxY) / 2;
        radarScreen.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if ((aircraft.isFuelEmergency() || aircraft.isConflict() || aircraft.isTerrainConflict() || aircraft.isTrajectoryTerrainConflict() || aircraft.isTrajectoryConflict() || aircraft.isActionRequired()) && (!MathTools.withinRange(aircraft.getRadarX(), minX, maxX) || !MathTools.withinRange(aircraft.getRadarY(), minY, maxY))) {
                float deltaX = aircraft.getRadarX() - ctrX;
                float deltaY = aircraft.getRadarY() - ctrY;
                float[] indicationPoint = MathTools.pointsAtBorder(new float[] {minX, maxX}, new float[] {minY, maxY}, (minX + maxX) / 2, (minY + maxY) / 2, 90 - MathUtils.radiansToDegrees * MathUtils.atan2(deltaY, deltaX));
                Color color;
                if (System.currentTimeMillis() % 2000 >= 1000) {
                    if (aircraft.isFuelEmergency() || aircraft.isConflict() || aircraft.isTerrainConflict()) {
                        color = Color.RED;
                    } else if (aircraft.isTrajectoryTerrainConflict() || aircraft.isTrajectoryConflict()) {
                        color = Color.MAGENTA;
                    } else if (aircraft instanceof Departure) {
                        color = Color.GREEN;
                    } else {
                        color = new Color(0, 200 / 255f, 255, 1);
                    }
                    radarScreen.shapeRenderer.setColor(color);
                    int radius = Gdx.app.getType() == Application.ApplicationType.Android ? 50 : 30;
                    radarScreen.shapeRenderer.circle(indicationPoint[0], indicationPoint[1], radius * camera.zoom);
                }
            }
        }
        radarScreen.shapeRenderer.end();
    }
}
