package com.bombbird.atcsim.utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
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
}
