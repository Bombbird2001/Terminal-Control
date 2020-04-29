package com.bombbird.terminalcontrol.desktop;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.DiscordManager;
import com.bombbird.terminalcontrol.utilities.Values;

public class DiscordRPCManager implements DiscordManager {
    @Override
    public void initializeDiscord() {
        boolean useDiscord = false;
        try {
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            DiscordRPC.INSTANCE.Discord_Initialize(TerminalControl.full ? Values.DISCORD_ID_FULL : Values.DISCORD_ID_LITE, handlers, true, null);
            System.out.println("Initialized Discord rich presence.");
            Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC.INSTANCE::Discord_Shutdown));
            useDiscord = true;
        } catch (Throwable t) {
            System.out.println("Failed to initialize Discord rich presence.");
        }

        TerminalControl.useDiscord = useDiscord;
        TerminalControl.loadedDiscord = true;
    }

    @Override
    public void updateRPC() {
        if (TerminalControl.useDiscord) {
            DiscordRichPresence discordRichPresence = new DiscordRichPresence();
            if (TerminalControl.radarScreen == null) {
                //In menu
                discordRichPresence.details = "In menu";
            } else {
                //In game
                discordRichPresence.details = "In game: " + (TerminalControl.radarScreen.tutorial ? "Tutorial" : TerminalControl.radarScreen.mainName);
                discordRichPresence.largeImageText = TerminalControl.radarScreen.mainName;
                int planes = TerminalControl.radarScreen.getPlanesInControl();
                discordRichPresence.state = planes + (planes == 1 ? " plane" : " planes") + " in control";
            }

            discordRichPresence.largeImageKey = "icon";
            DiscordRPC.INSTANCE.Discord_UpdatePresence(discordRichPresence);
        }
    }
}
