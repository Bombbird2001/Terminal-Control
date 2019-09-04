package com.bombbird.terminalcontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.sounds.Pronunciation;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechManager extends AndroidApplication implements TextToSpeech.OnInitListener, com.bombbird.terminalcontrol.sounds.TextToSpeech {
    private TextToSpeech tts = null;
    public static final int ACT_CHECK_TTS_DATA = 1000;
    public static final int ACT_INSTALL_TTS_DATA = 1001;

    public ToastManager toastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toastManager = new ToastManager((AndroidLauncher) this);
    }

    /** Performs relevant actions after receiving status for TTS data check  */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
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
        } catch (ActivityNotFoundException e) {
            toastManager.initTTSFail();
        }
    }

    /** Sets initial properties after initialisation of TTS is complete */
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (tts != null) {
                int result = tts.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    toastManager.ttsLangNotSupported();
                } else {
                    Gdx.app.log("Text to Speech", "TTS initialized successfully");
                    tts.setSpeechRate(1.7f);
                }
            }
        } else {
            toastManager.initTTSFail();
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

    /** Says the text depending on API level */
    private void sayText(String text, String voice) {
        if (tts == null) return;
        tts.setVoice(new Voice(voice, Locale.ENGLISH, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null));
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }

    /** Speaks the initial contact for arrivals */
    @Override
    public void initArrContact(String voice, String apchCallsign, String greeting, String icao, String flightNo, String wake, String action, String star, boolean starSaid, String direct, boolean inboundSaid, String info) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String callsign = Pronunciation.callsigns.get(icao);
        String newFlightNo = Pronunciation.convertNoToText(flightNo);
        String newAction = Pronunciation.convertToFlightLevel(action);
        String starString = "";
        if (starSaid) {
            starString = " on the " + star + " arrival";
        }
        String directString = "";
        if (inboundSaid) {
            String newDirect;
            if (Pronunciation.waypointPronunciations.containsKey(direct)) {
                newDirect = Pronunciation.waypointPronunciations.get(direct);
            } else {
                newDirect = Pronunciation.checkNumber(direct).toLowerCase();
            }
            directString = ", inbound " + newDirect;
        }
        String newInfo = "";
        if (info.length() >= 2) {
            newInfo = info.split("information ")[0] + "information " + Pronunciation.alphabetPronunciations.get(info.charAt(info.length() - 1));
        }
        String text = apchCallsign + greeting + ", " + callsign + " " + newFlightNo + " " + wake + " with you, " + newAction + starString + directString + newInfo;
        sayText(text, voice);
    }

    /** Speaks the contact from arrivals after going around */
    @Override
    public void goAroundContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String action, String heading) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String callsign = Pronunciation.callsigns.get(icao);
        String newAction = Pronunciation.convertToFlightLevel(action);
        String newFlightNo = Pronunciation.convertNoToText(flightNo);
        String newHeading = StringUtils.join(heading.split(""), " ");
        String text = apchCallsign + ", " + callsign + newFlightNo + " " + wake + " with you, " + newAction + ", heading " + newHeading;
        sayText(text, voice);
    }

    /** Speaks the initial contact for departures */
    @Override
    public void initDepContact(String voice, String apchCallsign, String greeting, String icao, String flightNo, String wake, String airport, String outbound,  String action, String sid, boolean sidSaid) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String callsign = Pronunciation.callsigns.get(icao);
        action = Pronunciation.convertToFlightLevel(action);
        String newFlightNo = Pronunciation.convertNoToText(flightNo);
        String sidString = "";
        if (sidSaid) {
            sidString = ", " + sid + " departure";
        }
        String text = apchCallsign + greeting + ", " + callsign + newFlightNo + " " + wake + " with you, " + outbound + action + sidString;
        sayText(text, voice);
    }

    @Override
    public void holdEstablishMsg(String voice, String icao, String flightNo, String wake, String wpt, int type) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String callsign = Pronunciation.callsigns.get(icao);
        String newFlightNo = Pronunciation.convertNoToText(flightNo);
        String text = callsign + newFlightNo + " " + wake;
        String newWpt;
        if (Pronunciation.waypointPronunciations.containsKey(wpt)) {
            newWpt = Pronunciation.waypointPronunciations.get(wpt);
        } else {
            newWpt = Pronunciation.checkNumber(wpt).toLowerCase();
        }
        if (type == 0) {
            text += " is established in the hold over " + newWpt;
        } else if (type == 1) {
            text += ", holding over " + newWpt;
        } else if (type == 2) {
            text += ", we're holding at " + newWpt;
        }

        sayText(text, voice);
    }

    /** Speaks handover of aircraft to other frequencies */
    @Override
    public void contactOther(String voice, String frequency, String icao, String flightNo, String wake) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String newFreq = Pronunciation.convertNoToText(frequency);
        String callsign = Pronunciation.callsigns.get(icao);
        String newFlightNo = Pronunciation.convertNoToText(flightNo);
        String text = newFreq + ", good day, " + callsign + newFlightNo + " " + wake;
        sayText(text, voice);
    }

    /** Speaks aircraft's low fuel call */
    @Override
    public void lowFuel(String voice, int status, String icao, String flightNo, char wakeCat) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String callsign = Pronunciation.callsigns.get(icao);
        String newFlightNo = Pronunciation.convertNoToText(flightNo);
        String wake = "";
        if (wakeCat == 'H') {
            wake = " heavy";
        } else if (wakeCat == 'J') {
            wake = " super";
        }
        String text = "";
        if (status == 0) {
            text = "Pan-pan, pan-pan, pan-pan, " + callsign + newFlightNo + " " + wake + " is low on fuel and requests priority landing.";
        } else if (status == 1) {
            text = "Mayday, mayday, mayday, " + callsign + newFlightNo + " " + wake + " requests immediate landing within 10 minutes or will divert.";
        } else if (status == 2) {
            text = callsign + newFlightNo + " " + wake + ", we are diverting to the alternate airport.";
        }
        sayText(text, voice);
    }

    /** Stops all current and subsequent speeches */
    public void cancel() {
        tts.stop();
    }

    /** Test function */
    @Override
    public void test(HashMap<String, Star> stars, HashMap<String, Sid> sids) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        for (Star star: stars.values()) {
            //System.out.println(star.getPronunciation());
            //tts.speak(star.getPronunciation(), TextToSpeech.QUEUE_ADD, null, null);
        }

        for (Sid sid: sids.values()) {
            //System.out.println(sid.getPronunciation());
            //tts.speak(sid.getPronunciation(), TextToSpeech.QUEUE_ADD, null, null);
        }
    }
}
