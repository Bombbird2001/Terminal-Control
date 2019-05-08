package com.bombbird.terminalcontrol.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.entities.airports.Airport;
import com.bombbird.terminalcontrol.entities.aircrafts.Aircraft;
import com.bombbird.terminalcontrol.screens.GameScreen;
import com.bombbird.terminalcontrol.screens.RadarScreen;
import com.bombbird.terminalcontrol.utilities.Fonts;
import com.bombbird.terminalcontrol.utilities.HttpRequests;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class Ui {
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

    public LatTab latTab;
    public AltTab altTab;
    public SpdTab spdTab;

    private TextButton latButton;
    private TextButton altButton;
    private TextButton spdButton;

    private TextButton cfmChange;
    private TextButton resetAll;

    private TextButton labelButton;

    //Label that displays score & high score
    private Label scoreLabel;

    //TextButton that pauses the game
    private TextButton pauseButton;

    //Array for METAR info on default pane
    private ScrollPane metarPane;
    private Array<Label> metarInfos;

    //Instructions panel info
    private Aircraft selectedAircraft;

    private int tab;

    private RadarScreen radarScreen;

    public Ui() {
        if (hdgBoxBackground == null) {
            generatePaneTextures();
        }

        radarScreen  = TerminalControl.radarScreen;

        tab = 0;
        loadNormalPane();
        loadAircraftLabel();
        latTab = new LatTab(this);
        altTab = new AltTab(this);
        spdTab = new SpdTab(this);
        loadButtons();
        setSelectedPane(null);
    }

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

    public void updateMetar() {
        //Updates text in METAR label
        for (int i = 0; i < metarInfos.size; i++) {
            Label label = metarInfos.get(i);
            //Get airport: ICAO code is first 4 letters of label's text
            Airport airport;
            try {
                airport = radarScreen.airports.get(label.getText().toString().substring(0, 4));
            } catch (StringIndexOutOfBoundsException e) {
                String error = label.getText().toString() + "\n" + ExceptionUtils.getStackTrace(e);
                HttpRequests.sendError(error, 0);
                e.printStackTrace();
                System.out.println(label.getText().toString());
                continue;
            }
            String[] metarText = new String[5];
            metarText[0] = airport.getIcao();
            //Wind: Speed + direction
            if (airport.getWinds()[0] != 0) {
                metarText[1] = "Winds: " + airport.getWinds()[0] + "@" + airport.getWinds()[1] + "kts";
            } else {
                if (airport.getWinds()[1] != 0) {
                    metarText[1] = "Winds: VRB@" + airport.getWinds()[1] + "kts";
                } else {
                    metarText[1] = "Winds: Calm";
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
            boolean success = false;
            while (!success) {
                try {
                    label.setText(StringUtils.join(metarText, "\n"));
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
        labelButton.setSize(0.8f * getPaneWidth(), 270);
        labelButton.setPosition(0.1f * getPaneWidth(), 3240 - 695);
        labelButton.align(Align.center);
        labelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Prevents click from being sent to other elements
                event.handle();
            }
        });
        radarScreen.uiStage.addActor(labelButton);
    }

    private void loadNormalPane() {
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

        Label.LabelStyle labelStyle2 = new Label.LabelStyle();
        labelStyle2.font = Fonts.defaultFont30;
        labelStyle2.fontColor = Color.WHITE;

        scoreLabel = new Label("Score: " + radarScreen.getScore() + "\nHigh score: " + radarScreen.getHighScore() , labelStyle2);
        scoreLabel.setPosition(paneImage.getWidth() / 19.2f, 2875);
        radarScreen.uiStage.addActor(scoreLabel);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.font = Fonts.defaultFont30;
        textButtonStyle.up = lightestBoxBackground;
        textButtonStyle.down = lightestBoxBackground;
        pauseButton = new TextButton("||", textButtonStyle);
        pauseButton.setPosition(paneImage.getWidth() * 0.75f, 2900);
        pauseButton.setSize(0.2f * paneImage.getWidth(), 200);
        pauseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                //Set game state to paused
                radarScreen.setGameState(GameScreen.State.PAUSE);
                event.handle();
            }
        });
        if (!radarScreen.tutorial) radarScreen.uiStage.addActor(pauseButton);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = Fonts.defaultFont20;
        labelStyle.fontColor = Color.WHITE;

        Table metarTable = new Table();
        metarTable.align(Align.left);
        metarInfos = new Array<Label>();
        for (Airport airport: radarScreen.airports.values()) {
            String[] metarText = new String[5];
            metarText[0] = airport.getIcao();
            metarText[1] = "Winds: Loading";
            metarText[2] = "Gusting to: Loading";
            metarText[3] = "Visibility: Loading";
            metarText[4] = "Windshear: Loading\n";
            Label metarInfo = new Label(StringUtils.join(metarText, "\n"), labelStyle);
            metarInfo.setSize(paneImage.getWidth() / 2.74f, 300);
            //radarScreen.uiStage.addActor(metarInfo);
            metarInfos.add(metarInfo);
            metarTable.add(metarInfo);
            metarTable.row();
        }

        metarPane = new ScrollPane(metarTable);
        metarPane.setupFadeScrollBars(1, 1.5f);
        metarPane.setX(paneImage.getWidth() / 19.2f);
        metarPane.setY(1550);
        metarPane.setWidth(paneImage.getWidth() / 2.74f);
        metarPane.setHeight(1200);

        InputListener inputListener = null;
        for (EventListener eventListener: metarPane.getListeners()) {
            if (eventListener instanceof InputListener) {
                inputListener = (InputListener) eventListener;
            }
        }
        if (inputListener != null) metarPane.removeListener(inputListener);

        radarScreen.uiStage.addActor(metarPane);
    }

    public void setNormalPane(boolean show) {
        //Sets visibility of elements
        for (Label label: metarInfos) {
            label.setVisible(show);
        }
        scoreLabel.setVisible(show);
        pauseButton.setVisible(show);
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
        cfmChange.setSize(0.25f * getPaneWidth(), 370);
        cfmChange.setPosition(0.1f * getPaneWidth(), 3240 - 3070);
        cfmChange.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateMode();
                event.handle();
            }
        });
        radarScreen.uiStage.addActor(cfmChange);

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
        radarScreen.uiStage.addActor(resetAll);
    }

    private void loadTabButtons() {
        //Lat mode
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = Fonts.defaultFont20;
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.up = lightBoxBackground;
        textButtonStyle.down = lightBoxBackground;

        latButton = new TextButton("Lateral", textButtonStyle);
        latButton.setSize(0.25f * getPaneWidth(), 300);
        latButton.setPosition(0.1f * getPaneWidth(), 3240 - 400);
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
        radarScreen.uiStage.addActor(latButton);

        //Alt mode
        TextButton.TextButtonStyle textButtonStyle2 = new TextButton.TextButtonStyle();
        textButtonStyle2.font = Fonts.defaultFont20;
        textButtonStyle2.fontColor = Color.BLACK;
        textButtonStyle2.up = lightBoxBackground;
        textButtonStyle2.down = lightBoxBackground;

        altButton = new TextButton("Altitude", textButtonStyle2);
        altButton.setSize(0.25f * getPaneWidth(), 300);
        altButton.setPosition(0.375f * getPaneWidth(), 3240 - 400);
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
        radarScreen.uiStage.addActor(altButton);

        //Spd mode
        TextButton.TextButtonStyle textButtonStyle3 = new TextButton.TextButtonStyle();
        textButtonStyle3.font = Fonts.defaultFont20;
        textButtonStyle3.fontColor = Color.BLACK;
        textButtonStyle3.up = lightBoxBackground;
        textButtonStyle3.down = lightBoxBackground;

        spdButton = new TextButton("Speed", textButtonStyle3);
        spdButton.setSize(0.25f * getPaneWidth(), 300);
        spdButton.setPosition(0.65f * getPaneWidth(), 3240 - 400);
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
        radarScreen.uiStage.addActor(spdButton);
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

    public void updatePaneWidth() {
        paneImage.setSize(1080 * (float) TerminalControl.WIDTH / TerminalControl.HEIGHT, 3240);
        cfmChange.setSize(paneImage.getWidth() / 4, 370);
        cfmChange.setX(0.1f * paneImage.getWidth());
        resetAll.setSize(paneImage.getWidth() / 4, 370);
        resetAll.setX(0.1f * paneImage.getWidth() + 0.55f * paneImage.getWidth());
        latTab.updatePaneWidth(paneImage.getWidth());
        altTab.updatePaneWidth(paneImage.getWidth());
        spdTab.updatePaneWidth(paneImage.getWidth());
        latButton.setSize(paneImage.getWidth() / 4, 300);
        latButton.setX(0.1f * paneImage.getWidth());
        altButton.setSize(paneImage.getWidth() / 4, 300);
        altButton.setX(0.375f * paneImage.getWidth());
        spdButton.setSize(paneImage.getWidth() / 4, 300);
        spdButton.setX(0.65f * paneImage.getWidth());
        labelButton.setSize(0.8f * paneImage.getWidth(), 270);
        labelButton.setX(0.1f * paneImage.getWidth());
        scoreLabel.setX(paneImage.getWidth() / 19.2f);
        pauseButton.setX(0.75f * paneImage.getWidth());
        metarPane.setX(paneImage.getWidth() / 19.2f);
        metarPane.setWidth(paneImage.getWidth() * 5 / 6);
        radarScreen.getCommBox().updateBoxWidth(paneImage.getWidth());
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
