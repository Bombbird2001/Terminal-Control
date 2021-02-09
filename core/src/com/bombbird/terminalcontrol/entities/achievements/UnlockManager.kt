package com.bombbird.terminalcontrol.entities.achievements

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.files.FileLoader
import com.bombbird.terminalcontrol.utilities.files.GameSaver
import java.util.*

object UnlockManager {
    var planesLanded = 0
        private set

    var emergenciesLanded = 0
        private set

    var conflicts = 0
        private set
    private var prevWakeConflictTime = 0f

    var wakeConflictTime = 0f
        private set

    val unlocks = HashSet<String>()

    val unlockList = LinkedHashMap<String, Int>()

    val unlockDescription = HashMap<String, String>()

    val achievementList = LinkedHashMap<String, Achievement>()

    val easterEggList = LinkedHashMap<String, kotlin.Array<String>>()

    var loaded = false

    /** Loads all the milestones, achievements and easter eggs  */
    private fun loadUnlockList() {
        if (unlockList.size == 0) {
            //TODO Add more unlocks
            addUnlock("sweep1s", 100, "Unlock 1-second radar sweep")
            addUnlock("sweep4s", 200, "Unlock 4-second radar sweep")
            addUnlock("sweep8s", 300, "Unlock 8-second radar sweep")
            addUnlock("sweep0.5s", 400, "Unlock 0.5-second radar sweep")
            addUnlock("traj30s", 500, "Unlock 30-second advanced trajectory prediction")
            addUnlock("traj1m", 600, "Unlock 1-minute advanced trajectory prediction")
            addUnlock("traj2m", 700, "Unlock 2-minute advanced trajectory prediction")
            addUnlock("area30s", 800, "Unlock area penetration warning 30 seconds look-ahead")
            addUnlock("area1m", 900, "Unlock area penetration warning 1 minute look-ahead")
            addUnlock("area2m", 1000, "Unlock area penetration warning 2 minutes look-ahead")
            addUnlock("collision30s", 1100, "Unlock short term collision alert 30 seconds look-ahead")
            addUnlock("collision1m", 1200, "Unlock short term collision alert 1 minute look-ahead")
            addUnlock("collision2m", 1300, "Unlock short term collision alert 2 minutes look-ahead")
        }
        if (achievementList.size == 0) {
            addAchievement("gettingStarted", "Getting Started", "Land your first plane", 1, Achievement.PLANES_LANDED, "CgkI0prDyawdEAIQAQ")
            addAchievement("novice", "Novice", "Land 50 planes", 50, Achievement.PLANES_LANDED, "CgkI0prDyawdEAIQAg")
            addAchievement("experienced", "Experienced", "Land 200 planes", 200, Achievement.PLANES_LANDED, "CgkI0prDyawdEAIQAw")
            addAchievement("expert", "Expert", "Land 500 planes", 500, Achievement.PLANES_LANDED, "CgkI0prDyawdEAIQBA")
            addAchievement("veteran", "Veteran", "Land 1000 planes", 1000, Achievement.PLANES_LANDED, "CgkI0prDyawdEAIQBQ")
            addAchievement("godly", "Godly", "Land 5000 planes", 5000, Achievement.PLANES_LANDED, "CgkI0prDyawdEAIQBg")
            addAchievement("thatWasClose", "That was close", "Have two planes come within 200 feet and 0.5nm of each other", -1, Achievement.NONE, "CgkI0prDyawdEAIQBw")
            addAchievement("typhoon", "Typhoon", "Land a plane in TCTP/TCSS/TCTT/TCAA/TCHH/TCMC with at least 40-knot winds", -1, Achievement.NONE, "CgkI0prDyawdEAIQCA")
            addAchievement("haze", "Haze", "Land a plane in TCWS with visibility at or below 2500 metres", -1, Achievement.NONE, "CgkI0prDyawdEAIQCQ")
            addAchievement("mayday", "Mayday", "Land your first emergency", 1, Achievement.EMERGENCIES_LANDED, "CgkI0prDyawdEAIQCg")
            addAchievement("maydayMayday", "Mayday, Mayday", "Land 30 emergencies", 30, Achievement.EMERGENCIES_LANDED, "CgkI0prDyawdEAIQCw")
            addAchievement("masterOfConflicts", "Master of Conflicts", "Have a total of 500 separation incidents, excluding wake conflicts", 500, Achievement.CONFLICTS, "CgkI0prDyawdEAIQDA")
            addAchievement("wakeUp", "Wake Up!", "Have over 600 seconds of wake separation infringement", 600, Achievement.WAKE_CONFLICT_TIME, "CgkI0prDyawdEAIQDQ")
            addAchievement("parallelLanding", "Parallel Landing", "Land two planes on parallel runways within 5 seconds at TCWS/TCTT/TCAA/TCPG", -1, Achievement.NONE, "CgkI0prDyawdEAIQDg")
        }
        if (easterEggList.size == 0) {
            easterEggList["HX"] = arrayOf("Unlock Tai Kek International Airport, TCHX", "CgkI0prDyawdEAIQDw")
        }
    }

    /** Helper function to add a new milestone/unlock  */
    private fun addUnlock(name: String, planesNeeded: Int, description: String) {
        unlockList[name] = planesNeeded
        unlockDescription[name] = description
    }

    /** Helper function to add a new achievement  */
    private fun addAchievement(name: String, title: String, description: String, value: Int, type: Int, id: String) {
        achievementList[name] = Achievement(name, title, description, value, type, id)
    }

    /** Function called on game launch to load achievements, milestones and stats  */
    fun loadStats() {
        loadUnlockList()
        val stats = FileLoader.loadStats()
        if (stats == null) {
            planesLanded = 0
            //Load current saves and count stats
            val saves = FileLoader.loadSaves()
            for (i in 0 until saves.length()) {
                val save = saves.getJSONObject(i)
                planesLanded += save.getInt("landings")
            }
            emergenciesLanded = 0
            conflicts = 0
            prevWakeConflictTime = 0f
            wakeConflictTime = 0f
        } else {
            //Load saved stats
            planesLanded = stats.optInt("planesLanded", 0)
            emergenciesLanded = stats.optInt("emergenciesLanded", 0)
            conflicts = stats.optInt("conflicts", 0)
            wakeConflictTime = stats.optDouble("wakeConflictTime", 0.0).toFloat()
            prevWakeConflictTime = wakeConflictTime
            val unlockArray = stats.getJSONArray("unlocks")
            for (i in 0 until unlockArray.length()) {
                unlocks.add(unlockArray.getString(i))
            }
        }
        checkNewUnlocks()
        setAchievementStatus()
        checkQuantifiableAchievements()
        GameSaver.saveStats()
        loaded = true
        checkGooglePlayAchievements()
    }

    /** Function called after Google Play Games sign in to update achievements */
    fun checkGooglePlayAchievements() {
        if (!TerminalControl.playGamesInterface.isSignedIn()) return
        for ((key, value) in achievementList) {
            if (value.type == Achievement.NONE || key == "gettingStarted" || key == "mayday") {
                if (!value.isUnlocked) continue
                TerminalControl.playGamesInterface.unlockAchievement(value.id)
            } else TerminalControl.playGamesInterface.incrementAchievement(value.id, value.currentValue, true)
        }
        for ((key, value) in easterEggList) {
            if (unlocks.contains(key)) TerminalControl.playGamesInterface.unlockAchievement(value[1])
        }
    }

    private fun incrementGooglePlayAchievement(type: Int, steps: Int) {
        for ((key, value) in achievementList) {
            if (value.type != type || value.isUnlocked) continue
            if (key != "gettingStarted" && key != "mayday") {
                TerminalControl.playGamesInterface.incrementAchievement(value.id, steps, false)
            } else TerminalControl.playGamesInterface.unlockAchievement(value.id)
        }
    }

    /** Called when an aircraft has landed; further checks whether any new milestone or achievement has been unlocked  */
    fun incrementLanded() {
        planesLanded++
        if (checkNewUnlocks() && TerminalControl.full) {
            TerminalControl.radarScreen?.utilityBox?.commsManager?.alertMsg("Congratulations, you have reached a milestone! A new option has been unlocked in the milestone/unlock page. Check it out!")
        }
        incrementGooglePlayAchievement(Achievement.PLANES_LANDED, 1)
        checkAchievement(Achievement.PLANES_LANDED)
        GameSaver.saveStats()
    }

    /** Called when an emergency has landed; further checks whether any new milestone or achievement has been unlocked  */
    fun incrementEmergency() {
        emergenciesLanded++
        incrementGooglePlayAchievement(Achievement.EMERGENCIES_LANDED, 1)
        checkAchievement(Achievement.EMERGENCIES_LANDED)
        GameSaver.saveStats()
    }

    /** Called when a new conflict has occurred; further checks whether any new milestone or achievement has been unlocked  */
    fun incrementConflicts() {
        conflicts++
        incrementGooglePlayAchievement(Achievement.CONFLICTS, 1)
        checkAchievement(Achievement.CONFLICTS)
        GameSaver.saveStats()
    }

    /** Called when wake separation infringement time is increased by the specified time; further checks whether any new milestone or achievement has been unlocked  */
    fun incrementWakeConflictTime(time: Float) {
        wakeConflictTime += time
        if (wakeConflictTime > prevWakeConflictTime + 2) { //Save & check every 2 seconds of wake time to reduce File I/O load
            incrementGooglePlayAchievement(Achievement.WAKE_CONFLICT_TIME, 2)
            checkAchievement(Achievement.WAKE_CONFLICT_TIME)
            GameSaver.saveStats()
            prevWakeConflictTime = wakeConflictTime
        }
    }

    /** Called to unlock a new achievement that cannot be unlocked using planesLanded, emergenciesLanded, conflicts or wakeConflictTime  */
    fun completeAchievement(name: String) {
        if (!achievementList.containsKey(name)) {
            Gdx.app.log("UnlockManager", "Unknown achievement $name")
            return
        }
        if (unlocks.contains(name)) return
        unlocks.add(name)
        achievementList[name]?.let { TerminalControl.playGamesInterface.unlockAchievement(it.id) }
        setAchievementStatus()
        TerminalControl.radarScreen?.utilityBox?.commsManager?.alertMsg("Congratulations, you have unlocked an achievement: " + (achievementList[name]?.title ?: ""))
        GameSaver.saveStats()
    }

    /** Called to check for whether a new milestone has been reached (depending on planesLanded only)  */
    private fun checkNewUnlocks(): Boolean {
        var newUnlock = false
        for (unlockName in unlockList.keys) {
            unlockList[unlockName]?.let {
                if (planesLanded >= it) {
                    newUnlock = unlocks.add(unlockName)
                }
            }
        }
        return newUnlock
    }

    /** Called to iterate through all achievements and checking those of the required type as specified in the Achievement class  */
    private fun checkAchievement(type: Int) {
        for ((key, value) in achievementList) {
            if (value.checkAchievement(type) && !unlocks.contains(key)) {
                unlocks.add(key)
                value.isUnlocked = true
                if (TerminalControl.radarScreen != null) TerminalControl.radarScreen?.utilityBox?.commsManager?.alertMsg("Congratulations, you have unlocked an achievement: " + value.title)
            }
        }
    }

    /** Called in loadStats to check if the quantifiable achievements are completed  */
    private fun checkQuantifiableAchievements() {
        checkAchievement(Achievement.PLANES_LANDED)
        checkAchievement(Achievement.EMERGENCIES_LANDED)
        checkAchievement(Achievement.CONFLICTS)
        checkAchievement(Achievement.WAKE_CONFLICT_TIME)
    }

    /** Called in loadStats to check which achievements are unlocked already in stats file  */
    private fun setAchievementStatus() {
        for ((key, value) in achievementList) {
            value.isUnlocked = unlocks.contains(key)
        }
    }

    /** Called to unlock an easter egg with the given name  */
    fun unlockEgg(name: String) {
        if (easterEggList.containsKey(name)) {
            unlocks.add(name)
            easterEggList[name]?.let { TerminalControl.playGamesInterface.unlockAchievement(it[1]) }
        }
        GameSaver.saveStats()
    }

    /** Returns the sweep times available depending on milestones unlocked  */

    val sweepAvailable: Array<String>
        get() {
            val sweeps = Array<String>()
            if (unlocks.contains("sweep0.5s")) sweeps.add("0.5s")
            if (unlocks.contains("sweep1s")) sweeps.add("1s")
            sweeps.add("2s")
            if (unlocks.contains("sweep4s")) sweeps.add("4s")
            if (unlocks.contains("sweep8s")) sweeps.add("8s")
            return sweeps
        }

    /** Returns the trajectory times available depending on milestones unlocked  */

    val trajAvailable: Array<String>
        get() {
            val areas = Array<String>()
            areas.add("Off")
            if (unlocks.contains("traj30s")) areas.add("30 sec")
            if (unlocks.contains("traj1m")) areas.add("60 sec")
            if (unlocks.contains("traj2m")) areas.add("120 sec")
            return areas
        }

    /** Returns the APW times available depending on milestones unlocked  */

    val areaAvailable: Array<String>
        get() {
            val areas = Array<String>()
            areas.add("Off")
            if (unlocks.contains("area30s")) areas.add("30 sec")
            if (unlocks.contains("area1m")) areas.add("60 sec")
            if (unlocks.contains("area2m")) areas.add("120 sec")
            return areas
        }

    /** Returns the STCAS times available depending on milestones unlocked  */

    val collisionAvailable: Array<String>
        get() {
            val collisions = Array<String>()
            collisions.add("Off")
            if (unlocks.contains("collision30s")) collisions.add("30 sec")
            if (unlocks.contains("collision1m")) collisions.add("60 sec")
            if (unlocks.contains("collision2m")) collisions.add("120 sec")
            return collisions
        }

    /** Returns whether the TCHX easter egg has been unlocked  */

    val isTCHXAvailable: Boolean
        get() = unlocks.contains("HX")
}