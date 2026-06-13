package com.example.voidecho.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Void Echo Journal — a guide book that unpacks into a written book
 * explaining the mod's progression.  Right-click to receive the guide.
 */
public class VoidEchoJournalItem extends Item {
    public VoidEchoJournalItem() {
        super(new Settings().rarity(Rarity.UNCOMMON).maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);

        // Create the written guide book
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, createBookContent(world.getRegistryManager()));

        // Give to player
        if (!user.getInventory().insertStack(book)) {
            user.dropItem(book, false);
        }

        // Consume the journal
        stack.decrement(1);
        return TypedActionResult.success(stack);
    }

    private WrittenBookContentComponent createBookContent(RegistryWrapper.WrapperLookup registries) {
        List<RawFilteredPair<Text>> pages = List.of(
            page("  §lVoid Echo: Ancient Sanctum§r\n  §l虚空回响：远古圣殿§r\n\n"
               + "§oA journey into the Void's End.§r\n§o一场通往终虚渊的旅程。§r\n\n"
               + "This journal records the knowledge needed to survive and conquer the void.\n"
               + "这本书记录了在虚空中生存与征服所需的知识。"),
            page("  §lChapter 1: Getting Started§r\n  §l第一章：入门§r\n\n"
               + "§nStep 1:§r Explore the overworld to find a §dForgotten Altar§r — a circular stone platform "
               + "with void portal frames and crystal pillars, found in plains and deserts.\n\n"
               + "§n第一步：§r 在主世界探索，寻找§d遗忘祭坛§r——一个由虚空传送门框架和水晶柱组成的圆形石台，"
               + "出现在平原和沙漠群系。"),
            page("  §lChapter 2: The Void Key§r\n  §l第二章：虚空钥匙§r\n\n"
               + "§nRecipe (primary):§r\n"
               + "Netherite Scrap × 1\nEnder Pearl × 1\nCrystal Shard × 3\n\n"
               + "§nRecipe (alternate):§r\n"
               + "Quartz × 3\nEnder Pearl × 1\nCrystal Shard × 3\n\n"
               + "Right-click the portal frame at the Forgotten Altar with the Void Key to activate it."),
            page("  §lChapter 3: The Void's End§r\n  §l第三章：终虚渊§r\n\n"
               + "You have entered a realm of eternal midnight. Four biomes await:\n\n"
               + "§bVoid Plains§r — barren, blue-tinted wastes\n"
               + "§aCrystal Forest§r — shimmering crystal flora\n"
               + "§6Void Wastes§r — harsh, red-brown badlands\n"
               + "§5Crystal Caverns§r — deep underground riches\n\n"
               + "§cBed do not work here!§r Use respawn anchors instead."),
            page("  §lChapter 4: Crystal Shards§r\n  §l第四章：水晶碎片§r\n\n"
               + "§dCrystal Shards§r are the backbone resource. Gather them by:\n\n"
               + "- Mining §dCrystal Ore§r (found in void stone)\n"
               + "- Defeating §3Crystal Wraiths§r (0-3 shards)\n"
               + "- Defeating Void Worms (0-2 shards)\n"
               + "- Breaking §dCrystal Bloom§r plants\n"
               + "- Harvesting from §dEcho Shards§r in structures"),
            page("  §lChapter 5: Void Alloy Equipment§r\n  §l第五章：虚空合金装备§r\n\n"
               + "Smelt §dCrystal Shards§r into §dCrystal Blocks§r, then combine with obsidian and iron to forge "
               + "§5Void Alloy Ingots§r.\n\n"
               + "Craft the full set:\n"
               + "- §5Void Sword§r — right-click to teleport\n"
               + "- §5Void Bow§r — +4 void damage\n"
               + "- §5Void Staff§r — fires void bolts\n"
               + "- §5Void Armor§r — speed per piece, full set grants fall/void immunity"),
            page("  §lChapter 6: Void Stalker§r\n  §l第六章：虚空追猎者§r\n\n"
               + "The first boss. Find it in the §bVoid Plains§r.\n\n"
               + "§cThree Phases:§r\n"
               + "Phase 1 §7(100%-60% HP)§r — teleports behind you\n"
               + "Phase 2 §7(60%-30% HP)§r — summons void worms, energy shield, void beams\n"
               + "Phase 3 §7(<30% HP)§r — enraged, rapid combos, AOE pulses\n\n"
               + "Drops: §5Void Heart§r (used to summon Echo Warden)"),
            page("  §lChapter 7: Echo Warden§r\n  §l第七章：回声守卫§r\n\n"
               + "The final boss. Summon it at the §dEcho Altar§r inside the §5Echo Sanctum§r "
               + "(found in Crystal Forest).\n\n"
               + "Place a §5Void Heart§r on the altar surrounded by §dCrystal Blocks§r.\n\n"
               + "§cAbilities:§r Full-screen slash, blink strike, time slow, passive teleport.\n\n"
               + "Drops: §5Echo Core§r + Void Alloy Ingots + Crystal Shards"),
            page("  §lChapter 8: Endgame§r\n  §l第八章：终局§r\n\n"
               + "With the §5Echo Core§r, craft:\n"
               + "- §5Echo Amulet§r — grants Strength+Speed+Resistance in the Void's End\n"
               + "- §5Echo Tome§r — grants Night Vision in the Void's End\n\n"
               + "Use the §5Void Forge§r to upgrade your equipment:\n"
               + "- Durability upgrades (up to +200)\n"
               + "- Echo upgrades (+damage/armor)\n"
               + "- Rift upgrades (special effects)\n\n"
               + "§6Close Void Rifts§r for Rift Fragments — used in advanced forging."),
            page("  §lAppendix: Tips & Tricks§r\n  §l附录：技巧与提示§r\n\n"
               + "- Always carry a §5Respawn Anchor§r (crafted with 4 Void Alloy + 4 Crystal Blocks)\n"
               + "- The §5Echo Amulet§r only works in the Void's End dimension\n"
               + "- Wearing 4 pieces of Void Armor makes you immune to void damage\n"
               + "- §5Crystal Sprites§r can be tamed with Crystal Berries\n"
               + "- Feed a Sprite a §5Void Catalyst§r to evolve it into a §5Crystal Guardian§r\n"
               + "- Close Void Rifts by standing near them for 3 seconds\n"
               + "- Collect all 5 Echo Memory Shards for lore and the Echo Historian advancement"),
            page("  §lThe Story So Far§r\n  §l故事背景§r\n\n"
               + "§oLong before your arrival, the civilization of §dAerolith§r thrived in this dimension.§r\n"
               + "§o在你到来之前，§d艾洛斯§r文明曾在此繁荣。§r\n\n"
               + "Their king, §6Aerion IV§r, ruled over a crystal empire. Crystal could store memory — "
               + "their greatest discovery, and their ultimate undoing. For the Void feeds on memory itself.\n\n"
               + "§5Kaelen§r, the king's dearest friend and captain of the royal guard, took the Void into his own body "
               + "to protect Aerion. He became the §5Void Stalker§r — still fighting, somewhere deep inside.\n\n"
               + "King §cAerion IV§r sealed his own consciousness inside a giant crystal to hold the Void gate shut. "
               + "He is the §cEcho Warden§r. Every moment awake drains what remains of his soul.\n\n"
               + "§oThe Void won, centuries ago. But you carry the echo of everything Aerolith was. "
               + "What will you do with it?§r")
        );

        return new WrittenBookContentComponent(
            RawFilteredPair.of("Void Echo Journal"),
            "Aerolith",
            0, // generation 0 = original
            pages,
            true // resolved
        );
    }

    private static RawFilteredPair<Text> page(String raw) {
        return RawFilteredPair.of(Text.literal(raw));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.void_echo.journal").formatted(Formatting.GRAY));
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
