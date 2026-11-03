package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import fr.minuskube.inv.content.SlotPos;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * One For All's Voting Paper: only the classes (and Random) that have votes, most voted first. Each one's stack size
 * is its votes, and clicking one votes for it like in the class selector.
 */
public class OneForAllVotesGUI implements InventoryProvider {

    private static final int ROWS = OneForAllClassGUI.ROWS;

    private final Core main;
    private final GameInstance game;
    public final SmartInventory inv;

    public OneForAllVotesGUI(Core main, GameInstance game) {
        this.main = main;
        this.game = game;

        this.inv = SmartInventory.builder()
                .id("oneForAllVotes")
                .provider(this)
                .size(ROWS, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Class Votes")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        final Map<ClassType, Integer> votes = game.getOneForAllVoteCounts();

        // Most votes first, then by name (Random goes first among a tie)
        List<ClassType> voted = new ArrayList<>(votes.keySet());
        voted.sort((a, b) -> {
            int byVotes = Integer.compare(votes.get(b), votes.get(a));
            if (byVotes != 0) return byVotes;
            if (a == null || b == null) return a == null ? -1 : 1;
            return a.toString().compareTo(b.toString());
        });

        int total = 0;
        ClickableItem[] items = new ClickableItem[voted.size()];
        for (int i = 0; i < voted.size(); i++) {
            ClassType type = voted.get(i);
            items[i] = OneForAllClassGUI.getOptionItem(game, player, type, votes.get(type));
            total += votes.get(type);
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(28);

        contents.fillBorders(ClickableItem.of(ItemHelper.getGlassFiller(), e -> {}));
        contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.PAPER),
                "&e&lClass Votes",
                "&7Everyone plays the class",
                "&7with the most votes",
                "",
                "&fVotes: &a" + total + "&7/&a" + game.players.size(),
                "",
                "&7Vote with the class selector",
                "",
                "&eClick to open the class selector"), e -> {
            SoundManager.playClickSound(player);
            new OneForAllClassGUI(main, game).inv.open(player);
        }));

        if (voted.isEmpty()) {
            contents.set(2, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER),
                    "&cNo Votes Yet",
                    "&7Vote for a class with",
                    "&7the class selector"), e -> {}));
        }

        // Previous / Next Page
        if (!pagination.isFirst()) {
            contents.set(2, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.previous().getPage());
            }));
        }
        if (!pagination.isLast()) {
            contents.set(2, 8, ClickableItem.of(ItemHelper.getNextPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.next().getPage());
            }));
        }

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1));
        iterator.allowOverride(false);
        pagination.addToIterator(iterator);
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
