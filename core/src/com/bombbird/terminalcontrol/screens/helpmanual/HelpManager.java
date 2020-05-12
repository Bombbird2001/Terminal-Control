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
                "Fly heading - Aircraft will fly the selected heading. Turn left/right heading will force the aircraft to turn to the heading in the direction stated. You can instruct the aircraft to intercept the ILS/LDA in this mode.",
                "Hold at - Aircraft will fly the STAR till a pre-defined holding pattern at the selected waypoint. The track is depicted on the radar screen. Not available in heading mode.",
                "Change STAR - Changes the STAR used by an arrival. Arrival will change to fly heading mode, and you will need to re-clear it to a waypoint on the new STAR.",
        "Altitude tab: Controls the altitude aspect of the aircraft.", "Descend via STAR/Climb via SID - Aircraft will fly to the selected altitude taking into consideration altitude restrictions of the SID/STAR. Not available in heading mode.",
                "Climb/descend to - Aircraft will fly directly to the selected altitude with no restrictions",
        "Speed tab: Controls the speed aspect of the aircraft.", "SID/STAR speed restrictions - Aircraft will follow the speed restrictions of the SID/STAR. Only allowed speeds are shown in the box. Not available in heading mode.",
                "No speed restrictions - Aircraft can ignore SID/STAR speed restrictions, all allowed speeds are shown in the box."});
        CONTENT_LIST.put("ILS, LDA", new String[] {"In this game, planes must capture the ILS/LDA before landing. To capture the ILS, the following must be ensured:",
                "- The aircraft has been instructed to intercept the ILS in the pane's lateral tab. The aircraft must be flying in heading mode to do so.",
                "- The aircraft's altitude must be equal to or lower than the glide slope altitude at the intercept point. You can refer to the circles on the ILS line for the altitude. " +
                        "Starting from the lowest altitude allowed (usually 2000 feet, but can be higher for some airports) to the highest altitude in intervals of 1000 feet. " +
                        "Cyan circles indicate 2000 or 3000 feet, while a green circle indicates 4000 feet or higher.",
                "- Ideally, the aircraft's intercept angle should not be too large (<= 30\u00B0), or else it would likely overshoot the localizer.",
                "LDAs are a variant of the ILS, but are offset from the runway heading by an angle usually due to terrain, noise or other restrictions.",
                "Capturing the LDA is similar to an ILS, but LDAs are usually considered non-precision approaches that use a step-down descent unlike the glide slope of the ILS. " +
                        "At a certain point, an aircraft is allowed to descend to a pre-determined minimum altitude. The circles on the LDA indicate the step down points.",
                "Before reaching the runway, the aircraft will turn and line up with the runway before landing."});
        CONTENT_LIST.put("Separation", new String[] {"Planes must be separated by a safe distance or altitude, otherwise you will lose points.", "Under normal circumstances, planes must be separated from one another by at least 3 nautical miles (nm) or 1000 feet (except some airports).",
                "However, for planes that are established on dependent parallel ILS approaches (parallel approaches without an NTZ), staggered separation of 2nm is required.",
                "For aircraft that are established on the same ILS, if both aircraft are less than 10nm from the runway, separation is reduced to 2.5nm or 1000 feet.",
                "Other separation standards also apply, such as wake turbulence separation and terrain separation. Refer to the MVAs, restricted areas and wake turbulence sections for more details."});
        CONTENT_LIST.put("MVAs, restricted areas", new String[] {"Planes must be separated from terrain and restricted areas, or you will lose points.",
                "Minimum vectoring altitude sectors (MVAs) are displayed on the radar screen as grey polygons with grey numbers, to help you keep aircraft separated from terrain. For simplicity, this only applies to planes flying in heading mode. " +
                "For example, the number 70 will mean that an aircraft flying in heading mode needs to have a minimum altitude of 7000 feet.",
                "On the other hand, restricted areas are displayed on the radar screen as orange polygons with orange numbers, and all aircraft must be kept separated from those areas at all times, including those flying on SIDs or STARs."});
        CONTENT_LIST.put("NTZ", new String[] {"No transgression zones, or NTZs, are areas designated between the final approach course of 2 simultaneous independent ILS approaches.",
                "Aircraft on parallel approaches must not intrude the NTZ, which is depicted as a red rectangle between 2 ILS lines, otherwise you lose points.",
                "Planes still need to be separated by the standard 3nm, 1000 feet until they both capture the ILS or are both in the Normal operating zone (NOZ) depicted by the green rectangles beside the NTZ."});
        CONTENT_LIST.put("Wake turbulence", new String[] {"Planes must have sufficient wake turbulence separation from preceding aircraft.",
                "This game utilises the EU's RECAT standards for wake turbulence, which is more efficient than ICAO's existing wake separation standards.",
                "Each aircraft is destined a RECAT code, from A to F, where A is the highest and F is the lowest. Required separation is then dependent on the RECAT codes of the preceding aircraft and trailing aircraft.",
                "In-game, the required separation can be estimated by selecting the preceding aircraft, and referring to the its trail. Find the RECAT code of the trailing aircraft to estimate the separation distance required.",
                "Separation is lost when the trail distance between the between the preceding aircraft and the trailing aircraft is less than required, and if the trailing aircraft is between 0 to ~1000 feet lower than the preceding aircraft when it was at the current location of the trailing aircraft.",
                "On the ILS, an additional wake separation line is displayed for ease of reference. The position of the line depicts the separation required between a preceding and trailing aircraft.",
                "If an aircraft experiences wake turbulence on approach, it risks going around. You also lose points if wake separation is infringed."});
        CONTENT_LIST.put("Advanced trajectory", new String[] {"(Available in full version only)", "In addition to the default trajectory line on the radar screen, there is an additional, more advanced option for trajectory prediction to be unlocked.",
                "The advanced trajectory system will predict the aircraft's future position for up to 2 minutes (depending on settings). The system takes into account the current track of the aircraft as well as the heading it is cleared to, to calculate a more accurate trajectory.",
                "This option is useful when an aircraft is being manually vectored, and the player can better see the turn trajectory of the aircraft.",
                "In the SID/STAR mode, the system can only predict the aircraft turn trajectory after it has started the turn. As such, the system cannot predict trajectory based on the SID/STAR route."});
        CONTENT_LIST.put("Conflict prediction alerts", new String[] {"(Available in full version only)", "In addition to the standard separation infringement alert, there 2 additional alerts available to unlock - the Short Term Collision Alert System (STCAS) and the Area Penetration Warning (APW).",
                "Both systems will predict an aircraft's future position based on its current trajectory, heading and altitude clearances.", "The STCAS will alert the player if a potential conflict (less than 3nm or 1000 feet separation most of the time - see the Separation section in the manual for more information) is predicted within a time frame. The aircraft labels will flash magenta and a magenta box is displayed to depict the approximate conflict position.",
                "The APW will alert the player if a potential infringement of MVA sectors (for aircraft being manually vectored only) or restricted areas (for all aircraft) is predicted within a time frame. This warning will be suppressed when the aircraft is cleared for an ILS/LDA approach to prevent nuisance warnings. Similarly, the aircraft label will flash magenta and the magenta box showing approximate infringement position.",
                "For more information on MVAs, restricted areas, see the respective section in the help manual.", "The prediction times available for unlock are 30 seconds, 60 seconds and 120 seconds.",
                "Do note that nuisance alerts can occur since the system predicts based only on the aircraft's current trajectory. The longer the prediction time selected, the higher the chance of nuisance alerts.", "Furthermore, changes to aircraft trajectory could result in a conflict that could not be predicted by the system."});
        CONTENT_LIST.put("TCTP", new String[] {"TCTP, Haoyuan International Airport", "Runways:", "05L-23R: 3660m", "05R-23L: 3800m", "Configurations: 05L and 05R or 23L and 23R",
                "Segregated runway operations usually used, where 1 runway is used for arrivals and the other for departures. Mixed mode is also used sometimes.",
                "TCSS, Rongshan Airport", "Runway:", "10-28: 2605m", "Configurations: 10 or 28", "Runway is used for both arrivals, departures. When 28 is in use, planes will use the LDA28 approach which is a non-precision approach with step down altitudes."});
        CONTENT_LIST.put("TCWS", new String[] {"TCWS, Changli International Airport", "Runways:", "02L-20R: 4000m", "02C-20C: 4000m", "Configurations: 02L and 02C or 20C and 20R",
                "Segregated runway operations usually used, where 02L-20R is usually used for arrivals, and 02C-20C used for departures. However when needed, independent simultaneous ILS approaches can be conducted for both runways."});
        CONTENT_LIST.put("TCTT", new String[] {"TCTT, Naheda Airport", "Runways:", "34L-16R: 3000m", "34R-16L: 3360m", "04-22: 2500m", "05-23: 2500m", "Configurations: 34L, 34R and 05 or 16L, 16R, 22 and 23",
                "Simultaneous approaches are used for 34L, 34R. Simultaneous LDA approaches are also used for 22, 23, which are non-precision approaches. Simultaneous departures are used for 16L, 16R.",
                "TCTT STARs get the aircraft to fly in an arc initially, and the controller can instruct the aircraft to fly direct towards DEWWY, GLISS or CAMER after ensuring sufficient separation.",
                "When night operations are active, STARs are modified to exclude the arc and various restrictions. Only 1 departure SID, POPAR3, is used.",
                "TCAA, Tanari Airport", "Runways:", "34L-16R: 4000m", "34R-16L: 2500m", "Configurations: 34L and 34R or 16L and 16R",
                "Segregated runway operations usually used, with 34L-16R used for departures and 34R-16L used for arrivals. However, when needed, simultaneous arrivals and departures may be used for both runways.",
                "TCAA STARs also utilise the arc, however they are outside the game world and so are not seen in the game."});
        CONTENT_LIST.put("TCHH", new String[] {"TCHH, Tang Gong International Airport", "Runways:", "07L-25R: 3800m", "07R-25L: 3800m", "Configurations: 07L and 07R or 25L and 25R",
                "Segregated runway operations usually used, with 07L-25R used for arrivals and 07R-25L used for departures. High terrain around the airport means caution must be taken when vectoring planes.",
                "Most holding waypoints are located outside the game world except SANTO and RANGE, hence delay vectors may be used instead.",
                "When night operations are active, SIDs are modified to take the aircraft out to sea early during the climb to reduce noise in the city area.",
                "TCMC, Macaw International Airport", "Runways:", "16-34: 3360m", "Configurations: 16 and 34", "Runway is used for both arrivals, departures. When 16 is in use, planes will use the LDA16 approach which is a non-precision approach",
                "TCMC STARs, SIDs have strict altitude, speed restrictions due to the busy airspace around it."});
        CONTENT_LIST.put("TCBB", new String[] {"TCBB, Saikan International Airport", "Runways:", "06L-24R: 4000m", "06R-24L: 3500m", "Configurations: 06L and 06R or 24L and 24R",
                "Segregated runway operations usually used, with 06L-24R used for arrivals, and 06R-24L used for departures.",
                "When night operations are active, SIDs are restricted to TANKI1, WAIYU1, MELEN1, SANUS1. STARs for runways 24L/24R are also modified to bring the aircraft further from the city area to reduce noise.",
                "TCOO, Mitai Airport", "Runways:", "32L-14R: 3000m", "32R-14L: 1800m", "Configuration: 32L",
                "Only a single runway, 32L-14R is used as 32R-14L is located too close and is likely too short. Furthermore, runway 14R has no ILS approach, hence circling approach is used which is not implemented in the game. As such, 14R is not used in the game.",
                "There is relatively high terrain to the east of the airport, hence caution must be taken when vectoring aircraft.",
                "TCBE, Boke Airport", "Runways:", "09-27: 2500m", "Configuration: 09", "Runway 09 is used for both arrivals, departures. Runway 27 is not used in game due to it lacking an ILS approach and requiring a circling approach instead.",
                "Due to its proximity with TCBB traffic, care must be taken when handling TCBE departures."});
        CONTENT_LIST.put("TCBD", new String[] {"TCBD, Lon Man International Airport", "Runways:", "03L-21R: 3700m", "03R-21L: 3500m", "Configurations: 03L and 03R or 21L and 21R",
                "Segregated runway operations are used, with 03L-21R used for arrivals, and 03R-21L used for departures.",
                "In order to avoid restricted areas at the city, runway 21L SIDs have to make an early right turn to avoid it. Runway 03L STARs also need to navigate around the area before lining up for a relatively short final.",
                "TCBS, Huvarna Airport", "Runways:", "01L-19R: 3700m", "01R-19L: 4000m", "Configurations: 01L and 01R or 19L or 19R",
                "Segregated runway operations usually used, with 01R-19L used for arrivals, and 01L-19R used for departures."});
        CONTENT_LIST.put("TCMD", new String[] {"TCMD, Hadrise Airport", "Runways:", "14L-32R: 3500m", "14R-32L: 4100m", "18L-36R: 3500m", "18R-36L: 4350m", "Configurations: 36L, 36R, 32L and 32R or 14L, 14R, 18L or 18R",
                "Simultaneous departures used for 36L and 36R or 14L and 14R. Simultaneous arrivals used for 18L and 18R or 32L or 32R",
                "High terrain, restricted areas are present to the north-west of the airport, caution should be taken when vectoring aircraft in that sector. Runway 18L, 18R STARs do take them into account.",
                "During night operations, only 2 runways are used. 14L or 36L is used for departure, 18L or 32R is used for arrivals."});
        CONTENT_LIST.put("TCPG", new String[] {"TCPG, Shartes o' Dickens Airport", "Runways:", "08L-26R: 4215m", "08R-26L: 2700m", "09L-27R: 2700m", "09R-27L: 4200m", "Configurations: 08L, 08R, 09L and 09R or 26L, 26R, 27L and 27R",
                "Simultaneous departures used for 08L and 09R or 26R and 27L. Simultaneous arrivals used for 08R and 09L or 26L and 27R",
                "TCPG STARs end some distance from the runway, and planes will need to be vectored manually to the ILS. When vectoring south-east or south-west arrivals, depending on the runway configuration, you will also need to be aware of the restricted area over the city, as well as TCPO traffic.",
                "TCPO, Roly Airport", "Runways:", "06-24: 3650m", "07-25: 3320m", "02-20: 2400m", "Configurations: 06 and 07 or 24 and 25",
                "Segregated runway operations are used, with 06 or 25 used for arrivals, and 07 or 24 used for departures. Runway 02-20 is almost always unused for IFR flights.",
                "TCPO only uses STARs from the south-east to prevent conflict with TCPG traffic. These STARs also end early like those in TCPG. Take note of possible conflict between TCPO departures and TCPG arrivals from south-east or south-west."});
        CONTENT_LIST.put("TCHX", new String[] {"TCHX, Tai Kek International Airport", "Runways:", "13-31: 3390m", "Configurations: 13 or 31",
                "Runway is used for both arrivals, departures. When runway 13 is in use, aircraft will use the IGS13 approach, which is an ILS approach offset about 47\u00B0 from runway 13. Planes will need to make a last minute right turn before landing on runway 13.",
                "High terrain exists around airport, caution should be used when vectoring aircraft."});
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
