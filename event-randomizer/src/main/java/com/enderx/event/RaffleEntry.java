package com.enderx.event;

import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record RaffleEntry(String name, ChatFormatting color, String subtitle, EventAction action) {

    /** Uruchamia metodę jeden raz. */
    public static RaffleEntry once(String name, ChatFormatting color, String subtitle, EventAction method) {
        return new RaffleEntry(name, color, subtitle, method);
    }

    /** Powtarza metodę co 'everyTicks' ticków przez 'seconds' sekund. */
    public static RaffleEntry loop(String name, ChatFormatting color, String subtitle,
                                   int seconds, int everyTicks, EventAction method) {
        return new RaffleEntry(name, color, subtitle,
                (server, players) -> EventLoop.start(seconds, everyTicks, method));
    }

    // ===== WPISY =====
    public static final List<RaffleEntry> ENTRIES = List.of(
            once("Charged Creeper", ChatFormatting.AQUA, "Creeper but more dangerous", RaffleEntry::chargedCreeper),
            loop("TnT", ChatFormatting.DARK_RED, "Uciekajcie, spada TNT!", 4 * 60 + 55, 20, RaffleEntry::tntRain)
    );

    // ===== METODY =====

    private static void chargedCreeper(MinecraftServer server, List<ServerPlayer> players) {
        runCommand(server, "execute at @a run summon minecraft:creeper ~ ~ ~ {powered:1b}");
    }

    private static void tntRain(MinecraftServer server, List<ServerPlayer> players) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        // losowe przesunięcie nad każdym graczem
        runCommand(server, "execute at @a run summon minecraft:tnt ~"
                + (r.nextInt(11) - 5) + " ~12 ~" + (r.nextInt(11) - 5) + " {fuse:60}");
    }

    private static void runCommand(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput(), command);
    }
}