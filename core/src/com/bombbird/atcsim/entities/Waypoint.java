package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.atcsim.AtcSim;

import static com.bombbird.atcsim.screens.GameScreen.shapeRenderer;

public class Waypoint extends Actor {
    public String name;
    public int x;
    public int y;
    private Label label;
    private boolean selected;

    public Waypoint(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        selected = false;

        //Set the label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont6;
        labelStyle.fontColor = Color.GRAY;
        label = new Label(name, labelStyle);
        label.setPosition(x - label.getWidth() / 2, y + 16);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (selected && x <= 4500 && x >= 1260 && y <= 3240 && y >= 0) {
            label.draw(batch, 1);
        }
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public void renderShape() {
        if (selected && x <= 4500 && x >= 1260 && y <= 3240 && y >= 0) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(x, y, 12, 10);
        }
    }
}
