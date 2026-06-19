package com.example.voidecho.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Appends forge upgrade tier labels to item tooltips
 * when the item has void_echo upgrade NBT tags.
 */
public class ForgeUpgradeTooltip {
    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (customData == null) return;
            NbtCompound nbt = customData.copyNbt();
            if (nbt.isEmpty()) return;

            // Header
            boolean hasAny = false;
            for (String key : nbt.getKeys()) {
                if (key.startsWith("void_echo:")) { hasAny = true; break; }
            }
            if (!hasAny) return;

            lines.add(Text.empty());
            lines.add(Text.translatable("tooltip.void_echo.forge_header")
                    .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));

            // T1
            if (nbt.getBoolean("void_echo:echo_upgrade")) {
                lines.add(Text.translatable("tooltip.void_echo.echo_upgrade")
                        .formatted(Formatting.LIGHT_PURPLE));
            }
            if (nbt.getBoolean("void_echo:rift_upgrade")) {
                lines.add(Text.translatable("tooltip.void_echo.rift_upgrade")
                        .formatted(Formatting.DARK_AQUA));
            }

            // T2
            if (nbt.getBoolean("void_echo:echo_strike")) {
                lines.add(Text.translatable("tooltip.void_echo.echo_strike")
                        .formatted(Formatting.DARK_PURPLE));
            }
            if (nbt.getBoolean("void_echo:echo_guard")) {
                lines.add(Text.translatable("tooltip.void_echo.echo_guard")
                        .formatted(Formatting.DARK_PURPLE));
            }
            if (nbt.getBoolean("void_echo:rift_walker")) {
                lines.add(Text.translatable("tooltip.void_echo.rift_walker")
                        .formatted(Formatting.DARK_AQUA));
            }
            if (nbt.getBoolean("void_echo:rift_fury")) {
                lines.add(Text.translatable("tooltip.void_echo.rift_fury")
                        .formatted(Formatting.DARK_AQUA));
            }

            // T3
            if (nbt.getBoolean("void_echo:void_resonance")) {
                lines.add(Text.translatable("tooltip.void_echo.void_resonance")
                        .formatted(Formatting.BLUE));
            }
            if (nbt.getBoolean("void_echo:crystal_barrier")) {
                lines.add(Text.translatable("tooltip.void_echo.crystal_barrier")
                        .formatted(Formatting.BLUE));
            }

            // T4
            if (nbt.getBoolean("void_echo:crystal_resonance")) {
                lines.add(Text.translatable("tooltip.void_echo.crystal_resonance")
                        .formatted(Formatting.GOLD));
            }

            // Repair
            int repairs = nbt.getInt("void_echo:forge_repaired");
            if (repairs > 0) {
                lines.add(Text.translatable("tooltip.void_echo.forge_repaired", repairs)
                        .formatted(Formatting.GRAY));
            }
        });
    }
}
