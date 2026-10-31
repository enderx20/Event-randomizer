package com.enderx.event.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.Random;

public class ThunderStorm {
    private  static  final  double CHANCE_PER_TICK = 0.05D;
    private  static  final  double MAX_DISTANCE = 40D;
    private  static  final  double MIN_DISTANCE = 2D;
    private  static  final  int STORM_DURATION_TICKS = (4 * 60 +55) * 20;
    private  static  int ticksLeft = -1;

    public static  void thunder (MinecraftServer server, List<ServerPlayer> players) {
        if (ticksLeft == -1) {
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack().withSuppressedOutput(),
                    "weather thunder " + STORM_DURATION_TICKS + "t"
            );
            ticksLeft = STORM_DURATION_TICKS;
        }


        if (ticksLeft == 0) {
            ticksLeft = -1; // gotowe na kolejny start
            return;
        }
        ticksLeft--;

        for(ServerPlayer player : players) {
            ServerLevel level = player.level();
            RandomSource random = level.getRandom();

            if (random.nextDouble() > CHANCE_PER_TICK) {
                continue;
            }

            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;

            int blockX = Mth.floor(x);
            int blockZ = Mth.floor(z);

            if(!level.hasChunk(blockX >> 4, blockZ >> 4)) {
                continue;
            }

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX,blockZ);

            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack().withSuppressedOutput(),
                    String.format(java.util.Locale.ROOT,
                            "execute in %s run summon minecraft:lightning_bolt %f %d %f",
                            level.dimension().identifier(), x, y, z)
            );


        }


    }
}
