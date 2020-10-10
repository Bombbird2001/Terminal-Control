package com.bombbird.terminalcontrol

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.Voice
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.sidstar.Sid
import com.bombbird.terminalcontrol.entities.sidstar.Star
import com.bombbird.terminalcontrol.sounds.Pronunciation
import com.bombbird.terminalcontrol.sounds.TextToSpeech
import org.apache.commons.lang3.StringUtils
import java.util.*

open class TextToSpeechManager : AndroidApplication(), OnInitListener, TextToSpeech {
    companion object {
        const val ACT_CHECK_TTS_DATA = 1000
        const val ACT_INSTALL_TTS_DATA = 1001
    }

    private var tts: android.speech.tts.TextToSpeech? = null
    var toastManager: ToastManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toastManager = ToastManager(this as AndroidLauncher)
    }

    /** Performs relevant actions after receiving status for TTS data check   */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        try {
            if (requestCode == ACT_CHECK_TTS_DATA) {
                if (resultCode == android.speech.tts.TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    //Data exists, so we instantiate the TTS engine
                    tts = android.speech.tts.TextToSpeech(this, this)
                } else {
                    //Data is missing, so we start the TTS installation process
                    val installIntent = Intent()
                    installIntent.action = android.speech.tts.TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                    startActivityForResult(installIntent, ACT_INSTALL_TTS_DATA)
                }
            } else if (requestCode == ACT_INSTALL_TTS_DATA) {
                val ttsIntent = Intent()
                ttsIntent.action = android.speech.tts.TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA)
            }
        } catch (e: ActivityNotFoundException) {
            toastManager?.initTTSFail()
        }
    }

    /** Sets initial properties after initialisation of TTS is complete  */
    override fun onInit(status: Int) {
        if (status == android.speech.tts.TextToSpeech.SUCCESS) {
            if (tts != null) {
                val result = tts?.setLanguage(Locale.ENGLISH)
                if (result == android.speech.tts.TextToSpeech.LANG_MISSING_DATA || result == android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED) {
                    toastManager?.ttsLangNotSupported()
                } else {
                    Gdx.app.log("Text to Speech", "TTS initialized successfully")
                    tts?.setSpeechRate(1.7f)
                }
            }
        } else {
            toastManager?.initTTSFail()
            Gdx.app.log("Text to Speech", "TTS initialization failed")
        }
    }

    /** Stops, destroys TTS instance  */
    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    /** Says the text depending on API level  */
    private fun sayText(text: String, voice: String) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        if (tts == null) return
        tts?.voice = Voice(voice, Locale.ENGLISH, Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, false, null)
        tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, null, null)
    }

    /** Speaks the initial contact for arrivals  */
    override fun initArrContact(aircraft: Aircraft, apchCallsign: String, greeting: String, action: String, star: String, starSaid: Boolean, direct: String, inboundSaid: Boolean, info: String) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val newAction = Pronunciation.convertToFlightLevel(action)
        var starString = ""
        if (starSaid) {
            starString = " on the $star arrival"
        }
        var directString = ""
        if (inboundSaid) {
            val newDirect: String? = if (Pronunciation.waypointPronunciations.containsKey(direct)) {
                Pronunciation.waypointPronunciations[direct]
            } else {
                Pronunciation.checkNumber(direct).toLowerCase(Locale.ROOT)
            }
            directString = ", inbound $newDirect"
        }
        var newInfo = ""
        if (info.length >= 2) {
            newInfo = info.split("information ".toRegex()).toTypedArray()[0] + "information " + Pronunciation.alphabetPronunciations[info[info.length - 1]]
        }
        val text = apchCallsign + greeting + ", " + icao + " " + newFlightNo + aircraft.wakeString + " with you, " + newAction + starString + directString + newInfo
        sayText(text, aircraft.voice)
    }

    /** Speaks the contact from arrivals after going around  */
    override fun goAroundContact(aircraft: Aircraft, apchCallsign: String, action: String, heading: String) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newAction = Pronunciation.convertToFlightLevel(action)
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val newHeading = StringUtils.join(heading.split("".toRegex()).toTypedArray(), " ")
        val text = apchCallsign + ", " + icao + newFlightNo + aircraft.wakeString + " with you, " + newAction + ", heading " + newHeading
        sayText(text, aircraft.voice)
    }

    override fun goAroundMsg(aircraft: Aircraft, goArdText: String, reason: String) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = icao + newFlightNo + aircraft.wakeString + ", " + goArdText + " due to " + reason
        sayText(text, aircraft.voice)
    }

    /** Speaks the initial contact for departures  */
    override fun initDepContact(aircraft: Aircraft, depCallsign: String, greeting: String, outbound: String, airborne: String, action: String, sid: String, sidSaid: Boolean) {
        var newAction = action
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        newAction = Pronunciation.convertToFlightLevel(newAction)
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        var sidString = ""
        if (sidSaid) {
            sidString = ", $sid departure"
        }
        val text = depCallsign + greeting + ", " + icao + newFlightNo + aircraft.wakeString + " with you, " + outbound + airborne + newAction + sidString
        sayText(text, aircraft.voice)
    }

    override fun holdEstablishMsg(aircraft: Aircraft, wpt: String, type: Int) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        var text = icao + newFlightNo + aircraft.wakeString
        val newWpt: String? = if (Pronunciation.waypointPronunciations.containsKey(wpt)) {
            Pronunciation.waypointPronunciations[wpt]
        } else {
            Pronunciation.checkNumber(wpt).toLowerCase(Locale.ROOT)
        }
        when (type) {
            0 -> text += " is established in the hold over $newWpt"
            1 -> text += ", holding over $newWpt"
            2 -> text += ", we're holding at $newWpt"
        }
        sayText(text, aircraft.voice)
    }

    /** Speaks handover of aircraft to other frequencies  */
    override fun contactOther(aircraft: Aircraft, frequency: String) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val newFreq = Pronunciation.convertNoToText(frequency)
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = newFreq + ", good day, " + icao + newFlightNo + aircraft.wakeString
        sayText(text, aircraft.voice)
    }

    /** Speaks aircraft's low fuel call  */
    override fun lowFuel(aircraft: Aircraft, status: Int) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val wake = aircraft.wakeString
        var text = ""
        when (status) {
            0 -> text = "Pan-pan, pan-pan, pan-pan, $icao$newFlightNo$wake is low on fuel and requests priority landing."
            1 -> text = "Mayday, mayday, mayday, $icao$newFlightNo$wake is declaring a fuel emergency and requests immediate landing within 10 minutes or will divert."
            2 -> text = "$icao$newFlightNo$wake, we are diverting to the alternate airport."
            3 -> text = "Pan-pan, pan-pan, pan-pan, $icao$newFlightNo$wake is low on fuel and will divert in 10 minutes if no landing runway is available."
            4 -> text = "Mayday, mayday, mayday, $icao$newFlightNo$wake is declaring a fuel emergency and is diverting immediately."
        }
        sayText(text, aircraft.voice)
    }

    override fun sayEmergency(aircraft: Aircraft, emergency: String, intent: String) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val newIntent = Pronunciation.convertToFlightLevel(intent)
        val text = "Mayday, mayday, mayday, " + icao + newFlightNo + aircraft.wakeString + " is declaring " + emergency + " and would like to return to the airport" + newIntent
        sayText(text, aircraft.voice)
    }

    override fun sayRemainingChecklists(aircraft: Aircraft, dumpFuel: Boolean) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = icao + newFlightNo + aircraft.wakeString + ", we'll need a few more minutes to run checklists" + if (dumpFuel) " before dumping fuel" else ""
        sayText(text, aircraft.voice)
    }

    override fun sayReadyForDump(aircraft: Aircraft) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = icao + newFlightNo + aircraft.wakeString + ", we are ready to dump fuel"
        sayText(text, aircraft.voice)
    }

    override fun sayDumping(aircraft: Aircraft) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = icao + newFlightNo + aircraft.wakeString + " is now dumping fuel"
        sayText(text, aircraft.voice)
    }

    override fun sayRemainingDumpTime(aircraft: Aircraft, min: Int) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = icao + newFlightNo + aircraft.wakeString + ", we'll need about " + min + " more minutes"
        sayText(text, aircraft.voice)
    }

    override fun sayReadyForApproach(aircraft: Aircraft, stayOnRwy: Boolean) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = icao + newFlightNo + aircraft.wakeString + " is ready for approach" + if (stayOnRwy) ", we will stay on the runway after landing" else ""
        sayText(text, aircraft.voice)
    }

    override fun sayRequest(aircraft: Aircraft, request: String) {
        if (TerminalControl.radarScreen.soundSel < 2) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = icao + newFlightNo + aircraft.wakeString + request
        sayText(text, aircraft.voice)
    }

    /** Stops all current and subsequent speeches  */
    override fun cancel() {
        tts?.stop()
    }

    /** Gets the 3 letter ICAO code from callsign  */
    private fun getIcaoCode(callsign: String): String {
        if (callsign.length < 3) Gdx.app.log("TTS", "Callsign is too short")
        val icao = callsign.substring(0, 3)
        if (!StringUtils.isAlpha(icao)) Gdx.app.log("TTS", "Invalid callsign")
        return icao
    }

    /** Gets the flight number from callsign, returns as a string  */
    private fun getFlightNo(callsign: String): String {
        if (callsign.length < 4) Gdx.app.log("TTS", "Callsign is too short")
        val flightNo = callsign.substring(3)
        if (!StringUtils.isNumeric(flightNo)) Gdx.app.log("TTS", "Invalid callsign")
        return flightNo
    }

    /** Test function  */
    override fun test(stars: HashMap<String, Star>, sids: HashMap<String, Sid>) {
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

    /** Gets a random voice from all available voices  */
    override fun getRandomVoice(): String {
        try {
            if (tts?.voices?.isEmpty() != false) return ""
        } catch (e: NullPointerException) {
            return ""
        }
        val voices = Array<String>()
        tts?.voices?.let {
            for (voice in it) {
                if ("en" == voice.name.substring(0, 2)) {
                    voices.add(voice.name)
                }
            }
        }
        return voices.random() ?: ""
    }
}