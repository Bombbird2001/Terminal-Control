package com.bombbird.terminalcontrol.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import com.bombbird.terminalcontrol.utilities.saving.FileLoader;

public class Shoreline {
    private static Array<Array<Integer>> landmasses = new Array<Array<Integer>>();

    public static void loadShoreline() {
        landmasses = FileLoader.loadShoreline();
    }

    public static void renderShape() {
        for (int i = 0; i < landmasses.size; i++) {
            for (int j = 2; j < landmasses.get(i).size; j += 2) {
                int prevX = landmasses.get(i).get(j - 2);
                int prevY = landmasses.get(i).get(j - 1);
                int thisX = landmasses.get(i).get(j);
                int thisY = landmasses.get(i).get(j + 1);

                if ((!MathTools.withinRange(prevX, 1260, 4500) || !MathTools.withinRange(prevY, 0, 3240)) && (!MathTools.withinRange(thisX, 1260, 4500) || !MathTools.withinRange(thisY, 0, 3240))) {
                    //Both points not inside range, don't draw line
                    continue;
                }
                //Draw lines to connect points
                TerminalControl.radarScreen.shapeRenderer.setColor(Color.BROWN);
                TerminalControl.radarScreen.shapeRenderer.line(prevX, prevY, thisX, thisY);
            }
        }
    }
}
