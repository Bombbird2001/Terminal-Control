package com.bombbird.terminalcontrol.client;

import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.sidstar.Sid;
import com.bombbird.terminalcontrol.entities.sidstar.Star;
import com.bombbird.terminalcontrol.sounds.TextToSpeech;

import java.util.HashMap;

public class TextToSpeechManager implements TextToSpeech {

    @Override
    public void initArrContact(Aircraft aircraft, String apchCallsign, String greeting, String action, String star, boolean starSaid, String direct, boolean inboundSaid, String info) {
        //No default implementation
    }

    @Override
    public void goAroundContact(Aircraft aircraft, String apchCallsign, String action, String heading) {
        //No default implementation
    }

    @Override
    public void goAroundMsg(Aircraft aircraft, String goArdText, String reason) {
        //No default implementation
    }

    @Override
    public void initDepContact(Aircraft aircraft, String depCallsign, String greeting, String outbound, String airport, String action, String sid, boolean sidSaid) {
        //No default implementation
    }

    @Override
    public void holdEstablishMsg(Aircraft aircraft, String wpt, int type) {
        //No default implementation
    }

    @Override
    public void contactOther(Aircraft aircraft, String frequency) {
        //No default implementation
    }

    @Override
    public void lowFuel(Aircraft aircraft, int status) {
        //No default implementation
    }

    @Override
    public void sayEmergency(Aircraft aircraft, String emergency, String intent) {
        //No default implementation
    }

    @Override
    public void sayRemainingChecklists(Aircraft aircraft, boolean dumpFuel) {
        //No default implementation
    }

    @Override
    public void sayReadyForDump(Aircraft aircraft) {
        //No default implementation
    }

    @Override
    public void sayDumping(Aircraft aircraft) {
        //No default implementation
    }

    @Override
    public void sayRemainingDumpTime(Aircraft aircraft, int min) {
        //No default implementation
    }

    @Override
    public void sayReadyForApproach(Aircraft aircraft, boolean stayOnRwy) {
        //No default implementation
    }

    @Override
    public void sayRequest(Aircraft aircraft, String request) {
        //No default implementation
    }

    @Override
    public void cancel() {
        //No default implementation
    }

    @Override
    public void test(HashMap<String, Star> stars, HashMap<String, Sid> sids) {
        //No default implementation
    }

    @Override
    public String getRandomVoice() {
        return null;
    }
}
