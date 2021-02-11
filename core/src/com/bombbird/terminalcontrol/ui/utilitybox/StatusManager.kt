package com.bombbird.terminalcontrol.ui.utilitybox

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.separation.SeparationChecker
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.TrafficFlowScreen
import kotlin.math.ceil

class StatusManager(private val utilityBox: UtilityBox) {
    private val radarScreen = TerminalControl.radarScreen!!
    var active = false
        set(value) {
            if (value && !field) timer = -1f //Reset if was inactive but now is active
            field = value
        }
    var timer = -1f

    fun update() {
        if (!active || !utilityBox.statusPane.isVisible) return
        timer -= Gdx.graphics.deltaTime
        if (timer > 0) return
        timer = 2f

        val conflicts = Array<String>()
        val emergency = Array<String>()
        val runwayChange = Array<String>()
        val congested = Array<String>()
        val goAround = Array<String>()
        val requests = Array<String>()
        val initialContact = Array<String>()
        val info = Array<String>()

        for ((index, callsign) in radarScreen.separationChecker.allConflictCallsigns.withIndex()) {
            val conflictMsg = if (index >= radarScreen.separationChecker.allConflicts.size) "???" else {
                when (radarScreen.separationChecker.allConflicts[index]) {
                    SeparationChecker.NORMAL_CONFLICT -> "3nm, 1000ft infringement"
                    SeparationChecker.ILS_LESS_THAN_10NM -> "2.5nm, 1000ft infringement - both aircraft less than 10nm final on same ILS"
                    SeparationChecker.PARALLEL_ILS -> "2nm, 1000ft infringement - aircraft on parallel ILS"
                    SeparationChecker.ILS_NTZ -> "Simultaneous ILS approach NTZ infringement"
                    SeparationChecker.MVA -> "MVA sector infringement"
                    SeparationChecker.SID_STAR_MVA -> "MVA sector infringement - aircraft deviates from SID/STAR route or is below minimum waypoint altitude"
                    SeparationChecker.RESTRICTED -> "Restricted area infringement"
                    SeparationChecker.WAKE_INFRINGE -> "Wake separation infringement"
                    SeparationChecker.STORM -> "Flying in thunder storm"
                    else -> "???"
                }
            }
            conflicts.add("[RED]$callsign: $conflictMsg")
        }

        for (aircraft: Aircraft in radarScreen.aircrafts.values) {
            var text = "${aircraft.callsign}: "
            if (aircraft is Arrival && aircraft.emergency.isActive) {
                text += when {
                    aircraft.emergency.isStayOnRwy && aircraft.isOnGround -> "Landed, runway ${aircraft.ils?.name?.substring(3)} closed"
                    aircraft.emergency.isReadyForApproach -> "Ready for approach" + if (aircraft.emergency.isStayOnRwy) ", will remain on runway" else ""
                    aircraft.emergency.isDumpingFuel -> "Dumping fuel" + if (aircraft.emergency.isRemainingTimeSaid) ", ${ceil(aircraft.emergency.fuelDumpTime.toDouble() / 60)} mins remaining" else ""
                    aircraft.emergency.isChecklistsSaid -> "Running checklists" + if (aircraft.emergency.isFuelDumpRequired) ", fuel dump required" else ""
                    else -> when (aircraft.emergency.type) {
                        Emergency.Type.MEDICAL -> "Medical emergency"
                        Emergency.Type.ENGINE_FAIL -> "Engine failure"
                        Emergency.Type.BIRD_STRIKE -> "Bird strike"
                        Emergency.Type.HYDRAULIC_FAIL -> "Hydraulic issue"
                        Emergency.Type.PRESSURE_LOSS -> "Cabin pressure lost" + if (aircraft.altitude > 9500) ", in emergency descent" else ""
                        Emergency.Type.FUEL_LEAK -> "Fuel leak"
                    }
                }
                emergency.add("[RED]$text")
            }

            val colorStr = aircraft.color.toString()
            if (aircraft.isActionRequired) {
                var default = true
                if (aircraft is Departure && aircraft.isAskedForHigher) {
                    default = false
                    text = "${aircraft.callsign}: Request higher"
                    requests.add("[#$colorStr]$text")
                }
                if (aircraft.stormWarningTime < 0) {
                    default = false
                    text = "${aircraft.callsign}: ${if (aircraft.isLocCap) "Request to cancel approach due to weather" else "Request different heading for weather"}"
                    requests.add("[#$colorStr]$text")
                }

                text = "${aircraft.callsign}: "
                if (aircraft.isRequested) {
                    default = false
                    text += when (aircraft.request) {
                        Aircraft.HIGH_SPEED_REQUEST -> "Request high speed"
                        Aircraft.SHORTCUT_REQUEST -> "Request direct"
                        else -> "Unknown request"
                    }
                    requests.add("[#$colorStr]$text")
                }
                if (aircraft is Arrival && aircraft.isGoAroundWindow) {
                    default = false
                    text = "${aircraft.callsign}: Missed approach"
                    goAround.add("[YELLOW]$text")
                }

                if (default) {
                    text += "Initial contact"
                    initialContact.add("[#$colorStr]$text")
                }
            }
        }

        if (DayNightManager.isNight) info.add("[BLACK]Night mode active")

        for (airport: Airport in radarScreen.airports.values) {
            if (airport.isPendingRwyChange) runwayChange.add("[YELLOW]${airport.icao}: Pending runway change")
            if (airport.isClosed) info.add("[BLACK]${airport.icao}: Airport closed")
            if (airport.isCongested) congested.add("[YELLOW]${airport.icao}: Departure congestion")
        }

        when (radarScreen.trafficMode) {
            TrafficFlowScreen.NORMAL -> info.add("[BLACK]Traffic mode: Normal")
            TrafficFlowScreen.PLANES_IN_CONTROL -> {
                info.add("[BLACK]Traffic mode: Arrivals in control")
                info.add("[BLACK]Arrivals to control: ${radarScreen.maxPlanes}")
            }
            TrafficFlowScreen.FLOW_RATE -> {
                info.add("[BLACK]Traffic mode: Flow rate")
                info.add("[BLACK]Arrival flow: ${radarScreen.flowRate}/hr")
            }
        }

        if (radarScreen.tfcMode == RadarScreen.TfcMode.ARRIVALS_ONLY) info.add("[BLACK]Arrivals only")
        if (radarScreen.remainingTime > 0) info.add("[BLACK]${if (radarScreen.remainingTime >= 60) "${radarScreen.remainingTime / 60}h" else "${radarScreen.remainingTime} ${if (radarScreen.remainingTime == 1) "min" else "mins"}"} of play time remaining")

        val finalArray = Array<String>()
        finalArray.addAll(conflicts)
        finalArray.addAll(emergency)
        finalArray.addAll(runwayChange)
        finalArray.addAll(congested)
        finalArray.addAll(goAround)
        finalArray.addAll(requests)
        finalArray.addAll(initialContact)
        finalArray.addAll(info)

        utilityBox.statusLabel.setText(finalArray.joinToString(separator = "\n"))
    }
}