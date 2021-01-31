package com.bombbird.terminalcontrol

import android.view.Gravity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.widget.Toast
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager
import com.bombbird.terminalcontrol.utilities.PlayGamesInterface
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.games.Games
import com.google.android.gms.tasks.Task

class PlayGamesManager(private val activity: AndroidLauncher): PlayGamesInterface {
    var signedInAccount: GoogleSignInAccount? = null
        set(value) {
            if (field == null && value != null) {
                val client = Games.getGamesClient(activity, value)
                client.setViewForPopups(activity.view)
                client.setGravityForPopups(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
            }
            field = value
            if (value != null && UnlockManager.achievementList.size > 0) UnlockManager.checkGooglePlayAchievements()
        }

    override fun gameSignIn() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) != ConnectionResult.SUCCESS) {
            activity.runOnUiThread {
                val toast = Toast.makeText(
                    activity,
                    "Google Play Games is unavailable; please check your installation on Google Play Store.",
                    Toast.LENGTH_LONG
                )
                toast.show()
            }
        }
        val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        val account = GoogleSignIn.getLastSignedInAccount(activity)
        if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray)) {
            //Already signed in
            //The signed in account is stored in the 'account' variable
            signedInAccount = account
        } else {
            //Haven't been signed-in before; try the silent sign-in first
            val signInClient = GoogleSignIn.getClient(activity, signInOptions)
            signInClient.silentSignIn().addOnCompleteListener(
                activity
            ) { task: Task<GoogleSignInAccount?> ->
                if (task.isSuccessful) {
                    //The signed in account is stored in the task's result
                    signedInAccount = task.result
                } else {
                    //Player will need to sign-in explicitly using via UI
                    startSignInIntent()
                }
            }
        }
    }

    private fun startSignInIntent() {
        val signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
        val intent = signInClient.signInIntent
        activity.startActivityForResult(intent, AndroidLauncher.PLAY_SIGN_IN)
    }

    override fun gameSignOut() {
        val signInClient = GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
        )
        signInClient.signOut().addOnCompleteListener(activity) {
            //User is signed out
            signedInAccount = null
        }
    }

    override fun isSignedIn(): Boolean {
        return signedInAccount != null
    }

    override fun showAchievements() {
        signedInAccount?.let {
            Games.getAchievementsClient(activity, it).achievementsIntent.addOnSuccessListener { intent ->
                activity.startActivityForResult(intent, AndroidLauncher.PLAY_SHOW_ACHIEVEMENTS)
            }
        }
    }

    override fun unlockAchievement(id: String) {
        signedInAccount?.let {
            Games.getAchievementsClient(activity, it).unlock(id)
        }
    }

    override fun incrementAchievement(id: String, steps: Int, set: Boolean) {
        signedInAccount?.let {
            if (set) Games.getAchievementsClient(activity, it).setSteps(id, steps)
            else Games.getAchievementsClient(activity, it).increment(id, steps)
        }
    }
}