package com.bombbird.terminalcontrol.entities.zones;

public class DepartureZone extends ApproachZone {
    public DepartureZone(String rwy1, String rwy2, float xMid, float yMid, int depHdg, float nozWidth, float nozLength, float ntzWidth) {
        super(rwy1, rwy2, xMid, yMid, depHdg + 180, nozWidth, nozLength, ntzWidth);
    }
}
