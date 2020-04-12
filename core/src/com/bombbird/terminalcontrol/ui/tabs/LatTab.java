package com.bombbird.terminalcontrol.ui.tabs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.aircrafts.NavState;
import com.bombbird.terminalcontrol.entities.approaches.ILS;
import com.bombbird.terminalcontrol.entities.procedures.holding.HoldingPoints;
import com.bombbird.terminalcontrol.entities.waypoints.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.ui.Ui;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.math.MathTools;

import java.util.HashMap;

public class LatTab extends Tab {
    private Label hdgBox;
    private Button hdgBoxClick;
    private TextButton hdg90add;
    private TextButton hdg90minus;
    private TextButton hdg10add;
    private TextButton hdg10minus;
    private TextButton hdg5add;
    private TextButton hdg5minus;
    private SelectBox<String> ilsBox;

    private Array<String> waypoints;
    private Array<String> ils;

    private boolean latModeChanged;
    private boolean wptChanged;
    private boolean hdgChanged;
    private boolean afterWptChanged;
    private boolean afterWptHdgChanged;
    private boolean holdWptChanged;
    private boolean ilsChanged;

    public LatTab(Ui ui) {
        super(ui);
        loadHdgElements();
        loadILSBox();
        waypoints = new Array<>();
    }

    private void loadILSBox() {
        //Selectbox for selecting ILS
        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle();
        boxStyle.font = Fonts.defaultFont20;
        boxStyle.fontColor = Color.WHITE;
        boxStyle.listStyle = listStyle;
        boxStyle.scrollStyle = paneStyle;
        boxStyle.background = Ui.lightBoxBackground;

        ils = new Array<>();
        ilsBox = new SelectBox<>(boxStyle);
        ilsBox.setPosition(0.1f * getPaneWidth(), 3240 - 1620);
        ilsBox.setSize(0.8f * getPaneWidth(), boxHeight);
        ilsBox.setAlignment(Align.center);
        ilsBox.getList().setAlignment(Align.center);
        ilsBox.setItems(ils);
        ilsBox.setVisible(false);
        ilsBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!notListening && selectedAircraft != null) {
                    getChoices();
                    updateElements();
                    compareWithAC();
                    updateElementColours();
                }
                event.handle();
            }
        });
        TerminalControl.radarScreen.uiStage.addActor(ilsBox);
    }

    private void loadHdgElements() {
        //Label for heading
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont30;
        labelStyle.fontColor = Color.WHITE;
        labelStyle.background = Ui.hdgBoxBackgroundDrawable;
        hdgBox = new Label("360", labelStyle);
        hdgBox.setPosition(1.1f / 3 * getPaneWidth(), 3240 - 2600);
        hdgBox.setSize(0.8f / 3 * getPaneWidth(), 900);
        hdgBox.setAlignment(Align.center);
        TerminalControl.radarScreen.uiStage.addActor(hdgBox);

        //Button for click spot below heading label, fixes annoying "click-through" bug
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.up = Ui.transBackgroundDrawable;
        buttonStyle.down = Ui.transBackgroundDrawable;
        hdgBoxClick = new Button(buttonStyle);
        hdgBoxClick.setPosition(1.1f / 3 * getPaneWidth(), 3240 - 2600);
        hdgBoxClick.setSize(0.8f / 3 * getPaneWidth(), 900);
        hdgBoxClick.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                event.handle();
            }
        });
        TerminalControl.radarScreen.uiStage.addActor(hdgBoxClick);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.WHITE;
        textButtonStyle.down = Ui.lightBoxBackground;
        textButtonStyle.up = Ui.lightBoxBackground;
        textButtonStyle.font = Fonts.defaultFont20;

        TextButton.TextButtonStyle textButtonStyle1 = new TextButton.TextButtonStyle();
        textButtonStyle1.fontColor = Color.WHITE;
        textButtonStyle1.down = Ui.lightestBoxBackground;
        textButtonStyle1.up = Ui.lightestBoxBackground;
        textButtonStyle1.font = Fonts.defaultFont20;

        //+90 button
        hdg90add = newButton(90, textButtonStyle);

        //-90 button
        hdg90minus = newButton(-90, textButtonStyle);

        //+10 button
        hdg10add = newButton(10, textButtonStyle1);

        //-10 button
        hdg10minus = newButton(-10, textButtonStyle1);

        //+5 button
        hdg5add = newButton(5, textButtonStyle);

        //-5 button
        hdg5minus = newButton(-5, textButtonStyle);
    }

    private TextButton newButton(final int value, TextButton.TextButtonStyle buttonStyle) {
        TextButton button = new TextButton(((value > 0) ? "+" : "") + value, buttonStyle);
        button.setSize((0.8f / 3f) * getPaneWidth(), 300);
        button.setPosition((value > 0 ? 1.9f : 0.1f) / 3 * getPaneWidth(), 0);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(value);
                event.handle();
            }
        });
        TerminalControl.radarScreen.uiStage.addActor(button);
        return button;
    }

    private void updateClearedHdg(int deltaHdg) {
        int hdgChange = deltaHdg;
        if ("After waypoint, fly heading".equals(latMode)) {
            int remainder = afterWptHdg % 5;
            if (remainder != 0) {
                if (hdgChange < 0) {
                    hdgChange += 5 - remainder;
                } else {
                    hdgChange -= remainder;
                }
            }
            afterWptHdg += hdgChange;
            afterWptHdg = MathTools.modulateHeading(afterWptHdg);
        } else {
            int remainder = clearedHdg % 5;
            if (remainder != 0) {
                if (hdgChange < 0) {
                    hdgChange += 5 - remainder;
                } else {
                    hdgChange -= remainder;
                }
            }
            clearedHdg += hdgChange;
            clearedHdg = MathTools.modulateHeading(clearedHdg);
        }
        updateElements();
        compareWithAC();
        updateElementColours();
    }

    @Override
    public void updateElements() {
        if (selectedAircraft == null) return;
        notListening = true;
        if (selectedAircraft.getSidStarIndex() >= selectedAircraft.getRoute().getWaypoints().size) {
            if ("Hold at".equals(selectedAircraft.getNavState().getDispLatMode().last())) {
                selectedAircraft.getNavState().updateLatModes(NavState.REMOVE_SIDSTAR_AFTERHDG, false); //Don't remove hold at if aircraft is gonna hold
            } else {
                selectedAircraft.getNavState().updateLatModes(NavState.REMOVE_ALL_SIDSTAR, false);
            }
        }
        settingsBox.setItems(selectedAircraft.getNavState().getLatModes());
        settingsBox.setSelected(latMode);

        ils.clear();
        ils.add("Not cleared approach");
        for (ILS approach: selectedAircraft.getAirport().getApproaches().values()) {
            String rwy = approach.getName().substring(3);
            if (selectedAircraft.getAirport().getLandingRunways().containsKey(rwy) && !selectedAircraft.getAirport().getRunways().get(rwy).isEmergencyClosed()) {
                ils.add(approach.getName());
            }
        }
        if (clearedILS == null) clearedILS = "Not cleared approach";
        if (!ils.contains(clearedILS, false)) {
            ils.clear();
            ils.add("Not cleared approach");
            if (selectedAircraft.getAirport().getRunways().get(clearedILS.substring(3)) != null) {
                ils.add(clearedILS);
            } else {
                clearedILS = "Not cleared approach";
            }
        }
        ilsBox.setItems(ils);
        ilsBox.setSelected(clearedILS);

        if (latMode.contains("waypoint") || latMode.contains("arrival") || latMode.contains("departure")) {
            //Make waypoint box visible
            if (visible) {
                valueBox.setVisible(true);
            }
            waypoints.clear();
            for (Waypoint waypoint: selectedAircraft.getRoute().getRemainingWaypoints(selectedAircraft.getSidStarIndex(), selectedAircraft.getRoute().getWaypoints().size - 1)) {
                waypoints.add(waypoint.getName());
            }
            valueBox.setItems(waypoints);
            if (latMode.contains("waypoint")) {
                valueBox.setSelected(afterWpt);
            } else {
                if (!waypoints.contains(clearedWpt, false)) {
                    clearedWpt = waypoints.first();
                }
                valueBox.setSelected(clearedWpt);
            }
            ilsBox.setVisible(false);
        } else if ("Hold at".equals(latMode) && selectedAircraft instanceof Arrival) {
            if (visible) {
                valueBox.setVisible(true);
            }
            waypoints.clear();
            if (selectedAircraft.isHolding()) {
                waypoints.add(selectedAircraft.getHoldWpt().getName());
            } else {
                Array<Waypoint> waypoints1 = selectedAircraft.getRoute().getWaypoints();
                for (int i = 0; i < waypoints1.size; i++) {
                    if (selectedAircraft.getRoute().getHoldProcedure().getHoldingPoints().containsKey(waypoints1.get(i).getName()) && selectedAircraft.getRoute().findWptIndex(waypoints1.get(i).getName()) >= selectedAircraft.getRoute().findWptIndex(selectedAircraft.getNavState().getClearedDirect().last().getName())) {
                        //Check if holding point is after current aircraft direct
                        waypoints.add(waypoints1.get(i).getName());
                    }
                }
            }
            if (!waypoints.contains(holdWpt, false)) {
                holdWpt = waypoints.first();
            }
            valueBox.setItems(waypoints);
            valueBox.setSelected(holdWpt);
            ilsBox.setVisible(false);
        } else {
            //Otherwise hide it
            valueBox.setVisible(false);

            //And set ILS box visible
            ilsBox.setVisible(selectedAircraft instanceof Arrival && (!selectedAircraft.getEmergency().isActive() || !selectedAircraft.getEmergency().isEmergency() || selectedAircraft.getEmergency().isReadyForApproach()));
        }

        //Show heading box if heading mode, otherwise hide it
        showHdgBoxes(latMode.contains("heading") && visible && (!selectedAircraft.isLocCap() || clearedILS == null || "Not cleared approach".equals(clearedILS) || !selectedAircraft.getAirport().getApproaches().get(clearedILS.substring(3)).equals(selectedAircraft.getNavState().getClearedIls().last())) && !(selectedAircraft.isLocCap() && "Not cleared approach".equals(clearedILS)));
        if (latMode.equals("After waypoint, fly heading")) {
            hdgBox.setText(Integer.toString(afterWptHdg));
        } else if (latMode.contains("heading")) {
            hdgBox.setText(Integer.toString(clearedHdg));
        }
        notListening = false;
    }

    @Override
    public void compareWithAC() {
        if (selectedAircraft == null) return;
        latModeChanged = !latMode.equals(selectedAircraft.getNavState().getDispLatMode().last());
        hdgChanged = clearedHdg != selectedAircraft.getNavState().getClearedHdg().last();
        Waypoint lastDirect = selectedAircraft.getNavState().getClearedDirect().last();
        if (clearedWpt != null && lastDirect != null) {
            wptChanged = !clearedWpt.equals(lastDirect.getName());
        }
        Waypoint lastAfterWpt = selectedAircraft.getNavState().getClearedAftWpt().last();
        if (afterWpt != null && lastAfterWpt != null) {
            afterWptChanged = !afterWpt.equals(lastAfterWpt.getName());
        }
        afterWptHdgChanged = afterWptHdg != selectedAircraft.getNavState().getClearedAftWptHdg().last();
        Waypoint lastHoldWpt = selectedAircraft.getNavState().getClearedHold().last();
        if (holdWpt != null && lastHoldWpt != null) {
            holdWptChanged = !holdWpt.equals(lastHoldWpt.getName());
        }
        ilsChanged = false;
        if (selectedAircraft instanceof Arrival) {
            if (clearedILS == null) {
                clearedILS = "Not cleared approach";
            }
            ILS lastILS = selectedAircraft.getNavState().getClearedIls().last();
            if (lastILS == null) {
                //Not cleared approach yet
                ilsChanged = !"Not cleared approach".equals(clearedILS);
            } else {
                ilsChanged = !clearedILS.equals(lastILS.getName());
            }
        }
    }

    @Override
    public void updateElementColours() {
        notListening = true;
        //Lat mode selectbox colour
        settingsBox.getStyle().fontColor = latModeChanged ? Color.YELLOW : Color.WHITE;

        //Lat mode waypoint box colour
        if ("After waypoint, fly heading".equals(latMode)) {
            valueBox.getStyle().fontColor = afterWptChanged ? Color.YELLOW : Color.WHITE;
        } else if (latMode.contains("arrival") || latMode.contains("departure")) {
            valueBox.getStyle().fontColor = wptChanged ? Color.YELLOW : Color.WHITE;
        } else if ("Hold at".equals(latMode)) {
            valueBox.getStyle().fontColor = holdWptChanged ? Color.YELLOW : Color.WHITE;
        }

        //Lat mode ILS box colour
        ilsBox.getStyle().fontColor = ilsChanged ? Color.YELLOW : Color.WHITE;

        //Lat mode hdg box colour
        if ("After waypoint, fly heading".equals(latMode)) {
            hdgBox.getStyle().fontColor = afterWptHdgChanged ? Color.YELLOW : Color.WHITE;
        } else if (latMode.contains("heading")) {
            hdgBox.getStyle().fontColor = hdgChanged ? Color.YELLOW : Color.WHITE;
        }

        if (latModeChanged) {
            tabChanged = true;
        } else {
            if ("After waypoint, fly heading".equals(latMode)) {
                tabChanged = afterWptChanged || afterWptHdgChanged;
            } else if (latMode.contains("arrival") || latMode.contains("departure")) {
                tabChanged = wptChanged;
            } else if (latMode.contains("heading")) {
                tabChanged = hdgChanged || ilsChanged;
            } else if ("Hold at".equals(latMode)) {
                tabChanged = holdWptChanged;
            }
        }
        super.updateElementColours();

        notListening = false;
    }

    @Override
    public void updateMode() {
        if (selectedAircraft == null) return;
        if (selectedAircraft.getNavState() == null) return;
        selectedAircraft.getNavState().sendLat(latMode, clearedWpt, afterWpt, holdWpt, afterWptHdg, clearedHdg, clearedILS);
    }

    @Override
    public void resetTab() {
        super.resetTab();
        updateSidStarOptions();
    }

    @Override
    public void updatePaneWidth(float paneWidth) {
        notListening = true;
        float paneSize = 0.8f * paneWidth;
        float leftMargin = 0.1f * paneWidth;
        hdgBox.setSize(paneSize / 3, 900);
        hdgBox.setX(leftMargin + paneSize / 3);
        hdgBoxClick.setSize(paneSize / 3, 900);
        hdgBoxClick.setX(leftMargin + paneSize / 3);
        ilsBox.setSize(paneSize, boxHeight);
        ilsBox.setX(leftMargin);
        hdg90add.setSize(paneSize / 3, 300);
        hdg90add.setX(leftMargin + paneSize / 1.5f);
        hdg90add.setY(3240 - 2000);
        hdg90minus.setSize(paneSize / 3, 300);
        hdg90minus.setX(leftMargin);
        hdg90minus.setY(3240 - 2000);
        hdg10add.setSize(paneSize / 3, 300);
        hdg10add.setX(leftMargin + paneSize / 1.5f);
        hdg10add.setY(3240 - 2300);
        hdg10minus.setSize(paneSize / 3, 300);
        hdg10minus.setX(leftMargin);
        hdg10minus.setY(3240 - 2300);
        hdg5add.setSize(paneSize / 3, 300);
        hdg5add.setX(leftMargin + paneSize / 1.5f);
        hdg5add.setY(3240 - 2600);
        hdg5minus.setSize(paneSize / 3, 300);
        hdg5minus.setX(leftMargin);
        hdg5minus.setY(3240 - 2600);
        super.updatePaneWidth(paneWidth);
        notListening = false;
    }

    @Override
    public void setVisibility(boolean show) {
        super.setVisibility(show);
        hdgBox.setVisible(show);
        hdgBoxClick.setVisible(show);
        hdg90add.setVisible(show);
        hdg90minus.setVisible(show);
        hdg10add.setVisible(show);
        hdg10minus.setVisible(show);
        hdg5add.setVisible(show);
        hdg5minus.setVisible(show);
        ilsBox.setVisible(show);
    }

    /** Gets the current lateral nav state of aircraft of the latest transmission, sets variable to them */
    @Override
    public void getACState() {
        latMode = selectedAircraft.getNavState().getDispLatMode().last();
        latModeChanged = false;
        clearedHdg = selectedAircraft.getNavState().getClearedHdg().last();
        hdgChanged = false;
        if (selectedAircraft.getNavState().getClearedDirect().last() != null) {
            clearedWpt = selectedAircraft.getNavState().getClearedDirect().last().getName();
        } else {
            clearedWpt = null;
        }
        wptChanged = false;
        if (selectedAircraft.getNavState().getClearedAftWpt().last() != null) {
            afterWpt = selectedAircraft.getNavState().getClearedAftWpt().last().getName();
        } else {
            afterWpt = null;
        }
        afterWptChanged = false;
        afterWptHdg = selectedAircraft.getNavState().getClearedAftWptHdg().last();
        afterWptHdgChanged = false;
        if (selectedAircraft.getNavState().getClearedHold().last() != null) {
            holdWpt = selectedAircraft.getNavState().getClearedHold().last().getName();
        } else {
            holdWpt = null;
        }
        holdWptChanged = false;
        ilsChanged = false;
        if (selectedAircraft instanceof Arrival && selectedAircraft.getNavState().getClearedIls().last() != null) {
                clearedILS = selectedAircraft.getNavState().getClearedIls().last().getName();
        } else {
            clearedILS = "Not cleared approach";
        }
    }

    @Override
    public void getChoices() {
        notListening = true;
        String prevMode = latMode;
        latMode = settingsBox.getSelected();

        if ("After waypoint, fly heading".equals(latMode)) {
            valueBox.setItems(waypoints);
            afterWpt = valueBox.getSelected();
        } else if (latMode.contains("arrival") || latMode.contains("departure")) {
            valueBox.setItems(waypoints);
            clearedWpt = valueBox.getSelected();
        } else if ("Hold at".equals(latMode)) {
            valueBox.setItems(waypoints);
            holdWpt = valueBox.getSelected();
        } else if (latMode.contains("heading")) {
            ilsBox.setItems(ils);
            clearedILS = ilsBox.getSelected();
            if (selectedAircraft != null && ("After waypoint, fly heading".equals(prevMode) || !prevMode.contains("heading"))) {
                //If previous mode is not a heading mode, set clearedHdg to current aircraft heading
                clearedHdg = (int) Math.round(selectedAircraft.getHeading());
                clearedHdg = MathTools.modulateHeading(clearedHdg);
            }
        }
        updateSidStarOptions();
        notListening = false;
    }

    private void showHdgBoxes(boolean show) {
        hdgBox.setVisible(show);
        hdgBoxClick.setVisible(show);
        hdg90add.setVisible(show);
        hdg90minus.setVisible(show);
        hdg10add.setVisible(show);
        hdg10minus.setVisible(show);
        hdg5add.setVisible(show);
        hdg5minus.setVisible(show);
    }

    private void updateSidStarOptions() {
        if (selectedAircraft == null) return;
        notListening = true;
        if (latMode.contains("arrival") && !selectedAircraft.getNavState().getAltModes().contains("Descend via STAR", false)) {
            selectedAircraft.getNavState().updateAltModes(NavState.ADD_SIDSTAR_RESTR, false);
        } else if (latMode.contains("departure") && !selectedAircraft.getNavState().getAltModes().contains("Climb via SID", false)) {
            selectedAircraft.getNavState().updateAltModes(NavState.ADD_SIDSTAR_RESTR, false);
        } else if (!"Hold at".equals(latMode) && !latMode.contains("arrival") && !latMode.contains("departure") && !latMode.contains("waypoint") && (selectedAircraft.getNavState().getAltModes().removeValue("Descend via STAR", false) || selectedAircraft.getNavState().getAltModes().removeValue("Climb via SID", false))) {
            ui.altTab.settingsBox.setSelected("Climb/descend to");
            altMode = "Climb/descend to";
        }

        if (latMode.contains("arrival") && !selectedAircraft.getNavState().getSpdModes().contains("STAR speed restrictions", false)) {
            selectedAircraft.getNavState().updateSpdModes(NavState.ADD_SIDSTAR_RESTR, false);
        } else if (latMode.contains("departure") && !selectedAircraft.getNavState().getSpdModes().contains("SID speed restrictions", false)) {
            selectedAircraft.getNavState().updateSpdModes(NavState.ADD_SIDSTAR_RESTR, false);
        } else if (!"Hold at".equals(latMode) && !latMode.contains("arrival") && !latMode.contains("departure") && !latMode.contains("waypoint") && (selectedAircraft.getNavState().getSpdModes().removeValue("STAR speed restrictions", false) || selectedAircraft.getNavState().getSpdModes().removeValue("SID speed restrictions", false))) {
            ui.spdTab.settingsBox.setSelected("No speed restrictions");
            spdMode = "No speed restrictions";
        }

        if (selectedAircraft instanceof Arrival && !"Hold at".equals(selectedAircraft.getNavState().getDispLatMode().last()) && selectedAircraft.getNavState().getClearedDirect().last() != null) {
            boolean found = false;
            HashMap<String, HoldingPoints> waypoints = selectedAircraft.getRoute().getHoldProcedure().getHoldingPoints();
            for (String holdingPoint: waypoints.keySet()) {
                if (selectedAircraft.getRoute().findWptIndex(holdingPoint) >= selectedAircraft.getRoute().findWptIndex(selectedAircraft.getNavState().getClearedDirect().last().getName())) {
                    //Check if holding point is after current aircraft direct
                    found = true;
                    break;
                }
            }

            if (!found) {
                selectedAircraft.getNavState().updateLatModes(NavState.REMOVE_HOLD_ONLY, false);
            }
        }

        ui.updateElements();
        notListening = false;
    }

    public boolean isLatModeChanged() {
        return latModeChanged;
    }

    public boolean isWptChanged() {
        return wptChanged;
    }

    public boolean isHdgChanged() {
        return hdgChanged;
    }

    public boolean isAfterWptChanged() {
        return afterWptChanged;
    }

    public boolean isAfterWptHdgChanged() {
        return afterWptHdgChanged;
    }

    public boolean isHoldWptChanged() {
        return holdWptChanged;
    }
}