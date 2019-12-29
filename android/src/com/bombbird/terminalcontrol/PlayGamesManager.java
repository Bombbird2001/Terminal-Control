package com.bombbird.terminalcontrol;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.google.android.gms.common.ConnectionResult.SIGN_IN_REQUIRED;

public class PlayGamesManager {
    private static int RC_SIGN_IN = 777;

    protected void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                Toast toast = Toast.makeText(activity, "Login successful", Toast.LENGTH_LONG);
                toast.show();
            } else {
                String message = result.getStatus().getStatusMessage();
                Log.e("e", String.valueOf(result.getStatus()));
                if (result.getStatus().getStatusCode() == SIGN_IN_REQUIRED) {
                    activity.startActivityForResult(GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).getSignInIntent(), RC_SIGN_IN);
                }
                /*
                if (message == null || message.isEmpty()) {
                    Toast toast = Toast.makeText(activity, R.string.signin_other_error, Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(activity, message, Toast.LENGTH_LONG);
                    toast.show();
                }
                */
            }
        }
    }

    protected void gameSignIn(Activity activity) {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) != ConnectionResult.SUCCESS) {
            Toast toast = Toast.makeText(activity, "Google Play Games is unavailable; please check your installation on Google Play Store.", Toast.LENGTH_LONG);
            toast.show();
        }
        GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            // Already signed in.
            // The signed in account is stored in the 'account' variable.
            GoogleSignInAccount signedInAccount = account;
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, signInOptions);
            signInClient.silentSignIn().addOnCompleteListener(
                activity, task -> {
                    if (task.isSuccessful()) {
                        // The signed in account is stored in the task's result.
                        GoogleSignInAccount signedInAccount = task.getResult();
                    } else {
                        // Player will need to sign-in explicitly using via UI.
                        // See [sign-in best practices](http://developers.google.com/games/services/checklist) for guidance on how and when to implement Interactive Sign-in,
                        // and [Performing Interactive Sign-in](http://developers.google.com/games/services/android/signin#performing_interactive_sign-in) for details on how to implement
                        // Interactive Sign-in.
                        startSignInIntent(activity);
                    }
                });
        }
    }

    protected void startSignInIntent(Activity activity) {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        activity.startActivityForResult(intent, RC_SIGN_IN);
    }
}
