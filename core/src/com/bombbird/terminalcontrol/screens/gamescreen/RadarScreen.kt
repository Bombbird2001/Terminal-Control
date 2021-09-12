package com.bombbird.terminalcontrol.screens.gamescreen

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.ScalingViewport
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.RangeCircle
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
import com.bombbird.terminalcontrol.entities.separation.HandoverController
import com.bombbird.terminalcontrol.entities.separation.SeparationChecker
import com.bombbird.terminalcontrol.entities.separation.trajectory.TrajectoryStorage
import com.bombbird.terminalcontrol.entities.sidstar.RandomSID
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR
import com.bombbird.terminalcontrol.entities.sidstar.Sid
import com.bombbird.terminalcontrol.entities.trafficmanager.MaxTraffic.getMaxTraffic
import com.bombbird.terminalcontrol.entities.trafficmanager.MaxTraffic.loadHashmaps
import com.bombbird.terminalcontrol.entities.waketurbulence.WakeManager
import com.bombbird.terminalcontrol.entities.waypoints.BackupWaypoints
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.entities.waypoints.WaypointManager
import com.bombbird.terminalcontrol.entities.weather.*
import com.bombbird.terminalcontrol.screens.selectgamescreen.LoadGameScreen
import com.bombbird.terminalcontrol.screens.settingsscreen.customsetting.TrafficFlowScreen
import com.bombbird.terminalcontrol.sounds.Pronunciation
import com.bombbird.terminalcontrol.ui.*
import com.bombbird.terminalcontrol.ui.datatag.DataTag
import com.bombbird.terminalcontrol.ui.datatag.DataTagConfig
import com.bombbird.terminalcontrol.ui.tabs.Tab
import com.bombbird.terminalcontrol.ui.tutorial.TutorialManager
import com.bombbird.terminalcontrol.ui.utilitybox.UtilityBox
import com.bombbird.terminalcontrol.utilities.Fonts
import com.bombbird.terminalcontrol.utilities.RenameManager.renameAirportICAO
import com.bombbird.terminalcontrol.utilities.Revision
import com.bombbird.terminalcontrol.utilities.SafeStage
import com.bombbird.terminalcontrol.utilities.math.MathTools
import com.bombbird.terminalcontrol.utilities.math.random.ArrivalGenerator
import com.bombbird.terminalcontrol.utilities.files.FileLoader
import com.bombbird.terminalcontrol.utilities.files.GameLoader
import com.bombbird.terminalcontrol.utilities.files.GameSaver
import org.apache.commons.lang3.ArrayUtils
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.round

class RadarScreen : GameScreen {
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
    var areaWarning = -1
    var collisionWarning = -1
    var showMva = false
    var showIlsDash = false
    var showUncontrolled = false
    var alwaysShowBordersBackground = false
    var lineSpacingValue = 0
    var colourStyle = 0
    var realisticMetar = false
    var distToGoVisible = 0
    var showSectorBoundary = true

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

    var score //Score of the player; equal to the number of planes landed without a separation incident (with other traffic or terrain)
            : Int
        private set
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

    //Timer for updating aircraft radar returns, trails, save, discord RPC every given amount of time
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
    lateinit var handoverController: HandoverController

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
    var selectedAircraft: Aircraft? = null
        private set

    //Simultaneous landing achievement storage
    private val simultaneousLanding: LinkedHashMap<String, Float>

    //Easter egg thing yey
    private val lastTapped: Queue<Char> = Queue()
    private val save: JSONObject?
    val revision //Revision for indicating if save parser needs to do anything special
            : Int

    //Distance measuring tool distance display
    private lateinit var distLabel: Label

    //Stores aircraft generators
    private val generatorList = Array<ArrivalGenerator>()

    //Temporary storage of loadGameScreen for exception handling
    private var loadGameScreen: LoadGameScreen? = null

    //Weather cells
    private var thunderCellTime: Float
    var stormSpawnTime: Float
    var stormNumber: Int
    val thunderCellArray = Array<ThunderCell>()

    //Datatag configuration
    var datatagConfig: DataTagConfig

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
        thunderCellTime = 10f
        stormSpawnTime = 10f
        stormNumber = 0
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
            datatagConfig = DataTagConfig(DataTagConfig.DEFAULT)
            showUncontrolled = false
            alwaysShowBordersBackground = true
            rangeCircleDist = 0
            lineSpacingValue = 1
            colourStyle = 0
            realisticMetar = false
            emerChance = Emergency.Chance.OFF
            soundSel = 2
            weatherSel = Weather.STATIC
            distToGoVisible = 0
            showSectorBoundary = true
        } else {
            trajectoryLine = TerminalControl.trajectorySel
            pastTrajTime = TerminalControl.pastTrajTime
            radarSweepDelay = TerminalControl.radarSweep
            advTraj = TerminalControl.advTraj
            areaWarning = TerminalControl.areaWarning
            collisionWarning = TerminalControl.collisionWarning
            showMva = TerminalControl.showMva
            showIlsDash = TerminalControl.showIlsDash
            datatagConfig = DataTagConfig(TerminalControl.datatagConfig)
            showUncontrolled = TerminalControl.showUncontrolled
            alwaysShowBordersBackground = TerminalControl.alwaysShowBordersBackground
            rangeCircleDist = TerminalControl.rangeCircleDist
            lineSpacingValue = TerminalControl.lineSpacingValue
            colourStyle = TerminalControl.colourStyle
            realisticMetar = TerminalControl.realisticMetar
            weatherSel = TerminalControl.weatherSel
            soundSel = TerminalControl.soundSel
            emerChance = TerminalControl.emerChance
            distToGoVisible = TerminalControl.distToGoVisible
            showSectorBoundary = TerminalControl.showSectorBoundary
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

    constructor(game: TerminalControl, save: JSONObject, loadGameScreen: LoadGameScreen) : super(game) {
        this.loadGameScreen = loadGameScreen

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
        information = save.optInt("information", MathUtils.random(65, 90)).toChar()
        loadStageCamTimer()

        //Set timer for radar delay, trails and autosave
        radarTime = save.optDouble("radarTime", TerminalControl.radarSweep.toDouble()).toFloat()
        trailTime = save.optDouble("trailTime", 10.0).toFloat()
        saveTime = TerminalControl.saveInterval.toFloat()
        rpcTime = 60f
        thunderCellTime = 10f
        stormSpawnTime = save.optDouble("stormSpawnTime", 0.0).toFloat()
        stormNumber = save.optInt("stormNumber", 0)
        trajectoryLine = save.optInt("trajectoryLine", 90)
        pastTrajTime = save.optInt("pastTrajTime", -1)
        radarSweepDelay = save.optDouble("radarSweep", 2.0).toFloat()
        val maxTraj = if (UnlockManager.trajAvailable.last() == "Off") -1 else UnlockManager.trajAvailable.last().split(" ")[0].toInt()
        val maxArea = if (UnlockManager.areaAvailable.last() == "Off") -1 else UnlockManager.areaAvailable.last().split(" ")[0].toInt()
        val maxCollision = if (UnlockManager.collisionAvailable.last() == "Off") -1 else UnlockManager.collisionAvailable.last().split(" ")[0].toInt()
        advTraj = if (TerminalControl.full) save.optInt("advTraj", -1).coerceAtMost(maxTraj) else -1
        areaWarning = if (TerminalControl.full) save.optInt("areaWarning", -1).coerceAtMost(maxArea) else -1
        collisionWarning = if (TerminalControl.full) save.optInt("collisionWarning", -1).coerceAtMost(maxCollision) else -1
        showMva = save.optBoolean("showMva", true)
        showIlsDash = save.optBoolean("showIlsDash", false)
        datatagConfig = DataTagConfig(save.optString("datatagConfig", if (save.optBoolean("compactData", false)) "Compact" else "Default"))
        if (!TerminalControl.datatagConfigs.contains(datatagConfig.name, false)) datatagConfig = DataTagConfig("Default")
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
        soundSel = save.optInt("sounds", 2)
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
        distToGoVisible = save.optInt("distToGoVisible", 0)
        showSectorBoundary = save.optBoolean("showSectorBoundary", true)
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
        stage = SafeStage(ScalingViewport(Scaling.fillY, 5760f, 3240f), game.batch)
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
        labelStage = SafeStage(ScalingViewport(Scaling.fillY, 5760f, 3240f), game.batch)
        labelStage.viewport.camera = camera
        labelStage.viewport.update(TerminalControl.WIDTH, TerminalControl.HEIGHT, true)
    }

    private fun loadInputProcessors() {
        //Set input processors
        gd = GestureDetector(40f, 0.2f, 1.1f, 0.15f, this)
        inputMultiplexer.addProcessor(uiStage)
        inputMultiplexer.addProcessor(labelStage)
        inputMultiplexer.addProcessor(gd)
        inputMultiplexer.addProcessor(this)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun loadPanel() {
        //Set 2nd stage, camera for UI
        uiStage = SafeStage(ExtendViewport(1920f, 3240f), game.batch)
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
            if (save == null) airport.loadOthers()
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
    fun newDeparture(callsign: String, icaoType: String, airport: Airport, runway: Runway, sid: Sid?) {
        val newSid = sid ?: RandomSID.randomSID(airport, runway.name) ?: return
        aircrafts[callsign] = Departure(callsign, icaoType, airport, runway, newSid)
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
        val multiThreadGenerator = ArrivalGenerator(this, allAircraft)
        generatorList.add(multiThreadGenerator)
        Thread(multiThreadGenerator).start()
    }

    /** Loads the full UI for RadarScreen  */
    private fun loadUI() {
        //Reset stage
        stage.clear()

        //Show loading screen
        metarLoading = true
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
        handoverController = HandoverController()

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

        //Load distance label
        val labelStyle = Label.LabelStyle()
        labelStyle.fontColor = Color.WHITE
        labelStyle.font = Fonts.defaultFont10
        distLabel = Label("", labelStyle)
        stage.addActor(distLabel)

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

        //Timer for updating radar info
        radarTime -= deltaTime
        if (radarTime <= 0) {
            updateRadarInfo()
            radarTime += if (radarSweepDelay / speed < 0.25f) {
                0.25f * speed
            } else {
                radarSweepDelay
            }
        }

        //Timer for drawing new trail dots
        trailTime -= deltaTime
        if (trailTime <= 0) {
            addTrailDot()
            trailTime += 10f
        }

        //Timer for updating discord RPC
        rpcTime -= deltaTime
        if (rpcTime <= 0) {
            TerminalControl.discordManager.updateRPC()
            rpcTime += 60f
        }

        //Timer for updating thunder cell states
        thunderCellTime -= deltaTime
        if (thunderCellTime <= 0) {
            while (thunderCellArray.size > stormNumber) thunderCellArray.pop()
            for ((index, cell) in Array(thunderCellArray).withIndex()) {
                cell.update()
                if (cell.canBeDeleted()) thunderCellArray.removeIndex(index)
            }
            thunderCellTime += 10
        }

        //Timer for spawning new storms
        stormSpawnTime -= deltaTime
        if (stormSpawnTime <= 0) {
            if (thunderCellArray.size < stormNumber) thunderCellArray.add(ThunderCell(null))
            //Minimum 30s, maximum 120s spawn time
            stormSpawnTime += (15 - (stormNumber - thunderCellArray.size)).coerceAtLeast(3).coerceAtMost(12) * 10
        }

        if (!tutorial) {
            //Timer for autosaving
            if (TerminalControl.saveInterval > 0) {
                //If autosave enabled
                saveTime -= deltaTime
                if (saveTime <= 0) {
                    GameSaver.saveGame()
                    saveTime += TerminalControl.saveInterval.toFloat()
                }
            }

            //Timer for spawning new arrivals
            arrivals = 0
            for (aircraft in aircrafts.values) {
                if (aircraft is Arrival && aircraft.controlState == Aircraft.ControlState.ARRIVAL) arrivals++
            }
            spawnTimer -= deltaTime
            if (spawnTimer <= 0 && arrivals < planesToControl) {
                //Minimum 50 sec interval between each new plane
                newArrival()
            }
            checkGenerators()
        }

        //Checking parallel landing achievement
        if (!UnlockManager.unlocks.contains("parallelLanding")) {
            //Update simultaneous landing linkedHashMap
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
            if (generator == null) {
                generatorIterator.remove()
                continue
            }
            if (generator.done) {
                //If generator is done generating, copy info to local variables and create a new arrival from them
                generatorIterator.remove() //Remove the generator since it's no longer needed
                val aircraftInfo = generator.aircraftInfo ?: continue
                val finalAirport = generator.finalAirport ?: continue
                if (allAircraft.contains(aircraftInfo[0])) {
                    spawnTimer = 5f //If by coincidence 2 generators somehow give the same result before either was added, don't add, instead generate another arrival in 5 seconds
                    continue
                }

                val star = RandomSTAR.randomSTAR(finalAirport) ?: continue
                val arrival = Arrival(aircraftInfo[0], aircraftInfo[1], finalAirport, star)
                aircrafts[aircraftInfo[0]] = arrival
                arrivals++
                allAircraft.add(aircraftInfo[0])
            } else {
                if (generator.cycles >= 100) {
                    generatorIterator.remove()
                    continue
                }
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
        shapeRenderer.end()
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)

        //Updates trajectory, aircraft separation status
        separationChecker.update()
        trajectoryStorage.update()

        //Shows the info if any (full version only)
        if (TerminalControl.full) {
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
            if (waypoint.isFlyOver()) {
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

        if (showSectorBoundary) {
            shapeRenderer.color = RangeCircle.DARK_GREEN
            shapeRenderer.line(1260f, 0f, 1260f, 3240f)
            shapeRenderer.line(4500f, 0f, 4500f, 3240f)
        }

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
        selectedAircraft?.let {
            wakeManager.renderWake(it)
        }
        wakeManager.renderIlsWake()
        drawDistPoints()
        shapeRenderer.end()
    }

    override fun drawWeatherCells() {
        game.batch.begin()
        //Draws the weather cells
        for (cell in thunderCellArray) {
            cell.renderShape()
        }
        game.batch.end()
    }

    private fun drawDistPoints() {
        if (!dragging) {
            distLabel.isVisible = false
            return
        }
        shapeRenderer.color = Color.WHITE
        shapeRenderer.circle(firstPoint.x, firstPoint.y, 10f)
        shapeRenderer.circle(secondPoint.x, secondPoint.y, 10f)
        shapeRenderer.line(firstPoint, secondPoint)
        distLabel.isVisible = true
        distLabel.setText((round(MathTools.pixelToNm(MathTools.distanceBetween(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y)) * 10) / 10).toString())
        distLabel.setPosition((firstPoint.x + secondPoint.x - distLabel.prefWidth) / 2, (firstPoint.y + secondPoint.y) / 2)
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
            if (arrival.apch == null) return false
            val rwy1 = arrival.apch?.rwy?.name
            if (aircraft.apch == null) continue
            val rwy2 = aircraft.apch?.rwy?.name
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) && !tutorial && finishedLoading) {
            //On android, change to pause screen if not paused, un-pause if paused
            setGameRunning(!running)
        }
        super.render(delta)
    }

    override fun show() {
        //Implements show method of screen, loads UI & save (if available) after show is called if it hasn't been done
        if (!uiLoaded) {
            Ui.generatePaneTextures()
            DataTag.LOADED_ICONS = false
            StormIntensity.LOADED_ICONS = false
            Tab.LOADED_STYLES = false
            loadUI()
            val newThread = Thread {
                try {
                    GameLoader.loadSaveData(save)
                    //If loaded successfully, clear error sent, incompatible flag
                    save?.put("errorSent", false)
                    save?.put("incompatible", false)
                    GameSaver.writeObjectToFile(save, save?.getInt("saveId") ?: -1)
                    updateWaypointDisplay()
                    TerminalControl.discordManager.updateRPC()
                    //Thread.sleep(100)
                    uiLoaded = true
                    loadGameScreen = null
                } catch (e: Exception) {
                    Gdx.app.postRunnable { save?.let { loadGameScreen?.handleSaveLoadError(this, it, e) } }
                }
            }
            newThread.start()
        }
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
                if (aircraft is Departure && aircraft.isArrivalDeparture) {
                    //If aircraft is a departure, and is in your control
                    count++
                }
            }
            return count
        }

    fun updateInformation() {
        information++
        if (information.code > 90) information -= 26
        utilityBox.commsManager.normalMsg("Information $information is now current")
    }
}