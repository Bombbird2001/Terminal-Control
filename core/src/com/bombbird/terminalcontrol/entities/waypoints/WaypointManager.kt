package com.bombbird.terminalcontrol.entities.waypoints

import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.NavState
import com.bombbird.terminalcontrol.entities.sidstar.Route
import com.bombbird.terminalcontrol.ui.Ui
import com.bombbird.terminalcontrol.ui.tabs.Tab

class WaypointManager {
    private val radarScreen = TerminalControl.radarScreen!!

    /** Main update function; checks whether a waypoint should be selected or not  */
    fun update() {
        unselectAll()
        updateSelected()
        updateChangeSelected()
        updateAircraftDirects()
    }

    /** Unselect all waypoints first  */
    private fun unselectAll() {
        for (waypoint in radarScreen.waypoints.values) {
            waypoint.isSelected = false
            waypoint.distToGoVisible = false
        }
    }

    /** Updates the waypoints that are displayed when an aircraft is selected  */
    private fun updateSelected() {
        //Only one aircraft can be selected at a time
        val aircraft = radarScreen.selectedAircraft
        if (aircraft != null) {
            val latMode: Int = aircraft.navState.dispLatMode.last()
            if (aircraft.navState.containsCode(latMode, NavState.SID_STAR, NavState.AFTER_WPT_HDG) || latMode == NavState.HOLD_AT && !aircraft.isHolding) {
                //Cleared is sidstar mode
                val remaining: Array<Waypoint> = aircraft.remainingWaypoints
                for (i in 0 until remaining.size) remaining[i].isSelected = true
            } else if (aircraft.isHolding && aircraft.holdWpt != null) {
                aircraft.holdWpt?.isSelected = true
            }
        }
    }

    /** Updates the waypoints displayed when aircraft is selected and changes have been made to latmode  */
    private fun updateChangeSelected() {
        //Only one aircraft selected at a time
        val aircraft = radarScreen.selectedAircraft
        if (aircraft != null && radarScreen.ui.latTab.tabChanged) {
            for (waypoint in aircraft.uiRemainingWaypoints) waypoint.isSelected = true
            if (radarScreen.ui.latTab.isIlsChanged && Tab.clearedILS != Ui.NOT_CLEARED_APCH) aircraft.airport.approaches[Tab.clearedILS?.substring(3)]?.getNextPossibleTransition(radarScreen.waypoints[Tab.clearedWpt], aircraft.route)?.first?.let {
                for (wpt in it.waypoints) wpt.isSelected = true
            }
        }
    }

    /** Updates the waypoints selected based on whether the aircraft's next direct is it  */
    private fun updateAircraftDirects() {
        for (aircraft in radarScreen.aircrafts.values) {
            if (aircraft.isHolding && aircraft.holdWpt != null) {
                aircraft.holdWpt?.isSelected = true
            } else if (aircraft.navState.containsCode(aircraft.navState.dispLatMode.last(), NavState.HOLD_AT, NavState.SID_STAR) && aircraft.navState.clearedDirect.last() != null) {
                aircraft.navState.clearedDirect.last()?.isSelected = true
            } else if (aircraft.navState.dispLatMode.last() == NavState.AFTER_WPT_HDG && aircraft.direct != null) {
                aircraft.direct?.isSelected = true
            }
        }
    }

    /** Updates the restrictions of STAR waypoints  */
    fun updateStarRestriction(route: Route, startIndex: Int, endIndex: Int) {
        val lowestAlt = IntArray(endIndex)
        val highestAlt = IntArray(endIndex)
        val spd = IntArray(endIndex)
        var prevHighest = -1
        var prevSpd = -1
        var prevLowest = -1
        for (i in endIndex - 1 downTo 0) {
            val thisLowest = route.getWptMinAlt(i)
            if (prevLowest == -1 || thisLowest > prevLowest) {
                lowestAlt[i] = thisLowest
                prevLowest = thisLowest
            } else {
                lowestAlt[i] = -1
            }
        }
        for (i in 0 until endIndex) {
            //Determine if highestAlt needs to be displayed
            val thisHighest = route.getWptMaxAlt(i)
            if (prevHighest == -1 || thisHighest < prevHighest) {
                highestAlt[i] = thisHighest
                prevHighest = thisHighest
            } else {
                highestAlt[i] = -1
            }
            if (thisHighest == route.getWptMinAlt(i) && thisHighest > -1) {
                //If is fixed altitude, set both highest and lowest to same alt
                highestAlt[i] = thisHighest
                lowestAlt[i] = thisHighest
            }

            //Determine if speed needs to be displayed
            val thisSpd = route.getWptMaxSpd(i)
            if (prevSpd == -1 || route.getWptMaxSpd(i) < prevSpd) {
                spd[i] = thisSpd
                prevSpd = thisSpd
            } else {
                spd[i] = -1
            }
        }
        setRestrictions(route, startIndex, endIndex, lowestAlt, highestAlt, spd)
    }

    /** Updates the restrictions of SID waypoints  */
    fun updateSidRestriction(route: Route, startIndex: Int, endIndex: Int) {
        val lowestAlt = IntArray(endIndex)
        val highestAlt = IntArray(endIndex)
        val spd = IntArray(endIndex)
        var prevHighest = -1
        var prevSpd = -1
        var prevLowest = -1
        for (i in endIndex - 1 downTo 0) {
            val thisHighest = route.getWptMaxAlt(i)
            if (prevHighest == -1 || thisHighest < prevHighest) {
                highestAlt[i] = thisHighest
                prevHighest = thisHighest
            } else {
                highestAlt[i] = -1
            }

            //Determine if speed needs to be displayed
            val thisSpd = route.getWptMaxSpd(i)
            if (prevSpd == -1 || route.getWptMaxSpd(i) < prevSpd) {
                spd[i] = thisSpd
                prevSpd = thisSpd
            } else {
                spd[i] = -1
            }
        }
        for (i in 0 until endIndex) {
            //Determine if lowestAlt needs to be displayed
            val thisLowest = route.getWptMinAlt(i)
            if (prevLowest == -1 || thisLowest > prevLowest) {
                lowestAlt[i] = thisLowest
                prevLowest = thisLowest
            } else {
                lowestAlt[i] = -1
            }
            if (thisLowest == route.getWptMaxAlt(i) && thisLowest > -1) {
                //If is fixed altitude, set both highest and lowest to same alt
                highestAlt[i] = thisLowest
                lowestAlt[i] = thisLowest
            }
        }
        setRestrictions(route, startIndex, endIndex, lowestAlt, highestAlt, spd)
    }

    /** Sets the restrictions to the labels; restrictions will be visible  */
    private fun setRestrictions(route: Route, startIndex: Int, endIndex: Int, lowestAlt: IntArray, highestAlt: IntArray, spd: IntArray) {
        for (i in startIndex until endIndex) {
            val waypoint = route.getWaypoint(i) ?: continue
            if (lowestAlt[i] == highestAlt[i]) {
                if (i == 0 || i == endIndex - 1) {
                    //Show due to being first or last wpt
                    waypoint.setRestrDisplay(spd[i], lowestAlt[i], highestAlt[i])
                } else if (lowestAlt[i] != lowestAlt[i - 1] || highestAlt[i] != highestAlt[i - 1]) {
                    //Prev wpt is different
                    waypoint.setRestrDisplay(spd[i], lowestAlt[i], highestAlt[i])
                } else if (highestAlt[i] != highestAlt[i + 1] || lowestAlt[i] != lowestAlt[i + 1]) {
                    //Next wpt is different
                    waypoint.setRestrDisplay(spd[i], lowestAlt[i], highestAlt[i])
                } else {
                    //No need to show alt restrictions
                    waypoint.setRestrDisplay(spd[i], -1, -1)
                }
            } else {
                waypoint.setRestrDisplay(spd[i], lowestAlt[i], highestAlt[i])
            }
        }
    }
}