package com.bombbird.atcsim.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.bombbird.atcsim.AtcSim;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("ATC Sim");
		config.setMaximized(true);
		config.setWindowedMode(1440, 810);
		AtcSim.ishtml = false;
		new Lwjgl3Application(new AtcSim(), config);
	}
}
