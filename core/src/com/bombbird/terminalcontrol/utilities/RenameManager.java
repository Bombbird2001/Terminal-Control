package com.bombbird.terminalcontrol.utilities;

import java.util.HashMap;

public class RenameManager {
    private static final HashMap<String, String> icaoMap = new HashMap<>();
    private static final HashMap<String, String> reverseIcaoMap = new HashMap<>();

    public static void loadMaps() {
        if (icaoMap.size() == 0) {
            icaoMap.put("RCTP", "TCTP");
            icaoMap.put("RCSS", "TCSS");
            icaoMap.put("WSSS", "TCWS");
            icaoMap.put("RJTT", "TCTT");
            icaoMap.put("RJAA", "TCAA");
            icaoMap.put("VHHH", "TCHH");
            icaoMap.put("VMMC", "TCMC");
            icaoMap.put("RJBB", "TCBB");
            icaoMap.put("RJOO", "TCOO");
            icaoMap.put("RJBE", "TCBE");
            icaoMap.put("VTBS", "TCBS");
            icaoMap.put("VTBD", "TCBD");
            icaoMap.put("LEMD", "TCMD");
            icaoMap.put("LFPG", "TCPG");
            icaoMap.put("LFPO", "TCPO");
            icaoMap.put("VHHX", "TCHX");
        }
        if (reverseIcaoMap.size() == 0) {
            reverseIcaoMap.put("TCTP", "RCTP");
            reverseIcaoMap.put("TCSS", "RCSS");
            reverseIcaoMap.put("TCWS", "WSSS");
            reverseIcaoMap.put("TCTT", "RJTT");
            reverseIcaoMap.put("TCAA", "RJAA");
            reverseIcaoMap.put("TCHH", "VHHH");
            reverseIcaoMap.put("TCMC", "VMMC");
            reverseIcaoMap.put("TCBB", "RJBB");
            reverseIcaoMap.put("TCOO", "RJOO");
            reverseIcaoMap.put("TCBE", "RJBE");
            reverseIcaoMap.put("TCBS", "VTBS");
            reverseIcaoMap.put("TCBD", "VTBD");
            reverseIcaoMap.put("TCMD", "LEMD");
            reverseIcaoMap.put("TCPG", "LFPG");
            reverseIcaoMap.put("TCPO", "LFPO");
            reverseIcaoMap.put("TCHX", "VHHX");
        }
    }

    /** Changes old ICAO codes to new ICAO codes */
    public static String renameAirportICAO(String icao) {
        return icaoMap.containsKey(icao) ? icaoMap.get(icao) : icao;
    }

    /** Changes new ICAO to real ICAO */
    public static String reverseNameAirportICAO(String icao) {
        return reverseIcaoMap.containsKey(icao) ? reverseIcaoMap.get(icao) : icao;
    }
}
