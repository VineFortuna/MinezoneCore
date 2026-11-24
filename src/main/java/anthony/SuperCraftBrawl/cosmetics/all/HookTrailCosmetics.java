package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.*;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
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

        registry.register(simple(tracker, "hook_flame", "Flame", Material.BLAZE_POWDER, EnumParticle.FLAME));
        registry.register(simple(tracker, "hook_hearts", "Hearts", Material.RED_ROSE, EnumParticle.HEART));
        registry.register(simple(tracker, "hook_magic", "Magic", Material.BREWING_STAND_ITEM, EnumParticle.SPELL_WITCH));
    }

    /** The always-unlocked hook trails have same format, different icon/name/particle. */
    private static Cosmetic simple(HookTracker tracker, String id, String displayName, Material iconMaterial,
                                   EnumParticle particle) {
        return new Cosmetic(id, CosmeticCategory.HOOK_TRAIL, displayName) {
            public ItemStack getIcon(Player player) {
                return ItemHelper.create(iconMaterial, ChatColor.YELLOW + displayName);
            }

            public boolean isUnlocked(Player player) {
                return true;
            }

            public String getUnlockMessage(Player player) {
                return "";
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

                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, false,
                        (float) location.getX(), (float) location.getY(), (float) location.getZ(),
                        0f, 0f, 0f, 0f, 1);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                }
            }
        };
    }
}