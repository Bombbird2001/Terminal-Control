package com.bombbird.atcsim.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

public class Fonts implements Disposable {
    private FreeTypeFontGenerator blockyFont;
    public BitmapFont blockyFont24;
    private FreeTypeFontGenerator defaultFont;
    public BitmapFont defaultFont6;
    public BitmapFont defaultFont10;
    public BitmapFont defaultFont12;
    public BitmapFont defaultFont20;
    public BitmapFont defaultFont40;

    public Fonts() {
        blockyFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/blocky.ttf"));
        blockyFont24 = generateFont(blockyFont, 24);
        defaultFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"));
        defaultFont6 = generateFont(defaultFont, 6);
        defaultFont10 = generateFont(defaultFont, 10);
        defaultFont12 = generateFont(defaultFont, 12);
        defaultFont20 = generateFont(defaultFont, 20);
        defaultFont40 = generateFont(defaultFont, 40);
    }

    private BitmapFont generateFont(FreeTypeFontGenerator generator, int size) {
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        return generator.generateFont(parameter);
    }

    @Override
    public void dispose() {
        blockyFont.dispose();
        blockyFont24.dispose();
        defaultFont.dispose();
        defaultFont6.dispose();
        defaultFont10.dispose();
        defaultFont12.dispose();
        defaultFont20.dispose();
        defaultFont40.dispose();
    }
}
