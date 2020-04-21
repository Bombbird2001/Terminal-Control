package com.bombbird.terminalcontrol.utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.bombbird.terminalcontrol.TerminalControl;

public class Fonts {
    private static FreeTypeFontGenerator defaultFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"));
    public static BitmapFont defaultFont6 = generateFont(defaultFont, 24);
    public static BitmapFont defaultFont8 = generateFont(defaultFont, 32);
    public static BitmapFont defaultFont10 = generateFont(defaultFont, 40);
    public static BitmapFont defaultFont12 = generateFont(defaultFont, 48);
    public static BitmapFont defaultFont16 = generateFont(defaultFont, 64);
    public static BitmapFont defaultFont20 = generateFont(defaultFont, 80);
    public static BitmapFont defaultFont30 = generateFont(defaultFont, 120);

    public static void generateAllFonts() {
        defaultFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"));
        defaultFont6 = generateFont(defaultFont, 24);
        defaultFont8 = generateFont(defaultFont, 32);
        defaultFont10 = generateFont(defaultFont, 40);
        defaultFont12 = generateFont(defaultFont, 48);
        defaultFont16 = generateFont(defaultFont, 64);
        defaultFont20 = generateFont(defaultFont, 80);
        defaultFont30 = generateFont(defaultFont, 120);
    }

    private static BitmapFont generateFont(FreeTypeFontGenerator generator, int size) {
        if (!TerminalControl.ishtml) {
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = size;
            parameter.minFilter = Texture.TextureFilter.Nearest;
            parameter.magFilter = Texture.TextureFilter.MipMapLinearNearest;
            generator.scaleForPixelHeight(size);
            BitmapFont font = generator.generateFont(parameter);
            font.getData().markupEnabled = true;
            return font;
        } else {
            BitmapFont bitmapFont = new BitmapFont();
            bitmapFont.getData().setScale(size / 160f * 3f);
            bitmapFont.getData().markupEnabled = true;
            return bitmapFont;
        }
    }

    public static void dispose() {
        if (!TerminalControl.ishtml && defaultFont != null) {
            defaultFont.dispose();
        }
        if (defaultFont6 != null) {
            defaultFont6.dispose();
            defaultFont8.dispose();
            defaultFont10.dispose();
            defaultFont12.dispose();
            defaultFont16.dispose();
            defaultFont20.dispose();
            defaultFont30.dispose();
        }

        defaultFont = null;
        defaultFont6 = null;
        defaultFont8 = null;
        defaultFont10 = null;
        defaultFont12 = null;
        defaultFont16 = null;
        defaultFont20 = null;
        defaultFont30 = null;
    }
}
