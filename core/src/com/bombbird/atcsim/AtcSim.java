package com.bombbird.atcsim;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class AtcSim extends Game {
	SpriteBatch batch;
	private FreeTypeFontGenerator blockyFont;
	BitmapFont blockyFont24;
	private FreeTypeFontGenerator defaultFont;
	BitmapFont defaultFont12;
	BitmapFont defaultFont40;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		blockyFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/blocky.ttf"));
		blockyFont24 = generateFont(blockyFont, 24);
		defaultFont = new FreeTypeFontGenerator(Gdx.files.internal("fonts/default.ttf"));
		defaultFont12 = generateFont(defaultFont, 12);
		defaultFont40 = generateFont(defaultFont, 40);
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		blockyFont.dispose();
		defaultFont.dispose();
		blockyFont24.dispose();
		defaultFont12.dispose();
		defaultFont40.dispose();
	}

	private BitmapFont generateFont(FreeTypeFontGenerator generator, int size) {
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = size;
		return generator.generateFont(parameter);
	}
}
