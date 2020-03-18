package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

public class Emergency {
    private RadarScreen radarScreen;

    public enum Type {
        MEDICAL,
        ENGINE_FAIL,
        BIRD_STRIKE,
        HYDRAULIC_FAIL,
        PRESSURE_LOSS,
        FUEL_LEAK
    }

    public enum Chance {
        OFF,
        LOW,
        MEDIUM,
        HIGH
    }

    private static final String[] canDumpFuel = new String[] {"A332", "A333", "A342", "A343", "A345", "A346", "A359", "A35K", "A388", "B742", "B743", "B744", "B748", "B762", "B763", "B764",
            "B772", "B77L", "B773", "B77W", "B788", "B789", "B78X", "MD11"};

    private Aircraft aircraft;
    private boolean emergency;
    private boolean active;
    private Type type;
    private float timeRequired; //Preparation time required before dump/approach
    private boolean checklistsSaid; //Whether aircraft has informed controller of running checklists, fuel dump
    private boolean readyForDump; //Preparation complete
    private float fuelDumpLag; //Time between preparation complete and fuel dump
    private boolean dumpingFuel; //Dumping fuel
    private boolean fuelDumpRequired; //Whether a fuel dump is required
    private float fuelDumpTime; //Fuel dump time required before approach
    private boolean remainingTimeSaid; //Whether aircraft has notified approach of remaining time
    private int sayRemainingTime; //Time to notify approach of remaining time
    private boolean readyForApproach; //Ready for approach
    private boolean stayOnRwy; //Needs to stay on runway, close it after landing
    private float stayOnRwyTime; //Time for staying on runway

    private int emergencyStartAlt; //Altitude where emergency occurs

    public Emergency(Aircraft aircraft, Chance emerChance) {
        radarScreen = TerminalControl.radarScreen;
        this.aircraft = aircraft;
        if (emerChance == Chance.OFF || !(aircraft instanceof Departure)) {
            emergency = false;
        } else {
            float chance = 0;
            if (emerChance == Chance.LOW) chance = 1 / 200f;
            if (emerChance == Chance.MEDIUM) chance = 1 / 100f;
            if (emerChance == Chance.HIGH) chance = 1 / 50f;
            emergency = MathUtils.randomBoolean(chance); //Chance depends on emergency setting and must be a departure (for now)
        }
        active = false;
        type = Type.values()[MathUtils.random(Type.values().length - 1)];
        timeRequired = MathUtils.random(300, 600); //Between 5 to 10 minutes
        checklistsSaid = false;
        readyForDump = false;
        fuelDumpLag = MathUtils.random(30, 60); //Between half to one minute of time between ready for dump and actual dump start
        dumpingFuel = false;
        fuelDumpRequired = randomFuelDump();
        fuelDumpTime = fuelDumpRequired ? MathUtils.random(600, 900) : 0;
        remainingTimeSaid = false;
        sayRemainingTime = (int)(0.5f * fuelDumpTime / 60);
        readyForApproach = false;
        stayOnRwy = randomStayOnRwy();
        stayOnRwyTime = MathUtils.random(300, 600); //Runway stays closed for 5-10 minutes
        emergencyStartAlt = randomEmerAlt();
    }

    public Emergency(Aircraft aircraft, boolean forceEmergency) {
        //Special constructor used when you want to force an aircraft to have an emergency or not
        radarScreen = TerminalControl.radarScreen;
        this.aircraft = aircraft;
        emergency = forceEmergency;
        active = false;
        type = Type.values()[MathUtils.random(Type.values().length - 1)];
        timeRequired = MathUtils.random(300, 600); //Between 5 to 10 minutes
        checklistsSaid = false;
        readyForDump = false;
        fuelDumpLag = MathUtils.random(30, 60); //Between half to one minute of time between ready for dump and actual dump start
        dumpingFuel = false;
        fuelDumpRequired = randomFuelDump();
        fuelDumpTime = fuelDumpRequired ? MathUtils.random(600, 900) : 0;
        remainingTimeSaid = false;
        sayRemainingTime = (int)(0.5f * fuelDumpTime / 60);
        readyForApproach = false;
        stayOnRwy = randomStayOnRwy();
        stayOnRwyTime = MathUtils.random(300, 600); //Runway stays closed for 5-10 minutes
        emergencyStartAlt = randomEmerAlt();
    }

    public Emergency(Aircraft aircraft, JSONObject save) {
        radarScreen = TerminalControl.radarScreen;
        this.aircraft = aircraft;
        emergency = save.getBoolean("emergency");
        active = save.getBoolean("active");
        type = Type.valueOf(save.getString("type"));
        timeRequired = (float) save.getDouble("timeRequired");
        checklistsSaid = save.optBoolean("checklistsSaid");
        readyForDump = save.optBoolean("readyForDump");
        fuelDumpLag = (float) save.optDouble("fuelDumpLag", MathUtils.random(30, 60));
        dumpingFuel = save.optBoolean("dumpingFuel");
        fuelDumpRequired = save.getBoolean("fuelDumpRequired");
        fuelDumpTime = (float) save.getDouble("fuelDumpTime");
        remainingTimeSaid = save.optBoolean("remainingTimeSaid");
        sayRemainingTime = save.optInt("sayRemainingTime", (int) (fuelDumpTime / 120));
        readyForApproach = save.getBoolean("readyForApproach");
        stayOnRwy = save.getBoolean("stayOnRwy");
        stayOnRwyTime = (float) save.optDouble("stayOnRwyTime", MathUtils.random(300, 600));
        emergencyStartAlt = save.getInt("emergencyStartAlt");
    }

    public void update() {
        if (!emergency) return;
        float dt = Gdx.graphics.getDeltaTime();
        if (aircraft instanceof Departure) {
            //Outbound emergency
            if (aircraft.getAltitude() > emergencyStartAlt && !active) {
                //Initiate the emergency
                aircraft.getDataTag().setEmergency();
                cancelSidStar();
                active = true;
                radarScreen.setPlanesToControl(Math.min(radarScreen.getPlanesToControl(), 5));
                aircraft.getDataTag().setMinimized(false);
                if (type == Type.BIRD_STRIKE || type == Type.ENGINE_FAIL) {
                    aircraft.setTypClimb((int) (aircraft.getTypClimb() * 0.5));
                    aircraft.setMaxClimb((int) (aircraft.getMaxClimb() * 0.5));
                }
                sayEmergency();
                //Create new arrival with same callsign and everything, remove old departure
                if (aircraft.isSelected()) {
                    radarScreen.ui.setSelectedPane(null);
                }
                Arrival arrival = new Arrival((Departure) aircraft);
                aircraft.removeAircraft();
                radarScreen.aircrafts.put(arrival.getCallsign(), arrival);
                radarScreen.separationChecker.updateAircraftPositions();
                if (aircraft.isSelected()) {
                    radarScreen.ui.setSelectedPane(arrival);
                    arrival.setSelected(true);
                    aircraft.setSelected(false);
                }
                aircraft = arrival;
                return;
            }

        }
        if (active) {
            //Emergency ongoing
            timeRequired -= dt;
            if (timeRequired < 180 && !checklistsSaid) {
                //When aircraft needs 3 more minutes, inform controller of remaining time and whether fuel dump is required
                checklistsSaid = true;
                sayRunChecklists();
            }
            if (timeRequired < 0 && !readyForDump) {
                //Preparation time over
                readyForDump = true;
                if (fuelDumpRequired) {
                    sayReadyForDump();
                }
            }
            if (readyForDump) {
                //Ready for fuel dumping (if available) or approach
                fuelDumpLag -= dt;
            }
            if (fuelDumpRequired && !dumpingFuel && fuelDumpLag < 0) {
                //Aircraft is dumping fuel
                dumpingFuel = true;
                sayDumping();
            }
            if (dumpingFuel) {
                //Fuel dump ongoing
                fuelDumpTime -= dt;
                if (!remainingTimeSaid && fuelDumpTime <= sayRemainingTime * 60) {
                    remainingTimeSaid = true;
                    sayRemainingDumpTime();
                }
            }
            if (!readyForApproach && fuelDumpTime <= 0 && timeRequired <= 0) {
                readyForApproach = true;
                if (aircraft.isSelected()) {
                    aircraft.updateUISelections();
                    aircraft.ui.updateState();
                }
                sayReadyForApproach();
            }
            if (aircraft.isTkOfLdg() && stayOnRwy && stayOnRwyTime > 0) {
                //Aircraft has touched down and needs to stay on runway
                stayOnRwyTime -= dt;
                String rwy = aircraft.getIls().getName().substring(3);
                if (!aircraft.getAirport().getRunways().get(rwy).isEmergencyClosed()) {
                    aircraft.getAirport().getRunways().get(rwy).setEmergencyClosed(true);
                    aircraft.getAirport().getRunways().get(rwy).getOppRwy().setEmergencyClosed(true);
                    radarScreen.getCommBox().normalMsg("Runway " + rwy + " is now closed");
                    radarScreen.getCommBox().normalMsg("Emergency vehicles are proceeding onto runway " + rwy);
                }

                if (stayOnRwyTime < 0) {
                    aircraft.getAirport().getRunways().get(rwy).setEmergencyClosed(false);
                    aircraft.getAirport().getRunways().get(rwy).getOppRwy().setEmergencyClosed(false);
                    aircraft.getAirport().updateRunwayUsage();
                    radarScreen.getCommBox().normalMsg("Emergency vehicles and subject aircraft have vacated runway " + rwy);
                    radarScreen.getCommBox().normalMsg("Runway " + rwy + " is now open");
                    stayOnRwy = false;
                }
            }
        }
    }

    /** Changes SID/STAR mode to vector mode after emergency occurs */
    private void cancelSidStar() {
        aircraft.getNavState().updateLatModes(NavState.REMOVE_ALL_SIDSTAR, false);
        aircraft.getNavState().updateAltModes(NavState.REMOVE_SIDSTAR_RESTR, false);
        aircraft.getNavState().updateSpdModes(NavState.REMOVE_SIDSTAR_RESTR, true);

        aircraft.getNavState().getDispLatMode().clear();
        aircraft.getNavState().getDispLatMode().addFirst("Fly heading");
        aircraft.getNavState().getDispAltMode().clear();
        aircraft.getNavState().getDispAltMode().addFirst(type == Type.PRESSURE_LOSS ? "Expedite climb/descent to" : "Climb/descend to");
        aircraft.getNavState().getDispSpdMode().clear();
        aircraft.getNavState().getDispSpdMode().addFirst("No speed restrictions");

        aircraft.getNavState().getClearedHdg().clear();
        aircraft.getNavState().getClearedHdg().addFirst((int) aircraft.getHeading());
        aircraft.getNavState().getClearedDirect().clear();
        aircraft.getNavState().getClearedDirect().addFirst(null);
        aircraft.getNavState().getClearedAftWpt().clear();
        aircraft.getNavState().getClearedAftWpt().addFirst(null);
        aircraft.getNavState().getClearedAftWptHdg().clear();
        aircraft.getNavState().getClearedAftWptHdg().addFirst((int) aircraft.getHeading());
        aircraft.getNavState().getClearedHold().clear();
        aircraft.getNavState().getClearedHold().addFirst(null);
        aircraft.getNavState().getClearedIls().clear();
        aircraft.getNavState().getClearedIls().addFirst(null);

        aircraft.getNavState().getClearedAlt().clear();
        aircraft.getNavState().getClearedAlt().addFirst(getClearedAltitudeAfterEmergency(aircraft.getAltitude()));
        aircraft.getNavState().getClearedExpedite().clear();
        aircraft.getNavState().getClearedExpedite().addFirst(type == Type.PRESSURE_LOSS);

        aircraft.getNavState().getClearedSpd().clear();
        aircraft.getNavState().getClearedSpd().addFirst(aircraft.getClearedIas());
        aircraft.getNavState().setLength(1);
        aircraft.getNavState().updateAircraftInfo();
    }

    /** Returns a random boolean for fuel dump given the aircraft is capable of doing so, else returns false */
    private boolean randomFuelDump() {
        if (ArrayUtils.contains(canDumpFuel, aircraft.getIcaoType())) return MathUtils.randomBoolean();
        return false;
    }

    /** Returns a random boolean for whether aircraft stays on runway after landing given the type of emergency */
    private boolean randomStayOnRwy() {
        if (type == Type.BIRD_STRIKE) return MathUtils.randomBoolean(0.7f);
        if (type == Type.ENGINE_FAIL) return MathUtils.randomBoolean(0.7f);
        if (type == Type.HYDRAULIC_FAIL) return true;
        if (type == Type.MEDICAL) return false;
        if (type == Type.PRESSURE_LOSS) return MathUtils.randomBoolean(0.3f);
        if (type == Type.FUEL_LEAK) return true;
        return false;
    }

    /** Returns a random altitude when emergency occurs depending on emergency type */
    private int randomEmerAlt() {
        if (aircraft instanceof Departure) {
            int elevation = aircraft.getAirport().getElevation();
            if (type == Type.BIRD_STRIKE) {
                if (elevation > 7000) return MathUtils.random(2300, 1500) + elevation;
                return MathUtils.random(2300, 7000) + elevation;
            }
            if (type == Type.ENGINE_FAIL) return MathUtils.random(2300, 5000) + elevation;
            if (type == Type.HYDRAULIC_FAIL) return MathUtils.random(2300, 5000) + elevation;
            if (type == Type.MEDICAL) return MathUtils.random(4000, 14000) + elevation;
            if (type == Type.PRESSURE_LOSS) return MathUtils.random(12000, 14500);
            if (type == Type.FUEL_LEAK) return MathUtils.random(11000, 14500);
        }
        return -1;
    }

    /** Returns the altitude the aircraft is targeting after emergency starts depending on emergency type */
    private int getClearedAltitudeAfterEmergency(float currentAlt) {
        if (aircraft instanceof Departure) {
            int elevation = aircraft.getAirport().getElevation();
            int minLevelOffAlt = elevation + 2000; //Minimum altitude to maintain is 2000 feet AGL
            if (type == Type.BIRD_STRIKE) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
            if (type == Type.ENGINE_FAIL) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
            if (type == Type.HYDRAULIC_FAIL) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
            if (type == Type.MEDICAL) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
            if (type == Type.PRESSURE_LOSS) return 9000;
            if (type == Type.FUEL_LEAK) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
        }
        return -1;
    }

    /** Adds comm box message, TTS when aircraft initially encounters emergency */
    private void sayEmergency() {
        String emergency = "";
        if (type == Type.BIRD_STRIKE) emergency = "an emergency due to bird strike";
        if (type == Type.ENGINE_FAIL) emergency = "an emergency due to engine failure";
        if (type == Type.HYDRAULIC_FAIL) emergency = "an emergency due to hydraulic failure";
        if (type == Type.MEDICAL) emergency = "a medical emergency";
        if (type == Type.PRESSURE_LOSS) emergency = "an emergency due to loss of cabin pressure";
        if (type == Type.FUEL_LEAK) emergency = "an emergency due to fuel leak";

        String intent;
        if (type == Type.PRESSURE_LOSS) {
            intent = ", we are initiating an emergency descent to 9000 feet";
        } else {
            String altitude = aircraft.getClearedAltitude() >= radarScreen.transLvl * 100 ? "FL" + aircraft.getClearedAltitude() / 100 : aircraft.getClearedAltitude() + " feet";
            intent = ", levelling off at " + altitude;
        }
        String text = "Mayday, mayday, mayday, " + aircraft.getCallsign() + aircraft.getWakeString() + " is declaring " + emergency + " and would like to return to the airport" + intent;
        radarScreen.getCommBox().warningMsg(text);
        TerminalControl.tts.sayEmergency(aircraft, emergency, intent);
    }

    /** Adds comm box message, TTS to notify controller of intentions, whether fuel dump is required */
    private void sayRunChecklists() {
        radarScreen.getCommBox().warningMsg(aircraft.getCallsign() + aircraft.getWakeString() + " will need a few more minutes to run checklists" + (fuelDumpRequired ? " before dumping fuel" : ""));
        TerminalControl.tts.sayRemainingChecklists(aircraft, fuelDumpRequired);
    }

    /** Adds comm box message, TTS when aircraft is ready to dump fuel */
    private void sayReadyForDump() {
        radarScreen.getCommBox().warningMsg(aircraft.getCallsign() + aircraft.getWakeString() + ", we are ready to dump fuel");
        float bearing = 90 - MathUtils.radiansToDegrees * MathUtils.atan2(aircraft.getY() - 1620, aircraft.getX() - 2880);
        int dist = (int) MathTools.pixelToNm(MathTools.distanceBetween(aircraft.getX(), aircraft.getY(), 2880, 1620));
        String dir = "";
        bearing = (float) MathTools.modulateHeading(bearing);
        if (bearing >= 330 || bearing < 30) dir = "north";
        if (MathTools.withinRange(bearing, 30, 60)) dir = "north-east";
        if (MathTools.withinRange(bearing, 60, 120)) dir = "east";
        if (MathTools.withinRange(bearing, 120, 150)) dir = "south-east";
        if (MathTools.withinRange(bearing, 150, 210)) dir = "south";
        if (MathTools.withinRange(bearing, 210, 240)) dir = "south-west";
        if (MathTools.withinRange(bearing, 240, 300)) dir = "west";
        if (MathTools.withinRange(bearing, 300, 330)) dir = "north-west";
        String alt = aircraft.getAltitude() >= radarScreen.transLvl ? ((int)(aircraft.getAltitude() / 100)) * 100 + " feet" : "FL" + (int)(aircraft.getAltitude() / 100);
        radarScreen.getCommBox().normalMsg("Attention all aircraft, fuel dumping in progress " + dist + " miles " + dir + " of " + radarScreen.mainName + ", " + alt);
        TerminalControl.tts.sayReadyForDump(aircraft);
    }

    /** Adds comm box message, TTS when aircraft starts dumping fuel */
    private void sayDumping() {
        radarScreen.getCommBox().warningMsg(aircraft.getCallsign() + aircraft.getWakeString() + " is now dumping fuel");
        TerminalControl.tts.sayDumping(aircraft);
    }

    /** Adds comm box message, TTS when aircraft is halfway through fuel dump */
    private void sayRemainingDumpTime() {
        radarScreen.getCommBox().warningMsg(aircraft.getCallsign() + aircraft.getWakeString() + ", we'll need about " + sayRemainingTime + " more minutes");
        TerminalControl.tts.sayRemainingDumpTime(aircraft, sayRemainingTime);
    }

    /** Adds comm box message, TTS when aircraft has finished dumping fuel, is ready for approach */
    private void sayReadyForApproach() {
        radarScreen.getCommBox().warningMsg(aircraft.getCallsign() + aircraft.getWakeString() + " is ready for approach" + (stayOnRwy ? ", we will stay on the runway after landing" : ""));
        TerminalControl.tts.sayReadyForApproach(aircraft, stayOnRwy);
    }

    /** Checks if aircraft is ready to conduct an ILS approach */
    public boolean isReadyForApproach() {
        return readyForApproach;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isFuelDumpRequired() {
        return fuelDumpRequired;
    }

    public void setFuelDumpRequired(boolean fuelDumpRequired) {
        this.fuelDumpRequired = fuelDumpRequired;
    }

    public boolean isStayOnRwy() {
        return stayOnRwy;
    }

    public void setStayOnRwy(boolean stayOnRwy) {
        this.stayOnRwy = stayOnRwy;
    }

    public float getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(int timeRequired) {
        this.timeRequired = timeRequired;
    }

    public float getFuelDumpTime() {
        return fuelDumpTime;
    }

    public void setFuelDumpTime(int fuelDumpTime) {
        this.fuelDumpTime = fuelDumpTime;
    }

    public int getEmergencyStartAlt() {
        return emergencyStartAlt;
    }

    public float getFuelDumpLag() {
        return fuelDumpLag;
    }

    public boolean isReadyForDump() {
        return readyForDump;
    }

    public boolean isDumpingFuel() {
        return dumpingFuel;
    }

    public boolean isRemainingTimeSaid() {
        return remainingTimeSaid;
    }

    public int getSayRemainingTime() {
        return sayRemainingTime;
    }

    public float getStayOnRwyTime() {
        return stayOnRwyTime;
    }

    public void setStayOnRwyTime(float stayOnRwyTime) {
        this.stayOnRwyTime = stayOnRwyTime;
    }

    public boolean isChecklistsSaid() {
        return checklistsSaid;
    }

    public void setChecklistsSaid(boolean checklistsSaid) {
        this.checklistsSaid = checklistsSaid;
    }
}
