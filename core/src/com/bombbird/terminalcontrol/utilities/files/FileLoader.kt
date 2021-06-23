package com.bombbird.terminalcontrol.utilities.files

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Base64Coder
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.approaches.*
import com.bombbird.terminalcontrol.entities.obstacles.CircleObstacle
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle
import com.bombbird.terminalcontrol.entities.obstacles.PolygonObstacle
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach
import com.bombbird.terminalcontrol.entities.procedures.holding.HoldingPoints
import com.bombbird.terminalcontrol.entities.runways.Runway
import com.bombbird.terminalcontrol.entities.sidstar.Sid
import com.bombbird.terminalcontrol.entities.sidstar.Star
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object FileLoader {
    lateinit var mainDir: String
    
    fun loadObstacles(): Array<Obstacle> {
        val obsArray = Array<Obstacle>()
        TerminalControl.radarScreen?.let {
            val obstacles = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/obstacle.obs")
            val indivObs = obstacles.readString().split("\\r?\\n".toRegex()).toTypedArray()
            for (s in indivObs) {
                if ("" == s) break
                if (s[0] == '#') continue
                //For each individual obstacle:
                val obs = PolygonObstacle(s)
                obsArray.add(obs)
                it.stage.addActor(obs)
            }
            val restrictions = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/restricted.restr")
            val indivRests = restrictions.readString().split("\\r?\\n".toRegex()).toTypedArray()
            for (s in indivRests) {
                if ("" == s) break
                if (s[0] == '#') continue
                //For each individual restricted area
                val area = CircleObstacle(s)
                obsArray.add(area)
                it.stage.addActor(area)
            }
        }
        return obsArray
    }

    fun loadWaypoints(): HashMap<String, Waypoint> {
        val waypoints = HashMap<String, Waypoint>()
        TerminalControl.radarScreen?.let {
            val handle = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/waypoint.way")
            val wayptStr = handle.readString()
            val indivWpt = wayptStr.split("\\r?\\n".toRegex()).toTypedArray()
            for (s in indivWpt) {
                //For each waypoint
                var name = ""
                var x = 0
                var y = 0
                for ((index, s1) in s.split(" ".toRegex()).toTypedArray().withIndex()) {
                    when (index) {
                        0 -> name = s1
                        1 -> x = s1.toInt()
                        2 -> y = s1.toInt()
                        else -> Gdx.app.log("Load error", "Unexpected additional parameter in game/" + it.mainName + "/" + it.airac + "/waypoint.way -> $name")
                    }
                }
                val waypoint = Waypoint(name, x, y)
                waypoints[name] = waypoint
                it.stage.addActor(waypoint)
            }
        }
        return waypoints
    }

    fun loadRunways(icao: String): HashMap<String, Runway> {
        val runways = HashMap<String, Runway>()
        TerminalControl.radarScreen?.let {
            val handle = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/runway" + icao + ".rwy")
            val indivRwys = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
            for (s in indivRwys) {
                //For each individual runway
                val runway = Runway(s)
                runways[runway.name] = runway
            }
        }
        return runways
    }

    fun loadStars(airport: Airport): HashMap<String, Star> {
        //Load STARs
        val stars = HashMap<String, Star>()
        TerminalControl.radarScreen?.let {
            val handle = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/star" + airport.icao + ".star")
            val jo = JSONObject(handle.readString())
            for (name in jo.keySet()) {
                //For each individual STAR
                val star = Star(airport, jo.getJSONObject(name))
                star.name = name
                stars[name] = star
            }
        }
        return stars
    }

    fun loadSids(airport: Airport): HashMap<String, Sid> {
        //Load SIDs
        val sids = HashMap<String, Sid>()
        TerminalControl.radarScreen?.let {
            val handle = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/sid" + airport.icao + ".sid")
            val jo = JSONObject(handle.readString())
            for (name in jo.keySet()) {
                //For each individual SID
                val sid = Sid(airport, jo.getJSONObject(name))
                sid.name = name
                sids[name] = sid
            }
        }
        return sids
    }

    fun loadAircraftData(): HashMap<String, IntArray> {
        val aircrafts = HashMap<String, IntArray>()
        val handle = Gdx.files.internal("game/aircrafts/aircrafts.air")
        val indivAircrafts = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
        for (s in indivAircrafts) {
            if (s[0] == '#') {
                continue
            }
            //For each aircraft
            var index = 0
            var icao = ""
            val perfData = IntArray(8)
            for (s1 in s.split(",".toRegex()).toTypedArray()) {
                if (index == 0) {
                    icao = s1 //Icao code of aircraft
                } else if (index in 2..5 || index in 7..8) {
                    perfData[index - 1] = s1.toInt()
                } else if (index == 1 || index == 6) {
                    //Wake turb cat, recat
                    val cat = s1[0]
                    perfData[index - 1] = cat.code
                    if (index == 1 && !(cat == 'M' || cat == 'H' || cat == 'J')) Gdx.app.log("Wake cat error", "Unknown category for $icao")
                    if (index == 6 && cat !in 'A'..'F') Gdx.app.log("Recat error", "Unknown category for $icao")
                } else {
                    Gdx.app.log("Load error", "Unexpected additional parameter in game/aircrafts.air")
                }
                index++
            }
            if (index <= perfData.size) Gdx.app.log("Load error", "Possible missing aircraft parameter for $icao")
            aircrafts[icao] = perfData
        }
        return aircrafts
    }

    fun loadApch(airport: Airport): HashMap<String, Approach> {
        val approaches = HashMap<String, Approach>()
        TerminalControl.radarScreen?.let {
            val handle = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/apch" + airport.icao + ".apch")
            val jo = JSONObject(handle.readString())
            for (name in jo.keySet()) {
                //For each approach
                val data = jo.getJSONObject(name)
                when(val type = data.getString("type")) {
                    "ILS" -> {
                        val ils = ILS(airport, name, data)
                        approaches[ils.rwy.name] = ils
                    }
                    "LDA", "IGS" -> {
                        val lda = OffsetILS(airport, name, data)
                        approaches[lda.rwy.name] = lda
                    }
                    "CIRCLING" -> {
                        val circle = Circling(airport, name, data)
                        approaches[circle.rwy.name] = circle
                    }
                    "RNP" -> {
                        val rnp = RNP(airport, name, data)
                        approaches[rnp.rwy.name] = rnp
                    }
                    else -> Gdx.app.log("ILS error", "Invalid approach type $type specified")
                }
            }
        }
        return approaches
    }

    fun loadHoldingPoints(airport: Airport): HashMap<String, HoldingPoints> {
        val holdingPoints = HashMap<String, HoldingPoints>()
        TerminalControl.radarScreen?.let {
            val handle = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/hold" + airport.icao + ".hold")
            val jo = JSONObject(handle.readString())
            for (point in jo.keySet()) {
                holdingPoints[point] = HoldingPoints(point, jo.getJSONObject(point))
            }
        }
        return holdingPoints
    }

    fun loadMissedInfo(airport: Airport): HashMap<String, MissedApproach> {
        val missedApproaches = HashMap<String, MissedApproach>()
        TerminalControl.radarScreen?.let {
            val handle = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/missedApch" + airport.icao + ".miss")
            val jo = JSONObject(handle.readString())
            for (missed in jo.keySet()) {
                missedApproaches[missed] = MissedApproach(jo.getJSONObject(missed))
            }
        }
        return missedApproaches
    }

    fun loadSaves(): JSONArray {
        val saves = JSONArray()
        val handle = getExtDir("saves/saves.saves")
        if (handle != null && handle.exists()) {
            val saveIds = Array(handle.readString().split(",".toRegex()).toTypedArray())
            if ("" == saveIds[0]) {
                //"" returned if saves.saves is empty
                return saves
            }
            for (id in saveIds) {
                val handle1 = handle.sibling("$id.json")
                if (!handle1.exists()) {
                    GameSaver.deleteSave(id.toInt())
                    continue
                }
                var saveString = handle1.readString()
                if (saveString.isEmpty()) {
                    GameSaver.deleteSave(id.toInt())
                    continue
                }
                try {
                    saveString = Base64Coder.decodeString(saveString)
                    val save = JSONObject(saveString)
                    saves.put(save)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    //ErrorHandler.sendSaveErrorNoThrow(e, saveString);
                    TerminalControl.toastManager.jsonParseFail()
                    Gdx.app.log("Corrupted save", "JSON parse failure")
                    if (GameSaver.deleteSave(id.toInt())) Gdx.app.log("Save deleted", "Corrupted save deleted")
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    //ErrorHandler.sendSaveErrorNoThrow(e, saveString);
                    TerminalControl.toastManager.jsonParseFail()
                    Gdx.app.log("Corrupted save", "Base64 decode failure")
                    if (GameSaver.deleteSave(id.toInt())) Gdx.app.log("Save deleted", "Corrupted save deleted")
                }
            }
        }
        return saves
    }

    fun checkIfSaveExists(): Boolean {
        val handle = getExtDir("saves/saves.saves")
        return if (handle != null && handle.exists()) {
            //"" returned if saves.saves is empty
            "" != handle.readString()
        } else false
    }

    fun loadAirlines(icao: String): HashMap<Int, String> {
        val airlines = HashMap<Int, String>()
        TerminalControl.radarScreen?.let {
            val info = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/airlines" + icao + ".airl").readString().split("\\r?\\n".toRegex()).toTypedArray()
            var counter = 0
            for (indivAirline in info) {
                val callsign = indivAirline.split(",".toRegex()).toTypedArray()[0]
                val times = indivAirline.split(",".toRegex()).toTypedArray()[1].toInt()
                for (i in 0 until times) {
                    airlines[counter] = callsign
                    counter++
                }
            }
        }
        return airlines
    }

    fun loadAirlineAircrafts(icao: String): HashMap<String, String> {
        val aircrafts = HashMap<String, String>()
        TerminalControl.radarScreen?.let {
            val info = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/airlines" + icao + ".airl").readString().split("\\r?\\n".toRegex()).toTypedArray()
            for (indivAirline in info) {
                aircrafts[indivAirline.split(",".toRegex()).toTypedArray()[0]] = indivAirline.split(",".toRegex()).toTypedArray()[2]
            }
        }
        return aircrafts
    }

    fun loadIcaoCallsigns(): HashMap<String, String> {
        val callsigns = HashMap<String, String>()
        val info = Gdx.files.internal("game/aircrafts/callsigns.call").readString().split("\\r?\\n".toRegex()).toTypedArray()
        for (indivAirline in info) {
            callsigns[indivAirline.split(",".toRegex()).toTypedArray()[0]] = indivAirline.split(",".toRegex()).toTypedArray()[1]
        }
        return callsigns
    }

    fun loadNoise(icao: String, sid: Boolean): HashMap<String, Boolean> {
        val noise = HashMap<String, Boolean>()
        TerminalControl.radarScreen?.let {
            val fileName = if (sid) "/noiseSid" else "/noiseStar"
            val jo = JSONObject(Gdx.files.internal("game/" + it.mainName + "/" + it.airac + fileName + icao + ".noi").readString())
            val nightArray = jo.getJSONArray("night")
            for (i in 0 until nightArray.length()) {
                noise[nightArray.getString(i)] = true
            }
            val dayArray = jo.getJSONArray("day")
            for (i in 0 until dayArray.length()) {
                noise[dayArray.getString(i)] = false
            }
        }
        return noise
    }

    fun loadShoreline(): Array<Array<Int>> {
        val shoreline = Array<Array<Int>>()
        TerminalControl.radarScreen?.let {
            val info = Gdx.files.internal("game/" + it.mainName + "/" + it.airac + "/shoreline.shore").readString().split("\\r?\\n".toRegex()).toTypedArray()
            if ("" == info[0]) return shoreline
            var landmass = Array<Int>()
            for (line in info) {
                if (line[0] == '\"') {
                    if (landmass.size > 0) {
                        shoreline.add(landmass)
                        landmass = Array()
                    }
                } else {
                    val data = line.split(" ".toRegex()).toTypedArray()
                    landmass.add(data[0].toInt()) //x coordinate
                    landmass.add(data[1].toInt()) //y coordinate
                }
            }
            if (landmass.size > 0) shoreline.add(landmass) //Add the last landmass
        }
        return shoreline
    }

    fun loadSettings(): JSONObject? {
        var settings: JSONObject? = null
        try {
            val handle = getExtDir("settings.json")
            if (handle != null && handle.exists()) {
                settings = JSONObject(handle.readString())
            }
        } catch (e: JSONException) {
            Gdx.app.log("Corrupted settings", "JSON parse failure")
        }
        return settings
    }

    fun loadStats(): JSONObject? {
        var stats: JSONObject? = null
        try {
            val handle = getExtDir("stats.json")
            if (handle != null && handle.exists()) {
                stats = JSONObject(handle.readString())
            }
        } catch (e: JSONException) {
            Gdx.app.log("Corrupted settings", "JSON parse failure")
        }
        return stats
    }

    fun getAvailableDatatagConfigs(): kotlin.Array<String> {
        val handle = getExtDir("datatags")
        return if (handle != null && handle.exists() && handle.isDirectory) {
            handle.list().map { it.name() }.toTypedArray()
        } else {
            handle?.mkdirs()
            arrayOf()
        }
    }

    fun getDatatagLayout(name: String): JSONObject? {
        val handle = getExtDir("datatags/$name")
        return if (handle != null && handle.exists()) JSONObject(handle.readString()) else null
    }

    fun getExtDir(path: String): FileHandle? {
        var handle: FileHandle? = null
        if (Gdx.app.type == Application.ApplicationType.Desktop) {
            //If desktop, save to external roaming appData
            handle = if (!TerminalControl.full) {
                Gdx.files.external("AppData/Roaming/TerminalControl/$path")
            } else {
                Gdx.files.external("AppData/Roaming/TerminalControlFull/$path")
            }
        } else if (Gdx.app.type == Application.ApplicationType.Android) {
            //If Android, check first if local storage available
            if (Gdx.files.isLocalStorageAvailable) {
                handle = Gdx.files.local(path)
            } else {
                Gdx.app.log("Storage error", "Local storage unavailable for Android!")
                TerminalControl.toastManager.readStorageFail()
            }
        } else {
            Gdx.app.log("Storage error", "Unknown platform!")
        }
        return handle
    }
}