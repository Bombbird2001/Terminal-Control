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
import com.bombbird.terminalcontrol.entities.sidstar.RandomSTAR;
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

    private final Array<String> waypoints;
    private Array<String> ils;

    private boolean latModeChanged;
    private boolean wptChanged;
    private boolean hdgChanged;
    private boolean afterWptChanged;
    private boolean afterWptHdgChanged;
    private boolean holdWptChanged;
    private boolean ilsChanged;
    private boolean starChanged;

    public LatTab(Ui ui) {
        super(ui);
        loadHdgElements();
        loadILSBox();
        waypoints = new Array<>();
    }

    public void loadModes() {
        modeButtons.addButton(NavState.SID_STAR, "SID/STAR");
        modeButtons.addButton(NavState.AFTER_WAYPOINT_FLY_HEADING, "After wpt, hdg");
        modeButtons.addButton(NavState.FLY_HEADING, "Vectors");
        modeButtons.addButton(NavState.HOLD_AT, "Hold");
        modeButtons.addButton(NavState.CHANGE_STAR, "Change STAR");
    }

    public void updateModeButtons() {
        modeButtons.changeButtonText(NavState.SID_STAR, selectedAircraft.getSidStar().getName() + (selectedAircraft instanceof Arrival ? " STAR" : " SID"));
        modeButtons.setButtonColour(false);
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
        addActor(ilsBox, 0.1f, 0.8f, 3240 - 1620, boxHeight);
    }

    private void loadHdgElements() {
        //Label for heading
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont30;
        labelStyle.fontColor = Color.WHITE;
        labelStyle.background = Ui.hdgBoxBackgroundDrawable;
        hdgBox = new Label("360", labelStyle);
        hdgBox.setAlignment(Align.center);
        addActor(hdgBox, 1.1f / 3, 0.8f / 3, 3240 - 2600, 900);

        //Button for click spot below heading label, fixes annoying "click-through" bug
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.up = Ui.transBackgroundDrawable;
        buttonStyle.down = Ui.transBackgroundDrawable;
        hdgBoxClick = new Button(buttonStyle);
        hdgBoxClick.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                event.handle();
            }
        });
        addActor(hdgBoxClick, 1.1f / 3, 0.8f / 3, 3240 - 2600, 900);

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
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(value);
                event.handle();
            }
        });
        int offset = 1700;
        if (value == -90 || value == 90) {
            offset = 2000;
        } else if (value == -10 || value == 10) {
            offset = 2300;
        } else if (value == -5 || value == 5) {
            offset = 2600;
        }
        addActor(button, (value > 0 ? 1.9f : 0.3f) / 3, 0.8f / 3, 3240 - offset, 300);
        TerminalControl.radarScreen.uiStage.addActor(button);
        return button;
    }

    private void updateClearedHdg(int deltaHdg) {
        int hdgChange = deltaHdg;
        if (latMode == NavState.AFTER_WAYPOINT_FLY_HEADING) {
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
        modeButtons.updateButtonActivity(selectedAircraft.getNavState().getLatModes());
        if (selectedAircraft.getSidStarIndex() >= selectedAircraft.getRoute().getWaypoints().size) {
            if (selectedAircraft.getNavState().getDispLatMode().last() == NavState.HOLD_AT) {
                selectedAircraft.getNavState().updateLatModes(NavState.REMOVE_SIDSTAR_AFTERHDG, false); //Don't remove hold at if aircraft is gonna hold
            } else {
                selectedAircraft.getNavState().updateLatModes(NavState.REMOVE_ALL_SIDSTAR, false);
            }
        }

        ils.clear();
        ils.add(Ui.NOT_CLEARED_APCH);
        for (ILS approach: selectedAircraft.getAirport().getApproaches().values()) {
            String rwy = approach.getName().substring(3);
            if (selectedAircraft.getAirport().getLandingRunways().containsKey(rwy) && !selectedAircraft.getAirport().getRunways().get(rwy).isEmergencyClosed()) {
                ils.add(approach.getName());
            }
        }
        if (clearedILS == null) clearedILS = Ui.NOT_CLEARED_APCH;
        if (!ils.contains(clearedILS, false)) {
            ils.clear();
            ils.add(Ui.NOT_CLEARED_APCH);
            if (selectedAircraft.getAirport().getRunways().get(clearedILS.substring(3)) != null) {
                ils.add(clearedILS);
            } else {
                clearedILS = Ui.NOT_CLEARED_APCH;
            }
        }
        ilsBox.setItems(ils);
        ilsBox.setSelected(clearedILS);

        if (latMode == NavState.AFTER_WAYPOINT_FLY_HEADING || latMode == NavState.SID_STAR) {
            //Make waypoint box visible
            if (visible) {
                valueBox.setVisible(true);
            }
            waypoints.clear();
            int startIndex = selectedAircraft.getSidStarIndex();
            Waypoint latestDirect = selectedAircraft.getNavState().getClearedDirect().last();
            if (latestDirect != null && selectedAircraft.getRoute().getWaypoints().contains(latestDirect, false)) startIndex = selectedAircraft.getRoute().findWptIndex(latestDirect.getName());
            for (Waypoint waypoint: selectedAircraft.getRoute().getRemainingWaypoints(startIndex, selectedAircraft.getRoute().getWaypoints().size - 1)) {
                waypoints.add(waypoint.getName());
            }
            valueBox.setItems(waypoints);
            if (latMode == NavState.AFTER_WAYPOINT_FLY_HEADING) {
                valueBox.setSelected(afterWpt);
            } else {
                if (!waypoints.contains(clearedWpt, false)) {
                    clearedWpt = waypoints.first();
                }
                valueBox.setSelected(clearedWpt);
            }
            ilsBox.setVisible(false);
        } else if (latMode == NavState.HOLD_AT && selectedAircraft instanceof Arrival) {
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
                if (waypoints.isEmpty()) {
                    selectedAircraft.getNavState().updateLatModes(NavState.REMOVE_HOLD_ONLY, true);
                    holdWpt = null;
                } else {
                    holdWpt = waypoints.first();
                }
            }
            valueBox.setItems(waypoints);
            valueBox.setSelected(holdWpt);
            ilsBox.setVisible(false);
        } else if (latMode == NavState.CHANGE_STAR && selectedAircraft instanceof Arrival) {
            if (visible) {
                valueBox.setVisible(true);
            }
            if (newStar == null) {
                valueBox.setSelectedIndex(0);
            } else {
                valueBox.setSelected(newStar);
            }
            ilsBox.setVisible(false);
        } else {
            //Otherwise hide it
            valueBox.setVisible(false);

            //And set ILS box visible
            ilsBox.setVisible(selectedAircraft instanceof Arrival && (!selectedAircraft.getEmergency().isActive() || !selectedAircraft.getEmergency().isEmergency() || selectedAircraft.getEmergency().isReadyForApproach()));
        }

        //Show heading box if heading mode, otherwise hide it
        showHdgBoxes((latMode == NavState.AFTER_WAYPOINT_FLY_HEADING || latMode == NavState.FLY_HEADING || latMode == NavState.TURN_LEFT || latMode == NavState.TURN_RIGHT) && visible && (!selectedAircraft.isLocCap() || clearedILS == null || Ui.NOT_CLEARED_APCH.equals(clearedILS) || !selectedAircraft.getAirport().getApproaches().get(clearedILS.substring(3)).equals(selectedAircraft.getNavState().getClearedIls().last())) && !(selectedAircraft.isLocCap() && Ui.NOT_CLEARED_APCH.equals(clearedILS)));
        if (latMode == NavState.AFTER_WAYPOINT_FLY_HEADING) {
            hdgBox.setText(Integer.toString(afterWptHdg));
        } else if (latMode == NavState.FLY_HEADING || latMode == NavState.TURN_LEFT || latMode == NavState.TURN_RIGHT) {
            hdgBox.setText(Integer.toString(clearedHdg));
        }
        notListening = false;
    }

    @Override
    public void compareWithAC() {
        if (selectedAircraft == null) return;
        latModeChanged = latMode != selectedAircraft.getNavState().getDispLatMode().last() && latMode != NavState.CHANGE_STAR;
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
        String lastNewStar = selectedAircraft.getNavState().getClearedNewStar().last();
        starChanged = ((lastNewStar == null && newStar != null) || (lastNewStar != null && !lastNewStar.equals(newStar))) && !newStar.equals(selectedAircraft.getSidStar().getName() + " arrival");
        ilsChanged = false;
        if (selectedAircraft instanceof Arrival) {
            if (clearedILS == null) {
                clearedILS = Ui.NOT_CLEARED_APCH;
            }
            ILS lastILS = selectedAircraft.getNavState().getClearedIls().last();
            if (lastILS == null) {
                //Not cleared approach yet
                ilsChanged = !Ui.NOT_CLEARED_APCH.equals(clearedILS);
            } else {
                ilsChanged = !clearedILS.equals(lastILS.getName());
            }
        }

        if (latModeChanged) {
            tabChanged = true;
        } else {
            if (latMode == NavState.AFTER_WAYPOINT_FLY_HEADING) {
                tabChanged = afterWptChanged || afterWptHdgChanged;
            } else if (latMode == NavState.SID_STAR) {
                tabChanged = wptChanged;
            } else if (latMode == NavState.FLY_HEADING || latMode == NavState.TURN_LEFT || latMode == NavState.TURN_RIGHT) {
                tabChanged = hdgChanged || ilsChanged;
            } else if (latMode == NavState.HOLD_AT) {
                tabChanged = holdWptChanged;
            } else if (latMode == NavState.CHANGE_STAR) {
                tabChanged = starChanged;
            }
        }
    }

    @Override
    public void updateElementColours() {
        notListening = true;
        //Lat mode selectbox colour
        modeButtons.setButtonColour(latModeChanged);

        //Lat mode waypoint box colour
        if (latMode == NavState.AFTER_WAYPOINT_FLY_HEADING) {
            valueBox.getStyle().fontColor = afterWptChanged ? Color.YELLOW : Color.WHITE;
        } else if (latMode == NavState.SID_STAR) {
            valueBox.getStyle().fontColor = wptChanged ? Color.YELLOW : Color.WHITE;
        } else if (latMode == NavState.HOLD_AT) {
            valueBox.getStyle().fontColor = holdWptChanged ? Color.YELLOW : Color.WHITE;
        } else if (latMode == NavState.CHANGE_STAR) {
            valueBox.getStyle().fontColor = starChanged ? Color.YELLOW : Color.WHITE;
        }

        //Lat mode ILS box colour
        ilsBox.getStyle().fontColor = ilsChanged ? Color.YELLOW : Color.WHITE;

        //Lat mode hdg box colour
        if (latMode == NavState.AFTER_WAYPOINT_FLY_HEADING) {
            hdgBox.getStyle().fontColor = afterWptHdgChanged ? Color.YELLOW : Color.WHITE;
        } else if (latMode == NavState.FLY_HEADING || latMode == NavState.TURN_LEFT || latMode == NavState.TURN_RIGHT) {
            hdgBox.getStyle().fontColor = hdgChanged ? Color.YELLOW : Color.WHITE;
        }

        super.updateElementColours();

        notListening = false;
    }

    @Override
    public void updateMode() {
        if (selectedAircraft == null) return;
        if (selectedAircraft.getNavState() == null) return;
        //Check if changing the waypoint leads to change in max speed restrictions
        if (spdMode == NavState.SID_STAR_RESTR && wptChanged) {
            int maxSpd = selectedAircraft.getRoute().getWptMaxSpd(clearedWpt);
            if (maxSpd > -1 && SpdTab.clearedSpd > maxSpd) {
                SpdTab.clearedSpd = maxSpd;
                ui.spdTab.updateElements();
            }
        }
        selectedAircraft.getNavState().sendLat(latMode, clearedWpt, afterWpt, holdWpt, afterWptHdg, clearedHdg, clearedILS, newStar);
    }

    @Override
    public void resetTab() {
        super.resetTab();
        updateSidStarOptions();
    }

    /** Gets the current lateral nav state of aircraft of the latest transmission, sets variable to them */
    @Override
    public void getACState() {
        latMode = selectedAircraft.getNavState().getDispLatMode().last();
        modeButtons.setMode(latMode);
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
        starChanged = false;
        if (selectedAircraft instanceof Arrival) {
            newStar = selectedAircraft.getNavState().getClearedNewStar().last();
        }
    }

    @Override
    public void getChoices() {
        notListening = true;
        int prevMode = latMode;
        latMode = modeButtons.getMode();

        if (latMode == NavState.AFTER_WAYPOINT_FLY_HEADING) {
            valueBox.setItems(waypoints);
            afterWpt = valueBox.getSelected();
        } else if (latMode == NavState.SID_STAR) {
            valueBox.setItems(waypoints);
            clearedWpt = valueBox.getSelected();
        } else if (latMode == NavState.HOLD_AT) {
            valueBox.setItems(waypoints);
            holdWpt = valueBox.getSelected();
        } else if (latMode == NavState.FLY_HEADING || latMode == NavState.TURN_LEFT || latMode == NavState.TURN_RIGHT) {
            ilsBox.setItems(ils);
            clearedILS = ilsBox.getSelected();
            if (selectedAircraft != null && !(prevMode == NavState.FLY_HEADING || prevMode == NavState.TURN_LEFT || prevMode == NavState.TURN_RIGHT)) {
                //If previous mode is not a heading mode, set clearedHdg to current aircraft heading
                clearedHdg = (int) Math.round(selectedAircraft.getHeading());
                clearedHdg = MathTools.modulateHeading(clearedHdg);
            }
        } else if (latMode == NavState.CHANGE_STAR && selectedAircraft instanceof Arrival) {
            Array<String> starsAvailable = RandomSTAR.getAllPossibleSTARnames(selectedAircraft.getAirport());
            starsAvailable.removeValue(selectedAircraft.getSidStar().getName() + " arrival", false);
            starsAvailable.insert(0, selectedAircraft.getSidStar().getName() + " arrival");
            valueBox.setItems(starsAvailable);
            newStar = valueBox.getSelected();
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
        if (latMode == NavState.SID_STAR && !selectedAircraft.getNavState().getAltModes().contains("Descend via STAR", false)) {
            selectedAircraft.getNavState().updateAltModes(NavState.ADD_SIDSTAR_RESTR, false);
        } else if (latMode == NavState.SID_STAR && !selectedAircraft.getNavState().getAltModes().contains("Climb via SID", false)) {
            selectedAircraft.getNavState().updateAltModes(NavState.ADD_SIDSTAR_RESTR, false);
        } else if (latMode != NavState.HOLD_AT && latMode != NavState.SID_STAR && latMode != NavState.AFTER_WAYPOINT_FLY_HEADING && (selectedAircraft.getNavState().getAltModes().removeValue("Descend via STAR", false) || selectedAircraft.getNavState().getAltModes().removeValue("Climb via SID", false))) {
            ui.altTab.modeButtons.setMode(NavState.NO_RESTR);
            altMode = NavState.NO_RESTR;
        }

        if (latMode == NavState.SID_STAR && !selectedAircraft.getNavState().getSpdModes().contains("STAR speed restrictions", false)) {
            selectedAircraft.getNavState().updateSpdModes(NavState.ADD_SIDSTAR_RESTR, false);
        } else if (latMode == NavState.SID_STAR && !selectedAircraft.getNavState().getSpdModes().contains("SID speed restrictions", false)) {
            selectedAircraft.getNavState().updateSpdModes(NavState.ADD_SIDSTAR_RESTR, false);
        } else if (latMode != NavState.HOLD_AT && latMode != NavState.SID_STAR && latMode != NavState.AFTER_WAYPOINT_FLY_HEADING && (selectedAircraft.getNavState().getSpdModes().removeValue("STAR speed restrictions", false) || selectedAircraft.getNavState().getSpdModes().removeValue("SID speed restrictions", false))) {
            ui.spdTab.modeButtons.setMode(NavState.NO_RESTR);
            spdMode = NavState.NO_RESTR;
        }

        if (selectedAircraft instanceof Arrival && selectedAircraft.getNavState().getDispLatMode().last() != NavState.HOLD_AT && selectedAircraft.getNavState().getClearedDirect().last() != null) {
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
        ui.compareWithAC();
        ui.updateElementColours();
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

    public boolean isIlsChanged() {
        return ilsChanged;
    }

    public boolean isStarChanged() {
        return starChanged;
    }
}