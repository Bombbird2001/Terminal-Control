package com.bombbird.terminalcontrol

import android.content.Context
import android.view.Gravity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.widget.Toast
import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager
import com.bombbird.terminalcontrol.screens.BasicScreen
import com.bombbird.terminalcontrol.screens.PlayGamesScreen
import com.bombbird.terminalcontrol.ui.dialogs.CustomDialog
import com.bombbird.terminalcontrol.utilities.PlayServicesInterface
import com.bombbird.terminalcontrol.utilities.SurveyAdsManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.games.Games
import com.google.android.gms.tasks.Task
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.pollfish.Pollfish
import kotlin.collections.HashMap

class PlayServicesManager(private val activity: AndroidLauncher): PlayServicesInterface {
    private var driveManager: DriveManager? = null
    var drivePermissionGranted = false
    var signedInAccount: GoogleSignInAccount? = null
        set(value) {
            if (field == null && value != null) {
                val client = Games.getGamesClient(activity, value)
                client.setViewForPopups(activity.view)
                client.setGravityForPopups(Gravity.TOP or Gravity.CENTER_HORIZONTAL)
                val pref = activity.getPreferences(Context.MODE_PRIVATE)
                if (pref.getBoolean("declinePlaySignIn", false)) with (pref.edit()) {
                    putBoolean("declinePlaySignIn", false)
                    apply()
                }
            }
            field = value
            if (value != null && UnlockManager.achievementList.size > 0) UnlockManager.checkGooglePlayAchievements()
            Gdx.app.postRunnable { (activity.game.screen as? PlayGamesScreen)?.updateSignInStatus() }
        }

    var save = true

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
        val signInOptions = getSignInOptions()
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
        val signInClient = GoogleSignIn.getClient(activity, getSignInOptions())
        activity.startActivityForResult(signInClient.signInIntent, AndroidLauncher.PLAY_SIGN_IN)
    }

    override fun gameSignOut() {
        val signInClient = GoogleSignIn.getClient(activity, getSignInOptions())
        signInClient.signOut().addOnCompleteListener(activity) {
            //User is signed out
            signedInAccount = null
            driveManager = null
            drivePermissionGranted = false
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

    override fun driveSaveGame() {
        save = true
        startDriveSignIn()
        driveManager?.saveGame()
    }

    override fun driveLoadGame() {
        save = false
        startDriveSignIn()
        driveManager?.loadGame()
    }

    private fun startDriveSignIn() {
        if (driveManager != null) return
        if (!GoogleSignIn.hasPermissions(signedInAccount, Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.EMAIL)) && !drivePermissionGranted) {
            //If google sign in claims has no permission, and the permission granted flag agrees, request permission
            requestPermissions()
        } else {
            //If google sign in has permission, or permission granted flag is true, sign in to drive
            drivePermissionGranted = true
            driveSignIn()
        }
    }

    fun requestPermissions() {
        (activity.game.screen as? BasicScreen)?.let {
            object : CustomDialog("Google Drive access", "Terminal Control will request access to your\nGoogle Drive account in order to store game\ndata on the cloud. The app will have access only\nto the game data files, and will not have\naccess to other files on your Drive.", "", "Ok", height = 750) {
                override fun result(resObj: Any?) {
                    if (resObj == DIALOG_POSITIVE) {
                        GoogleSignIn.requestPermissions(activity, AndroidLauncher.DRIVE_PERMISSION, signedInAccount, Scope(Scopes.DRIVE_APPFOLDER), Scope(Scopes.EMAIL))
                    }
                }
            }.show(it.stage)
        }
    }

    private fun driveSignIn() {
        val credential = GoogleAccountCredential.usingOAuth2(
            activity.applicationContext, arrayListOf(Scopes.DRIVE_APPFOLDER, Scopes.EMAIL)
        )
        signedInAccount?.account?.let { credential.selectedAccount = it }
        val googleDriveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Terminal Control").build()
        driveManager = DriveManager(googleDriveService, activity)
    }

    fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).requestEmail().build()
    }

    override fun isSurveyAvailable(): Boolean {
        return Pollfish.isPollfishPresent()
    }

    override fun showSurvey(airport: String) {
        activity.pollfishManager.showSurvey(airport)
    }

    override fun showAd(airport: String): Boolean {
        return activity.appodealManager.showAd(airport)
    }

    override fun showAdConsentForm(showAdAfter: Boolean) {
        activity.appodealManager.showConsentForm(showAdAfter)
    }

    override fun getAirportRewardTiming(): HashMap<String, String> {
        val map = HashMap<String, String>()
        val pref = activity.getPreferences(Context.MODE_PRIVATE)
        for (airport in SurveyAdsManager.unlockableAirports) {
            val dateTime = pref.getString(airport, null) ?: continue
            map[airport] = dateTime
        }
        return map
    }
}