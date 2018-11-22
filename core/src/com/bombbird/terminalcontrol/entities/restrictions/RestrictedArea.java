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

public class RestrictedArea extends Actor {
    private Circle circle;
    private int minAlt;
    private Label label;

    private RadarScreen radarScreen;
    private ShapeRenderer shapeRenderer;

    public RestrictedArea(String toParse) {
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
                case 1: label = new Label(s1, labelStyle); break;
                case 2: label.setX(Integer.parseInt(s1)); break;
                case 3: label.setY(Integer.parseInt(s1)); break;
                case 4: centreX = Float.parseFloat(s1); break;
                case 5: centreY = Float.parseFloat(s1); break;
                case 6: radius = Float.parseFloat(s1) * 2; break;
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
        shapeRenderer.setColor(Color.GRAY);
        shapeRenderer.circle(circle.x, circle.y, circle.radius);
    }

    /** Checks if input aircraft is in the circle */
    public boolean isIn(Aircraft aircraft) {
        return circle.contains(aircraft.getX(), aircraft.getY());
    }

    public int getMinAlt() {
        return minAlt;
    }
}
