package com.bombbird.terminalcontrol.entities.aircrafts

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.sidstar.Route
import com.bombbird.terminalcontrol.entities.sidstar.Sid
import com.bombbird.terminalcontrol.entities.sidstar.SidStar
import com.bombbird.terminalcontrol.ui.tabs.Tab
import com.bombbird.terminalcontrol.utilities.errors.IncompatibleSaveException
import com.bombbird.terminalcontrol.utilities.math.MathTools.componentInDirection
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.feetToNm
import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import org.json.JSONObject
import kotlin.math.pow
import kotlin.math.sqrt

class Departure : Aircraft {
    //Others
    private var sid: Sid
    var outboundHdg = 0
    val contactAlt: Int
    val handoverAlt: Int
    var isV2set: Boolean
        private set
    var isAccel: Boolean
        private set
    var isSidSet: Boolean
        private set
    var isContacted: Boolean
        private set
    var isHandedOver = false
        private set
    var cruiseAltTime: Float
        private set
    val cruiseAlt: Int
    var isHigherSpdSet: Boolean
        private set
    var isCruiseSpdSet: Boolean
        private set

    //Higher climb request
    var altitudeMaintainTime = 0f
        private set
    var isAskedForHigher = false
        private set

    constructor(callsign: String, icaoType: String, departure: Airport, runway: Runway, newSid: Sid) : super(callsign, icaoType, departure) {
        isOnGround = true
        contactAlt = airport.elevation + 2000 + MathUtils.random(-500, 200)
        handoverAlt = radarScreen.maxAlt + MathUtils.random(-800, -200)
        isV2set = false
        isAccel = false
        isSidSet = false
        isContacted = false
        cruiseAltTime = MathUtils.random(8, 20).toFloat()
        val maxAlt = AircraftType.getMaxCruiseAlt(icaoType)
        cruiseAlt = if (maxAlt >= 30000) MathUtils.random(30, maxAlt / 1000) * 1000 else MathUtils.random(radarScreen.maxAlt / 1000, maxAlt / 1000) * 1000
        isHigherSpdSet = false
        isCruiseSpdSet = false

        //Additional requests
        if (MathUtils.randomBoolean(0.1f) && !radarScreen.tutorial) {
            //10% chance to request shortcut/high speed climb, except in tutorial
            if (MathUtils.randomBoolean() && climbSpd > 250) {
                request = HIGH_SPEED_REQUEST
                requestAlt = MathUtils.random(airport.elevation + 4000, 9000)
            } else {
                request = SHORTCUT_REQUEST
                requestAlt = MathUtils.random(airport.elevation + 5000, radarScreen.maxAlt - 5000)
            }
        }

        //Sets requested runway for takeoff
        this.runway = runway

        //Gets a random SID
        sid = newSid
        if ("CAL641" == callsign && radarScreen.tutorial) {
            emergency.isEmergency = false
        }
        route = Route(this, sid, runway.name, typClimb)
        direct = route.getWaypoint(0)

        //Set initial IAS due to wind + 10 knots ground speed
        gs = 5f
        ias = airport.winds[1] * MathUtils.cosDeg(airport.winds[0] - runway.heading.toFloat()) + 10

        //Set initial altitude equal to runway elevation
        altitude = runway.elevation.toFloat()

        //Set initial position on runway
        var feetDist: Int = runway.feetLength
        if ("TCTT" == airport.icao && "16R" == runway.name) {
            //Special case for TCTT runway 16R intersection takeoff
            x = 2864.2f
            y = 1627.0f
            feetDist = 8559
        } else if ("TCHX" == airport.icao && "13" == runway.name) {
            //TCHX departure adjustment since runway data does not include threshold
            x = 2862.0f
            y = 1635.2f
        } else {
            x = runway.position[0]
            y = runway.position[1]
        }

        //Calculate max v2 speed to not overshoot, set to this value if smaller than original V2
        val u = 10 / 3600f //In nm/s
        val a = 3 / 3600f //In nm/s^2
        val s = feetToNm(feetDist.toFloat()) //In nm
        var maxV2 = (sqrt(u.toDouble().pow(2.0) + 2 * a * s) * 3600).toInt() //In nm/h (knots)
        maxV2 += componentInDirection(winds[1], winds[0], runway.heading).toInt()
        maxV2 = maxV2 / 5 * 5 //Round down to nearest 5 knots
        if (v2 > maxV2) v2 = maxV2
        loadLabel()
        navState = NavState(this)
        if (direct?.let { route.getWptFlyOver(it.name) } == true) direct?.setDepFlyOver() //Set the flyOver separately if is flyover waypoint
        updateControlState(ControlState.UNCONTROLLED)
        color = Color(0x11ff00ff)

        //Set takeoff heading
        heading = runway.heading.toDouble()
        track = heading - radarScreen.magHdgDev
        takeOff()
        initRadarPos()
    }

    constructor(save: JSONObject) : super(save) {
        if (save.isNull("route")) {
            throw IncompatibleSaveException("Old save format - route is null")
        }
        val route = save.getJSONObject("route")

        sid = airport.sids[save.getString("sid")] ?: Sid(airport, route.getJSONArray("waypoints"), route.getJSONArray("restrictions"), route.getJSONArray("flyOver"), route.optString("name", save.optString("sid", "null")))
        this.route = Route(route, sid, runway!!, typClimb)
        outboundHdg = save.getInt("outboundHdg")
        contactAlt = save.getInt("contactAlt")
        handoverAlt = save.getInt("handOverAlt")
        isV2set = save.getBoolean("v2set")
        isAccel = save.optBoolean("accel", false)
        isSidSet = save.getBoolean("sidSet")
        isContacted = save.getBoolean("contacted")
        isHandedOver = save.optBoolean("handedOver", !isArrivalDeparture && altitude > handoverAlt)
        cruiseAltTime = save.optDouble("cruiseAltTime", 1.0).toFloat()
        cruiseAlt = save.getInt("cruiseAlt")
        isHigherSpdSet = save.getBoolean("higherSpdSet")
        isCruiseSpdSet = save.getBoolean("cruiseSpdSet")
        altitudeMaintainTime = save.optDouble("altitudeMaintainTime", 0.0).toFloat()
        isAskedForHigher = save.optBoolean("askedForHigher", false)
        color = Color(0x11ff00ff)
        loadOtherLabelInfo(save)
    }

    private fun takeOff() {
        //Sets aircraft to takeoff mode
        runway?.let {
            airport.airborne = airport.airborne + 1
            updateClearedSpd(v2)
            super.updateSpd()
            updateClearedAltitude(it.initClimb)
            navState.clearedAlt.removeFirst()
            navState.clearedAlt.addFirst(clearedAltitude)
            clearedHeading = if (sid.getInitClimb(it.name)?.get(0) ?: -1 != -1) {
                sid.getInitClimb(it.name)?.get(0) ?: 360
            } else {
                it.heading
            }
            navState.clearedHdg.removeFirst()
            navState.clearedHdg.addFirst(clearedHeading)
            heading = it.heading.toDouble()
            isTkOfLdg = true
        }
    }

    override fun updateTkofLdg() {
        //Called to check for takeoff landing status
        if (ias > v2 - 10 && !isV2set) {
            isOnGround = false
            targetHeading = clearedHeading.toDouble()
            isV2set = true
            //Ensure the aircraft clears at least the first waypoint restriction to prevent nuisance alerts
            if (route.getWaypoint(0) != null && route.getWptMinAlt(0) != -1) {
                //Perform only if first waypoint has a minimum altitude
                val wpt = route.getWaypoint(0)
                val dist = if (wpt != null) pixelToNm(distanceBetween(x, y, wpt.posX.toFloat(), wpt.posY.toFloat())) else 0f //Distance in nm
                if (dist < 10) {
                    //Applicable only if initial waypoint is less than 10nm from liftoff
                    val estTime = dist / 220 * 60 //Assume average 220 knots speed
                    var minClimb = (route.getWptMinAlt(0) - altitude + 200) / estTime
                    if (minClimb > 3500) minClimb = 3500f
                    if (minClimb > typClimb) {
                        //Set the new climb speed only if minimum required is more than the actual aircraft climb speed
                        typClimb = minClimb.toInt()
                        maxClimb = typClimb + 800
                    }
                }
            }
        }
        if (altitude >= contactAlt && !isContacted) {
            updateControlState(ControlState.DEPARTURE)
            radarScreen.utilityBox.commsManager.initialContact(this)
            isActionRequired = true
            dataTag.startFlash()
            isContacted = true
        }
        if (altitude - airport.elevation > 1500 && !isAccel) {
            if (clearedIas == v2) {
                var speed = 220
                if (!isSidSet && route.getWptMaxSpd(route.getWaypoint(0)?.name) < 220) {
                    speed = route.getWptMaxSpd(0)
                } else if (isSidSet && route.getWptMaxSpd(direct?.name) < 220) {
                    speed = route.getWptMaxSpd(direct?.name)
                }
                if (speed == -1) speed = 220
                updateClearedSpd(speed)
                super.updateSpd()
            }
            isAccel = true
        }
        if ((sid.getInitClimb(runway?.name) == null || altitude >= sid.getInitClimb(runway?.name)?.get(1) ?: -1) && !isSidSet) {
            isSidSet = true
            updateAltRestrictions()
            updateTargetAltitude()
        }
        if (isContacted && isV2set && isSidSet && isAccel) {
            isTkOfLdg = false
        }
    }

    override fun update(): Double {
        val info = super.update()
        if (isHandedOver && cruiseAltTime > 0) {
            cruiseAltTime -= Gdx.graphics.deltaTime
            if (cruiseAltTime <= 0) {
                updateClearedAltitude(cruiseAlt)
                navState.replaceAllClearedAlt()
                if (radarScreen.handoverController.targetAltitudeList.containsKey(callsign)) radarScreen.handoverController.targetAltitudeList[callsign] = cruiseAlt
            }
        }
        if (checkHigherClimb()) {
            altitudeMaintainTime += Gdx.graphics.deltaTime
            if (!isAskedForHigher && altitudeMaintainTime > 180) {
                //Higher climb needed, request higher if have maintained cleared altitude for > 3 min
                isActionRequired = true
                dataTag.startFlash()
                ui.updateAckHandButton(this)
                radarScreen.utilityBox.commsManager.requestHigherClimb(this)
                isAskedForHigher = true
            }
        } else {
            //Otherwise reset the timer to 0, boolean to false in case another request is needed
            isAskedForHigher = false
            altitudeMaintainTime = 0f
        }
        return info
    }

    /** Checks whether the aircraft should request for higher climb  */
    private fun checkHigherClimb(): Boolean {
        //If altitude is within 100 feet below cleared altitude, cleared altitude is below maxAlt and departure is still in control by player
        return isArrivalDeparture && clearedAltitude >= altitude - 1 && clearedAltitude - altitude < 100 && clearedAltitude < radarScreen.maxAlt
    }

    /** Overrides method in Aircraft class to join the lines between each SID waypoint  */
    override fun drawSidStar() {
        //Draws line joining aircraft and sid/star track
        super.drawSidStar()
        navState.clearedDirect.last()?.let {
            route.joinLines(route.findWptIndex(it.name), route.size, outboundHdg)
            radarScreen.waypointManager.updateSidRestriction(route, route.findWptIndex(it.name), route.size)
        }
    }

    /** Overrides method in Aircraft class to join lines between each cleared SID waypoint  */
    override fun uiDrawSidStar() {
        super.uiDrawSidStar()
        route.joinLines(route.findWptIndex(Tab.clearedWpt), route.size, -1)
        radarScreen.waypointManager.updateSidRestriction(route, route.findWptIndex(Tab.clearedWpt), route.size)
    }

    /** Overrides method in Aircraft class to set to outbound heading  */
    override fun setAfterLastWpt() {
        clearedHeading = outboundHdg
        navState.replaceAllSidStarWithHdg(clearedHeading)
        removeSidStarMode()
    }

    override fun findNextTargetHdg(): Double {
        val result = super.findNextTargetHdg()
        return if (result < 0) outboundHdg.toDouble() else result
    }

    override fun updateAltRestrictions() {
        if (navState.dispLatMode.first() == NavState.SID_STAR) {
            //Aircraft on SID
            var highestAlt = -1
            if (direct != null) {
                highestAlt = route.getWptMaxAlt(direct?.name)
            }
            if (highestAlt > -1) {
                this.highestAlt = highestAlt
            } else if (isContacted && controlState == ControlState.DEPARTURE) {
                this.highestAlt = radarScreen.maxAlt
            } else {
                this.highestAlt = cruiseAlt
            }
            val nextFL: Int = if (altitude.toInt() % 1000 == 0) {
                altitude.toInt()
            } else {
                altitude.toInt() + 1000 - altitude.toInt() % 1000
            }
            if (lowestAlt < nextFL) {
                //If lowest alt value is less than the next flight level after current altitude that divisible by 10 (e.g. if at 5500 ft, next is 6000ft)
                lowestAlt = if (!isSidSet) {
                    //If still climbing on init climb
                    sid.getInitClimb(runway?.name)?.get(1) ?: radarScreen.minAlt
                } else {
                    nextFL
                }
            }
        }
    }

    override fun updateSpd() {
        if (!isHigherSpdSet && altitude >= 5000 && altitude > airport.elevation + 4000) {
            val waypointSpd = if (direct == null || navState.dispSpdMode.first() == NavState.NO_RESTR) -1 else route.getWptMaxSpd(direct?.name)
            if (clearedIas < 250 && (waypointSpd == -1 || waypointSpd >= 250)) {
                updateClearedSpd(250)
                super.updateSpd()
            } else if (clearedIas < waypointSpd) {
                updateClearedSpd(waypointSpd)
                super.updateSpd()
            }
            isHigherSpdSet = waypointSpd >= 250 || waypointSpd == -1
        }
        if (!isCruiseSpdSet && altitude >= 9999) {
            val waypointSpd = if (direct == null || navState.dispSpdMode.first() == NavState.NO_RESTR) -1 else route.getWptMaxSpd(direct?.name)
            if (clearedIas < climbSpd && waypointSpd == -1) {
                if (isSelected) {
                    Tab.notListening = true
                    val array = ui.spdTab.valueBox.list.items
                    array.add(climbSpd.toString())
                    ui.spdTab.valueBox.items = array
                    ui.spdTab.valueBox.selected = climbSpd.toString()
                    Tab.notListening = false
                }
                updateClearedSpd(climbSpd)
                super.updateSpd()
            } else if (clearedIas < waypointSpd) {
                updateClearedSpd(waypointSpd)
                super.updateSpd()
            }
            isCruiseSpdSet = waypointSpd == -1
        }
    }

    override fun updateAltitude(holdAlt: Boolean, fixedVs: Boolean) {
        super.updateAltitude(holdAlt, fixedVs)
        if (canHandover()) ui.updateAckHandButton(this)
        if (controlState == ControlState.DEPARTURE && altitude >= handoverAlt && navState.dispLatMode.first() == NavState.SID_STAR) {
            contactOther()
        }
        if (isArrivalDeparture && request != NO_REQUEST && !isRequested && altitude >= requestAlt) {
            //Ask for request when above trigger altitude
            if (request == SHORTCUT_REQUEST && remainingWaypoints.size <= 1) return  //Doesn't make sense for shortcut if less than 2 waypoints remaining
            isActionRequired = true
            dataTag.startFlash()
            ui.updateAckHandButton(this)
            radarScreen.utilityBox.commsManager.sayRequest(this)
            isRequested = true
        }
    }

    override fun canHandover(): Boolean {
        return controlState == ControlState.DEPARTURE && altitude >= radarScreen.maxAlt - 4000 && navState.dispLatMode.first() == NavState.SID_STAR
    }

    override fun contactOther() {
        updateControlState(ControlState.UNCONTROLLED)
        if (direct == null || route.getWptMaxSpd(direct?.name) == -1) updateClearedSpd(climbSpd)
        super.updateSpd()
        isHandedOver = true
        isExpedite = false
        if (expediteTime <= 120 && altitude > 10000.coerceAtMost(radarScreen.maxAlt - 6000)) radarScreen.setScore(radarScreen.score + 1)
        radarScreen.utilityBox.commsManager.contactFreq(this, sid.centre[0], sid.centre[1])
    }

    override val sidStar: SidStar
        get() = sid
}