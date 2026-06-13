package com.example.voidecho;

import com.example.voidecho.block.ModBlocks;
import com.example.voidecho.config.ModConfig;
import com.example.voidecho.enchantment.ModEnchantments;
import com.example.voidecho.entity.ModEntities;
import com.example.voidecho.entity.boss.EchoWardenEntity;
import com.example.voidecho.entity.boss.VoidStalkerEntity;
import com.example.voidecho.entity.mob.CrystalSpriteEntity;
import com.example.voidecho.entity.mob.CrystalGuardianEntity;
import com.example.voidecho.entity.mob.CrystalWraithEntity;
import com.example.voidecho.entity.mob.ShardGuardEntity;
import com.example.voidecho.entity.mob.VoidWormEntity;
import com.example.voidecho.item.ModItemGroups;
import com.example.voidecho.item.ModItems;
import com.example.voidecho.item.ModToolMaterials;
import com.example.voidecho.item.armor.VoidArmorMaterial;
import com.example.voidecho.network.ModNetwork;
import com.example.voidecho.world.biome.ModBiomes;
import com.example.voidecho.world.dimension.ModDimensions;
import com.example.voidecho.world.event.CrystalBloomEvent;
import com.example.voidecho.world.event.VoidRiftManager;
import com.example.voidecho.world.feature.ModFeatures;
import com.example.voidecho.world.structure.ModStructurePieceTypes;
import com.example.voidecho.world.structure.ModStructures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoidEcho implements ModInitializer {
    public static final String MOD_ID = "void_echo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** The voids_end dimension registry key, used throughout the mod. */
    public static final RegistryKey<World> VOIDS_END_DIMENSION_KEY =
            RegistryKey.of(RegistryKeys.WORLD, Identifier.of(MOD_ID, "voids_end"));

    @Override
    public void onInitialize() {
        LOGGER.info("Void Echo: Ancient Sanctum - The void calls...");

        // --- Config ---
        ModConfig.load();

        // --- Registry init ---
        ModSoundEvents.init();
        ModParticleTypes.init();
        ModEnchantments.init();
        ModBlocks.init();
        ModItems.init();
        ModToolMaterials.init();
        VoidArmorMaterial.init();
        ModItemGroups.init();
        com.example.voidecho.effect.ModEffects.init();
        ModEntities.init();
        ModBiomes.init();
        ModDimensions.init();
        ModStructurePieceTypes.init();
        ModStructures.init();
        ModFeatures.init();

        // Add crystal ore generation to overworld biomes
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.UNDERGROUND_ORES,
            ModFeatures.CRYSTAL_ORE_OVERWORLD_PLACED
        );

        ModNetwork.init();

        // --- Entity attributes ---
        FabricDefaultAttributeRegistry.register(
                ModEntities.VOID_WORM, VoidWormEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(
                ModEntities.CRYSTAL_WRAITH, CrystalWraithEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(
                ModEntities.SHARD_GUARD, ShardGuardEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(
                ModEntities.VOID_STALKER, VoidStalkerEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(
                ModEntities.ECHO_WARDEN, EchoWardenEntity.createMobAttributes());

        // F11: Crystal Sprite
        FabricDefaultAttributeRegistry.register(
                ModEntities.CRYSTAL_SPRITE, CrystalSpriteEntity.createMobAttributes());

        // B2: Crystal Guardian
        FabricDefaultAttributeRegistry.register(
                ModEntities.CRYSTAL_GUARDIAN, CrystalGuardianEntity.createMobAttributes());

        // --- Fuel ---
        FuelRegistry.INSTANCE.add(ModBlocks.VOID_STONE, 800);

        // --- Entity combat events (boss kill tracking) ---
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed) -> {
            if (killed instanceof VoidStalkerEntity) {
                LOGGER.info("Void Stalker defeated at {}", killed.getBlockPos());
            }
            if (killed instanceof EchoWardenEntity) {
                LOGGER.info("Echo Warden defeated at {}", killed.getBlockPos());
            }
        });

        // --- F11: Crystal Bloom Event ---
        CrystalBloomEvent.register();

        // --- F12: Void Rift Invasion ---
        VoidRiftManager.register();

        // Advancement completion checker
        com.example.voidecho.advancement.VoidMasterHandler.register();

        // --- First entry into Void's End: show title ---
        net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD
                .register((player, origin, destination) -> {
            if (destination.getRegistryKey().equals(VOIDS_END_DIMENSION_KEY)) {
                var title = net.minecraft.text.Text.translatable("dimension.void_echo.voids_end");
                var subtitle = net.minecraft.text.Text.translatable("message.void_echo.void_first_entry");
                player.networkHandler.sendPacket(
                    new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 70, 20));
                player.networkHandler.sendPacket(
                    new net.minecraft.network.packet.s2c.play.TitleS2CPacket(title));
                player.networkHandler.sendPacket(
                    new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(subtitle));
            }
        });

        // --- Give Void Echo Journal to new players ---
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            boolean hasJournal = handler.player.getInventory().contains(
                    new ItemStack(ModItems.VOID_ECHO_JOURNAL));
            if (!hasJournal) {
                ItemStack journal = new ItemStack(ModItems.VOID_ECHO_JOURNAL);
                if (!handler.player.getInventory().insertStack(journal)) {
                    handler.player.dropItem(journal, false);
                }
            }
        });

        LOGGER.info("Void Echo: Ancient Sanctum initialization complete.");
    }
}
