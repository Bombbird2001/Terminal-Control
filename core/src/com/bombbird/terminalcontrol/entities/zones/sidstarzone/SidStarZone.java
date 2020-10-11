package com.bombbird.terminalcontrol.entities.zones.sidstarzone;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.runways.Runway;
import com.bombbird.terminalcontrol.entities.sidstar.Route;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class SidStarZone {
    private final Route route;
    private final Array<Polygon> polygons;

    public SidStarZone(Route route) {
        this.route = route;
        polygons = new Array<>();
    }

    /** Returns the required polygon given the starting position, track (direction) and dist (length) */
    private Polygon calculatePolygon(float posX, float posY, float track, float dist, float extraNm) {
        float extraLengthPx = MathTools.nmToPixel(extraNm);
        float leftX = posX - extraLengthPx;
        float rightX = leftX + dist + 2 * extraLengthPx;
        float topY = posY + extraLengthPx;
        float bottomY = posY - extraLengthPx;
        Polygon polygon = new Polygon(new float[] {leftX, topY, rightX, topY, rightX, bottomY, leftX, bottomY});
        polygon.setOrigin(posX, posY);
        polygon.setRotation(track);
        return polygon;
    }

    /** Calculates all route polygons */
    public void calculatePolygons(int lastWpt) {
        for (int i = 0; i < route.getWaypoints().size; i++) {
            Waypoint wpt1 = route.getWaypoint(i);
            if (!wpt1.isInsideRadar()) continue;
            if (i + 1 < route.getWaypoints().size) {
                Waypoint wpt2 = route.getWaypoint(i + 1);
                Vector2 routeVector = new Vector2(wpt2.getPosX() - wpt1.getPosX(), wpt2.getPosY() - wpt1.getPosY());
                polygons.add(calculatePolygon(wpt1.getPosX(), wpt1.getPosY(), routeVector.angle(), routeVector.len(), 2));
            }

            if (i == lastWpt) {
                //Additionally calculates the polygon for inbound STAR/outbound SID segments - heading is from waypoint
                float outboundTrack = route.getHeading() - TerminalControl.radarScreen.magHdgDev;
                float dist = MathTools.distanceFromBorder(new float[] {1260, 4500}, new float[] {0, 3240}, wpt1.getPosX(), wpt1.getPosY(), outboundTrack);
                polygons.add(calculatePolygon(wpt1.getPosX(), wpt1.getPosY(), 90 - outboundTrack, dist, 2));
            }
        }
    }

    /** Calculates the runway polygons for departures */
    public void calculateDepRwyPolygons(Runway runway, Sid sid, int climbRate) {
        polygons.add(calculatePolygon(runway.getX(), runway.getY(), 90 - runway.getTrueHdg(), MathTools.feetToPixel(runway.getFeetLength()), 4));
        float oppX = runway.getOppRwy().getX();
        float oppY = runway.getOppRwy().getY();
        float wptX = route.getWaypoint(0).getPosX();
        float wptY = route.getWaypoint(0).getPosY();
        int[] initClimb = sid != null ? sid.getInitClimb(runway.getName()) : null;
        int initialClimbAlt = sid != null && initClimb != null ? initClimb[1] : -1;
        int initialClimbHdg = sid != null && initClimb != null ? initClimb[0] : -1;
        if (initialClimbHdg == -1) initialClimbHdg = runway.getHeading();
        if (initialClimbAlt != -1 && initialClimbAlt - runway.getElevation() > 800) {
            //Give some distance for aircraft to climb, in px
            float climbDist = MathTools.nmToPixel((initialClimbAlt - runway.getElevation()) / 60f / climbRate * 220); //Assume 220 knots climb speed on average
            float track = 90 - (initialClimbHdg - TerminalControl.radarScreen.magHdgDev);
            polygons.add(calculatePolygon(oppX, oppY, track, climbDist, 3));
            Vector2 intermediateVector = new Vector2(climbDist, 0);
            intermediateVector.rotate(track);
            intermediateVector.add(oppX, oppY);
            Vector2 wptVector = new Vector2(wptX, wptY);
            Vector2 intermediateToWpt = wptVector.sub(intermediateVector);
            polygons.add(calculatePolygon(intermediateVector.x, intermediateVector.y, intermediateToWpt.angle(), intermediateToWpt.len(), 3));
        } else {
            //Go directly to first waypoint
            Vector2 vector2 = new Vector2(wptX - oppX, wptY - oppY);
            polygons.add(calculatePolygon(oppX, oppY, vector2.angle(), vector2.len(), 3));
        }
    }

    /** Checks whether supplied aircraft is within any polygon */
    public boolean contains(float x, float y) {
        for (Polygon polygon: polygons) {
            if (polygon.contains(x, y)) return true;
        }
        return false;
    }

    public Array<Polygon> getPolygons() {
        return polygons;
    }
}
