package com.bombbird.atcsim.entities.restrictions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.bombbird.atcsim.AtcSim;

public class Obstacle extends Actor implements Disposable {
    private final AtcSim game;
    private Polygon polygon;
    private int minAlt;
    private ShapeRenderer shapeRenderer;
    private boolean matrixSet;
    private Label label;
    private String text;
    private int textX;
    private int textY;

    public Obstacle(AtcSim game, float[] vertices, int minAlt, String text, int textX, int textY) {
        this.game = game;
        this.minAlt = minAlt;
        polygon = new Polygon(vertices);
        shapeRenderer = new ShapeRenderer();
        matrixSet = false;
        this.text = text;
        this.textX = textX;
        this.textY = textY;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.fonts.defaultFont12;
        labelStyle.fontColor = Color.GRAY;
        label = new Label(text, labelStyle);
        label.setPosition(textX, textY);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!matrixSet) {
            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
            matrixSet = true;
        }
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.polygon(polygon.getVertices());
        shapeRenderer.end();
        batch.begin();
        label.draw(batch, 1);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
