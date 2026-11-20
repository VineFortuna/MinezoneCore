package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TNTClass extends BaseClass {

    private static final double TNT_COOLDOWN = 10;
    /** The nearest enemy this many blocks around the player gets the TNT... */
    private static final double TNT_RANGE = 20;
    /** ...and this many blocks up and down. */
    private static final double TNT_RANGE_VERTICAL = 25;
    private static final int TNT_SPAWN_HEIGHT = 5;
    private static final int TNT_FUSE_TICKS = 40;
    private static final PotionEffect TNT_SLOWNESS = new PotionEffect(PotionEffectType.SLOW, 100, 1);

    private final Ability tntAbility = new Ability("&c&lTNT", TNT_COOLDOWN, player);
    private final ItemStack weapon;
    private final ItemStack tntItem;

    public TNTClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTU3M2Q3MDQ2ZDZlMDgxOTgzOTBhYTU2YzhmODY3OGMxNmQ0NDA3YWY5ZjIxNGJmMDI5MWYzYzdkYjFmMzc5YSJ9fX0=",
                "B83816",
                6,
                "Tnt"
        );

        weapon = ItemHelper.setUnbreakable(ItemHelper.addEnchant(new ItemStack(Material.WOOD_SWORD), Enchantment.KNOCKBACK, 1));

        tntItem = ItemHelper.setDetails(
                new ItemStack(Material.TNT),
                tntAbility.getAbilityNameRightClickMessage(),
                "&7Spawn TNT on the nearest enemy around you",
                "",
                "&7Range: &a" + ItemHelper.formatDouble(TNT_RANGE) + " &7blocks"
        );
    }

    @Override
    public void setArmor(EntityEquipment playerEquip) {
        setArmorNew(playerEquip);
    }

    @Override
    public void setItems(Inventory playerInv) {
        tntAbility.getCooldownInstance().reset();

        playerInv.setItem(0, weapon);
        playerInv.setItem(1, tntItem);
    }

    @Override
    public void tick(int gameTicks) {
        if (!isPlayerAlive()) return;

        tntAbility.updateActionBar(player, this);
    }

    @Override
    public void useItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null || !item.isSimilar(tntItem)) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        // The TNT is never placed as a block
        event.setCancelled(true);
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        if (!tntAbility.isReady()) {
            long seconds = tntAbility.getCooldownInstance().getRemainingCooldownSeconds() + 1;
            tntAbility.sendCustomMessage("&c&l(!) &rYour " + tntAbility.getAbilityName()
                    + "&r is still regenerating for &a" + seconds + "s");
            return;
        }

        Player target = findNearestEnemy();
        if (target == null) {
            tntAbility.sendCustomMessage("&c&l(!) &rNo nearby players have been found!");
            return;
        }

        target.addPotionEffect(TNT_SLOWNESS);
        TNTPrimed tnt = target.getWorld().spawn(target.getLocation().add(0, TNT_SPAWN_HEIGHT, 0), TNTPrimed.class);
        tnt.setFuseTicks(TNT_FUSE_TICKS);
        // Everyone hears it where it is, so they can see it coming
        SoundManager.playSoundToAll(player, target.getLocation(), Sound.FUSE, 1, 1);

        tntAbility.sendCustomMessage("&e&l(!) &rSpawning a TNT at &e" + target.getName() + "'s &rlocation");
        tntAbility.use();
    }

    /** The closest player in range who can be hit, or null if there is none. */
    private Player findNearestEnemy() {
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Entity entity : player.getNearbyEntities(TNT_RANGE, TNT_RANGE_VERTICAL, TNT_RANGE)) {
            if (!(entity instanceof Player)) continue;
            Player other = (Player) entity;

            if (!instance.classes.containsKey(other)) continue;
            if (instance.classes.get(other).getLives() <= 0) continue;
            if (other.getGameMode() == GameMode.SPECTATOR) continue;
            if (instance.duosMap != null && instance.team.get(other).equals(instance.team.get(player))) continue;

            double distance = player.getLocation().distanceSquared(other.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = other;
            }
        }
        return nearest;
    }

    @Override
    public ClassType getType() {
        return ClassType.TNT;
    }

    @Override
    public void setNameTag() {}

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }
}
