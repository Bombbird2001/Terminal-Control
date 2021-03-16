package com.bombbird.terminalcontrol.entities.sidstar

import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint

class RouteData {
    val waypoints: Array<Waypoint> = Array()
    val restrictions: Array<IntArray> = Array()
    val flyOver: Array<Boolean> = Array()
    val size
        get() = waypoints.size

    fun add(wpt: Waypoint, rest: IntArray, fo: Boolean) {
        waypoints.add(wpt)
        restrictions.add(rest)
        flyOver.add(fo)
    }

    fun addAll(wpt: Array<Waypoint>, rest: Array<IntArray>, fo: Array<Boolean>) {
        waypoints.addAll(wpt)
        restrictions.addAll(rest)
        flyOver.addAll(fo)
    }

    fun addAll(routeData: RouteData) {
        waypoints.addAll(routeData.waypoints)
        restrictions.addAll(routeData.restrictions)
        flyOver.addAll(routeData.flyOver)
    }

    fun removeRange(firstIndex: Int, lastIndex: Int) {
        waypoints.removeRange(firstIndex, lastIndex)
        restrictions.removeRange(firstIndex, lastIndex)
        flyOver.removeRange(firstIndex, lastIndex)
    }
}