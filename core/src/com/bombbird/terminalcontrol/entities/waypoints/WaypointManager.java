package com.bombbird.terminalcontrol.entities.waypoints;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.sidstar.Route;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;

public class WaypointManager {
    private final RadarScreen radarScreen;

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
                Array<Waypoint> remaining = aircraft.getRemainingWaypoints();
                for (int i = 0; i < remaining.size; i++) {
                    remaining.get(i).setSelected(true);
                }
            } else if (aircraft.isHolding() && aircraft.getHoldWpt() != null) {
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

    /** Updates the waypoints selected based on whether the aircraft's next direct is it */
    private void updateAircraftDirects() {
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (aircraft.isHolding() && aircraft.getHoldWpt() != null) {
                aircraft.getHoldWpt().setSelected(true);
            } else if (("Hold at".equals(aircraft.getNavState().getDispLatMode().last()) || aircraft.getNavState().getDispLatMode().last().contains(aircraft.getSidStar().getName())) && aircraft.getNavState().getClearedDirect().last() != null) {
                aircraft.getNavState().getClearedDirect().last().setSelected(true);
            } else if ("After waypoint, fly heading".equals(aircraft.getNavState().getDispLatMode().last()) && aircraft.getDirect() != null) {
                aircraft.getDirect().setSelected(true);
            }
        }
    }

    /** Updates the drawing status of restrictions of STAR waypoints */
    public void updateStarRestriction(Route route, int endIndex) {
        int[] lowestAlt = new int[endIndex];
        int prevHighest = -1;
        int prevSpd = -1;

        int prevLowest = -1;
        for (int i = endIndex - 1; i >= 0; i--) {
            int thisLowest = route.getWptMinAlt(i);
            if (prevLowest == -1 || thisLowest > prevLowest) {
                lowestAlt[i] = thisLowest;
                prevLowest = thisLowest;
            } else {
                lowestAlt[i] = -1;
            }
        }

        for (int i = 0; i < endIndex; i++) {
            Waypoint waypoint = route.getWaypoint(i);

            //Determine if highestAlt needs to be displayed
            int thisHighest = -1;
            if (prevHighest == -1 || route.getWptMaxAlt(i) < prevHighest) {
                thisHighest = route.getWptMaxAlt(i);
                prevHighest = thisHighest;
            }

            //Determine if speed needs to be displayed
            int thisSpd = -1;
            if (prevSpd == -1 || route.getWptMaxSpd(i) < prevSpd) {
                thisSpd = route.getWptMaxSpd(i);
                prevSpd = thisSpd;
            }

            waypoint.setRestrDisplay(thisSpd, lowestAlt[i], thisHighest);
        }
    }

    /** Updates the drawing status of SID waypoints */
    public void updateSidRestriction(Route route, int endIndex) {
        int[] highestAlt = new int[endIndex];
        int prevLowest = -1;
        int prevSpd = -1;

        int prevHighest = -1;
        for (int i = endIndex - 1; i >= 0; i--) {
            int thisHighest = route.getWptMaxAlt(i);
            if (prevHighest == -1 || thisHighest > prevHighest) {
                highestAlt[i] = thisHighest;
                prevHighest = thisHighest;
            } else {
                highestAlt[i] = -1;
            }
        }

        for (int i = 0; i < endIndex; i++) {
            Waypoint waypoint = route.getWaypoint(i);

            //Determine if lowestAlt needs to be displayed
            int thisLowest = -1;
            if (prevLowest == -1 || route.getWptMinAlt(i) > prevLowest) {
                thisLowest = route.getWptMinAlt(i);
                prevLowest = thisLowest;
            }

            //Determine if speed needs to be displayed
            int thisSpd = -1;
            if (prevSpd == -1 || route.getWptMaxSpd(i) > prevSpd) {
                thisSpd = route.getWptMaxSpd(i);
                prevSpd = thisSpd;
            }

            waypoint.setRestrDisplay(thisSpd, thisLowest, highestAlt[i]);
        }
    }
}
