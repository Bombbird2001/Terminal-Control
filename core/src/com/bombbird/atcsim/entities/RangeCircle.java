package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.bombbird.atcsim.AtcSim;

public class RangeCircle extends Actor implements Disposable {
    private final AtcSim game;
    private int range;
    private ShapeRenderer shapeRenderer;
    private boolean matrixSet;
    private int yOffset;
    private int xOffset;

    public RangeCircle(AtcSim game, int range, int yOffset) {
        this.game = game;
        this.range = range;
        this.yOffset = yOffset;
        shapeRenderer = new ShapeRenderer();
        matrixSet = false;
        xOffset = -10;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!matrixSet) {
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            matrixSet = true;
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.circle(720, 405, range / 10f * 81);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
