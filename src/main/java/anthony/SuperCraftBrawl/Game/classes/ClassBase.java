package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Game.classes.dataTypes.*;
import anthony.SuperCraftBrawl.Timer;
import io.vavr.control.Either;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ClassMetaData(
        iconBlock = Material.GRASS,
        iconHead = "",
        masteryHead = "",
        title = "Base",
        description = "You shouldn't be able to see this?",
        category = ClassCategory.INVALID,
        tokenCount = 0,
        uniqueIdentifier = 0
)
public abstract class ClassBase {
    /* All the relevant class data. */
    private ClassData internalData;

    /* The object of the player of the selected class. */
    private final Player player;

    /* List of all the timers and their lengths. */
    private static List<Pair<Timer, Integer>> timers;

    private void fetchDataFromConfig() {
        internalData = ClassDataManager.loadCurrentClassConfig();
    }

    private void assignArmour() {
        List<ItemStack> armour = getClassArmour().createArmour();

        player.getEquipment().setArmorContents(armour.toArray(new ItemStack[0]));
    }

    private void assignWeapon() {
        ItemStack weapon = getClassWeapon().createItemStack();

        player.getInventory().setItem(0, weapon);
    }

    private void assignOtherItems() {
        List<ItemStack> items = getClassItems().stream().map(ClassItem::createItemStack).toList();

        int i = 1;
        for (ItemStack item : items) {
            player.getInventory().setItem(i, item);
            i++;
        }
    }

    private void assignTimers() {
        timers = getTimerLengths().stream().map(timerLength -> {
            Timer timer = new Timer();
            return Pair.of(timer, timerLength);
        }).collect(Collectors.toList());
    }

    public ClassBase(Player player) {
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

    /* Metadata getters */
    public Either<Material, String>       getClassIcon() { return ClassMetaDataManager.            getClassIcon(this.getClass()); }
    public String                  getClassMasteryHead() { return ClassMetaDataManager.     getClassMasteryHead(this.getClass()); }
    public String                        getClassTitle() { return ClassMetaDataManager.           getClassTitle(this.getClass()); }
    public String                  getClassDescription() { return ClassMetaDataManager.     getClassDescription(this.getClass()); }
    public ClassCategory              getClassCategory() { return ClassMetaDataManager.        getClassCategory(this.getClass()); }
    public Optional<Integer>        getClassTokenCount() { return ClassMetaDataManager.      getClassTokenCount(this.getClass()); }
    public Integer            getClassUniqueIdentifier() { return ClassMetaDataManager.getClassUniqueIdentifier(this.getClass()); }

    /* Internal data getters */
    public ClassArmour                  getClassArmour() { return internalData.armour();                                          }
    public Either<Material, String>     getClassHelmet() { return internalData.armour().helmet();                                 }
    public Either<Material, String> getClassChestplate() { return internalData.armour().chestplate();                             }
    public Either<Material, String>   getClassLeggings() { return internalData.armour().leggings();                               }
    public Either<Material, String>      getClassBoots() { return internalData.armour().boots();                                  }
    public Integer             getClassProtectionLevel() { return internalData.armour().protectionLevel();                        }
    public List<Integer>               getTimerLengths() { return internalData.timerLengths();                                    }
    public ClassItem                    getClassWeapon() { return internalData.weapon();                                          }
    public Material                 getClassWeaponItem() { return internalData.weapon().item();                                   }
    public String                   getClassWeaponName() { return internalData.weapon().name();                                   }
    public List<String>             getClassWeaponLore() { return internalData.weapon().lore();                                   }
    public List<ClassItem>               getClassItems() { return internalData.items();                                           }

    public void tick() {
        /* TODO: */
    }

    void useItem(PlayerInteractEvent event)                  { /* ... */ }
    void onConsumingItem(PlayerItemConsumeEvent event)       { /* ... */ }
    void takeDamage(EntityDamageEvent event)                 { /* ... */ }
    void projectileLaunch(ProjectileLaunchEvent event)       { /* ... */ }
    void projectileHit(ProjectileHitEvent event)             { /* ... */ }
    void potionSplashEvent(PotionSplashEvent event)          { /* ... */ }
    void doDamage(EntityDamageByEntityEvent event)           { /* ... */ }
    void onEntityTarget(EntityTargetLivingEntityEvent event) { /* ... */ }
    void onPlayerMove(PlayerMoveEvent event)                 { /* ... */ }
}
