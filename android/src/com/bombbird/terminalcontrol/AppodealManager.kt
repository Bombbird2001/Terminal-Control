package com.bombbird.terminalcontrol

import android.app.Activity
import com.bombbird.terminalcontrol.utilities.Values

class AppodealManager(private val activity: Activity) {
    //private var consentForm: ConsentForm? = null

    fun initAppodeal() {
        /*
        val consentManager = ConsentManager.getInstance(activity)
        consentManager?.requestConsentInfoUpdate(Values.APPODEAL_KEY,
        object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(p0: Consent?) {
                //No default implementation
            }

            override fun onFailedToUpdateConsentInfo(p0: ConsentManagerException?) {
                println("Failed to update consent info")
            }
        }) ?: return
        if (consentManager.shouldShowConsentDialog() == Consent.ShouldShow.TRUE) {
            showConsentForm(consentManager)
        } else {
            consentManager.consent?.let {
                println("Consent is ${it.status.name}")
                Appodeal.initialize(activity, Values.APPODEAL_KEY, Appodeal.REWARDED_VIDEO, it)
            }
        }
         */
    }

    /*
    private fun showConsentForm(consentManager: ConsentManager) {
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
                        consentManager.consent?.let {
                            println("Consent is ${it.status.name}")
                            Appodeal.initialize(activity, Values.APPODEAL_KEY, Appodeal.REWARDED_VIDEO, it)
                        }
                    }
                }).build()
        }
        // If Consent request form is already loaded, then we can display it, otherwise, we should load it first
        if (consentForm?.isLoaded == true) {
            consentForm?.showAsActivity()
        } else consentForm?.load()
    }
     */
}