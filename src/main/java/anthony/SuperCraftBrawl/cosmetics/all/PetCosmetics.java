package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRegistry;
import anthony.util.ItemHelper;
import anthony.util.PathfinderGoalFollowPlayer;
import anthony.util.PathfinderHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PetCosmetics {

    private PetCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        registry.register(snowman(main));
    }

    private static Cosmetic snowman(Core main) {
        final Map<UUID, Snowman> pets = new HashMap<>();

        return new Cosmetic("snowman", CosmeticCategory.PET, "Snowman Pet",
                "Christmas 2024 Exclusive") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(ItemHelper.create(Material.MONSTER_EGG), "&bSnowman Pet"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).snowmanPet == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this yet!");
            }

            public void onEquip(Player player) {
                Snowman existing = pets.remove(player.getUniqueId());
                if (existing != null) {
                    try {
                        existing.remove();
                    } catch (Throwable ignored) {
                    }
                }

                Location spawnLoc = player.getLocation().add(1, 0, 1);
                Snowman snowman = player.getWorld().spawn(spawnLoc, Snowman.class);
                snowman.setCustomName(ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Snowman");
                pets.put(player.getUniqueId(), snowman);

                EntityLiving targetPlayer = ((CraftLivingEntity) player).getHandle();
                PathfinderHelper.clearPathfinderGoals(snowman);
                PathfinderHelper.addPathfinderGoal(snowman, 1, new PathfinderGoalFollowPlayer(
                        (EntityInsentient) ((CraftLivingEntity) snowman).getHandle(), targetPlayer, 1.75, 5.0, 6.0));
            }

            public void onUnequip(Player player) {
                Snowman snowman = pets.remove(player.getUniqueId());
                if (snowman != null) {
                    try {
                        snowman.remove();
                    } catch (Throwable ignored) {
                    }
                }
            }

            @Override
            public long getTickPeriod() {
                return 20L;
            }

            @Override
            public void tick(Player player) {
                Snowman snowman = pets.get(player.getUniqueId());
                if (snowman == null) return;

                if (!snowman.isValid() || player.getWorld() != snowman.getWorld() || !main.isInAnyLobby(player)) {
                    try {
                        snowman.remove();
                    } catch (Throwable ignored) {
                    }
                    pets.remove(player.getUniqueId());
                    return;
                }

                Location playerLoc = player.getLocation();
                double distance = playerLoc.distance(snowman.getLocation());
                if (distance > 15) {
                    Location behind = playerLoc.clone().add(playerLoc.getDirection().multiply(-2));
                    behind.setY(Math.min(playerLoc.getWorld().getHighestBlockYAt(behind), playerLoc.getY() + 10));
                    snowman.teleport(behind);
                }
            }
        };
    }
}