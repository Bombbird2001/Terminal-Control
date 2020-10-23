package com.bombbird.terminalcontrol.entities.weather;

import com.badlogic.gdx.math.MathUtils;

public class VisibilityChance {
    public static int getRandomVis() {
        int randomNo = MathUtils.random(544);
        int counter = 0;
        for (int i = 1; i <= 9; i++) {
            if (randomNo <= counter) return i * 1000;
            counter += (i + 1);
        }
        return 10000;
    }
}
