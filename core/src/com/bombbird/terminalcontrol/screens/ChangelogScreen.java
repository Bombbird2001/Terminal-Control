package com.bombbird.terminalcontrol.screens;

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

        for (Map.Entry<String, Array<String>> entry: changeLogContent.entrySet()) {
            Label versionLabel = new Label("Version " + entry.getKey(), labelStyle1);
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
