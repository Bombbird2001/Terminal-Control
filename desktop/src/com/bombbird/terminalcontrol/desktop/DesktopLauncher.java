package com.bombbird.terminalcontrol.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.bombbird.terminalcontrol.TerminalControl;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Terminal Control");
		config.setMaximized(true);
		config.setWindowIcon("game/Icon48.png", "game/Icon32.png", "game/Icon16.png");
		config.setWindowedMode(1440, 810);
		TerminalControl.ishtml = false;
		new Lwjgl3Application(new TerminalControl(new TextToSpeechManager()), config);
	}
}
