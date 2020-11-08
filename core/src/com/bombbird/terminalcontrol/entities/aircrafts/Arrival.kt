package com.bombbird.terminalcontrol.entities.aircrafts

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Queue
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.completeAchievement
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.incrementEmergency
import com.bombbird.terminalcontrol.entities.achievements.UnlockManager.incrementLanded
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.approaches.ILS
import com.bombbird.terminalcontrol.entities.approaches.LDA
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR
import com.bombbird.terminalcontrol.entities.sidstar.Route
import com.bombbird.terminalcontrol.entities.sidstar.SidStar
import com.bombbird.terminalcontrol.entities.sidstar.Star
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.ui.tabs.Tab
import com.bombbird.terminalcontrol.utilities.errors.IncompatibleSaveException
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.feetToMetre
import com.bombbird.terminalcontrol.utilities.math.MathTools.nmToFeet
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import com.bombbird.terminalcontrol.utilities.math.MathTools.pointsAtBorder
import org.apache.commons.lang3.ArrayUtils
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.tan

class Arrival : Aircraft {
    //Others
    var contactAlt: Int
        private set
    private var star: Star
    var nonPrecAlts: Queue<FloatArray>? = null
    var isLowerSpdSet: Boolean
        private set
    var isIlsSpdSet: Boolean
        private set
    var isFinalSpdSet: Boolean
        private set

    //For fuel
    var fuel: Float
        private set
    var isRequestPriority = false
        private set
    var isDeclareEmergency = false
        private set
    var isDivert = false
        private set

    //For go around
    var isWillGoAround: Boolean
        private set
    var goAroundAlt: Int
        private set
    var isGoAroundSet = false
        private set

    constructor(callsign: String, icaoType: String, arrival: Airport) : super(callsign, icaoType, arrival) {
        isOnGround = false
        isLowerSpdSet = false
        isIlsSpdSet = false
        isFinalSpdSet = false
        isWillGoAround = false
        goAroundAlt = 0
        contactAlt = MathUtils.random(2000) + 22000

        //Gets a STAR for active runways
        star = RandomSTAR.randomSTAR(arrival)
        if ("EVA226" == callsign && radarScreen.tutorial) {
            star = arrival.stars["NTN1A"]!!
            emergency.isEmergency = false
            typDes = 2900
            contactAlt = 22000
        }
        RandomSTAR.starUsed(arrival.icao, star.name)
        route = Route(this, star, false)
        direct = route.getWaypoint(0)
        clearedHeading = heading.toInt()
        track = (heading - radarScreen.magHdgDev)

        //Calculate spawn border
        val point = pointsAtBorder(floatArrayOf(1310f, 4450f), floatArrayOf(50f, 3190f), direct?.posX?.toFloat() ?: 2880f, direct?.posY?.toFloat() ?: 1620f, 180 + track.toFloat())
        x = point[0]
        y = point[1]
        if ("BULLA-T" == star.name || "KOPUS-T" == star.name) direct = route.getWaypoint(1)
        loadLabel()
        navState = NavState(this)
        var maxAltWpt: Waypoint? = null
        var minAltWpt: Waypoint? = null
        for (waypoint in route.waypoints) {
            if (maxAltWpt == null && route.getWptMaxAlt(waypoint.name) > -1) {
                maxAltWpt = waypoint
            }
            if (minAltWpt == null && route.getWptMinAlt(waypoint.name) > -1) {
                minAltWpt = waypoint
            }
        }
        fuel = (45 + 10 + 10) * 60 + distToGo() / 250 * 3600 + 900 + MathUtils.random(-600, 600)
        val referenceTypDes = 2800
        var initAlt: Float = airport.elevation + 3000 + (distToGo() - 15) / 300 * 60 * referenceTypDes * 0.8f //Calculate an initial reference altitude
        val differenceInTypDes = MathUtils.clamp(typDes - referenceTypDes, -800, 800) //Calculate the different between reference and actual descent rate
        initAlt += differenceInTypDes * 5 + MathUtils.random(-1000, 1000).toFloat() //Add the difference in rate * 5 and a random altitude difference of +-1000 feet
        if ("BULLA-T" == star.name || "KOPUS-T" == star.name) {
            initAlt = 9000f
        } else {
            if (maxAltWpt != null) {
                val maxAlt: Float = route.getWptMaxAlt(maxAltWpt.name) + (distFromStartToPoint(maxAltWpt) - 5) / 300 * 60 * typDes
                if (maxAlt < initAlt) initAlt = maxAlt
            }
            if (initAlt > 28000) {
                initAlt = 28000f
            } else if (initAlt < airport.elevation + 6000) {
                initAlt = MathUtils.round(airport.elevation / 1000f) * 1000 + 6000.toFloat()
            }
            if (minAltWpt != null && initAlt < route.getWptMinAlt(minAltWpt.name)) {
                initAlt = route.getWptMinAlt(minAltWpt.name).toFloat()
            }
            for (obstacle in radarScreen.obsArray) {
                if (obstacle.isIn(this) && initAlt < obstacle.minAlt) {
                    initAlt = obstacle.minAlt.toFloat()
                }
            }
        }
        if (initAlt > AircraftType.getMaxCruiseAlt(icaoType)) initAlt = AircraftType.getMaxCruiseAlt(icaoType).toFloat()
        if (radarScreen.tutorial && "EVA226" == callsign) {
            initAlt = 23400f
        }
        altitude = initAlt
        updateAltRestrictions()
        updateClearedAltitude(if ("BULLA-T" == star.name || "KOPUS-T" == star.name) {
            6000
        } else if (initAlt > 15000) {
            15000
        } else {
            initAlt.toInt() - initAlt.toInt() % 1000
        })
        verticalSpeed = if (clearedAltitude < altitude - 500) {
            -typDes.toFloat()
        } else {
            -typDes * (altitude - clearedAltitude) / 500
        }
        updateClearedSpd(climbSpd)
        ias = climbSpd.toFloat()
        direct?.let {
            val spd: Int = route.getWptMaxSpd(it.name)
            if (spd > -1) {
                updateClearedSpd(spd)
                ias = spd.toFloat()
            }
        }
        if (altitude <= 10000 && (clearedIas > 250 || ias > 250)) {
            updateClearedSpd(250)
            ias = 250f
        }
        if (distToGo() <= 20 && clearedIas > 220) {
            updateClearedSpd(220)
            ias = 220f
        }
        checkArrival()
        navState.clearedSpd.removeFirst()
        navState.clearedSpd.addFirst(clearedIas)
        navState.clearedAlt.removeLast()
        navState.clearedAlt.addLast(clearedAltitude)
        updateControlState(ControlState.UNCONTROLLED)
        color = Color(0x00b3ffff)
        heading = update()
        track = heading - radarScreen.magHdgDev + updateTargetHeading()[1]
        initRadarPos()
    }

    constructor(save: JSONObject) : super(save) {
        if (save.isNull("route")) {
            throw IncompatibleSaveException("Old save format - route is null")
        }
        val route = save.getJSONObject("route")

        star = airport.stars[save.getString("star")] ?: Star(airport, route.getJSONArray("waypoints"), route.getJSONArray("restrictions"), route.getJSONArray("flyOver"), route.optString("name", "null"))

        this.route = Route(route, star)
        if (save.isNull("nonPrecAlts")) {
            //If non precision alt is null
            nonPrecAlts = null
        } else {
            val nonPrec = save.getJSONArray("nonPrecAlts")
            nonPrecAlts = Queue()
            for (i in 0 until nonPrec.length()) {
                val data = nonPrec.getJSONArray(i)
                nonPrecAlts?.addLast(floatArrayOf(data.getDouble(0).toFloat(), data.getDouble(1).toFloat()))
            }
        }
        isLowerSpdSet = save.getBoolean("lowerSpdSet")
        isIlsSpdSet = save.getBoolean("ilsSpdSet")
        isFinalSpdSet = save.getBoolean("finalSpdSet")
        isWillGoAround = save.getBoolean("willGoAround")
        goAroundAlt = save.getInt("goAroundAlt")
        isGoAroundSet = save.getBoolean("goAroundSet")
        contactAlt = save.getInt("contactAlt")
        fuel = save.optDouble("fuel", 75.0 * 60).toFloat()
        isRequestPriority = save.optBoolean("requestPriority", false)
        isDeclareEmergency = save.optBoolean("declareEmergency", false)
        isDivert = save.optBoolean("divert", false)
        color = Color(0x00b3ffff)
        loadOtherLabelInfo(save)
    }

    constructor(departure: Departure) : super(departure) {
        //Convert departure emergency to arrival
        isOnGround = false
        isLowerSpdSet = false
        isIlsSpdSet = false
        isFinalSpdSet = false
        isWillGoAround = false
        goAroundAlt = 0
        contactAlt = MathUtils.random(2000) + 22000
        loadLabel()
        navState = departure.navState
        fuel = 99999f //Just assume they won't run out of fuel
        color = Color(0x00b3ffff)
        updateControlState(ControlState.ARRIVAL)
        val size: Int = departure.dataTag.trailDots.size
        for (i in 0 until size) {
            val image: Image = departure.dataTag.trailDots.removeFirst()
            dataTag.addTrailDot(image.x + image.width / 2, image.y + image.height / 2)
        }
        star = airport.stars.values.iterator().next() //Assign any STAR for the sake of not crashing the game
        if (emergency.isActive) dataTag.setEmergency()
    }

    /** Calculates remaining distance on STAR from current start aircraft position to a certain point on it  */
    private fun distFromStartToPoint(waypoint: Waypoint): Float {
        var dist = direct?. let { pixelToNm(distanceBetween(x, y, it.posX.toFloat(), it.posY.toFloat())) } ?: 0f
        var nextIndex = 1
        if (route.waypoints.size > 1 && route.getWaypoint(0) != waypoint) {
            while (route.getWaypoint(nextIndex) != waypoint) {
                dist += route.distBetween(nextIndex - 1, nextIndex)
                nextIndex += 1
            }
        }
        return dist
    }

    /** Overrides method in Aircraft class to join the lines between each STAR waypoint  */
    override fun drawSidStar() {
        super.drawSidStar()
        navState.clearedDirect.last()?.let { radarScreen.waypointManager.updateStarRestriction(route, route.findWptIndex(it.name), route.waypoints.size) }
    }

    /** Overrides method in Aircraft class to join lines between each cleared STAR waypoint  */
    override fun uiDrawSidStar() {
        super.uiDrawSidStar()
        radarScreen.waypointManager.updateStarRestriction(route, route.findWptIndex(Tab.clearedWpt), route.waypoints.size)
    }

    /** Overrides method in Aircraft class to join lines between waypoints till afterWpt, then draws a heading line from there  */
    override fun drawAftWpt() {
        super.drawAftWpt()
        navState.clearedDirect.last()?.let {
            navState.clearedAftWpt.last()?.let { it2 ->
                route.joinLines(route.findWptIndex(it.name), route.findWptIndex(it2.name) + 1, navState.clearedAftWptHdg.last())
                radarScreen.waypointManager.updateStarRestriction(route, route.findWptIndex(it.name), route.findWptIndex(it2.name) + 1)
            }
        }
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected afterWpt, then draws a heading line from there  */
    override fun uiDrawAftWpt() {
        super.uiDrawAftWpt()
        navState.clearedDirect.last()?.let {
            route.joinLines(route.findWptIndex(it.name), route.findWptIndex(Tab.afterWpt) + 1, Tab.afterWptHdg)
            radarScreen.waypointManager.updateStarRestriction(route, route.findWptIndex(it.name), route.findWptIndex(Tab.afterWpt) + 1)
        }
    }

    /** Overrides method in Aircraft class to join lines between waypoints till holdWpt  */
    override fun drawHoldPattern() {
        super.drawHoldPattern()
        radarScreen.shapeRenderer.color = Color.WHITE
        navState.clearedDirect.last()?.let {
            navState.clearedHold.last()?.let { it2 ->
                route.joinLines(route.findWptIndex(it.name), route.findWptIndex(it2.name) + 1, -1)
                radarScreen.waypointManager.updateStarRestriction(route, route.findWptIndex(it.name), route.findWptIndex(it2.name) + 1)
            }
        }
    }

    /** Overrides method in Aircraft class to join lines between waypoints till selected holdWpt  */
    override fun uiDrawHoldPattern() {
        super.uiDrawHoldPattern()
        radarScreen.shapeRenderer.color = Color.YELLOW
        navState.clearedDirect.last()?.let {
            route.joinLines(route.findWptIndex(it.name), route.findWptIndex(Tab.holdWpt) + 1, -1)
            radarScreen.waypointManager.updateStarRestriction(route, route.findWptIndex(it.name), route.findWptIndex(Tab.holdWpt) + 1)
        }
    }

    /** Overrides method in Aircraft class to set to current heading  */
    override fun setAfterLastWpt() {
        clearedHeading = heading.toInt()
        navState.clearedHdg.removeFirst()
        navState.clearedHdg.addFirst(clearedHeading)
        updateVectorMode()
        removeSidStarMode()
    }

    /** Overrides updateSpd method in Aircraft, for setting the aircraft speed to 220 knots when within 20nm track miles if clearedIas > 220 knots  */
    override fun updateSpd() {
        if (altitude < 10000 && clearedIas > 250) {
            updateClearedSpd(250)
            super.updateSpd()
        }
        if (!isLowerSpdSet && direct != null && distToGo() <= 20) {
            if (clearedIas > 220) {
                updateClearedSpd(220)
                super.updateSpd()
            }
            isLowerSpdSet = true
        }
        if (!isIlsSpdSet && isLocCap) {
            if (clearedIas > 190) {
                updateClearedSpd(190)
                super.updateSpd()
            }
            isIlsSpdSet = true
        }
        if (!isFinalSpdSet && isLocCap && ils?.rwy?.let { pixelToNm(distanceBetween(x, y, it.x, it.y)) <= 7 } == true) {
            if (clearedIas > apchSpd) {
                updateClearedSpd(apchSpd)
                navState.replaceAllClearedSpdToLower()
                if (isArrivalDeparture && isSelected) {
                    ui.spdTab.valueBox.selected = apchSpd.toString()
                }
                super.updateSpd()
            }
            isFinalSpdSet = true
        }
        if (holdWpt != null && direct != null && holdWpt == direct && clearedIas > 250 && holdWpt?.let { pixelToNm(distanceBetween(x, y, it.posX.toFloat(), it.posY.toFloat())) <= 8 } == true) {
            updateClearedSpd(250)
            super.updateSpd()
        }
    }

    /** Overrides findNextTargetHdg method in Aircraft, for finding the next heading aircraft should fly in order to reach next waypoint  */
    override fun findNextTargetHdg(): Double {
        val result = super.findNextTargetHdg()
        return if (result < 0) heading else result
    }

    /** Overrides updateAltRestrictions method in Aircraft, for setting aircraft altitude restrictions when descending via the STAR  */
    override fun updateAltRestrictions() {
        if (navState.containsCode(navState.dispLatMode.first(), NavState.SID_STAR, NavState.AFTER_WPT_HDG) || navState.dispLatMode.first() == NavState.HOLD_AT && !isHolding) {
            //Aircraft on STAR
            var highestAlt = -1
            var lowestAlt = -1
            direct?.let {
                highestAlt = route.getWptMaxAlt(it.name).coerceAtLeast(altitude.toInt() / 1000 * 1000)
                lowestAlt = route.getWptMinAlt(it.name)
            }
            if (highestAlt == -1) highestAlt = radarScreen.maxAlt
            if (lowestAlt == -1) lowestAlt = radarScreen.minAlt
            if (highestAlt > radarScreen.maxAlt) highestAlt = radarScreen.maxAlt
            if (highestAlt < radarScreen.minAlt) highestAlt = radarScreen.minAlt
            if (highestAlt < lowestAlt) highestAlt = lowestAlt
            val tmpArray = ui.altTab.createAltArray(lowestAlt, highestAlt)
            if ("TCOO" == airport.icao) {
                checkAndAddOddAltitudes(tmpArray, lowestAlt, highestAlt, 3500)
            } else if ("TCHH" == airport.icao && sidStar.runways.contains("25R", false)) {
                checkAndAddOddAltitudes(tmpArray, lowestAlt, highestAlt, 4300)
                checkAndAddOddAltitudes(tmpArray, lowestAlt, highestAlt, 4500)
            } else if ("TCHX" == airport.icao && sidStar.runways.contains("31", false)) {
                checkAndAddOddAltitudes(tmpArray, lowestAlt, highestAlt, 4500)
            }
            tmpArray.sort()
            this.highestAlt = tmpArray[tmpArray.size - 1]
            this.lowestAlt = tmpArray.first()
        } else if (navState.dispLatMode.first() == NavState.HOLD_AT && isHolding && holdWpt != null) {
            holdWpt?.let {
                val altRestr: IntArray = route.holdProcedure.getAltRestAtWpt(it)
                val highestAlt = altRestr[1]
                val lowestAlt = altRestr[0]
                this.highestAlt = if (highestAlt > -1) highestAlt else radarScreen.maxAlt
                this.lowestAlt = if (lowestAlt > -1) lowestAlt else radarScreen.minAlt
            }
        }
    }

    /** Adds the odd altitudes if they are in range of the supplied lowest and highest altitudes */
    fun checkAndAddOddAltitudes(allAlts: Array<Int>, lowestAlt: Int, highestAlt: Int, altToAdd: Int) {
        if (altToAdd in lowestAlt..highestAlt) {
            allAlts.add(altToAdd)
        }
    }

    /** Overrides update method in Aircraft to include updating fuel time  */
    override fun update(): Double {
        val info = super.update()
        if (!isOnGround && !emergency.isEmergency) {
            updateFuel()
        }
        return info
    }

    /** Updates the fuel time for arrival  */
    private fun updateFuel() {
        fuel -= Gdx.graphics.deltaTime
        if (fuel < 2700 && !isRequestPriority && controlState == ControlState.ARRIVAL) {
            //Low fuel, request priority
            if (airport.landingRunways.size == 0) {
                //Airport has no landing runways available, different msg
                radarScreen.utilityBox.commsManager.warningMsg("Pan-pan, pan-pan, pan-pan, $callsign is low on fuel and will divert in 10 minutes if no landing runway is available.")
                TerminalControl.tts.lowFuel(this, 3)
            } else {
                radarScreen.utilityBox.commsManager.warningMsg("Pan-pan, pan-pan, pan-pan, $callsign is low on fuel and requests priority landing.")
                TerminalControl.tts.lowFuel(this, 0)
            }
            isRequestPriority = true
            isActionRequired = true
            dataTag.startFlash()
        }
        if (fuel < 2100 && !isDeclareEmergency && controlState == ControlState.ARRIVAL) {
            //Minimum fuel, declare emergency
            if (airport.landingRunways.size == 0) {
                //Airport has no landing runways available, divert directly
                radarScreen.utilityBox.commsManager.warningMsg("Mayday, mayday, mayday, $callsign is declaring a fuel emergency and is diverting immediately.")
                TerminalControl.tts.lowFuel(this, 4)
                divertToAltn()
            } else {
                radarScreen.utilityBox.commsManager.warningMsg("Mayday, mayday, mayday, $callsign is declaring a fuel emergency and requests immediate landing within 10 minutes or will divert.")
                radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.9f))
                TerminalControl.tts.lowFuel(this, 1)
            }
            isDeclareEmergency = true
            if (!isFuelEmergency) isFuelEmergency = true
            dataTag.setEmergency()
        }
        if (fuel < 1500 && !isDivert && !isLocCap && controlState == ControlState.ARRIVAL) {
            //Diverting to alternate
            radarScreen.utilityBox.commsManager.warningMsg("$callsign is diverting to the alternate airport.")
            TerminalControl.tts.lowFuel(this, 2)
            divertToAltn()
            radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.9f))
        }
    }

    /** Instructs the aircraft to divert to an alternate airport  */
    private fun divertToAltn() {
        navState.dispLatMode.clear()
        navState.dispLatMode.addFirst(NavState.VECTORS)
        navState.dispAltMode.clear()
        navState.dispAltMode.addFirst(NavState.NO_RESTR)
        navState.dispSpdMode.clear()
        navState.dispSpdMode.addFirst(NavState.NO_RESTR)
        navState.clearedHdg.clear()
        navState.clearedHdg.addFirst(radarScreen.divertHdg)
        navState.clearedDirect.clear()
        navState.clearedDirect.addFirst(null)
        navState.clearedAftWpt.clear()
        navState.clearedAftWpt.addFirst(null)
        navState.clearedAftWptHdg.clear()
        navState.clearedAftWptHdg.addFirst(radarScreen.divertHdg)
        navState.clearedHold.clear()
        navState.clearedHold.addFirst(null)
        navState.clearedIls.clear()
        navState.clearedIls.addFirst(null)
        navState.clearedAlt.clear()
        navState.clearedAlt.addFirst(10000)
        navState.clearedExpedite.clear()
        navState.clearedExpedite.addFirst(false)
        navState.clearedSpd.clear()
        navState.clearedSpd.addFirst(250)
        navState.length = 1
        navState.updateAircraftInfo()
        updateControlState(ControlState.UNCONTROLLED)
        isDivert = true
    }

    /** Overrides updateAltitude method in Aircraft for when arrival is on glide slope or non precision approach  */
    override fun updateAltitude(holdAlt: Boolean, fixedVs: Boolean) {
        ils?.let {
            if (!it.isNpa) {
                if (!isGsCap) {
                    super.updateAltitude(altitude < it.getGSAlt(this) && it.name.contains("IMG"), false)
                    if (canCaptureILS() && isLocCap && abs(altitude - it.getGSAlt(this)) <= 50 && altitude <= it.gsAlt + 50) {
                        if (navState.dispLatMode.first() == NavState.SID_STAR) {
                            //Change to vector mode upon GS capture
                            val prevDirect = direct
                            direct = null
                            navState.dispLatMode.removeFirst()
                            navState.dispLatMode.addFirst(NavState.VECTORS)
                            navState.replaceAllClearedAltMode()
                            navState.replaceAllClearedSpdMode()
                            setAfterLastWpt()
                            navState.replaceAllOutdatedDirects(null)
                            updateAltRestrictions()
                            updateTargetAltitude()
                            updateClearedSpd(clearedIas)
                            prevDirect?.updateFlyOverStatus()
                            if (isSelected && isArrivalDeparture) {
                                updateUISelections()
                                ui.updateState()
                            }
                        }
                        isGsCap = true
                        setMissedAlt()
                    }
                } else {
                    verticalSpeed = -nmToFeet(tan(Math.toRadians(3.0)).toFloat() * 140f / 60f)
                    altitude = it.getGSAlt(this)
                }
                if (nonPrecAlts != null) {
                    nonPrecAlts = null
                }
            } else {
                if (isLocCap && clearedAltitude != it.missedApchProc?.climbAlt && navState.dispLatMode.first() == NavState.VECTORS) {
                    setMissedAlt()
                }
                if (nonPrecAlts == null) {
                    nonPrecAlts = Queue()
                    val copy = (it as LDA).nonPrecAlts
                    if (copy != null) {
                        for (data in copy) {
                            nonPrecAlts?.addLast(data)
                        }
                    }
                }
                if (isLocCap) {
                    if (nonPrecAlts != null && (nonPrecAlts?.size ?: 0) > 0) {
                        nonPrecAlts?.let { it2 ->
                            //Set target altitude to current restricted altitude
                            if (navState.dispLatMode.first() == NavState.VECTORS) targetAltitude = it2.first()[0].toInt()
                            while (it2.size > 0 && pixelToNm(distanceBetween(x, y, it.x, it.y)) < it2.first()[1]) {
                                it2.removeFirst()
                            }
                        }
                        super.updateAltitude(holdAlt = false, fixedVs = false)
                    } else {
                        //Set final descent towards runway
                        targetAltitude = it.rwy?.elevation ?: airport.elevation
                        val lineUpDist: Float = (ils as LDA).lineUpDist
                        val tmpAlt: Float
                        var actlTargetAlt: Float
                        actlTargetAlt = (ils as LDA).imaginaryIls.getGSAltAtDist(lineUpDist)
                        tmpAlt = actlTargetAlt
                        actlTargetAlt -= 200f
                        actlTargetAlt = MathUtils.clamp(actlTargetAlt, (tmpAlt + (it.rwy?.elevation ?: airport.elevation)) / 2, tmpAlt)
                        val remainingAlt: Float = altitude - actlTargetAlt
                        val actlTargetPos: Vector2 = (ils as LDA).imaginaryIls.getPointAtDist(lineUpDist)
                        val distFromRwy = pixelToNm(distanceBetween(x, y, actlTargetPos.x, actlTargetPos.y))
                        verticalSpeed = -remainingAlt / (distFromRwy / gs * 60)
                        super.updateAltitude(remainingAlt < 0, true)
                    }
                } else {
                    super.updateAltitude(holdAlt = false, fixedVs = false)
                }
            }
            if (isLocCap) {
                if (!isGoAroundSet) {
                    generateGoAround()
                    it.rwy?.addToArray(this)
                    isGoAroundSet = true
                    wakeTolerance = MathUtils.clamp(wakeTolerance, 0f, 20f)
                }
                checkAircraftInFront()
            }
            if (ils != null && controlState == ControlState.ARRIVAL && altitude <= airport.elevation + 1300) {
                contactOther()
            }
            if (altitude <= (it.rwy?.elevation ?: airport.elevation) + 10 && ils != null) {
                isTkOfLdg = true
                isOnGround = true
                heading = it.rwy?.heading?.toDouble() ?: 360.0
                if (!UnlockManager.unlocks.contains("parallelLanding") && radarScreen.addAndCheckSimultLanding(this)) {
                    //Parallel landing achieved!
                    completeAchievement("parallelLanding")
                }
            }
            if (isLocCap && checkGoAround()) {
                initializeGoAround()
            }
        } ?: run {
            //If NPA not active yet
            if (nonPrecAlts != null) {
                nonPrecAlts = null
            }
            isGoAroundSet = false
            super.updateAltitude(holdAlt, fixedVs)
        }
        if (controlState != ControlState.ARRIVAL && altitude <= contactAlt && altitude > airport.elevation + 1300 && !isDivert && !isLocCap) {
            updateControlState(ControlState.ARRIVAL)
            radarScreen.utilityBox.commsManager.initialContact(this)
            isActionRequired = true
            dataTag.startFlash()
        }
    }

    /** Checks when aircraft should contact tower  */
    override fun contactOther() {
        //Contact the tower
        updateControlState(ControlState.UNCONTROLLED)
        var points = 0.6f - radarScreen.planesToControl / 30
        points = MathUtils.clamp(points, 0.15f, 0.5f)
        if (!airport.isCongested) {
            radarScreen.planesToControl = radarScreen.planesToControl + points
        } else {
            radarScreen.planesToControl = radarScreen.planesToControl - 0.4f
        }
        ils?.let { radarScreen.utilityBox.commsManager.contactFreq(this, it.towerFreq[0], it.towerFreq[1]) }
    }

    override fun canHandover(): Boolean {
        return controlState == ControlState.ARRIVAL && isLocCap && canCaptureILS()
    }

    /** Called to check the distance behind the aircraft ahead of current aircraft, calls swap in runway array if it somehow overtakes it  */
    private fun checkAircraftInFront() {
        ils?.let {
            it.rwy?.let { it2 ->
                val approachPosition: Int = it2.aircraftsOnAppr.indexOf(this, false)
                if (approachPosition > 0) {
                    val aircraftInFront: Aircraft = it2.aircraftsOnAppr.get(approachPosition - 1)
                    var targetX: Float = it.x
                    var targetY: Float = it.y
                    if (it is LDA) {
                        targetX = it2.oppRwy.x
                        targetY = it2.oppRwy.y
                    }
                    if (distanceBetween(aircraftInFront.x, aircraftInFront.y, targetX, targetY) > distanceBetween(x, y, targetX, targetY)) {
                        //If this aircraft overtakes the one in front of it
                        it2.swapAircrafts(this)
                    }
                }
            }
        }
    }

    /** Gets the chances of aircraft going around due to external conditions, sets whether it will do so  */
    private fun generateGoAround() {
        var chance = 0f
        if ("ALL RWY" == airport.windshear) {
            chance = 0.2f
        } else if ("None" != airport.windshear) {
            for (string in airport.windshear.split(" ".toRegex()).toTypedArray()) {
                if (ils?.rwy?.name?.let { string.contains(it) } == true) {
                    chance = 0.2f
                    break
                }
            }
        }
        if (chance > 0 && airport.gusts > -1) {
            chance += (1 - chance) * 1.2f * (airport.gusts - 15) / 100
        }
        if (MathUtils.random(1f) < chance) {
            isWillGoAround = true
            goAroundAlt = MathUtils.random(500, 1100)
        }
    }

    /** Checks whether the aircraft meets the criteria for going around, returns true if so  */
    private fun checkGoAround(): Boolean {
        //Gonna split the returns into different segments just to make it easier to read
        if (wakeTolerance > 25) {
            //If aircraft has reached wake limits
            radarScreen.utilityBox.commsManager.goAround(this, "wake turbulence", controlState)
            return true
        }
        ils?.let {
            it.rwy?.let { it2 ->
                if (isWillGoAround && altitude < goAroundAlt && (airport.windshear.contains(it2.name) || airport.windshear == "ALL RWY")) {
                    //If go around is determined to happen due to windshear, and altitude is below go around alt, and windshear is still going on
                    radarScreen.utilityBox.commsManager.goAround(this, "windshear", controlState)
                    return true
                }
                val firstAircraft: Aircraft? = if (it2.aircraftsOnAppr.size > 0) it2.aircraftsOnAppr.get(0) else null
                if (pixelToNm(distanceBetween(x, y, it2.x, it2.y)) <= 3) {
                    //If distance from runway is less than 3nm
                    if (firstAircraft != null && firstAircraft.callsign != callsign && firstAircraft.emergency.isActive && firstAircraft.emergency.isStayOnRwy) {
                        //If runway is closed due to emergency staying on runway
                        radarScreen.utilityBox.commsManager.goAround(this, "runway closed", controlState)
                        return true
                    }
                    if (it !is LDA && !it.name.contains("IMG") && !isGsCap) {
                        //If ILS GS has not been captured
                        radarScreen.utilityBox.commsManager.goAround(this, "being too high", controlState)
                        return true
                    } else if (ias - apchSpd > 10) {
                        //If airspeed is more than 10 knots higher than approach speed
                        radarScreen.utilityBox.commsManager.goAround(this, "being too fast", controlState)
                        return true
                    } else if (ils !is LDA && !it.name.contains("IMG") && MathUtils.cosDeg(it2.trueHdg - track.toFloat()) < MathUtils.cosDeg(10f)) {
                        //If aircraft is not fully stabilised on LOC course
                        radarScreen.utilityBox.commsManager.goAround(this, "unstable approach", controlState)
                        return true
                    } else {
                        val windDir: Int = airport.winds[0]
                        val windSpd: Int = airport.winds[1]
                        if (windSpd * MathUtils.cosDeg(windDir - it2.heading.toFloat()) < -10) {
                            //If tailwind exceeds 10 knots
                            radarScreen.utilityBox.commsManager.goAround(this, "strong tailwind", controlState)
                            return true
                        }
                    }
                }
                if (altitude < it.minima && airport.visibility < feetToMetre(it.minima - it2.elevation.toFloat()) * 9) {
                    //If altitude below minima and visibility is less than 9 times the minima (approx)
                    radarScreen.utilityBox.commsManager.goAround(this, "runway not in sight", controlState)
                    return true
                }
                if (altitude < it2.elevation + 150) {
                    if (firstAircraft != null && firstAircraft.callsign != callsign) {
                        //If previous arrival/departure has not cleared runway by the time aircraft reaches 150 feet AGL
                        radarScreen.utilityBox.commsManager.goAround(this, "traffic on runway", controlState)
                        return true
                    } else {
                        //If departure has not cleared runway
                        val dep: Aircraft? = airport.takeoffManager.prevAircraft[it2.name]
                        if (dep != null && dep.altitude - it2.elevation < 10) {
                            radarScreen.utilityBox.commsManager.goAround(this, "traffic on runway", controlState)
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    /** Sets the cleared altitude for aircraft on approach, updates UI altitude selections if selected  */
    private fun setMissedAlt() {
        updateClearedAltitude(ils?.missedApchProc?.climbAlt ?: 4000)
        navState.replaceAllClearedAltMode()
        navState.replaceAllClearedAlt()
        if (isSelected && controlState == ControlState.ARRIVAL) {
            ui.updateState()
        }
    }

    /** Updates the aircraft status when aircraft's tkOfLdg mode is active  */
    override fun updateTkofLdg() {
        altitude = ils?.rwy?.elevation?.toFloat() ?: airport.elevation.toFloat()
        verticalSpeed = 0f
        updateClearedSpd(0)
        if (gs <= 35 && (!emergency.isActive || !emergency.isStayOnRwy)) {
            var score = 1
            if (radarScreen.arrivals >= 12) score = 2 //2 points if you're controlling at least 12 planes at a time
            if (airport.isCongested && radarScreen.tfcMode !== RadarScreen.TfcMode.ARRIVALS_ONLY || expediteTime > 120) score = 0 //Add score only if the airport is not congested, if mode is not arrival only, and aircraft has not expedited for >2 mins
            if (emergency.isEmergency) {
                score = 5 //5 points for landing an emergency!
                incrementEmergency()
                radarScreen.emergenciesLanded = radarScreen.emergenciesLanded + 1
            }
            radarScreen.setScore(radarScreen.getScore() + score)
            airport.landings = airport.landings + 1
            removeAircraft()
            ils?.rwy?.removeFromArray(this)
            incrementLanded()
            val typhoonList = arrayOf("TCTP", "TCSS", "TCTT", "TCAA", "TCHH", "TCMC")
            if (ArrayUtils.contains(typhoonList, airport.icao) && airport.winds[1] >= 40) completeAchievement("typhoon")
            if ("TCWS" == airport.icao && airport.visibility <= 2500) completeAchievement("haze")
            isTkOfLdg = false
        }
    }

    /** Overrides resetApchSpdSet method in Aircraft, called to reset the ilsSpdSet & finalSpdSet booleans  */
    override fun resetApchSpdSet() {
        isIlsSpdSet = false
        isFinalSpdSet = false
    }

    /** Overrides updateGoAround method in Aircraft, called to set aircraft status during go-arounds  */
    override fun updateGoAround() {
        if (altitude > airport.elevation + 1300 && isGoAround) {
            updateControlState(ControlState.ARRIVAL)
            isGoAround = false
        }
    }

    /** Overrides initializeGoAround method in Aircraft, called to initialize go around mode of aircraft  */
    private fun initializeGoAround() {
        isGoAround = true
        isGoAroundWindow = true
        val missedApproach = ils?.missedApchProc
        clearedHeading = ils?.heading ?: 360
        navState.clearedHdg.removeFirst()
        navState.clearedHdg.addFirst(clearedHeading)
        updateClearedSpd(missedApproach?.climbSpd ?: 220)
        navState.clearedSpd.removeFirst()
        navState.clearedSpd.addFirst(clearedIas)
        if (clearedAltitude <= missedApproach?.climbAlt ?: 4000) {
            updateClearedAltitude(missedApproach?.climbAlt ?: 4000)
            navState.clearedAlt.removeFirst()
            navState.clearedAlt.addFirst(clearedAltitude)
        }
        updateILS(null)
        navState.voidAllIls()
        isIlsSpdSet = false
        isFinalSpdSet = false
        isWillGoAround = false
        isGoAroundSet = false
        if (controlState == ControlState.ARRIVAL) {
            isActionRequired = true
            dataTag.startFlash()
            if (isSelected) ui.updateState()
        }
        dataTag.isMinimized = false
    }

    /** Check initial arrival spawn separation  */
    private fun checkArrival() {
        for (aircraft in radarScreen.aircrafts.values) {
            if (aircraft == this || aircraft is Departure) continue
            if (altitude - aircraft.altitude < 2500 && pixelToNm(distanceBetween(x, y, aircraft.x, aircraft.y)) < 6) {
                altitude = if (typDes - aircraft.typDes > 300) {
                    aircraft.altitude + 3500
                } else {
                    aircraft.altitude + 2500
                }
                updateClearedSpd(if (aircraft.clearedIas > 250) 250 else aircraft.clearedIas - 10)
                ias = clearedIas.toFloat()
                if (clearedAltitude < aircraft.clearedAltitude + 1000) {
                    updateClearedAltitude(aircraft.clearedAltitude + 1000)
                }
            }
        }
    }

    override fun updateILS(ils: ILS?) {
        if (this.ils !== ils && (this.ils !is LDA || ils == null)) isGoAroundSet = false //Reset only if ILS is not LDA or ILS is LDA but new ILS is null
        super.updateILS(ils)
    }

    override val sidStar: SidStar
        get() = star

    fun setStar(star: Star) {
        this.star = star
    }
}