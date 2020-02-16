package com.bombbird.terminalcontrol.entities.zones;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

import java.util.HashMap;

public class AltitudeExclusionZone {
    private boolean active;
    private Array<String> runways;

    private Polygon polygon;

    //All dimensions in nautical miles
    public AltitudeExclusionZone(String[] rwys, float xMid, float yMid, int apchHdg, float length, float width) {
        runways = new Array<String>(rwys);

        float angle = 90 - apchHdg + TerminalControl.radarScreen.magHdgDev;
        float[] coords = new float[8];
        float lengthPx = MathTools.nmToPixel(length);
        float widthPx = MathTools.nmToPixel(width);

        //Front left
        angle -= 90;
        coords[0] = xMid - widthPx * MathUtils.cosDeg(angle) / 2;
        coords[1] = yMid - widthPx * MathUtils.sinDeg(angle) / 2;
        //Front right
        coords[6] = xMid + widthPx * MathUtils.cosDeg(angle) / 2;
        coords[7] = yMid + widthPx * MathUtils.sinDeg(angle) / 2;
        //Back left
        angle -= 90;
        float xOffset = lengthPx * MathUtils.cosDeg(angle);
        float yOffset = lengthPx * MathUtils.sinDeg(angle);
        coords[2] = coords[0] + xOffset;
        coords[3] = coords[1] + yOffset;
        //Back right
        coords[4] = coords[6] + xOffset;
        coords[5] = coords[7] + yOffset;
        polygon = new Polygon(coords);
    }

    public void renderShape() {
        if (!active) return;
        ShapeRenderer shapeRenderer = TerminalControl.radarScreen.shapeRenderer;
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.polygon(polygon.getVertices());
    }

    public void updateStatus(HashMap<String, Runway> ldgRwys) {
        boolean found = true;
        for (int i = 0; i < runways.size; i++) {
            if (!ldgRwys.containsKey(runways.get(i))) {
                found = false;
                break;
            }
        }
        active = found;
    }

    public boolean isInside(Aircraft aircraft) {
        return polygon.contains(aircraft.getX(), aircraft.getY());
    }

    public boolean isInside(float x, float y) {
        return polygon.contains(x, y);
    }
}
