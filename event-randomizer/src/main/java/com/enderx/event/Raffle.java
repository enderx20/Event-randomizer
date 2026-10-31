package com.enderx.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class Raffle {

    private static final int FRAMES = 30;      // ile nazw przeleci
    private static final int MAX_DELAY = 12;   // ostatnia przerwa w tickach (zatrzymanie)

    private final MinecraftServer server;
    private final List<RaffleEntry> entries;
    private final int resultIndex;
    private final String starter;

    private final int[] frameTicks = new int[FRAMES];
    private final int revealAt;

    private int age = 0;
    private int nextFrame = 0;
    private int lastShown = -1;
    private boolean revealed = false;



    public Raffle(MinecraftServer server, List<RaffleEntry> entries, String starter) {
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("List is empty");
        }
        this.server = server;
        this.entries = List.copyOf(entries);
        this.starter = starter;


        this.resultIndex = ThreadLocalRandom.current().nextInt(this.entries.size());


        int t = 0;
        for (int i = 0; i < FRAMES; i++) {
            frameTicks[i] = t;
            double x = (double) i / (FRAMES - 1);
            t += 1 + (int) Math.round(x * x * x * (MAX_DELAY - 1));
        }
        this.revealAt = t;

        send(new ClientboundSetTitlesAnimationPacket(0, 20, 0));

    }

    public boolean tick() {
        if (nextFrame < FRAMES && age >= frameTicks[nextFrame]) {
            showFrame(nextFrame);
            nextFrame++;
        } else if (!revealed && age >= revealAt) {
            reveal();
        } else if (revealed) {
            int since = age - revealAt;
            if (since % 4 == 0 && since < 60) {
                celebrate();
            }
            if (since >= 70) {
                return true;
            }
        }
        age++;
        return false;
    }

    private void showFrame(int i) {
        boolean last = (i == FRAMES - 1);
        int idx;

        if (entries.size() == 1) {
            idx = 0;
        } else if (last) {
            idx = resultIndex;
        } else if (i == FRAMES - 2) {
            idx = randomIndexExcluding(lastShown, resultIndex);
        } else {
            idx = randomIndexExcluding(lastShown, -1);
        }
        lastShown = idx;

        RaffleEntry entry = entries.get(idx);
        Component title = Component.literal(entry.name()).withStyle(entry.color(), ChatFormatting.BOLD);
        Component subtitle = Component.literal("iii ").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.DARK_GRAY)
                .append(Component.literal("Losowanie").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" iii").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.DARK_GRAY));

        send(new ClientboundSetTitleTextPacket(title));
        send(new ClientboundSetSubtitleTextPacket(subtitle));

        float progress = (float) i / (FRAMES - 1);
        float pitch = 0.5f + 1.5f * progress;
        playAll(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, pitch);
    }

    private int randomIndexExcluding(int a, int b) {
        List<Integer> allowed = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (i != a && i != b) allowed.add(i);
        }
        if (allowed.isEmpty()) {
            for (int i = 0; i < entries.size(); i++) {
                if (i != a) allowed.add(i);
            }
        }
        if (allowed.isEmpty()) {
            return 0;
        }
        return allowed.get(ThreadLocalRandom.current().nextInt(allowed.size()));
    }


    private void reveal() {
        revealed = true;
        RaffleEntry result = entries.get(resultIndex);
        EventTimer.setCurrentEvent(result.name());

        send(new ClientboundSetTitlesAnimationPacket(0, 70, 20));
        send(new ClientboundSetTitleTextPacket(
                Component.literal(result.name()).withStyle(result.color(), ChatFormatting.BOLD)));

// subtitle z wpisu; gdy pusty - domyślne "Wylosowano!"
        Component subtitle;
        if (result.subtitle() == null || result.subtitle().isBlank()) {
            subtitle = Component.literal("★ ").withStyle(ChatFormatting.YELLOW);

        } else {
            subtitle = Component.literal(result.subtitle()).withStyle(ChatFormatting.YELLOW);
        }
        send(new ClientboundSetSubtitleTextPacket(subtitle));
        playAll(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
        playAll(SoundEvents.TOTEM_USE, 0.6f, 1.2f);

        MutableComponent line = Component.literal("                                        ")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.STRIKETHROUGH);



        celebrate();

        List<ServerPlayer> players = List.copyOf(server.getPlayerList().getPlayers());
        if (!players.isEmpty()) {
            result.action().run(server, players);
        }
    }

    private void celebrate() {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            ServerLevel level = (ServerLevel) p.level();
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    p.getX(), p.getY() + 1.0, p.getZ(), 25, 0.6, 0.8, 0.6, 0.4);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    p.getX(), p.getY() + 1.0, p.getZ(), 6, 0.7, 0.7, 0.7, 0.0);
        }
    }

    private void playAll(SoundEvent sound, float volume, float pitch) {
        Holder<SoundEvent> holder = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.connection.send(new ClientboundSoundPacket(
                    holder, SoundSource.MASTER,
                    p.getX(), p.getY(), p.getZ(),
                    volume, pitch,
                    ThreadLocalRandom.current().nextLong()));
        }
    }

    private void send(Packet<?> packet) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.connection.send(packet);
        }
    }
}