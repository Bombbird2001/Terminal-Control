package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class SeparationChecker extends Actor {
    private Array<Array<Aircraft>> flightLevels;
    private Array<Label> labels;

    public SeparationChecker() {
        flightLevels = new Array<Array<Aircraft>>(true, RadarScreen.MAX_ALT / 1000);
        labels = new Array<Label>();
        for (int i = 0; i < RadarScreen.MAX_ALT / 1000; i++) {
            flightLevels.add(new Array<Aircraft>());
        }
    }

    /** Draws the labels, if any */
    @Override
    public void draw(Batch batch, float alpha) {
        for (Label label: labels) {
            label.draw(batch, 1);
        }
    }

    /** Called to add a new label to the array, for when there are less labels than the number of separation incidents between aircrafts */
    private Label newLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = Color.RED;
        labelStyle.font = Fonts.defaultFont12;
        return new Label("", labelStyle);
    }

    /** Updates the state of aircraft separation */
    public void update() {
        for (Aircraft aircraft: RadarScreen.AIRCRAFTS.values()) {
            aircraft.setConflict(false);
        }
        for (Label label: labels) {
            label.setVisible(false);
            label.setName("");
        }
        checkAircraftSep();
        checkRestrSep();
    }

    /** Updates the levels each aircraft belongs to */
    public void updateAircraftPositions() {
        for (Array<Aircraft> array: flightLevels) {
            array.clear();
        }
        for (Aircraft aircraft: RadarScreen.AIRCRAFTS.values()) {
            if (((int)(aircraft.getAltitude() / 1000)) < RadarScreen.MAX_ALT / 1000) {
                flightLevels.get((int)(aircraft.getAltitude() / 1000)).add(aircraft);
            }
        }
    }

    /** Checks that each aircraft is separated from one another */
    private void checkAircraftSep() {
        for (int i = 0; i < flightLevels.size; i++) {
            //Get all the possible planes to check
            Array<Aircraft> planesToCheck = new Array<Aircraft>();
            if (i - 1 >= 0) {
                planesToCheck.addAll(flightLevels.get(i - 1));
            }
            if (i + 1 < flightLevels.size - 1) {
                planesToCheck.addAll(flightLevels.get(i + 1));
            }
            planesToCheck.addAll(flightLevels.get(i));

            for (int j = 0; j < planesToCheck.size; j++) {
                for (int k = j + 1; k < planesToCheck.size; k++) {
                    Aircraft plane1 = planesToCheck.get(j);
                    Aircraft plane2 = planesToCheck.get(k);
                    float dist = MathTools.pixelToNm(MathTools.distanceBetween(plane1.getX(), plane1.getY(), plane2.getX(), plane2.getY()));
                    if (Math.abs(plane1.getAltitude() - plane2.getAltitude()) < 950 && dist < RadarScreen.SEPARATION_MINIMA) {
                        //TODO Change separation minima depending on visibility, and in future reduced separation for emergencies
                        //Aircrafts have infringed minima of 1000 feet and 3nm apart
                        plane1.setConflict(true);
                        plane2.setConflict(true);
                        GameScreen.SHAPE_RENDERER.setColor(Color.RED);
                        GameScreen.SHAPE_RENDERER.line(plane1.getX(), plane1.getY(), plane2.getX(), plane2.getY());
                        boolean found = false;
                        for (Label label: labels) {
                            if (label.getName().equals(plane1.getCallsign() + plane2.getCallsign()) || "".equals(label.getName())) {
                                setLabel(label, plane1, plane2, dist);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Label label = newLabel();
                            setLabel(label, plane1, plane2, dist);
                            labels.add(label);
                        }
                    }
                }
            }
        }
    }

    /** Called to update the label with aircraft data */
    private void setLabel(Label label, Aircraft plane1, Aircraft plane2, float dist) {
        label.setText(Float.toString(Math.round(dist * 100) / 100f));
        label.setName(plane1.getCallsign() + plane2.getCallsign());
        label.setVisible(true);
        label.setPosition((plane1.getRadarX() + plane2.getRadarX() - label.getWidth()) / 2, (plane1.getRadarY() + plane2.getRadarY() - label.getHeight()) / 2);
    }

    /** Checks that each aircraft is separated from each obstacles/restricted area */
    private void checkRestrSep() {
        for (Aircraft aircraft: RadarScreen.AIRCRAFTS.values()) {
            for (Obstacle obstacle: RadarScreen.OBS_ARRAY) {
                if (aircraft.getAltitude() < obstacle.getMinAlt() - 50 && obstacle.isIn(aircraft)) {
                    aircraft.setConflict(true);
                }
            }
            for (RestrictedArea restrictedArea: RadarScreen.REST_ARRAY) {
                if (aircraft.getAltitude() < restrictedArea.getMinAlt() - 50 && restrictedArea.isIn(aircraft)) {
                    aircraft.setConflict(true);
                }
            }
            if (aircraft.isConflict()) {
                RadarScreen.SHAPE_RENDERER.setColor(Color.RED);
                RadarScreen.SHAPE_RENDERER.circle(aircraft.getRadarX(), aircraft.getRadarY(), 49);
            }
        }
    }
}
