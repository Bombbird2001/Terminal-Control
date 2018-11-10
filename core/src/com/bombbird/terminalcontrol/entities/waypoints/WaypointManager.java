package com.bombbird.terminalcontrol.entities.waypoints;

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class WaypointManager {

    public WaypointManager() {

    }

    public void update() {
        unselectAll();
        updateSelected();
        updateChangeSelected();
        updateAircraftDirects();
    }

    /** Unselect all waypoints first */
    private void unselectAll() {
        for (Waypoint waypoint: RadarScreen.waypoints.values()) {
            waypoint.setSelected(false);
        }
    }

    /** Updates the waypoints that are displayed when an aircraft is selected */
    private void updateSelected() {
        //Only one aircraft can be selected at a time
        Aircraft aircraft = RadarScreen.getSelectedAircraft();
        if (aircraft != null) {
            String latMode = aircraft.getNavState().getDispLatMode().last();
            if (latMode.contains(aircraft.getSidStar().getName()) || "After waypoint, fly heading".equals(latMode) || "Hold at".equals(latMode)) {
                //Cleared is sidstar mode
                for (Waypoint waypoint: aircraft.getRemainingWaypoints()) {
                    waypoint.setSelected(true);
                }
            }
        }
    }

    /** Updates the waypoints displayed when aircraft is selected and changes have been made to latmode */
    private void updateChangeSelected() {
        //Only one aircraft selected at a time
        Aircraft aircraft = RadarScreen.getSelectedAircraft();
        if (aircraft != null && RadarScreen.ui.latTab.tabChanged && aircraft.getUiRemainingWaypoints() != null) {
            for (Waypoint waypoint: aircraft.getUiRemainingWaypoints()) {
                waypoint.setSelected(true);
            }
        }
    }

    /** Updates the waypoints selected based on whether the aircrafts' next direct is it */
    private void updateAircraftDirects() {
        for (Aircraft aircraft: RadarScreen.aircrafts.values()) {
            if (aircraft.isHolding()) {
                aircraft.getHoldWpt().setSelected(true);
            } else if (aircraft.getDirect() != null) {
                aircraft.getDirect().setSelected(true);
            }
        }
    }
}
