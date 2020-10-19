package com.bombbird.terminalcontrol.entities.airports

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.approaches.ILS
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach
import com.bombbird.terminalcontrol.entities.procedures.holding.BackupHoldingPoints
import com.bombbird.terminalcontrol.entities.procedures.holding.HoldingPoints
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.runways.RunwayManager
import com.bombbird.terminalcontrol.entities.sidstar.RandomSID
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR
import com.bombbird.terminalcontrol.entities.sidstar.Sid
import com.bombbird.terminalcontrol.entities.sidstar.Star
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager.isNight
import com.bombbird.terminalcontrol.entities.trafficmanager.TakeoffManager
import com.bombbird.terminalcontrol.entities.weather.WindshearChance
import com.bombbird.terminalcontrol.entities.zones.ApproachZone
import com.bombbird.terminalcontrol.entities.zones.DepartureZone
import com.bombbird.terminalcontrol.entities.zones.ZoneLoader.loadApchZones
import com.bombbird.terminalcontrol.entities.zones.ZoneLoader.loadDepZones
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.RenameManager.renameAirportICAO
import com.bombbird.terminalcontrol.utilities.RenameManager.reverseNameAirportICAO
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadAirlineAircrafts
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadAirlines
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadHoldingPoints
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadILS
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadMissedInfo
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadRunways
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadSids
import com.bombbird.terminalcontrol.utilities.saving.FileLoader.loadStars
import org.apache.commons.lang3.ArrayUtils
import org.json.JSONObject
import java.util.*

class Airport {
    private val radarScreen = TerminalControl.radarScreen!!
    private val save: JSONObject?
    val runways: HashMap<String, Runway>
    val landingRunways: HashMap<String, Runway>
    val takeoffRunways: HashMap<String, Runway>
    lateinit var holdingPoints: HashMap<String, HoldingPoints>
        private set
    lateinit var missedApproaches: HashMap<String, MissedApproach>
        private set
    lateinit var approaches: HashMap<String, ILS>
        private set
    val icao: String
    lateinit var metar: JSONObject
        private set
    lateinit var stars: HashMap<String, Star>
        private set
    lateinit var sids: HashMap<String, Sid>
        private set
    val elevation: Int
    var windshear = ""
        private set
    lateinit var takeoffManager: TakeoffManager
        private set
    lateinit var runwayManager: RunwayManager
        private set
    var landings: Int
    var airborne: Int
    var isCongested: Boolean
        private set
    val aircraftRatio: Int
    val airlines: HashMap<Int, String>
    val aircrafts: HashMap<String, String>
    lateinit var approachZones: Array<ApproachZone>
        private set
    lateinit var departureZones: Array<DepartureZone>
        private set
    var isPendingRwyChange: Boolean
    var rwyChangeTimer: Float
    var isClosed: Boolean

    constructor(icao: String, elevation: Int, aircraftRatio: Int) {
        save = null
        this.icao = icao
        this.elevation = elevation
        this.aircraftRatio = aircraftRatio
        runways = loadRunways(icao)
        landingRunways = HashMap()
        takeoffRunways = HashMap()
        landings = 0
        airborne = 0
        isCongested = false
        airlines = loadAirlines(icao)
        aircrafts = loadAirlineAircrafts(icao)
        isPendingRwyChange = false
        rwyChangeTimer = 301f //In seconds
        isClosed = false
    }

    constructor(save: JSONObject) {
        this.save = save
        icao = renameAirportICAO(save.getString("icao"))
        elevation = save.getInt("elevation")
        runways = loadRunways(icao)
        landingRunways = HashMap()
        takeoffRunways = HashMap()
        landings = save.getInt("landings")
        airborne = save.getInt("airborne")
        isCongested = save.getBoolean("congestion")
        aircraftRatio = save.getInt("aircraftRatio")
        airlines = loadAirlines(icao)
        aircrafts = loadAirlineAircrafts(icao)
        isPendingRwyChange = save.optBoolean("pendingRwyChange", false)
        rwyChangeTimer = save.optDouble("rwyChangeTimer", 301.0).toFloat()
        isClosed = save.optBoolean("closed", false)
        val landing = save.getJSONArray("landingRunways")
        for (i in 0 until landing.length()) {
            val runway = runways[landing.getString(i)] ?: continue
            runway.setActive(landing = true, takeoff = false)
            landingRunways[runway.name] = runway
        }
        val takeoff = save.getJSONArray("takeoffRunways")
        for (i in 0 until takeoff.length()) {
            val runway = runways[takeoff.getString(i)] ?: continue
            runway.setActive(runway.isLanding, true)
            takeoffRunways[runway.name] = runway
        }
    }

    /** Loads other runway info from save file separately after loading main airport data (since aircraft have not been loaded during the main airport loading stage)  */
    fun updateOtherRunwayInfo(save: JSONObject) {
        for (runway in runways.values) {
            val aircraftsOnAppr = Array<Aircraft>()
            val queue = save.getJSONObject("runwayQueues").getJSONArray(runway.name)
            for (i in 0 until queue.length()) {
                aircraftsOnAppr.add(radarScreen.aircrafts[queue.getString(i)])
            }
            runway.aircraftsOnAppr = aircraftsOnAppr
            runway.isEmergencyClosed = !save.isNull("emergencyClosed") && save.getJSONObject("emergencyClosed").optBoolean(runway.name)
        }
    }

    /** Loads the necessary resources that cannot be loaded in constructor  */
    fun loadOthers() {
        holdingPoints = loadHoldingPoints(this)
        loadBackupHoldingPts()
        missedApproaches = loadMissedInfo(this)
        approaches = loadILS(this)
        for (runway in runways.values) {
            approaches[runway.name]?.let { runway.ils = it }
        }
        setOppRwys()
        stars = loadStars(this)
        sids = loadSids(this)
        RandomSID.loadSidNoise(icao)
        RandomSTAR.loadStarNoise(icao)

        //TerminalControl.tts.test(stars, sids);
        for (missedApproach in missedApproaches.values) {
            missedApproach.loadIls()
        }
        takeoffManager = TakeoffManager(this)
        runwayManager = RunwayManager(this, save?.optBoolean("night", isNight) ?: isNight)
        loadZones()
        updateZoneStatus()
        RandomSTAR.loadEntryTiming(this)
    }

    /** Loads the backup holding waypoints from before waypoint overhaul  */
    private fun loadBackupHoldingPts() {
        if (save == null) return
        //If not a new game, load the backupPts as needed
        if (save.isNull("backupPts")) {
            //Not a new game and no previous backupPts save - load the possible default used backupPts
            holdingPoints = BackupHoldingPoints.loadBackupPoints(icao, holdingPoints)
        } else {
            val pts = save.getJSONObject("backupPts")
            for (name in pts.keySet()) {
                if (holdingPoints.containsKey(name)) continue
                val pt = pts.getJSONObject(name)
                holdingPoints[name] = HoldingPoints(name, intArrayOf(pt.getInt("minAlt"), pt.getInt("maxAlt")), pt.getInt("maxSpd"), pt.getBoolean("left"), pt.getInt("inboundHdg"), pt.getDouble("legDist").toFloat())
            }
        }
    }

    /** loadOthers from JSON save  */
    fun loadOthers(save: JSONObject) {
        loadOthers()
        for (runway in runways.values) {
            val queueArray = Array<Aircraft>()
            val queue = save.getJSONObject("runwayQueues").getJSONArray(runway.name)
            for (i in 0 until queue.length()) {
                val aircraft = radarScreen.aircrafts[queue.getString(i)]
                if (aircraft != null) queueArray.add(aircraft)
            }
        }
        takeoffManager = TakeoffManager(this, save.getJSONObject("takeoffManager"))
        if (save.isNull("starTimers")) {
            RandomSTAR.loadEntryTiming(this)
        } else {
            RandomSTAR.loadEntryTiming(this, save.getJSONObject("starTimers"))
        }
    }

    /** Sets the opposite runway for each runway  */
    private fun setOppRwys() {
        for (runway in runways.values) {
            if (!runway.isOppRwySet()) {
                var oppNumber = runway.name.substring(0, 2).toInt() + 18
                if (oppNumber > 36) {
                    oppNumber -= 36
                }
                val oppExtra = when (val extra = if (runway.name.length == 3) runway.name[2].toString() else "") {
                    "L" -> "R"
                    "R" -> "L"
                    else -> extra
                }
                var oppRwyStr = oppNumber.toString() + oppExtra
                if (oppNumber < 10) {
                    oppRwyStr = "0$oppRwyStr"
                }
                runways[oppRwyStr]?.oppRwy = runway
                runways[oppRwyStr]?.let { runway.oppRwy = it }
            }
        }
    }

    /** Sets the runway's active state, and removes or adds it into hashMap of takeoff & landing runways  */
    fun setActive(rwy: String, landing: Boolean, takeoff: Boolean) {
        //Ignore if countdown timer is not up yet
        if (rwyChangeTimer > 0 || !isPendingRwyChange) return

        //Retrieves runway from hashtable
        val runway = runways[rwy] ?: return
        var ldgChange = false
        var tkofChange = false
        var ldgOff = false
        var tkofOff = false
        if (!runway.isLanding && landing) {
            //Add to landing runways if not landing before, but landing now
            landingRunways[rwy] = runway
            ldgChange = true
        } else if (runway.isLanding && !landing) {
            //Remove if landing before, but not landing now
            landingRunways.remove(rwy)
            ldgOff = true
        }
        if (!runway.isTakeoff && takeoff) {
            //Add to takeoff runways if not taking off before, but taking off now
            takeoffRunways[rwy] = runway
            tkofChange = true
        } else if (runway.isTakeoff && !takeoff) {
            //Remove if taking off before, but not taking off now
            takeoffRunways.remove(rwy)
            tkofOff = true
        }

        //Set runway's internal active state
        runway.setActive(landing, takeoff)

        //Message to inform user of runway change
        if ((ldgChange || tkofChange) && !runway.isEmergencyClosed && !runway.oppRwy.isEmergencyClosed) {
            var msg = "Runway $rwy at $icao is now active for "
            msg += if (ldgChange && tkofChange) {
                "takeoffs and landings."
            } else if (ldgChange) {
                "landings."
            } else {
                "takeoffs."
            }
            radarScreen.utilityBox.commsManager.normalMsg(msg)
            radarScreen.soundManager.playRunwayChange()
        }
        if ((ldgOff || tkofOff) && !runway.isEmergencyClosed && !runway.oppRwy.isEmergencyClosed) {
            var msg = "Runway $rwy at $icao is no longer active for "
            msg += if (ldgOff && tkofOff) {
                "takeoffs and landings."
            } else if (ldgOff) {
                "landings."
            } else {
                "takeoffs."
            }
            radarScreen.utilityBox.commsManager.normalMsg(msg)
            radarScreen.soundManager.playRunwayChange()
        }
    }

    /** Resets the runway change countdown timer, boolean to original  */
    fun resetRwyChangeTimer() {
        isPendingRwyChange = false
        rwyChangeTimer = 301f
    }

    /** Update loop  */
    fun update() {
        if (!radarScreen.tutorial && radarScreen.tfcMode === RadarScreen.TfcMode.NORMAL) {
            takeoffManager.update()
        }
        if (isPendingRwyChange) rwyChangeTimer -= Gdx.graphics.deltaTime
        if (rwyChangeTimer < 0) {
            if (isPendingRwyChange) updateRunwayUsage()
            resetRwyChangeTimer()
        }
    }

    /** Draws runways  */
    fun renderRunways() {
        isCongested = if (landings - airborne > 12 && radarScreen.tfcMode === RadarScreen.TfcMode.NORMAL) {
            if (!isCongested) radarScreen.utilityBox.commsManager.warningMsg("$icao is experiencing congestion! To allow aircraft on the ground to take off, reduce the number of arrivals into the airport by reducing speed, putting them in holding patterns or by closing the sector.")
            true
        } else {
            false
        }
        for (runway in runways.values) {
            runway.setLabelColor(if (isCongested) Color.ORANGE else radarScreen.defaultColour)
            runway.renderShape()
        }
    }

    /** Loads the departure/approach/exclusion zones for this airport  */
    fun loadZones() {
        approachZones = loadApchZones(icao)
        departureZones = loadDepZones(icao)
    }

    /** Updates the active status of departure/approach/exclusion zones  */
    fun updateZoneStatus() {
        //Updates approach zone status
        for (i in 0 until approachZones.size) {
            approachZones[i].updateStatus(landingRunways)
        }

        //Updates departure zone status
        for (i in 0 until departureZones.size) {
            departureZones[i].updateStatus(takeoffRunways)
        }
    }

    fun renderZones() {
        for (i in 0 until approachZones.size) {
            approachZones[i].renderShape()
        }
        for (i in 0 until departureZones.size) {
            departureZones[i].renderShape()
        }
    }

    fun setMetar(newMetar: JSONObject) {
        metar = newMetar.getJSONObject(reverseNameAirportICAO(icao))
        if ("TCHX" == icao) {
            //Use random windshear for TCHX since TCHH windshear doesn't apply
            val ws = WindshearChance.getRandomWsForAllRwy(icao, metar.getInt("windSpeed"))
            metar.put("windshear", if ("" == ws) JSONObject.NULL else ws)
        }
        println("METAR of $icao: $metar")
        updateRunwayUsage()
        radarScreen.ui.updateInfoLabel()
    }

    fun updateRunwayUsage() {
        //Update active runways (windHdg 0 is VRB wind)
        val windHdg = if (metar.isNull("windDirection")) 0 else metar.getInt("windDirection")
        windshear = ""
        windshear = if (!metar.isNull("windshear")) {
            metar.getString("windshear")
        } else {
            "None"
        }
        runwayManager.updateRunways(windHdg, metar.getInt("windSpeed"))
        for (runway in runways.values) {
            runway.isWindshear = runway.isLanding && ("ALL RWY" == windshear || ArrayUtils.contains(windshear.split(" ".toRegex()).toTypedArray(), "R" + runway.name))
        }
        updateZoneStatus()
    }

    fun allowSimultDep(): Boolean {
        return landings - airborne >= 6
    }

    val winds: IntArray
        get() = if (metar.isNull("windDirection")) intArrayOf(0, 0) else intArrayOf(metar.getInt("windDirection"), metar.getInt("windSpeed"))
    val gusts: Int
        get() = if (metar["windGust"] === JSONObject.NULL) {
            -1
        } else {
            metar.getInt("windGust")
        }
    val night: Boolean
        get() = runwayManager.prevNight
    val visibility: Int
        get() = metar.getInt("visibility")
}