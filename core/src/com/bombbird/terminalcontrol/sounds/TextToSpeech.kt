package com.bombbird.terminalcontrol.sounds

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.sidstar.Sid
import com.bombbird.terminalcontrol.entities.sidstar.Star
import java.util.*

interface TextToSpeech {
    fun initArrContact(aircraft: Aircraft, apchCallsign: String, greeting: String, action: String, star: String, starSaid: Boolean, direct: String, inboundSaid: Boolean, info: String)
    fun goAroundContact(aircraft: Aircraft, apchCallsign: String, action: String, heading: String)
    fun goAroundMsg(aircraft: Aircraft, goArdText: String, reason: String)
    fun initDepContact(aircraft: Aircraft, depCallsign: String, greeting: String, outbound: String, airborne: String, action: String, sid: String, sidSaid: Boolean)
    fun holdEstablishMsg(aircraft: Aircraft, wpt: String, type: Int)
    fun contactOther(aircraft: Aircraft, frequency: String)
    fun lowFuel(aircraft: Aircraft, status: Int)
    fun sayEmergency(aircraft: Aircraft, emergency: String, intent: String)
    fun sayRemainingChecklists(aircraft: Aircraft, dumpFuel: Boolean)
    fun sayReadyForDump(aircraft: Aircraft)
    fun sayDumping(aircraft: Aircraft)
    fun sayRemainingDumpTime(aircraft: Aircraft, min: Int)
    fun sayReadyForApproach(aircraft: Aircraft, stayOnRwy: Boolean)
    fun sayRequest(aircraft: Aircraft, request: String)
    fun cancel()
    fun test(stars: HashMap<String, Star>, sids: HashMap<String, Sid>)
    fun getRandomVoice(): String
}