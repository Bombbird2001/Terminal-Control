package com.bombbird.terminalcontrol.entities.approaches;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.utilities.math.MathTools;
import org.apache.commons.lang3.StringUtils;

public class LDA extends ILS {
    private Queue<float[]> nonPrecAlts;
    private float lineUpDist;
    private boolean npa;
    private ILS imaginaryIls;

    public LDA(Airport airport, String toParse) {
        super(airport, toParse);
        loadImaginaryIls();
    }

    /** Overrides method in ILS to also load the non precision approach altitudes if applicable */
    @Override
    public void parseInfo(String toParse) {
        super.parseInfo(toParse);

        String[] info = toParse.split(",");
        lineUpDist = Float.parseFloat(info[9]);

        npa = false;

        if (info.length >= 11) {
            npa = true;
            nonPrecAlts = new Queue<float[]>();

            for (String s3 : info[10].split("-")) {
                float[] altDist = new float[2];
                int index1 = 0;
                for (String s2 : s3.split(">")) {
                    altDist[index1] = Float.parseFloat(s2);
                    index1++;
                }
                nonPrecAlts.addLast(altDist);
            }
        }
    }

    /** Loads the imaginary ILS from runway center line */
    private void loadImaginaryIls() {
        String text = "IMG" + getRwy().getName() + "," + getRwy().getName() + "," + getRwy().getHeading() + "," + getRwy().getX() + "," + getRwy().getY() + ",0,0,4000," + StringUtils.join(getTowerFreq(), ">");
        imaginaryIls = new ILS(getAirport(), text);
    }

    /** Overrides method in ILS to ignore it if NPA */
    @Override
    public void calculateGsRings() {
        if (!npa) {
            super.calculateGsRings();
        }  else {
            Array<Vector2> gsRings = new Array<Vector2>();
            for (int i = 0; i < nonPrecAlts.size; i++) {
                gsRings.add(new Vector2(getX() + MathTools.nmToPixel(nonPrecAlts.get(i)[1]) * MathUtils.cosDeg(270 - getHeading() + TerminalControl.radarScreen.magHdgDev), getY() + MathTools.nmToPixel(nonPrecAlts.get(i)[1]) * MathUtils.sinDeg(270 - getHeading() + TerminalControl.radarScreen.magHdgDev)));
            }
            setGsRings(gsRings);
        }
    }

    public Queue<float[]> getNonPrecAlts() {
        return nonPrecAlts;
    }

    public float getLineUpDist() {
        return lineUpDist;
    }

    public boolean isNpa() {
        return npa;
    }

    public ILS getImaginaryIls() {
        return imaginaryIls;
    }
}
