package com.bombbird.terminalcontrol.sounds;

import com.bombbird.terminalcontrol.utilities.saving.FileLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class Pronunciation {
    public static final HashMap<String, String> waypointPronunciations = new HashMap<String, String>();
    public static HashMap<String, String> callsigns;
    public static final HashMap<Character, String> alphabetPronunciations = new HashMap<Character, String>();

    /** Loads the pronunciations */
    public static void loadPronunciation() {
        waypointPronunciations.put("PAU", "pan on");
        waypointPronunciations.put("LHG", "low hon");
        waypointPronunciations.put("UZH", "sheng lian");
        waypointPronunciations.put("LNG", "lung nan");
        waypointPronunciations.put("MST", "mo she toe");
        waypointPronunciations.put("TVB", "tilla vobas");
        waypointPronunciations.put("LTD", "tornado");
        waypointPronunciations.put("VNS", "vanas");
        waypointPronunciations.put("JCN", "jo castin");
        waypointPronunciations.put("BRO", "noble ditto");
        waypointPronunciations.put("ISE", "momo terra");

        callsigns = FileLoader.loadIcaoCallsigns();

        alphabetPronunciations.put('A', "alpha");
        alphabetPronunciations.put('B', "bravo");
        alphabetPronunciations.put('C', "charlie");
        alphabetPronunciations.put('D', "delta");
        alphabetPronunciations.put('E', "echo");
        alphabetPronunciations.put('F', "foxtrot");
        alphabetPronunciations.put('G', "golf");
        alphabetPronunciations.put('H', "hotel");
        alphabetPronunciations.put('I', "india");
        alphabetPronunciations.put('J', "juliett");
        alphabetPronunciations.put('K', "kilo");
        alphabetPronunciations.put('L', "lima");
        alphabetPronunciations.put('M', "mike");
        alphabetPronunciations.put('N', "november");
        alphabetPronunciations.put('O', "oscar");
        alphabetPronunciations.put('P', "papa");
        alphabetPronunciations.put('Q', "quebec");
        alphabetPronunciations.put('R', "romeo");
        alphabetPronunciations.put('S', "sierra");
        alphabetPronunciations.put('T', "tango");
        alphabetPronunciations.put('U', "uniform");
        alphabetPronunciations.put('V', "victor");
        alphabetPronunciations.put('W', "whiskey");
        alphabetPronunciations.put('X', "x-ray");
        alphabetPronunciations.put('Y', "yankee");
        alphabetPronunciations.put('Z', "zulu");
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
    public static String convertNoToText(String number) {
        String[] list = number.split("");
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals("0")) list[i] = "zero";
            if (list[i].equals("9")) list[i] = "niner";
            if (list[i].equals(".")) list[i] = "decimal";
        }
        return StringUtils.join(list, " ");
    }

    /** Converts any FLXXX in text to flight level X X X, returns joined text */
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
