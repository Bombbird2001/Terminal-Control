package com.bombbird.terminalcontrol.ui.utilitybox

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Queue
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.airports.AirportName
import com.bombbird.terminalcontrol.utilities.errors.ErrorHandler
import org.json.JSONArray
import java.util.*
import kotlin.math.abs

class CommsManager(private val utilityBox: UtilityBox) {
    private val atcByeCtr = Array<String>()
    private val atcByeTwr = Array<String>()
    private val pilotBye = Array<String>()

    private val radarScreen = TerminalControl.radarScreen!!
    val labels: Queue<Label> = Queue()
    private val greetingByTime: String
        get() {
            val calendar = Calendar.getInstance(TimeZone.getDefault())
            val additional = if (calendar[Calendar.AM_PM] == Calendar.PM) 12 else 0
            val time = (calendar[Calendar.HOUR] + additional) * 100 + calendar[Calendar.MINUTE]
            var greeting = " good "
            greeting += when {
                time <= 1200 -> "morning"
                time <= 1700 -> "afternoon"
                else -> "evening"
            }
            return greeting
        }

    init {
        atcByeCtr.add("good day", "see you", "have a good flight", "have a safe flight")
        atcByeTwr.add("good day", "see you", "have a safe landing")
        pilotBye.add("good day", "see you", "have a nice day", "bye bye")
    }

    /** Loads the previous labels from save */
    fun loadLabels(save: JSONArray) {
        for (i in 0 until save.length()) {
            val info = save.getJSONObject(i)
            var color: Color
            val long = info.getString("color").toLong(16).toInt() //Workaround for NumberFormatException due to value > INT.MAX_VALUE
            color = Color(long)
            Gdx.app.postRunnable {
                val label = Label(info.getString("message"), utilityBox.getLabelStyle(color))
                updateLabelQueue(label)
            }
        }
    }

    /** Adds a message for the input aircraft to contact in the input frequency given the callsign of the next controller  */
    fun contactFreq(aircraft: Aircraft, callsign: String, freq: String) {
        val wake = aircraft.wakeString
        var bai = ", "
        bai += if (callsign.contains("Tower")) atcByeTwr.random() else atcByeCtr.random()
        if (aircraft.emergency.isActive) bai = ", have a safe landing"
        val finalBai = bai
        Gdx.app.postRunnable {
            val label = Label(aircraft.callsign + wake + ", contact " + callsign + " on " + freq + finalBai + ".", utilityBox.getLabelStyle(Color.BLACK))
            updateLabelQueue(label)
        }
        var bye = ", "
        bye += pilotBye.random().toString() + ", "
        val finalBye = bye
        Gdx.app.postRunnable {
            val label1 = Label(freq + finalBye + aircraft.callsign + wake, utilityBox.getLabelStyle(aircraft.color))
            updateLabelQueue(label1)
        }
    }

    /** Adds a message if the input aircraft goes around, with the reason for the go around  */
    fun goAround(aircraft: Aircraft, reason: String, state: Aircraft.ControlState) {
        var msg = ""
        if (state == Aircraft.ControlState.UNCONTROLLED) {
            msg = aircraft.callsign + " performed a go around due to " + reason
        } else if (state == Aircraft.ControlState.ARRIVAL) {
            val randomInt = MathUtils.random(0, 2)
            var goArdText = ""
            when (randomInt) {
                0 -> goArdText = "going around"
                1 -> goArdText = "we're going around"
                2 -> goArdText = "performing a missed approach"
            }
            TerminalControl.tts.goAroundMsg(aircraft, goArdText, reason)
            msg = aircraft.callsign + aircraft.wakeString + ", " + goArdText + " due to " + reason
        }
        alertMsg(msg)
    }

    /** Adds a message for an aircraft contacting the player for the first time  */
    fun initialContact(aircraft: Aircraft) {
        val apchCallsign = if (aircraft is Arrival) radarScreen.callsign else radarScreen.deptCallsign
        val wake = aircraft.wakeString
        val altitude: String = if (aircraft.altitude >= radarScreen.transLvl * 100) {
            "FL" + (aircraft.altitude / 100).toInt()
        } else {
            ((aircraft.altitude / 100).toInt() * 100).toString()
        }
        val clearedAltitude: String = if (aircraft.clearedAltitude >= radarScreen.transLvl * 100) {
            "FL" + aircraft.clearedAltitude / 100
        } else {
            (aircraft.clearedAltitude / 100 * 100).toString() + " feet"
        }
        var action: String
        val deltaAlt = aircraft.clearedAltitude - aircraft.altitude
        if (deltaAlt < -600) {
            action = "descending through $altitude for $clearedAltitude"
        } else if (deltaAlt > 400) {
            action = "climbing through $altitude for $clearedAltitude"
        } else {
            action = "levelling off at $clearedAltitude"
            if (abs(deltaAlt) <= 50) {
                action = "at $clearedAltitude"
            }
        }
        var text = ""
        var greeting = ""
        val random = MathUtils.random(2)
        if (random == 1) {
            greeting = greetingByTime
        } else if (random == 2) {
            greeting = " hello"
        }
        var starString = ""
        val starSaid = MathUtils.randomBoolean()
        if (starSaid) {
            starString = " on the " + aircraft.sidStar.name + " arrival"
        }
        var inboundString = ""
        val inboundSaid = MathUtils.randomBoolean()
        if (inboundSaid && !aircraft.isGoAroundWindow) {
            inboundString = ", inbound " + (aircraft.direct?.name ?: "somewhere")
        }
        var infoString = ""
        if (MathUtils.randomBoolean()) {
            infoString = if (MathUtils.randomBoolean()) {
                ", information " + radarScreen.information
            } else {
                ", we have information " + radarScreen.information
            }
        }
        if (aircraft is Arrival) {
            if (!aircraft.isGoAroundWindow && aircraft.direct != null) {
                text = apchCallsign + greeting + ", " + aircraft.callsign + wake + " with you, " + action + starString + inboundString + infoString
                TerminalControl.tts.initArrContact(aircraft, apchCallsign, greeting, action, aircraft.sidStar.pronunciation.toLowerCase(Locale.ROOT), starSaid, aircraft.direct?.name ?: "somewhere", inboundSaid, infoString)
            } else {
                action = (if (MathUtils.randomBoolean()) "going around, " else "missed approach, ") + action //Go around message
                text = apchCallsign + ", " + aircraft.callsign + wake + " with you, " + action + ", heading " + aircraft.clearedHeading
                TerminalControl.tts.goAroundContact(aircraft, apchCallsign, action, aircraft.clearedHeading.toString())
            }
        } else if (aircraft is Departure) {
            var outboundText = ""
            if ("-" != AirportName.getAirportName(aircraft.airport.icao)) outboundText = "outbound " + AirportName.getAirportName(aircraft.airport.icao) + ", "
            var sidString = ""
            val sidSaid = MathUtils.randomBoolean()
            if (sidSaid) {
                sidString = ", " + aircraft.sidStar.name + " departure"
            }
            val airborne = if (MathUtils.randomBoolean()) "" else "airborne "
            text = apchCallsign + greeting + ", " + aircraft.callsign + wake + " with you, " + outboundText + airborne + action + sidString
            TerminalControl.tts.initDepContact(aircraft, apchCallsign, greeting, outboundText, airborne, action, aircraft.sidStar.pronunciation.toLowerCase(Locale.ROOT), sidSaid)
        }
        val finalText = text
        Gdx.app.postRunnable {
            val label = Label(finalText, utilityBox.getLabelStyle(aircraft.color))
            updateLabelQueue(label)
        }
        radarScreen.soundManager.playInitialContact()
    }

    /** Says a request for an aircraft  */
    fun sayRequest(aircraft: Departure) {
        val wake = aircraft.wakeString
        val text = aircraft.callsign + wake
        var requestText = ""
        val random = MathUtils.random(1)
        when (aircraft.request) {
            Aircraft.HIGH_SPEED_REQUEST -> if (random == 0) {
                requestText = ", requesting high speed climb"
            } else if (random == 1) {
                requestText = ", we request high speed climb"
            }
            Aircraft.SHORTCUT_REQUEST -> if (random == 0) {
                requestText = ", requesting direct"
            } else if (random == 1) {
                requestText = ", request direct"
            }
            else -> Gdx.app.log("CommBox", "Unknown request code " + aircraft.request)
        }
        TerminalControl.tts.sayRequest(aircraft, requestText)
        val finalText = text + requestText
        Gdx.app.postRunnable {
            val label = Label(finalText, utilityBox.getLabelStyle(aircraft.color))
            updateLabelQueue(label)
        }
        radarScreen.soundManager.playInitialContact()
    }

    /** Requests higher climb for a departure  */
    fun requestHigherClimb(departure: Departure) {
        val wake = departure.wakeString
        val text = departure.callsign + wake
        val requestText: String = if (MathUtils.randomBoolean()) {
            ", requesting further climb"
        } else {
            ", we would like to climb higher"
        }
        TerminalControl.tts.sayRequest(departure, requestText)
        val finalText = text + requestText
        Gdx.app.postRunnable {
            val label = Label(finalText, utilityBox.getLabelStyle(departure.color))
            updateLabelQueue(label)
        }
        radarScreen.soundManager.playInitialContact()
    }

    /** Adds a message for an aircraft established in hold over a waypoint  */
    fun holdEstablishMsg(aircraft: Aircraft, wpt: String) {
        val wake = aircraft.wakeString
        var text = aircraft.callsign + wake
        val random = MathUtils.random(2)
        text += when (random) {
            0 -> " is established in the hold over $wpt"
            1 -> ", holding over $wpt"
            2 -> ", we're holding at $wpt"
            else -> ""
        }
        TerminalControl.tts.holdEstablishMsg(aircraft, wpt, random)
        val finalText = text
        Gdx.app.postRunnable {
            val label = Label(finalText, utilityBox.getLabelStyle(aircraft.color))
            updateLabelQueue(label)
        }
        radarScreen.soundManager.playInitialContact()
    }

    /** Adds a normal message  */
    fun normalMsg(msg: String?) {
        Gdx.app.postRunnable {
            for (i in 0..2) {
                try {
                    val label = Label(msg, utilityBox.getLabelStyle(Color.BLACK))
                    updateLabelQueue(label)
                    break
                } catch (e: NullPointerException) {
                    ErrorHandler.sendRepeatableError("Normal message error", e, i + 1)
                }
            }
        }
    }

    /** Adds an alert message in yellow  */
    fun alertMsg(msg: String?) {
        Gdx.app.postRunnable {
            for (i in 0..2) {
                try {
                    val label = Label(msg, utilityBox.getLabelStyle(Color.YELLOW))
                    updateLabelQueue(label)
                    break
                } catch (e: NullPointerException) {
                    ErrorHandler.sendRepeatableError("Alert message error", e, i + 1)
                }
            }
        }
        radarScreen.soundManager.playAlert()
    }

    /** Adds a message in red for warnings  */
    fun warningMsg(msg: String?) {
        Gdx.app.postRunnable {
            val label = Label(msg, utilityBox.getLabelStyle(Color.RED))
            updateLabelQueue(label)
        }
        radarScreen.soundManager.playConflict()
    }

    /** Adds the label to queue and removes labels if necessary, updates scrollPane to show messages  */
    private fun updateLabelQueue(label: Label) {
        labels.addLast(label)
        label.width = utilityBox.commsPane.width - 20
        label.setWrap(true)
        while (labels.size > 15) {
            labels.removeFirst()
        }
        utilityBox.commsTable.clearChildren()
        for (i in 0 until labels.size) {
            utilityBox.commsTable.add(labels[i]).width(utilityBox.commsPane.width - 20).pad(15f, 10f, 15f, 10f).actor.invalidate()
            utilityBox.commsTable.row()
        }

        try {
            utilityBox.commsPane.layout()
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            utilityBox.commsPane.layout()
        }
        utilityBox.commsPane.scrollTo(0f, 0f, 0f, 0f)
    }
}