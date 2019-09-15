package com.bombbird.terminalcontrol.entities.obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;

public class Obstacle extends Actor {
    private int minAlt;
    private Label label;
    private boolean conflict;
    private boolean enforced;
    public RadarScreen radarScreen;
    public ShapeRenderer shapeRenderer;

    public Obstacle() {
        radarScreen = TerminalControl.radarScreen;
        shapeRenderer = radarScreen.shapeRenderer;
    }

    /** Draws the label to screen */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (conflict || label.getText().toString().charAt(0) == '#') {
            label.getStyle().fontColor = Color.RED;
        } else if (!enforced) {
            label.getStyle().fontColor = Color.GRAY;
        } else {
            label.getStyle().fontColor = Color.ORANGE;
        }
        label.draw(batch, 1);
    }

    /** Renders the obstacle with shape renderer, overridden */
    public void renderShape() {
        //No default implementation
    }

    /** Checks if input aircraft is in the obstacle */
    public boolean isIn(Aircraft aircraft) {
        //No default implementation
        return false;
    }

    public int getMinAlt() {
        return minAlt;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public Label getLabel() {
        return label;
    }

    public boolean isEnforced() {
        return enforced;
    }

    public void setMinAlt(int minAlt) {
        this.minAlt = minAlt;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

    public void setEnforced(boolean enforced) {
        this.enforced = enforced;
    }
}
