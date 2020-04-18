package com.bombbird.terminalcontrol.desktop;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.ToastManager;
import com.bombbird.terminalcontrol.utilities.Values;

public class DesktopLauncher {
	public static void main (String[] arg) {
		boolean useDiscord = false;
		try {
			DiscordEventHandlers handlers = new DiscordEventHandlers();
			DiscordRPC.INSTANCE.Discord_Initialize(Values.DISCORD_ID, handlers, true, null);
			System.out.println("Initialized Discord rich presence.");
			Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC.INSTANCE::Discord_Shutdown));
			useDiscord = true;
		} catch (Throwable t) {
			System.out.println("Failed to initialize Discord rich presence.");
		}

		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setTitle("Terminal Control");
		config.setWindowIcon("game/Icon48.png", "game/Icon32.png", "game/Icon16.png");
		config.setWindowedMode(1440, 810);
		config.setMaximized(true);
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
		TerminalControl.ishtml = false;
		TerminalControl.useDiscord = useDiscord;
		new Lwjgl3Application(new TerminalControl(new TextToSpeechManager(), new ToastManager() {}, new DiscordRPCManager()), config);
	}
}
