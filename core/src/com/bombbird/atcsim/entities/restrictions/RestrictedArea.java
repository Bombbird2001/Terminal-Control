package com.bombbird.atcsim.entities.restrictions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.bombbird.atcsim.AtcSim;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

import static com.bombbird.atcsim.screens.GameScreen.shapeRenderer;

public class RestrictedArea extends Actor {
    private Circle circle;
    private int minAlt;
    private Label label;

    public RestrictedArea(float centreX, float centreY, float radius, int minAlt, String text, int textX, int textY) {
        this.setPosition(centreX - radius, centreY - radius);
        this.setSize(radius * 2, radius * 2);
        this.minAlt = minAlt;
        circle = new Circle(centreX, centreY, radius);
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
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(circle.x, circle.y, circle.radius);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.circle(circle.x, circle.y, circle.radius);
    }

    //TODO: Test for conflict with aircraft
}
