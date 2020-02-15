package com.bombbird.terminalcontrol.entities.separation;

import com.badlogic.gdx.graphics.Color;
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
    }

    public void calculateTrajectory() {
        //Calculate simple linear trajectory, plus arc if aircraft is turning > 5 degrees
        int requiredTime = Math.max(TerminalControl.radarScreen.areaWarning, TerminalControl.radarScreen.collisionWarning);
        positionPoints.clear();
        float targetHeading = (float) aircraft.getHeading() + deltaHeading;
        float targetTrack = targetHeading + (float) aircraft.calculateAngleDiff(targetHeading, aircraft.getWinds()[0] + 180, aircraft.getWinds()[1]) - TerminalControl.radarScreen.magHdgDev;
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
            for (int i = INTERVAL; i <= requiredTime; i += INTERVAL) {
                if (remainingAngle / turnRate > INTERVAL) {
                    remainingAngle -= turnRate * INTERVAL;
                    centerToCircum.rotate(-turnRate * INTERVAL);
                    Vector2 newVector = new Vector2(turnCenter);
                    prevPos = newVector.add(centerToCircum);
                } else {
                    float remainingTime = INTERVAL - remainingAngle / turnRate;
                    centerToCircum.rotate(-remainingAngle);
                    Vector2 newVector = new Vector2(turnCenter);
                    if (remainingAngle > 0.1 || remainingAngle < -0.1) prevPos = newVector.add(centerToCircum);
                    remainingAngle = 0;
                    Vector2 straightVector = new Vector2(0, MathTools.nmToPixel(remainingTime * aircraft.getGs() / 3600));
                    straightVector.rotate(-targetTrack);
                    prevPos.add(straightVector);
                }
                positionPoints.add(new PositionPoint(prevPos.x, prevPos.y, 0));
            }
        } else {
            for (int i = INTERVAL; i <= requiredTime; i += INTERVAL) {
                Vector2 trackVector = new Vector2(0, MathTools.nmToPixel(i * aircraft.getGs() / 3600));
                trackVector.rotate(-targetTrack);
                positionPoints.add(new PositionPoint(aircraft.getX() + trackVector.x, aircraft.getY() + trackVector.y, 0));
            }
        }

        //TODO Add altitude prediction
    }

    public void renderPoints() {
        //TODO For testing: draw all points predicted
        if (!aircraft.isSelected()) return;
        calculateTrajectory();
        TerminalControl.radarScreen.shapeRenderer.setColor(Color.ORANGE);
        for (PositionPoint positionPoint: positionPoints) {
            TerminalControl.radarScreen.shapeRenderer.circle(positionPoint.x, positionPoint.y, 5);
        }
    }

    public void setDeltaHeading(float deltaHeading) {
        this.deltaHeading = deltaHeading;
    }
}
