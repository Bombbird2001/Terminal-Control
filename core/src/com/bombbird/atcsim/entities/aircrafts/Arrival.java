package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.sidstar.Star;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;

import java.util.HashMap;

public class Arrival extends Aircraft {
    //Others
    private String ils;
    private Star star;
    private int starIndex;

    public Arrival(String callsign, String icaoType, Airport arrival) {
        super(callsign, icaoType, arrival);
        labelText[9] = arrival.getIcao();
        setOnGround(false);
        starIndex = 0;
        setLatMode("star");

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
        setDirect(star.getWaypoint(starIndex));
        setHeading(star.getInboundHdg());
        System.out.println("Heading: " + getHeading());
        setTrack(getHeading() - RadarScreen.magHdgDev);

        setControlState(1);

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

        setHeading(update());
        System.out.println("New heading: " + getHeading());

        getLabel().setPosition(getX() - 100, getY() + 25);
    }

    @Override
    public void removeSelectedWaypoints() {
        for (Waypoint waypoint: star.getWaypoints()) {
            waypoint.setSelected(false);
        }
        if (getDirect() != null) {
            getDirect().setSelected(true);
        }
    }

    @Override
    public void drawSidStar() {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(getX(), getY(), getDirect().getPosX(), getDirect().getPosY());
        star.joinLines(starIndex, 0);
    }

    @Override
    public void updateLabel() {
        labelText[8] = star.getName();
        super.updateLabel();
    }

    @Override
    public void updateDirect() {
        getDirect().setSelected(false);
        starIndex++;
        setDirect(star.getWaypoint(starIndex));
        if (getDirect() == null) {
            setLatMode("vector");
            setClearedHeading((int) getHeading());
        }
    }

    @Override
    double findNextTargetHdg() {
        Waypoint nextWpt = star.getWaypoint(starIndex + 1);
        if (nextWpt == null) {
            return getTrack();
        } else {
            float deltaX = nextWpt.getPosX() - getDirect().getPosX();
            float deltaY = nextWpt.getPosY() - getDirect().getPosY();
            double nextTarget;
            if (deltaX >= 0) {
                nextTarget = 90 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            } else {
                nextTarget = 270 - (Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees);
            }
            return nextTarget;
        }
    }
}
