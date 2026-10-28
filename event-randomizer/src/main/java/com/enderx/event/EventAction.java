package com.enderx.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

@FunctionalInterface
public interface EventAction {
    void run(MinecraftServer server, List<ServerPlayer> players);
}