package com.bombbird.terminalcontrol.sounds;

import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class Pronunciation {
    public static final HashMap<String, String> waypointPronunciations = new HashMap<String, String>();
    public static HashMap<String, String> callsigns;

    /** Loads the pronunciations */
    public static void loadPronunciation() {
        waypointPronunciations.put("APU", "aun poo");
        waypointPronunciations.put("YILAN", "yee lan");
        waypointPronunciations.put("HLG", "ho long");
        waypointPronunciations.put("ZUH", "Lian Sheng Way");
        callsigns = FileLoader.loadIcaoCallsigns();
    }

    /** Checks whether direct contains a number */
    public static String checkNumber(String direct) {
        String[] numbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
        String[] directList = direct.split("");
        for (int i = directList.length - 1; i>= 0; i--) {
            if (ArrayUtils.contains(numbers, directList[i])) return StringUtils.join(directList, " ");
        }
        return direct;
    }

    /** Converts any number into text due to special pronunciation requirements for 0, 9 and . */
    public static String convertNoToText(String altitude) {
        String[] list = altitude.split("");
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals("0")) list[i] = "zero";
            if (list[i].equals("9")) list[i] = "niner";
            if (list[i].equals(".")) list[i] = "decimal";
        }
        return StringUtils.join(list, " ");
    }

    /** Converts any FLXXX in text to flight level X X X */
    public static String convertToFlightLevel(String action) {
        String[] actionList = action.split(" ");
        for (int i = 0; i < actionList.length; i++) {
            if (actionList[i].contains("FL")) {
                String altitude = Pronunciation.convertNoToText(actionList[i].substring(2));
                String phrase = "flight level" + altitude;
                actionList[i] = phrase;
            }
        }
        return StringUtils.join(actionList, " ");
    }
}
