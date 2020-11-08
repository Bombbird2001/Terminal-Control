package com.bombbird.terminalcontrol.entities.waketurbulence

import com.badlogic.gdx.Gdx

object SeparationMatrix {
    private val takeoffSepTime = arrayOf(intArrayOf(80, 100, 120, 140, 160, 180), intArrayOf(80, 80, 80, 100, 120, 140), intArrayOf(80, 80, 80, 80, 100, 120), intArrayOf(80, 80, 80, 80, 80, 120), intArrayOf(80, 80, 80, 80, 80, 100), intArrayOf(80, 80, 80, 80, 80, 80))
    private val wakeSepDist = arrayOf(intArrayOf(3, 4, 5, 5, 6, 8), intArrayOf(0, 3, 4, 4, 5, 7), intArrayOf(0, 0, 3, 3, 4, 6), intArrayOf(0, 0, 0, 0, 0, 5), intArrayOf(0, 0, 0, 0, 0, 4), intArrayOf(0, 0, 0, 0, 0, 3))

    /** Returns the minimum takeoff separation time in seconds given leader, follower recat codes  */
    fun getTakeoffSepTime(leader: Char, follower: Char): Int {
        val leaderIndex = leader - 'A'
        val followerIndex = follower - 'A'
        return if (leaderIndex > 5 || followerIndex > 5 || leaderIndex < 0 || followerIndex < 0) {
            Gdx.app.log("Invalid takeoff sep index", "Array out of index for $leader $follower")
            80
        } else {
            takeoffSepTime[leaderIndex][followerIndex]
        }
    }

    /** Returns the minimum wake separation distance in nautical miles given leader, follower recat codes  */
    fun getWakeSepDist(leader: Char, follower: Char): Int {
        val leaderIndex = leader - 'A'
        val followerIndex = follower - 'A'
        return if (leaderIndex > 5 || followerIndex > 5 || leaderIndex < 0 || followerIndex < 0) {
            Gdx.app.log("Invalid wake sep index", "Array out of index for $leader $follower")
            3
        } else {
            wakeSepDist[leaderIndex][followerIndex]
        }
    }
}