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
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import com.bombbird.terminalcontrol.utilities.math.random.DepartureGenerator
import org.json.JSONObject
import kotlin.collections.HashMap

class TakeoffManager {
    private val airport: Airport
    val nextAircraft: HashMap<String, Array<String>?>
    val prevAircraft: HashMap<String, Aircraft?>
    val timers: HashMap<String, Float>
    private val radarScreen = TerminalControl.radarScreen!!

    //Stores aircraft generators
    private val generatorMap = HashMap<String, DepartureGenerator>()

    constructor(airport: Airport) {
        this.airport = airport
        nextAircraft = HashMap()
        prevAircraft = HashMap()
        timers = HashMap()
        for ((index, runway) in airport.runways.keys.withIndex()) {
            timers[runway] = 180f
            prevAircraft[runway] = null
            nextAircraft[runway] = null
            generateNewDeparture(runway, index)
        }
    }

    constructor(airport: Airport, save: JSONObject) {
        this.airport = airport
        nextAircraft = HashMap()
        prevAircraft = HashMap()
        timers = HashMap()
        var index = 0
        for (runway in airport.runways.values) {
            val info = save.getJSONObject("nextAircraft").getJSONArray(runway.name)
            if (info.length() == 2) {
                nextAircraft[runway.name] = arrayOf(info.getString(0), info.getString(1))
            } else {
                nextAircraft[runway.name] = null
                generateNewDeparture(runway.name, index)
                index++
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

    /** Checks the list of multi threaded generators, creates new arrival if done */
    private fun checkGenerators() {
        val toBeUpdated = com.badlogic.gdx.utils.Array<String>()
        val generatorIterator = generatorMap.iterator()
        while (generatorIterator.hasNext()) {
            val generator = generatorIterator.next()
            if (generator.value.done) {
                //If generator is done generating, copy info to local variables and create a new arrival from them
                generatorIterator.remove() //Remove the generator since it's no longer needed
                val aircraftInfo = generator.value.aircraftInfo ?: continue
                if (radarScreen.allAircraft.contains(aircraftInfo[0])) {
                    toBeUpdated.add(generator.key)
                    continue
                }

                nextAircraft[generator.key] = aircraftInfo
                radarScreen.allAircraft.add(aircraftInfo[0]) //Add to all aircraft list
            }
        }
        for ((index, rwy) in toBeUpdated.withIndex()) {
            generateNewDeparture(rwy, index + 10)
        }
    }

    /** Creates a new departure queued for takeoff */
    private fun generateNewDeparture(rwy: String, delay: Int) {
        val multiThreadGenerator = DepartureGenerator(radarScreen, airport, radarScreen.allAircraft, delay)
        generatorMap[rwy] = multiThreadGenerator
        Thread(multiThreadGenerator).start()
    }

    /** Update loop  */
    fun update() {
        //Update the timers
        for (rwy in timers.keys) {
            timers[rwy] = timers[rwy]?.plus(Gdx.graphics.deltaTime) ?: 0f
        }
        checkGenerators()
        if (airport.airborne - airport.landings < 5) {
            //Departure is checked and requested if takeoffs are less than 5 more than landings
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("05L" == runway1.name && checkPreceding("05R") && checkOppLanding("05R") && checkGoAround("05R") && checkAircraftLanded("05R")) {
                    runway = runway1
                    dist = distance
                } else if ("05R" == runway1.name && checkPreceding("05L") && checkOppLanding("05L") && checkGoAround("05L") && checkAircraftLanded("05L")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("23L" == runway1.name && checkPreceding("23R") && checkOppLanding("23R") && checkGoAround("23R") && checkAircraftLanded("23R")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("23R" == runway1.name && checkPreceding("23L") && checkOppLanding("23L") && checkGoAround("23L") && checkAircraftLanded("23L")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding("10") && checkPreceding("28") && checkLanding(runway1) && checkOppLanding(runway1.name) && checkGoAround("10") && distance > dist && !runway1.isStormInPath) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("02L" == runway1.name && checkPreceding("02C") && checkOppLanding("02C") && checkGoAround("02C")) {
                    runway = runway1
                    dist = distance
                } else if ("02C" == runway1.name && checkPreceding("02L") && checkOppLanding("02L") && checkGoAround("02L")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("20C" == runway1.name && checkPreceding("20R") && checkOppLanding("20R") && checkGoAround("20R")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("20R" == runway1.name && checkPreceding("20C") && checkOppLanding("20C") && checkGoAround("20C")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if (airport.allowSimultDep() && timers["05"] ?: 0f >= 60 && "34R" == runway1.name && checkOppLanding("04") && checkOppLanding("05") && checkGoAround("34L") && checkGoAround("22") && checkGoAround("23")) {
                    //Additional check for runway 05 departure - 60 seconds apart
                    //Don't use 34R if departure volume is low
                    runway = runway1
                    dist = distance
                } else if ((!airport.allowSimultDep() || timers["34R"] ?: 0f >= 60) && "05" == runway1.name && checkOppLanding("04") && checkPreceding("16L") && checkPreceding("16R") && checkGoAround("34R") && checkGoAround("22")) {
                    //Additional check for runway 34R departure - 60 seconds apart
                    //Additional check if aircraft landing on 34R has touched down; is no longer in conflict with 05
                    var tkof = false
                    val r34r = airport.runways["34R"] ?: continue
                    if (r34r.aircraftOnApp.size == 0) {
                        tkof = true
                    } else {
                        var index = 0
                        while (index < r34r.aircraftOnApp.size) {
                            val aircraft: Aircraft = r34r.aircraftOnApp.get(index)
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
                } else if ("16L" == runway1.name && checkLandingTCTT23() && checkOppLanding("16R") && checkPreceding("05") && checkGoAround("16R") && checkGoAround("23")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ((airport.takeoffRunways.containsKey("22") || airport.allowSimultDep()) && "16R" == runway1.name && checkLandingTCTT23() && checkOppLanding("16L") && checkPreceding("05") && checkGoAround("16L") && checkGoAround("23")) {
                    //Use 16R only if departure volume is low, or if other departure runway in use is 22
                    runway = runway1
                    if (airport.takeoffRunways.containsKey("22") && distance > 24.9) break
                    dist = distance
                } else if (airport.allowSimultDep() && "22" == runway1.name && checkLandingTCTT16R() && checkGoAround("16L") && checkGoAround("16R") && checkGoAround("23")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("16L" == runway1.name && checkOppLanding("16R") && (checkPreceding("16R") || airport.allowSimultDep()) && checkGoAround("16R")) {
                    //Prefer 16R if departure volume is low
                    runway = runway1
                    dist = distance
                } else if ("16R" == runway1.name && checkOppLanding("16L") && (checkPreceding("16L") || airport.allowSimultDep()) && checkGoAround("16L")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("34L" == runway1.name && checkOppLanding("34R") && (checkPreceding("34R") || airport.allowSimultDep()) && checkGoAround("34R")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("34R" == runway1.name && checkOppLanding("34L") && (checkPreceding("34L") || airport.allowSimultDep()) && checkGoAround("34L")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("06L" == runway1.name && checkPreceding("06R") && checkOppLanding("06R") && checkGoAround("06R") && checkAircraftLanded("06R")) {
                    runway = runway1
                    dist = distance
                } else if ("06R" == runway1.name && checkPreceding("06L") && checkOppLanding("06L") && checkGoAround("06L") && checkAircraftLanded("06L")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("24L" == runway1.name && checkPreceding("24R") && checkOppLanding("24R") && checkGoAround("24R") && checkAircraftLanded("24R")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("24R" == runway1.name && checkPreceding("24L") && checkOppLanding("24L") && checkGoAround("24L") && checkAircraftLanded("24L")) {
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
        if (!runway.isEmergencyClosed && runway.isTakeoff && checkPreceding("32L") && checkLanding(runway) && checkGoAround("32L") && !runway.isStormInPath) {
            updateRunway(runway)
        }
    }

    /** Checks takeoff status for Kobe  */
    private fun updateTCBE() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding("09") && checkPreceding("27") && checkLanding(runway1, if (runway1.name == "27") 20 else 5) && checkOppLanding(runway1.name) && checkGoAround("09") && distance > dist && !runway1.isStormInPath) {
                runway = runway1
                dist = distance
            }
        }
        updateRunway(runway)
    }

    /** Checks takeoff status for Hong Kong  */
    private fun updateTCHH() {
        var runway: Runway? = null
        var dist = -1f
        for (runway1 in airport.takeoffRunways.values) {
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("07L" == runway1.name && checkPreceding("07R") && checkOppLanding("07R") && checkGoAround("07R") && checkAircraftLanded("07R")) {
                    runway = runway1
                    dist = distance
                } else if ("07R" == runway1.name && checkPreceding("07L") && checkOppLanding("07L") && checkGoAround("07L") && checkAircraftLanded("07L")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("25L" == runway1.name && checkPreceding("25R") && checkOppLanding("25R") && checkGoAround("25R") && checkAircraftLanded("25R")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("25R" == runway1.name && checkPreceding("25L") && checkOppLanding("25L") && checkGoAround("25L") && checkAircraftLanded("25L")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding("16") && checkPreceding("34") && checkLanding(runway1) && checkOppLanding(runway1.name) && checkGoAround("34") && distance > dist && !runway1.isStormInPath) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && distance > dist && !runway1.isStormInPath) {
                if ("03R" == runway1.name && checkOppLanding("03L") && checkGoAround("03L")) {
                    runway = runway1
                    dist = distance
                } else if ("21L" == runway1.name && checkOppLanding("21R") && checkGoAround("21R")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("01L" == runway1.name && checkPreceding("01R") && checkOppLanding("01R") && checkGoAround("01R")  && checkAircraftLanded("01R")) {
                    runway = runway1
                    if (distance > 24.9) break
                    dist = distance
                } else if ("01R" == runway1.name && checkPreceding("01L") && checkOppLanding("01L") && checkGoAround("01L") && checkAircraftLanded("01L")) {
                    runway = runway1
                    dist = distance
                } else if ("19L" == runway1.name && checkPreceding("19R") && checkOppLanding("19R") && checkGoAround("19R") && checkAircraftLanded("19R")) {
                    runway = runway1
                    dist = distance
                } else if ("19R" == runway1.name && checkPreceding("19L") && checkOppLanding("19L") && checkGoAround("19L") && checkAircraftLanded("19L")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("36L" == runway1.name && checkOppLanding("36R") && checkGoAround("36R") && checkGoAround("32L") && checkGoAround("32R")) {
                    runway = runway1
                    dist = distance
                } else if (airport.allowSimultDep() && "36R" == runway1.name && checkOppLanding("36L") && checkGoAround("36L") && checkGoAround("32R")) {
                    //Use only 36L if departure volume is low
                    runway = runway1
                    dist = distance
                } else if ("14L" == runway1.name && checkOppLanding("14R") && checkGoAround("14R") && checkGoAround("18L")) {
                    runway = runway1
                    dist = distance
                } else if (airport.allowSimultDep() && "14R" == runway1.name && checkOppLanding("14L") && checkGoAround("14L") && checkGoAround("18L") && checkGoAround("18R")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("26R" == runway1.name && checkOppLanding("26L") && checkAircraftLanded("26L")) {
                    runway = runway1
                    dist = distance
                } else if (airport.allowSimultDep() && "27L" == runway1.name && checkOppLanding("27R") && checkAircraftLanded("27R")) {
                    //Use only 26R if departure volume is low
                    runway = runway1
                    dist = distance
                } else if ("08L" == runway1.name && checkOppLanding("08R") && checkAircraftLanded("08R")) {
                    runway = runway1
                    dist = distance
                } else if (airport.allowSimultDep() && "09R" == runway1.name && checkOppLanding("09L") && checkAircraftLanded("09L")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding(runway1.name) && checkLanding(runway1) && checkOppLanding(runway1.name) && checkPreceding(runway1.oppRwy.name) && checkGoAround(runway1.name) && (distance > dist || distance > 24.9) && !runway1.isStormInPath) {
                if ("07" == runway1.name && checkOppLanding("06") && checkGoAround("06")) {
                    runway = runway1
                    dist = distance
                } else if ("24" == runway1.name && checkOppLanding("25") && checkGoAround("25")) {
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
            val distance: Float = if (runway1.aircraftOnApp.size > 0) pixelToNm(distanceBetween(runway1.aircraftOnApp.first().x, runway1.aircraftOnApp.first().y, runway1.x, runway1.y)) else 25f
            if (!runway1.isEmergencyClosed && checkPreceding("13") && checkPreceding("31") && checkLanding(runway1) && checkOppLanding(runway1.name) && checkGoAround("13") && distance > dist && !runway1.isStormInPath) {
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
            generateNewDeparture(runway.name, 0)
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

    /** Check for any landing aircraft */
    private fun checkLanding(runway: Runway, dist: Int = 5): Boolean {
        return if (runway.aircraftOnApp.size == 0) {
            //No aircraft on approach
            true
        } else {
            val aircraft: Aircraft = runway.aircraftOnApp.first()
            pixelToNm(distanceBetween(aircraft.x, aircraft.y, runway.x, runway.y)) >= dist && !aircraft.isOnGround && runway.oppRwy.aircraftOnApp.size == 0
        }
    }

    /** Checks specific case for TCTT's runway 23, same as above function but will also return true if aircraft has landed  */
    private fun checkLandingTCTT23(): Boolean {
        if ("TCTT" != airport.icao) return false
        val runway: Runway = airport.runways["23"] ?: return false
        return checkLanding(runway) && checkAircraftLanded("23", 5f)
    }

    /** Checks specific case for TCTT's runway 16R, same as above function but will also return true if aircraft has landed  */
    private fun checkLandingTCTT16R(): Boolean {
        if ("TCTT" != airport.icao) return false
        val runway: Runway = airport.runways["16R"] ?: return false
        return checkLanding(runway) && checkAircraftLanded("16R", 5f)
    }

    /** Check that no aircraft is landing on opposite runway  */
    private fun checkOppLanding(rwy: String): Boolean {
        val runway = airport.runways[rwy] ?: return true
        return runway.oppRwy.aircraftOnApp.size == 0
    }

    /** Check that there are no recent go arounds, or the previous go around is sufficiently far from runway */
    private fun checkGoAround(rwy: String): Boolean {
        val runway = airport.runways[rwy] ?: return true
        val oppRwy = runway.oppRwy
        val rwy1 = runway.goAround?.let { it.goAroundTime < 60 || !it.isGoAround || distanceBetween(it.x, it.y, runway.x, runway.y) > nmToPixel(4f) } ?: true
        val rwy2 = oppRwy.goAround?.let { it.goAroundTime < 60 || !it.isGoAround || distanceBetween(it.x, it.y, runway.x, runway.y) > nmToPixel(4f) } ?: true
        return rwy1 && rwy2
    }

    /** Checks that 1st aircraft has landed on runway, and 2nd aircraft (if any) is more than 5 miles away */
    private fun checkAircraftLanded(rwy: String, dist: Float = 3f): Boolean {
        val runway = airport.runways[rwy] ?: return true
        if (runway.aircraftOnApp.size == 0) return true
        val aircraft1 = runway.aircraftOnApp.first()
        if (pixelToNm(distanceBetween(aircraft1.x, aircraft1.y, runway.x, runway.y)) < dist && !aircraft1.isOnGround) return false //If aircraft is <5 miles and has not landed, return false
        if (runway.aircraftOnApp.size == 1) return true //If only 1 aircraft which is more than 5 miles, return true
        //More than 1 aircraft - check 2nd aircraft as well
        val aircraft2 = runway.aircraftOnApp[1]
        return pixelToNm(distanceBetween(aircraft2.x, aircraft2.y, runway.x, runway.y)) >= dist && !aircraft2.isOnGround //Return true if 2nd aircraft is >5 miles and has not landed, else false
    }
}