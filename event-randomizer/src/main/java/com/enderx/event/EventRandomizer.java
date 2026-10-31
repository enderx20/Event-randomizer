package com.enderx.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class EventRandomizer implements ModInitializer {

    private static Raffle current;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(Commands.literal("draw")
                    .executes(ctx -> start(ctx.getSource())));

            // ===== KOMENDY DEBUGOWANIA =====
            dispatcher.register(Commands.literal("eventdebug")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

                    // /eventdebug stop
                    .then(Commands.literal("stop")
                            .executes(ctx -> stopAll(ctx.getSource())))

                    // /eventdebug list
                    .then(Commands.literal("list")
                            .executes(ctx -> list(ctx.getSource())))

                    // /eventdebug run <nazwa>
                    .then(Commands.literal("run")
                            .then(Commands.argument("name", StringArgumentType.greedyString())
                                    .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                            RaffleEntry.ENTRIES.stream().map(RaffleEntry::name), builder))
                                    .executes(ctx -> run(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "name"))))));
        });

        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            if (current != null && current.tick()) {
                current = null;
            }
            EventLoop.tick(server);
            EventTimer.tick(server, current != null);
        });

    }


    public static boolean startRaffle(MinecraftServer server, String starter) {
        if (current != null || EventLoop.condition) return false;
        if (server.getPlayerList().getPlayers().isEmpty()) return false;
        current = new Raffle(server, RaffleEntry.ENTRIES, starter);
        return true;
    }

    private static int start(CommandSourceStack source) {
        if (!startRaffle(source.getServer(), source.getTextName())) {
            source.sendFailure(Component.literal("You cannot draw now"));
            return 0;
        }
        return 1;





    }

    private static int stopAll(CommandSourceStack source) {
        EventLoop.stopAll();
        source.sendSuccess(() -> Component.literal("Stopped all loops")
                .withStyle(ChatFormatting.YELLOW), true);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Entries: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.join(", ",
                                RaffleEntry.ENTRIES.stream().map(RaffleEntry::name).toList()))
                        .withStyle(ChatFormatting.YELLOW)), false);
        source.sendSuccess(() -> Component.literal("EventLoop.condition = " + EventLoop.condition)
                .withStyle(EventLoop.condition ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        return 1;
    }

    private static int run(CommandSourceStack source, String name) {
        for (RaffleEntry entry : RaffleEntry.ENTRIES) {
            if (entry.name().equalsIgnoreCase(name)) {
                ServerPlayer player = source.getPlayer();
                if (player == null) {

                    return 0;
                }
                entry.action().run(source.getServer(), List.copyOf(source.getServer().getPlayerList().getPlayers()));
                source.sendSuccess(() -> Component.literal("Started event: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(entry.name())
                                .withStyle(entry.color(), ChatFormatting.BOLD)), true);
                return 1;
            }
        }
        source.sendFailure(Component.literal("Event doesn't exist " + name
                + " (use /eventdebug list)"));
        return 0;
    }
}