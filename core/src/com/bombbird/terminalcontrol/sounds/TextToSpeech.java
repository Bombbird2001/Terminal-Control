package com.bombbird.terminalcontrol.sounds;

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;

import java.util.HashMap;

public interface TextToSpeech {
    void initArrContact(Aircraft aircraft, String wake, String apchCallsign, String greeting, String action, String star, boolean starSaid, String direct, boolean inboundSaid, String info);

    void goAroundContact(Aircraft aircraft, String wake, String apchCallsign, String action, String heading);

    void initDepContact(Aircraft aircraft, String wake, String depCallsign, String greeting, String outbound, String airport, String action, String sid, boolean sidSaid);

    void holdEstablishMsg(Aircraft aircraft, String wake, String wpt, int type);

    void contactOther(Aircraft aircraft, String wake, String frequency);

    void lowFuel(Aircraft aircraft, String wake, int status);

    void cancel();

    void test(HashMap<String, Star> stars, HashMap<String, Sid> sids);
}
