package com.bombbird.atcsim.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.atcsim.AtcSim;

import static com.bombbird.atcsim.screens.GameScreen.shapeRenderer;

public class Waypoint extends Actor {
    private String name;
    private int posX;
    private int posY;
    private Label label;
    private boolean selected;

    public Waypoint(String name, int posX, int posY) {
        this.setName(name);
        this.setPosX(posX);
        this.setPosY(posY);
        setSelected(false);

        //Set the label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont6;
        labelStyle.fontColor = Color.GRAY;
        setLabel(new Label(name, labelStyle));
        getLabel().setPosition(posX - getLabel().getWidth() / 2, posY + 16);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isSelected() && getPosX() <= 4500 && getPosX() >= 1260 && getPosY() <= 3240 && getPosY() >= 0) {
            getLabel().draw(batch, 1);
        }
    }

    public void renderShape() {
        if (isSelected() && getPosX() <= 4500 && getPosX() >= 1260 && getPosY() <= 3240 && getPosY() >= 0) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(getPosX(), getPosY(), 12, 10);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
