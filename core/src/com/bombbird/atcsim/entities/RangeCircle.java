package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.atcsim.AtcSim;

import static com.bombbird.atcsim.screens.GameScreen.shapeRenderer;

public class RangeCircle extends Actor{
    private int range;
    private Label labelUp;
    private Label labelDown;

    public RangeCircle(int range, int yOffset) {
        this.range = range;
        int xOffset = -60;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont10;
        labelStyle.fontColor = Color.GRAY;
        labelUp = new Label(Integer.toString(range) + "nm", labelStyle);
        labelUp.setPosition(5760 / 2f + xOffset, 3240 / 2f - yOffset + 5);
        labelDown = new Label(Integer.toString(range)+ "nm", labelStyle);
        labelDown.setPosition(5760 / 2f + xOffset, 3140 / 2f + yOffset);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        labelUp.draw(batch, 1);
        labelDown.draw(batch, 1);
    }

    public void renderShape() {
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.circle(2880, 1620, range / 10f * 324, 60);
    }
}
