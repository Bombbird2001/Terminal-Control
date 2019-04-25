package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach;
import com.bombbird.terminalcontrol.entities.procedures.RandomSTAR;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.screens.ui.LatTab;
import com.bombbird.terminalcontrol.utilities.MathTools;
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
        star = RandomSTAR.randomSTAR(getAirport(), (String) arrival.getLandingRunways().keySet().toArray()[0]);

        if ("EVA226".equals(callsign) && radarScreen.tutorial) {
            star = arrival.getStars().get("TNN1A");
        }

        setDirect(star.getWaypoint(0));
        setHeading(star.getInboundHdg());

        setClearedHeading((int)getHeading());
        setTrack(getHeading() - TerminalControl.radarScreen.magHdgDev);

        //Calculate spawn border
        float[] point = MathTools.pointsAtBorder(new float[] {1310, 4450}, new float[] {50, 3190}, getDirect().getPosX(), getDirect().getPosY(), 180 + (float) getTrack());
        setX(point[0]);
        setY(point[1]);

        loadLabel();
        setNavState(new NavState(this));
        Waypoint maxAltWpt = null;
        Waypoint minAltWpt = null;
        for (Waypoint waypoint: getSidStar().getWaypoints()) {
            if (maxAltWpt == null && star.getWptMaxAlt(waypoint.getName()) > -1) {
                maxAltWpt = waypoint;
            }
            if (minAltWpt == null && star.getWptMinAlt(waypoint.getName()) > -1) {
                minAltWpt = waypoint;
            }
        }

        fuel = (45 + 10 + 10) * 60 + distToGo() / 250 * 3600 + 900 + MathUtils.random(-600, 600);

        float initAlt = 3000 + (distToGo() - 15) / 300 * 60 * getTypDes();
        if (maxAltWpt != null) {
            float maxAlt = star.getWptMaxAlt(maxAltWpt.getName()) + (distFromStartToPoint(maxAltWpt) - 5) / 300 * 60 * getTypDes();
            if (maxAlt < initAlt) initAlt = maxAlt;
        }
        if (initAlt > 28000) {
            initAlt = 28000;
        } else if (initAlt < 6000) {
            initAlt = 6000;
        } else if (minAltWpt != null && initAlt < star.getWptMinAlt(minAltWpt.getName())) {
            initAlt = star.getWptMinAlt(minAltWpt.getName());
        }
        for (Obstacle obstacle: radarScreen.obsArray) {
            if (obstacle.isIn(this) && initAlt < obstacle.getMinAlt()) {
                initAlt = obstacle.getMinAlt();
            }
        }
        for (RestrictedArea restrictedArea: radarScreen.restArray) {
            if (restrictedArea.isIn(this) && initAlt < restrictedArea.getMinAlt()) {
                initAlt = restrictedArea.getMinAlt();
            }
        }
        setAltitude(initAlt);
        updateAltRestrictions();
        if (initAlt > 15000) {
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

        if (getDirect() != null) {
            int spd = star.getWptMaxSpd(getDirect().getName());
            if (spd > -1) {
                setClearedIas(spd);
                setIas(spd);
            }
        }

        if (getAltitude() <= 10000 && (getClearedIas() > 250 || getIas() > 250)) {
            setClearedIas(250);
            setIas(250);
        }

        getNavState().getClearedSpd().removeFirst();
        getNavState().getClearedSpd().addFirst(getClearedIas());

        getNavState().getClearedAlt().removeLast();
        getNavState().getClearedAlt().addLast(getClearedAltitude());

        setControlState(0);
        setColor(new Color(0x00b3ffff));

        setHeading(update());
        setTrack(getHeading() - radarScreen.magHdgDev + updateTargetHeading()[1]);

        initRadarPos();
    }

    public Arrival(JSONObject save) {
        super(save);

        star = getAirport().getStars().get(save.getString("star"));

        if (save.isNull("nonPrecAlts")) {
            //If non precision alt is null
            nonPrecAlts = null;
        } else {
            JSONArray nonPrec = save.getJSONArray("nonPrecAlts");
            nonPrecAlts = new Queue<float[]>();
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
        setControlState(save.getInt("controlState"));

        loadOtherLabelInfo(save);
    }

    /** Calculates remaining distance on STAR from current aircraft position */
    private float distToGo() {
        float dist = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getDirect().getPosX(), getDirect().getPosY()));
        dist += ((Star) getSidStar()).distBetRemainPts(getSidStarIndex());
        return dist;
    }

    /** Calculates remaining distance on STAR from current start aircraft position to a certain point on it */
    private float distFromStartToPoint(Waypoint waypoint) {
        float dist = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getDirect().getPosX(), getDirect().getPosY()));
        int nextIndex = 1;
        if (getSidStar().getWaypoints().size > 1 && !getSidStar().getWaypoint(0).equals(waypoint)) {
            while (!getSidStar().getWaypoint(nextIndex).equals(waypoint)) {
                dist += ((Star) getSidStar()).distBetween(nextIndex - 1, nextIndex);
                nextIndex += 1;
            }
        }
        return dist;
    }

    /** Overrides method in Aircraft class to join the lines between each STAR waypoint */
    @Override
    public void drawSidStar() {
        super.drawSidStar();
        star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.getWaypoints().size, -1);
    }

    /** Overrides method in Aircraft class to join lines between each cleared STAR waypoint */
    @Override
    public void uiDrawSidStar() {
        super.uiDrawSidStar();
        star.joinLines(star.findWptIndex(LatTab.clearedWpt), star.getWaypoints().size, -1);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till afterWpt, then draws a heading line from there */
    @Override
    public void drawAftWpt() {
        super.drawAftWpt();
        star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.findWptIndex(getNavState().getClearedAftWpt().last().getName()) + 1, getNavState().getClearedAftWptHdg().last());
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected afterWpt, then draws a heading line from there */
    @Override
    public void uiDrawAftWpt() {
        super.uiDrawAftWpt();
        star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.findWptIndex(LatTab.afterWpt) + 1, LatTab.afterWptHdg);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till holdWpt */
    @Override
    public void drawHoldPattern() {
        super.drawHoldPattern();
        radarScreen.shapeRenderer.setColor(Color.WHITE);
        if (getNavState().getClearedHold().size > 0 && getNavState().getClearedHold().last() != null && getNavState().getClearedDirect().size > 0 && getNavState().getClearedDirect().last() != null) {
            star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.findWptIndex(getNavState().getClearedHold().last().getName()) + 1, -1);
        }
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected holdWpt */
    @Override
    public void uiDrawHoldPattern() {
        super.uiDrawHoldPattern();
        radarScreen.shapeRenderer.setColor(Color.YELLOW);
        star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.findWptIndex(LatTab.holdWpt) + 1, -1);
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
            if (getClearedIas() > 200) {
                setClearedIas(200);
                super.updateSpd();
            }
            ilsSpdSet = true;
        }
        if (!finalSpdSet && isLocCap() && MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getRwy().getX(), getIls().getRwy().getY())) <= 6) {
            if (getClearedIas() > getApchSpd()) {
                setClearedIas(getApchSpd());
                super.updateSpd();
            }
            finalSpdSet = true;
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
        if (getNavState().getDispLatMode().first().contains("arrival") || getNavState().getDispLatMode().first().contains("waypoint") || ("Hold at".equals(getNavState().getDispLatMode().first()) && !isHolding())) {
            //Aircraft on STAR
            int highestAlt = -1;
            int lowestAlt = -1;
            if (getDirect() != null) {
                highestAlt = getSidStar().getWptMaxAlt(getDirect().getName());
                lowestAlt = getSidStar().getWptMinAlt(getDirect().getName());
            }
            setHighestAlt(highestAlt > -1 ? highestAlt : radarScreen.maxAlt);
            setLowestAlt(lowestAlt > -1 ? lowestAlt : radarScreen.minAlt);
        }
    }

    /** Overrides update method in Aircraft to include updating fuel time */
    @Override
    public double update() {
        double info = super.update();

        if (!isOnGround()) {
            updateFuel();
        }

        return info;
    }

    /** Updates the fuel time for arrival */
    private void updateFuel() {
        fuel -= Gdx.graphics.getDeltaTime();

        if (fuel < 2700 && !requestPriority && getControlState() == 1) {
            //Low fuel, request priority
            radarScreen.getCommBox().warningMsg("Pan-pan, pan-pan, pan-pan, " + getCallsign() + " is low on fuel and requests priority landing.");
            requestPriority = true;
            TerminalControl.tts.lowFuel(getVoice(), 0, getCallsign().substring(0, 3), getCallsign().substring(3), getWakeCat());

            setActionRequired(true);
            getDataTag().flashIcon();
        }

        if (fuel < 2100 && !declareEmergency && getControlState() == 1) {
            //Minimum fuel, declare emergency
            radarScreen.getCommBox().warningMsg("Mayday, mayday, mayday, " + getCallsign() + " requests immediate landing within 10 minutes or will divert.");
            declareEmergency = true;
            radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.9f));
            TerminalControl.tts.lowFuel(getVoice(), 1, getCallsign().substring(0, 3), getCallsign().substring(3), getWakeCat());

            if (!isEmergency()) setEmergency(true);
            getDataTag().setEmergency();
        }

        if (fuel < 1500 && !divert && !isLocCap() && getControlState() == 1) {
            //Diverting to alternate
            radarScreen.getCommBox().warningMsg(getCallsign() + " is diverting to the alternate airport.");

            getNavState().getDispLatMode().clear();
            getNavState().getDispLatMode().addFirst("Fly heading");
            getNavState().getDispAltMode().clear();
            getNavState().getDispAltMode().addFirst("Climb/descend to");
            getNavState().getDispSpdMode().clear();
            getNavState().getDispSpdMode().addFirst("No speed restrictions");

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

            setControlState(0);

            divert = true;
            radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.9f));
            TerminalControl.tts.lowFuel(getVoice(), 2, getCallsign().substring(0, 3), getCallsign().substring(3), getWakeCat());
        }
    }

    /** Overrides updateAltitude method in Aircraft for when arrival is on glide slope or non precision approach */
    @Override
    public void updateAltitude(boolean holdAlt, boolean fixedVs) {
        if (getIls() != null) {
            if (!(getIls() instanceof LDA) || !((LDA) getIls()).isNpa()) {
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
                    nonPrecAlts = new Queue<float[]>();
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
                        float actlTargetAlt = ((LDA) getIls()).getImaginaryIls().getGSAltAtDist(lineUpDist);
                        actlTargetAlt = actlTargetAlt < getRunway().getElevation() + 300 ? getRunway().getElevation() + 300 : actlTargetAlt;
                        float remainingAlt = getAltitude() - actlTargetAlt + 200;
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
                }
                checkAircraftInFront();
            }
            if (getIls() != null && getControlState() == 1 && getAltitude() <= getAirport().getElevation() + 1300) {
                contactOther();
            }
            if (getAltitude() <= getIls().getRwy().getElevation() + 10) {
                setTkOfLdg(true);
                setOnGround(true);
                setHeading(getIls().getRwy().getHeading());
            }
            if (checkGoAround()) {
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
        if (getControlState() != 1 && getAltitude() <= contactAlt && getAltitude() > getAirport().getElevation() + 1300 && !divert) {
            setControlState(1);
            radarScreen.getCommBox().initialContact(this);
            setActionRequired(true);
            getDataTag().flashIcon();
        }
    }

    /** Checks when aircraft should contact tower */
    @Override
    public void contactOther() {
        //Contact the tower
        setControlState(0);
        setClearedIas(getApchSpd());
        float points = 0.7f - radarScreen.getPlanesToControl() / 30;
        points = MathUtils.clamp(points, 0.2f, 0.6f);
        if (!getAirport().isCongested()) {
            radarScreen.setPlanesToControl(radarScreen.getPlanesToControl() + points);
        } else {
            radarScreen.setPlanesToControl(radarScreen.getPlanesToControl() - 0.4f);
        }
        radarScreen.setArrivals(radarScreen.getArrivals() - 1);
        radarScreen.getCommBox().contactFreq(this, getIls().getTowerFreq()[0], getIls().getTowerFreq()[1]);
    }

    /** Called to check the distance behind the aircraft ahead of current aircraft, calls swap in runway array if it somehow overtakes it */
    private void checkAircraftInFront() {
        int approachPosition = getIls().getRwy().getAircraftsOnAppr().indexOf(this, false);
        if (approachPosition > 0) {
            Aircraft aircraftInFront = getIls().getRwy().getAircraftsOnAppr().get(approachPosition - 1);
            if (MathTools.distanceBetween(aircraftInFront.getX(), aircraftInFront.getY(), getIls().getX(), getIls().getY()) > MathTools.distanceBetween(getX(), getY(), getIls().getX(), getIls().getY())) {
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
            radarScreen.getCommBox().goAround(this, "windshear");
            return true;
        }
        if (MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getRwy().getX(), getIls().getRwy().getY())) <= 3) {
            //If distance from runway is less than 3nm
            if (!(getIls() instanceof LDA) && !getIls().getName().contains("IMG") && !isGsCap()) {
                //If ILS GS has not been captured
                radarScreen.getCommBox().goAround(this, "being too high");
                return true;
            } else if (getIas() - getApchSpd() > 10) {
                //If airspeed is more than 10 knots higher than approach speed
                radarScreen.getCommBox().goAround(this, "being too fast");
                return true;
            } else if (!(getIls() instanceof LDA) && !getIls().getName().contains("IMG") && MathUtils.cosDeg(getIls().getRwy().getTrueHdg() - (float) getTrack()) < MathUtils.cosDeg(10)) {
                //If aircraft is not fully stabilised on LOC course
                radarScreen.getCommBox().goAround(this, "unstable approach");
                return true;
            } else {
                int windDir = getAirport().getWinds()[0];
                int windSpd = getAirport().getWinds()[1];
                if (windSpd * MathUtils.cosDeg(windDir - getIls().getRwy().getHeading()) < -10) {
                    //If tailwind exceeds 10 knots
                    radarScreen.getCommBox().goAround(this, "strong tailwind");
                    return true;
                }
            }
        }
        if (getAltitude() < getIls().getMinima() && getAirport().getVisibility() < MathTools.feetToMetre(getIls().getMinima() - getIls().getRwy().getElevation()) * 9) {
            //If altitude below minima and visibility is less than 9 times the minima (approx)
            radarScreen.getCommBox().goAround(this, "runway not in sight");
            return true;
        }
        if (getAltitude() < getIls().getRwy().getElevation() + 150) {
            if (getIls().getRwy().getAircraftsOnAppr().indexOf(this, false) > 0) {
                //If previous arrival/departure has not cleared runway by the time aircraft reaches 150 feet AGL
                radarScreen.getCommBox().goAround(this, "traffic on runway");
                return true;
            } else {
                //If departure has not cleared runway
                Aircraft dep = getAirport().getTakeoffManager().getPrevAircraft().get(getIls().getRwy().getName());
                if (dep != null && dep.getAltitude() - getIls().getRwy().getElevation() < 10) {
                    radarScreen.getCommBox().goAround(this, "traffic on runway");
                    return true;
                }
            }
        }
        return false;
    }

    /** Sets the cleared altitude for aircrafts on approach, updates UI altitude selections if selected */
    private void setMissedAlt() {
        setClearedAltitude(getIls().getMissedApchProc().getClimbAlt());
        getNavState().replaceAllClearedAltMode();
        getNavState().replaceAllClearedAlt();
        if (isSelected() && getControlState() == 1) {
            ui.updateState();
        }
    }

    /** Updates the aircraft status when aircraft's tkOfLdg mode is active */
    @Override
    public void updateTkofLdg() {
        setAltitude(getIls().getRwy().getElevation());
        setVerticalSpeed(0);
        setClearedIas(0);
        if (getGs() <= 35) {
            int score = 1;
            if (!getSidStar().getRunways().contains(getIls().getRwy().getName(), false)) score = 3; //3 points if landing runway is not intended for SID (i.e. runway change occured)
            if (!getAirport().isCongested() && getExpediteTime() <= 120) radarScreen.setScore(radarScreen.getScore() + score); //Add score only if the airport is not congested
            getAirport().setLandings(getAirport().getLandings() + 1);
            removeAircraft();
            getIls().getRwy().removeFromArray(this);
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
        if (getAltitude() >= 1600 && getControlState() == 0) {
            setControlState(1);
            setGoAround(false);
            radarScreen.setArrivals(radarScreen.getArrivals() + 1);
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
        if (isSelected() && getControlState() == 2) {
            ui.updateState();
        }
    }

    @Override
    public void setIls(ILS ils) {
        if (this.getIls() != ils) goAroundSet = false;
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
}
