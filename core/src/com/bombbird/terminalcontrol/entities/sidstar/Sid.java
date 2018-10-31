package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class Sid extends SidStar {
    private int[] initClimb;
    private Array<Integer> outboundHdg;

    public Sid(String name, Array<String> runways, int[] initClimb, Array<Integer> outboundHdg, Array<Waypoint> waypoints, Array<int[]> restrictions) {
        super(name, runways, waypoints, restrictions);
        this.initClimb = initClimb;
        this.outboundHdg = outboundHdg;
    }

    @Override
    public void drawOutbound(float previousX, float previousY, int outbound) {
        //Calculate spawn border
        if (outbound != -1) {
            int[] xBorder = {1260, 4500};
            int[] yBorder = {0, 3240};
            float outboundTrack = outbound - RadarScreen.magHdgDev;
            float xDistRight = (xBorder[1] - previousX) / MathUtils.cosDeg(90 - outboundTrack);
            float xDistLeft = (xBorder[0] - previousX) / MathUtils.cosDeg(90 - outboundTrack);
            float yDistUp = (yBorder[1] - previousY) / MathUtils.sinDeg(90 - outboundTrack);
            float yDistDown = (yBorder[0] - previousY) / MathUtils.sinDeg(90 - outboundTrack);
            float xDist = xDistRight > 0 ? xDistRight : xDistLeft;
            float yDist = yDistUp > 0 ? yDistUp : yDistDown;
            float dist = xDist > yDist ? yDist : xDist;
            float x = previousX + dist * MathUtils.cosDeg(90 - outboundTrack);
            float y = previousY + dist * MathUtils.sinDeg(90 - outboundTrack);
            GameScreen.shapeRenderer.line(previousX, previousY, x, y);
        }
    }

    public int getOutboundHdg() {
        return outboundHdg.get(MathUtils.random(outboundHdg.size - 1));
    }

    public int[] getInitClimb() {
        return initClimb;
    }
}
