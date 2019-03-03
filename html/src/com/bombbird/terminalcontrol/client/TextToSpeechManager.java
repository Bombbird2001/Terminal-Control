package com.bombbird.terminalcontrol.client;

import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.sounds.TextToSpeech;

import java.util.HashMap;

public class TextToSpeechManager implements TextToSpeech {
    @Override
    public void initArrContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String action, String star, String direct) {

    }

    @Override
    public void goAroundContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String action, String heading) {

    }

    @Override
    public void initDepContact(String voice, String apchCallsign, String icao, String flightNo, String wake, String airport, String action, String sid) {

    }

    @Override
    public void contactOther(String voice, String frequency, String icao, String flightNo, String wake) {

    }

    @Override
    public void test(HashMap<String, Star> stars, HashMap<String, Sid> sids) {

    }
}
