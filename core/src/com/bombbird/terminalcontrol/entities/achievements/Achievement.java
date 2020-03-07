package com.bombbird.terminalcontrol.entities.achievements;

import com.badlogic.gdx.Gdx;

public class Achievement {
    public static int NONE = -1;
    public static int PLANES_LANDED = 0;
    public static int EMERGENCIES_LANDED = 1;
    public static int CONFLICTS = 2;
    public static int WAKE_CONFLICT_TIME = 3;

    private int type;
    private int valueNeeded;
    private String name;
    private String title;
    private String description;
    private boolean unlocked;

    public Achievement(String name, String title, String description, int valueNeeded, int type) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.valueNeeded = valueNeeded;
        this.type = type;
    }

    /** Checks whether an achievement is newly fulfilled - returns true if achievement has not been fulfilled yet
     * and is now fulfilled, else returns false
     * Type checked should be one of the specific achievement types and is equal to type of this achievement,
     * else will return false */
    public boolean checkAchievement(int typeChecked) {
        if (UnlockManager.unlocks.contains(name)) return false; //Already unlocked
        if (typeChecked == NONE || type == NONE) return false; //NONE type cannot be checked here
        if (typeChecked != type) return false; //Type is not type that needs to be checked
        if (type == PLANES_LANDED) return UnlockManager.getPlanesLanded() >= valueNeeded;
        if (type == EMERGENCIES_LANDED) return UnlockManager.getEmergenciesLanded() >= valueNeeded;
        if (type == CONFLICTS) return UnlockManager.getConflicts() >= valueNeeded;
        if (type == WAKE_CONFLICT_TIME) return UnlockManager.getWakeConflictTime() >= valueNeeded;
        Gdx.app.log("Achievement", "Unknown type " + type + " for " + name);
        return false;
    }

    /** Returns the current progress of achievement depending on type, returns -1 if type is NONE */
    public int getCurrentValue() {
        if (type == NONE) return -1;
        if (type == PLANES_LANDED) return UnlockManager.getPlanesLanded();
        if (type == EMERGENCIES_LANDED) return UnlockManager.getEmergenciesLanded();
        if (type == CONFLICTS) return UnlockManager.getConflicts();
        if (type == WAKE_CONFLICT_TIME) return UnlockManager.getWakeConflictTime();
        Gdx.app.log("Achievement", "Unknown type " + type + " for " + name);
        return -1;
    }

    public String getTitle() {
        return title;
    }

    public int getValueNeeded() {
        return valueNeeded;
    }

    public String getDescription() {
        return description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
