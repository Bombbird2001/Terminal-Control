package com.bombbird.terminalcontrol.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.entities.trafficmanager.DayNightManager;
import com.bombbird.terminalcontrol.screens.gamescreen.RadarScreen;
import com.bombbird.terminalcontrol.ui.tabs.AltTab;
import com.bombbird.terminalcontrol.ui.tabs.LatTab;
import com.bombbird.terminalcontrol.ui.tabs.SpdTab;
import com.bombbird.terminalcontrol.utilities.Fonts;
import org.apache.commons.lang3.StringUtils;

public class Ui {
    //UI string constants
    //Lateral
    public static final String AFTER_WPT_FLY_HDG = "After waypoint, fly heading";
    public static final String FLY_HEADING = "Fly heading";
    public static final String LEFT_HEADING = "Turn left heading";
    public static final String RIGHT_HEADING = "Turn right heading";
    public static final String HOLD_AT = "Hold at";
    public static final String CHANGE_STAR = "Change STAR";
    public static final String NOT_CLEARED_APCH = "Not cleared approach";

    //Altitude
    public static final String CLIMB_VIA_SID = "Climb via SID";
    public static final String DESCEND_VIA_STAR = "Descend via STAR";
    public static final String CLIMB_DESCEND_TO = "Climb/descend to";
    public static final String EXPEDITE_TO = "Expedite climb/descent to";

    //Speed
    public static final String SID_SPD_RESTRICTIONS = "SID speed restrictions";
    public static final String STAR_SPD_RESTRICTIONS = "STAR speed restrictions";
    public static final String NO_SPD_RESTRICTIONS = "No speed restrictions";

    private static Texture transBackground;
    private static Texture hdgBoxBackground;
    private static Texture paneTexture;
    private static Texture lightBackground;
    private static Texture lightestBackground;
    private Button paneImage;
    public static SpriteDrawable hdgBoxBackgroundDrawable;
    public static SpriteDrawable transBackgroundDrawable;
    public static SpriteDrawable lightBoxBackground;
    public static SpriteDrawable lightestBoxBackground;

    public Array<Actor> actorArray;
    public Array<Float> actorXArray;
    public Array<Float> actorWidthArray;

    public LatTab latTab;
    public AltTab altTab;
    public SpdTab spdTab;

    private TextButton latButton;
    private TextButton altButton;
    private TextButton spdButton;

    private TextButton cfmChange;
    private TextButton handoverAck;
    private TextButton resetAll;

    private TextButton labelButton;

    //Label that displays score & high score
    private Label scoreLabel;

    //Label that displays simulation speed
    private Label infoLabel;

    //TextButton that pauses the game
    private TextButton pauseButton;

    //TextButton that allows player to open/close the sector
    private TextButton sectorButton;

    //Array for METAR info on default pane
    private ScrollPane metarPane;
    private Array<Label> metarInfos;

    //Instructions panel info
    private Aircraft selectedAircraft;

    private int tab;

    private final RadarScreen radarScreen;

    public Ui() {
        if (hdgBoxBackground == null) {
            generatePaneTextures();
        }

        radarScreen  = TerminalControl.radarScreen;

        actorArray = new Array<>();
        actorXArray = new Array<>();
        actorWidthArray = new Array<>();

        tab = 0;
        loadNormalPane();
        loadAircraftLabel();
        loadButtons();
    }

    /** Loads the textures for the UI pane */
    public static void generatePaneTextures() {
        hdgBoxBackground = new Texture(Gdx.files.internal("game/ui/BoxBackground.png"));
        transBackground = new Texture(Gdx.files.internal("game/ui/TransBackground.png"));
        paneTexture = new Texture(Gdx.files.internal("game/ui/UI Pane_Normal.png"));
        lightBackground = new Texture(Gdx.files.internal("game/ui/LightBoxBackground.png"));
        lightestBackground = new Texture(Gdx.files.internal("game/ui/LightestBoxBackground.png"));

        hdgBoxBackgroundDrawable = new SpriteDrawable(new Sprite(hdgBoxBackground));
        transBackgroundDrawable = new SpriteDrawable(new Sprite(transBackground));
        lightBoxBackground = new SpriteDrawable(new Sprite(lightBackground));
        lightestBoxBackground = new SpriteDrawable(new Sprite(lightestBackground));
    }

    /** Loads the 3 tabs */
    public void loadTabs() {
        latTab = new LatTab(this);
        altTab = new AltTab(this);
        spdTab = new SpdTab(this);
        setSelectedPane(null);
    }

    /** Updates the UI label after weather is updated */
    public void updateMetar() {
        for (int i = 0; i < metarInfos.size; i++) {
            Label label = metarInfos.get(i);
            //Get airport: ICAO code is 1st 4 letters on label's text
            Airport airport = radarScreen.airports.get(label.getText().toString().substring(0, 4));
            String[] metarText;
            if (radarScreen.realisticMetar) {
                //Realistic metar format
                metarText = new String[3];
                metarText[0] = airport.getIcao() + " - " + radarScreen.getInformation();
                StringBuilder dep = new StringBuilder();
                for (String runway: airport.getTakeoffRunways().keySet()) {
                    if (dep.length() > 0) dep.append(", ");
                    dep.append(runway);
                }
                StringBuilder arr = new StringBuilder();
                for (String runway: airport.getLandingRunways().keySet()) {
                    if (arr.length() > 0) arr.append(", ");
                    arr.append(runway);
                }
                metarText[1] = "DEP - " + dep.toString() + "     ARR - " + arr.toString();
                metarText[2] = airport.getMetar().optString("metar", "");
            } else {
                //Simple metar format
                metarText = new String[5];
                metarText[0] = airport.getIcao();
                //Wind: Speed + direction
                if (airport.getWinds()[1] == 0) {
                    metarText[1] = "Winds: Calm";
                } else {
                    if (airport.getWinds()[0] != 0) {
                        metarText[1] = "Winds: " + airport.getWinds()[0] + "@" + airport.getWinds()[1] + "kts";
                    } else {
                        metarText[1] = "Winds: VRB@" + airport.getWinds()[1] + "kts";
                    }
                }
                //Gusts
                if (airport.getGusts() != -1) {
                    metarText[2] = "Gusting to: " + airport.getGusts() + "kts";
                } else {
                    metarText[2] = "Gusting to: None";
                }
                //Visbility
                metarText[3] = "Visibility: " + airport.getVisibility() + " metres";
                //Windshear
                metarText[4] = "Windshear: " + airport.getWindshear();
            }
            boolean success = false;
            while (!success) {
                try {
                    label.setText(StringUtils.join(metarText, "\n"));
                    label.setWrap(true);
                    metarPane.layout();
                    metarPane.layout();
                    success = true;
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadAircraftLabel() {
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = Fonts.defaultFont30;
        buttonStyle.fontColor = Color.BLACK;
        labelButton = new TextButton("No aircraft selected", buttonStyle);
        labelButton.align(Align.center);
        labelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Prevents click from being sent to other elements
                event.handle();
            }
        });
        addActor(labelButton, 0.1f, 0.8f, 3240 - 695, 270);
    }

    private void loadNormalPane() {
        //Background click "catcher"
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        SpriteDrawable drawable = new SpriteDrawable(new Sprite(paneTexture));
        buttonStyle.up = drawable;
        buttonStyle.down = drawable;
        paneImage = new Button(buttonStyle);
        paneImage.setPosition(0, 0);
        paneImage.setSize(1080 * (float) TerminalControl.WIDTH / TerminalControl.HEIGHT, 3240);
        paneImage.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Prevents click from being sent to other elements
                event.handle();
            }
        });
        radarScreen.uiStage.addActor(paneImage);

        //Score display
        Label.LabelStyle labelStyle2 = new Label.LabelStyle();
        labelStyle2.font = Fonts.defaultFont30;
        labelStyle2.fontColor = Color.WHITE;
        scoreLabel = new Label("Score: " + radarScreen.getScore() + "\nHigh score: " + radarScreen.getHighScore() , labelStyle2);
        addActor(scoreLabel, 1 / 19.2f, -1, 2875, scoreLabel.getHeight());

        //Info label
        Label.LabelStyle labelStyle3 = new Label.LabelStyle();
        labelStyle3.font = Fonts.defaultFont20;
        labelStyle3.fontColor = Color.WHITE;
        infoLabel = new Label("", labelStyle3);
        infoLabel.setAlignment(Align.topRight);
        addActor(infoLabel, 0.6f, 0.35f, 2650, infoLabel.getHeight());
        updateInfoLabel();

        //Pause button
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = lightestBoxBackground;
        textButtonStyle.down = lightestBoxBackground;
        pauseButton = new TextButton("||", textButtonStyle);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Set game state to paused
                if (radarScreen.getRunwayChanger().isVisible()) {
                    radarScreen.getRunwayChanger().hideAll();
                    radarScreen.getCommBox().setVisible(true);
                }
                radarScreen.setGameRunning(false);
                event.handle();
            }
        });
        addActor(pauseButton, 0.75f, 0.2f, 2800, 300);
        //radarScreen.uiStage.setDebugAll(true);

        //Metar display labels
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;
        Table metarTable = new Table();
        metarTable.align(Align.left);
        metarInfos = new Array<>();
        for (final Airport airport: radarScreen.airports.values()) {
            Label metarInfo = new Label(airport.getIcao(), labelStyle);
            metarInfo.setWidth(paneImage.getWidth() * 0.6f);
            metarInfos.add(metarInfo);
            metarInfo.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);
                    event.handle();
                    TerminalControl.radarScreen.getRunwayChanger().setAirport(airport.getIcao());
                    TerminalControl.radarScreen.getRunwayChanger().setMainVisible(true);
                    TerminalControl.radarScreen.getCommBox().setVisible(false);
                }
            });
            metarTable.add(metarInfo).width(paneImage.getWidth() * 0.6f).padBottom(70);
            metarTable.row();
        }

        metarPane = new ScrollPane(metarTable);
        metarPane.setupFadeScrollBars(1, 1.5f);
        InputListener inputListener = null;
        for (EventListener eventListener: metarPane.getListeners()) {
            if (eventListener instanceof InputListener) {
                inputListener = (InputListener) eventListener;
            }
        }
        if (inputListener != null) metarPane.removeListener(inputListener);
        addActor(metarPane, 1 / 19.2f, 0.6f, 1550, 1200);

        //Sector button
        TextButton.TextButtonStyle textButtonStyle1 = new TextButton.TextButtonStyle();
        textButtonStyle1.fontColor = Color.BLACK;
        textButtonStyle1.font = Fonts.defaultFont20;
        textButtonStyle1.up = TerminalControl.skin.getDrawable("ListBackground");
        textButtonStyle1.down = TerminalControl.skin.getDrawable("Button_down");
        sectorButton = new TextButton(radarScreen.isSectorClosed() ? "Open sector" : "Close sector", textButtonStyle1);
        sectorButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Change sectorClosed state
                radarScreen.setSectorClosed(!radarScreen.isSectorClosed());
                sectorButton.setText(radarScreen.isSectorClosed() ? "Open sector" : "Close sector");
                radarScreen.getCommBox().normalMsg("Sector has been " + (radarScreen.isSectorClosed() ? "closed" : "opened"));
                updateInfoLabel();
            }
        });
        if (!radarScreen.tutorial) {
            addActor(sectorButton, 0.7f, 0.25f, 1500, 300);
        }
    }

    public void setNormalPane(boolean show) {
        //Sets visibility of elements
        for (Label label: metarInfos) {
            label.setVisible(show);
        }
        scoreLabel.setVisible(show);
        infoLabel.setVisible(show && infoLabel.getText().length > 0);
        pauseButton.setVisible(show);
        sectorButton.setVisible(show);
        if (radarScreen.getCommBox() != null) radarScreen.getCommBox().setVisible(show);
    }

    public void setSelectedPane(Aircraft aircraft) {
        //Sets visibility of UI pane
        if (aircraft != null) {
            if (!aircraft.equals(selectedAircraft)) {
                tab = 0;
                selectedAircraft = aircraft;
                latTab.selectedAircraft = aircraft;
                altTab.selectedAircraft = aircraft;
                spdTab.selectedAircraft = aircraft;
            }
            updateTabVisibility(true);
            updateTabButtons();
            //Make tab buttons visible, then update them to show correct colour
            showTabBoxes(true);
            showChangesButtons(true);
            resetAll();
            updateElementColours();
            updateAckHandButton(aircraft);
            if (tab != TerminalControl.defaultTabNo && TerminalControl.defaultTabNo >= 0 && TerminalControl.defaultTabNo <= 2) {
                //Change default tab to user selected tab; for some reason changing tab = 1 or 2 above causes crash
                tab = TerminalControl.defaultTabNo;
                updateTabButtons();
                updateTabVisibility(true);
                updateElements();
                updateElementColours();
            }
        } else {
            //Aircraft unselected
            selectedAircraft = null;
            latTab.selectedAircraft = null;
            altTab.selectedAircraft = null;
            spdTab.selectedAircraft = null;
            updateTabVisibility(false);
            showTabBoxes(false);
            latTab.setVisibility(false);
            altTab.setVisibility(false);
            spdTab.setVisibility(false);
            showChangesButtons(false);
        }
    }

    private void showTabBoxes(boolean show) {
        //Show/hide tab selection buttons
        latButton.setVisible(show);
        altButton.setVisible(show);
        spdButton.setVisible(show);
    }

    private void showChangesButtons(boolean show) {
        //Show/hide elements for changes
        cfmChange.setVisible(show);
        handoverAck.setVisible(show);
        resetAll.setVisible(show);
    }

    private void loadChangeButtons() {
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont20;
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.up = lightBoxBackground;
        textButtonStyle.down = TerminalControl.skin.getDrawable("Button_down");

        //Transmit button
        cfmChange = new TextButton("Transmit", textButtonStyle);
        cfmChange.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (cfmChange.getStyle().fontColor == Color.YELLOW && "Acknow-\nledge".equals(handoverAck.getText().toString())) {
                    handoverAck.setVisible(false);
                }
                updateMode();
                event.handle();
            }
        });
        addActor(cfmChange, 0.1f, 0.25f, 3240 - 3070, 370);

        //Handover/acknowledge button
        TextButton.TextButtonStyle ackStyle = new TextButton.TextButtonStyle(textButtonStyle);
        ackStyle.fontColor = Color.YELLOW;
        handoverAck = new TextButton("Acknow-\nledge", ackStyle);
        handoverAck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String txt = handoverAck.getText().toString();
                if ("Acknow-\nledge".equals(txt)) {
                    Gdx.app.postRunnable(() -> {
                        selectedAircraft.setActionRequired(false);
                        handoverAck.setVisible(false);
                    });
                } else if ("Handover".equals(txt)) {
                    Gdx.app.postRunnable(() -> {
                        selectedAircraft.setActionRequired(false);
                        handoverAck.setVisible(false);
                        selectedAircraft.contactOther();
                    });
                } else {
                    Gdx.app.log("Ui error", "Unknown button text " + txt);
                }
                event.handle();
            }
        });
        addActor(handoverAck, 0.375f, 0.25f, 3240 - 3070, 370);

        //Undo all changes button
        resetAll = new TextButton("Undo all\nchanges", textButtonStyle);
        resetAll.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resetAll();
                event.handle();
            }
        });
        addActor(resetAll, 0.65f, 0.25f, 3240 - 3070, 370);
    }

    private void loadTabButtons() {
        //Lat mode
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont20;
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.up = lightBoxBackground;
        textButtonStyle.down = lightBoxBackground;

        latButton = new TextButton("Lateral", textButtonStyle);
        latButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab != 0) {
                    //Run only if tab is not lat
                    tab = 0;
                    updateTabButtons();
                    updateTabVisibility(true);
                    updateElements();
                    updateElementColours();
                }
                event.handle();
            }
        });
        setTabColours(latButton, true);
        addActor(latButton, 0.1f, 0.25f, 3240 - 400, 300);

        //Alt mode
        TextButton.TextButtonStyle textButtonStyle2 = new TextButton.TextButtonStyle();
        textButtonStyle2.font = Fonts.defaultFont20;
        textButtonStyle2.fontColor = Color.BLACK;
        textButtonStyle2.up = lightBoxBackground;
        textButtonStyle2.down = lightBoxBackground;

        altButton = new TextButton("Altitude", textButtonStyle2);
        altButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab != 1) {
                    //Run only if tab is not alt
                    tab = 1;
                    updateTabButtons();
                    updateTabVisibility(true);
                    updateElements();
                    updateElementColours();
                }
                event.handle();
            }
        });
        addActor(altButton, 0.375f, 0.25f, 3240 - 400, 300);

        //Spd mode
        TextButton.TextButtonStyle textButtonStyle3 = new TextButton.TextButtonStyle();
        textButtonStyle3.font = Fonts.defaultFont20;
        textButtonStyle3.fontColor = Color.BLACK;
        textButtonStyle3.up = lightBoxBackground;
        textButtonStyle3.down = lightBoxBackground;

        spdButton = new TextButton("Speed", textButtonStyle3);
        spdButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (tab != 2) {
                    //Run only if tab is not spd
                    tab = 2;
                    updateTabButtons();
                    updateTabVisibility(true);
                    updateElements();
                    updateElementColours();
                }
                event.handle();
            }
        });
        addActor(spdButton, 0.65f, 0.25f, 3240 - 400, 300);
    }

    private void updateTabButtons() {
        if (tab == 0) {
            setTabColours(latButton, true);
            setTabColours(altButton, false);
            setTabColours(spdButton, false);
        } else if (tab == 1) {
            setTabColours(latButton, false);
            setTabColours(altButton, true);
            setTabColours(spdButton, false);
        } else if (tab == 2) {
            setTabColours(latButton, false);
            setTabColours(altButton, false);
            setTabColours(spdButton, true);
        } else {
            Gdx.app.log("Invalid tab", "Unknown tab number " + tab + " set!");
        }
    }

    private void setTabColours(TextButton textButton, boolean selected) {
        if (selected) {
            textButton.getStyle().down = TerminalControl.skin.getDrawable("Button_down");
            textButton.getStyle().up = TerminalControl.skin.getDrawable("Button_down");
            textButton.getStyle().fontColor = Color.WHITE;
        } else {
            textButton.getStyle().down = lightBoxBackground;
            textButton.getStyle().up = lightBoxBackground;
            textButton.getStyle().fontColor = Color.BLACK;
        }
    }

    private void loadButtons() {
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
        if (latTab.tabChanged || altTab.tabChanged || spdTab.tabChanged) {
            //Lat mode
            latTab.updateMode();

            //Alt mode
            altTab.updateMode();

            //Spd mode
            spdTab.updateMode();

            selectedAircraft.getNavState().updateState();

            resetAll();

            selectedAircraft.setActionRequired(false);
        }
    }

    private void resetTab(int tab) {
        //Reset current tab to original aircraft state
        if (tab == 0) {
            latTab.resetTab();
        } else if (tab == 1) {
            altTab.resetTab();
        } else if (tab == 2) {
            spdTab.resetTab();
        } else {
            Gdx.app.log("Invalid tab", "Unknown tab number " + tab + " specified!");
        }
    }

    private void resetAll() {
        //Reset all tabs to original aircraft state
        resetTab(0);
        resetTab(1);
        resetTab(2);
    }

    private void getChoices() {
        //Gets the choices from the boxes, sets variables to them; called after selections in selectbox changes/heading value changes
        //Lat mode tab
        latTab.getChoices();
        //Alt mode tab
        altTab.getChoices();
        //Spd mode tab
        spdTab.getChoices();
    }

    public void updateElements() {
        if (tab == 0) {
            //Lat mode tab
            latTab.updateElements();
        } else if (tab == 1) {
            //Alt mode tab
            altTab.updateElements();
        } else if (tab == 2) {
            //Spd mode tab
            spdTab.updateElements();
        }
    }

    private void compareWithAC() {
        latTab.compareWithAC();
        altTab.compareWithAC();
        spdTab.compareWithAC();
    }

    private void updateElementColours() {
        if (tab == 0) {
            latTab.updateElementColours();
        } else if (tab == 1) {
            altTab.updateElementColours();
        } else if (tab == 2) {
            spdTab.updateElementColours();
        }
    }

    public void updateResetColours() {
        if (latTab.tabChanged || altTab.tabChanged || spdTab.tabChanged) {
            resetAll.getStyle().fontColor = Color.YELLOW;
        } else {
            resetAll.getStyle().fontColor = Color.WHITE;
        }
    }

    public void addActor(Actor actor, float xRatio, float widthRatio, float y, float height) {
        actor.setPosition(xRatio * getPaneWidth(), y);
        actor.setSize(widthRatio * getPaneWidth(), height);
        radarScreen.uiStage.addActor(actor);
        actorArray.add(actor);
        actorXArray.add(xRatio);
        actorWidthArray.add(widthRatio);
    }

    public void updatePaneWidth() {
        paneImage.setSize(1080 * (float) TerminalControl.WIDTH / TerminalControl.HEIGHT, 3240);
        for (int i = 0; i < actorArray.size; i++) {
            actorArray.get(i).setX(actorXArray.get(i) * paneImage.getWidth());
            if (actorWidthArray.get(i) > 0) actorArray.get(i).setWidth(actorWidthArray.get(i) * paneImage.getWidth());
        }
        radarScreen.getCommBox().updateHeaderWidth(paneImage.getWidth());
    }

    private void updateTabVisibility(boolean show) {
        if (show) {
            if (tab == 0) {
                latTab.setVisibility(true);
                altTab.setVisibility(false);
                spdTab.setVisibility(false);
            } else if (tab == 1) {
                latTab.setVisibility(false);
                altTab.setVisibility(true);
                spdTab.setVisibility(false);
            } else if (tab == 2) {
                latTab.setVisibility(false);
                altTab.setVisibility(false);
                spdTab.setVisibility(true);
            } else {
                Gdx.app.log("Invalid tab", "Unknown tab number set");
            }
        } else {
            latTab.setVisibility(false);
            altTab.setVisibility(false);
            spdTab.setVisibility(false);
        }
        if (selectedAircraft != null) {
            labelButton.setText(selectedAircraft.getCallsign() + "    " + selectedAircraft.getIcaoType());
        }
        labelButton.setVisible(show);
    }

    public void updateScoreLabels() {
        scoreLabel.setText("Score: " + radarScreen.getScore() + "\nHigh score: " + radarScreen.getHighScore());
    }

    public void updateInfoLabel() {
        String text = "";
        if (radarScreen.speed > 1) text = radarScreen.speed + "x speed";
        if (!text.isEmpty() && radarScreen.tfcMode != RadarScreen.TfcMode.NORMAL) text += "\n";
        if (radarScreen.tfcMode == RadarScreen.TfcMode.ARRIVALS_ONLY) text += "Arrivals only";
        if (!text.isEmpty() && DayNightManager.isNight()) text += "\n";
        if (DayNightManager.isNight()) text += "Night mode active";
        if (!text.isEmpty() && radarScreen.isSectorClosed()) text += "\n";
        if (radarScreen.isSectorClosed()) text += "Sector closed";
        infoLabel.setText(text);
        infoLabel.setVisible(!text.isEmpty() && selectedAircraft == null);
    }

    public void updateAckHandButton(Aircraft aircraft) {
        if (aircraft.equals(selectedAircraft)) {
            handoverAck.setVisible(true);
            if (aircraft.canHandover()) {
                handoverAck.setText("Handover");
            } else if (aircraft.isActionRequired()) {
                handoverAck.setText("Acknow-\nledge");
            } else {
                handoverAck.setVisible(false);
            }
        }
    }

    public float getPaneWidth() {
        return paneImage.getWidth();
    }

    /** Disposes of static textures */
    public static void disposeStatic() {
        if (hdgBoxBackground == null) return;
        hdgBoxBackground.dispose();
        lightBackground.dispose();
        lightestBackground.dispose();
        paneTexture.dispose();

        hdgBoxBackground = null;
        lightBackground = null;
        lightestBackground = null;
        paneTexture = null;
    }
}
