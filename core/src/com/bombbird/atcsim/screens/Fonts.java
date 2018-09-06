package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;
import com.bombbird.atcsim.AtcSim;

public class Fonts implements Disposable {
    private FreeTypeFontGenerator blockyFont;
    public BitmapFont blockyFont24;
    private FreeTypeFontGenerator defaultFont;
    public BitmapFont defaultFont6;
    public BitmapFont defaultFont8;
    public BitmapFont defaultFont10;
    public BitmapFont defaultFont12;
    public BitmapFont defaultFont20;
    public BitmapFont defaultFont40;

    public Fonts() {
        if (!AtcSim.ishtml) {
            blockyFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/blocky.ttf"));
            blockyFont24 = generateFont(blockyFont, 96);
            defaultFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"));
            defaultFont6 = generateFont(defaultFont, 24);
            defaultFont8 = generateFont(defaultFont, 32);
            defaultFont10 = generateFont(defaultFont, 40);
            defaultFont12 = generateFont(defaultFont, 48);
            defaultFont20 = generateFont(defaultFont, 80);
            defaultFont40 = generateFont(defaultFont, 160);
        } else {
            defaultFont6 = new BitmapFont();
            defaultFont6.getData().setScale(0.45f);
            defaultFont8 = new BitmapFont();
            defaultFont8.getData().setScale(0.6f);
            defaultFont10 = new BitmapFont();
            defaultFont10.getData().setScale(0.75f);
            defaultFont12 = new BitmapFont();
            defaultFont12.getData().setScale(0.9f);
            defaultFont20 = new BitmapFont();
            defaultFont20.getData().setScale(1.5f);
            defaultFont40 = new BitmapFont();
            defaultFont40.getData().setScale(3);
        }
    }

    private BitmapFont generateFont(FreeTypeFontGenerator generator, int size) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.minFilter = Texture.TextureFilter.Nearest;
        parameter.magFilter = Texture.TextureFilter.MipMapLinearNearest;
        generator.scaleForPixelHeight(size);
        return generator.generateFont(parameter);
    }

    @Override
    public void dispose() {
        if (!AtcSim.ishtml) {
            blockyFont.dispose();
            blockyFont24.dispose();
            defaultFont.dispose();
        }
        defaultFont6.dispose();
        defaultFont8.dispose();
        defaultFont10.dispose();
        defaultFont12.dispose();
        defaultFont20.dispose();
        defaultFont40.dispose();
    }
}
