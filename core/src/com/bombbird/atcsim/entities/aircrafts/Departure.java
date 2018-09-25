package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Runway;
import com.bombbird.atcsim.entities.Waypoint;
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
    private boolean v2set;
    private boolean sidSet;
    private boolean spdSet;

    public Departure(String callsign, String icaoType, Airport departure) {
        super(callsign, icaoType, departure);
        labelText[9] = departure.icao;
        onGround = true;
        sidIndex = 0;
        contactAlt = 2000 + MathUtils.random(-400, 400);
        v2set = false;
        sidSet = false;
        spdSet = false;

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
        if (airport.icao.equals("RCSS")) sid = sidList.get("MUKKA2H");
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

        setControlState(2);

        takeOff();
    }

    private void takeOff() {
        outboundHdg = sid.getOutboundHdg();
        clearedIas = v2;
        setTargetIas(v2);
        clearedAltitude = 3000;
        targetAltitude = clearedAltitude;
        clearedHeading = sid.getInitClimb()[0];
        tkofLdg = true;
    }

    @Override
    void updateTkofLdg() {
        if (ias > v2 - 10 && !v2set) {
            onGround = false;
            targetHeading = clearedHeading;
            v2set = true;
        }
        if (altitude - airport.elevation >= contactAlt) {
            tkofLdg = false;
            setControlState(2);
        }
        if (altitude > sid.getInitClimb()[1] && !sidSet) {
            direct = sid.getWaypoint(0);
            latMode = "sid";
            sidSet = true;
        }
        if (altitude - airport.elevation >= 1500 && !spdSet) {
            setTargetIas(250);
            clearedIas = 250;
            spdSet = true;
        }
    }

    @Override
    public void drawSidStar() {
        GameScreen.shapeRenderer.setColor(Color.WHITE);
        GameScreen.shapeRenderer.line(x, y, direct.x, direct.y);
        sid.joinLines(sidIndex, outboundHdg);
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

    @Override
    double findNextTargetHdg() {
        Waypoint nextWpt = sid.getWaypoint(sidIndex + 1);
        if (nextWpt == null) {
            return outboundHdg;
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
