package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.math.MathUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

public class Emergency {
    public enum Type {
        MEDICAL,
        ENGINE_FAIL,
        BIRD_STRIKE,
        HYDRAULIC_FAIL,
        PRESSURE_LOSS
    }
    private static final String[] canDumpFuel = new String[] {"A332", "A333", "A342", "A343", "A345", "A346", "A359", "A35K", "A388", "B742", "B743", "B744", "B748", "B762", "B763", "B764",
            "B772", "B77L", "B773", "B77W", "B788", "B789", "B78X", "MD11"};

    private Aircraft aircraft;
    private boolean emergency;
    private boolean active;
    private Type type;
    private int timeRequired;
    private boolean fuelDumpRequired;
    private int fuelDumpTime;
    private boolean readyForApproach;
    private boolean stayOnRwy;

    private int emergencyStartAlt;

    public Emergency(Aircraft aircraft) {
        this.aircraft = aircraft;
        emergency = MathUtils.randomBoolean(1 / 200f); //1 in 200 chance of emergency
        active = false;
        type = Type.values()[MathUtils.random(Type.values().length - 1)];
        timeRequired = MathUtils.random(180, 600); //Between 3 to 10 minutes
        fuelDumpRequired = randomFuelDump();
        fuelDumpTime = fuelDumpRequired ? MathUtils.random(600, 900) : 0;
        readyForApproach = false;
        stayOnRwy = randomStayOnRwy();
        emergencyStartAlt = randomEmerAlt();
    }

    public Emergency(Aircraft aircraft, JSONObject save) {
        this.aircraft = aircraft;
        emergency = save.getBoolean("emergency");
        active = save.getBoolean("active");
        type = Type.valueOf(save.getString("type"));
        timeRequired = save.getInt("timeRequired");
        fuelDumpRequired = save.getBoolean("fuelDumpRequired");
        fuelDumpTime = save.getInt("fuelDumpTime");
        readyForApproach = save.getBoolean("readyForApproach");
        stayOnRwy = save.getBoolean("stayOnRwy");
        emergencyStartAlt = save.getInt("emergencyStartAlt");
    }

    public void update() {
        if (aircraft instanceof Departure) {
            //Outbound emergency
            if (aircraft.getAltitude() > emergencyStartAlt) {
                //Initiate the emergency
                if (!active) cancelSidStar();
                active = true;
            }
            if (active) {
                if (!readyForApproach && fuelDumpTime <= 0 && timeRequired <= 0) {
                    readyForApproach = true;
                    if (aircraft.isSelected()) {
                        aircraft.updateUISelections();
                        aircraft.ui.updateState();
                    }
                }
            }
        } else if (aircraft instanceof Arrival) {
            //Inbound emergency
        }
    }

    /** Changes SID/STAR mode to vector mode after emergency occurs */
    private void cancelSidStar() {
        aircraft.getNavState().getLatModes().clear();
        aircraft.getNavState().getLatModes().add("Fly heading", "Turn left heading", "Turn right heading");
        aircraft.getNavState().getAltModes().removeValue("Climb via SID", false);
        aircraft.getNavState().getAltModes().removeValue("Descend vis STAR", false);
        aircraft.getNavState().getAltModes().removeValue("SID speed restrictions", false);
        aircraft.getNavState().getAltModes().removeValue("STAR speed restrictions", false);

        aircraft.getNavState().getDispLatMode().clear();
        aircraft.getNavState().getDispLatMode().addFirst("Fly heading");
        aircraft.getNavState().getDispAltMode().clear();
        aircraft.getNavState().getDispAltMode().addFirst("Climb/descend to");
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

    /** Returns a random boolean given the type of emergency */
    private boolean randomStayOnRwy() {
        if (type == Type.BIRD_STRIKE) return MathUtils.randomBoolean(0.7f);
        if (type == Type.ENGINE_FAIL) return MathUtils.randomBoolean(0.7f);
        if (type == Type.HYDRAULIC_FAIL) return true;
        if (type == Type.MEDICAL) return false;
        if (type == Type.PRESSURE_LOSS) return MathUtils.randomBoolean(0.3f);
        return false;
    }

    /** Returns a random altitude when emergency occurs depending on emergency type */
    private int randomEmerAlt() {
        if (aircraft instanceof Departure) {
            int elevation = aircraft.getAirport().getElevation();
            if (type == Type.BIRD_STRIKE) {
                if (elevation > 7000) return MathUtils.random(500, 1500) + elevation;
                return MathUtils.random(500, 7000) + elevation;
            }
            if (type == Type.ENGINE_FAIL) return MathUtils.random(500, 3000) + elevation;
            if (type == Type.HYDRAULIC_FAIL) return MathUtils.random(1000, 5000) + elevation;
            if (type == Type.MEDICAL) return MathUtils.random(3000, 14000) + elevation;
            if (type == Type.PRESSURE_LOSS) return MathUtils.random(12000, 14500);
        }
        return -1;
    }

    /** Returns the altitude the aircraft is targeting after emergency starts depending on emergency type */
    private int getClearedAltitudeAfterEmergency(float currentAlt) {
        if (aircraft instanceof Departure) {
            int elevation = aircraft.getAirport().getElevation();
            int minLevelOffAlt = elevation + 3000; //Minimum altitude to maintain is 3000 feet AGL
            if (type == Type.BIRD_STRIKE) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
            if (type == Type.ENGINE_FAIL) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
            if (type == Type.HYDRAULIC_FAIL) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
            if (type == Type.MEDICAL) return currentAlt > minLevelOffAlt ? ((int) (currentAlt / 1000) + 1) * 1000 : ((int) (minLevelOffAlt / 1000f) + 1) * 1000;
            if (type == Type.PRESSURE_LOSS) return 9000;
        }
        return -1;
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

    public int getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(int timeRequired) {
        this.timeRequired = timeRequired;
    }

    public int getFuelDumpTime() {
        return fuelDumpTime;
    }

    public void setFuelDumpTime(int fuelDumpTime) {
        this.fuelDumpTime = fuelDumpTime;
    }

    public int getEmergencyStartAlt() {
        return emergencyStartAlt;
    }
}
