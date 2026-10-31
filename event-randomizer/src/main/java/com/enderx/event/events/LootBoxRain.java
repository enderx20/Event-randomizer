package com.enderx.event.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LootBoxRain {

    private static final ResourceKey<LootTable> CHEST_LOOT = ResourceKey.create(
            Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("event", "chests/chests"));

    private static final Component CHEST_NAME = Component.literal("Op LootBox :)")
            .withStyle(ChatFormatting.GOLD);

    private static final List<FallingBlockEntity> ACTIVE = new ArrayList<>();

    private static final double MAX_FALL_SPEED = 0.6D;

    public static void BoxRain(MinecraftServer server, List<ServerPlayer> players) {
        final double chancePerTick = 0.01D;
        final double minDistance = 5D;
        final double maxDistance = 40D;

        tickActiveChests();

        for (ServerPlayer player : players) {
            ServerLevel level = player.level();
            RandomSource random = level.getRandom();

            if (random.nextDouble() > chancePerTick) {
                continue;
            }

            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);

            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            double y = player.getY() + 30.0D + random.nextDouble() * 15.0D;

            BlockPos pos = BlockPos.containing(x, y, z);

            BlockState chestState = Blocks.CHEST.defaultBlockState()
                    .setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));

            FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, chestState);
            falling.blockData = createChestData(level);
            falling.setHurtsEntities(2.0F, 40);
            falling.setGlowingTag(true);

            ACTIVE.add(falling);

            level.playSound(null, x, y, z, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.AMBIENT, 4.0F, 0.8F);
            level.sendParticles(ParticleTypes.FIREWORK, x, y, z, 30, 0.5D, 0.5D, 0.5D, 0.1D);
        }
    }

    public static void tickActiveChests() {
        Iterator<FallingBlockEntity> it = ACTIVE.iterator();

        while (it.hasNext()) {
            FallingBlockEntity chest = it.next();

            if (!(chest.level() instanceof ServerLevel level)) {
                it.remove();
                continue;
            }

            double x = chest.getX();
            double y = chest.getY();
            double z = chest.getZ();

            if (chest.isRemoved()) {
                level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.5D, z, 3, 0.4D, 0.2D, 0.4D, 0.0D);
                level.sendParticles(ParticleTypes.CLOUD, x, y + 0.2D, z, 40, 0.6D, 0.1D, 0.6D, 0.15D);
                level.sendParticles(ParticleTypes.FIREWORK, x, y + 0.5D, z, 25, 0.5D, 0.5D, 0.5D, 0.2D);
                level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(),
                        SoundSource.AMBIENT, 1.5F, 1.4F);
                it.remove();
                continue;
            }

            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 1.0D, z, 3, 0.2D, 0.3D, 0.2D, 0.01D);
            level.sendParticles(ParticleTypes.FLAME, x, y + 0.5D, z, 4, 0.2D, 0.2D, 0.2D, 0.02D);
            level.sendParticles(ParticleTypes.END_ROD, x, y + 1.5D, z, 2, 0.15D, 0.4D, 0.15D, 0.0D);

            Vec3 motion = chest.getDeltaMovement();
            if (motion.y < -MAX_FALL_SPEED) {
                chest.setDeltaMovement(motion.x, -MAX_FALL_SPEED, motion.z);
            }
        }
    }



    private static CompoundTag createChestData(ServerLevel level) {
        ChestBlockEntity chest = new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
        chest.setLootTable(CHEST_LOOT);

        CompoundTag tag = chest.saveWithoutMetadata(level.registryAccess());

        Tag name = ComponentSerialization.CODEC
                .encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), CHEST_NAME)
                .getOrThrow();
        tag.put("CustomName", name);

        return tag;
    }
}