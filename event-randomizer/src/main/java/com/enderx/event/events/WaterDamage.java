package com.enderx.event.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WaterDamage {
    private static final  double RADIUS = 30.0D;
    private static  final  float DAMAGE = 2.0F;
    private  static  final  int INTERVAL_TICKS = 10;

    public static void damage(MinecraftServer server, List<ServerPlayer> players) {
        if (server.getTickCount() % INTERVAL_TICKS != 0) {
            return;
        }
        Set<Entity> victims = new HashSet<>();

        for(ServerPlayer player : players) {
            ServerLevel level = player.level();
            AABB area = player.getBoundingBox().inflate(RADIUS);
            for (Entity entity : level.getEntities((Entity) null, area, e -> true)) {
                if (entity.isInWater()  && entity.distanceToSqr(player) <= RADIUS * RADIUS) {
                    victims.add(entity);

                }

            }

        }
        for (Entity entity : victims) {
            if (entity.level() instanceof  ServerLevel level) {
                DamageSource source = level.damageSources().drown();
                entity.hurtServer(level,source,DAMAGE);
            }
        }




    }
}