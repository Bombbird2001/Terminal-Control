package com.bombbird.atcsim.entities.aircrafts;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Runway;
import com.bombbird.atcsim.entities.sidstar.Sid;
import com.bombbird.atcsim.screens.RadarScreen;

import java.util.HashMap;

public class Departure extends Aircraft {
    //Others
    private Sid sid;
    private int outboundHdg;
    private int sidIndex;

    Departure(String callsign, String icaoType, Airport departure) {
        super(callsign, icaoType, departure);
        labelText[9] = departure.icao;
        onGround = true;
        sidIndex = 0;

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
        ias = airport.getWinds()[1] * MathUtils.cosDeg(airport.getWinds()[0] - runway.getHeading() - 180);

        //Set initial altitude equal to runway elevation
        altitude = runway.getElevation();

        //Set initial position on runway
        x = runway.getPosition()[0];
        y = runway.getPosition()[1];
    }

    private void takeOff() {
        outboundHdg = sid.getOutboundHdg();
        setTargetIas(140);
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
