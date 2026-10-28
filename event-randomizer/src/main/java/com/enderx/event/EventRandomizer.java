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

            dispatcher.register(Commands.literal("losuj")
                    .executes(ctx -> start(ctx.getSource())));

            // ===== KOMENDY DEBUGOWANIA =====
            dispatcher.register(Commands.literal("eventdebug")
                    .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))   // tylko OP   // tylko OP

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

            // automatyczne losowanie
            if (current == null && !EventLoop.condition) {
                if (++autoCounter >= AUTO_INTERVAL_TICKS) {
                    autoCounter = 0;
                    startRaffle(server, "Automat");
                }
            } else {
                autoCounter = 0;   // odliczanie do następnego losowania zaczyna się dopiero po końcu eventu
            }
        });

    }
    private static int autoCounter = 0;
    private static final int AUTO_INTERVAL_TICKS = 10 * 60 * 20;   // co 10 minut

    /** Startuje losowanie; zwraca false, gdy nie można (trwa losowanie, trwa pętla lub brak graczy). */
    public static boolean startRaffle(MinecraftServer server, String starter) {
        if (current != null || EventLoop.condition) return false;
        if (server.getPlayerList().getPlayers().isEmpty()) return false;
        current = new Raffle(server, RaffleEntry.ENTRIES, starter);
        return true;
    }

    private static int start(CommandSourceStack source) {
        if (!startRaffle(source.getServer(), source.getTextName())) {
            source.sendFailure(Component.literal("Nie można teraz losować (trwa losowanie/event albo brak graczy)!"));
            return 0;
        }
        return 1;





    }

    private static int stopAll(CommandSourceStack source) {
        EventLoop.stopAll();
        source.sendSuccess(() -> Component.literal("Zatrzymano wszystkie pętle.")
                .withStyle(ChatFormatting.YELLOW), true);
        return 1;
    }

    private static int list(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Wpisy: ").withStyle(ChatFormatting.GRAY)
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
                    source.sendFailure(Component.literal("Tę komendę musi wykonać gracz!"));
                    return 0;
                }
                entry.action().run(source.getServer(), List.copyOf(source.getServer().getPlayerList().getPlayers()));
                source.sendSuccess(() -> Component.literal("Uruchomiono event: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(entry.name())
                                .withStyle(entry.color(), ChatFormatting.BOLD)), true);
                return 1;
            }
        }
        source.sendFailure(Component.literal("Nie ma takiego eventu: " + name
                + " (użyj /eventdebug list)"));
        return 0;
    }
}