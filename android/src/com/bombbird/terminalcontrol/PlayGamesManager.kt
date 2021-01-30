package com.bombbird.terminalcontrol

import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task

class PlayGamesManager {
    fun gameSignIn(activity: Activity) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) != ConnectionResult.SUCCESS) {
            val toast = Toast.makeText(
                activity,
                "Google Play Games is unavailable; please check your installation on Google Play Store.",
                Toast.LENGTH_LONG
            )
            toast.show()
        }
        val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray)) {
            // Already signed in.
            // The signed in account is stored in the 'account' variable.
            val signedInAccount = account
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            val signInClient = GoogleSignIn.getClient(activity, signInOptions)
            signInClient.silentSignIn().addOnCompleteListener(
                activity
            ) { task: Task<GoogleSignInAccount?> ->
                if (task.isSuccessful) {
                    // The signed in account is stored in the task's result.
                    val signedInAccount = task.result
                } else {
                    // Player will need to sign-in explicitly using via UI.
                    // See [sign-in best practices](http://developers.google.com/games/services/checklist) for guidance on how and when to implement Interactive Sign-in,
                    // and [Performing Interactive Sign-in](http://developers.google.com/games/services/android/signin#performing_interactive_sign-in) for details on how to implement
                    // Interactive Sign-in.
                    startSignInIntent(activity)
                }
            }
        }
    }

    private fun startSignInIntent(activity: Activity) {
        val signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
        val intent = signInClient.signInIntent
        activity.startActivityForResult(intent, AndroidLauncher.PLAY_SIGN_IN)
    }
}