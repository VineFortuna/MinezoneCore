package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ChatColorHelper;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class ParrotClass extends BaseClass {

    private final ItemStack weapon;
    private final ItemStack danceItem;
    private final Ability flapAbility = new Ability("&a&lFlap", 2, player);
    private final Ability danceAbility = new Ability("&a&lDance", 25, player);
    private BukkitRunnable danceRunnable;
    private Block danceTargetBlock;

    private int armorColorCounter = 1;

    private static final double DANCE_ABILITY_RADIUS = 8;
    private static final double DANCE_ABILITY_DURATION = 10;
    private static final double HEAL_PER_SECOND = 1.0 ;
    private static final int JUMP_BOOST_AMP = 0;

    private static final Material[] RECORDS = {
            Material.RECORD_3, Material.RECORD_4, Material.RECORD_5, Material.RECORD_6,
            Material.RECORD_7, Material.RECORD_8, Material.RECORD_9, Material.RECORD_10,
            Material.RECORD_12
    };

    private static final int TICKS_PER_SECOND = 20;

    private final int protectionLevel = 6;

    public ParrotClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZjOWEzYjlkNTg3OWMyMTUwOTg0ZGJmZTU4OGNjMmU2MWZiMWRlMWU2MGZkMmE0NjlmNjlkZDRiNmY2YTk5MyJ9fX0=",
                "8DCE00",
                "8DCE00",
                "BD9D9D",
                protectionLevel,
                "Parrot"
        );

        // Weapon
        weapon = ItemHelper.setDetails(new ItemStack(Material.FEATHER),
                flapAbility.getAbilityNameRightClickMessage(),
                "",
                "&7Flap your wings up");
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 4);

        // Dance Ability
        double healingAmount = DANCE_ABILITY_DURATION * HEAL_PER_SECOND / 2;
        String healingDisplay = ItemHelper.formatDouble(healingAmount);
        String durationDisplay = ItemHelper.formatDouble(DANCE_ABILITY_DURATION);
        String radiusDisplay = ItemHelper.formatDouble(DANCE_ABILITY_RADIUS);

        danceItem = ItemHelper.setDetails(new ItemStack(Material.JUKEBOX),
                danceAbility.getAbilityNameRightClickMessage(),
                "&7Place down a jukebox",
                "&7and dance to regenerate health",
                "",
                "&7Heals &e" + healingDisplay + " &c❤ &7over &a" + durationDisplay + " &7s",
                "&7Range: &a" + radiusDisplay + " &7blocks");
    }

    @Override
    public void Tick(int gameTicks) {
        if (!(player.getActivePotionEffects().contains(PotionEffectType.JUMP)))
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, JUMP_BOOST_AMP));

        // ActionBar
        if (isPlayerAlive()) {
            ActionBarManager actionBarManager = this.getActionBarManager();
            ActionBarManager.AbilityActionBar abilityActionBar = new ActionBarManager.AbilityActionBar(this, actionBarManager);
            abilityActionBar.setActionBarAbility(player, flapAbility, danceAbility);
        }

        if (gameTicks % TICKS_PER_SECOND != 0) return;

        // Healing
        handleHealing();

    }

    private void handleHealing() {
        if (isDanceAbilityActive() && isInArea()) {
            player.setHealth(Math.min(player.getHealth() + HEAL_PER_SECOND, player.getMaxHealth()));
            selectDanceArmor(armorColorCounter++);
        }
    }

    @Override
    public void SetItems(Inventory playerInv) {
        // Resetting Dance Ability CD
        danceAbility.getCooldownInstance().reset();
        flapAbility.getCooldownInstance().reset();

        playerInv.setItem(0, weapon);
        playerInv.setItem(1, danceItem);

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, JUMP_BOOST_AMP));
    }

    @Override
    public void UseItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        if (action == Action.RIGHT_CLICK_BLOCK) {
            // DANCE ABILITY
            if (item.equals(danceItem)) {
                if (!danceAbility.isReady()) return;

                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock == null) return;

                BlockFace blockFace = event.getBlockFace();
                danceTargetBlock = clickedBlock.getRelative(blockFace);

                // Check if block is not air
                if (danceTargetBlock.getType() != Material.AIR) return;

                Block blockBelow = danceTargetBlock.getRelative(BlockFace.DOWN);
                Block blockAbove = danceTargetBlock.getRelative(BlockFace.UP);

                // Check if blocks below and above are not solid
                if (!blockBelow.getType().isSolid() && !blockAbove.getType().isSolid()) return;

                setDanceAbility();
                danceAbility.use();
            }
        }

        if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            // FLAP ABILITY
            if (item.equals(weapon)) {
                if (!flapAbility.isReady()) return;

                player.setVelocity(new Vector(0, 0.8, 0));
                SoundManager.playSoundToAllGamePlayersFromALocation(instance, player.getLocation(), Sound.BAT_TAKEOFF, 0.8f, 1.6f);

                flapAbility.use();
            }
        }
    }

    @Override
    public void Death(PlayerDeathEvent e) {
        super.Death(e);
        if (isDanceAbilityActive()) cleanupDanceAbility();
    }

    private void setDanceAbility() {
        setupJukebox();
        playRandomRecord();
        startDanceRunnable();
    }

    private void startDanceRunnable() {
        danceRunnable = new BukkitRunnable() {
            double duration = (DANCE_ABILITY_DURATION * TICKS_PER_SECOND); // Duration Ticks

            @Override
            public void run() {
                if (duration <= 0) {
                    cleanupDanceAbility();
                    return;
                }
                // Run every tick
                spawnParticles();
                duration--;
                if (duration % TICKS_PER_SECOND != 0) return;
                // Run every second

            }
        };

        danceRunnable.runTaskTimer(instance.getGameManager().getMain(), 0, 1);
    }

    private void setupJukebox() {
        assert danceTargetBlock != null;
        danceTargetBlock.setType(Material.JUKEBOX);
    }

    private void playRandomRecord() {
        Random random = new Random();
        Material randomRecord = RECORDS[random.nextInt(RECORDS.length)];
        player.getWorld().playEffect(danceTargetBlock.getLocation(), Effect.RECORD_PLAY, randomRecord);
        player.sendMessage(randomRecord.name()); // to delete after
    }

    private void cleanupDanceAbility() {
        if (danceRunnable != null) {
            danceRunnable.cancel();
            danceRunnable = null;
        }

        // Stopping Disc
        player.getWorld().playEffect(danceTargetBlock.getLocation(), Effect.RECORD_PLAY, 0);

        if (danceTargetBlock != null) {
            danceTargetBlock.setType(Material.AIR);
            danceTargetBlock = null;
        }

        selectDanceArmor(5);
        armorColorCounter = 1;
    }

    private void spawnParticles() {
        int particleAmount = 10; // Adjust the number of particles per tick

        for (int i = 0; i < particleAmount; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);
            double x = ParrotClass.DANCE_ABILITY_RADIUS * Math.sin(phi) * Math.cos(theta);
            double y = ParrotClass.DANCE_ABILITY_RADIUS * Math.sin(phi) * Math.sin(theta);
            double z = ParrotClass.DANCE_ABILITY_RADIUS * Math.cos(phi);

            Location particleLoc = danceTargetBlock.getLocation().clone().add(x, y, z);
            if (particleLoc.getBlock().getType() != Material.AIR) continue;
            player.getWorld().playEffect(particleLoc, Effect.NOTE, 1);
        }
    }

    private void selectDanceArmor(int colorSelector) {
        switch (colorSelector) {
            case 1:
            case 6:
                // Dark Blue
                setDanceArmor("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBlMDNiMTBjMTVlZTU2MDE0MjM4NjdkZmI4YmNiY2JjOTE5Y2E5NmMwZWVhNjMwNzNlYzhlNzk1ZWFiZDA1ZiJ9fX0=", "1C3DF7");
                break;
            case 2:
            case 7:
                // Cyan
                setDanceArmor("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmM2NDcxZjIzNTQ3YjJkYmRmNjAzNDdlYTEyOGY4ZWIyYmFhNmE3OWIwNDAxNzI0ZjIzYmQ0ZTI1NjRhMmI2MSJ9fX0=", "31BCEA");
                break;
            case 3:
            case 8:
                // Red
                setDanceArmor("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWQxYTE2OGJjNzJjYjMxNGY3Yzg2ZmVlZjlkOWJjNzYxMjM2NTI0NGNlNjdmMGExMDRmY2UwNDIwMzQzMGMxZCJ9fX0=", "FF1F27");
                break;
            case 4:
            case 9:
                // Gray
                setDanceArmor("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNjMzQ3MjJhYzY0NDk2YzliODRkMGM1NDAxOWRhYWU2MTg1ZDYwOTQ5OTAxMzNhZDY4MTBlZWEzZDI0MDY3YSJ9fX0=", "CCCBC3");
                break;
            case 5:
            case 10:
                // Green
                setArmorNew(player.getEquipment());
                break;
            default:
        }
    }

    private void setDanceArmor(String textureUrl, String hexCode) {
        // Head (helmet)
        ItemStack danceHead = ItemHelper.createSkullTexture(textureUrl, playerHead.getItemMeta().getDisplayName());
        danceHead.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protectionLevel);
        ItemHelper.createSkullTexture(textureUrl);
        player.getEquipment().setHelmet(danceHead);

        // Chestplate
        ItemStack danceChestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, hexCode, chestplate.getItemMeta().getDisplayName());
        player.getEquipment().setChestplate(danceChestplate);

        // Leggings
        ItemStack danceLeggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, hexCode, leggings.getItemMeta().getDisplayName());
        player.getEquipment().setLeggings(danceLeggings);
    }

    private boolean isInArea() {
        return player.getLocation().distance(danceTargetBlock.getLocation()) <= ParrotClass.DANCE_ABILITY_RADIUS;
    }

    private boolean isDanceAbilityActive() {
        return danceRunnable != null && danceTargetBlock != null;
    }

    @Override
    public ClassType getType() {
        return ClassType.Parrot;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }
}
