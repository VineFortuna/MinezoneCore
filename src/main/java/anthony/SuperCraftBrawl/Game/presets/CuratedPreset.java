package anthony.SuperCraftBrawl.Game.presets;

import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.GameLootDrops.Rarity;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import org.bukkit.Material;

/*
 * Presets made by us that every player can load. Anything a preset doesn't set stays at its default, and every
 * setting can still be edited after loading it. They're shown in this order: the Presets menu puts the first one
 * in its 1st slot, the second in the 2nd, and so on.
 */
public enum CuratedPreset {

    BRAWL(Material.IRON_SWORD, "&4&lBrawl",
            "&7Close-range classes only,",
            "&7switching every 15 seconds"),
    BULLSEYE(Material.BOW, "&3&lBullseye",
            "&7Ranged classes only,",
            "&7switching every 15 seconds"),
    OP_ITEM_DROPS(Material.WHEAT, "&6&lOP Item Drops",
            "&7Lightning drops only spawn",
            "&9&lRARE &7and &6&lLEGENDARY &7items",
            "&7(No Extra Lives)"),
    ONE_SHOT(Material.ARROW, "&b&lOne Shot",
            "&7Skeletons only, with a single heart,",
            "&7low gravity and 20 lives"),
    ONE_EGG(Material.EGG, "&e&lOne Egg",
            "&7Chickens only, with a single heart,",
            "&7low gravity and 20 lives"),
    PUPPET_MASTER(Material.MONSTER_EGG, "&d&lPuppet Master", // the Summoner's ability icon
            "&7Summoners only, with mob eggs",
            "&7raining down every 5 seconds"),
    LIGHT_EM_UP(Material.FLINT_AND_STEEL, "&6&lL&c&li&6&lg&c&lh&6&lt &c&l'&6&lE&c&lm &6&lU&c&lp",
            "&7Flint and Steel only, with creeper",
            "&7eggs dropping every second"),
    PERMA_DEATH(Material.SKULL_ITEM, "&5&lPerma Death",
            "&7One life and 40 hearts, no Bombs or",
            "&7Extra Lives, and some classes disabled");

    private final Material icon;
    private final String name;
    private final String[] description;

    CuratedPreset(Material icon, String name, String... description) {
        this.icon = icon;
        this.name = name;
        this.description = description;
    }

    public Material getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String[] getDescription() {
        return description;
    }

    /** A fresh preset: default settings with this preset's changes. */
    public GamePreset build() {
        GamePreset preset = new GamePreset();
        GameSettings s = preset.settings;

        switch (this) {
            case BRAWL:
                setClasses(s, ClassType.Hunter, ClassType.Ninja, ClassType.EnderDragon, ClassType.Ice, ClassType.Cloud,
                        ClassType.Jeb, ClassType.Notch, ClassType.ZombiePigman, ClassType.Anvil, ClassType.Chicken,
                        ClassType.TNT, ClassType.Cactus, ClassType.Zombie, ClassType.Herobrine, ClassType.IronGolem,
                        ClassType.Slime, ClassType.MagmaCube, ClassType.PiglinBrute);
                classSwitch(s, 15);
                break;
            case BULLSEYE:
                setClasses(s, ClassType.Skeleton, ClassType.Firework, ClassType.Blaze, ClassType.Wither,
                        ClassType.WitherSkeleton, ClassType.Shulker, ClassType.Vampire, // Shulker = the Levitator
                        ClassType.Ghast);
                classSwitch(s, 15);
                break;
            case OP_ITEM_DROPS:
                s.enabledDrops.clear();
                for (GameLootDrops drop : GameLootDrops.values()) {
                    Rarity rarity = drop.getRarity();
                    if ((rarity == Rarity.RARE || rarity == Rarity.LEGENDARY) && drop != GameLootDrops.EXTRA_LIFE)
                        s.enabledDrops.add(drop);
                }
                break;
            case ONE_SHOT:
                oneHeartLowGravity(s);
                setClasses(s, ClassType.Skeleton);
                break;
            case ONE_EGG:
                oneHeartLowGravity(s);
                setClasses(s, ClassType.Chicken);
                classSwitch(s, 5);
                break;
            case PUPPET_MASTER:
                setClasses(s, ClassType.Summoner);
                classSwitch(s, 15);
                setDrops(s, GameLootDrops.SKELE_EGG, GameLootDrops.CREEPER_EGG, GameLootDrops.ZOMBIE_EGG);
                s.dropTimer = 5;
                tenLivesNoRespawn(s);
                break;
            case LIGHT_EM_UP:
                setClasses(s, ClassType.FlintAndSteel);
                setDrops(s, GameLootDrops.CREEPER_EGG);
                s.dropTimer = 1;
                tenLivesNoRespawn(s);
                break;
            case PERMA_DEATH:
                s.startingLives = 1;
                s.maxHealthHearts = 40;
                s.enabledDrops.remove(GameLootDrops.BOMB);
                s.enabledDrops.remove(GameLootDrops.EXTRA_LIFE);
                // The default class list (so active holiday classes are in it) minus these
                disableClasses(s, ClassType.Summoner, ClassType.Chicken, ClassType.Bunny, ClassType.Bee,
                        ClassType.ZombieVillager, ClassType.ZombiePigman, ClassType.Mooshroom, ClassType.SethBling,
                        ClassType.Melon, ClassType.MagmaCube, ClassType.Endermite, ClassType.PiglinBrute,
                        ClassType.Elf, ClassType.GingerBreadMan, ClassType.Freddy);
                break;
        }
        return preset;
    }

    private static void oneHeartLowGravity(GameSettings s) {
        s.maxHealthHearts = 1;
        s.respawnSeconds = 0;
        s.spawnProtectionSeconds = 0;
        s.startingLives = 20;
        s.lowGravity = true;
        s.dropTimer = 0; // lightning drops disabled
    }

    private static void tenLivesNoRespawn(GameSettings s) {
        s.startingLives = 10;
        s.respawnSeconds = 0;
        s.spawnProtectionSeconds = 0;
    }

    private static void classSwitch(GameSettings s, int seconds) {
        s.randomClasses = true;
        s.randomClassCycleSeconds = seconds;
    }

    private static void setClasses(GameSettings s, ClassType... types) {
        s.enabledClasses.clear();
        for (ClassType type : types) s.enabledClasses.add(type);
    }

    private static void disableClasses(GameSettings s, ClassType... types) {
        for (ClassType type : types) s.enabledClasses.remove(type);
    }

    private static void setDrops(GameSettings s, GameLootDrops... drops) {
        s.enabledDrops.clear();
        for (GameLootDrops drop : drops) s.enabledDrops.add(drop);
    }
}
