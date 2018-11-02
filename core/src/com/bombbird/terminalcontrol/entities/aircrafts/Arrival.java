package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

import java.util.HashMap;

public class Arrival extends Aircraft {
    //Others
    private Star star;

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
        setAltitude(2000 + (distToGo() - 25) / 325 * 60 * getTypDes());
        updateAltRestrictions();
        setClearedAltitude(11000);

        if (callsign.equals("EVA226")) {
            getNavState().getDispAltMode().removeFirst();
            getNavState().getDispAltMode().addFirst("Climb/descend to");
            getNavState().getDispLatMode().removeFirst();
            getNavState().getDispLatMode().addFirst("Fly heading");
            getNavState().getDispSpdMode().removeFirst();
            getNavState().getDispSpdMode().addFirst("No speed restrictions");
            setLatMode("vector");
            setAltMode("open");
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

    private float distToGo() {
        float dist = MathTools.pixelToNm(MathTools.distanceBetween(getX(), getY(), getDirect().getPosX(), getDirect().getPosY()));
        dist += ((Star) getSidStar()).distBetRemainPts(getSidStarIndex());
        return dist;
    }

    /** Overrides method in Aircraft class to join the lines between each STAR waypoint */
    @Override
    public void drawSidStar() {
        super.drawSidStar();
        star.joinLines(getSidStarIndex(), 0);
    }

    /** Overrides method in Aircraft class to update label + update STAR name */
    @Override
    public void updateLabel() {
        labelText[8] = star.getName();
        super.updateLabel();
    }

    @Override
    public void updateDirect() {
        super.updateDirect();
        if (getDirect() == null) {
            setClearedHeading((int) getHeading());
            updateVectorMode();
            removeSidStarMode();
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
            if (!isGsCap()) {
                if (getIls().isInsideILS(getX(), getY()) && Math.abs(getAltitude() - getIls().getGSAlt(this)) <= 50) {
                    setGsCap(true);
                }
            } else {
                setAltitude(getIls().getGSAlt(this));
                if (getControlState() == 1 && getAltitude() <= getAirport().getElevation() + 1400) {
                    //Contact the tower
                    setControlState(0);
                    //TODO Add contact tower transmission
                }
                if (getAltitude() <= getIls().getRwy().getElevation()) {
                    setTkofLdg(true);
                    setOnGround(true);
                }
                return;
            }
        }
        super.updateAltitude();
    }

    @Override
    public void updateTkofLdg() {
        setAltitude(getIls().getRwy().getElevation());
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
