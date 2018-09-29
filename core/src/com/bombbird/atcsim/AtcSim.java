package com.bombbird.atcsim;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bombbird.atcsim.utilities.Fonts;
import com.bombbird.atcsim.screens.MainMenuScreen;

public class AtcSim extends Game {
	//Get screen size
	public static int WIDTH;
	public static int HEIGHT;

	//Is html? (freetype not supported in html)
	public static boolean ishtml;

	public SpriteBatch batch;

	//Set font to be usable for all classes
	public static Fonts fonts;
	
	@Override
	public void create () {
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();
		batch = new SpriteBatch();
		fonts = new Fonts();
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		fonts.dispose();
		MainMenuScreen.buttonAtlas.dispose();
		MainMenuScreen.skin.dispose();
	}

}
