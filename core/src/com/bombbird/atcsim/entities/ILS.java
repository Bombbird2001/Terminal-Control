package com.bombbird.atcsim.entities;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.bombbird.atcsim.utilities.MathTools;

public class ILS extends Actor {
    private float x;
    private float y;
    private Runway runway;
    private float distance1;
    private float distance2;

    public ILS(float x, float y, Runway runway) {
        this.x = x;
        this.y = y;
        this.runway = runway;
        distance1 = MathTools.nmToPixel(17) - runway.getPxLength();
        distance2 = MathTools.nmToPixel(25) - runway.getPxLength();
    }
}
