package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Star;
import com.bombbird.atcsim.screens.GameScreen;

import java.util.Hashtable;

public class Arrival extends Aircraft {
    //Others
    private int ils;
    private Star star;
    private Airport arrival = GameScreen.airports.get(0);

    public Arrival(String callsign, String icaoType, int wakeCat, int[] maxVertSpd, int minSpeed) {
        super(callsign, icaoType, wakeCat, maxVertSpd, minSpeed);
        Hashtable starList = arrival.getStars();
        String starStr = (String) starList.keySet().toArray()[MathUtils.random(0, starList.size() - 1)];
        star = (Star) starList.get(starStr);
        star.printWpts();
    }

    @Override
    public void drawStar() {
        star.joinLines();
    }
}
