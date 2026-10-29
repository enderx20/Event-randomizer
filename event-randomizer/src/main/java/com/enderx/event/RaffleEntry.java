package com.enderx.event;

import com.enderx.event.events.FireballRain;
import com.enderx.event.events.Midas;
import com.enderx.event.events.WoodenTools;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record RaffleEntry(String name, ChatFormatting color, String subtitle, EventAction action) {


    public static RaffleEntry once(String name, ChatFormatting color, String subtitle, EventAction method) {
        return new RaffleEntry(name, color, subtitle, method);
    }


    public static RaffleEntry loop(String name, ChatFormatting color, String subtitle,
                                   int seconds, int everyTicks, EventAction method) {
        return new RaffleEntry(name, color, subtitle,
                (server, players) -> EventLoop.start(seconds, everyTicks, method));
    }

    // ===== WPISY =====
    public static final List<RaffleEntry> ENTRIES = List.of(
            once("Charged Creeper", ChatFormatting.AQUA, "Creeper but more dangerous", RaffleEntry::chargedCreeper),
            loop("Midas", ChatFormatting.YELLOW, "Get rich", 4 * 60 + 55, 1, Midas::replaceBlocks),
            loop("FireBall rain", ChatFormatting.DARK_PURPLE, "Dodge the meteors", 4 * 60 + 55, 1, FireballRain::fireballRain),
            once("Loot chest", ChatFormatting.GOLD, "Get some OP loot",RaffleEntry::lootChest),
            once("Wodden tools", ChatFormatting.YELLOW, "Replaces all of your tools with wodden", WoodenTools::replaceWoddenTools)
    );

    // ===== METODY =====

    private static void chargedCreeper(MinecraftServer server, List<ServerPlayer> players) {
        runCommand(server, "execute at @a run summon minecraft:creeper ~ ~ ~ {powered:1b}");
    }

    private  static  void lootChest(MinecraftServer server, List<ServerPlayer> players) {
        runCommand(server, "give @a minecraft:chest[custom_name={\"color\":\"gold\",\"italic\":false,\"text\":\"OP LootBox :)\"},container=[{slot:0,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:1,item:{id:\"minecraft:gold_ingot\",count:1}},{slot:2,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:3,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:4,item:{id:\"minecraft:iron_ingot\",count:2}},{slot:5,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:6,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:7,item:{id:\"minecraft:gold_ingot\",count:1}},{slot:8,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:9,item:{id:\"minecraft:gold_ingot\",count:1}},{slot:10,item:{id:\"minecraft:coal\",count:10}},{slot:11,item:{id:\"minecraft:gold_ingot\",count:1}},{slot:12,item:{id:\"minecraft:iron_ingot\",count:2}},{slot:13,item:{id:\"minecraft:diamond\",count:10}},{slot:14,item:{id:\"minecraft:iron_ingot\",count:2}},{slot:15,item:{id:\"minecraft:gold_ingot\",count:1}},{slot:16,item:{id:\"minecraft:coal\",count:10}},{slot:17,item:{id:\"minecraft:gold_ingot\",count:1}},{slot:18,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:19,item:{id:\"minecraft:gold_ingot\",count:1}},{slot:20,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:21,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:22,item:{id:\"minecraft:iron_ingot\",count:2}},{slot:23,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:24,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}},{slot:25,item:{id:\"minecraft:gold_ingot\",count:1}},{slot:26,item:{id:\"minecraft:yellow_stained_glass_pane\",count:1}}]]");
    }


    private static void runCommand(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput(), command);
    }
}