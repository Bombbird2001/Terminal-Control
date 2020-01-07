package com.bombbird.terminalcontrol.entities.weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class WindDirChance {
    private static final HashMap<String, Array<int[]>> windDirChances = new HashMap<String, Array<int[]>>();

    public static void loadWindDirChance() {
        windDirChances.put("TCTP", loadDirArray("387,150,225,297,479,488,363,332,292,145,126,109,117,105,103,136,157,164,107,108,154,185,414,818,681,236,113,94,128,107,119,112,107,101,95,94,115"));
        windDirChances.put("TCSS", loadDirArray("915,15,11,19,8,22,92,243,815,920,459,220,93,34,23,8,7,6,25,16,32,22,24,28,40,103,185,258,290,240,261,150,99,71,41,26,15"));
        windDirChances.put("TCWS", loadDirArray("1523,76,47,62,98,45,29,14,41,42,31,45,142,266,468,949,703,1025,683,280,381,286,159,52,30,14,6,17,24,51,56,75,110,112,73,45,51"));
        windDirChances.put("TCTT", loadDirArray("91,181,230,249,244,221,295,285,258,213,161,153,167,138,142,164,160,283,791,867,513,148,60,65,9,12,15,26,23,17,22,26,42,55,72,100,165"));
        windDirChances.put("TCAA", loadDirArray("487,181,289,432,405,269,166,140,103,94,94,145,240,306,396,427,425,403,281,204,115,131,188,133,83,60,35,26,24,26,31,39,66,86,49,41,46"));
        windDirChances.put("TCBB", loadDirArray("461,124,150,226,313,333,241,247,194,135,96,81,67,56,80,97,73,80,96,279,300,401,387,377,338,215,298,209,183,179,174,138,121,98,79,83,104"));
        windDirChances.put("TCOO", loadDirArray("380,18,10,12,18,43,94,174,134,76,36,12,11,14,3,4,3,6,18,20,47,128,213,210,114,32,17,5,15,16,33,62,64,70,44,42,25"));
        windDirChances.put("TCBE", loadDirArray("77,8,6,14,28,123,145,71,52,27,33,27,21,15,14,11,17,21,27,43,74,141,179,175,215,150,140,72,35,24,10,14,9,23,21,10,7"));
        windDirChances.put("TCHH", loadDirArray("379,61,65,97,132,189,320,392,447,500,539,229,89,84,134,183,125,73,124,203,324,367,593,364,204,98,172,148,76,57,49,50,56,64,53,57,66"));
        windDirChances.put("TCMC", loadDirArray("348,154,151,180,100,65,90,151,253,391,403,313,313,223,195,131,145,179,230,360,665,664,402,146,96,62,64,47,36,25,21,18,8,26,73,112,178"));
        windDirChances.put("TCBD", loadDirArray("487,13,10,6,9,17,14,20,26,22,54,96,111,73,115,87,130,236,368,646,639,607,290,423,575,571,529,288,197,108,68,55,37,28,18,20,15"));
        windDirChances.put("TCBS", loadDirArray("381,22,9,23,18,18,8,25,42,86,64,40,27,51,69,95,130,269,526,786,1271,799,521,360,264,308,294,181,129,92,62,60,30,22,28,22,22"));
        windDirChances.put("TCMD", loadDirArray("931,273,165,126,115,99,75,44,44,55,40,62,99,173,130,167,167,219,225,219,222,295,343,369,290,189,153,197,170,119,66,56,63,124,244,340,382"));
        windDirChances.put("TCPG", loadDirArray("228,160,145,126,126,124,99,58,57,74,95,76,94,137,130,194,222,305,354,449,329,350,330,275,292,221,183,155,112,88,75,71,72,70,51,54,77"));
        windDirChances.put("TCPO", loadDirArray("461,91,94,116,148,116,92,69,89,85,77,57,48,48,56,77,109,159,242,410,418,423,346,492,350,261,184,169,141,111,91,69,70,53,46,45,34"));
    }

    private static Array<int[]> loadDirArray(String toParse) {
        int total = 1;
        Array<int[]> directions = new Array<int[]>();
        String[] nos = toParse.split(",");
        for (String str: nos) {
            int[] range;
            if ("0".equals(str)) {
                range = new int[] {};
            } else {
                total += Integer.parseInt(str);
                range = new int[] {total - Integer.parseInt(str), total - 1};
            }
            directions.add(range);
        }
        //Add the total size of array to 1st index of last int[]
        directions.add(new int[] {total - 1});

        return directions;
    }

    public static int getRandomWindDir(String arpt) {
        Array<int[]> spds = windDirChances.get(arpt);
        int rand = MathUtils.random(1, spds.get(spds.size - 1)[0]);

        //Cycle through whole array to see which wind speed matches
        for (int i = 0; i < spds.size - 1; i++) {
            int[] range = spds.get(i);
            if (range.length < 2) continue;
            if (range[0] <= rand && rand <= range[1]) return i * 10;
        }
        Gdx.app.log("Random weather", "Random wind direction could not be generated from data. Completely random direction will be generated.");
        return MathUtils.random(1, 36) * 10;
    }
}
