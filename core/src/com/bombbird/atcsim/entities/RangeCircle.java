package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.atcsim.AtcSim;

public class RangeCircle extends Actor{
    private int range;
    private Label labelUp;
    private Label labelDown;
    private ShapeRenderer shapeRenderer;

    public RangeCircle(AtcSim game, int range, int yOffset, ShapeRenderer shapeRenderer) {
        this.range = range;
        this.shapeRenderer = shapeRenderer;
        int xOffset = -15;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.fonts.defaultFont10;
        labelStyle.fontColor = Color.GRAY;
        labelUp = new Label(Integer.toString(range) + "nm", labelStyle);
        labelUp.setPosition(1440 / 2f + xOffset, 810 / 2f - yOffset - 10);
        labelDown = new Label(Integer.toString(range)+ "nm", labelStyle);
        labelDown.setPosition(1440 / 2f + xOffset, 810 / 2f + yOffset);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        labelUp.draw(batch, 1);
        labelDown.draw(batch, 1);
    }

    public void renderShape() {
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.circle(720, 405, range / 10f * 81);
    }
}
