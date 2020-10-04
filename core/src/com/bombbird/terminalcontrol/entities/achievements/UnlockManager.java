package com.bombbird.terminalcontrol.entities.achievements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class UnlockManager {
    private static int planesLanded;
    private static int emergenciesLanded;
    private static int conflicts;
    private static float prevWakeConflictTime;
    private static float wakeConflictTime;
    public static final HashSet<String> unlocks = new HashSet<>();

    public static final LinkedHashMap<String, Integer> unlockList = new LinkedHashMap<>();
    public static final HashMap<String, String> unlockDescription = new HashMap<>();

    public static final LinkedHashMap<String, Achievement> achievementList = new LinkedHashMap<>();

    public static final LinkedHashMap<String, String> easterEggList = new LinkedHashMap<>();

    /** Loads all the milestones, achievements and easter eggs */
    public static void loadUnlockList() {
        if (unlockList.size() == 0) {
            //TODO Add more unlocks
            addUnlock("sweep1s", 100, "Unlock 1-second radar sweep");
            addUnlock("sweep4s", 200, "Unlock 4-second radar sweep");
            addUnlock("sweep8s", 300, "Unlock 8-second radar sweep");
            addUnlock("sweep0.5s", 400, "Unlock 0.5-second radar sweep");
            addUnlock("traj30s", 500, "Unlock 30-second advanced trajectory prediction");
            addUnlock("traj1m", 600, "Unlock 1-minute advanced trajectory prediction");
            addUnlock("traj2m", 700, "Unlock 2-minute advanced trajectory prediction");
            addUnlock("area30s", 800, "Unlock area penetration warning 30 seconds look-ahead");
            addUnlock("area1m", 900, "Unlock area penetration warning 1 minute look-ahead");
            addUnlock("area2m", 1000, "Unlock area penetration warning 2 minutes look-ahead");
            addUnlock("collision30s", 1100, "Unlock short term collision alert 30 seconds look-ahead");
            addUnlock("collision1m", 1200, "Unlock short term collision alert 1 minute look-ahead");
            addUnlock("collision2m", 1300, "Unlock short term collision alert 2 minutes look-ahead");
        }
        if (achievementList.size() == 0) {
            addAchievement("gettingStarted", "Getting Started", "Land your first plane", 1, Achievement.PLANES_LANDED);
            addAchievement("novice", "Novice", "Land 50 planes", 50, Achievement.PLANES_LANDED);
            addAchievement("experienced", "Experienced", "Land 200 planes", 200, Achievement.PLANES_LANDED);
            addAchievement("expert", "Expert", "Land 500 planes", 500, Achievement.PLANES_LANDED);
            addAchievement("veteran", "Veteran", "Land 1000 planes", 1000, Achievement.PLANES_LANDED);
            addAchievement("godly", "Godly", "Land 5000 planes", 5000, Achievement.PLANES_LANDED);
            addAchievement("thatWasClose", "That was close", "Have two planes come within 200 feet and 0.5nm of each other", -1, Achievement.NONE);
            addAchievement("typhoon", "Typhoon", "Land a plane in TCTP/TCSS" + (TerminalControl.full ? "/TCTT/TCAA/TCHH/TCMC" : "") + " with at least 40-knot winds", -1, Achievement.NONE);
            addAchievement("haze", "Haze", "Land a plane in TCWS with visibility at or below 2500 metres", -1, Achievement.NONE);
            addAchievement("mayday", "Mayday", "Land your first emergency", 1, Achievement.EMERGENCIES_LANDED);
            addAchievement("maydayMayday", "Mayday, Mayday", "Land 30 emergencies", 30, Achievement.EMERGENCIES_LANDED);
            addAchievement("masterOfConflicts", "Master of Conflicts", "Have a total of 500 separation incidents, excluding wake conflicts", 500, Achievement.CONFLICTS);
            addAchievement("wakeUp", "Wake Up!", "Have over 600 seconds of wake separation infringement", 600, Achievement.WAKE_CONFLICT_TIME);
            addAchievement("parallelLanding", "Parallel Landing", "Land two planes on parallel runways within 5 seconds at TCWS" + (TerminalControl.full ? "/TCTT/TCAA/TCPG" : ""), -1, Achievement.NONE);
        }
        if (easterEggList.size() == 0) {
            easterEggList.put("HX", "Unlock Tai Kek International Airport, TCHX");
        }
    }

    /** Helper function to add a new milestone/unlock */
    private static void addUnlock(String name, int planesNeeded, String description) {
        unlockList.put(name, planesNeeded);
        unlockDescription.put(name, description);
    }

    /** Helper function to add a new achievement */
    private static void addAchievement(String name, String title, String description, int value, int type) {
        achievementList.put(name, new Achievement(name, title, description, value, type));
    }

    /** Function called on game launch to load achievements, milestones and stats */
    public static void loadStats() {
        loadUnlockList();
        JSONObject stats = FileLoader.loadStats();
        if (stats == null) {
            planesLanded = 0;
            //Load current saves and count stats
            JSONArray saves = FileLoader.loadSaves();
            for (int i = 0; i < saves.length(); i++) {
                JSONObject save = saves.getJSONObject(i);
                planesLanded += save.getInt("landings");
            }
            emergenciesLanded = 0;
            conflicts = 0;
            prevWakeConflictTime = 0;
            wakeConflictTime = 0;
        } else {
            //Load saved stats
            planesLanded = stats.optInt("planesLanded", 0);
            emergenciesLanded = stats.optInt("emergenciesLanded", 0);
            conflicts = stats.optInt("conflicts", 0);
            wakeConflictTime = (float) stats.optDouble("wakeConflictTime", 0);
            prevWakeConflictTime = wakeConflictTime;
            JSONArray unlockArray = stats.getJSONArray("unlocks");
            for (int i = 0; i < unlockArray.length(); i++) {
                unlocks.add(unlockArray.getString(i));
            }
        }
        checkNewUnlocks();
        setAchievementStatus();
        checkAllAchievements();
        GameSaver.saveStats();
    }

    /** Called when an aircraft has landed; further checks whether any new milestone or achievement has been unlocked */
    public static void incrementLanded() {
        planesLanded++;
        if (checkNewUnlocks() && TerminalControl.full) {
            TerminalControl.radarScreen.getCommBox().alertMsg("Congratulations, you have reached a milestone! A new option has been unlocked in the milestone/unlock page. Check it out!");
        }
        checkAchievement(Achievement.PLANES_LANDED);
        GameSaver.saveStats();
    }

    /** Called when an emergency has landed; further checks whether any new milestone or achievement has been unlocked */
    public static void incrementEmergency() {
        emergenciesLanded++;
        checkAchievement(Achievement.EMERGENCIES_LANDED);
        GameSaver.saveStats();
    }

    /** Called when a new conflict has occurred; further checks whether any new milestone or achievement has been unlocked */
    public static void incrementConflicts() {
        conflicts++;
        checkAchievement(Achievement.CONFLICTS);
        GameSaver.saveStats();
    }

    /** Called when wake separation infringement time is increased by the specified time; further checks whether any new milestone or achievement has been unlocked */
    public static void incrementWakeConflictTime(float time) {
        wakeConflictTime += time;
        if (wakeConflictTime > prevWakeConflictTime + 2) { //Save & check every 2 seconds of wake time to reduce File I/O load
            checkAchievement(Achievement.WAKE_CONFLICT_TIME);
            GameSaver.saveStats();
            prevWakeConflictTime = wakeConflictTime;
        }
    }

    /** Called to unlock a new achievement that cannot be unlocked using planesLanded, emergenciesLanded, conflicts or wakeConflictTime */
    public static void completeAchievement(String name) {
        if (!achievementList.containsKey(name)) {
            Gdx.app.log("UnlockManager", "Unknown achievement " + name);
            return;
        }
        if (unlocks.contains(name)) return;
        unlocks.add(name);
        TerminalControl.radarScreen.getCommBox().alertMsg("Congratulations, you have unlocked an achievement: " + achievementList.get(name).getTitle());
        GameSaver.saveStats();
    }

    /** Called to check for whether a new milestone has been reached (depending on planesLanded only) */
    private static boolean checkNewUnlocks() {
        boolean newUnlock = false;
        for (String unlockName: unlockList.keySet()) {
            if (planesLanded >= unlockList.get(unlockName)) {
                newUnlock = unlocks.add(unlockName);
            }
        }

        return newUnlock;
    }

    /** Called to iterate through all achievements and checking those of the required type as specified in the Achievement class */
    public static void checkAchievement(int type) {
        for (Map.Entry<String, Achievement> entry: achievementList.entrySet()) {
            if (entry.getValue().checkAchievement(type) && !unlocks.contains(entry.getKey())) {
                unlocks.add(entry.getKey());
                entry.getValue().setUnlocked(true);
                if (TerminalControl.radarScreen != null) TerminalControl.radarScreen.getCommBox().alertMsg("Congratulations, you have unlocked an achievement: " + entry.getValue().getTitle());
            }
        }
    }

    /** Called in loadStats to check if the quantifiable achievements are completed */
    private static void checkAllAchievements() {
        checkAchievement(Achievement.PLANES_LANDED);
        checkAchievement(Achievement.EMERGENCIES_LANDED);
        checkAchievement(Achievement.CONFLICTS);
        checkAchievement(Achievement.WAKE_CONFLICT_TIME);
    }

    /** Called in loadStats to check which achievements are unlocked already in stats file */
    private static void setAchievementStatus() {
        for (Map.Entry<String, Achievement> entry: achievementList.entrySet()) {
            entry.getValue().setUnlocked(unlocks.contains(entry.getKey()));
        }
    }

    /** Called to unlock an easter egg with the given name */
    public static void unlockEgg(String name) {
        if (easterEggList.containsKey(name)) unlocks.add(name);
        GameSaver.saveStats();
    }

    /** Returns the sweep times available depending on milestones unlocked */
    public static Array<String> getSweepAvailable() {
        Array<String> sweeps = new Array<>();
        if (unlocks.contains("sweep0.5s")) sweeps.add("0.5s");
        if (unlocks.contains("sweep1s")) sweeps.add("1s");
        sweeps.add("2s");
        if (unlocks.contains("sweep4s")) sweeps.add("4s");
        if (unlocks.contains("sweep8s")) sweeps.add("8s");

        return sweeps;
    }

    /** Returns the trajectory times available depending on milestones unlocked */
    public static Array<String> getTrajAvailable() {
        Array<String> areas = new Array<>();
        areas.add("Off");
        if (unlocks.contains("traj30s")) areas.add("30 sec");
        if (unlocks.contains("traj1m")) areas.add("60 sec");
        if (unlocks.contains("traj2m")) areas.add("120 sec");

        return areas;
    }

    /** Returns the APW times available depending on milestones unlocked */
    public static Array<String> getAreaAvailable() {
        Array<String> areas = new Array<>();
        areas.add("Off");
        if (unlocks.contains("area30s")) areas.add("30 sec");
        if (unlocks.contains("area1m")) areas.add("60 sec");
        if (unlocks.contains("area2m")) areas.add("120 sec");

        return areas;
    }

    /** Returns the STCAS times available depending on milestones unlocked */
    public static Array<String> getCollisionAvailable() {
        Array<String> collisions = new Array<>();
        collisions.add("Off");
        if (unlocks.contains("collision30s")) collisions.add("30 sec");
        if (unlocks.contains("collision1m")) collisions.add("60 sec");
        if (unlocks.contains("collision2m")) collisions.add("120 sec");

        return collisions;
    }

    /** Returns whether the TCHX easter egg has been unlocked */
    public static boolean isTCHXAvailable() {
        return unlocks.contains("HX");
    }

    public static int getPlanesLanded() {
        return planesLanded;
    }

    public static int getEmergenciesLanded() {
        return emergenciesLanded;
    }

    public static int getConflicts() {
        return conflicts;
    }

    public static float getWakeConflictTime() {
        return wakeConflictTime;
    }
}
