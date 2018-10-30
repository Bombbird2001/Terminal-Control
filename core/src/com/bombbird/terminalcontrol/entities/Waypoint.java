package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.utilities.Fonts;

import static com.bombbird.terminalcontrol.screens.GameScreen.shapeRenderer;

public class Waypoint extends Actor {
    private String name;
    private int posX;
    private int posY;
    private Label label;
    private boolean selected;

    public Waypoint(String name, int posX, int posY) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        selected = false;

        //Set the label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont6;
        labelStyle.fontColor = Color.GRAY;
        setLabel(new Label(name, labelStyle));
        getLabel().setPosition(posX - getLabel().getWidth() / 2, posY + 16);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (selected && posX <= 4500 && posX >= 1260 && posY <= 3240 && posY >= 0) {
            label.draw(batch, 1);
        }
    }

    public void renderShape() {
        if (selected && posX <= 4500 && posX >= 1260 && posY <= 3240 && posY >= 0) {
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
