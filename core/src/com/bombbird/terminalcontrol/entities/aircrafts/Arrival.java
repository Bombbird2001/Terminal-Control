package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.screens.ui.LatTab;
import com.bombbird.terminalcontrol.utilities.MathTools;

import java.util.HashMap;

public class Arrival extends Aircraft {
    //Others
    private Star star;
    private Queue<int[]> nonPrecAlts;
    private boolean lowerSpdSet;

    //For go around
    //private boolean transSet;
    private MissedApproach missedApproach;
    //private float lastDistFromRwy;
    //private Queue<String> goAroundQueue;
    //private int goAroundDir;

    public Arrival(String callsign, String icaoType, Airport arrival) {
        super(callsign, icaoType, arrival);
        setOnGround(false);
        lowerSpdSet = false;
        //transSet = false;
        //goAroundDir = 0;

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

        if (callsign.equals("EVA226")) {
            star = starList.get("BAKER1A");
        }

        setDirect(star.getWaypoint(0));
        setHeading(star.getInboundHdg());

        setClearedHeading((int)getHeading());
        setTrack(getHeading() - RadarScreen.magHdgDev);

        //Calculate spawn border
        int[] xBorder = {1310, 4450};
        int[] yBorder = {50, 3190};
        float xDistRight = (xBorder[1] - getDirect().getPosX())/MathUtils.cosDeg((float)(270 - getTrack()));
        float xDistLeft = (xBorder[0] - getDirect().getPosX())/MathUtils.cosDeg((float)(270 - getTrack()));
        float yDistUp = (yBorder[1] - getDirect().getPosY())/MathUtils.sinDeg((float)(270 - getTrack()));
        float yDistDown = (yBorder[0] - getDirect().getPosY())/MathUtils.sinDeg((float)(270 - getTrack()));
        float xDist = xDistRight > 0 ? xDistRight : xDistLeft;
        float yDist = yDistUp > 0 ? yDistUp : yDistDown;
        float dist = xDist > yDist ? yDist : xDist;
        setX(getDirect().getPosX() + dist * MathUtils.cosDeg((float)(270 - getTrack())));
        setY(getDirect().getPosY() + dist * MathUtils.sinDeg((float)(270 - getTrack())));

        loadLabel();
        setNavState(new NavState(1, this));
        float initAlt = 3000 + (distToGo() - 20) / 300 * 60 * getTypDes();
        if (initAlt > 26000) {
            initAlt = 26000;
        } else if (initAlt < 6000) {
            initAlt = 6000;
        }
        setAltitude(initAlt);
        updateAltRestrictions();
        if (initAlt > 11000) {
            setClearedAltitude(11000);
        } else {
            setClearedAltitude((int) initAlt - (int) initAlt % 1000);
        }

        if (callsign.equals("EVA226")) {
            getNavState().getDispAltMode().removeFirst();
            getNavState().getDispAltMode().addFirst("Climb/descend to");
            getNavState().getDispLatMode().removeFirst();
            getNavState().getDispLatMode().addFirst("Fly heading");
            getNavState().getDispSpdMode().removeFirst();
            getNavState().getDispSpdMode().addFirst("No speed restrictions");
            setHeading(54);
            setClearedHeading(54);
            setAltitude(4000);
            setClearedAltitude(4000);
            setX(2394);
            setY(1296);
        }

        getNavState().getClearedAlt().removeLast();
        getNavState().getClearedAlt().addLast(getClearedAltitude());

        setControlState(1);
        setColor(new Color(0x00b3ffff));

        setHeading(update());
        setTrack(getHeading() - RadarScreen.magHdgDev + updateTargetHeading()[1]);

        initRadarPos();
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
        star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.getWaypoints().size, -1, false);
    }

    /** Overrides method in Aircraft class to join lines between each cleared STAR waypoint */
    @Override
    public void uiDrawSidStar() {
        super.uiDrawSidStar();
        star.joinLines(star.findWptIndex(LatTab.clearedWpt), star.getWaypoints().size, -1, getNavState().getDispLatMode().last().contains("arrival"));
    }

    /** Overrides method in Aircraft class to join lines between waypoints till afterWpt, then draws a heading line from there */
    @Override
    public void drawAftWpt() {
        super.drawAftWpt();
        star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.findWptIndex(getNavState().getClearedAftWpt().last().getName()) + 1, getNavState().getClearedAftWptHdg().last(), false);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected afterWpt, then draws a heading line from there */
    @Override
    public void uiDrawAftWpt() {
        super.uiDrawAftWpt();
        star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.findWptIndex(LatTab.afterWpt) + 1, LatTab.afterWptHdg, true);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till holdWpt */
    @Override
    public void drawHoldPattern() {
        super.drawHoldPattern();
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        if (getNavState().getClearedHold().size > 0 && getNavState().getClearedHold().last() != null && getNavState().getClearedDirect().size > 0 && getNavState().getClearedDirect().last() != null) {
            star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.findWptIndex(getNavState().getClearedHold().last().getName()) + 1, -1, false);
        }
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected holdWpt */
    @Override
    public void uiDrawHoldPattern() {
        super.uiDrawHoldPattern();
        GameScreen.shapeRenderer.setColor(Color.YELLOW);
        star.joinLines(star.findWptIndex(getNavState().getClearedDirect().last().getName()), star.findWptIndex(LatTab.holdWpt) + 1, -1, true);
    }

    /** Overrides method in Aircraft class to update label + update STAR name */
    @Override
    public void updateLabel() {
        labelText[8] = star.getName();
        super.updateLabel();
    }

    /** Overrides method in Aircraft class to set to current heading*/
    @Override
    public void setAfterLastWpt() {
        getNavState().getLatModes().removeValue(star.getName() + " arrival", false);
        getNavState().getLatModes().removeValue("After waypoint, fly heading", false);
        getNavState().getLatModes().removeValue("Hold at", false);
        setClearedHeading((int) getHeading());
        getNavState().getClearedHdg().removeFirst();
        getNavState().getClearedHdg().addFirst(getClearedHeading());
        updateVectorMode();
        removeSidStarMode();
    }

    /** Overrides updateSpd method in Aircraft, for setting the aircraft speed to 220 knots when within 20nm track miles if clearedIas > 220 knots */
    @Override
    public void updateSpd() {
        if (!lowerSpdSet && getDirect() != null && distToGo() <= 20) {
            if (getClearedIas() > 220) {
                setClearedIas(220);
                super.updateSpd();
            }
            lowerSpdSet = true;
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
        if (getNavState().getDispLatMode().first().contains("arrival") || getNavState().getDispLatMode().first().contains("waypoint")) {
            //Aircraft on STAR
            int highestAlt = -1;
            int lowestAlt = -1;
            if (getDirect() != null) {
                highestAlt = getSidStar().getWptMaxAlt(getDirect().getName());
                lowestAlt = getSidStar().getWptMinAlt(getDirect().getName());
            }
            setHighestAlt(highestAlt > -1 ? highestAlt : RadarScreen.maxArrAlt);
            setLowestAlt(lowestAlt > -1 ? lowestAlt : RadarScreen.minArrAlt);
        }
    }

    /** Overrides updateAltitude method in Aircraft for when arrival is on glide slope or non precision approach */
    @Override
    public void updateAltitude() {
        if (getIls() != null) {
            if (!(getIls() instanceof LDA)) {
                if (!isGsCap()) {
                    super.updateAltitude();
                    if (getIls().isInsideILS(getX(), getY()) && Math.abs(getAltitude() - getIls().getGSAlt(this)) <= 50) {
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
                    nonPrecAlts = new Queue<int[]>();
                    Queue<int[]> copy = ((LDA) getIls()).getNonPrecAlts();
                    for (int[] data: copy) {
                        nonPrecAlts.addLast(data);
                    }
                }
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
            if (getControlState() == 1 && getAltitude() <= getAirport().getElevation() + 1400) {
                //Contact the tower
                setControlState(0);
                //TODO Add contact tower transmission
            }
            if (getAltitude() <= getIls().getRwy().getElevation() + 10) {
                setTkofLdg(true);
                setOnGround(true);
                setHeading(getIls().getRwy().getHeading());
            }
            if (getCallsign().equals("EVA226") && getAltitude() <= 800) {
                initializeGoAround();
            }
        } else {
            //If GS/NPA not active yet
            if (nonPrecAlts != null) {
                nonPrecAlts = null;
            }
            super.updateAltitude();
        }
    }

    /** Sets the cleared altitude for aircrafts on approach, updates UI altitude selections if selected */
    private void setMissedAlt() {
        setClearedAltitude(getIls().getMissedApchProc().getClimbAlt());
        if (isSelected()) {
            GameScreen.ui.updateState();
        }
    }

    /** Updates the aircraft status when aircraft's tkOfLdg mode is active */
    @Override
    public void updateTkofLdg() {
        setAltitude(getIls().getRwy().getElevation());
        setVerticalSpeed(0);
        setClearedIas(0);
        if (getGs() <= 35) {
            removeAircraft();
        }
    }

    /** Overrides updateGoAround method in Aircraft, called to set aircraft status during go-arounds */
    @Override
    public void updateGoAround() {
        if (getAltitude() >= 1600 && getControlState() == 0) {
            setControlState(1);
            setGoAround(false);
        }
        /*
        //Check if aircraft has passed initial climb phase
        float dist = MathTools.distanceBetween(getX(), getY(), missedApproach.getIls().getX(), missedApproach.getIls().getY());
        if (!transSet) {
            if (missedApproach.getTransition().equals("ALT")) {
                //Check if aircraft has climbed above this altitude
                if (getAltitude() > missedApproach.getTransInfo() && dist > lastDistFromRwy) {
                    transSet = true;
                }
            } else if (missedApproach.getTransition().equals("DIST")) {
                //Check if aircraft has flew past this distance from end of runway
                if (dist > missedApproach.getTransInfo() && dist > lastDistFromRwy) {
                    //Make sure aircraft is flying from (not towards) the end of the runway
                    transSet = true;
                }
            }
        }
        */
    }


    /*
    /** Overrides updateGoAroundDirect method in Aircraft, called to update aircraft action after reaching the next waypoint in the waypoint queue
    @Override
    public void updateGoAroundDirect() {
        if (goAroundQueue.size > 0) {
            String[] info = goAroundQueue.first().split(" ");
            if (info[0].equals("ANY")) {
                goAroundDir = 0;
            } else if (info[0].equals("LEFT")) {
                goAroundDir = 1;
            } else if (info[0].equals("RIGHT")) {
                goAroundDir = 2;
            } else {
                Gdx.app.log("Go around direction", "Invalid go around direction " + info[0] + " specified!");
            }

        } else {
            //Last waypoint reached, check for holding pattern TODO
        }
    }
    */

    /** Overrides initializeGoAround method in Aircraft, called to initialize go around mode of aircraft */
    private void initializeGoAround() {
        setGoAround(true);
        missedApproach = getIls().getMissedApchProc();
        setClearedHeading(getIls().getHeading());
        setClearedIas(missedApproach.getClimbSpd());
        setClearedAltitude(missedApproach.getClimbAlt());
        //lastDistFromRwy = MathTools.distanceBetween(getX(), getY(), getIls().getX(), getIls().getY());
        //goAroundQueue = missedApproach.getProcedure();
        setIls(null);
        if (isSelected() && getControlState() == 2) {
            GameScreen.ui.updateState();
        }
    }

    @Override
    public SidStar getSidStar() {
        return star;
    }

    /*
    public boolean isTransSet() {
        return transSet;
    }
    */
}
