package com.bombbird.terminalcontrol.entities.waypoints;

import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class WaypointManager {
    private RadarScreen radarScreen;

    public WaypointManager() {
        radarScreen = TerminalControl.radarScreen;
    }

    /** Main update function; checks whether a waypoint should be selected or not */
    public void update() {
        unselectAll();
        updateSelected();
        updateChangeSelected();
        updateAircraftDirects();
    }

    /** Unselect all waypoints first */
    private void unselectAll() {
        for (Waypoint waypoint: radarScreen.waypoints.values()) {
            waypoint.setSelected(false);
        }
    }

    /** Updates the waypoints that are displayed when an aircraft is selected */
    private void updateSelected() {
        //Only one aircraft can be selected at a time
        Aircraft aircraft = radarScreen.getSelectedAircraft();
        if (aircraft != null) {
            String latMode = aircraft.getNavState().getDispLatMode().last();
            if (latMode.contains(aircraft.getSidStar().getName()) || "After waypoint, fly heading".equals(latMode) || ("Hold at".equals(latMode) && !aircraft.isHolding())) {
                //Cleared is sidstar mode
                for (Waypoint waypoint: aircraft.getRemainingWaypoints()) {
                    waypoint.setSelected(true);
                }
            } else if (aircraft.isHolding()) {
                aircraft.getHoldWpt().setSelected(true);
            }
        }
    }

    /** Updates the waypoints displayed when aircraft is selected and changes have been made to latmode */
    private void updateChangeSelected() {
        //Only one aircraft selected at a time
        Aircraft aircraft = radarScreen.getSelectedAircraft();
        if (aircraft != null && radarScreen.ui.latTab.tabChanged && aircraft.getUiRemainingWaypoints() != null) {
            for (Waypoint waypoint: aircraft.getUiRemainingWaypoints()) {
                waypoint.setSelected(true);
            }
        }
    }

    /** Updates the waypoints selected based on whether the aircrafts' next direct is it */
    private void updateAircraftDirects() {
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (aircraft.isHolding()) {
                aircraft.getHoldWpt().setSelected(true);
            } else if (aircraft.getNavState().getDispLatMode().last().contains(aircraft.getSidStar().getName()) && aircraft.getNavState().getClearedDirect().last() != null) {
                aircraft.getNavState().getClearedDirect().last().setSelected(true);
            } else if ("After waypoint, fly heading".equals(aircraft.getNavState().getDispLatMode().last()) && aircraft.getDirect() != null) {
                aircraft.getDirect().setSelected(true);
            }
        }
    }
}
