package com.enderx.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class EventLoop {

    public static volatile boolean condition = false;

    private static final List<EventLoop> ACTIVE = new ArrayList<>();

    private final EventAction code;
    private final int everyTicks;
    private int remaining;
    private int counter = 0;

    private EventLoop(int seconds, int everyTicks, EventAction code) {
        this.remaining = seconds * 20;
        this.everyTicks = Math.max(1, everyTicks);
        this.code = code;
    }

    public static void start(int seconds, int everyTicks, EventAction code) {
        ACTIVE.add(new EventLoop(seconds, everyTicks, code));
        condition = true;
    }

    public static void stopAll() {
        ACTIVE.clear();
        condition = false;
    }

    public static void tick(MinecraftServer server) {
        for (EventLoop loop : new ArrayList<>(ACTIVE)) {
            if (loop.counter % loop.everyTicks == 0) {
                // świeża lista graczy w każdym obrocie pętli
                List<ServerPlayer> players = List.copyOf(server.getPlayerList().getPlayers());
                if (!players.isEmpty()) {
                    try {
                        loop.code.run(server, players);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            loop.counter++;
            if (--loop.remaining <= 0) {
                ACTIVE.remove(loop);
            }
        }
        condition = !ACTIVE.isEmpty();
    }
}