package com.example.voidecho.entity;

import com.example.voidecho.VoidEcho;
import com.example.voidecho.entity.boss.EchoWardenEntity;
import com.example.voidecho.entity.boss.VoidStalkerEntity;
import com.example.voidecho.entity.mob.CrystalGuardianEntity;
import com.example.voidecho.entity.mob.CrystalSpriteEntity;
import com.example.voidecho.entity.mob.CrystalWraithEntity;
import com.example.voidecho.entity.mob.ShardGuardEntity;
import com.example.voidecho.entity.mob.VoidShadeEntity;
import com.example.voidecho.entity.mob.VoidWormEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.Heightmap;

public final class ModEntities {
    public static final EntityType<VoidWormEntity> VOID_WORM = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "void_worm"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VoidWormEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 0.6f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(3)
                    .build()
    );

    public static final EntityType<CrystalWraithEntity> CRYSTAL_WRAITH = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "crystal_wraith"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, CrystalWraithEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 1.6f))
                    .trackRangeChunks(8)
                    .trackedUpdateRate(3)
                    .build()
    );

    public static final EntityType<ShardGuardEntity> SHARD_GUARD = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "shard_guard"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ShardGuardEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 2.2f))
                    .trackRangeChunks(8)
                    .trackedUpdateRate(3)
                    .build()
    );

    public static final EntityType<VoidStalkerEntity> VOID_STALKER = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "void_stalker"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VoidStalkerEntity::new)
                    .dimensions(EntityDimensions.fixed(1.2f, 3.5f))
                    .trackRangeChunks(8)
                    .trackedUpdateRate(1)
                    .build()
    );

    public static final EntityType<EchoWardenEntity> ECHO_WARDEN = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "echo_warden"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, EchoWardenEntity::new)
                    .dimensions(EntityDimensions.fixed(1.4f, 4.0f))
                    .trackRangeChunks(8)
                    .trackedUpdateRate(1)
                    .build()
    );

    public static final EntityType<VoidBoltEntity> VOID_BOLT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "void_bolt"),
            FabricEntityTypeBuilder.<VoidBoltEntity>create(SpawnGroup.MISC, VoidBoltEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static final EntityType<CrystalSpriteEntity> CRYSTAL_SPRITE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "crystal_sprite"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CrystalSpriteEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 0.6f))
                    .trackRangeChunks(8)
                    .trackedUpdateRate(3)
                    .build()
    );

    public static final EntityType<CrystalGuardianEntity> CRYSTAL_GUARDIAN = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "crystal_guardian"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, CrystalGuardianEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 1.4f))
                    .trackRangeChunks(8)
                    .trackedUpdateRate(3)
                    .build()
    );

    public static final EntityType<VoidShadeEntity> VOID_SHADE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "void_shade"),
            FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, VoidShadeEntity::new)
                    .dimensions(EntityDimensions.fixed(0.8f, 2.2f))
                    .trackRangeChunks(8)
                    .trackedUpdateRate(3)
                    .build()
    );

    public static final EntityType<VoidLureBobberEntity> VOID_LURE_BOBBER = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(VoidEcho.MOD_ID, "void_lure_bobber"),
            FabricEntityTypeBuilder.<VoidLureBobberEntity>create(SpawnGroup.MISC,
                            (type, world) -> new VoidLureBobberEntity(type, world))
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(5)
                    .build()
    );

    public static void init() {
        SpawnRestriction.register(VOID_WORM, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, VoidWormEntity::canSpawn);
        SpawnRestriction.register(CRYSTAL_WRAITH, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, CrystalWraithEntity::canSpawn);
        SpawnRestriction.register(SHARD_GUARD, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, ShardGuardEntity::canSpawn);
        SpawnRestriction.register(VOID_STALKER, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, VoidStalkerEntity::canSpawn);
        SpawnRestriction.register(ECHO_WARDEN, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EchoWardenEntity::canSpawn);
        SpawnRestriction.register(CRYSTAL_SPRITE, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING, CrystalSpriteEntity::canSpawn);
    }

    private ModEntities() {}
}
