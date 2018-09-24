package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Runway;
import com.bombbird.atcsim.entities.sidstar.Sid;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;

import java.util.HashMap;

public class Departure extends Aircraft {
    //Others
    private Sid sid;
    private int outboundHdg;
    private int sidIndex;
    private int contactAlt;

    public Departure(String callsign, String icaoType, Airport departure) {
        super(callsign, icaoType, departure);
        labelText[9] = departure.icao;
        onGround = true;
        sidIndex = 0;
        contactAlt = 2000 + MathUtils.random(-500, 500);

        //Gets a runway for takeoff
        HashMap<String, Runway> deptRwys = departure.getTakeoffRunways();
        String rwy = (String) deptRwys.keySet().toArray()[MathUtils.random(deptRwys.size() - 1)];
        runway = deptRwys.get(rwy);

        //Gets a random SID
        HashMap<String, Sid> sidList = airport.getSids();
        boolean sidSet = false;
        while (!sidSet) {
            String sidStr = (String) sidList.keySet().toArray()[MathUtils.random(sidList.size() - 1)];
            sid = sidList.get(sidStr);
            for (String runwayStr: sid.getRunways()) {
                if (runwayStr.equals(runway.name)) {
                    sidSet = true;
                    break;
                }
            }
        }
        sid.printWpts();

        //Set initial IAS due to wind
        ias = airport.getWinds()[1] * MathUtils.cosDeg(airport.getWinds()[0] - runway.getHeading());

        //Set initial altitude equal to runway elevation
        altitude = runway.getElevation();

        //Set initial position on runway
        x = runway.getPosition()[0];
        y = runway.getPosition()[1];
        label.setPosition(x - 100, y + 25);

        //Set takeoff heading
        heading = runway.getHeading();

        setControlState(0);

        takeOff();
    }

    private void takeOff() {
        outboundHdg = sid.getOutboundHdg();
        clearedIas = v2;
        setTargetIas(v2);
        clearedAltitude = 3000;
        targetAltitude = clearedAltitude;
        tkofLdg = true;
    }

    @Override
    void updateTkofLdg() {
        if (ias > v2 - 10) {
            onGround = false;
            clearedHeading = sid.getInitClimb()[0];
        }
        if (altitude - airport.elevation >= contactAlt) {
            tkofLdg = false;
            setControlState(2);
        }
        if (altitude > sid.getInitClimb()[1]) {
            direct = sid.getWaypoint(0);
            latMode = "sid";
        }
        if (altitude - airport.elevation > 1500) {
            setTargetIas(250);
            clearedIas = 250;
        }
    }

    @Override
    public void drawSidStar() {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(x, y, direct.x, direct.y);
        sid.joinLines(sidIndex);
    }

    @Override
    public void updateLabel() {
        labelText[8] = sid.name;
        super.updateLabel();
    }

    @Override
    public void updateDirect() {
        direct.setSelected(false);
        sidIndex++;
        direct = sid.getWaypoint(sidIndex);
        if (direct == null) {
            latMode = "vector";
            clearedHeading = (int)(outboundHdg + RadarScreen.magHdgDev);
        }
    }
}
