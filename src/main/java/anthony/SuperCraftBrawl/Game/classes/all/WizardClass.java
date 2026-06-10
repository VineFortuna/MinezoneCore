package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Random;

public class WizardClass extends BaseClass {

    private boolean spellPicked = false;
    private int spell = -1; // 1 = teleport, 2 = fireball, 3 = speed/jump, 4 = blindness
    private boolean speedyjumpy = false;
    private boolean blindness = false;

    public WizardClass(GameInstance instance, Player player) {
        super(instance, player);
        baseVerticalJump = 1.1;
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM4NTY0ZTI4YWJhOTgzMDFkYmRhNWZhZmQ4NmQxZGE0ZTJlYWVlZjEyZWE5NGRjZjQ0MGI4ODNlNTU5MzExYyJ9fX0=",
                "C178E8",
                "C178E8",
                "965905",
                6,
                "Wizard"
        );
    }

    // -----------------------------------------------------------------------
    //  Wand item builders
    // -----------------------------------------------------------------------

    private ItemStack makePlainWand() {
        return ItemHelper.setDetails(new ItemStack(Material.STICK),
                ChatColor.RESET + "Magic Wand " + ChatColor.GRAY + "(Right Click)");
    }

    private ItemStack makeWand(int sharpness, int knockback) {
        ItemStack wand = ItemHelper.setDetails(new ItemStack(Material.STICK),
                ChatColor.RESET + "Magic Wand");
        wand.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpness);
        wand.addUnsafeEnchantment(Enchantment.KNOCKBACK, knockback);
        return wand;
    }

    // -----------------------------------------------------------------------
    //  Lifecycle
    // -----------------------------------------------------------------------

    @Override
    public ClassType getType() { return ClassType.Wizard; }

    @Override
    public void setArmor(EntityEquipment playerEquip) { setArmorNew(playerEquip); }

    @Override
    public ItemStack getAttackWeapon() { return player.getInventory().getItem(0); }

    @Override
    public void SetNameTag() {}

    @Override
    public void SetItems(Inventory playerInv) {
        if (speedyjumpy) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.JUMP);
        }
        spellPicked = false;
        spell = -1;
        speedyjumpy = false;
        blindness = false;
        playerInv.setItem(0, makePlainWand());
    }

    @Override
    public void Tick(int gameTicks) {
        // Keep speed/jump alive for spell 3
        if (speedyjumpy) {
            if (!player.hasPotionEffect(PotionEffectType.SPEED))
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1)); // Speed 2
            if (!player.hasPotionEffect(PotionEffectType.JUMP))
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 2)); // Jump Boost 3
        }

        // Action bar cooldown for teleport and fireball spells
        if ((spell == 1 || spell == 2)
                && instance.classes.containsKey(player)
                && instance.classes.get(player).getType() == ClassType.Wizard
                && instance.classes.get(player).getLives() > 0) {

            int cooldownMs = (spell == 1) ? 15000 : 10000;
            int cooldownSec = (cooldownMs - wizard.getTime()) / 1000 + 1;
            String label = (spell == 1) ? "Teleport" : "Fireballs";

            if (wizard.getTime() < cooldownMs) {
                String msg = ChatColor.RED + "" + ChatColor.BOLD + label
                        + ChatColor.RESET + " regenerates in: " + ChatColor.YELLOW + cooldownSec + "s";
                getActionBarManager().setActionBar(player, "wizard.cooldown", msg, 2);
            } else {
                String msg = ChatColor.RESET + "You can use " + ChatColor.RED + ChatColor.BOLD + label;
                getActionBarManager().setActionBar(player, "wizard.cooldown", msg, 2);
            }
        }
    }

    @Override
    public void DoDamage(EntityDamageByEntityEvent event) {
        if (!blindness) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        // 1/3 chance of Blindness 2 for 5 seconds
        if (new Random().nextInt(3) == 0) {
            ((LivingEntity) event.getEntity())
                    .addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, true));
        }
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        event.setCancelled(true);

        if (!spellPicked) {
            pickSpell();
            return;
        }

        if (spell == 1) useTeleport();
        else if (spell == 2) useFireball();
    }

    // -----------------------------------------------------------------------
    //  Spell selection
    // -----------------------------------------------------------------------

    private void pickSpell() {
        spellPicked = true;
        spell = new Random().nextInt(4) + 1;

        switch (spell) {
            case 1:
                // Teleport — Sharp 4, KB 1
                player.getInventory().setItem(0, makeWand(4, 1));
                instance.TellAll(instance.color("&6&l&lWizard> &rI cast spell... Teleport!"));
                break;
            case 2:
                // Triple Fireball — Sharp 3, KB 2
                player.getInventory().setItem(0, makeWand(3, 2));
                instance.TellAll(instance.color("&6&l&lWizard> &rI cast spell... Fireball fireball fireball!!"));
                break;
            case 3:
                // Speed 2 + Jump Boost 3 — Sharp 3, KB 1
                player.getInventory().setItem(0, makeWand(3, 1));
                speedyjumpy = true;
                instance.TellAll(instance.color("&6&l&lWizard> &rI cast... Speedy speedy jumpy jumpy"));
                break;
            case 4:
                // Blindness on hit — Sharp 3, KB 2
                player.getInventory().setItem(0, makeWand(3, 2));
                blindness = true;
                instance.TellAll(instance.color("&6&l&lWizard> &rI cast... Let my enemies see darkness.."));
                break;
        }
    }

    // -----------------------------------------------------------------------
    //  Spell 1: Teleport
    // -----------------------------------------------------------------------

    private void useTeleport() {
        if (wizard.getTime() < 15000) {
            int seconds = (15000 - wizard.getTime()) / 1000 + 1;
            player.sendMessage(instance.color("&c&l(!) Teleport&r is still on cooldown for &a" +
                    seconds + "s"));
            return;
        }

        // Trace ray up to 15 blocks — track the last free block before the wall
        // so we never teleport the player inside a solid block
        BlockIterator bi = new BlockIterator(player.getEyeLocation(), 0, 15);
        Block lastFree = null;
        while (bi.hasNext()) {
            Block block = bi.next();
            if (block.getType().isSolid()) break;
            lastFree = block;
        }

        // No free space found (wall right at face, or nothing in range)
        if (lastFree == null) {
            player.sendMessage(instance.color("&c&l(!) &rNo safe space to teleport to!"));
            return;
        }

        // Make sure there is head room at the destination (2-block tall player)
        if (lastFree.getRelative(org.bukkit.block.BlockFace.UP).getType().isSolid()) {
            player.sendMessage(instance.color("&c&l(!) &rNot enough room to teleport there!"));
            return;
        }

        Location origin = player.getLocation().clone();
        Location destination = lastFree.getLocation().add(0.5, 0, 0.5);
        destination.setYaw(origin.getYaw());
        destination.setPitch(origin.getPitch());

        playVanishEffect(origin);
        player.teleport(destination);
        playAppearEffect(destination);

        wizard.restart();
    }

    // -----------------------------------------------------------------------
    //  Teleport particle effects
    // -----------------------------------------------------------------------

    /**
     * Played at the origin — portal particles spiral upward in expanding rings,
     * giving the impression of the wizard dissolving into thin air.
     */
    private void playVanishEffect(Location loc) {
        World world = loc.getWorld();

        // Rising spiral: 5 rings climbing from feet to above head, each slightly wider
        int rings = 5;
        int points = 10;
        for (int ring = 0; ring < rings; ring++) {
            double y      = ring * 0.4;          // climb upward
            double radius = 0.3 + ring * 0.12;   // expand outward
            double offset = ring * 0.35;          // rotate each ring slightly
            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI / points) * i + offset;
                Location p = loc.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
                world.playEffect(p, Effect.PORTAL, 1);
            }
        }
        // Dense central burst at chest height
        for (int i = 0; i < 5; i++)
            world.playEffect(loc.clone().add(0, 1, 0), Effect.PORTAL, 1);

        // Low whoosh — slightly lower pitch than the appear sound so they feel different
        world.playSound(loc, Sound.ENDERMAN_TELEPORT, 1.0f, 0.75f);
        world.playSound(loc, Sound.PORTAL_TRAVEL, 0.3f, 1.8f);
    }

    /**
     * Played at the destination — portal particles burst outward from rings that
     * contract toward the centre, like the wizard snapping into existence.
     */
    private void playAppearEffect(Location loc) {
        World world = loc.getWorld();

        // Contracting rings: largest at the bottom, smallest at the top
        int rings = 4;
        int points = 12;
        for (int ring = 0; ring < rings; ring++) {
            double y      = ring * 0.45;
            double radius = 1.2 - ring * 0.25;    // shrink as they rise
            double offset = ring * 0.5;
            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI / points) * i + offset;
                Location p = loc.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
                world.playEffect(p, Effect.PORTAL, 1);
                // Sprinkle enchantment-table runes on every other point for extra flair
                if (i % 2 == 0) world.playEffect(p, Effect.ENDER_SIGNAL, 1);
            }
        }
        // Sharp snap sound — higher pitch to signal arrival
        world.playSound(loc, Sound.ENDERMAN_TELEPORT, 1.0f, 1.3f);
    }

    // -----------------------------------------------------------------------
    //  Spell 2: Triple Fireball
    // -----------------------------------------------------------------------
    //  Spell 2: Blaze Powder — 3 projectiles from chest (center, left, right)
    // -----------------------------------------------------------------------

    private void useFireball() {
        if (wizard.getTime() < 10000) {
            int seconds = (10000 - wizard.getTime()) / 1000 + 1;
            player.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET
                    + "Blaze Powder is still on cooldown for " + ChatColor.YELLOW + seconds + "s");
            return;
        }

        wizard.restart();

        Location chest = player.getLocation().add(0, 1.2, 0);
        Vector dir = player.getEyeLocation().getDirection();

        // Perpendicular right vector relative to the direction the player is facing
        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0));
        if (right.lengthSquared() < 0.001) right = new Vector(1, 0, 0);
        right.normalize();

        shootBlazePowder(chest.clone(), dir);
        shootBlazePowder(chest.clone().add(right), dir);
        shootBlazePowder(chest.clone().subtract(right), dir);
    }

    private void shootBlazePowder(Location from, Vector direction) {
        ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
            @Override
            public void onHit(Player hit) {
                // Direct hit only — set fire for 5 seconds
                if (hit != null && hit.getGameMode() != GameMode.SPECTATOR) {
                    hit.setFireTicks(100);
                }
            }
        }, new ItemStack(Material.BLAZE_POWDER));
        instance.getGameManager().getProjManager().shootProjectile(proj, from, direction.clone().multiply(2.5));
    }
}