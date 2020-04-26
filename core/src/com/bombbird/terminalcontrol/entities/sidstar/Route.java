package com.bombbird.terminalcontrol.entities.sidstar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.procedures.holding.HoldProcedure;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.json.JSONArray;
import org.json.JSONObject;

public class Route {
    private final RadarScreen radarScreen;

    private final Array<Waypoint> wpts;
    private final Array<int[]> restrictions;
    private final Array<Boolean> flyOver;
    private HoldProcedure holdProcedure;

    private String name;

    /** Basic constructor for Route, should only be called by other Route constructors */
    private Route() {
        radarScreen = TerminalControl.radarScreen;

        wpts = new Array<>();
        restrictions = new Array<>();
        flyOver = new Array<>();
    }

    /** Create a new Route based on STAR, for compatibility with older versions */
    public Route(Star star) {
        this();
        Array<String> inbound = star.getRandomInbound();
        if (!"HDG".equals(inbound.get(0).split(" ")[0])) {
            String[] data = inbound.get(0).split(" ");
            wpts.add(radarScreen.waypoints.get(data[1]));
            restrictions.add(new int[] {Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4])});
            flyOver.add(data.length > 5 && data[5].equals("FO"));
        }

        wpts.addAll(star.getWaypoints());
        restrictions.addAll(star.getRestrictions());
        flyOver.addAll(star.getFlyOver());

        holdProcedure = new HoldProcedure(star);

        name = star.getName();
    }

    /** Create a new Route based on SID, for compatibility with older versions */
    public Route(Sid sid, String runway) {
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
            }
        }

        holdProcedure = new HoldProcedure();

        name = sid.getName();
    }

    /** Create new Route based on newly assigned STAR */
    public Route(Aircraft aircraft, Star star) {
        this();
        Array<String> inbound = star.getRandomInbound();
        for (int i = 0; i < inbound.size; i++) {
            if ("HDG".equals(inbound.get(i).split(" ")[0])) {
                aircraft.setHeading(Integer.parseInt(inbound.get(i).split(" ")[1]));
            } else {
                String[] data = inbound.get(i).split(" ");
                wpts.add(radarScreen.waypoints.get(data[1]));
                restrictions.add(new int[] {Integer.parseInt(data[2]), Integer.parseInt(data[3]), Integer.parseInt(data[4])});
                flyOver.add(data.length > 5 && data[5].equals("FO"));
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

        holdProcedure = new HoldProcedure(star);

        name = star.getName();
    }

    /** Create new Route based on newly assigned SID */
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
                ((Departure) aircraft).setOutboundHdg(Integer.parseInt(data[MathUtils.random(1, data.length - 1)]));
            }
        }

        holdProcedure = new HoldProcedure();

        name = sid.getName();
    }

    /** Create new Route based on saved route, called only by other constructors */
    private Route(JSONObject jo) {
        this();

        JSONArray waypoints = jo.getJSONArray("waypoints");
        JSONArray restr = jo.getJSONArray("restrictions");
        JSONArray fo = jo.getJSONArray("flyOver");
        for (int i = 0; i < waypoints.length(); i++) {
            wpts.add(radarScreen.waypoints.get(waypoints.getString(i)));
            String[] data = restr.getString(i).split(" ");
            restrictions.add(new int[] {Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2])});
            flyOver.add(fo.getBoolean(i));
        }

        holdProcedure = new HoldProcedure();

        name = jo.isNull("name") ? "null" : jo.getString("name");
    }

    /** Create new Route based on saved route and SID name */
    public Route(JSONObject jo, Sid sid) {
        this(jo);

        if ("null".equals(name) && sid != null) name = sid.getName();
    }

    /** Create new Route based on saved route and STAR name */
    public Route(JSONObject jo, Star star) {
        this(jo);
        holdProcedure = new HoldProcedure(star);

        name = star.getName();
    }

    /** Draws the lines between aircraft, waypoints with shapeRenderer */
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

    /** Draws the outbound track from waypoint (if latMode is After waypoint, fly heading) */
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

    /** Calculates distance between remaining points, excluding distance between aircraft and current waypoint */
    public float distBetRemainPts(int nextWptIndex) {
        int currentIndex = nextWptIndex;
        float dist = 0;
        while (currentIndex < getWaypoints().size - 1) {
            dist += distBetween(currentIndex, currentIndex + 1);
            currentIndex++;
        }
        return dist;
    }

    /** Calculates distance between 2 waypoints in the route based on their indices */
    public float distBetween(int pt1, int pt2) {
        Waypoint waypoint1 = getWaypoint(pt1);
        Waypoint waypoint2 = getWaypoint(pt2);
        return MathTools.pixelToNm(MathTools.distanceBetween(waypoint1.getPosX(), waypoint1.getPosY(), waypoint2.getPosX(), waypoint2.getPosY()));
    }

    /** Returns an array of waypoints from start to end index inclusive */
    public Array<Waypoint> getRemainingWaypoints(int start, int end) {
        //Returns array of waypoints starting from index
        Array<Waypoint> newRange = new Array<>(wpts);
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

    public int getWptMinAlt(int index) {
        return restrictions.get(index)[0];
    }

    public int getWptMaxAlt(String wptName) {
        return restrictions.get(findWptIndex(wptName))[1];
    }

    public int getWptMaxAlt(int index) {
        return restrictions.get(index)[1];
    }

    public int getWptMaxSpd(String wptName) {
        return restrictions.get(findWptIndex(wptName))[2];
    }

    public int getWptMaxSpd(int index) {
        return restrictions.get(index)[2];
    }

    public boolean getWptFlyOver(String wptName) {
        return flyOver.get(findWptIndex(wptName));
    }

    public HoldProcedure getHoldProcedure() {
        return holdProcedure;
    }

    public Array<int[]> getRestrictions() {
        return restrictions;
    }

    public Array<Boolean> getFlyOver() {
        return flyOver;
    }

    public String getName() {
        return name;
    }
}
