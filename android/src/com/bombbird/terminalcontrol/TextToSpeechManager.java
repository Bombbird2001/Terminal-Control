package com.bombbird.terminalcontrol;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
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
        if (TerminalControl.radarScreen.soundSel < 2) return;
        if (tts == null) return;
        tts.setVoice(new Voice(voice, Locale.ENGLISH, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null));
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
    }

    /** Speaks the initial contact for arrivals */
    @Override
    public void initArrContact(Aircraft aircraft, String apchCallsign, String greeting, String action, String star, boolean starSaid, String direct, boolean inboundSaid, String info) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
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
        String text = apchCallsign + greeting + ", " + icao + " " + newFlightNo + aircraft.getWakeString() + " with you, " + newAction + starString + directString + newInfo;
        sayText(text, aircraft.getVoice());
    }

    /** Speaks the contact from arrivals after going around */
    @Override
    public void goAroundContact(Aircraft aircraft, String apchCallsign, String action, String heading) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newAction = Pronunciation.convertToFlightLevel(action);
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String newHeading = StringUtils.join(heading.split(""), " ");
        String text = apchCallsign + ", " + icao + newFlightNo + aircraft.getWakeString() + " with you, " + newAction + ", heading " + newHeading;
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void goAroundMsg(Aircraft aircraft, String goArdText, String reason) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = icao + newFlightNo + aircraft.getWakeString() + ", " + goArdText + " due to " + reason;
        sayText(text, aircraft.getVoice());
    }

    /** Speaks the initial contact for departures */
    @Override
    public void initDepContact(Aircraft aircraft, String apchCallsign, String greeting, String outbound, String airborne,  String action, String sid, boolean sidSaid) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        action = Pronunciation.convertToFlightLevel(action);
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String sidString = "";
        if (sidSaid) {
            sidString = ", " + sid + " departure";
        }
        String text = apchCallsign + greeting + ", " + icao + newFlightNo + aircraft.getWakeString() + " with you, " + outbound + airborne + action + sidString;
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void holdEstablishMsg(Aircraft aircraft, String wpt, int type) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = icao + newFlightNo + aircraft.getWakeString();
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

        sayText(text, aircraft.getVoice());
    }

    /** Speaks handover of aircraft to other frequencies */
    @Override
    public void contactOther(Aircraft aircraft, String frequency) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String newFreq = Pronunciation.convertNoToText(frequency);
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = newFreq + ", good day, " + icao + newFlightNo + aircraft.getWakeString();
        sayText(text, aircraft.getVoice());
    }

    /** Speaks aircraft's low fuel call */
    @Override
    public void lowFuel(Aircraft aircraft, int status) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String wake = aircraft.getWakeString();
        String text = "";
        if (status == 0) {
            text = "Pan-pan, pan-pan, pan-pan, " + icao + newFlightNo + wake + " is low on fuel and requests priority landing.";
        } else if (status == 1) {
            text = "Mayday, mayday, mayday, " + icao + newFlightNo + wake + " is declaring a fuel emergency and requests immediate landing within 10 minutes or will divert.";
        } else if (status == 2) {
            text = icao + newFlightNo + wake + ", we are diverting to the alternate airport.";
        } else if (status == 3) {
            text = "Pan-pan, pan-pan, pan-pan, " + icao + newFlightNo + wake + " is low on fuel and will divert in 10 minutes if no landing runway is available.";
        } else if (status == 4) {
            text = "Mayday, mayday, mayday, " + icao + newFlightNo + wake + " is declaring a fuel emergency and is diverting immediately.";
        }
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void sayEmergency(Aircraft aircraft, String emergency, String intent) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String newIntent = Pronunciation.convertToFlightLevel(intent);
        String text = "Mayday, mayday, mayday, " + icao + newFlightNo + aircraft.getWakeString() + " is declaring " + emergency + " and would like to return to the airport" + newIntent;
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void sayRemainingChecklists(Aircraft aircraft, boolean dumpFuel) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = icao + newFlightNo + aircraft.getWakeString() + ", we'll need a few more minutes to run checklists" + (dumpFuel ? " before dumping fuel" : "");
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void sayReadyForDump(Aircraft aircraft) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = icao + newFlightNo + aircraft.getWakeString() + ", we are ready to dump fuel";
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void sayDumping(Aircraft aircraft) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = icao + newFlightNo + aircraft.getWakeString() + " is now dumping fuel";
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void sayRemainingDumpTime(Aircraft aircraft, int min) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = icao + newFlightNo + aircraft.getWakeString() + ", we'll need about " + min + " more minutes";
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void sayReadyForApproach(Aircraft aircraft, boolean stayOnRwy) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = icao + newFlightNo + aircraft.getWakeString() + " is ready for approach" + (stayOnRwy ? ", we will stay on the runway after landing" : "");
        sayText(text, aircraft.getVoice());
    }

    @Override
    public void sayRequest(Aircraft aircraft, String request) {
        if (TerminalControl.radarScreen.soundSel < 2) return;
        String icao = Pronunciation.callsigns.get(getIcaoCode(aircraft.getCallsign()));
        String newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.getCallsign()));
        String text = icao + newFlightNo + aircraft.getWakeString() + request;
        sayText(text, aircraft.getVoice());
    }

    /** Stops all current and subsequent speeches */
    public void cancel() {
        if (tts == null) return;
        tts.stop();
    }

    /** Gets the 3 letter ICAO code from callsign */
    private String getIcaoCode(String callsign) {
        if (callsign.length() < 3) Gdx.app.log("TTS", "Callsign is too short");
        String icao = callsign.substring(0, 3);
        if (!StringUtils.isAlpha(icao)) Gdx.app.log("TTS", "Invalid callsign");
        return icao;
    }

    /** Gets the flight number from callsign, returns as a string */
    private String getFlightNo(String callsign) {
        if (callsign.length() < 4) Gdx.app.log("TTS", "Callsign is too short");
        String flightNo = callsign.substring(3);
        if (!StringUtils.isNumeric(flightNo)) Gdx.app.log("TTS", "Invalid callsign");
        return flightNo;
    }

    /** Test function */
    @Override
    public void test(HashMap<String, Star> stars, HashMap<String, Sid> sids) {
        //Not implemented
        /*
        if (TerminalControl.radarScreen.soundSel < 2) return;
        for (Star star: stars.values()) {
            System.out.println(star.getPronunciation());
            tts.speak(star.getPronunciation(), TextToSpeech.QUEUE_ADD, null, null);
        }

        for (Sid sid: sids.values()) {
            System.out.println(sid.getPronunciation());
            tts.speak(sid.getPronunciation(), TextToSpeech.QUEUE_ADD, null, null);
        }
        */
    }

    /** Gets a random voice from all available voices */
    @Override
    public String getRandomVoice() {
        if (tts == null || tts.getVoices().isEmpty()) return null;
        Array<String> voices = new Array<>();
        for (Voice voice: tts.getVoices()) {
            if ("en".equals(voice.getName().substring(0, 2))) {
                voices.add(voice.getName());
            }
        }

        return voices.random();
    }
}
