package com.bombbird.atcsim.entities.restrictions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.screens.GameScreen;

public class Obstacle extends Actor {
    private Polygon polygon;
    private int minAlt;
    private Label label;

    public Obstacle(float[] vertices, int minAlt, String text, int textX, int textY) {
        this.minAlt = minAlt;
        polygon = new Polygon(vertices);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont12;
        labelStyle.fontColor = Color.GRAY;
        label = new Label(text, labelStyle);
        label.setPosition(textX, textY);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        label.draw(batch, 1);
    }

    public void renderShape() {
        GameScreen.shapeRenderer.setColor(Color.GRAY);
        GameScreen.shapeRenderer.polygon(polygon.getVertices());
    }

    //TODO: Test for conflict with aircraft
}
