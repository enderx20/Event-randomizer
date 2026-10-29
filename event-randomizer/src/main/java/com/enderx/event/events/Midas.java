package com.enderx.event.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class Midas {
    public static void replaceBlocks(MinecraftServer server, List<ServerPlayer> players) {
        BlockState replacement = Blocks.GOLD_BLOCK.defaultBlockState();

        for (ServerPlayer player : players) {
            HitResult hit = player.pick(64.0D, 1.0F, false);

            if (hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult blockHit)) {
                continue;
            }

            BlockPos pos = blockHit.getBlockPos();
            Level level = player.level();
            BlockState current = level.getBlockState(pos);

            if (current.isAir()
                    || current.getDestroySpeed(level, pos) < 0
                    || current.is(replacement.getBlock())
                    || level.getBlockEntity(pos) != null) {
                continue;
            }

            level.setBlockAndUpdate(pos, replacement);
        }
    }
}
