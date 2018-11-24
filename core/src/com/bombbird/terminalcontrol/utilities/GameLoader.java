package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Gdx;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import org.json.JSONArray;
import org.json.JSONObject;

public class GameLoader {
    /** Loads save information from the save JSONObject */
    public static void loadSaveData(JSONObject save) {
        if (save == null) return;

        loadAircraft(save.getJSONArray("aircrafts"));
        loadAirportData(save.getJSONArray("airports"));
    }

    /** Loads aircraft data from save */
    private static void loadAircraft(JSONArray aircrafts) {
        for (Aircraft aircraft: TerminalControl.radarScreen.aircrafts.values()) {
            aircraft.removeAircraft();
        }

        for (int i = 0; i < aircrafts.length(); i++) {
            if ("Arrival".equals(aircrafts.getJSONObject(i).getString("TYPE"))) {
                //Load arrival
                Arrival arrival = new Arrival(aircrafts.getJSONObject(i));
                TerminalControl.radarScreen.aircrafts.put(arrival.getCallsign(), arrival);
            } else if ("Departure".equals(aircrafts.getJSONObject(i).getString("TYPE"))) {
                //Load departure
                Departure departure = new Departure(aircrafts.getJSONObject(i));
                TerminalControl.radarScreen.aircrafts.put(departure.getCallsign(), departure);
            } else {
                //Unknown type TODO En-route aircraft in future
                Gdx.app.log("Aircraft load error", "Unknown aircraft type " + aircrafts.getJSONObject(i).getString("TYPE") + " in save file!");
            }
        }
    }

    /** Loads airport data from save */
    private static void loadAirportData(JSONArray airports) {
        for (int i = 0; i < airports.length(); i++) {
            Airport airport = new Airport(airports.getJSONObject(i));
            TerminalControl.radarScreen.airports.put(airport.getIcao(), airport);
            airport.loadOthers(airports.getJSONObject(i).getJSONObject("takeoffManager"));
        }
    }
}
