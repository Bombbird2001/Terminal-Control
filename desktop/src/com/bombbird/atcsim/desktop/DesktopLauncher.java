package com.bombbird.atcsim.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.bombbird.atcsim.AtcSim;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "ATC Sim";
		config.width = 1440;
		config.height = 810;
		config.forceExit = false;
		new LwjglApplication(new AtcSim(), config);
	}
}
