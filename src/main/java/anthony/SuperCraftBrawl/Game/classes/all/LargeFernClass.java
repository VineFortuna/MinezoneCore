package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import anthony.util.TitleHelper;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
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
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.BlockTexture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LargeFernClass extends BaseClass {

    // Spore Burst
    /** What a spore does to a player in the class armor (6 armor points, Protection 6 on the head): about 0.5 ❤. */
    private static final double SPORE_EFFECTIVE_DAMAGE = 1.0;
    /** How much of a hit is left on average after the class armor. */
    private static final double CLASS_ARMOR_MITIGATION = 0.53;
    /** The damage a spore is dealt with, before the armor lowers it to SPORE_EFFECTIVE_DAMAGE. */
    private static final double SPORE_DAMAGE = SPORE_EFFECTIVE_DAMAGE / CLASS_ARMOR_MITIGATION;
    private static final double SPORE_SPEED = 1.5;
    /** How far the corner spores are off the aim, for every block they fly. */
    private static final double SPORE_SPREAD = 0.15;
    /** The spores of a burst land within a few ticks of each other, the damage is dealt once they all did. */
    private static final long SPORE_DAMAGE_DELAY_TICKS = 3;
    private static final int SPORE_AMOUNT = 5;

    // Transfern
    private static final double SMALL_FERN_TIME = 2;
    private static final double LARGE_FERN_TIME = 10;
    private static final double MAX_LARGE_FERN_TIME = 10;
    /** The invisible player's armor is sent as empty this often, since the server sends the real one again every now and then. */
    private static final int ARMOR_HIDE_TICKS = 2;
    /** The block breaking particles shown when a block of the fern is placed or removed. */
    private static final int BLOCK_PARTICLES = 12;
    private static final byte FERN_DATA = 2;
    private static final byte LARGE_FERN_BOTTOM_DATA = 3;
    private static final byte LARGE_FERN_TOP_DATA = 11;
    private static final PotionEffect REGENERATION = new PotionEffect(PotionEffectType.REGENERATION, 9999999, 2, false, false);
    private static final PotionEffect INVISIBILITY = new PotionEffect(PotionEffectType.INVISIBILITY, 99999, 0, false, false);

    private final ItemStack weapon;
    private final ItemStack transfernItem;
    private final Ability sporeAbility = new Ability("&2&lSpore Burst", 4, player);
    private final Ability transfernAbility = new Ability("&2&lTransfern", player);
    private TransfernRunnable transfernRunnable;

    public LargeFernClass(GameInstance instance, Player player) {
        super(instance, player);
        createArmor(
                null,
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQxNTczYmYyZTAyNzBiYjYyMDNkMmI3NjRkZDdkMGNiYmM1ZDdiMWJhNmNkY2NjOWFmNWZmNDc0MzRhMGViNCJ9fX0=",
                "285F3F",
                6,
                "LargeFern"
        );

        // Weapon
        weapon = ItemHelper.setDetails(
                new ItemStack(Material.DOUBLE_PLANT, 1, (short) 3),
                sporeAbility.getAbilityNameRightClickMessage(),
                "",
                "&7Launch a burst of spores in an X pattern",
                "&7Each spore that hits deals &e" + ItemHelper.formatDouble(SPORE_EFFECTIVE_DAMAGE / 2) + " &c❤",
                "",
                "&7Effective at close range"
        );
        weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);
        weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);

        // Disguise Ability
        transfernItem = ItemHelper.setDetails(
                new ItemStack(Material.LONG_GRASS, 1, (short) 2),
                transfernAbility.getAbilityNameRightClickMessage(),
                "&7Transform into a fern to regenerate health",
                "",
                "&7Moving or performing actions cancel this",
                "",
                "&7Small Fern Time: &a" + ItemHelper.formatDouble(SMALL_FERN_TIME) + "&as",
                "&7Large Fern Time: &a" + ItemHelper.formatDouble(LARGE_FERN_TIME) + "&as",
                "&7Max Large Fern Time: &a" + ItemHelper.formatDouble(MAX_LARGE_FERN_TIME) + "&as"
        );
    }

    @Override
    public void setItems(Inventory playerInv) {
        sporeAbility.getCooldownInstance().reset();
        playerInv.setItem(0, weapon);
        playerInv.setItem(1, transfernItem);
    }

    @Override
    public void tick(int gameTicks) {
        if (!isPlayerAlive()) return;
        sporeAbility.updateActionBar(player, this);
    }

    @Override
    public void useItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        // Any action ends the transformation, so clicking the Transfern item again ends it too instead of starting over
        if (transfernRunnable != null) {
            endTransfern();
            return;
        }

        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        if (item.equals(weapon)) {
            event.setCancelled(true);
            if (!sporeAbility.isReady()) return;
            useSporesAbility();
            sporeAbility.use();
        } else if (item.equals(transfernItem)) {
            useTransfernAbility();
        }
    }

    private void useSporesAbility() {
        Vector aim = player.getEyeLocation().getDirection();

        // Perpendicular to the aim, the corner spores are offset along these
        Vector right = aim.clone().crossProduct(new Vector(0, 1, 0));
        if (right.lengthSquared() < 0.001) right = new Vector(1, 0, 0); // aiming straight up or down
        right.normalize();
        Vector up = right.clone().crossProduct(aim).normalize();

        // Always the same X, a spore at each corner and one in the middle
        List<Vector> directions = new ArrayList<>(SPORE_AMOUNT);
        directions.add(aim.clone());
        for (int sideways = -1; sideways <= 1; sideways += 2)
            for (int vertical = -1; vertical <= 1; vertical += 2)
                directions.add(aim.clone()
                        .add(right.clone().multiply(sideways * SPORE_SPREAD))
                        .add(up.clone().multiply(vertical * SPORE_SPREAD)));

        SporeBurst burst = new SporeBurst();
        for (Vector direction : directions)
            instance.getGameManager().getProjManager().shootProjectile(
                    burst.createSpore(), player.getEyeLocation(), direction.normalize().multiply(SPORE_SPEED));

        SoundManager.playSoundToAll(player, Sound.EXPLODE, 1, 4);
    }

    private boolean isEnemy(Player other) {
        return other != player
                && (instance.duosMap == null || !instance.team.get(other).equals(instance.team.get(player)));
    }

    /**
     * One shot of the Spore Burst. A player only has a moment of immunity after taking damage, so the spores
     * don't damage on their own: they are counted, and the damage depends on how many of them hit.
     */
    private class SporeBurst {
        private final Map<Player, Integer> sporesHit = new HashMap<>();

        private ItemProjectile createSpore() {
            return new ItemProjectile(instance, player, new ProjectileOnHit() {
                @Override
                public void onHit(Player hit) {
                    if (hit == null || hit.getGameMode() == GameMode.SPECTATOR || !isEnemy(hit)) return;

                    // The first spore to hit starts the wait for the others of the burst
                    if (sporesHit.merge(hit, 1, Integer::sum) == 1)
                        Bukkit.getScheduler().runTaskLater(instance.getGameManager().getMain(),
                                () -> damage(hit), SPORE_DAMAGE_DELAY_TICKS);
                }
            }, new ItemStack(Material.WOOD_BUTTON));
        }

        private void damage(Player hit) {
            Integer spores = sporesHit.remove(hit);
            if (spores == null || !hit.isOnline() || hit.getGameMode() == GameMode.SPECTATOR) return;
            hit.damage(spores * SPORE_DAMAGE, player);
        }
    }

    private void useTransfernAbility() {
        if (!player.isOnGround()) {
            transfernAbility.sendCustomMessage(transfernAbility.getOnGroundChatMessage());
            return;
        }

        Location playerLoc = player.getLocation();
        Block feetBlock = playerLoc.getBlock();
        Block headBlock = playerLoc.clone().add(0, 1, 0).getBlock();
        Block groundBlock = playerLoc.clone().subtract(0, 1, 0).getBlock();

        if (feetBlock.getType() == Material.AIR &&
                headBlock.getType() == Material.AIR &&
                groundBlock.getType().isSolid()) {

            transfernRunnable = new TransfernRunnable();
            instance.trackClassTask(transfernRunnable.runTaskTimer(instance.getGameManager().getMain(), 0, 1));
        } else {
            transfernAbility.sendCustomMessage("&c&l(!) &rYou cannot transfern on this block");
        }
    }

    /** Ends the transformation if there is one going: the fern goes away and the player shows again. */
    public void endTransfern() {
        if (transfernRunnable == null) return;
        transfernRunnable.end(false);
    }

    @Override
    public void onDeath(PlayerDeathEvent e) {
        super.onDeath(e);
        endTransfern();
    }

    @Override
    public void GameEnd() {
        endTransfern();
    }

    /**
     * Stand still to turn into a small fern, then a large one that regenerates health. The player becomes
     * invisible without a name tag or armor, but can still be hit. It ends when they move, act, get far enough
     * on the large fern, or the large fern runs out of time.
     */
    private class TransfernRunnable extends BukkitRunnable {
        private int ticks = 0;
        private int standingStillSeconds = 0;
        private int largeFernSeconds = 0;
        private int lastDisplayedTime = -1;
        private boolean smallFernSet = false;
        private boolean largeFernSet = false;
        private boolean ended = false;
        private Location fernLocation;

        private final int smallFernTime = (int) Math.ceil(SMALL_FERN_TIME);
        private final int largeFernTime = (int) Math.ceil(LARGE_FERN_TIME);
        private final int maxLargeFernTime = (int) Math.ceil(MAX_LARGE_FERN_TIME);

        // The ground block the fern is placed on is turned into grass, and put back
        private Block originalGroundBlock;
        private Material originalGroundType;
        private byte originalGroundData;

        @Override
        public void run() {
            // Also stops if they're no longer playing this class here, which otherwise only GameEnd and quitting catch
            if (!player.isOnline() || instance.classes.get(player) != LargeFernClass.this) {
                end(false);
                return;
            }

            if (smallFernSet && ticks % ARMOR_HIDE_TICKS == 0) sendArmor(true);

            boolean secondPassed = ticks++ % 20 == 0;
            if (!secondPassed) return;

            if (instance.hasPlayerMovedPosition(player)) {
                end(false);
                return;
            }

            if (largeFernSet) {
                if (++largeFernSeconds >= maxLargeFernTime) end(true);
                return;
            }

            int remainingTime = largeFernTime - standingStillSeconds;
            if (remainingTime > 0 && remainingTime != lastDisplayedTime) {
                TitleHelper.sendTitle(player, "", "&2" + remainingTime + "...", 0, 20, 5);
                lastDisplayedTime = remainingTime;
            }

            if (!smallFernSet && standingStillSeconds >= smallFernTime) setSmallFern();
            if (standingStillSeconds >= largeFernTime) setLargeFern();

            standingStillSeconds++;
        }

        private void setSmallFern() {
            smallFernSet = true;
            SoundManager.playSoundToAll(player, Sound.DIG_GRASS, 1, 1);
            player.addPotionEffect(INVISIBILITY);
            instance.setNameTagVisible(player, false);
            sendArmor(true);

            fernLocation = player.getLocation().clone();

            originalGroundBlock = fernLocation.clone().subtract(0, 1, 0).getBlock();
            originalGroundType = originalGroundBlock.getType();
            originalGroundData = originalGroundBlock.getData();

            // Change the ground to grass to allow fern placement
            originalGroundBlock.setType(Material.GRASS);
            originalGroundBlock.setData((byte) 0);
            showBlockParticles(originalGroundBlock, Material.GRASS, (byte) 0);

            fernLocation.getBlock().setType(Material.LONG_GRASS);
            fernLocation.getBlock().setData(FERN_DATA);
            showBlockParticles(fernLocation.getBlock(), Material.LONG_GRASS, FERN_DATA);
        }

        private void setLargeFern() {
            if (largeFernSet) return;
            largeFernSet = true;
            SoundManager.playSoundToAll(player, Sound.DIG_GRASS, 1, 1);
            player.addPotionEffect(REGENERATION);

            // Replace the small fern with the large one, the bottom part and the top part
            Block bottomBlock = fernLocation.getBlock();
            removeBlock(bottomBlock, Material.LONG_GRASS, FERN_DATA);

            bottomBlock.setType(Material.DOUBLE_PLANT);
            bottomBlock.setData(LARGE_FERN_BOTTOM_DATA);
            showBlockParticles(bottomBlock, Material.DOUBLE_PLANT, LARGE_FERN_BOTTOM_DATA);

            Block topBlock = fernLocation.clone().add(0, 1, 0).getBlock();
            topBlock.setType(Material.DOUBLE_PLANT);
            topBlock.setData(LARGE_FERN_TOP_DATA);
            showBlockParticles(topBlock, Material.DOUBLE_PLANT, LARGE_FERN_TOP_DATA);
        }

        /** @param timedOut the large fern ran out of time, which is not a failure so it has no error sound */
        private void end(boolean timedOut) {
            if (ended) return;
            ended = true;
            cancel();
            if (transfernRunnable == this) transfernRunnable = null;

            player.removePotionEffect(INVISIBILITY.getType());
            player.removePotionEffect(REGENERATION.getType());
            if (smallFernSet) {
                instance.setNameTagVisible(player, true);
                sendArmor(false);
            }

            // The fern goes away before the ground it grows on is restored, or it would break off and drop seeds
            if (smallFernSet) SoundManager.playSoundToAll(player, fernLocation, Sound.DIG_GRASS, 1, 1);
            if (largeFernSet) {
                removeBlock(fernLocation.clone().add(0, 1, 0).getBlock(), Material.DOUBLE_PLANT, LARGE_FERN_TOP_DATA);
                removeBlock(fernLocation.getBlock(), Material.DOUBLE_PLANT, LARGE_FERN_BOTTOM_DATA);
            } else if (smallFernSet) {
                removeBlock(fernLocation.getBlock(), Material.LONG_GRASS, FERN_DATA);
            }

            // Restore the original ground block if it was changed
            if (originalGroundBlock != null) {
                showBlockParticles(originalGroundBlock, Material.GRASS, (byte) 0);
                originalGroundBlock.setType(originalGroundType);
                originalGroundBlock.setData(originalGroundData);
            }

            if (!timedOut && (!largeFernSet || player.getHealth() != player.getMaxHealth()))
                SoundManager.playErrorSound(player);

            instance.clearLastPosition(player);
        }

        /** Takes the block away without block updates, so a plant never breaks off and drops anything, with its particles. */
        @SuppressWarnings("deprecation")
        private void removeBlock(Block block, Material type, byte data) {
            showBlockParticles(block, type, data);
            block.setTypeIdAndData(Material.AIR.getId(), (byte) 0, false);
        }

        private void showBlockParticles(Block block, Material type, byte data) {
            ParticleEffect.BLOCK_CRACK.display(block.getLocation().add(0.5, 0.5, 0.5),
                    0.3f, 0.3f, 0.3f, 0.05f, BLOCK_PARTICLES, new BlockTexture(type, data));
        }

        /** Everyone else is sent the armor slots of the player as empty, or as what they are really wearing. */
        private void sendArmor(boolean hidden) {
            ItemStack[] armor = player.getInventory().getArmorContents(); // boots, leggings, chestplate, helmet
            PacketPlayOutEntityEquipment[] packets = new PacketPlayOutEntityEquipment[armor.length];
            for (int slot = 0; slot < armor.length; slot++)
                packets[slot] = new PacketPlayOutEntityEquipment(player.getEntityId(), slot + 1,
                        hidden ? null : CraftItemStack.asNMSCopy(armor[slot]));

            for (Player viewer : player.getWorld().getPlayers()) {
                if (viewer == player) continue;
                for (PacketPlayOutEntityEquipment packet : packets)
                    ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    @Override
    public ClassType getType() {
        return ClassType.LargeFern;
    }

    @Override
    public ItemStack getAttackWeapon() {
        return weapon;
    }
}
