package com.bombbird.terminalcontrol.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.ToastManager;

public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Terminal Control");
		config.setWindowIcon("game/Icon48.png", "game/Icon32.png", "game/Icon16.png");
		config.setWindowedMode(1440, 810);
		config.setMaximized(true);
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
		TerminalControl.ishtml = false;
		new Lwjgl3Application(new TerminalControl(new TextToSpeechManager(), new ToastManager() {
			@Override
			public void saveFail(GdxRuntimeException e) {
				//No default implementation
			}

			@Override
			public void readStorageFail() {
				//No default implementation
			}

			@Override
			public void jsonParseFail() {
				//No default implementation
			}
		}), config);
	}
}
