package com.bombbird.terminalcontrol.entities.aircrafts

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Queue
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.approaches.Approach
import com.bombbird.terminalcontrol.entities.sidstar.Route
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import com.bombbird.terminalcontrol.ui.Ui
import com.bombbird.terminalcontrol.utilities.Revision
import org.apache.commons.lang3.ArrayUtils
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

class NavState {
    companion object {
        //Update available mode codes
        const val REMOVE_ALL_SIDSTAR = 0 //Removes all SID/STAR choices - SID, STAR, after waypoint fly heading, hold at
        const val REMOVE_AFTERHDG_HOLD = 1 //Removes after waypoint fly heading, hold at
        const val REMOVE_SIDSTAR_ONLY = 2 //Removes only the SID, STAR mode
        const val REMOVE_SIDSTAR_AFTERHDG = 3 //Removes SID, STAR, after waypoint fly heading
        const val REMOVE_HOLD_ONLY = 4 //Removes only hold at
        const val REMOVE_AFTERHDG_ONLY = 5 //Removes only after waypoint fly heading
        const val ADD_ALL_SIDSTAR = 6 //Adds all SID/STAR choices - SID, STAR, after waypoint fly heading, hold at
        const val REMOVE_CHANGE_STAR = 7 //Removes change STAR choice
        const val ADD_CHANGE_STAR = 8 //Adds change STAR choice
        const val ADD_SIDSTAR_ONLY = 9 //Adds SID/STAR option only
        const val REMOVE_SIDSTAR_RESTR = 10 //Removes SID/STAR alt/speed restrictions
        const val ADD_SIDSTAR_RESTR_UNRESTR = 11 //Adds all modes
        const val REMOVE_UNRESTR = 12 //Removes unrestricted mode
        const val ADD_UNRESTR_ONLY = 13 //Adds unrestricted mode only
        const val ADD_SIDSTAR_RESTR_ONLY = 14 //Adds SID/STAR restrictions only

        //NavState codes
        private const val UNKNOWN_STATE = -1

        //Lateral modes
        const val SID_STAR = 20
        const val AFTER_WPT_HDG = 21
        const val VECTORS = 22
        const val HOLD_AT = 25
        const val CHANGE_STAR = 26

        //Turn directions - separate from lateral mode
        const val TURN_LEFT = 23
        const val TURN_RIGHT = 24
        const val NO_DIRECTION = 27

        //Altitude/Speed modes
        const val SID_STAR_RESTR = 30
        const val NO_RESTR = 31

        //Expedite - separate from altitude mode
        const val EXPEDITE = 32

        /** Gets the appropriate navState code from string  */

        fun getCodeFromString(string: String): Int {
            return if (string.contains("arrival") || string.contains("departure")) {
                SID_STAR
            } else if (Ui.AFTER_WPT_FLY_HDG == string) {
                AFTER_WPT_HDG
            } else if (Ui.FLY_HEADING == string) {
                VECTORS
            } else if (Ui.LEFT_HEADING == string) {
                VECTORS //No longer used, defaults to fly heading
            } else if (Ui.RIGHT_HEADING == string) {
                VECTORS //No longer used, defaults to fly heading
            } else if (Ui.HOLD_AT == string) {
                HOLD_AT
            } else if (Ui.CLIMB_VIA_SID == string || Ui.DESCEND_VIA_STAR == string || Ui.SID_SPD_RESTRICTIONS == string || Ui.STAR_SPD_RESTRICTIONS == string) {
                SID_STAR_RESTR
            } else if (Ui.CLIMB_DESCEND_TO == string || Ui.NO_SPD_RESTRICTIONS == string) {
                NO_RESTR
            } else if (Ui.EXPEDITE_TO == string) {
                NO_RESTR //No longer used, defaults to no restriction
            } else if (Ui.CHANGE_STAR == string) {
                CHANGE_STAR
            } else {
                //No such code
                Gdx.app.log("Navstate", "Unknown navState string $string")
                UNKNOWN_STATE
            }
        }
    }

    var aircraft: Aircraft
    val latModes: Array<String>
    val altModes: Array<String>
    val spdModes: Array<String>
    val timeQueueArray: Array<Float>

    //Modes used for display
    val dispLatMode: Queue<Int>
    val dispAltMode: Queue<Int>
    val dispSpdMode: Queue<Int>
    val clearedHdg: Queue<Int>
    val clearedDirect: Queue<Waypoint?>
    val clearedAftWpt: Queue<Waypoint?>
    val clearedAftWptHdg: Queue<Int>
    val clearedHold: Queue<Waypoint?>
    val clearedApch: Queue<Approach?>
    val clearedNewStar: Queue<String?>
    val clearedTurnDir: Queue<Int>
    val clearedAlt: Queue<Int>
    val clearedExpedite: Queue<Boolean>
    var clearedSpd: Queue<Int>
        private set
    val goAround: Queue<Boolean>
    var length = 1
    private val radarScreen = TerminalControl.radarScreen!!

    constructor(aircraft: Aircraft) {
        this.aircraft = aircraft
        altModes = Array(5)
        spdModes = Array(3)
        when (aircraft) {
            is Arrival -> {
                //Arrival
                latModes = Array(6)
                latModes.add(aircraft.sidStar.name + " arrival", Ui.AFTER_WPT_FLY_HDG, Ui.FLY_HEADING)
                latModes.add(Ui.HOLD_AT)
                if (!radarScreen.tutorial) latModes.add(Ui.CHANGE_STAR)
                altModes.add(Ui.DESCEND_VIA_STAR)
                spdModes.add(Ui.STAR_SPD_RESTRICTIONS)
            }
            is Departure -> {
                //Departure
                latModes = Array(4)
                latModes.add(aircraft.sidStar.name + " departure", Ui.FLY_HEADING)
                altModes.add(Ui.CLIMB_VIA_SID)
                spdModes.add(Ui.SID_SPD_RESTRICTIONS)
            }
            else -> {
                //Nani
                Gdx.app.log("Navstate type error", "Unknown navstate type specified!")
                latModes = Array(1)
            }
        }
        altModes.add(Ui.CLIMB_DESCEND_TO)
        spdModes.add(Ui.NO_SPD_RESTRICTIONS)
        dispLatMode = Queue()
        dispLatMode.addLast(SID_STAR)
        dispAltMode = Queue()
        dispAltMode.addLast(SID_STAR_RESTR)
        dispSpdMode = Queue()
        dispSpdMode.addLast(SID_STAR_RESTR)
        timeQueueArray = Array()
        clearedHdg = Queue()
        clearedHdg.addLast(aircraft.clearedHeading)
        clearedDirect = Queue()
        clearedDirect.addLast(aircraft.direct)
        clearedAftWpt = Queue()
        clearedAftWpt.addLast(aircraft.afterWaypoint)
        clearedAftWptHdg = Queue()
        clearedAftWptHdg.addLast(aircraft.afterWptHdg)
        clearedHold = Queue()
        clearedHold.addFirst(null)
        clearedApch = Queue()
        clearedApch.addLast(null)
        clearedNewStar = Queue()
        clearedNewStar.addLast(null)
        clearedTurnDir = Queue()
        clearedTurnDir.addLast(NO_DIRECTION)
        clearedAlt = Queue()
        clearedAlt.addLast(aircraft.clearedAltitude)
        clearedExpedite = Queue()
        clearedExpedite.addLast(aircraft.isExpedite)
        clearedSpd = Queue()
        if (aircraft is Departure) {
            clearedSpd.addLast(aircraft.v2)
        } else {
            clearedSpd.addLast(aircraft.climbSpd)
        }
        goAround = Queue()
        goAround.addLast(false)
    }

    constructor(aircraft: Aircraft, save: JSONObject) {
        this.aircraft = aircraft
        latModes = Array()
        altModes = Array()
        spdModes = Array()
        timeQueueArray = Array()
        dispLatMode = Queue()
        dispAltMode = Queue()
        dispSpdMode = Queue()
        clearedHdg = Queue()
        clearedDirect = Queue()
        clearedAftWpt = Queue()
        clearedAftWptHdg = Queue()
        clearedHold = Queue()
        clearedApch = Queue()
        clearedNewStar = Queue()
        clearedTurnDir = Queue()
        clearedAlt = Queue()
        clearedExpedite = Queue()
        clearedSpd = Queue()
        goAround = Queue()
        length = save.getInt("length")
        val array = save.getJSONArray("latModes")
        for (i in 0 until array.length()) {
            latModes.add(array.getString(i))
        }
        val array1 = save.getJSONArray("altModes")
        for (i in 0 until array1.length()) {
            val mode: String = array1.getString(i)
            if (Ui.EXPEDITE_TO == mode) {
                altModes.add(Ui.CLIMB_DESCEND_TO)
            } else altModes.add(array1.getString(i))
        }
        val array2 = save.getJSONArray("spdModes")
        for (i in 0 until array2.length()) {
            spdModes.add(array2.getString(i))
        }
        val array3 = save.getJSONArray("timeQueue")
        for (i in 0 until array3.length()) {
            timeQueueArray.add(array3.getDouble(i).toFloat())
        }
        if (radarScreen.revision < Revision.NAVSTATE_REVISION) {
            //Old navstate format, parse to fit
            addToQueueIntParseFromString(save.getJSONArray("dispLatMode"), dispLatMode)
            addToQueueIntParseFromString(save.getJSONArray("dispAltMode"), dispAltMode)
            addToQueueIntParseFromString(save.getJSONArray("dispSpdMode"), dispSpdMode)
        } else {
            //New format
            addToQueueInt(save.getJSONArray("dispLatMode"), dispLatMode)
            addToQueueInt(save.getJSONArray("dispAltMode"), dispAltMode)
            addToQueueInt(save.getJSONArray("dispSpdMode"), dispSpdMode)
        }
        replaceAllTurnHdgModes() //Replace any old turn left/right modes to fly heading mode
        addToQueueInt(save.getJSONArray("clearedHdg"), clearedHdg)
        addToQueueWpt(save.getJSONArray("clearedDirect"), clearedDirect)
        addToQueueWpt(save.getJSONArray("clearedAftWpt"), clearedAftWpt)
        addToQueueInt(save.getJSONArray("clearedAftWptHdg"), clearedAftWptHdg)
        addToQueueWpt(save.getJSONArray("clearedHold"), clearedHold)
        val array4 = save.getJSONArray("clearedIls")
        for (i in 0 until array4.length()) {
            clearedApch.addLast(if (array4.isNull(i)) null else aircraft.airport.approaches[array4.getString(i).substring(3)])
        }
        if (save.isNull("newStar")) {
            clearedNewStar.addLast(null)
            fillUpString(clearedNewStar)
        } else {
            addToQueueString(save.getJSONArray("clearedNewStar"), clearedNewStar)
        }
        if (save.isNull("clearedTurnDir")) {
            clearedTurnDir.addLast(NO_DIRECTION)
            fillUpInt(clearedTurnDir)
        } else {
            addToQueueInt(save.getJSONArray("clearedTurnDir"), clearedTurnDir)
        }
        addToQueueInt(save.getJSONArray("clearedAlt"), clearedAlt)
        addToQueueBool(save.getJSONArray("clearedExpedite"), clearedExpedite)
        addToQueueInt(save.getJSONArray("clearedSpd"), clearedSpd)
        addToQueueBool(save.getJSONArray("goAround"), goAround)
    }

    /** Adds all elements in string array to string queue  */
    private fun addToQueueIntParseFromString(array: JSONArray, queue: Queue<Int>) {
        for (i in 0 until array.length()) {
            queue.addLast(if (array.isNull(i)) -1 else getCodeFromString(array.getString(i)))
        }
    }

    /** Adds all elements in string array to string queue  */
    private fun addToQueueString(array: JSONArray, queue: Queue<String?>) {
        for (i in 0 until array.length()) {
            queue.addLast(if (array.isNull(i)) null else array.getString(i))
        }
    }

    /** Adds all elements in int array to int queue  */
    private fun addToQueueInt(array: JSONArray, queue: Queue<Int>) {
        for (i in 0 until array.length()) {
            queue.addLast(if (array.isNull(i)) -1 else array.getInt(i))
            if (queue.last() != -1 && queue.last() == EXPEDITE) {
                queue.removeLast()
                queue.addLast(NO_RESTR)
            }
        }
    }

    /** Adds all elements in bool array to bool queue  */
    private fun addToQueueBool(array: JSONArray, queue: Queue<Boolean>) {
        for (i in 0 until array.length()) {
            queue.addLast(if (array.isNull(i)) null else array.getBoolean(i))
        }
    }

    /** Adds all elements in wpt array to wpt queue  */
    private fun addToQueueWpt(array: JSONArray, queue: Queue<Waypoint?>) {
        for (i in 0 until array.length()) {
            queue.addLast(if (array.isNull(i)) null else radarScreen.waypoints[array.getString(i)])
        }
    }

    /** Adds the time delay to keep track of when to send instructions  */
    fun updateState() {
        var min = 2f
        var max = 4f
        if (timeQueueArray.size > 1) {
            min = min.coerceAtLeast(timeQueueArray.peek() + 0.1f) //Ensure instructions are always executed in order - last instruction will always be carried out last
            if (min > max) max = min + 0.1f //If min time required exceeds max time, then make max time 0.1s longer than min time
        }
        timeQueueArray.add(MathUtils.random(min, max))
    }

    /** When called updates the aircraft's intentions (i.e. after reaction time has passed)  */
    private fun sendInstructions() {
        if (!goAround[1] && aircraft.isGoAround) {
            //Do not send inputs if aircraft went around during delay
            dispLatMode.removeIndex(1)
            dispAltMode.removeIndex(1)
            dispSpdMode.removeIndex(1)
            clearedHdg.removeIndex(1)
            clearedDirect.removeIndex(1)
            clearedAftWpt.removeIndex(1)
            clearedAftWptHdg.removeIndex(1)
            clearedHold.removeIndex(1)
            clearedApch.removeIndex(1)
            clearedNewStar.removeIndex(1)
            clearedTurnDir.removeIndex(1)
            clearedAlt.removeIndex(1)
            clearedExpedite.removeIndex(1)
            clearedSpd.removeIndex(1)
            goAround.removeIndex(1)
        } else {
            validateInputs()
            if (dispLatMode.size > 1) dispLatMode.removeFirst()
            if (dispAltMode.size > 1) dispAltMode.removeFirst()
            if (dispSpdMode.size > 1) dispSpdMode.removeFirst()
            if (clearedHdg.size > 1) clearedHdg.removeFirst()
            if (clearedDirect.size > 1) clearedDirect.removeFirst()
            if (clearedAftWpt.size > 1) clearedAftWpt.removeFirst()
            if (clearedAftWptHdg.size > 1) clearedAftWptHdg.removeFirst()
            if (clearedHold.size > 1) clearedHold.removeFirst()
            if (clearedApch.size > 1) clearedApch.removeFirst()
            if (clearedNewStar.size > 1) clearedNewStar.removeFirst()
            if (clearedTurnDir.size > 1) clearedTurnDir.removeFirst()
            if (clearedAlt.size > 1) clearedAlt.removeFirst()
            if (clearedExpedite.size > 1) clearedExpedite.removeFirst()
            if (clearedSpd.size > 1) clearedSpd.removeFirst()
            if (goAround.size > 1) goAround.removeFirst()
            updateAircraftInfo()
        }
        if (length > 1) length--
    }

    /** Sets the direct aircraft navigation states  */
    fun updateAircraftInfo() {
        aircraft.clearedHeading = clearedHdg.first()
        aircraft.direct = clearedDirect.first()
        aircraft.direct?.name?.let { aircraft.sidStarIndex = aircraft.route.findWptIndex(it) }
        aircraft.afterWaypoint = clearedAftWpt.first()
        aircraft.afterWptHdg = clearedAftWptHdg.first()
        aircraft.holdWpt = clearedHold.first()
        aircraft.updateApch(clearedApch.first())
        if (clearedApch.first() != null && dispLatMode.first() == SID_STAR) {
            var lowestAlt = aircraft.route.getWptMinAlt(aircraft.route.size - 1)
            if (lowestAlt == -1) lowestAlt = radarScreen.minAlt
            aircraft.updateClearedAltitude(lowestAlt)
            replaceAllClearedAlt()
        } else {
            var alt = clearedAlt.first()
            var replace = false
            if ((dispLatMode.first() == VECTORS || clearedApch.first() == null) && alt < radarScreen.minAlt) {
                alt = radarScreen.minAlt
                replace = true
            }
            aircraft.updateClearedAltitude(alt)
            if (replace) replaceAllClearedAlt()
        }
        aircraft.isExpedite = clearedExpedite.first()
        if (aircraft is Arrival || aircraft is Departure && (aircraft as Departure).isSidSet) {
            aircraft.updateClearedSpd(clearedSpd.first())
        }
        if (aircraft is Arrival && clearedNewStar.first() != null) {
            if (!aircraft.isLocCap && aircraft.apch == null) {
                val newStar = aircraft.airport.stars[clearedNewStar.first()?.split(" ".toRegex())?.toTypedArray()?.get(0)]
                newStar?.let {
                    (aircraft as Arrival).setStar(newStar)
                    if (!dispLatMode.isEmpty && !containsCode(dispLatMode.first(), VECTORS)) {
                        dispLatMode.removeFirst()
                        dispLatMode.addFirst(VECTORS)
                    }
                    aircraft.route = Route(aircraft, newStar, true)
                    aircraft.direct = null
                    aircraft.afterWaypoint = null
                    aircraft.afterWptHdg = aircraft.clearedHeading
                    aircraft.sidStarIndex = 0
                    clearedNewStar.removeFirst()
                    clearedNewStar.addFirst(null)
                    updateLatModes(ADD_ALL_SIDSTAR, false)
                    updateLatModes(REMOVE_AFTERHDG_HOLD, true)
                    radarScreen.utilityBox.commsManager.alertMsg("The STAR for " + aircraft.callsign + " has been changed to " + newStar.name + ". You may clear the aircraft to a waypoint on the new STAR.")
                }
            } else {
                clearedNewStar.removeFirst()
                clearedNewStar.addFirst(null)
                radarScreen.utilityBox.commsManager.alertMsg("The STAR for " + aircraft.callsign + " cannot be changed when cleared for approach.")
            }
        }
    }

    /** Called before updating aircraft mode to ensure inputs are valid in case aircraft state changes during the pilot delay */
    private fun validateInputs() {
        if (dispLatMode.size < 2 || clearedDirect.size < 2 || clearedAftWptHdg.size < 2 || clearedHdg.size < 2) return
        val currentDispLatMode = dispLatMode.first()
        val clearedDispLatMode = dispLatMode[1]
        val currentDirect = clearedDirect.first()
        val newDirect = clearedDirect[1]
        if (containsCode(currentDispLatMode, VECTORS) && containsCode(clearedDispLatMode, HOLD_AT, AFTER_WPT_HDG)) {
            //Case 1: Aircraft changed from after waypoint fly heading, to heading mode during delay: Remove hold at, after waypoint fly heading
            dispLatMode.removeFirst()
            dispLatMode.removeFirst()
            dispLatMode.addFirst(currentDispLatMode)
            dispLatMode.addFirst(currentDispLatMode)

            //Updates cleared heading to aftWptHdg to ensure aircraft flies the new requested after waypoint heading
            val initHdg = clearedHdg.removeFirst()
            clearedHdg.removeFirst()
            clearedHdg.addFirst(clearedAftWptHdg[1])
            clearedHdg.addFirst(initHdg)
            replaceAllClearedAltMode()
        } else if (newDirect != null && currentDirect != null && currentDispLatMode == SID_STAR && clearedNewStar[1] == null && aircraft.route.findWptIndex(newDirect.name) < aircraft.route.findWptIndex(currentDirect.name)) {
            //Case 2: Aircraft direct changes during delay: Replace cleared direct if it is before new direct (if changing STARs, do not replace)
            clearedDirect.removeFirst()
            clearedDirect.removeFirst()
            clearedDirect.addFirst(currentDirect)
            clearedDirect.addFirst(currentDirect)
        } else if (newDirect != null && !aircraft.route.getRemainingWaypoints(aircraft.sidStarIndex, aircraft.route.size - 1).contains(newDirect, false) && currentDispLatMode == VECTORS && containsCode(clearedDispLatMode, SID_STAR, HOLD_AT, AFTER_WPT_HDG)) {
            //Case 3: Aircraft has reached end of SID/STAR during delay: Replace latmode with "fly heading"
            //Set all the cleared heading to current aircraft cleared heading
            replaceAllClearedHdg(aircraft.clearedHeading)
            replaceAllClearedAltMode()
        } else if (aircraft.isLocCap && clearedHdg[1] != aircraft.apch?.heading) {
            //Case 4: Aircraft captured LOC during delay: Replace all set headings to ILS heading
            replaceAllClearedHdg(aircraft.apch?.heading ?: 360)
            if (aircraft.apch?.isNpa == true) {
                //Case 4b: Aircraft on LDA has captured LOC and is performing non precision approach: Replace all set altitude to missed approach altitude
                replaceAllClearedAltMode()
                replaceAllClearedAlt()
            }
        }
        if (aircraft.isGsCap || (aircraft.apch?.isNpa == true && aircraft.isLocCap)) {
            //Case 5: Aircraft captured GS during delay: Replace all set altitude to missed approach altitude
            replaceAllClearedAltMode()
            replaceAllClearedAlt()
        }
    }

    /** Replaces all SID/STAR modes with fly heading, replaces cleared heading to input heading  */
    fun replaceAllSidStarWithHdg(hdg: Int) {
        val latSize = dispLatMode.size
        for (i in 0 until latSize) {
            var latMode = dispLatMode.removeFirst()
            var altMode = dispAltMode.removeFirst()
            var spdMode = dispSpdMode.removeFirst()
            if (latMode == SID_STAR) {
                latMode = VECTORS
                altMode = NO_RESTR
                spdMode = NO_RESTR
            }
            dispLatMode.addLast(latMode)
            dispAltMode.addLast(altMode)
            dispSpdMode.addLast(spdMode)
        }
        val size = clearedHdg.size
        clearedHdg.clear()
        for (i in 0 until size) {
            clearedHdg.addLast(hdg)
        }
    }

    /** Replaces all after waypoint, fly heading modes with fly heading, replaces cleared heading to input heading  */
    fun replaceAllAfterWptModesWithHdg(hdg: Int) {
        val latSize = dispLatMode.size
        for (i in 0 until latSize) {
            var latMode = dispLatMode.removeFirst()
            var altMode = dispAltMode.removeFirst()
            var spdMode = dispSpdMode.removeFirst()
            if (latMode == AFTER_WPT_HDG) {
                latMode = VECTORS
                altMode = NO_RESTR
                spdMode = NO_RESTR
            }
            dispLatMode.addLast(latMode)
            dispAltMode.addLast(altMode)
            dispSpdMode.addLast(spdMode)
        }
        val size = clearedHdg.size
        clearedHdg.clear()
        for (i in 0 until size) {
            clearedHdg.addLast(hdg)
        }
    }

    /** Replaces all directs that is earlier than the current aircraft direct waypoint (or if null)  */
    fun replaceAllOutdatedDirects(latestDirect: Waypoint?) {
        val size = clearedDirect.size
        for (i in 0 until size) {
            var wpt = clearedDirect.removeFirst()
            if (latestDirect == null || wpt != null && aircraft.route.findWptIndex(wpt.name) < aircraft.route.findWptIndex(latestDirect.name)) wpt = latestDirect
            clearedDirect.addLast(wpt)
        }
    }

    /** Gets the current cleared aircraft heading and sets all subsequently cleared headings to that value, sets lat mode to fly heading  */
    private fun replaceAllClearedHdg(hdg: Int) {
        val latSize = dispLatMode.size
        dispLatMode.clear()
        for (i in 0 until latSize) {
            dispLatMode.addLast(VECTORS)
        }
        val size = clearedHdg.size
        clearedHdg.clear()
        for (i in 0 until size) {
            clearedHdg.addLast(hdg)
        }
    }

    /** Replaces all turn left/right heading modes (old) with fly heading, used to migrate old mode (before turn/left removed) to new mode  */
    private fun replaceAllTurnHdgModes() {
        val size = dispLatMode.size
        for (i in 0 until size) {
            val code = dispLatMode.removeFirst()
            dispLatMode.addLast(if (containsCode(code, TURN_RIGHT, TURN_LEFT)) VECTORS else code)
        }
    }

    /** Replaces all turn directions with no direction, called after aircraft has finished a turn instructed in a specific direction  */
    fun replaceAllTurnDirections() {
        val size = clearedTurnDir.size
        clearedTurnDir.clear()
        for (i in 0 until size) {
            clearedTurnDir.addLast(NO_DIRECTION)
        }
    }

    /** Sets all alt mode to climb/descend (no expedite)  */
    fun replaceAllClearedAltMode() {
        val altSize = dispAltMode.size
        dispAltMode.clear()
        for (i in 0 until altSize) {
            dispAltMode.addLast(NO_RESTR)
        }
        val expSize = clearedExpedite.size
        clearedExpedite.clear()
        for (i in 0 until expSize) {
            clearedExpedite.addLast(false)
        }
    }

    /** Sets all spd mode to unrestricted  */
    fun replaceAllClearedSpdMode() {
        val spdSize = dispSpdMode.size
        dispSpdMode.clear()
        for (i in 0 until spdSize) {
            dispSpdMode.addLast(NO_RESTR)
        }
    }

    /** Gets the current cleared aircraft altitude and sets all subsequently cleared altitudes to that value  */
    fun replaceAllClearedAlt() {
        val currentAlt = aircraft.clearedAltitude
        val size = clearedAlt.size
        clearedAlt.clear()
        for (i in 0 until size) {
            clearedAlt.addLast(currentAlt)
        }
    }

    /** Gets current cleared aircraft speed and sets all subsequently cleared speed to that value if larger  */
    fun replaceAllClearedSpdToLower() {
        val newQueue = Queue<Int>()
        while (!clearedSpd.isEmpty) {
            val first = clearedSpd.removeFirst()
            newQueue.addLast(min(first, aircraft.clearedIas))
        }
        clearedSpd = newQueue
    }

    /** Gets current cleared aircraft speed and sets all subsequently cleared speed to that value if smaller  */
    fun replaceAllClearedSpdToHigher() {
        val newQueue = Queue<Int>()
        while (!clearedSpd.isEmpty) {
            val first = clearedSpd.removeFirst()
            newQueue.addLast(max(first, aircraft.clearedIas))
        }
        clearedSpd = newQueue
    }

    /** Removes all ILS clearances  */
    fun voidAllIls() {
        val size = clearedApch.size
        clearedApch.clear()
        for (i in 0 until size) {
            clearedApch.addLast(null)
        }
    }

    /** Adds new lateral instructions to queue  */
    fun sendLat(latMode: Int, clearedWpt: String?, afterWpt: String?, holdWpt: String?, afterWptHdg: Int, clearedHdg: Int, clearedApch: String?, newStar: String?, turnDir: Int) {
        var latModeName = latMode
        if (latMode == SID_STAR) {
            clearedDirect.addLast(radarScreen.waypoints[clearedWpt])
            if (latModes.first().contains("arrival")) {
                updateLatModes(ADD_ALL_SIDSTAR, false)
            }
            if (clearedHold.last() != null) {
                clearedHold.removeLast()
                clearedHold.addLast(null)
            }
        } else if (latMode == AFTER_WPT_HDG) {
            clearedAftWpt.addLast(radarScreen.waypoints[afterWpt])
            clearedAftWptHdg.addLast(afterWptHdg)
        } else if (latMode == HOLD_AT) {
            clearedHold.addLast(radarScreen.waypoints[holdWpt])
            updateLatModes(REMOVE_AFTERHDG_ONLY, false)
        } else if (latMode == CHANGE_STAR) {
            clearedNewStar.addLast(newStar)
            if (dispLatMode.last() == VECTORS) {
                latModeName = dispLatMode.last()
            } else {
                latModeName = VECTORS
                this.clearedHdg.addLast(aircraft.heading.toInt())
            }
            clearedDirect.addLast(null)
            clearedAftWpt.addLast(null)
            clearedHold.addLast(null)
            updateLatModes(REMOVE_ALL_SIDSTAR, true)
        } else {
            clearedDirect.addLast(null)
            clearedAftWpt.addLast(null)
            clearedHold.addLast(null)
            this.clearedHdg.addLast(clearedHdg)
            clearedTurnDir.addLast(turnDir)
        }
        if (aircraft is Arrival) {
            this.clearedApch.addLast(aircraft.airport.approaches[clearedApch?.substring(3)])
            if (latMode == SID_STAR) {
                if (this.clearedApch.last() != null) {
                    updateLatModes(REMOVE_AFTERHDG_HOLD, false)
                } else {
                    updateLatModes(ADD_ALL_SIDSTAR, false)
                }
            }
        }
        dispLatMode.addLast(latModeName)
        goAround.addLast(aircraft.isGoAround)
        length++
        fillUpInt(this.clearedHdg)
        fillUpWpt(clearedDirect)
        fillUpWpt(clearedAftWpt)
        fillUpInt(clearedAftWptHdg)
        fillUpWpt(clearedHold)
        fillUpApch(this.clearedApch)
        fillUpString(clearedNewStar)
        fillUpInt(clearedTurnDir)
    }

    /** Adds new altitude instructions to queue, called after sendLat  */
    fun sendAlt(altMode: Int, clearedAlt: Int, expedite: Boolean) {
        this.clearedAlt.addLast(clearedAlt)
        dispAltMode.addLast(altMode)
        clearedExpedite.addLast(expedite)
        fillUpBool(clearedExpedite)
    }

    /** Adds new speed instructions to queue, called after sendAlt  */
    fun sendSpd(spdMode: Int, clearedSpd: Int) {
        if (aircraft is Departure && !(aircraft as Departure).isAccel && clearedSpd == aircraft.v2) {
            this.clearedSpd.addLast(220)
        } else {
            this.clearedSpd.addLast(clearedSpd)
        }
        dispSpdMode.addLast(spdMode)
    }

    /** Fills up input queue to ideal length, with its last element  */
    private fun fillUpInt(queue: Queue<Int>) {
        while (queue.size < length) {
            queue.addLast(queue.last())
        }
    }

    private fun fillUpWpt(queue: Queue<Waypoint?>) {
        while (queue.size < length) {
            queue.addLast(queue.last())
        }
    }

    private fun fillUpBool(queue: Queue<Boolean>) {
        while (queue.size < length) {
            queue.addLast(queue.last())
        }
    }

    private fun fillUpApch(queue: Queue<Approach?>) {
        while (queue.size < length) {
            queue.addLast(queue.last())
        }
    }

    private fun fillUpString(queue: Queue<String?>) {
        while (queue.size < length) {
            queue.addLast(queue.last())
        }
    }

    /** Updates the time queue for instructions  */
    fun updateTime() {
        for (i in 0 until timeQueueArray.size) {
            timeQueueArray[i] = timeQueueArray[i] - Gdx.graphics.deltaTime
        }
        if (timeQueueArray.size > 0 && timeQueueArray[0] <= 0) {
            timeQueueArray.removeIndex(0)
            sendInstructions()
        }
    }

    /** Replaces the selections in latModes depending on input mode, and will update the current UI if updateUI is true  */
    fun updateLatModes(mode: Int, updateUI: Boolean) {
        //Will not throw exception even if element not in array
        when (mode) {
            REMOVE_ALL_SIDSTAR -> {
                latModes.clear()
                latModes.add(Ui.FLY_HEADING)
                if (!radarScreen.tutorial && aircraft is Arrival) latModes.add(Ui.CHANGE_STAR)
            }
            REMOVE_AFTERHDG_HOLD -> {
                latModes.removeValue(Ui.AFTER_WPT_FLY_HDG, false)
                latModes.removeValue(Ui.HOLD_AT, false)
            }
            REMOVE_SIDSTAR_ONLY -> {
                latModes.removeValue(aircraft.sidStar.name + " arrival", false)
                latModes.removeValue(aircraft.sidStar.name + " departure", false)
            }
            REMOVE_SIDSTAR_AFTERHDG -> {
                latModes.removeValue(aircraft.sidStar.name + " arrival", false)
                latModes.removeValue(aircraft.sidStar.name + " departure", false)
                latModes.removeValue(Ui.AFTER_WPT_FLY_HDG, false)
            }
            REMOVE_HOLD_ONLY -> latModes.removeValue(Ui.HOLD_AT, false)
            REMOVE_AFTERHDG_ONLY -> latModes.removeValue(Ui.AFTER_WPT_FLY_HDG, false)
            ADD_ALL_SIDSTAR -> {
                latModes.clear()
                if (aircraft is Arrival) {
                    latModes.add(aircraft.sidStar.name + " arrival", Ui.AFTER_WPT_FLY_HDG, Ui.FLY_HEADING)
                    latModes.add(Ui.HOLD_AT)
                    if (!radarScreen.tutorial) latModes.add(Ui.CHANGE_STAR)
                } else if (aircraft is Departure) {
                    latModes.add(aircraft.sidStar.name + " departure", Ui.FLY_HEADING)
                }
            }
            REMOVE_CHANGE_STAR -> latModes.removeValue(Ui.CHANGE_STAR, false)
            ADD_CHANGE_STAR -> latModes.add(Ui.CHANGE_STAR)
            ADD_SIDSTAR_ONLY -> latModes.add(aircraft.sidStar.name + " ${if (aircraft is Arrival) "arrival" else "departure"}")
            else -> Gdx.app.log("NavState", "Invalid latModes update mode: $mode")
        }
        if (updateUI && aircraft.isSelected && aircraft.isArrivalDeparture) aircraft.ui.updateState()
    }

    /** Replaces the selections in altModes depending on input mode, and will update the current UI if updateUI is true  */
    fun updateAltModes(mode: Int, updateUI: Boolean) {
        //Will not throw exception even if element not in array
        when (mode) {
            REMOVE_SIDSTAR_RESTR -> {
                altModes.removeValue(Ui.CLIMB_VIA_SID, false)
                altModes.removeValue(Ui.DESCEND_VIA_STAR, false)
            }
            ADD_SIDSTAR_RESTR_UNRESTR -> {
                altModes.clear()
                if (aircraft is Arrival) {
                    altModes.add(Ui.DESCEND_VIA_STAR)
                } else if (aircraft is Departure) {
                    altModes.add(Ui.CLIMB_VIA_SID)
                }
                altModes.add(Ui.CLIMB_DESCEND_TO)
            }
            REMOVE_UNRESTR -> altModes.removeValue(Ui.CLIMB_DESCEND_TO, false)
            ADD_UNRESTR_ONLY -> {
                altModes.clear()
                altModes.add(Ui.CLIMB_DESCEND_TO)
            }
            ADD_SIDSTAR_RESTR_ONLY -> {
                altModes.clear()
                if (aircraft is Arrival) {
                    altModes.add(Ui.DESCEND_VIA_STAR)
                } else if (aircraft is Departure) {
                    altModes.add(Ui.CLIMB_VIA_SID)
                }
            }
            else -> Gdx.app.log("NavState", "Invalid altModes update mode: $mode")
        }
        if (updateUI && aircraft.isSelected && aircraft.isArrivalDeparture) aircraft.ui.updateState()
    }

    /** Replaces the selections in spdModes depending on input mode, and will update the current UI if updateUI is true  */
    fun updateSpdModes(mode: Int, updateUI: Boolean) {
        //Will not throw exception even if element not in array
        when (mode) {
            REMOVE_SIDSTAR_RESTR -> {
                spdModes.removeValue(Ui.SID_SPD_RESTRICTIONS, false)
                spdModes.removeValue(Ui.STAR_SPD_RESTRICTIONS, false)
            }
            ADD_SIDSTAR_RESTR_UNRESTR -> {
                spdModes.clear()
                if (aircraft is Arrival) {
                    spdModes.add(Ui.STAR_SPD_RESTRICTIONS)
                } else if (aircraft is Departure) {
                    spdModes.add(Ui.SID_SPD_RESTRICTIONS)
                }
                spdModes.add(Ui.NO_SPD_RESTRICTIONS)
            }
            REMOVE_UNRESTR -> spdModes.removeValue(Ui.NO_SPD_RESTRICTIONS, false)
            ADD_UNRESTR_ONLY -> {
                spdModes.clear()
                spdModes.add(Ui.NO_SPD_RESTRICTIONS)
            }
            ADD_SIDSTAR_RESTR_ONLY -> {
                spdModes.clear()
                if (aircraft is Arrival) {
                    spdModes.add(Ui.STAR_SPD_RESTRICTIONS)
                } else if (aircraft is Departure) {
                    spdModes.add(Ui.SID_SPD_RESTRICTIONS)
                }
            }
            else -> Gdx.app.log("NavState", "Invalid spdModes update mode: $mode")
        }
        if (updateUI && aircraft.isSelected && aircraft.isArrivalDeparture) aircraft.ui.updateState()
    }

    /** Checks if the supplied code matches any of the required codes  */
    fun containsCode(toCheck: Int, vararg codesAllowed: Int): Boolean {
        return ArrayUtils.contains(codesAllowed, toCheck)
    }
}