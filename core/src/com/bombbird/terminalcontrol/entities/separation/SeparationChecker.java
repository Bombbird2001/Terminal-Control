package com.bombbird.terminalcontrol.entities.separation;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.entities.approaches.LDA;
import com.bombbird.terminalcontrol.entities.obstacles.Obstacle;
import com.bombbird.terminalcontrol.entities.zones.AltitudeExclusionZone;
import com.bombbird.terminalcontrol.entities.zones.ApproachZone;
import com.bombbird.terminalcontrol.entities.zones.DepartureZone;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class SeparationChecker extends Actor {
    private Array<Array<Aircraft>> flightLevels;
    private Array<Label> labels;

    private RadarScreen radarScreen;
    private int lastNumber;
    private float time;

    public SeparationChecker() {
        radarScreen = TerminalControl.radarScreen;
        lastNumber = 0;
        time = 3;

        flightLevels = new Array<>(true, radarScreen.maxAlt / 1000);
        labels = new Array<>();
        for (int i = 0; i < radarScreen.maxAlt / 1000; i++) {
            flightLevels.add(new Array<>());
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
            aircraft.setTerrainConflict(false);
        }
        for (Label label: labels) {
            label.setText("");
            label.setName("");
        }
        for (Obstacle obstacle : radarScreen.obsArray) {
            obstacle.setConflict(false);
        }
        int active = checkAircraftSep();
        active = checkRestrSep(active);
        int tmpActive = active;
        while (tmpActive > lastNumber) {
            radarScreen.setScore(MathUtils.ceil(radarScreen.getScore() * 0.95f));
            tmpActive--;
        }
        //Subtract wake separately (don't include 5% penalty)
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (aircraft.isWakeInfringe() && aircraft.isArrivalDeparture()) {
                active++;
                aircraft.setConflict(true);
                radarScreen.shapeRenderer.setColor(Color.RED);
                radarScreen.shapeRenderer.circle(aircraft.getRadarX(), aircraft.getRadarY(), 48.6f);
            }
        }
        if (time <= 0) {
            radarScreen.setScore(radarScreen.getScore() - active);
            time += 5;
        }
        lastNumber = active;
        time -= Gdx.graphics.getDeltaTime() * radarScreen.speed;

        if (lastNumber > 0) {
            radarScreen.soundManager.playConflict();
        }
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
    private int checkAircraftSep() {
        int active = 0;
        for (int i = 0; i < flightLevels.size; i++) {
            //Get all the possible planes to check
            Array<Aircraft> planesToCheck = new Array<>();
            if (i - 1 >= 0) {
                planesToCheck.addAll(flightLevels.get(i - 1));
            }
            if (i + 1 < flightLevels.size - 1) {
                planesToCheck.addAll(flightLevels.get(i + 1));
            }
            planesToCheck.addAll(flightLevels.get(i));

            for (int j = 0; j < planesToCheck.size; j++) {
                Aircraft plane1 = planesToCheck.get(j);
                for (int k = j + 1; k < planesToCheck.size; k++) {
                    Aircraft plane2 = planesToCheck.get(k);

                    //Split up exception cases to make it easier to read
                    if (plane1.getEmergency().isActive() || plane2.getEmergency().isActive()) {
                        //If either plane is an emergency
                        continue;
                    }

                    if (plane1.getAltitude() < plane1.getAirport().getElevation() + 1400 || plane2.getAltitude() < plane1.getAirport().getElevation() + 1400 || (plane1.getAltitude() > radarScreen.maxAlt && plane2.getAltitude() > radarScreen.maxAlt)) {
                        //If either plane is below 1400 feet or both above max alt
                        continue;
                    }

                    if (plane1 instanceof Arrival && plane2 instanceof Arrival && plane1.getAirport().getIcao().equals(plane2.getAirport().getIcao()) && plane1.getAltitude() < plane1.getAirport().getElevation() + 6000 && plane2.getAltitude() < plane2.getAirport().getElevation() + 6000) {
                        //If both planes are arrivals into same airport, check whether they are in different NOZ for simultaneous approach
                        Array<ApproachZone> approachZones = plane1.getAirport().getApproachZones();
                        boolean found = false;
                        for (int l = 0; l < approachZones.size; l++) {
                            if (approachZones.get(l).checkSeparation(plane1, plane2)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) continue;
                    }

                    if (plane1 instanceof Departure && plane2 instanceof Departure && plane1.getAirport().getIcao().equals(plane2.getAirport().getIcao())) {
                        //If both planes are departures from same airport, check whether they are in different NOZ for simultaneous departure
                        Array<DepartureZone> departureZones = plane1.getAirport().getDepartureZones();
                        boolean found = false;
                        for (int l = 0; l < departureZones.size; l++) {
                            if (departureZones.get(l).checkSeparation(plane1, plane2)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) continue;
                    }

                    if (plane1.isGoAroundWindow() || plane2.isGoAroundWindow()) {
                        //If either plane went around less than 2 minutes ago
                        continue;
                    }

                    float dist = MathTools.pixelToNm(MathTools.distanceBetween(plane1.getX(), plane1.getY(), plane2.getX(), plane2.getY()));
                    float minima = radarScreen.separationMinima;

                    if (plane1.getIls() != null && plane2.getIls() != null && !plane1.getIls().equals(plane2.getIls()) && plane1.getIls().isInsideILS(plane1.getX(), plane1.getY()) && plane2.getIls().isInsideILS(plane2.getX(), plane2.getY())) {
                        //If both planes are on different ILS and both have captured LOC and are within at least 1 of the 2 arcs, reduce separation to 2nm (staggered separation)
                        minima = 2f;
                    }

                    if (plane1.getIls() != null && plane1.getIls().equals(plane2.getIls())) {
                        Runway runway = plane1.getIls().getRwy();
                        if (MathTools.pixelToNm(MathTools.distanceBetween(plane1.getX(), plane1.getY(), runway.getX(), runway.getY())) < 10 &&
                                MathTools.pixelToNm(MathTools.distanceBetween(plane2.getX(), plane2.getY(), runway.getX(), runway.getY())) < 10) {
                            //If both planes on the same LOC but are both less than 10nm from runway threshold, separation minima is reduced to 2.5nm
                            minima = 2.5f;
                            //TODO If visibility is poor, reduced separation doesn't apply?
                        }
                    }

                    if (Math.abs(plane1.getAltitude() - plane2.getAltitude()) < 975 && dist < minima + 2) {
                        if (Math.abs(plane1.getAltitude() - plane2.getAltitude()) < 900 && dist < minima) {
                            if ((!plane1.isConflict() || !plane2.isConflict())) {
                                //TODO Change separation minima depending on visibility(?)
                                //Aircrafts have infringed minima of 1000 feet and 3nm apart
                                plane1.setConflict(true);
                                plane2.setConflict(true);
                                radarScreen.shapeRenderer.setColor(Color.RED);
                                radarScreen.shapeRenderer.line(plane1.getRadarX(), plane1.getRadarY(), plane2.getRadarX(), plane2.getRadarY());
                                active++;
                            }
                        } else if (!plane1.isWarning() || !plane2.isWarning()) {
                            //Aircrafts within 1000 feet, 5nm of each other
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
        return active;
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
    private int checkRestrSep(int active) {
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (aircraft.isOnGround() || aircraft.isGsCap() || (aircraft instanceof Arrival && aircraft.getIls() instanceof LDA && aircraft.isLocCap()) || (aircraft instanceof Arrival && aircraft.getIls() != null && aircraft.getIls().getName().contains("IMG")) ||
                    (aircraft instanceof Departure && aircraft.getAltitude() <= 4200 + aircraft.getAirport().getElevation()) ||
                    aircraft.isGoAroundWindow()) {
                //Suppress terrain warnings if aircraft is already on the ILS's GS or is on the NPA, or is on the ground, or is on the imaginary ILS for LDA (if has not captured its GS yet), or just did a go around
                continue;
            }

            boolean conflict = false;
            boolean excl = false;

            if (aircraft instanceof Arrival) {
                //Check if aircraft is in altitude exclusion zone
                Array<AltitudeExclusionZone> zones = aircraft.getAirport().getAltitudeExclusionZones();
                for (int i = 0; i < zones.size; i++) {
                    if (zones.get(i).isInside(aircraft)) {
                        excl = true;
                        break;
                    }
                }
            }

            for (Obstacle obstacle : radarScreen.obsArray) {
                //If aircraft is infringing obstacle
                if (obstacle.isIn(aircraft) && aircraft.getAltitude() < obstacle.getMinAlt() - 100) {
                    if (obstacle.isEnforced()) {
                        //Enforced, conflict
                        conflict = true;
                    } else {
                        //Not enforced, conflict only if not excluded and is vectored
                        conflict = !excl && aircraft.isVectored();
                        obstacle.setConflict(conflict);
                    }
                }
            }

            if (conflict && !aircraft.isTerrainConflict()) {
                aircraft.setTerrainConflict(true);
                aircraft.setConflict(true);
                active++;
            }
        }
        return active;
    }

    /** Renders the separation rings if aircraft is in conflict */
    public void renderShape() {
        int radius = (int)(MathTools.nmToPixel(radarScreen.separationMinima) / 2);
        for (Aircraft aircraft: radarScreen.aircrafts.values()) {
            if (aircraft.isConflict() || aircraft.isTerrainConflict()) {
                radarScreen.shapeRenderer.setColor(Color.RED);
                radarScreen.shapeRenderer.circle(aircraft.getRadarX(), aircraft.getRadarY(), radius);
                radarScreen.setPlanesToControl(radarScreen.getPlanesToControl() - Gdx.graphics.getDeltaTime() * 0.025f);
            } else if (aircraft.isWarning()) {
                radarScreen.shapeRenderer.setColor(Color.YELLOW);
                radarScreen.shapeRenderer.circle(aircraft.getRadarX(), aircraft.getRadarY(), radius);
            }
        }
    }

    public int getLastNumber() {
        return lastNumber;
    }

    public void setLastNumber(int lastNumber) {
        this.lastNumber = lastNumber;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }
}