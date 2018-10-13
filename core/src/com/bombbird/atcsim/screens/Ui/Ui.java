package com.bombbird.atcsim.screens.Ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import com.bombbird.atcsim.entities.aircrafts.Aircraft;
import com.bombbird.atcsim.screens.RadarScreen;
import org.apache.commons.lang3.StringUtils;

public class Ui implements Disposable {
    private static Texture hdgBoxBackground = new Texture(Gdx.files.internal("game/ui/BoxBackground.png"));
    private static Texture paneTexture = new Texture(Gdx.files.internal("game/ui/UI Pane_Normal.png"));
    private static Texture lightBackground = new Texture(Gdx.files.internal("game/ui/lightBoxBackground.png"));
    private static Texture lightestBackground = new Texture(Gdx.files.internal("game/ui/lightestBoxBackground.png"));
    private Image paneImage;
    public static SpriteDrawable hdgBoxBackgroundDrawable = new SpriteDrawable(new Sprite(hdgBoxBackground));
    public static SpriteDrawable lightBoxBackground = new SpriteDrawable(new Sprite(lightBackground));
    public static SpriteDrawable lightestBoxBackground = new SpriteDrawable(new Sprite(lightestBackground));

    public LatTab latTab;
    public AltTab altTab;
    public SpdTab spdTab;

    private TextButton latButton;
    private TextButton altButton;
    private TextButton spdButton;

    private TextButton cfmChange;
    private TextButton resetAll;

    //Array for METAR info on default pane
    private Array<Label> metarInfos;

    //Instructions panel info
    private Aircraft selectedAircraft;

    private int tab;

    public Ui() {
        tab = 0;
        loadNormalPane();
        latTab = new LatTab(this);
        altTab = new AltTab(this);
        spdTab = new SpdTab(this);
        loadButtons();
        setSelectedPane(null);
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
            metarInfo.setPosition(100, 2775 - index * 575);
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
    }

    private void loadTabButtons() {
        //Lat mode
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = AtcSim.fonts.defaultFont20;
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.up = hdgBoxBackgroundDrawable;
        textButtonStyle.down = hdgBoxBackgroundDrawable;

        latButton = new TextButton("Lateral", textButtonStyle);
        latButton.setSize(0.25f * getPaneWidth(), 370);
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
        RadarScreen.uiStage.addActor(latButton);

        //Alt mode
        TextButton.TextButtonStyle textButtonStyle2 = new TextButton.TextButtonStyle();
        textButtonStyle2.font = AtcSim.fonts.defaultFont20;
        textButtonStyle2.fontColor = Color.BLACK;
        textButtonStyle2.up = hdgBoxBackgroundDrawable;
        textButtonStyle2.down = hdgBoxBackgroundDrawable;

        altButton = new TextButton("Altitude", textButtonStyle2);
        altButton.setSize(0.25f * getPaneWidth(), 370);
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
        RadarScreen.uiStage.addActor(altButton);

        //Spd mode
        TextButton.TextButtonStyle textButtonStyle3 = new TextButton.TextButtonStyle();
        textButtonStyle3.font = AtcSim.fonts.defaultFont20;
        textButtonStyle3.fontColor = Color.BLACK;
        textButtonStyle3.up = hdgBoxBackgroundDrawable;
        textButtonStyle3.down = hdgBoxBackgroundDrawable;

        spdButton = new TextButton("Speed", textButtonStyle3);
        spdButton.setSize(0.25f * getPaneWidth(), 370);
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
        RadarScreen.uiStage.addActor(spdButton);
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
        latTab.updateMode();

        //Alt mode
        altTab.updateMode();

        //Spd mode
        spdTab.updateMode();

        resetAll();
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
            Gdx.app.log("Invalid tab", "Unknown tab number " + Integer.toString(tab) + " specified!");
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
        paneImage.setSize(1080 * (float)AtcSim.WIDTH / AtcSim.HEIGHT, 3240);
        cfmChange.setSize(paneImage.getWidth() / 4, 370);
        cfmChange.setX(0.1f * paneImage.getWidth());
        resetAll.setSize(paneImage.getWidth() / 4, 370);
        resetAll.setX(0.1f * paneImage.getWidth() + 0.55f * paneImage.getWidth());
        latTab.updatePaneWidth(paneImage.getWidth());
        altTab.updatePaneWidth(paneImage.getWidth());
        spdTab.updatePaneWidth(paneImage.getWidth());
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
    }

    public float getPaneWidth() {
        return paneImage.getWidth();
    }

    @Override
    public void dispose() {
        hdgBoxBackground.dispose();
        lightBackground.dispose();
        lightestBackground.dispose();
        paneTexture.dispose();
    }
}
