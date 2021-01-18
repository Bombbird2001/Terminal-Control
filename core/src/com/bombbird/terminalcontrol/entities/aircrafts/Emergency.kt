package com.bombbird.terminalcontrol.entities.aircrafts

import com.bombbird.terminalcontrol.utilities.math.MathTools.pixelToNm
import com.bombbird.terminalcontrol.utilities.math.MathTools.distanceBetween
import com.bombbird.terminalcontrol.utilities.math.MathTools.modulateHeading
import com.bombbird.terminalcontrol.utilities.math.MathTools.withinRange
import com.bombbird.terminalcontrol.TerminalControl
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import org.apache.commons.lang3.ArrayUtils
import org.json.JSONObject

class Emergency {
    companion object {
        private val canDumpFuel = arrayOf("A332", "A333", "A339", "A342", "A343", "A345", "A346", "A359", "A35K", "A388", "B742", "B743", "B744", "B748", "B762", "B763", "B764",
                "B772", "B77L", "B773", "B77W", "B788", "B789", "B78X", "MD11")
    }

    private val radarScreen = TerminalControl.radarScreen!!

    enum class Type {
        MEDICAL, ENGINE_FAIL, BIRD_STRIKE, HYDRAULIC_FAIL, PRESSURE_LOSS, FUEL_LEAK
    }

    enum class Chance {
        OFF, LOW, MEDIUM, HIGH
    }

    private var aircraft: Aircraft
    var isEmergency = false
    var isActive: Boolean
    var type: Type
    var timeRequired //Preparation time required before dump/approach
            : Float
    var isChecklistsSaid //Whether aircraft has informed controller of running checklists, fuel dump
            : Boolean
    var isReadyForDump //Preparation complete
            : Boolean
    var fuelDumpLag //Time between preparation complete and fuel dump
            : Float
    var isDumpingFuel //Dumping fuel
            : Boolean
    val isFuelDumpRequired //Whether a fuel dump is required
            : Boolean
    var fuelDumpTime //Fuel dump time required before approach
            : Float
    var isRemainingTimeSaid //Whether aircraft has notified approach of remaining time
            : Boolean
    val sayRemainingTime //Time to notify approach of remaining time
            : Int

    /** Checks if aircraft is ready to conduct an ILS approach  */
    var isReadyForApproach //Ready for approach
            : Boolean
    var isStayOnRwy //Needs to stay on runway, close it after landing
            : Boolean
    var stayOnRwyTime //Time for staying on runway
            : Float
    val emergencyStartAlt //Altitude where emergency occurs
            : Int

    constructor(aircraft: Aircraft, emerChance: Chance) {
        this.aircraft = aircraft
        if (emerChance == Chance.OFF || aircraft !is Departure) {
            isEmergency = false
        } else {
            var chance = 0f
            if (emerChance == Chance.LOW) chance = 1 / 200f
            if (emerChance == Chance.MEDIUM) chance = 1 / 100f
            if (emerChance == Chance.HIGH) chance = 1 / 50f
            isEmergency = MathUtils.randomBoolean(chance) //Chance depends on emergency setting and must be a departure (for now)
        }
        isActive = false
        type = Type.values()[MathUtils.random(Type.values().size - 1)]
        timeRequired = MathUtils.random(300, 600).toFloat() //Between 5 to 10 minutes
        isChecklistsSaid = false
        isReadyForDump = false
        fuelDumpLag = MathUtils.random(30, 60).toFloat() //Between half to one minute of time between ready for dump and actual dump start
        isDumpingFuel = false
        isFuelDumpRequired = randomFuelDump()
        fuelDumpTime = if (isFuelDumpRequired) MathUtils.random(600, 900).toFloat() else 0.toFloat()
        isRemainingTimeSaid = false
        sayRemainingTime = (0.5f * fuelDumpTime / 60).toInt()
        isReadyForApproach = false
        isStayOnRwy = randomStayOnRwy()
        stayOnRwyTime = MathUtils.random(300, 600).toFloat() //Runway stays closed for 5-10 minutes
        emergencyStartAlt = randomEmerAlt()
    }

    constructor(aircraft: Aircraft, forceEmergency: Boolean) {
        //Special constructor used when you want to force an aircraft to have an emergency or not
        this.aircraft = aircraft
        isEmergency = forceEmergency
        isActive = false
        type = Type.values()[MathUtils.random(Type.values().size - 1)]
        timeRequired = MathUtils.random(300, 600).toFloat() //Between 5 to 10 minutes
        isChecklistsSaid = false
        isReadyForDump = false
        fuelDumpLag = MathUtils.random(30, 60).toFloat() //Between half to one minute of time between ready for dump and actual dump start
        isDumpingFuel = false
        isFuelDumpRequired = randomFuelDump()
        fuelDumpTime = if (isFuelDumpRequired) MathUtils.random(600, 900).toFloat() else 0.toFloat()
        isRemainingTimeSaid = false
        sayRemainingTime = (0.5f * fuelDumpTime / 60).toInt()
        isReadyForApproach = false
        isStayOnRwy = randomStayOnRwy()
        stayOnRwyTime = MathUtils.random(300, 600).toFloat() //Runway stays closed for 5-10 minutes
        emergencyStartAlt = randomEmerAlt()
    }

    constructor(aircraft: Aircraft, save: JSONObject) {
        this.aircraft = aircraft
        isEmergency = save.getBoolean("emergency")
        isActive = save.getBoolean("active")
        type = Type.valueOf(save.getString("type"))
        timeRequired = save.getDouble("timeRequired").toFloat()
        isChecklistsSaid = save.optBoolean("checklistsSaid")
        isReadyForDump = save.optBoolean("readyForDump")
        fuelDumpLag = save.optDouble("fuelDumpLag", MathUtils.random(30, 60).toDouble()).toFloat()
        isDumpingFuel = save.optBoolean("dumpingFuel")
        isFuelDumpRequired = save.getBoolean("fuelDumpRequired")
        fuelDumpTime = save.getDouble("fuelDumpTime").toFloat()
        isRemainingTimeSaid = save.optBoolean("remainingTimeSaid")
        sayRemainingTime = save.optInt("sayRemainingTime", (fuelDumpTime / 120).toInt())
        isReadyForApproach = save.getBoolean("readyForApproach")
        isStayOnRwy = save.getBoolean("stayOnRwy")
        stayOnRwyTime = save.optDouble("stayOnRwyTime", MathUtils.random(300, 600).toDouble()).toFloat()
        emergencyStartAlt = save.getInt("emergencyStartAlt")
    }

    fun update() {
        if (!isEmergency) return
        val dt = Gdx.graphics.deltaTime
        if (aircraft is Departure) {
            //Outbound emergency
            if (aircraft.altitude > emergencyStartAlt && !isActive) {
                //Initiate the emergency
                aircraft.dataTag.setEmergency()
                cancelSidStar()
                isActive = true
                radarScreen.planesToControl = radarScreen.planesToControl.coerceAtMost(5f)
                aircraft.dataTag.isMinimized = false
                if (type == Type.BIRD_STRIKE || type == Type.ENGINE_FAIL) {
                    aircraft.typClimb = (aircraft.typClimb * 0.5).toInt()
                    aircraft.maxClimb = (aircraft.maxClimb * 0.5).toInt()
                }
                sayEmergency()
                //Create new arrival with same callsign and everything, remove old departure
                if (aircraft.isSelected) {
                    radarScreen.ui.setSelectedPane(null)
                }
                val arrival = Arrival((aircraft as Departure))
                aircraft.removeAircraft()
                radarScreen.aircrafts[arrival.callsign] = arrival
                radarScreen.separationChecker.updateAircraftPositions()
                if (aircraft.isSelected) {
                    radarScreen.ui.setSelectedPane(arrival)
                    arrival.isSelected = true
                    aircraft.isSelected = false
                }
                aircraft = arrival
                radarScreen.arrivals = radarScreen.arrivals + 1
                return
            }
        }
        if (isActive) {
            //Emergency ongoing
            timeRequired -= dt
            if (timeRequired < 180 && !isChecklistsSaid) {
                //When aircraft needs 3 more minutes, inform controller of remaining time and whether fuel dump is required
                isChecklistsSaid = true
                sayRunChecklists()
            }
            if (timeRequired < 0 && !isReadyForDump) {
                //Preparation time over
                isReadyForDump = true
                if (isFuelDumpRequired) {
                    sayReadyForDump()
                }
            }
            if (isReadyForDump) {
                //Ready for fuel dumping (if available) or approach
                fuelDumpLag -= dt
            }
            if (isFuelDumpRequired && !isDumpingFuel && fuelDumpLag < 0) {
                //Aircraft is dumping fuel
                isDumpingFuel = true
                sayDumping()
            }
            if (isDumpingFuel) {
                //Fuel dump ongoing
                fuelDumpTime -= dt
                if (!isRemainingTimeSaid && fuelDumpTime <= sayRemainingTime * 60) {
                    isRemainingTimeSaid = true
                    sayRemainingDumpTime()
                }
            }
            if (!isReadyForApproach && fuelDumpTime <= 0 && timeRequired <= 0) {
                isReadyForApproach = true
                if (aircraft.isSelected) {
                    aircraft.updateUISelections()
                    aircraft.ui.updateState()
                }
                sayReadyForApproach()
            }
            if (aircraft.isTkOfLdg && isStayOnRwy && stayOnRwyTime > 0) {
                //Aircraft has touched down and needs to stay on runway
                if (aircraft.ils == null) {
                    isStayOnRwy = false
                    stayOnRwyTime = -1f
                } else {
                    stayOnRwyTime -= dt
                    val rwy = aircraft.airport.runways[aircraft.ils?.name?.substring(3)] ?: return
                    if (!rwy.isEmergencyClosed) {
                        rwy.isEmergencyClosed = true
                        rwy.oppRwy.isEmergencyClosed = true
                        radarScreen.utilityBox.commsManager.normalMsg("Runway ${rwy.name} is now closed")
                        radarScreen.utilityBox.commsManager.normalMsg("Emergency vehicles are proceeding onto runway ${rwy.name}")
                    }
                    if (stayOnRwyTime < 0) {
                        rwy.isEmergencyClosed = false
                        rwy.oppRwy.isEmergencyClosed = false
                        radarScreen.utilityBox.commsManager.normalMsg("Emergency vehicles and subject aircraft have vacated runway ${rwy.name}")
                        radarScreen.utilityBox.commsManager.normalMsg("Runway ${rwy.name} is now open")
                        aircraft.airport.updateRunwayUsage()
                        isStayOnRwy = false
                    }
                }
            }
        }
    }

    /** Changes SID/STAR mode to vector mode after emergency occurs  */
    private fun cancelSidStar() {
        aircraft.navState.updateLatModes(NavState.REMOVE_ALL_SIDSTAR, false)
        aircraft.navState.updateAltModes(NavState.REMOVE_SIDSTAR_RESTR, false)
        aircraft.navState.updateSpdModes(NavState.REMOVE_SIDSTAR_RESTR, true) //UpdateUI only after all 3 modes are changed
        aircraft.navState.dispLatMode.clear()
        aircraft.navState.dispLatMode.addFirst(NavState.VECTORS)
        aircraft.navState.dispAltMode.clear()
        aircraft.navState.dispAltMode.addFirst(if (type == Type.PRESSURE_LOSS) NavState.EXPEDITE else NavState.NO_RESTR)
        aircraft.navState.dispSpdMode.clear()
        aircraft.navState.dispSpdMode.addFirst(NavState.NO_RESTR)
        aircraft.navState.clearedHdg.clear()
        aircraft.navState.clearedHdg.addFirst(aircraft.heading.toInt())
        aircraft.navState.clearedDirect.clear()
        aircraft.navState.clearedDirect.addFirst(null)
        aircraft.navState.clearedAftWpt.clear()
        aircraft.navState.clearedAftWpt.addFirst(null)
        aircraft.navState.clearedAftWptHdg.clear()
        aircraft.navState.clearedAftWptHdg.addFirst(aircraft.heading.toInt())
        aircraft.navState.clearedHold.clear()
        aircraft.navState.clearedHold.addFirst(null)
        aircraft.navState.clearedIls.clear()
        aircraft.navState.clearedIls.addFirst(null)
        aircraft.navState.clearedAlt.clear()
        aircraft.navState.clearedAlt.addFirst(getClearedAltitudeAfterEmergency(aircraft.altitude))
        aircraft.navState.clearedExpedite.clear()
        aircraft.navState.clearedExpedite.addFirst(type == Type.PRESSURE_LOSS)
        aircraft.navState.clearedSpd.clear()
        aircraft.navState.clearedSpd.addFirst(aircraft.clearedIas)
        aircraft.navState.length = 1
        aircraft.navState.updateAircraftInfo()
    }

    /** Returns a random boolean for fuel dump given the aircraft is capable of doing so, else returns false  */
    private fun randomFuelDump(): Boolean {
        return if (ArrayUtils.contains(canDumpFuel, aircraft.icaoType)) MathUtils.randomBoolean() else false
    }

    /** Returns a random boolean for whether aircraft stays on runway after landing given the type of emergency  */
    private fun randomStayOnRwy(): Boolean {
        return when (type) {
            Type.BIRD_STRIKE, Type.ENGINE_FAIL -> MathUtils.randomBoolean(0.7f)
            Type.HYDRAULIC_FAIL, Type.FUEL_LEAK -> true
            Type.MEDICAL -> false
            Type.PRESSURE_LOSS -> MathUtils.randomBoolean(0.3f)
        }
    }

    /** Returns a random altitude when emergency occurs depending on emergency type  */
    private fun randomEmerAlt(): Int {
        if (aircraft is Departure) {
            val elevation = aircraft.airport.elevation
            if (type == Type.BIRD_STRIKE) {
                return if (elevation > 7000) MathUtils.random(2300, 1500) + elevation else MathUtils.random(2300, 7000) + elevation
            }
            if (type == Type.ENGINE_FAIL) return MathUtils.random(2300, 5000) + elevation
            if (type == Type.HYDRAULIC_FAIL) return MathUtils.random(2300, 5000) + elevation
            if (type == Type.MEDICAL) return MathUtils.random(4000, 14000) + elevation
            if (type == Type.PRESSURE_LOSS) return MathUtils.random(12000, 14500)
            if (type == Type.FUEL_LEAK) return MathUtils.random(11000, 14500)
        }
        return -1
    }

    /** Returns the altitude the aircraft is targeting after emergency starts depending on emergency type  */
    private fun getClearedAltitudeAfterEmergency(currentAlt: Float): Int {
        if (aircraft is Departure) {
            val elevation = aircraft.airport.elevation
            val minLevelOffAlt = elevation + 2000 //Minimum altitude to maintain is 2000 feet AGL
            if (type == Type.BIRD_STRIKE) return if (currentAlt > minLevelOffAlt) ((currentAlt / 1000).toInt() + 1) * 1000 else ((minLevelOffAlt / 1000f).toInt() + 1) * 1000
            if (type == Type.ENGINE_FAIL) return if (currentAlt > minLevelOffAlt) ((currentAlt / 1000).toInt() + 1) * 1000 else ((minLevelOffAlt / 1000f).toInt() + 1) * 1000
            if (type == Type.HYDRAULIC_FAIL) return if (currentAlt > minLevelOffAlt) ((currentAlt / 1000).toInt() + 1) * 1000 else ((minLevelOffAlt / 1000f).toInt() + 1) * 1000
            if (type == Type.MEDICAL) return if (currentAlt > minLevelOffAlt) ((currentAlt / 1000).toInt() + 1) * 1000 else ((minLevelOffAlt / 1000f).toInt() + 1) * 1000
            if (type == Type.PRESSURE_LOSS) return 9000
            if (type == Type.FUEL_LEAK) return if (currentAlt > minLevelOffAlt) ((currentAlt / 1000).toInt() + 1) * 1000 else ((minLevelOffAlt / 1000f).toInt() + 1) * 1000
        }
        return -1
    }

    /** Adds comm box message, TTS when aircraft initially encounters emergency  */
    private fun sayEmergency() {
        var emergency = ""
        if (type == Type.BIRD_STRIKE) emergency = "an emergency due to bird strike"
        if (type == Type.ENGINE_FAIL) emergency = "an emergency due to engine failure"
        if (type == Type.HYDRAULIC_FAIL) emergency = "an emergency due to hydraulic failure"
        if (type == Type.MEDICAL) emergency = "a medical emergency"
        if (type == Type.PRESSURE_LOSS) emergency = "an emergency due to loss of cabin pressure"
        if (type == Type.FUEL_LEAK) emergency = "an emergency due to fuel leak"
        val intent: String = if (type == Type.PRESSURE_LOSS) {
            ", we are initiating an emergency descent to 9000 feet"
        } else {
            val altitude = if (aircraft.clearedAltitude >= radarScreen.transLvl * 100) "FL" + aircraft.clearedAltitude / 100 else aircraft.clearedAltitude.toString() + " feet"
            ", levelling off at $altitude"
        }
        val text = "Mayday, mayday, mayday, " + aircraft.callsign + aircraft.wakeString + " is declaring " + emergency + " and would like to return to the airport" + intent
        radarScreen.utilityBox.commsManager.warningMsg(text)
        TerminalControl.ttsManager.sayEmergency(aircraft, emergency, intent)
    }

    /** Adds comm box message, TTS to notify controller of intentions, whether fuel dump is required  */
    private fun sayRunChecklists() {
        radarScreen.utilityBox.commsManager.warningMsg(aircraft.callsign + aircraft.wakeString + " will need a few more minutes to run checklists" + if (isFuelDumpRequired) " before dumping fuel" else "")
        TerminalControl.ttsManager.sayRemainingChecklists(aircraft, isFuelDumpRequired)
    }

    /** Adds comm box message, TTS when aircraft is ready to dump fuel  */
    private fun sayReadyForDump() {
        radarScreen.utilityBox.commsManager.warningMsg(aircraft.callsign + aircraft.wakeString + ", we are ready to dump fuel")
        var bearing = 90 - MathUtils.radiansToDegrees * MathUtils.atan2(aircraft.y - 1620, aircraft.x - 2880)
        val dist = pixelToNm(distanceBetween(aircraft.x, aircraft.y, 2880f, 1620f)).toInt()
        var dir = ""
        bearing = modulateHeading(bearing.toDouble()).toFloat()
        if (bearing >= 330 || bearing < 30) dir = "north"
        if (withinRange(bearing, 30f, 60f)) dir = "north-east"
        if (withinRange(bearing, 60f, 120f)) dir = "east"
        if (withinRange(bearing, 120f, 150f)) dir = "south-east"
        if (withinRange(bearing, 150f, 210f)) dir = "south"
        if (withinRange(bearing, 210f, 240f)) dir = "south-west"
        if (withinRange(bearing, 240f, 300f)) dir = "west"
        if (withinRange(bearing, 300f, 330f)) dir = "north-west"
        val alt = if (aircraft.altitude >= radarScreen.transLvl) ((aircraft.altitude / 100).toInt() * 100).toString() + " feet" else "FL" + (aircraft.altitude / 100).toInt()
        radarScreen.utilityBox.commsManager.normalMsg("Attention all aircraft, fuel dumping in progress " + dist + " miles " + dir + " of " + radarScreen.mainName + ", " + alt)
        TerminalControl.ttsManager.sayReadyForDump(aircraft)
    }

    /** Adds comm box message, TTS when aircraft starts dumping fuel  */
    private fun sayDumping() {
        radarScreen.utilityBox.commsManager.warningMsg(aircraft.callsign + aircraft.wakeString + " is now dumping fuel")
        TerminalControl.ttsManager.sayDumping(aircraft)
    }

    /** Adds comm box message, TTS when aircraft is halfway through fuel dump  */
    private fun sayRemainingDumpTime() {
        radarScreen.utilityBox.commsManager.warningMsg(aircraft.callsign + aircraft.wakeString + ", we'll need about " + sayRemainingTime + " more minutes")
        TerminalControl.ttsManager.sayRemainingDumpTime(aircraft, sayRemainingTime)
    }

    /** Adds comm box message, TTS when aircraft has finished dumping fuel, is ready for approach  */
    private fun sayReadyForApproach() {
        radarScreen.utilityBox.commsManager.warningMsg(aircraft.callsign + aircraft.wakeString + " is ready for approach" + if (isStayOnRwy) ", we will stay on the runway after landing" else "")
        TerminalControl.ttsManager.sayReadyForApproach(aircraft, isStayOnRwy)
    }
}