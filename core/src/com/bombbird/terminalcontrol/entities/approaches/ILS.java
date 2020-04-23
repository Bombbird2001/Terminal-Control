package com.bombbird.terminalcontrol.entities.approaches;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.Runway;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.procedures.MissedApproach;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.ui.tabs.LatTab;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

public class ILS extends Actor {
    private final Airport airport;
    private String name;
    private float x;
    private float y;
    private int heading;
    private float gsOffsetNm;
    private int minima;
    private int gsAlt;
    private String[] towerFreq;
    private Runway rwy;
    private final MissedApproach missedApchProc;
    private boolean npa;

    private Array<Vector2> gsRings = new Array<>();
    int minAlt;

    private static final float distance1 = MathTools.nmToPixel(10);
    private static final int angle1 = 6;

    private static final float ilsDistNm = 25;
    private static final float distance2 = MathTools.nmToPixel(ilsDistNm);
    private static final int angle2 = 3;

    private static final float dashDistNm = 1;

    private final RadarScreen radarScreen;

    public ILS(Airport airport, String toParse) {
        radarScreen = TerminalControl.radarScreen;

        this.airport = airport;
        parseInfo(toParse);

        String missed = name;
        if ("IMG".equals(name.substring(0, 3))) missed = "LDA" + name.substring(3);
        if ("TCHX".equals(airport.getIcao()) && "IMG13".equals(name)) missed = "IGS13";
        missedApchProc = airport.getMissedApproaches().get(missed);

        calculateGsRings();
    }

    /** Parses the input string into info for the ILS */
    public void parseInfo(String toParse) {
        int index = 0;
        npa = false;
        for (String s1: toParse.split(",")) {
            switch (index) {
                case 0: name = s1; break;
                case 1: rwy = airport.getRunways().get(s1); break;
                case 2: heading = Integer.parseInt(s1); break;
                case 3: x = Float.parseFloat(s1); break;
                case 4: y = Float.parseFloat(s1); break;
                case 5: gsOffsetNm = Float.parseFloat(s1); break;
                case 6: minima = Integer.parseInt(s1); break;
                case 7: gsAlt = Integer.parseInt(s1); break;
                case 8: towerFreq = s1.split(">"); break;
                default:
                    if (!(this instanceof LDA)) {
                        Gdx.app.log("Load error", "Unexpected additional parameter in game/" + radarScreen.mainName + "/ils" + airport.getIcao() + ".ils");
                    }
            }
            index++;
        }
    }

    /** Calculates positions of the GS rings; overriden for LDAs */
    public void calculateGsRings() {
        minAlt = -1;
        for (int i = 2; i <= gsAlt / 1000; i++) {
            if (i * 1000 > airport.getElevation() + 1000) {
                gsRings.add(new Vector2(x + MathTools.nmToPixel(getDistAtGsAlt(i * 1000)) * (float) Math.cos(Math.toRadians(270 - heading + radarScreen.magHdgDev)), y + MathTools.nmToPixel(getDistAtGsAlt(i * 1000)) * (float) Math.sin(Math.toRadians(270 - heading + radarScreen.magHdgDev))));
                if (minAlt == -1) minAlt = i;
            }
        }
    }

    /** Draws ILS line using shapeRenderer */
    public void renderShape() {
        boolean landing = rwy.isLanding();
        Aircraft aircraft = radarScreen.getSelectedAircraft();
        boolean selectedIls = aircraft instanceof Arrival && aircraft.getAirport().equals(airport) && ((aircraft.getControlState() == Aircraft.ControlState.ARRIVAL && LatTab.clearedILS.equals(name)) || (aircraft.getControlState() == Aircraft.ControlState.UNCONTROLLED && this.equals(aircraft.getIls())));
        boolean rwyChange = radarScreen.getRunwayChanger().containsLandingRunway(airport.getIcao(), rwy.getName());
        if ((landing || selectedIls || rwyChange) && !rwy.isEmergencyClosed()) {
            radarScreen.shapeRenderer.setColor(Color.CYAN);
            if (selectedIls || rwyChange) radarScreen.shapeRenderer.setColor(Color.YELLOW);
            if (radarScreen.showIlsDash) {
                float gsOffsetPx = -MathTools.nmToPixel(gsOffsetNm);
                float trackerX = x + gsOffsetPx * (float) Math.cos(Math.toRadians(270 - heading + radarScreen.magHdgDev));
                float trackerY = y + gsOffsetPx * (float) Math.sin(Math.toRadians(270 - heading + radarScreen.magHdgDev));
                float deltaX = MathTools.nmToPixel(dashDistNm) * (float) Math.cos(Math.toRadians(270 - heading + radarScreen.magHdgDev));
                float deltaY = MathTools.nmToPixel(dashDistNm) * (float) Math.sin(Math.toRadians(270 - heading + radarScreen.magHdgDev));
                boolean drawing = true;
                radarScreen.shapeRenderer.line(x, y, trackerX, trackerY);
                for (int i = 0; i < ilsDistNm + gsOffsetNm; i += dashDistNm) {
                    drawing = !drawing;
                    if (drawing) {
                        radarScreen.shapeRenderer.line(trackerX, trackerY, trackerX + deltaX, trackerY + deltaY);
                    }
                    trackerX += deltaX;
                    trackerY += deltaY;
                }

                //Separation marks at 5, 10 miles
                int halfWidth = 30;
                double trackRad = Math.toRadians(heading - radarScreen.magHdgDev);
                float xOffset = halfWidth * (float) Math.cos(trackRad);
                float yOffset = halfWidth * (float) Math.sin(trackRad);
                Vector2 centre5nm = getPointAtDist(5 - gsOffsetNm);
                Vector2 centre10nm = getPointAtDist(10 - gsOffsetNm);
                TerminalControl.radarScreen.shapeRenderer.line(centre5nm.x - xOffset, centre5nm.y + yOffset, centre5nm.x + xOffset, centre5nm.y - yOffset);
                TerminalControl.radarScreen.shapeRenderer.line(centre10nm.x - xOffset, centre10nm.y + yOffset, centre10nm.x + xOffset, centre10nm.y - yOffset);
            } else {
                radarScreen.shapeRenderer.line(x, y, x + distance2 * (float) Math.cos(Math.toRadians(270 - heading + radarScreen.magHdgDev)), y + distance2 * (float) Math.sin(Math.toRadians(270 - heading + radarScreen.magHdgDev)));
            }
            drawGsCircles();
        }
    }

    public void drawGsCircles() {
        int i = 0;
        for (Vector2 vector2: gsRings) {
            radarScreen.shapeRenderer.setColor(i + minAlt > 3 && !npa ? Color.GREEN : Color.CYAN);
            radarScreen.shapeRenderer.circle(vector2.x, vector2.y, 8);
            i++;
        }
    }

    /** Tests if coordinates input is inside either of the 2 ILS arcs */
    public boolean isInsideILS(float planeX, float planeY) {
        return isInsideArc(planeX, planeY, distance1, angle1) || isInsideArc(planeX, planeY, distance2, angle2);
    }

    /** Tests if coordinates input is inside the arc of the ILS given the arc angle and distance */
    private boolean isInsideArc(float planeX, float planeY, float distance, int angle) {
        float deltaX = planeX - x;
        float deltaY = planeY - y;
        double planeHdg = 0;
        if (deltaX == 0) {
            if (deltaY > 0) {
                planeHdg = 180;
            } else if (deltaY < 0) {
                planeHdg = 360;
            }
        } else {
            double principleAngle = Math.toDegrees(Math.atan(deltaY / deltaX));
            if (deltaX > 0) {
                //Quadrant 1/4
                planeHdg = 270 - principleAngle;
            } else {
                //Quadrant 2/3
                planeHdg = 90 - principleAngle;
            }
        }

        planeHdg += radarScreen.magHdgDev;
        planeHdg = MathTools.modulateHeading(planeHdg);

        float smallRange = heading - angle / 2f;
        float bigRange = smallRange + angle;

        boolean inAngle = false;

        if (smallRange <= 0) {
            if (planeHdg >= smallRange + 360 && planeHdg <= 360) {
                inAngle = true;
            } else if (planeHdg > 0 && planeHdg <= bigRange) {
                inAngle = true;
            }
        } else if (bigRange > 360) {
            if (planeHdg <= bigRange - 360 && planeHdg > 0) {
                inAngle = true;
            } else if (planeHdg <= 360 && planeHdg >= smallRange) {
                inAngle = true;
            }
        } else if (planeHdg <= bigRange && planeHdg >= smallRange) {
            inAngle = true;
        }

        boolean inDist;

        float dist = MathTools.distanceBetween(x, y, planeX, planeY);
        inDist = dist <= distance;

        return inAngle && inDist;
    }

    /** Gets the coordinates of the point on the localiser a distance ahead (behind if -ve) of aircraft */
    public Vector2 getPointAhead(Aircraft aircraft, float distAhead) {
        return getPointAtDist(getDistFrom(aircraft.getX(), aircraft.getY()) - distAhead);
    }

    /** Gets the coordinates of the point on the localiser at a distance away from ILS origin */
    public Vector2 getPointAtDist(float dist) {
        return new Vector2(x + MathTools.nmToPixel(dist) * (float) Math.cos(Math.toRadians(270 - heading + radarScreen.magHdgDev)), y + MathTools.nmToPixel(dist) * (float) Math.sin(Math.toRadians(270 - heading + radarScreen.magHdgDev)));
    }

    /** Gets the glide slope altitude (in feet) at distance away from ILS origin */
    public float getGSAltAtDist(float dist) {
        return MathTools.nmToFeet(dist + gsOffsetNm) * (float) Math.tan(Math.toRadians(3)) + rwy.getElevation();
    }

    /** Gets the glide slope altitude (in feet) of aircraft */
    public float getGSAlt(Aircraft aircraft) {
        return getGSAltAtDist(getDistFrom(aircraft.getX(), aircraft.getY()));
    }

    /** Gets the distance (in nautical miles) from GS origin for a specified altitude */
    public float getDistAtGsAlt(float altitude) {
        return MathTools.feetToNm((altitude - rwy.getElevation()) / (float) Math.tan(Math.toRadians(3))) - gsOffsetNm;
    }

    /** Gets distance (in nautical miles) from ILS origin, of the input coordinates */
    public float getDistFrom(float planeX, float planeY) {
        return MathTools.pixelToNm(MathTools.distanceBetween(x, y, planeX, planeY));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Runway getRwy() {
        return rwy;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public int getHeading() {
        return heading;
    }

    public float getGsOffsetNm() {
        return gsOffsetNm;
    }

    public int getMinima() {
        return minima;
    }

    public int getGsAlt() {
        return gsAlt;
    }

    public MissedApproach getMissedApchProc() {
        return missedApchProc;
    }

    public String[] getTowerFreq() {
        return towerFreq;
    }

    public Airport getAirport() {
        return airport;
    }

    public void setGsRings(Array<Vector2> gsRings) {
        this.gsRings = gsRings;
    }

    public boolean isNpa() {
        return npa;
    }

    public void setNpa(boolean npa) {
        this.npa = npa;
    }
}
