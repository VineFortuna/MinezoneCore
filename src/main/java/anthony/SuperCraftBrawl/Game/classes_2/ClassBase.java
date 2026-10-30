package anthony.SuperCraftBrawl.Game.classes_2;

import anthony.SuperCraftBrawl.Game.classes_2.dataTypes.ClassData;
import anthony.SuperCraftBrawl.Game.classes_2.dataTypes.ClassItem;
import anthony.SuperCraftBrawl.Timer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ClassBase {
    private static ClassData internalData;
    private final Player player;
    private static List<Pair<Timer, Integer>> timers;

    private static double jumpVelocity = 1.0;

    private void fetchDataFromConfig() {
        internalData = ClassDataManager.loadCurrentClassConfig();
    }

    private void assignArmour() {
        List<ItemStack> armour = internalData.armour().createArmour();

        player.getEquipment().setArmorContents(armour.toArray(new ItemStack[0]));
    }

    private void assignWeapon() {
        ItemStack weapon = internalData.weapon().createItemStack();

        player.getInventory().setItem(0, weapon);
    }

    private void assignOtherItems() {
        List<ItemStack> items = internalData.items().stream().map(ClassItem::createItemStack).toList();

        int i = 1;
        for (ItemStack item : items) {
            player.getInventory().setItem(i, item);
            i++;
        }
    }

    private void assignTimers() {
        timers = internalData.timerLengths().stream().map(timerLength -> {
            Timer timer = new Timer();
            return Pair.of(timer, timerLength);
        }).collect(Collectors.toList());
    }

    ClassBase(Player player) {
        /* Store player object in private member variable */
        this.player = player;

        /* Get data from the config file and load it into a member variable */
        fetchDataFromConfig();

        /* Create and assign armor */
        assignArmour();

        /* Create and assign weapon */
        assignWeapon();

        /* Create and assign additional items */
        assignOtherItems();

        /* Initialize and assign timers */
        assignTimers();

        /* ... */
    }

    public void tick() {
        /* TODO: */
    }

    public void setNameTag() {
        /* TODO: */
    }

    public abstract void useItem(PlayerInteractEvent event);
    public abstract void onConsumingItem(PlayerItemConsumeEvent event);
    public abstract void takeDamage(EntityDamageEvent event);
    public abstract void projectileLaunch(ProjectileLaunchEvent event);
    public abstract void projectileHit(ProjectileHitEvent event);
    public abstract void potionSplashEvent(PotionSplashEvent event);
    public abstract void doDamage(EntityDamageByEntityEvent event);
    public abstract void onEntityTarget(EntityTargetLivingEntityEvent event);
    public abstract void onPlayerMove(PlayerMoveEvent event);

}
