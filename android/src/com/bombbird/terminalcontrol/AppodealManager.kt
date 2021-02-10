package com.bombbird.terminalcontrol

import android.app.Activity
import android.widget.Toast
import com.appodeal.ads.Appodeal
import com.appodeal.ads.RewardedVideoCallbacks
import com.bombbird.terminalcontrol.utilities.Values
import com.explorestack.consent.*
import com.explorestack.consent.exception.ConsentManagerException

class AppodealManager(private val activity: Activity) {
    private var consentForm: ConsentForm? = null
    private lateinit var consentManager: ConsentManager

    fun initAppodeal() {
        consentManager = ConsentManager.getInstance(activity)
        consentManager.requestConsentInfoUpdate(Values.APPODEAL_KEY,
            object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(p0: Consent?) {
                    if (consentManager.shouldShowConsentDialog() == Consent.ShouldShow.TRUE) {
                        showConsentForm()
                    } else {
                        consentManager.consent?.let {
                            initializeAds()
                        }
                    }
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
                        initializeAds()
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
            Appodeal.disableLocationPermissionCheck()
            Appodeal.initialize(activity, Values.APPODEAL_KEY, Appodeal.REWARDED_VIDEO, it)
            Appodeal.setRewardedVideoCallbacks(object : RewardedVideoCallbacks {
                override fun onRewardedVideoLoaded(p0: Boolean) {
                    Appodeal.show(activity, Appodeal.REWARDED_VIDEO)
                }

                override fun onRewardedVideoFailedToLoad() {
                }

                override fun onRewardedVideoShown() {
                }

                override fun onRewardedVideoShowFailed() {
                }

                override fun onRewardedVideoFinished(p0: Double, p1: String?) {
                    println("Video completed!")
                }

                override fun onRewardedVideoClosed(p0: Boolean) {
                    println("Video closed")
                }

                override fun onRewardedVideoExpired() {
                }

                override fun onRewardedVideoClicked() {
                }
            })
        }
    }
}