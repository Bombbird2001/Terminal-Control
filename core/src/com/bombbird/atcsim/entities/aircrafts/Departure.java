package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Runway;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.sidstar.Sid;
import com.bombbird.atcsim.entities.sidstar.SidStar;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;

import java.util.HashMap;

public class Departure extends Aircraft {
    //Others
    private Sid sid;
    private int outboundHdg;
    private int contactAlt;
    private boolean v2set;
    private boolean sidSet;
    private boolean contacted;

    public Departure(String callsign, String icaoType, Airport departure) {
        super(callsign, icaoType, departure);
        setOnGround(true);
        contactAlt = 2000 + MathUtils.random(-400, 400);
        v2set = false;
        sidSet = false;
        contacted = false;

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

        loadLabel();
        setNavState(new NavState(2, this));

        setControlState(0);
        setColor(new Color(0x11ff00ff));

        //Set takeoff heading
        setHeading(getRunway().getHeading());

        takeOff();
    }

    private void takeOff() {
        //Sets aircraft to takeoff mode
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
        //Called to check for takeoff landing status
        if (getIas() > getV2() - 10 && !v2set) {
            setOnGround(false);
            setTargetHeading(getClearedHeading());
            v2set = true;
        }
        if (getAltitude() - getAirport().getElevation() >= contactAlt) {
            setControlState(2);
            contacted = true;
        }
        if (getAltitude() > sid.getInitClimb()[1] && !sidSet) {
            setDirect(sid.getWaypoint(0));
            setLatMode("sidstar");
            setTargetIas(250);
            setClearedIas(250);
            sidSet = true;
        }
        if (contacted && v2set && sidSet) {
            setTkofLdg(false);
        }
    }

    @Override
    public void drawSidStar() {
        //Draws line joining aircraft and sid/star track
        super.drawSidStar();
        sid.joinLines(getSidStarIndex(), outboundHdg);
    }

    @Override
    public void updateLabel() {
        labelText[8] = sid.getName();
        super.updateLabel();
    }

    @Override
    public void updateDirect() {
        //Updates direct to next waypoint
        super.updateDirect();
        if (getDirect() == null) {
            setClearedHeading((int)(outboundHdg + RadarScreen.magHdgDev));
            updateVectorMode();
            removeSidStarMode();
        }
    }

    @Override
    double findNextTargetHdg() {
        double result = super.findNextTargetHdg();
        if (result < -0.5) {
            return outboundHdg;
        } else {
            return result;
        }
    }

    @Override
    public SidStar getSidStar() {
        return sid;
    }
}
