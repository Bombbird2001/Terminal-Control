package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.screens.ui.Ui;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ErrorHandler {
    public static void sendGenericError(Exception e) {
        String error = ExceptionUtils.getStackTrace(e);
        HttpRequests.sendError(error, 0);
        e.printStackTrace();
        //Quit game
        TerminalControl.radarScreen.getMetar().setQuit(true);
        RadarScreen.disposeStatic();
        Ui.disposeStatic();
        TerminalControl.radarScreen.dispose();
        Gdx.app.exit();
        if (Gdx.app.getType() == Application.ApplicationType.Android) throw new RuntimeException(e);
    }

    public static void sendMetarError(Label label, Exception e) {
        String error = label.getText().toString() + "\n" + ExceptionUtils.getStackTrace(e);
        HttpRequests.sendError(error, 0);
        e.printStackTrace();
        System.out.println(label.getText().toString());
    }
}
