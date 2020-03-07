package com.bombbird.terminalcontrol.entities.achievements;

import com.badlogic.gdx.utils.Array;
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
    private static int wakeConflictTime;
    private static String previousUnlock = "";
    public static final HashSet<String> unlocks = new HashSet<>();

    public static final LinkedHashMap<String, Integer> unlockList = new LinkedHashMap<>();
    public static final HashMap<String, String> unlockDescription = new HashMap<>();

    public static final LinkedHashMap<String, Achievement> achievementList = new LinkedHashMap<>();

    public static final LinkedHashMap<String, String> easterEggList = new LinkedHashMap<>();

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
            addAchievement("typhoon", "Typhoon", "Land a plane in TCTP/TCSS with at least 40-knot winds", -1, Achievement.NONE);
            addAchievement("haze", "Haze", "Land a plane in TCWS with visibility at or below 2000 metres", -1, Achievement.NONE);
            addAchievement("mayday", "Mayday", "Land your first emergency", 1, Achievement.EMERGENCIES_LANDED);
            addAchievement("maydayMayday", "Mayday, Mayday", "Land 30 emergencies", 30, Achievement.EMERGENCIES_LANDED);
            addAchievement("masterOfConflicts", "Master of Conflicts", "Have a total of 500 separation incidents, excluding wake conflicts", 500, Achievement.CONFLICTS);
            addAchievement("wakeUp", "Wake Up!", "Have over 600 seconds of wake separation infringement", 600, Achievement.WAKE_CONFLICT_TIME);
        }
        if (easterEggList.size() == 0) {
            easterEggList.put("HX", "Unlock Tai Kek International Airport, TCHX");
        }
    }

    private static void addUnlock(String name, int planesNeeded, String description) {
        unlockList.put(name, planesNeeded);
        unlockDescription.put(name, description);
    }

    private static void addAchievement(String name, String title, String description, int value, int type) {
        achievementList.put(name, new Achievement(name, title, description, value, type));
    }

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
            wakeConflictTime = 0;
        } else {
            //Load saved stats
            planesLanded = stats.optInt("planesLanded", 0);
            emergenciesLanded = stats.optInt("emergenciesLanded", 0);
            conflicts = stats.optInt("conflicts", 0);
            wakeConflictTime = stats.optInt("wakeConflictTime", 0);
            JSONArray unlockArray = stats.getJSONArray("unlocks");
            for (int i = 0; i < unlockArray.length(); i++) {
                unlocks.add(unlockArray.getString(i));
            }
        }
        checkNewUnlocks();
        checkAllAchievements();
        setAchievementStatus();
        GameSaver.saveStats(planesLanded, emergenciesLanded, conflicts, wakeConflictTime, unlocks);
    }

    public static boolean incrementLanded() {
        planesLanded++;
        boolean changed = checkNewUnlocks();
        GameSaver.saveStats(planesLanded, emergenciesLanded, conflicts, wakeConflictTime, unlocks);

        return changed;
    }

    private static boolean checkNewUnlocks() {
        boolean newUnlock = false;
        for (String unlockName: unlockList.keySet()) {
            if (planesLanded >= unlockList.get(unlockName)) {
                newUnlock = unlocks.add(unlockName);
            }
        }

        return newUnlock;
    }

    public static boolean checkAchievement(int type) {
        boolean unlocked = false;
        for (Map.Entry<String, Achievement> entry: achievementList.entrySet()) {
            if (entry.getValue().checkAchievement(type)) {
                unlocked = true;
                unlocks.add(entry.getKey());
            }
            previousUnlock = entry.getValue().getTitle();
        }
        return unlocked;
    }

    private static void checkAllAchievements() {
        checkAchievement(Achievement.PLANES_LANDED);
        checkAchievement(Achievement.EMERGENCIES_LANDED);
        checkAchievement(Achievement.CONFLICTS);
        checkAchievement(Achievement.WAKE_CONFLICT_TIME);
    }

    private static void setAchievementStatus() {
        for (Map.Entry<String, Achievement> entry: achievementList.entrySet()) {
            entry.getValue().setUnlocked(unlocks.contains(entry.getKey()));
        }
    }

    public static void unlockEgg(String name) {
        if (easterEggList.containsKey(name)) unlocks.add(name);
        GameSaver.saveStats(planesLanded, emergenciesLanded, conflicts, wakeConflictTime, unlocks);
    }

    public static int getPlanesLanded() {
        return planesLanded;
    }

    public static Array<String> getSweepAvailable() {
        Array<String> sweeps = new Array<>();
        if (unlocks.contains("sweep0.5s")) sweeps.add("0.5s");
        if (unlocks.contains("sweep1s")) sweeps.add("1s");
        sweeps.add("2s");
        if (unlocks.contains("sweep4s")) sweeps.add("4s");
        if (unlocks.contains("sweep8s")) sweeps.add("8s");

        return sweeps;
    }

    public static Array<String> getTrajAvailable() {
        Array<String> areas = new Array<>();
        areas.add("Off");
        if (unlocks.contains("traj30s")) areas.add("30 sec");
        if (unlocks.contains("traj1m")) areas.add("60 sec");
        if (unlocks.contains("traj2m")) areas.add("120 sec");

        return areas;
    }

    public static Array<String> getAreaAvailable() {
        Array<String> areas = new Array<>();
        areas.add("Off");
        if (unlocks.contains("area30s")) areas.add("30 sec");
        if (unlocks.contains("area1m")) areas.add("60 sec");
        if (unlocks.contains("area2m")) areas.add("120 sec");

        return areas;
    }

    public static Array<String> getCollisionAvailable() {
        Array<String> collisions = new Array<>();
        collisions.add("Off");
        if (unlocks.contains("collision30s")) collisions.add("30 sec");
        if (unlocks.contains("collision1m")) collisions.add("60 sec");
        if (unlocks.contains("collision2m")) collisions.add("120 sec");

        return collisions;
    }

    public static boolean isTCHXAvailable() {
        return unlocks.contains("HX");
    }

    public static int getEmergenciesLanded() {
        return emergenciesLanded;
    }

    public static int getConflicts() {
        return conflicts;
    }

    public static int getWakeConflictTime() {
        return wakeConflictTime;
    }

    public static String getPreviousUnlock() {
        return previousUnlock;
    }
}
