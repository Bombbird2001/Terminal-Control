package com.bombbird.terminalcontrol.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.restrictions.Obstacle;
import com.bombbird.terminalcontrol.entities.restrictions.RestrictedArea;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.MathTools;

public class SeparationChecker extends Actor {
    private Array<Array<Aircraft>> flightLevels;
    private Array<Label> labels;

    private RadarScreen radarScreen;
    private int lastNumber;

    public SeparationChecker() {
        radarScreen = TerminalControl.radarScreen;
        lastNumber = 0;

        flightLevels = new Array<Array<Aircraft>>(true, radarScreen.maxAlt / 1000);
        labels = new Array<Label>();
        for (int i = 0; i < radarScreen.maxAlt / 1000; i++) {
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

    /** Called to add a new separation label to the array, for when there are less labels than the number of separation incidents between aircrafts */
    private Label newSeparationLabel() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = Color.RED;
        labelStyle.font = Fonts.defaultFont6;
        return new Label("", labelStyle);
    }

    /** Updates the state of aircraft separation */
    public void update() {
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            aircraft.setWarning(false);
            aircraft.setConflict(false);
        }
        for (Label label: labels) {
            label.setText("");
            label.setName("");
        }
        checkAircraftSep();
        checkRestrSep();
        renderShape();
        updateIncidentNumber();
    }

    /** Updates the levels each aircraft belongs to */
    public void updateAircraftPositions() {
        for (Array<Aircraft> array: flightLevels) {
            array.clear();
        }
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (((int)(aircraft.getAltitude() / 1000)) < radarScreen.maxAlt / 1000) {
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

                    //Split up exception cases to make it easier to read
                    if (plane1.getAltitude() < 1400 || plane2.getAltitude() < 1400 || plane1.getAltitude() > 20000 || plane2.getAltitude() > 20000) {
                        //If either plane is below 1400 feet or above 20000 feet
                        continue;
                    }
                    if (plane1.getIls() != null && plane2.getIls() != null && !plane1.getIls().equals(plane2.getIls()) && plane1.getIls().isInsideILS(plane1.getX(), plane1.getY()) && plane2.getIls().isInsideILS(plane2.getX(), plane2.getY())) {
                        //If both planes are on different ILS and both have captured LOC and are within at least 1 of the 2 arcs
                        continue;
                    }
                    if (plane1.isGoAroundWindow() || plane2.isGoAroundWindow()) {
                        //If either plane went around less than 2 minutes ago
                        continue;
                    }

                    float dist = MathTools.pixelToNm(MathTools.distanceBetween(plane1.getX(), plane1.getY(), plane2.getX(), plane2.getY()));
                    float minima = radarScreen.separationMinima;
                    if (plane1.getIls() != null && plane1.getIls().equals(plane2.getIls())) {
                        Runway runway = plane1.getIls().getRwy();
                        if (MathTools.pixelToNm(MathTools.distanceBetween(plane1.getX(), plane1.getY(), runway.getX(), runway.getY())) < 10 &&
                                MathTools.pixelToNm(MathTools.distanceBetween(plane2.getX(), plane2.getY(), runway.getX(), runway.getY())) < 10) {
                            //If both planes on the same LOC but are both less than 10nm from runway threshold, separation minima is reduced to 2.5nm
                            minima = 2.5f;
                            //TODO If visibility is poor, reduced separation doesn't apply
                        }
                    }
                    if (Math.abs(plane1.getAltitude() - plane2.getAltitude()) < 950 && dist < minima + 2) {
                        if (dist < minima) {
                            //TODO Change separation minima depending on visibility, and in future reduced separation for emergencies
                            //Aircrafts have infringed minima of 1000 feet and 3nm apart
                            plane1.setConflict(true);
                            plane2.setConflict(true);
                            radarScreen.shapeRenderer.setColor(Color.RED);
                            radarScreen.shapeRenderer.line(plane1.getRadarX(), plane1.getRadarY(), plane2.getRadarX(), plane2.getRadarY());
                        } else {
                            //Aircrafts within 1000 feet, 4.5nm of each other
                            plane1.setWarning(true);
                            plane2.setWarning(true);
                            radarScreen.shapeRenderer.setColor(Color.YELLOW);
                            radarScreen.shapeRenderer.line(plane1.getRadarX(), plane1.getRadarY(), plane2.getRadarX(), plane2.getRadarY());
                        }
                        boolean found = false;
                        for (Label label : labels) {
                            if ("".equals(label.getName())) {
                                setLabel(label, plane1, plane2);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Label label = newSeparationLabel();
                            setLabel(label, plane1, plane2);
                            labels.add(label);
                        }
                    }
                }
            }
        }
    }

    /** Called to update the label with aircraft data */
    private void setLabel(Label label, Aircraft plane1, Aircraft plane2) {
        float dist = MathTools.pixelToNm(MathTools.distanceBetween(plane1.getRadarX(), plane1.getRadarY(), plane2.getRadarX(), plane2.getRadarY()));
        label.getStyle().fontColor = plane1.isConflict() && plane2.isConflict() ? Color.RED : Color.ORANGE;
        label.setText(Float.toString(Math.round(dist * 100) / 100f));
        label.pack();
        label.setName("Taken");
        label.setVisible(true);
        label.setPosition((plane1.getRadarX() + plane2.getRadarX() - label.getWidth()) / 2, (plane1.getRadarY() + plane2.getRadarY() - label.getHeight()) / 2);
    }

    /** Checks that each aircraft is separated from each obstacles/restricted area */
    private void checkRestrSep() {
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (aircraft.isOnGround() || aircraft.isGsCap() || (aircraft instanceof Arrival && aircraft.getIls() instanceof LDA && aircraft.isLocCap()) ||
                    (aircraft instanceof Departure && aircraft.getAltitude() <= 4000 + aircraft.getAirport().getElevation()) ||
                    aircraft.isGoAroundWindow()) {
                //Suppress terrain warnings if aircraft is already on the ILS's GS or is on the NPA, or is on the ground, or is a departure that is below 4000ft AGL
                continue;
            }
            for (Obstacle obstacle: radarScreen.obsArray) {
                if (aircraft.getAltitude() < obstacle.getMinAlt() - 50 && obstacle.isIn(aircraft)) {
                    aircraft.setConflict(true);
                }
            }
            for (RestrictedArea restrictedArea: radarScreen.restArray) {
                if (aircraft.getAltitude() < restrictedArea.getMinAlt() - 50 && restrictedArea.isIn(aircraft)) {
                    aircraft.setConflict(true);
                }
            }
        }
    }

    /** Renders the separation rings if aircraft is in conflict */
    private void renderShape() {
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (aircraft.isConflict()) {
                radarScreen.shapeRenderer.setColor(Color.RED);
                radarScreen.shapeRenderer.circle(aircraft.getRadarX(), aircraft.getRadarY(), 49);
                radarScreen.setPlanesToControl(radarScreen.getPlanesToControl() - Gdx.graphics.getDeltaTime() * 0.05f);
            } else if (aircraft.isWarning()) {
                radarScreen.shapeRenderer.setColor(Color.YELLOW);
                radarScreen.shapeRenderer.circle(aircraft.getRadarX(), aircraft.getRadarY(), 49);
            }
        }
    }

    /** Updates the number of separation incidents going on */
    private void updateIncidentNumber() {
        int active = 0;
        for (Label label: labels) {
            if ("Taken".equals(label.getName())) {
                active++;
            }
        }

        if (active > lastNumber) {
            for (int i = 0; i < active - lastNumber; i++) {
                radarScreen.setScore(radarScreen.getScore() / 2);
            }
        }
        lastNumber = active;
    }

    public int getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(int lastNumber) {
        this.lastNumber = lastNumber;
    }
}
