package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Waypoint;

public class Star extends SidStar {
    private Array<Integer> inboundHdg;
    private Array<Waypoint> holdingPoints;
    private Array<int[]> holdingInfo;

    public Star(String name, Array<String>runways, Array<Integer> inboundHdg, Array<Waypoint> waypoints, Array<int[]> restrictions, Array<Waypoint> holdingPoints, Array<int[]> holdingInfo) {
        super(name, runways, waypoints, restrictions);
        this.inboundHdg = inboundHdg;
        this.holdingPoints = holdingPoints;
        this.holdingInfo = holdingInfo;
    }

    public int getInboundHdg() {
        return inboundHdg.get(MathUtils.random(inboundHdg.size - 1));
    }
}
