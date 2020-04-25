package com.bombbird.terminalcontrol.entities.waypoints;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.HashMap;

public class Waypoint extends Actor {
    private String name;
    private final int posX;
    private final int posY;
    private boolean restrVisible;
    private final Label restrLabel;
    private final Label label;
    private boolean selected;
    private boolean flyOver;

    private final ShapeRenderer shapeRenderer = TerminalControl.radarScreen.shapeRenderer;

    public static final HashMap<String, Boolean> flyOverPts = new HashMap<>();

    public Waypoint(String name, int posX, int posY) {
        this.name = name;
        this.posX = posX;
        this.posY = posY;
        selected = false;

        //Set the label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont6;
        labelStyle.fontColor = Color.WHITE;
        label = new Label(name, labelStyle);
        label.setPosition(posX - label.getWidth() / 2, posY + 16);
        label.setAlignment(Align.bottom);

        //Set restriction label
        Label.LabelStyle labelStyle1 = new Label.LabelStyle();
        labelStyle1.font = Fonts.defaultFont6;
        labelStyle1.fontColor = Color.GRAY;
        restrLabel = new Label("This should not be visible", labelStyle1);
        restrLabel.setPosition(posX - restrLabel.getWidth() / 2, posY + 48);
        restrLabel.setAlignment(Align.bottom);
        restrVisible = false;

        adjustPositions();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (selected && posX <= 4500 && posX >= 1260 && posY <= 3240 && posY >= 0) {
            label.draw(batch, 1);
            if (restrVisible) restrLabel.draw(batch, 1);
        }
    }

    /** Moves the restriction information labels for certain waypoints to reduce clutter */
    private void adjustPositions() {
        WaypointShifter.loadData();
        if ("ITRF14.1".equals(name)) {
            label.moveBy(-80, -16);
            restrLabel.moveBy(-80, -16);
            return;
        }
        String icao = TerminalControl.radarScreen.mainName;
        if (WaypointShifter.movementData.containsKey(icao) && WaypointShifter.movementData.get(icao).containsKey(name)) {
            int[] shiftData = WaypointShifter.movementData.get(icao).get(name);
            restrLabel.moveBy(shiftData[0], shiftData[1]);
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

    /** Used to set flyOver to true when creating new departures since the Departure would not have been constructed and added to the HashMap of aircrafts */
    public void setDepFlyOver() {
        flyOver = true;
    }

    /** Checks whether the waypoint is marked as a flyover waypoint for correct rendering */
    public boolean isFlyOver() {
        Aircraft selectedAircraft = TerminalControl.radarScreen.getSelectedAircraft();
        if (selectedAircraft != null && selectedAircraft.getRemainingWaypoints().contains(this, true)) {
            //If there is aircraft selected, and remaining waypoints contains this waypoint, return whether this waypoint is flyover
            return selectedAircraft.getRoute().getWptFlyOver(name);
        } else {
            //Otherwise, just use the flyOver waypoint which should be updated earlier
            return flyOver;
        }
    }

    /** Displays the input speed/altitude restrictions above waypoint name */
    public void setRestrDisplay(int maxSpeed, int minAlt, int maxAlt) {
        if (maxSpeed == -1 && minAlt == -1 && maxAlt == -1) {
            restrVisible = false;
            return;
        }
        String restrStr = "";
        if (minAlt == maxAlt && minAlt > -1) {
            restrStr = Integer.toString(minAlt / 100);
        } else {
            if (minAlt > -1) {
                restrStr = "A" + minAlt / 100;
            }
            if (maxAlt > -1) {
                if (restrStr.length() > 0) restrStr += " ";
                restrStr += "B" + maxAlt / 100;
            }
        }
        if (maxSpeed > -1) {
            if (restrStr.length() > 0) {
                restrStr = maxSpeed + "kts\n" + restrStr;
            } else {
                restrStr = maxSpeed + "kts";
            }
        }
        restrLabel.setText(restrStr);
        restrVisible = true;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
