package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;

public class Ui implements Disposable {
    //private TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("game/ui/ui.atlas"));
    //private Skin skin = new Skin();
    private Texture paneTexture = new Texture(Gdx.files.internal("game/ui/UI Pane.png"));
    private Image paneImage;
    private ImageButton imageButton;
    private SelectBox<String> selectBox;

    public Ui() {
        //skin.addRegions(atlas);
        paneImage = new Image(paneTexture);
        paneImage.setPosition(0, 0);
        paneImage.setSize(1260, 3240);
        RadarScreen.uiStage.addActor(paneImage);
    }

    @Override
    public void dispose() {
        paneTexture.dispose();
    }
}
