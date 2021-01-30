package com.bombbird.terminalcontrol.sounds

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import org.apache.commons.lang3.StringUtils
import java.util.*

class TextToSpeechManager {
    /** Speaks the initial contact for arrivals  */
    fun initArrContact(aircraft: Aircraft, apchCallsign: String, greeting: String, action: String, star: String, starSaid: Boolean, direct: String, inboundSaid: Boolean, info: String) {
        if (voiceDisabled()) return
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
        val text = "$apchCallsign$greeting, $icao $newFlightNo ${aircraft.wakeString} with you, $newAction$starString$directString$newInfo"
        sayText(text, aircraft.voice)
    }

    /** Speaks the contact from arrivals after going around  */
    fun goAroundContact(aircraft: Aircraft, apchCallsign: String, action: String, heading: String) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newAction = Pronunciation.convertToFlightLevel(action)
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val newHeading = StringUtils.join(heading.split("".toRegex()).toTypedArray(), " ")
        val text = "$apchCallsign, $icao $newFlightNo ${aircraft.wakeString} with you, $newAction, heading $newHeading"
        sayText(text, aircraft.voice)
    }

    fun goAroundMsg(aircraft: Aircraft, goArdText: String, reason: String) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = "$icao $newFlightNo ${aircraft.wakeString}, $goArdText due to $reason"
        sayText(text, aircraft.voice)
    }

    /** Speaks the initial contact for departures  */
    fun initDepContact(aircraft: Aircraft, depCallsign: String, greeting: String, outbound: String, airborne: String, action: String, sid: String, sidSaid: Boolean) {
        if (voiceDisabled()) return
        var newAction = action
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        newAction = Pronunciation.convertToFlightLevel(newAction)
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        var sidString = ""
        if (sidSaid) {
            sidString = ", $sid departure"
        }
        val text = "$depCallsign$greeting, $icao $newFlightNo ${aircraft.wakeString} with you, $outbound$airborne$newAction$sidString"
        sayText(text, aircraft.voice)
    }

    fun holdEstablishMsg(aircraft: Aircraft, wpt: String, type: Int) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        var text = "$icao $newFlightNo ${aircraft.wakeString}"
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

    /* Speaks handover of aircraft to other frequencies -- NOT USED
    fun contactOther(aircraft: Aircraft, frequency: String) {
        if (voiceDisabled()) return
        val newFreq = Pronunciation.convertNoToText(frequency)
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = "$newFreq, good day, $icao $newFlightNo ${aircraft.wakeString}"
        sayText(text, aircraft.voice)
    }
     */

    /** Speaks aircraft's low fuel call  */
    fun lowFuel(aircraft: Aircraft, status: Int) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val wake = aircraft.wakeString
        var text = ""
        when (status) {
            0 -> text = "Pan-pan, pan-pan, pan-pan, $icao $newFlightNo $wake is low on fuel and requests priority landing"
            1 -> text = "Mayday, mayday, mayday, $icao $newFlightNo $wake is declaring a fuel emergency and requests immediate landing within 10 minutes or will divert"
            2 -> text = "$icao $newFlightNo $wake, we are diverting to the alternate airport"
            3 -> text = "Pan-pan, pan-pan, pan-pan, $icao $newFlightNo $wake is low on fuel and will divert in 10 minutes if no landing runway is available"
            4 -> text = "Mayday, mayday, mayday, $icao $newFlightNo $wake is declaring a fuel emergency and is diverting immediately"
        }
        sayText(text, aircraft.voice)
    }

    fun sayEmergency(aircraft: Aircraft, emergency: String, intent: String) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val newIntent = Pronunciation.convertToFlightLevel(intent)
        val text = "Mayday, mayday, mayday, $icao $newFlightNo ${aircraft.wakeString} is declaring $emergency and would like to return to the airport$newIntent"
        sayText(text, aircraft.voice)
    }

    fun sayRemainingChecklists(aircraft: Aircraft, dumpFuel: Boolean) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = "$icao $newFlightNo ${aircraft.wakeString}, we'll need a few more minutes to run checklists${if (dumpFuel) " before dumping fuel" else ""}"
        sayText(text, aircraft.voice)
    }

    fun sayReadyForDump(aircraft: Aircraft) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = "$icao $newFlightNo ${aircraft.wakeString}, we are ready to dump fuel"
        sayText(text, aircraft.voice)
    }

    fun sayDumping(aircraft: Aircraft) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = "$icao $newFlightNo ${aircraft.wakeString} is now dumping fuel"
        sayText(text, aircraft.voice)
    }

    fun sayRemainingDumpTime(aircraft: Aircraft, min: Int) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = "$icao $newFlightNo ${aircraft.wakeString}, we'll need about $min more minutes"
        sayText(text, aircraft.voice)
    }

    fun sayReadyForApproach(aircraft: Aircraft, stayOnRwy: Boolean) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = "$icao $newFlightNo ${aircraft.wakeString} is ready for approach${if (stayOnRwy) ", we will stay on the runway after landing" else ""}"
        sayText(text, aircraft.voice)
    }

    fun sayRequest(aircraft: Aircraft, request: String) {
        if (voiceDisabled()) return
        val icao = Pronunciation.callsigns[getIcaoCode(aircraft.callsign)]
        val newFlightNo = Pronunciation.convertNoToText(getFlightNo(aircraft.callsign))
        val text = "$icao $newFlightNo ${aircraft.wakeString}$request"
        sayText(text, aircraft.voice)
    }

    /** Stops all current and subsequent speeches  */
    fun cancel() {
        TerminalControl.tts.cancel()
    }

    /** Says the text - will be called through interface */
    private fun sayText(text: String, voice: String) {
        TerminalControl.tts.sayText(text, voice)
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

    private fun voiceDisabled(): Boolean {
        return TerminalControl.radarScreen?.soundSel ?: -1 < 2
    }
}