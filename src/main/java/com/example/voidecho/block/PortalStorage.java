package com.example.voidecho.block;

import com.example.voidecho.VoidEcho;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalStorage extends PersistentState {
    private static final Type<PortalStorage> TYPE = new Type<>(
            PortalStorage::new, PortalStorage::fromNbt, DataFixTypes.LEVEL);
    private static final int MAX_ENTRIES = 500;

    private final Map<UUID, BlockPos> returnPositions = new HashMap<>();

    public PortalStorage() {}

    public static PortalStorage get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, "void_echo_portals");
    }

    public void setReturnPosition(UUID playerUuid, BlockPos pos) {
        returnPositions.put(playerUuid, pos);
        // Prevent unbounded growth on long-running servers
        if (returnPositions.size() > MAX_ENTRIES) {
            // Remove a random entry to keep size in check (simple eviction)
            UUID keyToRemove = returnPositions.keySet().iterator().next();
            returnPositions.remove(keyToRemove);
        }
        markDirty();
    }

    public BlockPos getReturnPosition(UUID playerUuid, BlockPos defaultPos) {
        BlockPos result = returnPositions.getOrDefault(playerUuid, defaultPos);
        // Clean up this entry after retrieval — portal positions are single-use
        if (returnPositions.containsKey(playerUuid)) {
            returnPositions.remove(playerUuid);
            markDirty();
        }
        return result;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();
        for (Map.Entry<UUID, BlockPos> entry : returnPositions.entrySet()) {
            NbtCompound entryTag = new NbtCompound();
            entryTag.putUuid("uuid", entry.getKey());
            entryTag.putLong("pos", entry.getValue().asLong());
            list.add(entryTag);
        }
        nbt.put("returnPositions", list);
        return nbt;
    }

    private static PortalStorage fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        PortalStorage storage = new PortalStorage();
        NbtList list = nbt.getList("returnPositions", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entryTag = list.getCompound(i);
            UUID uuid = entryTag.getUuid("uuid");
            BlockPos pos = BlockPos.fromLong(entryTag.getLong("pos"));
            storage.returnPositions.put(uuid, pos);
        }
        return storage;
    }
}
