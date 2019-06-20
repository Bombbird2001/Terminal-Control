package com.bombbird.terminalcontrol.entities.trafficmanager;

import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class ArrivalManager {
    public static void checkArrival(Arrival arrival) {
        for (Aircraft aircraft : TerminalControl.radarScreen.aircrafts.values()) {
            if (aircraft instanceof Arrival && arrival.getAltitude() - aircraft.getAltitude() < 2500 && MathTools.pixelToNm(MathTools.distanceBetween(arrival.getX(), arrival.getY(), aircraft.getX(), aircraft.getY())) < 6) {
                if (arrival.getTypDes() - aircraft.getTypDes() > 300) {
                    arrival.setAltitude(aircraft.getAltitude() + 3500);
                } else {
                    arrival.setAltitude(aircraft.getAltitude() + 2500);
                }
                arrival.setClearedIas(aircraft.getClearedIas() > 250 ? 250 : aircraft.getClearedIas() - 10);
                arrival.getNavState().getClearedSpd().removeFirst();
                arrival.getNavState().getClearedSpd().addFirst(arrival.getClearedIas());

                if (arrival.getClearedAltitude() < aircraft.getClearedAltitude() + 1000)
                    arrival.setClearedAltitude(aircraft.getClearedAltitude() + 1000);
                arrival.getNavState().getClearedAlt().removeFirst();
                arrival.getNavState().getClearedAlt().addFirst(arrival.getClearedAltitude());
            }
        }
    }
}
