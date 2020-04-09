package com.bombbird.terminalcontrol.sounds;

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;

import java.util.HashMap;

public interface TextToSpeech {
    void initArrContact(Aircraft aircraft, String apchCallsign, String greeting, String action, String star, boolean starSaid, String direct, boolean inboundSaid, String info);

    void goAroundContact(Aircraft aircraft, String apchCallsign, String action, String heading);

    void goAroundMsg(Aircraft aircraft, String goArdText, String reason);

    void initDepContact(Aircraft aircraft, String depCallsign, String greeting, String outbound, String airborne, String action, String sid, boolean sidSaid);

    void holdEstablishMsg(Aircraft aircraft, String wpt, int type);

    void contactOther(Aircraft aircraft, String frequency);

    void lowFuel(Aircraft aircraft, int status);

    void sayEmergency(Aircraft aircraft, String emergency, String intent);

    void sayRemainingChecklists(Aircraft aircraft, boolean dumpFuel);

    void sayReadyForDump(Aircraft aircraft);

    void sayDumping(Aircraft aircraft);

    void sayRemainingDumpTime(Aircraft aircraft, int min);

    void sayReadyForApproach(Aircraft aircraft, boolean stayOnRwy);

    void cancel();

    void test(HashMap<String, Star> stars, HashMap<String, Sid> sids);
}
