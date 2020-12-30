package com.bombbird.terminalcontrol.utilities.files

import com.badlogic.gdx.Gdx
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival
import com.bombbird.terminalcontrol.entities.aircrafts.Departure
import com.bombbird.terminalcontrol.entities.airports.Airport
import com.bombbird.terminalcontrol.entities.weather.ThunderCell
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen
import com.bombbird.terminalcontrol.utilities.RenameManager.renameAirportICAO
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object GameLoader {
    /** Loads save information from the save JSONObject  */
    fun loadSaveData(save: JSONObject?) {
        val radarScreen = TerminalControl.radarScreen!!
        if (save == null) return

        val airports = save.getJSONArray("airports")
        for (airport in radarScreen.airports.values) {
            for (runway in airport.runways.values) {
                runway.label.remove()
            }
        }

        val jsonArray = save.getJSONArray("allAircraft")
        val allAircrafts = HashSet<String>()
        for (i in 0 until jsonArray.length()) {
            allAircrafts.add(jsonArray.getString(i))
        }
        radarScreen.allAircraft = allAircrafts

        loadAirportData(airports)
        radarScreen.metar.updateMetar(false)
        loadAircraft(save.getJSONArray("aircrafts"))
        for (i in 0 until airports.length()) {
            val airport = radarScreen.airports[renameAirportICAO(airports.getJSONObject(i).getString("icao"))]
            airport?.takeoffManager?.updatePrevAcft(airports.getJSONObject(i).getJSONObject("takeoffManager"))
            airport?.updateOtherRunwayInfo(airports.getJSONObject(i))
        }
        radarScreen.utilityBox.loadSave(save.getJSONArray("commBox"))
        radarScreen.separationChecker.lastNumber = save.getInt("lastNumber")
        radarScreen.separationChecker.time = save.optDouble("sepTime", 3.0).toFloat()

        radarScreen.playTime = save.optDouble("playTime", estimatePlayTime(radarScreen).toDouble()).toFloat()

        radarScreen.handoverController.loadSaveData(save.optJSONObject("handoverController"))

        val array = save.optJSONArray("thunderCells")
        if (array != null) {
            for (i in 0 until array.length()) {
                radarScreen.thunderCellArray.add(ThunderCell(array.optJSONObject(i)))
            }
        }

        //GameSaver.saveGame();
    }

    /** Estimates the duration played (if save has no play time data)  */
    private fun estimatePlayTime(radarScreen: RadarScreen): Float {
        var landed = 0
        for (airport in radarScreen.airports.values) {
            landed += airport.landings
        }

        //Assume 90 seconds between landings lol
        return (landed * 90).toFloat()
    }

    /** Loads aircraft data from save  */
    private fun loadAircraft(aircrafts: JSONArray) {
        val radarScreen = TerminalControl.radarScreen!!
        for (aircraft in radarScreen.aircrafts.values) {
            aircraft.removeAircraft()
        }
        for (i in 0 until aircrafts.length()) {
            when (aircrafts.getJSONObject(i).getString("TYPE")) {
                "Arrival" -> {
                    //Load arrival
                    val arrival = Arrival(aircrafts.getJSONObject(i))
                    radarScreen.aircrafts[arrival.callsign] = arrival
                }
                "Departure" -> {
                    //Load departure
                    val departure = Departure(aircrafts.getJSONObject(i))
                    radarScreen.aircrafts[departure.callsign] = departure
                }
                //Unknown type TODO En-route aircraft in future
                else -> Gdx.app.log("Aircraft load error", "Unknown aircraft type " + aircrafts.getJSONObject(i).getString("TYPE") + " in save file!")
            }
        }
    }

    /** Loads airport data from save  */
    private fun loadAirportData(airports: JSONArray) {
        for (i in 0 until airports.length()) {
            val airport = Airport(airports.getJSONObject(i))
            TerminalControl.radarScreen?.airports?.set(airport.icao, airport)
            airport.loadOthers(airports.getJSONObject(i))
        }
    }
}