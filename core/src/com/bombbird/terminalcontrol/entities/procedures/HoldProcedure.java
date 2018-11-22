package com.bombbird.terminalcontrol.entities.procedures;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class HoldProcedure {
    //Constant turn diameter
    private static final float turnDiameterNm = 3f;

    private String name;
    private Airport airport;
    private Array<Waypoint> waypoints;
    private Array<int[]> altRestrictions;
    private Array<Integer> maxSpd;
    private Array<Boolean> left; //If false then direction is right
    private Array<Integer> entryProcedure;
    private Array<Integer> inboundHdg;
    private Array<Integer> legDist;
    private Array<float[]> oppPoint;

    private RadarScreen radarScreen;
    private ShapeRenderer shapeRenderer;

    public HoldProcedure(String name, Airport airport, String info) {
        radarScreen = TerminalControl.radarScreen;
        shapeRenderer = radarScreen.shapeRenderer;

        this.name = name;
        this.airport = airport;
        waypoints = new Array<Waypoint>();
        altRestrictions = new Array<int[]>();
        maxSpd = new Array<Integer>();
        left = new Array<Boolean>();
        entryProcedure = new Array<Integer>();
        inboundHdg = new Array<Integer>();
        legDist = new Array<Integer>();

        parseInfo(info);

        calculateOppPoint();
    }

    /** Parses input string into the relevant information for the holding procedure */
    private void parseInfo(String info) {
        for (String indivWpt: info.split(",")) {
            //For each individual holding waypoint specified
            int index = 0;
            int[] altRest = new int[2];
            for (String wptInfo: indivWpt.split(" ")) {
                switch (index) {
                    case 0: waypoints.add(radarScreen.waypoints.get(wptInfo)); break;
                    case 1: altRest[0] = Integer.parseInt(wptInfo); break;
                    case 2: altRest[1] = Integer.parseInt(wptInfo); break;
                    case 3: maxSpd.add(Integer.parseInt(wptInfo)); break;
                    case 4: left.add(wptInfo.equals("LEFT")); break;
                    case 5: entryProcedure.add(Integer.parseInt(wptInfo)); break;
                    case 6: inboundHdg.add(Integer.parseInt(wptInfo)); break;
                    case 7: legDist.add(Integer.parseInt(wptInfo)); break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + radarScreen.mainName + "/hold" + airport.getIcao() + ".hold");
                }
                index++;
            }
            altRestrictions.add(altRest);
        }
    }

    /** Calculates the coordinates of the point opposite to the fix in the holding pattern, using the given turn diameter and leg distance */
    private void calculateOppPoint() {
        oppPoint = new Array<float[]>();

        int index = 0;
        for (Waypoint waypoint: waypoints) {
            float inboundTrack = inboundHdg.get(index) - radarScreen.magHdgDev;
            float legPxDist = MathTools.nmToPixel(legDist.get(index));

            float xOffset1 = legPxDist * MathUtils.cosDeg(270 - inboundTrack);
            float yOffset1 = legPxDist * MathUtils.sinDeg(270 - inboundTrack);
            float xOffset2 = MathTools.nmToPixel(turnDiameterNm) * MathUtils.cosDeg(-inboundTrack);
            float yOffset2 = MathTools.nmToPixel(turnDiameterNm) * MathUtils.sinDeg(-inboundTrack);

            if (left.get(index)) {
                xOffset2 = -xOffset2;
                yOffset2 = -yOffset2;
            }

            oppPoint.add(new float[] {waypoint.getPosX() + xOffset1 + xOffset2, waypoint.getPosY() + yOffset1 + yOffset2});

            index++;
        }
    }

    /** Renders the visuals for the holding pattern */
    public void renderShape(Waypoint waypoint) {
        int index = waypoints.indexOf(waypoint, false);
        float radiusPx = MathTools.nmToPixel(turnDiameterNm / 2f);

        float track1 = inboundHdg.get(index) - radarScreen.magHdgDev;
        track1 += left.get(index) ? -90 : 90;
        float[] midpoint1 = new float[] {waypoint.getPosX() + radiusPx * MathUtils.cosDeg(90 - track1), waypoint.getPosY() + radiusPx * MathUtils.sinDeg(90 - track1)};
        float[] end1 = new float[] {waypoint.getPosX() + 2 * radiusPx * MathUtils.cosDeg(90 - track1), waypoint.getPosY() + 2 * radiusPx * MathUtils.sinDeg(90 - track1)};

        float track2 = track1 + 180;
        float[] midpoint2 = new float[] {oppPoint.get(index)[0] + radiusPx * MathUtils.cosDeg(90 - track2), oppPoint.get(index)[1] + radiusPx * MathUtils.sinDeg(90 - track2)};
        float[] end2 = new float[] {oppPoint.get(index)[0] + 2 * radiusPx * MathUtils.cosDeg(90 - track2), oppPoint.get(index)[1] + 2 * radiusPx * MathUtils.sinDeg(90 - track2)};

        shapeRenderer.arc(midpoint1[0], midpoint1[1], radiusPx, 270 - (track1 + (left.get(index) ? 0 : -180)), 180);
        shapeRenderer.arc(midpoint2[0], midpoint2[1], radiusPx, 270 - (track2 + (left.get(index) ? 0 : -180)), 180);

        shapeRenderer.line(waypoint.getPosX(), waypoint.getPosY(), end2[0], end2[1]);
        shapeRenderer.line(end1[0], end1[1], oppPoint.get(index)[0], oppPoint.get(index)[1]);

        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.line(waypoint.getPosX(), waypoint.getPosY(), end1[0], end1[1]);
        shapeRenderer.line(oppPoint.get(index)[0], oppPoint.get(index)[1], end2[0], end2[1]);
    }

    public Array<Waypoint> getWaypoints() {
        return waypoints;
    }

    public int getMaxSpdAtWpt(Waypoint waypoint) {
        return maxSpd.get(waypoints.indexOf(waypoint, false));
    }

    public int getEntryProcAtWpt(Waypoint waypoint) {
        return entryProcedure.get(waypoints.indexOf(waypoint, false));
    }

    public int getInboundHdgAtWpt(Waypoint waypoint) {
        return inboundHdg.get(waypoints.indexOf(waypoint, false));
    }

    public int getLegDistAtWpt(Waypoint waypoint) {
        return legDist.get(waypoints.indexOf(waypoint, false));
    }

    public float[] getOppPtAtWpt(Waypoint waypoint) {
        return oppPoint.get(waypoints.indexOf(waypoint, false));
    }

    public boolean isLeftAtWpt(Waypoint waypoint) {
        return left.get(waypoints.indexOf(waypoint, false));
    }

    public int[] getAltRestAtWpt(Waypoint waypoint) {
        return altRestrictions.get(waypoints.indexOf(waypoint, false));
    }
}
