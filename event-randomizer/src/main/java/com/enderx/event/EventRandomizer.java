package com.enderx.event;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class EventRandomizer implements ModInitializer {

    private static Raffle current;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(Commands.literal("losuj")
                        .executes(ctx -> start(ctx.getSource())))
        );

        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            if (current != null && current.tick()) {
                current = null;
            }
        });
    }

    private static int start(CommandSourceStack source) {
        if (current != null) {
            source.sendFailure(Component.literal("Losowanie już trwa, poczekaj chwilę!"));
            return 0;
        }
        current = new Raffle(source.getServer(), RaffleEntry.ENTRIES, source.getTextName());
        return 1;
    }
}