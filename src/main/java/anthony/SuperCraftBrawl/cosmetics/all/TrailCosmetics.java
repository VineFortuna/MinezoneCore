package anthony.SuperCraftBrawl.cosmetics.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRegistry;
import anthony.util.ItemHelper;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class TrailCosmetics {

    private static final String CANDY_CANE_TEXTURE = "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM4M2E0MmU4MmNkNmE3MGUyMTZkOWE4YzJmZjZmMWU1ZTViMjU2Y2VhM2I4Y2QyMjU0NzIzOTNhYTNlY2E1YSJ9fX0=";

    private TrailCosmetics() {
    }

    public static void registerAll(Core main, CosmeticRegistry registry) {
        registry.register(snowParticles(main));
        registry.register(candyCaneSwirl(main));
        registry.register(candyAura(main));
    }

    private static Cosmetic snowParticles(Core main) {
        return new Cosmetic("snow_particles", CosmeticCategory.TRAIL, "Snow Particles",
                "Christmas 2025 Exclusive") {
            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.setDetails(ItemHelper.create(Material.SNOW_BALL), "&bSnow Particles"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).snowParticles == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this yet!");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }

            @Override
            public long getTickPeriod() {
                return 1L;
            }

            public void tick(Player player) {
                if (!main.isInAnyLobby(player)) return;

                Location loc = player.getLocation().add(0, 0.2, 0);
                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.CLOUD, false,
                        (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), 0.3f, 0.3f, 0.3f, 0f, 5);

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packet);
                }
            }
        };
    }

    private static Cosmetic candyCaneSwirl(Core main) {
        return new Cosmetic("candy_cane_swirl", CosmeticCategory.TRAIL, "Candy Cane Swirl",
                "Christmas 2024 Exclusive") {
            private double angle = 0;

            public ItemStack getIcon(Player player) {
                return withRequirementLore(ItemHelper.createSkullTexture(CANDY_CANE_TEXTURE, "&cCandy &fCane &cSwirl"));
            }

            public boolean isUnlocked(Player player) {
                return main.getDataManager().getPlayerData(player).candycaneParticles == 1;
            }

            public String getUnlockMessage(Player player) {
                return main.color("&c&l(!) &rYou have not unlocked this yet!");
            }

            public void onEquip(Player player) {
            }

            public void onUnequip(Player player) {
            }

            @Override
            public long getTickPeriod() {
                return 1L;
            }

            @Override
            public void tick(Player player) {
                if (!main.isInAnyLobby(player)) return;

                angle += Math.PI / 16;

                double radius = 1.0;
                double height = 1.0;

                double xRed = radius * Math.cos(angle);
                double zRed = radius * Math.sin(angle);

                double xWhite = radius * Math.cos(angle + Math.PI);
                double zWhite = radius * Math.sin(angle + Math.PI);

                Location baseLoc = player.getLocation();

                sendParticleToAll(EnumParticle.REDSTONE, baseLoc.getX() + xRed, baseLoc.getY() + height,
                        baseLoc.getZ() + zRed, 0.1f, 0.1f, 0.1f, 0f, 5);

                sendParticleToAll(EnumParticle.SNOW_SHOVEL, baseLoc.getX() + xWhite, baseLoc.getY() + height,
                        baseLoc.getZ() + zWhite, 0.1f, 0.1f, 0.1f, 0f, 5);
            }

            private void sendParticleToAll(EnumParticle particle, double x, double y, double z, float offsetX,
                                            float offsetY, float offsetZ, float speed, int count) {
                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, false,
                        (float) x, (float) y, (float) z, offsetX, offsetY, offsetZ, speed, count);

                for (Player online : Bukkit.getOnlinePlayers()) {
                    ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                }
            }
        };
    }

    private static Cosmetic candyAura(Core main) {
        return new Cosmetic("candy_aura", CosmeticCategory.TRAIL, "Candy Aura",
                "Halloween 2025 Exclusive") {
            public ItemStack getIcon(Player player) {
                int found = main.getHalloweenManager() != null
                        ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return withRequirementLore(ItemHelper.setDetails(ItemHelper.create(Material.SUGAR),
                        main.color("&dCandy Aura"), "",
                        main.color("&7Progress: &e" + Math.min(found, 4) + "&7/4 baskets")));
            }

            public boolean isUnlocked(Player player) {
                int found = main.getHalloweenManager() != null
                        ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return found >= 4;
            }

            public String getUnlockMessage(Player player) {
                int found = main.getHalloweenManager() != null
                        ? main.getHalloweenManager().getFoundCount(player.getUniqueId()) : 0;
                return main.color("&c&l(!) &rYou need &e4&r baskets to use this!");
            }

            public void onEquip(Player player) {
                main.getCandyAuraManager().enable(player);
            }

            public void onUnequip(Player player) {
                main.getCandyAuraManager().disable(player);
            }
        };
    }
}
