package com.bombbird.atcsim;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bombbird.atcsim.screens.Fonts;
import com.bombbird.atcsim.screens.MainMenuScreen;

public class AtcSim extends Game {
	//Get screen size
	public static int WIDTH;
	public static int HEIGHT;

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
	}

	//Set some constant conversion methods
	public static float nmToPixel(float nm) {
		return nm * 16.2f;
	}

	public static float pixelToNm(float pixel) {
		return pixel / 16.2f;
	}

	public static float nmToFeet(float nm) {
		return nm * 6076.12f;
	}

	public static float feetToNm(float feet) {
		return feet / 6076.12f;
	}

	public static float feetToPixel(float feet) {
		return nmToPixel(feetToNm(feet));
	}
}
