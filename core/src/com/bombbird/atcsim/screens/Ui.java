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
    private Image paneImage;
    private SpriteDrawable hdgBoxBackgroundDrawable = new SpriteDrawable(new Sprite(hdgBoxBackground));

    private TextButton latTab;
    private TextButton altTab;
    private TextButton spdTab;

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
    private int afterWptHdg;
    private String afterWpt;
    private Array<String> waypoints;
    private Array<String> holdingWaypoints;
    private boolean latModeChanged;
    private boolean wptChanged;
    private boolean hdgChanged;
    private boolean afterWptChanged;
    private boolean afterWptHdgChanged;

    private String altMode;
    private int clearedAlt;
    private boolean altModeChanged;
    private boolean altChanged;
    private Array<String> alts;

    private String spdMode;
    private int clearedSpd;
    private boolean spdModeChanged;
    private boolean spdChanged;

    private boolean resetting;

    public Ui() {
        tab = 0;
        loadNormalPane();
        loadSelectBox();
        loadButtons();
        resetting = false;
    }

    public void updateMetar() {
        //Updates text in METAR label
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
            if (!aircraft.equals(selectedAircraft)) {
                tab = 0;
                selectedAircraft = aircraft;
            }
            //Make tab buttons visible, then update them to show correct colour
            showTabBoxes(true);
            //Aircraft selected; show default lat mode pane first
            settingsBox.setVisible(true);
            showChangesButtons(true);
            resetAll();
        } else {
            //Aircraft unselected
            selectedAircraft = null;
            showTabBoxes(false);
            settingsBox.setVisible(false);
            valueBox.setVisible(false);
            showHdgBoxes(false);
            showChangesButtons(false);
        }
    }

    private void showTabBoxes(boolean show) {
        //Show/hide tab selection buttons
        latTab.setVisible(show);
        altTab.setVisible(show);
        spdTab.setVisible(show);
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
        boxStyle.background = hdgBoxBackgroundDrawable;

        //Settings box for setting lat/alt/spd modes
        settingsBox = new SelectBox<String>(boxStyle);
        settingsBox.setPosition(0.1f * getPaneWidth(), 3240 - 970);
        settingsBox.setSize(0.8f * getPaneWidth(), 270);
        settingsBox.setAlignment(Align.center);
        settingsBox.getList().setAlignment(Align.center);
        settingsBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectedAircraft != null && !resetting) {
                    getChoices();
                    updateElements();
                    compareWithAC();
                    updateElementColours();
                }
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(settingsBox);

        waypoints = new Array<String>();
        holdingWaypoints = new Array<String>();
        alts = new Array<String>();

        SelectBox.SelectBoxStyle boxStyle2 = new SelectBox.SelectBoxStyle();
        boxStyle2.font = AtcSim.fonts.defaultFont20;
        boxStyle2.fontColor = Color.WHITE;
        boxStyle2.listStyle = listStyle;
        boxStyle2.scrollStyle = scrollPaneStyle;
        boxStyle2.background = hdgBoxBackgroundDrawable;

        //Valuebox for setting waypoint selections
        valueBox = new SelectBox<String>(boxStyle2);
        valueBox.setPosition(0.1f * getPaneWidth(), 3240 - 1570);
        valueBox.setSize(0.8f * getPaneWidth(), 270);
        valueBox.setAlignment(Align.center);
        valueBox.getList().setAlignment(Align.center);
        valueBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectedAircraft != null && !resetting) {
                    getChoices();
                    updateElements();
                    compareWithAC();
                    updateElementColours();
                }
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(valueBox);
    }

    private void loadHdgElements() {
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
        textButtonStyle.down = hdgBoxBackgroundDrawable;
        textButtonStyle.up = hdgBoxBackgroundDrawable;
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
    }

    private void loadChangeButtons() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = AtcSim.fonts.defaultFont20;
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.up = hdgBoxBackgroundDrawable;
        textButtonStyle.down = AtcSim.skin.getDrawable("Button_down");

        //Transmit button
        cfmChange = new TextButton("Transmit", textButtonStyle);
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
        resetAll = new TextButton("Undo all\nchanges", textButtonStyle);
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
        TextButton.TextButtonStyle textButtonStyle2 = new TextButton.TextButtonStyle();
        textButtonStyle2.font = AtcSim.fonts.defaultFont20;
        textButtonStyle2.fontColor = Color.BLACK;
        textButtonStyle2.up = hdgBoxBackgroundDrawable;
        textButtonStyle2.down = AtcSim.skin.getDrawable("Button_down");

        //Undo this tab button
        resetTab = new TextButton("Undo\nthis tab", textButtonStyle2);
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

    private void loadTabButtons() {
        //Lat mode
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = AtcSim.fonts.defaultFont20;
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.up = hdgBoxBackgroundDrawable;
        textButtonStyle.down = hdgBoxBackgroundDrawable;

        latTab = new TextButton("Lateral", textButtonStyle);
        latTab.setSize(0.25f * getPaneWidth(), 370);
        latTab.setPosition(0.1f * getPaneWidth(), 3240 - 400);
        latTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab != 0) {
                    //Run only if tab is not lat
                    tab = 0;
                    updateTabButtons();
                    updateElements();
                    updateElementColours();
                }
                event.handle();
            }
        });
        setTabColours(latTab, true);
        RadarScreen.uiStage.addActor(latTab);

        //Alt mode
        TextButton.TextButtonStyle textButtonStyle2 = new TextButton.TextButtonStyle();
        textButtonStyle2.font = AtcSim.fonts.defaultFont20;
        textButtonStyle2.fontColor = Color.BLACK;
        textButtonStyle2.up = hdgBoxBackgroundDrawable;
        textButtonStyle2.down = hdgBoxBackgroundDrawable;

        altTab = new TextButton("Altitude", textButtonStyle2);
        altTab.setSize(0.25f * getPaneWidth(), 370);
        altTab.setPosition(0.375f * getPaneWidth(), 3240 - 400);
        altTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab != 1) {
                    //Run only if tab is not alt
                    tab = 1;
                    updateTabButtons();
                    updateElements();
                    updateElementColours();
                }
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(altTab);

        //Spd mode
        TextButton.TextButtonStyle textButtonStyle3 = new TextButton.TextButtonStyle();
        textButtonStyle3.font = AtcSim.fonts.defaultFont20;
        textButtonStyle3.fontColor = Color.BLACK;
        textButtonStyle3.up = hdgBoxBackgroundDrawable;
        textButtonStyle3.down = hdgBoxBackgroundDrawable;

        spdTab = new TextButton("Speed", textButtonStyle3);
        spdTab.setSize(0.25f * getPaneWidth(), 370);
        spdTab.setPosition(0.65f * getPaneWidth(), 3240 - 400);
        spdTab.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab != 2) {
                    //Run only if tab is not spd
                    tab = 2;
                    updateTabButtons();
                    updateElements();
                    updateElementColours();
                }
                event.handle();
            }
        });
        RadarScreen.uiStage.addActor(spdTab);
    }

    private void updateTabButtons() {
        if (tab == 0) {
            setTabColours(latTab, true);
            setTabColours(altTab, false);
            setTabColours(spdTab, false);
        } else if (tab == 1) {
            setTabColours(latTab, false);
            setTabColours(altTab, true);
            setTabColours(spdTab, false);
        } else if (tab == 2) {
            setTabColours(latTab, false);
            setTabColours(altTab, false);
            setTabColours(spdTab, true);
        } else {
            Gdx.app.log("Invalid tab", "Unknown tab number " + Integer.toString(tab) + " set!");
        }
    }

    private void setTabColours(TextButton textButton, boolean selected) {
        if (selected) {
            textButton.getStyle().down = AtcSim.skin.getDrawable("Button_down");
            textButton.getStyle().up = AtcSim.skin.getDrawable("Button_down");
            textButton.getStyle().fontColor = Color.WHITE;
        } else {
            textButton.getStyle().down = hdgBoxBackgroundDrawable;
            textButton.getStyle().up = hdgBoxBackgroundDrawable;
            textButton.getStyle().fontColor = Color.BLACK;
        }
    }

    private void loadButtons() {
        loadHdgElements();

        loadChangeButtons();

        loadTabButtons();
    }

    public void updateState() {
        //Called when aircraft navstate changes not due to player, updates choices so that they are appropriate
        getChoices();
        updateElements();
        compareWithAC();
        updateElementColours();
    }

    private void updateMode() {
        //Lat mode
        if (latMode.contains(selectedAircraft.getSidStar().getName())) {
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
            System.out.println("Sidstar set");
        } else if (latMode.equals("After waypoint, fly heading")) {
            selectedAircraft.setLatMode("sidstar");
            selectedAircraft.setAfterWaypoint(RadarScreen.waypoints.get(afterWpt));
            selectedAircraft.setAfterWptHdg(afterWptHdg);
            System.out.println("After waypoint fly heading");
        } else if (latMode.equals("Hold at")) {
            System.out.println("Hold at");
        } else if (latMode.equals("Fly heading") || latMode.equals("Turn left heading") || latMode.equals("Turn right heading")) {
            selectedAircraft.setLatMode("vector");
            selectedAircraft.setClearedHeading(clearedHdg);
            System.out.println("Vectors");
        } else {
            Gdx.app.log("Invalid lat mode", "Invalid latmode " + latMode + " set!");
        }
        selectedAircraft.getNavState().setLatMode(latMode);

        //Alt mode
        if (altMode.equals("Climb via SID") || altMode.equals("Descend via STAR")) {
            selectedAircraft.setAltMode("sidstar");
        } else {
            selectedAircraft.setAltMode("open");
            selectedAircraft.setExpedite(altMode.contains("Expedite"));
        }
        selectedAircraft.getNavState().setAltMode(altMode);

        selectedAircraft.setClearedAltitude(clearedAlt);

        //Spd mode
        selectedAircraft.getNavState().setSpdMode(spdMode);

        selectedAircraft.setClearedIas(clearedSpd);

        resetAll();
    }

    private void resetTab(int tab) {
        //Reset current tab to original aircraft state
        resetting = true;
        if (tab == 0) {
            settingsBox.setSelected(selectedAircraft.getNavState().getLatMode());
            clearedHdg = selectedAircraft.getClearedHeading();
            afterWptHdg = selectedAircraft.getAfterWptHdg();
            if ((settingsBox.getSelected().contains("arrival") || settingsBox.getSelected().contains("departure")) && selectedAircraft.getDirect() != null) {
                valueBox.setSelected(selectedAircraft.getDirect().getName());
            } else {
                valueBox.setSelected(null);
            }
            if ((settingsBox.getSelected().equals("After waypoint, fly heading")) && selectedAircraft.getAfterWaypoint() != null) {
                valueBox.setSelected(selectedAircraft.getAfterWaypoint().getName());
            } else {
                valueBox.setSelected(null);
            }
        } else if (tab == 1) {
            settingsBox.setSelected(selectedAircraft.getNavState().getAltMode());
            clearedAlt = selectedAircraft.getClearedAltitude();
            valueBox.setSelected(Integer.toString(clearedAlt));
        } else if (tab == 2) {
            settingsBox.setSelected(selectedAircraft.getNavState().getSpdMode());
            clearedSpd = selectedAircraft.getClearedIas();
            valueBox.setSelected(Integer.toString(clearedSpd));
        }
        getChoices();
        updateElements();
        compareWithAC();
        updateElementColours();
        resetting = false;
    }

    private void resetAll() {
        //Reset all tabs to original aircraft state
        getACState();
        updateTabButtons();
        updateElements();
        compareWithAC();
        updateElementColours();
    }

    private void getACState() {
        //Gets initial navstate from aircraft (when first selected)
        latMode = selectedAircraft.getNavState().getLatMode();
        latModeChanged = false;
        clearedHdg = selectedAircraft.getClearedHeading();
        hdgChanged = false;
        if (selectedAircraft.getDirect() != null) {
            clearedWpt = selectedAircraft.getDirect().getName();
        }
        wptChanged = false;
        if (selectedAircraft.getAfterWaypoint() != null) {
            afterWpt = selectedAircraft.getAfterWaypoint().getName();
        }
        afterWptChanged = false;
        afterWptHdg = selectedAircraft.getAfterWptHdg();
        afterWptHdgChanged = false;

        altMode = selectedAircraft.getNavState().getAltMode();
        altModeChanged = false;
        clearedAlt = selectedAircraft.getClearedAltitude();
        altChanged = false;

        spdMode = selectedAircraft.getNavState().getSpdMode();
        spdModeChanged = false;
        clearedSpd = selectedAircraft.getClearedIas();
        spdChanged = false;
    }

    private void getChoices() {
        //Gets the choices from the boxes, sets variables to them; called after selections in selectbox changes/heading value changes
        if (tab == 0) {
            //Lat mode tab
            latMode = settingsBox.getSelected();
            if (latMode.contains("waypoint")) {
                afterWpt = valueBox.getSelected();
            } else if (latMode.contains("arrival") || latMode.contains("departure")) {
                clearedWpt = valueBox.getSelected();
            }
        } else if (tab == 1) {
            //Alt mode tab
            altMode = settingsBox.getSelected();
            clearedAlt = Integer.parseInt(valueBox.getSelected());
        } else if (tab == 2) {
            //Spd mode tab
            spdMode = settingsBox.getSelected();
            clearedSpd = Integer.parseInt(valueBox.getSelected());
        }
    }

    private void updateElements() {
        resetting = true;
        settingsBox.setVisible(true);
        if (tab == 0) {
            //Lat mode tab
            settingsBox.setItems(selectedAircraft.getNavState().getLatModes());
            settingsBox.setSelected(latMode);

            if (latMode.contains("waypoint") || latMode.contains("arrival") || latMode.contains("departure") || latMode.equals("Hold at")) {
                //Make waypoint box visibile
                valueBox.setVisible(true);
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
            } else {
                //Otherwise hide it
                valueBox.setVisible(false);
            }

            //Show heading box if heading mode, otherwise hide it
            showHdgBoxes(latMode.contains("heading"));
            if (latMode.equals("After waypoint, fly heading")) {
                hdgBox.setText(Integer.toString(afterWptHdg));
            } else if (latMode.contains("heading")) {
                hdgBox.setText(Integer.toString(clearedHdg));
            }
        } else if (tab == 1) {
            //Alt mode tab
            settingsBox.setItems(selectedAircraft.getNavState().getAltModes());
            settingsBox.setSelected(altMode);
            showHdgBoxes(false);
            valueBox.setVisible(true);
            alts.clear();
            int lowestAlt = selectedAircraft.getLowestAlt();
            if (lowestAlt % 1000 != 0) {
                alts.add(Integer.toString(lowestAlt));
                int altTracker = lowestAlt + (1000 - lowestAlt % 1000);
                while (altTracker <= selectedAircraft.getHighestAlt()) {
                    alts.add(Integer.toString(altTracker));
                    altTracker += 1000;
                }
            } else {
                while (lowestAlt <= selectedAircraft.getHighestAlt()) {
                    alts.add(Integer.toString(lowestAlt));
                    lowestAlt += 1000;
                }
            }
            valueBox.setItems(alts);
            valueBox.setSelected(Integer.toString(clearedAlt));
        } else if (tab == 2) {
            //Spd mode tab
            settingsBox.setItems(selectedAircraft.getNavState().getSpdModes());
            settingsBox.setSelected(spdMode);
            showHdgBoxes(false);
            valueBox.setVisible(true);
        }
        resetting = false;
    }

    private void compareWithAC() {
        latModeChanged = !latMode.equals(selectedAircraft.getNavState().getLatMode());
        hdgChanged = !(clearedHdg == selectedAircraft.getClearedHeading());
        if (clearedWpt != null && selectedAircraft.getDirect() != null) {
            wptChanged = !clearedWpt.equals(selectedAircraft.getDirect().getName());
        }
        if (afterWpt != null && selectedAircraft.getAfterWaypoint() != null) {
            afterWptChanged = !afterWpt.equals(selectedAircraft.getAfterWaypoint().getName());
        }
        afterWptHdgChanged = !(afterWptHdg == selectedAircraft.getAfterWptHdg());

        altModeChanged = !altMode.equals(selectedAircraft.getNavState().getAltMode());
        altChanged = !(clearedAlt == selectedAircraft.getClearedAltitude());

        spdModeChanged = !spdMode.equals(selectedAircraft.getNavState().getSpdMode());
        spdChanged = !(clearedSpd == selectedAircraft.getClearedIas());
    }

    private void updateElementColours() {
        resetting = true;
        if (tab == 0) {
            //Lat mode selectbox colour
            if (latModeChanged) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
            }

            //Lat mode waypoint box colour
            if (latMode.equals("After waypoint, fly heading")) {
                if (afterWptChanged) {
                    valueBox.getStyle().fontColor = Color.YELLOW;
                } else {
                    valueBox.getStyle().fontColor = Color.WHITE;
                }
            } else {
                if (wptChanged) {
                    valueBox.getStyle().fontColor = Color.YELLOW;
                } else {
                    valueBox.getStyle().fontColor = Color.WHITE;
                }
            }

            //Lat mode hdg box colour
            if (latMode.equals("After waypoint, fly heading")) {
                if (afterWptHdgChanged) {
                    hdgBox.getStyle().fontColor = Color.YELLOW;
                } else {
                    hdgBox.getStyle().fontColor = Color.WHITE;
                }
            } else {
                if (hdgChanged) {
                    hdgBox.getStyle().fontColor = Color.YELLOW;
                } else {
                    hdgBox.getStyle().fontColor = Color.WHITE;
                }
            }
        } else if (tab == 1) {
            //Alt mode selectbox colour
            if (altModeChanged) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
            }

            //Alt box colour: TODO
        } else if (tab == 2) {
            //Spd mode selectbox colour
            if (spdModeChanged) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
            }

            //Spd box colour: TODO
        }

        //Update transmit, reset all buttons
        boolean cfmButtonChange;
        if (latModeChanged) {
            cfmButtonChange = true;
        } else {
            if (latMode.contains("waypoint")) {
                //After waypoint, fly heading
                cfmButtonChange = afterWptChanged || afterWptHdgChanged;
            } else if (latMode.contains("heading")) {
                //Fly heading (left/right)
                cfmButtonChange = hdgChanged;
            } else {
                //Sid/star
                cfmButtonChange = wptChanged;
            }
        }
        if (!cfmButtonChange) {
            //If lat did not change at all, test for alt changes
            cfmButtonChange = altModeChanged || altChanged;
            if (tab == 0) {
                resetTab.getStyle().fontColor = Color.WHITE;
            }
        } else if (tab == 0) {
            //If tab is at lat page, set yellow
            resetTab.getStyle().fontColor = Color.YELLOW;
        }
        if (!cfmButtonChange) {
            //If both lat, alt did not change, test for spd changes
            cfmButtonChange = spdModeChanged || spdChanged;
            if (tab == 1) {
                resetTab.getStyle().fontColor = Color.WHITE;
            }
        } else if (tab == 1) {
            //If tab is at alt page, set yellow
            resetTab.getStyle().fontColor = Color.YELLOW;
        }
        if (cfmButtonChange) {
            cfmChange.getStyle().fontColor = Color.YELLOW;
            if (tab == 2) {
                resetTab.getStyle().fontColor = Color.YELLOW;
            }
        } else {
            cfmChange.getStyle().fontColor = Color.WHITE;
            if (tab == 2) {
                resetTab.getStyle().fontColor = Color.WHITE;
            }
        }
        resetting = false;
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

    public float getPaneWidth() {
        return paneImage.getWidth();
    }

    @Override
    public void dispose() {
        hdgBoxBackground.dispose();
        paneTexture.dispose();
    }

    public int getClearedHdg() {
        return clearedHdg;
    }

    public void setClearedHdg(int clearedHdg) {
        this.clearedHdg = clearedHdg;
    }

    public SelectBox<String> getSettingsBox() {
        return settingsBox;
    }

    public SelectBox<String> getValueBox() {
        return valueBox;
    }

    public int getTab() {
        return tab;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }
}
