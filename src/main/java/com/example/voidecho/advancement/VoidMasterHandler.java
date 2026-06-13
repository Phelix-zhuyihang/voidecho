package com.example.voidecho.advancement;

import com.example.voidecho.VoidEcho;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Periodically checks whether players have completed all prerequisite
 * advancements for the void_master challenge and grants its criteria.
 */
public class VoidMasterHandler {
    private static final Identifier VOID_MASTER_ID = Identifier.of(VoidEcho.MOD_ID, "void_master");
    private static final String[] PREREQUISITES = {
            "root", "find_crystal", "craft_key", "find_altar", "enter_void",
            "void_explorer", "find_fortress", "vault_loot", "defeat_stalker",
            "void_alloy", "full_armor", "all_weapons", "find_sanctum",
            "summon_warden", "defeat_warden"
    };
    private static final String[] CRITERIA_NAMES = {
            "completed_root", "completed_find_crystal", "completed_craft_key",
            "completed_find_altar", "completed_enter_void", "completed_void_explorer",
            "completed_find_fortress", "completed_vault_loot", "completed_defeat_stalker",
            "completed_void_alloy", "completed_full_armor", "completed_all_weapons",
            "completed_find_sanctum", "completed_summon_warden", "completed_defeat_warden"
    };
    private static int tickCounter = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter % 60 != 0) return; // Check every 3 seconds

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerAdvancementTracker tracker = player.getAdvancementTracker();
                AdvancementEntry voidMaster = server.getAdvancementLoader().get(VOID_MASTER_ID);
                if (voidMaster == null) continue;

                AdvancementProgress progress = tracker.getProgress(voidMaster);
                if (progress.isDone()) continue; // Already completed

                boolean allDone = true;
                for (int i = 0; i < PREREQUISITES.length; i++) {
                    Identifier advId = Identifier.of(VoidEcho.MOD_ID, PREREQUISITES[i]);
                    AdvancementEntry entry = server.getAdvancementLoader().get(advId);
                    if (entry == null) {
                        allDone = false;
                        break;
                    }
                    AdvancementProgress advProgress = tracker.getProgress(entry);
                    if (!advProgress.isDone()) {
                        allDone = false;
                        break;
                    }
                }

                if (allDone) {
                    // Grant all criteria for void_master
                    for (String criterion : CRITERIA_NAMES) {
                        tracker.grantCriterion(voidMaster, criterion);
                    }
                }
            }
        });
    }
}
