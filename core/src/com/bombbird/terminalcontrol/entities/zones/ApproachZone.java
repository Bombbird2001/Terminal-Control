package com.bombbird.terminalcontrol.entities.zones;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

import java.util.HashMap;

public class ApproachZone {
    private boolean active;
    private final String rwy1;
    private final String rwy2;

    private final Polygon noz1;
    private final Polygon noz2;
    private final Polygon ntz;

    //All dimensions in nautical miles
    public ApproachZone(String rwy1, String rwy2, float xMid, float yMid, int apchHdg, float nozWidth, float nozLength, float ntzWidth) {
        this.rwy1 = rwy1;
        this.rwy2 = rwy2;

        float angle = 90 - apchHdg + TerminalControl.radarScreen.magHdgDev;
        float[] ntzCoord = new float[8];
        float ntzWidthPx = MathTools.nmToPixel(ntzWidth);
        float nozLengthPx = MathTools.nmToPixel(nozLength);
        float nozWidthPx = MathTools.nmToPixel(nozWidth);
        //NTZ
        //Front left
        angle -= 90;
        ntzCoord[0] = xMid - ntzWidthPx * MathUtils.cosDeg(angle) / 2;
        ntzCoord[1] = yMid - ntzWidthPx * MathUtils.sinDeg(angle) / 2;
        //Front right
        ntzCoord[6] = xMid + ntzWidthPx * MathUtils.cosDeg(angle) / 2;
        ntzCoord[7] = yMid + ntzWidthPx * MathUtils.sinDeg(angle) / 2;
        //Back left
        angle -= 90;
        float xOffset = nozLengthPx * MathUtils.cosDeg(angle);
        float yOffset = nozLengthPx * MathUtils.sinDeg(angle);
        ntzCoord[2] = ntzCoord[0] + xOffset;
        ntzCoord[3] = ntzCoord[1] + yOffset;
        //Back right
        ntzCoord[4] = ntzCoord[6] + xOffset;
        ntzCoord[5] = ntzCoord[7] + yOffset;
        ntz = new Polygon(ntzCoord);

        //Left NOZ
        float[] noz1Coord = new float[8];
        //Front left
        angle += 90;
        noz1Coord[0] = xMid - nozWidthPx * MathUtils.cosDeg(angle);
        noz1Coord[1] = yMid - nozWidthPx * MathUtils.sinDeg(angle);
        //Front right
        noz1Coord[6] = ntzCoord[0];
        noz1Coord[7] = ntzCoord[1];
        //Back left
        noz1Coord[2] = noz1Coord[0] + xOffset;
        noz1Coord[3] = noz1Coord[1] + yOffset;
        //Back right
        noz1Coord[4] = noz1Coord[6] + xOffset;
        noz1Coord[5] = noz1Coord[7] + yOffset;
        noz1 = new Polygon(noz1Coord);

        //Right NOZ
        float[] noz2Coord = new float[8];
        //Front right
        noz2Coord[0] = xMid + nozWidthPx * MathUtils.cosDeg(angle);
        noz2Coord[1] = yMid + nozWidthPx * MathUtils.sinDeg(angle);
        //Front left
        noz2Coord[6] = ntzCoord[6];
        noz2Coord[7] = ntzCoord[7];
        //Back right
        noz2Coord[2] = noz2Coord[0] + xOffset;
        noz2Coord[3] = noz2Coord[1] + yOffset;
        //Back left
        noz2Coord[4] = noz2Coord[6] + xOffset;
        noz2Coord[5] = noz2Coord[7] + yOffset;
        noz2 = new Polygon(noz2Coord);
    }

    public void renderShape() {
        if (!active) return;
        ShapeRenderer shapeRenderer = TerminalControl.radarScreen.shapeRenderer;
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.polygon(noz1.getVertices());
        shapeRenderer.polygon(noz2.getVertices());
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.polygon(ntz.getVertices());
    }

    public void updateStatus(HashMap<String, Runway> ldgRwys) {
        active = ldgRwys.containsKey(rwy1) && ldgRwys.containsKey(rwy2);
    }

    public boolean checkSeparation(Aircraft plane1, Aircraft plane2) {
        return (noz1.contains(plane1.getX(), plane1.getY()) && noz2.contains(plane2.getX(), plane2.getY())) || (noz1.contains(plane2.getX(), plane2.getY()) && noz2.contains(plane1.getX(), plane1.getY()));
    }
}
