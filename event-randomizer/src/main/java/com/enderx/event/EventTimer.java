package com.enderx.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EventTimer {

    private static final int INTERVAL_TICKS = 10 *20;

    private static final ServerBossEvent BAR = new ServerBossEvent(
            UUID.randomUUID(),
            Component.empty(),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.PROGRESS);
    private static int remaining = INTERVAL_TICKS;
    private static String currentEvent = null;
    private static boolean drawing = false;

    /** Wywoływane z Raffle.reveal() */
    public static void setCurrentEvent(String name) {
        currentEvent = name;
    }

    public static void tick(MinecraftServer server, boolean raffleRunning) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();


        if (players.isEmpty()) {
            BAR.removeAllPlayers();
            remaining = INTERVAL_TICKS;
            currentEvent = null;
            drawing = false;
            return;
        }


        for (ServerPlayer p : players) {
            if (!BAR.getPlayers().contains(p)) {
                BAR.addPlayer(p);
            }
        }
        for (ServerPlayer p : new ArrayList<>(BAR.getPlayers())) {
            if (!players.contains(p)) {
                BAR.removePlayer(p);
            }
        }

        // trwa losowanie - czekamy aż się skończy
        if (drawing) {
            if (raffleRunning) {
                BAR.setName(Component.literal("Drawing..."));
                BAR.setProgress(1.0f);
                return;
            }
            drawing = false;
            remaining = INTERVAL_TICKS;
        }

        remaining--;

        if (remaining <= 0) {
            EventLoop.stopAll();                       // = /eventdebug stop
            if (EventRandomizer.startRaffle(server, "Automat")) { // = /draw
                drawing = true;
            } else {
                remaining = 1; // spróbuj ponownie w następnym ticku
            }
            return;
        }

        int totalSeconds = remaining / 20;
        String time = String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
        String text = (currentEvent == null)
                ? "Next event in " + time
                : currentEvent + " | Next event in " + time;

        BAR.setName(Component.literal(text));
        BAR.setProgress(Math.max(0f, Math.min(1f, (float) remaining / INTERVAL_TICKS)));
    }
}