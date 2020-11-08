package com.bombbird.terminalcontrol.entities.weather

import com.badlogic.gdx.math.MathUtils

object VisibilityChance {
    val randomVis: Int
        get() {
            val randomNo = MathUtils.random(544)
            var counter = 0
            for (i in 1..9) {
                if (randomNo <= counter) return i * 1000
                counter += i + 1
            }
            return 10000
        }
}