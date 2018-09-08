package com.bombbird.atcsim.entities.aircrafts;

public class Departure extends Aircraft {
    //Others
    private String sid;
    private int deptRwy;

    Departure(String callsign, String icaoType, int wakeCat, int[] maxVertSpd, int minSpeed) {
        super(callsign, icaoType, wakeCat, maxVertSpd, minSpeed);
    }
}
