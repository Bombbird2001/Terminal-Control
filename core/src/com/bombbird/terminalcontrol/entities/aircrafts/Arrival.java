package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.screens.ui.LatTab;
import com.bombbird.terminalcontrol.utilities.MathTools;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Arrival extends Aircraft {
    //Others
    private int contactAlt;
    private Star star;
    private Queue<int[]> nonPrecAlts;
    private boolean lowerSpdSet;
    private boolean ilsSpdSet;
    private boolean finalSpdSet;

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
        HashMap<String, Star> starList = getAirport().getStars();
        boolean starSet = false;
        while (!starSet) {
            String starStr = (String) starList.keySet().toArray()[MathUtils.random(starList.size() - 1)];
            star = starList.get(starStr);
            for (String runway: star.getRunways()) {
                if (arrival.getLandingRunways().containsKey(runway)) {
                    starSet = true;
                    break;
                }
            }
        }

        if ("EVA226".equals(callsign) && radarScreen.tutorial) {
            star = starList.get("TNN1A");
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
        float initAlt = 3000 + (distToGo() - 15) / 300 * 60 * getTypDes();
        int limit = 28000;
        if (getDirect() != null && star.getWptMaxAlt(getDirect().getName()) > -1) {
            limit = star.getWptMaxAlt(getDirect().getName());
        }
        if (initAlt > limit) {
            initAlt = limit;
        } else if (initAlt < 6000) {
            initAlt = 6000;
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
            nonPrecAlts = new Queue<int[]>();
            for (int i = 0; i < nonPrec.length(); i++) {
                JSONArray data = nonPrec.getJSONArray(i);
                nonPrecAlts.addLast(new int[] {data.getInt(0), data.getInt(1)});
            }
        }

        lowerSpdSet = save.getBoolean("lowerSpdSet");
        ilsSpdSet = save.getBoolean("ilsSpdSet");
        finalSpdSet = save.getBoolean("finalSpdSet");
        willGoAround = save.getBoolean("willGoAround");
        goAroundAlt = save.getInt("goAroundAlt");
        goAroundSet = save.getBoolean("goAroundSet");
        contactAlt = save.getInt("contactAlt");

        loadLabel();
        setColor(new Color(0x00b3ffff));
        setControlState(save.getInt("controlState"));

        if (!save.isNull("labelPos")) {
            JSONArray labelPos = save.getJSONArray("labelPos");
            getLabel().setPosition((float) labelPos.getDouble(0), (float) labelPos.getDouble(1));
        }
    }

    /** Calculates remaining distance on STAR from current aircraft position */
    private float distToGo() {
        float dist = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getDirect().getPosX(), getDirect().getPosY()));
        dist += ((Star) getSidStar()).distBetRemainPts(getSidStarIndex());
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

    /** Overrides method in Aircraft class to update label + update STAR name */
    @Override
    public void updateLabel() {
        getLabelText()[8] = star.getName();
        super.updateLabel();
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

    /** Overrides updateAltitude method in Aircraft for when arrival is on glide slope or non precision approach */
    @Override
    public void updateAltitude() {
        if (getIls() != null) {
            if (!(getIls() instanceof LDA) || !((LDA) getIls()).isNpa()) {
                if (!isGsCap()) {
                    super.updateAltitude();
                    if (isLocCap() && Math.abs(getAltitude() - getIls().getGSAlt(this)) <= 50 && getAltitude() <= getIls().getGsAlt() + 50) {
                        setGsCap(true);
                        setMissedAlt(); //TODO Reproduce & fix bug where altitude is set to go around alt when GS not captured
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
                    nonPrecAlts = new Queue<int[]>();
                    Queue<int[]> copy = ((LDA) getIls()).getNonPrecAlts();
                    for (int[] data: copy) {
                        nonPrecAlts.addLast(data);
                    }
                }
                if (isLocCap()) {
                    if (nonPrecAlts != null && nonPrecAlts.size > 0) {
                        //Set target altitude to current restricted altitude
                        setTargetAltitude(nonPrecAlts.first()[0]);
                        while (nonPrecAlts.size > 0 && MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getX(), getIls().getY())) < nonPrecAlts.first()[1]) {
                            nonPrecAlts.removeFirst();
                        }
                        super.updateAltitude();
                    } else {
                        //Set final descent towards runway
                        setTargetAltitude(getIls().getRwy().getElevation());
                        float remainingAlt = getAltitude() - getIls().getRwy().getElevation();
                        float distFromRwy = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getX(), getIls().getY()));
                        setVerticalSpeed(-remainingAlt / distFromRwy * getGs() / 60);
                        setAltitude(getAltitude() + getVerticalSpeed() / 60 * Gdx.graphics.getDeltaTime());
                    }
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
            if (getControlState() == 1 && getAltitude() <= getAirport().getElevation() + 1300) {
                //Contact the tower
                setControlState(0);
                setClearedIas(getApchSpd());
                float points = 0.7f - radarScreen.getPlanesToControl() / 40;
                points = MathUtils.clamp(points, 0.15f, 0.6f);
                radarScreen.setPlanesToControl(radarScreen.getPlanesToControl() + points);
                radarScreen.setArrivals(radarScreen.getArrivals() - 1);
                radarScreen.getCommBox().contactFreq(this, getIls().getTowerFreq()[0], getIls().getTowerFreq()[1]);
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
            super.updateAltitude();
        }
        if (getControlState() != 1 && getAltitude() <= contactAlt && getAltitude() > getAirport().getElevation() + 1300) {
            setControlState(1);
            radarScreen.getCommBox().initialContact(this);
        }
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
        if (isSelected()) {
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
            if (!getAirport().isCongested() && getExpediteTime() <= 120) radarScreen.setScore(radarScreen.getScore() + 1); //Add score only if the airport is not congested
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
    public SidStar getSidStar() {
        return star;
    }

    public Queue<int[]> getNonPrecAlts() {
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
}
