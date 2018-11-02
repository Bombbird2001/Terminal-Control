package com.bombbird.terminalcontrol.entities.aircrafts.approaches;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class LDA extends ILS {
    private Array<Integer[]> nonPrecAlts;
    private Vector2 gsRing;

    public LDA(String name, float x, float y, int heading, float gsOffset, int minima, int gsAlt, Array<Integer[]> nonPrecAlts, Runway rwy) {
        super(name, x, y, heading, gsOffset, minima, gsAlt, rwy);
        this.nonPrecAlts = nonPrecAlts;
    }

    @Override
    public void calculateGsRings() {
        gsRing = new Vector2(getX() + MathTools.nmToPixel(getDistAtGsAlt(getGsAlt())) * MathUtils.cosDeg(270 - getHeading() + RadarScreen.magHdgDev), getY() + MathTools.nmToPixel(getDistAtGsAlt(getGsAlt())) * MathUtils.sinDeg(270 - getHeading() + RadarScreen.magHdgDev));
    }

    @Override
    public void drawGsCircles() {
        GameScreen.shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        GameScreen.shapeRenderer.circle(gsRing.x, gsRing.y, 8);
        GameScreen.shapeRenderer.set(ShapeRenderer.ShapeType.Line);
    }


    public Array<Integer[]> getNonPrecAlts() {
        return nonPrecAlts;
    }
}
