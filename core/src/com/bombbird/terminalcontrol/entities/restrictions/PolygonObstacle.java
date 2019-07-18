package com.bombbird.terminalcontrol.entities.restrictions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.ArrayList;

public class PolygonObstacle extends Actor {
    private Polygon polygon;
    private int minAlt;
    private Label label;
    private boolean conflict;
    private boolean enforced;

    public PolygonObstacle(String toParse) {
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
                case 0: minAlt = Integer.parseInt(s1); break;
                case 1:
                    if (s1.length() > 0 && s1.charAt(s1.length() - 1) == 'C') {
                    enforced = true;
                    label = new Label(s1.substring(0, s1.length() - 1), labelStyle);
                } else {
                    label = new Label(s1, labelStyle);
                }
                break;
                case 2: label.setX(Integer.parseInt(s1)); break;
                case 3: label.setY(Integer.parseInt(s1)); break;
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

    /** Draws the obstacle label to screen */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (conflict || label.getText().toString().charAt(0) == '#') {
            label.getStyle().fontColor = Color.RED;
        } else if (!enforced) {
            label.getStyle().fontColor = Color.GRAY;
        } else {
            label.getStyle().fontColor = Color.ORANGE;
        }
        label.draw(batch, 1);
    }

    /** Renders the polygon of obstacle to screen */
    public void renderShape() {
        if (conflict || label.getText().toString().charAt(0) == '#') {
            TerminalControl.radarScreen.shapeRenderer.setColor(Color.RED);
        } else if (!enforced) {
            TerminalControl.radarScreen.shapeRenderer.setColor(Color.GRAY);
        } else {
            TerminalControl.radarScreen.shapeRenderer.setColor(Color.ORANGE);
        }
        TerminalControl.radarScreen.shapeRenderer.polygon(polygon.getVertices());
    }

    /** Checks whether the input aircraft is inside the polygon area */
    public boolean isIn(Aircraft aircraft) {
        return (enforced || (aircraft.getNavState().getDispLatMode().last().contains("Turn") || aircraft.getNavState().getDispLatMode().last().equals("Fly heading"))) && polygon.contains(aircraft.getX(), aircraft.getY());
    }

    public int getMinAlt() {
        return minAlt;
    }

    public boolean isConflict() {
        return conflict;
    }

    public void setConflict(boolean conflict) {
        this.conflict = conflict;
    }

    public Label getLabel() {
        return label;
    }

    public boolean isEnforced() {
        return enforced;
    }
}
