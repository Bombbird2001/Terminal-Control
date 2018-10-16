package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;
import com.bombbird.atcsim.utilities.MathTools;

public class ILS extends Actor {
    private String name;
    private float x;
    private float y;
    private Runway runway;
    private int heading;
    private Array<Integer> minima;
    private boolean active;

    private static float distance1 = MathTools.nmToPixel(17);
    private static int angle1 = 35;

    private static float distance2 = MathTools.nmToPixel(25);
    private static int angle2 = 10;

    public ILS(String name, float x, float y, Runway runway, int heading, String minima) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.runway = runway;
        this.heading = heading;
        this.minima = new Array<Integer>();
        active = false;
        for (String s: minima.split(">")) {
            //Add all the minima to array
            this.minima.add(Integer.parseInt(s));
        }
    }

    public void renderShape() {
        GameScreen.shapeRenderer.setColor(Color.BLUE);
        GameScreen.shapeRenderer.arc(x, y, distance1, 270 - (heading - RadarScreen.magHdgDev + angle1 / 2f), angle1, 5);
        GameScreen.shapeRenderer.arc(x, y, distance2, 270 - (heading - RadarScreen.magHdgDev + angle2 / 2f), angle2, 5);
    }

    public boolean isInsideILS(float planeX, float planeY) {
        return isInsideArc(planeX, planeY, distance1, angle1) || isInsideArc(planeX, planeY, distance2, angle2);
    }

    private boolean isInsideArc(float planeX, float planeY, float distance, int angle) {
        float deltaX = planeX - x;
        float deltaY = planeY - y;
        double planeHdg = 0;
        if (deltaX == 0) {
            if (deltaY > 0) {
                planeHdg = 180;
            } else if (deltaY < 0) {
                planeHdg = 360;
            }
        } else {
            double principleAngle = Math.atan(deltaY / deltaX) * MathUtils.radiansToDegrees;
            if (deltaX > 0) {
                //Quadrant 1/4
                planeHdg = 270 - principleAngle;
            } else {
                //Quadrant 2/3
                planeHdg = 90 - principleAngle;
            }
        }

        planeHdg += RadarScreen.magHdgDev;

        if (planeHdg <= 0) {
            planeHdg += 360;
        } else if (planeHdg > 360) {
            planeHdg -= 360;
        }

        float smallRange = heading - angle / 2f;
        float bigRange = smallRange + angle;

        boolean inAngle = false;

        if (smallRange <= 0) {
            if (planeHdg >= smallRange + 360 && planeHdg <= 360) {
                inAngle = true;
            } else if (planeHdg > 0 && planeHdg <= bigRange) {
                inAngle = true;
            }
        } else if (bigRange > 360) {
            if (planeHdg <= bigRange - 360 && planeHdg > 0) {
                inAngle = true;
            } else if (planeHdg <= 360 && planeHdg >= smallRange) {
                inAngle = true;
            }
        } else if (planeHdg <= bigRange && planeHdg >= smallRange) {
            inAngle = true;
        }

        boolean inDist;

        float dist = MathTools.distanceBetween(x, y, planeX, planeY);
        inDist = dist <= distance;

        return inAngle && inDist;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
