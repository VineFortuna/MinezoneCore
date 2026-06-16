package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;

public class WizardClass extends BaseClass {

    private boolean spellPicked = false;
    private int spell = -1; // 1 = teleport, 2 = fireball, 3 = speed/jump, 4 = blindness
    private boolean speedyjumpy = false;
    private boolean blindness = false;

    private ItemStack weapon;
    private ItemStack spellBook;

    private static final int TELEPORT_COOLDOWN_MS = 10 * 1000;
    private static final int TELEPORT_RANGE_BLOCKS = 10;
    private static final int FIREBALL_COOLDOWN_MS = 5 * 1000;
    private static final int SPELL_BOOK_COOLDOWN_MS = 25 * 1000;

    private long lastSpellBookUse = 0L;

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
    //  Item builders
    // -----------------------------------------------------------------------

    private ItemStack makePlainWand() {
        return ItemHelper.setDetails(
                new ItemStack(Material.STICK),
                instance.color("&6&lMagic Wand"),
                instance.color("&7Use your spell book to cast a spell")
        );
    }

    private ItemStack makeWand(String displayName, int sharpness, int knockback, String... lore) {
        ItemStack wand = ItemHelper.setDetails(
                new ItemStack(Material.STICK),
                instance.color(displayName),
                lore
        );

        wand.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpness);
        wand.addUnsafeEnchantment(Enchantment.KNOCKBACK, knockback);
        return wand;
    }

    private ItemStack makeSpellBook() {
        return ItemHelper.setDetails(
                new ItemStack(Material.BOOK),
                instance.color("&6&lSpell Book &7(Right Click)"),
                instance.color("&7Cast one of the following spells:"),
                instance.color("&7▶ &6&oFireballs"),
                instance.color("&7▶ &5&oTeleport"),
                instance.color("&7▶ &8&oDarkness"),
                instance.color("&7▶ &b&oSpeedy Speedy Jumpy Jumpy")
        );
    }

    @Override
    public ClassType getType() {
        return ClassType.Wizard;
    }

    @Override
    public void setArmor(EntityEquipment playerEquip) {
        setArmorNew(playerEquip);
    }

    @Override
    public ItemStack getAttackWeapon() {
        return player.getInventory().getItem(0);
    }

    @Override
    public void SetNameTag() {}

    @Override
    public void SetItems(Inventory playerInv) {
        clearCurrentSpellEffects();

        spellPicked = false;
        spell = -1;
        weapon = makePlainWand();
        spellBook = makeSpellBook();

        lastSpellBookUse = System.currentTimeMillis() - SPELL_BOOK_COOLDOWN_MS;

        playerInv.setItem(0, weapon);
        playerInv.setItem(1, spellBook);
    }

    @Override
    public void Tick(int gameTicks) {
        if (!instance.classes.containsKey(player)) return;
        if (instance.classes.get(player).getType() != ClassType.Wizard) return;
        if (instance.classes.get(player).getLives() <= 0) return;

        // Keep speed/jump alive for spell 3
        if (speedyjumpy) {
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1)); // Speed 2
            }

            if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 2)); // Jump Boost 3
            }
        }

        // Spell Book cooldown action bar
        if (!isSpellBookReady()) {
            int cooldownSec = getSpellBookCooldownSeconds();
            String msg = instance.color("&6&lSpell Book&r in: &e" + cooldownSec + "s");
            getActionBarManager().setActionBar(player, "wizard.spellbook", msg, 2);
        } else {
            String msg = instance.color("&rYou can use &6&lSpell Book");
            getActionBarManager().setActionBar(player, "wizard.spellbook", msg, 2);
        }

        // Action bar cooldown for teleport and fireball spells
        if (spellPicked && (spell == 1 || spell == 2)) {
            int cooldownMs = (spell == 1) ? TELEPORT_COOLDOWN_MS : FIREBALL_COOLDOWN_MS;
            int cooldownSec = (cooldownMs - wizard.getTime()) / 1000 + 1;
            String label = (spell == 1) ? "&5&lTeleport" : "&6&lFireballs";

            if (wizard.getTime() < cooldownMs) {
                String msg = instance.color(label + "&r in: &e" + cooldownSec + "s");
                getActionBarManager().setActionBar(player, "wizard.cooldown", msg, 2);
            } else {
                String msg = instance.color("&rYou can use " + label);
                getActionBarManager().setActionBar(player, "wizard.cooldown", msg, 2);
            }
        }
    }

    @Override
    public void DoDamage(EntityDamageByEntityEvent event) {
        if (!blindness) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

        boolean isWeaponMelee =
                event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                        && heldItem != null
                        && heldItem.equals(weapon);

        if (!isWeaponMelee) return;

        // 1/3 chance of Blindness 2 for 5 seconds
        if (new Random().nextInt(3) == 0) {
            ((LivingEntity) event.getEntity())
                    .addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1, true));
        }
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Spell Book changes your Wizard spell
        if (isSpellBook(item)) {
            event.setCancelled(true);
            useSpellBook();
            return;
        }

        // Wand only uses the selected active spell now.
        // It does NOT roll spells anymore.
        if (item.getType() == Material.STICK) {
            event.setCancelled(true);

            if (!spellPicked) {
                player.sendMessage(instance.color("&c&l(!) &rUse your &6&lSpell Book &rto choose a spell first."));
                return;
            }

            if (spell == 1) {
                useTeleport();
            } else if (spell == 2) {
                useFireball();
            }

            return;
        }
    }

    // -----------------------------------------------------------------------
    //  Spell Book
    // -----------------------------------------------------------------------

    private boolean isSpellBook(ItemStack item) {
        return spellBook != null && item != null && item.isSimilar(spellBook);
    }

    private boolean isSpellBookReady() {
        return getSpellBookElapsedTime() >= SPELL_BOOK_COOLDOWN_MS;
    }

    private int getSpellBookElapsedTime() {
        return (int) (System.currentTimeMillis() - lastSpellBookUse);
    }

    private int getSpellBookCooldownSeconds() {
        return (SPELL_BOOK_COOLDOWN_MS - getSpellBookElapsedTime()) / 1000 + 1;
    }

    private void useSpellBook() {
        if (!isSpellBookReady()) {
            int seconds = getSpellBookCooldownSeconds();
            player.sendMessage(instance.color("&c&l(!) &rYour &6&lSpell Book&r is still regenerating for &a" + seconds + "s"));
            return;
        }

        lastSpellBookUse = System.currentTimeMillis();
        pickSpell();
    }

    // -----------------------------------------------------------------------
    //  Spell selection
    // -----------------------------------------------------------------------

    private void pickSpell() {
        clearCurrentSpellEffects();

        spellPicked = true;

        int newSpell = new Random().nextInt(4) + 1;

        // If you already have a spell, try to give a different one when rerolling.
        if (spell != -1) {
            int attempts = 0;

            while (newSpell == spell && attempts < 10) {
                newSpell = new Random().nextInt(4) + 1;
                attempts++;
            }
        }

        spell = newSpell;

        // Make the newly selected active spell ready right away.
        wizard.startTime = System.currentTimeMillis() - 100000;

        switch (spell) {
            case 1:
                // Teleport - Sharp 4, KB 1
                weapon = makeWand(
                        "&6&lMagic Wand: &5&lTeleport",
                        4,
                        1,
                        "",
                        instance.color("&7Right click to teleport forward"),
                        "",
                        instance.color("&rRange: &a" + TELEPORT_RANGE_BLOCKS + " blocks"),
                        instance.color("&rCooldown: &a" + TELEPORT_COOLDOWN_MS / 1000 + "s")
                );
                instance.TellAll(instance.color("&6&lWizard> &rA rift opens before me... Teleport!"));
                break;

            case 2:
                // Triple Fireball - Sharp 3, KB 2
                weapon = makeWand(
                        "&6&lMagic Wand: &6&lFireballs",
                        3,
                        2,
                        "",
                        instance.color("&7Shoot fireballs, setting enemies on fire"),
                        "",
                        instance.color("&rCooldown: &a" + FIREBALL_COOLDOWN_MS / 1000 + "s")
                );
                instance.TellAll(instance.color("&6&lWizard> &rFlames answer my call... &rFireballs!"));
                break;

            case 3:
                // Speed 2 + Jump Boost 3 - Sharp 3, KB 1
                weapon = makeWand(
                        "&6&lMagic Wand: &b&lSpeedy Jumpy",
                        3,
                        1,
                        "",
                        instance.color("&7You have &aSpeed 2 &r& &aJump Boost 3")
                );
                speedyjumpy = true;
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 2));
                instance.TellAll(instance.color("&6&lWizard> &rLook at me... Speedy speedy jumpy jumpy!"));
                break;

            case 4:
                // Blindness on hit - Sharp 3, KB 2
                weapon = makeWand(
                        "&6&lMagic Wand: &8&lDarkness",
                        3,
                        2,
                        "",
                        instance.color("&7Hitting enemies have a &e1/3 &7chance"),
                        instance.color("&7to apply &8&oBlindness&r for &e5s")
                );
                blindness = true;
                instance.TellAll(instance.color("&6&lWizard> &rShadows gather around my enemies... Darkness!"));
                break;
        }

        player.getInventory().setItem(0, weapon);
        player.getInventory().setItem(1, spellBook);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1.0f, 1.4f);
    }

    private void clearCurrentSpellEffects() {
        if (speedyjumpy) {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.JUMP);
        }

        speedyjumpy = false;
        blindness = false;
    }

    // -----------------------------------------------------------------------
    //  Spell 1: Teleport
    // -----------------------------------------------------------------------

    private void useTeleport() {
        if (wizard.getTime() < TELEPORT_COOLDOWN_MS) {
            int seconds = (TELEPORT_COOLDOWN_MS - wizard.getTime()) / 1000 + 1;
            player.sendMessage(instance.color("&c&l(!) &5&lTeleport&r is still on cooldown for &a" +
                    seconds + "s"));
            return;
        }

        BlockIterator bi = new BlockIterator(player.getEyeLocation(), 0, TELEPORT_RANGE_BLOCKS);
        Block lastFree = null;

        while (bi.hasNext()) {
            Block block = bi.next();

            if (block.getType().isSolid()) {
                break;
            }

            lastFree = block;
        }

        if (lastFree == null) {
            player.sendMessage(instance.color("&c&l(!) &rNo safe space to teleport to!"));
            return;
        }

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

    private void playVanishEffect(Location loc) {
        World world = loc.getWorld();

        int rings = 5;
        int points = 10;

        for (int ring = 0; ring < rings; ring++) {
            double y = ring * 0.4;
            double radius = 0.3 + ring * 0.12;
            double offset = ring * 0.35;

            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI / points) * i + offset;
                Location p = loc.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
                world.playEffect(p, Effect.PORTAL, 1);
            }
        }

        for (int i = 0; i < 5; i++) {
            world.playEffect(loc.clone().add(0, 1, 0), Effect.PORTAL, 1);
        }

        world.playSound(loc, Sound.ENDERMAN_TELEPORT, 1.0f, 0.75f);
        world.playSound(loc, Sound.PORTAL_TRAVEL, 0.3f, 1.8f);
    }

    private void playAppearEffect(Location loc) {
        World world = loc.getWorld();

        int rings = 3;
        int points = 8;

        for (int ring = 0; ring < rings; ring++) {
            double y = ring * 0.45;
            double radius = 0.85 - ring * 0.20;
            double offset = ring * 0.5;

            for (int i = 0; i < points; i++) {
                double angle = (2 * Math.PI / points) * i + offset;
                Location p = loc.clone().add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
                world.playEffect(p, Effect.PORTAL, 1);
            }
        }

        for (int i = 0; i < 3; i++) {
            world.playEffect(loc.clone().add(0, 1, 0), Effect.PORTAL, 1);
        }

        world.playSound(loc, Sound.ENDERMAN_TELEPORT, 1.0f, 1.3f);
    }

    // -----------------------------------------------------------------------
    //  Spell 2: Triple Fireball
    // -----------------------------------------------------------------------

    private void useFireball() {
        if (wizard.getTime() < FIREBALL_COOLDOWN_MS) {
            int seconds = (FIREBALL_COOLDOWN_MS - wizard.getTime()) / 1000 + 1;
            player.sendMessage(instance.color("&c&l(!) &rFireballs are still on cooldown for &a" + seconds + "s"));
            return;
        }

        wizard.restart();

        Location chest = player.getLocation().add(0, 1.2, 0);
        Vector dir = player.getEyeLocation().getDirection();

        Vector right = dir.clone().crossProduct(new Vector(0, 1, 0));
        if (right.lengthSquared() < 0.001) right = new Vector(1, 0, 0);
        right.normalize();

        shootBlazePowder(chest.clone(), dir);
        shootBlazePowder(chest.clone().add(right), dir);
        shootBlazePowder(chest.clone().subtract(right), dir);

        SoundManager.playSoundToAll(player, Sound.GHAST_FIREBALL, 1, 1);
    }

    private void shootBlazePowder(Location from, Vector direction) {
        final Vector straightVelocity = direction.clone().normalize().multiply(2.5);

        ItemProjectile proj = new ItemProjectile(instance, player, new ProjectileOnHit() {
            @Override
            public void onHit(Player hit) {
                if (hit != null && hit.getGameMode() != GameMode.SPECTATOR) {
                    hit.setFireTicks(100);
                }
            }
        }, new ItemStack(Material.BLAZE_POWDER));

        instance.getGameManager().getProjManager().shootProjectile(proj, from, straightVelocity.clone());

        keepProjectileFlyingStraight(proj, straightVelocity);
    }

    private void keepProjectileFlyingStraight(final ItemProjectile proj, final Vector velocity) {
        new BukkitRunnable() {
            private int ticksAlive = 0;

            @Override
            public void run() {
                ticksAlive++;

                if (proj == null || proj.getEntity() == null || !proj.getEntity().isValid() || proj.getEntity().isDead()) {
                    this.cancel();
                    return;
                }

                if (ticksAlive > 100) {
                    proj.getEntity().remove();
                    this.cancel();
                    return;
                }

                proj.getEntity().setVelocity(velocity.clone());
            }
        }.runTaskTimer(instance.getGameManager().getMain(), 1L, 1L);
    }
}