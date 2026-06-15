package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class TNTClass extends BaseClass {

    private int cooldownSec;
    private static final int TNT_COOLDOWN = 10 * 1000;

    public TNTClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTU3M2Q3MDQ2ZDZlMDgxOTgzOTBhYTU2YzhmODY3OGMxNmQ0NDA3YWY5ZjIxNGJmMDI5MWYzYzdkYjFmMzc5YSJ9fX0=",
                6,
                "B83816",
                "B83816",
                "B83816"
        );
    }

    @Override
    public void setArmor(EntityEquipment playerEquip) {
        setArmorNew(playerEquip);
    }

    public ItemStack getTNT() {
        return ItemHelper.setDetails(new ItemStack(Material.TNT, 1), "", "",
                instance.getGameManager().getMain().color("&7Spawn TNT to a random player around you"));
    }

    @Override
    public void SetItems(Inventory playerInv) {
        tntItem.startTime = System.currentTimeMillis() - 100000;
        playerInv.setItem(0, this.getAttackWeapon());
        playerInv.setItem(1, this.getTNT());
    }

    @Override
    public void Tick(int gameTicks) {
        if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.TNT
                && instance.classes.get(player).getLives() > 0) {
            this.cooldownSec = (TNT_COOLDOWN - tntItem.getTime()) / 1000 + 1;

            if (tntItem.getTime() < TNT_COOLDOWN) {
                String msg = instance.color("&c&lTNT &rregenerates in: &a" + this.cooldownSec + "s");
                getActionBarManager().setActionBar(player, "tnt.cooldown", msg, 2);
            } else {
                String msg = instance.getGameManager().getMain().color("&rYou can use &c&lTNT");
                getActionBarManager().setActionBar(player, "tnt.cooldown", msg, 2);
            }
        }
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.TNT && item.getAmount() == 1) {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                if (tntItem.getTime() < TNT_COOLDOWN) {
                    int seconds = (TNT_COOLDOWN - tntItem.getTime()) / 1000 + 1;
                    event.setCancelled(true);
                    player.sendMessage(instance.color("&c&l(!) &rYour &c&lTNT&r is still regenerating for &a" +
                            seconds + "s"));
                } else {
                    List<Entity> near = player.getNearbyEntities(20.0D, 25.0D, 20.0D);

                    // Find the nearest valid player rather than a random one
                    Player nearest = null;
                    double nearestDist = Double.MAX_VALUE;

                    for (Entity entity : near) {
                        if (!(entity instanceof Player)) continue;
                        Player playerInRange = (Player) entity;

                        if (!instance.classes.containsKey(playerInRange)) continue;
                        if (instance.classes.get(playerInRange).getLives() <= 0) continue;
                        if (playerInRange.getGameMode() == GameMode.SPECTATOR) continue;
                        if (instance.duosMap != null &&
                                instance.team.get(playerInRange).equals(instance.team.get(player))) continue;

                        double dist = player.getLocation().distanceSquared(playerInRange.getLocation());
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearest = playerInRange;
                        }
                    }

                    if (nearest != null) {
                        nearest.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
                        TNTPrimed tnt = nearest.getWorld().spawn(nearest.getLocation().add(0, 5, 0), TNTPrimed.class);
                        tnt.setFuseTicks(40);
                        player.sendMessage(instance.getGameManager().getMain()
                                .color("&e&l(!) &rSpawning a TNT at &e" + nearest.getName() + "'s &rlocation"));
                        tntItem.restart();
                    } else {
                        player.sendMessage(ChatColorHelper.color("&c&l(!) &rNo nearby players have been found!"));
                    }
                }
            }
        }
    }

    @Override
    public ClassType getType() {
        return ClassType.TNT;
    }

    @Override
    public void SetNameTag() {}

    @Override
    public ItemStack getAttackWeapon() {
        ItemStack sword = ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1);
        ItemMeta meta = sword.getItemMeta();
        meta.spigot().setUnbreakable(true);
        sword.setItemMeta(meta);
        return sword;
    }
}