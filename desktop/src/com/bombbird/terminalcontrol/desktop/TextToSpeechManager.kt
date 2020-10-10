package com.bombbird.terminalcontrol.desktop

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.sidstar.Sid
import com.bombbird.terminalcontrol.entities.sidstar.Star
import com.bombbird.terminalcontrol.sounds.TextToSpeech
import java.util.*

class TextToSpeechManager : TextToSpeech {
    override fun initArrContact(aircraft: Aircraft, apchCallsign: String, greeting: String, action: String, star: String, starSaid: Boolean, direct: String, inboundSaid: Boolean, info: String) {
        //No default implementation
    }

    override fun goAroundContact(aircraft: Aircraft, apchCallsign: String, action: String, heading: String) {
        //No default implementation
    }

    override fun goAroundMsg(aircraft: Aircraft, goArdText: String, reason: String) {
        //No default implementation
    }

    override fun initDepContact(aircraft: Aircraft, depCallsign: String, greeting: String, outbound: String, airborne: String, action: String, sid: String, sidSaid: Boolean) {
        //No default implementation
    }

    override fun holdEstablishMsg(aircraft: Aircraft, wpt: String, type: Int) {
        //No default implementation
    }

    override fun contactOther(aircraft: Aircraft, frequency: String) {
        //No default implementation
    }

    override fun lowFuel(aircraft: Aircraft, status: Int) {
        //No default implementation
    }

    override fun sayEmergency(aircraft: Aircraft, emergency: String, intent: String) {
        //No default implementation
    }

    override fun sayRemainingChecklists(aircraft: Aircraft, dumpFuel: Boolean) {
        //No default implementation
    }

    override fun sayReadyForDump(aircraft: Aircraft) {
        //No default implementation
    }

    override fun sayDumping(aircraft: Aircraft) {
        //No default implementation
    }

    override fun sayRemainingDumpTime(aircraft: Aircraft, min: Int) {
        //No default implementation
    }

    override fun sayReadyForApproach(aircraft: Aircraft, stayOnRwy: Boolean) {
        //No default implementation
    }

    override fun sayRequest(aircraft: Aircraft, request: String) {
        //No default implementation
    }

    override fun cancel() {
        //No default implementation
    }

    override fun test(stars: HashMap<String, Star>, sids: HashMap<String, Sid>) {
        //No default implementation
    }

    override fun getRandomVoice(): String {
        return ""
    }
}