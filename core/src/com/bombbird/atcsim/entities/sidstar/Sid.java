package com.bombbird.atcsim.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;

public class Sid extends SidStar {
    private int[] initClimb;
    private Array<Integer> outboundHdg;

    public Sid(String name, Array<String> runways, int[] initClimb, Array<Integer> outboundHdg, Array<Waypoint> waypoints, Array<int[]> restrictions) {
        super(name, runways, waypoints, restrictions);
        this.initClimb = initClimb;
        this.outboundHdg = outboundHdg;
    }

    @Override
    void drawOutbound(float previousX, float previousY, int outbound) {
        //Calculate spawn border
        if (outbound != -1) {
            int[] xBorder = {1260, 4500};
            int[] yBorder = {0, 3240};
            outbound -= RadarScreen.magHdgDev;
            float xDistRight = (xBorder[1] - previousX) / MathUtils.cosDeg((float) (90 - outbound));
            float xDistLeft = (xBorder[0] - previousX) / MathUtils.cosDeg((float) (90 - outbound));
            float yDistUp = (yBorder[1] - previousY) / MathUtils.sinDeg((float) (90 - outbound));
            float yDistDown = (yBorder[0] - previousY) / MathUtils.sinDeg((float) (90 - outbound));
            float xDist = xDistRight > 0 ? xDistRight : xDistLeft;
            float yDist = yDistUp > 0 ? yDistUp : yDistDown;
            float dist = xDist > yDist ? yDist : xDist;
            float x = previousX + dist * MathUtils.cosDeg((float) (90 - outbound));
            float y = previousY + dist * MathUtils.sinDeg((float) (90 - outbound));
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
