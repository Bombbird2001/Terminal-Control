package com.bombbird.terminalcontrol.entities.aircrafts;

import com.badlogic.gdx.math.MathUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONObject;

public class Emergency {
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
    private boolean stayOnRwy;

    public Emergency(Aircraft aircraft) {
        this.aircraft = aircraft;
        emergency = MathUtils.randomBoolean(1 / 200f); //1 in 200 chance of emergency
        active = false;
        type = Type.values()[MathUtils.random(Type.values().length - 1)];
        timeRequired = MathUtils.random(180, 600); //Between 3 to 10 minutes
        fuelDumpRequired = randomFuelDump();
        fuelDumpTime = fuelDumpRequired ? MathUtils.random(600, 900) : 0;
        stayOnRwy = randomStayOnRwy();
    }

    public Emergency(Aircraft aircraft, JSONObject save) {
        this.aircraft = aircraft;
        emergency = save.getBoolean("emergency");
        active = save.getBoolean("active");
        type = Type.valueOf(save.getString("type"));
        timeRequired = save.getInt("timeRequired");
        fuelDumpRequired = save.getBoolean("fuelDumpRequired");
        fuelDumpTime = save.getInt("fuelDumpTime");
        stayOnRwy = save.getBoolean("stayOnRwy");
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
}
