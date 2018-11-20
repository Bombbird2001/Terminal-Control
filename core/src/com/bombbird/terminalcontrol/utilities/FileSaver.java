package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.json.JSONArray;
import org.json.JSONObject;

public class FileSaver {
    public static void saveGame() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("aircrafts", saveAircraft());
        jsonObject.put("airports", saveAirports());

        FileHandle handle = null;
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            //If desktop, save to external roaming appData
            handle = Gdx.files.external("AppData/Roaming/TerminalControl/saves/Test.json");
            if (!handle.exists()) {
                handle.mkdirs();
            }
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            //If Android, check first if external storage available
            if (Gdx.files.isExternalStorageAvailable()) {
                //External available
                handle = Gdx.files.external("com.bombbird.terminalcontrol/data/saves/Test.json");
            } else if (Gdx.files.isLocalStorageAvailable()) {
                //External not available; fallback on local
                handle = Gdx.files.local("saves/Test.json");
            } else {
                Gdx.app.log("Storage error", "Both local and external storage unavailable for Android!");
            }
        }

        if (handle != null) {
            handle.writeString(jsonObject.toString(), false);
        }
    }

    private static JSONArray saveAircraft() {
        return new JSONArray();
    }

    private static JSONArray saveAirports() {
        return new JSONArray();
    }
}
