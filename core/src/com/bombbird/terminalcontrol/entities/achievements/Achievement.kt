package com.bombbird.terminalcontrol.entities.achievements

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.conflicts
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.emergenciesLanded
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.planesLanded
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.wakeConflictTime

class Achievement(private val name: String, val title: String, val description: String, val valueNeeded: Int, private val type: Int) {
    companion object {
        var NONE = -1
        var PLANES_LANDED = 0
        var EMERGENCIES_LANDED = 1
        var CONFLICTS = 2
        var WAKE_CONFLICT_TIME = 3
    }

    var isUnlocked = false

    /** Checks whether an achievement is newly fulfilled - returns true if achievement has not been fulfilled yet
     * and is now fulfilled, else returns false
     * Type checked should be one of the specific achievement types and is equal to type of this achievement,
     * else will return false  */
    fun checkAchievement(typeChecked: Int): Boolean {
        if (UnlockManager.unlocks.contains(name)) return false //Already unlocked
        if (typeChecked == NONE || type == NONE) return false //NONE type cannot be checked here
        if (typeChecked != type) return false //Type is not type that needs to be checked
        if (type == PLANES_LANDED) return planesLanded >= valueNeeded
        if (type == EMERGENCIES_LANDED) return emergenciesLanded >= valueNeeded
        if (type == CONFLICTS) return conflicts >= valueNeeded
        if (type == WAKE_CONFLICT_TIME) return wakeConflictTime.toInt() >= valueNeeded
        Gdx.app.log("Achievement", "Unknown type $type for $name")
        return false
    }

    /** Returns the current progress of achievement depending on type, returns -1 if type is NONE
     * If value is not of type int, returns (int) value  */
    val currentValue: Int
        get() {
            if (type == NONE) return -1
            if (type == PLANES_LANDED) return planesLanded
            if (type == EMERGENCIES_LANDED) return emergenciesLanded
            if (type == CONFLICTS) return conflicts
            if (type == WAKE_CONFLICT_TIME) return wakeConflictTime.toInt()
            Gdx.app.log("Achievement", "Unknown type $type for $name")
            return -1
        }
}