package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.Waypoint;
import com.bombbird.atcsim.entities.aircrafts.Aircraft;
import org.apache.commons.lang3.StringUtils;

public class Ui implements Disposable {
    private Texture hdgBoxBackground = new Texture(Gdx.files.internal("game/ui/BoxBackground.png"));
    private Texture paneTexture = new Texture(Gdx.files.internal("game/ui/UI Pane_Normal.png"));
    private Texture boxBackground = new Texture(Gdx.files.internal("game/ui/SelectBoxBackground.png"));
    private Image paneImage;
    private SpriteDrawable selectBoxBackgroundDrawable = new SpriteDrawable(new Sprite(boxBackground));
    private SpriteDrawable hdgBoxBackgroundDrawable = new SpriteDrawable(new Sprite(hdgBoxBackground));

    private SelectBox<String> settingsBox;
    private SelectBox<String> valueBox;
    private Label hdgBox;
    private TextButton hdg100add;
    private TextButton hdg100minus;
    private TextButton hdg10add;
    private TextButton hdg10minus;
    private TextButton hdg5add;
    private TextButton hdg5minus;

    private TextButton cfmChange;
    private TextButton resetTab;
    private TextButton resetAll;

    //Array for METAR info on default pane
    private Array<Label> metarInfos;

    //Instructions panel info
    private Aircraft selectedAircraft;

    private int tab;
    private String latMode;
    private int clearedHdg;
    private String clearedWpt;
    private Array<String> waypoints;
    private Array<String> holdingWaypoints;
    private boolean latModeChanged;
    private boolean wptChanged;
    private boolean hdgChanged;

    private String altMode;
    private int clearedAlt;
    private boolean altModeChanged;
    private boolean altChanged;

    private String spdMode;
    private int clearedSpd;
    private boolean spdModeChanged;
    private boolean spdChanged;

    public Ui() {
        tab = 0;
        loadNormalPane();
        loadSelectBox();
        loadButtons();
    }

    public void updateMetar() {
        for (Label label: metarInfos) {
            //Get airport: ICAO code is first 4 letters of label's text
            Airport airport = RadarScreen.airports.get(label.getText().toString().substring(0, 4));
            String[] metarText = new String[5];
            metarText[0] = airport.getIcao();
            //Wind: Speed + direction
            if (airport.getWinds()[0] != 0) {
                metarText[1] = "Winds: " + Integer.toString(airport.getWinds()[0]) + "@" + Integer.toString(airport.getWinds()[1]) + "kts";
            } else {
                metarText[1] = "Winds: VRB@" + Integer.toString(airport.getWinds()[1]) + "kts";
            }
            //Gusts
            if (airport.getGusts() != -1) {
                metarText[2] = "Gusting to: " + Integer.toString(airport.getGusts()) + "kts";
            } else {
                metarText[2] = "Gusting to: None";
            }
            //Visbility
            metarText[3] = "Visibility: " + Integer.toString(airport.getVisibility()) + " metres";
            //Windshear
            metarText[4] = "Windshear: " + airport.getWindshear();
            label.setText(StringUtils.join(metarText, "\n"));
        }
    }

    private void loadNormalPane() {
        //Loads default pane shown when no aircraft selected
        paneImage = new Image(paneTexture);
        paneImage.setPosition(0, 0);
        paneImage.setSize(1080 * (float)AtcSim.WIDTH / AtcSim.HEIGHT, 3240);
        RadarScreen.uiStage.addActor(paneImage);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        int index = 0;
        metarInfos = new Array<Label>();
        for (Airport airport: RadarScreen.airports.values()) {
            String[] metarText = new String[5];
            metarText[0] = airport.getIcao();
            metarText[1] = "Winds: Loading";
            metarText[2] = "Gusting to: Loading";
            metarText[3] = "Visibility: Loading";
            metarText[4] = "Windshear: Loading";
            Label metarInfo = new Label(StringUtils.join(metarText, "\n"), labelStyle);
            metarInfo.setPosition(100, 2775 - index * 525);
            metarInfo.setSize(700, 300);
            RadarScreen.uiStage.addActor(metarInfo);
            metarInfos.add(metarInfo);
            index++;
        }
    }

    public void setNormalPane(boolean show) {
        //Sets visibility of elements
        for (Label label: metarInfos) {
            label.setVisible(show);
        }
    }

    public void setSelectedPane(Aircraft aircraft) {
        //Sets visibility of UI pane
        if (aircraft != null) {
            if (selectedAircraft != aircraft) {
                //Different aircraft selected
                resetValues();
            }
            selectedAircraft = aircraft;
            //Aircraft selected; show default lat mode pane first
            updateBoxes(0);
            settingsBox.setVisible(true);
            String latMode1 = selectedAircraft.getNavState().getLatMode();
            settingsBox.setSelected(latMode1);
            valueBox.setVisible(latMode1.contains("waypoint") || latMode1.contains("arrival") || latMode1.contains("departure") || latMode1.contains("Hold at"));
            if (selectedAircraft.getDirect() != null) {
                valueBox.setSelected(selectedAircraft.getDirect().getName());
            } else {
                valueBox.setSelected(null);
            }
            showHdgBoxes(latMode1.contains("heading"));
            showChangesButtons(true);
        } else {
            //Aircraft unselected
            selectedAircraft = null;
            settingsBox.setVisible(false);
            valueBox.setVisible(false);
            showHdgBoxes(false);
            showChangesButtons(false);
        }
    }

    private void showHdgBoxes(boolean show) {
        //Show/hide elements for heading box
        hdgBox.setVisible(show);
        hdg100add.setVisible(show);
        hdg100minus.setVisible(show);
        hdg10add.setVisible(show);
        hdg10minus.setVisible(show);
        hdg5add.setVisible(show);
        hdg5minus.setVisible(show);
    }

    private void showChangesButtons(boolean show) {
        //Show/hide elements for changes
        cfmChange.setVisible(show);
        resetTab.setVisible(show);
        resetAll.setVisible(show);
    }

    private void loadSelectBox() {
        //Load the select boxes to be used
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = AtcSim.skin.getDrawable("ListBackground");

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = AtcSim.fonts.defaultFont20;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.BLACK;
        Drawable button_down = AtcSim.skin.getDrawable("Button_down");
        button_down.setTopHeight(50);
        button_down.setBottomHeight(50);
        listStyle.selection = button_down;

        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle();
        boxStyle.font = AtcSim.fonts.defaultFont20;
        boxStyle.fontColor = Color.WHITE;
        boxStyle.listStyle = listStyle;
        boxStyle.scrollStyle = scrollPaneStyle;
        boxStyle.background = selectBoxBackgroundDrawable;

        //Settings box for setting lat/alt/spd modes
        settingsBox = new SelectBox<String>(boxStyle);
        settingsBox.setPosition(0.1f * getPaneWidth(), 3240 - 970);
        settingsBox.setSize(0.8f * getPaneWidth(), 270);
        settingsBox.setAlignment(Align.center);
        settingsBox.getList().setAlignment(Align.center);
        settingsBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectedAircraft != null) {
                    updateChoice();
                    event.handle();
                }
            }
        });
        RadarScreen.uiStage.addActor(settingsBox);

        waypoints = new Array<String>();
        holdingWaypoints = new Array<String>();

        SelectBox.SelectBoxStyle boxStyle2 = new SelectBox.SelectBoxStyle();
        boxStyle2.font = AtcSim.fonts.defaultFont20;
        boxStyle2.fontColor = Color.WHITE;
        boxStyle2.listStyle = listStyle;
        boxStyle2.scrollStyle = scrollPaneStyle;
        boxStyle2.background = selectBoxBackgroundDrawable;

        //Valuebox for setting waypoint selections
        valueBox = new SelectBox<String>(boxStyle2);
        valueBox.setPosition(0.1f * getPaneWidth(), 3240 - 1570);
        valueBox.setSize(0.8f * getPaneWidth(), 270);
        valueBox.setAlignment(Align.center);
        valueBox.getList().setAlignment(Align.center);
        valueBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectedAircraft != null) {
                    updateChoice();
                    event.handle();
                }
            }
        });
        RadarScreen.uiStage.addActor(valueBox);
    }

    private void loadButtons() {
        //Label for heading
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont40;
        labelStyle.fontColor = Color.WHITE;
        labelStyle.background = hdgBoxBackgroundDrawable;
        hdgBox = new Label("360", labelStyle);
        hdgBox.setPosition(0.1f * getPaneWidth(), 3240 - 2370);
        hdgBox.setSize(0.8f * getPaneWidth(), 270);
        hdgBox.setAlignment(Align.center);
        RadarScreen.uiStage.addActor(hdgBox);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.down = selectBoxBackgroundDrawable;
        textButtonStyle.up = selectBoxBackgroundDrawable;
        textButtonStyle.font = AtcSim.fonts.defaultFont40;

        //+100 button
        hdg100add = new TextButton("+", textButtonStyle);
        hdg100add.setSize(0.8f / 3f * getPaneWidth(), 200);
        hdg100add.setPosition(0.1f * getPaneWidth(), 3240 - 2100);
        hdg100add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(100);
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(hdg100add);

        //-100 button
        hdg100minus = new TextButton("-", textButtonStyle);
        hdg100minus.setSize(0.8f / 3f * getPaneWidth(), 200);
        hdg100minus.setPosition(0.1f * getPaneWidth(), 3240 - 2570);
        hdg100minus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(-100);
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(hdg100minus);

        //+10 button
        hdg10add = new TextButton("+", textButtonStyle);
        hdg10add.setSize(0.8f / 3f * getPaneWidth(), 200);
        hdg10add.setPosition((0.1f + 0.8f / 3) * getPaneWidth(), 3240 - 2100);
        hdg10add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(10);
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(hdg10add);

        //-10 button
        hdg10minus = new TextButton("-", textButtonStyle);
        hdg10minus.setSize(0.8f / 3f * getPaneWidth(), 200);
        hdg10minus.setPosition((0.1f + 0.8f / 3) * getPaneWidth(), 3240 - 2570);
        hdg10minus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(-10);
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(hdg10minus);

        //+5 button
        hdg5add = new TextButton("+", textButtonStyle);
        hdg5add.setSize(0.8f / 3f * getPaneWidth(), 200);
        hdg5add.setPosition((0.1f + 0.8f / 1.5f) * getPaneWidth(), 3240 - 2100);
        hdg5add.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(5);
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(hdg5add);

        //-5 button
        hdg5minus = new TextButton("-", textButtonStyle);
        hdg5minus.setSize((0.8f / 3f) * getPaneWidth(), 200);
        hdg5minus.setPosition((0.1f + 0.8f / 1.5f) * getPaneWidth(), 3240 - 2570);
        hdg5minus.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateClearedHdg(-5);
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(hdg5minus);

        TextButton.TextButtonStyle textButtonStyle2 = new TextButton.TextButtonStyle();
        textButtonStyle2.font = AtcSim.fonts.defaultFont20;
        textButtonStyle2.fontColor = Color.BLACK;
        textButtonStyle2.up = selectBoxBackgroundDrawable;
        textButtonStyle2.down = AtcSim.skin.getDrawable("Button_down");

        //Transmit button
        cfmChange = new TextButton("Transmit", textButtonStyle2);
        cfmChange.setSize(0.25f * getPaneWidth(), 370);
        cfmChange.setPosition(0.1f * getPaneWidth(), 3240 - 3070);
        cfmChange.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateMode();
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(cfmChange);

        //Undo all changes button
        resetAll = new TextButton("Undo all\nchanges", textButtonStyle2);
        resetAll.setSize(0.25f * getPaneWidth(), 370);
        resetAll.setPosition(0.65f * getPaneWidth(), 3240 - 3070);
        resetAll.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetAll();
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(resetAll);

        //Separate buttonstyle for reset tab
        TextButton.TextButtonStyle textButtonStyle3 = new TextButton.TextButtonStyle();
        textButtonStyle3.font = AtcSim.fonts.defaultFont20;
        textButtonStyle3.fontColor = Color.BLACK;
        textButtonStyle3.up = selectBoxBackgroundDrawable;
        textButtonStyle3.down = AtcSim.skin.getDrawable("Button_down");

        //Undo this tab button
        resetTab = new TextButton("Undo\nthis tab", textButtonStyle3);
        resetTab.setSize(0.25f * getPaneWidth(), 370);
        resetTab.setPosition(0.375f * getPaneWidth(), 3240 - 3070);
        resetTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetTab(tab);
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(resetTab);
    }

    private void updateBoxes(int tab) {
        //Update box values
        if (selectedAircraft != null) {
            latMode = selectedAircraft.getLatMode();
            if (selectedAircraft.getDirect() != null) {
                clearedWpt = selectedAircraft.getDirect().getName();
            }
            clearedAlt = selectedAircraft.getClearedAltitude();
            clearedSpd = selectedAircraft.getClearedIas();
            clearedHdg = selectedAircraft.getClearedHeading();
            waypoints.clear();
            for (Waypoint waypoint: selectedAircraft.getRemainingWaypoints()) {
                waypoints.add(waypoint.getName());
            }

            if (tab == 0) {
                //Lateral mode tab
                settingsBox.setItems(selectedAircraft.getNavState().getLatModes());
                settingsBox.setSelected(latMode);
                valueBox.setItems(waypoints);
                valueBox.setSelected(clearedWpt);
            } else if (tab == 1) {
                //Altitude mode tab
                settingsBox.setItems(selectedAircraft.getNavState().getAltModes());
                settingsBox.setSelected(altMode);
            } else {
                //Speed mode tab
                settingsBox.setItems(selectedAircraft.getNavState().getSpdModes());
                settingsBox.setSelected(spdMode);
            }
        }
    }

    public void updateState() {
        //Called when aircraft navstate changes not due to player, updates choices so that they are appropriate
        updateBoxes(tab);
        updateChoice();
    }

    private void updateChoice() {
        //Update selected choices, called upon selectbox/button change
        String newMode = settingsBox.getSelected();
        if (tab == 0) {
            //Lat mode tab
            latMode = newMode;
            if (!latMode.equals(selectedAircraft.getNavState().getLatMode())) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
                latModeChanged = true;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
                latModeChanged = false;
            }
            showHdgBoxes(latMode.contains("heading"));

            //Valuebox (for waypoints)
            clearedWpt = valueBox.getSelected();
            valueBox.setVisible(latMode.contains("waypoint") || latMode.contains("arrival") || latMode.contains("departure") || latMode.equals("Hold at"));
            if (clearedWpt != null && selectedAircraft.getDirect() != null) {
                if (selectedAircraft.getDirect() == null || clearedWpt.equals(selectedAircraft.getDirect().getName())) {
                    valueBox.getStyle().fontColor = Color.WHITE;
                    wptChanged = false;
                } else {
                    valueBox.getStyle().fontColor = Color.YELLOW;
                    wptChanged = true;
                }
            }

            //Hdg box
            if (clearedHdg != selectedAircraft.getClearedHeading()) {
                hdgBox.getStyle().fontColor = Color.YELLOW;
                hdgChanged = true;
            } else {
                hdgBox.getStyle().fontColor = Color.WHITE;
                hdgChanged = false;
            }
            hdgBox.setText(Integer.toString(clearedHdg));
            if (latModeChanged || hdgChanged || wptChanged) {
                resetTab.getStyle().fontColor = Color.YELLOW;
            } else {
                resetTab.getStyle().fontColor = Color.BLACK;
            }
        } else if (tab == 1) {
            //Alt mode tab
            altMode = newMode;
            if (!altMode.equals(selectedAircraft.getNavState().getAltMode())) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
                altModeChanged = true;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
                altModeChanged = false;
            }
            if (altModeChanged || altChanged) {
                resetTab.getStyle().fontColor = Color.YELLOW;
            } else {
                resetTab.getStyle().fontColor = Color.BLACK;
            }
        } else if (tab == 2) {
            //Spd mode tab
            spdMode = newMode;
            if (!spdMode.equals(selectedAircraft.getNavState().getSpdMode())) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
                spdModeChanged = true;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
                spdModeChanged = false;
            }
            if (spdModeChanged || spdChanged) {
                resetTab.getStyle().fontColor = Color.YELLOW;
            } else {
                resetTab.getStyle().fontColor = Color.BLACK;
            }
        }
        if (latModeChanged || hdgChanged || wptChanged || altModeChanged || altChanged || spdModeChanged || spdChanged) {
            cfmChange.getStyle().fontColor = Color.YELLOW;
        } else {
            cfmChange.getStyle().fontColor = Color.BLACK;
        }
    }

    private void updateMode() {
        //Lat mode
        if (latModeChanged) {
            if (latMode.contains(selectedAircraft.getSidStar().getName())) {
                selectedAircraft.setLatMode("sidstar");
                System.out.println("Sidstar set");
            } else if (latMode.equals("After waypoint, fly heading")) {
                System.out.println("After waypoint fly heading");
            } else if (latMode.equals("Hold at")) {
                System.out.println("Hold at");
            } else if (latMode.equals("Fly heading") || latMode.equals("Turn left heading") || latMode.equals("Turn right heading")) {
                selectedAircraft.setLatMode("vector");
                System.out.println("Vectors");
            } else {
                Gdx.app.log("Invalid lat mode", "Invalid latmode " + latMode + " set!");
            }
            selectedAircraft.getNavState().setLatMode(latMode);
        }

        if (hdgChanged) {
            selectedAircraft.setClearedHeading(clearedHdg);
        }

        if (wptChanged) {
            selectedAircraft.setDirect(RadarScreen.waypoints.get(clearedWpt));
            selectedAircraft.updateSelectedWaypoints(null);
            selectedAircraft.setSidStarIndex(selectedAircraft.getSidStar().findWptIndex(selectedAircraft.getDirect()));
        }

        //Alt mode
        if (altModeChanged) {
            if (altMode.equals("Climb via SID") || altMode.equals("Descend via STAR")) {
                selectedAircraft.setAltMode("sidstar");
            } else {
                selectedAircraft.setAltMode("open");
                selectedAircraft.setExpedite(altMode.contains("Expedite"));
            }
            selectedAircraft.getNavState().setAltMode(altMode);
        }

        if (altChanged) {
            selectedAircraft.setClearedAltitude(clearedAlt);
        }

        //Spd mode
        if (spdModeChanged) {
            selectedAircraft.getNavState().setSpdMode(spdMode);
        }

        if (spdChanged) {
            selectedAircraft.setClearedIas(clearedSpd);
        }

        updateChoice();
    }

    private void resetValues() {
        tab = 0;
        latMode = null;
        latModeChanged = false;
        clearedWpt = null;
        wptChanged = false;
        altMode = null;
        altModeChanged = false;
        spdMode = null;
        spdModeChanged = false;
        clearedHdg = 360;
        hdgChanged = false;
        clearedAlt = 5000;
        altChanged = false;
        clearedSpd = 250;
        spdChanged = false;
        settingsBox.getStyle().fontColor = Color.WHITE;
        valueBox.getStyle().fontColor = Color.WHITE;
        hdgBox.getStyle().fontColor = Color.WHITE;
    }

    public void resetTab(int tab) {
        //Reset current tab to original aircraft state
        if (tab == 0) {
            settingsBox.setSelected(selectedAircraft.getLatMode());
            clearedHdg = selectedAircraft.getClearedHeading();
            if (selectedAircraft.getDirect() != null) {
                clearedWpt = selectedAircraft.getDirect().getName();
            } else {
                clearedWpt = null;
            }
            valueBox.setSelected(clearedWpt);
        } else if (tab == 1) {
            settingsBox.setSelected(selectedAircraft.getAltMode());
            clearedAlt = selectedAircraft.getClearedAltitude();
        } else if (tab == 2) {
            settingsBox.setSelected(selectedAircraft.getSpdMode());
            clearedSpd = selectedAircraft.getClearedIas();
        }
        updateChoice();
    }

    public void resetAll() {
        //Reset all tabs to original aircraft state
        resetTab(0);
        resetTab(1);
        resetTab(2);
        updateBoxes(tab);
    }

    public void updatePaneWidth() {
        paneImage.setSize(1080 * (float)AtcSim.WIDTH / AtcSim.HEIGHT, 3240);
        float paneSize = 0.8f * paneImage.getWidth();
        float leftMargin = 0.1f * paneImage.getWidth();
        settingsBox.setSize(paneSize, 270);
        settingsBox.setX(leftMargin);
        valueBox.setSize(paneSize, 270);
        valueBox.setX(leftMargin);
        hdgBox.setSize(paneSize, 270);
        hdgBox.setX(leftMargin);
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
        cfmChange.setSize(paneImage.getWidth() / 4, 370);
        cfmChange.setX(leftMargin);
        resetTab.setSize(paneImage.getWidth() / 4, 370);
        resetTab.setX(leftMargin + 0.275f * paneImage.getWidth());
        resetAll.setSize(paneImage.getWidth() / 4, 370);
        resetAll.setX(leftMargin + 0.55f * paneImage.getWidth());
    }

    private void updateClearedHdg(int deltaHdg) {
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
        updateChoice();
    }

    public float getPaneWidth() {
        return paneImage.getWidth();
    }

    @Override
    public void dispose() {
        hdgBoxBackground.dispose();
        paneTexture.dispose();
        boxBackground.dispose();
    }
}
