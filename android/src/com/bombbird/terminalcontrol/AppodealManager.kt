package com.bombbird.terminalcontrol

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.adcolony.sdk.AdColony
import com.adcolony.sdk.AdColonyAppOptions
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks
import com.badlogic.gdx.Game
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.PauseScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.screens.selectgamescreen.NewGameScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.SurveyAdsManager
import com.bombbird.terminalcontrol.utilities.Values
import com.explorestack.consent.*
import com.explorestack.consent.exception.ConsentManagerException

class AppodealManager(private val activity: Activity, private val game: Game) {
    private var consentForm: ConsentForm? = null
    private lateinit var consentManager: ConsentManager
    private var currentAirport = ""

    fun initAppodeal() {
        consentManager = ConsentManager.getInstance(activity)
        consentManager.requestConsentInfoUpdate(Values.APPODEAL_KEY,
            object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(p0: Consent?) {
                    println("Consent info updated!")
                    val pref = activity.getPreferences(Context.MODE_PRIVATE)
                    if (pref.getBoolean("appodealConsentShown", false)) initializeAds() //If user already indicated consent preference, can initialize ads immediately
                }

                override fun onFailedToUpdateConsentInfo(p0: ConsentManagerException?) {
                    println("Failed to update consent info")
                }
            })
    }

    private fun showConsentForm() {
        if (consentForm == null) {
            consentForm = ConsentForm.Builder(activity)
                .withListener(object : ConsentFormListener {
                    override fun onConsentFormLoaded() {
                        // Show ConsentManager Consent request form
                        consentForm?.showAsActivity()
                    }

                    override fun onConsentFormError(error: ConsentManagerException) {
                        Toast.makeText(
                            activity,
                            "Consent form error: " + error.reason,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onConsentFormOpened() {
                        //Ignore
                    }

                    override fun onConsentFormClosed(consent: Consent) {
                        val pref = activity.getPreferences(Context.MODE_PRIVATE)
                        pref.edit().putBoolean("appodealConsentShown", true).apply()
                        initializeAds()
                        showAd(currentAirport)
                    }
                }).build()
        }
        // If Consent request form is already loaded, then we can display it, otherwise, we should load it first
        if (consentForm?.isLoaded == true) {
            consentForm?.showAsActivity()
        } else consentForm?.load()
    }

    private fun initializeAds() {
        consentManager.consent?.let {
            println("Consent is ${it.status.name}")
            val adColonyAppOptions = AdColonyAppOptions()
                .setPrivacyFrameworkRequired(AdColonyAppOptions.GDPR, consentManager.consentZone == Consent.Zone.GDPR)
            it.iabConsentString?.let { it2 ->
                adColonyAppOptions.setPrivacyConsentString(AdColonyAppOptions.GDPR, it2)
            }
            Appodeal.disableLocationPermissionCheck()
            Appodeal.initialize(activity, Values.APPODEAL_KEY, Appodeal.REWARDED_VIDEO, it)
            AdColony.setAppOptions(adColonyAppOptions)
            Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {
                override fun onRewardedVideoLoaded(p0: Boolean) {
                }

                override fun onRewardedVideoFailedToLoad() {
                }

                override fun onRewardedVideoShown() {
                }

                override fun onRewardedVideoShowFailed() {
                }

                override fun onRewardedVideoFinished(p0: Double, p1: String?) {
                    val newExpiry = SurveyAdsManager.getExpiryDateTime(1)
                    val pref = activity.getPreferences(Context.MODE_PRIVATE)
                    pref.edit().putString(currentAirport, newExpiry).apply()
                    SurveyAdsManager.loadData()
                    game.screen?.let { it2 ->
                        if (it2 is LoadGameScreen || it2 is NewGameScreen) {
                            CustomDialog("Ad", "Thank you for watching the ad -\n$currentAirport is now unlocked for 1 hour from now", "", "Ok!").show((it2 as BasicScreen).stage)
                        } else if (it2 is PauseScreen) {
                            CustomDialog("Ad", "Thank you for watching the ad -\n$currentAirport is now unlocked for 1 hour from now", "", "Ok!", height = 1000, width = 2400, fontScale = 2f).show((it2 as BasicScreen).stage)
                        } else Unit
                    }
                }

                override fun onRewardedVideoClosed(p0: Boolean) {
                }

                override fun onRewardedVideoExpired() {
                }

                override fun onRewardedVideoClicked() {
                }
            })
        }
    }

    fun showAd(airport: String): Boolean {
        currentAirport = airport
        val pref = activity.getPreferences(Context.MODE_PRIVATE)
        if (!pref.getBoolean("appodealConsentShown", false)) {
            showConsentForm()
            return true
        }
        else if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) return Appodeal.show(activity, Appodeal.REWARDED_VIDEO)
        return false
    }
}