package com.bombbird.terminalcontrol.entities.weather

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.HttpRequests
import com.bombbird.terminalcontrol.utilities.RenameManager.reverseNameAirportICAO
import com.bombbird.terminalcontrol.utilities.math.MathTools.modulateHeading
import org.json.JSONObject
import kotlin.collections.HashMap

class Metar(private val radarScreen: RadarScreen) {
    private var prevMetar: JSONObject? = null
    var metarObject: JSONObject? = null
    var isQuit = false

    constructor(radarScreen: RadarScreen, save: JSONObject?) : this(radarScreen) {
        prevMetar = save
        metarObject = save
    }

    /** Initialise the getting of live weather/changing of random weather  */
    fun updateMetar(tutorial: Boolean) {
        if (radarScreen.weatherSel === RadarScreen.Weather.LIVE && !tutorial) {
            HttpRequests.getMetar(this, true)
        } else {
            Thread {
                if (tutorial) {
                    updateTutorialMetar()
                } else {
                    randomWeather()
                }
            }.start()
        }
    }

    /** Sets the weather specific to tutorial  */
    private fun updateTutorialMetar() {
        metarObject = JSONObject()
        val rctpMetar = JSONObject()
        rctpMetar.put("rain", JSONObject.NULL)
        rctpMetar.put("visibility", 9000)
        rctpMetar.put("windSpeed", 14)
        rctpMetar.put("windDirection", 60)
        rctpMetar.put("windGust", JSONObject.NULL)
        rctpMetar.put("windshear", JSONObject.NULL)
        rctpMetar.put("metar", generateRawMetar(rctpMetar))
        metarObject?.put("RCTP", rctpMetar)
        val rcssMetar = JSONObject()
        rcssMetar.put("rain", JSONObject.NULL)
        rcssMetar.put("visibility", 10000)
        rcssMetar.put("windSpeed", 8)
        rcssMetar.put("windDirection", 100)
        rcssMetar.put("windGust", JSONObject.NULL)
        rcssMetar.put("windshear", JSONObject.NULL)
        rcssMetar.put("metar", generateRawMetar(rcssMetar))
        metarObject?.put("RCSS", rcssMetar)
        updateRadarScreenState()
    }

    /** Updates all in game airports with new weather data  */
    private fun updateAirports() {
        for (airport in radarScreen.airports.values) {
            if (prevMetar == null) {
                airport.rwyChangeTimer = -1f
                airport.isPendingRwyChange = true
            }
            metarObject?.let { airport.setMetar(it) }
            if (prevMetar == null) {
                airport.resetRwyChangeTimer()
            }
        }
    }

    /** Create new weather based on current (i.e. no big changes)  */
    private fun randomBasedOnCurrent(): JSONObject {
        val airports = JSONObject()
        for (airport in radarScreen.airports.keys) {
            val jsonObject = JSONObject()
            jsonObject.put("visibility", VisibilityChance.randomVis)
            var windDir = metarObject?.getJSONObject(reverseNameAirportICAO(airport))?.getInt("windDirection")?.let { it + MathUtils.random(-2, 2) * 10 } ?: WindDirChance.getRandomWindDir(airport)
            windDir = modulateHeading(windDir)
            jsonObject.put("windDirection", windDir)
            val currentSpd = metarObject?.getJSONObject(reverseNameAirportICAO(airport))?.getInt("windSpeed") ?: WindspeedChance.getRandomWindspeed(airport, windDir)
            val windSpd = (2 * currentSpd + WindspeedChance.getRandomWindspeed(airport, windDir)) / 3
            jsonObject.put("windSpeed", windSpd)
            val ws = WindshearChance.getRandomWsForAllRwy(airport, windSpd)
            jsonObject.put("windshear", if ("" == ws) JSONObject.NULL else ws)
            var gust = -1
            if (windSpd >= 15 && MathUtils.random(2) == 2) {
                //Gusts
                gust = MathUtils.random(windSpd + 3, 40)
            }
            jsonObject.put("windGust", if (gust > -1) gust else JSONObject.NULL)
            jsonObject.put("rain", JSONObject.NULL)
            jsonObject.put("metar", generateRawMetar(jsonObject))
            airports.put(reverseNameAirportICAO(airport), jsonObject)
        }
        return airports
    }

    /** Generates random weather without any previous weather to "rely" upon  */
    private fun generateRandomWeather(): JSONObject {
        val jsonObject = JSONObject()
        for (airport in radarScreen.airports.keys) {
            //For each airport, create random weather and parse to JSON object
            var windSpd: Int
            var gust = -1
            val visibility: Int = VisibilityChance.randomVis
            val windDir: Int = WindDirChance.getRandomWindDir(airport)
            windSpd = WindspeedChance.getRandomWindspeed(airport, windDir)
            val ws: String = WindshearChance.getRandomWsForAllRwy(airport, windSpd)
            if (windSpd >= 15 && MathUtils.random(2) == 2) {
                //Gusts
                gust = MathUtils.random(windSpd + 3, 40)
            }
            val newObject = JSONObject()
            newObject.put("rain", JSONObject.NULL)
            newObject.put("visibility", visibility)
            newObject.put("windDirection", windDir)
            newObject.put("windSpeed", windSpd)
            newObject.put("windGust", if (gust > -1) gust else JSONObject.NULL)
            newObject.put("windshear", if ("" == ws) JSONObject.NULL else ws)
            newObject.put("metar", generateRawMetar(newObject))
            jsonObject.put(reverseNameAirportICAO(airport), newObject)
        }
        return jsonObject
    }

    /** Generates and applies a randomised weather  */
    fun randomWeather() {
        if (radarScreen.metarLoading) {
            if (metarObject == null) {
                metarObject = generateRandomWeather()
            } else if (radarScreen.weatherSel === RadarScreen.Weather.RANDOM) {
                metarObject = randomBasedOnCurrent()
            }
            updateRadarScreenState()
        } else {
            Gdx.app.postRunnable {
                if (metarObject == null) {
                    metarObject = generateRandomWeather()
                } else if (radarScreen.weatherSel === RadarScreen.Weather.RANDOM) {
                    metarObject = randomBasedOnCurrent()
                }
                updateRadarScreenState()
            }
        }
    }

    /** Generates a basic raw metar for randomised weather  */
    private fun generateRawMetar(newObject: JSONObject): String {
        val sb = StringBuilder()
        val windDir = newObject.optInt("windDirection", 0)
        val windDirStr = when {
            windDir == 0 -> "VRB"
            windDir < 100 -> "0$windDir"
            else -> windDir.toString()
        }
        sb.append(windDirStr)
        val windSpd = newObject.optInt("windSpeed", 1)
        sb.append(if (windSpd < 10) "0$windSpd" else windSpd).append("KT ")
        var visibility = newObject.optInt("visibility", 10000)
        if (visibility >= 10000) visibility = 9999
        sb.append(visibility).append(" ")
        val temp = MathUtils.random(20, 35)
        sb.append(temp).append("/").append(temp - MathUtils.random(2, 8)).append(" ")
        sb.append("Q").append(MathUtils.random(1005, 1018)).append(" ")
        sb.append(if (newObject.isNull("windshear")) "NOSIG" else "WS " + newObject.getString("windshear"))
        return sb.toString()
    }

    /** Called after changing the metarObject, to update the in game weather and UI  */
    fun updateRadarScreenState() {
        if (isQuit) return
        if (prevMetar == null || metarObject.toString() != prevMetar.toString()) {
            Gdx.app.postRunnable { radarScreen.updateInformation() }
        }
        if (radarScreen.loadingTime < 4.5 && Thread.currentThread().name != "main") {
            val deltaTime = ((4.5 - radarScreen.loadingTime) * 1000).toLong()
            try {
                Thread.sleep(deltaTime)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        Gdx.app.postRunnable {
            updateAirports()
            prevMetar = metarObject
            radarScreen.ui.updateMetar()
            radarScreen.metarLoading = false
        }
    }

    /** Updates the METAR object and in game weather given custom weather data for airports  */
    fun updateCustomWeather(arptData: HashMap<String, IntArray>) {
        for ((key, value) in arptData) {
            val realIcao = reverseNameAirportICAO(key)
            val newObject = metarObject?.getJSONObject(realIcao) ?: continue
            newObject.put("windDirection", value[0])
            newObject.put("windSpeed", value[1])
            val randomWs = WindshearChance.getRandomWsForAllRwy(key, value[1])
            newObject.put("windshear", if ("" == randomWs) JSONObject.NULL else randomWs)
            newObject.put("visibility", value[2])
            newObject.put("metar", generateRawMetar(newObject))
        }
        updateRadarScreenState()
    }
}