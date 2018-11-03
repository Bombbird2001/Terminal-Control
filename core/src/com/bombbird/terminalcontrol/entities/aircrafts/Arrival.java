package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.screens.ui.LatTab;
import com.bombbird.terminalcontrol.utilities.MathTools;

import java.util.HashMap;

public class Arrival extends Aircraft {
    //Others
    private Star star;
    private Queue<int[]> nonPrecAlts;

    public Arrival(String callsign, String icaoType, Airport arrival) {
        super(callsign, icaoType, arrival);
        setOnGround(false);
        setLatMode("sidstar");

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

        if (getAirport().getIcao().equals("RCSS")) {
            star = starList.get("KUDOS1B");
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
            setLatMode("vector");
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
        star.joinLines(star.findWptIndex(LatTab.clearedWpt), star.getWaypoints().size, -1, true);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till afterWpt, then draws a heading line from there */
    @Override
    public void drawAftWpt() {
        super.drawAftWpt();
        star.joinLines(getSidStarIndex(), star.findWptIndex(getNavState().getClearedAftWpt().last().getName()) + 1, getNavState().getClearedAftWptHdg().last(), false);
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected afterWpt, then draws a heading line from there */
    @Override
    public void uiDrawAftWpt() {
        super.uiDrawAftWpt();
        star.joinLines(getSidStarIndex(), star.findWptIndex(LatTab.afterWpt) + 1, LatTab.afterWptHdg, true);
    }

    /** Overrides method in Aircraft class to update label + update STAR name */
    @Override
    public void updateLabel() {
        labelText[8] = star.getName();
        super.updateLabel();
    }

    /** Overrides method in Aircraft class to check if there is no direct waypoint next, sets to current heading if so */
    @Override
    public void updateDirect() {
        super.updateDirect();
        if (getDirect() == null) {
            setClearedHeading((int) getHeading());
            getNavState().getClearedHdg().removeFirst();
            getNavState().getClearedHdg().addFirst(getClearedHeading());
            updateVectorMode();
            removeSidStarMode();
        }
    }

    @Override
    public void updateSpd() {
        if (getDirect() != null && distToGo() <= 20 && getClearedIas() > 220) {
            setClearedIas(220);
            super.updateSpd();
        }
    }

    @Override
    public double findNextTargetHdg() {
        double result = super.findNextTargetHdg();
        if (result < -0.5) {
            return getHeading();
        } else {
            return result;
        }
    }

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
            if (highestAlt > -1) {
                setHighestAlt(highestAlt);
            } else {
                setHighestAlt(RadarScreen.maxArrAlt);
            }
            if (lowestAlt > -1) {
                setLowestAlt(lowestAlt);
            } else {
                setLowestAlt(RadarScreen.minArrAlt);
            }
        }
    }

    @Override
    public void updateAltitude() {
        if (getIls() != null) {
            if (!(getIls() instanceof LDA)) {
                if (!isGsCap()) {
                    super.updateAltitude();
                    if (getIls().isInsideILS(getX(), getY()) && Math.abs(getAltitude() - getIls().getGSAlt(this)) <= 50) {
                        setGsCap(true);
                    }
                } else {
                    setVerticalSpeed(-MathTools.nmToFeet((float) Math.tan(Math.toRadians(3)) * 140f / 60f));
                    setAltitude(getIls().getGSAlt(this));
                    if (getControlState() == 1 && getAltitude() <= getAirport().getElevation() + 1400) {
                        //Contact the tower
                        setControlState(0);
                        //TODO Add contact tower transmission
                    }
                }
            } else if (nonPrecAlts != null && nonPrecAlts.size > 0) {
                //Set target altitude to current restricted altitude
                setTargetAltitude(nonPrecAlts.first()[0]);
                while (nonPrecAlts.size > 0 && MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getX(), getIls().getY())) < nonPrecAlts.first()[1]) {
                    nonPrecAlts.removeFirst();
                }
                super.updateAltitude();
            } else {
                //TODO Fix bug where aircraft enters this mode prematurely
                //Set final descent towards runway
                setTargetAltitude(getIls().getRwy().getElevation());
                float remainingAlt = getAltitude() - getIls().getRwy().getElevation();
                float distFromRwy = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getIls().getX(), getIls().getY()));
                setVerticalSpeed(-remainingAlt / distFromRwy * getGs() / 60);
                setAltitude(getAltitude() + getVerticalSpeed() / 60 * Gdx.graphics.getDeltaTime());
                System.out.println("Vert spd: " + getVerticalSpeed());
            }
            if (getAltitude() <= getIls().getRwy().getElevation() + 10) {
                setTkofLdg(true);
                setOnGround(true);
            }
        } else {
            super.updateAltitude();
        }
    }

    @Override
    public void updateTargetAltitude() {
        if (getIls() instanceof LDA && isLocCap()) {
            if (nonPrecAlts == null) {
                nonPrecAlts = new Queue<int[]>();
                Queue<int[]> copy = ((LDA) getIls()).getNonPrecAlts();
                for (int[] data: copy) {
                    nonPrecAlts.addLast(data);
                }
            }
        } else {
            if (nonPrecAlts != null) {
                nonPrecAlts = null;
            }
            super.updateTargetAltitude();
        }
    }

    @Override
    public void updateTkofLdg() {
        setAltitude(getIls().getRwy().getElevation());
        setVerticalSpeed(0);
        setTargetIas(0);
        if (getGs() <= 35) {
            removeAircraft();
        }
    }

    @Override
    public SidStar getSidStar() {
        return star;
    }
}
