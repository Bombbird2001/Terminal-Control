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
        labelText[9] = arrival.icao;
        onGround = false;
        starIndex = 0;
        latMode = "star";

        //Gets a STAR for active runways
        HashMap<String, Star> starList = airport.getStars();
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
        direct = star.getWaypoint(starIndex);
        heading = star.getInboundHdg();
        System.out.println("Heading: " + heading);
        track = heading - RadarScreen.magHdgDev;

        setControlState(1);

        //Calculate spawn border
        int[] xBorder = {1310, 4450};
        int[] yBorder = {50, 3190};
        float xDistRight = (xBorder[1] - direct.x)/MathUtils.cosDeg((float)(270 - track));
        float xDistLeft = (xBorder[0] - direct.x)/MathUtils.cosDeg((float)(270 - track));
        float yDistUp = (yBorder[1] - direct.y)/MathUtils.sinDeg((float)(270 - track));
        float yDistDown = (yBorder[0] - direct.y)/MathUtils.sinDeg((float)(270 - track));
        float xDist = xDistRight > 0 ? xDistRight : xDistLeft;
        float yDist = yDistUp > 0 ? yDistUp : yDistDown;
        float dist = xDist > yDist ? yDist : xDist;
        x = direct.x + dist * MathUtils.cosDeg((float)(270 - track));
        y = direct.y + dist * MathUtils.sinDeg((float)(270 - track));

        heading = update();
        System.out.println("New heading: " + heading);

        label.setPosition(x - 100, y + 25);
    }

    @Override
    public void drawSidStar() {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(x, y, direct.x, direct.y);
        star.joinLines(starIndex, 0);
    }

    @Override
    public void updateLabel() {
        labelText[8] = star.name;
        super.updateLabel();
    }

    @Override
    public void updateDirect() {
        direct.setSelected(false);
        starIndex++;
        direct = star.getWaypoint(starIndex);
        if (direct == null) {
            latMode = "vector";
            clearedHeading = (int)heading;
        }
    }

    @Override
    double findNextTargetHdg() {
        Waypoint nextWpt = star.getWaypoint(starIndex + 1);
        if (nextWpt == null) {
            return track;
        } else {
            float deltaX = nextWpt.x - direct.x;
            float deltaY = nextWpt.y - direct.y;
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
