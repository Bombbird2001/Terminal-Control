package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.sidstar.RandomSID;
import com.bombbird.terminalcontrol.entities.sidstar.Route;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.ui.tabs.LatTab;
import com.bombbird.terminalcontrol.ui.tabs.Tab;
import org.json.JSONObject;

public class Departure extends Aircraft {
    //Others
    private Sid sid;
    private int outboundHdg;
    private final int contactAlt;
    private final int handoveralt;
    private boolean v2set;
    private boolean accel;
    private boolean sidSet;
    private boolean contacted;
    private boolean handedOver;
    private float cruiseAltTime;
    private final int cruiseAlt;
    private boolean higherSpdSet;
    private boolean cruiseSpdSet;

    public Departure(String callsign, String icaoType, Airport departure, Runway runway) {
        super(callsign, icaoType, departure);
        setOnGround(true);
        contactAlt = getAirport().getElevation() + 2000 + MathUtils.random(-500, 200);
        handoveralt = radarScreen.maxAlt + MathUtils.random(-800, -200);
        v2set = false;
        accel = false;
        sidSet = false;
        contacted = false;
        cruiseAltTime = MathUtils.random(8, 20);
        cruiseAlt = MathUtils.random(30, 39) * 1000;
        higherSpdSet = false;
        cruiseSpdSet = false;

        //Sets requested runway for takeoff
        setRunway(runway);

        //Gets a random SID
        sid = RandomSID.randomSID(departure, runway.getName());

        if ("CAL641".equals(callsign) && radarScreen.tutorial) {
            sid = getAirport().getSids().get("HICAL1C");
            getEmergency().setEmergency(false);
        }

        setRoute(new Route(this, sid, runway.getName()));

        setDirect(getRoute().getWaypoint(0));

        //Set initial IAS due to wind + 10 knots ground speed
        setGs(5);
        setIas(getAirport().getWinds()[1] * MathUtils.cosDeg(getAirport().getWinds()[0] - getRunway().getHeading()) + 10);

        //Set initial altitude equal to runway elevation
        setAltitude(getRunway().getElevation());

        //Set initial position on runway
        if ("TCTT".equals(getAirport().getIcao()) && "16R".equals(runway.getName())) {
            //Special case for TCTT runway 16R intersection takeoff
            setX(2864.2f);
            setY(1627.0f);
        } else if ("TCHX".equals(getAirport().getIcao()) && "13".equals(runway.getName())) {
            //TCHX departure adjustment since runway data does not include threshold
            setX(2862.0f);
            setY(1635.2f);
        } else {
            setX(getRunway().getPosition()[0]);
            setY(getRunway().getPosition()[1]);
        }

        loadLabel();
        setNavState(new NavState(this));

        if (getDirect() != null && getRoute().getWptFlyOver(getDirect().getName())) getDirect().setDepFlyOver(); //Set the flyOver separately if is flyover waypoint

        setControlState(ControlState.UNCONTROLLED);
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
        if (save.isNull("route")) {
            setRoute(new Route(sid, getRunway().getName()));
        } else {
            JSONObject route = save.getJSONObject("route");
            if (sid == null) {
                sid = new Sid(getAirport(), route.getJSONArray("waypoints"), route.getJSONArray("restrictions"), route.getJSONArray("flyOver"), route.isNull("name") ? "null" : route.getString("name"));
            }
            setRoute(new Route(route, sid));
        }
        outboundHdg = save.getInt("outboundHdg");
        contactAlt = save.getInt("contactAlt");
        handoveralt = save.getInt("handOverAlt");
        v2set = save.getBoolean("v2set");
        accel = !save.isNull("accel") && save.getBoolean("accel");
        sidSet = save.getBoolean("sidSet");
        contacted = save.getBoolean("contacted");
        handedOver = save.optBoolean("handedOver", !isArrivalDeparture() && getAltitude() > handoveralt);
        cruiseAltTime = (float) save.optDouble("cruiseAltTime", 1);
        cruiseAlt = save.getInt("cruiseAlt");
        higherSpdSet = save.getBoolean("higherSpdSet");
        cruiseSpdSet = save.getBoolean("cruiseSpdSet");

        loadLabel();
        setColor(new Color(0x11ff00ff));
        String control = save.optString("controlState");
        if ("0".equals(control)) {
            setControlState(ControlState.UNCONTROLLED);
        } else if ("1".equals(control)) {
            setControlState(ControlState.ARRIVAL);
        } else if ("2".equals(control)) {
            setControlState(ControlState.DEPARTURE);
        } else {
            setControlState(ControlState.valueOf(control));
        }

        loadOtherLabelInfo(save);
    }

    private void takeOff() {
        //Sets aircraft to takeoff mode
        getAirport().setAirborne(getAirport().getAirborne() + 1);

        setClearedIas(getV2());
        super.updateSpd();
        setClearedAltitude(getRunway().getInitClimb());
        updateAltRestrictions();
        getNavState().getClearedAlt().removeFirst();
        getNavState().getClearedAlt().addFirst(getClearedAltitude());
        if (sid.getInitClimb(getRunway().getName())[0] != -1) {
            setClearedHeading(sid.getInitClimb(getRunway().getName())[0]);
        } else {
            setClearedHeading(getRunway().getHeading());
        }
        getNavState().getClearedHdg().removeFirst();
        getNavState().getClearedHdg().addFirst(getClearedHeading());
        setHeading(getRunway().getHeading());
        setTkOfLdg(true);
    }

    @Override
    public void updateTkofLdg() {
        //Called to check for takeoff landing status
        if (getIas() > getV2() - 10 && !v2set) {
            setOnGround(false);
            setTargetHeading(getClearedHeading());
            v2set = true;
        }
        if (getAltitude() >= contactAlt && !contacted) {
            setControlState(ControlState.DEPARTURE);
            radarScreen.getCommBox().initialContact(this);
            setActionRequired(true);
            getDataTag().startFlash();
            contacted = true;
        }
        if (getAltitude() - getAirport().getElevation() > 1500 && !accel) {
            if (getClearedIas() == getV2()) {
                int speed = 220;
                if (!sidSet && getRoute().getWptMaxSpd(getRoute().getWaypoint(0).getName()) < 220) {
                    speed = getRoute().getWptMaxSpd(0);
                } else if (sidSet && getRoute().getWptMaxSpd(getDirect().getName()) < 220) {
                    speed = getRoute().getWptMaxSpd(getDirect().getName());
                }
                if (speed == -1) speed = 220;
                setClearedIas(speed);
                super.updateSpd();
            }
            accel = true;
        }
        if ((sid.getInitClimb(getRunway().getName()) == null || getAltitude() >= sid.getInitClimb(getRunway().getName())[1]) && !sidSet) {
            sidSet = true;
            updateAltRestrictions();
            updateTargetAltitude();
        }
        if (contacted && v2set && sidSet && accel) {
            setTkOfLdg(false);
        }
    }

    @Override
    public double update() {
        double info = super.update();

        if (handedOver && cruiseAltTime > 0) {
            cruiseAltTime -= Gdx.graphics.getDeltaTime();
            if (cruiseAltTime <= 0) {
                setClearedAltitude(cruiseAlt);
                getNavState().replaceAllClearedAltMode();
                getNavState().replaceAllClearedAlt();
            }
        }

        return info;
    }

    /** Overrides method in Aircraft class to join the lines between each SID waypoint */
    @Override
    public void drawSidStar() {
        //Draws line joining aircraft and sid/star track
        super.drawSidStar();
        radarScreen.waypointManager.updateSidRestriction(getRoute(), getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().getWaypoints().size);
    }

    /** Overrides method in Aircraft class to join lines between each cleared SID waypoint */
    @Override
    public void uiDrawSidStar() {
        super.uiDrawSidStar();
        radarScreen.waypointManager.updateSidRestriction(getRoute(), getRoute().findWptIndex(LatTab.clearedWpt), getRoute().getWaypoints().size);
    }

    /** Overrides method in Aircraft class to set to outbound heading */
    @Override
    public void setAfterLastWpt() {
        getNavState().updateLatModes(NavState.REMOVE_SIDSTAR_ONLY, true);
        setClearedHeading(outboundHdg);
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
        if (getNavState().getDispLatMode().first().contains("departure")) {
            //Aircraft on SID
            int highestAlt = -1;
            int lowestAlt = -1;
            if (getDirect() != null) {
                highestAlt = getRoute().getWptMaxAlt(getDirect().getName());
                lowestAlt = getRoute().getWptMinAlt(getDirect().getName());
            }
            if (highestAlt > -1) {
                setHighestAlt(highestAlt);
            } else if (contacted && getControlState() == ControlState.DEPARTURE) {
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
                        setLowestAlt(sid.getInitClimb(getRunway().getName())[1]);
                    } else {
                        setLowestAlt(nextFL);
                    }
                }
            }
        }
    }

    @Override
    public void updateSpd() {
        if (!higherSpdSet && getAltitude() >= 5000 && getAltitude() > getAirport().getElevation() + 4000) {
            if (getClearedIas() < 250) {
                setClearedIas(250);
                super.updateSpd();
            }
            int waypointSpd = getDirect() == null ? -1 : getRoute().getWptMaxSpd(getDirect().getName());
            higherSpdSet = getNavState().getDispSpdMode().last().contains("No") || waypointSpd >= 250 || waypointSpd == -1;
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
            int waypointSpd = getDirect() == null ? -1 : getRoute().getWptMaxSpd(getDirect().getName());
            cruiseSpdSet = getNavState().getDispSpdMode().last().contains("No") || waypointSpd >= getClimbSpd() || waypointSpd == -1;
        }
    }

    @Override
    public void updateAltitude(boolean holdAlt, boolean fixedVs) {
        super.updateAltitude(holdAlt, fixedVs);
        if (canHandover()) ui.updateAckHandButton(this);
        if (getControlState() == ControlState.DEPARTURE && getAltitude() >= handoveralt && getNavState().getDispLatMode().first().contains("departure")) {
            contactOther();
        }
    }

    @Override
    public boolean canHandover() {
        return getControlState() == ControlState.DEPARTURE && getAltitude() >= radarScreen.maxAlt - 4000 && getNavState().getDispLatMode().first().contains("departure");
    }

    @Override
    public void contactOther() {
        setControlState(ControlState.UNCONTROLLED);
        setClearedIas(getClimbSpd());
        super.updateSpd();
        handedOver = true;
        setExpedite(false);
        if (getExpediteTime() <= 120) radarScreen.setScore(radarScreen.getScore() + 1);
        if (sid.getCentre() != null) {
            radarScreen.getCommBox().contactFreq(this, sid.getCentre()[0], sid.getCentre()[1]);
        } else {
            radarScreen.getCommBox().contactFreq(this, "Centre", "120.5");
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

    public boolean isAccel() {
        return accel;
    }

    public void setOutboundHdg(int outboundHdg) {
        this.outboundHdg = outboundHdg;
    }

    public boolean isHandedOver() {
        return handedOver;
    }

    public float getCruiseAltTime() {
        return cruiseAltTime;
    }
}
