package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.christmas.ReindeerFlight;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PetCosmetics {

    private static final String RUDOLPH_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWJkNDZiMzhiMjFiMzQyY2FmOTE3YWQ5Y2E0MmFmYjY4Mzg4YTU1OTFiY2M5YWRlZDFlOGUzNDZlMTg4OTAifX19";

    private PetCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        registry.register(snowman(main));
        registry.register(rudolph(main));
    }

    /**
     * The Rudolph Pet, unlocked by finding 8 Christmas presents. Equipping it spawns an idle Reindeer next
     * to the player instead of mounting them automatically - right-clicking it is what starts the flight
     * (exactly like Santa's Reindeer), and only the owner can ride it.
     */
    private static Cosmetic rudolph(Core main) {
        final Map<UUID, Horse> idlePets = new HashMap<>();          // owner uuid -> their standing Reindeer
        final Map<UUID, ReindeerFlight> flights = new HashMap<>();  // owner uuid -> their active flight
        final Map<UUID, UUID> ownerByHorse = new ConcurrentHashMap<>(); // horse uuid -> owner uuid

        main.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(ignoreCancelled = true)
            public void onClickReindeer(PlayerInteractEntityEvent event) {
                if (!(event.getRightClicked() instanceof Horse)) return;
                UUID ownerId = ownerByHorse.get(event.getRightClicked().getUniqueId());
                if (ownerId == null) return; // not one of our reindeer

                event.setCancelled(true); // never let vanilla mount/open its inventory
                Player clicker = event.getPlayer();

                if (!clicker.getUniqueId().equals(ownerId)) {
                    clicker.sendMessage(main.color("&c&l(!) &rThis isn't your Reindeer!"));
                    return;
                }
                if (flights.containsKey(ownerId)) return; // already riding it

                Horse idle = idlePets.remove(ownerId);
                if (idle != null) {
                    ownerByHorse.remove(idle.getUniqueId());
                    idle.remove();
                }

                ReindeerFlight flight = new ReindeerFlight(clicker, 0,
                        () -> clicker.isOnline() && main.isInLobby(clicker));
                flights.put(ownerId, flight);
                flight.start(main);
            }
        }, main);

        return new Cosmetic("rudolph", CosmeticCategory.PET, "Rudolph Pet",
                "Christmas 2026 Exclusive") {
            public ItemStack getIcon(Player player) {
                int found = main.getChristmasManager() != null
                        ? main.getChristmasManager().getFoundCount(player.getUniqueId()) : 0;
                return withRequirementLore(ItemHelper.createSkullTexture(RUDOLPH_TEXTURE, main.color("&c&lRudolph Pet"), "",
                        main.color("&7Progress: &e" + Math.min(found, 8) + "&7/8 presents")));
            }

            public boolean isUnlocked(Player player) {
                int found = main.getChristmasManager() != null
                        ? main.getChristmasManager().getFoundCount(player.getUniqueId()) : 0;
                return found >= 8;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou need to find &e8 presents &rto use the &c&lRudolph Pet&r.");
            }

            public void onEquip(Player player) {
                UUID uuid = player.getUniqueId();

                Horse existingIdle = idlePets.remove(uuid);
                if (existingIdle != null) {
                    ownerByHorse.remove(existingIdle.getUniqueId());
                    existingIdle.remove();
                }
                ReindeerFlight existingFlight = flights.remove(uuid);
                if (existingFlight != null) existingFlight.end();

                Horse idle = spawnIdleReindeer(player);
                idlePets.put(uuid, idle);
                ownerByHorse.put(idle.getUniqueId(), uuid);
            }

            public void onUnequip(Player player) {
                UUID uuid = player.getUniqueId();

                Horse idle = idlePets.remove(uuid);
                if (idle != null) {
                    ownerByHorse.remove(idle.getUniqueId());
                    idle.remove();
                }
                ReindeerFlight flight = flights.remove(uuid);
                if (flight != null) flight.end();
            }

            @Override
            public long getTickPeriod() {
                return 20L;
            }

            @Override
            public void tick(Player player) {
                UUID uuid = player.getUniqueId();
                Horse idle = idlePets.get(uuid);
                if (idle == null) return;

                if (!idle.isValid() || !main.isInLobby(player)) {
                    ownerByHorse.remove(idle.getUniqueId());
                    idle.remove();
                    idlePets.remove(uuid);
                }
            }
        };
    }

    /** Spawns an idle (unmounted) Reindeer beside the player - styled the same as Santa's. */
    private static Horse spawnIdleReindeer(Player player) {
        Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation().add(1, 0, 1), EntityType.HORSE);
        horse.setTamed(true);
        horse.setAdult();
        horse.setVariant(Horse.Variant.HORSE);
        horse.setColor(Horse.Color.DARK_BROWN);
        horse.setStyle(Horse.Style.NONE);
        horse.setCustomName(ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Reindeer");
        horse.setOwner(player);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
        return horse;
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

                if (!snowman.isValid() || player.getWorld() != snowman.getWorld() || !main.isInLobby(player)) {
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