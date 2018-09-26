package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.bombbird.atcsim.AtcSim;
import com.bombbird.atcsim.entities.Airport;
import org.apache.commons.lang3.StringUtils;

public class Ui implements Disposable {
    //private TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("game/ui/ui.atlas"));
    //private Skin skin = new Skin();
    private Texture paneTexture = new Texture(Gdx.files.internal("game/ui/UI Pane.png"));
    private Texture paneTextureUnselected = new Texture(Gdx.files.internal("game/ui/UI Pane_Normal.png"));
    private Image paneImage;
    private Image paneImageUnselected;

    private ImageButton imageButton;
    private SelectBox<String> settingsMode;

    //Resources for METAR info on default pane
    private Label.LabelStyle labelStyle;
    private Array<Label> metarInfos;

    public Ui() {
        loadNormalPane();
        loadSelectedPane();
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
            metarText[1] = "Winds: " + Integer.toString(airport.getWinds()[0]) + "@" + Integer.toString(airport.getWinds()[1]) + "kts";
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
        paneImageUnselected.setSize(1260, 3240);
        RadarScreen.uiStage.addActor(paneImageUnselected);

        labelStyle = new Label.LabelStyle();
        labelStyle.font = AtcSim.fonts.defaultFont12;
        labelStyle.fontColor = Color.BLACK;

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
            System.out.println(StringUtils.join(metarText, "\n"));
            metarInfo.setPosition(100, 2750 - index * 325);
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
        paneImage = new Image(paneTexture);
        paneImage.setPosition(0, 0);
        paneImage.setSize(1260, 3240);
        RadarScreen.uiStage.addActor(paneImage);
    }

    public void setSelectedPane(boolean show) {
        paneImage.setVisible(show);
    }

    public void resetSelectedPane() {

    }

    @Override
    public void dispose() {
        paneTexture.dispose();
        paneTextureUnselected.dispose();
    }
}
