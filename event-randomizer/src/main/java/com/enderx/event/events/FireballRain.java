package com.enderx.event.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class FireballRain {
    public static void fireballRain (MinecraftServer server, List<ServerPlayer> players) {
        final double chancePerTick = 0.08D;
        final double minDistance = 5D ;
        final double maxDistance = 40D;
        final int explosionPower = new Random().nextInt(10) + 1;

        for (ServerPlayer player: players) {
            ServerLevel level = player.level();
            RandomSource random = level.getRandom();

            if (random.nextDouble() > chancePerTick) {
                continue;
            }

            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance =  minDistance + random.nextDouble() * (maxDistance - minDistance);

            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            double y = player.getY() + 30.0D + random.nextDouble() * 15.0D;

            Vec3 movement= new Vec3(
                    (random.nextDouble() - 0.5D) * 0.3D,
                    -1.0D,
                    (random.nextDouble() - 0.5D) * 0.3D
            );

            LargeFireball fireball = new LargeFireball(level,player,movement,explosionPower);
            fireball.setPos(x,y,z);
            level.addFreshEntity(fireball);



        }
    }

}
