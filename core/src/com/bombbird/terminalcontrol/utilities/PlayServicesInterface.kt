package com.bombbird.terminalcontrol.utilities

import kotlin.collections.HashMap

interface PlayServicesInterface {
    fun gameSignIn() {
        //No default implementation
    }

    fun gameSignOut() {
        //No default implementation
    }

    fun isSignedIn(): Boolean {
        //No default implementation
        return false
    }

    fun showAchievements() {
        //No default implementation
    }

    fun unlockAchievement(id: String) {
        //No default implementation
    }

    fun incrementAchievement(id: String, steps: Int, set: Boolean) {
        //No default implementation
    }

    fun driveSaveGame() {
        //No default implementation
    }

    fun driveLoadGame() {
        //No default implementation
    }

    fun isSurveyAvailable(): Boolean {
        //No default implementation
        return false
    }

    fun showSurvey(airport: String) {
        //No default implementation
    }

    fun showAd(airport: String): Boolean {
        //No default implementation
        return false
    }

    fun getAirportRewardTiming(): HashMap<String, String> {
        //No default implementation
        return HashMap()
    }
}