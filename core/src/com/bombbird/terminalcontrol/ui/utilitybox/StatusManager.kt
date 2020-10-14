package com.bombbird.terminalcontrol.ui.utilitybox

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.TrafficFlowScreen
import kotlin.math.ceil

class StatusManager(private val utilityBox: UtilityBox) {
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

        val emergency = Array<String>()
        val runwayChange = Array<String>()
        val goAround = Array<String>()
        val requests = Array<String>()
        val initialContact = Array<String>()
        val info = Array<String>()

        for (aircraft: Aircraft in TerminalControl.radarScreen.aircrafts.values) {
            var text = "${aircraft.callsign}: "
            if (aircraft is Arrival && aircraft.emergency.isActive) {
                text += when {
                    aircraft.emergency.stayOnRwy && aircraft.isOnGround -> "Landed, runway ${aircraft.ils?.name?.substring(3)} closed"
                    aircraft.emergency.readyForApproach -> "Ready for approach" + if (aircraft.emergency.stayOnRwy) ", will remain on runway" else ""
                    aircraft.emergency.dumpingFuel -> "Dumping fuel" + if (aircraft.emergency.remainingTimeSaid) ", ${ceil(aircraft.emergency.fuelDumpTime.toDouble())} mins remaining" else ""
                    aircraft.emergency.checklistsSaid -> "Running checklists" + if (aircraft.emergency.fuelDumpRequired) ", fuel dump required" else ""
                    else -> when (aircraft.emergency.type) {
                        Emergency.Type.MEDICAL -> "Medical emergency"
                        Emergency.Type.ENGINE_FAIL -> "Engine failure"
                        Emergency.Type.BIRD_STRIKE -> "Bird strike"
                        Emergency.Type.HYDRAULIC_FAIL -> "Hydraulic issue"
                        Emergency.Type.PRESSURE_LOSS -> "Cabin pressure lost" + if (aircraft.altitude > 9500) ", in emergency descent" else ""
                        Emergency.Type.FUEL_LEAK -> "Fuel leak"
                        else -> "Unknown emergency"
                    }
                }
                emergency.add("[RED]$text")
            }

            text = "${aircraft.callsign}: "
            val colorStr = aircraft.color.toString()
            if (aircraft.isActionRequired) {
                if (aircraft is Departure && aircraft.isAskedForHigher) {
                    text += "Request higher"
                    requests.add("[#$colorStr]$text")
                } else if (aircraft.isRequested) {
                    when (aircraft.request) {
                        Aircraft.HIGH_SPEED_REQUEST -> {
                            text += "Request high speed"
                            requests.add("[#$colorStr]$text")
                        }
                        Aircraft.SHORTCUT_REQUEST -> {
                            text += "Request direct"
                            requests.add("[#$colorStr]$text")
                        }
                        else -> {
                            text += "Unknown request"
                            initialContact.add("[#$colorStr]$text")
                        }
                    }
                } else if (aircraft is Arrival && aircraft.isGoAroundWindow) {
                    text += "Missed approach"
                    goAround.add("[YELLOW]$text")
                } else {
                    text += "Initial contact"
                    initialContact.add("[#$colorStr]$text")
                }
            }
        }

        for (airport: Airport in TerminalControl.radarScreen.airports.values) {
            if (airport.isPendingRwyChange) runwayChange.add("[YELLOW]${airport.icao}: Pending runway change")
            if (airport.isClosed) info.add("[BLACK]${airport.icao}: Airport closed")
        }

        when (TerminalControl.radarScreen.trafficMode) {
            TrafficFlowScreen.NORMAL -> info.add("[BLACK]Traffic mode: Normal")
            TrafficFlowScreen.PLANES_IN_CONTROL -> {
                info.add("[BLACK]Traffic mode: Planes in control")
                info.add("[BLACK]Arrivals to control: ${TerminalControl.radarScreen.maxPlanes}")
            }
            TrafficFlowScreen.FLOW_RATE -> {
                info.add("[BLACK]Traffic mode: Flow rate")
                info.add("[BLACK]Arrival flow: ${TerminalControl.radarScreen.flowRate}/hr")
            }
        }

        if (TerminalControl.radarScreen.tfcMode == RadarScreen.TfcMode.ARRIVALS_ONLY) info.add("[BLACK]Arrivals only")

        val finalArray = Array<String>()
        finalArray.addAll(emergency)
        finalArray.addAll(runwayChange)
        finalArray.addAll(goAround)
        finalArray.addAll(requests)
        finalArray.addAll(initialContact)
        finalArray.addAll(info)

        utilityBox.statusLabel.setText(finalArray.joinToString(separator = "\n"))
    }
}