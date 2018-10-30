package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.ILS;
import com.bombbird.terminalcontrol.entities.sidstar.SidStar;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.screens.RadarScreen;

import java.util.HashMap;

public class Arrival extends Aircraft {
    //Others
    private ILS ils;
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
        setDirect(star.getWaypoint(getSidStarIndex()));
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

        setControlState(1);
        setColor(new Color(0x00b3ffff));

        setHeading(update());
    }

    @Override
    public void drawSidStar() {
        super.drawSidStar();
        star.joinLines(getSidStarIndex(), 0);
    }

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
        if (getNavState().getLatMode().contains("arrival") || getNavState().getLatMode().contains("waypoint")) {
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
    public SidStar getSidStar() {
        return star;
    }
}
