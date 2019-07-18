package com.bombbird.terminalcontrol.entities.restrictions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class CircleObstacle extends Actor {
    private Circle circle;
    private int minAlt;
    private Label label;
    private boolean conflict;
    private boolean enforced;

    private RadarScreen radarScreen;
    private ShapeRenderer shapeRenderer;

    public CircleObstacle(String toParse) {
        parseData(toParse);
        radarScreen = TerminalControl.radarScreen;
        shapeRenderer = radarScreen.shapeRenderer;
    }

    /** Parses input string into relevant information */
    private void parseData(String toParse) {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont12;
        labelStyle.fontColor = Color.GRAY;

        String[] restInfo = toParse.split(", ");

        int index = 0;
        float centreX = 0;
        float centreY = 0;
        float radius = 0;
        for (String s1: restInfo) {
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
                case 4: centreX = Float.parseFloat(s1); break;
                case 5: centreY = Float.parseFloat(s1); break;
                case 6: radius = Float.parseFloat(s1); break;
                default: Gdx.app.log("Load error", "Unexpected additional parameter in game/" + radarScreen.mainName + "/restricted.rest");
            }
            index++;
        }
        setPosition(centreX - radius, centreY - radius);
        setSize(radius * 2, radius * 2);
        circle = new Circle(centreX, centreY, radius);
    }

    /** Draws the area label to screen */
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

    /** Renders the circle for restricted areas on screen */
    public void renderShape() {
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(circle.x, circle.y, circle.radius);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (conflict) {
            shapeRenderer.setColor(Color.RED);
        } else if (!enforced) {
            shapeRenderer.setColor(Color.GRAY);
        } else {
            shapeRenderer.setColor(Color.ORANGE);
        }
        shapeRenderer.circle(circle.x, circle.y, circle.radius);
    }

    /** Checks if input aircraft is in the circle */
    public boolean isIn(Aircraft aircraft) {
        return (enforced || (aircraft.getNavState().getDispLatMode().last().contains("Turn") || aircraft.getNavState().getDispLatMode().last().equals("Fly heading"))) && circle.contains(aircraft.getX(), aircraft.getY());
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
}
