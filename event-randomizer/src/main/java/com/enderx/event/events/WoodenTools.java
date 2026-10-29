package com.enderx.event.events;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class WoodenTools {
    public  static  void  replaceWoddenTools (MinecraftServer server, List<ServerPlayer> players) {
        for (ServerPlayer player :players) {
            Inventory inventory = player.getInventory();
            int replaced = 0;

            for (int i = 0; i <inventory.getContainerSize(); i ++ ) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty() ) continue;

                Item wooden = getWoodenEquivalent(stack);
                if (wooden == null || stack.is(wooden)) continue;

                ItemStack replacement = new ItemStack(wooden, stack.getCount());
                if (stack.has(DataComponents.ENCHANTMENTS)) {
                    replacement.set(DataComponents.ENCHANTMENTS, stack.get(DataComponents.ENCHANTMENTS));
                }
                if (stack.has(DataComponents.CUSTOM_NAME)) {
                    replacement.set(DataComponents.CUSTOM_NAME, stack.get(DataComponents.CUSTOM_NAME));
                }

                inventory.setItem(i,replacement);
                replaced ++;
            }
            if (replaced > 0) {
                player.containerMenu.broadcastChanges();
            }

        }
    }

    private static Item getWoodenEquivalent(ItemStack stack) {
        if (stack.is(ItemTags.SWORDS))   return Items.WOODEN_SWORD;
        if (stack.is(ItemTags.PICKAXES)) return Items.WOODEN_PICKAXE;
        if (stack.is(ItemTags.AXES))     return Items.WOODEN_AXE;
        if (stack.is(ItemTags.SHOVELS))  return Items.WOODEN_SHOVEL;
        if (stack.is(ItemTags.HOES))     return Items.WOODEN_HOE;
        return null;
    }
}
