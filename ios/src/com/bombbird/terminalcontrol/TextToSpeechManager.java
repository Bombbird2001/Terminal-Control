package com.bombbird.terminalcontrol;

import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.sounds.TextToSpeech;

import java.util.HashMap;

public class TextToSpeechManager implements TextToSpeech {
    @Override
    public void lowFuel(String voice, int status, String icao, String flightNo, char wake) {
        //No default implementation
    }

    @Override
    public void initArrContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String action, String star, String direct) {
        //No default implementation
    }

    @Override
    public void goAroundContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String action, String heading) {
        //No default implementation
    }

    @Override
    public void initDepContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String airport, String action, String sid) {
        //No default implementation
    }

    @Override
    public void contactOther(String voice, String frequency, String icao, String flightNo, String wake) {
        //No default implementation
    }

    @Override
    public void test(HashMap<String, Star> stars, HashMap<String, Sid> sids) {
        //No default implementation
    }
}
