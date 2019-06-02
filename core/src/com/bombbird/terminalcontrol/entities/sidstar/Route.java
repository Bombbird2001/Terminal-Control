package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONObject;

public class Route {
    private RadarScreen radarScreen;

    private Array<Waypoint> wpts;
    private Array<int[]> restrictions;
    private Array<Boolean> flyOver;

    private Route() {
        radarScreen = TerminalControl.radarScreen;

        wpts = new Array<Waypoint>();
        restrictions = new Array<int[]>();
        flyOver = new Array<Boolean>();
    }

    public Route(Aircraft aircraft, Star star) {
        this();
        Array<String> inbound = star.getRandomInbound();
        for (int i = 0; i < inbound.size; i++) {
            if (inbound.get(0).split(" ")[0].equals("HDG")) {
                aircraft.setHeading(Integer.parseInt(inbound.get(0).split(" ")[1]));
            } else {
                String[] data = inbound.get(i).split(" ");
                wpts.add(radarScreen.waypoints.get(data[0]));
                restrictions.add(new int[] {Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])});
                flyOver.add(data.length > 4 && data[4].equals("FO"));
            }
        }

        wpts.addAll(star.getWaypoints());
        restrictions.addAll(star.getRestrictions());
        flyOver.addAll(star.getFlyOver());

        String runway = null;
        for (int i = 0; i < star.getRunways().size; i++) {
            String rwy = star.getRunways().get(i);
            if (aircraft.getAirport().getLandingRunways().containsKey(rwy)) {
                runway = rwy;
                break;
            }
        }

        if (runway == null) throw new RuntimeException("Runway selected is null");

        wpts.addAll(star.getRwyWpts(runway));
        restrictions.addAll(star.getRwyRestrictions(runway));
        flyOver.addAll(star.getRwyFlyOver(runway));
    }

    public Route(Aircraft aircraft, Sid sid, String runway) {
        this();
        wpts.addAll(sid.getInitWpts(runway));
        restrictions.addAll(sid.getInitRestrictions(runway));
        flyOver.addAll(sid.getInitFlyOver(runway));
        wpts.addAll(sid.getWaypoints());
        restrictions.addAll(sid.getRestrictions());
        flyOver.addAll(sid.getFlyOver());
        Array<String> transition = sid.getRandomTransition();
        for (int i = 0; i < transition.size; i++) {
            String[] data = transition.get(i).split(" ");
            if (data[0].equals("WPT")) {
                //Waypoint
                wpts.add(TerminalControl.radarScreen.waypoints.get(data[1]));
                restrictions.add(new int[] {Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4])});
                flyOver.add(data.length > 5 && data[5].equals("FO"));
            } else {
                //Outbound heading
                ((Departure) aircraft).setOutboundHdg(Integer.parseInt(data[1]));
            }
        }
    }

    public Route(Aircraft aircraft, JSONObject jo) {
        //TODO Load route
        this();
    }

    public void joinLines(int start, int end, int outbound) {
        Waypoint prevPt = null;
        int index = start;
        while (index < end) {
            Waypoint waypoint = getWaypoint(index);
            if (prevPt != null) {
                radarScreen.shapeRenderer.line(prevPt.getPosX(), prevPt.getPosY(), waypoint.getPosX(), waypoint.getPosY());
            }
            prevPt = waypoint;
            index++;
        }
        if (prevPt != null) {
            drawOutbound(prevPt.getPosX(), prevPt.getPosY(), outbound);
        }
    }

    private void drawOutbound(float previousX, float previousY, int outbound) {
        if (outbound != -1 && previousX <= 4500 && previousX >= 1260 && previousY <= 3240 && previousY >= 0) {
            float outboundTrack = outbound - radarScreen.magHdgDev;
            float[] point = MathTools.pointsAtBorder(new float[] {1260, 4500}, new float[] {0, 3240}, previousX, previousY, outboundTrack);
            radarScreen.shapeRenderer.line(previousX, previousY, point[0], point[1]);
        }
    }

    public Array<Waypoint> getWaypoints() {
        return wpts;
    }

    public Waypoint getWaypoint(int index) {
        if (index >= wpts.size) {
            return null;
        }
        return wpts.get(index);
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

    public float distBetween(int pt1, int pt2) {
        Waypoint waypoint1 = getWaypoint(pt1);
        Waypoint waypoint2 = getWaypoint(pt2);
        return MathTools.pixelToNm(MathTools.distanceBetween(waypoint1.getPosX(), waypoint1.getPosY(), waypoint2.getPosX(), waypoint2.getPosY()));
    }

    public Array<Waypoint> getRemainingWaypoints(int start, int end) {
        //Returns array of waypoints starting from index
        Array<Waypoint> newRange = new Array<Waypoint>(wpts);
        if (end >= start) {
            if (start > 0) {
                newRange.removeRange(0, start - 1);
            }
            int newEnd = end - start;
            if (newEnd < newRange.size - 1) {
                newRange.removeRange(newEnd + 1, newRange.size - 1);
            }
        }
        return newRange;
    }

    public int findWptIndex(String wptName) {
        return wpts.indexOf(radarScreen.waypoints.get(wptName), false);
    }

    public int getWptMinAlt(String wptName) {
        return restrictions.get(findWptIndex(wptName))[0];
    }

    public int getWptMaxAlt(String wptName) {
        return restrictions.get(findWptIndex(wptName))[1];
    }

    public int getWptMaxSpd(String wptName) {
        return restrictions.get(findWptIndex(wptName))[2];
    }
}
