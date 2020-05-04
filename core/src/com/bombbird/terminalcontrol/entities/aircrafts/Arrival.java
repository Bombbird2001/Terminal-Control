package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle;
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach;
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR;
import com.bombbird.terminalcontrol.entities.sidstar.Route;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.ui.tabs.LatTab;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Arrival extends Aircraft {
    //Others
    private int contactAlt;
    private Star star;
    private Queue<float[]> nonPrecAlts;
    private boolean lowerSpdSet;
    private boolean ilsSpdSet;
    private boolean finalSpdSet;

    //For fuel
    private float fuel;
    private boolean requestPriority = false;
    private boolean declareEmergency = false;
    private boolean divert = false;

    //For go around
    private boolean willGoAround;
    private int goAroundAlt;
    private boolean goAroundSet;

    public Arrival(String callsign, String icaoType, Airport arrival) {
        super(callsign, icaoType, arrival);
        setOnGround(false);
        lowerSpdSet = false;
        ilsSpdSet = false;
        finalSpdSet = false;
        willGoAround = false;
        goAroundAlt = 0;
        contactAlt = MathUtils.random(2000) + 22000;

        //Gets a STAR for active runways
        star = RandomSTAR.randomSTAR(arrival);

        if ("EVA226".equals(callsign) && radarScreen.tutorial) {
            star = arrival.getStars().get("NTN1A");
            getEmergency().setEmergency(false);
            setTypDes(2900);
            contactAlt = 22000;
        }
        RandomSTAR.starUsed(arrival.getIcao(), star.getName());

        setRoute(new Route(this, star));

        setDirect(getRoute().getWaypoint(0));

        setClearedHeading((int)getHeading());
        setTrack(getHeading() - TerminalControl.radarScreen.magHdgDev);

        //Calculate spawn border
        float[] point = MathTools.pointsAtBorder(new float[] {1310, 4450}, new float[] {50, 3190}, getDirect().getPosX(), getDirect().getPosY(), 180 + (float) getTrack());
        setX(point[0]);
        setY(point[1]);

        if ("BULLA-T".equals(star.getName()) || "KOPUS-T".equals(star.getName())) setDirect(getRoute().getWaypoint(1));

        loadLabel();
        setNavState(new NavState(this));
        Waypoint maxAltWpt = null;
        Waypoint minAltWpt = null;
        for (Waypoint waypoint: getRoute().getWaypoints()) {
            if (maxAltWpt == null && getRoute().getWptMaxAlt(waypoint.getName()) > -1) {
                maxAltWpt = waypoint;
            }
            if (minAltWpt == null && getRoute().getWptMinAlt(waypoint.getName()) > -1) {
                minAltWpt = waypoint;
            }
        }

        fuel = (45 + 10 + 10) * 60 + distToGo() / 250 * 3600 + 900 + MathUtils.random(-600, 600);

        float initAlt = 3000 + (distToGo() - 15) / 300 * 60 * getTypDes() * 0.8f;
        if ("BULLA-T".equals(star.getName()) || "KOPUS-T".equals(star.getName())) {
            initAlt = 9000;
        } else {
            if (maxAltWpt != null) {
                float maxAlt = getRoute().getWptMaxAlt(maxAltWpt.getName()) + (distFromStartToPoint(maxAltWpt) - 5) / 300 * 60 * getTypDes();
                if (maxAlt < initAlt) initAlt = maxAlt;
            }
            if (initAlt > 28000) {
                initAlt = 28000;
            } else if (initAlt < 6000) {
                initAlt = 6000;
            }
            if (minAltWpt != null && initAlt < getRoute().getWptMinAlt(minAltWpt.getName())) {
                initAlt = getRoute().getWptMinAlt(minAltWpt.getName());
            }
            for (Obstacle obstacle : radarScreen.obsArray) {
                if (obstacle.isIn(this) && initAlt < obstacle.getMinAlt()) {
                    initAlt = obstacle.getMinAlt();
                }
            }
        }
        if (radarScreen.tutorial && "EVA226".equals(callsign)) {
            initAlt = 23400;
        }
        setAltitude(initAlt);
        updateAltRestrictions();
        if ("BULLA-T".equals(star.getName()) || "KOPUS-T".equals(star.getName())) {
            setClearedAltitude(6000);
        } else if (initAlt > 15000) {
            setClearedAltitude(15000);
        } else {
            setClearedAltitude((int) initAlt - (int) initAlt % 1000);
        }
        if (getClearedAltitude() < getAltitude() - 500) {
            setVerticalSpeed(-getTypDes());
        } else {
            setVerticalSpeed(-getTypDes() * (getAltitude() - getClearedAltitude()) / 500);
        }

        setClearedIas(getClimbSpd());
        setIas(getClimbSpd());

        if (getDirect() != null) {
            int spd = getRoute().getWptMaxSpd(getDirect().getName());
            if (spd > -1) {
                setClearedIas(spd);
                setIas(spd);
            }
        }

        if (getAltitude() <= 10000 && (getClearedIas() > 250 || getIas() > 250)) {
            setClearedIas(250);
            setIas(250);
        }

        if (distToGo() <= 20 && getClearedIas() > 220) {
            setClearedIas(220);
            setIas(220);
        }

        checkArrival();

        getNavState().getClearedSpd().removeFirst();
        getNavState().getClearedSpd().addFirst(getClearedIas());

        getNavState().getClearedAlt().removeLast();
        getNavState().getClearedAlt().addLast(getClearedAltitude());

        setControlState(ControlState.UNCONTROLLED);
        setColor(new Color(0x00b3ffff));

        setHeading(update());
        setTrack(getHeading() - radarScreen.magHdgDev + updateTargetHeading()[1]);

        initRadarPos();
    }

    public Arrival(JSONObject save) {
        super(save);

        star = getAirport().getStars().get(save.getString("star"));

        if (save.isNull("route")) {
            setRoute(new Route(star));
        } else {
            JSONObject route = save.getJSONObject("route");
            if (star == null) {
                star = new Star(getAirport(), route.getJSONArray("waypoints"), route.getJSONArray("restrictions"), route.getJSONArray("flyOver"), route.isNull("name") ? "null" : route.getString("name"));
            }
            setRoute(new Route(route, star));
        }

        if (save.isNull("nonPrecAlts")) {
            //If non precision alt is null
            nonPrecAlts = null;
        } else {
            JSONArray nonPrec = save.getJSONArray("nonPrecAlts");
            nonPrecAlts = new Queue<>();
            for (int i = 0; i < nonPrec.length(); i++) {
                JSONArray data = nonPrec.getJSONArray(i);
                nonPrecAlts.addLast(new float[] {(float) data.getDouble(0), (float) data.getDouble(1)});
            }
        }

        lowerSpdSet = save.getBoolean("lowerSpdSet");
        ilsSpdSet = save.getBoolean("ilsSpdSet");
        finalSpdSet = save.getBoolean("finalSpdSet");
        willGoAround = save.getBoolean("willGoAround");
        goAroundAlt = save.getInt("goAroundAlt");
        goAroundSet = save.getBoolean("goAroundSet");
        contactAlt = save.getInt("contactAlt");

        fuel = save.isNull("fuel") ? 75 * 60 : (float) save.getDouble("fuel");
        requestPriority = !save.isNull("requestPriority") && save.getBoolean("requestPriority");
        declareEmergency = !save.isNull("declareEmergency") && save.getBoolean("declareEmergency");
        divert = !save.isNull("divert") && save.getBoolean("divert");

        loadLabel();
        setColor(new Color(0x00b3ffff));
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

    public Arrival(Departure departure) {
        //Convert departure emergency to arrival
        super(departure);
        setOnGround(false);
        lowerSpdSet = false;
        ilsSpdSet = false;
        finalSpdSet = false;
        willGoAround = false;
        goAroundAlt = 0;
        contactAlt = MathUtils.random(2000) + 22000;

        loadLabel();
        setNavState(departure.getNavState());
        fuel = 99999; //Just assume they won't run out of fuel

        setColor(new Color(0x00b3ffff));
        setControlState(ControlState.ARRIVAL);
        int size = departure.getDataTag().getTrailDots().size;
        for (int i = 0; i < size; i++) {
            Image image = departure.getDataTag().getTrailDots().removeFirst();
            getDataTag().addTrailDot(image.getX() + image.getWidth() / 2, image.getY() + image.getHeight() / 2);
        }

        star = getAirport().getStars().values().iterator().next(); //Assign any STAR for the sake of not crashing the game

        if (getEmergency().isActive()) getDataTag().setEmergency();
    }

    /** Calculates remaining distance on STAR from current aircraft position */
    private float distToGo() {
        float dist = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getDirect().getPosX(), getDirect().getPosY()));
        dist += getRoute().distBetRemainPts(getSidStarIndex());
        return dist;
    }

    /** Calculates remaining distance on STAR from current start aircraft position to a certain point on it */
    private float distFromStartToPoint(Waypoint waypoint) {
        float dist = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getDirect().getPosX(), getDirect().getPosY()));
        int nextIndex = 1;
        if (getRoute().getWaypoints().size > 1 && !getRoute().getWaypoint(0).equals(waypoint)) {
            while (!getRoute().getWaypoint(nextIndex).equals(waypoint)) {
                dist += getRoute().distBetween(nextIndex - 1, nextIndex);
                nextIndex += 1;
            }
        }
        return dist;
    }

    /** Overrides method in Aircraft class to join the lines between each STAR waypoint */
    @Override
    public void drawSidStar() {
        super.drawSidStar();
        radarScreen.waypointManager.updateStarRestriction(getRoute(), getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().getWaypoints().size);
    }

    /** Overrides method in Aircraft class to join lines between each cleared STAR waypoint */
    @Override
    public void uiDrawSidStar() {
        super.uiDrawSidStar();
        radarScreen.waypointManager.updateStarRestriction(getRoute(), getRoute().findWptIndex(LatTab.clearedWpt), getRoute().getWaypoints().size);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till afterWpt, then draws a heading line from there */
    @Override
    public void drawAftWpt() {
        super.drawAftWpt();
        getRoute().joinLines(getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().findWptIndex(getNavState().getClearedAftWpt().last().getName()) + 1, getNavState().getClearedAftWptHdg().last());
        radarScreen.waypointManager.updateStarRestriction(getRoute(), getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().findWptIndex(getNavState().getClearedAftWpt().last().getName()) + 1);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected afterWpt, then draws a heading line from there */
    @Override
    public void uiDrawAftWpt() {
        super.uiDrawAftWpt();
        getRoute().joinLines(getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().findWptIndex(LatTab.afterWpt) + 1, LatTab.afterWptHdg);
        radarScreen.waypointManager.updateStarRestriction(getRoute(), getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().findWptIndex(LatTab.afterWpt) + 1);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till holdWpt */
    @Override
    public void drawHoldPattern() {
        super.drawHoldPattern();
        radarScreen.shapeRenderer.setColor(Color.WHITE);
        if (getNavState().getClearedHold().size > 0 && getNavState().getClearedHold().last() != null && getNavState().getClearedDirect().size > 0 && getNavState().getClearedDirect().last() != null) {
            getRoute().joinLines(getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().findWptIndex(getNavState().getClearedHold().last().getName()) + 1, -1);
            radarScreen.waypointManager.updateStarRestriction(getRoute(), getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().findWptIndex(getNavState().getClearedHold().last().getName()) + 1);
        }
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected holdWpt */
    @Override
    public void uiDrawHoldPattern() {
        super.uiDrawHoldPattern();
        radarScreen.shapeRenderer.setColor(Color.YELLOW);
        getRoute().joinLines(getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().findWptIndex(LatTab.holdWpt) + 1, -1);
        radarScreen.waypointManager.updateStarRestriction(getRoute(), getRoute().findWptIndex(getNavState().getClearedDirect().last().getName()), getRoute().findWptIndex(LatTab.holdWpt) + 1);
    }

    /** Overrides method in Aircraft class to set to current heading*/
    @Override
    public void setAfterLastWpt() {
        setClearedHeading((int) getHeading());
        getNavState().getClearedHdg().removeFirst();
        getNavState().getClearedHdg().addFirst(getClearedHeading());
        updateVectorMode();
        removeSidStarMode();
    }

    /** Overrides updateSpd method in Aircraft, for setting the aircraft speed to 220 knots when within 20nm track miles if clearedIas > 220 knots */
    @Override
    public void updateSpd() {
        if (getAltitude() < 10000 && getClearedIas() > 250) {
            setClearedIas(250);
            super.updateSpd();
        }
        if (!lowerSpdSet && getDirect() != null && distToGo() <= 20) {
            if (getClearedIas() > 220) {
                setClearedIas(220);
                super.updateSpd();
            }
            lowerSpdSet = true;
        }
        if (!ilsSpdSet && isLocCap()) {
            if (getClearedIas() > 190) {
                setClearedIas(190);
                super.updateSpd();
            }
            ilsSpdSet = true;
        }
        if (!finalSpdSet && isLocCap() && MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getRwy().getX(), getIls().getRwy().getY())) <= 7) {
            if (getClearedIas() > getApchSpd()) {
                setClearedIas(getApchSpd());
                super.updateSpd();
            }
            finalSpdSet = true;
        }
        if (getHoldWpt() != null && getDirect() != null && getHoldWpt().equals(getDirect()) && getClearedIas() > 250 && MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getHoldWpt().getPosX(), getHoldWpt().getPosY())) <= 8) {
            setClearedIas(250);
            super.updateSpd();
        }
    }

    /** Overrides findNextTargetHdg method in Aircraft, for finding the next heading aircraft should fly in order to reach next waypoint */
    @Override
    public double findNextTargetHdg() {
        double result = super.findNextTargetHdg();
        return result < 0 ? getHeading() : result;
    }

    /** Overrides updateAltRestrictions method in Aircraft, for setting aircraft altitude restrictions when descending via the STAR */
    @Override
    public void updateAltRestrictions() {
        if (getNavState().containsCode(getNavState().getDispLatMode().first(), NavState.SID_STAR, NavState.AFTER_WAYPOINT_FLY_HEADING) || (getNavState().getDispLatMode().first() == NavState.HOLD_AT && !isHolding())) {
            //Aircraft on STAR
            int highestAlt = -1;
            int lowestAlt = -1;
            if (getDirect() != null) {
                highestAlt = Math.max(getRoute().getWptMaxAlt(getDirect().getName()), ((int) getAltitude()) / 1000 * 1000);
                lowestAlt = getRoute().getWptMinAlt(getDirect().getName());
                if ("TCOO".equals(getAirport().getIcao()) && getAltitude() >= 3499 && highestAlt == 3000) {
                    highestAlt = 3500;
                } else if ("TCHH".equals(getAirport().getIcao()) && getSidStar().getRunways().contains("25R", false) && getAltitude() >= 4499 && highestAlt == 4000) {
                    highestAlt = 4500;
                }
                if (highestAlt > radarScreen.maxAlt) highestAlt = radarScreen.maxAlt;
                if (highestAlt < radarScreen.minAlt) highestAlt = radarScreen.minAlt;
                if (lowestAlt > -1 && highestAlt < lowestAlt) highestAlt = lowestAlt;
            }
            setHighestAlt(highestAlt > -1 ? highestAlt : radarScreen.maxAlt);
            setLowestAlt(lowestAlt > -1 ? lowestAlt : radarScreen.minAlt);
        } else if (getNavState().getDispLatMode().first() == NavState.HOLD_AT && isHolding() && getHoldWpt() != null) {
            int[] altRestr = getRoute().getHoldProcedure().getAltRestAtWpt(getHoldWpt());
            int highestAlt = altRestr[1];
            int lowestAlt = altRestr[0];
            setHighestAlt(highestAlt > -1 ? highestAlt : radarScreen.maxAlt);
            setLowestAlt(lowestAlt > -1 ? lowestAlt : radarScreen.minAlt);
        }
    }

    /** Overrides update method in Aircraft to include updating fuel time */
    @Override
    public double update() {
        double info = super.update();

        if (!isOnGround() && !getEmergency().isEmergency()) {
            updateFuel();
        }

        return info;
    }

    /** Updates the fuel time for arrival */
    private void updateFuel() {
        fuel -= Gdx.graphics.getDeltaTime();

        if (fuel < 2700 && !requestPriority && getControlState() == ControlState.ARRIVAL) {
            //Low fuel, request priority
            if (getAirport().getLandingRunways().size() == 0) {
                //Airport has no landing runways available, different msg
                radarScreen.getCommBox().warningMsg("Pan-pan, pan-pan, pan-pan, " + getCallsign() + " is low on fuel and will divert in 10 minutes if no landing runway is available.");
                TerminalControl.tts.lowFuel(this, 3);
            } else {
                radarScreen.getCommBox().warningMsg("Pan-pan, pan-pan, pan-pan, " + getCallsign() + " is low on fuel and requests priority landing.");
                TerminalControl.tts.lowFuel(this, 0);
            }

            requestPriority = true;

            setActionRequired(true);
            getDataTag().startFlash();
        }

        if (fuel < 2100 && !declareEmergency && getControlState() == ControlState.ARRIVAL) {
            //Minimum fuel, declare emergency
            if (getAirport().getLandingRunways().size() == 0) {
                //Airport has no landing runways available, divert directly
                radarScreen.getCommBox().warningMsg("Mayday, mayday, mayday, " + getCallsign() + " is declaring a fuel emergency and is diverting immediately.");
                TerminalControl.tts.lowFuel(this, 4);
                divertToAltn();
            } else {
                radarScreen.getCommBox().warningMsg("Mayday, mayday, mayday, " + getCallsign() + " is declaring a fuel emergency and requests immediate landing within 10 minutes or will divert.");
                radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.9f));
                TerminalControl.tts.lowFuel(this, 1);
            }

            declareEmergency = true;
            if (!isFuelEmergency()) setFuelEmergency(true);
            getDataTag().setEmergency();
        }

        if (fuel < 1500 && !divert && !isLocCap() && getControlState() == ControlState.ARRIVAL) {
            //Diverting to alternate
            radarScreen.getCommBox().warningMsg(getCallsign() + " is diverting to the alternate airport.");
            TerminalControl.tts.lowFuel(this, 2);
            divertToAltn();

            radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.9f));
        }
    }

    /** Instructs the aircraft to divert to an alternate airport */
    private void divertToAltn() {
        getNavState().getDispLatMode().clear();
        getNavState().getDispLatMode().addFirst(NavState.FLY_HEADING);
        getNavState().getDispAltMode().clear();
        getNavState().getDispAltMode().addFirst(NavState.NO_RESTR);
        getNavState().getDispSpdMode().clear();
        getNavState().getDispSpdMode().addFirst(NavState.NO_RESTR);

        getNavState().getClearedHdg().clear();
        getNavState().getClearedHdg().addFirst(radarScreen.divertHdg);
        getNavState().getClearedDirect().clear();
        getNavState().getClearedDirect().addFirst(null);
        getNavState().getClearedAftWpt().clear();
        getNavState().getClearedAftWpt().addFirst(null);
        getNavState().getClearedAftWptHdg().clear();
        getNavState().getClearedAftWptHdg().addFirst(radarScreen.divertHdg);
        getNavState().getClearedHold().clear();
        getNavState().getClearedHold().addFirst(null);
        getNavState().getClearedIls().clear();
        getNavState().getClearedIls().addFirst(null);
        getNavState().getClearedAlt().clear();
        getNavState().getClearedAlt().addFirst(10000);
        getNavState().getClearedExpedite().clear();
        getNavState().getClearedExpedite().addFirst(false);
        getNavState().getClearedSpd().clear();
        getNavState().getClearedSpd().addFirst(250);
        getNavState().setLength(1);
        getNavState().updateAircraftInfo();

        setControlState(ControlState.UNCONTROLLED);

        divert = true;
    }

    /** Overrides updateAltitude method in Aircraft for when arrival is on glide slope or non precision approach */
    @Override
    public void updateAltitude(boolean holdAlt, boolean fixedVs) {
        if (getIls() != null) {
            if (!getIls().isNpa()) {
                if (!isGsCap()) {
                    super.updateAltitude(getAltitude() < getIls().getGSAlt(this) && getIls().getName().contains("IMG"), false);
                    if (isLocCap() && Math.abs(getAltitude() - getIls().getGSAlt(this)) <= 50 && getAltitude() <= getIls().getGsAlt() + 50) {
                        setGsCap(true);
                        setMissedAlt();
                    }
                } else {
                    setVerticalSpeed(-MathTools.nmToFeet((float) Math.tan(Math.toRadians(3)) * 140f / 60f));
                    setAltitude(getIls().getGSAlt(this));
                }
                if (nonPrecAlts != null) {
                    nonPrecAlts = null;
                }
            } else {
                if (isLocCap() && getClearedAltitude() != getIls().getMissedApchProc().getClimbAlt()) {
                    setMissedAlt();
                }
                if (nonPrecAlts == null) {
                    nonPrecAlts = new Queue<>();
                    Queue<float[]> copy = ((LDA) getIls()).getNonPrecAlts();
                    for (float[] data: copy) {
                        nonPrecAlts.addLast(data);
                    }
                }
                if (isLocCap()) {
                    if (nonPrecAlts != null && nonPrecAlts.size > 0) {
                        //Set target altitude to current restricted altitude
                        setTargetAltitude((int) nonPrecAlts.first()[0]);
                        while (nonPrecAlts.size > 0 && MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getX(), getIls().getY())) < nonPrecAlts.first()[1]) {
                            nonPrecAlts.removeFirst();
                        }
                        super.updateAltitude(false, false);
                    } else {
                        //Set final descent towards runway
                        setTargetAltitude(getIls().getRwy().getElevation());
                        float lineUpDist = ((LDA) getIls()).getLineUpDist();
                        float tmpAlt;
                        float actlTargetAlt;
                        tmpAlt = actlTargetAlt = ((LDA) getIls()).getImaginaryIls().getGSAltAtDist(lineUpDist);
                        actlTargetAlt -= 200;
                        actlTargetAlt = MathUtils.clamp(actlTargetAlt, (tmpAlt + getRunway().getElevation()) / 2, tmpAlt);
                        float remainingAlt = getAltitude() - actlTargetAlt;
                        Vector2 actlTargetPos = ((LDA) getIls()).getImaginaryIls().getPointAtDist(lineUpDist);
                        float distFromRwy = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), actlTargetPos.x, actlTargetPos.y));
                        setVerticalSpeed(-remainingAlt / (distFromRwy / getGs() * 60));
                        super.updateAltitude(remainingAlt < 0, true);
                    }
                } else {
                    super.updateAltitude(false, false);
                }
            }
            if (isLocCap()) {
                if (!goAroundSet) {
                    generateGoAround();
                    getIls().getRwy().addToArray(this);
                    goAroundSet = true;
                    setWakeTolerance(MathUtils.clamp(getWakeTolerance(), 0, 20));
                }
                checkAircraftInFront();
            }
            if (getIls() != null && getControlState() == ControlState.ARRIVAL && getAltitude() <= getAirport().getElevation() + 1300) {
                contactOther();
            }
            if (getAltitude() <= getIls().getRwy().getElevation() + 10 && getIls() != null) {
                setTkOfLdg(true);
                setOnGround(true);
                setHeading(getIls().getRwy().getHeading());
            }
            if (isLocCap() && checkGoAround()) {
                initializeGoAround();
            }
        } else {
            //If NPA not active yet
            if (nonPrecAlts != null) {
                nonPrecAlts = null;
            }
            goAroundSet = false;
            super.updateAltitude(holdAlt, fixedVs);
        }
        if (getControlState() != ControlState.ARRIVAL && getAltitude() <= contactAlt && getAltitude() > getAirport().getElevation() + 1300 && !divert && !isLocCap()) {
            setControlState(ControlState.ARRIVAL);
            radarScreen.getCommBox().initialContact(this);
            setActionRequired(true);
            getDataTag().startFlash();
        }
    }

    /** Checks when aircraft should contact tower */
    @Override
    public void contactOther() {
        //Contact the tower
        setControlState(ControlState.UNCONTROLLED);
        float points = 0.6f - radarScreen.getPlanesToControl() / 30;
        points = MathUtils.clamp(points, 0.15f, 0.5f);
        if (!getAirport().isCongested()) {
            radarScreen.setPlanesToControl(radarScreen.getPlanesToControl() + points);
        } else {
            radarScreen.setPlanesToControl(radarScreen.getPlanesToControl() - 0.4f);
        }
        radarScreen.getCommBox().contactFreq(this, getIls().getTowerFreq()[0], getIls().getTowerFreq()[1]);
    }

    @Override
    public boolean canHandover() {
        return getIls() != null && getControlState() == ControlState.ARRIVAL && isLocCap();
    }

    /** Called to check the distance behind the aircraft ahead of current aircraft, calls swap in runway array if it somehow overtakes it */
    private void checkAircraftInFront() {
        int approachPosition = getIls().getRwy().getAircraftsOnAppr().indexOf(this, false);
        if (approachPosition > 0) {
            Aircraft aircraftInFront = getIls().getRwy().getAircraftsOnAppr().get(approachPosition - 1);
            float targetX = getIls().getX();
            float targetY = getIls().getY();
            if (getIls() instanceof LDA) {
                targetX = getIls().getRwy().getOppRwy().getX();
                targetY = getIls().getRwy().getOppRwy().getY();
            }
            if (MathTools.distanceBetween(aircraftInFront.getX(), aircraftInFront.getY(), targetX, targetY) > MathTools.distanceBetween(getX(), getY(), targetX, targetY)) {
                //If this aircraft overtakes the one in front of it
                getIls().getRwy().swapAircrafts(this);
            }
        }
    }

    /** Gets the chances of aircraft going around due to external conditions, sets whether it will do so */
    private void generateGoAround() {
        float chance = 0;
        if ("ALL RWY".equals(getAirport().getWindshear())) {
            chance = 0.2f;
        } else if (!"None".equals(getAirport().getWindshear())) {
            for (String string: getAirport().getWindshear().split(" ")) {
                if (string.contains(getIls().getRwy().getName())) {
                    chance = 0.2f;
                    break;
                }
            }
        }
        if (chance > 0 && getAirport().getGusts() > -1) {
            chance += (1 - chance) * 1.2f * (getAirport().getGusts() - 15) / 100;
        }

        if (MathUtils.random(1f) < chance) {
            willGoAround = true;
            goAroundAlt = MathUtils.random(500, 1100);
        }
    }

    /** Checks whether the aircraft meets the criteria for going around, returns true if so */
    private boolean checkGoAround() {
        //Gonna split the returns into different segments just to make it easier to read
        if (willGoAround && getAltitude() < goAroundAlt && (getAirport().getWindshear().contains(getIls().getRwy().getName()) || getAirport().getWindshear().equals("ALL RWY"))) {
            //If go around is determined to happen due to windshear, and altitude is below go around alt, and windshear is still going on
            radarScreen.getCommBox().goAround(this, "windshear", getControlState());
            return true;
        }
        if (getWakeTolerance() > 25) {
            //If aircraft has reached wake limits
            radarScreen.getCommBox().goAround(this, "wake turbulence", getControlState());
            return true;
        }
        Aircraft firstAircraft = getIls().getRwy().getAircraftsOnAppr().size > 0 ? getIls().getRwy().getAircraftsOnAppr().get(0) : null;
        if (MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getRwy().getX(), getIls().getRwy().getY())) <= 3) {
            //If distance from runway is less than 3nm
            if (firstAircraft != null && !firstAircraft.getCallsign().equals(getCallsign()) && firstAircraft.getEmergency().isActive() && firstAircraft.getEmergency().isStayOnRwy()) {
                //If runway is closed due to emergency staying on runway
                radarScreen.getCommBox().goAround(this, "runway closed", getControlState());
                return true;
            }
            if (!(getIls() instanceof LDA) && !getIls().getName().contains("IMG") && !isGsCap()) {
                //If ILS GS has not been captured
                radarScreen.getCommBox().goAround(this, "being too high", getControlState());
                return true;
            } else if (getIas() - getApchSpd() > 10) {
                //If airspeed is more than 10 knots higher than approach speed
                radarScreen.getCommBox().goAround(this, "being too fast", getControlState());
                return true;
            } else if (!(getIls() instanceof LDA) && !getIls().getName().contains("IMG") && MathUtils.cosDeg(getIls().getRwy().getTrueHdg() - (float) getTrack()) < MathUtils.cosDeg(10)) {
                //If aircraft is not fully stabilised on LOC course
                radarScreen.getCommBox().goAround(this, "unstable approach", getControlState());
                return true;
            } else {
                int windDir = getAirport().getWinds()[0];
                int windSpd = getAirport().getWinds()[1];
                if (windSpd * MathUtils.cosDeg(windDir - getIls().getRwy().getHeading()) < -10) {
                    //If tailwind exceeds 10 knots
                    radarScreen.getCommBox().goAround(this, "strong tailwind", getControlState());
                    return true;
                }
            }
        }
        if (getAltitude() < getIls().getMinima() && getAirport().getVisibility() < MathTools.feetToMetre(getIls().getMinima() - getIls().getRwy().getElevation()) * 9) {
            //If altitude below minima and visibility is less than 9 times the minima (approx)
            radarScreen.getCommBox().goAround(this, "runway not in sight", getControlState());
            return true;
        }
        if (getAltitude() < getIls().getRwy().getElevation() + 150) {
            if (firstAircraft != null && !firstAircraft.getCallsign().equals(getCallsign())) {
                //If previous arrival/departure has not cleared runway by the time aircraft reaches 150 feet AGL
                radarScreen.getCommBox().goAround(this, "traffic on runway", getControlState());
                return true;
            } else {
                //If departure has not cleared runway
                Aircraft dep = getAirport().getTakeoffManager().getPrevAircraft().get(getIls().getRwy().getName());
                if (dep != null && dep.getAltitude() - getIls().getRwy().getElevation() < 10) {
                    radarScreen.getCommBox().goAround(this, "traffic on runway", getControlState());
                    return true;
                }
            }
        }
        return false;
    }

    /** Sets the cleared altitude for aircraft on approach, updates UI altitude selections if selected */
    private void setMissedAlt() {
        setClearedAltitude(getIls().getMissedApchProc().getClimbAlt());
        getNavState().replaceAllClearedAltMode();
        getNavState().replaceAllClearedAlt();
        if (isSelected() && getControlState() == ControlState.ARRIVAL) {
            ui.updateState();
        }
    }

    /** Updates the aircraft status when aircraft's tkOfLdg mode is active */
    @Override
    public void updateTkofLdg() {
        setAltitude(getIls() == null ? getAirport().getElevation() : getIls().getRwy().getElevation());
        setVerticalSpeed(0);
        setClearedIas(0);
        if (getGs() <= 35 && (!getEmergency().isActive() || !getEmergency().isStayOnRwy())) {
            int score = 1;
            if (radarScreen.getArrivals() >= 12) score = 2; //2 points if you're controlling at least 12 planes at a time
            if (getIls() != null && !getSidStar().getRunways().contains(getIls().getRwy().getName(), false)) score += 2; //2 additional points if landing runway is not intended for SID (i.e. runway change occurred)
            if ((getAirport().isCongested() && radarScreen.tfcMode != RadarScreen.TfcMode.ARRIVALS_ONLY) || getExpediteTime() > 120) score = 0; //Add score only if the airport is not congested, if mode is not arrival only, and aircraft has not expedited for >2 mins
            if (getEmergency().isEmergency()) {
                score = 5; //5 points for landing an emergency!
                UnlockManager.incrementEmergency();
            }
            radarScreen.setScore(radarScreen.getScore() + score);
            getAirport().setLandings(getAirport().getLandings() + 1);
            removeAircraft();
            if (getIls() != null) getIls().getRwy().removeFromArray(this);
            UnlockManager.incrementLanded();
            String[] typhoonList = new String[] {"TCTP", "TCSS", "TCTT", "TCAA", "TCHH", "TCMC"};
            if (ArrayUtils.contains(typhoonList, getAirport().getIcao()) && getAirport().getWinds()[1] >= 40) UnlockManager.completeAchievement("typhoon");
            if ("TCWS".equals(getAirport().getIcao()) && getAirport().getVisibility() <= 2500) UnlockManager.completeAchievement("haze");
        }
    }

    /** Overrides resetApchSpdSet method in Aircraft, called to reset the ilsSpdSet & finalSpdSet booleans */
    @Override
    public void resetApchSpdSet() {
        ilsSpdSet = false;
        finalSpdSet = false;
    }

    /** Overrides updateGoAround method in Aircraft, called to set aircraft status during go-arounds */
    @Override
    public void updateGoAround() {
        if (getAltitude() > getAirport().getElevation() + 1300 && isGoAround()) {
            setControlState(ControlState.ARRIVAL);
            setGoAround(false);
        }
    }

    /** Overrides initializeGoAround method in Aircraft, called to initialize go around mode of aircraft */
    private void initializeGoAround() {
        setGoAround(true);
        setGoAroundWindow(true);

        MissedApproach missedApproach = getIls().getMissedApchProc();

        setClearedHeading(getIls().getHeading());
        getNavState().getClearedHdg().removeFirst();
        getNavState().getClearedHdg().addFirst(getClearedHeading());

        setClearedIas(missedApproach.getClimbSpd());
        getNavState().getClearedSpd().removeFirst();
        getNavState().getClearedSpd().addFirst(getClearedIas());

        if (getClearedAltitude() <= missedApproach.getClimbAlt()) {
            setClearedAltitude(missedApproach.getClimbAlt());
            getNavState().getClearedAlt().removeFirst();
            getNavState().getClearedAlt().addFirst(getClearedAltitude());
        }
        setIls(null);
        getNavState().voidAllIls();
        ilsSpdSet = false;
        finalSpdSet = false;
        willGoAround = false;
        goAroundSet = false;
        if (isSelected() && getControlState() == ControlState.ARRIVAL) {
            ui.updateState();
        }
        getDataTag().setMinimized(false);
    }

    /** Check initial arrival spawn separation */
    private void checkArrival() {
        for (Aircraft aircraft : TerminalControl.radarScreen.aircrafts.values()) {
            if (aircraft == this || aircraft instanceof Departure) continue;
            if (getAltitude() - aircraft.getAltitude() < 2500 && MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), aircraft.getX(), aircraft.getY())) < 6) {
                if (getTypDes() - aircraft.getTypDes() > 300) {
                    setAltitude(aircraft.getAltitude() + 3500);
                } else {
                    setAltitude(aircraft.getAltitude() + 2500);
                }
                setClearedIas(aircraft.getClearedIas() > 250 ? 250 : aircraft.getClearedIas() - 10);
                setIas(getClearedIas());

                if (getClearedAltitude() < aircraft.getClearedAltitude() + 1000) {
                    setClearedAltitude(aircraft.getClearedAltitude() + 1000);
                }
            }
        }
    }

    @Override
    public void setIls(ILS ils) {
        if (this.getIls() != ils && (!(this.getIls() instanceof LDA) || ils == null)) goAroundSet = false; //Reset only if ILS is not LDA or ILS is LDA but new ILS is null
        super.setIls(ils);
    }

    @Override
    public SidStar getSidStar() {
        return star;
    }

    public Queue<float[]> getNonPrecAlts() {
        return nonPrecAlts;
    }

    public boolean isLowerSpdSet() {
        return lowerSpdSet;
    }

    public boolean isIlsSpdSet() {
        return ilsSpdSet;
    }

    public boolean isFinalSpdSet() {
        return finalSpdSet;
    }

    public boolean isWillGoAround() {
        return willGoAround;
    }

    public int getGoAroundAlt() {
        return goAroundAlt;
    }

    public boolean isGoAroundSet() {
        return goAroundSet;
    }

    public int getContactAlt() {
        return contactAlt;
    }

    public float getFuel() {
        return fuel;
    }

    public boolean isRequestPriority() {
        return requestPriority;
    }

    public boolean isDeclareEmergency() {
        return declareEmergency;
    }

    public boolean isDivert() {
        return divert;
    }

    public void setNonPrecAlts(Queue<float[]> nonPrecAlts) {
        this.nonPrecAlts = nonPrecAlts;
    }
}
