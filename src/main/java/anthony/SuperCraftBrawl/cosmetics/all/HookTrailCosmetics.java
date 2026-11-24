package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.*;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class HookTrailCosmetics {

    private HookTrailCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        HookTracker tracker = new HookTracker(main);

        registry.register(snowflake(main, tracker));
        registry.register(golden(main, tracker));
        registry.register(blue(main, tracker));
    }

    /** The Golden hook trail, locked to the LEGENDARY rank. */
    private static Cosmetic golden(Core main, HookTracker tracker) {
        return new Cosmetic("hook_golden", CosmeticCategory.HOOK_TRAIL, "Golden",
                Rank.LEGENDARY.getTag() + " Exclusive") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.create(Material.GOLD_NUGGET, ChatColor.GOLD + "Golden"));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.legendary");
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need the rank ") + Rank.LEGENDARY.getTag()
                        + ChatColor.RESET + " to use this!";
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }

            @Override
            public long getTickPeriod() {
                return 2L;
            }

            @Override
            public void tick(Player player) {
                Location location = tracker.getFlyingLocation(player);
                if (location == null) return;

                // Count 0 makes the 1.8 client read the offsets as an RGB colour instead of a spread, so a
                // second, slightly offset packet is sent to thicken the trail without losing the colour.
                // A red offset of exactly 0 is read as full red, so it's kept just above 0 here.
                PacketPlayOutWorldParticles packet1 = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, false,
                        (float) location.getX(), (float) location.getY(), (float) location.getZ(),
                        1f, 0.92f, 0f, 1f, 0);
                PacketPlayOutWorldParticles packet2 = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, false,
                        (float) location.getX() + 0.1f, (float) location.getY() + 0.1f, (float) location.getZ() - 0.1f,
                        1f, 0.92f, 0f, 1f, 0);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet1);
                    ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet2);
                }
            }
        };
    }

    /** The Blue hook trail, locked to the PRO rank. */
    private static Cosmetic blue(Core main, HookTracker tracker) {
        return new Cosmetic("hook_blue", CosmeticCategory.HOOK_TRAIL, "Blue",
                Rank.PRO.getTag() + " Exclusive") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.createDye(DyeColor.BLUE, 1, ChatColor.BLUE + "Blue"));
            }

            public boolean isUnlocked(Player player) {
                return player.hasPermission("scb.pro");
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need the rank ") + Rank.PRO.getTag()
                        + ChatColor.RESET + " to use this!";
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }

            @Override
            public long getTickPeriod() {
                return 2L;
            }

            @Override
            public void tick(Player player) {
                Location location = tracker.getFlyingLocation(player);
                if (location == null) return;

                // Count 0 makes the 1.8 client read the offsets as an RGB colour instead of a spread, so a
                // second, slightly offset packet is sent to thicken the trail without losing the colour.
                // A red offset of exactly 0 is read as full red, so it's kept just above 0 here.
                PacketPlayOutWorldParticles packet1 = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, false,
                        (float) location.getX(), (float) location.getY(), (float) location.getZ(),
                        0.001f, 0.4f, 1f, 1f, 0);
                PacketPlayOutWorldParticles packet2 = new PacketPlayOutWorldParticles(EnumParticle.REDSTONE, false,
                        (float) location.getX() + 0.1f, (float) location.getY() + 0.1f, (float) location.getZ() - 0.1f,
                        0.001f, 0.4f, 1f, 1f, 0);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet1);
                    ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet2);
                }
            }
        };
    }

    /** The Snowflake hook trail, unlocked by finding 7 Christmas presents. */
    private static Cosmetic snowflake(Core main, HookTracker tracker) {
        return new Cosmetic("hook_snowflake", CosmeticCategory.HOOK_TRAIL, "Snowflake",
                "Christmas 2026 Exclusive") {
            public ItemStack getIcon(Player player) {
                int found = main.getChristmasManager() != null
                        ? main.getChristmasManager().getFoundCount(player.getUniqueId()) : 0;
                return withRequirementLore(ItemHelper.setDetails(new ItemStack(Material.SNOW_BALL), ChatColor.AQUA + "Snowflake",
                        main.color("&7Progress: &e" + Math.min(found, 7) + "&7/7 presents")));
            }

            public boolean isUnlocked(Player player) {
                int found = main.getChristmasManager() != null
                        ? main.getChristmasManager().getFoundCount(player.getUniqueId()) : 0;
                return found >= 7;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need to find &e7 presents &rto use &bSnowflake &rhook trail.");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }

            @Override
            public long getTickPeriod() {
                return 2L;
            }

            @Override
            public void tick(Player player) {
                Location location = tracker.getFlyingLocation(player);
                if (location == null) return;

                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.SNOW_SHOVEL, false,
                        (float) location.getX(), (float) location.getY(), (float) location.getZ(),
                        0.05f, 0.05f, 0.05f, 0f, 2);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                }
            }
        };
    }
}