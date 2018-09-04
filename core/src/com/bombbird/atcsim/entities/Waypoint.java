package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.screens.GameScreen;

import static com.bombbird.atcsim.screens.GameScreen.shapeRenderer;

public class Waypoint extends Actor {
    private String name;
    private int x;
    private int y;
    private Label label;

    public Waypoint(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;

        //Set the label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont6;
        labelStyle.fontColor = Color.GRAY;
        label = new Label(name, labelStyle);
        label.setPosition(x + 5, y - 3);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        label.draw(batch, 1);
    }

    public void renderShape() {
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.circle(x, y, 3, 10);
    }
}
