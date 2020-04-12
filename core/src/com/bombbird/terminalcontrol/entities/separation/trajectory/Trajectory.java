package com.bombbird.terminalcontrol.entities.separation.trajectory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class Trajectory {
    public static final int INTERVAL = 5;

    private Aircraft aircraft;
    private float deltaHeading;
    private Array<PositionPoint> positionPoints;

    public Trajectory(Aircraft aircraft) {
        this.aircraft = aircraft;
        deltaHeading = 0;
        positionPoints = new Array<>();
    }

    /** Calculates trajectory of aircraft, adds points to array */
    public void calculateTrajectory() {
        //Calculate simple linear trajectory, plus arc if aircraft is turning > 5 degrees
        int requiredTime = Math.max(TerminalControl.radarScreen.areaWarning, TerminalControl.radarScreen.collisionWarning);
        requiredTime = Math.max(requiredTime, TerminalControl.radarScreen.advTraj);
        positionPoints.clear();
        float targetHeading = (float) aircraft.getHeading() + deltaHeading;
        int windSpd = aircraft.getWinds()[1];
        int windHdg = aircraft.getWinds()[0];
        if (windHdg == 0) windSpd = 0;
        float targetTrack = targetHeading + (float) aircraft.calculateAngleDiff(targetHeading,  windHdg + 180, windSpd) - TerminalControl.radarScreen.magHdgDev;
        targetTrack = (float) MathTools.modulateHeading(targetTrack);
        if (Math.abs(deltaHeading) > 5) {
            //Calculate arc if aircraft is turning > 5 degrees
            float turnRate = aircraft.getIas() > 250 ? 1.5f : 3f; //In degrees/second
            float turnRadius = aircraft.getGs() / 3600 / (float) Math.toRadians(turnRate); //In nautical miles - r = v/w - turnRate must be converted to radians/second - GS must be coverted to nm/second
            float centerOffsetAngle = 360 - (float) aircraft.getTrack(); //In degrees
            float deltaX = MathTools.nmToPixel(turnRadius) * (float) Math.cos(Math.toRadians(centerOffsetAngle)); //In px
            float deltaY = MathTools.nmToPixel(turnRadius) * (float) Math.sin(Math.toRadians(centerOffsetAngle)); //In px
            Vector2 turnCenter = new Vector2();
            Vector2 centerToCircum = new Vector2();
            if (deltaHeading > 0) {
                //Turning right
                turnCenter.x = aircraft.getX() + deltaX;
                turnCenter.y = aircraft.getY() + deltaY;
                centerToCircum.x = -deltaX;
                centerToCircum.y = -deltaY;
            } else {
                //Turning left
                turnCenter.x = aircraft.getX() - deltaX;
                turnCenter.y = aircraft.getY() - deltaY;
                centerToCircum.x = deltaX;
                centerToCircum.y = deltaY;
                turnRate = -turnRate;
            }
            float remainingAngle = deltaHeading;
            Vector2 prevPos = new Vector2();
            boolean sidStarMode = aircraft.getNavState() != null && (aircraft.getNavState().getDispLatMode().first().contains("arrival") || aircraft.getNavState().getDispLatMode().first().contains("departure") || "After waypoint, fly heading".equals(aircraft.getNavState().getDispLatMode().first()) || "Hold at".equals(aircraft.getNavState().getDispLatMode().first()) && !aircraft.isHolding());
            float prevTargetTrack = targetTrack;
            for (int i = INTERVAL; i <= requiredTime; i += INTERVAL) {
                if (remainingAngle / turnRate > INTERVAL) {
                    remainingAngle -= turnRate * INTERVAL;
                    centerToCircum.rotate(-turnRate * INTERVAL);
                    Vector2 newVector = new Vector2(turnCenter);
                    prevPos = newVector.add(centerToCircum);
                    if (sidStarMode && aircraft.getDirect() != null) {
                        //Do additional turn checking
                        float newTrack = (float) MathTools.modulateHeading(MathTools.getRequiredTrack(prevPos.x, prevPos.y, aircraft.getDirect().getPosX(), aircraft.getDirect().getPosY()));
                        remainingAngle += (newTrack - prevTargetTrack); //Add the difference in target track to remaining angle
                        if (newTrack < 16 && newTrack > 0 && prevTargetTrack <= 360 && prevTargetTrack > 344) remainingAngle += 360; //In case new track rotates right past 360 hdg
                        if (newTrack <= 360 && newTrack > 344 && prevTargetTrack < 16 && newTrack > 0) remainingAngle -= 360; //In case new track rotates left past 360 hdg
                        prevTargetTrack = newTrack;
                    }
                } else {
                    float remainingTime = INTERVAL - remainingAngle / turnRate;
                    centerToCircum.rotate(-remainingAngle);
                    Vector2 newVector = new Vector2(turnCenter);
                    if (Math.abs(remainingAngle) > 0.1) prevPos = newVector.add(centerToCircum);
                    remainingAngle = 0;
                    Vector2 straightVector = new Vector2(0, MathTools.nmToPixel(remainingTime * aircraft.getGs() / 3600));
                    straightVector.rotate(-prevTargetTrack);
                    prevPos.add(straightVector);
                }
                positionPoints.add(new PositionPoint(aircraft, prevPos.x, prevPos.y, 0));
            }
        } else {
            for (int i = INTERVAL; i <= requiredTime; i += INTERVAL) {
                Vector2 trackVector = new Vector2(0, MathTools.nmToPixel(i * aircraft.getGs() / 3600));
                trackVector.rotate(aircraft.isOnGround() ? -aircraft.getRunway().getHeading() + TerminalControl.radarScreen.magHdgDev : -targetTrack);
                positionPoints.add(new PositionPoint(aircraft, aircraft.getX() + trackVector.x, aircraft.getY() + trackVector.y, 0));
            }
        }

        int index = 1;
        for (PositionPoint positionPoint: positionPoints) {
            int time = index * INTERVAL; //Time from now in seconds
            float targetAlt = aircraft.getTargetAltitude();
            if (aircraft.isGsCap()) targetAlt = -100;
            if (aircraft.getAltitude() > aircraft.getTargetAltitude()) {
                //Descending
                positionPoint.altitude = (int) Math.max(aircraft.getAltitude() + aircraft.getEffectiveVertSpd()[0] * time / 60, targetAlt);
            } else {
                //Climbing
                positionPoint.altitude = (int) Math.min(aircraft.getAltitude() + aircraft.getEffectiveVertSpd()[1] * time / 60, targetAlt);
            }
            index++;
        }
    }

    public void renderPoints() {
        if (!aircraft.isSelected()) return;
        TerminalControl.radarScreen.shapeRenderer.setColor(Color.ORANGE);
        for (int i = 0; i < TerminalControl.radarScreen.advTraj / 5; i++) {
            if (i >= positionPoints.size) break;
            PositionPoint positionPoint = positionPoints.get(i);
            TerminalControl.radarScreen.shapeRenderer.circle(positionPoint.x, positionPoint.y, 5);
        }
    }

    public void setDeltaHeading(float deltaHeading) {
        this.deltaHeading = deltaHeading;
    }

    public Array<PositionPoint> getPositionPoints() {
        return positionPoints;
    }
}
