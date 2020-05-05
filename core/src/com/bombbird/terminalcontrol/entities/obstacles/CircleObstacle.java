package com.bombbird.terminalcontrol.entities.obstacles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class CircleObstacle extends Obstacle {
    private Circle circle;

    public CircleObstacle(String toParse) {
        super();
        parseData(toParse);
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

    /** Renders the circle for restricted areas on screen */
    @Override
    public void renderShape() {
        shapeRenderer.end();
        shapeRenderer.begin("TCWS".equals(radarScreen.mainName) && getMinAlt() == 30000 ? ShapeRenderer.ShapeType.Filled : ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(circle.x, circle.y, circle.radius);
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (isConflict()) {
            shapeRenderer.setColor(Color.RED);
        } else if (!isEnforced()) {
            shapeRenderer.setColor(Color.GRAY);
        } else {
            shapeRenderer.setColor(Color.ORANGE);
        }
        shapeRenderer.circle(circle.x, circle.y, circle.radius);
    }

    /** Checks if input aircraft is in the circle */
    @Override
    public boolean isIn(float x, float y) {
        return circle.contains(x, y);
    }
}
