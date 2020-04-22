package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.ui.Ui;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ErrorHandler {
    private static String getVersionInfo() {
        String type = "Unknown";
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            type = "Android";
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            type = "Desktop";
        }
        String arpt = TerminalControl.radarScreen == null ? "unknown airport" : TerminalControl.radarScreen.mainName;
        return type + " " + (TerminalControl.full ? "full" : "lite") + " version " + TerminalControl.versionName + ", build " + TerminalControl.versionCode + ", " + arpt + "\n";
    }

    public static void sendGenericError(Exception e, boolean exit) {
        String error = getVersionInfo() + (exit ? "Crash" : "No crash") + "\n" + ExceptionUtils.getStackTrace(e);
        HttpRequests.sendError(error, 0);
        e.printStackTrace();
        if (!exit) return;
        //Quit game
        TerminalControl.radarScreen.getMetar().setQuit(true);
        RadarScreen.disposeStatic();
        Ui.disposeStatic();
        TerminalControl.radarScreen.dispose();
        Gdx.app.exit();
        if (Gdx.app.getType() == Application.ApplicationType.Android) throw new RuntimeException(e);
    }

    public static void sendStringError(Exception e, String str) {
        String error = getVersionInfo() + ExceptionUtils.getStackTrace(e);
        error = str + "\n" + error;
        HttpRequests.sendError(error, 0);
        e.printStackTrace();
        //Quit game
        if (TerminalControl.radarScreen != null) {
            TerminalControl.radarScreen.getMetar().setQuit(true);
            RadarScreen.disposeStatic();
        }
        Ui.disposeStatic();
        TerminalControl.radarScreen.dispose();
        Gdx.app.exit();
        if (Gdx.app.getType() == Application.ApplicationType.Android) throw new RuntimeException(e);
    }

    public static void sendSaveErrorNoThrow(Exception e, String str) {
        String error = getVersionInfo() + "No crash\n" + ExceptionUtils.getStackTrace(e);
        error = str + "\n" + error;
        HttpRequests.sendError(error, 0);
        e.printStackTrace();
        //Don't throw runtime exception
    }

    public static void sendRepeatableError(String original, Exception e, int attempt) {
        String error = getVersionInfo() + "Try " + attempt + ":\n" + original + "\n" + ExceptionUtils.getStackTrace(e);
        HttpRequests.sendError(error, 0);
        e.printStackTrace();
        System.out.println(original);
    }
}
