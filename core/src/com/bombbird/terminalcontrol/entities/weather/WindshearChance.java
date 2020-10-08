package com.bombbird.terminalcontrol.entities.weather;

import com.badlogic.gdx.math.MathUtils;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.runways.Runway;
import com.bombbird.terminalcontrol.entities.airports.Airport;

import java.util.HashMap;

public class WindshearChance {
    private static final HashMap<String, float[]> logRegCoefficients = new HashMap<>();

    public static void loadWsChance() {
        logRegCoefficients.put("TCTP", new float[] {-7.828657998633319f, 0.3877695849020738f});
        logRegCoefficients.put("TCSS", new float[] {-7.570236541732326f, 0.42294053309933444f});
        logRegCoefficients.put("TCWS", new float[] {-8.192262378235482f, 0.24738504082400115f});
        logRegCoefficients.put("TCTT", new float[] {});
        logRegCoefficients.put("TCAA", new float[] {-6.916322317610984f, 0.30476535614392136f});
        logRegCoefficients.put("TCBB", new float[] {});
        logRegCoefficients.put("TCOO", new float[] {-8.435239918930467f, 0.2668787164182838f});
        logRegCoefficients.put("TCBE", new float[] {});
        logRegCoefficients.put("TCHH", new float[] {-6.956372521078374f, 0.22558516085627847f});
        logRegCoefficients.put("TCMC", new float[] {});
        logRegCoefficients.put("TCBD", new float[] {-12.598352844435272f, 0.4759057515584942f});
        logRegCoefficients.put("TCBS", new float[] {-11.162357827620264f, 0.4884167870726882f});
        logRegCoefficients.put("TCMD", new float[] {-5.842837306470557f, 0.18198226237092718f});
        logRegCoefficients.put("TCPG", new float[] {});
        logRegCoefficients.put("TCPO", new float[] {});
        logRegCoefficients.put("TCHX", logRegCoefficients.get("TCHH"));
    }

    private static boolean getRandomWs(String icao, int speed) {
        if (!logRegCoefficients.containsKey(icao) || logRegCoefficients.get(icao).length == 0) return false;
        float b0 = logRegCoefficients.get(icao)[0];
        float b1 = logRegCoefficients.get(icao)[1];
        float prob = (float)(1 / (1 + Math.exp(-b0 - b1 * speed)));
        return MathUtils.randomBoolean(prob);
    }

    public static String getRandomWsForAllRwy(String icao, int speed) {
        String ws;
        Airport airport = TerminalControl.radarScreen.airports.get(icao);
        StringBuilder stringBuilder = new StringBuilder();
        for (Runway runway: airport.getRunways().values()) {
            if (!runway.isLanding()) continue;
            if (getRandomWs(airport.getIcao(), speed)) {
                stringBuilder.append("R");
                stringBuilder.append(runway.getName());
                stringBuilder.append(" ");
            }
        }
        if (stringBuilder.length() > 3 && stringBuilder.length() == airport.getLandingRunways().size() * 3) {
            ws = "ALL RWY";
        } else {
            ws = stringBuilder.toString();
        }

        return ws;
    }
}
