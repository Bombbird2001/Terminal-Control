package com.bombbird.terminalcontrol.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files
import com.badlogic.gdx.utils.Array
import com.bombbird.terminalcontrol.screens.ChangelogScreen
import com.bombbird.terminalcontrol.utilities.math.MathTools
import org.apache.commons.lang.ArrayUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class NavDataTests {
    @TestFactory
    fun checkAIRACCycles(): Iterable<DynamicTest> {
        Gdx.files = Lwjgl3Files()
        val handle = Gdx.files.internal("game/available.arpt")
        assertTrue(handle.exists(), "available.arpt missing")
        val handle2 = Gdx.files.internal("game/type.type")
        assertTrue(handle2.exists(), "type.type missing")
        val typeData = handle2.readString().split(" ")
        assertEquals(2, typeData.size, "Incorrect parameter length for type.type")
        val latestAirac = typeData[0].split(".")[2]
        assertEquals(4, latestAirac.length, "Invalid AIRAC cycle")
        val airportArray = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
        return airportArray.map { arptData ->
            val arpt = arptData.split(":".toRegex()).toTypedArray()[0]
            val airac = arptData.split(":".toRegex()).toTypedArray()[1].split(",".toRegex())
                .toTypedArray()[0].split("-".toRegex()).toTypedArray()[1]
            DynamicTest.dynamicTest("$arpt AIRAC check") {
                assertTrue(Gdx.files.internal("game/$arpt/$airac").exists(), "AIRAC $airac does not exist")
                assertEquals(latestAirac, airac, "AIRAC $airac does not match latest AIRAC $latestAirac")
            }
        }
    }

    @DisplayName("Changelog check")
    @Test
    fun checkChangelog() {
        Gdx.files = Lwjgl3Files()
        val handle = Gdx.files.internal("game/type.type")
        assertTrue(handle.exists(), "type.type missing")
        val typeData = handle.readString().split(" ")
        assertEquals(2, typeData.size, "Incorrect parameter length for type.type")
        val versionData = typeData[0].split(".")
        val major = "${versionData[0]}.${versionData[1]}"
        val minor = "${versionData[2]}.${versionData[3]}"
        ChangelogScreen.loadHashmapContent(full = true, android = true, desktop = true)
        assertTrue(ChangelogScreen.changeLogContent.containsKey(major), "Major version $major not in changelog")
        val minorVersions = ChangelogScreen.changeLogContent[major]!!
        assertTrue(minorVersions.containsKey(minor), "Minor version $major.$minor not in changelog")
        val changes = minorVersions[minor]!!
        assertTrue(changes.notEmpty(), "Empty changelog for minor version $major.$minor")
        assertEquals(major, ChangelogScreen.latestMajor, "Latest major version ${ChangelogScreen.latestMajor} does not match game major version $major")
        assertEquals(minor, ChangelogScreen.latestMinor, "Latest minor version ${ChangelogScreen.latestMajor}.${ChangelogScreen.latestMinor} does not match game major version $major.$minor")
    }

    @TestFactory
    fun checkAircraft(): Iterable<DynamicTest> {
        Gdx.files = Lwjgl3Files()
        val handle = Gdx.files.internal("game/aircrafts/aircrafts.air")
        assertTrue(handle.exists(), "aircrafts.air missing")
        val indivAircrafts = handle.readString().split("\\r?\\n".toRegex()).filter { !it.contains('#') }.toTypedArray()
        return indivAircrafts.map { acftData ->
            val acftArray = acftData.split(",".toRegex()).toTypedArray()
            DynamicTest.dynamicTest("${acftArray[0]} aircraft check") {
                assertEquals(9, acftArray.size, "${acftArray.size} parameters for ${acftArray[0]}, expected 9") //Data array size
                assertEquals(4, acftArray[0].length, "Invalid ICAO code ${acftArray[0]}") //ICAO code length
                assertTrue(ArrayUtils.contains(arrayOf("L", "M", "H", "J"), acftArray[1]), "Invalid WTC ${acftArray[1]}") //Wake turbulence category
                assertTrue(MathTools.withinRange(acftArray[2].toInt(), 100, 200), "V2 speed ${acftArray[2]} out of range") //V2 speed
                assertTrue(MathTools.withinRange(acftArray[5].toInt(), 100, 200), "Approach speed ${acftArray[5]} out of range") //Apch speed
                assertTrue(ArrayUtils.contains(arrayOf("A", "B", "C", "D", "E", "F"), acftArray[6]), "Invalid Recat ${acftArray[6]}") //Recat turbulence category
                assertTrue(acftArray[7].toInt() == -1 || MathTools.withinRange(acftArray[7].toInt(), 200, 250), "Max cruise speed ${acftArray[7]} out of range") //Max cruise speed
                assertTrue(MathTools.withinRange(acftArray[8].toInt(), 10000, 60000), "Max cruise altitude ${acftArray[8]} out of range") //Max cruise altitude
            }
        }
    }

    @TestFactory
    fun checkAirlines(): Iterable<DynamicTest> {
        Gdx.files = Lwjgl3Files()
        val handle = Gdx.files.internal("game/aircrafts/aircrafts.air")
        assertTrue(handle.exists(), "aircrafts.air missing")
        val indivAircrafts = HashSet(handle.readString().split("\\r?\\n".toRegex()).filter { !it.contains('#') }.map { it.split(",")[0] })

        val handle2 = Gdx.files.internal("game/aircrafts/callsigns.call")
        assertTrue(handle2.exists(), "callsigns.call missing")
        val allCallsigns = HashSet(handle2.readString().split("\\r?\\n".toRegex()).map { it.split(",")[0] })

        val handle3 = Gdx.files.internal("game/available.arpt")
        assertTrue(handle3.exists(), "available.arpt missing")
        val airportArray = handle3.readString().split("\\r?\\n".toRegex()).toTypedArray()
        val allArptArray = Array<Triple<String, String, String>>()
        airportArray.map { arptData ->
            val arpt = arptData.split(":".toRegex()).toTypedArray()[0]
            val airac = arptData.split(":".toRegex()).toTypedArray()[1].split(",".toRegex())
                .toTypedArray()[0].split("-".toRegex()).toTypedArray()[1]
            val handle4 = Gdx.files.internal("game/$arpt/$airac/airport.arpt")
            val jo = JSONObject(handle4.readString())
            val airports = jo.getJSONObject("airports")
            for (icao in airports.keySet()) {
                allArptArray.add(Triple<String, String, String>(icao, airac, arpt))
            }
        }
        return allArptArray.map { data ->
            val arpt = data.first
            val airac = data.second
            val mainName = data.third
            DynamicTest.dynamicTest("$arpt airline check") {
                val arptHandle = Gdx.files.internal("game/$mainName/$airac/airlines$arpt.airl")
                assertTrue(arptHandle.exists(), "Airline file does not exist")
                val info = arptHandle.readString().split("\\r?\\n".toRegex()).toTypedArray()
                for (indivAirline in info) {
                    val callsign = indivAirline.split(",".toRegex()).toTypedArray()[0]
                    assertTrue(allCallsigns.contains(callsign), "Airline $callsign not in callsigns.call")
                    val planes = indivAirline.split(",".toRegex()).toTypedArray()[2].split(">")
                    for (plane in planes) {
                        assertTrue(indivAircrafts.contains(plane), "Aircraft $plane not in aircrafts.air for $callsign")
                    }
                }
            }
        }
    }

    @TestFactory
    fun checkApch(): Iterable<DynamicTest> {
        Gdx.files = Lwjgl3Files()
        val handle = Gdx.files.internal("game/available.arpt")
        assertTrue(handle.exists(), "available.arpt missing")
        val airportArray = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
        val allArptArray = Array<Triple<String, String, String>>()
        airportArray.map { arptData ->
            val arpt = arptData.split(":".toRegex()).toTypedArray()[0]
            val airac = arptData.split(":".toRegex()).toTypedArray()[1].split(",".toRegex())
                .toTypedArray()[0].split("-".toRegex()).toTypedArray()[1]
            val handle4 = Gdx.files.internal("game/$arpt/$airac/airport.arpt")
            val jo = JSONObject(handle4.readString())
            val airports = jo.getJSONObject("airports")
            for (icao in airports.keySet()) {
                allArptArray.add(Triple<String, String, String>(icao, airac, arpt))
            }
        }
        return allArptArray.map { data ->
            val arpt = data.first
            val airac = data.second
            val mainName = data.third
            DynamicTest.dynamicTest("$arpt approach check") {
                val wptHandle = Gdx.files.internal("game/$mainName/$airac/waypoint.way")
                val wptData = HashMap<String, IntArray>()
                val wptArray = wptHandle.readString().split("\\r?\\n".toRegex()).toTypedArray()
                for (wptLine in wptArray) {
                    val indivWpt = wptLine.split(" ")
                    wptData[indivWpt[0]] = intArrayOf(indivWpt[1].toInt(), indivWpt[2].toInt())
                }
                val arptHandle = Gdx.files.internal("game/$mainName/$airac/apch$arpt.apch")
                assertTrue(arptHandle.exists(), "Approach file does not exist")
                val jo = JSONObject(arptHandle.readString())
                for (apchName in jo.keySet()) {
                    //For each approach
                    val apchData = jo.getJSONObject(apchName)
                    assertTrue(ArrayUtils.contains(arrayOf("ILS", "LDA", "IGS", "CIRCLING", "RNP"), apchData.getString("type")), "Invalid approach type ${apchData.getString("type")} for $apchName")
                    val rwy = apchData.getString("runway")
                    assertTrue(rwy.matches("\\d\\d[LRC]?".toRegex()) && rwy.substring(0, 2).toInt() <= 36 && rwy.substring(0, 2).toInt() > 0, "Invalid runway $rwy for $apchName")
                    assertTrue(MathTools.withinRange(apchData.getInt("heading"), 1, 360), "Invalid heading ${apchData.getInt("heading")} for $apchName")
                    assertTrue(apchData.getDouble("gsOffset") <= 0, "gsOffset cannot be positive for $apchName")
                    val towerData = apchData.getString("tower").split(">")
                    assertEquals(2, towerData.size, "${towerData.size} parameters for $apchName, expected 2")
                    assertEquals(2, towerData[1].split(".").size, "Invalid tower frequency ${towerData[1]} for $apchName")
                    assertEquals(3, towerData[1].split(".")[0].length, "Invalid tower frequency ${towerData[1]} for $apchName")

                    val joWpts = apchData.getJSONObject("wpts")
                    for (key in joWpts.keySet()) {
                        assertTrue(key == "vector"|| wptData.containsKey(key), "Waypoint $key for $apchName not in waypoints.way")
                        val coord = wptData[key]
                        assertTrue(coord == null || MathTools.withinRange(coord[0], 1311, 4449) && MathTools.withinRange(coord[1], 51, 3289), "$key not within spawn range for $apchName transition")
                        val transWpt = joWpts.getJSONArray(key)
                        for (i in 0 until transWpt.length()) {
                            val wptLine = transWpt.getString(i).split(" ".toRegex()).toTypedArray()
                            val expected = if (ArrayUtils.contains(wptLine, "FO")) 5 else 4
                            assertEquals(expected, wptLine.size, "${wptLine.size} parameters for $apchName $key transition, expected $expected")
                            val wptName = wptLine[0]
                            assertTrue(wptData.containsKey(wptName), "Waypoint $wptName for $apchName $key transition not in waypoints.way")
                            val coord2 = wptData[wptName]!!
                            assertTrue(MathTools.withinRange(coord2[0], 1311, 4449) && MathTools.withinRange(coord2[1], 51, 3289), "$wptName not within spawn range for $apchName $key transition")
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun checkSid(): Iterable<DynamicTest> {
        Gdx.files = Lwjgl3Files()
        val handle = Gdx.files.internal("game/available.arpt")
        assertTrue(handle.exists(), "available.arpt missing")
        val airportArray = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
        val allArptArray = Array<Triple<String, String, String>>()
        airportArray.map { arptData ->
            val arpt = arptData.split(":".toRegex()).toTypedArray()[0]
            val airac = arptData.split(":".toRegex()).toTypedArray()[1].split(",".toRegex())
                .toTypedArray()[0].split("-".toRegex()).toTypedArray()[1]
            val handle4 = Gdx.files.internal("game/$arpt/$airac/airport.arpt")
            val jo = JSONObject(handle4.readString())
            val airports = jo.getJSONObject("airports")
            for (icao in airports.keySet()) {
                allArptArray.add(Triple<String, String, String>(icao, airac, arpt))
            }
        }
        return allArptArray.map { data ->
            val arpt = data.first
            val airac = data.second
            val mainName = data.third
            DynamicTest.dynamicTest("$arpt SID check") {
                val wptHandle = Gdx.files.internal("game/$mainName/$airac/waypoint.way")
                val wptData = HashMap<String, IntArray>()
                val wptArray = wptHandle.readString().split("\\r?\\n".toRegex()).toTypedArray()
                for (wptLine in wptArray) {
                    val indivWpt = wptLine.split(" ")
                    wptData[indivWpt[0]] = intArrayOf(indivWpt[1].toInt(), indivWpt[2].toInt())
                }
                val arptHandle = Gdx.files.internal("game/$mainName/$airac/sid$arpt.sid")
                assertTrue(arptHandle.exists(), "SID file does not exist")
                val jo = JSONObject(arptHandle.readString())
                for (sidName in jo.keySet()) {
                    //For each SID
                    val sidData = jo.getJSONObject(sidName)
                    assertTrue(sidData.getBoolean("day") || sidData.getBoolean("night"), "Invalid day/night for $sidName")
                    val rwys = sidData.getJSONObject("rwys")
                    assertTrue(rwys.length() > 0, "No runways for $sidName")
                    for (rwy in rwys.keySet()) {
                        val rwyWpts = rwys.getJSONObject(rwy).getJSONArray("wpts")
                        for (i in 0 until rwyWpts.length()) {
                            val wptLine = rwyWpts.getString(i).split(" ".toRegex()).toTypedArray()
                            val expected = if (ArrayUtils.contains(wptLine, "FO")) 5 else 4
                            assertEquals(expected, wptLine.size, "${wptLine.size} parameters for $sidName $rwy, expected $expected")
                            val wptName = wptLine[0]
                            assertTrue(wptData.containsKey(wptName), "Waypoint $wptName for $sidName $rwy not in waypoints.way")
                        }
                    }

                    val route = sidData.getJSONArray("route")
                    for (i in 0 until route.length()) {
                        val wptLine = route.getString(i).split(" ".toRegex()).toTypedArray()
                        val expected = if (ArrayUtils.contains(wptLine, "FO")) 5 else 4
                        assertEquals(expected, wptLine.size, "${wptLine.size} parameters for $sidName, expected $expected")
                        val wptName = wptLine[0]
                        assertTrue(wptData.containsKey(wptName), "Waypoint $wptName for $sidName not in waypoints.way")
                    }

                    val trans = sidData.getJSONArray("transitions")
                    for (i in 0 until trans.length()) {
                        val transData = trans.getJSONArray(i)
                        for (j in 0 until transData.length()) {
                            val lineData = transData.getString(j).split(" ").toTypedArray()
                            assertTrue(lineData[0] == "HDG" || lineData[0] == "WPT", "Invalid transition leg ${lineData[0]} for $sidName")
                            if (lineData[0] == "HDG") {
                                assertTrue(lineData.size > 1, "Missing heading transition for $sidName")
                                for (index in 1 until lineData.size) {
                                    assertTrue(MathTools.withinRange(lineData[index].toInt(), 1, 360), "Invalid heading transition ${lineData[index]} for $sidName")
                                }
                            } else if (lineData[0] == "WPT") {
                                val expected = if (ArrayUtils.contains(lineData, "FO")) 6 else 5
                                assertEquals(expected, lineData.size, "${lineData.size} parameters for $sidName waypoint transition, expected $expected")
                                val wptName = lineData[1]
                                assertTrue(wptData.containsKey(wptName), "Waypoint $wptName for $sidName waypoint transition not in waypoints.way")
                            }
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun checkStar(): Iterable<DynamicTest> {
        val inboundExceptions = HashMap<String, HashSet<String>>()
        inboundExceptions["TCTT"] = HashSet(arrayListOf("NIBBO"))

        Gdx.files = Lwjgl3Files()
        val handle = Gdx.files.internal("game/available.arpt")
        assertTrue(handle.exists(), "available.arpt missing")
        val airportArray = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
        val allArptArray = Array<Triple<String, String, String>>()
        airportArray.map { arptData ->
            val arpt = arptData.split(":".toRegex()).toTypedArray()[0]
            val airac = arptData.split(":".toRegex()).toTypedArray()[1].split(",".toRegex())
                .toTypedArray()[0].split("-".toRegex()).toTypedArray()[1]
            val handle4 = Gdx.files.internal("game/$arpt/$airac/airport.arpt")
            val jo = JSONObject(handle4.readString())
            val airports = jo.getJSONObject("airports")
            for (icao in airports.keySet()) {
                allArptArray.add(Triple<String, String, String>(icao, airac, arpt))
            }
        }
        return allArptArray.map { data ->
            val arpt = data.first
            val airac = data.second
            val mainName = data.third
            DynamicTest.dynamicTest("$arpt STAR check") {
                val wptHandle = Gdx.files.internal("game/$mainName/$airac/waypoint.way")
                val wptData = HashMap<String, IntArray>()
                val wptArray = wptHandle.readString().split("\\r?\\n".toRegex()).toTypedArray()
                for (wptLine in wptArray) {
                    val indivWpt = wptLine.split(" ")
                    wptData[indivWpt[0]] = intArrayOf(indivWpt[1].toInt(), indivWpt[2].toInt())
                }
                val arptHandle = Gdx.files.internal("game/$mainName/$airac/star$arpt.star")
                assertTrue(arptHandle.exists(), "STAR file does not exist")
                val jo = JSONObject(arptHandle.readString())
                for (starName in jo.keySet()) {
                    //For each STAR
                    val sidData = jo.getJSONObject(starName)
                    assertTrue(sidData.getBoolean("day") || sidData.getBoolean("night"), "Invalid day/night for $starName")
                    val rwys = sidData.getJSONObject("rwys")
                    assertTrue(rwys.length() > 0, "No runways for $starName")
                    for (rwy in rwys.keySet()) {
                        val rwyWpts = rwys.getJSONArray(rwy)
                        for (i in 0 until rwyWpts.length()) {
                            val wptLine = rwyWpts.getString(i).split(" ".toRegex()).toTypedArray()
                            val expected = if (ArrayUtils.contains(wptLine, "FO")) 5 else 4
                            assertEquals(expected, wptLine.size, "${wptLine.size} parameters for $starName $rwy, expected $expected")
                            val wptName = wptLine[0]
                            assertTrue(wptData.containsKey(wptName), "Waypoint $wptName for $starName $rwy not in waypoints.way")
                            val coord = wptData[wptName]!!
                            assertTrue(MathTools.withinRange(coord[0], 1311, 4449) && MathTools.withinRange(coord[1], 51, 3289), "$wptName not within spawn range for $starName $rwy")
                        }
                    }

                    val route = sidData.getJSONArray("route")
                    for (i in 0 until route.length()) {
                        val wptLine = route.getString(i).split(" ".toRegex()).toTypedArray()
                        val expected = if (ArrayUtils.contains(wptLine, "FO")) 5 else 4
                        assertEquals(expected, wptLine.size, "${wptLine.size} parameters for $starName, expected $expected")
                        val wptName = wptLine[0]
                        assertTrue(wptData.containsKey(wptName), "Waypoint $wptName for $starName not in waypoints.way")
                        val coord = wptData[wptName]!!
                        assertTrue(MathTools.withinRange(coord[0], 1311, 4449) && MathTools.withinRange(coord[1], 51, 3289), "$wptName not within spawn range for $starName")
                    }

                    val trans = sidData.getJSONArray("inbound")
                    for (i in 0 until trans.length()) {
                        val transData = trans.getJSONArray(i)
                        for (j in 0 until transData.length()) {
                            val lineData = transData.getString(j).split(" ").toTypedArray()
                            assertTrue(lineData[0] == "HDG" || lineData[0] == "WPT", "Invalid inbound leg ${lineData[0]} for $starName")
                            if (lineData[0] == "HDG") {
                                assertTrue(lineData.size > 1, "Missing inbound heading for $starName")
                                for (index in 1 until lineData.size) {
                                    assertTrue(MathTools.withinRange(lineData[index].toInt(), 1, 360), "Invalid inbound heading ${lineData[index]} for $starName")
                                }
                            } else if (lineData[0] == "WPT") {
                                val expected = if (ArrayUtils.contains(lineData, "FO")) 6 else 5
                                assertEquals(expected, lineData.size, "${lineData.size} parameters for $starName inbound waypoint, expected $expected")
                                val wptName = lineData[1]
                                assertTrue(wptData.containsKey(wptName), "Waypoint $wptName for $starName inbound waypoint not in waypoints.way")
                                if (inboundExceptions.containsKey(wptName)) {
                                    val coord = wptData[wptName]!!
                                    assertTrue(MathTools.withinRange(coord[0], 1311, 4449) && MathTools.withinRange(coord[1], 51, 3289), "$wptName not within spawn range for $starName inbound")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @TestFactory
    fun checkHold(): Iterable<DynamicTest> {
        //val holdExceptions = HashMap<String, HashSet<String>>()
        //holdExceptions["TCTT"] = HashSet(arrayListOf("NIBBO"))

        Gdx.files = Lwjgl3Files()
        val handle = Gdx.files.internal("game/available.arpt")
        assertTrue(handle.exists(), "available.arpt missing")
        val airportArray = handle.readString().split("\\r?\\n".toRegex()).toTypedArray()
        val allArptArray = Array<Triple<String, String, String>>()
        airportArray.map { arptData ->
            val arpt = arptData.split(":".toRegex()).toTypedArray()[0]
            val airac = arptData.split(":".toRegex()).toTypedArray()[1].split(",".toRegex())
                .toTypedArray()[0].split("-".toRegex()).toTypedArray()[1]
            val handle4 = Gdx.files.internal("game/$arpt/$airac/airport.arpt")
            val jo = JSONObject(handle4.readString())
            val airports = jo.getJSONObject("airports")
            for (icao in airports.keySet()) {
                allArptArray.add(Triple<String, String, String>(icao, airac, arpt))
            }
        }
        return allArptArray.map { data ->
            val arpt = data.first
            val airac = data.second
            val mainName = data.third
            DynamicTest.dynamicTest("$arpt hold check") {
                val wptHandle = Gdx.files.internal("game/$mainName/$airac/waypoint.way")
                val wptData = HashMap<String, IntArray>()
                val wptArray = wptHandle.readString().split("\\r?\\n".toRegex()).toTypedArray()
                for (wptLine in wptArray) {
                    val indivWpt = wptLine.split(" ")
                    wptData[indivWpt[0]] = intArrayOf(indivWpt[1].toInt(), indivWpt[2].toInt())
                }
                val arptHandle = Gdx.files.internal("game/$mainName/$airac/hold$arpt.hold")
                assertTrue(arptHandle.exists(), "Hold file does not exist")
                val jo = JSONObject(arptHandle.readString())
                for (holdName in jo.keySet()) {
                    //For each hold waypoint
                    assertTrue(wptData.containsKey(holdName), "Waypoint $holdName for holding not in waypoints.way")
                    assertTrue(MathTools.withinRange(jo.getJSONObject(holdName).getInt("inboundHdg"), 1, 360), "Invalid inbound heading ${jo.getJSONObject(holdName).getInt("inboundHdg")} for $holdName holding")
                    val coord = wptData[holdName]!!
                    assertTrue(MathTools.withinRange(coord[0], 1311, 4449) && MathTools.withinRange(coord[1], 51, 3289), "$holdName not within spawn range for holding")
                }
            }
        }
    }
}