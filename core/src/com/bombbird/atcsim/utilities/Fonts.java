package com.bombbird.atcsim.utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.bombbird.atcsim.AtcSim;

public class Fonts {
    private static FreeTypeFontGenerator defaultFont;
    public static BitmapFont defaultFont6;
    public static BitmapFont defaultFont8;
    public static BitmapFont defaultFont10;
    public static BitmapFont defaultFont12;
    public static BitmapFont defaultFont20;
    public static BitmapFont defaultFont30;

    public Fonts() {
        if (!AtcSim.ishtml) {
            defaultFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"));
            defaultFont6 = generateFont(defaultFont, 24);
            defaultFont8 = generateFont(defaultFont, 32);
            defaultFont10 = generateFont(defaultFont, 40);
            defaultFont12 = generateFont(defaultFont, 48);
            defaultFont20 = generateFont(defaultFont, 80);
            defaultFont30 = generateFont(defaultFont, 120);
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

    public static void dispose() {
        if (!AtcSim.ishtml) {
            defaultFont.dispose();
        }
        defaultFont6.dispose();
        defaultFont8.dispose();
        defaultFont10.dispose();
        defaultFont12.dispose();
        defaultFont20.dispose();
        defaultFont30.dispose();
    }
}
