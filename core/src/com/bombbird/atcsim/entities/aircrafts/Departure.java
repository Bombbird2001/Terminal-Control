package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Runway;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.sidstar.Sid;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;

import java.util.HashMap;

public class Departure extends Aircraft {
    //Others
    private Sid sid;
    private int outboundHdg;
    private int sidIndex;
    private int contactAlt;
    private boolean v2set;
    private boolean sidSet;
    private boolean spdSet;

    public Departure(String callsign, String icaoType, Airport departure) {
        super(callsign, icaoType, departure);
        labelText[9] = departure.getIcao();
        setOnGround(true);
        sidIndex = 0;
        contactAlt = 2000 + MathUtils.random(-400, 400);
        v2set = false;
        sidSet = false;
        spdSet = false;

        //Gets a runway for takeoff
        HashMap<String, Runway> deptRwys = departure.getTakeoffRunways();
        String rwy = (String) deptRwys.keySet().toArray()[MathUtils.random(deptRwys.size() - 1)];
        setRunway(deptRwys.get(rwy));

        //Gets a random SID
        HashMap<String, Sid> sidList = getAirport().getSids();
        boolean sidSet = false;
        while (!sidSet) {
            String sidStr = (String) sidList.keySet().toArray()[MathUtils.random(sidList.size() - 1)];
            sid = sidList.get(sidStr);
            for (String runwayStr: sid.getRunways()) {
                if (runwayStr.equals(getRunway().getName())) {
                    sidSet = true;
                    break;
                }
            }
        }

        sid.printWpts();

        //Set initial IAS due to wind
        setIas(getAirport().getWinds()[1] * MathUtils.cosDeg(getAirport().getWinds()[0] - getRunway().getHeading()));

        //Set initial altitude equal to runway elevation
        setAltitude(getRunway().getElevation());

        //Set initial position on runway
        setX(getRunway().getPosition()[0]);
        setY(getRunway().getPosition()[1]);
        getLabel().setPosition(getX() - 100, getY() + 25);

        //Set takeoff heading
        setHeading(getRunway().getHeading());

        setControlState(0);

        takeOff();
    }

    private void takeOff() {
        outboundHdg = sid.getOutboundHdg();
        setClearedIas(getV2());
        setTargetIas(getV2());
        setClearedAltitude(3000);
        setTargetAltitude(getClearedAltitude());
        setClearedHeading(sid.getInitClimb()[0]);
        setTkofLdg(true);
    }

    @Override
    void updateTkofLdg() {
        if (getIas() > getV2() - 10 && !v2set) {
            setOnGround(false);
            setTargetHeading(getClearedHeading());
            v2set = true;
        }
        if (getAltitude() - getAirport().getElevation() >= contactAlt) {
            setTkofLdg(false);
            setControlState(2);
        }
        if (getAltitude() > sid.getInitClimb()[1] && !sidSet) {
            setDirect(sid.getWaypoint(0));
            setLatMode("sid");
            sidSet = true;
        }
        if (getAltitude() - getAirport().getElevation() >= 1500 && !spdSet) {
            setTargetIas(250);
            setClearedIas(250);
            spdSet = true;
        }
    }

    @Override
    public void removeSelectedWaypoints() {
        for (Waypoint waypoint: sid.getWaypoints()) {
            waypoint.setSelected(false);
        }
        if (getDirect() != null) {
            getDirect().setSelected(true);
        }
    }

    @Override
    public void drawSidStar() {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(getX(), getY(), getDirect().getPosX(), getDirect().getPosY());
        sid.joinLines(sidIndex, outboundHdg);
    }

    @Override
    public void updateLabel() {
        labelText[8] = sid.getName();
        super.updateLabel();
    }

    @Override
    public void updateDirect() {
        getDirect().setSelected(false);
        sidIndex++;
        setDirect(sid.getWaypoint(sidIndex));
        if (getDirect() == null) {
            setLatMode("vector");
            setClearedHeading((int)(outboundHdg + RadarScreen.magHdgDev));
        }
    }

    @Override
    double findNextTargetHdg() {
        Waypoint nextWpt = sid.getWaypoint(sidIndex + 1);
        if (nextWpt == null) {
            return outboundHdg;
        } else {
            float deltaX = nextWpt.getPosX() - getDirect().getPosX();
            float deltaY = nextWpt.getPosY() - getDirect().getPosY();
            double nextTarget;
            if (deltaX >= 0) {
                nextTarget = 90 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            } else {
                nextTarget = 270 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            }
            return nextTarget;
        }
    }
}
