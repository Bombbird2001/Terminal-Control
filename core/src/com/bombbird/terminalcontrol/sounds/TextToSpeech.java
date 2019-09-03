package com.bombbird.terminalcontrol.sounds;

import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;

import java.util.HashMap;

public interface TextToSpeech {
    void initArrContact(String voice, String apchCallsign, String greeting, String icao, String flightNo, String wake, String action, String star, boolean starSaid, String direct, boolean inboundSaid, String info);

    void goAroundContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String action, String heading);

    void initDepContact(String voice, String apchCallsign, String greeting, String icao, String outbound, String flightNo, String wake, String airport, String action, String sid, boolean sidSaid);

    void holdEstablishMsg(String voice, String icao, String flightNo, String wake, String wpt, int type);

    void contactOther(String voice, String frequency, String icao, String flightNo, String wake);

    void lowFuel(String voice, int status, String icao, String flightNo, char wake);

    void cancel();

    void test(HashMap<String, Star> stars, HashMap<String, Sid> sids);
}
