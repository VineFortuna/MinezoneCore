package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.ItemShears;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EndermiteClass extends BaseClass {

    private final ItemStack weapon;
    private final ItemStack passiveItem;
    private final ItemStack hostileItem;
    private final ItemStack eggItem;
    private final Ability phaseAbility = new Ability("&5&lPhase Shifter", 8, player);
    private static final double PHASE_SHIFTER_RANGE = 25;
    
    private List<Endermite> endermites = new ArrayList<>();
    
    public EndermiteClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWJjN2I5ZDM2ZmI5MmI2YmYyOTJiZTczZDMyYzZjNWIwZWNjMjViNDQzMjNhNTQxZmFlMWYxZTY3ZTM5M2EzZSJ9fX0=",
                "3C2B4F",
                "3C2B4F",
                "23192E",
                6,
                "Endermite"
        );

        // Weapon
        String rangeDisplay = ItemHelper.formatDouble(PHASE_SHIFTER_RANGE);

        weapon = ItemHelper.setDetails(
                new ItemStack(Material.EYE_OF_ENDER),
                phaseAbility.getAbilityNameRightClickMessage(),
                "",
                "&7Shoot at your Endermite to swap places with it",
                "",
                "&7Range: &a" + rangeDisplay + " &7blocks"
        );
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

        // Passive trigger ability
        passiveItem = ItemHelper.setDetails(
                new ItemStack(Material.INK_SACK, 1, (short) 8),
                "&8&lPassive &7(Right Click)",
               "&7Make your Endermites attack players"
        );

        // Passive trigger ability
        hostileItem = ItemHelper.setDetails(
                new ItemStack(Material.INK_SACK, 1, (short) 5),
                "&5&lHostile &7(Right Click)",
                "&7Make your Endermites stay in place"
        );

        eggItem = ItemHelper.setDetails(
                ItemHelper.createMonsterEgg(EntityType.ENDERMITE, 6),
                "&5&lEndermite Pokeball",
                "&7Spawns an endermite"
        );
    }
    
    @Override
    public void SetItems(Inventory playerInv) {
        for (Entity e : player.getWorld().getEntities())
            if (e instanceof Endermite)
                if (e.getName().contains(player.getName()))
                    e.remove();
    
        phaseAbility.getCooldownInstance().reset();
        endermites.clear();
        
        playerInv.setItem(0, weapon);
        playerInv.setItem(1, hostileItem);
        playerInv.setItem(2, eggItem);
    }
    
    @Override
    public void Tick(int gameTicks) {
        if (!isPlayerAlive()) return;
        phaseAbility.updateActionBar(player, this);
        if (!player.getInventory().contains(weapon)) {
            player.getInventory().setItem(1, weapon);
        }

        if (endermites.isEmpty()) return;

        Iterator<Endermite> iterator = endermites.iterator();

        while (iterator.hasNext()) {
            Endermite mite = iterator.next();
            if (!mite.isDead()) {
                if (gameTicks % 20 == 0) {
                    if (player.getItemInHand().isSimilar(weapon)) {
                        player.playEffect(mite.getLocation().clone().add(0, 1, 0), Effect.HAPPY_VILLAGER, 1);
                        player.playEffect(mite.getLocation().clone().add(0, 1.5, 0), Effect.HAPPY_VILLAGER, 1);
                    }
                }
            } else {
                iterator.remove();  // Safe removal using the iterator
            }
        }
    }
    
    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        if (item.equals(weapon)) {
            if (!phaseAbility.isReady()) return;
            usePhaseAbility();
        }

        if (item.isSimilar(passiveItem)) {
            event.setCancelled(true);
            turnHostile();
            SoundManager.playSuccessfulHit(player);
        }

        if (item.isSimilar(hostileItem)) {
            event.setCancelled(true);
            turnPassive();
            SoundManager.playSuccessfulHit(player);
        }

        if (item.isSimilar(eggItem)) {
            ItemMeta meta = item.getItemMeta();

            if (meta != null && meta.getDisplayName().contains("Endermite")) {
                int amount = item.getAmount();
                amount--;

                if (amount == 0)
                    player.getInventory().clear(player.getInventory().getHeldItemSlot());
                else
                    item.setAmount(amount);

                spawnEndermite();
            }
        }
    }

    private void usePhaseAbility() {
        Location playerEyeLoc = player.getEyeLocation();
        Vector dir = playerEyeLoc.getDirection();

        Location endLoc = playerEyeLoc;
        BlockIterator b = new BlockIterator(playerEyeLoc, 0, (int) PHASE_SHIFTER_RANGE);

        // Determine the farthest valid location along the beam
        while (b.hasNext()) {
            Block block = b.next();
            endLoc = block.getLocation();

            if (block.getType().isSolid())
                break;
        }

        boolean foundTarget = false;
        // Maximum distance along the beam
        double maxDist = endLoc.distance(playerEyeLoc);

        for (Endermite e : endermites) {
            Vector d = e.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
            double dist = d.dot(dir);

            if (dist < maxDist) {
                Location closest = player.getEyeLocation().add(dir.clone().multiply(dist));

                if (closest.distanceSquared(e.getLocation().add(0, 1, 0)) <= 1.8 * 1.8) {
                    if (instance.isInBounds(e.getLocation())) {
                        Location playerLoc = player.getLocation().clone();
                        Location targetLoc = e.getLocation().clone();
                        targetLoc.setDirection(playerLoc.getDirection());

                        // Swap player and Endermite locations
                        player.getWorld().playSound(playerLoc, Sound.ENDERMAN_TELEPORT, 1, 0);
                        player.teleport(targetLoc);

                        player.getWorld().playEffect(playerLoc.add(0, 1, 0), Effect.PORTAL, 1);
                        e.teleport(playerLoc);

                        player.sendMessage(ChatColorHelper
                                .color("&2&l(!) &rYou and your Endermite teleported to each other's location"));
                        phaseAbility.use();
                        foundTarget = true;
                        break;
                    }
                }
            }
        }
        for (double t = 1; t < maxDist; t += 0.5) {
            if (foundTarget) {
                ParticleEffect.BLOCK_CRACK.display(player.getEyeLocation().add(dir.clone().multiply(t)), 0.0F,
                        0.0F, 0.0F, 0.0F, 1, new BlockTexture(Material.PORTAL));
            } else {
                ParticleEffect.BLOCK_CRACK.display(player.getEyeLocation().add(dir.clone().multiply(t)), 0.0F,
                        0.0F, 0.0F, 0.0F, 1, new BlockTexture(Material.PORTAL), player);
            }
        }
    }

    private void spawnEndermite() {
        ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
            @Override
            public void onHit(Player hit) {
                Location hitLoc = this.getBaseProj().getEntity().getLocation();
                player.playSound(hitLoc, Sound.SUCCESSFUL_HIT, 1, 1);
                Endermite en = (Endermite) player.getWorld().spawnEntity(hitLoc, EntityType.ENDERMITE);
                en.setCustomName(
                        "" + ChatColor.RED + player.getName() + "'s " + ChatColor.YELLOW + "Endermite");
                en.setCustomNameVisible(true);
                en.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0, true, false));
                if (player.getInventory().contains(hostileItem)) {
                    en.setTarget(instance.getNearestPlayer(player, en, 150));
                } else if (player.getInventory().contains(passiveItem))  {
                    en.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 999999999, true, false));
                }
                player.playSound(player.getLocation(), Sound.ENDERMAN_STARE, 1, 1);
                endermites.add(en);
            }

        }, ItemHelper.createMonsterEgg(EntityType.ENDERMITE, 1));
        instance.getGameManager().getProjManager().shootProjectile(proj, player.getEyeLocation(),
                player.getLocation().getDirection().multiply(2.0D));
    }

    private void turnHostile() {

        for (Endermite mite : endermites) {
            if (!mite.isDead()) {
                mite.setTarget(instance.getNearestPlayer(player, mite, 150));
                mite.removePotionEffect(PotionEffectType.SLOW);
            }
        }

        player.sendMessage(ChatColorHelper.color("&2&l(!) &rYour Endermites will now attack other players"));
        player.getInventory().setItemInHand(hostileItem);
    }

    private void turnPassive() {
        for (Endermite mite : endermites) {
            if (!mite.isDead()) {
                mite.setTarget(null);
                mite.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999999, 999999999, true, false));
            }
        }
        player.sendMessage(ChatColorHelper.color("&2&l(!) &rYour Endermites are now passive"));
        player.getInventory().setItemInHand(passiveItem);
    }

    @Override
    public void classesEvent(Player damagerPlayer, BaseClass baseClass) {
        super.classesEvent(damagerPlayer, baseClass);

        ItemStack endermiteEgg = eggItem.clone();
        endermiteEgg.setAmount(1);

        player.getInventory().addItem(endermiteEgg);
    }

    @Override
    public ClassType getType() {
        return ClassType.Endermite;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }
}
