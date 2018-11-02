package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.screens.ui.LatTab;

import java.util.HashMap;

public class Departure extends Aircraft {
    //Others
    private Sid sid;
    private int outboundHdg;
    private int contactAlt;
    private int handOverAlt;
    private boolean v2set;
    private boolean sidSet;
    private boolean contacted;
    private int cruiseAlt;

    public Departure(String callsign, String icaoType, Airport departure) {
        super(callsign, icaoType, departure);
        setOnGround(true);
        contactAlt = 2000 + MathUtils.random(-400, 400);
        handOverAlt = 11500 + MathUtils.random(-500, 500);
        v2set = false;
        sidSet = false;
        contacted = false;
        cruiseAlt = MathUtils.random(30, 39) * 1000;

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
        setDirect(sid.getWaypoint(0));

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
        setTrack(getHeading() - RadarScreen.magHdgDev);

        takeOff();

        initRadarPos();
    }

    private void takeOff() {
        //Sets aircraft to takeoff mode
        outboundHdg = sid.getOutboundHdg();
        setClearedIas(getV2());
        setTargetIas(getV2());
        setClearedAltitude(sid.getInitClimb()[1]);
        int clearedAltitude = sid.getInitClimb()[1];
        if (clearedAltitude < 3000) {
            clearedAltitude = 3000;
        }
        if (clearedAltitude % 1000 != 0) {
            clearedAltitude += 1000 - getClearedAltitude() % 1000;
        }
        updateAltRestrictions();
        setClearedAltitude(clearedAltitude);
        getNavState().getClearedAlt().removeFirst();
        getNavState().getClearedAlt().addFirst(clearedAltitude);
        if (sid.getInitClimb()[0] != -1) {
            setClearedHeading(sid.getInitClimb()[0]);
        } else {
            setClearedHeading(getRunway().getHeading());
        }
        setTkofLdg(true);
    }

    @Override
    public void updateTkofLdg() {
        //Called to check for takeoff landing status
        if (getIas() > getV2() - 10 && !v2set) {
            setOnGround(false);
            setTargetHeading(getClearedHeading());
            v2set = true;
        }
        if (getAltitude() - getAirport().getElevation() >= contactAlt && !contacted) {
            setControlState(2);
            contacted = true;
        }
        if (getAltitude() >= sid.getInitClimb()[1] && !sidSet) {
            setLatMode("sidstar");
            if (getClearedIas() == getV2()) {
                setTargetIas(250);
                setClearedIas(250);
            }
            sidSet = true;
            updateAltRestrictions();
            updateTargetAltitude();
        }
        if (contacted && v2set && sidSet) {
            setTkofLdg(false);
        }
    }

    /** Overrides method in Aircraft class to join the lines between each SID waypoint */
    @Override
    public void drawSidStar() {
        //Draws line joining aircraft and sid/star track
        super.drawSidStar();
        sid.joinLines(getSidStarIndex(), getSidStar().getWaypoints().size, outboundHdg, false);
    }

    /** Overrides method in Aircraft class to join lines between each cleared SID waypoint */
    @Override
    public void uiDrawSidStar() {
        super.uiDrawSidStar();
        sid.joinLines(sid.findWptIndex(LatTab.clearedWpt), sid.getWaypoints().size, -1, true);
    }

    /** Overrides method in Aircraft class to update label + update SID name */
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
    public double findNextTargetHdg() {
        double result = super.findNextTargetHdg();
        if (result < -0.5) {
            return outboundHdg;
        } else {
            return result;
        }
    }

    @Override
    public void updateAltRestrictions() {
        if (getNavState().getDispLatMode().first().contains("departure")) {
            //Aircraft on SID
            int highestAlt = -1;
            int lowestAlt = -1;
            if (getDirect() != null) {
                highestAlt = getSidStar().getWptMaxAlt(getDirect().getName());
                lowestAlt = getSidStar().getWptMinAlt(getDirect().getName());
            }
            if (highestAlt > -1) {
                setHighestAlt(highestAlt);
            } else if (contacted && getControlState() == 2) {
                setHighestAlt(RadarScreen.maxDeptAlt);
            } else {
                setHighestAlt(cruiseAlt);
            }
            if (lowestAlt > -1 && sidSet && getAltitude() < lowestAlt) {
                //If there is a waypoint with minimum altitude
                setLowestAlt(lowestAlt);
            } else {
                int nextFL;
                if (((int) getAltitude()) % 1000 == 0) {
                    nextFL = (int) getAltitude();
                } else {
                    nextFL = (int) getAltitude() + 1000 - ((int) getAltitude()) % 1000;
                }
                if (getLowestAlt() < nextFL) {
                    //If lowest alt value is less than the next flight level after current altitude that divisible by 10 (e.g. if at 5500 ft, next is 6000ft)
                    if (!sidSet) {
                        //If still climbing on init climb
                        setLowestAlt(sid.getInitClimb()[1]);
                    } else {
                        setLowestAlt(nextFL);
                    }
                }
            }
        }
    }

    @Override
    public void updateAltitude() {
        super.updateAltitude();
        if (getControlState() == 2 && getAltitude() >= handOverAlt) {
            setControlState(0);
            updateAltRestrictions();
            setClearedAltitude(cruiseAlt);
        }
    }

    @Override
    public SidStar getSidStar() {
        return sid;
    }

    public boolean isSidSet() {
        return sidSet;
    }
}
