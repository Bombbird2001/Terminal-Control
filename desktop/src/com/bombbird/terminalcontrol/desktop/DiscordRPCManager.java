package com.bombbird.terminalcontrol.desktop;

import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.bombbird.terminalcontrol.TerminalControl;
import com.bombbird.terminalcontrol.utilities.DiscordManager;

public class DiscordRPCManager implements DiscordManager {
    @Override
    public void updateRPC() {
        if (TerminalControl.useDiscord) {
            DiscordRichPresence discordRichPresence = new DiscordRichPresence();
            if (TerminalControl.radarScreen == null) {
                //In menu
                discordRichPresence.details = "In menu";
            } else {
                //In game
                discordRichPresence.details = "In game: " + TerminalControl.radarScreen.mainName;
                discordRichPresence.largeImageText = TerminalControl.radarScreen.mainName;
                discordRichPresence.state = TerminalControl.radarScreen.getPlanesInControl() + " planes in control";
            }

            discordRichPresence.largeImageKey = "iconlite";
            DiscordRPC.INSTANCE.Discord_UpdatePresence(discordRichPresence);
        }
    }
}
