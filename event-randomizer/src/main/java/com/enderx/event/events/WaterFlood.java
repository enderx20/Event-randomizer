package com.enderx.event.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WaterFlood {
    private  static  final  int HALF = 25;
    private  static  final Map<UUID, BlockPos> LAST_POS = new HashMap<>();


    public  static  void  Flood(MinecraftServer server, List<ServerPlayer> players) {
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (ServerPlayer player : players) {
            BlockPos center = player.blockPosition();

            if(center.equals(LAST_POS.get(player.getUUID()))) {
                continue;
            }
            LAST_POS.put(player.getUUID(),center);


            ServerLevel level = player.level();
            int minY = Math.max(level.getMinY(),center.getY() - HALF);
            int maxY = Math.min(level.getMaxY(),center.getY() + HALF - 1);
            for(int x = center.getX() - HALF; x < center.getX() + HALF; x ++) {
                for (int z = center.getZ() - HALF; z < center.getZ() + HALF; z ++) {
                    for(int y = minY; y <= maxY; y ++) {
                        pos.set(x,y,z);

                        if (!level.isLoaded(pos)) {
                            continue;
                        }

                        if(level.getBlockState(pos).isAir()) {
                            level.setBlock(pos,water, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }

        }

    }
}
