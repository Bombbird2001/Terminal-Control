package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class RangeCircle extends Actor {
    private final int range;
    private final Label labelUp;
    private final Label labelDown;

    private final ShapeRenderer shapeRenderer = TerminalControl.radarScreen.shapeRenderer;
    private static final Color DARK_GREEN = new Color(0x005720ff);

    public RangeCircle(int range) {
        this.range = range;
        int yOffset = (int) MathTools.nmToPixel(range);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont6;
        labelStyle.fontColor = DARK_GREEN;
        labelUp = new Label(range + "nm", labelStyle);
        int xOffset = (int) (labelUp.getWidth() / 2);
        labelUp.setPosition(5760 / 2f - xOffset, 3240 / 2f + yOffset);
        labelDown = new Label(range + "nm", labelStyle);
        labelDown.setPosition(5760 / 2f - xOffset, 3240 / 2f - yOffset - 30);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        labelUp.draw(batch, 1);
        labelDown.draw(batch, 1);
    }

    public void renderShape() {
        shapeRenderer.setColor(DARK_GREEN);
        shapeRenderer.circle(2880, 1620, range / 10f * 324, 60);
    }
}
