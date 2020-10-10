package com.bombbird.terminalcontrol.desktop

import club.minnced.discord.rpc.DiscordEventHandlers
import club.minnced.discord.rpc.DiscordRPC
import club.minnced.discord.rpc.DiscordRichPresence
import com.bombbird.terminalcontrol.TerminalControl
import com.bombbird.terminalcontrol.utilities.DiscordManager
import com.bombbird.terminalcontrol.utilities.Values

class DiscordRPCManager : DiscordManager {
    override fun initializeDiscord() {
        var useDiscord = false
        try {
            val handlers = DiscordEventHandlers()
            DiscordRPC.INSTANCE.Discord_Initialize(if (TerminalControl.full) Values.DISCORD_ID_FULL else Values.DISCORD_ID_LITE, handlers, true, null)
            println("Initialized Discord rich presence.")
            Runtime.getRuntime().addShutdownHook(Thread { DiscordRPC.INSTANCE.Discord_Shutdown() })
            useDiscord = true
        } catch (t: Throwable) {
            println("Failed to initialize Discord rich presence.")
        }
        TerminalControl.useDiscord = useDiscord
        TerminalControl.loadedDiscord = true
    }

    override fun updateRPC() {
        if (TerminalControl.useDiscord) {
            val discordRichPresence = DiscordRichPresence()
            if (TerminalControl.radarScreen == null) {
                //In menu
                discordRichPresence.details = "In menu"
            } else {
                //In game
                discordRichPresence.details = "In game: " + if (TerminalControl.radarScreen.tutorial) "Tutorial" else TerminalControl.radarScreen.mainName
                discordRichPresence.largeImageText = TerminalControl.radarScreen.mainName
                val planes = TerminalControl.radarScreen.planesInControl
                discordRichPresence.state = planes.toString() + (if (planes == 1) " plane" else " planes") + " in control"
            }
            discordRichPresence.largeImageKey = "icon"
            DiscordRPC.INSTANCE.Discord_UpdatePresence(discordRichPresence)
        }
    }
}