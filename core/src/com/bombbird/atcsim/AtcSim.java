package com.bombbird.atcsim;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.bombbird.atcsim.utilities.Fonts;
import com.bombbird.atcsim.screens.MainMenuScreen;

public class AtcSim extends Game {
	//Get screen size
	public static int WIDTH;
	public static int HEIGHT;

	//Is html? (freetype not supported in html)
	public static boolean ishtml;

	//Create texture stuff
	private static TextureAtlas buttonAtlas;
	public static Skin skin;

	//The one and only spritebatch
	public SpriteBatch batch;
	
	@Override
	public void create () {
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();
		batch = new SpriteBatch();

		buttonAtlas = new TextureAtlas(Gdx.files.internal("game/ui/mainmenubuttons.atlas"));
		skin = new Skin();
		skin.addRegions(AtcSim.buttonAtlas);

		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		Fonts.dispose();
		buttonAtlas.dispose();
		skin.dispose();
	}

}
