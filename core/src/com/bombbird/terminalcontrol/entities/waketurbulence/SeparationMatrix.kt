package com.bombbird.terminalcontrol.entities.waketurbulence;

import com.badlogic.gdx.Gdx;

public class SeparationMatrix {
    private static final int[][] takeoffSepTime = new int[][] {
            {80, 100, 120, 140, 160, 180},
            {80, 80, 80, 100, 120, 140},
            {80, 80, 80, 80, 100, 120},
            {80, 80, 80, 80, 80, 120},
            {80, 80, 80, 80, 80, 100},
            {80, 80, 80, 80, 80, 80}
    };

    private static final int[][] wakeSepDist = new int[][] {
            {3, 4, 5, 5, 6, 8},
            {0, 3, 4, 4, 5, 7},
            {0, 0, 3, 3, 4, 6},
            {0, 0, 0, 0, 0, 5},
            {0, 0, 0, 0, 0, 4},
            {0, 0, 0, 0, 0, 3}
    };

    /** Returns the minimum takeoff separation time in seconds given leader, follower recat codes */
    public static int getTakeoffSepTime(char leader, char follower) {
        int leaderIndex = leader - 'A';
        int followerIndex = follower - 'A';
        if (leaderIndex > 5 || followerIndex > 5 || leaderIndex < 0 || followerIndex < 0) {
            Gdx.app.log("Invalid takeoff sep index", "Array out of index for " + leader + " " + follower);
            return 80;
        } else {
            return takeoffSepTime[leaderIndex][followerIndex];
        }
    }

    /** Returns the minimum wake separation distance in nautical miles given leader, follower recat codes */
    public static int getWakeSepDist(char leader, char follower) {
        int leaderIndex = leader - 'A';
        int followerIndex = follower - 'A';
        if (leaderIndex > 5 || followerIndex > 5 || leaderIndex < 0 || followerIndex < 0) {
            Gdx.app.log("Invalid wake sep index", "Array out of index for " + leader + " " + follower);
            return 3;
        } else {
            return wakeSepDist[leaderIndex][followerIndex];
        }
    }
}
