package com.bombbird.terminalcontrol

import android.app.Activity
import android.content.Context
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks
import com.appodeal.consent.*
import com.badlogic.gdx.Game
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.PauseScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.NewGameScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.SurveyAdsManager
import com.bombbird.terminalcontrol.utilities.Values
import androidx.core.content.edit

class AppodealManager(private val activity: Activity, private val game: Game) {
    private var currentAirport = ""

    fun initAppodeal() {
        val parameters = ConsentUpdateRequestParameters(activity, Values.APPODEAL_KEY, sdk="Appodeal", sdkVersion=Appodeal.getVersion())
        ConsentManager.requestConsentInfoUpdate(parameters,
            object : ConsentInfoUpdateCallback {
                override fun onUpdated() {
                    println("Consent info updated!")
                    initializeAds()
                }

                override fun onFailed(error: ConsentManagerError) {
                    println("Failed to update consent info: ${error.message}")
                }
            }
        )
    }

    private fun initializeAds() {
        Appodeal.initialize(activity, Values.APPODEAL_KEY, Appodeal.REWARDED_VIDEO) {
            // Appodeal initialization finished
        }

        Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {
            override fun onRewardedVideoLoaded(isPrecache: Boolean) {
            }

            override fun onRewardedVideoFailedToLoad() {
            }

            override fun onRewardedVideoShown() {
            }

            override fun onRewardedVideoShowFailed() {
            }

            override fun onRewardedVideoFinished(amount: Double, currency: String) {
                val newExpiry = SurveyAdsManager.getExpiryDateTime(1)
                val pref = activity.getPreferences(Context.MODE_PRIVATE)
                pref.edit(commit = true) { putString(currentAirport, newExpiry) }
                SurveyAdsManager.loadData()
                game.screen?.let { it2 ->
                    when (it2) {
                        is LoadGameScreen, is NewGameScreen -> CustomDialog("Ad", "Thank you for watching the ad -\n$currentAirport is now unlocked for 1 hour from now", "", "Ok!").show((it2 as BasicScreen).stage)
                        is PauseScreen -> {
                            CustomDialog("Ad", "Thank you for watching the ad -\n$currentAirport is now unlocked for 1 hour from now", "", "Ok!", height = 1000, width = 2400, fontScale = 2f).show((it2 as BasicScreen).stage)
                            TerminalControl.radarScreen?.remainingTime = 60
                        }
                        else -> Unit
                    }
                }
            }

            override fun onRewardedVideoClosed(finished: Boolean) {
            }

            override fun onRewardedVideoExpired() {
            }

            override fun onRewardedVideoClicked() {
            }
        })
    }

    fun showAd(airport: String): Boolean {
        if (airport.isEmpty()) return false
        currentAirport = airport
        if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) return Appodeal.show(activity, Appodeal.REWARDED_VIDEO)
        return false
    }

    fun showConsentForm() {
        ConsentManager.load(activity, { form ->
            form.show(activity) {}
        }) {}
    }
}