package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Star;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;

import java.util.Hashtable;

public class Arrival extends Aircraft {
    //Others
    private String ils;
    private Star star;
    private Airport arrival = GameScreen.airports.get("RCTP");

    public Arrival(String callsign, String icaoType, char wakeCat, int[] maxVertSpd, int minSpeed) {
        super(callsign, icaoType, wakeCat, maxVertSpd, minSpeed);
        Hashtable starList = arrival.getStars();
        boolean starSet = false;

        //Gets a STAR for active runways
        while (!starSet) {
            String starStr = (String) starList.keySet().toArray()[MathUtils.random(0, starList.size() - 1)];
            star = (Star) starList.get(starStr);
            for (String runway: star.getRunways()) {
                if (arrival.getLandingRunways().containsKey(runway)) {
                    starSet = true;
                    break;
                }
            }
        }
        star.printWpts();
        direct = star.getWaypoint(0);
        heading = star.getInboundHdg();
        System.out.println("Heading: " + heading);
        track = heading - RadarScreen.magHdgDev;

        setControlState(0);

        //Calculate spawn border
        int[] xBorder = {1310, 4450};
        int[] yBorder = {50, 3190};
        float xDistRight = (xBorder[1] - direct.x)/MathUtils.cosDeg(270 - track);
        float xDistLeft = (xBorder[0] - direct.x)/MathUtils.cosDeg(270 - track);
        float yDistUp = (yBorder[1] - direct.y)/MathUtils.sinDeg(270 - track);
        float yDistDown = (yBorder[0] - direct.y)/MathUtils.sinDeg(270 - track);
        float xDist = xDistRight > 0 ? xDistRight : xDistLeft;
        float yDist = yDistUp > 0 ? yDistUp : yDistDown;
        float dist = xDist > yDist ? yDist : xDist;
        x = direct.x + dist * MathUtils.cosDeg(270 - track);
        y = direct.y + dist * MathUtils.sinDeg(270 - track);

        label.setPosition(x - 100, y + 25);
    }

    @Override
    public void drawStar() {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(x, y, direct.x, direct.y);
        star.joinLines();
    }

    @Override
    public void updateLabel() {
        labelText[8] = star.name;
        super.updateLabel();
    }
}
