package com.bombbird.terminalcontrol;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.sounds.TextToSpeech;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.utilities.ToastManager;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import com.bombbird.terminalcontrol.utilities.saving.GameSaver;
import org.json.JSONObject;

public class TerminalControl extends Game {
    //Get screen size
    public static int WIDTH;
    public static int HEIGHT;

    //Is html? (freetype not supported in html)
    public static boolean ishtml;

    //Version info
    public static boolean full;
    public static String versionName;
    public static int versionCode;

    //Active gameScreen instance
    public static RadarScreen radarScreen = null;

    //Create texture stuff
    private static TextureAtlas buttonAtlas;
    public static Skin skin;

    //The one and only spritebatch
    public SpriteBatch batch;

    //Text-to-speech (for Android only)
    public static TextToSpeech tts;

    //Toast (for Android only)
    public static ToastManager toastManager;

    //Default settings
    public static int trajectorySel;
    public static boolean weatherSel;
    public static int soundSel;
    public static Emergency.Chance emerChance;
    public static boolean sendAnonCrash;

    public TerminalControl(TextToSpeech tts, ToastManager toastManager) {
        TerminalControl.tts = tts;
        TerminalControl.toastManager = toastManager;
    }

    public static void loadSettings() {
        JSONObject settings = FileLoader.loadSettings();
        if (settings == null) {
            //Default settings if save unavailable
            trajectorySel = 90;
            weatherSel = true;
            if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                soundSel = 1;
            } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
                soundSel = 2;
            }
            sendAnonCrash = true;
            emerChance = Emergency.Chance.MEDIUM;
            GameSaver.saveSettings(trajectorySel, weatherSel, soundSel, sendAnonCrash, emerChance);
        } else {
            trajectorySel = settings.getInt("trajectory");
            weatherSel = settings.getBoolean("weather");
            soundSel = settings.getInt("sound");
            sendAnonCrash = settings.optBoolean("sendCrash", true);
            if (settings.isNull("emerChance")) {
                emerChance = Emergency.Chance.MEDIUM;
            } else {
                emerChance = Emergency.Chance.valueOf(settings.getString("emerChance"));
            }
        }
    }

    public static void loadVersionInfo() {
        String[] info = Gdx.files.internal("game/type.type").readString().split(" ");
        full = !"lite".equals(info[0]);
        versionName = info[1];
        versionCode = Integer.parseInt(info[2]);
        FileLoader.mainDir = TerminalControl.full ? "AppData/Roaming/TerminalControlFull" : "AppData/Roaming/TerminalControl";
    }

    @Override
    public void create () {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();
        batch = new SpriteBatch();

        buttonAtlas = new TextureAtlas(Gdx.files.internal("game/ui/mainmenubuttons.atlas"));
        skin = new Skin();
        skin.addRegions(TerminalControl.buttonAtlas);

        Gdx.input.setCatchBackKey(true);

        this.setScreen(new MainMenuScreen(this, null));
    }

    @Override
    public void render () {
        super.render();
    }

    @Override
    public void dispose () {
        batch.dispose();
        Fonts.dispose();
        buttonAtlas.dispose();
        skin.dispose();
    }

}
