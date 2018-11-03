package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class Star extends SidStar {
    private Array<Integer> inboundHdg;
    private Array<Waypoint> holdingPoints;
    private Array<int[]> holdingInfo;

    public Star(String name, Array<String>runways, Array<Integer> inboundHdg, Array<Waypoint> waypoints, Array<int[]> restrictions) {
        super(name, runways, waypoints, restrictions);
        this.inboundHdg = inboundHdg;
    }

    public float distBetRemainPts(int nextWptIndex) {
        int currentIndex = nextWptIndex;
        float dist = 0;
        while (currentIndex < getWaypoints().size - 1) {
            dist += distBetween(currentIndex, currentIndex + 1);
            currentIndex++;
        }
        return dist;
    }

    private float distBetween(int pt1, int pt2) {
        Waypoint waypoint1 = getWaypoint(pt1);
        Waypoint waypoint2 = getWaypoint(pt2);
        return MathTools.pixelToNm(MathTools.distanceBetween(waypoint1.getPosX(), waypoint1.getPosY(), waypoint2.getPosX(), waypoint2.getPosY()));
    }

    public int getInboundHdg() {
        return inboundHdg.get(MathUtils.random(inboundHdg.size - 1));
    }
}
