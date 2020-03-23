package com.bombbird.terminalcontrol.entities.waypoints;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.HashMap;

public class Waypoint extends Actor {
    private String name;
    private int posX;
    private int posY;
    private Label label;
    private boolean selected;
    private boolean flyOver;

    private ShapeRenderer shapeRenderer = TerminalControl.radarScreen.shapeRenderer;

    public static final HashMap<String, Boolean> flyOverPts = new HashMap<>();

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

    /** Called to update whether this waypoint is considered as a fly-over waypoint for any aircraft (fully filled circle) */
    public void updateFlyOverStatus() {
        //Flyover is true if at least one aircraft has it as a direct flyover
        flyOver = false;
        for (Aircraft aircraft: TerminalControl.radarScreen.aircrafts.values()) {
            Waypoint direct = aircraft.getNavState().getClearedDirect().first();
            if (direct != null && direct.getName().equals(name) && aircraft.getRoute().getWptFlyOver(name)) {
                flyOver = true;
                break;
            }
        }
    }

    public boolean isFlyOver() {
        Aircraft selectedAircraft = TerminalControl.radarScreen.getSelectedAircraft();
        //directFlyOver is to show whether a selected aircraft has the waypoint as a flyover one; if selected aircraft does not have it as flyover or has already passed it
        //then it will not be displayed even if other aircraft have it as flyover
        //Will be false if no aircraft selected
        boolean directFlyOver = selectedAircraft != null && selectedAircraft.getRemainingWaypoints().contains(this, true) && selectedAircraft.getRoute().getWptFlyOver(name);
        return directFlyOver || flyOver;
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

    public int getPosY() {
        return posY;
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
