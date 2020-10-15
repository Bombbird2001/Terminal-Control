package com.bombbird.terminalcontrol.entities.procedures.holding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONObject;

public class HoldingPoints {
    //Constant turn diameter
    private static final float turnDiameterNm = 3f;

    private Waypoint waypoint;
    private int[] altRestrictions;
    private int maxSpd;
    private boolean left;
    private int inboundHdg;
    private float legDist;
    private float[] oppPoint;

    private RadarScreen radarScreen;
    private ShapeRenderer shapeRenderer;

    public HoldingPoints(String wpt, int[] altRestrictions, int maxSpd, boolean left, int inboundHdg, float legDist) {
        radarScreen = TerminalControl.radarScreen;
        shapeRenderer = radarScreen.shapeRenderer;

        waypoint = radarScreen.waypoints.get(wpt);
        this.altRestrictions = altRestrictions;
        this.maxSpd = maxSpd;
        this.left = left;
        this.inboundHdg = inboundHdg;
        this.legDist = legDist;

        calculateOppPoint();
    }

    public HoldingPoints(String wpt, JSONObject jo) {
        this(wpt, new int[] {jo.getInt("minAlt"), jo.getInt("maxAlt")}, jo.getInt("maxSpd"), jo.getBoolean("left"), jo.getInt("inboundHdg"), (float) jo.getDouble("legDist"));
    }

    /** Calculates the coordinates of the point opposite to the fix in the holding pattern, using the given turn diameter and leg distance */
    private void calculateOppPoint() {
        float inboundTrack = inboundHdg - radarScreen.getMagHdgDev();
        float legPxDist = MathTools.nmToPixel(legDist);

        float xOffset1 = legPxDist * MathUtils.cosDeg(270 - inboundTrack);
        float yOffset1 = legPxDist * MathUtils.sinDeg(270 - inboundTrack);
        float xOffset2 = MathTools.nmToPixel(turnDiameterNm) * MathUtils.cosDeg(-inboundTrack);
        float yOffset2 = MathTools.nmToPixel(turnDiameterNm) * MathUtils.sinDeg(-inboundTrack);

        if (left) {
            xOffset2 = -xOffset2;
            yOffset2 = -yOffset2;
        }

        oppPoint = new float[] {waypoint.getPosX() + xOffset1 + xOffset2, waypoint.getPosY() + yOffset1 + yOffset2};
    }

    /** Renders the visuals for the holding pattern */
    public void renderShape() {
        float radiusPx = MathTools.nmToPixel(turnDiameterNm / 2f);

        float track1 = inboundHdg - radarScreen.getMagHdgDev();
        track1 += left ? -90 : 90;
        float[] midpoint1 = new float[] {waypoint.getPosX() + radiusPx * MathUtils.cosDeg(90 - track1), waypoint.getPosY() + radiusPx * MathUtils.sinDeg(90 - track1)};
        float[] end1 = new float[] {waypoint.getPosX() + 2 * radiusPx * MathUtils.cosDeg(90 - track1), waypoint.getPosY() + 2 * radiusPx * MathUtils.sinDeg(90 - track1)};

        float track2 = track1 + 180;
        float[] midpoint2 = new float[] {oppPoint[0] + radiusPx * MathUtils.cosDeg(90 - track2), oppPoint[1] + radiusPx * MathUtils.sinDeg(90 - track2)};
        float[] end2 = new float[] {oppPoint[0] + 2 * radiusPx * MathUtils.cosDeg(90 - track2), oppPoint[1] + 2 * radiusPx * MathUtils.sinDeg(90 - track2)};

        shapeRenderer.arc(midpoint1[0], midpoint1[1], radiusPx, 270 - (track1 + (left ? 0 : -180)), 180);
        shapeRenderer.arc(midpoint2[0], midpoint2[1], radiusPx, 270 - (track2 + (left ? 0 : -180)), 180);

        shapeRenderer.line(waypoint.getPosX(), waypoint.getPosY(), end2[0], end2[1]);
        shapeRenderer.line(end1[0], end1[1], oppPoint[0], oppPoint[1]);

        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.line(waypoint.getPosX(), waypoint.getPosY(), end1[0], end1[1]);
        shapeRenderer.line(oppPoint[0], oppPoint[1], end2[0], end2[1]);
    }

    public int getEntryProc(double heading) {
        //Offset is relative to opposite of inbound heading
        double offset = heading - inboundHdg + 180;
        if (offset < -180) {
            offset += 360;
        } else if (offset > 180) {
            offset -= 360;
        }
        if (!left) {
            if (offset > -1 && offset < 129) {
                return 1;
            } else if (offset < -1 && offset > -69) {
                return 2;
            } else {
                return 3;
            }
        } else {
            if (offset < 1 && offset > -129) {
                return 1;
            } else if (offset > 1 && offset < 69) {
                return 2;
            } else {
                return 3;
            }
        }
    }

    public Waypoint getWaypoint() {
        return waypoint;
    }

    public int[] getAltRestrictions() {
        return altRestrictions;
    }

    public int getMaxSpd() {
        return maxSpd;
    }

    public boolean isLeft() {
        return left;
    }

    public int getInboundHdg() {
        return inboundHdg;
    }

    public float getLegDist() {
        return legDist;
    }

    public float[] getOppPoint() {
        return oppPoint;
    }
}
