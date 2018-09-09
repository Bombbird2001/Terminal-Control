package com.bombbird.atcsim.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.screens.GameScreen;

import static com.bombbird.atcsim.screens.GameScreen.stage;
import static com.bombbird.atcsim.screens.RadarScreen.mainName;

import java.util.Enumeration;
import java.util.Hashtable;

public class Airport {
    private Hashtable<String, Runway> runways;
    private Hashtable<String, Runway> landingRunways;
    private Hashtable<String, Runway> takeoffRunways;
    private String icao;
    private String metar;
    private Hashtable<String, Star> stars;
    private Hashtable<String, Star> sids;

    public Airport(String icao) {
        this.icao = icao;
        loadRunways();
        loadStars();
    }

    private void loadRunways() {
        runways = new Hashtable<String, Runway>();
        FileHandle handle = Gdx.files.internal("game/" + mainName + "/runway" + icao + ".rwy");
        String[] indivRwys = handle.readString().split("\\r?\\n");
        for (String s: indivRwys) {
            //For each individual runway
            String rwyInfo[] = s.split(" ");
            int index = 0;
            String name = "";
            float x = 0;
            float y = 0;
            int length = 0;
            int heading = 0;
            float textX = 0;
            float textY = 0;
            int elevation = 0;
            for (String s1: rwyInfo) {
                switch (index) {
                    case 0: name = s1; break;
                    case 1: x = Float.parseFloat(s1); break;
                    case 2: y = Float.parseFloat(s1); break;
                    case 3: length = Integer.parseInt(s1); break;
                    case 4: heading = Integer.parseInt(s1); break;
                    case 5: textX = Float.parseFloat(s1); break;
                    case 6: textY = Float.parseFloat(s1); break;
                    case 7: elevation = Integer.parseInt(s1); break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + mainName + "/runway" + icao + ".rest");
                }
                index++;
            }
            Runway runway = new Runway(name, x, y, length, heading, textX, textY, elevation);
            runways.put(name, runway);
        }
        landingRunways = new Hashtable<String, Runway>();
        takeoffRunways = new Hashtable<String, Runway>();
        if (icao.equals("RCTP")) {
            setActive("05L", true, false);
            setActive("05R", false, true);
        }
        /*
        setActive("23R", true, false);
        setActive("23L", false, true);
        */

    }

    private void setActive(String rwy, boolean landing, boolean takeoff) {
        //Retrieves runway from hashtable
        Runway runway = runways.get(rwy);

        //Set actor
        if ((landing || takeoff) && !runway.isActive()) {
            //Add if active now but not before
            stage.addActor(runway);
        } else if (!landing && !takeoff && runway.isActive()) {
            //Remove if not active but active before
            stage.getActors().removeValue(runway, true);
        }

        if (!runway.isLanding() && landing) {
            //Add to landing runways if not landing before, but landing now
            landingRunways.put(rwy, runway);
        } else if (runway.isLanding() && !landing) {
            //Remove if landing before, but not landing now
            landingRunways.remove(rwy);
        }
        if (!runway.isTakeoff() && takeoff) {
            //Add to takeoff runways if not taking off before, but taking off now
            takeoffRunways.put(rwy, runway);
        } else if (runway.isTakeoff() && !takeoff) {
            //Remove if taking off before, but not taking off now
            takeoffRunways.remove(rwy);
        }

        //Set runway's internal active state
        runway.setActive(landing, takeoff);
    }

    public void renderRunways() {
        Enumeration<String> enumKeys = runways.keys();
        while (enumKeys.hasMoreElements()) {
            String key = enumKeys.nextElement();
            Runway runway = runways.get(key);
            if (runway.isActive()) {
                runway.renderShape();
            }
        }
    }

    private void loadStars() {
        //Load STARs
        stars = new Hashtable<String, Star>();
        FileHandle handle = Gdx.files.internal("game/" + mainName + "/star" + icao + ".star");
        String[] indivStars = handle.readString().split("\\r?\\n");
        for (String s: indivStars) {
            //For each individual STAR
            String starInfo[] = s.split(",");

            //Info for the STAR
            String name = "";
            Array<Integer> inboundHdg = new Array<Integer>();
            Array<Waypoint> waypoints = new Array<Waypoint>();
            Array<int[]> restrictions = new Array<int[]>();
            Array<Waypoint> holdingPoints = new Array<Waypoint>();
            Array<int[]> holdingInfo = new Array<int[]>();
            Array<String> runways = new Array<String>();

            int index = 0;
            for (String s1: starInfo) {
                switch (index) {
                    case 0: name = s1; break; //First part is the name of the STAR
                    case 1: //Add STAR runways
                        for (String s2: s1.split(">")) {
                            runways.add(s2);
                        }
                        break;
                    case 2: //Second part is the inbound track(s) of the STAR
                        for (String s2: s1.split(">")) {
                            inboundHdg.add(Integer.parseInt(s2));
                        }
                        break;
                    case 3: //Add waypoints to STAR
                        for (String s2: s1.split(">")) {
                            //For each waypoint on the STAR
                            int index1 = 0;
                            int[] altSpdRestrictions = new int[3];
                            for (String s3: s2.split(" ")) {
                                switch (index1) {
                                    case 0:
                                        waypoints.add(GameScreen.waypoints.get(s3));
                                        break; //First part is the name of the waypoint
                                    case 1:
                                        altSpdRestrictions[0] = Integer.parseInt(s3);
                                        break; //1 is min alt
                                    case 2:
                                        altSpdRestrictions[1] = Integer.parseInt(s3);
                                        break; //2 is max alt
                                    case 3:
                                        altSpdRestrictions[2] = Integer.parseInt(s3);
                                        break; //3 is max speed
                                    default:
                                        Gdx.app.log("Load error", "Unexpected additional waypoint parameter in game/" + mainName + "/star" + icao + ".rest");
                                }
                                index1++;
                            }
                            restrictions.add(altSpdRestrictions);
                        }
                    case 4: //Add holding points to STAR
                        for (String s2: s1.split(">")) {
                            //For each holding point in STAR
                            int index1 = 0;
                            int[] info = new int[4];
                            for (String s3: s2.split(" ")) {
                                switch (index1) {
                                    case 0:
                                        holdingPoints.add(GameScreen.waypoints.get(s3)); //Get waypoint
                                        break;
                                    case 1:
                                        info[0] = Integer.parseInt(s3); //Min alt for holding
                                        break;
                                    case 2:
                                        info[1] = Integer.parseInt(s3); //Max alt for holding
                                        break;
                                    case 3:
                                        info[2] = Integer.parseInt(s3); //Holding pattern heading
                                        break;
                                    case 4:
                                        info[3] = Integer.parseInt(s3); //Leg distance
                                        break;
                                    default:
                                        Gdx.app.log("Load error", "Unexpected additional holding point parameter in game/" + mainName + "/star" + icao + ".rest");
                                }
                                index1++;
                            }
                            holdingInfo.add(info);
                        }
                }
                index++;
            }
            stars.put(name, new Star(name, runways, inboundHdg, waypoints, restrictions, holdingPoints, holdingInfo));
        }
    }

    public void loadSids() {
        //TODO: Load SIDs
    }

    public Hashtable<String, Star> getStars() {
        return stars;
    }

    public Hashtable<String, Runway> getLandingRunways() {
        return landingRunways;
    }

    public Hashtable<String, Runway> getTakeoffRunways() {
        return takeoffRunways;
    }
}
