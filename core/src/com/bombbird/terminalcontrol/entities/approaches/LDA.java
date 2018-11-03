package com.bombbird.terminalcontrol.entities.approaches;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.entities.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class LDA extends ILS {
    private Queue<int[]> nonPrecAlts;
    private Vector2 gsRing;
    private float lineUpDist;

    public LDA(String name, Airport airport, float x, float y, int heading, float gsOffset, int minima, int gsAlt, Queue<int[]> nonPrecAlts, Runway rwy, float lineUpDist) {
        super(name, airport, x, y, heading, gsOffset, minima, gsAlt, rwy);
        this.nonPrecAlts = nonPrecAlts;
        this.lineUpDist = lineUpDist;

        calculateFAFRing();
    }

    /** Calculates position of FAF on LOC course */
    private void calculateFAFRing() {
        gsRing = new Vector2(getX() + MathTools.nmToPixel(nonPrecAlts.last()[1]) * MathUtils.cosDeg(270 - getHeading() + RadarScreen.magHdgDev), getY() + MathTools.nmToPixel(nonPrecAlts.last()[1]) * MathUtils.sinDeg(270 - getHeading() + RadarScreen.magHdgDev));

    }

    /** Overrides method in ILS to ignore it */
    @Override
    public void calculateGsRings() {
    }

    /** Overrides method in ILS to draw FAF point on LOC course */

    @Override
    public void drawGsCircles() {
        GameScreen.shapeRenderer.circle(gsRing.x, gsRing.y, 8);
    }


    public Queue<int[]> getNonPrecAlts() {
        return nonPrecAlts;
    }

    public float getLineUpDist() {
        return lineUpDist;
    }
}
