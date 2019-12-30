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
                "Fly heading - Aircraft will fly the selected heading. Turn left/right heading will force the aircraft to turn to the heading in the direction stated. You can instruct the aircraft to intercept the ILS/LDA in this mode.",
        "Altitude tab: Controls the altitude aspect of the aircraft.", "Descend via STAR/Climb via SID - Aircraft will fly to the selected altitude taking into consideration altitude restrictions of the SID/STAR. Not available in heading mode.",
                "Climb/descend to - Aircraft will fly directly to the selected altitude with no restrictions",
        "Speed tab: Controls the speed aspect of the aircraft.", "SID/STAR speed restrictions - Aircraft will follow the speed restrictions of the SID/STAR. Only allowed speeds are shown in the box. Not available in heading mode.",
                "No speed restrictions - Aircraft can ignore SID/STAR speed restrictions, all allowed speeds are shown in the box."});
        CONTENT_LIST.put("ILS, LDA", new String[] {"In this game, aircrafts must capture the ILS/LDA before landing. To capture the ILS, the following must be ensured:",
                "- The aircraft has been instructed to intercept the ILS in the pane's lateral tab. The aircraft must be flying in heading mode to do so.",
                "- The aircraft's altitude must be equal to or lower than the glide slope altitude at the intercept point. You can refer to the circles on the ILS line for the altitude. " +
                        "Starting from the lowest altitude allowed (usually 2000 feet, but can be higher for some airports) to the highest altitude in intervals of 1000 feet. " +
                        "Cyan circles indicate 2000 or 3000 feet, while a green circle indicates 4000 feet or higher.",
                "- Ideally, the aircraft's intercept angle should not be too large (\u2264 30\u00B0), or else it would likely overshoot the localizer.",
                "LDAs are a variant of the ILS, but are offset from the runway heading by an angle usually due to terrain, noise or other restrictions.",
                "Capturing the LDA is similar to an ILS, but LDAs are usually considered non-precision approaches that use a step-down descent unlike the glide slope of the ILS. " +
                        "At a certain point, aircrafts are allowed to descend to a pre-determined minimum altitude. The circles on the LDA indicate the step down points.",
                "Before reaching the runway, the aircraft will turn and line up with the runway before landing."});
        CONTENT_LIST.put("Separation", new String[] {"Aircrafts must be separated by a safe distance or altitude, otherwise you will lose points.", "Under normal circumstances, aircrafts must be seoarated from one another by at least 3 nautical miles (nm) or 1000 feet.",
                "However, for aircrafts that are established on parallel ILS approaches, no separation is required. (In real life, dependent parallel approaches require staggered separation but it is not implemented in this game)",
                "For aircraft that are established on the same ILS, if both aircrafts are less than 10nm from the runway, separation is reduced to 2.5nm or 1000 feet.",
                "Other separation standards also apply, such as wake turbulence separation and terrain separation. Refer to the MVAs, restricted areas and wake turbulence sections for more details."});
        CONTENT_LIST.put("MVAs, restricted areas", new String[] {"Aircrafts must be separated from terrain and restricted areas, or you will lose points.",
                "Minimum vectoring altitude sectors (MVAs) are displayed on the radar screen as grey polygons with grey numbers, to help you keep aircrafts separated from terrain. For simplicity, this only applies to aircrafts flying in heading mode. " +
                "For example, the number 70 will mean that an aircraft flying in heading mode needs to have a minimum altitude of 7000 feet.",
                "On the other hand, restricted areas are displayed on the radar screen as orange polygons with orange numbers, and all aircrafts must be kept separated from those areas at all times, including aircrafts flying on SIDs or STARs."});
        CONTENT_LIST.put("NTZs", new String[] {"No transgression zones, or NTZs, are areas designated between the final approach course of 2 simultaneous independent ILS approaches.",
                "Aircrafts on parallel approaches must not intrude the NTZ, which is depicted as a red rectangle between 2 ILS lines, otherwise you lose points.",
                "Aircrafts still need to be separated by the standard 3nm, 1000 feet until they both capture the ILS or are both in the Normal operating zone (NOZ) depicted by the green rectangles beside the NTZ."});
        CONTENT_LIST.put("Wake turbulence", new String[] {"Aircrafts must have sufficient wake turbulence separation from preceding aircrafts.",
                "This game utilises the EU's RECAT standards for wake turbulence, which is more efficient than ICAO's existing wake separation standards. Please note that one or more airports in this game may not use RECAT in real life.",
                "Each aircraft is destined a RECAT code, from A to F, where A is the highest and F is the lowest. Required separation is then dependent on the RECAT codes of the preceding aircraft and trailing aircraft.",
                "In-game, the required separation can be estimated by selecting the preceding aircraft, and referring to the its trail. Find the RECAT code of the trailing aircraft to estimate the separation distance required.",
                "Separation is lost when the trail distance between the between the preceding aircraft and the trailing aircraft is less than required, and if the trailing aircraft is between 0 to ~1000 feet lower than the preceding aircraft when it was at the current location of the trailing aircraft.",
                "On the ILS, an additional wake separation line is displayed for ease of reference. The position of the line depicts the separation required between a preceding and trailing aircraft.",
                "If an aircraft experiences wake turbulence on approach, it risks going around. You also lose points if wake separation is infringed."});
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
