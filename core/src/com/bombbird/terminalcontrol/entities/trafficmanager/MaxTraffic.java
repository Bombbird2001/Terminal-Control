package com.bombbird.terminalcontrol.entities.trafficmanager;

import java.util.HashMap;

public class MaxTraffic {
    public static final HashMap<String, Float> maxTraffic = new HashMap<String, Float>();

    public static void loadMaxTraffic() {
        maxTraffic.put("RCTP", 20f);
        maxTraffic.put("WSSS", 20f);
        maxTraffic.put("RJTT", 26f);
        maxTraffic.put("RJBB", 18f);
        maxTraffic.put("VHHH", 24f);
        maxTraffic.put("VTBD", 20f);
    }
}
