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
    //private Texture paneTexture = new Texture(Gdx.files.internal("game/ui/UI Pane.png"));
    private Texture paneTextureUnselected = new Texture(Gdx.files.internal("game/ui/UI Pane_Normal.png"));
    private Image paneImage;
    private Image paneImageUnselected;
    private Texture boxBackground = new Texture(Gdx.files.internal("game/ui/SelectBoxBackground.png"));
    private SpriteDrawable spriteDrawable = new SpriteDrawable(new Sprite(boxBackground));

    private SelectBox<String> settingsBox;
    private SelectBox<String> valueBox;
    private Label hdgBoxBackground;
    private ImageButton hdg100add;
    private ImageButton hdg100minus;
    private ImageButton hdg10add;
    private ImageButton hdg10minus;
    private ImageButton hdg5add;
    private ImageButton hdg5minus;

    //Resources for METAR info on default pane
    private Label.LabelStyle labelStyle;
    private Array<Label> metarInfos;

    //Instructions panel info
    private Aircraft selectedAircraft;

    private int tab;
    private String latMode;
    private int clearedHdg;
    private Waypoint clearedWpt;
    private Array<String> waypoints;
    private Array<String> holdingWaypoints;

    private String altMode;
    private int clearedAlt;

    private String spdMode;
    private int clearedSpd;

    public Ui() {
        tab = 0;
        loadNormalPane();
        loadSelectedPane();
        loadSelectBox();
        loadButtons();
    }

    public void update() {
        //TODO: Allow user to give instructions to aircrafts
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
        paneImageUnselected = new Image(paneTextureUnselected);
        paneImageUnselected.setPosition(0, 0);
        paneImageUnselected.setSize(1080 * (float)AtcSim.WIDTH / AtcSim.HEIGHT, 3240);
        RadarScreen.uiStage.addActor(paneImageUnselected);

        labelStyle = new Label.LabelStyle();
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
        paneImageUnselected.setVisible(show);
        for (Label label: metarInfos) {
            label.setVisible(show);
        }
    }

    private void loadSelectedPane() {
        paneImage = new Image(paneTextureUnselected);
        paneImage.setPosition(0, 0);
        paneImage.setSize(1080 * (float)AtcSim.WIDTH / AtcSim.HEIGHT, 3240);
        RadarScreen.uiStage.addActor(paneImage);
    }

    public void setSelectedPane(boolean show, Aircraft aircraft) {
        selectedAircraft = aircraft;
        paneImage.setVisible(show);
        settingsBox.setVisible(show);
        valueBox.setVisible(show);
        hdgBoxBackground.setVisible(show);
        if (show) {
            updateBoxes(0);
        }
    }

    private void loadSelectBox() {
        //Load the select boxes to be used
        ScrollPane.ScrollPaneStyle scrollPaneStyle = new ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = MainMenuScreen.skin.getDrawable("Button_up");

        List.ListStyle listStyle = new List.ListStyle();
        listStyle.font = AtcSim.fonts.defaultFont20;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.BLACK;
        Drawable button_down = MainMenuScreen.skin.getDrawable("Button_down");
        button_down.setTopHeight(50);
        button_down.setBottomHeight(50);
        listStyle.selection = button_down;

        SelectBox.SelectBoxStyle boxStyle = new SelectBox.SelectBoxStyle();
        boxStyle.font = AtcSim.fonts.defaultFont20;
        boxStyle.fontColor = Color.WHITE;
        boxStyle.listStyle = listStyle;
        boxStyle.scrollStyle = scrollPaneStyle;
        boxStyle.background = spriteDrawable;

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

        //Valuebox for setting waypoint selections
        valueBox = new SelectBox<String>(boxStyle);
        valueBox.setItems(waypoints);
        valueBox.setPosition(0.1f * getPaneWidth(), 3240 - 1570);
        valueBox.setSize(0.8f * getPaneWidth(), 270);
        valueBox.setAlignment(Align.center);
        valueBox.getList().setAlignment(Align.center);
        RadarScreen.uiStage.addActor(valueBox);
    }

    private void loadButtons() {
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont40;
        labelStyle.fontColor = Color.WHITE;
        labelStyle.background = spriteDrawable;
        hdgBoxBackground = new Label("Test", labelStyle);
        hdgBoxBackground.setPosition(0.1f * getPaneWidth(), 3240 - 2570);
        hdgBoxBackground.setSize(0.8f * getPaneWidth(), 470);
        hdgBoxBackground.setAlignment(Align.center);
        RadarScreen.uiStage.addActor(hdgBoxBackground);
    }

    private void updateBoxes(int tab) {
        //Update box values when aircraft is selected
        if (selectedAircraft != null) {
            clearedWpt = selectedAircraft.getDirect();
            clearedAlt = selectedAircraft.getClearedAltitude();
            clearedSpd = selectedAircraft.getClearedIas();
            clearedHdg = selectedAircraft.getClearedHeading();

            if (tab == 0) {
                //Lateral mode tab
                settingsBox.setItems(selectedAircraft.getNavState().getLatModes());
            } else if (tab == 1) {
                //Altitude mode tab
                settingsBox.setItems(selectedAircraft.getNavState().getAltModes());
            } else {
                //Speed mode tab
                settingsBox.setItems(selectedAircraft.getNavState().getSpdModes());
            }
        }
    }

    private void updateChoice() {
        //Update selected choices, called upon selectbox/button change
        String newMode = settingsBox.getSelected();
        if (tab == 0) {
            //Lat mode tab
            latMode = newMode;
            if (!latMode.equals(selectedAircraft.getNavState().getLatMode())) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
            }
        } else if (tab == 1) {
            //Alt mode tab
            altMode = newMode;
            if (!altMode.equals(selectedAircraft.getNavState().getAltMode())) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
            }
        } else if (tab == 2) {
            //Spd mode tab
            spdMode = newMode;
            if (!spdMode.equals(selectedAircraft.getNavState().getSpdMode())) {
                settingsBox.getStyle().fontColor = Color.YELLOW;
            } else {
                settingsBox.getStyle().fontColor = Color.WHITE;
            }
        }
    }

    private void updateMode() {
        //Lat mode TODO: Call in OK button
        if (latMode.contains(selectedAircraft.getSidStar().getName())) {
            selectedAircraft.setLatMode("sidstar");
            System.out.println("Sidstar set");
        } else if (latMode.equals("After waypoint, fly heading")) {
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
    }

    public void resetSelectedPane() {
        tab = 0;
        clearedHdg = 360;
        clearedAlt = 5000;
        clearedSpd = 250;
    }

    public void updatePaneWidth() {
        paneImageUnselected.setSize(1080 * (float)AtcSim.WIDTH / AtcSim.HEIGHT, 3240);
        paneImage.setSize(1080 * (float)AtcSim.WIDTH / AtcSim.HEIGHT, 3240);
        settingsBox.setSize(0.8f * paneImage.getWidth(), 270);
        valueBox.setSize(0.8f * paneImage.getWidth(), 270);
        hdgBoxBackground.setSize(0.8f * paneImage.getWidth(), 470);
    }

    public float getPaneWidth() {
        return paneImage.getWidth();
    }

    @Override
    public void dispose() {
        //paneTexture.dispose();
        paneTextureUnselected.dispose();
        boxBackground.dispose();
    }
}
