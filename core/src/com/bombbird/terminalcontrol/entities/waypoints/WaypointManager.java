package com.bombbird.terminalcontrol.entities.waypoints;

import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.NavState;
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
            int latMode = aircraft.getNavState().getDispLatMode().last();
            if (aircraft.getNavState().containsCode(latMode, NavState.SID_STAR, NavState.AFTER_WPT_HDG) || (latMode == NavState.HOLD_AT && !aircraft.isHolding())) {
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
            } else if (aircraft.getNavState().containsCode(aircraft.getNavState().getDispLatMode().last(), NavState.HOLD_AT, NavState.SID_STAR) && aircraft.getNavState().getClearedDirect().last() != null) {
                aircraft.getNavState().getClearedDirect().last().setSelected(true);
            } else if (aircraft.getNavState().getDispLatMode().last() == NavState.AFTER_WPT_HDG && aircraft.getDirect() != null) {
                aircraft.getDirect().setSelected(true);
            }
        }
    }

    /** Updates the restrictions of STAR waypoints */
    public void updateStarRestriction(Route route, int startIndex, int endIndex) {
        int[] lowestAlt = new int[endIndex];
        int[] highestAlt = new int[endIndex];
        int[] spd = new int[endIndex];
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
            //Determine if highestAlt needs to be displayed
            int thisHighest = route.getWptMaxAlt(i);
            if (prevHighest == -1 || thisHighest < prevHighest) {
                highestAlt[i] = thisHighest;
                prevHighest = thisHighest;
            } else {
                highestAlt[i] = -1;
            }

            if (thisHighest == route.getWptMinAlt(i) && thisHighest > -1) {
                //If is fixed altitude, set both highest and lowest to same alt
                highestAlt[i] = thisHighest;
                lowestAlt[i] = thisHighest;
            }

            //Determine if speed needs to be displayed
            int thisSpd = route.getWptMaxSpd(i);
            if (prevSpd == -1 || route.getWptMaxSpd(i) < prevSpd) {
                spd[i] = thisSpd;
                prevSpd = thisSpd;
            } else {
                spd[i] = -1;
            }
        }

        setRestrictions(route, startIndex, endIndex, lowestAlt, highestAlt, spd);
    }

    /** Updates the restrictions of SID waypoints */
    public void updateSidRestriction(Route route, int startIndex, int endIndex) {
        int[] lowestAlt = new int[endIndex];
        int[] highestAlt = new int[endIndex];
        int[] spd = new int[endIndex];
        int prevHighest = -1;
        int prevSpd = -1;
        int prevLowest = -1;

        for (int i = endIndex - 1; i >= 0; i--) {
            int thisHighest = route.getWptMaxAlt(i);
            if (prevHighest == -1 || thisHighest < prevHighest) {
                highestAlt[i] = thisHighest;
                prevHighest = thisHighest;
            } else {
                highestAlt[i] = -1;
            }

            //Determine if speed needs to be displayed
            int thisSpd = route.getWptMaxSpd(i);
            if (prevSpd == -1 || route.getWptMaxSpd(i) < prevSpd) {
                spd[i] = thisSpd;
                prevSpd = thisSpd;
            } else {
                spd[i] = -1;
            }
        }

        for (int i = 0; i < endIndex; i++) {
            //Determine if lowestAlt needs to be displayed
            int thisLowest = route.getWptMinAlt(i);
            if (prevLowest == -1 || thisLowest > prevLowest) {
                lowestAlt[i] = thisLowest;
                prevLowest = thisLowest;
            } else {
                lowestAlt[i] = -1;
            }

            if (thisLowest == route.getWptMaxAlt(i) && thisLowest > -1) {
                //If is fixed altitude, set both highest and lowest to same alt
                highestAlt[i] = thisLowest;
                lowestAlt[i] = thisLowest;
            }
        }

        setRestrictions(route, startIndex, endIndex, lowestAlt, highestAlt, spd);
    }

    /** Sets the restrictions to the labels; restrictions will be visible */
    private void setRestrictions(Route route, int startIndex, int endIndex, int[] lowestAlt, int[] highestAlt, int[] spd) {
        for (int i = startIndex; i < endIndex; i++) {
            Waypoint waypoint = route.getWaypoint(i);
            if (lowestAlt[i] == highestAlt[i]) {
                if (i == 0 || i == endIndex - 1) {
                    //Show due to being first or last wpt
                    waypoint.setRestrDisplay(spd[i], lowestAlt[i], highestAlt[i]);
                } else if (lowestAlt[i] != lowestAlt[i - 1] || highestAlt[i] != highestAlt[i - 1]) {
                    //Prev wpt is different
                    waypoint.setRestrDisplay(spd[i], lowestAlt[i], highestAlt[i]);
                } else if (highestAlt[i] != highestAlt[i + 1] || lowestAlt[i] != lowestAlt[i + 1]) {
                    //Next wpt is different
                    waypoint.setRestrDisplay(spd[i], lowestAlt[i], highestAlt[i]);
                } else {
                    //No need to show alt restrictions
                    waypoint.setRestrDisplay(spd[i], -1, -1);
                }
            } else {
                waypoint.setRestrDisplay(spd[i], lowestAlt[i], highestAlt[i]);
            }
        }
    }
}
