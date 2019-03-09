package com.bombbird.terminalcontrol;

import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.widget.Toast;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.utilities.FileLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechManager extends AndroidApplication implements TextToSpeech.OnInitListener, com.bombbird.terminalcontrol.sounds.TextToSpeech {
    private TextToSpeech tts = null;
    public static final int ACT_CHECK_TTS_DATA = 1000;
    public static final int ACT_INSTALL_TTS_DATA = 1001;
    private static HashMap<String, String> callsigns;

    /** Performs relevant actions after receiving status for TTS data check  */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACT_CHECK_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                //Data exists, so we instantiate the TTS engine
                tts = new TextToSpeech(this, this);
            } else {
                //Data is missing, so we start the TTS installation process
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivityForResult(installIntent, ACT_INSTALL_TTS_DATA);
            }
        } else if (requestCode == ACT_INSTALL_TTS_DATA) {
            Intent ttsIntent = new Intent();
            ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA);
        }
    }

    /** Sets initial properties after initialisation of TTS is complete */
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (tts != null) {
                int result = tts.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TTS language is not supported", Toast.LENGTH_LONG).show();
                } else {
                    Gdx.app.log("Text to Speech", "TTS initialized successfully");
                    tts.setSpeechRate(1.7f);
                    callsigns = FileLoader.loadIcaoCallsigns();
                }
            }
        } else {
            Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_LONG).show();
            Gdx.app.log("Text to Speech", "TTS initialization failed");
        }
    }

    /** Stops, destroys TTS instance */
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /** Converts any FLXXX in text to flight level X X X */
    private String convertToFlightLevel(String action) {
        String[] actionList = action.split(" ");
        for (int i = 0; i < actionList.length; i++) {
            if (actionList[i].contains("FL")) {
                String altitude = convertNoToText(actionList[i].substring(2));
                String phrase = "flight level" + altitude;
                actionList[i] = phrase;
            }
        }
        return StringUtils.join(actionList, " ");
    }

    /** Converts any number into text due to special pronunciation requirements for 0, 9 and . */
    private String convertNoToText(String altitude) {
        String[] list = altitude.split("");
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals("0")) list[i] = "zero";
            if (list[i].equals("9")) list[i] = "niner";
            if (list[i].equals(".")) list[i] = "decimal";
        }
        return StringUtils.join(list, " ");
    }

    /** Says the text depending on API level */
    private void sayText(String text, String voice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.setVoice(new Voice(voice, Locale.ENGLISH, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null));
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    /** Speaks the initial contact for arrivals */
    @Override
    public void initArrContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String action, String star, String direct) {
        String callsign = callsigns.get(icao);
        String newFlightNo = convertNoToText(flightNo);
        String newAction = convertToFlightLevel(action);
        String newDirect = direct.toLowerCase();
        String text = apchCallsign + ", " + callsign + " " + newFlightNo + " " + wake + " with you, " + newAction + " on the " + star + " arrival, inbound " + newDirect;
        Gdx.app.log("TTS initArr", text);
        sayText(text, voice);
    }

    /** Speaks the contact from arrivals after going around */
    @Override
    public void goAroundContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String action, String heading) {
        String callsign = callsigns.get(icao);
        String newAction = convertToFlightLevel(action);
        String newFlightNo = convertNoToText(flightNo);
        String newHeading = StringUtils.join(heading.split(""), " ");
        String text = apchCallsign + ", " + callsign + newFlightNo + " " + wake + " with you, " + newAction + ", heading " + newHeading;
        Gdx.app.log("TTS goAround", text);
        sayText(text, voice);
    }

    /** Speaks the initial contact for departures */
    @Override
    public void initDepContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String airport, String action, String sid) {
        String callsign = callsigns.get(icao);
        action = convertToFlightLevel(action);
        flightNo = convertNoToText(flightNo);
        String text = apchCallsign + ", " + callsign + flightNo + " " + wake + " with you, outbound " + airport + ", " + action + ", " + sid + " departure";
        Gdx.app.log("TTS initDep", text);
        sayText(text, voice);
    }

    /** Speaks handover of aircraft to other frequencies */
    @Override
    public void contactOther(String voice, String frequency, String icao, String flightNo, String wake) {
        String newFreq = convertNoToText(frequency);
        String callsign = callsigns.get(icao);
        flightNo = convertNoToText(flightNo);
        String text = newFreq + ", good day, " + callsign + flightNo + " " + wake;
        Gdx.app.log("TTS contactOther", text);
        sayText(text, voice);
    }

    /** Test function */
    @Override
    public void test(HashMap<String, Star> stars, HashMap<String, Sid> sids) {
        for (Star star: stars.values()) {
            //System.out.println(star.getPronounciation());
            //tts.speak(star.getPronounciation(), TextToSpeech.QUEUE_ADD, null, null);
        }

        for (Sid sid: sids.values()) {
            //System.out.println(sid.getPronounciation());
            //tts.speak(sid.getPronounciation(), TextToSpeech.QUEUE_ADD, null, null);
        }
    }
}
