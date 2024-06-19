package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.InventoryListener;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ClassRewardsGUI implements InventoryProvider {
    
    public Core main;
    public ClassType type;
    public SmartInventory inv;
    
    public ClassRewardsGUI(Core main, ClassType type, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(1, 9)
                .title(String.valueOf(ChatColor.DARK_GRAY) + ChatColor.BOLD + "Class Rewards").parent(parent).build();
        this.main = main;
        this.type = type;
    }
    
    @Override
    public void init(Player player, InventoryContents contents) {
    
        PlayerData data = main.getDataManager().getPlayerData(player);
        ClassDetails details = data.playerClasses.get(type.getID());
        
        ItemStack tokens = ItemHelper.setDetails(new ItemStack(Material.EMERALD), main.color("&e&l100 Tokens"));
        if (details.gamesPlayed < 50) {
            ItemHelper.setLore(tokens, Arrays.asList("", main.progressBar(details.gamesPlayed, 50, 25)));
        } else {
            if (details.reward1) {
                ItemHelper.setLore(tokens, Arrays.asList("", main.color("&a&lREWARD CLAIMED")));
                ItemHelper.setGlowing(tokens, true);
            } else {
                ItemHelper.setLore(tokens, Arrays.asList("", main.color("&eClick to claim reward")));
            }
        }
    
        ItemStack head = ItemHelper.setDetails(headReward(type), main.color("&e&lAlternate Head"));
        if (details.gamesPlayed < 100) {
            ItemHelper.setLore(head , Arrays.asList("", main.progressBar(details.gamesPlayed, 100, 25)));
        } else {
            if (details.reward1) {
                ItemHelper.setLore(head , Arrays.asList("", main.color("&a&lREWARD CLAIMED")));
                ItemHelper.setGlowing(head , true);
            } else {
                ItemHelper.setLore(head , Arrays.asList("", main.color("&eClick to claim reward")));
            }
        }
        
        contents.set(0, 0,
                ClickableItem.of(tokens, e -> {
                        }));
        contents.set(0, 1,
                ClickableItem.of(head, e -> {
                        }));
        contents.set(0, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }));
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
    
    public ItemStack headReward(ClassType type) {
        switch (type) {
            case Cactus:
                return new ItemStack(Material.CACTUS);
            case Fade:
                return new ItemStack(Material.STRING);
            case Cloud:
                return new ItemStack(Material.WOOL);
            case Firework:
                return new ItemStack(Material.FIREWORK);
            case Shulker:
                return new ItemStack(Material.STAINED_CLAY, 1, (byte) DyeColor.PURPLE.getData());
            case Dweller:
                return new ItemStack(Material.BONE);
            case WitherSk:
                return ItemHelper.createSkullHead(1, SkullType.WITHER);
            case Rabbit:
                return new ItemStack(Material.RABBIT_FOOT);
            case FlintAndSteel:
                return new ItemStack(Material.FLINT_AND_STEEL);
            case Hunter:
                return new ItemStack(Material.GOLD_SWORD);
            case Jeb:
                return new ItemStack(Material.STONE);
            case Bee:
                return new ItemStack(Material.GLOWSTONE_DUST);
            case Ice:
                return new ItemStack(Material.ICE);
            case Vampire:
                return new ItemStack(Material.GHAST_TEAR);
            case ZombiePigman:
                return ItemHelper.createSkullTexture(
                        "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY5OGEzY2ZkZjhjMTNlZTY2MzQxNDBmOTQ1YjcxZDJlNDg4ZmY0ODVlMTBjMzNhZTI1ODIxZDgyZDg0OGE3MyJ9fX0=");
            case Villager:
                return new ItemStack(Material.EMERALD);
            case DarkSethBling:
                return new ItemStack(Material.COAL_BLOCK);
            case ZombieVillager:
                return new ItemStack(Material.ROTTEN_FLESH);
            case MagmaCube:
                return ItemHelper.createSkullTexture(
                        "e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGFhNmM0NWIyY2Y3OTc1Yjk1ZmJjY2U0ZWQ5YjA2NDZhYzAwY2I5Y2M5ZjY2ZGM1YzI0ZTgxZDJjOTFlZTdjMSJ9fX0=");
            case Summoner:
                return new ItemStack(Material.ENCHANTED_BOOK);
            case Anvil:
                return new ItemStack(Material.ANVIL);
            case Silverfish:
                return new ItemStack(Material.IRON_HOE);
            case Zombie:
                return ItemHelper.createSkullHead(1, SkullType.ZOMBIE);
            case Star:
                return new ItemStack(Material.NETHER_STAR);
            case Wizard:
                return new ItemStack(Material.BLAZE_POWDER);
            case Present:
                return new ItemStack(Material.CHEST);
            case Noteblock:
                return new ItemStack(Material.NOTE_BLOCK);
            case Bedrock:
                return new ItemStack(Material.BEDROCK);
            case EnchantTable:
                return new ItemStack(Material.ENCHANTMENT_TABLE);
            case Skeleton:
                return new ItemStack(Material.SKULL_ITEM);
            case Enderman:
                return new ItemStack(Material.ENDER_PEARL);
            case Horse:
                return new ItemStack(Material.LEASH);
            case Squid:
                return new ItemStack(Material.INK_SACK);
            case Spider:
                return new ItemStack(Material.SPIDER_EYE);
            case Pig:
                return new ItemStack(Material.PORK);
            case Blaze:
                return new ItemStack(Material.BLAZE_ROD);
            case Wither:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODg2ZGMwY2ZjYWVlY2ZlMWFiNjkxNDZlNGQ0ZjExOTA4MzcwNzZhNjdkZWMxMzVmYWJkYTYyNzFmMzc1ZDAxZiJ9fX0=");
            case Creeper:
                return ItemHelper.createSkullHead(1, SkullType.CREEPER);
            case IronGolem:
                return new ItemStack(new ItemStack(Material.IRON_BLOCK));
            case Ghast:
                return new ItemStack(new ItemStack(Material.GHAST_TEAR));
            case Slime:
                return new ItemStack(new ItemStack(Material.SLIME_BALL));
            case ButterGolem:
                return new ItemStack(new ItemStack(Material.GOLD_AXE));
            case Enderdragon:
                return new ItemStack(new ItemStack(Material.DRAGON_EGG));
            case Bat:
                return new ItemStack(new ItemStack(Material.SHEARS));
            case SethBling:
                return new ItemStack(new ItemStack(Material.REDSTONE_BLOCK));
            case Melon:
                return new ItemStack(new ItemStack(Material.MELON));
            case BabyCow:
                return new ItemStack(new ItemStack(Material.RED_MUSHROOM));
            case Herobrine:
                return new ItemStack(new ItemStack(Material.DIAMOND));
            case Ninja:
                return new ItemStack(Material.STICK);
            case TNT:
                return new ItemStack(Material.TNT);
            case Chicken:
                return new ItemStack(Material.EGG);
            case Witch:
                return new ItemStack(Material.WHEAT);
            case Sheep:
                return new ItemStack(Material.WOOL);
            case SnowGolem:
                return new ItemStack(Material.SNOW_BALL);
            case Bunny:
                return new ItemStack(Material.GOLDEN_CARROT);
            case ButterBro:
                return new ItemStack(Material.GOLD_INGOT);
            case Steve:
                return new ItemStack(Material.STONE_PICKAXE);
            case Notch:
                return new ItemStack(Material.GRASS);
            case Potato:
                return new ItemStack(Material.POTATO_ITEM);
            case Ocelot:
                return new ItemStack(Material.RAW_FISH);
            case LargeFern:
                return new ItemStack(Material.DOUBLE_PLANT, 1, (short) 3);
            case Vindicator:
                return new ItemStack(Material.IRON_AXE);
            case Wolf:
                return new ItemStack(Material.BONE);
        }
        return ItemHelper.create(Material.SKULL_ITEM);
    }
}
