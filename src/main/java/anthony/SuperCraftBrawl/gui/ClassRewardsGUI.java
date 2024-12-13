package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class ClassRewardsGUI implements InventoryProvider {
    
    public Core main;
    public ClassType type;
    public SmartInventory inv;
    
    public ClassRewardsGUI(Core main, ClassType type, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(3, 9)
                .title(main.color("&8&l" + type.name() + " Mastery")).parent(parent).build();
        this.main = main;
        this.type = type;
    }
    
    @Override
    public void init(Player player, InventoryContents contents) {
    
        contents.fill(ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e-> {}));
    
        PlayerData data = main.getDataManager().getPlayerData(player);
        ClassDetails details = data.playerClasses.get(type.getID());
        int played = details.gamesPlayed + details.gamesWon;
    
        /*First reward [10] 10 tokens & 20 xp
        Second reward [25] 25 tokens & 50 xp
        Third Reward [50] 50 tokens & 100 xp
        Fourth Reward [75] 75 tokens & 150 xp
        Fifth Reward [100] Alternative Head.*/
        
        ItemStack tokens1 = ItemHelper.setDetails(new ItemStack(Material.GOLD_PLATE), main.color("&e&l10 Tokens & 20 EXP"));
        if (played < 10) {
            ItemHelper.setLore(tokens1, Arrays.asList("", main.progressBar(played, 10, 25)));
        } else {
            if (details.reward1) {
                ItemHelper.setLore(tokens1, Arrays.asList("", main.color("&a&lCLAIMED")));
                ItemHelper.setGlowing(tokens1, true);
            } else {
                ItemHelper.setLore(tokens1, Arrays.asList("", main.color("&eClick to claim reward")));
            }
        }
        ItemStack tokens2 = ItemHelper.setDetails(new ItemStack(Material.GOLD_NUGGET), main.color("&e&l25 Tokens & 50 EXP"));
        if (played < 25) {
            ItemHelper.setLore(tokens2, Arrays.asList("", main.progressBar(played, 25, 25)));
        } else {
            if (details.reward2) {
                ItemHelper.setLore(tokens2, Arrays.asList("", main.color("&a&lCLAIMED")));
                ItemHelper.setGlowing(tokens2, true);
            } else {
                ItemHelper.setLore(tokens2, Arrays.asList("", main.color("&eClick to claim reward")));
            }
        }
        ItemStack tokens3 = ItemHelper.setDetails(new ItemStack(Material.GOLD_INGOT), main.color("&e&l50 Tokens & 100 EXP"));
        if (played < 50) {
            ItemHelper.setLore(tokens3, Arrays.asList("", main.progressBar(played, 50, 25)));
        } else {
            if (details.reward3) {
                ItemHelper.setLore(tokens3, Arrays.asList("", main.color("&a&lCLAIMED")));
                ItemHelper.setGlowing(tokens3, true);
            } else {
                ItemHelper.setLore(tokens3, Arrays.asList("", main.color("&eClick to claim reward")));
            }
        }
        ItemStack tokens4 = ItemHelper.setDetails(new ItemStack(Material.GOLD_BLOCK), main.color("&e&l75 Tokens & 150 EXP"));
        if (played < 75) {
            ItemHelper.setLore(tokens4, Arrays.asList("", main.progressBar(played, 75, 25)));
        } else {
            if (details.reward4) {
                ItemHelper.setLore(tokens4, Arrays.asList("", main.color("&a&lCLAIMED")));
                ItemHelper.setGlowing(tokens4, true);
            } else {
                ItemHelper.setLore(tokens4, Arrays.asList("", main.color("&eClick to claim reward")));
            }
        }
        
        ItemStack head = ItemHelper.setDetails(headReward(type), main.color("&e&lAlternate Head"));
        if (played < 100) {
            ItemHelper.setLore(head, Arrays.asList("", main.progressBar(played, 100, 25)));
        } else {
            if (details.reward5) {
                ItemHelper.setLore(head, Arrays.asList("", main.color("&a&lENABLED"), main.color("&eClick to disable")));
                ItemHelper.setGlowing(head, true);
            } else {
                ItemHelper.setLore(head, Arrays.asList("", main.color("&eClick to enable")));
            }
        }
        
        contents.set(1, 1,
                ClickableItem.of(tokens1, e -> {
                    if (played >= 10) {
                        if (!details.reward1) {
                            details.reward1 = true;
                            player.sendMessage(
                                    main.color("&d&l(!) &rYou have earned &a10 Tokens & 20 EXP!"));
                            data.tokens += 10;
                            data.exp += 20;
                            if (data.exp >= 2500) {
                                data.level++;
                                data.exp -= 2500;
                                player.sendMessage("Level upgraded to " + data.level + "!");
                            }
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                main.getScoreboardManager().lobbyBoard(player);
                            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0);
                            details.hasUpdated = true;
                            player.closeInventory();
                        }
                    }
                    }));
        contents.set(1, 2,
                ClickableItem.of(tokens2, e -> {
                    if (played >= 25) {
                        if (!details.reward2) {
                            details.reward2 = true;
                            player.sendMessage(
                                    main.color("&d&l(!) &rYou have earned &a25 Tokens & 50 EXP!"));
                            data.tokens += 25;
                            data.exp += 50;
                            if (data.exp >= 2500) {
                                data.level++;
                                data.exp -= 2500;
                                player.sendMessage("Level upgraded to " + data.level + "!");
                            }
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                main.getScoreboardManager().lobbyBoard(player);
                            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0);
                            details.hasUpdated = true;
                            player.closeInventory();
                        }
                    }
                }));
        contents.set(1, 3,
                ClickableItem.of(tokens3, e -> {
                    if (played >= 50) {
                        if (!details.reward3) {
                            details.reward3 = true;
                            player.sendMessage(
                                    main.color("&d&l(!) &rYou have earned &a50 Tokens & 100 EXP!"));
                            data.tokens += 50;
                            data.exp += 100;
                            if (data.exp >= 2500) {
                                data.level++;
                                data.exp -= 2500;
                                player.sendMessage("Level upgraded to " + data.level + "!");
                            }
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                main.getScoreboardManager().lobbyBoard(player);
                            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0);
                            details.hasUpdated = true;
                            player.closeInventory();
                        }
                    }
                }));
        contents.set(1, 4,
                ClickableItem.of(tokens4, e -> {
                    if (played >= 75) {
                        if (!details.reward4) {
                            details.reward4 = true;
                            player.sendMessage(
                                    main.color("&d&l(!) &rYou have earned &a75 Tokens & 150 EXP!"));
                            data.tokens += 75;
                            data.exp += 150;
                            if (data.exp >= 2500) {
                                data.level++;
                                data.exp -= 2500;
                                player.sendMessage("Level upgraded to " + data.level + "!");
                            }
                            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                                main.getScoreboardManager().lobbyBoard(player);
                            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0);
                            details.hasUpdated = true;
                            player.closeInventory();
                        }
                    }
                }));
        contents.set(1, 5,
                ClickableItem.of(head, e -> {
                    if (played >= 100) {
                        if (!details.reward5) {
                            details.reward5 = true;
                            player.sendMessage(
                                    main.color("&2&l(!) &rEnabled alternate head for " + type.getTag()));
                        } else {
                            details.reward5 = false;
                            player.sendMessage(
                                    main.color("&2&l(!) &rDisabled alternate head for " + type.getTag()));
                        }
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 2);
                        details.hasUpdated = true;
                        player.closeInventory();
                    }
                }));
        contents.set(1, 7, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.PAPER), "&aWhen using this class:",
                        "&a- Match played: +1 point", "&a- Match won: +1 point"), e -> {
                }));
        contents.set(2, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                    inv.getParent().get().open(player);
                }));
    }
    
    @Override
    public void update(Player player, InventoryContents contents) {
    
    }
    
    public static ItemStack headReward(ClassType type) {
        switch (type) {
            case Cactus:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTA0ZjFhNTU5NDNjNTk0ZTcxMTllODg0YzVkYTJhMmJjYThlN2U2NTE2YTA2NDlhYTdlNTU2NThlMGU5In19fQ==");
            case Fade:
                return new ItemStack(Material.STAINED_GLASS, 1, (short) ItemHelper.StainedClayColor.BLACK.getColorCode());
            case Cloud:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDVjMWU3YTFlMzMxZDIyYjk0YzUzMzY1MTc0NzY2YzdjY2EwNzgzYjhiNDZjY2UwNmMwYjE3MjQ0YjMyOGQ0MiJ9fX0=");
            case Firework:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODNkNDlkZGU3NWUxMmI2MGViZTZlODk4MWVhNGZiMjY2YjIwNzUyYzJmNTVlOTZhZjExM2MyODdlZWQ2M2U4MSJ9fX0=");
            case Shulker:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTVhMmZiYTUyNjg0ZDFmNzJkODMwZjc0ZDA4N2RmNzJmMjZiNWE2NmEwMzI1ZmZiODA3ZWY0OTQwOTMzM2VhMCJ9fX0=");
            case Dweller:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2JlM2NhMzc5MDAzNTRiODFiMjU5MWI4ZTljYzcwNmMyYTQ1MGZlZTM3MGVlNTIyZTZiOWYzMmRjMDM2Y2E4MCJ9fX0=");
            case WitherSk:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjhjMDE2NWU5YjJkYmQ3OGRhYzkxMjc3ZTk3ZDlhMDI2NDhmMzA1OWUxMjZhNTk0MWE4NGQwNTQyOWNlIn19fQ==");
            case Rabbit:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2M0MzQ5ZmU5OTAyZGQ3NmMxMzYxZjhkNmExZjc5YmZmNmY0MzNmM2I3YjE4YTQ3MDU4ZjBhYTE2YjkwNTNmIn19fQ==");
            case FlintAndSteel:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTAwZjYwNzI4NDZhMjhmOWNkZDE5YmIwY2E2MTQyMDljZWI1MWZiYzI4Mzc2ZmM4ZTU4M2JkMThjYzk1N2ZkOSJ9fX0=");
            case Hunter:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTMwMGJiNThlZjFhYjZjYzljMGNmNWUzZDYwZTJmZWJiZTZjNDMxZTNkNmJmM2M2ZGIzY2Y4MjQ3OTFmZjkxNCJ9fX0=");
            case Jeb:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDNhMmYzN2Y3YjBmMjY2MzljNmYzZGMxZTI3YjI0NGM0NzAzNzk3NjY3NjRlZmM3MTQzNjk3YThlMTViNiJ9fX0=");
            case Bee:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDUzYzZhODRiNWE1NGMxMDIyMTAyNzgwZTVkNTJiYWQ2NmZkNDJmYzY2NGY2ZGFjOThlOTQxOTY2OTdiOSJ9fX0=");
            case Ice:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTZhYWI1OGZhMDFmY2U5YWY0NjllZDc0N2FlZDgxMWQ3YmExOGM0NzZmNWE3ZjkwODhlMTI5YzMxYjQ1ZjMifX19");
            case Vampire:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjVhNzAwNzAwN2Q1YTM5NmQ2MDQ5YzcxYWI2ZmY1ZmVkYjZjYTNlMTc1M2IzZmQ2ZjEzYmI2OTQ2YTdlMGRhZiJ9fX0=");
            case ZombiePigman:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2VhYmFlY2M1ZmFlNWE4YTQ5Yzg4NjNmZjQ4MzFhYWEyODQxOThmMWEyMzk4ODkwYzc2NWUwYThkZTE4ZGE4YyJ9fX0=");
            case Villager:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTVhMGIwN2UzNmVhZmRlY2YwNTljOGNiMTM0YTdiZjBhMTY3ZjkwMDk2NmYxMDk5MjUyZDkwMzI3NjQ2MWNjZSJ9fX0=");
            case DarkSethBling:
                return new ItemStack(Material.COAL_BLOCK);
            case ZombieVillager:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTQwMzVhY2EyNmJlOTdiZTg0MDY0MDZmMTU1N2ZhOTkwNzM4NzcwZmUwMzgxOTRhNGFiODFjZTBjODM5NmM3MiJ9fX0=");
            case MagmaCube:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAyNzgzNDhlNjU3YjlkN2ExNGM4MjQ5ZmNlZjFiNGI5YmQ0ZTQ4YTY1OThkYzU2NDRhNzAxYmQ0OGI0MjcxMSJ9fX0=");
            case Summoner:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2FmZmNlNjVmNDIzNjY0N2VjZDZkNTVjMTI3ZWVmOGU3ZTEzMmY0M2QwMTQzMmFlYTM2MGYwYjY3YzhhNjY3NyJ9fX0=");
            case Anvil:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNmN2FmNTQ4ZGNhNmEyYTk0MmVkNzI2NDBkZDgwZTUwMGY4MzI5OGY4OWMzMWUzYWI0YTVmNmNlMjBlMmY0ZCJ9fX0=");
            case Silverfish:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWJiYTczM2M5YmU5MmJlYmVmNTBhM2FkZDhiMmE0OTc5MTdkMTliM2MyYWVlZGYzNmEzNDNkMDNjYTE2M2M2YyJ9fX0=");
            case Zombie:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY5Yjk3MzRkMGU3YmYwNjBmZWRjNmJmN2ZlYzY0ZTFmN2FkNmZjODBiMGZkODQ0MWFkMGM3NTA4Yzg1MGQ3MyJ9fX0=");
            case Star:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQwMWM1MWEyODI0ODZiYTBiNWZiYzZjZWU4ZDlkNThiMzk1MjBjNWM0MzkzNTc1Mjk5OTUzYzI3M2JhMGY3MCJ9fX0=");
            case Wizard:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2JmYzE3ZWQ5MjhhZGZhZmZmYmY5ZjkxNTg5ZjBkNWI3YWIyMTZmNzRjMGQ3MjE0ZjI5ZTY5NDM4ZTYwOTdiMCJ9fX0=");
            case Present:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI5MTljNjczMTdjNzY3ODQzOGZmNTIwYzk4ZGRlMGUzYjRkNjg3NjljODkzOGE1YTNkZTI5NjhlZGZjNzMxNCJ9fX0=");
            case Noteblock:
                return new ItemStack(Material.JUKEBOX);
            case Bedrock:
                return new ItemStack(Material.BEDROCK);
            case EnchantTable:
                return new ItemStack(Material.BOOKSHELF);
            case Skeleton:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUzOTFjNmU1MzVmN2FhNWEyYjZlZTZkMTM3ZjU5ZjJkN2M2MGRlZjg4ODUzYmE2MTFjZWIyZDE2YTdlN2M3MyJ9fX0=");
            case Enderman:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmJhNjkzNzJhMDk0MGRkZjE0N2U0NjM5ODc5NDU1MDViMWJlOTcwOGE1MzM4OTRlNGY1Mjk3ODg0MmEyY2Q1NSJ9fX0=");
            case Horse:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzk5YmI1MGQxYTIxNGMzOTQ5MTdlMjViYjNmMmUyMDY5OGJmOThjYTcwM2U0Y2MwOGI0MjQ2MmRmMzA5ZDZlNiJ9fX0=");
            case Squid:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTczMjdlZTExODEyYjc2NGM3YWRlNzBiMjgyY2NlNGM1OGU2MzViMjAxNTI0NDA4MWQxNDkwNTQzZGE3MjgwZSJ9fX0=");
            case Spider:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWVjNTU3NDYwM2YzMDQ4ZjIxYWQ1YTNjOTRkOTcxMTU3MDYwMTFmZTZiYTY3NzgxMDkxYjhhOWFjMTBhZjU0ZiJ9fX0=");
            case Pig:
                return ItemHelper.createSkullTexture("e3RleHR1cmVzOntTS0lOOnt1cmw6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNhNTM4Zjc4NzA0OGRiYTI3ZGNkYmJjYjcyZDJmNTc4Zjg1NzczMTY4ZDcyNDY2MjY2ZTc1NWY0NzFjODkifX19");
            case Blaze:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAyNzcwNGZmMDFiZTk0NzI5YTJkYTAyNjIyYTM4Y2ZiOWU4Yjg1ZWRkZjMyMmZmZDY4OWFlODA1ZWUxMWM3ZCJ9fX0=");
            case Wither:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTE2OWM5MGM4ODc0YWI1NzViMjAxYjYxNmE2OWVhYzdlMGI1YWM2OWJiY2NjYmIyNzcyZTM2Nzc2ZmU2OTQ0MSJ9fX0=");
            case Creeper:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzAyMTMwMzFlNjdlZTRiNjJhMGQ5MzljZDIyZjliMmQ4Zjg0NDIyMDRhYzM1ZGIwMzA4OThlZDk4MzZkMDNkOSJ9fX0=");
            case IronGolem:
                return new ItemStack(Material.IRON_BLOCK);
            case Ghast:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzhmNzdlZWVlZjZmZmIyZjY4MThlNTc2OTg3OTRhZTAzNTFhYjMyYmEyMzRkNjIxYzIyZmU0Y2U4ZTE1OTlkMiJ9fX0=");
            case Slime:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjU0ZjJiNGNiNmQyMWQzNTZlYzVjMGNiNmY1MTY2ZmVlMzExOWM3ZGM1OWUyMDgzOWMyMDMzMWNkMTNlNDM5ZCJ9fX0=");
            case ButterGolem:
                return new ItemStack(Material.GOLD_BLOCK);
            case Enderdragon:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWVjM2ZmNTYzMjkwYjEzZmYzYmNjMzY4OThhZjdlYWE5ODhiNmNjMThkYzI1NDE0N2Y1ODM3NGFmZTliMjFiOSJ9fX0=");
            case Bat:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDczYWY2OWVkOWJmNjdlMmY1NDAzZGQ3ZDI4YmJlMzIwMzQ3NDliYmZiNjM1YWMxNzg5YTQxMjA1M2NkY2JmMCJ9fX0=");
            case SethBling:
                return new ItemStack(Material.REDSTONE_BLOCK);
            case Melon:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWY5YzJlOWUwYThmNTJhYjg1NDdkYTZlMmE2ODg4NTUyZmE0ZTFkZWEyNDM3Y2ZmNjViMDdhMTg5N2NmYmI2OCJ9fX0=");
            case BabyCow:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGM0YjVmNmQ3NTEyNjM4MGY1MjBhNjdjYTU3YmM5YTU2YWExMWRiOGFmZTdlNWRjYjJhNTJkZmNmZWFlMDc4NSJ9fX0=");
            case Herobrine:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWI3OGFiM2EzODhhNjliNTFhNjI1YjA2MGI3YzY3ZjQ3ZTczM2IyMTliMzMxMjE2YTY3OTFhZDZhYTU3YThmZCJ9fX0=");
            case Ninja:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjg1MzIxNWU0ZTM5NDZmMjAwNmRmYmVhNjFjOTIzY2U3MzQyYjEzZjIzZmE3ZjM1ZjJhNDBlODQ1M2VhYzdlNSJ9fX0=");
            case TNT:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQzNzUzY2QxZjRjNDkyM2ZlOWM3ZmZiNWZjZTExNGYzYWI5ODBjNzU2NDQ2NDYxZDNiNDczMzIyZGE4ZDE0YiJ9fX0=");
            case Chicken:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjg2YzcxYjMxMGVjZmQ1Y2E4MGNmMTM2NGJhYzdmMjUxOWM3MjYwZjI5M2E4OGVkNzY2MzA0ZDAzOWY2YmU4NSJ9fX0=");
            case Witch:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjEwMDVlY2I2OGIzZGY3MThlNzQwYjk5NGE2M2I4ODM2NDk0YTQ5OTJkMzYzZWEzODAxYzM5YjZhZTM2N2M2OCJ9fX0=");
            case Sheep:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzAzMDcxODY1NmNhOGJlZGIxNWQ3MzA3YWJmMThkN2ViOGJjY2UxMGFlMDZhZGVmY2MyNjRmZjRlNDEyY2M0YiJ9fX0=");
            case SnowGolem:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWZkZmQxZjc1MzhjMDQwMjU4YmU3YTkxNDQ2ZGE4OWVkODQ1Y2M1ZWY3MjhlYjVlNjkwNTQzMzc4ZmNmNCJ9fX0=");
            case Bunny:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzYwZDM3YWQyOWE0MDhmOGNkMmJhYWM1MTBlNTQwOGNiN2I3MTVkMjVkZTYxMzM0MmExY2I1YzNkMTA1ZTM0ZiJ9fX0=");
            case ButterBro:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NlYzI3MTI1NGFjM2JlODQxMzM3M2JhZDdjMjUyYTE3YTU0Mzc4ZDNjODgwNWIwY2RjN2MxMmQ0ODg2N2QxNyJ9fX0=");
            case Steve:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZhY2QwNmU4NDgzYjE3NmU4ZWEzOWZjMTJmZTEwNWViM2EyYTQ5NzBmNTEwMDA1N2U5ZDg0ZDRiNjBiZGZhNyJ9fX0=");
            case Notch:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTNmYmU0YTZjNTk0ZDgxYTMwNzM5MjE4N2ZhMThlMjQ1ZjQwODZlZjRkYmUzYzE1NzEzZjBhNDk0ZjU2ODg2NSJ9fX0=");
            case Potato:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzcwZmIzNzdkMmI1MDEwN2Y1NDZjMjc5YTIyODNjZTJkMDY3ZTQ1NGNhOWZhYTEzMTU5YTgzNmQ0N2MzZDAyYiJ9fX0=");
            case Ocelot:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTYwMzllMzYxYmYwZjEyNTRiYmIzOTFjMTYzYTYwMjUwMzBmY2RmOThjMjA3MDVkY2E4NWY1NDQwNWRmMDRiZSJ9fX0=");
            case LargeFern:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjEzYTY4MzJjMTdjZWJiZDM1ODc2NjQwM2ZmM2NmZTAzMjJiNzBmNTQwZmQ3MzBjMTUyMGRiNjUwOThkZjRkZSJ9fX0=");
            case Vindicator:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjE5Y2M3ZGNhYzg2MzQzMzZjYzQwN2M1NzEyOTNmZWVmYWZjYTBlMWVmZDVlMmM4ZjVkMjIyNjdhNzI5ZDAwMyJ9fX0=");
            case Fisherman:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjlhYzgwNGEyYzVhOGVhNTdlZjY5NjU3YWI2NDM0N2QxZWQzNmIzNGNhNzBhMjE4ZjZhNjNkNWI2YWEyZmU5ZiJ9fX0=");
            case BrewingStand:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNhNzI4YWQ4ZDMxNDg2YTdmOWFhZDIwMGVkYjM3M2VhODAzZDFmYzVmZDQzMjFiMmUyYTk3MTM0ODIzNDQ0MyJ9fX0=");
            case Endermite:
                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTczMDEyN2UzYWM3Njc3MTIyNDIyZGYwMDI4ZDllNzM2OGJkMTU3NzM4YzhjM2NkZGVjYzUwMmU4OTZiZTAxYyJ9fX0=");
//            case Wolf:
//                return ItemHelper.createSkullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjBjN2Y4ODUzMjZiYTA5NDljMzE2Njk2ZDE5ZDUzMDgyYjk5NGU5YjQ4Y2FkNjY3MzU1OGRkNmM1YmNhYjQ5In19fQ==");
        }
        return ItemHelper.create(Material.SKULL_ITEM);
    }
}
