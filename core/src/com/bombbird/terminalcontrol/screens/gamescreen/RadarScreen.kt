package com.bombbird.terminalcontrol.screens.gamescreen

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.isTCHXAvailable
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.unlockEgg
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.aircrafts.Emergency
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.airports.AirportName
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.separation.AreaPenetrationChecker
import com.bombbird.terminalcontrol.entities.separation.CollisionChecker
import com.bombbird.terminalcontrol.entities.separation.SeparationChecker
import com.bombbird.terminalcontrol.entities.separation.trajectory.TrajectoryStorage
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR
import com.bombbird.terminalcontrol.entities.trafficmanager.MaxTraffic.getMaxTraffic
import com.bombbird.terminalcontrol.entities.trafficmanager.MaxTraffic.loadHashmaps
import com.bombbird.terminalcontrol.entities.waketurbulence.WakeManager
import com.bombbird.terminalcontrol.entities.waypoints.BackupWaypoints
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.entities.waypoints.WaypointManager
import com.bombbird.terminalcontrol.entities.weather.Metar
import com.bombbird.terminalcontrol.entities.weather.WindDirChance
import com.bombbird.terminalcontrol.entities.weather.WindshearChance
import com.bombbird.terminalcontrol.entities.weather.WindspeedChance
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.TrafficFlowScreen
import com.bombbird.terminalcontrol.sounds.Pronunciation
import com.bombbird.terminalcontrol.ui.*
import com.bombbird.terminalcontrol.ui.tabs.Tab
import com.bombbird.terminalcontrol.ui.tutorial.TutorialManager
import com.bombbird.terminalcontrol.ui.utilitybox.UtilityBox
import com.bombbird.terminalcontrol.utilities.RenameManager.renameAirportICAO
import com.bombbird.terminalcontrol.utilities.Revision
import com.bombbird.terminalcontrol.utilities.math.RandomGenerator
import com.bombbird.terminalcontrol.utilities.saving.FileLoader
import com.bombbird.terminalcontrol.utilities.saving.GameLoader
import com.bombbird.terminalcontrol.utilities.saving.GameSaver
import org.apache.commons.lang3.ArrayUtils
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashSet

class RadarScreen : GameScreen {
    companion object {
        /** Disposes of static final variables after user quits app  */
        @JvmStatic
        fun disposeStatic() {
            if (DataTag.SKIN != null) DataTag.SKIN.dispose()
            if (DataTag.ICON_ATLAS != null) DataTag.ICON_ATLAS.dispose()
        }
    }

    enum class Weather {
        LIVE, RANDOM, STATIC
    }

    enum class TfcMode {
        NORMAL, ARRIVALS_ONLY
    }

    var saveId: Int
    var mainName: String
    var magHdgDev = 0f
    var maxAlt = 0
    var minAlt = 0
    var transLvl = 0
    var separationMinima = 0
    var divertHdg = 0
    var airac: Int
    var callsign = ""
    var deptCallsign = ""
    var trajectoryLine = 0
    var pastTrajTime = 0
    var weatherSel: Weather = Weather.LIVE
    var soundSel = 0
    var emerChance: Emergency.Chance = Emergency.Chance.MEDIUM
    var tfcMode: TfcMode = TfcMode.NORMAL
    var allowNight: Boolean
    var nightStart: Int
    var nightEnd: Int
    var radarSweepDelay = 0f
    var advTraj = 0
    var areaWarning = 0
    var collisionWarning = 0
    var showMva = false
    var showIlsDash = false
    var compactData = false
    var showUncontrolled = false
    var alwaysShowBordersBackground = false
    var lineSpacingValue = 0
    var colourStyle = 0
    var realisticMetar = false

    //Advanced traffic settings
    var trafficMode: Int
    var maxPlanes: Int
    var flowRate: Int

    //Whether the game is a tutorial
    var tutorial = false

    //Score of current game
    var planesToControl //To keep track of how well the user is coping; number of arrivals to control is approximately this value
            : Float
        set(value) {
            field = if (trafficMode == TrafficFlowScreen.PLANES_IN_CONTROL) maxPlanes.toFloat() else MathUtils.clamp(value, 4f, getMaxTraffic(mainName))
        }

    private var score //Score of the player; equal to the number of planes landed without a separation incident (with other traffic or terrain)
            : Int
    var highScore //High score of player
            : Int
        private set

    val planesInControl: Int
        get() {
            var count = 0
            for (aircraft in aircrafts.values) {
                if (aircraft.isArrivalDeparture) count++
            }
            return count
        }

    //Fun stats
    var arrivals: Int
    var separationIncidents: Int
    var wakeInfringeTime: Float
    var emergenciesLanded: Int
    var spawnTimer: Float
    var previousOffset: Float
        private set
    var information: Char
        private set

    //Timer for getting METAR every quarter of hour
    private val timer = Timer(true)
    lateinit var metar: Metar
        private set

    //Timer for updating aircraft radar returns, trails, save and discord RPC every given amount of time
    var radarTime: Float
    var trailTime: Float
        private set
    private var saveTime: Float
    private var rpcTime: Float

    //Stores callsigns of all aircraft generated and aircraft waiting to be generated (for take offs)
    var allAircraft: HashSet<String> = HashSet()

    //Waypoint manager for managing waypoint selected status
    lateinit var waypointManager: WaypointManager

    //Separation checker for checking separation between aircraft & terrain
    lateinit var separationChecker: SeparationChecker

    //Trajectory storage, APW, STCAS
    lateinit var trajectoryStorage: TrajectoryStorage
    lateinit var areaPenetrationChecker: AreaPenetrationChecker
    lateinit var collisionChecker: CollisionChecker

    //Wake turbulence checker
    var wakeManager: WakeManager

    //Tutorial manager
    var tutorialManager: TutorialManager? = null

    //Communication box to keep track of aircraft transmissions
    lateinit var utilityBox: UtilityBox
        private set

    //Runway change box for changing runway configuration
    lateinit var runwayChanger: RunwayChanger
        private set

    //The selected aircraft
    private var selectedAircraft: Aircraft? = null

    //Simultaneous landing achievement storage
    private val simultaneousLanding: LinkedHashMap<String, Float>

    //Easter egg thing yey
    private val lastTapped: Queue<Char> = Queue()
    private val save: JSONObject?
    val revision //Revision for indicating if save parser needs to do anything special
            : Int

    //Stores aircraft generators
    private val generatorList = Array<RandomGenerator.MultiThreadGenerator>()

    constructor(game: TerminalControl, name: String, airac: Int, saveID: Int, tutorial: Boolean) : super(game) {
        //Creates new game
        save = null
        mainName = name
        this.airac = airac
        revision = Revision.CURRENT_REVISION
        saveId = saveID
        this.tutorial = tutorial
        planesToControl = 6f
        score = 0
        highScore = 0
        arrivals = 0
        separationIncidents = 0
        wakeInfringeTime = 0f
        emergenciesLanded = 0
        spawnTimer = 0f
        previousOffset = 0f
        information = MathUtils.random(65, 90).toChar()
        loadStageCamTimer()

        //Set timer for radar delay, trails and autosave
        radarTime = radarSweepDelay
        trailTime = 10f
        saveTime = TerminalControl.saveInterval.toFloat()
        rpcTime = 60f
        simultaneousLanding = LinkedHashMap()
        if (tutorial) {
            trajectoryLine = 90
            pastTrajTime = -1
            radarSweepDelay = 2f
            advTraj = -1
            areaWarning = -1
            collisionWarning = -1
            showMva = true
            showIlsDash = false
            compactData = false
            showUncontrolled = false
            alwaysShowBordersBackground = true
            rangeCircleDist = 0
            lineSpacingValue = 1
            colourStyle = 0
            realisticMetar = false
            emerChance = Emergency.Chance.OFF
            soundSel = TerminalControl.getDefaultSoundSetting()
            weatherSel = Weather.STATIC
        } else {
            trajectoryLine = TerminalControl.trajectorySel
            pastTrajTime = TerminalControl.pastTrajTime
            radarSweepDelay = TerminalControl.radarSweep
            advTraj = TerminalControl.advTraj
            areaWarning = TerminalControl.areaWarning
            collisionWarning = TerminalControl.collisionWarning
            showMva = TerminalControl.showMva
            showIlsDash = TerminalControl.showIlsDash
            compactData = TerminalControl.compactData
            showUncontrolled = TerminalControl.showUncontrolled
            alwaysShowBordersBackground = TerminalControl.alwaysShowBordersBackground
            rangeCircleDist = TerminalControl.rangeCircleDist
            lineSpacingValue = TerminalControl.lineSpacingValue
            colourStyle = TerminalControl.colourStyle
            realisticMetar = TerminalControl.realisticMetar
            weatherSel = TerminalControl.weatherSel
            soundSel = TerminalControl.soundSel
            emerChance = TerminalControl.emerChance
        }
        tfcMode = TfcMode.NORMAL
        allowNight = true
        nightStart = 2200
        nightEnd = 600
        trafficMode = 0
        maxPlanes = -1
        flowRate = -1
        wakeManager = WakeManager()
        loadEasterEggQueue()
        if (tutorial) {
            tutorialManager = TutorialManager(this)
        }
    }

    constructor(game: TerminalControl?, save: JSONObject) : super(game) {
        //Loads the game from save
        this.save = save
        saveId = save.getInt("saveId")
        mainName = renameAirportICAO(save.getString("MAIN_NAME"))
        airac = save.getInt("AIRAC")
        revision = save.optInt("revision", 0)
        planesToControl = save.getDouble("planesToControl").toFloat()
        score = save.getInt("score")
        highScore = save.getInt("highScore")
        arrivals = save.getInt("arrivals")
        separationIncidents = save.optInt("separationIncidents", 0)
        wakeInfringeTime = save.optDouble("wakeInfringeTime", 0.0).toFloat()
        emergenciesLanded = save.optInt("emergenciesLanded", 0)
        spawnTimer = save.optDouble("spawnTimer", 60.0).toFloat()
        previousOffset = save.optDouble("previousOffset", 0.0).toFloat()
        information = if (save.isNull("information")) MathUtils.random(65, 90).toChar() else save.getInt("information").toChar()
        loadStageCamTimer()

        //Set timer for radar delay, trails and autosave
        radarTime = save.optDouble("radarTime", TerminalControl.radarSweep.toDouble()).toFloat()
        trailTime = save.optDouble("trailTime", 10.0).toFloat()
        saveTime = TerminalControl.saveInterval.toFloat()
        rpcTime = 60f
        trajectoryLine = save.optInt("trajectoryLine", 90)
        pastTrajTime = save.optInt("pastTrajTime", -1)
        radarSweepDelay = save.optDouble("radarSweep", 2.0).toFloat()
        advTraj = save.optInt("advTraj", -1)
        areaWarning = save.optInt("areaWarning", -1)
        collisionWarning = save.optInt("collisionWarning", -1)
        showMva = save.optBoolean("showMva", true)
        showIlsDash = save.optBoolean("showIlsDash", false)
        compactData = save.optBoolean("compactData", false)
        showUncontrolled = save.optBoolean("showUncontrolled", false)
        alwaysShowBordersBackground = save.optBoolean("alwaysShowBordersBackground", true)
        rangeCircleDist = save.optInt("rangeCircleDist", 0)
        lineSpacingValue = save.optInt("lineSpacingValue", 1)
        colourStyle = save.optInt("colourStyle", 0)
        realisticMetar = save.optBoolean("realisticMetar", false)
        val weather = save.optString("liveWeather")
        weatherSel = when (weather) {
            "true" -> Weather.LIVE
            "false" -> Weather.RANDOM
            else -> Weather.valueOf(save.getString("liveWeather"))
        }
        soundSel = if (save.isNull("sounds")) TerminalControl.getDefaultSoundSetting() else save.getInt("sounds")
        emerChance = if (save.isNull("emerChance")) {
            Emergency.Chance.MEDIUM
        } else {
            Emergency.Chance.valueOf(save.getString("emerChance"))
        }
        tfcMode = if (save.isNull("tfcMode")) {
            TfcMode.NORMAL
        } else {
            TfcMode.valueOf(save.getString("tfcMode"))
        }
        allowNight = save.optBoolean("allowNight", true)
        nightStart = save.optInt("nightStart", 2200)
        nightEnd = save.optInt("nightEnd", 600)
        trafficMode = save.optInt("trafficMode", 0)
        maxPlanes = save.optInt("maxPlanes", -1)
        flowRate = save.optInt("flowRate", -1)
        wakeManager = if (save.isNull("wakeManager")) WakeManager() else WakeManager(save.getJSONObject("wakeManager"))
        loadEasterEggQueue()
        simultaneousLanding = LinkedHashMap()
    }

    private fun loadEasterEggQueue() {
        lastTapped.addLast(' ')
        lastTapped.addLast(' ')
    }

    private fun loadBackupWaypoints() {
        if (save == null) return
        if (save.isNull("backupWpts")) {
            //No backup waypoints saved, add possible backup ones in here
            val backup = BackupWaypoints.loadBackupWpts(mainName)
            for ((key, value) in backup) {
                if (waypoints.containsKey(key)) continue
                val newWpt = Waypoint(key, value[0], value[1])
                waypoints[key] = newWpt
                stage.addActor(newWpt)
            }
        } else {
            //Waypoints saved, load them directly instead
            val wpts = save.getJSONObject("backupWpts")
            for (wptName in wpts.keySet()) {
                val wpt = wpts.getJSONObject(wptName)
                //Add the waypoint only if it doesn't exist
                if (!waypoints.containsKey(wptName)) {
                    val newWpt = Waypoint(wptName, wpt.getInt("x"), wpt.getInt("y"))
                    waypoints[wptName] = newWpt
                    stage.addActor(newWpt)
                }
            }
        }
    }

    private fun loadStageCamTimer() {
        //Set stage params
        stage = Stage(ScalingViewport(Scaling.fillY, 5760f, 3240f), game.batch)
        stage.viewport.update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true)

        //Set camera params
        camera = stage.viewport.camera as OrthographicCamera
        camera.setToOrtho(false, 5760f, 3240f)
        viewport = ScalingViewport(Scaling.fillY, TerminalControl.WIDTH.toFloat(), TerminalControl.HEIGHT.toFloat(), camera)
        viewport.apply()
        camera.position[1890f, 1620f] = 0f
        if (Gdx.app.type == Application.ApplicationType.Android) {
            camera.position[2286f, 1620f] = 0f
        }
        labelStage = Stage(ScalingViewport(Scaling.fillY, 5760f, 3240f), game.batch)
        labelStage.getViewport().camera = camera
        labelStage.getViewport().update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true)
    }

    private fun loadInputProcessors() {
        //Set input processors
        inputMultiplexer.addProcessor(uiStage)
        inputMultiplexer.addProcessor(labelStage)
        inputMultiplexer.addProcessor(gd)
        inputMultiplexer.addProcessor(this)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun loadPanel() {
        //Set 2nd stage, camera for UI
        uiStage = Stage(ExtendViewport(1920f, 3240f), game.batch)
        uiStage.viewport.update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true)
        ui = Ui()
        ui.loadTabs()
        uiCam = uiStage.viewport.camera as OrthographicCamera
        uiCam.setToOrtho(false, 1920f, 3240f)
        uiViewport = ExtendViewport(TerminalControl.WIDTH.toFloat(), TerminalControl.HEIGHT.toFloat(), uiCam)
        uiViewport.apply()
        uiCam.position[2880f, 1620f] = 0f
    }

    private fun loadAirports() {
        //Load airport information form file, add to hashmap
        val handle = Gdx.files.internal("game/$mainName/$airac/airport.arpt")
        val jo = JSONObject(handle.readString())
        minAlt = jo.getInt("minAlt")
        maxAlt = jo.getInt("maxAlt")
        transLvl = jo.getInt("transLvl")
        separationMinima = jo.getInt("minSep")
        magHdgDev = jo.getDouble("magHdgDev").toFloat()
        callsign = jo.getString("apchCallsign")
        deptCallsign = jo.getString("depCallsign")
        val airports1 = jo.getJSONObject("airports")
        for (icao in airports1.keySet()) {
            val airport1 = airports1.getJSONObject(icao)
            val airport = Airport(icao, airport1.getInt("elevation"), airport1.getInt("ratio"))
            airport.loadOthers()
            airports[icao] = airport
            AirportName.airportNames[icao] = airport1.getString("name")
        }
    }

    private fun loadMetar() {
        //Load METAR info for airports, sets it for each airport
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.add(Calendar.MINUTE, 15)
        var minute = calendar[Calendar.MINUTE]
        minute = when {
            minute >= 45 -> 45
            minute >= 30 -> 30
            minute >= 15 -> 15
            else -> 0
        }
        calendar[Calendar.MINUTE] = minute
        calendar[Calendar.SECOND] = 0
        metar = if (save == null) Metar(this) else Metar(this, save.getJSONObject("metar"))

        //Update the METAR every quarter of the hour
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Gdx.app.postRunnable {
                    if (!runwayChanger.isVisible) return@postRunnable
                    runwayChanger.hideAll()
                    utilityBox.setVisible(true)
                }
                metar.updateMetar(tutorial)
            }
        }, calendar.time, 900000)
        if (save == null) {
            metar.updateMetar(tutorial) //Update the current airport METAR if not from save (airports not loaded in save at this stage)
        }
    }

    /** Creates a new departure at the given airport  */
    fun newDeparture(callsign: String, icaoType: String, airport: Airport, runway: Runway) {
        aircrafts[callsign] = Departure(callsign, icaoType, airport, runway)
    }

    /** Creates a new arrival for random airport  */
    private fun newArrival() {
        if (trafficMode == TrafficFlowScreen.FLOW_RATE) {
            if (flowRate == 0) return
            planesToControl = arrivals.toFloat()
            spawnTimer = -previousOffset //Subtract the additional (or less) time before spawning previous aircraft
            val defaultRate = 3600f / flowRate
            spawnTimer += defaultRate //Add the constant rate timing
            previousOffset = defaultRate * MathUtils.random(-0.1f, 0.1f)
            spawnTimer += previousOffset
        } else {
            //Min 50sec for >=4 planes diff, max 80sec for <=1 plane diff
            spawnTimer = 90f - 10 * (planesToControl - arrivals)
            spawnTimer = MathUtils.clamp(spawnTimer, 50f, 80f)
        }

        //Start a new thread for generating arrivals to prevent lag in main game loop
        val multiThreadGenerator = RandomGenerator.MultiThreadGenerator(this, allAircraft)
        generatorList.add(multiThreadGenerator)
        Thread(multiThreadGenerator).start()
    }

    /** Loads the full UI for RadarScreen  */
    private fun loadUI() {
        //Reset stage
        stage.clear()

        //Show loading screen
        loading = true
        loadingTime = 0f

        //Load range circles
        loadRange()

        //Load shoreline data
        Shoreline.loadShoreline()

        //Load waypoints
        waypoints = FileLoader.loadWaypoints()
        loadBackupWaypoints()

        //Load specific waypoint pronunciation
        Pronunciation.loadPronunciation()

        //Load maximum traffic limits for different airports
        loadHashmaps()

        //Load airports
        loadAirports()

        //Load separation checker
        separationChecker = SeparationChecker()
        stage.addActor(separationChecker)

        //Load trajectory storage, APW, STCAS
        trajectoryStorage = TrajectoryStorage()
        areaPenetrationChecker = AreaPenetrationChecker()
        collisionChecker = CollisionChecker()

        //Load waypoint manager
        waypointManager = WaypointManager()

        //Load obstacles
        obsArray = FileLoader.loadObstacles()

        //Load panels
        loadPanel()
        ui.setNormalPane(true)
        ui.setSelectedPane(null)

        //Load utility box
        utilityBox = UtilityBox()

        //Load runway change box
        runwayChanger = RunwayChanger()

        //Load request flasher
        requestFlasher = RequestFlasher(this)

        //Initialise tutorial manager if is tutorial
        if (tutorial) tutorialManager?.init()

        //Load weather chances
        WindshearChance.loadWsChance()
        WindspeedChance.loadWindSpdChance()
        WindDirChance.loadWindDirChance()

        //Load METARs
        loadMetar()
        loadInputProcessors()
    }

    /** Updates the display state (flyover or not) for all waypoints  */
    private fun updateWaypointDisplay() {
        for (waypoint in waypoints.values) {
            waypoint.updateFlyOverStatus()
        }
    }

    /** Updates the time values for each timer & runs tasks when time is reached  */
    private fun updateTimers() {
        val deltaTime = Gdx.graphics.deltaTime
        radarTime -= deltaTime
        if (radarTime <= 0) {
            updateRadarInfo()
            radarTime += if (radarSweepDelay / speed < 0.25f) {
                0.25f * speed
            } else {
                radarSweepDelay
            }
        }
        trailTime -= deltaTime
        if (trailTime <= 0) {
            addTrailDot()
            trailTime += 10f
        }
        rpcTime -= deltaTime
        if (rpcTime <= 0) {
            TerminalControl.discordManager.updateRPC()
            rpcTime += 60f
        }
        if (!tutorial) {
            if (TerminalControl.saveInterval > 0) {
                //If autosave enabled
                saveTime -= deltaTime
                if (saveTime <= 0) {
                    GameSaver.saveGame()
                    saveTime += TerminalControl.saveInterval.toFloat()
                }
            }
            arrivals = 0
            for (aircraft in aircrafts.values) {
                if (aircraft is Arrival && aircraft.getControlState() == Aircraft.ControlState.ARRIVAL) arrivals++
            }
            spawnTimer -= deltaTime
            if (spawnTimer <= 0 && arrivals < planesToControl) {
                //Minimum 50 sec interval between each new plane
                newArrival()
            }
            checkGenerators()
        }
        if (!UnlockManager.unlocks.contains("parallelLanding")) {
            //Update simultaneous landing linkedhashmap
            val iterator = simultaneousLanding.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                entry.setValue(entry.value + deltaTime)
                if (entry.value > 5) iterator.remove()
            }
        }
    }

    /** Checks the list of multi threaded generators, creates new arrival if done */
    private fun checkGenerators() {
        val generatorIterator = generatorList.iterator()
        while (generatorIterator.hasNext()) {
            val generator = generatorIterator.next()
            if (generator?.done == true) {
                //If generator is done generating, copy info to local variables and create a new arrival from them
                val aircraftInfo = generator.aircraftInfo ?: continue
                val finalAirport = generator.finalAirport ?: continue
                val arrival = Arrival(aircraftInfo[0], aircraftInfo[1], finalAirport)
                allAircraft.add(aircraftInfo[0])
                aircrafts[aircraftInfo[0]] = arrival
                arrivals++
                generatorIterator.remove() //Remove the generator since it's no longer needed
            }
        }
    }

    /** Sets the radar return of aircraft to current aircraft information  */
    private fun updateRadarInfo() {
        for (aircraft in aircrafts.values) {
            aircraft.updateRadarInfo()
        }
    }

    /** Adds a new trail dot value to the aircraft's trail queue  */
    private fun addTrailDot() {
        for (aircraft in aircrafts.values) {
            aircraft.addTrailDot()
        }
    }

    override fun updateTutorial() {
        tutorialManager?.update()
    }

    override fun update() {
        //Update timers
        updateTimers()

        //Updates waypoints status
        waypointManager.update()

        //Updates STAR timers
        RandomSTAR.update()

        //Update airport stuff
        for (airport in airports.values) {
            airport.update()
        }

        //Update runway changer timer
        runwayChanger.update()

        //Update status manager timer
        utilityBox.statusManager.update()

        //Update tutorial stuff if is tutorial
        if (tutorialManager != null) {
            tutorialManager?.update()
        }
    }

    override fun renderShape() {
        //Updates aircraft separation status
        separationChecker.update()

        //Update trajectory stuff (full version only)
        if (TerminalControl.full) {
            trajectoryStorage.update()
            areaPenetrationChecker.renderShape()
            collisionChecker.renderShape()
        }

        //Draw shoreline
        Shoreline.renderShape()

        //Draw obstacles
        val saveForLast = Array<Obstacle>()
        for (obstacle in obsArray) {
            if (obstacle.isEnforced || obstacle.isConflict || obstacle.label.text.toString()[0] == '#') {
                saveForLast.add(obstacle)
            } else {
                obstacle.renderShape()
            }
        }
        for (obstacle in saveForLast) {
            obstacle.renderShape()
        }

        //Draw the range circles if present
        renderRangeCircles()

        //Additional adjustments for certain airports
        shapeRenderer.color = Color.BLACK
        if ("TCTP" == mainName) {
            shapeRenderer.line(4500f, 2416f, 4500f, 2124f)
            shapeRenderer.line(1256f, 2050f, 1256f, 1180f)
        }

        //Draw runway(s) for each airport
        for (airport in airports.values) {
            airport.renderRunways()
        }

        //Draw approach zone(s) for each airport
        for (airport in airports.values) {
            airport.renderZones()
        }

        //Draw waypoints, reset restriction display
        val flyOver = Array<Waypoint>()
        for (waypoint in waypoints.values) {
            waypoint.setRestrDisplay(-1, -1, -1) //Reset all restrictions before drawing in renderShape
            if (waypoint.isFlyOver) {
                //Save flyovers for later
                flyOver.add(waypoint)
            } else {
                waypoint.renderShape()
            }
        }
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        //Draw filled flyover waypoints
        for (waypoint in flyOver) {
            waypoint.renderShape()
        }
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)

        //Draw aircraft
        for (aircraft in aircrafts.values) {
            aircraft.renderShape()
        }

        //Draw ILS arcs
        for (airport in airports.values) {
            for (ils in airport.approaches.values) {
                ils.renderShape()
            }
        }
        separationChecker.renderShape()
        if (selectedAircraft != null) {
            wakeManager.renderWake(selectedAircraft)
        }
        wakeManager.renderIlsWake()
        shapeRenderer.end()
    }

    fun addToEasterEggQueue(aircraft: Aircraft) {
        lastTapped.removeFirst()
        lastTapped.addLast(aircraft.callsign[0])
        checkEasterEgg()
    }

    private fun checkEasterEgg() {
        if (lastTapped.size < 2) return
        if (!isTCHXAvailable && lastTapped.first() == 'H' && lastTapped.last() == 'X') {
            //Easter egg unlocked
            unlockEgg("HX")
            utilityBox.commsManager.alertMsg("Congratulations, you have found the easter egg! A new airport is waiting for you!")
        }
    }

    fun addAndCheckSimultLanding(arrival: Arrival): Boolean {
        for (callsign in simultaneousLanding.keys) {
            val aircraft = aircrafts[callsign]
            if (arrival.airport != aircraft?.airport) continue
            val airportIcao = arrival.airport.icao
            if (arrival.ils == null) return false
            val rwy1 = arrival.ils.rwy.name
            if (aircraft?.ils == null) continue
            val rwy2 = aircraft.ils.rwy.name
            val rwys = arrayOf(rwy1, rwy2)
            if ("TCWS" == airportIcao) {
                if (ArrayUtils.contains(rwys, "02L") && ArrayUtils.contains(rwys, "02C") || ArrayUtils.contains(rwys, "20R") && ArrayUtils.contains(rwys, "20C")) return true
            } else if ("TCTT" == airportIcao || "TCAA" == airportIcao) {
                if (ArrayUtils.contains(rwys, "34L") && ArrayUtils.contains(rwys, "34R") || ArrayUtils.contains(rwys, "16R") && ArrayUtils.contains(rwys, "16L")) return true
            } else if ("TCPG" == airportIcao) {
                if (ArrayUtils.contains(rwys, "26L") && ArrayUtils.contains(rwys, "27R") || ArrayUtils.contains(rwys, "08R") && ArrayUtils.contains(rwys, "09L")) return true
            }
        }
        simultaneousLanding[arrival.callsign] = 0f
        return false
    }

    /** Estimates the duration played (if save has no play time data)  */
    private fun estimatePlayTime(): Float {
        var landed = 0
        for (airport in airports.values) {
            landed += airport.landings
        }

        //Assume 90 seconds between landings lol
        return (landed * 90).toFloat()
    }

    /** Updates the colour scheme of the radar screen  */
    fun updateColourStyle() {
        //Runway label colour
        for (airport in airports.values) {
            for (runway in airport.runways.values) {
                runway.setLabelColor(defaultColour)
            }
        }
    }

    /** Gets the route/runway drawing colour depending on colour scheme  */
    val defaultColour: Color
        get() = when (colourStyle) {
            0 -> Color.WHITE
            1 -> Color.GRAY
            else -> {
                Gdx.app.log("RadarScreen", "Unknown colour style $colourStyle")
                Color.WHITE
            }
        }

    /** Gets the ILS drawing colour depending on colour scheme  */
    val iLSColour: Color
        get() = when (colourStyle) {
            0 -> Color.CYAN
            1 -> Color.GRAY
            else -> {
                Gdx.app.log("RadarScreen", "Unknown colour style $colourStyle")
                Color.CYAN
            }
        }

    override fun render(delta: Float) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) && !tutorial && !loading) {
            //On android, change to pause screen if not paused, un-pause if paused
            setGameRunning(!running)
        }
        super.render(delta)
    }

    override fun show() {
        //Implements show method of screen, loads UI & save (if available) after show is called if it hasn't been done
        if (!uiLoaded) {
            Ui.generatePaneTextures()
            DataTag.setLoadedIcons(false)
            Tab.setLoadedStyles(false)
            loadUI()
            GameLoader.loadSaveData(save)
            uiLoaded = true
            playTime = save?.optDouble("playTime", estimatePlayTime().toDouble())?.toFloat() ?: 0f
        }
        TerminalControl.discordManager.updateRPC()
        updateWaypointDisplay()
        setGameRunning(true)
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        //Updates position of tutorial box if tutorial
        if (tutorial) {
            tutorialManager?.scrollPane?.x = TerminalControl.WIDTH.toFloat() / TerminalControl.HEIGHT * 3240 - 1750
        }
    }

    override fun dispose() {
        //Implements dispose method of screen, disposes resources after they're no longer needed
        super.dispose()
        timer.cancel()
        TerminalControl.tts.cancel()
    }

    fun setSelectedAircraft(aircraft: Aircraft?) {
        runwayChanger.hideAll()
        if (selectedAircraft != null) {
            selectedAircraft?.isSelected = false
            selectedAircraft?.dataTag?.updateBorderBackgroundVisibility(false)
        }
        if (aircraft != null) {
            aircraft.isSelected = true
            aircraft.dataTag.updateBorderBackgroundVisibility(true)
        }
        if (aircraft != null && aircraft.isArrivalDeparture) {
            ui.setSelectedPane(aircraft)
            ui.setNormalPane(false)
        } else {
            ui.setNormalPane(true)
            ui.setSelectedPane(null)
            utilityBox.setVisible(true)
        }
        selectedAircraft = aircraft
    }

    fun getSelectedAircraft(): Aircraft? {
        return selectedAircraft
    }

    fun getScore(): Int {
        return score
    }

    fun setScore(score: Int) {
        this.score = if (score < 0) 0 else score
        if (score > highScore) {
            highScore = score
        }
        ui.updateScoreLabels()
    }

    fun isUtilityBoxInitialized(): Boolean {
        return this::utilityBox.isInitialized
    }

    //If aircraft is a departure, and is in your control
    val departures: Int
        get() {
            var count = 0
            for (aircraft in aircrafts.values) {
                if (aircraft is Departure && aircraft.isArrivalDeparture()) {
                    //If aircraft is a departure, and is in your control
                    count++
                }
            }
            return count
        }

    fun updateInformation() {
        information++
        if (information.toInt() > 90) information -= 26
        utilityBox.commsManager.normalMsg("Information $information is now current")
    }
}