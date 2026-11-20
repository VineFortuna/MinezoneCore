package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.mastery.MasteryHeads;
import anthony.SuperCraftBrawl.mastery.MasteryReward;
import anthony.SuperCraftBrawl.mastery.MasteryStatus;
import anthony.SuperCraftBrawl.mastery.MasteryTier;
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

public class ClassMasteryGUI implements InventoryProvider {

    public Core main;
    public ClassType type;
    public SmartInventory inv;

    private static final String[] NUMERALS = {"I", "II", "III", "IV", "V"};

    public ClassMasteryGUI(Core main, ClassType type, SmartInventory parent) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(3, 9)
                .title(main.color("&8&l" + type.name() + " Mastery"))
                .parent(parent)
                .build();

        this.main = main;
        this.type = type;
    }

    @Override
    public void init(Player player, InventoryContents contents) {

        contents.fill(ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "),
                e -> {}));

        PlayerData data = main.getDataManager().getPlayerData(player);
        ClassDetails details = data.playerClasses.get(type.getID());

        MasteryTier[] tiers = new MasteryTier[]{
                new MasteryTier(10,  new MasteryReward(10,  20,  Material.GOLD_PLATE),  1),
                new MasteryTier(25,  new MasteryReward(25,  50,  Material.GOLD_NUGGET), 2),
                new MasteryTier(50,  new MasteryReward(50,  100, Material.GOLD_INGOT),  3),
                new MasteryTier(75,  new MasteryReward(75,  150, Material.GOLD_BLOCK),  4),
                new MasteryTier(100, new MasteryReward(0,   0,   null),                 5)
        };

        // Status indicator row
        for (int i = 0; i < tiers.length; i++) {
            MasteryTier tier = tiers[i];
            MasteryStatus status = tier.getStatus(details);

            short color;
            if (status == MasteryStatus.CLAIMED) color = 5;        // lime
            else if (status == MasteryStatus.UNLOCKED) color = 4;  // yellow
            else color = 14;                                        // red

            ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, color);
            ItemHelper.setDetails(pane, " ");
            contents.set(0, i + 2, ClickableItem.empty(pane));
        }

        // Reward items
        for (int i = 0; i < tiers.length; i++) {
            MasteryTier tier = tiers[i];

            ItemStack item = (i < 4)
                    ? createRewardItem(details, tier, i)
                    : createAltHeadItem(details);

            contents.set(1, i + 2, ClickableItem.of(
                    item,
                    e -> handleRewardClick(player, data, details, tier)));
        }

        // Mastery info
        contents.set(1, 8, ClickableItem.of(
                ItemHelper.setDetails(
                        new ItemStack(Material.PAPER),
                        "&a&lMastery Points",
                        "&7When using this class:",
                        "&a- Match played: &r+1 point",
                        "&a- Match won: &r+2 points"),
                e -> {}));

        // Back button
        contents.set(2, 8, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"),
                e -> inv.getParent().get().open(player)));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private ItemStack createRewardItem(ClassDetails details, MasteryTier tier, int index) {
        MasteryReward reward = tier.getReward();
        MasteryStatus status = tier.getStatus(details);
        int played = MasteryTier.getProgress(details);

        ItemStack item = ItemHelper.setDetails(
                new ItemStack(reward.getMaterial()),
                main.color("&e&lTier " + NUMERALS[index]));

        if (status == MasteryStatus.LOCKED) {
            ItemHelper.setLore(item, Arrays.asList(
                    main.color("&7" + reward.getTokenReward() + " Tokens & " + reward.getExpReward() + " EXP"),
                    "",
                    MasteryTier.buildMasteryProgressBar(played, tier.getRequiredPoints(), 25)));

        } else if (status == MasteryStatus.CLAIMED) {
            ItemHelper.setLore(item, Arrays.asList(
                    main.color("&7" + reward.getTokenReward() + " Tokens & " + reward.getExpReward() + " EXP"),
                    "",
                    main.color("&a&lCLAIMED")));
            ItemHelper.setGlowing(item, true);

        } else {
            ItemHelper.setLore(item, Arrays.asList(
                    main.color("&7" + reward.getTokenReward() + " Tokens & " + reward.getExpReward() + " EXP"),
                    "",
                    main.color("&eClick to claim reward")));
        }

        return item;
    }

    private ItemStack createAltHeadItem(ClassDetails details) {
        int played = MasteryTier.getProgress(details);

        ItemStack head = ItemHelper.setDetails(
                MasteryHeads.headReward(type),
                main.color("&e&lTier " + NUMERALS[4]));

        if (played < 100) {
            ItemHelper.setLore(head, Arrays.asList(
                    main.color("&7Alternate Head"),
                    "",
                    MasteryTier.buildMasteryProgressBar(played, 100, 25)));

        } else if (details.reward5) {
            ItemHelper.setLore(head, Arrays.asList(
                    main.color("&7Alternate Head"),
                    "",
                    main.color("&a&lENABLED"),
                    main.color("&eClick to disable")));
            ItemHelper.setGlowing(head, true);

        } else {
            ItemHelper.setLore(head, Arrays.asList(
                    main.color("&7Alternate Head"),
                    "",
                    main.color("&eClick to enable")));
        }

        return head;
    }

    private void handleRewardClick(Player player, PlayerData data, ClassDetails details, MasteryTier tier) {
        MasteryStatus status = tier.getStatus(details);
        MasteryReward reward = tier.getReward();

        // Alt head toggle
        if (reward.getMaterial() == null) {
            if (status != MasteryStatus.LOCKED) {
                boolean enabling = !tier.isClaimed(details);
                tier.setClaimed(details, enabling);
                player.sendMessage(main.color("&2&l(!) &r"
                        + (enabling ? "Enabled" : "Disabled")
                        + " alternate head for " + type.getTag()));
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 2);
                details.hasUpdated = true;
                main.getDataManager().saveData(data);
                inv.open(player);
            }
            return;
        }

        // Token/EXP rewards
        if (status == MasteryStatus.UNLOCKED) {
            tier.claim(details);

            player.sendMessage(main.color("&d&l(!) &rYou have earned &a"
                    + reward.getTokenReward() + " Tokens & " + reward.getExpReward() + " EXP!"));

            data.tokens += reward.getTokenReward();
            data.exp += reward.getExpReward();

            main.getLevelManager().checkLevelUp(player);

            if (main.getGameManager().GetInstanceOfPlayer(player) == null)
                main.getScoreboardManager().lobbyBoard(player);

            player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 0);
            details.hasUpdated = true;
            main.getDataManager().saveData(data);
            inv.open(player);
        }
    }
}