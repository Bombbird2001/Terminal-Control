package com.bombbird.terminalcontrol.entities.separation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class Trajectory {
    private static final int INTERVAL = 5;

    private Aircraft aircraft;
    private float deltaHeading;
    private Array<PositionPoint> positionPoints;

    public Trajectory(Aircraft aircraft) {
        this.aircraft = aircraft;
        deltaHeading = 0;
        positionPoints = new Array<>();

        calculateTrajectory();
    }

    public void calculateTrajectory() {
        //Calculate simple linear trajectory, plus arc if aircraft is turning > 10 degrees
        int requiredTime = Math.max(TerminalControl.radarScreen.areaWarning, TerminalControl.radarScreen.collisionWarning);
        positionPoints.clear();
        float targetHeading = (float) aircraft.getHeading() + deltaHeading;
        float targetTrack = targetHeading - (float) aircraft.calculateAngleDiff(targetHeading, aircraft.getWinds()[0] + 180, aircraft.getWinds()[1]);
        if (Math.abs(deltaHeading) > 10) {
            //Calculate arc if aircraft is turning > 10 degrees
            float turnRate = aircraft.getIas() > 250 ? 1.5f : 3f; //In degrees/second
            float turnRadius = aircraft.getGs() / 3600 / (float) Math.toRadians(turnRate); //In nautical miles - r = v/w - turnRate must be converted to radians/second - GS must be coverted to nm/second
            float centerOffsetAngle = 360 - (float) aircraft.getTrack(); //In degrees
            float deltaX = turnRadius * MathTools.nmToPixel((float) Math.cos(Math.toRadians(centerOffsetAngle))); //In px
            float deltaY = turnRadius * MathTools.nmToPixel((float) Math.sin(Math.toRadians(centerOffsetAngle))); //In px
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
            Vector2 prevPos;
            for (int i = INTERVAL; i <= requiredTime; i += INTERVAL) {
                if (remainingAngle / turnRate > 1) {
                    remainingAngle -= turnRate;
                    centerToCircum.rotate(-turnRate);
                    prevPos = turnCenter.add(centerToCircum);
                } else {
                    float remainingTime = INTERVAL - remainingAngle / turnRate;
                    centerToCircum.rotate(-remainingAngle);
                    prevPos = turnCenter.add(centerToCircum);
                    remainingAngle = 0;
                    Vector2 straightVector = new Vector2(0, MathTools.nmToPixel(remainingTime * aircraft.getGs() / 3600));
                    straightVector.rotate(-targetTrack);
                    prevPos.add(straightVector);
                }
                positionPoints.add(new PositionPoint(prevPos.x, prevPos.y, 0));
            }
        } else {
            for (int i = INTERVAL; i <= requiredTime; i += INTERVAL) {
                Vector2 trackVector = new Vector2(0, MathTools.nmToPixel(INTERVAL * aircraft.getGs() / 3600));
                trackVector.rotate(-targetTrack);
                positionPoints.add(new PositionPoint(aircraft.getX() + trackVector.x, aircraft.getY() + trackVector.y, 0));
            }
        }

        //TODO Add altitude prediction
    }

    public void renderPoints() {
        //TODO For testing: draw all points predicted
    }

    public void setDeltaHeading(float deltaHeading) {
        this.deltaHeading = deltaHeading;
    }
}
