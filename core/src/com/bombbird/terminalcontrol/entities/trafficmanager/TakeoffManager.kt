package com.bombbird.terminalcontrol.entities.trafficmanager

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.AircraftType
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.waketurbulence.SeparationMatrix
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import com.bombbird.terminalcontrol.utilities.math.RandomGenerator.randomPlane
import org.json.JSONObject
import java.util.*

class TakeoffManager {
    private val airport: Airport
    val nextAircraft: HashMap<String, Array<String>?>
    val prevAircraft: HashMap<String, Aircraft?>
    val timers: HashMap<String, Float>
    private val radarScreen = TerminalControl.radarScreen!!

    constructor(airport: Airport) {
        this.airport = airport
        nextAircraft = HashMap()
        prevAircraft = HashMap()
        timers = HashMap()
        for (runway in airport.runways.values) {
            timers[runway.name] = 180f
            prevAircraft[runway.name] = null
        }
    }

    constructor(airport: Airport, save: JSONObject) {
        this.airport = airport
        nextAircraft = HashMap()
        prevAircraft = HashMap()
        timers = HashMap()
        for (runway in airport.runways.values) {
            val info = save.getJSONObject("nextAircraft").getJSONArray(runway.name)
            if (info.length() == 2) {
                nextAircraft[runway.name] = arrayOf(info.getString(0), info.getString(1))
            } else {
                nextAircraft[runway.name] = null
            }
            timers[runway.name] = save.getJSONObject("timers").getDouble(runway.name).toFloat()
        }
    }

    /** Called after aircraft load during game load since aircraft have not been loaded during the initial airport loading  */
    fun updatePrevAcft(save: JSONObject) {
        for (runway in airport.runways.values) {
            prevAircraft[runway.name] = if (save.getJSONObject("prevAircraft").isNull(runway.name)) null else radarScreen.aircrafts[save.getJSONObject("prevAircraft").getString(runway.name)]
        }
    }

    /** Update loop  */
    fun update() {
        //Request takeoffs if takeoffs are less than 5 more than landings
        //Update the timers & next aircrafts to take off
        for (rwy in timers.keys) {
            timers[rwy] = timers[rwy]?.plus(Gdx.graphics.deltaTime) ?: 0f
            if (nextAircraft[rwy] == null) {
                val aircraftInfo = randomPlane(airport, radarScreen.allAircraft)
                nextAircraft[rwy] = aircraftInfo
            }
        }
        if (airport.airborne - airport.landings < 5) {
            when (airport.icao) {
                "TCTP" -> updateTCTP()
                "TCSS" -> updateTCSS()
                "TCWS" -> updateTCWS()
                "TCTT" -> updateTCTT()
                "TCAA" -> updateTCAA()
                "TCBB" -> updateTCBB()
                "TCOO" -> updateTCOO()
                "TCBE" -> updateTCBE()
                "TCHH" -> updateTCHH()
                "TCMC" -> updateTCMC()
                "TCBD" -> updateTCBD()
                "TCBS" -> updateTCBS()
                "TCMD" -> updateTCMD()
                "TCPG" -> updateTCPG()
                "TCPO" -> updateTCPO()
                "TCHX" -> updateTCHX()
                else -> Gdx.app.log("Takeoff manager", "Takeoff settings for " + airport.icao + " are unavailable.")
            }
        }
    }

    /** Checks takeoff status for Taiwan Taoyuan  */
    private fun updateTCTP() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("05L" == runway1.name && checkPreceding("05R") && checkOppLanding(airport.runways["05R"])) {
                    runway = runway1
                    dist = distance
                } else if ("05R" == runway1.name && checkPreceding("05L") && checkOppLanding(airport.runways["05L"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("23L" == runway1.name && checkPreceding("23R") && checkOppLanding(airport.runways["23R"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("23R" == runway1.name && checkPreceding("23L") && checkOppLanding(airport.runways["23L"])) {
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Taipei Songshan  */
    private fun updateTCSS() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding("10") && checkPreceding("28") && checkLanding(runway1) && checkOppLanding(runway1) && distance > dist) {
                runway = runway1
                dist = distance
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Singapore Changi  */
    private fun updateTCWS() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("02L" == runway1.name && checkPreceding("02C") && checkOppLanding(airport.runways["02C"])) {
                    runway = runway1
                    dist = distance
                } else if ("02C" == runway1.name && checkPreceding("02L") && checkOppLanding(airport.runways["02L"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("20C" == runway1.name && checkPreceding("20R") && checkOppLanding(airport.runways["20R"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("20R" == runway1.name && checkPreceding("20C") && checkOppLanding(airport.runways["20C"])) {
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Tokyo Haneda  */
    private fun updateTCTT() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if (airport.allowSimultDep() && timers["05"] ?: 0f >= 60 && "34R" == runway1.name && checkOppLanding(airport.runways["04"]) && checkOppLanding(airport.runways["05"])) {
                    //Additional check for runway 05 departure - 60 seconds apart
                    //Don't use 34R if departure volume is low
                    runway = runway1
                    dist = distance
                } else if ((!airport.allowSimultDep() || timers["34R"] ?: 0f >= 60) && "05" == runway1.name && checkOppLanding(airport.runways["04"]) && checkPreceding("16L") && checkPreceding("16R")) {
                    //Additional check for runway 34R departure - 60 seconds apart
                    //Additional check if aircraft landing on 34R has touched down; is no longer in conflict with 05
                    var tkof = false
                    val r34r = airport.runways["34R"] ?: continue
                    if (r34r.aircraftsOnAppr.size == 0) {
                        tkof = true
                    } else {
                        var index = 0
                        while (index < r34r.aircraftsOnAppr.size) {
                            val aircraft: Aircraft = r34r.aircraftsOnAppr.get(index)
                            if (aircraft.isOnGround) {
                                tkof = true
                            } else {
                                tkof = pixelToNm(distanceBetween(aircraft.x, aircraft.y, r34r.x, r34r.y)) >= 6
                                break
                            }
                            index++
                        }
                    }
                    if (tkof) {
                        runway = runway1
                        if (distance > 24.9) break
                        dist = distance
                    }
                } else if ("16L" == runway1.name && checkLandingTCTT23() && checkOppLanding(airport.runways["16R"]) && checkPreceding("05")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ((airport.takeoffRunways.containsKey("22") || airport.allowSimultDep()) && "16R" == runway1.name && checkLandingTCTT23() && checkOppLanding(airport.runways["16L"]) && checkPreceding("05")) {
                    //Use 16R only if departure volume is low, or if other departure runway in use is 22
                    runway = runway1
                    if (airport.takeoffRunways.containsKey("22") && distance > 24.9) break
                    dist = distance
                } else if (airport.allowSimultDep() && "22" == runway1.name && checkLandingTCTT16R()) {
                    //Use only 16R if departure volume is low
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Tokyo Narita  */
    private fun updateTCAA() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("16L" == runway1.name && checkOppLanding(airport.runways["16R"]) && (checkPreceding("16R") || airport.allowSimultDep())) {
                    //Prefer 16R if departure volume is low
                    runway = runway1
                    dist = distance
                } else if ("16R" == runway1.name && checkOppLanding(airport.runways["16L"]) && (checkPreceding("16L") || airport.allowSimultDep())) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("34L" == runway1.name && checkOppLanding(airport.runways["34R"]) && (checkPreceding("34R") || airport.allowSimultDep())) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("34R" == runway1.name && checkOppLanding(airport.runways["34L"]) && (checkPreceding("34L") || airport.allowSimultDep())) {
                    //Prefer 34L if departure volume is low
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Osaka Kansai  */
    private fun updateTCBB() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("06L" == runway1.name && checkPreceding("06R") && checkOppLanding(airport.runways["06R"])) {
                    runway = runway1
                    dist = distance
                } else if ("06R" == runway1.name && checkPreceding("06L") && checkOppLanding(airport.runways["06L"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("24L" == runway1.name && checkPreceding("24R") && checkOppLanding(airport.runways["24R"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("24R" == runway1.name && checkPreceding("24L") && checkOppLanding(airport.runways["24L"])) {
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Osaka Itami  */
    private fun updateTCOO() {
        val runway = airport.runways["32L"] ?: return
        if (!runway.isEmergencyClosed && runway.isTakeoff && checkPreceding("32L") && checkLanding(runway)) {
            updateRunway(runway)
        }
    }

    /** Checks takeoff status for Kobe  */
    private fun updateTCBE() {
        val runway = airport.runways["09"] ?: return
        if (!runway.isEmergencyClosed && runway.isTakeoff && checkPreceding("09") && checkLanding(runway)) {
            updateRunway(runway)
        }
    }

    /** Checks takeoff status for Hong Kong  */
    private fun updateTCHH() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("07L" == runway1.name && checkPreceding("07R") && checkOppLanding(airport.runways["07R"])) {
                    runway = runway1
                    dist = distance
                } else if ("07R" == runway1.name && checkPreceding("07L") && checkOppLanding(airport.runways["07L"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("25L" == runway1.name && checkPreceding("25R") && checkOppLanding(airport.runways["25R"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("25R" == runway1.name && checkPreceding("25L") && checkOppLanding(airport.runways["25L"])) {
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Macau  */
    private fun updateTCMC() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding("16") && checkPreceding("34") && checkLanding(runway1) && checkOppLanding(runway1) && distance > dist) {
                runway = runway1
                dist = distance
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Bangkok Don Mueang  */
    private fun updateTCBD() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && distance > dist) {
                if ("03R" == runway1.name && checkOppLanding(airport.runways["03L"])) {
                    runway = runway1
                    dist = distance
                } else if ("21L" == runway1.name && checkOppLanding(airport.runways["21R"])) {
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Bangkok Suvarnabhumi  */
    private fun updateTCBS() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("01L" == runway1.name && checkPreceding("01R") && checkOppLanding(airport.runways["01R"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("01R" == runway1.name && checkPreceding("01L") && checkOppLanding(airport.runways["01L"])) {
                    runway = runway1
                    dist = distance
                } else if ("19L" == runway1.name && checkPreceding("19R") && checkOppLanding(airport.runways["19R"])) {
                    runway = runway1
                    dist = distance
                } else if ("19R" == runway1.name && checkPreceding("19L") && checkOppLanding(airport.runways["19L"])) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Madrid Barajas  */
    private fun updateTCMD() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("36L" == runway1.name && checkOppLanding(airport.runways["36R"])) {
                    runway = runway1
                    dist = distance
                } else if (airport.allowSimultDep() && "36R" == runway1.name && checkOppLanding(airport.runways["36L"])) {
                    //Use only 36L if departure volume is low
                    runway = runway1
                    dist = distance
                } else if ("14L" == runway1.name && checkOppLanding(airport.runways["14R"])) {
                    runway = runway1
                    dist = distance
                } else if (airport.allowSimultDep() && "14R" == runway1.name && checkOppLanding(airport.runways["14L"])) {
                    //Use only 14L if departure volume is low
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Paris Charles de Gaulle  */
    private fun updateTCPG() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("26R" == runway1.name && checkOppLanding(airport.runways["26L"])) {
                    runway = runway1
                    dist = distance
                } else if (airport.allowSimultDep() && "27L" == runway1.name && checkOppLanding(airport.runways["27R"])) {
                    //Use only 26R if departure volume is low
                    runway = runway1
                    dist = distance
                } else if ("08L" == runway1.name && checkOppLanding(airport.runways["08R"])) {
                    runway = runway1
                    dist = distance
                } else if (airport.allowSimultDep() && "09R" == runway1.name && checkOppLanding(airport.runways["09L"])) {
                    //Use only 08L if departure volume is low
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Paris Orly  */
    private fun updateTCPO() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1) && checkPreceding(runway1.oppRwy.name) && (distance > dist || distance > 24.9)) {
                if ("07" == runway1.name && checkOppLanding(airport.runways["06"])) {
                    runway = runway1
                    dist = distance
                } else if ("24" == runway1.name && checkOppLanding(airport.runways["25"])) {
                    runway = runway1
                    dist = distance
                }
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Kai Tak (old Hong Kong airport)  */
    private fun updateTCHX() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftsOnAppr.size > 0) pixelToNm(distanceBetween(runway1.aircraftsOnAppr.first().x, runway1.aircraftsOnAppr.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding("13") && checkPreceding("31") && checkLanding(runway1) && checkOppLanding(runway1) && distance > dist) {
                runway = runway1
                dist = distance
            }
        }
        updateRunway(runway)
    }

    /** Checks whether airport has available runways for takeoff, updates hashMap and timer if available  */
    private fun updateRunway(runway: Runway?) {
        if (runway != null && radarScreen.tfcMode == RadarScreen.TfcMode.NORMAL) {
            val nextAcft = nextAircraft[runway.name] ?: return
            val callsign = nextAcft[0]
            radarScreen.newDeparture(callsign, nextAcft[1], airport, runway)
            prevAircraft[runway.name] = radarScreen.aircrafts[callsign]
            nextAircraft[runway.name] = null
            timers[runway.name] = 0f
        }
    }

    /** Check the previous departure aircraft  */
    private fun checkPreceding(runway: String): Boolean {
        val prevAcft = prevAircraft[runway] ?: return true
        val nextAcft = nextAircraft[runway] ?: return true
        var additionalTime: Float = 100 - 15 * (airport.landings - airport.airborne).toFloat() //Additional time between departures when arrivals are not much higher than departures
        additionalTime = MathUtils.clamp(additionalTime, 0f, 150f)
        return prevAircraft[runway] == null || timers[runway] ?: 0f > SeparationMatrix.getTakeoffSepTime(prevAcft.recat, AircraftType.getRecat(nextAcft[1])) + additionalTime
    }

    /** Check for any landing aircrafts  */
    private fun checkLanding(runway: Runway): Boolean {
        return if (runway.aircraftsOnAppr.size == 0) {
            //No aircraft on approach
            true
        } else {
            val aircraft: Aircraft = runway.aircraftsOnAppr.first()
            pixelToNm(distanceBetween(aircraft.x, aircraft.y, runway.x, runway.y)) >= 5 && !aircraft.isOnGround && runway.oppRwy.aircraftsOnAppr.size == 0
        }
    }

    /** Checks specific case for TCTT's runway 23, same as above function but will also return true if aircraft has landed  */
    private fun checkLandingTCTT23(): Boolean {
        if ("TCTT" != airport.icao) return false
        val runway: Runway = airport.runways["23"] ?: return false
        return if (runway.aircraftsOnAppr.size == 0) {
            //No aircraft on approach
            true
        } else {
            val aircraft: Aircraft = runway.aircraftsOnAppr.first()
            var aircraft1: Aircraft? = null
            if (runway.aircraftsOnAppr.size > 1) aircraft1 = runway.aircraftsOnAppr.get(1)
            if (runway.oppRwy.aircraftsOnAppr.size == 0) {
                //No planes landing opposite
                return if (pixelToNm(distanceBetween(aircraft.x, aircraft.y, runway.x, runway.y)) >= 5 && !aircraft.isOnGround) {
                    //If latest aircraft is more than 5 miles away and not landed yet
                    true
                } else {
                    //If first aircraft has touched down, 2nd aircraft is non-existent OR is more than 5 miles away
                    aircraft.isOnGround && (aircraft1 == null || !aircraft1.isOnGround && pixelToNm(distanceBetween(aircraft1.x, aircraft1.y, runway.x, runway.y)) >= 5)
                }
            }
            false
        }
    }

    /** Checks specific case for TCTT's runway 16R, same as above function but will also return true if aircraft has landed  */
    private fun checkLandingTCTT16R(): Boolean {
        if ("TCTT" != airport.icao) return false
        val runway: Runway = airport.runways["16R"] ?: return false
        return if (runway.aircraftsOnAppr.size == 0) {
            //No aircraft on approach
            true
        } else {
            val aircraft: Aircraft = runway.aircraftsOnAppr.first()
            var aircraft1: Aircraft? = null
            if (runway.aircraftsOnAppr.size > 1) aircraft1 = runway.aircraftsOnAppr.get(1)
            if (runway.oppRwy.aircraftsOnAppr.size == 0) {
                //No planes landing opposite
                return if (pixelToNm(distanceBetween(aircraft.x, aircraft.y, runway.x, runway.y)) >= 5 && !aircraft.isOnGround) {
                    //If latest aircraft is more than 5 miles away and not landed yet
                    true
                } else {
                    //If first aircraft has touched down, 2nd aircraft is non-existent OR is more than 5 miles away
                    aircraft.isOnGround && (aircraft1 == null || !aircraft1.isOnGround && pixelToNm(distanceBetween(aircraft1.x, aircraft1.y, runway.x, runway.y)) >= 5)
                }
            }
            false
        }
    }

    /** Check that no aircraft is landing on opposite runway  */
    private fun checkOppLanding(runway: Runway?): Boolean {
        if (runway == null) return true
        return runway.oppRwy.aircraftsOnAppr.size == 0
    }
}