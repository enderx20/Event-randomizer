package com.enderx.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.function.Consumer;

public record RaffleEntry(String name, ChatFormatting color, Consumer<MinecraftServer> action) {

    // ===== TU DODAJESZ WPISY DO LISTY =====
    // new RaffleEntry("nazwa", kolor, metoda uruchamiana po wylosowaniu)
    public static final List<RaffleEntry> ENTRIES = List.of(
            new RaffleEntry("Diament", ChatFormatting.AQUA, RaffleEntry::nagrodaDiament)

    );

    // ===== TU PISZESZ KOD, KTÓRY MA SIĘ WYKONAĆ PO WYLOSOWANIU =====
    private static void nagrodaDiament(MinecraftServer server) {
        // np. dawanie przedmiotów graczom
    }

}