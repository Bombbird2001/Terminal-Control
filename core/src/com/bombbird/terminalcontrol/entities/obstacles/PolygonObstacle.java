package com.bombbird.terminalcontrol.entities.obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.ArrayList;

public class PolygonObstacle extends Obstacle {
    private Polygon polygon;

    public PolygonObstacle(String toParse) {
        super();
        parseInfo(toParse);
    }

    /** Parses the input string into relevant data */
    private void parseInfo(String toParse) {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.GRAY;

        String[] obsInfo = toParse.split(", ");
        int index = 0;
        ArrayList<Float> vertices = new ArrayList<Float>();
        for (String s1: obsInfo) {
            switch (index) {
                case 0: setMinAlt(Integer.parseInt(s1)); break;
                case 1:
                    if (s1.length() > 0 && s1.charAt(s1.length() - 1) == 'C') {
                        setEnforced(true);
                        setLabel(new Label(s1.substring(0, s1.length() - 1), labelStyle));
                    } else {
                        setLabel(new Label(s1, labelStyle));
                    }
                    break;
                case 2: getLabel().setX(Integer.parseInt(s1)); break;
                case 3: getLabel().setY(Integer.parseInt(s1)); break;
                default: vertices.add(Float.parseFloat(s1));
            }
            index++;
        }
        int i = 0;
        float[] verts = new float[vertices.size()];
        for (float f: vertices) {
            verts[i++] = f;
        }
        polygon = new Polygon(verts);
    }

    /** Renders the polygon of obstacle to screen */
    @Override
    public void renderShape() {
        if (isConflict() || getLabel().getText().toString().charAt(0) == '#') {
            TerminalControl.radarScreen.shapeRenderer.setColor(Color.RED);
        } else if (!isEnforced()) {
            TerminalControl.radarScreen.shapeRenderer.setColor(Color.GRAY);
        } else {
            TerminalControl.radarScreen.shapeRenderer.setColor(Color.ORANGE);
        }
        TerminalControl.radarScreen.shapeRenderer.polygon(polygon.getVertices());
    }

    /** Checks whether the input aircraft is inside the polygon area */
    @Override
    public boolean isIn(Aircraft aircraft) {
        return polygon.contains(aircraft.getX(), aircraft.getY());
    }
}
