package com.bombbird.terminalcontrol.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.entities.ILS;
import com.bombbird.terminalcontrol.entities.Waypoint;
import com.bombbird.terminalcontrol.entities.aircrafts.Arrival;
import com.bombbird.terminalcontrol.entities.aircrafts.Departure;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;

public class LatTab extends Tab {
    private Label hdgBox;
    private TextButton hdg100add;
    private TextButton hdg100minus;
    private TextButton hdg10add;
    private TextButton hdg10minus;
    private TextButton hdg5add;
    private TextButton hdg5minus;
    private SelectBox<String> ilsBox;

    private Array<String> waypoints;
    private Array<String> holdingWaypoints;
    private Array<String> ils;

    private boolean latModeChanged;
    private boolean wptChanged;
    private boolean hdgChanged;
    private boolean afterWptChanged;
    private boolean afterWptHdgChanged;
    private boolean ilsChanged;

    public LatTab(Ui ui) {
        super(ui);
        loadHdgElements();
        loadILSBox();
        waypoints = new Array<String>();
        holdingWaypoints = new Array<String>();
    }

    private void loadILSBox() {
        //Selectbox for selecting ILS
        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle();
        boxStyle.font = Fonts.defaultFont20;
        boxStyle.fontColor = Color.WHITE;
        boxStyle.listStyle = listStyle;
        boxStyle.scrollStyle = paneStyle;
        boxStyle.background = Ui.lightBoxBackground;

        ils = new Array<String>();
        ilsBox = new SelectBox<String>(boxStyle);
        ilsBox.setPosition(0.1f * getPaneWidth(), 3240 - 1570);
        ilsBox.setSize(0.8f * getPaneWidth(), 270);
        ilsBox.setAlignment(Align.center);
        ilsBox.getList().setAlignment(Align.center);
        ilsBox.setItems(ils);
        ilsBox.setVisible(false);
        ilsBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!notListening) {
                    getChoices();
                    updateElements();
                    compareWithAC();
                    updateElementColours();
                }
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(ilsBox);
    }

    private void loadHdgElements() {
        //Label for heading
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont30;
        labelStyle.fontColor = Color.WHITE;
        labelStyle.background = Ui.hdgBoxBackgroundDrawable;
        hdgBox = new Label("360", labelStyle);
        hdgBox.setPosition(0.1f * getPaneWidth(), 3240 - 2370);
        hdgBox.setSize(0.8f * getPaneWidth(), 270);
        hdgBox.setAlignment(Align.center);
        RadarScreen.uiStage.addActor(hdgBox);

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

        //+100 button
        hdg100add = newButton(100, textButtonStyle);

        //-100 button
        hdg100minus = newButton(-100, textButtonStyle);

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
        TextButton button = new TextButton((value > 0) ? "+" : "-", buttonStyle);
        button.setSize((0.8f / 3f) * getPaneWidth(), 200);
        button.setPosition((0.1f + 0.8f / 1.5f) * getPaneWidth(), (value > 0) ? (3240 - 2100) : (3240 - 2570));
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(value);
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(button);
        return button;
    }

    private void updateClearedHdg(int deltaHdg) {
        if (latMode.equals("After waypoint, fly heading")) {
            int remainder = afterWptHdg % 5;
            if (remainder != 0) {
                if (deltaHdg < 0) {
                    deltaHdg += 5 - remainder;
                } else {
                    deltaHdg -= remainder;
                }
            }
            afterWptHdg += deltaHdg;
            if (afterWptHdg <= 0) {
                afterWptHdg += 360;
            } else if (afterWptHdg > 360) {
                afterWptHdg -= 360;
            }
        } else {
            int remainder = clearedHdg % 5;
            if (remainder != 0) {
                if (deltaHdg < 0) {
                    deltaHdg += 5 - remainder;
                } else {
                    deltaHdg -= remainder;
                }
            }
            clearedHdg += deltaHdg;
            if (clearedHdg <= 0) {
                clearedHdg += 360;
            } else if (clearedHdg > 360) {
                clearedHdg -= 360;
            }
        }
        updateElements();
        compareWithAC();
        updateElementColours();
    }

    @Override
    public void updateElements() {
        notListening = true;
        settingsBox.setItems(selectedAircraft.getNavState().getLatModes());
        settingsBox.setSelected(latMode);

        if (latMode.contains("waypoint") || latMode.contains("arrival") || latMode.contains("departure") || latMode.equals("Hold at")) {
            //Make waypoint box visible
            if (visible) {
                valueBox.setVisible(true);
            }
            waypoints.clear();
            for (Waypoint waypoint: selectedAircraft.getRemainingWaypoints()) {
                waypoints.add(waypoint.getName());
            }
            valueBox.setItems(waypoints);
            if (latMode.contains("waypoint")) {
                valueBox.setSelected(afterWpt);
            } else {
                valueBox.setSelected(clearedWpt);
            }
            ilsBox.setVisible(false);
        } else {
            //Otherwise hide it
            valueBox.setVisible(false);

            //And set ILS box visible
            ils.clear();
            ils.add("Not cleared approach");
            for (ILS approach: selectedAircraft.getAirport().getApproaches().values()) {
                if (selectedAircraft.getAirport().getLandingRunways().keySet().contains(approach.getName().substring(3))) {
                    ils.add(approach.getName());
                }
            }
            ilsBox.setItems(ils);
            ilsBox.setSelected(clearedILS);
            ilsBox.setVisible(selectedAircraft instanceof Arrival);
        }

        //Show heading box if heading mode, otherwise hide it
        showHdgBoxes(latMode.contains("heading") && visible);
        if (latMode.equals("After waypoint, fly heading")) {
            hdgBox.setText(Integer.toString(afterWptHdg));
        } else if (latMode.contains("heading")) {
            hdgBox.setText(Integer.toString(clearedHdg));
        }
        notListening = false;
    }

    @Override
    public void compareWithAC() {
        latModeChanged = !latMode.equals(selectedAircraft.getNavState().getLatMode());
        hdgChanged = clearedHdg != selectedAircraft.getClearedHeading();
        if (clearedWpt != null && selectedAircraft.getDirect() != null) {
            wptChanged = !clearedWpt.equals(selectedAircraft.getDirect().getName());
        }
        if (afterWpt != null && selectedAircraft.getAfterWaypoint() != null) {
            afterWptChanged = !afterWpt.equals(selectedAircraft.getAfterWaypoint().getName());
        }
        afterWptHdgChanged = afterWptHdg != selectedAircraft.getAfterWptHdg();
        ilsChanged = false;
        if (selectedAircraft instanceof Arrival) {
            if (clearedILS == null) {
                clearedILS = "Not cleared approach";
            }
            if (selectedAircraft.getIls() == null) {
                //Not cleared approach yet
                ilsChanged = !clearedILS.equals("Not cleared approach");
            } else {
                ilsChanged = !clearedILS.equals(selectedAircraft.getIls().getName());
            }
        }
    }

    @Override
    public void updateElementColours() {
        notListening = true;
        //Lat mode selectbox colour
        settingsBox.getStyle().fontColor = latModeChanged ? Color.YELLOW : Color.WHITE;

        //Lat mode waypoint box colour
        if (latMode.equals("After waypoint, fly heading")) {
            valueBox.getStyle().fontColor = afterWptChanged ? Color.YELLOW : Color.WHITE;
        } else if (latMode.contains("arrival") || latMode.contains("departure")) {
            valueBox.getStyle().fontColor = wptChanged ? Color.YELLOW : Color.WHITE;
        }

        //Lat mode ILS box colour
        ilsBox.getStyle().fontColor = ilsChanged ? Color.YELLOW : Color.WHITE;

        //Lat mode hdg box colour
        if (latMode.equals("After waypoint, fly heading")) {
            hdgBox.getStyle().fontColor = afterWptHdgChanged ? Color.YELLOW : Color.WHITE;
        } else if (latMode.contains("heading")) {
            hdgBox.getStyle().fontColor = hdgChanged ? Color.YELLOW : Color.WHITE;
        }

        if (latModeChanged) {
            tabChanged = true;
        } else {
            if (latMode.equals("After waypoint, fly heading")) {
                tabChanged = afterWptChanged || afterWptHdgChanged;
            } else if (latMode.contains("arrival") || latMode.contains("departure")) {
                tabChanged = wptChanged;
            } else if (latMode.contains("heading")) {
                tabChanged = hdgChanged || ilsChanged;
            }
        }
        super.updateElementColours();

        notListening = false;
    }

    @Override
    public void updateMode() {
        if (latMode.contains(selectedAircraft.getSidStar().getName())) {
            if (selectedAircraft instanceof Arrival || (selectedAircraft instanceof Departure && ((Departure) selectedAircraft).isSidSet())) {
                selectedAircraft.setLatMode("sidstar");
                selectedAircraft.setDirect(RadarScreen.waypoints.get(clearedWpt));
                selectedAircraft.updateSelectedWaypoints(null);
                selectedAircraft.setSidStarIndex(selectedAircraft.getSidStar().findWptIndex(selectedAircraft.getDirect().getName()));
                if (!selectedAircraft.getNavState().getLatModes().contains("After waypoint, fly heading", false)) {
                    selectedAircraft.getNavState().getLatModes().add("After waypoint, fly heading");
                }
                if (!selectedAircraft.getNavState().getLatModes().contains("Hold at", false)) {
                    selectedAircraft.getNavState().getLatModes().add("Hold at");
                }
            }
            if (selectedAircraft instanceof Arrival) {
                selectedAircraft.setIls(null);
            }
        } else if (latMode.equals("After waypoint, fly heading")) {
            selectedAircraft.setLatMode("sidstar");
            selectedAircraft.setAfterWaypoint(RadarScreen.waypoints.get(afterWpt));
            selectedAircraft.setAfterWptHdg(afterWptHdg);
            if (selectedAircraft instanceof Arrival) {
                selectedAircraft.setIls(null);
            }
        } else if (latMode.equals("Hold at")) {
            System.out.println("Hold at");
            if (selectedAircraft instanceof Arrival) {
                selectedAircraft.setIls(null);
            }
        } else if (latMode.equals("Fly heading") || latMode.equals("Turn left heading") || latMode.equals("Turn right heading")) {
            selectedAircraft.setLatMode("vector");
            selectedAircraft.setClearedHeading(clearedHdg);
            if (selectedAircraft instanceof Arrival) {
                selectedAircraft.setIls(selectedAircraft.getAirport().getApproaches().get(clearedILS.substring(3)));
            }
        } else {
            Gdx.app.log("Invalid lat mode", "Invalid latmode " + latMode + " set!");
        }
        selectedAircraft.getNavState().setLatMode(latMode);
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
        hdgBox.setSize(paneSize, 270);
        hdgBox.setX(leftMargin);
        ilsBox.setSize(paneSize, 270);
        ilsBox.setX(leftMargin);
        hdg100add.setSize(paneSize / 3, 200);
        hdg100add.setX(leftMargin);
        hdg100minus.setSize(paneSize / 3, 200);
        hdg100minus.setX(leftMargin);
        hdg10add.setSize(paneSize / 3, 200);
        hdg10add.setX(leftMargin + paneSize / 3);
        hdg10minus.setSize(paneSize / 3, 200);
        hdg10minus.setX(leftMargin + paneSize / 3);
        hdg5add.setSize(paneSize / 3, 200);
        hdg5add.setX(leftMargin + paneSize / 1.5f);
        hdg5minus.setSize(paneSize / 3, 200);
        hdg5minus.setX(leftMargin + paneSize / 1.5f);
        super.updatePaneWidth(paneWidth);
        notListening = false;
    }

    @Override
    public void setVisibility(boolean show) {
        super.setVisibility(show);
        hdgBox.setVisible(show);
        hdg100add.setVisible(show);
        hdg100minus.setVisible(show);
        hdg10add.setVisible(show);
        hdg10minus.setVisible(show);
        hdg5add.setVisible(show);
        hdg5minus.setVisible(show);
        ilsBox.setVisible(show);
    }

    @Override
    public void getACState() {
        latMode = selectedAircraft.getNavState().getLatMode();
        latModeChanged = false;
        clearedHdg = selectedAircraft.getClearedHeading();
        hdgChanged = false;
        if (selectedAircraft.getDirect() != null) {
            clearedWpt = selectedAircraft.getDirect().getName();
        } else {
            clearedWpt = null;
        }
        wptChanged = false;
        if (selectedAircraft.getAfterWaypoint() != null) {
            afterWpt = selectedAircraft.getAfterWaypoint().getName();
        } else {
            afterWpt = null;
        }
        afterWptChanged = false;
        afterWptHdg = selectedAircraft.getAfterWptHdg();
        afterWptHdgChanged = false;
        ilsChanged = false;
        if (selectedAircraft instanceof Arrival) {
            if (selectedAircraft.getIls() != null) {
                clearedILS = selectedAircraft.getIls().getName();
            } else {
                clearedILS = "Not cleared approach";
            }
        }
    }

    @Override
    public void getChoices() {
        notListening = true;
        latMode = settingsBox.getSelected();

        if (latMode.contains("waypoint")) {
            valueBox.setItems(waypoints);
            afterWpt = valueBox.getSelected();
        } else if (latMode.contains("arrival") || latMode.contains("departure")) {
            valueBox.setItems(waypoints);
            clearedWpt = valueBox.getSelected();
        } else if (latMode.equals("Hold at")) {
            valueBox.setItems(holdingWaypoints);
        } else if (latMode.contains("heading")) {
            ilsBox.setItems(ils);
            clearedILS = ilsBox.getSelected();
        }
        updateSidStarOptions();
        notListening = false;
    }

    private void showHdgBoxes(boolean show) {
        hdgBox.setVisible(show);
        hdg100add.setVisible(show);
        hdg100minus.setVisible(show);
        hdg10add.setVisible(show);
        hdg10minus.setVisible(show);
        hdg5add.setVisible(show);
        hdg5minus.setVisible(show);
    }

    private void updateSidStarOptions() {
        notListening = true;
        if (latMode.contains("arrival") && !selectedAircraft.getNavState().getAltModes().contains("Descend via STAR", false)) {
            selectedAircraft.getNavState().getAltModes().add("Descend via STAR");
        } else if (latMode.contains("departure") && !selectedAircraft.getNavState().getAltModes().contains("Climb via SID", false)) {
            selectedAircraft.getNavState().getAltModes().add("Climb via SID");
        } else if (!latMode.contains("arrival") && !latMode.contains("departure") && !latMode.contains("waypoint")) {
            if (!selectedAircraft.getNavState().getAltModes().removeValue("Descend via STAR", false)) {
                selectedAircraft.getNavState().getAltModes().removeValue("Climb via SID", false);
            }
        }

        if (latMode.contains("arrival") && !selectedAircraft.getNavState().getSpdModes().contains("STAR speed restrictions", false)) {
            selectedAircraft.getNavState().getSpdModes().add("STAR speed restrictions");
        } else if (latMode.contains("departure") && !selectedAircraft.getNavState().getSpdModes().contains("SID speed restrictions", false)) {
            selectedAircraft.getNavState().getSpdModes().add("SID speed restrictions");
        } else if (!latMode.contains("arrival") && !latMode.contains("departure")) {
            if (!selectedAircraft.getNavState().getSpdModes().removeValue("STAR speed restrictions", false)) {
                selectedAircraft.getNavState().getSpdModes().removeValue("SID speed restrictions", false);
            }
        }
        ui.updateElements();
        notListening = false;
    }
}