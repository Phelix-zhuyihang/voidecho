package com.example.voidecho;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSoundEvents {
    // Void Stalker
    public static final SoundEvent ENTITY_VOID_STALKER_AMBIENT = register("entity.void_stalker.ambient");
    public static final SoundEvent ENTITY_VOID_STALKER_HURT = register("entity.void_stalker.hurt");
    public static final SoundEvent ENTITY_VOID_STALKER_DEATH = register("entity.void_stalker.death");
    public static final SoundEvent ENTITY_VOID_STALKER_SCREAM = register("entity.void_stalker.scream");
    public static final SoundEvent ENTITY_VOID_STALKER_PHASE_SHIFT = register("entity.void_stalker.phase_shift");

    // Echo Warden
    public static final SoundEvent ENTITY_ECHO_WARDEN_AMBIENT = register("entity.echo_warden.ambient");
    public static final SoundEvent ENTITY_ECHO_WARDEN_HURT = register("entity.echo_warden.hurt");
    public static final SoundEvent ENTITY_ECHO_WARDEN_DEATH = register("entity.echo_warden.death");
    public static final SoundEvent ENTITY_ECHO_WARDEN_SLASH = register("entity.echo_warden.slash");
    public static final SoundEvent ENTITY_ECHO_WARDEN_TIME_SLOW = register("entity.echo_warden.time_slow");

    // Void Worm
    public static final SoundEvent ENTITY_VOID_WORM_AMBIENT = register("entity.void_worm.ambient");
    public static final SoundEvent ENTITY_VOID_WORM_HURT = register("entity.void_worm.hurt");
    public static final SoundEvent ENTITY_VOID_WORM_DEATH = register("entity.void_worm.death");

    // Crystal Wraith
    public static final SoundEvent ENTITY_CRYSTAL_WRAITH_AMBIENT = register("entity.crystal_wraith.ambient");
    public static final SoundEvent ENTITY_CRYSTAL_WRAITH_HURT = register("entity.crystal_wraith.hurt");
    public static final SoundEvent ENTITY_CRYSTAL_WRAITH_DEATH = register("entity.crystal_wraith.death");

    // Shard Guard
    public static final SoundEvent ENTITY_SHARD_GUARD_AMBIENT = register("entity.shard_guard.ambient");
    public static final SoundEvent ENTITY_SHARD_GUARD_HURT = register("entity.shard_guard.hurt");
    public static final SoundEvent ENTITY_SHARD_GUARD_DEATH = register("entity.shard_guard.death");

    // Items
    public static final SoundEvent ITEM_VOID_STAFF_CAST = register("item.void_staff.cast");

    // Blocks
    public static final SoundEvent BLOCK_PORTAL_ACTIVATE = register("block.portal.activate");
    public static final SoundEvent BLOCK_PORTAL_AMBIENT = register("block.portal.ambient");

    // Music
    public static final SoundEvent MUSIC_VOIDS_END = register("music.voids_end");

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of(VoidEcho.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void init() {
        // Static initialisation is sufficient — calling this ensures the class loads.
    }
}
