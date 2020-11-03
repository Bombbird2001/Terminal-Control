package com.bombbird.terminalcontrol.utilities.saving

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Base64Coder
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.Queue
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.conflicts
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.emergenciesLanded
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.planesLanded
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.wakeConflictTime
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.procedures.holding.HoldingPoints
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.Revision
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.getExtDir
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.HashMap

object GameSaver {
    private lateinit var backupWpts: HashMap<String, IntArray>
    private lateinit var backupHoldingPts: HashMap<String, HashMap<String, HoldingPoints>>

    /** Saves current game state  */

    fun saveGame() {
        val radarScreen = TerminalControl.radarScreen ?: return
        backupWpts = HashMap()
        backupHoldingPts = HashMap()
        for (icao in radarScreen.airports.keys) {
            backupHoldingPts[icao] = HashMap()
        }
        val jsonObject = JSONObject()
        jsonObject.put("aircrafts", saveAircraft(radarScreen))
        jsonObject.put("airports", saveAirports(radarScreen))
        jsonObject.put("saveId", radarScreen.saveId)
        jsonObject.put("MAIN_NAME", radarScreen.mainName)
        jsonObject.put("AIRAC", radarScreen.airac)
        jsonObject.put("score", radarScreen.getScore())
        jsonObject.put("highScore", radarScreen.highScore)
        jsonObject.put("planesToControl", radarScreen.planesToControl.toDouble())
        jsonObject.put("arrivals", radarScreen.arrivals)
        jsonObject.put("separationIncidents", radarScreen.separationIncidents)
        jsonObject.put("wakeInfringeTime", radarScreen.wakeInfringeTime.toDouble())
        jsonObject.put("emergenciesLanded", radarScreen.emergenciesLanded)
        jsonObject.put("playTime", radarScreen.playTime.toDouble())
        jsonObject.put("spawnTimer", radarScreen.spawnTimer.toDouble())
        jsonObject.put("previousOffset", radarScreen.previousOffset.toDouble())
        jsonObject.put("information", radarScreen.information.toInt())
        jsonObject.put("radarTime", radarScreen.radarTime.toDouble())
        jsonObject.put("trailTime", radarScreen.trailTime.toDouble())
        jsonObject.put("trajectoryLine", radarScreen.trajectoryLine)
        jsonObject.put("pastTrajTime", radarScreen.pastTrajTime)
        jsonObject.put("radarSweep", radarScreen.radarSweepDelay.toDouble())
        jsonObject.put("advTraj", radarScreen.advTraj)
        jsonObject.put("areaWarning", radarScreen.areaWarning)
        jsonObject.put("collisionWarning", radarScreen.collisionWarning)
        jsonObject.put("showMva", radarScreen.showMva)
        jsonObject.put("showIlsDash", radarScreen.showIlsDash)
        jsonObject.put("compactData", radarScreen.compactData)
        jsonObject.put("showUncontrolled", radarScreen.showUncontrolled)
        jsonObject.put("alwaysShowBordersBackground", radarScreen.alwaysShowBordersBackground)
        jsonObject.put("rangeCircleDist", radarScreen.rangeCircleDist)
        jsonObject.put("lineSpacingValue", radarScreen.lineSpacingValue)
        jsonObject.put("colourStyle", radarScreen.colourStyle)
        jsonObject.put("liveWeather", radarScreen.weatherSel)
        jsonObject.put("sounds", radarScreen.soundSel)
        jsonObject.put("realisticMetar", radarScreen.realisticMetar)
        jsonObject.put("emerChance", radarScreen.emerChance.toString())
        jsonObject.put("tfcMode", radarScreen.tfcMode.toString())
        jsonObject.put("allowNight", radarScreen.allowNight)
        jsonObject.put("nightStart", radarScreen.nightStart)
        jsonObject.put("nightEnd", radarScreen.nightEnd)
        jsonObject.put("commBox", getUtilityBoxSave(radarScreen))
        jsonObject.put("metar", radarScreen.metar.metarObject)
        jsonObject.put("lastNumber", radarScreen.separationChecker.lastNumber)
        jsonObject.put("sepTime", radarScreen.separationChecker.time.toDouble())
        jsonObject.put("wakeManager", radarScreen.wakeManager.save)
        jsonObject.put("trafficMode", radarScreen.trafficMode)
        jsonObject.put("maxPlanes", radarScreen.maxPlanes)
        jsonObject.put("flowRate", radarScreen.flowRate)
        jsonObject.put("revision", Revision.CURRENT_REVISION)
        val jsonArray = JSONArray()
        for (aircraft in radarScreen.allAircraft) {
            jsonArray.put(aircraft)
        }
        jsonObject.put("allAircraft", jsonArray)
        var aircraftsLanded = 0
        var aircraftsAirborne = 0
        for (airport in radarScreen.airports.values) {
            aircraftsLanded += airport.landings
            aircraftsAirborne += airport.airborne
        }
        jsonObject.put("landings", aircraftsLanded)
        jsonObject.put("airborne", aircraftsAirborne)
        val backup = JSONObject()
        for ((key, value) in backupWpts) {
            val wpt = JSONObject()
            wpt.put("x", value[0])
            wpt.put("y", value[1])
            backup.put(key, wpt)
        }
        jsonObject.put("backupWpts", backup)
        val handle = getExtDir("saves/" + radarScreen.saveId + ".json")
        if (handle != null) {
            val encode = Base64Coder.encodeString(jsonObject.toString())
            try {
                handle.writeString(encode, false)
                saveID(radarScreen.saveId)
            } catch (e: GdxRuntimeException) {
                TerminalControl.toastManager.saveFail(e)
                //ErrorHandler.sendSaveErrorNoThrow(e, encode);
            }
        }
    }

    /** Saves all aircraft information  */
    private fun saveAircraft(radarScreen: RadarScreen): JSONArray {
        val aircrafts = JSONArray()
        for (aircraft in radarScreen.aircrafts.values) {
            val aircraftInfo = JSONObject()
            var type: String
            aircraftInfo.put("actionRequired", aircraft.isActionRequired) //Whether aircraft label is flashing
            aircraftInfo.put("dataTagMin", aircraft.dataTag.isMinimized) //Whether aircraft label is minimized
            aircraftInfo.put("fuelEmergency", aircraft.isFuelEmergency) //Whether aircraft is having a fuel emergency
            aircraftInfo.put("airport", aircraft.airport.icao) //Airport
            aircraftInfo.put("runway", aircraft.runway?.name ?: JSONObject.NULL) //Runway
            aircraftInfo.put("onGround", aircraft.isOnGround) //Whether it is on the ground
            aircraftInfo.put("tkOfLdg", aircraft.isTkOfLdg) //Whether it is taking off/landing
            aircraftInfo.put("callsign", aircraft.callsign) //Callsign
            aircraftInfo.put("icaoType", aircraft.icaoType) //ICAO aircraft type
            aircraftInfo.put("wakeCat", aircraft.wakeCat.toString()) //Wake turbulence category
            aircraftInfo.put("recat", aircraft.recat.toInt()) //Recat code
            aircraftInfo.put("wakeInfringe", aircraft.isWakeInfringe) //Whether wake separation too low
            aircraftInfo.put("wakeTolerance", aircraft.wakeTolerance.toDouble()) //Wake tolerance of aircraft
            aircraftInfo.put("v2", aircraft.v2) //V2 speed
            aircraftInfo.put("typClimb", aircraft.typClimb) //Typical climb rate
            aircraftInfo.put("maxClimb", aircraft.maxClimb) //Max climb rate
            aircraftInfo.put("typDes", aircraft.typDes) //Typical descent rate
            aircraftInfo.put("maxDes", aircraft.maxDes) //Max descent rate
            aircraftInfo.put("apchSpd", aircraft.apchSpd) //Approach speed
            aircraftInfo.put("controlState", aircraft.controlState.toString()) //Control state
            aircraftInfo.put("navState", getNavState(aircraft)) //Nav state
            aircraftInfo.put("goAround", aircraft.isGoAround) //Go around
            aircraftInfo.put("goAroundWindow", aircraft.isGoAroundWindow) //Go around window is active
            aircraftInfo.put("goAroundTime", aircraft.goAroundTime.toDouble()) //Go around window timing
            aircraftInfo.put("conflict", aircraft.isConflict) //Aircraft is in conflict
            aircraftInfo.put("warning", aircraft.isWarning) //Aircraft is in warning state
            aircraftInfo.put("terrainConflict", aircraft.isTerrainConflict) //Aircraft in conflict with terrain
            aircraftInfo.put("emergency", getEmergency(aircraft)) //Emergency status
            aircraftInfo.put("request", aircraft.request) //Type of request aircraft has
            aircraftInfo.put("requested", aircraft.isRequested) //Whether aircraft has requested it
            aircraftInfo.put("requestAlt", aircraft.requestAlt) //Altitude at which request triggers
            aircraftInfo.put("x", aircraft.x.toDouble()) //x coordinate
            aircraftInfo.put("y", aircraft.y.toDouble()) //y coordinate
            aircraftInfo.put("prevDistTravelled", aircraft.prevDistTravelled.toDouble()) //Distance travelled previously (for wake points)
            aircraftInfo.put("heading", aircraft.heading) //Heading
            aircraftInfo.put("targetHeading", aircraft.targetHeading) //Target heading
            aircraftInfo.put("clearedHeading", aircraft.clearedHeading) //Cleared heading
            aircraftInfo.put("angularVelocity", aircraft.angularVelocity) //Angular velocity
            aircraftInfo.put("track", aircraft.track) //Track
            aircraftInfo.put("sidStarIndex", aircraft.sidStarIndex) //Sid star index
            aircraftInfo.put("direct", aircraft.direct?.name ?: JSONObject.NULL) //Direct waypoint
            aircraftInfo.put("afterWaypoint", aircraft.afterWaypoint?.name ?: JSONObject.NULL) //After direct waypoint
            aircraftInfo.put("afterWptHdg", aircraft.afterWptHdg) //After waypoint heading
            aircraftInfo.put("ils", aircraft.ils?.name ?: JSONObject.NULL) //ILS
            aircraftInfo.put("locCap", aircraft.isLocCap) //Localizer captured
            aircraftInfo.put("holdWpt", aircraft.holdWpt?.name ?: JSONObject.NULL) //Holding point
            aircraftInfo.put("holdingType", aircraft.holdingType) //Entry pattern
            aircraftInfo.put("holding", aircraft.isHolding) //Aircraft holding
            aircraftInfo.put("init", aircraft.isInit) //Additional holding info
            aircraftInfo.put("type1leg", aircraft.isType1leg) //Additional holding info

            //More additional holding info
            aircraft.holdTargetPt?.let {
                val info0 = JSONArray()
                val info1 = JSONArray()
                for ((index, info) in it.withIndex()) {
                    val point = JSONArray()
                    for (info2 in info) {
                        point.put(info2.toDouble())
                    }
                    info0.put(point)
                    info1.put(it[index])
                }
                aircraftInfo.put("holdTargetPt", info0)
                aircraftInfo.put("holdTargetPtSelected", info1)
            } ?: run {
                aircraftInfo.put("holdTargetPt", JSONObject.NULL)
                aircraftInfo.put("holdTargetPtSelected", JSONObject.NULL)
            }

            //Trail dots
            val trail = JSONArray()
            for (image in aircraft.dataTag.trailDots) {
                val point = JSONArray()
                point.put((image.x + image.width / 2).toDouble())
                point.put((image.y + image.height / 2).toDouble())
                trail.put(point)
            }
            aircraftInfo.put("trailDots", trail)
            aircraftInfo.put("prevAlt", aircraft.prevAlt.toDouble()) //Previous altitude
            aircraftInfo.put("altitude", aircraft.altitude.toDouble()) //Altitude
            aircraftInfo.put("clearedAltitude", aircraft.clearedAltitude) //Cleared altitude
            aircraftInfo.put("targetAltitude", aircraft.targetAltitude) //Target altitude
            aircraftInfo.put("verticalSpeed", aircraft.verticalSpeed.toDouble()) //Vertical speed
            aircraftInfo.put("expedite", aircraft.isExpedite) //Expedite
            aircraftInfo.put("expediteTime", aircraft.expediteTime.toDouble()) //Expedite time
            aircraftInfo.put("lowestAlt", aircraft.lowestAlt) //Lowest altitude
            aircraftInfo.put("highestAlt", aircraft.highestAlt) //Highest altitude
            aircraftInfo.put("gsCap", aircraft.isGsCap) //Glide slope captured
            aircraftInfo.put("ias", aircraft.ias.toDouble()) //Airspeed
            aircraftInfo.put("tas", aircraft.tas.toDouble()) //True airspeed
            aircraftInfo.put("gs", aircraft.gs.toDouble()) //Ground speed

            //Delta position
            val deltaPosition = JSONArray()
            deltaPosition.put(aircraft.deltaPosition.x.toDouble())
            deltaPosition.put(aircraft.deltaPosition.y.toDouble())
            aircraftInfo.put("deltaPosition", deltaPosition)
            aircraftInfo.put("clearedIas", aircraft.clearedIas) //Cleared speed
            aircraftInfo.put("deltaIas", aircraft.deltaIas.toDouble()) //Rate of change of speed
            aircraftInfo.put("climbSpd", aircraft.climbSpd) //Climb speed
            aircraftInfo.put("radarX", aircraft.radarX.toDouble()) //Radar x position
            aircraftInfo.put("radarY", aircraft.radarY.toDouble()) //Radar y position
            aircraftInfo.put("radarHdg", aircraft.radarHdg) //Radar heading
            aircraftInfo.put("radarTrack", aircraft.radarTrack) //Radar track
            aircraftInfo.put("radarGs", aircraft.radarGs.toDouble()) //Radar ground speed
            aircraftInfo.put("radarAlt", aircraft.radarAlt.toDouble()) //Radar altitude
            aircraftInfo.put("radarVs", aircraft.radarVs.toDouble()) //Radar vertical speed
            aircraftInfo.put("voice", aircraft.voice) //Text to speech voice
            val labelPos = JSONArray() //Aircraft label position
            labelPos.put(aircraft.dataTag.labelPosition[0].toDouble())
            labelPos.put(aircraft.dataTag.labelPosition[1].toDouble())
            aircraftInfo.put("labelPos", labelPos)

            when (aircraft) {
                is Arrival -> {
                    type = "Arrival"
                    aircraftInfo.put("star", aircraft.sidStar.name)
                    aircraft.nonPrecAlts?.let {
                        //Non prec alts for arrivals
                        val nonPrecAlts = JSONArray()
                        for (info in it) {
                            val data = JSONArray()
                            data.put(info[0].toDouble())
                            data.put(info[1].toDouble())
                            nonPrecAlts.put(data)
                        }
                        aircraftInfo.put("nonPrecAlts", nonPrecAlts)
                    } ?: run {
                        aircraftInfo.put("nonPrecAlts", JSONObject.NULL)
                    }
                    aircraftInfo.put("lowerSpdSet", aircraft.isLowerSpdSet)
                    aircraftInfo.put("ilsSpdSet", aircraft.isIlsSpdSet)
                    aircraftInfo.put("finalSpdSet", aircraft.isFinalSpdSet)
                    aircraftInfo.put("willGoAround", aircraft.isWillGoAround)
                    aircraftInfo.put("goAroundAlt", aircraft.goAroundAlt)
                    aircraftInfo.put("goAroundSet", aircraft.isGoAroundSet)
                    aircraftInfo.put("contactAlt", aircraft.contactAlt)
                    aircraftInfo.put("fuel", aircraft.fuel.toDouble())
                    aircraftInfo.put("requestPriority", aircraft.isRequestPriority)
                    aircraftInfo.put("declareEmergency", aircraft.isDeclareEmergency)
                    aircraftInfo.put("divert", aircraft.isDivert)
                }
                is Departure -> {
                    type = "Departure"
                    aircraftInfo.put("sid", aircraft.sidStar.name)
                    aircraftInfo.put("outboundHdg", aircraft.outboundHdg)
                    aircraftInfo.put("contactAlt", aircraft.contactAlt)
                    aircraftInfo.put("handOverAlt", aircraft.handoverAlt)
                    aircraftInfo.put("v2set", aircraft.isV2set)
                    aircraftInfo.put("accel", aircraft.isAccel)
                    aircraftInfo.put("sidSet", aircraft.isSidSet)
                    aircraftInfo.put("contacted", aircraft.isContacted)
                    aircraftInfo.put("handedOver", aircraft.isHandedOver)
                    aircraftInfo.put("cruiseAltTime", aircraft.cruiseAltTime.toDouble())
                    aircraftInfo.put("cruiseAlt", aircraft.cruiseAlt)
                    aircraftInfo.put("higherSpdSet", aircraft.isHigherSpdSet)
                    aircraftInfo.put("cruiseSpdSet", aircraft.isCruiseSpdSet)
                    aircraftInfo.put("askedForHigher", aircraft.isAskedForHigher)
                    aircraftInfo.put("altitudeMaintainTime", aircraft.altitudeMaintainTime.toDouble())
                }
                else -> {
                    type = "Type error"
                    Gdx.app.log("Save error", "Invalid aircraft instance type")
                }
            }
            aircraftInfo.put("TYPE", type)

            //Save aircraft route
            val route = JSONObject()
            val wpts = JSONArray()
            val restrictions = JSONArray()
            val flyOver = JSONArray()
            for (i in 0 until aircraft.route.waypoints.size) {
                val wptName: String = aircraft.route.waypoints.get(i).name
                //Add into used holding waypoints if applicable
                if (aircraft is Arrival && aircraft.airport.holdingPoints.containsKey(wptName)) {
                    //Must be arrival, and holdingPoints map must contain the wpt
                    aircraft.airport.holdingPoints[wptName]?.let { backupHoldingPts[aircraft.airport.icao]?.set(wptName, it) }
                }
                //Add all used waypoints into the backup save hashMap
                val wpt = radarScreen.waypoints[wptName]
                wpt?.let {
                    backupWpts[wptName] = intArrayOf(wpt.posX, wpt.posY)
                    wpts.put(wptName)
                    val data: IntArray = aircraft.route.restrictions.get(i)
                    val stuff = data[0].toString() + " " + data[1] + " " + data[2]
                    restrictions.put(stuff)
                    flyOver.put(aircraft.route.flyOver.get(i))
                }
            }
            route.put("waypoints", wpts)
            route.put("restrictions", restrictions)
            route.put("flyOver", flyOver)
            route.put("heading", aircraft.route.heading)
            route.put("name", aircraft.route.name)
            aircraftInfo.put("route", route)
            aircrafts.put(aircraftInfo)
        }
        return aircrafts
    }

    /** Saves all navState information for input aircraft  */
    private fun getNavState(aircraft: Aircraft): JSONObject {
        val navState = JSONObject()

        //Add all allowed lat modes
        val latModes = JSONArray()
        for (string in aircraft.navState.latModes) {
            latModes.put(string)
        }
        navState.put("latModes", latModes)

        //Add all allowed alt modes
        val altModes = JSONArray()
        for (string in aircraft.navState.altModes) {
            altModes.put(string)
        }
        navState.put("altModes", altModes)

        //Add all allowed spd modes
        val spdModes = JSONArray()
        for (string in aircraft.navState.spdModes) {
            spdModes.put(string)
        }
        navState.put("spdModes", spdModes)

        //Add transmit timings
        val timeQueue = JSONArray()
        for (time in aircraft.navState.timeQueueArray) {
            timeQueue.put(time.toDouble())
        }
        navState.put("timeQueue", timeQueue)

        //Add display lat mode queue
        navState.put("dispLatMode", getIntArray(aircraft.navState.dispLatMode))

        //Add display alt mode queue
        navState.put("dispAltMode", getIntArray(aircraft.navState.dispAltMode))

        //Add display spd mode queue
        navState.put("dispSpdMode", getIntArray(aircraft.navState.dispSpdMode))

        //Add cleared heading
        navState.put("clearedHdg", getIntArray(aircraft.navState.clearedHdg))

        //Add cleared direct
        navState.put("clearedDirect", getWptArray(aircraft.navState.clearedDirect))

        //Add cleared after waypoint
        navState.put("clearedAftWpt", getWptArray(aircraft.navState.clearedAftWpt))

        //Add cleared after waypoint heading
        navState.put("clearedAftWptHdg", getIntArray(aircraft.navState.clearedAftWptHdg))

        //Add cleared hold waypoint
        navState.put("clearedHold", getWptArray(aircraft.navState.clearedHold))

        //Add cleared ILS
        val clearedIls = JSONArray()
        for (ils in aircraft.navState.clearedIls) {
            clearedIls.put(ils?.name ?: JSONObject.NULL)
        }
        navState.put("clearedIls", clearedIls)

        //Add cleared new STAR
        navState.put("clearedNewStar", getStringArray(aircraft.navState.clearedNewStar))

        //Add cleared turn direction
        navState.put("clearedTurnDir", getIntArray(aircraft.navState.clearedTurnDir))

        //Add cleared altitude
        navState.put("clearedAlt", getIntArray(aircraft.navState.clearedAlt))

        //Add cleared expedite
        navState.put("clearedExpedite", getBoolArray(aircraft.navState.clearedExpedite))

        //Add cleared speed
        navState.put("clearedSpd", getIntArray(aircraft.navState.clearedSpd))

        //Add go around state
        navState.put("goAround", getBoolArray(aircraft.navState.goAround))

        //Save length
        navState.put("length", aircraft.navState.length)
        return navState
    }

    /** Gets emergency status for input aircraft  */
    private fun getEmergency(aircraft: Aircraft): JSONObject {
        val emer = JSONObject()
        emer.put("emergency", aircraft.emergency.isEmergency)
        emer.put("active", aircraft.emergency.isActive)
        emer.put("type", aircraft.emergency.type.name)
        emer.put("timeRequired", aircraft.emergency.timeRequired.toDouble())
        emer.put("checklistsSaid", aircraft.emergency.isChecklistsSaid)
        emer.put("readyForDump", aircraft.emergency.isReadyForDump)
        emer.put("fuelDumpLag", aircraft.emergency.fuelDumpLag.toDouble())
        emer.put("dumpingFuel", aircraft.emergency.isDumpingFuel)
        emer.put("fuelDumpRequired", aircraft.emergency.isFuelDumpRequired)
        emer.put("fuelDumpTime", aircraft.emergency.fuelDumpTime.toDouble())
        emer.put("remainingTimeSaid", aircraft.emergency.isRemainingTimeSaid)
        emer.put("sayRemainingTime", aircraft.emergency.sayRemainingTime)
        emer.put("readyForApproach", aircraft.emergency.isReadyForApproach)
        emer.put("stayOnRwy", aircraft.emergency.isStayOnRwy)
        emer.put("stayOnRwyTime", aircraft.emergency.stayOnRwyTime.toDouble())
        emer.put("emergencyStartAlt", aircraft.emergency.emergencyStartAlt)
        return emer
    }

    /** Returns a JSONArray given an input queue of strings  */
    private fun getStringArray(queue: Queue<String?>): JSONArray {
        val array = JSONArray()
        for (string in queue) {
            array.put(string)
        }
        return array
    }

    /** Returns a JSONArray given an input queue of integers  */
    private fun getIntArray(queue: Queue<Int>): JSONArray {
        val array = JSONArray()
        for (integer in queue) {
            array.put(integer)
        }
        return array
    }

    /** Returns a JSONArray given an input queue of waypoints  */
    private fun getWptArray(queue: Queue<Waypoint?>): JSONArray {
        val array = JSONArray()
        for (waypoint in queue) {
            array.put(waypoint?.name ?: JSONObject.NULL)
        }
        return array
    }

    /** Returns a JSONArray given an input queue of booleans  */
    private fun getBoolArray(queue: Queue<Boolean>): JSONArray {
        val array = JSONArray()
        for (bool in queue) {
            array.put(bool)
        }
        return array
    }

    /** Saves current information for all airports  */
    private fun saveAirports(radarScreen: RadarScreen): JSONArray {
        val airports = JSONArray()
        for (airport in radarScreen.airports.values) {
            val airportInfo = JSONObject()

            //Landing runways
            val landingRunways = JSONArray()
            for (runway in airport.landingRunways.values) {
                landingRunways.put(runway.name)
            }
            airportInfo.put("landingRunways", landingRunways)

            //Takeoff runways
            val takeoffRunways = JSONArray()
            for (runway in airport.takeoffRunways.values) {
                takeoffRunways.put(runway.name)
            }
            airportInfo.put("takeoffRunways", takeoffRunways)

            //Runway manager night mode
            airportInfo.put("night", airport.night)
            airportInfo.put("icao", airport.icao) //ICAO code of airport
            airportInfo.put("elevation", airport.elevation) //Elevation of airport
            airportInfo.put("aircraftRatio", airport.aircraftRatio) //Ratio of flights to the airport
            airportInfo.put("takeoffManager", getTakeoffManager(airport)) //Takeoff manager

            //Queue for runway landings
            val runwayQueues = JSONObject()
            for (runway in airport.runways.values) {
                val queue = JSONArray()
                for (aircraft in runway.aircraftsOnAppr) {
                    queue.put(aircraft.callsign)
                }
                runwayQueues.put(runway.name, queue)
            }
            airportInfo.put("runwayQueues", runwayQueues)

            //Closed for emergency?
            val runwayClosed = JSONObject()
            for (runway in airport.runways.values) {
                runwayClosed.put(runway.name, runway.isEmergencyClosed)
            }
            airportInfo.put("emergencyClosed", runwayClosed)
            airportInfo.put("landings", airport.landings) //Landings
            airportInfo.put("airborne", airport.airborne) //Airborne
            airportInfo.put("congestion", airport.isCongested) //Congestion
            airportInfo.put("pendingRwyChange", airport.isPendingRwyChange) //Whether is pending runway change
            airportInfo.put("rwyChangeTimer", airport.rwyChangeTimer.toDouble()) //Runway change timer
            airportInfo.put("closed", airport.isClosed) //Whether airport is closed

            //STAR timers
            val starTimers = JSONObject(RandomSTAR.time[airport.icao])
            airportInfo.put("starTimers", starTimers)

            //Backup holding points
            val backupPts = JSONObject()
            backupHoldingPts[airport.icao]?.let {
                for ((key, wpt) in it) {
                    val pt = JSONObject()
                    pt.put("minAlt", wpt.altRestrictions[0])
                    pt.put("maxAlt", wpt.altRestrictions[1])
                    pt.put("maxSpd", wpt.maxSpd)
                    pt.put("left", wpt.isLeft)
                    pt.put("inboundHdg", wpt.inboundHdg)
                    pt.put("legDist", wpt.legDist.toDouble())
                    backupPts.put(key, pt)
                }
            }
            airportInfo.put("backupPts", backupPts)
            airports.put(airportInfo)
        }
        return airports
    }

    /** Returns the airport's takeoff manager as JSONObject  */
    private fun getTakeoffManager(airport: Airport): JSONObject {
        val takeoffManager = JSONObject()

        //Save nextAircraft for each runway
        val nextAircraft = JSONObject()
        for (rwy in airport.takeoffManager.nextAircraft.keys) {
            val aircraftInfo = JSONArray()
            airport.takeoffManager.nextAircraft[rwy]?.let {
                for (s in it) {
                    aircraftInfo.put(s)
                }
            }
            nextAircraft.put(rwy, aircraftInfo)
        }
        takeoffManager.put("nextAircraft", nextAircraft)

        //Save prevAircraft for each runway
        val prevAircraft = JSONObject()
        for (rwy in airport.takeoffManager.prevAircraft.keys) {
            prevAircraft.put(rwy, airport.takeoffManager.prevAircraft[rwy]?.callsign ?: JSONObject.NULL)
        }
        takeoffManager.put("prevAircraft", prevAircraft)

        //Save timers for each runway
        val timers = JSONObject()
        for (rwy in airport.takeoffManager.timers.keys) {
            timers.put(rwy, airport.takeoffManager.timers[rwy]?.toDouble() ?: 0.0)
        }
        takeoffManager.put("timers", timers)
        return takeoffManager
    }

    /** Saves the input ID into saves.saves file  */
    private fun saveID(id: Int) {
        val handle: FileHandle
        when (Gdx.app.type) {
            Application.ApplicationType.Android -> handle = Gdx.files.local("saves/saves.saves")
            Application.ApplicationType.Desktop -> handle = Gdx.files.external(FileLoader.mainDir + "/saves/saves.saves")
            else -> {
                handle = Gdx.files.local("saves/saves.saves")
                Gdx.app.log("File load error", "Unknown platform " + Gdx.app.type.name + " used!")
            }
        }
        var ids: Array<String>? = null
        if (handle.exists()) {
            ids = Array(handle.readString().split(",".toRegex()).toTypedArray())
        }
        if (ids != null) {
            if (!ids.contains(id.toString(), false)) {
                //If does not contain, add to array
                ids.add(id.toString())
            } else {
                return  //If contains ID, no action required
            }
            ids.removeValue("", false) //Remove a random "" that is loaded into the array
        } else {
            ids = Array()
            ids.add(id.toString())
        }
        handle.writeString(ids.toString(","), false)
    }

    /** Gets the save array for utility box */
    private fun getUtilityBoxSave(radarScreen: RadarScreen): JSONArray {
        val jsonArray = JSONArray()
        for (label in radarScreen.utilityBox.commsManager.labels) {
            val jsonObject = JSONObject()
            jsonObject.put("message", label.text)
            jsonObject.put("color", label.style.fontColor.toString())
            jsonArray.put(jsonObject)
        }
        return jsonArray
    }

    /** Deletes a save given input game ID, returns true if success, false if fail  */

    fun deleteSave(id: Int): Boolean {
        var handle = getExtDir("saves/saves.saves")
        if (handle != null && handle.exists()) {
            val saveIDs = handle.readString().split(",".toRegex()).toTypedArray()
            val ids = Array<Int>()
            for (stringID in saveIDs) {
                ids.add(stringID.toInt())
            }
            if (!ids.removeValue(id, false)) {
                Gdx.app.log("Delete error", "Save ID $id not found in saves.saves")
            }
            handle.writeString(ids.toString(","), false)
            handle = handle.sibling("$id.json")
            if (!handle.delete()) {
                Gdx.app.log("Delete error", "$id.json not found")
                return false
            }
            return true
        }
        return false
    }

    /** Saves the default settings  */

    fun saveSettings() {
        val handle = getExtDir("settings.json")
        if (handle != null) {
            val settings = JSONObject()
            settings.put("trajectory", TerminalControl.trajectorySel)
            settings.put("pastTrajTime", TerminalControl.pastTrajTime)
            settings.put("weather", TerminalControl.weatherSel.toString())
            settings.put("sound", TerminalControl.soundSel)
            settings.put("sendCrash", TerminalControl.sendAnonCrash)
            settings.put("increaseZoom", TerminalControl.increaseZoom)
            settings.put("saveInterval", TerminalControl.saveInterval)
            settings.put("radarSweep", TerminalControl.radarSweep.toDouble())
            settings.put("advTraj", TerminalControl.advTraj)
            settings.put("areaWarning", TerminalControl.areaWarning)
            settings.put("collisionWarning", TerminalControl.collisionWarning)
            settings.put("showMva", TerminalControl.showMva)
            settings.put("showIlsDash", TerminalControl.showIlsDash)
            settings.put("compactData", TerminalControl.compactData)
            settings.put("showUncontrolled", TerminalControl.showUncontrolled)
            settings.put("alwaysShowBordersBackground", TerminalControl.alwaysShowBordersBackground)
            settings.put("rangeCircleDist", TerminalControl.rangeCircleDist)
            settings.put("lineSpacingValue", TerminalControl.lineSpacingValue)
            settings.put("colourStyle", TerminalControl.colourStyle)
            settings.put("realisticMetar", TerminalControl.realisticMetar)
            settings.put("defaultTabNo", TerminalControl.defaultTabNo)
            settings.put("emerChance", TerminalControl.emerChance.toString())
            settings.put("revision", TerminalControl.revision)
            handle.writeString(settings.toString(4), false)
        }
    }

    /** Saves game stats  */
    fun saveStats() {
        val handle = getExtDir("stats.json")
        if (handle != null) {
            val stats = JSONObject()
            stats.put("planesLanded", planesLanded)
            stats.put("emergenciesLanded", emergenciesLanded)
            stats.put("conflicts", conflicts)
            stats.put("wakeConflictTime", wakeConflictTime.toDouble())
            val unlockArray = JSONArray()
            for (unlock in UnlockManager.unlocks) {
                unlockArray.put(unlock)
            }
            stats.put("unlocks", unlockArray)
            handle.writeString(stats.toString(4), false)
        }
    }
}