package com.bombbird.terminalcontrol.entities.weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class WindspeedChance {
    private static final HashMap<String, Array<int[]>> windspdChances = new HashMap<String, Array<int[]>>();

    public static void loadWindSpdChance() {
        windspdChances.put("TCTP", loadSpdArray("0,128,319,489,590,687,634,565,512,423,352,330,316,300,333,341,334,316,259,214,165,133,103,62,44,28,22,23,15,4,8,7,3,1,1,1,1,0,0,0,0"));
        windspdChances.put("TCSS", loadSpdArray("2,266,569,602,572,477,426,425,419,355,390,303,252,193,160,132,81,72,38,29,22,14,10,11,7,3,3,3,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCWS", loadSpdArray("11,357,1038,846,648,801,926,923,830,705,435,263,181,94,44,8,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCTT", loadSpdArray("2,38,136,277,409,450,519,513,509,458,473,452,511,451,359,276,212,193,138,91,57,27,29,28,14,13,15,2,3,0,1,2,0,1,1,0,1,1,0,0,1"));
        windspdChances.put("TCAA", loadSpdArray("41,207,457,642,725,737,685,582,560,456,403,346,265,212,103,72,51,41,22,13,13,3,8,7,6,2,1,0,1,0,0,1,1,1,0,0,0,0,1,1,0"));
        windspdChances.put("TCBB", loadSpdArray("27,167,378,734,710,773,605,589,506,499,404,341,232,236,174,138,109,103,69,46,46,38,33,25,21,17,12,17,12,12,6,8,1,6,3,0,6,4,3,3,0"));
        windspdChances.put("TCOO", loadSpdArray("13,73,193,250,232,256,261,187,169,132,108,77,71,57,51,26,19,16,13,6,3,3,3,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCBE", loadSpdArray("4,20,67,129,143,169,190,189,188,174,166,113,101,83,65,55,54,39,25,19,22,19,18,7,7,4,2,3,0,2,0,0,1,0,0,1,0,0,0,0,0"));
        windspdChances.put("TCHH", loadSpdArray("1,21,126,362,455,586,658,657,623,565,677,510,456,385,299,223,151,146,94,54,50,29,16,7,4,2,2,1,1,2,0,0,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCMC", loadSpdArray("21,50,233,321,437,447,570,555,587,615,669,612,521,451,364,258,147,54,22,18,14,13,11,11,6,7,3,1,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCBD", loadSpdArray("368,30,171,389,553,708,927,926,865,713,507,303,202,128,89,59,29,14,12,6,5,0,0,1,0,2,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCBS", loadSpdArray("47,50,381,572,657,838,757,710,665,566,569,435,371,244,126,116,22,15,5,2,3,2,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCMD", loadSpdArray("0,593,926,1127,757,574,461,410,382,345,266,252,242,204,148,125,89,50,47,18,10,8,6,3,4,2,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCPG", loadSpdArray("15,23,96,247,401,420,513,520,541,550,614,442,378,282,229,214,145,159,89,70,33,34,15,6,6,3,4,5,3,0,0,1,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCPO", loadSpdArray("79,75,275,476,493,575,598,542,568,495,519,386,251,182,136,103,74,55,29,17,6,2,3,1,1,3,2,1,0,0,0,0,0,0,0,0,0,0,0,0,0"));
        windspdChances.put("TCHX", windspdChances.get("TCHH"));
    }

    private static Array<int[]> loadSpdArray(String toParse) {
        int total = 1;
        Array<int[]> spds = new Array<>();
        String[] nos = toParse.split(",");
        for (String str: nos) {
            int[] range;
            if ("0".equals(str)) {
                range = new int[] {};
            } else {
                total += Integer.parseInt(str);
                range = new int[] {total - Integer.parseInt(str), total - 1};
            }
            spds.add(range);
        }
        //Add the total size of array to 1st index of last int[]
        spds.add(new int[] {total - 1});

        return spds;
    }

    public static int getRandomWindspeed(String arpt, int dir) {
        if (dir == 0) return MathUtils.random(0, 5);
        Array<int[]> spds = windspdChances.get(arpt);
        int rand = MathUtils.random(1, spds.get(spds.size - 1)[0]);

        //Cycle through whole array to see which wind speed matches
        for (int i = 0; i < spds.size - 1; i++) {
            int[] range = spds.get(i);
            if (range.length < 2) continue;
            if (range[0] <= rand && rand <= range[1]) return i;
        }
        Gdx.app.log("Random weather", "Random wind speed could not be generated from data. Completely random speed will be generated.");
        return MathUtils.random(1, 40);
    }
}
