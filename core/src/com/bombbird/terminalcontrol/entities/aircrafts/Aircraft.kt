package com.bombbird.terminalcontrol.entities.aircrafts

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.incrementWakeConflictTime
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.approaches.ILS
import com.bombbird.terminalcontrol.entities.approaches.LDA
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.separation.trajectory.Trajectory
import com.bombbird.terminalcontrol.entities.sidstar.Route
import com.bombbird.terminalcontrol.entities.sidstar.SidStar
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.ui.DataTag
import com.bombbird.terminalcontrol.ui.Ui
import com.bombbird.terminalcontrol.ui.tabs.Tab
import com.bombbird.terminalcontrol.utilities.RenameManager.renameAirportICAO
import com.bombbird.terminalcontrol.utilities.SafeStage
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.iasToTas
import com.bombbird.terminalcontrol.utilities.math.MathTools.modulateHeading
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToFeet
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToPixel
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import com.bombbird.terminalcontrol.utilities.math.MathTools.pointsAtBorder
import org.json.JSONObject
import kotlin.math.*

abstract class Aircraft : Actor {
    companion object {
        //Request types
        const val NO_REQUEST = -1
        const val HIGH_SPEED_REQUEST = 0
        const val SHORTCUT_REQUEST = 1
    }

    enum class ControlState {
        UNCONTROLLED, ARRIVAL, DEPARTURE, ENROUTE
    }

    //Android text-to-speech
    val voice: String
    val radarScreen: RadarScreen = TerminalControl.radarScreen!!
    private val stage = radarScreen.stage
    val shapeRenderer = radarScreen.shapeRenderer
    val ui = radarScreen.ui
    lateinit var dataTag: DataTag
        private set
    private lateinit var color: Color
    var isSelected = false
    var isActionRequired = false
    var isFuelEmergency = false

    //Aircraft information
    var airport: Airport
    var runway: Runway? = null
    var isOnGround = false
    var isTkOfLdg: Boolean
    val trajectory: Trajectory
    var isTrajectoryConflict: Boolean
    var isTrajectoryTerrainConflict: Boolean
    var stormWarningTime: Float

    //Aircraft characteristics
    var callsign: String
    val icaoType: String
    val wakeCat: Char
    val recat: Char
    var isWakeInfringe: Boolean
        private set
    var wakeTolerance: Float
    var v2: Int
    var typClimb: Int
    var maxClimb: Int
    var typDes: Int
    val maxDes: Int
    val apchSpd: Int
    lateinit var controlState: ControlState
        private set
    lateinit var navState: NavState
    var isGoAround: Boolean
    var isGoAroundWindow: Boolean
        set(value) {
            field = value
            if (value) {
                goAroundTime = 120f
            }
        }
    var goAroundTime: Float
        private set
    var isConflict: Boolean
    var isWarning: Boolean
    var isTerrainConflict = false
    var isStormConflict = false
    var isPrevConflict: Boolean
    var isSilenced = false
    val emergency: Emergency

    //Additional requests
    var request = NO_REQUEST
    var isRequested = false
    var requestAlt = -1

    //Aircraft position
    private var x = 0f
    private var y = 0f
    var heading: Double
    var targetHeading: Double
    var clearedHeading: Int
    var angularVelocity = 0.0
        private set
    var track: Double
    lateinit var route: Route
    abstract val sidStar: SidStar
    var sidStarIndex: Int
    var direct: Waypoint? = null
    var afterWaypoint: Waypoint? = null
    var afterWptHdg: Int
    var ils: ILS? = null
        private set
    var isLocCap: Boolean
        private set
    var holdWpt: Waypoint? = null
        get() {
            if (field == null && navState.dispLatMode.last() == NavState.HOLD_AT) field = radarScreen.waypoints[navState.clearedHold.last()?.name]
            return field
        }
    var isHolding = false
        private set
    var holdingType: Int
        private set
    var isInit = false
        private set
    var isType1leg = false
        private set
    var holdTargetPt: Array<FloatArray>? = null
        private set
    var holdTargetPtSelected: BooleanArray? = null
        private set
    var prevDistTravelled: Float
        private set

    //Altitude
    var prevAlt = 0f
        private set
    var altitude: Float
    var clearedAltitude: Int
        private set
    var targetAltitude: Int
    var verticalSpeed: Float
    var isExpedite: Boolean
    var expediteTime = 0f
        private set
    var lowestAlt = 0
    var highestAlt = 0
    var isGsCap: Boolean

    //Speed
    var ias: Float
    var tas: Float
        private set
    var gs: Float
    val deltaPosition: Vector2
    var clearedIas: Int
        private set
    var deltaIas: Float
        private set
    val climbSpd: Int

    //Radar returns (for sweep delay)
    var radarX = 0f
        private set
    var radarY = 0f
        private set
    var radarHdg = 0.0
    var radarTrack = 0.0
        private set
    var radarGs = 0f
        private set
    var radarAlt = 0f
        private set
    var radarVs = 0f
        private set

    constructor(callsign: String, icaoType: String, airport: Airport) {
        this.callsign = callsign
        stage.addActor(this)
        this.icaoType = icaoType
        wakeCat = AircraftType.getWakeCat(icaoType)
        recat = AircraftType.getRecat(icaoType)
        isWakeInfringe = false
        wakeTolerance = 0f
        val loadFactor = MathUtils.random(-1, 1) / 5f
        v2 = (AircraftType.getV2(icaoType) * (1 + loadFactor)).toInt()
        typClimb = (AircraftType.getTypClimb(icaoType) * (1 - loadFactor)).toInt()
        maxClimb = typClimb + 800
        typDes = (AircraftType.getTypDes(icaoType) * (1 - loadFactor) * 0.9).toInt()
        maxDes = typDes + 800
        apchSpd = (AircraftType.getApchSpd(icaoType) * (1 + loadFactor / 8)).toInt()
        this.airport = airport
        if (airport.landingRunways.size == 0) {
            radarScreen.airports[radarScreen.mainName]?.let {
                //No landing runways available at departure airport, land at main airport instead
                this.airport = airport
            }
        }
        heading = 0.0
        targetHeading = 0.0
        clearedHeading = heading.toInt()
        track = 0.0
        sidStarIndex = 0
        afterWptHdg = 360
        altitude = 10000f
        clearedAltitude = 10000
        targetAltitude = 10000
        verticalSpeed = 0f
        isExpedite = false
        expediteTime = 0f
        ias = 250f
        tas = iasToTas(ias, altitude)
        gs = tas
        deltaPosition = Vector2()
        prevDistTravelled = 0f
        clearedIas = 250
        deltaIas = 0f
        isTkOfLdg = false
        isGsCap = false
        isLocCap = false
        holdingType = 0
        climbSpd = if (AircraftType.getMaxCruiseSpd(icaoType) == -1) MathUtils.random(270, 280) else AircraftType.getMaxCruiseSpd(icaoType)
        isGoAround = false
        isGoAroundWindow = false
        goAroundTime = 0f
        isConflict = false
        isWarning = false
        isTerrainConflict = false
        isStormConflict = false
        isPrevConflict = false
        isSilenced = false
        emergency = Emergency(this, radarScreen.emerChance)
        radarScreen.wakeManager.addAircraft(callsign)
        voice = TerminalControl.tts.getRandomVoice()
        trajectory = Trajectory(this)
        isTrajectoryConflict = false
        isTrajectoryTerrainConflict = false
        stormWarningTime = 65f
    }

    /** Constructs aircraft from another aircraft  */
    constructor(aircraft: Aircraft) {
        callsign = aircraft.callsign
        stage.addActor(this)
        icaoType = aircraft.icaoType
        wakeCat = aircraft.wakeCat
        recat = aircraft.recat
        isWakeInfringe = aircraft.isWakeInfringe
        wakeTolerance = aircraft.wakeTolerance
        v2 = aircraft.v2
        typClimb = aircraft.typClimb
        maxClimb = aircraft.maxClimb
        typDes = aircraft.typDes
        maxDes = aircraft.maxDes
        apchSpd = aircraft.apchSpd
        airport = aircraft.airport
        heading = aircraft.heading
        targetHeading = aircraft.targetHeading
        clearedHeading = aircraft.clearedHeading
        track = aircraft.track
        sidStarIndex = aircraft.sidStarIndex
        afterWptHdg = aircraft.afterWptHdg
        altitude = aircraft.altitude
        clearedAltitude = aircraft.clearedAltitude
        targetAltitude = aircraft.targetAltitude
        verticalSpeed = aircraft.verticalSpeed
        isExpedite = aircraft.isExpedite
        expediteTime = aircraft.expediteTime
        ias = aircraft.ias
        tas = aircraft.tas
        gs = aircraft.gs
        deltaPosition = aircraft.deltaPosition
        prevDistTravelled = aircraft.prevDistTravelled
        clearedIas = aircraft.clearedIas
        deltaIas = aircraft.deltaIas
        isTkOfLdg = aircraft.isTkOfLdg
        isGsCap = aircraft.isGsCap
        isLocCap = aircraft.isLocCap
        holdingType = aircraft.holdingType
        climbSpd = aircraft.climbSpd
        isGoAround = aircraft.isGoAround
        isGoAroundWindow = aircraft.isGoAroundWindow
        goAroundTime = aircraft.goAroundTime
        isConflict = aircraft.isConflict
        isWarning = aircraft.isWarning
        isTerrainConflict = aircraft.isTerrainConflict
        isStormConflict = aircraft.isStormConflict
        isPrevConflict = aircraft.isPrevConflict
        isSilenced = aircraft.isSilenced
        emergency = aircraft.emergency
        radarAlt = aircraft.radarAlt
        radarGs = aircraft.radarGs
        radarHdg = aircraft.radarHdg
        radarTrack = aircraft.radarTrack
        radarVs = aircraft.radarVs
        radarX = aircraft.radarX
        radarY = aircraft.radarY
        isActionRequired = aircraft.isActionRequired
        route = aircraft.route
        x = aircraft.x
        y = aircraft.y
        angularVelocity = aircraft.angularVelocity
        isOnGround = aircraft.isOnGround
        navState = aircraft.navState
        navState.aircraft = this
        voice = aircraft.voice
        trajectory = Trajectory(this)
        isTrajectoryConflict = false
        isTrajectoryTerrainConflict = false
        stormWarningTime = aircraft.stormWarningTime
        request = NO_REQUEST
        isRequested = false
        requestAlt = -1
    }

    constructor(save: JSONObject) {
        airport = radarScreen.airports[renameAirportICAO(save.getString("airport"))]!!
        runway = if (save.isNull("runway")) null else airport.runways[save.getString("runway")]
        isOnGround = save.getBoolean("onGround")
        isTkOfLdg = save.getBoolean("tkOfLdg")
        callsign = save.getString("callsign")
        stage.addActor(this)
        icaoType = save.getString("icaoType")
        wakeCat = save.getString("wakeCat")[0]
        recat = if (save.isNull("recat")) AircraftType.getRecat(icaoType) else save.getInt("recat").toChar()
        isWakeInfringe = save.optBoolean("wakeInfringe")
        wakeTolerance = save.optDouble("wakeTolerance", 0.0).toFloat()
        v2 = save.getInt("v2")
        typClimb = save.getInt("typClimb")
        maxClimb = save.getInt("maxClimb")
        typDes = save.getInt("typDes")
        maxDes = save.getInt("maxDes")
        apchSpd = save.getInt("apchSpd")
        navState = NavState(this, save.getJSONObject("navState"))
        isGoAround = save.getBoolean("goAround")
        isGoAroundWindow = save.getBoolean("goAroundWindow")
        goAroundTime = save.getDouble("goAroundTime").toFloat()
        isConflict = save.getBoolean("conflict")
        isWarning = save.getBoolean("warning")
        isTerrainConflict = save.optBoolean("terrainConflict", false)
        isStormConflict = save.optBoolean("stormConflict", false)
        isPrevConflict = isConflict
        emergency = if (save.optJSONObject("emergency") == null) Emergency(this, false) else Emergency(this, save.getJSONObject("emergency"))
        request = save.optInt("request", NO_REQUEST)
        isRequested = save.optBoolean("requested", false)
        requestAlt = save.optInt("requestAlt", -1)
        x = save.getDouble("x").toFloat()
        y = save.getDouble("y").toFloat()
        prevDistTravelled = save.optDouble("prevDistTravelled", 0.0).toFloat()
        heading = save.getDouble("heading")
        targetHeading = save.getDouble("targetHeading")
        clearedHeading = save.getInt("clearedHeading")
        angularVelocity = save.getDouble("angularVelocity")
        track = save.getDouble("track")
        sidStarIndex = save.getInt("sidStarIndex")
        direct = if (save.isNull("direct")) null else radarScreen.waypoints[save.getString("direct")]
        afterWaypoint = if (save.isNull("afterWaypoint")) null else radarScreen.waypoints[save.getString("afterWaypoint")]
        afterWptHdg = save.getInt("afterWptHdg")
        ils = if (save.isNull("ils")) null else airport.approaches[save.getString("ils").substring(3)]
        isLocCap = save.getBoolean("locCap")
        holdWpt = if (save.isNull("holdWpt")) null else radarScreen.waypoints[save.getString("holdWpt")]
        isHolding = save.getBoolean("holding")
        holdingType = save.optInt("holdingType", 0)
        isInit = save.getBoolean("init")
        isType1leg = save.getBoolean("type1leg")
        if (save.isNull("holdTargetPt")) {
            //Null holding arrays
            holdTargetPt = null
            holdTargetPtSelected = null
        } else {
            //Not null
            val the2points = save.getJSONArray("holdTargetPt")
            holdTargetPt = Array(2) { FloatArray(2) }
            for (i in 0 until the2points.length()) {
                val coordinates = the2points.getJSONArray(i)
                holdTargetPt?.let {
                    it[i][0] = coordinates.getDouble(0).toFloat()
                    it[i][1] = coordinates.getDouble(1).toFloat()
                }
            }
            val the2bools = save.getJSONArray("holdTargetPtSelected")
            holdTargetPtSelected = BooleanArray(2)
            holdTargetPtSelected?.let {
                it[0] = the2bools.getBoolean(0)
                it[1] = the2bools.getBoolean(1)
            }
        }
        prevAlt = save.getDouble("prevAlt").toFloat()
        altitude = save.getDouble("altitude").toFloat()
        clearedAltitude = save.getInt("clearedAltitude")
        targetAltitude = save.getInt("targetAltitude")
        verticalSpeed = save.getDouble("verticalSpeed").toFloat()
        isExpedite = save.getBoolean("expedite")
        expediteTime = save.optDouble("expediteTime", 0.0).toFloat()
        lowestAlt = save.getInt("lowestAlt")
        highestAlt = save.getInt("highestAlt")
        isGsCap = save.getBoolean("gsCap")
        ias = save.getDouble("ias").toFloat()
        tas = save.getDouble("tas").toFloat()
        gs = save.getDouble("gs").toFloat()
        val delta = save.getJSONArray("deltaPosition")
        deltaPosition = Vector2()
        deltaPosition.x = delta.getDouble(0).toFloat()
        deltaPosition.y = delta.getDouble(1).toFloat()
        clearedIas = save.getInt("clearedIas")
        deltaIas = save.getDouble("deltaIas").toFloat()
        climbSpd = save.getInt("climbSpd")
        radarX = save.getDouble("radarX").toFloat()
        radarY = save.getDouble("radarY").toFloat()
        radarHdg = save.getDouble("radarHdg")
        radarTrack = save.getDouble("radarTrack")
        radarGs = save.getDouble("radarGs").toFloat()
        radarAlt = save.getDouble("radarAlt").toFloat()
        radarVs = save.getDouble("radarVs").toFloat()
        voice = save.optString("voice", TerminalControl.tts.getRandomVoice())
        trajectory = Trajectory(this)
        isTrajectoryConflict = false
        isTrajectoryTerrainConflict = false
        stormWarningTime = save.optDouble("stormWarningTime", 65.0).toFloat()

        loadLabel()
        updateControlState(when (val control = save.optString("controlState")) {
            "0" -> ControlState.UNCONTROLLED
            "1" -> ControlState.ARRIVAL
            "2" -> ControlState.DEPARTURE
            else -> ControlState.valueOf(control)
        })
    }

    /** Sets the initial radar position for aircraft  */
    fun initRadarPos() {
        radarX = x
        radarY = y
        radarHdg = heading
        radarTrack = track
        radarGs = gs
        radarAlt = altitude
        radarVs = verticalSpeed
    }

    /** Loads the aircraft data labels  */
    fun loadLabel() {
        dataTag = DataTag(this)
    }

    /** Loads other aircraft data label info  */
    fun loadOtherLabelInfo(save: JSONObject) {
        val trails = save.getJSONArray("trailDots")
        for (i in 0 until trails.length()) {
            dataTag.addTrailDot(trails.getJSONArray(i).getDouble(0).toFloat(), trails.getJSONArray(i).getDouble(1).toFloat())
        }
        if (!save.isNull("labelPos")) {
            val labelPos = save.getJSONArray("labelPos")
            dataTag.setLabelPosition(labelPos.getDouble(0).toFloat(), labelPos.getDouble(1).toFloat())
        }
        dataTag.isMinimized = save.optBoolean("dataTagMin", false)
        isFuelEmergency = save.optBoolean("fuelEmergency", save.optBoolean("emergency", false))
        if (hasEmergency()) dataTag.setEmergency()
        isActionRequired = save.optBoolean("actionRequired", false)
        if (isActionRequired) dataTag.startFlash()
    }

    /** Renders shapes using shapeRenderer; all rendering should be called here  */
    fun renderShape() {
        drawLatLines()
        dataTag.moderateLabel()
        shapeRenderer.color = Color.WHITE
        dataTag.renderShape()
        if (TerminalControl.full) trajectory.renderPoints()
        if (isArrivalDeparture) {
            shapeRenderer.color = color
            shapeRenderer.line(radarX, radarY, radarX + radarScreen.trajectoryLine / 3600f * nmToPixel(radarGs) * cos(Math.toRadians(90 - radarTrack)).toFloat(), radarY + radarScreen.trajectoryLine / 3600f * nmToPixel(radarGs) * sin(Math.toRadians(90 - radarTrack)).toFloat())
        }
    }

    /** Draws the lines displaying the lateral status of aircraft  */
    private fun drawLatLines() {
        if (isSelected) {
            //Draws cleared status
            if (navState.dispLatMode.last() == NavState.SID_STAR && navState.clearedDirect.last() != null) {
                drawSidStar()
            } else if (navState.dispLatMode.last() == NavState.AFTER_WPT_HDG && navState.clearedDirect.last() != null && navState.clearedAftWpt.last() != null) {
                drawAftWpt()
            } else if (navState.containsCode(navState.dispLatMode.last(), NavState.VECTORS) && (!isLocCap || navState.clearedIls.last() == null)) {
                drawHdgLine()
            } else if (navState.dispLatMode.last() == NavState.HOLD_AT) {
                drawHoldPattern()
            }

            //Draws selected status (from UI)
            if (isArrivalDeparture) {
                if (Tab.latMode == NavState.SID_STAR && (ui.latTab.isWptChanged || ui.latTab.isLatModeChanged)) {
                    uiDrawSidStar()
                } else if (Tab.latMode == NavState.AFTER_WPT_HDG && (ui.latTab.isAfterWptChanged || ui.latTab.isAfterWptHdgChanged || ui.latTab.isLatModeChanged)) {
                    uiDrawAftWpt()
                } else if (Tab.latMode == NavState.VECTORS && (this is Departure || Ui.NOT_CLEARED_APCH == Tab.clearedILS || !isLocCap) && (ui.latTab.isHdgChanged || ui.latTab.isLatModeChanged)) {
                    uiDrawHdgLine()
                } else if (Tab.latMode == NavState.HOLD_AT && (ui.latTab.isLatModeChanged || ui.latTab.isHoldWptChanged)) {
                    uiDrawHoldPattern()
                }
            }
        }
    }

    /** Draws the cleared sidStar when selected  */
    open fun drawSidStar() {
        shapeRenderer.color = radarScreen.defaultColour
        navState.clearedDirect.last()?.let {
            shapeRenderer.line(radarX, radarY, it.posX.toFloat(), it.posY.toFloat())
            route.joinLines(route.findWptIndex(it.name), route.waypoints.size, -1)
            calculateAndSetDistToGo(it, route.waypoints[route.waypoints.size - 1])
        }
        //route.drawPolygons();
    }

    /** Draws the sidStar for the UI  */
    open fun uiDrawSidStar() {
        shapeRenderer.color = Color.YELLOW
        radarScreen.waypoints[Tab.clearedWpt]?.let {
            shapeRenderer.line(radarX, radarY, it.posX.toFloat(), it.posY.toFloat())
            calculateAndSetDistToGo(it, route.waypoints[route.waypoints.size - 1])
        }
        route.joinLines(route.findWptIndex(Tab.clearedWpt), route.waypoints.size, -1)
    }

    /** Draws the cleared after waypoint + cleared outbound heading when selected  */
    open fun drawAftWpt() {
        shapeRenderer.color = radarScreen.defaultColour
        navState.clearedDirect.last()?.let {
            shapeRenderer.line(radarX, radarY, it.posX.toFloat(), it.posY.toFloat())
        }
    }

    /** Draws the after waypoint + outbound heading for UI  */
    open fun uiDrawAftWpt() {
        shapeRenderer.color = Color.YELLOW
        radarScreen.waypoints[Tab.clearedWpt]?.let {
            shapeRenderer.line(radarX, radarY, it.posX.toFloat(), it.posY.toFloat())
        }
    }

    /** Draws the cleared heading when selected  */
    private fun drawHdgLine() {
        shapeRenderer.color = radarScreen.defaultColour
        val point = pointsAtBorder(floatArrayOf(1260f, 4500f), floatArrayOf(0f, 3240f), radarX, radarY, navState.clearedHdg.last() - radarScreen.magHdgDev)
        shapeRenderer.line(radarX, radarY, point[0], point[1])
    }

    /** Draws the heading for the UI  */
    private fun uiDrawHdgLine() {
        shapeRenderer.color = Color.YELLOW
        val point = pointsAtBorder(floatArrayOf(1260f, 4500f), floatArrayOf(0f, 3240f), radarX, radarY, Tab.clearedHdg - radarScreen.magHdgDev)
        shapeRenderer.line(radarX, radarY, point[0], point[1])
    }

    /** Draws the cleared holding pattern when selected  */
    open fun drawHoldPattern() {
        shapeRenderer.color = radarScreen.defaultColour
        if (!isHolding) direct?.let {
            shapeRenderer.line(radarX, radarY, it.posX.toFloat(), it.posY.toFloat())
        }
        navState.clearedHold.last()?.let { route.holdProcedure.renderShape(it) }
    }

    /** Draws the holding pattern for the UI  */
    open fun uiDrawHoldPattern() {
        shapeRenderer.color = Color.YELLOW
        radarScreen.waypoints[Tab.clearedWpt]?.let {
            shapeRenderer.line(radarX, radarY, it.posX.toFloat(), it.posY.toFloat())
        }
        radarScreen.waypoints[Tab.holdWpt]?.let { route.holdProcedure.renderShape(it) }
    }

    /** Returns whether the dist to go should be calculated and displayed */
    fun eligibleDisplayDistToGo(): Boolean {
        return isSelected && ((radarScreen.distToGoVisible == 1 && this is Arrival) ||
                (radarScreen.distToGoVisible == 2 && this is Departure) ||
                radarScreen.distToGoVisible == 3)
    }

    /** Calculates the distance to go for each waypoint, sets the distToGo variable for those waypoints */
    fun calculateAndSetDistToGo(nextWpt: Waypoint, lastWpt: Waypoint) {
        if (!eligibleDisplayDistToGo()) return
        var currentIndex = route.findWptIndex(nextWpt.name)
        val lastIndex = route.findWptIndex(lastWpt.name)
        if (currentIndex == -1 || lastIndex == -1) return
        var cumulativeDist = pixelToNm(distanceBetween(radarX, radarY, nextWpt.posX.toFloat(), nextWpt.posY.toFloat()))
        nextWpt.distToGo = cumulativeDist
        nextWpt.distToGoVisible = true
        while (currentIndex < lastIndex) {
            cumulativeDist += route.distBetween(currentIndex, currentIndex + 1)
            route.getWaypoint(currentIndex + 1)?.let {
                it.distToGo = cumulativeDist
                it.distToGoVisible = true
            }
            currentIndex++
        }
    }

    /** The main update function  */
    open fun update(): Double {
        navState.updateTime()
        tas = iasToTas(ias, altitude)
        updateIas()
        if (isTkOfLdg) {
            updateTkofLdg()
        }
        return if (!isOnGround) {
            val info = updateTargetHeading()
            targetHeading = info[0]
            updateHeading(targetHeading)
            updatePosition(info[1])
            updateAltitude()
            updateSpd()
            if (isGoAround) {
                updateGoAround()
            }
            if (isGoAroundWindow) {
                goAroundTime -= Gdx.graphics.deltaTime
                if (goAroundTime < 0) {
                    isGoAroundWindow = false
                    goAroundTime = 0f
                }
            }
            if (stormWarningTime >= 0 && stormWarningTime < 61) {
                //Storm warning is active
                stormWarningTime -= Gdx.graphics.deltaTime
                if (stormWarningTime < 0) {
                    isActionRequired = true
                    dataTag.startFlash()
                    ui.updateAckHandButton(this)
                    radarScreen.utilityBox.commsManager.requestHeadingForWeather(this, isLocCap)
                }
            }
            emergency.update()
            targetHeading
        } else {
            gs = tas - airport.winds[1] * cos(Math.toRadians(airport.winds[0].toDouble() - (runway?.heading?.toDouble() ?: 0.0))).toFloat()
            if (tas == 0f || gs < 0) gs = 0f
            updatePosition(0.0)
            emergency.update()
            0.0
        }
    }

    /** Overridden method that updates the target speed of the aircraft depending on situation  */
    open fun updateSpd() {
        navState.clearedSpd.removeFirst()
        navState.clearedSpd.addFirst(clearedIas)
        if (isSelected && isArrivalDeparture) {
            updateUISelections()
            ui.updateState()
        }
    }

    /** Overridden method for arrival/departure  */
    open fun updateTkofLdg() {
        //No default implementation
    }

    /** Overridden method for arrivals during go around  */
    open fun updateGoAround() {
        //No default implementation
    }

    /** Updates the aircraft speed  */
    private fun updateIas() {
        val targetDeltaIas = (clearedIas - ias) / 5
        when {
            targetDeltaIas > deltaIas + 0.05 -> deltaIas += 0.2f * Gdx.graphics.deltaTime
            targetDeltaIas < deltaIas - 0.05 -> deltaIas -= 0.2f * Gdx.graphics.deltaTime
            else -> deltaIas = targetDeltaIas
        }
        val max: Float
        val min: Float
        if (isTkOfLdg) {
            max = 3f
            min = if (gs >= 60) {
                -4.5f
            } else {
                -1.5f
            }
        } else {
            max = 1.5f * (1 - verticalSpeed / maxClimb)
            min = -1.5f * (1 + verticalSpeed / maxDes)
        }
        if (deltaIas > max) {
            deltaIas = max
        } else if (deltaIas < min) {
            deltaIas = min
        }
        ias += deltaIas * Gdx.graphics.deltaTime
        if (abs(clearedIas - ias) < 1) {
            ias = clearedIas.toFloat()
        }
    }

    val effectiveVertSpd: FloatArray
        get() {
            if (isGsCap) return floatArrayOf(-nmToFeet(gs / 60) * sin(Math.toRadians(3.0)).toFloat(), typClimb.toFloat())
            var multiplier = 1f
            if (typClimb <= 2200) multiplier = 1.1f
            if (altitude > 20000) multiplier -= 0.2f
            return if (!isExpedite) floatArrayOf(-typDes * multiplier, typClimb * multiplier) else floatArrayOf(-maxDes * multiplier, maxClimb * multiplier)
        }

    open fun updateAltitude(holdAlt: Boolean = false, fixedVs: Boolean = false) {
        var targetVertSpd = 0f
        if (!holdAlt) targetVertSpd = (targetAltitude - altitude) / 0.1f
        if (fixedVs) targetVertSpd = verticalSpeed
        if (targetVertSpd > verticalSpeed + 100) {
            verticalSpeed += 300 * Gdx.graphics.deltaTime
        } else if (targetVertSpd < verticalSpeed - 100) {
            verticalSpeed -= 300 * Gdx.graphics.deltaTime
        }
        val range = effectiveVertSpd
        verticalSpeed = MathUtils.clamp(verticalSpeed, range[0], range[1])
        expediteTime += if (isExpedite) Gdx.graphics.deltaTime else 0f
        altitude += verticalSpeed / 60 * Gdx.graphics.deltaTime
        if (abs(targetAltitude - altitude) < 50 && abs(verticalSpeed) < 200) {
            altitude = targetAltitude.toFloat()
            verticalSpeed = 0f
            if (navState.clearedExpedite.first()) {
                navState.clearedExpedite.removeFirst()
                navState.clearedExpedite.addFirst(false)
                isExpedite = false
            }
        }
        if (prevAlt < altitude && (prevAlt / 1000).toInt() <= (altitude / 1000).toInt()) {
            updateAltRestrictions()
        }
        if ((prevAlt / 1000).toInt() != (altitude / 1000).toInt()) {
            radarScreen.separationChecker.updateAircraftPositions()
        }
        prevAlt = altitude
    }

    /** Gets aircraft to contact other frequencies, overridden in Arrival, Departure  */
    open fun contactOther() {
        //No default implementation
    }

    /** Returns whether aircraft can be handed over to tower/centre, overridden in Arrival, Departure  */
    open fun canHandover(): Boolean {
        //No default implementation
        return false
    }

    /** Returns whether aircraft is eligible for capturing ILS - either be in heading mode and locCap is true or be in STAR mode, locCap true and direct is inside ILS arc  */
    fun canCaptureILS(): Boolean {
        if (ils == null) return false
        if (navState.dispLatMode.first() == NavState.VECTORS) return true
        ils?.let {
            direct?.let { it2 ->
                return navState.dispLatMode.first() == NavState.SID_STAR && it.isInsideILS(it2.posX.toFloat(), it2.posY.toFloat())
            }
        }
        return false
    }

    private fun findRequiredDistance(deltaHeading: Double): Double {
        val turnRate = if (ias > 250) 1.5f else 3f
        val radius = gs / 3600 / (MathUtils.degreesToRadians * turnRate).toDouble()
        val halfTheta = (180 - deltaHeading) / 2f
        return 32.4 * radius / tan(Math.toRadians(halfTheta)) + 10
    }

    val winds: IntArray
        get() = if (altitude - airport.elevation <= 4000) {
            airport.winds
        } else {
            radarScreen.airports[radarScreen.mainName]?.winds ?: intArrayOf(0, 0)
        }

    /** Called to update the heading aircraft should turn towards to follow instructed lateral mode, returns the heading it should fly as well as the resulting difference in angle of the track due to winds  */
    fun updateTargetHeading(): DoubleArray {
        deltaPosition.setZero()
        var targetHeading = 360.0

        //Get wind data
        val winds = winds
        val windHdg = winds[0] + 180
        var windSpd = winds[1]
        if (winds[0] == 0) {
            windSpd = 0
        }
        var sidstar = navState.containsCode(navState.dispLatMode.first(), NavState.SID_STAR, NavState.AFTER_WPT_HDG, NavState.HOLD_AT)
        var vector = !sidstar && navState.dispLatMode.first() == NavState.VECTORS
        if (this is Departure) {
            //Check if aircraft has climbed past initial climb
            sidstar = sidstar && this.isSidSet
            if (!sidstar) {
                //Otherwise continue climbing on current heading
                vector = true
            }
        }
        if (vector) {
            if (!isLocCap) {
                targetHeading = clearedHeading.toDouble()
            } else {
                ils?.let {
                    clearedHeading = it.heading
                    if (it.rwy != runway) {
                        runway = ils?.rwy
                    }
                    if (ils is LDA && pixelToNm(distanceBetween(x, y, runway?.x ?: 0f, runway?.y ?: 0f) - 15) <= (ils as LDA).lineUpDist) {
                        ils = (ils as LDA).imaginaryIls
                        isGsCap = ils?.isNpa == false
                        return updateTargetHeading()
                    } else {
                        //Calculates x, y of point 0.75nm or 1.5nm ahead of plane depending on distance from runway
                        val distAhead: Float
                        val distFromIls = it.getDistFrom(x, y)
                        distAhead = when {
                            distFromIls > 10 -> 1.5f
                            distFromIls > 2 -> 0.75f
                            else -> 0.25f
                        }
                        val position = it.getPointAhead(this, distAhead)
                        targetHeading = calculatePointTargetHdg(floatArrayOf(position.x, position.y), windHdg, windSpd)
                    }
                }
            }
        } else if (sidstar && !isHolding && direct != null) {
            direct?.let {
                targetHeading = calculateWaypointTargetHdg(it, windHdg, windSpd)

                //If within __px of waypoint, target next waypoint
                //Distance determined by angle that needs to be turned
                val distance = distanceBetween(x, y, it.posX.toFloat(), it.posY.toFloat()).toDouble()
                var requiredDistance = 4.0
                if (holdWpt != null && it.name == holdWpt?.name) {
                    holdWpt?.let { it2 ->
                        holdingType = route.holdProcedure.getEntryProcAtWpt(it2, heading)
                        if (holdingType == 1) {
                            val requiredHdg = route.holdProcedure.getInboundHdgAtWpt(it2) + 180
                            val turnDir = if (route.holdProcedure.isLeftAtWpt(it2)) 2 else 1 //Inverse left & right directions for initial turn
                            requiredDistance = findRequiredDistance(abs(findDeltaHeading(requiredHdg.toDouble(), turnDir, heading)))
                            requiredDistance = MathUtils.clamp(requiredDistance, 4.0, 180.0)
                        } else {
                            requiredDistance = 4.0
                        }
                    }
                } else if (route.getWptFlyOver(it.name)) {
                    requiredDistance = 4.0
                } else {
                    requiredDistance = findRequiredDistance(abs(findSidStarDeltaHeading(findNextTargetHdg(), targetHeading)))
                }
                if (this is Departure && distance <= requiredDistance + 15 && controlState == ControlState.DEPARTURE && route.getWaypoint(sidStarIndex + 1) == null) {
                    //If departure hasn't contacted centre before reaching last waypoint, contact centre immediately
                    contactOther()
                }
                if (distance <= requiredDistance) {
                    updateDirect()
                }
            }
        } else if (isHolding) {
            if (holdWpt == null) {
                holdWpt = navState.clearedHold.first()
            }
            if (holdWpt != null) {
                holdWpt?.let {
                    if (navState.dispLatMode.first() != NavState.HOLD_AT) {
                        resetHoldParameters()
                        return updateTargetHeading()
                    }
                    if (holdTargetPt == null) {
                        val point = route.holdProcedure.getOppPtAtWpt(it)
                        holdTargetPt = arrayOf(floatArrayOf(it.posX.toFloat(), it.posY.toFloat()), point)
                        holdTargetPtSelected = booleanArrayOf(false, false)
                        navState.replaceAllClearedSpdToLower()
                    }
                    if (!isInit) {
                        if (holdingType == 0) holdingType = route.holdProcedure.getEntryProcAtWpt(it, heading)
                        //Aircraft has just entered holding pattern, follow procedures relevant to each type of holding pattern entry
                        if (holdingType == 1) {
                            //After reaching waypoint, fly opposite inbound track, then after flying for leg dist, turn back to entry fix in direction opposite of holding direction
                            targetHeading = route.holdProcedure.getInboundHdgAtWpt(it) + 180.toDouble()
                            if (pixelToNm(distanceBetween(x, y, it.posX.toFloat(), it.posY.toFloat())) >= route.holdProcedure.getLegDistAtWpt(it) || isType1leg) {
                                //Once it has flown leg dist, turn back towards entry fix
                                isType1leg = true
                                targetHeading = calculateWaypointTargetHdg(it, windHdg, windSpd)
                                //Once it reaches entry fix, init has ended
                                if (distanceBetween(x, y, it.posX.toFloat(), it.posY.toFloat()) <= 10) {
                                    isInit = true
                                    holdTargetPtSelected?.set(1, true)
                                } else Unit
                            } else Unit
                        } else {
                            //Apparently no difference for types 2 and 3 in this case - fly straight towards opp waypoint with direction same as hold direction
                            targetHeading = calculatePointTargetHdg(holdTargetPt?.get(1) ?: floatArrayOf(0f, 0f), windHdg, windSpd)
                            holdTargetPtSelected?.set(1, true)
                            val deltaHdg = findDeltaHeading(route.holdProcedure.getInboundHdgAtWpt(it) + 180.toDouble())
                            val left = route.holdProcedure.isLeftAtWpt(it)
                            if (left && deltaHdg > -150 || !left && deltaHdg < 150) {
                                //Set init to true once aircraft has turned to a heading of not more than 150 deg offset from target, in the turn direction
                                isInit = true
                            } else Unit
                        }
                    } else {
                        holdTargetPtSelected?.let { it2 ->
                            var track = route.holdProcedure.getInboundHdgAtWpt(it) - radarScreen.magHdgDev
                            if (it2[1]) {
                                track += 180f
                            }
                            val target = holdTargetPt?.let { it3 ->
                                if (it2[0]) it3[0] else it3[1]
                            } ?: floatArrayOf(0f, 0f)
                            //Just keep turning and turning and turning
                            var distance = distanceBetween(x, y, target[0], target[1])
                            if (distance <= 10) {
                                //If reached target point
                                holdTargetPtSelected?.set(0, !it2[0])
                                holdTargetPtSelected?.set(1, !it2[1])
                            }
                            distance -= nmToPixel(0.5f)
                            targetHeading = calculatePointTargetHdg(floatArrayOf(target[0] + distance * cos(Math.toRadians(270 - track.toDouble())).toFloat(), target[1] + distance * sin(Math.toRadians(270 - track.toDouble())).toFloat()), windHdg, windSpd)
                        }
                    }
                }
            } else {
                resetHoldParameters()
                if (navState.dispLatMode.first() == NavState.HOLD_AT && holdWpt == null) {
                    navState.dispLatMode.removeFirst()
                    navState.dispLatMode.addFirst(NavState.VECTORS)
                    navState.clearedHdg.removeFirst()
                    navState.clearedHdg.addFirst(heading.toInt())
                }
                return updateTargetHeading()
            }
        } else {
            targetHeading = heading
            Gdx.app.log("Update target heading", "Oops, something went wrong")
        }
        targetHeading = modulateHeading(targetHeading)
        return doubleArrayOf(targetHeading, calculateAngleDiff(heading, windHdg, windSpd))
    }

    private fun resetHoldParameters() {
        isHolding = false
        holdTargetPt = null
        holdTargetPtSelected = null
        isInit = false
    }

    fun calculateAngleDiff(heading: Double, windHdg: Int, windSpd: Int): Double {
        val angle = 180 - windHdg + heading
        gs = sqrt(tas.toDouble().pow(2.0) + windSpd.toDouble().pow(2.0) - 2 * tas * windSpd * cos(Math.toRadians(angle))).toFloat()
        return asin(windSpd * sin(Math.toRadians(angle)) / gs) * MathUtils.radiansToDegrees
    }

    private fun calculateWaypointTargetHdg(waypoint: Waypoint, windHdg: Int, windSpd: Int): Double {
        return calculatePointTargetHdg(waypoint.posX - x, waypoint.posY - y, windHdg, windSpd)
    }

    private fun calculatePointTargetHdg(position: FloatArray, windHdg: Int, windSpd: Int): Double {
        return calculatePointTargetHdg(position[0] - x, position[1] - y, windHdg, windSpd)
    }

    private fun calculatePointTargetHdg(deltaX: Float, deltaY: Float, windHdg: Int, windSpd: Int): Double {
        val angleDiff: Double

        //Find target track angle
        val principleAngle = atan(deltaY / deltaX.toDouble()) * MathUtils.radiansToDegrees
        targetHeading = if (deltaX >= 0) {
            90 - principleAngle
        } else {
            270 - principleAngle
        }

        //Calculate required aircraft heading to account for winds
        //Using sine rule to determine angle between aircraft velocity and actual velocity
        val angle = windHdg - targetHeading
        angleDiff = asin(windSpd * sin(Math.toRadians(angle)) / tas) * MathUtils.radiansToDegrees
        targetHeading -= angleDiff //Heading = track - anglediff

        //Add magnetic deviation to give magnetic heading
        targetHeading += radarScreen.magHdgDev
        return targetHeading
    }

    open fun findNextTargetHdg(): Double {
        if (navState.dispLatMode.first() == NavState.AFTER_WPT_HDG && direct == afterWaypoint || navState.dispLatMode.first() == NavState.HOLD_AT && direct == holdWpt) {
            return targetHeading
        }
        val nextWpt = route.getWaypoint(sidStarIndex + 1)
        return if (nextWpt == null) {
            if (this is Departure) this.outboundHdg.toDouble() else targetHeading
        } else {
            direct?.let {
                val deltaX = nextWpt.posX - it.posX.toFloat()
                val deltaY = nextWpt.posY - it.posY.toFloat()
                val nextTarget: Double
                val principleAngle = atan(deltaY / deltaX.toDouble()) * MathUtils.radiansToDegrees
                nextTarget = if (deltaX >= 0) {
                    90 - principleAngle
                } else {
                    270 - principleAngle
                }
                nextTarget
            } ?: 360.0
        }
    }

    /** Updates the lateral position of the aircraft and its label, removes aircraft if it goes out of radar range  */
    private fun updatePosition(angleDiff: Double) {
        //Angle diff is angle correction due to winds = track - heading
        track = heading - radarScreen.magHdgDev + angleDiff
        deltaPosition.x = Gdx.graphics.deltaTime * nmToPixel(gs) / 3600 * cos(Math.toRadians(90 - track)).toFloat()
        deltaPosition.y = Gdx.graphics.deltaTime * nmToPixel(gs) / 3600 * sin(Math.toRadians(90 - track)).toFloat()
        x += deltaPosition.x
        y += deltaPosition.y
        val dist = pixelToNm(distanceBetween(0f, 0f, deltaPosition.x, deltaPosition.y))
        if (!isOnGround) prevDistTravelled += dist
        if (prevDistTravelled > 0.5) {
            prevDistTravelled -= 0.5f
            radarScreen.wakeManager.addPoint(this)
        }
        val diffDist = radarScreen.wakeManager.checkAircraftWake(this)
        if (diffDist < 0) {
            //Safe separation
            isWakeInfringe = false
            wakeTolerance -= Gdx.graphics.deltaTime * 2
        } else {
            isWakeInfringe = true
            if (!isPrevConflict) isSilenced = false
            wakeTolerance += Gdx.graphics.deltaTime * diffDist
            incrementWakeConflictTime(Gdx.graphics.deltaTime)
            radarScreen.wakeInfringeTime = radarScreen.wakeInfringeTime + Gdx.graphics.deltaTime
        }
        if (wakeTolerance < 0) wakeTolerance = 0f
        if (!isLocCap && ils != null && ils?.isInsideILS(x, y) == true && (navState.dispLatMode.first() == NavState.VECTORS || navState.dispLatMode.first() == NavState.SID_STAR && direct != null && ils?.isInsideILS(direct?.posX?.toFloat() ?: 0f, direct?.posY?.toFloat() ?: 0f) == true)) {
            isLocCap = true
            navState.replaceAllTurnDirections()
            ui.updateAckHandButton(this)
        } else if (isLocCap && !navState.containsCode(navState.dispLatMode.first(), NavState.SID_STAR, NavState.VECTORS)) {
            isLocCap = false
        }
        if (x < 1260 || x > 4500 || y < 0 || y > 3240) {
            if (this is Arrival) {
                radarScreen.setScore(MathUtils.ceil(radarScreen.score * 0.95f))
                radarScreen.utilityBox.commsManager.warningMsg("$callsign has left the airspace!")
            } else if (this is Departure && navState.dispLatMode.last() == NavState.SID_STAR && navState.clearedAlt.last() == radarScreen.maxAlt) {
                //Contact centre if departure is on SID, is not high enough but is cleared to highest altitude
                contactOther()
            }
            removeAircraft()
        }
    }

    /** Finds the deltaHeading with the appropriate force direction under different circumstances  */
    private fun findDeltaHeading(targetHeading: Double): Double {
        var forceDirection = 0
        if (navState.clearedTurnDir.first() == NavState.TURN_LEFT) {
            forceDirection = 1
        } else if (navState.clearedTurnDir.first() == NavState.TURN_RIGHT) {
            forceDirection = 2
        } else if (navState.dispLatMode.first() == NavState.HOLD_AT && isHolding && !isInit) {
            holdWpt?.let {
                if (holdingType == 1 && pixelToNm(distanceBetween(x, y, it.posX.toFloat(), it.posY.toFloat())) >= route.holdProcedure.getLegDistAtWpt(it)) {
                    forceDirection = if (route.holdProcedure.isLeftAtWpt(it)) 2 else 1
                } else if (holdingType == 2 || holdingType == 3) {
                    forceDirection = if (route.holdProcedure.isLeftAtWpt(it)) 1 else 2
                }
            }
        } else if (this is Departure && navState.dispLatMode.first() == NavState.SID_STAR) {
            direct?.let {
                //Force directions for certain departure procedures
                if (sidStar.name.contains("ANNKO1") && runway?.name?.contains("06") == true && direct != null && "ANNKO" == it.name && heading > 90 && heading < 320) {
                    //RJBB ANNKO1(6L) and ANNKO1(6R) departures
                    forceDirection = 1
                } else if (sidStar.name.contains("NKE1") && runway?.name?.contains("24") == true  && direct != null && "NKE" == it.name && heading > 180 && heading <= 360) {
                    //RJBB NKE1(24L) and NKE1(24R) departures
                    forceDirection = 2
                } else if ("SAUKA4" == sidStar.name && direct != null && "SAUKA" == it.name && heading > 180 && heading <= 360) {
                    //RJOO SAUKA4 departure
                    forceDirection = 1
                } else if ("APSU5" == sidStar.name && direct != null && "SHINY" == it.name && heading > 180 && heading <= 360) {
                    //RJOO APSU5 departure
                    forceDirection = 1
                } else if ("IRNAC4" == sidStar.name && direct != null && "BOKUN" == it.name && heading > 180 && heading <= 360) {
                    //RJOO IRNAC4 departure
                    forceDirection = 1
                } else if ((sidStar.name.contains("LNG2D") || "HSL2D" == sidStar.name || "IMPAG2D" == sidStar.name) && direct != null && "CMU" == it.name && heading > 90 && heading <= 360) {
                    //VMMC LNG2D, HSL2D and IMPAG2D departures
                    forceDirection = 2
                } else if ("POPAR3" == sidStar.name && runway?.name?.contains("16") == true && direct != null && "POPAR" == it.name && heading < 165) {
                    //RJTT POPAR3 departure
                    forceDirection = 1
                }
            }
        }
        return findDeltaHeading(targetHeading, forceDirection, heading)
    }

    /** Finds the deltaHeading for the leg after current leg  */
    private fun findSidStarDeltaHeading(targetHeading: Double, prevHeading: Double): Double {
        return if (targetHeading == -1.0) 0.0 else findDeltaHeading(targetHeading, 0, prevHeading) //Return 0 if last waypoint is outside radar, no early turn required
    }

    /** Finds the deltaHeading given a forced direction  */
    private fun findDeltaHeading(targetHeading: Double, forceDirection: Int, heading: Double): Double {
        var deltaHeading = targetHeading - heading
        while (deltaHeading > 360) deltaHeading -= 360.0
        while (deltaHeading < -360) deltaHeading += 360.0
        when (forceDirection) {
            0 -> if (deltaHeading > 180) {
                deltaHeading -= 360.0 //Turn left: deltaHeading is -ve
            } else if (deltaHeading <= -180) {
                deltaHeading += 360.0 //Turn right: deltaHeading is +ve
            }
            1 -> if (deltaHeading > 0) {
                deltaHeading -= 360.0
            }
            2 -> if (deltaHeading < 0) {
                deltaHeading += 360.0
            }
            else -> Gdx.app.log("Direction error", "Invalid turn direction specified!")
        }
        trajectory.setDeltaHeading(deltaHeading.toFloat())
        return deltaHeading
    }

    /** Updates the aircraft heading given an input targetHeading  */
    private fun updateHeading(targetHeading: Double) {
        val deltaHeading = findDeltaHeading(targetHeading)
        //Note: angular velocities unit is change in heading per second
        var targetAngularVelocity = 0.0
        if (deltaHeading > 0) {
            //Aircraft needs to turn right
            targetAngularVelocity = if (ias > 250) 1.5 else 3.0
        } else if (deltaHeading < 0) {
            //Aircraft needs to turn left
            targetAngularVelocity = if (ias > 250) -1.5 else -3.0
        }
        if (abs(deltaHeading) <= 10) {
            targetAngularVelocity = deltaHeading / 3
            if (navState.containsCode(navState.clearedTurnDir.first(), NavState.TURN_LEFT, NavState.TURN_RIGHT)) {
                navState.replaceAllTurnDirections()
                if (isSelected && isArrivalDeparture) {
                    updateUISelections()
                    ui.updateState()
                }
            }
        }
        //Update angular velocity towards target angular velocity
        when {
            targetAngularVelocity > angularVelocity + 0.1f -> angularVelocity += 0.3f * Gdx.graphics.deltaTime.toDouble() //If need to turn right, start turning right
            targetAngularVelocity < angularVelocity - 0.1f -> angularVelocity -= 0.3f * Gdx.graphics.deltaTime.toDouble() //If need to turn left, start turning left
            else -> angularVelocity = targetAngularVelocity //If within +-0.1 of target, set equal to target
        }

        //Add angular velocity to heading
        heading += angularVelocity * Gdx.graphics.deltaTime
        heading = modulateHeading(heading)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        for (i in 0 until radarScreen.speed) {
            if (radarScreen.tutorialManager?.isPausedForReading == true) break
            update()
        }
        dataTag.updateLabel()
        dataTag.updateIcon(batch)
        dataTag.drawTrailDots(batch, parentAlpha)
        if (isSelected) radarScreen.wakeManager.drawSepRequired(batch, this)
    }

    /** Updates direct waypoint of aircraft to next waypoint in SID/STAR, or switches to vector mode if after waypoint, fly heading option selected  */
    private fun updateDirect() {
        val prevDirect = direct
        sidStarIndex++
        if (direct == afterWaypoint && navState.dispLatMode.first() == NavState.AFTER_WPT_HDG) {
            clearedHeading = afterWptHdg
            navState.updateLatModes(NavState.REMOVE_AFTERHDG_HOLD, false)
            navState.updateAltModes(NavState.REMOVE_SIDSTAR_RESTR, false)
            navState.updateSpdModes(NavState.REMOVE_SIDSTAR_RESTR, false)
            navState.replaceAllAfterWptModesWithHdg(afterWptHdg)
            direct = null
        } else if (direct == holdWpt && navState.dispLatMode.first() == NavState.HOLD_AT) {
            holdWpt?.let {
                isHolding = true
                val spdRestr = route.holdProcedure.getMaxSpdAtWpt(it)
                if (spdRestr > -1 && clearedIas > spdRestr) {
                    clearedIas = spdRestr
                } else if (spdRestr == -1 && clearedIas > 250) {
                    clearedIas = 250
                }
                direct = route.getWaypoint(sidStarIndex)
                if (direct == null) {
                    navState.updateLatModes(NavState.REMOVE_SIDSTAR_ONLY, true)
                }
                radarScreen.utilityBox.commsManager.holdEstablishMsg(this, holdWpt?.name ?: "")
            }
        } else {
            direct = route.getWaypoint(sidStarIndex)
            if (direct == null) {
                navState.dispLatMode.removeFirst()
                navState.dispLatMode.addFirst(NavState.VECTORS)
                navState.replaceAllClearedAltMode()
                navState.replaceAllClearedSpdMode()
                setAfterLastWpt()
            }
        }
        navState.replaceAllOutdatedDirects(direct)
        updateAltRestrictions()
        updateTargetAltitude()
        updateClearedSpd(clearedIas)
        prevDirect?.updateFlyOverStatus()
        direct?.updateFlyOverStatus()
        if (isSelected && isArrivalDeparture) {
            updateUISelections()
            ui.updateState()
        }
    }

    /** Overridden method that sets aircraft heading after the last waypoint is reached  */
    open fun setAfterLastWpt() {
        //No default implementation
    }

    /** Switches aircraft latMode to vector, sets active nav state latMode to vector  */
    fun updateVectorMode() {
        //Switch aircraft latmode to vector mode
        navState.dispLatMode.removeFirst()
        navState.dispLatMode.addFirst(NavState.VECTORS)
    }

    /** Removes the SID/STAR options from aircraft UI after there are no waypoints left  */
    fun removeSidStarMode() {
        if (navState.dispLatMode.last() == NavState.HOLD_AT) {
            navState.updateLatModes(NavState.REMOVE_SIDSTAR_AFTERHDG, true) //Don't remove hold at if aircraft is gonna hold
        } else {
            navState.updateLatModes(NavState.REMOVE_ALL_SIDSTAR, true)
        }
    }

    /** Updates the control state of the aircraft, and updates the UI pane visibility if aircraft is selected  */
    fun updateControlState(controlState: ControlState) {
        this.controlState = controlState
        dataTag.updateIconColors(controlState)
        isActionRequired = isActionRequired && isArrivalDeparture
        if (isSelected) {
            if (controlState == ControlState.UNCONTROLLED || controlState == ControlState.ENROUTE) {
                ui.setNormalPane(true)
                ui.setSelectedPane(null)
            } else {
                ui.setNormalPane(false)
                ui.setSelectedPane(this)
            }
        }
    }

    /** Returns whether control state of aircraft is arrival or departure  */
    val isArrivalDeparture: Boolean
        get() = controlState == ControlState.ARRIVAL || controlState == ControlState.DEPARTURE

    /** Updates the selections in the UI when it is active and aircraft state changes that requires selections to change in order to be valid  */
    fun updateUISelections() {
        ui.latTab.modeButtons.mode = navState.dispLatMode.last()
        Tab.latMode = navState.dispLatMode.last()
        ui.altTab.modeButtons.mode = navState.dispAltMode.last()
        Tab.altMode = navState.dispAltMode.last()
        ui.spdTab.modeButtons.mode = navState.dispSpdMode.last()
        Tab.spdMode = navState.dispSpdMode.last()
        Tab.clearedHdg = navState.clearedHdg.last()
        if (direct != null && ui.latTab.modeButtons.mode == NavState.SID_STAR && route.findWptIndex(direct?.name) > route.findWptIndex(ui.latTab.valueBox.selected)) {
            //Update the selected direct when aircraft direct changes itself - only in SID/STAR mode and direct must after the currently selected point
            ui.latTab.valueBox.selected = direct?.name ?: ""
        }
        if (this is Departure && !ui.altTab.valueBox.selected.contains("FL") && ui.altTab.valueBox.selected.toInt() < lowestAlt) {
            ui.altTab.valueBox.selected = lowestAlt.toString()
        }
        Tab.turnDir = navState.clearedTurnDir.last()
        ui.spdTab.valueBox.selected = clearedIas.toString()
        Tab.clearedSpd = clearedIas
        ui.updateElements()
        ui.compareWithAC()
        ui.updateElementColours()
    }

    /** Gets the current aircraft data and sets the radar data to it, called after every radar sweep  */
    fun updateRadarInfo() {
        dataTag.moveLabel(x - radarX, y - radarY)
        radarX = x
        radarY = y
        radarHdg = heading
        radarTrack = track
        radarAlt = altitude
        radarGs = gs
        radarVs = verticalSpeed
    }

    /** Calculates remaining distance on SID/STAR from current aircraft position, excluding outbound  */
    fun distToGo(): Float {
        var dist = direct?.let {
            pixelToNm(distanceBetween(x, y, it.posX.toFloat(), it.posY.toFloat()))
        } ?: 0f
        dist += route.distBetRemainPts(sidStarIndex)
        return dist
    }

    val remainingWaypoints: com.badlogic.gdx.utils.Array<Waypoint>
        get() {
            navState.clearedDirect.last()?.let {
                return when (navState.dispLatMode.last()) {
                    NavState.SID_STAR -> return route.getRemainingWaypoints(route.findWptIndex(it.name), route.waypoints.size - 1)
                    NavState.AFTER_WPT_HDG -> return route.getRemainingWaypoints(route.findWptIndex(it.name), navState.clearedAftWpt.last()?.let { it2 -> route.findWptIndex(it2.name) } ?: route.findWptIndex(it.name))
                    NavState.HOLD_AT -> return route.getRemainingWaypoints(route.findWptIndex(it.name), navState.clearedHold.last()?.let { it2 -> route.findWptIndex(it2.name) } ?: route.findWptIndex(it.name))
                    else -> com.badlogic.gdx.utils.Array()
                }
            }
            return com.badlogic.gdx.utils.Array()
        }

    val uiRemainingWaypoints: com.badlogic.gdx.utils.Array<Waypoint>
        get() {
            if (isSelected && isArrivalDeparture) {
                when (Tab.latMode) {
                    NavState.SID_STAR -> return route.getRemainingWaypoints(route.findWptIndex(Tab.clearedWpt), route.waypoints.size - 1)
                    NavState.AFTER_WPT_HDG -> return route.getRemainingWaypoints(sidStarIndex, route.findWptIndex(Tab.afterWpt))
                    NavState.HOLD_AT -> return route.getRemainingWaypoints(sidStarIndex, route.findWptIndex(Tab.holdWpt))
                }
            }
            return com.badlogic.gdx.utils.Array()
        }

    /** Checks if aircraft is being manually vectored  */
    val isVectored: Boolean
        get() = navState.dispLatMode.last() == NavState.VECTORS

    val isEligibleForHandoverCheck: Boolean
        get() = !isArrivalDeparture && altitude >= radarScreen.maxAlt - 4000

    /** Checks if aircraft has a sort of emergency (fuel or active emergency)  */
    fun hasEmergency(): Boolean {
        return isFuelEmergency || emergency.isActive
    }

    override fun getX(): Float {
        return x
    }

    override fun setX(x: Float) {
        this.x = x
    }

    override fun getY(): Float {
        return y
    }

    override fun setY(y: Float) {
        this.y = y
    }

    override fun getStage(): SafeStage {
        return stage
    }

    fun updateClearedAltitude(alt: Int) {
        clearedAltitude = alt
        updateAltRestrictions()
        updateTargetAltitude()
    }

    /** Gets current cleared altitude, compares it to highest and lowest possible altitudes, sets the target altitude and possibly the cleared altitude itself  */
    fun updateTargetAltitude() {
        //When called, gets current cleared altitude, alt nav mode and updates the target altitude of aircraft
        if (navState.containsCode(navState.dispAltMode.first(), NavState.NO_RESTR, NavState.EXPEDITE)) {
            //No alt restrictions
            targetAltitude = clearedAltitude
        } else {
            //Restrictions
            when {
                clearedAltitude > highestAlt -> {
                    if (this is Arrival) {
                        clearedAltitude = highestAlt
                        navState.replaceAllClearedAlt()
                    }
                    targetAltitude = highestAlt
                }
                clearedAltitude < lowestAlt -> {
                    if (this is Departure) {
                        clearedAltitude = lowestAlt
                        navState.replaceAllClearedAlt()
                    }
                    targetAltitude = lowestAlt
                }
                else -> targetAltitude = clearedAltitude
            }
        }
    }

    /** Updates the cleared IAS under certain circumstances  */
    fun updateClearedSpd(ias: Int) {
        clearedIas = ias
        var highestSpd = -1
        if (navState.dispSpdMode.last() == NavState.SID_STAR_RESTR && direct != null) {
            highestSpd = direct?.let { route.getWptMaxSpd(it.name) } ?: -1
        }
        if (highestSpd == -1) {
            highestSpd = if (altitude >= 9900 || request == HIGH_SPEED_REQUEST) {
                climbSpd
            } else {
                250
            }
        }
        if (clearedIas > highestSpd) {
            clearedIas = highestSpd
            navState.replaceAllClearedSpdToLower()
            if (isSelected && isArrivalDeparture) {
                updateUISelections()
                ui.updateState()
            }
        }
    }

    /** Removes the aircraft completely from game, including its labels, other elements  */
    fun removeAircraft() {
        dataTag.removeLabel()
        remove()
        radarScreen.allAircraft.remove(callsign)
        radarScreen.aircrafts.remove(callsign)
        radarScreen.separationChecker.updateAircraftPositions()
        radarScreen.wakeManager.removeAircraft(callsign)
    }

    /** Overridden method that sets the altitude restrictions of the aircraft  */
    open fun updateAltRestrictions() {
        //No default implementation
    }

    /** Overridden method that resets the booleans in arrival checking whether the appropriate speeds during approach have been set  */
    open fun resetApchSpdSet() {
        //No default implementation
    }

    /** Appends a new image to end of queue for aircraft's own position  */
    fun addTrailDot() {
        if (gs <= 80) return  //Don't add dots if below 80 knots ground speed
        dataTag.addTrailDot(x, y)
    }

    /** Returns heavy/super if wake category is heavy or super  */
    val wakeString: String
        get() {
            if (wakeCat == 'H') return " heavy"
            return if (wakeCat == 'J') " super" else ""
        }

    override fun getColor(): Color {
        return color
    }

    override fun setColor(color: Color) {
        this.color = color
    }

    fun getMaxWptSpd(wpt: String?): Int {
        return route.getWptMaxSpd(wpt)
    }

    open fun updateILS(ils: ILS?) {
        if (this.ils !== ils) {
            if (this is Arrival) this.nonPrecAlts = null
            if (isLocCap) {
                if (this.ils !is LDA || ils == null) this.ils?.rwy?.removeFromArray(this) //Remove from runway array only if is not LDA or is LDA but new ILS is null
                if (isSelected && isArrivalDeparture) ui.updateState()
            }
            isGsCap = false
            isLocCap = false
            resetApchSpdSet()
            if (clearedIas < 160) {
                clearedIas = 160
                navState.replaceAllClearedSpdToHigher()
            }
        }
        this.ils = ils
    }
}