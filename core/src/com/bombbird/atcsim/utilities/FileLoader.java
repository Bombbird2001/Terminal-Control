package com.bombbird.atcsim.utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.bombbird.atcsim.entities.Runway;
import com.bombbird.atcsim.entities.sidstar.Sid;
import com.bombbird.atcsim.entities.sidstar.Star;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.restrictions.Obstacle;
import com.bombbird.atcsim.entities.restrictions.RestrictedArea;
import com.bombbird.atcsim.screens.GameScreen;
import com.bombbird.atcsim.screens.RadarScreen;

import java.util.ArrayList;
import java.util.HashMap;

public class FileLoader {
    public static Array<Obstacle> loadObstacles() {
        FileHandle obstacles = Gdx.files.internal("game/" + RadarScreen.mainName + "/obstacle.obs");
        Array<Obstacle> obsArray = new Array<Obstacle>();
        String[] indivObs = obstacles.readString().split("\\r?\\n");
        for (String s: indivObs) {
            //For each individual obstacle:
            String[] obsInfo = s.split(", ");
            int index = 0;
            int minAlt = 0;
            String text = "";
            int textX = 0;
            int textY = 0;
            ArrayList<Float> vertices = new ArrayList<Float>();
            for (String s1: obsInfo) {
                switch (index) {
                    case 0: minAlt = Integer.parseInt(s1); break;
                    case 1: text = s1; break;
                    case 2: textX = Integer.parseInt(s1); break;
                    case 3: textY = Integer.parseInt(s1); break;
                    default: vertices.add(Float.parseFloat(s1));
                }
                index++;
            }
            int i = 0;
            float[] verts = new float[vertices.size()];
            for (float f: vertices) {
                verts[i++] = f;
            }
            Obstacle obs = new Obstacle(verts, minAlt, text, textX, textY);
            obsArray.add(obs);
            GameScreen.stage.addActor(obs);
        }
        return obsArray;
    }

    public static Array<RestrictedArea> loadRestricted() {
        FileHandle restrictions = Gdx.files.internal("game/" + RadarScreen.mainName + "/restricted.rest");
        Array<RestrictedArea> restArray = new Array<RestrictedArea>();
        String[] indivRests = restrictions.readString().split("\\r?\\n");
        for (String s: indivRests) {
            //For each individual restricted area
            String[] restInfo = s.split(", ");
            int index = 0;
            int minAlt = 0;
            String text = "";
            int textX = 0;
            int textY = 0;
            float centreX = 0;
            float centreY = 0;
            float radius = 0;
            for (String s1: restInfo) {
                switch (index) {
                    case 0: minAlt = Integer.parseInt(s1); break;
                    case 1: text = s1; break;
                    case 2: textX = Integer.parseInt(s1); break;
                    case 3: textY = Integer.parseInt(s1); break;
                    case 4: centreX = Float.parseFloat(s1); break;
                    case 5: centreY = Float.parseFloat(s1); break;
                    case 6: radius = Float.parseFloat(s1); break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + RadarScreen.mainName + "/restricted.rest");
                }
                index++;
            }
            RestrictedArea area = new RestrictedArea(centreX, centreY, radius, minAlt, text, textX, textY);
            restArray.add(area);
            GameScreen.stage.addActor(area);
        }
        return restArray;
    }

    public static HashMap<String, Waypoint> loadWaypoints() {
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/waypoint.way");
        String wayptStr = handle.readString();
        String[] indivWpt = wayptStr.split("\\r?\\n");
        HashMap <String, Waypoint> waypoints = new HashMap<String, Waypoint>(indivWpt.length + 1, 0.999f);
        for (String s: indivWpt) {
            //For each waypoint
            int index = 0;
            String name = "";
            int x = 0;
            int y = 0;
            for (String s1: s.split(" ")) {
                switch (index) {
                    case 0: name = s1; break;
                    case 1: x = Integer.parseInt(s1); break;
                    case 2: y = Integer.parseInt(s1); break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + RadarScreen.mainName + "/restricted.rest");
                }
                index++;
            }
            Waypoint waypoint = new Waypoint(name, x, y);
            waypoints.put(name, waypoint);
            GameScreen.stage.addActor(waypoint);
        }
        return waypoints;
    }

    public static HashMap<String, Runway> loadRunways(String icao) {
        HashMap<String, Runway> runways = new HashMap<String, Runway>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/runway" + icao + ".rwy");
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
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + RadarScreen.mainName + "/runway" + icao + ".rest");
                }
                index++;
            }
            Runway runway = new Runway(name, x, y, length, heading, textX, textY, elevation);
            runways.put(name, runway);
        }
        return runways;
    }

    public static HashMap<String, Star> loadStars(String icao) {
        //Load STARs
        HashMap<String, Star> stars = new HashMap<String, Star>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/star" + icao + ".star");
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
                                        Gdx.app.log("Load error", "Unexpected additional waypoint parameter in game/" + RadarScreen.mainName + "/star" + icao + ".star");
                                }
                                index1++;
                            }
                            restrictions.add(altSpdRestrictions);
                        }
                        break;
                    case 4: //Add holding points to STAR
                        for (String s2: s1.split(">")) {
                            //For each holding point in STAR
                            int index1 = 0;
                            int[] info = new int[5];
                            for (String s3: s2.split(" ")) {
                                if (index1 == 0) {
                                    holdingPoints.add(GameScreen.waypoints.get(s3)); // Get waypoint
                                } else if (index1 > 0 && index1 < 6) {
                                    info[index1 - 1] = Integer.parseInt(s3);
                                } else {
                                    Gdx.app.log("Load error", "Unexpected additional holding point parameter in game/" + RadarScreen.mainName + "/star" + icao + ".star");
                                }
                                index1++;
                            }
                            holdingInfo.add(info);
                        }
                        break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + RadarScreen.mainName + "/star" + icao + ".star");
                }
                index++;
            }
            stars.put(name, new Star(name, runways, inboundHdg, waypoints, restrictions, holdingPoints, holdingInfo));
        }
        return stars;
    }

    public static HashMap<String, Sid> loadSids(String icao) {
        //Load SIDs
        HashMap<String, Sid> sids = new HashMap<String, Sid>();
        FileHandle handle = Gdx.files.internal("game/" + RadarScreen.mainName + "/sid" + icao + ".sid");
        String[] indivStars = handle.readString().split("\\r?\\n");
        for (String s: indivStars) {
            //For each individual SID
            String[] sidInfo = s.split(",");

            //Info for the SID
            String name = "";
            Array<String> runways = new Array<String>();
            int[] initClimb = new int[3];
            Array<Integer> outboundHdg = new Array<Integer>();
            Array<Waypoint> waypoints = new Array<Waypoint>();
            Array<int[]> restrictions = new Array<int[]>();

            int index = 0;
            for (String s1: sidInfo) {
                switch (index) {
                    case 0: name = s1; break; //First part is the name of the SID
                    case 1: //Add SID runways
                        for (String s2: s1.split(">")) {
                            runways.add(s2);
                        }
                        break;
                    case 2: //Second part is the initial climb of the SID
                        String[] info = s1.split(" ");
                        for (int i = 0; i < 3; i++) {
                            initClimb[i] = Integer.parseInt(info[i]);
                        }
                        break;
                    case 3: //Add waypoints to SID
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
                                        Gdx.app.log("Load error", "Unexpected additional waypoint parameter in game/" + RadarScreen.mainName + "/sid" + icao + ".sid");
                                }
                                index1++;
                            }
                            restrictions.add(altSpdRestrictions);
                        }
                        break;
                    case 4: //Outbound heading after last waypoint
                        for (String s2: s1.split(">")) {
                            outboundHdg.add(Integer.parseInt(s2));
                        }
                        break;
                    default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + RadarScreen.mainName + "/sid" + icao + ".sid");
                }
                index++;
            }
            sids.put(name, new Sid(name, runways, initClimb, outboundHdg, waypoints, restrictions));
        }
        return sids;
    }

    public static HashMap<String, int[]> loadAircraftData() {
        HashMap<String, int[]> aircrafts = new HashMap<String, int[]>();
        FileHandle handle = Gdx.files.internal("game/aircrafts/aircrafts.air");
        String[] indivAircrafts = handle.readString().split("\\r?\\n");
        for (String s: indivAircrafts) {
            if (s.charAt(0) == '#') {
                continue;
            }
            //For each aircraft
            int index = 0;
            String icao = "";
            int[] perfData = new int[5];
            for (String s1: s.split(",")) {
                if (index == 0) {
                    icao = s1; //Icao code of aircraft
                } else if (index >= 1 && index <= 5) {
                    perfData[index - 1] = Integer.parseInt(s1);
                } else {
                    Gdx.app.log("Load error", "Unexpected additional parameter in game/aircrafts.air");
                }
                index++;
            }
            aircrafts.put(icao, perfData);
        }
        return aircrafts;
    }
}
