package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.screens.ui.LatTab;
import com.bombbird.terminalcontrol.screens.ui.Tab;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Departure extends Aircraft {
    //Others
    private Sid sid;
    private int outboundHdg;
    private int contactAlt;
    private int handoveralt;
    private boolean v2set;
    private boolean sidSet;
    private boolean contacted;
    private int cruiseAlt;
    private boolean higherSpdSet;
    private boolean cruiseSpdSet;

    public Departure(String callsign, String icaoType, Airport departure, Runway runway) {
        super(callsign, icaoType, departure);
        setOnGround(true);
        contactAlt = radarScreen.minAlt + MathUtils.random(-800, -200);
        handoveralt = radarScreen.maxAlt - 1500 + MathUtils.random(-500, 500);
        v2set = false;
        sidSet = false;
        contacted = false;
        cruiseAlt = MathUtils.random(30, 39) * 1000;
        higherSpdSet = false;
        cruiseSpdSet = false;

        //Sets requested runway for takeoff
        setRunway(runway);

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

        if ("CAL641".equals(callsign) && radarScreen.tutorial) {
            sid = sidList.get("CHALI1C");
        }

        setDirect(sid.getWaypoint(0));

        //Set initial IAS due to wind + 10 knots ground speed
        setGs(5);
        setIas(getAirport().getWinds()[1] * MathUtils.cosDeg(getAirport().getWinds()[0] - getRunway().getHeading()) + 10);

        //Set initial altitude equal to runway elevation
        setAltitude(getRunway().getElevation());

        //Set initial position on runway
        setX(getRunway().getPosition()[0]);
        setY(getRunway().getPosition()[1]);

        loadLabel();
        setNavState(new NavState(this));

        setControlState(0);
        setColor(new Color(0x11ff00ff));

        //Set takeoff heading
        setHeading(getRunway().getHeading());
        setTrack(getHeading() - radarScreen.magHdgDev);

        takeOff();

        initRadarPos();
    }

    public Departure(JSONObject save) {
        super(save);

        sid = getAirport().getSids().get(save.getString("sid"));
        outboundHdg = save.getInt("outboundHdg");
        contactAlt = save.getInt("contactAlt");
        handoveralt = save.getInt("handOverAlt");
        v2set = save.getBoolean("v2set");
        sidSet = save.getBoolean("sidSet");
        contacted = save.getBoolean("contacted");
        cruiseAlt = save.getInt("cruiseAlt");
        higherSpdSet = save.getBoolean("higherSpdSet");
        cruiseSpdSet = save.getBoolean("cruiseSpdSet");

        loadLabel();
        setColor(new Color(0x11ff00ff));
        setControlState(save.getInt("controlState"));

        JSONArray labelPos = save.getJSONArray("labelPos");
        getLabel().setPosition((float) labelPos.getDouble(0), (float) labelPos.getDouble(1));
    }

    private void takeOff() {
        //Sets aircraft to takeoff mode
        outboundHdg = sid.getOutboundHdg();
        setClearedIas(getV2());
        super.updateSpd();
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
        setHeading(getClearedHeading());
        setTkOfLdg(true);
    }

    @Override
    public void updateTkofLdg() {
        //Called to check for takeoff landing status
        if (getIas() > getV2() - 10 && !v2set) {
            setOnGround(false);
            getAirport().setAirborne(getAirport().getAirborne() + 1);
            setTargetHeading(getClearedHeading());
            v2set = true;
        }
        if (getAltitude() - getAirport().getElevation() >= contactAlt && !contacted) {
            setControlState(2);
            radarScreen.getCommBox().initialContact(this);
            contacted = true;
        }
        if (getAltitude() >= sid.getInitClimb()[1] && !sidSet) {
            if (getClearedIas() == getV2()) {
                setClearedIas(220);
                super.updateSpd();
            }
            sidSet = true;
            updateAltRestrictions();
            updateTargetAltitude();
        }
        if (contacted && v2set && sidSet) {
            setTkOfLdg(false);
        }
    }

    /** Overrides method in Aircraft class to join the lines between each SID waypoint */
    @Override
    public void drawSidStar() {
        //Draws line joining aircraft and sid/star track
        super.drawSidStar();
        sid.joinLines(sid.findWptIndex(getNavState().getClearedDirect().last().getName()), sid.getWaypoints().size, outboundHdg);
    }

    /** Overrides method in Aircraft class to join lines between each cleared SID waypoint */
    @Override
    public void uiDrawSidStar() {
        super.uiDrawSidStar();
        sid.joinLines(sid.findWptIndex(LatTab.clearedWpt), sid.getWaypoints().size, -1);
    }

    /** Overrides method in Aircraft class to update label + update SID name */
    @Override
    public void updateLabel() {
        getLabelText()[8] = sid.getName();
        super.updateLabel();
    }

    /** Overrides method in Aircraft class to set to outbound heading */
    @Override
    public void setAfterLastWpt() {
        getNavState().getLatModes().removeValue(sid.getName() + " deaprture", false);
        setClearedHeading((int)(outboundHdg + radarScreen.magHdgDev));
        getNavState().getClearedHdg().removeFirst();
        getNavState().getClearedHdg().addFirst(getClearedHeading());
        updateVectorMode();
        removeSidStarMode();
    }

    @Override
    public double findNextTargetHdg() {
        double result = super.findNextTargetHdg();
        return result < 0 ? outboundHdg : result;
    }

    @Override
    public void updateAltRestrictions() {
        if (getAltitude() > handoveralt) {
            //Aircraft has been handed over
            setLowestAlt(cruiseAlt);
            setHighestAlt(cruiseAlt);
        }
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
                setHighestAlt(radarScreen.maxAlt);
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
    public void updateSpd() {
        if (!higherSpdSet && getAltitude() >= 7000) {
            if (getClearedIas() < 250) {
                setClearedIas(250);
                super.updateSpd();
            }
            higherSpdSet = true;
        }
        if (!cruiseSpdSet && getAltitude() > 10000) {
            if (getClearedIas() < getClimbSpd()) {
                if (isSelected()) {
                    Tab.notListening = true;
                    Array<String> array = ui.spdTab.valueBox.getList().getItems();
                    array.add(Integer.toString(getClimbSpd()));
                    ui.spdTab.valueBox.setItems(array);
                    ui.spdTab.valueBox.setSelected(Integer.toString(getClimbSpd()));
                    Tab.notListening = false;
                }
                setClearedIas(getClimbSpd());
                super.updateSpd();
            }
            cruiseSpdSet = true;
        }
    }

    @Override
    public void updateAltitude() {
        super.updateAltitude();
        if (getControlState() == 2 && getAltitude() >= handoveralt) {
            setControlState(0);
            setClearedIas(getClimbSpd());
            super.updateSpd();
            setClearedAltitude(cruiseAlt);
            getNavState().replaceAllClearedAlt();
            setExpedite(false);
            radarScreen.setScore(radarScreen.getScore() + 1);
            radarScreen.getCommBox().contactFreq(this, radarScreen.centreFreq[0], radarScreen.centreFreq[1]);
        }
    }

    @Override
    public SidStar getSidStar() {
        return sid;
    }

    public boolean isSidSet() {
        return sidSet;
    }

    public int getOutboundHdg() {
        return outboundHdg;
    }

    public int getContactAlt() {
        return contactAlt;
    }

    public int getHandoveralt() {
        return handoveralt;
    }

    public boolean isV2set() {
        return v2set;
    }

    public boolean isContacted() {
        return contacted;
    }

    public int getCruiseAlt() {
        return cruiseAlt;
    }

    public boolean isHigherSpdSet() {
        return higherSpdSet;
    }

    public boolean isCruiseSpdSet() {
        return cruiseSpdSet;
    }
}
