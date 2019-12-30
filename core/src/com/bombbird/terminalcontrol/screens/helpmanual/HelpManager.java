package com.bombbird.terminalcontrol.screens.helpmanual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.bombbird.terminalcontrol.screens.MainMenuScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.HashMap;

public class HelpManager {
    private final static HashMap<String, String[]> CONTENT_LIST = new HashMap<>();

    /** Loads the hashmap content order if not loaded */
    private static void loadHashMap() {
        CONTENT_LIST.put("Aircraft instructions", new String[] {"When an aircraft is selected by tapping on its label, a pane is shown on the left. It contains 3 tabs - the lateral, altitude, and speed tabs.",
        "Lateral tab: Controls the lateral aspects of the aircraft like heading.", "XXX arrival/departure - Aircraft will follow the prescribed STAR/SID, and the waypoint being flown to is displayed in the 2nd box below.",
                "After waypoint, fly heading - Aircraft will follow the prescribed STAR till the waypoint selected, then turns to the heading selected in the heading box after reaching the waypoint. Not available in heading mode.",
                "Hold at - Aircraft will fly the STAR till a pre-defined holding pattern at the selected waypoint. The track is depicted on the radar screen. Not available in heading mode.",
                "Fly heading - Aircraft will fly the selected heading. Turn left/right heading will force the aircraft to turn to the heading in the direction stated.",
        "Altitude tab: Controls the altitude aspect of the aircraft.", "Descend via STAR/Climb via SID - Aircraft will fly to the selected altitude taking into consideration altitude restrictions of the SID/STAR. Not available in heading mode.",
                "Climb/descend to - Aircraft will fly directly to the selected altitude with no restrictions",
        "Speed tab: Controls the speed aspect of the aircraft.", "SID/STAR speed restrictions - Aircraft will follow the speed restrictions of the SID/STAR. Only allowed speeds are shown in the box. Not available in heading mode.",
                "No speed restrictions - Aircraft can ignore SID/STAR speed restrictions, all allowed speeds are shown in the box."});
        CONTENT_LIST.put("NTZs", new String[] {"Test test test"});
    }

    /** Loads all the content for a specific page, adds to table for display */
    public static void loadContent(Table table, String page) {
        if (CONTENT_LIST.size() == 0) loadHashMap();
        String[] list = CONTENT_LIST.get(page);
        if (list == null) {
            Gdx.app.log("HelpManager", "Null list for " + page);
            return;
        }
        for (String str: list) {
            loadLabel(table, str);
        }
    }

    /** Loads a label */
    private static void loadLabel(Table table, String msg) {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = Color.WHITE;
        labelStyle.font = Fonts.defaultFont12;

        Label label = new Label(msg, labelStyle);
        label.setWrap(true);
        label.setWidth(MainMenuScreen.BUTTON_WIDTH * 2f - 20);
        table.add(label).width(MainMenuScreen.BUTTON_WIDTH * 2f - 20).pad(15, 10, 15, 0);
        table.row();
    }
}
