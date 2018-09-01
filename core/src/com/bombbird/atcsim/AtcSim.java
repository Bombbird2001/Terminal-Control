package com.bombbird.atcsim;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bombbird.atcsim.screens.Fonts;
import com.bombbird.atcsim.screens.MainMenuScreen;

public class AtcSim extends Game {
	public SpriteBatch batch;
	public Fonts fonts;
	
	@Override
	public void create () {
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
}
