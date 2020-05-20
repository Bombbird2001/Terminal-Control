package com.bombbird.terminalcontrol.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.Fonts;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChangelogScreen extends StandardUIScreen {
    private static final LinkedHashMap<String, Array<String>> changeLogContent = new LinkedHashMap<>();
    private final Table scrollTable;

    public ChangelogScreen(final TerminalControl game, Image background) {
        super(game, background);

        scrollTable = new Table();
    }

    /** Loads the full UI of this screen */
    public void loadUI() {
        super.loadUI();
        loadLabel("Changelog");
        loadScroll();
        loadContent();
        loadButtons();
    }

    private void loadScroll() {
        ScrollPane scrollPane = new ScrollPane(scrollTable);

        scrollPane.setX(2880 / 2f - MainMenuScreen.BUTTON_WIDTH);
        scrollPane.setY(1620 * 0.2f);
        scrollPane.setWidth(MainMenuScreen.BUTTON_WIDTH * 2);
        scrollPane.setHeight(1620 * 0.6f);
        scrollPane.getStyle().background = TerminalControl.skin.getDrawable("ListBackground");

        stage.addActor(scrollPane);
    }

    private void loadContent() {
        loadHashmapContent();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.fontColor = Color.WHITE;
        labelStyle.font = Fonts.defaultFont12;

        Label.LabelStyle labelStyle1 = new Label.LabelStyle();
        labelStyle1.fontColor = Color.BLACK;
        labelStyle1.font = Fonts.defaultFont16;

        boolean thisVersion = true;
        for (Map.Entry<String, Array<String>> entry: changeLogContent.entrySet()) {
            String version = entry.getKey();
            if (thisVersion) {
                version += " (current version)";
                thisVersion = false;
            }
            Label versionLabel = new Label("Version " + version, labelStyle1);
            scrollTable.add(versionLabel).width(MainMenuScreen.BUTTON_WIDTH * 2f - 20).pad(15, 10, 15, 0);
            scrollTable.row();

            Label label = new Label("Loading...", labelStyle);
            StringBuilder content = new StringBuilder();
            for (String change: entry.getValue()) {
                content.append(change).append("\n");
            }
            label.setText(content.toString());
            label.setWrap(true);
            label.setWidth(MainMenuScreen.BUTTON_WIDTH * 2f - 20);
            scrollTable.add(label).width(MainMenuScreen.BUTTON_WIDTH * 2f - 40).pad(15, 30, 15, 0);
            scrollTable.row();
        }
    }

    private void loadHashmapContent() {
        if (changeLogContent.size() > 0) return;
        //Version 1.4.2006.1
        Array<String> content10 = new Array<>();
        content10.add("-Mechanics: More realistic cruise altitude, speeds for certain aircraft types");
        content10.add("-Mechanics: Departures may now request for shortcuts, high speed climb, or reminder to be cleared to a higher altitude");
        if (TerminalControl.full) content10.add("-TCPG: Added STAR transitions for TCPG, TCPO");
        content10.add("-Stats: Individual game stats are shown on the pause screen - most stats will start from 0");
        content10.add("-Settings: A refreshed settings page, with options grouped into different sections for easier navigation");
        content10.add("-Display: New option to set different colour styles for the radar screen - colourful or standardised");
        content10.add("-Display: New option whether to show the trail of an uncontrolled aircraft always, or only when selected");
        content10.add("-Display: New option to set the intervals between range rings, or to turn range rings off");
        content10.add("-Data tag: New option whether to show the data tag border, background always, or only when selected");
        content10.add("-Data tag: New option to set spacing between data tag rows");
        content10.add("-Sounds: New sound for alerts (yellow messages) and go-arounds");
        content10.add("-Tutorial: Important points, key words are now highlighted in different colours for easier following");
        content10.add("-Many bug fixes, including restrictions being incorrectly displayed for SIDs");
        changeLogContent.put("1.4.2006.1", content10);

        //Version 1.4.2005.2
        Array<String> content9 = new Array<>();
        content9.add("-Weather: New custom weather option in settings - set your own static weather for each airport");
        content9.add("-Mechanics: New close/open sector button to manage arrival flow to prevent over-congestion");
        content9.add("-Mechanics: New Change STAR option for arrivals; arrival will change to fly heading mode and needs to be re-cleared to waypoint on the new STAR");
        content9.add("-UI: Made SID/STAR restriction display more compact and realistic");
        content9.add("-UI: New option to turn the trajectory line off in settings");
        content9.add("-UI: Options for displaying of aircraft trail in settings");
        content9.add("-Tutorial: Added option to pause, speed up, quit tutorial");
        content9.add("-Optimisations, bug fixes");
        changeLogContent.put("1.4.2005.2", content9);

        //Version 1.4.2005.1
        Array<String> content8 = new Array<>();
        content8.add("-Mechanics: More realistic initial climb altitudes");
        content8.add("-Mechanics: Reduced descent rates for increased realism");
        content8.add("-Wake turbulence: Reduced altitude separation required");
        content8.add("-UI: Data tag overhaul - choose between the default and compact data tag styles");
        content8.add("-UI: Data tag will show changes to aircraft clearances");
        content8.add("-UI: If present, SID/STAR restrictions are displayed beside the waypoint");
        content8.add("-UI: You can now choose to hide the MVA sector altitudes to reduce clutter");
        content8.add("-UI: Choose between default or realistic ILS display");
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            content8.add("-Discord: Added Discord Rich Presence integration");
        }
        content8.add("-And of course, various bug fixes");
        changeLogContent.put("1.4.2005.1", content8);

        //Version 1.4.2004.2
        Array<String> content7 = new Array<>();
        content7.add("-Handover: You can now hand planes over to the next controller with the handover button");
        content7.add("-Acknowledge: You can acknowledge new arrivals if you do not wish to give them any new clearances");
        content7.add("-Heading UI: Heading buttons are now positioned left-right instead of up-down");
        content7.add("-Default tab: Change the default UI tab (lateral, altitude or speed) in the game settings");
        content7.add("-Conflict warnings: Mute conflict warnings by tapping on the planes involved");
        content7.add("-Communications: Go-arounds are now stated explicitly by pilots");
        if (TerminalControl.full) {
            content7.add("-TCBD: Minimum separation for TCBD has been corrected to 3nm");
        }
        content7.add("-A couple of bug fixes");
        changeLogContent.put("1.4.2004.2", content7);

        //Version 1.3.2004.1
        Array<String> content6 = new Array<>();
        content6.add("-Better fly-over waypoint display");
        content6.add("-A couple of bug fixes, optimisations");
        changeLogContent.put("1.3.2004.1", content6);

        //Version 1.3.2003.3
        Array<String> content5 = new Array<>();
        content5.add("-Just a couple of bug fixes, optimisations");
        changeLogContent.put("1.3.2003.3", content5);

        //Version 1.3.2003.2
        Array<String> content4 = new Array<>();
        content4.add("-Achievements: Unlock new achievements as you play - check out the achievements tab in the main menu");
        content4.add("-Mechanics: Improved holding pattern entry");
        content4.add("-A couple of bug fixes");
        content4.add("-HX: (Hmm, maybe doing something will unlock a new airport?)");
        changeLogContent.put("1.3.2003.2", content4);

        //Version 1.3.2003.1
        Array<String> content3 = new Array<>();
        if (TerminalControl.full) {
            content3.add("-Unlocks & upgrades: Unlock new features as you play! Check out the upgrade menu in the main menu for more information");
            content3.add("-TCTT: Changed STARs for TCTT south wind night operations");
        }
        content3.add("-Autosave: Added option to change autosave frequency");
        content3.add("-A couple of small bug fixes");
        changeLogContent.put("1.3.2003.1", content3);

        //Version 1.3.2002.2
        Array<String> content2 = new Array<>();
        if (TerminalControl.full) {
            content2.add("-Simultaneous departure: Simultaneous departures (if available) will only be active if traffic volume is high");
        }
        content2.add("-A couple of small bug fixes");
        changeLogContent.put("1.3.2002.2", content2);

        //Version 1.3.2002.1
        Array<String> content1 = new Array<>();
        content1.add("-Increased zoom: Option for increased radar zoom has been added in the default settings page");
        content1.add("-Bug fixes");
        changeLogContent.put("1.3.2002.1", content1);

        //Version 1.3.2001.4
        Array<String> content = new Array<>();
        if (TerminalControl.full) {
            content.add("-Night operations: Added customisable time settings for night operations at select airports");
        }
        content.add("-Minor bug fixes");
        changeLogContent.put("1.3.2001.4", content);
    }
}
